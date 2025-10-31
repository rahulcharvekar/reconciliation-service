package com.example.paymentreconciliation.service;

import com.shared.utilities.logger.LoggerFactoryProvider;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

/**
 * VAN Ingestion Service
 * Processes VAN CSV files.
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paymentreconciliation.config.VANIngestionProperties;

import com.example.paymentreconciliation.entity.*;
import com.example.paymentreconciliation.repository.*;

@Service
public class VANIngestionService extends BaseIngestionService {
    private static final Logger log = LoggerFactoryProvider.getLogger(VANIngestionService.class);
    @Autowired
    private ImportRunRepository importRunRepository;
    @Autowired
    private VANTransactionRepository vanTransactionRepository;
    @Autowired
    private ImportErrorRepository importErrorRepository;

    @Autowired
    private VANIngestionProperties vanProps;

    @Override
    protected String getInboxDir() {
        return vanProps.getInboxDir();
    }

    @Override
    protected String getProcessingDir() {
        return vanProps.getProcessingDir();
    }

    @Override
    protected String getArchiveDir() {
        return vanProps.getArchiveDir();
    }

    @Override
    protected String getQuarantineDir() {
        return vanProps.getQuarantineDir();
    }

    @Override
    protected String getFileExtension() {
        return ".csv";
    }

    /**
     * Main entry point for polling and processing files.
     */
    @Override
    public void pollAndProcessInbox() {
        log.info("Polling VAN inbox directory: {}", vanProps.getInboxDir());
        List<File> files = discoverStableFiles(vanProps.getInboxDir());
        log.info("Discovered {} stable file(s) for ingestion", files.size());
        for (File file : files) {
            try {
                log.info("Processing file: {}", file.getAbsolutePath());
                processFile(file);
            } catch (Exception e) {
                log.error("Error processing file: {}. Moving to quarantine. Error: {}", file.getAbsolutePath(), e.getMessage(), e);
                moveToQuarantine(file, "Unhandled error: " + e.getMessage());
            }
        }
    }

    /**
     * Process a single file: move, hash, parse, validate, persist, archive/quarantine.
     */
    @Override
    protected void processFile(File file) {
        log.info("Starting processing for file: {}", file.getAbsolutePath());
        File processingFile = moveToProcessing(file);
        log.debug("Moved file to processing: {}", processingFile.getAbsolutePath());
        if (processingFile.length() > MAX_FILE_SIZE_BYTES) {
            log.warn("File exceeds max size policy ({} bytes): {}", processingFile.length(), processingFile.getAbsolutePath());
            moveToQuarantine(processingFile, "File exceeds max size policy");
            return;
        }

        String fileHash = computeSha256(processingFile);
        log.debug("Computed SHA-256 hash for file {}: {}", processingFile.getName(), fileHash);
        long fileSize = processingFile.length();
        if (isDuplicate(fileHash)) {
            log.warn("Duplicate file detected: {} (hash={})", processingFile.getName(), fileHash);
            moveToArchive(processingFile);
            return;
        }

        ImportRun importRun = createImportRun(processingFile.getName(), fileHash, fileSize);

        try {
            log.info("Parsing and persisting VAN file: {}", processingFile.getAbsolutePath());
            parseValidatePersist(processingFile, importRun);
            log.info("Successfully processed file: {}. Moving to archive.", processingFile.getAbsolutePath());
            moveToArchive(processingFile);
        } catch (Exception e) {
            log.warn("Processing failed for file: {}. Moving to quarantine.", processingFile.getAbsolutePath(), e);
            importRun.setStatus(ImportRun.Status.FAILED);
            importRun.setErrorMessage(e.getMessage());
            importRunRepository.save(importRun);
            persistImportError(importRun, "UNHANDLED", "Unhandled error during VAN ingest: " + e.getMessage(), null);
            moveToQuarantine(processingFile, "One or more statements failed to import");
        }
    }

    /**
     * Check DB for existing file hash.
     */
    private boolean isDuplicate(String fileHash) {
        log.debug("Checking for duplicate file hash: {}", fileHash);
        return importRunRepository.findByFileHash(fileHash).isPresent();
    }

    private ImportRun createImportRun(String filename, String fileHash, long fileSize) {
        ImportRun importRun = new ImportRun();
        importRun.setFilename(filename);
        importRun.setFileHash(fileHash);
        importRun.setFileSizeBytes(fileSize);
        importRun.setReceivedAt(java.time.LocalDateTime.now());
        importRun.setFileType("VAN");
        importRun.setStatus(ImportRun.Status.NEW);
        return importRunRepository.save(importRun);
    }

    /**
     * Parse, validate, and persist all transactions in a DB transaction.
     */
    private void parseValidatePersist(File csvFile, ImportRun importRun) {
        log.debug("Parsing and validating VAN file: {}", csvFile.getAbsolutePath());
        VANParser parser = new VANParser();
        List<VANParser.VANTransactionData> transactions;
        try {
            transactions = parser.parse(csvFile);
        } catch (VANParser.VANParseException e) {
            throw new RuntimeException("VAN parse error: " + e.getMessage(), e);
        }
        persistParsedTransactions(transactions, csvFile.getName(), importRun);
    }

    @Transactional
    public void persistParsedTransactions(List<VANParser.VANTransactionData> transactions, String filename, ImportRun importRun) {
        log.info("Persisting parsed transactions for file: {} (hash={})", filename, importRun.getFileHash());
        importRun.setStatus(ImportRun.Status.PARSED);
        importRun.setErrorMessage(null);
        importRunRepository.save(importRun);

        int totalRecords = transactions.size();
        int processedRecords = 0;
        int failedRecords = 0;

        for (VANParser.VANTransactionData txn : transactions) {
            // Basic validation
            if (txn.mainAccountNumber == null || txn.mainAccountNumber.trim().isEmpty()) {
                log.error("Missing main account number. Skipping transaction: {}", txn);
                persistImportError(importRun, "VALIDATION", "Missing main account number", null);
                failedRecords++;
                continue;
            }
            if (txn.virtualAccountNumber == null || txn.virtualAccountNumber.trim().isEmpty()) {
                log.error("Missing virtual account number. Skipping transaction: {}", txn);
                persistImportError(importRun, "VALIDATION", "Missing virtual account number", null);
                failedRecords++;
                continue;
            }
            if (txn.amount == null || txn.amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                log.error("Invalid amount. Skipping transaction: {}", txn);
                persistImportError(importRun, "VALIDATION", "Invalid amount", null);
                failedRecords++;
                continue;
            }

            // Persist transaction
            processedRecords++;
            VANTransaction vanTxn = new VANTransaction();
            vanTxn.setImportRun(importRun);
            vanTxn.setMainAccountNumber(txn.mainAccountNumber);
            vanTxn.setVirtualAccountNumber(txn.virtualAccountNumber);
            vanTxn.setTransactionReferenceNumber(txn.transactionReferenceNumber);
            vanTxn.setBankReferenceTraceId(txn.bankReferenceTraceId);
            vanTxn.setRemitterName(txn.remitterName);
            vanTxn.setRemitterAccountNumber(txn.remitterAccountNumber);
            vanTxn.setRemitterIfscBankName(txn.remitterIfscBankName);
            vanTxn.setRemitterVpa(txn.remitterVpa);
            vanTxn.setTransactionDate(txn.transactionDate);
            vanTxn.setValueDate(txn.valueDate);
            vanTxn.setAmount(txn.amount);
            vanTxn.setModeChannel(txn.modeChannel);
            vanTxn.setPaymentDescriptionNarration(txn.paymentDescriptionNarration);
            vanTxn.setPaymentStatus(txn.paymentStatus);
            vanTxn.setMappedCustomerIdCode(txn.mappedCustomerIdCode);
            vanTxn.setInvoiceReferenceId(txn.invoiceReferenceId);
            vanTxn.setDateTimeOfCredit(txn.dateTimeOfCredit);
            vanTxn.setBranchBankCode(txn.branchBankCode);
            vanTxn.setCreatedAt(java.time.LocalDateTime.now());
            vanTransactionRepository.save(vanTxn);
        }

        // Update ImportRun with counts and final status
        importRun.setTotalRecords(totalRecords);
        importRun.setProcessedRecords(processedRecords);
        importRun.setFailedRecords(failedRecords);
        if (processedRecords > 0 && failedRecords == 0) {
            importRun.setStatus(ImportRun.Status.IMPORTED);
        } else if (processedRecords > 0) {
            importRun.setStatus(ImportRun.Status.PARTIAL);
        } else {
            importRun.setStatus(ImportRun.Status.FAILED);
        }
        importRunRepository.save(importRun);
    }

    /**
     * Persist import error details.
     */
    private void persistImportError(ImportRun importRun, String code, String errorMsg, Integer lineNo) {
        if (importRun == null) {
            log.warn("Unable to persist import error because import run is not available: {}", errorMsg);
            return;
        }

        ImportError error = new ImportError();
        error.setImportRun(importRun);
        error.setCode(code != null ? code : "VALIDATION_ERROR");
        error.setMessage(errorMsg);
        error.setLineNo(lineNo);
        importErrorRepository.save(error);
    }
}

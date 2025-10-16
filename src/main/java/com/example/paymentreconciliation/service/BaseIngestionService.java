package com.example.paymentreconciliation.service;

import com.shared.utilities.logger.LoggerFactoryProvider;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Base Ingestion Service with common file processing logic.
 */
public abstract class BaseIngestionService {
    private static final Logger log = LoggerFactoryProvider.getLogger(BaseIngestionService.class);

    protected static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    protected static final int FILE_STABILITY_WINDOW_SEC = 10;

    protected abstract String getInboxDir();
    protected abstract String getProcessingDir();
    protected abstract String getArchiveDir();
    protected abstract String getQuarantineDir();
    protected abstract String getFileExtension();
    protected abstract void processFile(File file);

    /**
     * Main entry point for polling and processing files.
     */
    public void pollAndProcessInbox() {
        log.info("Polling inbox directory: {}", getInboxDir());
        List<File> files = discoverStableFiles(getInboxDir());
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
     * Discover files that are stable (not changing) for at least FILE_STABILITY_WINDOW_SEC.
     */
    protected List<File> discoverStableFiles(String inboxDir) {
        log.debug("Discovering stable files in inbox: {}", inboxDir);
        File dir = new File(inboxDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(getFileExtension()));
        if (files == null) {
            log.debug("No files found in inbox directory: {}", inboxDir);
            return List.of();
        }
        List<File> stable = new ArrayList<>();
        for (File f : files) {
            long size1 = f.length();
            try { Thread.sleep(FILE_STABILITY_WINDOW_SEC * 1000L); } catch (InterruptedException ignored) {}
            long size2 = f.length();
            if (size1 == size2) {
                log.debug("File is stable: {} (size: {} bytes)", f.getName(), size1);
                stable.add(f);
            } else {
                log.debug("File is not stable (size changed): {} ({} -> {} bytes)", f.getName(), size1, size2);
            }
        }
        return stable;
    }

    /**
     * Move file from INBOX to PROCESSING with a GUID suffix to avoid collisions.
     */
    protected File moveToProcessing(File file) {
        log.debug("Moving file to processing directory: {}", file.getAbsolutePath());
        String guid = UUID.randomUUID().toString();
        String newName = file.getName() + "_" + guid;
        File dest = new File(getProcessingDir(), newName);
        try {
            Files.move(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to processing: " + file.getAbsolutePath(), e);
        }
        return dest;
    }

    /**
     * Compute SHA-256 hash of the file.
     */
    protected String computeSha256(File file) {
        log.debug("Computing SHA-256 for file: {}", file.getAbsolutePath());
        try (java.io.InputStream fis = new java.io.FileInputStream(file)) {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256 for file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Move file to ARCHIVE/YYYY/MM/DD.
     */
    protected void moveToArchive(File file) {
        log.debug("Moving file to archive: {}", file.getAbsolutePath());
        LocalDate today = LocalDate.now();
        String archivePath = String.format("%s/%04d/%02d/%02d", getArchiveDir(), today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        File archiveDir = new File(archivePath);
        if (!archiveDir.exists() && !archiveDir.mkdirs()) {
            throw new RuntimeException("Failed to create archive dir: " + archiveDir.getAbsolutePath());
        }
        File dest = new File(archiveDir, file.getName());
        try {
            Files.move(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to archive: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Move file to QUARANTINE and log error.
     */
    protected void moveToQuarantine(File file, String errorMessage) {
        log.warn("Moving file to quarantine: {}. Reason: {}", file.getAbsolutePath(), errorMessage);
        File dest = new File(getQuarantineDir(), file.getName());
        try {
            Files.move(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to quarantine: " + file.getAbsolutePath(), e);
        }
        // TODO: Persist error details to import_error table
        System.err.println("Quarantined file: " + file.getName() + ", reason: " + errorMessage);
    }
}

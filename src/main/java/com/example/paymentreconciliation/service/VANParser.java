package com.example.paymentreconciliation.service;

import com.example.paymentreconciliation.entity.VANTransaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VANParser {

    public static class VANParseException extends Exception {
        public VANParseException(String message) {
            super(message);
        }
    }

    public List<VANTransactionData> parse(File csvFile) throws VANParseException {
        List<VANTransactionData> transactions = new ArrayList<>();
        try (FileReader reader = new FileReader(csvFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                VANTransactionData data = new VANTransactionData();
                // Assuming CSV headers match the field names, adjust as needed
                data.mainAccountNumber = record.get("Main Account Number");
                data.virtualAccountNumber = record.get("Virtual Account Number (VAN)");
                data.transactionReferenceNumber = record.get("Transaction Reference Number");
                data.bankReferenceTraceId = record.get("Bank Reference / Trace ID");
                data.remitterName = record.get("Remitter Name");
                data.remitterAccountNumber = record.get("Remitter Account Number");
                data.remitterIfscBankName = record.get("Remitter IFSC / Bank Name");
                data.remitterVpa = record.get("Remitter VPA");
                data.transactionDate = LocalDate.parse(record.get("Transaction Date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                data.valueDate = LocalDate.parse(record.get("Value Date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                data.amount = new BigDecimal(record.get("Amount (INR)"));
                data.modeChannel = record.get("Mode / Channel");
                data.paymentDescriptionNarration = record.get("Payment Description / Narration");
                data.paymentStatus = record.get("Payment Status");
                data.mappedCustomerIdCode = record.get("Mapped Customer ID / Code");
                data.invoiceReferenceId = record.get("Invoice / Reference ID");
                data.dateTimeOfCredit = LocalDateTime.parse(record.get("Date & Time of Credit"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                data.branchBankCode = record.get("Branch / Bank Code");
                transactions.add(data);
            }
        } catch (IOException e) {
            throw new VANParseException("Failed to parse CSV file: " + e.getMessage());
        }
        return transactions;
    }

    public static class VANTransactionData {
        public String mainAccountNumber;
        public String virtualAccountNumber;
        public String transactionReferenceNumber;
        public String bankReferenceTraceId;
        public String remitterName;
        public String remitterAccountNumber;
        public String remitterIfscBankName;
        public String remitterVpa;
        public LocalDate transactionDate;
        public LocalDate valueDate;
        public BigDecimal amount;
        public String modeChannel;
        public String paymentDescriptionNarration;
        public String paymentStatus;
        public String mappedCustomerIdCode;
        public String invoiceReferenceId;
        public LocalDateTime dateTimeOfCredit;
        public String branchBankCode;
    }
}

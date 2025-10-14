package com.example.paymentreconciliation.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// prowidesoftware dependency (add to pom.xml):
// <dependency>
//   <groupId>com.prowidesoftware</groupId>
//   <artifactId>pw-swift-core</artifactId>
//   <version>SRU2025-10.0.0</version>
// </dependency>

import com.prowidesoftware.swift.model.mt.mt9xx.MT940;
import com.prowidesoftware.swift.model.field.*;

/**
 * MT940 Parser using prowidesoftware (Prowide Core)
 * Parses MT940 files into structured statement objects as per the ingestion spec.
 */
public class Mt940Parser {

    /**
     * Parse the given MT940 file into a list of Statement objects.
     * Throws exception on parse/validation error.
     */
    public List<Statement> parse(File mt940File) throws Mt940ParseException {
        List<Statement> statements = new ArrayList<>();
        String content;
        try {
            content = Files.readString(mt940File.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Mt940ParseException("Failed to read MT940 file", e);
        }

        // Split file into multiple MT940 messages if needed
        List<String> mt940Messages = splitMessages(content);
        int statementIndex = 0;
        for (String msg : mt940Messages) {
            try {
                MT940 mt940 = MT940.parse(msg);
                Statement stmt = new Statement();
                stmt.stmtRef20 = getFieldValue(mt940, Field20.class);
                stmt.seq28c = getFieldValue(mt940, Field28C.class);
                stmt.accountNo = getFieldValue(mt940, Field25.class);
                stmt.currency = getOpeningBalanceCurrency(mt940);
                stmt.isInterim = isInterim(mt940);
                stmt.openingBalance = toBalance(mt940.getField60F(), "OPENING");
                stmt.closingBalance = toBalance(mt940.getField62F(), "CLOSING");
                stmt.otherBalances = new ArrayList<>();
                if (mt940.getField64() != null) stmt.otherBalances.add(toBalance(mt940.getField64(), "AVAILABLE"));
                // Field65 can be multiple (forward available balances)
                List<Field65> f65s = mt940.getField65();
                if (f65s != null) {
                    for (Field65 f65 : f65s) {
                        stmt.otherBalances.add(toBalance(f65, "FORWARD"));
                    }
                }
                stmt.transactions = new ArrayList<>();
                List<Field61> txns = mt940.getField61();
                List<Field86> narrs = mt940.getField86();
                int txnCount = txns != null ? txns.size() : 0;
                for (int i = 0; i < txnCount; i++) {
                    Field61 f61 = txns.get(i);
                    Field86 f86 = (narrs != null && narrs.size() > i) ? narrs.get(i) : null;
                    Transaction txn = toTransaction(f61, f86, stmt, i + 1);
                    stmt.transactions.add(txn);
                }
                stmt.rawLines = new ArrayList<>(); // Optionally fill for audit
                statements.add(stmt);
            } catch (Exception e) {
                throw new Mt940ParseException("Failed to parse statement at index " + statementIndex, e);
            }
            statementIndex++;
        }
        return statements;
    }

    // --- prowidesoftware helpers ---

    private <T> String getFieldValue(MT940 mt940, Class<T> fieldClass) {
        if (fieldClass == Field20.class && mt940.getField20() != null) return mt940.getField20().getValue();
        if (fieldClass == Field25.class && mt940.getField25() != null) return mt940.getField25().getValue();
        if (fieldClass == Field28C.class && mt940.getField28C() != null) return mt940.getField28C().getValue();
        return null;
    }

    private String getOpeningBalanceCurrency(MT940 mt940) {
        // Field60F: 1=DC, 2=Date, 3=Currency, 4=Amount
        if (mt940.getField60F() != null) return mt940.getField60F().getComponent(3);
        return null;
    }

    private boolean isInterim(MT940 mt940) {
        if (mt940.getField60M() != null || mt940.getField62M() != null) return true;
        return false;
    }

    private Balance toBalance(Field field, String type) {
        if (field == null) return null;
        Balance b = new Balance();
        b.type = type;
        // Field60F, Field62F, Field64, Field65: 1=DC, 2=Date, 3=Currency, 4=Amount
        b.dc = field.getComponent(1);
        b.date = field.getComponent(2);
        b.currency = field.getComponent(3);
        b.amount = field.getComponent(4);
        return b;
    }

    private Transaction toTransaction(Field61 f61, Field86 f86, Statement stmt, int lineNo) {
        Transaction t = new Transaction();
        t.lineNo = lineNo;
        // Field61: 1=ValueDate, 2=EntryDate, 3=DC, 4=FundsCode, 5=Amount, 6=TxnType, 7=BankRef, 8=CustRef, 9=SuppDetails
        t.valueDate = f61.getComponent(1);
        t.entryDate = f61.getComponent(2);
        t.dc = f61.getComponent(3);
        t.amount = f61.getComponent(5);
        t.signedAmount = getSignedAmount(t.amount, t.dc);
        t.currency = stmt.currency;
        t.txnTypeCode = f61.getComponent(6);
        t.bankReference = f61.getComponent(7);
        t.customerReference = f61.getComponent(8);
        t.entryReference = f61.getComponent(9);
        t.narrative = (f86 != null) ? f86.getValue() : null;
        t.narrativeTokens = parseNarrativeTokens(t.narrative);
        t.extIdempotencyHash = computeIdempotencyHash(stmt, t);
        return t;
    }

    private String getSignedAmount(String amount, String dc) {
        if (amount == null || dc == null) return null;
        return ("D".equalsIgnoreCase(dc) ? "-" : "") + amount;
    }

    private Map<String, String> parseNarrativeTokens(String narrative) {
        // Optionally parse structured :86: segments into key-value pairs
        return new HashMap<>();
    }

    private String computeIdempotencyHash(Statement stmt, Transaction t) {
        // Hash(account, :20:, :28C:, value_date, amount, DC, entry_ref, bank_ref, cust_ref)
        String raw = String.join("|",
                stmt.accountNo != null ? stmt.accountNo : "",
                stmt.stmtRef20 != null ? stmt.stmtRef20 : "",
                stmt.seq28c != null ? stmt.seq28c : "",
                t.valueDate != null ? t.valueDate : "",
                t.amount != null ? t.amount : "",
                t.dc != null ? t.dc : "",
                t.entryReference != null ? t.entryReference : "",
                t.bankReference != null ? t.bankReference : "",
                t.customerReference != null ? t.customerReference : ""
        );
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    private List<String> splitMessages(String content) {
        // Split by SWIFT envelope: each message starts with {1:
        List<String> messages = new ArrayList<>();
        String[] parts = content.split("(?=\\{1:)");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                // Only add if it looks like a SWIFT message
                if (trimmed.startsWith("{1:")) {
                    messages.add(trimmed);
                } else if (!messages.isEmpty()) {
                    // In case the first split part is before the first {1:
                    // append to previous
                    String last = messages.remove(messages.size() - 1);
                    messages.add(last + "\n" + trimmed);
                }
            }
        }
        return messages;
    }

    // --- Data structures for parsed output ---

    public static class Statement {
        public String stmtRef20;
        public String seq28c;
        public String accountNo;
        public String currency;
        public boolean isInterim;
        public Balance openingBalance;
        public Balance closingBalance;
        public List<Balance> otherBalances;
        public List<Transaction> transactions;
        public List<RawLine> rawLines;
    }

    public static class Balance {
        public String type; // OPENING, CLOSING, AVAILABLE, FORWARD
        public String dc;   // D or C
        public String date;
        public String currency;
        public String amount;
    }

    public static class Transaction {
        public int lineNo;
        public String valueDate;
        public String entryDate;
        public String dc;
        public String amount;
        public String signedAmount;
        public String currency;
        public String txnTypeCode;
        public String bankReference;
        public String customerReference;
        public String entryReference;
        public String narrative;
        public Map<String, String> narrativeTokens; // parsed from :86: if structured
        public String extIdempotencyHash;
    }

    public static class RawLine {
        public int lineNo;
        public String tag;
        public String rawText;
    }

    public static class Mt940ParseException extends Exception {
        public Mt940ParseException(String msg) { super(msg); }
        public Mt940ParseException(String msg, Throwable cause) { super(msg, cause); }
    }
}

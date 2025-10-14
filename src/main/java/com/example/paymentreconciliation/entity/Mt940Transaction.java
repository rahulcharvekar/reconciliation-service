package com.example.paymentreconciliation.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Represents a parsed transaction from MT940 statement
 */
public class Mt940Transaction {
    private String transactionReference;
    private BigDecimal amount;
    private LocalDate valueDate;
    private String debitCreditIndicator;
    private String narrative;
    private String accountNumber;
    public Mt940Transaction() {}
    public Mt940Transaction(String transactionReference, BigDecimal amount, LocalDate valueDate, String debitCreditIndicator, String narrative, String accountNumber) {
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.valueDate = valueDate;
        this.debitCreditIndicator = debitCreditIndicator;
        this.narrative = narrative;
        this.accountNumber = accountNumber;
    }
    // Getters and Setters
    public String getTransactionReference() {
        return transactionReference;
    }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public LocalDate getValueDate() {
        return valueDate;
    }
    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }
    public String getDebitCreditIndicator() {
        return debitCreditIndicator;
    }
    public void setDebitCreditIndicator(String debitCreditIndicator) {
        this.debitCreditIndicator = debitCreditIndicator;
    }
    public String getNarrative() {
        return narrative;
    }
    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}

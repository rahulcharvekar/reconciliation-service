package com.example.paymentreconciliation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for reconciliation response
 */
public class ReconciliationResponse {
    
    private String transactionReference;
    private BigDecimal requestAmount;
    private BigDecimal mt940Amount;
    private String mt940TransactionReference;
    private LocalDate mt940ValueDate;
    private MatchStatus amountMatch;
    private MatchStatus referenceMatch;
    private ReconciliationStatus status;
    private String message;
    
    public enum MatchStatus {
        MATCHED, NOT_MATCHED, NOT_FOUND
    }
    
    public enum ReconciliationStatus {
        RECONCILED, UN_RECONCILED
    }
    
    public ReconciliationResponse() {}
    
    // Getters and Setters
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public BigDecimal getRequestAmount() {
        return requestAmount;
    }
    
    public void setRequestAmount(BigDecimal requestAmount) {
        this.requestAmount = requestAmount;
    }
    
    public BigDecimal getMt940Amount() {
        return mt940Amount;
    }
    
    public void setMt940Amount(BigDecimal mt940Amount) {
        this.mt940Amount = mt940Amount;
    }
    
    public String getMt940TransactionReference() {
        return mt940TransactionReference;
    }
    
    public void setMt940TransactionReference(String mt940TransactionReference) {
        this.mt940TransactionReference = mt940TransactionReference;
    }
    
    public LocalDate getMt940ValueDate() {
        return mt940ValueDate;
    }
    
    public void setMt940ValueDate(LocalDate mt940ValueDate) {
        this.mt940ValueDate = mt940ValueDate;
    }
    
    public MatchStatus getAmountMatch() {
        return amountMatch;
    }
    
    public void setAmountMatch(MatchStatus amountMatch) {
        this.amountMatch = amountMatch;
    }
    
    public MatchStatus getReferenceMatch() {
        return referenceMatch;
    }
    
    public void setReferenceMatch(MatchStatus referenceMatch) {
        this.referenceMatch = referenceMatch;
    }
    
    public ReconciliationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReconciliationStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "ReconciliationResponse{" +
                "transactionReference='" + transactionReference + '\'' +
                ", requestAmount=" + requestAmount +
                ", mt940Amount=" + mt940Amount +
                ", mt940TransactionReference='" + mt940TransactionReference + '\'' +
                ", mt940ValueDate=" + mt940ValueDate +
                ", amountMatch=" + amountMatch +
                ", referenceMatch=" + referenceMatch +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}

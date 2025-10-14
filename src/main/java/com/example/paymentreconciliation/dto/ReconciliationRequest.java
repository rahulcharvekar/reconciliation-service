package com.example.paymentreconciliation.dto;

import java.math.BigDecimal;

/**
 * DTO for reconciliation request
 */
public class ReconciliationRequest {
    
    private String transactionReference;
    private BigDecimal amount;
    
    public ReconciliationRequest() {}
    
    public ReconciliationRequest(String transactionReference, BigDecimal amount) {
        this.transactionReference = transactionReference;
        this.amount = amount;
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
    
    @Override
    public String toString() {
        return "ReconciliationRequest{" +
                "transactionReference='" + transactionReference + '\'' +
                ", amount=" + amount +
                '}';
    }
}

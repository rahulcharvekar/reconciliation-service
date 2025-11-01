package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "statement_transaction", uniqueConstraints = @UniqueConstraint(name = "uq_txn_hash", columnNames = {"ext_idempotency_hash"}))
public class StatementTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "statement_file_id", nullable = false)
    private StatementFile statementFile;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "dc", nullable = false, length = 1)
    private String dc;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "signed_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal signedAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "txn_type_code", length = 4)
    private String txnTypeCode;

    @Column(name = "bank_reference", length = 35)
    private String bankReference;

    @Column(name = "customer_reference", length = 35)
    private String customerReference;

    @Column(name = "entry_reference", length = 16)
    private String entryReference;

    @Column(name = "narrative")
    private String narrative;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "narrative_tokens", columnDefinition = "jsonb")
    private String narrativeTokens;


    @Column(name = "ext_idempotency_hash", nullable = false, length = 64)
    private String extIdempotencyHash;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatementFile getStatementFile() {
        return statementFile;
    }

    public void setStatementFile(StatementFile statementFile) {
        this.statementFile = statementFile;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public java.time.LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(java.time.LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public java.time.LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(java.time.LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public java.math.BigDecimal getSignedAmount() {
        return signedAmount;
    }

    public void setSignedAmount(java.math.BigDecimal signedAmount) {
        this.signedAmount = signedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTxnTypeCode() {
        return txnTypeCode;
    }

    public void setTxnTypeCode(String txnTypeCode) {
        this.txnTypeCode = txnTypeCode;
    }

    public String getBankReference() {
        return bankReference;
    }

    public void setBankReference(String bankReference) {
        this.bankReference = bankReference;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getEntryReference() {
        return entryReference;
    }

    public void setEntryReference(String entryReference) {
        this.entryReference = entryReference;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getNarrativeTokens() {
        return narrativeTokens;
    }

    public void setNarrativeTokens(String narrativeTokens) {
        this.narrativeTokens = narrativeTokens;
    }

    public String getExtIdempotencyHash() {
        return extIdempotencyHash;
    }

    public void setExtIdempotencyHash(String extIdempotencyHash) {
        this.extIdempotencyHash = extIdempotencyHash;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}

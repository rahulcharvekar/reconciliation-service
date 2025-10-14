package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "statement_file", uniqueConstraints = @UniqueConstraint(name = "uq_stmt", columnNames = {"bank_account_id", "stmt_ref_20", "seq_28c"}))
public class StatementFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "import_run_id", nullable = false)
    private ImportRun importRun;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "stmt_ref_20", nullable = false, length = 35)
    private String stmtRef20;

    @Column(name = "seq_28c", length = 35)
    private String seq28c;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "opening_dc", nullable = false, length = 1)
    private String openingDc;

    @Column(name = "opening_amount", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal openingAmount;

    @Column(name = "closing_dc", nullable = false, length = 1)
    private String closingDc;

    @Column(name = "closing_amount", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal closingAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "is_interim", nullable = false)
    private Boolean isInterim = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ImportRun getImportRun() {
        return importRun;
    }

    public void setImportRun(ImportRun importRun) {
        this.importRun = importRun;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getStmtRef20() {
        return stmtRef20;
    }

    public void setStmtRef20(String stmtRef20) {
        this.stmtRef20 = stmtRef20;
    }

    public String getSeq28c() {
        return seq28c;
    }

    public void setSeq28c(String seq28c) {
        this.seq28c = seq28c;
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDate statementDate) {
        this.statementDate = statementDate;
    }

    public String getOpeningDc() {
        return openingDc;
    }

    public void setOpeningDc(String openingDc) {
        this.openingDc = openingDc;
    }

    public java.math.BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(java.math.BigDecimal openingAmount) {
        this.openingAmount = openingAmount;
    }

    public String getClosingDc() {
        return closingDc;
    }

    public void setClosingDc(String closingDc) {
        this.closingDc = closingDc;
    }

    public java.math.BigDecimal getClosingAmount() {
        return closingAmount;
    }

    public void setClosingAmount(java.math.BigDecimal closingAmount) {
        this.closingAmount = closingAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getIsInterim() {
        return isInterim;
    }

    public void setIsInterim(Boolean isInterim) {
        this.isInterim = isInterim;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "statement_balance")
public class StatementBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "statement_file_id", nullable = false)
    private StatementFile statementFile;
    @Column(name = "bal_type", nullable = false, length = 16)
    private String balType; // OPENING, CLOSING, AVAILABLE, FORWARD
    @Column(name = "dc", nullable = false, length = 1)
    private String dc;
    @Column(name = "bal_date", nullable = false)
    private LocalDate balDate;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
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
    public String getBalType() {
        return balType;
    }
    public void setBalType(String balType) {
        this.balType = balType;
    }
    public String getDc() {
        return dc;
    }
    public void setDc(String dc) {
        this.dc = dc;
    }
    public LocalDate getBalDate() {
        return balDate;
    }
    public void setBalDate(LocalDate balDate) {
        this.balDate = balDate;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

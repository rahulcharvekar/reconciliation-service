package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "transaction_86_segment")
public class Transaction86Segment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "statement_transaction_id", nullable = false)
    private StatementTransaction statementTransaction;
    @Column(name = "seg_key", nullable = false, length = 32)
    private String segKey;
    @Column(name = "seg_value", length = 512)
    private String segValue;
    @Column(name = "seg_seq", nullable = false)
    private Integer segSeq;
    public void setStatementTransaction(StatementTransaction statementTransaction) {
        this.statementTransaction = statementTransaction;
    }
    public void setSegKey(String segKey) {
        this.segKey = segKey;
    }
    public void setSegValue(String segValue) {
        this.segValue = segValue;
    }
    public void setSegSeq(Integer segSeq) {
        this.segSeq = segSeq;
    }
}

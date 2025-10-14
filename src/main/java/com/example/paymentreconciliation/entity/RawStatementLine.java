package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
@Entity
@Table(name = "raw_statement_line")
public class RawStatementLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "statement_file_id", nullable = false)
    private StatementFile statementFile;
    @Column(name = "line_no", nullable = false)
    private Integer lineNo;
    @Column(name = "tag", length = 8)
    private String tag;
    @Column(name = "raw_text", nullable = false)
    private String rawText;
    public void setStatementFile(StatementFile statementFile) {
        this.statementFile = statementFile;
    }
    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}

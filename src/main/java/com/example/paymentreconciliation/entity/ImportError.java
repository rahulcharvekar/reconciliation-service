package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_error")
public class ImportError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "import_run_id", nullable = false)
    private ImportRun importRun;

    @ManyToOne
    @JoinColumn(name = "statement_file_id")
    private StatementFile statementFile;

    @Column(name = "line_no")
    private Integer lineNo;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and setters omitted for brevity
}

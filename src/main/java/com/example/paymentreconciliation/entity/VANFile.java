package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "van_file")
public class VANFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "import_run_id", nullable = false)
    private ImportRun importRun;

    @Column(name = "file_generation_date_time")
    private LocalDateTime fileGenerationDateTime;

    @Column(name = "file_sequence_number")
    private String fileSequenceNumber;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ImportRun getImportRun() { return importRun; }
    public void setImportRun(ImportRun importRun) { this.importRun = importRun; }

    public LocalDateTime getFileGenerationDateTime() { return fileGenerationDateTime; }
    public void setFileGenerationDateTime(LocalDateTime fileGenerationDateTime) { this.fileGenerationDateTime = fileGenerationDateTime; }

    public String getFileSequenceNumber() { return fileSequenceNumber; }
    public void setFileSequenceNumber(String fileSequenceNumber) { this.fileSequenceNumber = fileSequenceNumber; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

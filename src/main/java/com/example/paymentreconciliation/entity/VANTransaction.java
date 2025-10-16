package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "van_transaction")
public class VANTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "import_run_id", nullable = false)
    private ImportRun importRun;

    // Identification fields
    @Column(name = "main_account_number", nullable = false)
    private String mainAccountNumber;

    @Column(name = "virtual_account_number", nullable = false)
    private String virtualAccountNumber;

    @Column(name = "transaction_reference_number")
    private String transactionReferenceNumber;

    @Column(name = "bank_reference_trace_id")
    private String bankReferenceTraceId;

    // Remitter / Payer details
    @Column(name = "remitter_name")
    private String remitterName;

    @Column(name = "remitter_account_number")
    private String remitterAccountNumber;

    @Column(name = "remitter_ifsc_bank_name")
    private String remitterIfscBankName;

    @Column(name = "remitter_vpa")
    private String remitterVpa;

    // Transaction details
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "mode_channel")
    private String modeChannel;

    @Column(name = "payment_description_narration")
    private String paymentDescriptionNarration;

    @Column(name = "payment_status")
    private String paymentStatus;

    // VAN reconciliation metadata
    @Column(name = "mapped_customer_id_code")
    private String mappedCustomerIdCode;

    @Column(name = "invoice_reference_id")
    private String invoiceReferenceId;

    @Column(name = "date_time_of_credit")
    private LocalDateTime dateTimeOfCredit;

    @Column(name = "branch_bank_code")
    private String branchBankCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ImportRun getImportRun() { return importRun; }
    public void setImportRun(ImportRun importRun) { this.importRun = importRun; }

    public String getMainAccountNumber() { return mainAccountNumber; }
    public void setMainAccountNumber(String mainAccountNumber) { this.mainAccountNumber = mainAccountNumber; }

    public String getVirtualAccountNumber() { return virtualAccountNumber; }
    public void setVirtualAccountNumber(String virtualAccountNumber) { this.virtualAccountNumber = virtualAccountNumber; }

    public String getTransactionReferenceNumber() { return transactionReferenceNumber; }
    public void setTransactionReferenceNumber(String transactionReferenceNumber) { this.transactionReferenceNumber = transactionReferenceNumber; }

    public String getBankReferenceTraceId() { return bankReferenceTraceId; }
    public void setBankReferenceTraceId(String bankReferenceTraceId) { this.bankReferenceTraceId = bankReferenceTraceId; }

    public String getRemitterName() { return remitterName; }
    public void setRemitterName(String remitterName) { this.remitterName = remitterName; }

    public String getRemitterAccountNumber() { return remitterAccountNumber; }
    public void setRemitterAccountNumber(String remitterAccountNumber) { this.remitterAccountNumber = remitterAccountNumber; }

    public String getRemitterIfscBankName() { return remitterIfscBankName; }
    public void setRemitterIfscBankName(String remitterIfscBankName) { this.remitterIfscBankName = remitterIfscBankName; }

    public String getRemitterVpa() { return remitterVpa; }
    public void setRemitterVpa(String remitterVpa) { this.remitterVpa = remitterVpa; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public LocalDate getValueDate() { return valueDate; }
    public void setValueDate(LocalDate valueDate) { this.valueDate = valueDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getModeChannel() { return modeChannel; }
    public void setModeChannel(String modeChannel) { this.modeChannel = modeChannel; }

    public String getPaymentDescriptionNarration() { return paymentDescriptionNarration; }
    public void setPaymentDescriptionNarration(String paymentDescriptionNarration) { this.paymentDescriptionNarration = paymentDescriptionNarration; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getMappedCustomerIdCode() { return mappedCustomerIdCode; }
    public void setMappedCustomerIdCode(String mappedCustomerIdCode) { this.mappedCustomerIdCode = mappedCustomerIdCode; }

    public String getInvoiceReferenceId() { return invoiceReferenceId; }
    public void setInvoiceReferenceId(String invoiceReferenceId) { this.invoiceReferenceId = invoiceReferenceId; }

    public LocalDateTime getDateTimeOfCredit() { return dateTimeOfCredit; }
    public void setDateTimeOfCredit(LocalDateTime dateTimeOfCredit) { this.dateTimeOfCredit = dateTimeOfCredit; }

    public String getBranchBankCode() { return branchBankCode; }
    public void setBranchBankCode(String branchBankCode) { this.branchBankCode = branchBankCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

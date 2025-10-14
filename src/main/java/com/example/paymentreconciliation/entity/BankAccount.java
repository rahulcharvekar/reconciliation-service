package com.example.paymentreconciliation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bank_account", uniqueConstraints = @UniqueConstraint(name = "uq_account", columnNames = {"account_no", "currency"}))
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false, length = 64)
    private String accountNo;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "bank_bic", length = 11)
    private String bankBic;

    @Column(name = "holder_name", length = 128)
    private String holderName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBankBic() {
        return bankBic;
    }

    public void setBankBic(String bankBic) {
        this.bankBic = bankBic;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

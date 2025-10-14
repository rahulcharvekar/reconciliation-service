package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.BankAccount;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNoAndCurrency(String accountNo, String currency);
}

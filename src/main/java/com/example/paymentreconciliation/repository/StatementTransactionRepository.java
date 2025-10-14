package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.StatementTransaction;

public interface StatementTransactionRepository extends JpaRepository<StatementTransaction, Long> {
}

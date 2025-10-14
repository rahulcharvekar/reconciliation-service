package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.StatementBalance;

public interface StatementBalanceRepository extends JpaRepository<StatementBalance, Long> {
}

package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.VANTransaction;

public interface VANTransactionRepository extends JpaRepository<VANTransaction, Long> {
}

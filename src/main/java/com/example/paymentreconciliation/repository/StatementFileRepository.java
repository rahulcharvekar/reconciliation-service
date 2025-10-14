package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.StatementFile;

public interface StatementFileRepository extends JpaRepository<StatementFile, Long> {
}

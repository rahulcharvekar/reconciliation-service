package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.RawStatementLine;

public interface RawStatementLineRepository extends JpaRepository<RawStatementLine, Long> {
}

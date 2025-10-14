package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.ImportError;

public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
}

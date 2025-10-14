package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.ImportRun;

import java.util.Optional;

public interface ImportRunRepository extends JpaRepository<ImportRun, Long> {
    Optional<ImportRun> findByFileHash(String fileHash);
}

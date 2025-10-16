package com.example.paymentreconciliation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.paymentreconciliation.entity.VANFile;

public interface VANFileRepository extends JpaRepository<VANFile, Long> {
}

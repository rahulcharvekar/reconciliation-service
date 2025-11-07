# Reconciliation Service

Spring Boot microservice for payment reconciliation, bank file processing (MT940/VAN), and board approval workflows. Implements matching logic, file ingestion, and multi-tenant data isolation.

**Stack:** Java 17 | Spring Boot 3.2.5 | PostgreSQL | jOOQ | JWT

## Features

- Payment reconciliation and matching
- Bank file ingestion (MT940/VAN)
- Board approval workflows
- Row-level security (RLS) for multi-tenancy
- Audit logging (API & entity level)

## Key Docs

- See `documentation/LBE/README.md` for system overview
- See `copilot-instructions.md` for coding standards and audit rules

## Build & Run

- `mvn clean install` to build
- `docker build -t reconciliation-service:latest .` to build Docker image

## Folder Structure

- `src/main/java/com.example.reconciliation/` — code
- `src/main/resources/` — configs

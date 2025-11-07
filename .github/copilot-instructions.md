# Copilot Instructions for Reconciliation Service

## Project Overview

Spring Boot microservice handling payment reconciliation, bank file processing (MT940/VAN), and board approval workflows. Implements reconciliation matching logic, file ingestion, and board receipt management with multi-tenant data isolation.

**Stack:** Java 17 | Spring Boot 3.2.5 | PostgreSQL | jOOQ | JWT

**Setup:** Follow `documentation/LBE/guides/local-environment.md`

## Code Organization

```
com.example.reconciliation/
├── config/           # Spring configuration (JPA, jOOQ, etc.)
├── controller/       # REST API endpoints for reconciliation and board workflows
├── dao/              # Data Access Objects for complex queries (jOOQ-based)
├── dto/              # Data Transfer Objects (requests, responses)
├── entity/           # JPA entities (Reconciliation, BankFile, Receipt, etc.)
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic (ingestion, matching, approval)
└── util/             # Utility classes and helpers
```

## Coding Standards

- Follow Spring Boot conventions and existing patterns
- Use constructor injection for dependencies
- Add JavaDoc for public APIs
- Use meaningful variable names (`tenantId`, `reconciliationId`, `bankFileId`)
- Return DTOs from controllers, not entities
- Document endpoints with OpenAPI annotations

## Database Access Patterns ⭐ CRITICAL

**ALWAYS read `documentation/LBE/guides/data-access-patterns.md` before writing database code.**

| Pattern              | Use For                               | Examples                                           |
| -------------------- | ------------------------------------- | -------------------------------------------------- |
| **JPA Repository**   | CRUD, writes, simple reads            | `ReconciliationRepository`, `BankFileRepository`   |
| **jOOQ DSL**         | Complex queries, multi-joins, filters | `ReconciliationQueryDao` (future)                  |
| **jOOQ + SQL Files** | Analyst reports, aggregations, CTEs   | `sql/reconciliation/matching_summary.sql` (future) |

### Rules for ALL Patterns

🔒 **Security:** Always set RLS context: `SELECT auth.set_user_context(:userId)`  
🔄 **Transactions:** Use `@Transactional` for writes, `@Transactional(readOnly=true)` for reads  
✅ **Testing:** Test with multiple personas, verify RLS isolation

**Details:** See `documentation/LBE/guides/data-access-patterns.md`

    │    └──────┬───────┘          │ Analysis       │
    │           │                  └────────┬───────┘

## Security Guidelines

### Authorization & Data Access

- Validate all user input with **Bean Validation** annotations
- Check authorization before accessing resources:
  - Consult `documentation/LBE/reference/policy-matrix.md` for required policies
  - Use appropriate `@PreAuthorize` annotations
- **Never log sensitive data** (bank account numbers, reconciliation details)
- Implement **CORS** configuration properly for production

### RLS & Multi-Tenancy

- **Always** set tenant context before queries
- Test multi-tenancy isolation thoroughly
- Always include `tenantId` in audit logs
- Follow patterns in `documentation/LBE/foundations/data-guardrails-101.md`

## Audit Logging Guidelines ⭐ CRITICAL

**Read:** `documentation/LBE/architecture/audit-design.md` | `documentation/LBE/reference/audit-quick-reference.md`

### Configuration (DO NOT CHANGE)

```yaml
shared-lib:
  audit:
    enabled: true
    service-name: reconciliation-service
    source-schema: reconciliation
  entity-audit:
    enabled: true
```

### 1. API-Level Auditing with @Auditable

```java
@PostMapping("/bank-files")
@Auditable(
    action = "BANK_FILE_UPLOADED",
    entityType = "BANK_FILE",
    description = "Bank file uploaded for processing"
)
public ResponseEntity<?> uploadBankFile(@RequestParam("file") MultipartFile file) {
    // Audit logged automatically
}
```

### 2. Entity-Level Auditing

```java
@Entity
@EntityListeners(SharedEntityAuditListener.class)
public class Transaction {
    // All changes tracked with before/after values + hash chain
}
```

### Best Practices

**DO:**

- ✅ Use `@Auditable` on bank file upload/processing endpoints
- ✅ Use `@EntityListeners` on Transaction, Reconciliation entities
- ✅ Log reconciliation matches with confidence scores
- ✅ Track board approval actions

**DON'T:**

- ❌ Log sensitive bank details (account numbers)
- ❌ Skip audit for failed matches (failures are valuable data)
- ❌ Use generic action names (be specific: BANK_FILE_PROCESSED)

**Troubleshooting:** Check `shared-lib.audit.enabled=true` | Verify DB grants | See audit-design.md

## Common Tasks

### Adding a New API Endpoint

**Step 1:** Consult `PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (§12, §17), `policy-matrix.md`, `data-access-patterns.md`  
**Step 2:** Choose pattern: JPA (simple), jOOQ DSL (complex), jOOQ+SQL (reports)  
**Step 3:** Implement: DTO → DAO/Repository → Service → Controller with `@PreAuthorize`  
**Step 4:** Register: Migration → `auth.endpoints` + `auth.endpoint_policies`  
**Step 5:** Update `PHASE5_ENDPOINT_POLICY_MAPPINGS.md` + `policy-matrix.md`  
**Step 6:** Test: Authorization + RLS isolation

### Adding Bank File Processing Logic

1. Determine file format (MT940, VAN, custom)
2. Create parser in service layer
3. Implement validation logic
4. Store file metadata using `BankFileRepository`
5. Process transactions and create reconciliation records
6. Add comprehensive tests with sample files
7. Document file format in README

### Adding Reconciliation Matching Rule

1. Define matching rule criteria
2. Implement in `MatchingEngine` or service layer
3. Add configurable threshold parameters
4. Test with various scenarios (exact match, fuzzy match, no match)
5. Document rule in relevant guides
6. Update `documentation/LBE/reference/TABLE_NAMES_REFERENCE.md` if schema changes

### Debugging Reconciliation Issues

1. Check reconciliation status and matching confidence
2. Verify bank file parsing and transaction extraction
3. Review matching logic and thresholds
4. Check audit logs for reconciliation events
5. Check RLS context: `SELECT current_setting('app.current_user_id')`
6. Consult `documentation/LBE/playbooks/troubleshoot-auth.md` for auth issues

## Important Considerations

- **RLS:** Always use `RLSContext` for tenant isolation. Test multi-tenancy thoroughly.
- **Performance:** Use pagination, caching, proper indexes. Profile matching logic.
- **Migrations:** SQL scripts only. Test on production copies. Document in `TABLE_NAMES_REFERENCE.md`.

---

# Reconciliation Service — Documentation Reference 📚

**Source of Truth:** `documentation/LBE/` - Always consult before coding

## Essential Reading 🎯

**Start Here:**

- `documentation/LBE/README.md` – Guided journey through auth system
- `documentation/LBE/architecture/overview.md` – System topology and flows
- `documentation/LBE/architecture/data-map.md` – Table relationships
- `documentation/LBE/architecture/audit-design.md` – Audit system ⭐

**Foundations:**

- `documentation/LBE/foundations/access-control-101.md` – RBAC fundamentals
- `documentation/LBE/foundations/data-guardrails-101.md` – RLS primer

## Implementation Guides 💻

**Data Access (CRITICAL):**

- `documentation/LBE/guides/data-access-patterns.md` ⭐ – **Read before ANY database code**

**Workflows:**

- `documentation/LBE/guides/login-to-data.md` – Login → JWT → RLS flow
- `documentation/LBE/guides/setup/rbac.md` – RBAC setup
- `documentation/LBE/guides/setup/vpd.md` – RLS/VPD setup
- `documentation/LBE/guides/extend-access.md` – Adding policies
- `documentation/LBE/guides/verify-permissions.md` – Testing

## Quick Reference 📖

- `documentation/LBE/reference/role-catalog.md` – All roles
- `documentation/LBE/reference/policy-matrix.md` – Policy mappings
- `documentation/LBE/reference/audit-quick-reference.md` – Audit guide
- `documentation/LBE/reference/TABLE_NAMES_REFERENCE.md` – Schema reference
- `documentation/LBE/reference/recent-updates.md` – Latest changes

## Troubleshooting 🔧

- `documentation/LBE/playbooks/troubleshoot-auth.md` – Auth issues
- `documentation/LBE/reference/postgres-operations.md` – Database ops

## Maintenance Checklist ✅

**Adding Endpoint:**

1. Choose data pattern (`data-access-patterns.md`)
2. Implement: DTO → DAO → Service → Controller
3. Register: `auth.endpoints` + `auth.endpoint_policies`
4. Update: `PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (§12, §17) + `policy-matrix.md`
5. Test: Authorization + RLS

**Modifying Roles/Policies:**

1. SQL migration
2. Update: `policy-matrix.md` + `role-catalog.md`
3. Test with personas
4. Document in `recent-updates.md`

**Schema Changes:**

1. Migration script
2. Update: `data-map.md` + `TABLE_NAMES_REFERENCE.md`
3. Test RLS
4. Document in `recent-updates.md`

**Audit Changes:**

1. Match `audit-quick-reference.md`
2. Update `audit-design.md` (Reconciliation section)
3. Ensure compliance

## Key Principles 🎯

- 🔒 **Security:** Never bypass RLS | Always validate JWT | Set session context | Check authorization | No sensitive logging
- 📝 **Documentation:** Read docs first | Update with code | Keep in sync
- 🧪 **Testing:** Multiple personas | Tenant isolation | RBAC | Error scenarios

## Quick Links �

| Task               | Documentation                           |
| ------------------ | --------------------------------------- |
| Local setup        | `guides/local-environment.md`           |
| Architecture       | `architecture/overview.md`              |
| **Data access**    | **`guides/data-access-patterns.md`** ⭐ |
| Add endpoint       | `guides/extend-access.md`               |
| Create role/policy | `guides/setup/rbac.md`                  |
| Debug auth         | `playbooks/troubleshoot-auth.md`        |
| RLS                | `foundations/data-guardrails-101.md`    |
| PostgreSQL ops     | `reference/postgres-operations.md`      |
| Recent changes     | `reference/recent-updates.md`           |

---

**Remember:** `documentation/LBE/` is the single source of truth. Consult before changing, update with changes.

# Copilot Instructions for Reconciliation Service

## Project Overview

This is a Spring Boot microservice that handles payment reconciliation, bank file processing (MT940/VAN), and board approval workflows. The service implements:

- **MT940/VAN file ingestion and processing**
- **Payment reconciliation and matching logic**
- **Board receipt and approval workflows**
- **Multi-tenant data isolation** using PostgreSQL RLS
- **Comprehensive audit logging** for compliance and debugging

## Technology Stack

- **Java 17** (OpenJDK)
- **Spring Boot 3.2.5** with Spring Data JPA, Spring Web, jOOQ
- **Maven** for build and dependency management
- **PostgreSQL** as the primary database (with RLS policies, `reconciliation` schema)
- **jOOQ** for type-safe SQL queries
- **Docker** for containerization
- **OpenAPI/Swagger** for API documentation

## Development Environment Setup

### Prerequisites

- Java 17 or later
- Maven 3.8+
- Docker Desktop (for PostgreSQL container)
- PostgreSQL client (psql) for database setup
- IDE with Java support (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### Initial Setup

1. **Clone the repository** and create a feature branch
2. **Install dependencies**:
   ```bash
   mvn dependency:go-offline
   ```
3. **Build the project**:
   ```bash
   mvn clean package
   ```
4. **Set up the database** following `documentation/LBE/guides/local-environment.md`:
   - Run PostgreSQL via Docker or connect to PostgreSQL instance
   - Execute SQL scripts for `reconciliation` schema
   - Load seed data for testing

### Environment Configuration

- Configuration files are in `src/main/resources/`
- Use `application-dev.yml` for local development
- Configure `currentSchema=reconciliation` in datasource
- Never commit secrets; use environment variables:
  - Database credentials via `SPRING_DATASOURCE_*` variables
  - Internal service authentication keys

### Running the Service

```bash
# Run locally with dev profile
mvn spring-boot:run

# Or specify a profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Access health endpoint
curl http://localhost:8080/actuator/health

# Access API documentation
http://localhost:8080/swagger-ui.html
```

### Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify

# Run specific test class
mvn test -Dtest=ReconciliationServiceTest
```

## Code Organization

### Package Structure

```
com.example.reconciliation/
├── config/           # Spring configuration classes (JPA, jOOQ, etc.)
├── controller/       # REST API endpoints for reconciliation and board workflows
├── dao/              # Data Access Objects for complex queries (jOOQ-based)
├── dto/              # Data Transfer Objects (requests, responses)
├── entity/           # JPA entities (Reconciliation, BankFile, Receipt, etc.)
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic layer (ingestion, matching, approval)
└── util/             # Utility classes and helpers
```

### Key Components

- **ReconciliationService** - Manages reconciliation logic and payment matching
- **BankFileIngestionService** - Processes MT940/VAN file uploads
- **BoardReceiptService** - Handles board receipt processing and approvals
- **MatchingEngine** - Matches bank transactions with payment records
- **AuditLogService** - Records audit events for compliance

## Coding Standards

### Java Code Style

- **Follow Spring Boot conventions** and existing code patterns
- Use **constructor injection** for dependencies
- Add **JavaDoc comments** for public APIs and complex business logic
- Use **meaningful variable names** (e.g., `reconciliationId`, `bankFileId`, `receiptId`, `tenantId`)
- Keep methods **focused and small** (single responsibility)
- Use **Optional** for potentially null return values
- Handle exceptions appropriately with **custom exception classes**

### REST API Design

- Follow REST principles with proper HTTP methods (GET, POST, PUT, DELETE)
- Use appropriate HTTP status codes (200, 201, 400, 403, 404, 500)
- Return consistent response structures using DTOs
- Document all endpoints with OpenAPI annotations (@Operation, @ApiResponse)
- Version APIs if making breaking changes

## Database Access Patterns ⭐ CRITICAL

**ALWAYS consult `documentation/LBE/guides/data-access-patterns.md` before writing any database code.**

### Pattern Selection for Reconciliation Service

This service currently uses **JPA primarily**, with plans to add jOOQ for analytical queries:

```
┌─────────────────────────────────────────┐
│ What type of operation are you doing?  │
└─────────────┬───────────────────────────┘
              │
    ┌─────────┴─────────┐
    │                   │
    ▼                   ▼
┌───────┐         ┌──────────┐
│ WRITE │         │   READ   │
└───┬───┘         └────┬─────┘
    │                  │
    │            ┌─────┴─────────────────────┐
    │            │                           │
    │            ▼                           ▼
    │    ┌──────────────┐          ┌────────────────┐
    │    │ Simple       │          │ Complex        │
    │    │ Lookup       │          │ Reconciliation │
    │    └──────┬───────┘          │ Analysis       │
    │           │                  └────────┬───────┘
    │           │                           │
    │           │                    ┌──────┴──────┐
    │           │                    │             │
    │           │                    ▼             ▼
    │           │          ┌────────────┐  ┌──────────────┐
    │           │          │ Report /   │  │  Matching    │
    │           │          │ Dashboard  │  │  Logic       │
    │           │          └──────┬─────┘  └──────┬───────┘
    │           │                 │                │
    ▼           ▼                 ▼                ▼
┌────────┐ ┌────────┐   ┌────────────┐   ┌────────────┐
│  JPA   │ │  JPA   │   │    jOOQ    │   │    jOOQ    │
│Repository││Repository│ │ +SQL File  │   │    DSL     │
└────────┘ └────────┘   └────────────┘   └────────────┘
```

### 1. Spring Data JPA - Use for:

✅ **When to use:**

- Reconciliation record creation and updates
- Bank file metadata persistence
- Board receipt CRUD operations
- Status transitions requiring entity callbacks
- Any mutation on JPA entities

📁 **Examples in this service:**

- `ReconciliationRepository` - Reconciliation entity persistence
- `BankFileRepository` - Bank file metadata
- `BoardReceiptRepository` - Receipt management
- `MatchingResultRepository` - Matching results

💡 **Rules:**

- Use for all write operations
- Keep repository interfaces focused on persistence
- Map to DTOs before returning from controllers
- Add `@DataJpaTest` for new repository methods

### 2. jOOQ DSL - Use for:

✅ **When to use (future enhancements):**

- Complex reconciliation matching queries
- Multi-table joins for reconciliation analysis
- Board approval workflows with filters
- Reconciliation status dashboards

📁 **Planned examples:**

- `ReconciliationQueryDao` - Complex reconciliation queries
- `BoardReceiptQueryDao` - Receipt queries with filters
- `MatchingAnalysisDao` - Reconciliation matching analysis

💡 **Rules:**

- Inject `DSLContext` for all jOOQ operations
- Use type-safe DSL for matching logic
- Map results to DTOs using small mappers
- Test with Testcontainers or H2

### 3. jOOQ + SQL Templates - Use for:

✅ **When to use (future enhancements):**

- Reconciliation reports and dashboards
- Bank file processing statistics
- Board approval reports
- Analyst-maintained reconciliation queries

📁 **Planned file locations:**

- `src/main/resources/sql/reconciliation/matching_summary.sql`
- `src/main/resources/sql/board/approval_summary.sql`
- `src/main/resources/sql/reports/bank_file_processing_report.sql`

💡 **Rules:**

- Load templates via `SqlTemplateLoader` (create if needed)
- Keep column aliases stable
- Document templates in README
- Test template loading and execution

### Database Access Rules (ALL PATTERNS)

🔒 **Security & RLS:**

- **ALWAYS** set PostgreSQL session context before queries
- Use `RLSContext` or similar mechanism
- Set both `app.current_user_id` and `app.current_tenant_id`
- Never bypass RLS policies

🔄 **Transactions:**

- Use `@Transactional` for all write operations
- Consider `@Transactional(readOnly = true)` for reads
- Coordinate transactions for complex reconciliation workflows

✅ **Testing:**

- Test with board persona and admin roles
- Verify tenant isolation for reconciliation data
- Test matching logic thoroughly
- Test bank file ingestion with various formats

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

### Two Audit Mechanisms

| Mechanism                 | Purpose                                            | Implementation                                                  |
| ------------------------- | -------------------------------------------------- | --------------------------------------------------------------- |
| **API-Level Auditing**    | Log controller actions, endpoints, business events | `@Auditable` annotation on controllers                          |
| **Entity-Level Auditing** | Track data changes with tamper detection           | `@EntityListeners(SharedEntityAuditListener.class)` on entities |

### Configuration (DO NOT CHANGE)

```yaml
shared-lib:
  audit:
    enabled: true
    service-name: reconciliation-service
    source-schema: reconciliation
  entity-audit:
    enabled: true
    service-name: reconciliation-service
    source-schema: reconciliation
    source-table: transactions
```

### 1. API-Level Auditing with @Auditable

```java
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    @PostMapping("/bank-files")
    @Auditable(
        action = "BANK_FILE_UPLOADED",
        entityType = "BANK_FILE",
        description = "Bank file uploaded for processing"
    )
    public ResponseEntity<?> uploadBankFile(@RequestParam("file") MultipartFile file) {
        // Audit logged with endpoint, file metadata, trace_id
    }

    @PostMapping("/match")
    @Auditable(
        action = "RECONCILIATION_MATCHED",
        entityType = "RECONCILIATION",
        includeRequestBody = true
    )
    public ResponseEntity<?> matchTransactions(@RequestBody MatchRequest request) {
        // Business logic
    }
}
```

### 2. Entity-Level Auditing

```java
@Entity
@Table(name = "transactions", schema = "reconciliation")
@EntityListeners(SharedEntityAuditListener.class)
public class Transaction {
    // All changes tracked with before/after values + hash chain
}
```

### 3. Manual Auditing for Complex Operations

```java
@Autowired
private AuditTrailService auditTrailService;

public void processBankFile(BankFile file, Long userId) {
    List<Transaction> transactions = parseBankFile(file);
    auditTrailService.logAction(userId, "BANK_FILE_PROCESSED", "BANK_FILE",
        String.valueOf(file.getId()), file.getFileName(),
        String.format("Processed %d transactions from %s", transactions.size(), file.getFileName()),
        Map.of("transaction_count", transactions.size(), "file_type", file.getType()));
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

### Reconciliation Service Audit Checklist

- [ ] Bank file endpoints have `@Auditable`
- [ ] Reconciliation matching actions are logged
- [ ] Board approval endpoints have `@Auditable`
- [ ] Transaction/Reconciliation entities have `@EntityListeners`
- [ ] Import/export operations are tracked

**Troubleshooting:** Check `shared-lib.audit.enabled=true` | Verify DB grants | See audit-design.md

## Building and Testing

### Build Commands

```bash
# Clean build
mvn clean install

# Build without tests (use sparingly)
mvn clean install -DskipTests

# Build Docker image
docker build -t reconciliation-service:latest .

# Package for deployment
mvn clean package spring-boot:repackage

# Run jOOQ codegen (if configured)
mvn clean generate-sources
```

### Running Tests

- Use Testcontainers or H2 for database interactions
- Mock external dependencies using **Mockito**
- Write tests for:
  - Service layer business logic (reconciliation, matching, approval)
  - jOOQ queries (integration tests, when added)
  - API endpoints (use MockMvc)
  - Bank file ingestion with various file formats
  - RLS isolation

## Common Tasks

### Adding a New API Endpoint (e.g., GET /api/reconciliations/by-status/{status})

**Step 1: Consult Documentation**

- Read `documentation/LBE/reference/raw/RBAC/MAPPINGS/PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (sections 12, 17)
- Check reconciliation policies in `documentation/LBE/reference/policy-matrix.md`
- Review `documentation/LBE/guides/data-access-patterns.md`

**Step 2: Determine Data Access Pattern**

1. Simple reconciliation lookup by ID? → Use JPA Repository
2. List with complex filters (status, date range, matching confidence)? → Use jOOQ DSL (future)
3. Analyst-maintained report? → Use jOOQ + SQL Template (future)

**Step 3: Implement**

1. Create DTO classes in `dto/` package
2. Create appropriate DAO/Repository
3. Implement service layer business logic
4. Add controller method with OpenAPI annotations
5. Add authorization checks
6. Ensure RLS context is set

**Step 4: Register in Auth Catalog** (via auth-service)

1. Create migration to register endpoint in `auth.endpoints`
2. Link to policies via `auth.endpoint_policies`
3. Update `documentation/LBE/reference/raw/RBAC/MAPPINGS/PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (§12 or §17)

**Step 5: Test & Document**

1. Write unit tests for business logic
2. Write integration tests for database queries
3. Test with board/admin personas
4. Test tenant isolation
5. Update `documentation/LBE/reference/recent-updates.md`

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

### Multi-Tenancy and Data Isolation

- Tenant isolation enforced at database level via RLS
- Always include `tenantId` in audit logs and queries
- Never bypass tenant checks in application code
- Test isolation between different organizations

### Performance

- Use pagination for reconciliation list endpoints
- Consider caching for frequently accessed matching rules
- Monitor database connection pool usage
- Use database indexes appropriately (especially for matching queries)
- Profile matching logic for optimization

### Migrations and Schema Changes

- PostgreSQL is the primary database
- Schema changes via SQL migration scripts
- Configure `currentSchema=reconciliation` in datasource
- Test migrations on copy of production data
- Keep `ddl-auto: update` for development only
- Document in `documentation/LBE/reference/TABLE_NAMES_REFERENCE.md`

## Additional Resources

- Spring Boot: https://docs.spring.io/spring-boot/docs/3.2.5/reference/html/
- jOOQ: https://www.jooq.org/doc/latest/manual/
- PostgreSQL: https://www.postgresql.org/docs/current/
- MT940 Format: https://www.sepaforcorporates.com/swift-for-corporates/account-statement-mt940-file-format-overview/

---

# Reconciliation Service — Documentation Reference 📚

All domain, RBAC, and ops notes for this service live in the shared documentation project (`documentation/LBE`). **Always consult this documentation before implementing features or making changes**.

## Essential Reading (Start Here) 🎯

### Platform & Security Context

1. **`documentation/LBE/README.md`** – High-level auth + RLS flow this service plugs into
2. **`documentation/LBE/architecture/overview.md`** – System architecture including reconciliation service
3. **`documentation/LBE/guides/login-to-data.md`** – Board and admin personas: auth → reconciliation APIs

### Reconciliation Architecture

- **`documentation/LBE/architecture/request-lifecycle.md`** – Request flow for reconciliation operations
- **`documentation/LBE/architecture/policy-binding.md`** – Permission interconnections
- **`documentation/LBE/architecture/audit-design.md`** – Reconciliation section: audit logging requirements

## Implementation Guides (Use While Coding) 💻

### Data Access Patterns ⭐ CRITICAL ⭐

- **`documentation/LBE/guides/data-access-patterns.md`** – **Read before writing ANY database code**
  - Reconciliation service currently uses JPA
  - Guidance for adding jOOQ for complex matching queries
  - When to consider jOOQ + SQL templates for reports

### Security & Authorization

- **`documentation/LBE/foundations/access-control-101.md`** – RBAC fundamentals
- **`documentation/LBE/foundations/data-guardrails-101.md`** – RLS primer for reconciliation data
- **`documentation/LBE/guides/integrate-your-service.md`** – Connecting to auth service
- **`documentation/LBE/guides/verify-permissions.md`** – Testing reconciliation permissions

### Setup & Local Development

- **`documentation/LBE/guides/local-environment.md`** – Local setup instructions
- **`documentation/LBE/foundations/postgres-for-auth.md`** – PostgreSQL setup including reconciliation schema

## Quick Reference (Use During Development) 📖

### Reconciliation Specific References

#### Endpoint Mappings

- **`documentation/LBE/reference/raw/RBAC/MAPPINGS/PHASE5_ENDPOINT_POLICY_MAPPINGS.md`**
  - **Section 12** – MT940/VAN ingestion triggers (`system.ingestion.*` capabilities)
  - **Section 17** – Board receipt endpoints and required policies
    - `board.request.read`
    - `board.receipt.process`
    - `board.payment.reject`
  - Update these sections when adding/modifying reconciliation endpoints

#### Endpoint Categorization

- **`documentation/LBE/reference/raw/RBAC/MAPPINGS/PHASE1_ENDPOINTS_EXTRACTION.md`**
  - Category counts for MT940/VAN ingestion
  - Used by onboarding SQL

#### Capability Mappings

- **`documentation/LBE/reference/raw/RBAC/MAPPINGS/PHASE4_POLICY_CAPABILITY_MAPPINGS.md`**
  - Approvals & Reconciliation UI/page mappings per role
  - Update when new capabilities added

#### UI Pages & Actions

- **`documentation/LBE/reference/raw/RBAC/DEFINITIONS/PHASE2_UI_PAGES_ACTIONS.md`**
  - Defines "Approvals & Reconciliation" screens
  - Links to reconciliation service endpoints

#### Role Narratives

- **`documentation/LBE/reference/raw/ONBOARDING_ROLES.md`** – Role descriptions with reconciliation context
- **`documentation/LBE/reference/raw/RBAC/ROLES.md`** – Board and admin roles with reconciliation access

### General References

- **`documentation/LBE/reference/role-catalog.md`** – All roles (especially board and admin)
- **`documentation/LBE/reference/policy-matrix.md`** – Policy mappings for reconciliation operations
- **`documentation/LBE/reference/TABLE_NAMES_REFERENCE.md`** – Canonical `reconciliation` schema description
- **`documentation/LBE/reference/audit-quick-reference.md`** – Audit requirements for reconciliation service
- **`documentation/LBE/reference/recent-updates.md`** – November 2025 PostgreSQL alignment entry

### PostgreSQL Operations

- **`documentation/LBE/reference/postgres-operations.md`** – PostgreSQL operations guidance
- **`documentation/LBE/reference/raw/POSTGRES/README.md`** – PostgreSQL migration guidance
  - Includes `currentSchema=reconciliation` datasource setup

## Troubleshooting & Operations 🔧

### Problem Resolution

- **`documentation/LBE/playbooks/troubleshoot-auth.md`** – Auth/authorization troubleshooting
  - JWT validation issues affecting reconciliation access
  - RLS context problems
  - Policy resolution failures

### Operational References

- **`documentation/LBE/reference/postgres-operations.md`** – PostgreSQL operations for reconciliation schema
- **`documentation/LBE/foundations/postgres-for-auth.md`** – Database role management

## Maintenance Checklist ✅

### When Adding/Modifying Reconciliation or Board Endpoints

1. ✅ Determine data access pattern from `documentation/LBE/guides/data-access-patterns.md`
2. ✅ Implement with appropriate pattern (JPA for now, consider jOOQ for complex queries)
3. ✅ Define endpoint with OpenAPI annotations
4. ✅ Add authorization checks (consult policy-matrix.md)
5. ✅ Ensure RLS context is set
6. ✅ Register in auth-service: `auth.endpoints` + `auth.endpoint_policies`
7. ✅ Update sections 12 or 17 in `PHASE5_ENDPOINT_POLICY_MAPPINGS.md`
8. ✅ Update `documentation/LBE/reference/policy-matrix.md`
9. ✅ Test with board/admin personas
10. ✅ Test tenant isolation
11. ✅ Document in `documentation/LBE/reference/recent-updates.md`

### When Changing Reconciliation Schema

1. ✅ Write migration script
2. ✅ Update `documentation/LBE/reference/TABLE_NAMES_REFERENCE.md`
3. ✅ Update `documentation/LBE/architecture/data-map.md` if relationships change
4. ✅ Verify `currentSchema=reconciliation` still works
5. ✅ Test RLS policies still work correctly
6. ✅ Document in `documentation/LBE/reference/recent-updates.md`

### When Modifying Audit/Logging

1. ✅ Confirm config matches `documentation/LBE/reference/audit-quick-reference.md`
2. ✅ Ensure `service_name` = `reconciliation-service` and `source_schema` = `reconciliation`
3. ✅ Update Reconciliation subsection in `documentation/LBE/architecture/audit-design.md`
4. ✅ Verify compliance requirements still met

### When Adding jOOQ Queries (Future Enhancement)

1. ✅ Consult `documentation/LBE/guides/data-access-patterns.md`
2. ✅ Configure jOOQ and `DSLContext`
3. ✅ Create DAO classes for complex queries
4. ✅ Test with Testcontainers or H2
5. ✅ Document pattern in README
6. ✅ Note in `documentation/LBE/reference/recent-updates.md`

### Major Releases

1. ✅ Capture summary in `documentation/LBE/reference/recent-updates.md`
2. ✅ Update any changed endpoint mappings (§12, §17)
3. ✅ Review and update affected guides
4. ✅ Notify other services if reconciliation APIs changed

## Key Principles 🎯

### Security First 🔒

- ✅ Always set RLS context before queries
- ✅ Validate reconciliation authorization with policies
- ✅ Never bypass tenant checks
- ✅ Never log sensitive bank/reconciliation data
- ✅ Follow `documentation/LBE/foundations/data-guardrails-101.md`

### Data Access Pattern Discipline 💾

- ✅ **Always** consult `documentation/LBE/guides/data-access-patterns.md` first
- ✅ Use JPA for writes and simple reads (current pattern)
- ✅ Consider jOOQ DSL for complex reconciliation matching queries (future)
- ✅ Consider jOOQ + SQL templates for analyst-maintained reports (future)
- ✅ Test all patterns thoroughly

### Documentation Driven 📝

- ✅ Read relevant docs BEFORE coding
- ✅ Update docs WITH your code changes
- ✅ Keep endpoint mappings current (§12, §17)
- ✅ Document bank file formats clearly

### Test Comprehensively 🧪

- ✅ Test with board and admin personas
- ✅ Test tenant isolation
- ✅ Test authorization (RBAC)
- ✅ Test bank file parsing with various formats
- ✅ Test matching logic thoroughly
- ✅ Follow `documentation/LBE/guides/verify-permissions.md`

## Quick Links by Task 🔗

| Task                                      | Primary Documentation                                                                         |
| ----------------------------------------- | --------------------------------------------------------------------------------------------- |
| Setting up local environment              | `documentation/LBE/guides/local-environment.md`                                               |
| Understanding reconciliation architecture | `documentation/LBE/architecture/overview.md`                                                  |
| **Choosing data access pattern**          | **`documentation/LBE/guides/data-access-patterns.md`** ⭐                                     |
| Finding reconciliation endpoint policies  | `documentation/LBE/reference/raw/RBAC/MAPPINGS/PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (§12, §17) |
| Adding new reconciliation endpoint        | `documentation/LBE/guides/extend-access.md`                                                   |
| Understanding board roles                 | `documentation/LBE/reference/role-catalog.md`                                                 |
| Debugging authorization                   | `documentation/LBE/playbooks/troubleshoot-auth.md`                                            |
| Understanding RLS for reconciliation      | `documentation/LBE/foundations/data-guardrails-101.md`                                        |
| Reconciliation schema reference           | `documentation/LBE/reference/TABLE_NAMES_REFERENCE.md`                                        |
| PostgreSQL reconciliation config          | `documentation/LBE/reference/raw/POSTGRES/README.md`                                          |
| Checking recent changes                   | `documentation/LBE/reference/recent-updates.md`                                               |

---

**Remember**: The documentation in `documentation/LBE/` is the single source of truth. Always consult it before making changes, and update it along with your code changes. Reconciliation service currently uses JPA—consult data-access-patterns.md before adding complex queries!

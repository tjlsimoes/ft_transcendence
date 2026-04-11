# Database Operations and Management

This document outlines the operational procedures for the PostgreSQL database, including persistence and schema migration workflows.

## Persistence Architecture

Data persistence is implemented through Docker host-path volumes. The internal PostgreSQL data directory is mapped to the host filesystem to ensure data survival across container lifecycle events.

### Configuration

The following volume mapping is defined in the `docker-compose.yml` file:

```yaml
    volumes:
      - ./database/data:/var/lib/postgresql
```

### Persistence Guarantees

*   **Container Restarts**: Database state remains unaffected by container stops or restarts.
*   **Container Deletion**: State is preserved even if containers are removed (`docker compose down`), provided the `./database/data` directory on the host remains intact.
*   **Initialization Integrity**: The PostgreSQL initialization process only executes if the data directory is empty. Consequently, existing data is never overwritten by a container restart.

## Schema Migration Workflow

Database schema evolution is managed by **Flyway**, integrated into the Spring Boot backend application.

### Migration Discovery and Execution

1.  **Migration Location**: SQL migration scripts must be placed in `backend/src/main/resources/db/migration/`.
2.  **Naming Convention**: Files must follow the `V<Version>__<Description>.sql` pattern (e.g., `V2__add_user_profile.sql`).
3.  **Application Startup**: Upon backend startup, Flyway scans the migration directory and compares its contents with the `flyway_schema_history` table.
4.  **Execution**: Any new migrations are executed sequentially, and the schema history table is updated to reflect the new version.

### Flyway and Non-Empty Databases

When connecting Flyway to a database that already contains tables but lacks a `flyway_schema_history` table (common with managed cloud providers like Neon), Flyway will refuse to migrate by default to prevent accidental data corruption.

### Baseline Behavior and Schema Alignment

The "Baseline Version" (default is **1**) represents the starting point Flyway uses to coordinate its history on a non-empty database. When `spring.flyway.baseline-on-migrate=true` is enabled, the system operates under the following logic:

*   **Assumption**: Flyway assumes the database is already successfully initialized up to and including the state defined in the baseline version (e.g., `V1__init.sql`).
*   **Action**: Flyway creates the `flyway_schema_history` table and marks the baseline version as `Already Applied`.
*   **Result**: The corresponding SQL migration script (e.g., `V1__init.sql`) is **skipped** and never executed against the database.

If the existing database schema is incomplete or outdated relative to the baseline script (e.g., missing expected columns), Hibernate's validation (`spring.jpa.hibernate.ddl-auto=validate`) will fail. This failure occurs because Hibernate expects a schema that matches the baseline version, which Flyway assumed was already present.

#### Remediation

*   **Fresh Initialization**: In development environments, it is recommended to drop the `public` schema (`DROP SCHEMA public CASCADE; CREATE SCHEMA public;`) to allow Flyway to execute the baseline migrations on a clean state.
*   **Manual Alignment**: In environments where data must be preserved, schema missing from the baseline version must be applied manually to align the database state with the JPA entity models.

### Summary of Operational Scripts

The system distinguishes between **Bootstrap** scripts (Docker-level) and **Migration** scripts (Application-level).

*   **Initialization Script (`database/init.sql`)**: 
    *   **Architectural Placeholder**: This file is required for the initial PostgreSQL Docker container initialization. Removing it may result in container startup warnings.
    *   **No-op Status**: The script is currently a "no-op" (no operation), meaning it contains SQL commands (e.g., `SELECT 1;`) that perform no functional changes. It is reserved for future system-level tasks such as role creation or global extension configuration.
*   **Schema Validation**: The `spring.jpa.hibernate.ddl-auto=validate` setting ensures that the Java entity models remain consistent with the database schema as defined by Flyway, without attempting to modify the schema directly.

---

## Team Environment Safety (Shared Databases)

In environments where multiple developers connect to a shared database instance (e.g., a managed Neon database), Flyway provides mechanisms to prevent data loss and schema inconsistency.

### Conflict Prevention

*   **Idempotent Execution**: Flyway only executes migration scripts with a version number strictly greater than the one recorded in the `flyway_schema_history` table. Concurrent application startups do not result in redundant or destructive executions.
*   **Checksum Validation**: Each applied migration is recorded with a CRC32 checksum. Any modification to a previously executed migration file will trigger a validation failure on startup, preventing the application from running against an inconsistent schema.
*   **Version Locking**: Flyway utilizes database-level locking during the migration process to ensure that only one instance coordinates schema updates at any given time.

### Operational Recommendations

*   **Version Coordination**: Team members must coordinate the assignment of version numbers to avoid collisions (e.g., two developers creating `V3` simultaneously).
*   **Non-Destructive Migrations**: For shared development environments, migrations should focus on additive changes. Destructive operations (e.g., dropping columns) require explicit team synchronization to avoid breaking dependent codebases.

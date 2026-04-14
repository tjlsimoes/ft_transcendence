# Development Workflow Guide

This guide explains how to efficiently develop Code Arena in 2026, leveraging modern tools and a "Hybrid" workflow.

## 1. The "Hybrid" Workflow

While Docker is optimal for infrastructure and production, it may be less efficient for active development. A **Hybrid** approach is recommended:

### Infrastructure in Docker
Heavy lifting is handled within Docker to ensure that the database and cache are always configured correctly.
```bash
# Option A: Full Infrastructure (Local DB)
docker compose up -d db cache

# Option B: Cloud-Hybrid (Neon DB)
docker compose up -d cache
```
*Note: The `proxy` service is omitted during active development to simplify hot-reloading.*

### Application code on Host
Running code natively on the host machine provides **instant hot-reloading** and **IDE debugging**.

#### Frontend (Angular 21)
```bash
cd frontend
npm install
npm start  # Runs 'ng serve'
```
*Access at: http://localhost:4200 (or redirected via Proxy)*

#### Backend (Spring Boot 4)
When running on the Host, the `.env` variables must be loaded manually (a task typically handled by Docker):

```bash
cd backend
chmod u+x mvnw
# Load .env and run
export $(grep -v '^#' ../.env | xargs) && ./mvnw spring-boot:run
```
*Access at: http://localhost:${BACKEND_PORT:-8080}*

---

## 2. Dealing with CORS

When the Nginx proxy is omitted, the Frontend (`:${FRONTEND_PORT:-80}`) and Backend (`:${BACKEND_PORT:-8080}`) operate on different ports. This triggers **CORS** (Cross-Origin Resource Sharing) security.

### Choice A: Angular Proxy (Recommended)
Configure the Angular CLI to proxy `/api` requests to the backend during development.
1. Create `frontend/proxy.conf.json`:
   ```json
   { "/api": { "target": "http://localhost:${BACKEND_PORT:-8080}", "secure": false } }
   ```
2. Run with: `ng serve --proxy-config proxy.conf.json`.

### Choice B: Spring Boot CORS
Add `@CrossOrigin("http://localhost:${FRONTEND_PORT:-80}")` to the controllers or apply a global configuration in Java.

---

## 3. Database Schema Migrations

Flyway is utilized for database schema management. Schema modifications require the addition of a new `.sql` script in the following directory:
`backend/src/main/resources/db/migration/`

These scripts are automatically executed by the Spring Boot backend upon application startup, regardless of whether a local or cloud-based database is employed. Manual modification of `database/init.sql` is prohibited for schema changes.

---

## 4. Cloud Database Integration (Neon)

Managed cloud databases, such as [Neon](https://neon.tech), can be integrated into the development environment.

### 4.1. Configuration via Environment Variables

To utilize a Neon database, the following environment variables in the `.env` file must be updated:
- `DB_HOST`: Set to the Neon hostname (e.g., `ep-pretty-dawn-123.us-east-2.aws.neon.tech`).
- `DB_PORT`: `5432`.
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: Assigned Neon credentials.
- `DB_SSL_MODE`: `require` (SSL is mandatory for Neon connections).

### 4.2. Local Database Deactivation

When using a cloud-based database, the local `codearena-db` container is not required. It can be stopped or deactivated to conserve system resources:
- To stop: `docker compose stop db`
- To deactivate permanently: `docker compose up -d --scale db=0`

> [!IMPORTANT]
> If the `db` service is deactivated in `docker-compose.yml`, it must also be removed from the `depends_on` list in the `backend` service. Failure to do so will cause the backend to stall during startup.

### 4.3. Remote Database Integration Workflow

To utilize a remote database while maintaining the local environment as a backup:

1.  **Connection String acquisition**: Obtain the PostgreSQL connection URL from the Neon dashboard.
2.  **Hybrid Workflow**:
    - When executed natively on the host, the backend connects directly to the Neon instance.
    - When executed via Docker, the `db` service can be commented out in `docker-compose.yml` to save resources.

> [!TIP]
> This configuration facilitates the sharing of a consistent database state across a development team without the need for manual SQL synchronization.

---

## 5. Best Practices
- **Linting**: Utilize the provided ESLint and Prettier configurations within the IDE.
- **Hot Reload**: Both Angular and Spring Boot (via devtools) support hot-reloading. Restarting is not required for most changes.
- **Nginx Templating**: 
    - Always edit the `.template` files in `infra/nginx/` or `frontend/`. 
    - Changes to processed `.conf` files inside containers will be overwritten on the next startup as `envsubst` runs.
    - If you add a new environment variable to a template, ensure it is also added to the `environment` block of the respective service in `docker-compose.yml`.

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

## 3. Dealing with CORS

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

## 4. Best Practices
- **Linting**: Utilize the provided ESLint and Prettier configurations within the IDE.
- **Hot Reload**: Both Angular and Spring Boot (via devtools) support hot-reloading. Restarting is not required for most changes.
- **Database Migrations**: SQL scripts should be added to `database/init.sql`. Docker executes these automatically upon the clearance of the `database-data` volume.
- **Nginx Templating**: 
    - Always edit the `.template` files in `infra/nginx/` or `frontend/`. 
    - Changes to processed `.conf` files inside containers will be overwritten on the next startup as `envsubst` runs.
    - If you add a new environment variable to a template, ensure it is also added to the `environment` block of the respective service in `docker-compose.yml`.

## 4. Cloud Database Integration (Neon)

If you prefer using a managed cloud database like **Neon**, follow these steps:

1.  **Update `.env`**:
    - `DB_HOST`: Set to your Neon hostname (e.g., `ep-pretty-dawn-123.us-east-2.aws.neon.tech`).
    - `DB_PORT`: `5432`.
    - `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: Use your Neon credentials.
    - `DB_SSL_MODE`: `require` (Neon requires SSL).
2.  **Disable Local DB**:
    - To save resources, you don't need the local `codearena-db` container.
    - Stop it specifically: `docker compose stop db`.
    - Or keep it down permanently: `docker compose up -d --scale db=0`.
3.  **Inheritance**: The Backend will automatically pick up these variables and connect to the cloud instead of the local network.

---

## 3. Remote Database Integration (Neon)

To use a remote database (like [Neon](https://neon.tech)) while keeping the local setup as a backup:

1.  **Connection String acquisition**: Obtain the Postgres URL from the Neon dashboard.
2.  **Update `.env`**:
    - `DB_HOST`: Set to the Neon host (e.g., `ep-cool-fog-12345.us-east-2.aws.neon.tech`).
    - `POSTGRES_USER` & `POSTGRES_PASSWORD`: Use Neon credentials.
    - `POSTGRES_DB`: Usually `neondb`.
    - `DB_SSL_MODE`: Set to `require`.
3.  **Hybrid Workflow**:
    - When running locally (on Host), the application connects to Neon directly.
    - When running via Docker, the `db` service can be **commented out** in `docker-compose.yml` to save resources:
      ```yaml
      # docker-compose.yml
      # db:
      #   image: codearena-db:1.0
      #   ...
      ```

> [!TIP]
> This is ideal for sharing the same database state across the team without manual SQL syncs!

> If the `db` service is commented out in `docker-compose.yml` to use Neon, it must also be removed from the `depends_on` list in the `backend` service. Failure to do so will cause the backend to stall while waiting for a missing healthcheck.

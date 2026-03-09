# Development Workflow Guide

This guide explains how to efficiently develop Code Arena in 2026, leveraging modern tools and a "Hybrid" workflow.

## 1. The "Hybrid" Workflow

While Docker is excellent for infrastructure and production, it can be slow for active development (requiring rebuilds). We recommend a **Hybrid** approach:

### Infrastructure in Docker
Keep the heavy lifting in Docker. This ensures your DB and Cache are always configured correctly.
```bash
# Option A: Full Infrastructure (Local DB)
docker compose up -d db cache

# Option B: Cloud-Hybrid (Neon DB)
docker compose up -d cache
```
*Note: We skip `proxy` during active development to simplify hot-reloading.*

### Application code on Host
Run your code natively on your host machine for **instant hot-reloading** and **IDE debugging**.

#### Frontend (Angular 21)
```bash
cd frontend
npm install
npm start  # Runs 'ng serve'
```
*Access at: http://localhost:4200 (or redirected via Proxy)*

#### Backend (Spring Boot 4)
When running on the Host, you must manually load the `.env` variables (which Docker usually handles for you):

```bash
cd backend
chmod u+x mvnw
# Load .env and run
export $(grep -v '^#' ../.env | xargs) && ./mvnw spring-boot:run
```
*Access at: http://localhost:8080*

---

## 3. Dealing with CORS

When you skip the Nginx proxy, your Frontend (`:4200`) and Backend (`:8080`) run on different ports. This triggers **CORS** (Cross-Origin Resource Sharing) security.

### Choice A: Angular Proxy (Recommended)
Configure the Angular CLI to proxy `/api` requests to the backend during development.
1. Create `frontend/proxy.conf.json`:
   ```json
   { "/api": { "target": "http://localhost:8080", "secure": false } }
   ```
2. Run with: `ng serve --proxy-config proxy.conf.json`.

### Choice B: Spring Boot CORS
Add `@CrossOrigin("http://localhost:4200")` to your controllers or a global configuration in Java.

---

## 4. Best Practices
- **Linting**: Use the provided ESLint and Prettier configs in your IDE.
- **Hot Reload**: Both Angular and Spring Boot (via devtools) support hot-reloading. You don't need to restart them for most changes.
- **Database Migrations**: Add your SQL scripts to `database/init.sql`. Docker will run them automatically if you clear the `database-data` volume.

---

## 3. Remote Database Integration (Neon)

To use a remote database (like [Neon](https://neon.tech)) while keeping your local setup as a backup:

1.  **Get your connection string**: Obtain the Postgres URL from your Neon dashboard.
2.  **Update `.env`**:
    - `DB_HOST`: Set to your Neon host (e.g., `ep-cool-fog-12345.us-east-2.aws.neon.tech`).
    - `POSTGRES_USER` & `POSTGRES_PASSWORD`: Use your Neon credentials.
    - `POSTGRES_DB`: Usually `neondb`.
    - `DB_SSL_MODE`: Set to `require`.
3.  **Hybrid Workflow**:
    - When running locally (on Host), your app will connect to Neon directly.
    - When running via Docker, you can **comment out** the `db` service in `docker-compose.yml` to save resources:
      ```yaml
      # docker-compose.yml
      # db:
      #   image: codearena-db:1.0
      #   ...
      ```

> [!TIP]
> This is ideal for sharing the same database state across your team without manual SQL syncs!

> [!CAUTION]
> If you comment out the `db` service in `docker-compose.yml` to use Neon, you must also remove `db` from the `depends_on` list in the `backend` service. Otherwise, Docker will fail to start the backend while waiting for a missing healthcheck!

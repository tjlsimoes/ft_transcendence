# Service Configuration Details

This document outlines the specific configuration and Docker setup for each service in the Code Arena ecosystem.

## Common Infrastructure

### Base Image Strategy
Due to network restrictions on some host environments, all custom images (`frontend`, `backend`, `proxy`) use the **cached** `eclipse-temurin:21-jre-alpine` as their base image. This ensures builds can proceed by installing necessary packages (like `nodejs` or `nginx`) via `apk` rather than pulling new base images.

---

## 1. Frontend (Angular)
- **Base Image**: `eclipse-temurin:21-jre-alpine`
- **Build Stage**:
    - Installs `nodejs` and `npm`.
    - Runs `npm install` and `npm run build -- --configuration production`.
- **Runtime Stage**:
    - Installs `nginx`.
    - Serves files from `/usr/share/nginx/html`.
- **Key Options**:
    - `NG_BUILD_CACHE`: Enabled by default in Angular builds.
    - `nginx.conf`: Custom configuration to handle SPA routing (`try_files $uri $uri/ /index.html`).

## 2. Backend (Spring Boot)
- **Base Image**: `maven:3.9.6-eclipse-temurin-21` (Build) / `eclipse-temurin:21-jre-alpine` (Runtime)
- **Build Stage**:
    - Multi-stage build to minimize runtime image size.
    - `mvn dependency:go-offline` used to cache dependencies.
- **Health Checks**:
    - Uses `wget` (since `curl` is absent) to probe `http://localhost:8080/api/health`.
- **Environment Support**:
    - `SPRING_PROFILES_ACTIVE`: Configurable via `.env` (default: `dev`).

## 3. Proxy (Nginx)
- **Base Image**: `eclipse-temurin:21-jre-alpine`
- **Port Mapping**:
    - **Host 8000** → **Container 80** (Avoids host-level conflicts).
    - **Host 443** → **Container 443**.
- **Volume Mounts**:
    - `./proxy/nginx.conf`: Main configuration.
    - `./proxy/ssl`: TLS certificates.

## 4. Database (PostgreSQL)
- **Image**: `postgres:15-alpine`
- **Persistence**: Initialized via `./database/init.sql`.
- **Health Check**:
    - `pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}`.

## 5. Cache (Redis)
- **Image**: `redis:7-alpine`
- **Persistence**: Ephemeral by default for the setup phase.
- **Health Check**:
    - `redis-cli ping`.

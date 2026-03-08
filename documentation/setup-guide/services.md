# Service Configuration Details

This document outlines the specific configuration and Docker setup for each service in the Code Arena ecosystem.

## Common Infrastructure

### Base Image Strategy
The project uses specialized **Alpine-based** official images to ensure minimal overhead and small image sizes:
- **Frontend**: Uses `node:20-alpine` for building and `nginx:alpine` for serving.
- **Backend**: Uses `maven:3.9.6-eclipse-temurin-21` for building and `eclipse-temurin:21-jre-alpine` for runtime (as it requires a JRE).
- **Proxy**: Uses `nginx:alpine`.

---

## 1. Frontend (Angular)
- **Base Image**: `node:20-alpine` (Build) / `nginx:alpine` (Runtime)
- **Build Stage**:
    - Runs `npm install` and `npm run build -- --configuration production`.
- **Runtime Stage**:
    - Serves static files from `/usr/share/nginx/html`.
- **Key Options**:
    - `nginx.conf`: Custom configuration to handle SPA routing.

## 2. Backend (Spring Boot)
- **Base Image**: `maven:3.9.6-eclipse-temurin-21` (Build) / `eclipse-temurin:21-jre-alpine` (Runtime)
- **Build Stage**:
    - Multi-stage build to minimize runtime image size.
- **Health Checks**:
    - Uses `wget` to probe `http://localhost:8080/api/health`.

## 3. Proxy (Nginx)
- **Base Image**: `nginx:alpine`
- **Port Mapping**:
    - **Host 8000** → **Container 80** (Handles HTTP -> HTTPS redirect).
    - **Host 443** → **Container 443** (Main HTTPS entry point).
- **Volume Mounts**:
    - `./infra/nginx/nginx.conf`: Main configuration.
    - `./infra/nginx/ssl`: TLS certificates.

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

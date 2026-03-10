# Service Configuration Details

This document outlines the specific configuration and Docker setup for each service in the Code Arena ecosystem.

## Common Infrastructure

### Base Image Strategy
The project uses specialized **Alpine-based** official images to ensure minimal overhead and small image sizes:
- **Frontend**: Uses `node:21-alpine` for building and `nginx:1.27.4-alpine-slim` for serving.
- **Backend**: Uses `maven:3.9.12-eclipse-temurin-25` for building and `eclipse-temurin:25.0.1_8-jre-alpine` for runtime.
- **Proxy**: Uses `nginx:1.28.2-alpine-slim`.

---

## 1. Frontend (Angular)
- **Base Image**: `node:20-alpine` (Build) / `nginx:alpine` (Runtime)
- **Build Stage**:
    - Runs `npm install` and `npm run build -- --configuration production`.
- **Runtime Stage**:
    - Serves static files from `/usr/share/nginx/html`.
    - **Dynamic Configuration**: Uses `nginx.conf.template` and `envsubst` to listen on `${FRONTEND_PORT}`.

## 2. Backend (Spring Boot)
- **Base Image**: `maven:3.9.6-eclipse-temurin-21` (Build) / `eclipse-temurin:21-jre-alpine` (Runtime)
- **Build Stage**:
    - Multi-stage build to minimize runtime image size.
- **Health Checks**:
    - Uses `wget` to probe `http://localhost:8080/api/health`.

## 3. Proxy (Nginx)
- **Base Image**: `nginx:1.28.2-alpine-slim`
- **Dynamic Port Mapping**:
    - **Host `${HOST_TO_PROXY_HTTP_PORT}`** → **Container `${PROXY_INTERNAL_HTTP_PORT}`**.
    - **Host `${HOST_TO_PROXY_HTTPS_PORT}`** → **Container `${PROXY_INTERNAL_HTTPS_PORT}`**.
- **Configuration**:
    - Uses `envsubst` to dynamically set up upstream routing for `${BACKEND_PORT}` and `${FRONTEND_PORT}`.
- **Volume Mounts**:
    - `./infra/nginx/nginx.conf.template`: Source for dynamic config.
    - `./infra/nginx/ssl`: TLS certificates.

## 4. Database (PostgreSQL)
- **Image**: `postgres:18.3-alpine3.23`
    - `./database/data:/var/lib/postgresql`: Mapped for version-specific data storage (PostgreSQL 18+).
- **Health Check**:
    - `pg_isready -U \${POSTGRES_USER} -d \${POSTGRES_DB}`.

## 5. Cache (Redis)
- **Image**: `redis:8.6.1-alpine3.23`
- **Security**: Enforced via `--requirepass \${REDIS_PASSWORD}`.
- **Health Check**:
    - `redis-cli -a \${REDIS_PASSWORD} ping`.

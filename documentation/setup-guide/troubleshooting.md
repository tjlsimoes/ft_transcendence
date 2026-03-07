# Troubleshooting Guide

Common issues encountered during the initial setup and their resolutions.

## 1. Port 80 Conflict
**Problem**: Docker fails to start the `proxy` service because port 80 is already in use by another process (e.g., Apache, Nginx, or a background service).
**Solution**: The host port for HTTP has been moved to **8000** in `docker-compose.yml`. Use `https://localhost` (port 443) or `http://localhost:8000`.

## 2. Docker Compose vs Docker-Compose
**Problem**: Command `docker compose` is not found.
**Solution**: Depending on your Docker installation, you may need to use the legacy hyphenated command `docker-compose`. The setup has been verified with `docker-compose`.

## 3. Network Unreachable (Building Images)
**Problem**: `docker build` fails because it cannot pull base images (e.g., `node:20-alpine`) due to network or IPv6 issues.
**Solution**: Dockerfiles have been updated to use the **cached** `eclipse-temurin:21-jre-alpine` image and install dependencies via `apk`.

## 4. Backend Health "Starting" Forever
**Problem**: The `codearena-backend` container shows `(health: starting)` and never becomes `healthy`.
**Solution**: 
1. Check if Spring Security is blocking the health check (should be permitted in `SecurityConfig.java`).
2. Verify that the health check command in `docker-compose.yml` uses `wget` if `curl` is not installed in the image.

## 5. Browser "Connection Refused"
**Problem**: Accessing `localhost` fails even if containers are running.
**Solution**: Ensure you are using the correct protocol and port.
- **HTTPS**: `https://localhost`
- **HTTP**: `http://localhost:8000`

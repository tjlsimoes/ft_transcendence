# Troubleshooting Guide

Common issues encountered during the initial setup and their resolutions.

## 1. Port 80 Conflict
**Problem**: Docker fails to start the `proxy` service because port 80 is already in use by another process (e.g., Apache, Nginx, or a background service).
**Solution**: The host port for HTTP has been moved to **8000** in `docker-compose.yml`. Use `https://localhost` (port 443) or `http://localhost:8000`.

## 2. Docker Compose vs Docker-Compose
**Problem**: Command `docker compose` is not found.
**Solution**: Installation of a more recent version of docker compose is required.

## 3. Backend Health "Starting" Forever
**Problem**: The `codearena-backend` container shows `(health: starting)` and never becomes `healthy`.
**Solution**: 
1. Check if Spring Security is blocking the health check (should be permitted in `SecurityConfig.java`).
2. Verify that the health check command in `docker-compose.yml` uses `wget` if `curl` is not installed in the image.

## 4. Browser "Connection Refused"
**Problem**: Accessing `localhost` fails even if containers are running.
**Solution**: Verification of the correct protocol and port is required.
- **HTTPS**: `https://localhost`
- **HTTP**: `http://localhost:8000`

## 5. Redis: "Memory overcommit must be enabled!"
If the following warning appears in the Redis logs:
`WARNING Memory overcommit must be enabled! [...] To fix this issue add 'vm.overcommit_memory = 1' to /etc/sysctl.conf`

**Resolution**:
This is a host-level Linux kernel setting. Remediation on the host machine:
1. Run: `sudo sysctl vm.overcommit_memory=1` (Immediate effect)
2. To make it persistent, add `vm.overcommit_memory = 1` to `/etc/sysctl.conf`.
3. Restart the containers: `./setup.sh` (Choose option 3).

## 6. "The legacy builder is deprecated"
If the following warning appears during `docker-compose up`:
`DEPRECATED: The legacy builder is deprecated and will be removed in a future release.`

**Resolution**:
The `setup.sh` script is configured to utilize `docker compose` (v2), which employs BuildKit. Utilization of the `setup.sh` script is recommended instead of direct calls to legacy `docker-compose`.

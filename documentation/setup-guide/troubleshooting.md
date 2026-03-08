# Troubleshooting Guide

Common issues encountered during the initial setup and their resolutions.

## 1. Port 80 Conflict
**Problem**: Docker fails to start the `proxy` service because port 80 is already in use by another process (e.g., Apache, Nginx, or a background service).
**Solution**: The host port for HTTP has been moved to **8000** in `docker-compose.yml`. Use `https://localhost` (port 443) or `http://localhost:8000`.

## 2. Docker Compose vs Docker-Compose
**Problem**: Command `docker compose` is not found.
**Solution**: Depending on your Docker installation, you may need to use the legacy hyphenated command `docker-compose`. The setup has been verified with `docker-compose`.

## 3. Backend Health "Starting" Forever
**Problem**: The `codearena-backend` container shows `(health: starting)` and never becomes `healthy`.
**Solution**: 
1. Check if Spring Security is blocking the health check (should be permitted in `SecurityConfig.java`).
2. Verify that the health check command in `docker-compose.yml` uses `wget` if `curl` is not installed in the image.

## 4. Browser "Connection Refused"
**Problem**: Accessing `localhost` fails even if containers are running.
**Solution**: Ensure you are using the correct protocol and port.
- **HTTPS**: `https://localhost`
- **HTTP**: `http://localhost:8000`

## 5. Redis: "Memory overcommit must be enabled!"
If you see this warning in the Redis logs:
`WARNING Memory overcommit must be enabled! [...] To fix this issue add 'vm.overcommit_memory = 1' to /etc/sysctl.conf`

**Resolution**:
This is a host-level Linux kernel setting. To fix it on your host machine:
1. Run: `sudo sysctl vm.overcommit_memory=1` (Immediate effect)
2. To make it persistent, add `vm.overcommit_memory = 1` to `/etc/sysctl.conf`.
3. Restart the containers: `./setup.sh` (Choose option 3).

## 6. "The legacy builder is deprecated"
If you see this warning during `docker-compose up`:
`DEPRECATED: The legacy builder is deprecated and will be removed in a future release.`

**Resolution**:
We've updated `setup.sh` to prefer `docker compose` (v2), which uses BuildKit. Ensure you are using the `setup.sh` script instead of calling legacy `docker-compose` directly.

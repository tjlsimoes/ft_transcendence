# Networking & Security

This document covers the networking layout, HTTPS configuration, and security measures implemented in the Code Arena setup.

## 1. Network Layout

All services are part of a shared bridge network defined in `docker-compose.yml`:

| Service | Hostname | Internal Port | Accessible from Host? |
|---------|----------|---------------|-----------------------|
| Proxy | `proxy` | 80, 443 | **Yes** (8000, 443) |
| Frontend | `frontend` | 80 | No |
| Backend | `backend` | 8080 | No |
| Database | `db` | 5432 | No |
| Cache | `cache` | 6379 | No |

### Reverse Proxy Logic
The `proxy` container handles all incoming traffic and routes it based on the URL path:
- `https://localhost/` → `http://frontend:80` (Angular)
- `https://localhost/api/*` → `http://backend:8080/api/*` (REST API)
- `https://localhost/ws/*` → `http://backend:8080/ws/*` (WebSockets)

A permanent redirect from HTTP (port 80) to HTTPS (port 443) is enforced.

### HTTPS & TLS
A self-signed certificate is generated for local development.
- **Certificate**: `proxy/ssl/localhost.crt`
- **Private Key**: `proxy/ssl/localhost.key`

> [!WARNING]
> Browsers typically flag self-signed certificates as untrusted. Manual selection of "Proceed to localhost (unsafe)" is required during the initial visit.

### Spring Security (Backend)
The backend uses Spring Security to protect endpoints.
- **Health Check**: `/api/health` is explicitly permitted for anonymous access to facilitate Docker health probes.
- **CSRF**: Disabled for development to simplify initial setup.

### Environment Secrecy
- `.env.example`: Committed to VCS as a template.
- `.env`: **Git-ignored**. Contains sensitive secrets like `POSTGRES_PASSWORD`.

## 3. Container Isolation
- **No Direct Access**: Only the `proxy` service exposes ports to the host machine.
- **Internal DNS**: Services resolve each other using Docker's internal DNS, preventing hardcoded IP addresses.

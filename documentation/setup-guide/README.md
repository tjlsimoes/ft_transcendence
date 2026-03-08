# Setup Guide — Code Arena

Welcome to the internal setup and architecture documentation for the Code Arena project.

## Guide Contents

1. [**Architecture Overview**](architecture.md)
   - High-level system design and component interaction diagram.
2. [**Service Details**](services.md)
   - Deep dive into Frontend, Backend, Database, Cache, and Proxy configurations.
3. [**Networking & Security**](network-security.md)
   - HTTPS, reverse proxy logic, and Spring Security setup.
4. [**Troubleshooting**](troubleshooting.md)
   - Solutions for common startup errors and environment issues.

---

## Quick Start (Summary)

To bring up the entire environment:

```bash
# 1. Prepare environment variables
cp .env.example .env

# 2. Generate SSL certificates
mkdir -p infra/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout infra/nginx/ssl/localhost.key \
  -out infra/nginx/ssl/localhost.crt \
  -subj "/C=FR/ST=Paris/L=Paris/O=42/OU=Transcendence/CN=localhost"

# 3. Build and start services
docker-compose up -d --build
```

Access the platform at: [**https://localhost**](https://localhost)

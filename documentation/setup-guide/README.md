# Setup Guide — Code Arena

Welcome to the internal setup and architecture documentation for the Code Arena project.

## Guide Contents

1. [**Architecture Overview**](architecture.md)
   - High-level system design and component interaction diagram.
2. [**Service Details**](services.md)
   - Deep dive into Frontend, Backend, Database, Cache, and Proxy configurations.
3. [**Network & Security**](network-security.md)
   - HTTPS, reverse proxy logic, and Spring Security setup.
4. [**Image Versioning (2026)**](image-versions.md)
   - Detailed strategy and specific versions of all Docker images.
5. [**Development Workflow**](development.md)
   - Tips for hot-reloading and hybrid setups.
6. [**Troubleshooting**](troubleshooting.md)
   - Solutions for common startup errors and environment issues.

---

## Quick Start (Automated)

The easiest way to initialize the environment is using the interactive setup script:

```bash
# Start the automated setup
./setup.sh
```

The script will:
1. Interactively prompt for missing environment variables.
2. Generate self-signed SSL certificates.
3. Start all services using `docker compose` (v2+).

Access the platform at: [**https://localhost**](https://localhost)

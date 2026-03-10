# Image Versioning Strategy (2026)

This document outlines the specific versions of Docker images and runtimes used in the Code Arena project to ensure maximum security, stability, and reproducibility in early 2026.

## Current Versions

| Component | Provider | Image Tag / Version | Stability |
| :--- | :--- | :--- | :--- |
| **Backend (JRE)** | Eclipse Temurin | `25.0.1_8-jre-alpine` | LTS (Jan 2026 Patch) |
| **Build (Maven)** | Maven (Temurin) | `3.9.12-eclipse-temurin-25-alpine` | Stable |
| **Frontend** | Node.js | `24-alpine` | LTS |
| **Database** | PostgreSQL | `18.3-alpine3.23` | Stable |
| **Cache** | Redis | `8.6.1-alpine3.23` | Stable |
| **Proxy** | Nginx | `1.28.2-alpine3.23` | Stable |

## Why these versions?

### 1. LTS-first Strategy
Priority is given to **Long-Term Support (LTS)** releases for core runtimes:
- **Node 24 (LTS)**: Entered LTS in late 2025, providing security updates until late 2028.
- **JDK 25 (LTS)**: The current LTS for the Java ecosystem, optimized for modern Spring Boot features.

### 2. Security Patching
Utilization of general tags such as `:latest` or `:21` is avoided. Runtimes are pinned to specific patch releases (e.g., `25.0.1_8`) to incorporate recent 2026 vulnerability fixes.

### 3. Alpine Linux (musl)
All images use **Alpine Linux** (currently based on `alpine3.23`) to:
- Reduce the attack surface (only minimal packages included).
- Minimize image size (~100MB vs ~600MB for Ubuntu-based images).
- Speed up deployment and build times.

## Infrastructure Requirements
- **Docker Compose v2+**: The legacy `docker-compose` (v1) is deprecated. BuildKit (enabled by default in v2) is required for efficient builds and modern image support.

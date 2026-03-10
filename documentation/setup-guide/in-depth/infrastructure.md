# Infrastructure & Proxy Deep Dive

This document explains the orchestration of the project using Docker Compose and the central Proxy server.

## Docker Compose (`docker-compose.yml`)

### Redis Configuration
- **`command: redis-server --requirepass ${REDIS_PASSWORD}`**:
    - This sets the command that runs as the foreground process.
    - It starts the Redis server and enforces password protection using an environment variable.
- **Healthcheck: `redis-cli -a ${REDIS_PASSWORD} ping`**:
    - `redis-cli`: The command-line tool for interacting with Redis.
    - `-a`: Specifies the password for authentication.
    - `ping`: A simple command that returns `PONG` if the server is alive.
    - **Purpose**: Docker uses this to ensure Redis is fully ready before starting the backend.

### Database Configuration (PostgreSQL)
- **Volumes**: 
    - `- ./database:/docker-entrypoint-initdb.d`: For initial schema loading on first run.
    - `- ./database/data:/var/lib/postgresql/data`: A **local directory** for actual data persistence. This ensures that user and game data are retained even if containers are deleted.
- **Healthcheck: `pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}`**:
    - `pg_isready`: A standard PostgreSQL utility to check if the server is accepting connections.
    - `-U`: The username to connect as.
    - `-d`: The database to check.
    - **Purpose**: Ensures the database is up and the specific DB is created before the backend tries to connect.

## Volume Persistence
To ensure data is preserved when containers are stopped or removed, the database uses a bind mount to a local directory.

- **`database/data`**: This local folder is mapped to `/var/lib/postgresql` inside the container. It contains version-specific subdirectories (e.g., `18/main`) as required by PostgreSQL 18+.
    - [!IMPORTANT]
    - If this folder is **empty**, PostgreSQL will execute `init.sql`.
    - If it **already contains data**, PostgreSQL skips the initialization and loads the existing state.

## Dynamic Nginx Configuration (`envsubst`)

The project uses the official Nginx Docker image's **templating** feature to prevent hardcoded ports and endpoints.

### How it Works
1.  **Templates**: We store configuration in `.template` files (e.g., `nginx.conf.template`).
2.  **Mapping**: These are mounted to `/etc/nginx/templates/` inside the container.
3.  **Substitution**: At startup, Nginx automatically runs `envsubst`, replacing `${VAR}` placeholders with environment variables from `.env`.
4.  **Output**: The final configuration is written to `/etc/nginx/conf.d/` (for modular configs) or the root `/etc/nginx/` (if configured).

### Benefits
- **Zero Hardcoding**: You can move your `BACKEND_PORT` or `FRONTEND_PORT` in `.env`, and the Nginx proxy and Frontend server will automatically adjust their routing logic on the next start.
- **Port Conflict Resolution**: Host-level ports can be changed via `HOST_TO_PROXY_*` variables to avoid local system conflicts.

---

## Proxy Configuration (`infra/nginx/nginx.conf.template`)

### Structural Overview

#### The Redirect Server
- **`listen \${PROXY_INTERNAL_HTTP_PORT};`**: Listens for insecure HTTP traffic on the dynamic internal port.
- **`return 301 https://\$host\$request_uri;`**: Automatically redirects every request to HTTPS.

#### The Main SSL Server
- **`listen \${PROXY_INTERNAL_HTTPS_PORT} ssl;`**: Listens for secure traffic.
- **`ssl_certificate` / `ssl_certificate_key`**: Point to the self-signed certificates.

#### Routing (The `location` blocks)
1.  **`/ws` (WebSockets)**:
    - Routes traffic to the backend via `http://backend:\${BACKEND_PORT}`.
    - Includes `Upgrade` and `Connection` headers for the WebSocket protocol.
2.  **`/api` (Backend API)**:
    - Routes traffic to the Spring Boot backend via `http://backend:\${BACKEND_PORT}`.
3.  **`/` (Frontend)**:
    - Routes all other traffic to the frontend container via `http://frontend:\${FRONTEND_PORT}`.

### Proxy Headers Deep Dive
When Nginx proxies a request to the backend, the backend normally sees the internal IP of the proxy as the "client". These headers fix that:

- **`proxy_set_header Host $host;`**: Passes the original `Host` header (e.g., `localhost`) requested by the user.
- **`proxy_set_header X-Real-IP $remote_addr;`**: Sends the actual IP address of the user to the backend.
- **`proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;`**: Keeps track of all IPs the request has passed through (in case there are multiple proxies).
- **`proxy_set_header X-Forwarded-Proto $scheme;`**: Tells the backend whether the original request was `http` or `https`.

## References
- [Redis Official Documentation](https://redis.io/documentation)
- [PostgreSQL pg_isready Reference](https://www.postgresql.org/docs/current/app-pg-isready.html)
- [Nginx Proxy Module Guide](https://nginx.org/en/docs/http/ngx_http_proxy_module.html)
- [Nginx WebSocket Proxying](https://nginx.org/en/docs/http/websocket.html)

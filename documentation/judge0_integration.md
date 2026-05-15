# Judge0 Integration Documentation

Judge0 is a robust, open-source online code execution system used in Code Arena to provide secure, sandboxed code execution for competitive programming and matchmaking challenges.

## Architecture

The Judge0 subsystem consists of four interconnected services within the `codearena` Docker network:

| Service | Image | Role |
| :--- | :--- | :--- |
| `judge0-server` | `judge0/judge0:1.13.1-extra` | The main API server (Ruby on Rails). Handles incoming execution requests. |
| `judge0-worker` | `judge0/judge0:1.13.1-extra` | Background processors that execute the code inside the `isolate` sandbox. |
| `judge0-db` | `postgres:16-alpine` | Relational database for storing submission data and logs. |
| `judge0-cache` | `redis:7-alpine` | Message broker used for the job queue between the server and workers. |

## Configuration

All configuration is centralized in the project's root `.env` file. The separate `judge0.conf` file is no longer used, as all variables are injected directly into the containers.

### Infrastructure Settings
*   `JUDGE0_POSTGRES_DB`: Name of the Judge0 database.
*   `JUDGE0_POSTGRES_USER`: Database username.
*   `JUDGE0_POSTGRES_PASSWORD`: Database password.
*   `JUDGE0_REDIS_PASSWORD`: Password for the Judge0 Redis instance.
*   `JUDGE0_AUTH_TOKEN`: The API key required by the backend to communicate with Judge0.

### Execution Limits
You can adjust the sandboxing limits directly from `.env`:
*   `JUDGE0_CPU_TIME_LIMIT`: Default CPU time limit (seconds).
*   `JUDGE0_MEMORY_LIMIT`: Default memory limit (kilobytes).
*   `JUDGE0_MAX_PROCESSES_AND_OR_THREADS`: Max threads allowed per submission.
*   `JUDGE0_MAX_FILE_SIZE`: Max file size created by a program (kilobytes).
*   `JUDGE0_ENABLE_NETWORK`: Set to `true` if user programs require internet access (default: `false`).

## Security Design

The integration follows a "Zero Trust" approach within the infrastructure:

### 1. Network Isolation
The `judge0-server` does **not** expose any ports to the host machine. It is entirely internal to the `codearena` bridge network. Communication is only possible between the `backend` and `judge0-server`.

### 2. API Authentication
Access is protected by an `X-Auth-Token` header. The `backend` must provide the `JUDGE0_AUTH_TOKEN` defined in the environment for every request.

### 3. Privileged Mode
The `judge0-server` and `judge0-worker` run with `privileged: true`. This is a requirement for the underlying `isolate` tool to manage Linux namespaces and cgroups required for secure sandboxing. 

> [!WARNING]
> Because Judge0 runs in privileged mode, it should be treated as a sensitive component. Never expose the `judge0-server` directly to the public internet.

## Troubleshooting and Testing

### Verifying Internal Access
To verify that the backend can reach Judge0, run:
```bash
docker compose exec backend wget -q -O - --header="X-Auth-Token: <your_token>" http://judge0-server:2358/system_info
```

### Healthchecks
The health of the system can be monitored via Docker Compose:
*   **Database**: Uses `pg_isready` with credentials from `.env`.
*   **Redis**: Uses `redis-cli ping` with the configured password.
*   **Server/Worker**: Dependent on the healthy state of the DB and Cache.

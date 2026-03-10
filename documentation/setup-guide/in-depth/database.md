# Database Deep Dive

This document provides an overview of the data storage layer in Code Arena.

## PostgreSQL
The primary database is **PostgreSQL 18**, a powerful, open-source object-relational database system.

- **Role**: Stores persistent user data, match history, and chat logs.
- **Configuration**:
    - The database is initialized using SQL scripts found in the root `database` directory.
    - It is isolated within the `codearena` Docker network and is only accessible to the backend (and the proxy for healthchecks).

## Redis
Redis is used as an in-memory data structure store.

- **Role**: 
    - **Caching**: Speeds up frequent lookups.
    - **Session Management**: (Potentially) stores active user sessions.
    - **Live State**: Managing active game invitations and matchmaking queues.
- **Security**: Access is protected by a password defined in the `.env` file (`REDIS_PASSWORD`).

## Volume Persistence
To ensure data is preserved when containers are stopped or removed, the database uses a bind mount to a local directory.

- **`database/data`**: This directory is mounted to `/var/lib/postgresql/data` inside the container.
- **Initialization Logic**:
    - If `database/data` is **empty**, the database executes `init.sql` (mapped to `/docker-entrypoint-initdb.d`).
    - If it **already contains data**, the database skips the scripts and loads the existing binary files.

## References
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/docs/)

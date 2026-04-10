# Code Arena

Code Arena is a competitive programming platform providing a real-time environment for coding duels and challenges. The system is architected as a set of distributed services including an Angular frontend, a Spring Boot backend, and PostgreSQL/Redis infrastructure.

## Deployment Guide

The platform is designed to be deployed using Docker and Docker Compose, ensuring consistency across environments.

### Prerequisites

-   Docker and Docker Compose (v2.x recommended)
-   Properly configured environment variables in a `.env` file

### Standard Deployment

To deploy the full application stack (Frontend, Backend, Proxy, Database, and Cache):

1.  **Environment Configuration**: Ensure the `.env` file is present and populated with the required secrets and configurations.
2.  **System Initialization**: Execute the setup script to prepare the environment (e.g., generating SSL certificates if required):
    ```bash
    ./setup.sh
    ```
3.  **Service Orchestration**: Launch all services in detached mode:
    ```bash
    docker compose up -d
    ```

The application will be accessible via the endpoint defined in the `proxy` service configuration.

---

## Documentation

Comprehensive project documentation is maintained in the `documentation/` directory for various functional areas:

-   **Architecture & Design**: Detailed overview of the system [architecture](documentation/database/architecture.md).
-   **Database Operations**: Procedures for [persistence and migrations](documentation/database/operations.md).
-   **Development Workflow**: Instructions for [local development and hot-reloading](documentation/setup-guide/development.md).
-   **Project Roadmap**: Current status and [future milestones](documentation/roadmap.md).

## Development and Maintenance

### Local Development

For developers requiring hot-reloading capabilities and IDE integration, a "Hybrid" workflow is supported. Refer to the [Development Workflow Guide](documentation/setup-guide/development.md) for detailed setup instructions.

### Database Operations

The database schema is managed via **Flyway** migrations. Detailed operational procedures, including backup and restore logic, are documented in the [Database Operations Guide](documentation/database/operations.md).

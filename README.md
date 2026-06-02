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

---

## Bonus Modules

### OAuth 2.0 Authentication (GitHub & 42 Intra)

This platform supports OAuth 2.0 single-sign-on using **42 Intra** and **GitHub** as providers.

#### Configuration Steps

1. **42 Intra**:
   - Register a new application at [42 API Applications](https://profile.intra.42.fr/oauth/applications).
   - Set the Redirect URI to `https://localhost/api/auth/oauth2/callback/42`.
2. **GitHub**:
   - Register a new Developer OAuth App at [GitHub Developer Settings](https://github.com/settings/developers).
   - Set the Authorization Callback URL to `https://localhost/api/auth/oauth2/callback/github`.

3. Populate the credentials in your `.env` file:
   ```env
   # OAuth2 Configurations
   GITHUB_CLIENT_ID=your_github_client_id
   GITHUB_CLIENT_SECRET=your_github_client_secret
   FORTY_TWO_CLIENT_ID=your_42_client_id
   FORTY_TWO_CLIENT_SECRET=your_42_client_secret
   OAUTH2_FRONTEND_CALLBACK_URL=https://localhost/oauth2/callback
   ```

#### Account Linking & Creation

- **First-time OAuth Login**: A new local account is automatically created with the username and email fetched from the OAuth provider.
- **Account Linking**: If an OAuth login matches the email address of an existing local account, the OAuth provider credentials are automatically linked to that existing account.
- **Passwordless Accounts**: Accounts created solely via OAuth do not require a password. Local login is disabled for these accounts until a password is set in profile settings.


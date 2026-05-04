# Authentication Architecture

This document provides a technical walkthrough of the Spring Security and JWT integration within the project.

## 1. Security Filter Chain

The application utilizes the Spring Security Filter Chain to manage request-level security. Every incoming HTTP request passes through a series of filters before reaching the controller.

### 1.1 Core Filter Sequence

1.  **CORS Filter**: Manages Cross-Origin Resource Sharing based on configured policies.
2.  **JwtAuthenticationFilter**: A custom filter that intercepts requests to identify valid access tokens.
3.  **Authorization Filter**: Enforces the security rules defined in the configuration (e.g., which endpoints require authentication).

## 2. Authentication Mechanism

The authentication engine is composed of several interactive components:

-   **AuthenticationManager**: The central entry point for authentication requests. Injected from `AuthenticationConfiguration` and used during the login flow.
-   **Security Configuration**: Automatically wires the `UserDetailsService` and `PasswordEncoder` into a default `DaoAuthenticationProvider`. This ensures a clean startup without redundant bean warnings.
-   **UserDetailsService**: An interface fulfilled by the `UserService` to load user-specific data from the JPA repository.
-   **PasswordEncoder**: Utilizes the BCrypt algorithm (12 rounds) for secure password hashing and verification.

## 3. Token Lifecycle

The application implements a dual-token system for enhanced security:

-   **Access Token**: A short-lived token (15m) containing a `type: access` claim. Used for authorizing resource requests.
-   **Refresh Token**: A long-lived token (7d) containing `type: refresh` and a unique `jti` (JWT ID). Used to obtain new access tokens.
-   **Token Rotation**: Upon a successful refresh, the old refresh token is blacklisted and a new one is issued to the client.
-   **Logout**: Invalidation is implemented via the `/api/auth/logout` endpoint. The server blacklists the access token string and the refresh token's `jti` in **Redis**. This state is persistent across restarts thanks to AOF (Append Only File) logging and dedicated Docker volumes.

## 3. Session Management

The application is configured for **stateless** session management. This means:

-   No HTTP sessions are created or maintained on the server.
-   The `SecurityContext` is populated for each request based on the provided JWT and cleared once the request is complete.
-   Authentication state is entirely contained within the token presented by the client.

## 4. Identity Representation

When a valid token is processed, an `Authentication` object is created and stored in the `SecurityContextHolder`. This object contains:

-   **Principal**: The `UserDetails` representing the authenticated user.
-   **Authorities**: The permissions or roles assigned to the user.
-   **Status**: A boolean indicating successful authentication.

## 5. Unauthorized Access Handling

Endpoints not explicitly whitelisted in the security configuration require a valid token. If a request to a protected resource lacks a valid token:

-   The **AuthenticationEntryPoint** is triggered.
-   A `401 Unauthorized` response is returned to the client, signaling that authentication is required.

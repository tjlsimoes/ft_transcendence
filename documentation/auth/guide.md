# Authentication Conceptual Guide

This document explains the core concepts of JSON Web Token (JWT) authentication as implemented in the project.

## 1. Overview of JWT

A JSON Web Token (JWT) is a compact, self-contained method for securely transmitting information between parties as a JSON object. In the context of authentication, it serves as a digital credential that proves a user's identity without requiring the server to maintain session state.

## 2. Structure of a Token

A JWT consists of three parts separated by dots (`.`):

1.  **Header**: Specifies the algorithm used for signing (e.g., HMAC-SHA256).
2.  **Payload**: Contains claims, which are statements about an entity (typically the user) and additional data (e.g., username, expiration time).
3.  **Signature**: Created by taking the encoded header, encoded payload, and a secret key, and signing them using the algorithm specified in the header.

## 3. Authentication Workflow

The authentication process follows a stateless model:

1.  **Credential Submission**: The client provides credentials (e.g., username and password) via the `/api/auth/login` endpoint.
2.  **Token Issuance**: Upon successful verification, the server generates a signed JWT and returns it to the client.
3.  **State Management**: The client stores the JWT (e.g., in `localStorage` or a secure cookie). 
4.  **Authorized Requests**: The client includes the JWT in the `Authorization` header (`Bearer <token>`) for subsequent requests to protected resources.
5.  **Server Verification**: The server verifies the token's signature using the secret key. If valid, the request is processed; otherwise, it is rejected.

## 4. Security Considerations

-   **Secret Key Integrity**: The security of the system relies entirely on the confidentiality of the secret key used for signing. 
-   **Token Expiration**: To minimize the impact of a compromised token, access tokens are short-lived (15m).
-   **Refresh & Rotation**: A secondary, longer-lived token (Refresh Token) is used to obtain new access tokens. To prevent replay attacks, the server implements **Token Rotation**: every time a refresh token is used, it is invalidated and a new one is issued.
-   **Revocation (Redis)**: Although stateless, the system supports immediate logout via a **Redis Blacklist**. Access tokens are blacklisted by their full string, while refresh tokens are blacklisted by a unique **JTI (JWT ID)** claim.
-   **Statelessness**: The server does not store permanent session data in the database, relying on Redis for short-term revocation state.

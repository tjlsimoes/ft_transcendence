# Code Arena

Core docs live in [documentation/setup-guide/README.md](documentation/setup-guide/README.md).

## Database ERD (MVP)

```mermaid
erDiagram
    USERS {
        bigint id PK
        string login
        string email
        string password_hash
        string avatar_url
        int elo
        timestamp created_at
    }

    FRIENDSHIPS {
        bigint user_id PK, FK
        bigint friend_id PK, FK
        string status
    }

    CHALLENGES {
        bigint id PK
        string title
        string description
        string difficulty
        int time_limit_secs
        jsonb test_cases
    }

    DUELS {
        bigint id PK
        bigint challenger_id FK
        bigint opponent_id FK
        bigint challenge_id FK
        string status
        timestamp started_at
        timestamp ended_at
    }

    SUBMISSIONS {
        bigint id PK
        bigint duel_id FK
        bigint user_id FK
        string language
        text code
        int score
        timestamp submitted_at
    }

    RANKINGS {
        bigint user_id PK, FK
        int elo
        string league
        int win_streak
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        string type
        jsonb payload
        boolean read
        timestamp created_at
    }

    MESSAGES {
        bigint id PK
        bigint sender_id FK
        bigint recipient_id FK
        text content
        timestamp created_at
    }

    USERS ||--o{ FRIENDSHIPS : user
    USERS ||--o{ FRIENDSHIPS : friend
    USERS ||--o{ DUELS : challenger
    USERS ||--o{ DUELS : opponent
    CHALLENGES ||--o{ DUELS : challenge
    DUELS ||--o{ SUBMISSIONS : has
    USERS ||--o{ SUBMISSIONS : author
    USERS ||--|| RANKINGS : has
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ MESSAGES : sends
    USERS ||--o{ MESSAGES : receives
```

## Migrations

- Tool: Flyway
- Baseline migration: [backend/src/main/resources/db/migration/V1__init.sql](backend/src/main/resources/db/migration/V1__init.sql)

## Local Reset Helper

If local startup fails due to reused Postgres state (roles/schema mismatch), run this from the repository root to reset local data and recreate containers:

```bash
docker compose down && rm -rf database/data && echo "3" | ./setup.sh
```

This removes local database files and should only be used for local development.

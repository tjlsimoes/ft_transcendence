-- Add OAuth provider columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_provider_id VARCHAR(255);

-- Make password nullable for OAuth-only users
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- Unique constraint: one account per provider+id pair
ALTER TABLE users ADD CONSTRAINT uq_users_oauth
    UNIQUE (oauth_provider, oauth_provider_id);

-- Index for OAuth lookups
CREATE INDEX idx_users_oauth ON users(oauth_provider, oauth_provider_id);

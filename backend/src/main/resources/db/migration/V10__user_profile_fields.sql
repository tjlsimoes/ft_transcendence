ALTER TABLE users
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS bio TEXT;

UPDATE users
SET display_name = login
WHERE display_name IS NULL
   OR TRIM(display_name) = '';

UPDATE users
SET avatar_url = '/api/users/avatars/default-avatar.svg'
WHERE avatar_url IS NULL;

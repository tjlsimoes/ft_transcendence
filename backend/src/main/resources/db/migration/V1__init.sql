CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(2048),
    elo INTEGER NOT NULL DEFAULT 0,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0,
    win_streak INTEGER NOT NULL DEFAULT 0,
    league VARCHAR(50) NOT NULL DEFAULT 'BRONZE',
    status VARCHAR(50) NOT NULL DEFAULT 'OFFLINE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE friendships (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_friendships_no_self CHECK (user_id <> friend_id)
);

CREATE TABLE challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) NOT NULL,
    time_limit_secs INTEGER NOT NULL,
    test_cases JSONB NOT NULL,
    CONSTRAINT chk_challenges_time_limit_positive CHECK (time_limit_secs > 0)
);

CREATE TABLE duels (
    id BIGSERIAL PRIMARY KEY,
    challenger_id BIGINT NOT NULL,
    opponent_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    CONSTRAINT fk_duels_challenger FOREIGN KEY (challenger_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_duels_opponent FOREIGN KEY (opponent_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_duels_challenge FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE RESTRICT,
    CONSTRAINT chk_duels_distinct_players CHECK (challenger_id <> opponent_id)
);

CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    duel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_submissions_duel FOREIGN KEY (duel_id) REFERENCES duels(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_submissions_score_non_negative CHECK (score >= 0)
);

CREATE TABLE rankings (
    user_id BIGINT PRIMARY KEY,
    elo INTEGER NOT NULL DEFAULT 0,
    league VARCHAR(50) NOT NULL DEFAULT 'BRONZE',
    win_streak INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_rankings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_rankings_elo_non_negative CHECK (elo >= 0),
    CONSTRAINT chk_rankings_streak_non_negative CHECK (win_streak >= 0)
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_messages_distinct_users CHECK (sender_id <> recipient_id)
);

CREATE INDEX idx_friendships_friend_id ON friendships(friend_id);
CREATE INDEX idx_friendships_status ON friendships(status);

CREATE INDEX idx_challenges_difficulty ON challenges(difficulty);

CREATE INDEX idx_duels_challenger_id ON duels(challenger_id);
CREATE INDEX idx_duels_opponent_id ON duels(opponent_id);
CREATE INDEX idx_duels_challenge_id ON duels(challenge_id);
CREATE INDEX idx_duels_status ON duels(status);

CREATE INDEX idx_submissions_duel_id ON submissions(duel_id);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_submitted_at ON submissions(submitted_at);

CREATE INDEX idx_rankings_elo_desc ON rankings(elo DESC);
CREATE INDEX idx_rankings_league_elo ON rankings(league, elo DESC);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);

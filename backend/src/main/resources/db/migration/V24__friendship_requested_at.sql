ALTER TABLE friendships ADD COLUMN requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX idx_friendships_friend_status_requested ON friendships(friend_id, status, requested_at DESC);
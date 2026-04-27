-- Add winner_id to duels so match results can be determined accurately.
-- NULL means the duel has no winner yet (in progress, draw, or cancelled).
ALTER TABLE duels ADD COLUMN winner_id BIGINT;
ALTER TABLE duels ADD CONSTRAINT fk_duels_winner
    FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE SET NULL;

-- Constrain duel status to known values.
-- Existing rows must already contain one of these values (or be empty).
ALTER TABLE duels ADD CONSTRAINT chk_duels_status
    CHECK (status IN ('WAITING', 'MATCHED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'DRAW'));

CREATE INDEX idx_duels_winner_id ON duels(winner_id);

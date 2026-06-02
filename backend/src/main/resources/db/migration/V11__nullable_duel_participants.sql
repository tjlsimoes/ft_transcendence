ALTER TABLE duels
    DROP CONSTRAINT fk_duels_challenger,
    DROP CONSTRAINT fk_duels_opponent;

ALTER TABLE duels
    ALTER COLUMN challenger_id DROP NOT NULL,
    ALTER COLUMN opponent_id DROP NOT NULL;

ALTER TABLE duels
    ADD CONSTRAINT fk_duels_challenger
        FOREIGN KEY (challenger_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_duels_opponent
        FOREIGN KEY (opponent_id) REFERENCES users(id) ON DELETE SET NULL;
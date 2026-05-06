-- Normalize existing values and enforce the fixed difficulty/time mapping.
UPDATE challenges
SET difficulty = UPPER(TRIM(difficulty))
WHERE difficulty IS NOT NULL;

UPDATE challenges
SET time_limit_secs = CASE difficulty
    WHEN 'EASY' THEN 300
    WHEN 'MEDIUM' THEN 600
    WHEN 'HARD' THEN 1200
    WHEN 'INSANE' THEN 1800
    ELSE time_limit_secs
END
WHERE difficulty IN ('EASY', 'MEDIUM', 'HARD', 'INSANE');

ALTER TABLE challenges
    DROP CONSTRAINT IF EXISTS chk_challenges_difficulty;

ALTER TABLE challenges
    DROP CONSTRAINT IF EXISTS chk_challenges_time_limit_by_difficulty;

ALTER TABLE challenges
    ADD CONSTRAINT chk_challenges_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'INSANE'));

ALTER TABLE challenges
    ADD CONSTRAINT chk_challenges_time_limit_by_difficulty
        CHECK (
            (difficulty = 'EASY' AND time_limit_secs = 300) OR
            (difficulty = 'MEDIUM' AND time_limit_secs = 600) OR
            (difficulty = 'HARD' AND time_limit_secs = 1200) OR
            (difficulty = 'INSANE' AND time_limit_secs = 1800)
        );

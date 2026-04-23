-- Single source of truth for challenge durations by difficulty.
CREATE TABLE IF NOT EXISTS challenge_difficulty_settings (
    difficulty VARCHAR(20) PRIMARY KEY,
    time_limit_secs INTEGER NOT NULL,
    CONSTRAINT chk_challenge_difficulty_settings_time_positive CHECK (time_limit_secs > 0),
    CONSTRAINT uq_challenge_difficulty_settings_difficulty_time UNIQUE (difficulty, time_limit_secs)
);

INSERT INTO challenge_difficulty_settings (difficulty, time_limit_secs)
VALUES
    ('EASY', 300),
    ('MEDIUM', 600),
    ('HARD', 1200),
    ('INSANE', 1800)
ON CONFLICT (difficulty)
DO UPDATE SET time_limit_secs = EXCLUDED.time_limit_secs;

-- Replace hardcoded challenge time checks with config-backed validation.
UPDATE challenges
SET difficulty = UPPER(TRIM(difficulty))
WHERE difficulty IS NOT NULL;

UPDATE challenges c
SET time_limit_secs = s.time_limit_secs
FROM challenge_difficulty_settings s
WHERE c.difficulty = s.difficulty
  AND c.time_limit_secs <> s.time_limit_secs;

ALTER TABLE challenges
    DROP CONSTRAINT IF EXISTS chk_challenges_time_limit_by_difficulty;

ALTER TABLE challenges
    DROP CONSTRAINT IF EXISTS chk_challenges_difficulty;

ALTER TABLE challenges
    DROP CONSTRAINT IF EXISTS fk_challenges_difficulty_time;

ALTER TABLE challenges
    ADD CONSTRAINT fk_challenges_difficulty_time
        FOREIGN KEY (difficulty, time_limit_secs)
        REFERENCES challenge_difficulty_settings (difficulty, time_limit_secs);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'first_word',
    'Assignment name: first_word\nExpected files: first_word.c\nAllowed functions: write\n\nWrite a program that takes a string and displays its first word followed by a newline.\nA word is delimited by spaces/tabs or string boundaries.\nIf argc != 2, or if there are no words, print only a newline.',
    'EASY',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'EASY'),
    '[{"input":"./first_word \"FOR PONY\"","expected_output":"FOR\\n","is_hidden":false},{"input":"./first_word \"this        ...    is sparta, then again, maybe    not\"","expected_output":"this\\n","is_hidden":true},{"input":"./first_word \"   \"","expected_output":"\\n","is_hidden":true},{"input":"./first_word \"a\" \"b\"","expected_output":"\\n","is_hidden":true},{"input":"./first_word \"  lorem,ipsum  \"","expected_output":"lorem,ipsum\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'first_word'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'fizzbuzz',
    'Assignment name: fizzbuzz\nExpected files: fizzbuzz.c\nAllowed functions: write\n\nWrite a program that prints numbers from 1 to 100, one per line.\nMultiples of 3 print fizz, multiples of 5 print buzz, multiples of both print fizzbuzz.',
    'EASY',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'EASY'),
    '[{"input":"./fizzbuzz","expected_output":"1\\n2\\nfizz\\n4\\nbuzz\\nfizz\\n7\\n8\\nfizz\\nbuzz\\n...\\n97\\n98\\nfizz\\nbuzz\\n","is_hidden":false},{"input":"line 15","expected_output":"fizzbuzz","is_hidden":true},{"input":"line 100","expected_output":"buzz","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'fizzbuzz'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'search_and_replace',
    'Assignment name: search_and_replace\nExpected files: search_and_replace.c\nAllowed functions: write, exit\n\nWrite a program that takes 3 arguments: string, search-char, replace-char.\nIf argc != 4, print newline only.\nIf second or third argument is not exactly one character, print newline only.\nOtherwise replace all occurrences of search-char in the string and print result with newline.',
    'EASY',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'EASY'),
    '[{"input":"./search_and_replace \"Papache est un sabre\" \"a\" \"o\"","expected_output":"Popoche est un sobre\\n","is_hidden":false},{"input":"./search_and_replace \"zaz\" \"art\" \"zul\"","expected_output":"\\n","is_hidden":true},{"input":"./search_and_replace \"zaz\" \"r\" \"u\"","expected_output":"zaz\\n","is_hidden":true},{"input":"./search_and_replace \"ZoZ eT Dovid oiME le METol.\" \"o\" \"a\"","expected_output":"ZaZ eT David aiME le METal.\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'search_and_replace'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'rot_13',
    'Assignment name: rot_13\nExpected files: rot_13.c\nAllowed functions: write\n\nWrite a program that takes one string argument and applies ROT13 to letters only.\nCase must be preserved.\nIf argc != 2, print newline only.',
    'EASY',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'EASY'),
    '[{"input":"./rot_13 \"abc\"","expected_output":"nop\\n","is_hidden":false},{"input":"./rot_13 \"My horse is Amazing.\"","expected_output":"Zl ubefr vf Nznmvat.\\n","is_hidden":true},{"input":"./rot_13 \"AkjhZ zLKIJz , 23y \"","expected_output":"NxwuM mYXVWm , 23l \\n","is_hidden":true},{"input":"./rot_13","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'rot_13'
);

INSERT INTO challenges (title, description, difficulty, time_limit_secs, test_cases)
SELECT
    'repeat_alpha',
    'Assignment name: repeat_alpha\nExpected files: repeat_alpha.c\nAllowed functions: write\n\nWrite a program that repeats each alphabetical character by its alphabet index\n(a=1, b=2, ..., z=26), preserving case, then prints newline.\nIf argc != 2, print newline only.',
    'EASY',
    (SELECT time_limit_secs FROM challenge_difficulty_settings WHERE difficulty = 'EASY'),
    '[{"input":"./repeat_alpha \"abc\"","expected_output":"abbccc\\n","is_hidden":false},{"input":"./repeat_alpha \"Alex.\"","expected_output":"Alllllllllllleeeeexxxxxxxxxxxxxxxxxxxxxxxx.\\n","is_hidden":true},{"input":"./repeat_alpha \"abacadaba 42!\"","expected_output":"abbacccaddddabba 42!\\n","is_hidden":true},{"input":"./repeat_alpha","expected_output":"\\n","is_hidden":true}]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM challenges WHERE title = 'repeat_alpha'
);

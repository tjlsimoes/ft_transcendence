ALTER TABLE submissions
    ADD COLUMN time_score INTEGER DEFAULT 0,
    ADD COLUMN perf_score INTEGER DEFAULT 0,
    ADD COLUMN correctness_score INTEGER DEFAULT 0,
    ADD COLUMN quality_score INTEGER DEFAULT 0,
    ADD COLUMN runtime_ms BIGINT DEFAULT 0,
    ADD COLUMN time_taken_secs INTEGER DEFAULT 0;

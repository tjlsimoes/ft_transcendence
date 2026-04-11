package com.codearena.code_arena_backend.challenge.entity;

public enum ChallengeDifficulty {
    EASY(300),
    MEDIUM(600),
    HARD(1200),
    INSANE(1800);

    private final int defaultTimeLimitSecs;

    ChallengeDifficulty(int defaultTimeLimitSecs) {
        this.defaultTimeLimitSecs = defaultTimeLimitSecs;
    }

    public int getDefaultTimeLimitSecs() {
        return defaultTimeLimitSecs;
    }
}

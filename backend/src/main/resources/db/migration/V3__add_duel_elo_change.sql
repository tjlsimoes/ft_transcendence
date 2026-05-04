-- Tracks how much LP each player gained or lost in a duel.
ALTER TABLE duels ADD COLUMN challenger_elo_change INTEGER DEFAULT 0;
ALTER TABLE duels ADD COLUMN opponent_elo_change INTEGER DEFAULT 0;

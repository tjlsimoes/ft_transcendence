-- Add code scaffolding columns to challenges.
--
-- solution_template : stub shown in the editor (function signature + empty body).
--                     The user writes ONLY the function; no int main is needed.
-- test_harness      : complete C boilerplate (includes + int main) that the
--                     backend appends to the user's function before sending to Judge0.
--                     Never exposed to the client.

ALTER TABLE challenges ADD COLUMN IF NOT EXISTS solution_template TEXT;
ALTER TABLE challenges ADD COLUMN IF NOT EXISTS test_harness      TEXT;

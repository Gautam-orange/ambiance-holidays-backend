-- Migrate password reset to a 6-digit OTP flow.
-- The same table now stores hashed OTP codes instead of long URL-safe tokens, so
-- we drop the global uniqueness constraint (a 6-digit OTP namespace is too small
-- to guarantee uniqueness across users) and add an `attempts` counter to limit
-- brute force.

ALTER TABLE password_reset_tokens
    DROP CONSTRAINT IF EXISTS password_reset_tokens_token_hash_key;

ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS attempts INT NOT NULL DEFAULT 0;

-- Helps the new "find latest active OTP for this user" lookup.
CREATE INDEX IF NOT EXISTS idx_pwd_reset_user_active
    ON password_reset_tokens(user_id, expires_at)
    WHERE used_at IS NULL;

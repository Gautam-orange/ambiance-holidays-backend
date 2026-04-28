-- ============================================================
-- V4 — Auth enhancements: email verification + login lockout
-- ============================================================

-- users: verification token
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS verification_token_hash       TEXT,
    ADD COLUMN IF NOT EXISTS verification_token_expires_at TIMESTAMPTZ;

-- login_attempts: for brute-force lockout
CREATE TABLE IF NOT EXISTS login_attempts (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email        TEXT        NOT NULL,
    ip_address   TEXT,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    success      BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_login_attempts_email_time
    ON login_attempts (email, attempted_at DESC);

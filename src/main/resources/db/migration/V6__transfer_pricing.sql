-- ============================================================
-- V6: Distance-based transfer pricing tiers
-- Replaces fixed-route model with distance-band pricing
-- ============================================================

CREATE TABLE transfer_pricing_tiers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label       VARCHAR(200) NOT NULL,
    min_km      INTEGER NOT NULL CHECK (min_km >= 0),
    max_km      INTEGER,                          -- NULL = unlimited (open upper bound)
    price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  SMALLINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Default tiers as specified by business
INSERT INTO transfer_pricing_tiers (label, min_km, max_km, price_cents, sort_order)
VALUES
    ('Short Distance (0 – 20 km)',   0,  20,  2000,  1),
    ('Long Distance (21 – 150 km)', 21, 150, 10000,  2);

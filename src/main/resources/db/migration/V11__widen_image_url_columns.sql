-- Long Google/CDN image URLs (with auth signatures, params, etc.) routinely exceed
-- 500 chars and triggered "field too long" errors when admins pasted them. Widen
-- to TEXT so any reasonable URL fits.
ALTER TABLE cars  ALTER COLUMN cover_image_url    TYPE TEXT;
ALTER TABLE tours ALTER COLUMN cover_image_url    TYPE TEXT;
ALTER TABLE day_trips ALTER COLUMN cover_image_url TYPE TEXT;
ALTER TABLE agents ALTER COLUMN business_proof_url TYPE TEXT;

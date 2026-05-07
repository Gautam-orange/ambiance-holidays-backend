-- V13: complete day_trip schema parity with frontend.
-- Adds availability_mode (admin "Always Available" vs "On Request" picker),
-- and converts pickup_time_from/to from TIME to VARCHAR so admins can enter
-- location names like "Hotel lobby" rather than only HH:MM:SS values.
-- Also renames the columns to match the new semantics.

-- 1. day_trips: add availability_mode (mirrors the existing tours.availability_mode column)
ALTER TABLE day_trips
    ADD COLUMN IF NOT EXISTS availability_mode VARCHAR(20) NOT NULL DEFAULT 'always'
        CONSTRAINT chk_day_trip_avail_mode CHECK (availability_mode IN ('always','on_request'));

-- 2. day_trip_pickup_zones: convert TIME -> VARCHAR and rename
--    Existing TIME values (e.g. '09:00:00') survive the cast as text.
ALTER TABLE day_trip_pickup_zones
    ALTER COLUMN pickup_time_from TYPE VARCHAR(100) USING pickup_time_from::text,
    ALTER COLUMN pickup_time_to   TYPE VARCHAR(100) USING pickup_time_to::text;

ALTER TABLE day_trip_pickup_zones
    RENAME COLUMN pickup_time_from TO pickup_from;
ALTER TABLE day_trip_pickup_zones
    RENAME COLUMN pickup_time_to   TO pickup_to;

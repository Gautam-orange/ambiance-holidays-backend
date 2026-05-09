-- V15: Add transmission/gear-count to cars.
--
-- Reference design captures a numeric "Transmission" field on the car detail
-- form (interpreted as the gear count, e.g. 5 / 6 / 8). This is independent
-- of the existing `is_automatic` boolean which only distinguishes Automatic
-- vs Manual transmissions.

ALTER TABLE cars
    ADD COLUMN IF NOT EXISTS transmission_gears SMALLINT
        CONSTRAINT chk_car_transmission_gears CHECK (transmission_gears IS NULL OR (transmission_gears > 0 AND transmission_gears <= 12));

-- Remove the Drivers module — no longer part of the product.
-- Drop the assignment table first because it has an FK into drivers.

DROP TABLE IF EXISTS driver_assignments CASCADE;
DROP TABLE IF EXISTS drivers CASCADE;

-- Custom enum that backed Driver.status.
DROP TYPE IF EXISTS driver_status;

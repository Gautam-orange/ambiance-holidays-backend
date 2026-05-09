-- V14: backfill car add-on services from the legacy XSVC: encoding.
--
-- Add-on services (Baby Seat, Additional Driver, GPS, etc.) were historically
-- encoded as `XSVC:Name:PriceCents` strings inside the `cars.includes` TEXT[]
-- column even though V3 created a dedicated `car_extra_services` table. This
-- migration:
--   1. Pulls every XSVC:* element out of cars.includes and inserts a
--      corresponding car_extra_services row.
--   2. Strips the XSVC:* entries from cars.includes so the remaining values
--      are real "What's Included" bullets.

-- 1) Backfill car_extra_services from XSVC entries
INSERT INTO car_extra_services (car_id, name, price_cents, display_order)
SELECT
    c.id,
    NULLIF(split_part(elem.val, ':', 2), '')                        AS name,
    COALESCE(NULLIF(split_part(elem.val, ':', 3), ''), '0')::int    AS price_cents,
    (elem.ord - 1)::smallint                                        AS display_order
FROM cars c
CROSS JOIN LATERAL unnest(c.includes) WITH ORDINALITY AS elem(val, ord)
WHERE c.includes IS NOT NULL
  AND elem.val LIKE 'XSVC:%'
  AND NULLIF(split_part(elem.val, ':', 2), '') IS NOT NULL;

-- 2) Strip XSVC:* entries from cars.includes
UPDATE cars
   SET includes = ARRAY(
       SELECT v
       FROM unnest(includes) AS v
       WHERE v NOT LIKE 'XSVC:%'
   )
 WHERE includes IS NOT NULL
   AND EXISTS (
       SELECT 1 FROM unnest(includes) v WHERE v LIKE 'XSVC:%'
   );

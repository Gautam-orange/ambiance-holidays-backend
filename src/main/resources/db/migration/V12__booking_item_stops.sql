-- Multi-trip transfers can have intermediate stops between pickup and drop-off.
-- We were already accepting `stops: string[]` from the cart payload but had no
-- column to persist it on the booking_items row, so the data was silently
-- dropped at checkout. Add a TEXT[] column.
ALTER TABLE booking_items
    ADD COLUMN IF NOT EXISTS stops TEXT[];

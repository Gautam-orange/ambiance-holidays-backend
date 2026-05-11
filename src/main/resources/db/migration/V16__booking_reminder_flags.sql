-- Reminder dedupe columns: the scheduler sets these once an email has been
-- queued for a booking so subsequent runs skip it. Nullable + timestamp so we
-- can also audit *when* the reminder went out.
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS reminder_day_sent_at       TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reminder_imminent_sent_at  TIMESTAMPTZ;

-- Index the scheduler's hot path: PENDING/CONFIRMED bookings whose serviceDate
-- is today and reminderDaySentAt is null.
CREATE INDEX IF NOT EXISTS idx_bookings_reminder_day
    ON bookings (service_date)
    WHERE reminder_day_sent_at IS NULL AND deleted_at IS NULL;

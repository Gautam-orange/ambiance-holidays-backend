-- Peach Payments integration: add columns + enum value

-- Payment method enum: add PEACH
ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'PEACH';

-- Payment columns for Peach
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS peach_checkout_id   VARCHAR(80),
    ADD COLUMN IF NOT EXISTS peach_payment_id    VARCHAR(80),
    ADD COLUMN IF NOT EXISTS peach_result_code   VARCHAR(40),
    ADD COLUMN IF NOT EXISTS peach_result_desc   VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_peach_checkout
    ON payments(peach_checkout_id) WHERE peach_checkout_id IS NOT NULL;

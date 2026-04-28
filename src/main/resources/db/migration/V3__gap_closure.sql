-- ============================================================
-- V3 — SOW Audit Gap Closure
-- ============================================================

-- ============================================================
-- 1.1 MISSING COLUMNS ON EXISTING TABLES
-- ============================================================

-- agents: email verification + rejection/suspension reasons
ALTER TABLE agents
    ADD COLUMN IF NOT EXISTS verification_token_hash TEXT,
    ADD COLUMN IF NOT EXISTS verification_sent_at     TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS rejection_reason         TEXT,
    ADD COLUMN IF NOT EXISTS suspension_reason        TEXT;

-- bookings: T&C audit trail + enquiry flow + cancellation type
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS terms_version        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS terms_agreed_at      TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS terms_agreed_ip      INET,
    ADD COLUMN IF NOT EXISTS terms_agreed_user_agent TEXT,
    ADD COLUMN IF NOT EXISTS is_enquiry           BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS enquiry_converted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS enquiry_declined_at  TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS cancelled_by_type    VARCHAR(10)
        CONSTRAINT chk_cancelled_by_type CHECK (cancelled_by_type IN ('ADMIN','CUSTOMER'));

CREATE INDEX IF NOT EXISTS idx_bookings_enquiry ON bookings(is_enquiry) WHERE deleted_at IS NULL AND is_enquiry = TRUE;

-- booking_items: multi-instance tabs (Hotel 1/2/3, Car Rental 1/2/3)
ALTER TABLE booking_items
    ADD COLUMN IF NOT EXISTS instance_index SMALLINT NOT NULL DEFAULT 1;

-- tour_pickup_zones: pickup time window (SOW §20.5.5)
ALTER TABLE tour_pickup_zones
    ADD COLUMN IF NOT EXISTS pickup_time_from TIME,
    ADD COLUMN IF NOT EXISTS pickup_time_to   TIME;

-- tours: availability mode + theme
ALTER TABLE tours
    ADD COLUMN IF NOT EXISTS availability_mode VARCHAR(20) NOT NULL DEFAULT 'always'
        CONSTRAINT chk_tour_avail_mode CHECK (availability_mode IN ('always','on_request')),
    ADD COLUMN IF NOT EXISTS theme VARCHAR(30)
        CONSTRAINT chk_tour_theme CHECK (theme IN ('NATURE','ADVENTURE','CULTURAL','SEA_ACTIVITIES','BEACH'));

-- day_trips: pricing + theme
ALTER TABLE day_trips
    ADD COLUMN IF NOT EXISTS theme                 VARCHAR(30)
        CONSTRAINT chk_day_trip_theme CHECK (theme IN ('NATURE','ADVENTURE','CULTURAL','SEA_ACTIVITIES','BEACH')),
    ADD COLUMN IF NOT EXISTS net_rate_per_pax_cents INTEGER NOT NULL DEFAULT 0
        CONSTRAINT chk_day_trip_net_rate CHECK (net_rate_per_pax_cents >= 0),
    ADD COLUMN IF NOT EXISTS markup_pct            NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS price_per_vehicle_cents INTEGER NOT NULL DEFAULT 0
        CONSTRAINT chk_day_trip_ppv CHECK (price_per_vehicle_cents >= 0);

-- cart_items: agent_id for server-side agent cart persistence
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS agent_id UUID REFERENCES agents(id);

CREATE INDEX IF NOT EXISTS idx_cart_agent ON cart_items(agent_id) WHERE agent_id IS NOT NULL;

-- newsletter_subscribers: double opt-in fields
ALTER TABLE newsletter_subscribers
    ADD COLUMN IF NOT EXISTS confirmation_token_hash TEXT,
    ADD COLUMN IF NOT EXISTS confirmed_at            TIMESTAMPTZ;

-- payments: fix default currency from EUR to MUR (SOW §16)
ALTER TABLE payments
    ALTER COLUMN currency SET DEFAULT 'MUR';

-- ============================================================
-- 1.2 NEW TABLES
-- ============================================================

-- Car extra services (Baby Seat, Additional Driver, Wifi Dongle, etc.)
CREATE TABLE IF NOT EXISTS car_extra_services (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    car_id        UUID NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    name          VARCHAR(100) NOT NULL,
    price_cents   INTEGER NOT NULL DEFAULT 0 CHECK (price_cents >= 0),
    display_order SMALLINT NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_car_extras_car ON car_extra_services(car_id);

-- Activity variants (Mini Golf, 4D Cinema per booking_item)
CREATE TABLE IF NOT EXISTS activity_variants (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_item_id  UUID NOT NULL REFERENCES booking_items(id) ON DELETE CASCADE,
    name             VARCHAR(150) NOT NULL,
    pax_adults       SMALLINT NOT NULL DEFAULT 0,
    pax_children     SMALLINT NOT NULL DEFAULT 0,
    pax_infants      SMALLINT NOT NULL DEFAULT 0,
    unit_price_cents INTEGER NOT NULL DEFAULT 0,
    subtotal_cents   INTEGER NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_activity_variants_item ON activity_variants(booking_item_id);

-- Day trip key highlights chips
CREATE TABLE IF NOT EXISTS day_trip_highlights (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    day_trip_id   UUID NOT NULL REFERENCES day_trips(id) ON DELETE CASCADE,
    text          VARCHAR(100) NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_dt_highlights ON day_trip_highlights(day_trip_id);

-- Product sessions (Half Day / Full Day differential pricing)
CREATE TABLE IF NOT EXISTS product_sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_type        VARCHAR(20) NOT NULL,
    product_id          UUID NOT NULL,
    label               VARCHAR(10) NOT NULL CHECK (label IN ('half_day','full_day')),
    price_adult_cents   INTEGER NOT NULL DEFAULT 0,
    price_child_cents   INTEGER NOT NULL DEFAULT 0,
    price_infant_cents  INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (product_type, product_id, label)
);
CREATE INDEX IF NOT EXISTS idx_product_sessions ON product_sessions(product_type, product_id);

-- Day trip itinerary stops
CREATE TABLE IF NOT EXISTS day_trip_itinerary_stops (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    day_trip_id UUID NOT NULL REFERENCES day_trips(id) ON DELETE CASCADE,
    stop_order  SMALLINT NOT NULL DEFAULT 0,
    title       VARCHAR(200) NOT NULL,
    time_label  VARCHAR(20),
    location    VARCHAR(200),
    description TEXT
);
CREATE INDEX IF NOT EXISTS idx_dt_itinerary ON day_trip_itinerary_stops(day_trip_id, stop_order);

-- Day trip pickup zones
CREATE TABLE IF NOT EXISTS day_trip_pickup_zones (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    day_trip_id      UUID NOT NULL REFERENCES day_trips(id) ON DELETE CASCADE,
    zone_name        VARCHAR(100) NOT NULL,
    hotel_name       VARCHAR(200),
    pickup_time_from TIME,
    pickup_time_to   TIME,
    sort_order       SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_dt_pickup_zones ON day_trip_pickup_zones(day_trip_id);

-- Transfer rates (replaces single base_price_cents — multi-line pricing)
CREATE TABLE IF NOT EXISTS transfer_rates (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id    UUID NOT NULL REFERENCES transfer_routes(id) ON DELETE CASCADE,
    rate_name   VARCHAR(50) NOT NULL,
    price_cents INTEGER NOT NULL DEFAULT 0 CHECK (price_cents >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_transfer_rates_route ON transfer_rates(route_id);

-- ============================================================
-- 1.3 SYSTEM SETTINGS SEED (idempotent)
-- ============================================================

INSERT INTO system_settings (key, value, data_type, description) VALUES
    ('vat_rate',                   '15.00',                          'DECIMAL', 'VAT percentage applied to all bookings'),
    ('same_day_cutoff_hour',       '18',                             'INTEGER', 'Same-day booking cutoff hour (Mauritius, UTC+4)'),
    ('cart_ttl_days',              '7',                              'INTEGER', 'Cart item TTL in days'),
    ('cancellation_free_hours',    '24',                             'INTEGER', 'Hours before service: free cancellation'),
    ('cancellation_50pct_hours',   '12',                             'INTEGER', 'Hours before service: 50% fee'),
    ('cancellation_75pct_hours',   '3',                              'INTEGER', 'Hours before service: 75% fee'),
    ('cancellation_100pct_hours',  '2',                              'INTEGER', 'Hours before service: 100% fee (no refund)'),
    ('head_office_address',        'Draper Avenue Quatre Bornes Mauritius', 'STRING', 'Head office address'),
    ('phone_1',                    '+230 5285 0500',                 'STRING', 'Primary contact phone'),
    ('phone_2',                    '+230 4608423',                   'STRING', 'Secondary contact phone'),
    ('email_general',              'info@ambianceholidays.com',       'STRING', 'General enquiries email'),
    ('email_reservations',         'reservation@ambianceholidays.com','STRING', 'Reservations email'),
    ('currency',                   'MUR',                            'STRING', 'Billing currency code'),
    ('currency_symbol',            'Rs',                             'STRING', 'Currency symbol for display'),
    ('default_commission',         '10.00',                          'DECIMAL', 'Default agent commission rate'),
    ('max_markup_percent',         '100.00',                         'DECIMAL', 'Maximum allowed agent markup %')
ON CONFLICT (key) DO UPDATE
    SET value = EXCLUDED.value,
        description = EXCLUDED.description,
        updated_at = NOW();

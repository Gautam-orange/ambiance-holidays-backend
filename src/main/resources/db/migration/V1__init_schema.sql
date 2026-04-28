-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
-- ENUMS
-- ============================================================
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER','B2B_AGENT','GUEST');
CREATE TYPE agent_status AS ENUM ('PENDING','ACTIVE','SUSPENDED');
CREATE TYPE agent_tier AS ENUM ('BRONZE','SILVER','GOLD','PLATINUM');
CREATE TYPE business_type AS ENUM ('TRAVEL_AGENCY','FREELANCER','CORPORATE');
CREATE TYPE car_category AS ENUM ('ECONOMY','STANDARD','PREMIUM','LUXURY','SUV','MINIVAN');
CREATE TYPE car_usage_type AS ENUM ('RENTAL','TRANSFER','BOTH');
CREATE TYPE car_status AS ENUM ('ACTIVE','INACTIVE','MAINTENANCE');
CREATE TYPE rate_period AS ENUM ('DAILY','WEEKLY','MONTHLY','PER_KM');
CREATE TYPE driver_status AS ENUM ('FREE','PARTIALLY_FREE','BOOKED','OFF_DUTY');
CREATE TYPE tour_category AS ENUM ('LAND','SEA','AIR');
CREATE TYPE tour_duration AS ENUM ('HALF_DAY','FULL_DAY');
CREATE TYPE tour_region AS ENUM ('NORTH','SOUTH','EAST','WEST','CENTRAL');
CREATE TYPE tour_status AS ENUM ('ACTIVE','ON_REQUEST','INACTIVE');
CREATE TYPE day_trip_type AS ENUM ('SHARED','PRIVATE');
CREATE TYPE transfer_trip_type AS ENUM ('ONE_WAY','ROUND_TRIP','ARRIVAL','DEPARTURE','HOURLY','POINT_TO_POINT','MULTI_TRIP');
CREATE TYPE booking_status AS ENUM ('PENDING','CONFIRMED','CANCELLED');
CREATE TYPE booking_item_type AS ENUM ('CAR_RENTAL','CAR_TRANSFER','TOUR','DAY_TRIP','HOTEL');
CREATE TYPE payment_status AS ENUM ('PENDING','SUCCEEDED','FAILED','REFUNDED','PARTIALLY_REFUNDED');
CREATE TYPE payment_method AS ENUM ('STRIPE','BANK_TRANSFER','CASH');
CREATE TYPE invoice_status AS ENUM ('DRAFT','ISSUED','PAID','VOID');
CREATE TYPE notification_channel AS ENUM ('EMAIL','SMS','WHATSAPP','IN_APP');
CREATE TYPE notification_status AS ENUM ('PENDING','SENT','FAILED');
CREATE TYPE audit_action AS ENUM (
    'USER_CREATED','USER_UPDATED','USER_DELETED',
    'AGENT_APPROVED','AGENT_SUSPENDED','AGENT_CREATED',
    'CAR_CREATED','CAR_UPDATED','CAR_DELETED',
    'TOUR_CREATED','TOUR_UPDATED','TOUR_DELETED',
    'DRIVER_CREATED','DRIVER_UPDATED',
    'BOOKING_CREATED','BOOKING_CONFIRMED','BOOKING_CANCELLED',
    'PAYMENT_SUCCEEDED','PAYMENT_FAILED','PAYMENT_REFUNDED',
    'DRIVER_ASSIGNED','DRIVER_UNASSIGNED',
    'INVOICE_ISSUED','INVOICE_VOIDED',
    'LOGIN','LOGOUT','PASSWORD_RESET','SETTINGS_CHANGED'
);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email            VARCHAR(254) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    phone            VARCHAR(30),
    whatsapp         VARCHAR(30),
    role             user_role NOT NULL DEFAULT 'GUEST',
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role  ON users(role)  WHERE deleted_at IS NULL;

-- ============================================================
-- AGENTS (B2B Partners — 1:1 with a User record)
-- ============================================================
CREATE TABLE agents (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users(id),
    company_name      VARCHAR(200) NOT NULL,
    country           VARCHAR(100) NOT NULL,
    city              VARCHAR(100),
    address           TEXT,
    business_type     business_type NOT NULL,
    tier              agent_tier NOT NULL DEFAULT 'BRONZE',
    status            agent_status NOT NULL DEFAULT 'PENDING',
    markup_percent    NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    commission_rate   NUMERIC(5,2) NOT NULL DEFAULT 10.00,
    credit_limit      INTEGER NOT NULL DEFAULT 0,
    total_bookings    INTEGER NOT NULL DEFAULT 0,
    business_proof_url VARCHAR(500),
    approved_at       TIMESTAMPTZ,
    approved_by_id    UUID REFERENCES users(id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);
CREATE UNIQUE INDEX idx_agents_user_id ON agents(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_agents_status ON agents(status) WHERE deleted_at IS NULL;

-- ============================================================
-- CUSTOMERS
-- ============================================================
CREATE TABLE customers (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID REFERENCES users(id),
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(254) NOT NULL,
    phone         VARCHAR(30),
    whatsapp      VARCHAR(30),
    nationality   VARCHAR(100),
    passport_no   VARCHAR(50),
    address       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);
CREATE INDEX idx_customers_email ON customers(email) WHERE deleted_at IS NULL;

-- ============================================================
-- SUPPLIERS
-- ============================================================
CREATE TABLE suppliers (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(200) NOT NULL,
    contact_name  VARCHAR(150),
    email         VARCHAR(254),
    phone         VARCHAR(30),
    country       VARCHAR(100),
    address       TEXT,
    notes         TEXT,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

-- ============================================================
-- CARS
-- ============================================================
CREATE TABLE cars (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id         UUID REFERENCES suppliers(id),
    registration_no     VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(150) NOT NULL,
    category            car_category NOT NULL,
    usage_type          car_usage_type NOT NULL,
    year                SMALLINT NOT NULL,
    passenger_capacity  SMALLINT NOT NULL,
    luggage_capacity    SMALLINT,
    has_ac              BOOLEAN NOT NULL DEFAULT TRUE,
    is_automatic        BOOLEAN NOT NULL DEFAULT TRUE,
    fuel_type           VARCHAR(30) NOT NULL DEFAULT 'Petrol',
    color               VARCHAR(50),
    description         TEXT,
    cover_image_url     VARCHAR(500),
    gallery_urls        TEXT[],
    includes            TEXT[],
    excludes            TEXT[],
    status              car_status NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
CREATE INDEX idx_cars_status   ON cars(status)     WHERE deleted_at IS NULL;
CREATE INDEX idx_cars_category ON cars(category)   WHERE deleted_at IS NULL;
CREATE INDEX idx_cars_usage    ON cars(usage_type) WHERE deleted_at IS NULL;

-- ============================================================
-- CAR RATES
-- ============================================================
CREATE TABLE car_rates (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    car_id        UUID NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    period        rate_period NOT NULL,
    amount_cents  INTEGER NOT NULL CHECK (amount_cents >= 0),
    km_from       INTEGER,
    km_to         INTEGER,
    valid_from    DATE,
    valid_to      DATE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_car_rates_car_id ON car_rates(car_id);

-- ============================================================
-- CAR AVAILABILITY
-- ============================================================
CREATE TABLE car_availability (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    car_id      UUID NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    date_from   DATE NOT NULL,
    date_to     DATE NOT NULL,
    reason      VARCHAR(200) DEFAULT 'BLOCKED',
    booking_id  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_car_avail_dates CHECK (date_to >= date_from)
);
CREATE INDEX idx_car_avail_car_date ON car_availability(car_id, date_from, date_to);

-- ============================================================
-- DRIVERS
-- ============================================================
CREATE TABLE drivers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code             VARCHAR(20),
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    phone            VARCHAR(30) NOT NULL,
    email            VARCHAR(254),
    address          TEXT,
    license_no       VARCHAR(100) NOT NULL,
    license_expiry   DATE NOT NULL,
    experience_years SMALLINT NOT NULL DEFAULT 0,
    status           driver_status NOT NULL DEFAULT 'FREE',
    photo_url        VARCHAR(500),
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);
CREATE INDEX idx_drivers_status ON drivers(status) WHERE deleted_at IS NULL;

-- ============================================================
-- DRIVER ASSIGNMENTS
-- ============================================================
CREATE TABLE driver_assignments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id        UUID NOT NULL REFERENCES drivers(id),
    booking_item_id  UUID NOT NULL,
    car_id           UUID REFERENCES cars(id),
    start_at         TIMESTAMPTZ NOT NULL,
    end_at           TIMESTAMPTZ NOT NULL,
    pickup_address   TEXT,
    dropoff_address  TEXT,
    notes            TEXT,
    assigned_by_id   UUID REFERENCES users(id),
    assigned_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_driver_assign_dates CHECK (end_at > start_at)
);
CREATE INDEX idx_driver_assign_driver_date ON driver_assignments(driver_id, start_at, end_at);

-- ============================================================
-- TOURS
-- ============================================================
CREATE TABLE tours (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id       UUID REFERENCES suppliers(id),
    title             VARCHAR(300) NOT NULL,
    slug              VARCHAR(300) NOT NULL UNIQUE,
    description       TEXT,
    category          tour_category NOT NULL,
    region            tour_region NOT NULL,
    duration          tour_duration NOT NULL,
    duration_hours    NUMERIC(4,1),
    adult_price_cents INTEGER NOT NULL CHECK (adult_price_cents >= 0),
    child_price_cents INTEGER NOT NULL CHECK (child_price_cents >= 0),
    infant_price_cents INTEGER NOT NULL DEFAULT 0,
    min_pax           SMALLINT NOT NULL DEFAULT 1,
    max_pax           SMALLINT NOT NULL DEFAULT 20,
    includes          TEXT[],
    excludes          TEXT[],
    important_notes   TEXT[],
    cover_image_url   VARCHAR(500),
    gallery_urls      TEXT[],
    status            tour_status NOT NULL DEFAULT 'ACTIVE',
    created_by_id     UUID REFERENCES users(id),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);
CREATE INDEX idx_tours_slug     ON tours(slug)     WHERE deleted_at IS NULL;
CREATE INDEX idx_tours_category ON tours(category) WHERE deleted_at IS NULL;
CREATE INDEX idx_tours_status   ON tours(status)   WHERE deleted_at IS NULL;
CREATE INDEX idx_tours_fts ON tours USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));

-- ============================================================
-- TOUR PICKUP ZONES
-- ============================================================
CREATE TABLE tour_pickup_zones (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id       UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    zone_name     VARCHAR(200) NOT NULL,
    extra_cents   INTEGER NOT NULL DEFAULT 0,
    pickup_time   TIME,
    sort_order    SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_tour_pickup_zones_tour ON tour_pickup_zones(tour_id);

-- ============================================================
-- TOUR ITINERARY STOPS
-- ============================================================
CREATE TABLE tour_itinerary_stops (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tour_id     UUID NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    stop_time   VARCHAR(20),
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    sort_order  SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_itinerary_tour ON tour_itinerary_stops(tour_id);

-- ============================================================
-- DAY TRIPS
-- ============================================================
CREATE TABLE day_trips (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id       UUID REFERENCES suppliers(id),
    title             VARCHAR(300) NOT NULL,
    slug              VARCHAR(300) NOT NULL UNIQUE,
    description       TEXT,
    trip_type         day_trip_type NOT NULL DEFAULT 'SHARED',
    region            tour_region NOT NULL,
    duration          tour_duration NOT NULL,
    adult_price_cents INTEGER NOT NULL CHECK (adult_price_cents >= 0),
    child_price_cents INTEGER NOT NULL CHECK (child_price_cents >= 0),
    max_pax           SMALLINT,
    includes          TEXT[],
    excludes          TEXT[],
    cover_image_url   VARCHAR(500),
    gallery_urls      TEXT[],
    status            tour_status NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ
);
CREATE INDEX idx_day_trips_slug ON day_trips(slug) WHERE deleted_at IS NULL;

-- ============================================================
-- TRANSFER ROUTES
-- ============================================================
CREATE TABLE transfer_routes (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_location     VARCHAR(300) NOT NULL,
    to_location       VARCHAR(300) NOT NULL,
    trip_type         transfer_trip_type NOT NULL DEFAULT 'ONE_WAY',
    car_category      car_category NOT NULL,
    base_price_cents  INTEGER NOT NULL CHECK (base_price_cents >= 0),
    est_duration_mins SMALLINT,
    est_km            SMALLINT,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_transfer_routes ON transfer_routes(from_location, to_location, trip_type);

-- ============================================================
-- BOOKINGS
-- ============================================================
CREATE TABLE bookings (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference              VARCHAR(30) NOT NULL UNIQUE,
    customer_id            UUID NOT NULL REFERENCES customers(id),
    agent_id               UUID REFERENCES agents(id),
    status                 booking_status NOT NULL DEFAULT 'PENDING',
    subtotal_cents         INTEGER NOT NULL DEFAULT 0,
    markup_cents           INTEGER NOT NULL DEFAULT 0,
    commission_cents       INTEGER NOT NULL DEFAULT 0,
    vat_cents              INTEGER NOT NULL DEFAULT 0,
    total_cents            INTEGER NOT NULL DEFAULT 0,
    vat_rate               NUMERIC(5,2) NOT NULL DEFAULT 15.00,
    markup_rate            NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    commission_rate        NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    special_requests       TEXT,
    cancel_reason          TEXT,
    cancelled_at           TIMESTAMPTZ,
    cancelled_by_id        UUID REFERENCES users(id),
    cancellation_fee_cents INTEGER NOT NULL DEFAULT 0,
    service_date           DATE NOT NULL,
    created_by_id          UUID REFERENCES users(id),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at             TIMESTAMPTZ
);
CREATE INDEX idx_bookings_reference  ON bookings(reference);
CREATE INDEX idx_bookings_customer   ON bookings(customer_id)  WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_agent      ON bookings(agent_id)     WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_status     ON bookings(status)       WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_date       ON bookings(service_date) WHERE deleted_at IS NULL;

-- ============================================================
-- BOOKING ITEMS
-- ============================================================
CREATE TABLE booking_items (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id        UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    item_type         booking_item_type NOT NULL,
    ref_id            UUID NOT NULL,
    quantity          SMALLINT NOT NULL DEFAULT 1,
    unit_price_cents  INTEGER NOT NULL,
    total_cents       INTEGER NOT NULL,
    service_date      DATE NOT NULL,
    start_at          TIMESTAMPTZ,
    end_at            TIMESTAMPTZ,
    pickup_location   TEXT,
    dropoff_location  TEXT,
    pax_adults        SMALLINT NOT NULL DEFAULT 1,
    pax_children      SMALLINT NOT NULL DEFAULT 0,
    pax_infants       SMALLINT NOT NULL DEFAULT 0,
    notes             TEXT,
    trip_type         transfer_trip_type,
    rental_days       SMALLINT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_booking_items_booking ON booking_items(booking_id);
CREATE INDEX idx_booking_items_ref     ON booking_items(ref_id);

-- ============================================================
-- BOOKING EXTRAS
-- ============================================================
CREATE TABLE booking_extras (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_item_id  UUID NOT NULL REFERENCES booking_items(id) ON DELETE CASCADE,
    label            VARCHAR(200) NOT NULL,
    quantity         SMALLINT NOT NULL DEFAULT 1,
    unit_price_cents INTEGER NOT NULL,
    total_cents      INTEGER NOT NULL
);

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id              UUID NOT NULL REFERENCES bookings(id),
    stripe_payment_intent   VARCHAR(200) UNIQUE,
    stripe_charge_id        VARCHAR(200),
    method                  payment_method NOT NULL DEFAULT 'STRIPE',
    amount_cents            INTEGER NOT NULL,
    currency                CHAR(3) NOT NULL DEFAULT 'EUR',
    status                  payment_status NOT NULL DEFAULT 'PENDING',
    refunded_cents          INTEGER NOT NULL DEFAULT 0,
    stripe_refund_id        VARCHAR(200),
    paid_at                 TIMESTAMPTZ,
    refunded_at             TIMESTAMPTZ,
    metadata                JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_payments_intent  ON payments(stripe_payment_intent);

-- ============================================================
-- INVOICES
-- ============================================================
CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL REFERENCES bookings(id),
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    status          invoice_status NOT NULL DEFAULT 'DRAFT',
    subtotal_cents  INTEGER NOT NULL,
    vat_cents       INTEGER NOT NULL,
    total_cents     INTEGER NOT NULL,
    pdf_url         VARCHAR(500),
    issued_at       TIMESTAMPTZ,
    due_date        DATE,
    voided_at       TIMESTAMPTZ,
    voided_reason   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_invoices_booking ON invoices(booking_id);
CREATE INDEX idx_invoices_number  ON invoices(invoice_number);

-- ============================================================
-- CART ITEMS (server-side, 7-day TTL)
-- ============================================================
CREATE TABLE cart_items (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_key      VARCHAR(200) NOT NULL,
    item_type        booking_item_type NOT NULL,
    ref_id           UUID NOT NULL,
    quantity         SMALLINT NOT NULL DEFAULT 1,
    unit_price_cents INTEGER NOT NULL,
    options          JSONB,
    expires_at       TIMESTAMPTZ NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cart_session ON cart_items(session_key);
CREATE INDEX idx_cart_expires ON cart_items(expires_at);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id),
    booking_id  UUID REFERENCES bookings(id),
    channel     notification_channel NOT NULL,
    status      notification_status NOT NULL DEFAULT 'PENDING',
    subject     VARCHAR(300),
    body        TEXT NOT NULL,
    recipient   VARCHAR(300) NOT NULL,
    sent_at     TIMESTAMPTZ,
    error_msg   TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_user    ON notifications(user_id);
CREATE INDEX idx_notif_booking ON notifications(booking_id);

-- ============================================================
-- AUDIT LOG (append-only, 7-year retention)
-- ============================================================
CREATE TABLE audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    action        audit_action NOT NULL,
    actor_id      UUID,
    actor_email   VARCHAR(254),
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     UUID,
    before_state  JSONB,
    after_state   JSONB,
    ip_address    INET,
    user_agent    TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_actor   ON audit_logs(actor_id);
CREATE INDEX idx_audit_entity  ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
CREATE POLICY audit_no_delete ON audit_logs AS RESTRICTIVE FOR DELETE USING (FALSE);

-- ============================================================
-- SYSTEM SETTINGS
-- ============================================================
CREATE TABLE system_settings (
    key         VARCHAR(100) PRIMARY KEY,
    value       TEXT NOT NULL,
    data_type   VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description TEXT,
    updated_by  UUID REFERENCES users(id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO system_settings (key, value, data_type, description) VALUES
    ('vat_rate',           '15.00', 'DECIMAL', 'VAT percentage applied to all bookings'),
    ('default_commission', '10.00', 'DECIMAL', 'Default agent commission rate'),
    ('cart_ttl_days',      '7',     'INTEGER', 'Cart item TTL in days'),
    ('cutoff_time_local',  '18:00', 'STRING',  'Same-day booking cutoff (Mauritius local time)'),
    ('invoice_prefix',     'INV',   'STRING',  'Invoice number prefix'),
    ('currency',           'EUR',   'STRING',  'Default billing currency');

-- ============================================================
-- LEADS
-- ============================================================
CREATE TABLE leads (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(200) NOT NULL,
    email         VARCHAR(254) NOT NULL,
    phone         VARCHAR(30),
    message       TEXT,
    source        VARCHAR(100) DEFAULT 'contact_form',
    is_converted  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- NEWSLETTER SUBSCRIBERS
-- ============================================================
CREATE TABLE newsletter_subscribers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email            VARCHAR(254) NOT NULL UNIQUE,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    subscribed_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    unsubscribed_at  TIMESTAMPTZ
);

-- ============================================================
-- REFRESH TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ NOT NULL,
    revoked_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_hash ON refresh_tokens(token_hash);

-- ============================================================
-- PASSWORD RESET TOKENS
-- ============================================================
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pwd_reset_hash ON password_reset_tokens(token_hash);

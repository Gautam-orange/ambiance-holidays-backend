--
-- PostgreSQL database dump
--

\restrict MQ5UAEilOON8lsfibSnLS8PpD8NnVdnoxxRs2Q7hyohKb3ThawqcMeVgwJStqfS

-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP POLICY IF EXISTS audit_no_delete ON public.audit_logs;
ALTER TABLE IF EXISTS ONLY public.transfer_rates DROP CONSTRAINT IF EXISTS transfer_rates_route_id_fkey;
ALTER TABLE IF EXISTS ONLY public.tours DROP CONSTRAINT IF EXISTS tours_supplier_id_fkey;
ALTER TABLE IF EXISTS ONLY public.tours DROP CONSTRAINT IF EXISTS tours_created_by_id_fkey;
ALTER TABLE IF EXISTS ONLY public.tour_pickup_zones DROP CONSTRAINT IF EXISTS tour_pickup_zones_tour_id_fkey;
ALTER TABLE IF EXISTS ONLY public.tour_itinerary_stops DROP CONSTRAINT IF EXISTS tour_itinerary_stops_tour_id_fkey;
ALTER TABLE IF EXISTS ONLY public.system_settings DROP CONSTRAINT IF EXISTS system_settings_updated_by_fkey;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.payments DROP CONSTRAINT IF EXISTS payments_booking_id_fkey;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_booking_id_fkey;
ALTER TABLE IF EXISTS ONLY public.invoices DROP CONSTRAINT IF EXISTS invoices_booking_id_fkey;
ALTER TABLE IF EXISTS ONLY public.day_trips DROP CONSTRAINT IF EXISTS day_trips_supplier_id_fkey;
ALTER TABLE IF EXISTS ONLY public.day_trip_pickup_zones DROP CONSTRAINT IF EXISTS day_trip_pickup_zones_day_trip_id_fkey;
ALTER TABLE IF EXISTS ONLY public.day_trip_itinerary_stops DROP CONSTRAINT IF EXISTS day_trip_itinerary_stops_day_trip_id_fkey;
ALTER TABLE IF EXISTS ONLY public.day_trip_highlights DROP CONSTRAINT IF EXISTS day_trip_highlights_day_trip_id_fkey;
ALTER TABLE IF EXISTS ONLY public.customers DROP CONSTRAINT IF EXISTS customers_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.cart_items DROP CONSTRAINT IF EXISTS cart_items_agent_id_fkey;
ALTER TABLE IF EXISTS ONLY public.cars DROP CONSTRAINT IF EXISTS cars_supplier_id_fkey;
ALTER TABLE IF EXISTS ONLY public.car_rates DROP CONSTRAINT IF EXISTS car_rates_car_id_fkey;
ALTER TABLE IF EXISTS ONLY public.car_extra_services DROP CONSTRAINT IF EXISTS car_extra_services_car_id_fkey;
ALTER TABLE IF EXISTS ONLY public.car_availability DROP CONSTRAINT IF EXISTS car_availability_car_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bookings DROP CONSTRAINT IF EXISTS bookings_customer_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bookings DROP CONSTRAINT IF EXISTS bookings_created_by_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bookings DROP CONSTRAINT IF EXISTS bookings_cancelled_by_id_fkey;
ALTER TABLE IF EXISTS ONLY public.bookings DROP CONSTRAINT IF EXISTS bookings_agent_id_fkey;
ALTER TABLE IF EXISTS ONLY public.booking_items DROP CONSTRAINT IF EXISTS booking_items_booking_id_fkey;
ALTER TABLE IF EXISTS ONLY public.booking_extras DROP CONSTRAINT IF EXISTS booking_extras_booking_item_id_fkey;
ALTER TABLE IF EXISTS ONLY public.agents DROP CONSTRAINT IF EXISTS agents_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.agents DROP CONSTRAINT IF EXISTS agents_approved_by_id_fkey;
ALTER TABLE IF EXISTS ONLY public.activity_variants DROP CONSTRAINT IF EXISTS activity_variants_booking_item_id_fkey;
DROP INDEX IF EXISTS public.idx_users_role;
DROP INDEX IF EXISTS public.idx_users_email;
DROP INDEX IF EXISTS public.idx_transfer_routes;
DROP INDEX IF EXISTS public.idx_transfer_rates_route;
DROP INDEX IF EXISTS public.idx_tours_status;
DROP INDEX IF EXISTS public.idx_tours_slug;
DROP INDEX IF EXISTS public.idx_tours_fts;
DROP INDEX IF EXISTS public.idx_tours_category;
DROP INDEX IF EXISTS public.idx_tour_pickup_zones_tour;
DROP INDEX IF EXISTS public.idx_refresh_user;
DROP INDEX IF EXISTS public.idx_refresh_hash;
DROP INDEX IF EXISTS public.idx_pwd_reset_user_active;
DROP INDEX IF EXISTS public.idx_pwd_reset_hash;
DROP INDEX IF EXISTS public.idx_product_sessions;
DROP INDEX IF EXISTS public.idx_payments_peach_checkout;
DROP INDEX IF EXISTS public.idx_payments_intent;
DROP INDEX IF EXISTS public.idx_payments_booking;
DROP INDEX IF EXISTS public.idx_notif_user;
DROP INDEX IF EXISTS public.idx_notif_booking;
DROP INDEX IF EXISTS public.idx_login_attempts_email_time;
DROP INDEX IF EXISTS public.idx_itinerary_tour;
DROP INDEX IF EXISTS public.idx_invoices_number;
DROP INDEX IF EXISTS public.idx_invoices_booking;
DROP INDEX IF EXISTS public.idx_dt_pickup_zones;
DROP INDEX IF EXISTS public.idx_dt_itinerary;
DROP INDEX IF EXISTS public.idx_dt_highlights;
DROP INDEX IF EXISTS public.idx_day_trips_slug;
DROP INDEX IF EXISTS public.idx_customers_email;
DROP INDEX IF EXISTS public.idx_cart_session;
DROP INDEX IF EXISTS public.idx_cart_expires;
DROP INDEX IF EXISTS public.idx_cart_agent;
DROP INDEX IF EXISTS public.idx_cars_usage;
DROP INDEX IF EXISTS public.idx_cars_status;
DROP INDEX IF EXISTS public.idx_cars_category;
DROP INDEX IF EXISTS public.idx_car_rates_car_id;
DROP INDEX IF EXISTS public.idx_car_extras_car;
DROP INDEX IF EXISTS public.idx_car_avail_car_date;
DROP INDEX IF EXISTS public.idx_bookings_status;
DROP INDEX IF EXISTS public.idx_bookings_reference;
DROP INDEX IF EXISTS public.idx_bookings_enquiry;
DROP INDEX IF EXISTS public.idx_bookings_date;
DROP INDEX IF EXISTS public.idx_bookings_customer;
DROP INDEX IF EXISTS public.idx_bookings_agent;
DROP INDEX IF EXISTS public.idx_booking_items_ref;
DROP INDEX IF EXISTS public.idx_booking_items_booking;
DROP INDEX IF EXISTS public.idx_audit_entity;
DROP INDEX IF EXISTS public.idx_audit_created;
DROP INDEX IF EXISTS public.idx_audit_actor;
DROP INDEX IF EXISTS public.idx_agents_user_id;
DROP INDEX IF EXISTS public.idx_agents_status;
DROP INDEX IF EXISTS public.idx_activity_variants_item;
DROP INDEX IF EXISTS public.flyway_schema_history_s_idx;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE IF EXISTS ONLY public.transfer_routes DROP CONSTRAINT IF EXISTS transfer_routes_pkey;
ALTER TABLE IF EXISTS ONLY public.transfer_rates DROP CONSTRAINT IF EXISTS transfer_rates_pkey;
ALTER TABLE IF EXISTS ONLY public.transfer_pricing_tiers DROP CONSTRAINT IF EXISTS transfer_pricing_tiers_pkey;
ALTER TABLE IF EXISTS ONLY public.tours DROP CONSTRAINT IF EXISTS tours_slug_key;
ALTER TABLE IF EXISTS ONLY public.tours DROP CONSTRAINT IF EXISTS tours_pkey;
ALTER TABLE IF EXISTS ONLY public.tour_pickup_zones DROP CONSTRAINT IF EXISTS tour_pickup_zones_pkey;
ALTER TABLE IF EXISTS ONLY public.tour_itinerary_stops DROP CONSTRAINT IF EXISTS tour_itinerary_stops_pkey;
ALTER TABLE IF EXISTS ONLY public.system_settings DROP CONSTRAINT IF EXISTS system_settings_pkey;
ALTER TABLE IF EXISTS ONLY public.suppliers DROP CONSTRAINT IF EXISTS suppliers_pkey;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_token_hash_key;
ALTER TABLE IF EXISTS ONLY public.refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.product_sessions DROP CONSTRAINT IF EXISTS product_sessions_product_type_product_id_label_key;
ALTER TABLE IF EXISTS ONLY public.product_sessions DROP CONSTRAINT IF EXISTS product_sessions_pkey;
ALTER TABLE IF EXISTS ONLY public.payments DROP CONSTRAINT IF EXISTS payments_stripe_payment_intent_key;
ALTER TABLE IF EXISTS ONLY public.payments DROP CONSTRAINT IF EXISTS payments_pkey;
ALTER TABLE IF EXISTS ONLY public.password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_pkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_pkey;
ALTER TABLE IF EXISTS ONLY public.newsletter_subscribers DROP CONSTRAINT IF EXISTS newsletter_subscribers_pkey;
ALTER TABLE IF EXISTS ONLY public.newsletter_subscribers DROP CONSTRAINT IF EXISTS newsletter_subscribers_email_key;
ALTER TABLE IF EXISTS ONLY public.login_attempts DROP CONSTRAINT IF EXISTS login_attempts_pkey;
ALTER TABLE IF EXISTS ONLY public.leads DROP CONSTRAINT IF EXISTS leads_pkey;
ALTER TABLE IF EXISTS ONLY public.invoices DROP CONSTRAINT IF EXISTS invoices_pkey;
ALTER TABLE IF EXISTS ONLY public.invoices DROP CONSTRAINT IF EXISTS invoices_invoice_number_key;
ALTER TABLE IF EXISTS ONLY public.flyway_schema_history DROP CONSTRAINT IF EXISTS flyway_schema_history_pk;
ALTER TABLE IF EXISTS ONLY public.day_trips DROP CONSTRAINT IF EXISTS day_trips_slug_key;
ALTER TABLE IF EXISTS ONLY public.day_trips DROP CONSTRAINT IF EXISTS day_trips_pkey;
ALTER TABLE IF EXISTS ONLY public.day_trip_pickup_zones DROP CONSTRAINT IF EXISTS day_trip_pickup_zones_pkey;
ALTER TABLE IF EXISTS ONLY public.day_trip_itinerary_stops DROP CONSTRAINT IF EXISTS day_trip_itinerary_stops_pkey;
ALTER TABLE IF EXISTS ONLY public.day_trip_highlights DROP CONSTRAINT IF EXISTS day_trip_highlights_pkey;
ALTER TABLE IF EXISTS ONLY public.customers DROP CONSTRAINT IF EXISTS customers_pkey;
ALTER TABLE IF EXISTS ONLY public.cart_items DROP CONSTRAINT IF EXISTS cart_items_pkey;
ALTER TABLE IF EXISTS ONLY public.cars DROP CONSTRAINT IF EXISTS cars_registration_no_key;
ALTER TABLE IF EXISTS ONLY public.cars DROP CONSTRAINT IF EXISTS cars_pkey;
ALTER TABLE IF EXISTS ONLY public.car_rates DROP CONSTRAINT IF EXISTS car_rates_pkey;
ALTER TABLE IF EXISTS ONLY public.car_extra_services DROP CONSTRAINT IF EXISTS car_extra_services_pkey;
ALTER TABLE IF EXISTS ONLY public.car_availability DROP CONSTRAINT IF EXISTS car_availability_pkey;
ALTER TABLE IF EXISTS ONLY public.bookings DROP CONSTRAINT IF EXISTS bookings_reference_key;
ALTER TABLE IF EXISTS ONLY public.bookings DROP CONSTRAINT IF EXISTS bookings_pkey;
ALTER TABLE IF EXISTS ONLY public.booking_items DROP CONSTRAINT IF EXISTS booking_items_pkey;
ALTER TABLE IF EXISTS ONLY public.booking_extras DROP CONSTRAINT IF EXISTS booking_extras_pkey;
ALTER TABLE IF EXISTS ONLY public.audit_logs DROP CONSTRAINT IF EXISTS audit_logs_pkey;
ALTER TABLE IF EXISTS ONLY public.agents DROP CONSTRAINT IF EXISTS agents_pkey;
ALTER TABLE IF EXISTS ONLY public.activity_variants DROP CONSTRAINT IF EXISTS activity_variants_pkey;
ALTER TABLE IF EXISTS public.audit_logs ALTER COLUMN id DROP DEFAULT;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.transfer_routes;
DROP TABLE IF EXISTS public.transfer_rates;
DROP TABLE IF EXISTS public.transfer_pricing_tiers;
DROP TABLE IF EXISTS public.tours;
DROP TABLE IF EXISTS public.tour_pickup_zones;
DROP TABLE IF EXISTS public.tour_itinerary_stops;
DROP TABLE IF EXISTS public.system_settings;
DROP TABLE IF EXISTS public.suppliers;
DROP TABLE IF EXISTS public.refresh_tokens;
DROP TABLE IF EXISTS public.product_sessions;
DROP TABLE IF EXISTS public.payments;
DROP TABLE IF EXISTS public.password_reset_tokens;
DROP TABLE IF EXISTS public.notifications;
DROP TABLE IF EXISTS public.newsletter_subscribers;
DROP TABLE IF EXISTS public.login_attempts;
DROP TABLE IF EXISTS public.leads;
DROP TABLE IF EXISTS public.invoices;
DROP TABLE IF EXISTS public.flyway_schema_history;
DROP TABLE IF EXISTS public.day_trips;
DROP TABLE IF EXISTS public.day_trip_pickup_zones;
DROP TABLE IF EXISTS public.day_trip_itinerary_stops;
DROP TABLE IF EXISTS public.day_trip_highlights;
DROP TABLE IF EXISTS public.customers;
DROP TABLE IF EXISTS public.cart_items;
DROP TABLE IF EXISTS public.cars;
DROP TABLE IF EXISTS public.car_rates;
DROP TABLE IF EXISTS public.car_extra_services;
DROP TABLE IF EXISTS public.car_availability;
DROP TABLE IF EXISTS public.bookings;
DROP TABLE IF EXISTS public.booking_items;
DROP TABLE IF EXISTS public.booking_extras;
DROP SEQUENCE IF EXISTS public.audit_logs_id_seq;
DROP TABLE IF EXISTS public.audit_logs;
DROP TABLE IF EXISTS public.agents;
DROP TABLE IF EXISTS public.activity_variants;
DROP TYPE IF EXISTS public.user_role;
DROP TYPE IF EXISTS public.transfer_trip_type;
DROP TYPE IF EXISTS public.tour_status;
DROP TYPE IF EXISTS public.tour_region;
DROP TYPE IF EXISTS public.tour_duration;
DROP TYPE IF EXISTS public.tour_category;
DROP TYPE IF EXISTS public.rate_period;
DROP TYPE IF EXISTS public.payment_status;
DROP TYPE IF EXISTS public.payment_method;
DROP TYPE IF EXISTS public.notification_status;
DROP TYPE IF EXISTS public.notification_channel;
DROP TYPE IF EXISTS public.invoice_status;
DROP TYPE IF EXISTS public.day_trip_type;
DROP TYPE IF EXISTS public.car_usage_type;
DROP TYPE IF EXISTS public.car_status;
DROP TYPE IF EXISTS public.car_category;
DROP TYPE IF EXISTS public.business_type;
DROP TYPE IF EXISTS public.booking_status;
DROP TYPE IF EXISTS public.booking_item_type;
DROP TYPE IF EXISTS public.audit_action;
DROP TYPE IF EXISTS public.agent_tier;
DROP TYPE IF EXISTS public.agent_status;
DROP EXTENSION IF EXISTS pgcrypto;
DROP EXTENSION IF EXISTS pg_trgm;
--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: agent_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.agent_status AS ENUM (
    'PENDING',
    'ACTIVE',
    'SUSPENDED'
);


--
-- Name: agent_tier; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.agent_tier AS ENUM (
    'BRONZE',
    'SILVER',
    'GOLD',
    'PLATINUM'
);


--
-- Name: audit_action; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.audit_action AS ENUM (
    'USER_CREATED',
    'USER_UPDATED',
    'USER_DELETED',
    'AGENT_APPROVED',
    'AGENT_SUSPENDED',
    'AGENT_CREATED',
    'CAR_CREATED',
    'CAR_UPDATED',
    'CAR_DELETED',
    'TOUR_CREATED',
    'TOUR_UPDATED',
    'TOUR_DELETED',
    'DRIVER_CREATED',
    'DRIVER_UPDATED',
    'BOOKING_CREATED',
    'BOOKING_CONFIRMED',
    'BOOKING_CANCELLED',
    'PAYMENT_SUCCEEDED',
    'PAYMENT_FAILED',
    'PAYMENT_REFUNDED',
    'DRIVER_ASSIGNED',
    'DRIVER_UNASSIGNED',
    'INVOICE_ISSUED',
    'INVOICE_VOIDED',
    'LOGIN',
    'LOGOUT',
    'PASSWORD_RESET',
    'SETTINGS_CHANGED'
);


--
-- Name: booking_item_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.booking_item_type AS ENUM (
    'CAR_RENTAL',
    'CAR_TRANSFER',
    'TOUR',
    'DAY_TRIP',
    'HOTEL'
);


--
-- Name: booking_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.booking_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'CANCELLED'
);


--
-- Name: business_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.business_type AS ENUM (
    'TRAVEL_AGENCY',
    'FREELANCER',
    'CORPORATE'
);


--
-- Name: car_category; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.car_category AS ENUM (
    'ECONOMY',
    'STANDARD',
    'PREMIUM',
    'LUXURY',
    'SUV',
    'MINIVAN'
);


--
-- Name: car_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.car_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'MAINTENANCE'
);


--
-- Name: car_usage_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.car_usage_type AS ENUM (
    'RENTAL',
    'TRANSFER',
    'BOTH'
);


--
-- Name: day_trip_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.day_trip_type AS ENUM (
    'SHARED',
    'PRIVATE'
);


--
-- Name: invoice_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.invoice_status AS ENUM (
    'DRAFT',
    'ISSUED',
    'PAID',
    'VOID'
);


--
-- Name: notification_channel; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.notification_channel AS ENUM (
    'EMAIL',
    'SMS',
    'WHATSAPP',
    'IN_APP'
);


--
-- Name: notification_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.notification_status AS ENUM (
    'PENDING',
    'SENT',
    'FAILED'
);


--
-- Name: payment_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_method AS ENUM (
    'STRIPE',
    'BANK_TRANSFER',
    'CASH',
    'PEACH'
);


--
-- Name: payment_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.payment_status AS ENUM (
    'PENDING',
    'SUCCEEDED',
    'FAILED',
    'REFUNDED',
    'PARTIALLY_REFUNDED'
);


--
-- Name: rate_period; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.rate_period AS ENUM (
    'DAILY',
    'WEEKLY',
    'MONTHLY',
    'PER_KM'
);


--
-- Name: tour_category; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tour_category AS ENUM (
    'LAND',
    'SEA',
    'AIR'
);


--
-- Name: tour_duration; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tour_duration AS ENUM (
    'HALF_DAY',
    'FULL_DAY'
);


--
-- Name: tour_region; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tour_region AS ENUM (
    'NORTH',
    'SOUTH',
    'EAST',
    'WEST',
    'CENTRAL'
);


--
-- Name: tour_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tour_status AS ENUM (
    'ACTIVE',
    'ON_REQUEST',
    'INACTIVE'
);


--
-- Name: transfer_trip_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.transfer_trip_type AS ENUM (
    'ONE_WAY',
    'ROUND_TRIP',
    'ARRIVAL',
    'DEPARTURE',
    'HOURLY',
    'POINT_TO_POINT',
    'MULTI_TRIP'
);


--
-- Name: user_role; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_role AS ENUM (
    'SUPER_ADMIN',
    'ADMIN_OPS',
    'FLEET_MANAGER',
    'B2B_AGENT',
    'GUEST'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: activity_variants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.activity_variants (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    booking_item_id uuid NOT NULL,
    name character varying(150) NOT NULL,
    pax_adults smallint DEFAULT 0 NOT NULL,
    pax_children smallint DEFAULT 0 NOT NULL,
    pax_infants smallint DEFAULT 0 NOT NULL,
    unit_price_cents integer DEFAULT 0 NOT NULL,
    subtotal_cents integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: agents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agents (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    company_name character varying(200) NOT NULL,
    country character varying(100) NOT NULL,
    city character varying(100),
    address text,
    business_type public.business_type NOT NULL,
    tier public.agent_tier DEFAULT 'BRONZE'::public.agent_tier NOT NULL,
    status public.agent_status DEFAULT 'PENDING'::public.agent_status NOT NULL,
    markup_percent numeric(5,2) DEFAULT 0.00 NOT NULL,
    commission_rate numeric(5,2) DEFAULT 10.00 NOT NULL,
    credit_limit integer DEFAULT 0 NOT NULL,
    total_bookings integer DEFAULT 0 NOT NULL,
    business_proof_url character varying(500),
    approved_at timestamp with time zone,
    approved_by_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    verification_token_hash text,
    verification_sent_at timestamp with time zone,
    rejection_reason text,
    suspension_reason text
);


--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_logs (
    id bigint NOT NULL,
    action public.audit_action NOT NULL,
    actor_id uuid,
    actor_email character varying(254),
    entity_type character varying(100) NOT NULL,
    entity_id uuid,
    before_state jsonb,
    after_state jsonb,
    ip_address inet,
    user_agent text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_logs_id_seq OWNED BY public.audit_logs.id;


--
-- Name: booking_extras; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking_extras (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    booking_item_id uuid NOT NULL,
    label character varying(200) NOT NULL,
    quantity smallint DEFAULT 1 NOT NULL,
    unit_price_cents integer NOT NULL,
    total_cents integer NOT NULL
);


--
-- Name: booking_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking_items (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    booking_id uuid NOT NULL,
    item_type public.booking_item_type NOT NULL,
    ref_id uuid NOT NULL,
    quantity smallint DEFAULT 1 NOT NULL,
    unit_price_cents integer NOT NULL,
    total_cents integer NOT NULL,
    service_date date NOT NULL,
    start_at timestamp with time zone,
    end_at timestamp with time zone,
    pickup_location text,
    dropoff_location text,
    pax_adults smallint DEFAULT 1 NOT NULL,
    pax_children smallint DEFAULT 0 NOT NULL,
    pax_infants smallint DEFAULT 0 NOT NULL,
    notes text,
    trip_type public.transfer_trip_type,
    rental_days smallint,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    instance_index smallint DEFAULT 1 NOT NULL
);


--
-- Name: bookings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bookings (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    reference character varying(30) NOT NULL,
    customer_id uuid NOT NULL,
    agent_id uuid,
    status public.booking_status DEFAULT 'PENDING'::public.booking_status NOT NULL,
    subtotal_cents integer DEFAULT 0 NOT NULL,
    markup_cents integer DEFAULT 0 NOT NULL,
    commission_cents integer DEFAULT 0 NOT NULL,
    vat_cents integer DEFAULT 0 NOT NULL,
    total_cents integer DEFAULT 0 NOT NULL,
    vat_rate numeric(5,2) DEFAULT 15.00 NOT NULL,
    markup_rate numeric(5,2) DEFAULT 0.00 NOT NULL,
    commission_rate numeric(5,2) DEFAULT 0.00 NOT NULL,
    special_requests text,
    cancel_reason text,
    cancelled_at timestamp with time zone,
    cancelled_by_id uuid,
    cancellation_fee_cents integer DEFAULT 0 NOT NULL,
    service_date date NOT NULL,
    created_by_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    terms_version character varying(20),
    terms_agreed_at timestamp with time zone,
    terms_agreed_ip inet,
    terms_agreed_user_agent text,
    is_enquiry boolean DEFAULT false NOT NULL,
    enquiry_converted_at timestamp with time zone,
    enquiry_declined_at timestamp with time zone,
    cancelled_by_type character varying(10),
    CONSTRAINT chk_cancelled_by_type CHECK (((cancelled_by_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'CUSTOMER'::character varying])::text[])))
);


--
-- Name: car_availability; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.car_availability (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    car_id uuid NOT NULL,
    date_from date NOT NULL,
    date_to date NOT NULL,
    reason character varying(200) DEFAULT 'BLOCKED'::character varying,
    booking_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT chk_car_avail_dates CHECK ((date_to >= date_from))
);


--
-- Name: car_extra_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.car_extra_services (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    car_id uuid NOT NULL,
    name character varying(100) NOT NULL,
    price_cents integer DEFAULT 0 NOT NULL,
    display_order smallint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT car_extra_services_price_cents_check CHECK ((price_cents >= 0))
);


--
-- Name: car_rates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.car_rates (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    car_id uuid NOT NULL,
    period public.rate_period NOT NULL,
    amount_cents integer NOT NULL,
    km_from integer,
    km_to integer,
    valid_from date,
    valid_to date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT car_rates_amount_cents_check CHECK ((amount_cents >= 0))
);


--
-- Name: cars; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cars (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    supplier_id uuid,
    registration_no character varying(50) NOT NULL,
    name character varying(150) NOT NULL,
    category public.car_category NOT NULL,
    usage_type public.car_usage_type NOT NULL,
    year smallint NOT NULL,
    passenger_capacity smallint NOT NULL,
    luggage_capacity smallint,
    has_ac boolean DEFAULT true NOT NULL,
    is_automatic boolean DEFAULT true NOT NULL,
    fuel_type character varying(30) DEFAULT 'Petrol'::character varying NOT NULL,
    color character varying(50),
    description text,
    cover_image_url character varying(500),
    gallery_urls text[],
    includes text[],
    excludes text[],
    status public.car_status DEFAULT 'ACTIVE'::public.car_status NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_items (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    session_key character varying(200) NOT NULL,
    item_type public.booking_item_type NOT NULL,
    ref_id uuid NOT NULL,
    quantity smallint DEFAULT 1 NOT NULL,
    unit_price_cents integer NOT NULL,
    options jsonb,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    agent_id uuid
);


--
-- Name: customers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customers (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    email character varying(254) NOT NULL,
    phone character varying(30),
    whatsapp character varying(30),
    nationality character varying(100),
    passport_no character varying(50),
    address text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: day_trip_highlights; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.day_trip_highlights (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    day_trip_id uuid NOT NULL,
    text character varying(100) NOT NULL,
    display_order smallint DEFAULT 0 NOT NULL
);


--
-- Name: day_trip_itinerary_stops; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.day_trip_itinerary_stops (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    day_trip_id uuid NOT NULL,
    stop_order smallint DEFAULT 0 NOT NULL,
    title character varying(200) NOT NULL,
    time_label character varying(20),
    location character varying(200),
    description text
);


--
-- Name: day_trip_pickup_zones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.day_trip_pickup_zones (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    day_trip_id uuid NOT NULL,
    zone_name character varying(100) NOT NULL,
    hotel_name character varying(200),
    pickup_time_from time without time zone,
    pickup_time_to time without time zone,
    sort_order smallint DEFAULT 0 NOT NULL
);


--
-- Name: day_trips; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.day_trips (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    supplier_id uuid,
    title character varying(300) NOT NULL,
    slug character varying(300) NOT NULL,
    description text,
    trip_type public.day_trip_type DEFAULT 'SHARED'::public.day_trip_type NOT NULL,
    region public.tour_region NOT NULL,
    duration public.tour_duration NOT NULL,
    adult_price_cents integer NOT NULL,
    child_price_cents integer NOT NULL,
    max_pax smallint,
    includes text[],
    excludes text[],
    cover_image_url character varying(500),
    gallery_urls text[],
    status public.tour_status DEFAULT 'ACTIVE'::public.tour_status NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    theme character varying(30),
    net_rate_per_pax_cents integer DEFAULT 0 NOT NULL,
    markup_pct numeric(5,2) DEFAULT 0.00 NOT NULL,
    price_per_vehicle_cents integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_day_trip_net_rate CHECK ((net_rate_per_pax_cents >= 0)),
    CONSTRAINT chk_day_trip_ppv CHECK ((price_per_vehicle_cents >= 0)),
    CONSTRAINT chk_day_trip_theme CHECK (((theme)::text = ANY ((ARRAY['NATURE'::character varying, 'ADVENTURE'::character varying, 'CULTURAL'::character varying, 'SEA_ACTIVITIES'::character varying, 'BEACH'::character varying])::text[]))),
    CONSTRAINT day_trips_adult_price_cents_check CHECK ((adult_price_cents >= 0)),
    CONSTRAINT day_trips_child_price_cents_check CHECK ((child_price_cents >= 0))
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: invoices; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoices (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    booking_id uuid NOT NULL,
    invoice_number character varying(50) NOT NULL,
    status public.invoice_status DEFAULT 'DRAFT'::public.invoice_status NOT NULL,
    subtotal_cents integer NOT NULL,
    vat_cents integer NOT NULL,
    total_cents integer NOT NULL,
    pdf_url character varying(500),
    issued_at timestamp with time zone,
    due_date date,
    voided_at timestamp with time zone,
    voided_reason text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: leads; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.leads (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(200) NOT NULL,
    email character varying(254) NOT NULL,
    phone character varying(30),
    message text,
    source character varying(100) DEFAULT 'contact_form'::character varying,
    is_converted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: login_attempts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.login_attempts (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    email text NOT NULL,
    ip_address text,
    attempted_at timestamp with time zone DEFAULT now() NOT NULL,
    success boolean DEFAULT false NOT NULL
);


--
-- Name: newsletter_subscribers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.newsletter_subscribers (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    email character varying(254) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    subscribed_at timestamp with time zone DEFAULT now() NOT NULL,
    unsubscribed_at timestamp with time zone,
    confirmation_token_hash text,
    confirmed_at timestamp with time zone
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid,
    booking_id uuid,
    channel public.notification_channel NOT NULL,
    status public.notification_status DEFAULT 'PENDING'::public.notification_status NOT NULL,
    subject character varying(300),
    body text NOT NULL,
    recipient character varying(300) NOT NULL,
    sent_at timestamp with time zone,
    error_msg text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    token_hash character varying(255) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    attempts integer DEFAULT 0 NOT NULL
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    booking_id uuid NOT NULL,
    stripe_payment_intent character varying(200),
    stripe_charge_id character varying(200),
    method public.payment_method DEFAULT 'STRIPE'::public.payment_method NOT NULL,
    amount_cents integer NOT NULL,
    currency character(3) DEFAULT 'MUR'::bpchar NOT NULL,
    status public.payment_status DEFAULT 'PENDING'::public.payment_status NOT NULL,
    refunded_cents integer DEFAULT 0 NOT NULL,
    stripe_refund_id character varying(200),
    paid_at timestamp with time zone,
    refunded_at timestamp with time zone,
    metadata jsonb,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    peach_checkout_id character varying(80),
    peach_payment_id character varying(80),
    peach_result_code character varying(40),
    peach_result_desc character varying(255)
);


--
-- Name: product_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_sessions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    product_type character varying(20) NOT NULL,
    product_id uuid NOT NULL,
    label character varying(10) NOT NULL,
    price_adult_cents integer DEFAULT 0 NOT NULL,
    price_child_cents integer DEFAULT 0 NOT NULL,
    price_infant_cents integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT product_sessions_label_check CHECK (((label)::text = ANY ((ARRAY['half_day'::character varying, 'full_day'::character varying])::text[])))
);


--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_tokens (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    token_hash character varying(255) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: suppliers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suppliers (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    name character varying(200) NOT NULL,
    contact_name character varying(150),
    email character varying(254),
    phone character varying(30),
    country character varying(100),
    address text,
    notes text,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: system_settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_settings (
    key character varying(100) NOT NULL,
    value text NOT NULL,
    data_type character varying(20) DEFAULT 'STRING'::character varying NOT NULL,
    description text,
    updated_by uuid,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: tour_itinerary_stops; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tour_itinerary_stops (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tour_id uuid NOT NULL,
    stop_time character varying(20),
    title character varying(200) NOT NULL,
    description text,
    sort_order smallint DEFAULT 0 NOT NULL
);


--
-- Name: tour_pickup_zones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tour_pickup_zones (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tour_id uuid NOT NULL,
    zone_name character varying(200) NOT NULL,
    extra_cents integer DEFAULT 0 NOT NULL,
    pickup_time time without time zone,
    sort_order smallint DEFAULT 0 NOT NULL,
    pickup_time_from time without time zone,
    pickup_time_to time without time zone
);


--
-- Name: tours; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tours (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    supplier_id uuid,
    title character varying(300) NOT NULL,
    slug character varying(300) NOT NULL,
    description text,
    category public.tour_category NOT NULL,
    region public.tour_region NOT NULL,
    duration public.tour_duration NOT NULL,
    duration_hours numeric(4,1),
    adult_price_cents integer NOT NULL,
    child_price_cents integer NOT NULL,
    infant_price_cents integer DEFAULT 0 NOT NULL,
    min_pax smallint DEFAULT 1 NOT NULL,
    max_pax smallint DEFAULT 20 NOT NULL,
    includes text[],
    excludes text[],
    important_notes text[],
    cover_image_url character varying(500),
    gallery_urls text[],
    status public.tour_status DEFAULT 'ACTIVE'::public.tour_status NOT NULL,
    created_by_id uuid,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    availability_mode character varying(20) DEFAULT 'always'::character varying NOT NULL,
    theme character varying(30),
    CONSTRAINT chk_tour_avail_mode CHECK (((availability_mode)::text = ANY ((ARRAY['always'::character varying, 'on_request'::character varying])::text[]))),
    CONSTRAINT chk_tour_theme CHECK (((theme)::text = ANY ((ARRAY['NATURE'::character varying, 'ADVENTURE'::character varying, 'CULTURAL'::character varying, 'SEA_ACTIVITIES'::character varying, 'BEACH'::character varying])::text[]))),
    CONSTRAINT tours_adult_price_cents_check CHECK ((adult_price_cents >= 0)),
    CONSTRAINT tours_child_price_cents_check CHECK ((child_price_cents >= 0))
);


--
-- Name: transfer_pricing_tiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transfer_pricing_tiers (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    label character varying(200) NOT NULL,
    min_km integer NOT NULL,
    max_km integer,
    price_cents integer NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    sort_order smallint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    includes text[] DEFAULT '{}'::text[] NOT NULL,
    excludes text[] DEFAULT '{}'::text[] NOT NULL,
    CONSTRAINT transfer_pricing_tiers_min_km_check CHECK ((min_km >= 0)),
    CONSTRAINT transfer_pricing_tiers_price_cents_check CHECK ((price_cents >= 0))
);


--
-- Name: transfer_rates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transfer_rates (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    route_id uuid NOT NULL,
    rate_name character varying(50) NOT NULL,
    price_cents integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT transfer_rates_price_cents_check CHECK ((price_cents >= 0))
);


--
-- Name: transfer_routes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transfer_routes (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    from_location character varying(300) NOT NULL,
    to_location character varying(300) NOT NULL,
    trip_type public.transfer_trip_type DEFAULT 'ONE_WAY'::public.transfer_trip_type NOT NULL,
    car_category public.car_category NOT NULL,
    base_price_cents integer NOT NULL,
    est_duration_mins smallint,
    est_km smallint,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT transfer_routes_base_price_cents_check CHECK ((base_price_cents >= 0))
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    email character varying(254) NOT NULL,
    password_hash character varying(255) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    phone character varying(30),
    whatsapp character varying(30),
    role public.user_role DEFAULT 'GUEST'::public.user_role NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    verification_token_hash text,
    verification_token_expires_at timestamp with time zone
);


--
-- Name: audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq'::regclass);


--
-- Data for Name: activity_variants; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.activity_variants (id, booking_item_id, name, pax_adults, pax_children, pax_infants, unit_price_cents, subtotal_cents, created_at) FROM stdin;
\.


--
-- Data for Name: agents; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.agents (id, user_id, company_name, country, city, address, business_type, tier, status, markup_percent, commission_rate, credit_limit, total_bookings, business_proof_url, approved_at, approved_by_id, created_at, updated_at, deleted_at, verification_token_hash, verification_sent_at, rejection_reason, suspension_reason) FROM stdin;
20000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000005	Sunrise Travel Agency	Mauritius	Port Louis	12 Royal Road, Port Louis	TRAVEL_AGENCY	GOLD	ACTIVE	10.00	12.00	500000	47	\N	2026-01-27 07:18:19.663585+00	00000000-0000-0000-0000-000000000001	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N	\N
b4bb00d5-cf7d-4582-a6f7-56666d311d94	25c70058-b889-4132-a38e-67de5bd60db1	Ambiance 	Mauritius	\N	XYZ	TRAVEL_AGENCY	BRONZE	ACTIVE	0.00	10.00	0	0	\N	2026-04-27 07:24:28.692002+00	00000000-0000-0000-0000-000000000001	2026-04-27 07:22:44.924376+00	2026-04-27 07:24:28.694783+00	\N	\N	\N	\N	\N
0f73f5cb-efde-48ff-9527-2ac064593e10	2fec982b-1835-47d2-a843-03110112af50	Ambiance	India	\N	Pakistan	TRAVEL_AGENCY	BRONZE	ACTIVE	0.00	10.00	0	0	\N	2026-04-27 12:31:38.359077+00	00000000-0000-0000-0000-000000000001	2026-04-27 12:30:02.881623+00	2026-04-27 12:31:38.362059+00	\N	\N	\N	\N	\N
00e35f95-9490-4d21-898e-3a5ce97c8c73	69afc204-0bdc-4dff-a68b-7534bbdf70c6	Ambiance	Mauritius	\N	Ambaince	TRAVEL_AGENCY	BRONZE	ACTIVE	0.00	10.00	0	0	\N	2026-04-30 08:33:07.574533+00	00000000-0000-0000-0000-000000000001	2026-04-30 08:03:20.922358+00	2026-04-30 08:33:07.576293+00	\N	\N	\N	\N	\N
5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	c4d61168-bb5d-482e-a556-c63afda6103b	Ambiance	Mauritius	\N	sacsac	TRAVEL_AGENCY	BRONZE	ACTIVE	0.00	10.00	0	0	\N	2026-04-30 09:15:33.112328+00	00000000-0000-0000-0000-000000000001	2026-04-30 08:41:50.903068+00	2026-04-30 09:15:33.113758+00	\N	\N	\N	\N	\N
\.


--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.audit_logs (id, action, actor_id, actor_email, entity_type, entity_id, before_state, after_state, ip_address, user_agent, created_at) FROM stdin;
\.


--
-- Data for Name: booking_extras; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.booking_extras (id, booking_item_id, label, quantity, unit_price_cents, total_cents) FROM stdin;
13c1357c-3ae5-42df-9ecc-fcce528436f0	7ddf92e4-1bd8-4a89-9353-73451d430039	Wifi	1	100	100
34775cff-1466-466c-b5bf-ead6428ce8e3	7ddf92e4-1bd8-4a89-9353-73451d430039	Baby Sit	1	200	200
94ce5c7f-9913-4dfb-9bd5-05ce92af0154	7ddf92e4-1bd8-4a89-9353-73451d430039	Driver	1	300	300
b7855f38-f675-48d6-b678-fd9a8e1d6f97	a1a78a62-b8bf-4ec9-850e-03bd4a0f7a1f	Wifi	1	100	100
6044036f-fbe9-40d2-818d-237cb488ce17	a1a78a62-b8bf-4ec9-850e-03bd4a0f7a1f	Baby Sit	1	200	200
fb5d9f40-bffc-4c36-9caa-a3960ee50574	a1a78a62-b8bf-4ec9-850e-03bd4a0f7a1f	Driver	1	300	300
a5ba7a5b-3461-4f45-827b-6adff9caf3bb	682f2128-9167-40a4-9b9a-c9de52f1b115	Wifi	1	100	100
0d65ac05-83fa-4124-8098-600b59471b57	682f2128-9167-40a4-9b9a-c9de52f1b115	Baby Sit	1	200	200
169beb85-9ab9-4af1-8e52-9a9f789e361b	682f2128-9167-40a4-9b9a-c9de52f1b115	Driver	1	300	300
9b0b9756-1069-425b-aa6d-ff1d41b2b926	202bb01b-e186-4359-aec4-8e3bb03f03bb	Wifi	1	100	100
c7e21efd-66e9-491b-9d64-67044f2685e5	202bb01b-e186-4359-aec4-8e3bb03f03bb	Baby Sit	1	200	200
29f5a7d2-c8b3-4353-984d-a04409b11e7a	202bb01b-e186-4359-aec4-8e3bb03f03bb	Driver	1	300	300
ded13679-b854-42cd-b6c6-d3bcf1a0cf70	72e29117-d0b2-46db-9342-cb32a3fafdf1	Wifi	1	100	100
8be8a7dc-5cd8-4b70-8bde-9c8f2fabbabb	72e29117-d0b2-46db-9342-cb32a3fafdf1	Baby Sit	1	200	200
5ef4606b-287e-4745-87af-213aea939fcd	72e29117-d0b2-46db-9342-cb32a3fafdf1	Driver	1	300	300
50781fe9-2a17-4bac-899b-8a15ef72b424	45859c3f-45ab-47d3-9936-118fcf07410d	Wifi	1	100	100
6a916385-ae57-4e9b-bce5-0b734038be01	45859c3f-45ab-47d3-9936-118fcf07410d	Baby Sit	1	200	200
78c52427-8aab-4936-9627-01bb29e550ca	45859c3f-45ab-47d3-9936-118fcf07410d	Driver	1	300	300
\.


--
-- Data for Name: booking_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.booking_items (id, booking_id, item_type, ref_id, quantity, unit_price_cents, total_cents, service_date, start_at, end_at, pickup_location, dropoff_location, pax_adults, pax_children, pax_infants, notes, trip_type, rental_days, created_at, instance_index) FROM stdin;
c1000000-0000-0000-0000-000000000001	b0000000-0000-0000-0000-000000000001	CAR_RENTAL	a0e7382c-0e02-47be-9c96-08b9ea0a466b	1	220000	440000	2026-05-10	\N	\N	\N	\N	2	0	0	\N	\N	2	2026-04-27 07:18:19.663585+00	1
c1000000-0000-0000-0000-000000000002	b0000000-0000-0000-0000-000000000002	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	1120000	1120000	2026-05-15	\N	\N	\N	\N	3	1	0	\N	\N	\N	2026-04-27 07:18:19.663585+00	1
c1000000-0000-0000-0000-000000000003	b0000000-0000-0000-0000-000000000003	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	880000	880000	2026-06-01	\N	\N	\N	\N	4	0	0	\N	\N	\N	2026-04-27 07:18:19.663585+00	1
c1000000-0000-0000-0000-000000000004	b0000000-0000-0000-0000-000000000004	TOUR	22d175ec-6bb9-45ae-9559-44b8cf1839c2	1	640000	640000	2026-04-20	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-27 07:18:19.663585+00	1
e37a9a1e-04ab-49d9-bb4b-020ac4ee91b2	b610e654-c8cf-4bc1-a7e9-cafd151a1f4f	CAR_RENTAL	461eb95a-0583-4d19-85e9-9237d0fcdab0	1	140000	140000	2026-04-27	\N	\N	airport	\N	1	0	0	\N	\N	1	2026-04-27 08:43:57.21831+00	1
5b7301cc-33e3-445d-ad65-413ddd2efd8c	66388ea1-07bf-4491-be43-4ff8a9c984db	CAR_TRANSFER	f0e98cfd-a3d0-44ba-a690-34b930544935	1	10000	10000	2026-04-27	\N	\N	Sonipat	Vatika	1	0	0	\N	\N	\N	2026-04-27 12:20:16.031717+00	1
1c734d9f-b77e-4b8c-ac1d-d9f43cecfc63	3526d195-18e0-4eea-81a5-e0e23c6f85c5	CAR_TRANSFER	f0e98cfd-a3d0-44ba-a690-34b930544935	1	10000	10000	2026-04-27	\N	\N	Vatika	Sonipat	1	0	0	\N	\N	\N	2026-04-27 13:19:41.027636+00	1
94df4e6b-ebf7-4b94-94b1-6d47c02a8b37	c3721856-acd0-4372-83f9-f56df26fdea1	CAR_RENTAL	79858582-5a9b-4a5d-a825-c9015e1d9368	1	760000	760000	2026-04-28	\N	\N	Sonipat	Panipat	1	0	0	\N	\N	2	2026-04-28 05:08:26.259718+00	1
1aaab139-8214-416a-a1df-1b3804c0f9cb	1ba0aa87-9c56-48fb-aacc-b2d515646e26	CAR_RENTAL	79858582-5a9b-4a5d-a825-c9015e1d9368	1	380000	380000	2026-04-28	\N	\N	Army Service Corps - ASC North	ACS Meghana and Shalini Apartments	1	0	0	\N	\N	1	2026-04-28 06:29:14.461426+00	1
3e2a3f10-fc12-4e79-9ab1-672321469fe9	e1b3b23b-58c4-491f-b87d-d38267946c0f	CAR_RENTAL	79858582-5a9b-4a5d-a825-c9015e1d9368	1	380000	380000	2026-04-28	\N	\N	Sad	Akshar Ads	1	0	0	\N	\N	1	2026-04-28 07:22:18.58357+00	1
1074ac4c-af5f-40a0-89aa-99cce3a002bc	b3eb1cc4-8240-4ac8-821c-2ad3c3c1d8db	CAR_TRANSFER	f0e98cfd-a3d0-44ba-a690-34b930544935	1	10000	10000	2026-04-28	\N	\N	Sonipat	Gurgaon	1	0	0	\N	\N	\N	2026-04-28 07:24:57.632776+00	1
0201911c-b78e-4fcf-a7a1-93391db65735	56e16be4-06a3-499a-8425-252c2a283f56	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-04-28	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-28 09:22:01.601095+00	1
098e99de-ee91-43f6-bc98-f7db1f9897a7	56e16be4-06a3-499a-8425-252c2a283f56	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-04-28	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-28 09:22:01.60111+00	1
dab4e154-79e0-4ec9-a4a4-e48df666f510	455ccb3f-85cf-49db-927f-7aae7ee1871e	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	440000	440000	2026-12-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-28 10:02:23.802058+00	1
71a22dc4-025b-4566-84e9-f3a013686ffe	3faf03cf-8110-4f47-81da-996e0ab976ea	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	440000	440000	2026-12-20	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-28 10:04:09.773866+00	1
57bd4f4d-bdab-461a-9306-f9e39eab0b47	da5300e1-54e7-48d6-9b5c-ae2e67cc9a59	DAY_TRIP	50000000-0000-0000-0000-000000000002	1	220000	220000	2026-12-22	\N	\N	\N	\N	1	0	0	\N	\N	\N	2026-04-28 10:54:22.738724+00	1
8ffeedc3-db38-47b1-ace8-ff1bbe7711b5	4aae022d-8199-48ac-b1f4-e9a3cc549163	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	440000	440000	2026-12-29	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-28 10:58:55.721284+00	1
fb1b6acc-2eb4-4928-bcc7-31bc47adf920	1cb504e7-50f4-49ce-9b08-dbabff644c93	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	45000	45000	2027-01-10	\N	\N	\N	\N	2	1	0	\N	\N	\N	2026-04-28 11:15:40.38765+00	1
1922a66b-0868-472b-8c65-eb00c363cfbf	fe35748e-6c80-4568-b58d-2221fd1df82b	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	18000	18000	2027-02-14	\N	\N	\N	\N	1	0	0	\N	\N	\N	2026-04-28 12:16:00.74629+00	1
956ea976-8dcc-4fdc-9a2d-ac2a12d12286	56a43100-47e7-4004-807b-afab815600bc	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2027-03-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-29 08:05:46.262255+00	1
41a12fb5-f801-48a7-bf1c-a86cdb0a38fc	db9a785a-8461-4afe-a89d-d291a1a65627	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	280000	280000	2026-06-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-30 06:47:21.692599+00	1
51e90a74-7cd1-4517-8f98-610332893177	616c004f-9098-4591-a17c-bda59f9c6acd	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	280000	280000	2026-07-15	\N	\N	\N	\N	1	0	0	\N	\N	\N	2026-04-30 06:48:30.967453+00	1
d24d75c1-74fe-4fce-9a06-085e94494fa8	f2e62ccd-53f6-4cd6-b19f-0fe30968541f	CAR_RENTAL	461eb95a-0583-4d19-85e9-9237d0fcdab0	1	420000	420000	2026-06-15	\N	\N	Airport	\N	5	0	0	\N	\N	3	2026-04-30 09:23:45.250155+00	1
62fccec9-3bf3-478a-9a46-e4c4db19eeaf	e5fa9cff-7c94-4b30-9049-9c1299450ff5	CAR_TRANSFER	79858582-5a9b-4a5d-a825-c9015e1d9368	1	14000	14000	2026-06-15	\N	\N	Airport	Grand Baie	2	0	0	\N	\N	\N	2026-04-30 09:55:43.520384+00	1
ad27e72a-ca8d-4c51-a3e3-192fac89c632	ecfa530f-3392-4082-ba07-f58c60ad1b8e	CAR_TRANSFER	f0e98cfd-a3d0-44ba-a690-34b930544935	1	14000	14000	2026-06-15	\N	\N	Airport	Grand Baie	2	0	0	\N	\N	\N	2026-04-30 10:17:03.583095+00	1
75c096a1-fe84-464e-83ba-9c6544943f0e	ab0e6418-d013-4280-ae70-e55560915e36	CAR_TRANSFER	a5fd8156-965a-42a6-b685-d493fa720dba	1	12000	12000	2026-06-15	\N	\N	Airport	Grand Baie	2	0	0	\N	\N	\N	2026-04-30 10:26:27.312698+00	1
d812098f-39d9-4676-b251-e98b564d32e9	83778b5a-1a57-4ffe-9673-114d966dd6d6	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-30 12:00:28.744925+00	1
065461a3-f2c6-4d59-85aa-f115cb265635	bf2f6b76-515f-4311-ab00-c812efeba212	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-30 12:00:40.730842+00	1
7ddf92e4-1bd8-4a89-9353-73451d430039	9e24952f-c3dc-4c91-93e0-78bbd29b5161	CAR_RENTAL	a5fd8156-965a-42a6-b685-d493fa720dba	1	1600	1600	2026-04-30	\N	\N	TBD	\N	18	0	0	\N	\N	1	2026-04-30 12:03:14.769881+00	1
d0f9b992-3a57-4ee6-a4e4-871b05b3298f	9e24952f-c3dc-4c91-93e0-78bbd29b5161	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	36000	36000	2026-04-30	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-04-30 12:03:14.769943+00	1
6c6ce8bf-179b-4033-a625-1b9d1646fbf1	3f1d7a53-63ab-4faf-9351-f07ec60cc1a3	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:26:43.37652+00	1
0e94edc9-6052-45a9-96a3-d5d2bbdbd067	09b810f6-a8b3-4da5-aa96-b5437f9b098d	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:39:42.600041+00	1
a1a78a62-b8bf-4ec9-850e-03bd4a0f7a1f	25fa813e-e5fa-4b56-9084-b39ad6f91d3c	CAR_RENTAL	a5fd8156-965a-42a6-b685-d493fa720dba	1	1600	1600	2026-05-01	\N	\N	TBD	\N	18	0	0	\N	\N	1	2026-05-01 05:43:23.011423+00	1
d751d4cc-5c8b-40c8-9c7f-bc98e70f7edf	25fa813e-e5fa-4b56-9084-b39ad6f91d3c	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	36000	36000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:43:23.011455+00	1
154f49a5-7b18-4205-abf4-2629b2044b00	25fa813e-e5fa-4b56-9084-b39ad6f91d3c	DAY_TRIP	50000000-0000-0000-0000-000000000002	1	440000	440000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:43:23.011458+00	1
9fb1853d-dc12-4d2d-af74-0799f7f635fb	02a4fe4b-f751-4f62-b197-d237b5331740	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:47:02.033138+00	1
42e68fcd-14aa-4e94-be80-42253d72dfb4	6120ae3f-402b-4077-98bc-e7b663800ae0	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:47:14.769419+00	1
682f2128-9167-40a4-9b9a-c9de52f1b115	5e33eec6-7d30-499a-8742-f5028bcc5b7c	CAR_RENTAL	a5fd8156-965a-42a6-b685-d493fa720dba	1	1600	1600	2026-05-01	\N	\N	TBD	\N	18	0	0	\N	\N	1	2026-05-01 05:50:50.655419+00	1
f682cda6-1ff1-4347-9d8d-a7a8731e2e97	5e33eec6-7d30-499a-8742-f5028bcc5b7c	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	36000	36000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:50:50.655446+00	1
82817f77-c5a6-45f2-b7ea-751769e4a28f	5e33eec6-7d30-499a-8742-f5028bcc5b7c	DAY_TRIP	50000000-0000-0000-0000-000000000002	1	440000	440000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:50:50.655448+00	1
202bb01b-e186-4359-aec4-8e3bb03f03bb	dc0868a6-a270-4b8b-a6a9-b340af742273	CAR_RENTAL	a5fd8156-965a-42a6-b685-d493fa720dba	1	1600	1600	2026-05-01	\N	\N	TBD	\N	18	0	0	\N	\N	1	2026-05-01 05:56:19.011224+00	1
8ebe7253-31da-4b65-98bf-bbf033b355d5	dc0868a6-a270-4b8b-a6a9-b340af742273	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	36000	36000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:56:19.011253+00	1
fd78edd4-bbf6-4286-b07c-05973ad9f566	dc0868a6-a270-4b8b-a6a9-b340af742273	DAY_TRIP	50000000-0000-0000-0000-000000000002	1	440000	440000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:56:19.011255+00	1
3f3e1886-6e4a-4d83-a613-32e0874aece3	fb9ef279-f1a9-42b0-a97d-30fc9f418b37	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 05:59:14.75532+00	1
90cc8235-eae2-4d16-870b-d6dc2edfafb2	c760e252-c448-4d53-a1da-7bedbfb78749	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:12:35.584161+00	1
72e29117-d0b2-46db-9342-cb32a3fafdf1	420af25e-fe48-4f50-8fdb-bbbd9992d17e	CAR_RENTAL	a5fd8156-965a-42a6-b685-d493fa720dba	1	1600	1600	2026-05-01	\N	\N	TBD	\N	18	0	0	\N	\N	1	2026-05-01 07:22:51.362242+00	1
c3de93be-e505-445c-bda2-53d01d122500	420af25e-fe48-4f50-8fdb-bbbd9992d17e	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	36000	36000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:22:51.362292+00	1
e030832c-9200-4503-a2d5-552e00a1c1d4	420af25e-fe48-4f50-8fdb-bbbd9992d17e	DAY_TRIP	50000000-0000-0000-0000-000000000002	1	440000	440000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:22:51.362293+00	1
af7ba354-706e-4c5f-a772-a22748c91df4	762b6810-bd47-46f2-879e-8ae72a314df4	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	560000	2026-08-15	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:42:21.189283+00	1
45859c3f-45ab-47d3-9936-118fcf07410d	21447da6-93aa-4b04-a825-47495f4394cd	CAR_RENTAL	a5fd8156-965a-42a6-b685-d493fa720dba	1	1600	1600	2026-05-01	\N	\N	TBD	\N	18	0	0	\N	\N	1	2026-05-01 07:44:09.78501+00	1
d8edc191-1051-4d83-a91f-b26d9dee2356	21447da6-93aa-4b04-a825-47495f4394cd	DAY_TRIP	dbdd5536-a4a1-4eff-9c35-3c04e522de64	1	36000	36000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:44:09.785062+00	1
fd89e46d-317a-4a2e-9680-f444a77fcfe2	21447da6-93aa-4b04-a825-47495f4394cd	DAY_TRIP	50000000-0000-0000-0000-000000000002	1	440000	440000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:44:09.785065+00	1
eef38ae2-99e2-4454-940a-1619e8cdc768	020e0cd4-d44d-4911-9727-2477816be834	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:47:31.875906+00	1
67f71f48-6aa0-41eb-9810-642473904059	54349f33-b98f-4a43-accc-6b2de0b8ce6c	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 07:50:09.578516+00	1
8bd3c671-61f9-48f5-9157-7b1f50abb47d	f42d7a66-98e2-4ae3-857b-998e6c290bfe	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 08:01:33.668025+00	1
f9742652-86bc-4b7b-b2b0-210ef29ab336	efb73144-4034-46c8-bcf2-c743e34f068e	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 08:20:27.477414+00	1
87e32d88-ca2a-4a0b-ab64-73d7c78b7ecc	e50e3906-20af-4d00-9c8b-c044ce2b5b1f	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 08:23:38.130535+00	1
5024a3f7-b00a-4080-994f-266a30c619bd	570bdc3a-f681-4c24-b8ba-69cf5e495b34	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 08:25:59.127378+00	1
63812db2-ecb2-4ca5-b203-bf9deb12e5b4	6f0cdca2-17c6-4878-b567-998c754c552e	TOUR	54d55904-cf37-46d6-bae0-0dba770a6d08	2	390000	780000	2026-05-01	\N	\N	\N	\N	2	0	0	\N	\N	\N	2026-05-01 09:50:12.72961+00	1
\.


--
-- Data for Name: bookings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.bookings (id, reference, customer_id, agent_id, status, subtotal_cents, markup_cents, commission_cents, vat_cents, total_cents, vat_rate, markup_rate, commission_rate, special_requests, cancel_reason, cancelled_at, cancelled_by_id, cancellation_fee_cents, service_date, created_by_id, created_at, updated_at, deleted_at, terms_version, terms_agreed_at, terms_agreed_ip, terms_agreed_user_agent, is_enquiry, enquiry_converted_at, enquiry_declined_at, cancelled_by_type) FROM stdin;
b0000000-0000-0000-0000-000000000001	AMB-2026-0001	30000000-0000-0000-0000-000000000001	\N	CONFIRMED	440000	0	0	66000	506000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-05-10	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N	\N	f	\N	\N	\N
b0000000-0000-0000-0000-000000000002	AMB-2026-0002	30000000-0000-0000-0000-000000000002	20000000-0000-0000-0000-000000000001	CONFIRMED	1120000	112000	134400	168000	1534400	15.00	10.00	12.00	\N	\N	\N	\N	0	2026-05-15	00000000-0000-0000-0000-000000000005	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N	\N	f	\N	\N	\N
b0000000-0000-0000-0000-000000000003	AMB-2026-0003	30000000-0000-0000-0000-000000000003	\N	PENDING	880000	0	0	132000	1012000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-06-01	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N	\N	t	\N	\N	\N
b0000000-0000-0000-0000-000000000004	AMB-2026-0004	30000000-0000-0000-0000-000000000001	\N	CANCELLED	640000	0	0	96000	736000	15.00	0.00	0.00	\N	Change of travel plans	2026-04-22 07:18:19.663585+00	\N	0	2026-04-20	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N	\N	f	\N	\N	CUSTOMER
b610e654-c8cf-4bc1-a7e9-cafd151a1f4f	AMB-088931	d8728616-7c0f-47f0-9e05-8b0e2b9f89dd	\N	CONFIRMED	140000	0	0	21000	161000	15.00	0.00	0.00	asfsafasfasf	\N	\N	\N	0	2026-04-27	\N	2026-04-27 08:43:57.215918+00	2026-04-27 08:44:37.496993+00	\N	\N	\N	\N	\N	f	\N	\N	\N
66388ea1-07bf-4491-be43-4ff8a9c984db	AMB-027261	d8728616-7c0f-47f0-9e05-8b0e2b9f89dd	\N	PENDING	10000	0	0	1500	11500	15.00	0.00	0.00	saasfasfasf	\N	\N	\N	0	2026-04-27	\N	2026-04-27 12:20:16.025153+00	2026-04-27 12:20:16.025153+00	\N	\N	\N	\N	\N	f	\N	\N	\N
3526d195-18e0-4eea-81a5-e0e23c6f85c5	AMB-033444	c45879e5-2f3f-4b61-a7b7-efcd21607e9b	\N	PENDING	10000	0	0	1500	11500	15.00	0.00	0.00	asdasdasd	\N	\N	\N	0	2026-04-27	\N	2026-04-27 13:19:41.023219+00	2026-04-27 13:19:41.023219+00	\N	\N	\N	\N	\N	f	\N	\N	\N
c3721856-acd0-4372-83f9-f56df26fdea1	AMB-792440	c45879e5-2f3f-4b61-a7b7-efcd21607e9b	\N	PENDING	760000	0	0	114000	874000	15.00	0.00	0.00	csaacs	\N	\N	\N	0	2026-04-28	\N	2026-04-28 05:08:26.25606+00	2026-04-28 05:08:26.25606+00	\N	\N	\N	\N	\N	f	\N	\N	\N
1ba0aa87-9c56-48fb-aacc-b2d515646e26	AMB-847671	c45879e5-2f3f-4b61-a7b7-efcd21607e9b	0f73f5cb-efde-48ff-9527-2ac064593e10	CONFIRMED	380000	0	38000	57000	437000	15.00	0.00	10.00	21	\N	\N	\N	0	2026-04-28	2fec982b-1835-47d2-a843-03110112af50	2026-04-28 06:29:14.458934+00	2026-04-28 06:31:02.884011+00	\N	\N	\N	\N	\N	f	\N	\N	\N
e1b3b23b-58c4-491f-b87d-d38267946c0f	AMB-681132	c45879e5-2f3f-4b61-a7b7-efcd21607e9b	\N	PENDING	380000	0	0	57000	437000	15.00	0.00	0.00	dsfdsfdsf	\N	\N	\N	0	2026-04-28	\N	2026-04-28 07:22:18.579713+00	2026-04-28 07:22:18.579713+00	\N	\N	\N	\N	\N	f	\N	\N	\N
b3eb1cc4-8240-4ac8-821c-2ad3c3c1d8db	AMB-537274	c45879e5-2f3f-4b61-a7b7-efcd21607e9b	0f73f5cb-efde-48ff-9527-2ac064593e10	CONFIRMED	10000	0	1000	1500	11500	15.00	0.00	10.00	234	\N	\N	\N	0	2026-04-28	2fec982b-1835-47d2-a843-03110112af50	2026-04-28 07:24:57.630595+00	2026-04-28 07:25:58.522227+00	\N	\N	\N	\N	\N	f	\N	\N	\N
56e16be4-06a3-499a-8425-252c2a283f56	AMB-197409	c45879e5-2f3f-4b61-a7b7-efcd21607e9b	\N	PENDING	1560000	0	0	234000	1794000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-04-28	\N	2026-04-28 09:22:01.598878+00	2026-04-28 09:22:01.598879+00	\N	\N	\N	\N	\N	f	\N	\N	\N
455ccb3f-85cf-49db-927f-7aae7ee1871e	AMB-828097	a272400f-f27e-48ce-a7f8-6ade23334ff4	20000000-0000-0000-0000-000000000001	PENDING	440000	44000	52800	66000	550000	15.00	10.00	12.00	Test booking for E2E sync	\N	\N	\N	0	2026-12-15	00000000-0000-0000-0000-000000000005	2026-04-28 10:02:23.799678+00	2026-04-28 10:02:23.799679+00	\N	\N	\N	\N	\N	f	\N	\N	\N
3faf03cf-8110-4f47-81da-996e0ab976ea	AMB-672580	8434c5a8-3f85-4021-921d-5f997efe23d9	\N	PENDING	440000	0	0	66000	506000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-12-20	\N	2026-04-28 10:04:09.771575+00	2026-04-28 10:04:09.771575+00	\N	\N	\N	\N	\N	f	\N	\N	\N
da5300e1-54e7-48d6-9b5c-ae2e67cc9a59	AMB-763015	5eea0a07-00aa-499e-bc50-8e3ec97f2653	\N	PENDING	220000	0	0	33000	253000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-12-22	\N	2026-04-28 10:54:22.733694+00	2026-04-28 10:54:22.733694+00	\N	\N	\N	\N	\N	f	\N	\N	\N
4aae022d-8199-48ac-b1f4-e9a3cc549163	AMB-897834	5eea0a07-00aa-499e-bc50-8e3ec97f2653	20000000-0000-0000-0000-000000000001	PENDING	440000	44000	52800	66000	550000	15.00	10.00	12.00	\N	\N	\N	\N	0	2026-12-29	00000000-0000-0000-0000-000000000005	2026-04-28 10:58:55.716606+00	2026-04-28 10:58:55.716606+00	\N	\N	\N	\N	\N	f	\N	\N	\N
1cb504e7-50f4-49ce-9b08-dbabff644c93	AMB-120259	e5d2d508-99bc-495b-9094-0dc038039a0a	20000000-0000-0000-0000-000000000001	PENDING	45000	4500	5400	6750	56250	15.00	10.00	12.00	\N	\N	\N	\N	0	2027-01-10	00000000-0000-0000-0000-000000000005	2026-04-28 11:15:40.385668+00	2026-04-28 11:15:40.385668+00	\N	\N	\N	\N	\N	f	\N	\N	\N
fe35748e-6c80-4568-b58d-2221fd1df82b	AMB-267261	5eea0a07-00aa-499e-bc50-8e3ec97f2653	20000000-0000-0000-0000-000000000001	PENDING	18000	1800	2160	2700	22500	15.00	10.00	12.00	\N	\N	\N	\N	0	2027-02-14	00000000-0000-0000-0000-000000000005	2026-04-28 12:16:00.744965+00	2026-04-28 12:16:00.744965+00	\N	\N	\N	\N	\N	f	\N	\N	\N
56a43100-47e7-4004-807b-afab815600bc	AMB-491525	a7209ea9-a80f-4ddd-a60b-53746b16f31b	\N	CONFIRMED	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2027-03-15	\N	2026-04-29 08:05:46.258674+00	2026-04-29 08:09:42.634662+00	\N	\N	\N	\N	\N	f	\N	\N	\N
db9a785a-8461-4afe-a89d-d291a1a65627	AMB-857519	ac7e934e-9a12-4125-981e-45b3135b8d7d	\N	CONFIRMED	280000	0	0	42000	322000	15.00	0.00	0.00	none	\N	\N	\N	0	2026-06-15	\N	2026-04-30 06:47:21.690584+00	2026-04-30 06:48:10.436729+00	\N	\N	\N	\N	\N	f	\N	\N	\N
616c004f-9098-4591-a17c-bda59f9c6acd	AMB-802295	114bedec-7423-4cde-b979-af5097c08896	\N	PENDING	280000	0	0	42000	322000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-07-15	\N	2026-04-30 06:48:30.96582+00	2026-04-30 06:48:30.96582+00	\N	\N	\N	\N	\N	f	\N	\N	\N
f2e62ccd-53f6-4cd6-b19f-0fe30968541f	AMB-965967	15ccf9b3-a651-410d-976b-6e673a4f2b50	\N	PENDING	420000	0	0	63000	483000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-06-15	\N	2026-04-30 09:23:45.24811+00	2026-04-30 09:23:45.24811+00	\N	\N	\N	\N	\N	f	\N	\N	\N
e5fa9cff-7c94-4b30-9049-9c1299450ff5	AMB-004323	099ebf26-9acc-4d53-a7ed-5d49bd783086	\N	PENDING	14000	0	0	2100	16100	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-06-15	\N	2026-04-30 09:55:43.516508+00	2026-04-30 09:55:43.516508+00	\N	\N	\N	\N	\N	f	\N	\N	\N
ecfa530f-3392-4082-ba07-f58c60ad1b8e	AMB-520343	b98c06ac-4670-4447-89fc-6d15cb553f69	\N	PENDING	14000	0	0	2100	16100	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-06-15	\N	2026-04-30 10:17:03.579085+00	2026-04-30 10:17:03.579086+00	\N	\N	\N	\N	\N	f	\N	\N	\N
ab0e6418-d013-4280-ae70-e55560915e36	AMB-861342	3a480513-9de5-4f28-a772-10fd9cf3ad92	\N	PENDING	12000	0	0	1800	13800	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-06-15	\N	2026-04-30 10:26:27.307631+00	2026-04-30 10:26:27.307632+00	\N	\N	\N	\N	\N	f	\N	\N	\N
83778b5a-1a57-4ffe-9673-114d966dd6d6	AMB-660619	feee6b2d-be82-40fa-a46c-133704cd4212	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-04-30 12:00:28.739765+00	2026-04-30 12:00:28.739765+00	\N	\N	\N	\N	\N	f	\N	\N	\N
bf2f6b76-515f-4311-ab00-c812efeba212	AMB-392834	feee6b2d-be82-40fa-a46c-133704cd4212	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-04-30 12:00:40.729163+00	2026-04-30 12:00:40.729165+00	\N	\N	\N	\N	\N	f	\N	\N	\N
9e24952f-c3dc-4c91-93e0-78bbd29b5161	AMB-736475	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	37600	0	3760	5640	43240	15.00	0.00	10.00	\ncghj	\N	\N	\N	0	2026-04-30	c4d61168-bb5d-482e-a556-c63afda6103b	2026-04-30 12:03:14.767059+00	2026-04-30 12:03:14.767059+00	\N	\N	\N	\N	\N	f	\N	\N	\N
3f1d7a53-63ab-4faf-9351-f07ec60cc1a3	AMB-904392	1a93f960-138b-4615-9039-3ceb84756550	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 05:26:43.371546+00	2026-05-01 05:26:43.371546+00	\N	\N	\N	\N	\N	f	\N	\N	\N
09b810f6-a8b3-4da5-aa96-b5437f9b098d	AMB-566367	1a93f960-138b-4615-9039-3ceb84756550	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 05:39:42.595964+00	2026-05-01 05:39:42.595965+00	\N	\N	\N	\N	\N	f	\N	\N	\N
25fa813e-e5fa-4b56-9084-b39ad6f91d3c	AMB-121427	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	477600	0	47760	71640	549240	15.00	0.00	10.00	123	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 05:43:23.009763+00	2026-05-01 05:43:23.009763+00	\N	\N	\N	\N	\N	f	\N	\N	\N
02a4fe4b-f751-4f62-b197-d237b5331740	AMB-436161	1a93f960-138b-4615-9039-3ceb84756550	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 05:47:02.03131+00	2026-05-01 05:47:02.03131+00	\N	\N	\N	\N	\N	f	\N	\N	\N
6120ae3f-402b-4077-98bc-e7b663800ae0	AMB-935811	1a93f960-138b-4615-9039-3ceb84756550	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 05:47:14.767466+00	2026-05-01 05:47:14.767467+00	\N	\N	\N	\N	\N	f	\N	\N	\N
5e33eec6-7d30-499a-8742-f5028bcc5b7c	AMB-432585	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	477600	0	47760	71640	549240	15.00	0.00	10.00	asfasf	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 05:50:50.65272+00	2026-05-01 05:50:50.65272+00	\N	\N	\N	\N	\N	f	\N	\N	\N
dc0868a6-a270-4b8b-a6a9-b340af742273	AMB-208494	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	477600	0	47760	71640	549240	15.00	0.00	10.00	fbfb	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 05:56:19.007823+00	2026-05-01 05:56:19.007823+00	\N	\N	\N	\N	\N	f	\N	\N	\N
fb9ef279-f1a9-42b0-a97d-30fc9f418b37	AMB-448707	ebaa3986-76fc-4b30-8410-a5a1948af801	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 05:59:14.75004+00	2026-05-01 05:59:14.75004+00	\N	\N	\N	\N	\N	f	\N	\N	\N
c760e252-c448-4d53-a1da-7bedbfb78749	AMB-629694	ebaa3986-76fc-4b30-8410-a5a1948af801	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 07:12:35.583149+00	2026-05-01 07:12:35.58315+00	\N	\N	\N	\N	\N	f	\N	\N	\N
420af25e-fe48-4f50-8fdb-bbbd9992d17e	AMB-274224	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	477600	0	47760	71640	549240	15.00	0.00	10.00	qwrqwrqwrqwrq	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 07:22:51.360705+00	2026-05-01 07:22:51.360705+00	\N	\N	\N	\N	\N	f	\N	\N	\N
762b6810-bd47-46f2-879e-8ae72a314df4	AMB-073825	ebaa3986-76fc-4b30-8410-a5a1948af801	\N	PENDING	560000	0	0	84000	644000	15.00	0.00	0.00	\N	\N	\N	\N	0	2026-08-15	\N	2026-05-01 07:42:21.186917+00	2026-05-01 07:42:21.186917+00	\N	\N	\N	\N	\N	f	\N	\N	\N
21447da6-93aa-4b04-a825-47495f4394cd	AMB-629916	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	477600	0	47760	71640	549240	15.00	0.00	10.00	423	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 07:44:09.782611+00	2026-05-01 07:44:09.782611+00	\N	\N	\N	\N	\N	f	\N	\N	\N
020e0cd4-d44d-4911-9727-2477816be834	AMB-879225	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	780000	0	78000	117000	897000	15.00	0.00	10.00	24	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 07:47:31.872948+00	2026-05-01 07:47:31.872948+00	\N	\N	\N	\N	\N	f	\N	\N	\N
54349f33-b98f-4a43-accc-6b2de0b8ce6c	AMB-911644	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	780000	0	78000	117000	897000	15.00	0.00	10.00	adssadasd	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 07:50:09.574733+00	2026-05-01 07:50:09.574733+00	\N	\N	\N	\N	\N	f	\N	\N	\N
f42d7a66-98e2-4ae3-857b-998e6c290bfe	AMB-899924	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	PENDING	780000	0	78000	117000	897000	15.00	0.00	10.00	aaaaa	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 08:01:33.664069+00	2026-05-01 08:01:33.664069+00	\N	\N	\N	\N	\N	f	\N	\N	\N
efb73144-4034-46c8-bcf2-c743e34f068e	AMB-085561	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	CONFIRMED	780000	0	78000	117000	897000	15.00	0.00	10.00	qwertyuiopkjhvc.	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 08:20:27.473888+00	2026-05-01 08:21:19.1597+00	\N	\N	\N	\N	\N	f	\N	\N	\N
e50e3906-20af-4d00-9c8b-c044ce2b5b1f	AMB-814724	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	CONFIRMED	780000	0	78000	117000	897000	15.00	0.00	10.00	12112	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 08:23:38.127506+00	2026-05-01 08:25:17.780037+00	\N	\N	\N	\N	\N	f	\N	\N	\N
570bdc3a-f681-4c24-b8ba-69cf5e495b34	AMB-553524	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	CONFIRMED	780000	0	78000	117000	897000	15.00	0.00	10.00	wefefewfewfewf	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 08:25:59.125645+00	2026-05-01 08:26:30.725873+00	\N	\N	\N	\N	\N	f	\N	\N	\N
6f0cdca2-17c6-4878-b567-998c754c552e	AMB-205865	deb5aa04-45a0-473d-833b-2cf83a4a60bd	5d230cb3-3905-41fc-aa2f-f3ba0ba7c710	CONFIRMED	780000	0	78000	117000	897000	15.00	0.00	10.00	\N	\N	\N	\N	0	2026-05-01	c4d61168-bb5d-482e-a556-c63afda6103b	2026-05-01 09:50:12.727417+00	2026-05-01 09:51:06.862493+00	\N	\N	\N	\N	\N	f	\N	\N	\N
\.


--
-- Data for Name: car_availability; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.car_availability (id, car_id, date_from, date_to, reason, booking_id, created_at) FROM stdin;
\.


--
-- Data for Name: car_extra_services; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.car_extra_services (id, car_id, name, price_cents, display_order, created_at) FROM stdin;
8e40cb0d-06e5-4fab-828f-4cca1ef7d3da	22fe7d16-72fe-44e5-ab9a-46d8d67bf101	Baby Seat	1500	0	2026-04-27 07:18:19.663585+00
ff5fa55e-3160-4fc4-9cac-2be3266d2737	1ccd5fd5-8193-44fd-afc4-9732292f3bfb	Baby Seat	1500	0	2026-04-27 07:18:19.663585+00
5af46e7d-cc0c-4f84-9cfc-2b60eb73897e	a0e7382c-0e02-47be-9c96-08b9ea0a466b	Baby Seat	1500	0	2026-04-27 07:18:19.663585+00
57b3b597-dbb9-4847-97d5-e1ddf4d85809	461eb95a-0583-4d19-85e9-9237d0fcdab0	Baby Seat	1500	0	2026-04-27 07:18:19.663585+00
fb23f88c-8d08-4c1a-a5ca-dc3d4bce07df	bd6e2cfb-e93b-42dc-b311-b4aee6edf92c	Baby Seat	1500	0	2026-04-27 07:18:19.663585+00
ffce7056-d7bb-468b-91c3-2c2820112900	79858582-5a9b-4a5d-a825-c9015e1d9368	Additional Driver	2500	1	2026-04-27 07:18:19.663585+00
a7592946-e604-4b4f-a564-032541511c14	a0e7382c-0e02-47be-9c96-08b9ea0a466b	Additional Driver	2500	1	2026-04-27 07:18:19.663585+00
76d7b9ae-ee8b-43f3-bdaf-7d1b40345030	461eb95a-0583-4d19-85e9-9237d0fcdab0	Additional Driver	2500	1	2026-04-27 07:18:19.663585+00
45cfeaa8-f7e0-4249-84aa-f029cac03cdb	22fe7d16-72fe-44e5-ab9a-46d8d67bf101	WiFi Dongle	800	2	2026-04-27 07:18:19.663585+00
36348dec-5d60-4138-81eb-30d7b64f25a3	1ccd5fd5-8193-44fd-afc4-9732292f3bfb	WiFi Dongle	800	2	2026-04-27 07:18:19.663585+00
2fe77365-f629-437c-b47b-86249b8be6fa	79858582-5a9b-4a5d-a825-c9015e1d9368	WiFi Dongle	800	2	2026-04-27 07:18:19.663585+00
\.


--
-- Data for Name: car_rates; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.car_rates (id, car_id, period, amount_cents, km_from, km_to, valid_from, valid_to, created_at, updated_at) FROM stdin;
abe29638-1479-47d6-b404-751a0d17f8e9	bd6e2cfb-e93b-42dc-b311-b4aee6edf92c	DAILY	580000	\N	\N	\N	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
41f6df75-6f24-4322-8ebf-3a73f2c20586	79858582-5a9b-4a5d-a825-c9015e1d9368	DAILY	380000	\N	\N	\N	\N	2026-04-28 10:13:23.612532+00	2026-04-28 10:13:23.612532+00
332da2e7-1168-47e9-8c6c-a722b41c9a0b	79858582-5a9b-4a5d-a825-c9015e1d9368	WEEKLY	1200	\N	\N	\N	\N	2026-04-28 10:13:23.613922+00	2026-04-28 10:13:23.613922+00
2793d3ff-843d-4201-911f-13cb869ecfdf	79858582-5a9b-4a5d-a825-c9015e1d9368	MONTHLY	1200	\N	\N	\N	\N	2026-04-28 10:13:23.614004+00	2026-04-28 10:13:23.614004+00
a28bd121-f517-490e-ae6a-5f6a4450fd1c	a5fd8156-965a-42a6-b685-d493fa720dba	PER_KM	5000	0	20	\N	\N	2026-04-30 10:26:27.156551+00	2026-04-30 10:26:27.156551+00
2ee009cb-ef2f-4436-889b-dc801a8336b4	a5fd8156-965a-42a6-b685-d493fa720dba	PER_KM	12000	21	150	\N	\N	2026-04-30 10:26:27.156695+00	2026-04-30 10:26:27.156695+00
ce52c64f-a792-4779-928b-b6963ebf5067	a5fd8156-965a-42a6-b685-d493fa720dba	PER_KM	20000	151	\N	\N	\N	2026-04-30 10:26:27.156768+00	2026-04-30 10:26:27.156768+00
f5945722-1f16-4be0-b00d-e9bfdbb41a8a	22fe7d16-72fe-44e5-ab9a-46d8d67bf101	PER_KM	9000	0	20	\N	\N	2026-04-30 10:26:27.171596+00	2026-04-30 10:26:27.171596+00
58f71515-78d5-4810-be8b-84f7b1221de7	22fe7d16-72fe-44e5-ab9a-46d8d67bf101	PER_KM	22000	21	150	\N	\N	2026-04-30 10:26:27.171755+00	2026-04-30 10:26:27.171755+00
2891724c-93ca-4068-aa8b-aac3abe06ecc	22fe7d16-72fe-44e5-ab9a-46d8d67bf101	PER_KM	35000	151	\N	\N	\N	2026-04-30 10:26:27.171866+00	2026-04-30 10:26:27.171866+00
bfcc2edc-7c1d-447d-8755-1515d9e9b3db	1ccd5fd5-8193-44fd-afc4-9732292f3bfb	DAILY	480000	\N	\N	\N	\N	2026-04-30 10:28:45.912634+00	2026-04-30 10:28:45.912634+00
4d5fccb4-3291-45d9-a927-511b94d1efd2	1ccd5fd5-8193-44fd-afc4-9732292f3bfb	PER_KM	2000	1	20	\N	\N	2026-04-30 10:28:45.912792+00	2026-04-30 10:28:45.912792+00
d1d4284b-887c-430b-ba82-c1d13992bea9	1ccd5fd5-8193-44fd-afc4-9732292f3bfb	PER_KM	15000	21	150	\N	\N	2026-04-30 10:28:45.912909+00	2026-04-30 10:28:45.912909+00
ee09d0cb-c509-432f-a16a-ed9a6f950e96	a0e7382c-0e02-47be-9c96-08b9ea0a466b	DAILY	220000	\N	\N	\N	\N	2026-04-30 10:30:03.015647+00	2026-04-30 10:30:03.015647+00
a00540fe-65ae-416b-ac1e-6da848b1fb92	a0e7382c-0e02-47be-9c96-08b9ea0a466b	PER_KM	1100	1	21	\N	\N	2026-04-30 10:30:03.016161+00	2026-04-30 10:30:03.016161+00
170808ff-0467-4173-b6e3-2bcf4d2df697	a0e7382c-0e02-47be-9c96-08b9ea0a466b	PER_KM	111100	22	121	\N	\N	2026-04-30 10:30:03.016341+00	2026-04-30 10:30:03.016341+00
bc0c1728-d745-4487-9f20-7f42c03935e1	461eb95a-0583-4d19-85e9-9237d0fcdab0	PER_KM	2000	0	20000	\N	\N	2026-04-30 10:31:05.606084+00	2026-04-30 10:31:05.606084+00
\.


--
-- Data for Name: cars; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cars (id, supplier_id, registration_no, name, category, usage_type, year, passenger_capacity, luggage_capacity, has_ac, is_automatic, fuel_type, color, description, cover_image_url, gallery_urls, includes, excludes, status, created_at, updated_at, deleted_at) FROM stdin;
a0e7382c-0e02-47be-9c96-08b9ea0a466b	\N	MU-STD-004	Toyota Corolla Cross	STANDARD	BOTH	2022	5	2	t	t	Petrol	Silver	Reliable, comfortable, and fuel-efficient. The Corolla Cross is our most popular choice for families and couples exploring the island at their own pace.	https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?auto=format&fit=crop&w=800&q=80	{https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80}	{"Comprehensive insurance","GPS Navigation","Air conditioning"}	{Fuel,Driver,"Child seats (available on request)"}	ACTIVE	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N
461eb95a-0583-4d19-85e9-9237d0fcdab0	\N	MU-ECO-005	Suzuki Swift	ECONOMY	BOTH	2023	4	1	t	t	Petrol	Red	The zippiest way to discover Mauritius. The Suzuki Swift is compact, economical and easy to park — perfect for solo travellers or couples on a budget.	https://images.unsplash.com/photo-1609521263047-f8f205293f24?auto=format&fit=crop&w=800&q=80	\N	{"Comprehensive insurance","Air conditioning"}	{"GPS (available on request)",Fuel,Driver}	ACTIVE	2026-04-27 07:16:00.85958+00	2026-04-30 10:26:27.11241+00	\N
79858582-5a9b-4a5d-a825-c9015e1d9368	\N	MU-PRE-003	Audi A4	PREMIUM	BOTH	2023	5	3	t	t	Petrol	Glacier White	The Audi A4 combines sportiness with sophistication. Its quattro all-wheel drive and refined cabin make it the ideal choice for discerning travellers.	https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80	{https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1542362567-b07e54358753?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=800&q=80}	{"Comprehensive insurance","GPS Navigation","Air conditioning","Roadside assistance","XSVC:TEST 1:12100","XSVC:TEST 4:1100"}	{Fuel,Driver}	ACTIVE	2026-04-27 07:16:00.85958+00	2026-04-28 10:13:23.614292+00	\N
1ccd5fd5-8193-44fd-afc4-9732292f3bfb	\N	MU-SUV-002	Toyota Land Cruiser Prado	SUV	BOTH	2022	7	5	t	t	Diesel	Pearl White	Conquer every corner of Mauritius in the Land Cruiser Prado. This full-size 4×4 SUV handles mountain roads, coastal tracks and city streets with equal ease.	https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80	{https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1529988885170-6dc8ce34ce6d?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=800&q=80}	{"7-seat configuration","Comprehensive insurance","GPS Navigation","Air conditioning","Professional driver optional","XSVC:Men Seats:1000"}	{Fuel,"Off-road excursion surcharge"}	ACTIVE	2026-04-27 07:16:00.85958+00	2026-04-30 10:28:45.913132+00	\N
22fe7d16-72fe-44e5-ab9a-46d8d67bf101	\N	MU-LUX-001	Mercedes-Benz E-Class	LUXURY	BOTH	2023	4	3	t	t	Petrol	Obsidian Black	The Mercedes E-Class redefines executive travel. With plush leather interiors, MBUX infotainment and a whisper-quiet ride, every journey feels first class.	https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80	\N	{"Professional chauffeur available","Fuel included (first 150km)","Comprehensive insurance","GPS Navigation","Chilled water & refreshments"}	{"Additional fuel beyond 150km","Airport fast-track"}	ACTIVE	2026-04-27 07:16:00.85958+00	2026-04-30 10:26:27.093528+00	\N
bd6e2cfb-e93b-42dc-b311-b4aee6edf92c	\N	MU-VAN-006	Toyota HiAce Commuter	MINIVAN	BOTH	2022	12	8	t	f	Diesel	White	The HiAce Commuter is the gold standard for group travel in Mauritius. Spacious, air-conditioned and reliable — ideal for corporate groups, wedding parties and large families.	https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80	{https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1464219789935-c2d9d9aba644?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1506015391300-4802dc74de2a?auto=format&fit=crop&w=800&q=80}	{"Professional driver","Fuel included","Comprehensive insurance","Air conditioning","Luggage trailer available"}	{"Overtime after 8 hours","Toll fees"}	ACTIVE	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N
a5fd8156-965a-42a6-b685-d493fa720dba	\N	HR-32-8383	Honda City	PREMIUM	BOTH	2026	4	2	t	t	Petrol	Black	Black-Petrol	\N	\N	{XSVC:Wifi:100,"XSVC:Baby Sit:200",XSVC:Driver:300}	\N	ACTIVE	2026-04-30 09:17:35.511542+00	2026-04-30 10:26:27.063608+00	\N
\.


--
-- Data for Name: cart_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cart_items (id, session_key, item_type, ref_id, quantity, unit_price_cents, options, expires_at, created_at, updated_at, agent_id) FROM stdin;
ed61a395-ca6f-4fdf-95ee-46bd0b65fbf1	guest:peach-smoke-1777367888	CAR_RENTAL	461eb95a-0583-4d19-85e9-9237d0fcdab0	1	420000	{"rentalDays": 3, "dailyRateCents": 10000}	2026-05-05 09:18:08.414093+00	2026-04-28 09:18:08.414084+00	2026-04-28 09:18:08.414085+00	\N
6c29dd83-d186-4d4f-87bc-5f3bb5ddd728	guest:d3c60724-5efd-4f0a-8116-6bc8b6fdd1b4	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	440000	{"paxAdults": 2, "paxChildren": 0}	2026-05-05 09:48:26.024567+00	2026-04-28 09:48:26.024566+00	2026-04-28 09:48:26.024566+00	\N
b04449ff-9a4d-46af-80dc-ce1493f84393	guest:smoke-cart-1777370248	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	440000	{"paxAdults": 2}	2026-05-05 09:57:28.854255+00	2026-04-28 09:57:28.854249+00	2026-04-28 09:57:28.85425+00	\N
11825953-8ab1-4385-9335-ca1eab0d0cb5	guest:d3c60724-5efd-4f0a-8116-6bc8b6fdd1b4	TOUR	bb779850-7054-4b43-ae3b-a7501c9fc940	1	440000	{"paxAdults": 2}	2026-05-05 09:57:49.801317+00	2026-04-28 09:57:49.801315+00	2026-04-28 09:57:49.801316+00	\N
9aacc8e3-d4a8-4af7-8653-d8b31d565027	guest:ed9c3416-6fd4-48d1-8954-471c5e84a06d	CAR_TRANSFER	f0e98cfd-a3d0-44ba-a690-34b930544935	1	10000	{"date": "2026-04-28", "time": "04:12", "carId": "a0e7382c-0e02-47be-9c96-08b9ea0a466b", "adults": 2, "carName": "Toyota Corolla Cross", "tripType": "ONE_WAY", "markupPct": 10, "tierLabel": "Long Distance (21 km+)", "distanceKm": 44, "pickupLocation": "Sonipat", "unitPriceCents": 10000, "dropoffLocation": "Panipat"}	2026-05-05 10:41:50.64498+00	2026-04-28 10:41:50.644976+00	2026-04-28 10:41:50.644976+00	\N
35bfb75e-f37f-458f-a4b1-1a97f741a81f	user:25c70058-b889-4132-a38e-67de5bd60db1	CAR_RENTAL	461eb95a-0583-4d19-85e9-9237d0fcdab0	1	280000	{"adults": 2, "markupPct": 9.5, "pickupDate": "2026-04-29", "pickupTime": "16:26", "rentalDays": 2, "dropoffDate": "2026-04-29", "dropoffTime": "16:26", "dailyRateCents": 140000, "pickupLocation": "Sonipat", "unitPriceCents": 280000, "dropoffLocation": "Jaipur"}	2026-05-06 10:56:38.154634+00	2026-04-29 10:56:38.154629+00	2026-04-29 10:56:38.15463+00	\N
e0a994d6-e8c5-43c4-a8b4-c457592efeff	guest:e81b4a7d-b38b-45c5-a36f-a81f8d9f71c5	CAR_TRANSFER	461eb95a-0583-4d19-85e9-9237d0fcdab0	1	2000	{"carId": "461eb95a-0583-4d19-85e9-9237d0fcdab0", "adults": 4, "distanceKm": 30, "pickupLocation": "A", "dropoffLocation": "B"}	2026-05-07 10:35:49.616824+00	2026-04-30 10:35:49.616823+00	2026-04-30 10:35:49.616823+00	\N
1cab13db-a76b-475e-899d-8bf4e55901d2	guest:c43961a9-301a-47eb-82e7-13b2b3fed352	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-07 10:43:47.798951+00	2026-04-30 10:43:47.798938+00	2026-04-30 10:43:47.798939+00	\N
6eba800f-6e62-4a18-98d2-175b8011c102	guest:ba640f1d-7e57-4a53-aa6b-ea64a401ad07	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-07 10:48:55.538244+00	2026-04-30 10:48:55.538235+00	2026-04-30 10:48:55.538236+00	\N
949d69f2-3125-484b-86e5-f8c3de222a24	guest:b5b18fcd-ab21-4506-ae47-a46d67862210	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-07 10:53:04.167578+00	2026-04-30 10:53:04.167562+00	2026-04-30 10:53:04.167564+00	\N
56951e83-3d22-451e-8f5b-23eed125ab09	guest:c658fb63-7fd2-4b84-819e-b80eaccf952c	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-07 10:55:29.298706+00	2026-04-30 10:55:29.298699+00	2026-04-30 10:55:29.298699+00	\N
615f8954-fba9-4805-bb7b-ad03adfc51df	guest:e7ca5cb5-c32a-4717-aa17-27607310b98e	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-07 12:00:28.649769+00	2026-04-30 12:00:28.649762+00	2026-04-30 12:00:28.649762+00	\N
db334a92-10f0-4712-9124-29d3de720018	guest:709bf8f6-2585-4373-8d88-976fee5d9bf0	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-07 12:00:40.699263+00	2026-04-30 12:00:40.699262+00	2026-04-30 12:00:40.699262+00	\N
f9203364-1c42-4903-88b7-ae58347dbc37	guest:55565532-93c0-46c1-ba23-63bc6a6c9af1	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 05:26:43.329895+00	2026-05-01 05:26:43.329895+00	2026-05-01 05:26:43.329895+00	\N
03fdc904-1607-41dc-a7c9-7d1ccb9f9359	guest:1f2d79f1-6848-4c2b-8a9a-22fe2f93a4f7	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 05:39:42.516884+00	2026-05-01 05:39:42.516877+00	2026-05-01 05:39:42.516878+00	\N
b0571d53-add1-49a7-ae99-61a1419dda94	guest:99d121c6-10ba-4c9c-82f3-e378ced0d828	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 05:47:02.002336+00	2026-05-01 05:47:02.002333+00	2026-05-01 05:47:02.002334+00	\N
33e86a8f-17f1-4ee5-a641-68dda6bc5ce7	guest:58111571-9c3d-48c7-ad9d-eabdcf63dcb9	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 05:47:14.742765+00	2026-05-01 05:47:14.742763+00	2026-05-01 05:47:14.742764+00	\N
f74b97a4-a4ef-4dbb-b763-7dd753276998	guest:b6fa50e9-87bc-456a-8907-1a42a9fdae22	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 05:59:14.664101+00	2026-05-01 05:59:14.66409+00	2026-05-01 05:59:14.664092+00	\N
44bdc8f3-7628-4421-89d0-ac748385c9c6	guest:ae5c8f87-462e-4c2e-8289-d7c5b1c1b5d6	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 07:12:35.565952+00	2026-05-01 07:12:35.565951+00	2026-05-01 07:12:35.565951+00	\N
bb48eb46-7eb1-40c2-a786-adad974c2083	guest:84d214b3-3ca1-44bd-83dd-fc5668e52aa0	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 07:29:38.412297+00	2026-05-01 07:29:38.412289+00	2026-05-01 07:29:38.412289+00	\N
b27eda0b-f79f-4497-acfd-0c12e3c1d698	guest:ba77200d-99b1-477c-80ae-7cde7f03cbf9	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 07:31:40.148907+00	2026-05-01 07:31:40.148898+00	2026-05-01 07:31:40.1489+00	\N
3f7d5ca8-74a9-4573-abe4-b0c65d107e7a	guest:933b3cfe-1c20-4023-82ec-165368bd7333	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 07:32:56.247372+00	2026-05-01 07:32:56.247365+00	2026-05-01 07:32:56.247366+00	\N
2ef3b33d-ce7e-452b-bf8a-3f7af30c088b	guest:95847d0f-8a15-4293-81f9-3af3bb4d8678	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 07:34:27.255916+00	2026-05-01 07:34:27.255907+00	2026-05-01 07:34:27.255908+00	\N
1360d34b-c5cb-4f00-9a13-abd841b7474c	guest:426eb9a3-ecf7-4353-8fe5-bdb95aa76f3f	TOUR	6dd05f51-58da-49d5-83aa-5d2e45859b63	1	560000	{"date": "2026-08-15", "adults": 2, "paxAdults": 2}	2026-05-08 07:42:21.153101+00	2026-05-01 07:42:21.153099+00	2026-05-01 07:42:21.153099+00	\N
\.


--
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.customers (id, user_id, first_name, last_name, email, phone, whatsapp, nationality, passport_no, address, created_at, updated_at, deleted_at) FROM stdin;
30000000-0000-0000-0000-000000000001	00000000-0000-0000-0000-000000000004	John	Doe	john.doe@example.com	+44 7700 900123	\N	British	GB123456789	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N
30000000-0000-0000-0000-000000000002	00000000-0000-0000-0000-000000000006	Sarah	Martin	sarah.martin@example.com	+33 6 12 34 56 78	\N	French	FR987654321	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N
30000000-0000-0000-0000-000000000003	\N	Marco	Rossi	marco.rossi@example.com	+39 347 123 4567	\N	Italian	IT567890123	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N
d8728616-7c0f-47f0-9e05-8b0e2b9f89dd	\N	Gautam	Nagpal	nagpal.gautam@orangemnatra.in	7015664715	\N	\N	\N	\N	2026-04-27 08:43:57.211665+00	2026-04-27 08:43:57.211665+00	\N
c45879e5-2f3f-4b61-a7b7-efcd21607e9b	\N	Gautam	Nagppal	gautamnagpal70156@gmail.com	7015664715	\N	\N	\N	\N	2026-04-27 13:19:41.013048+00	2026-04-27 13:19:41.013048+00	\N
a272400f-f27e-48ce-a7f8-6ade23334ff4	\N	Cross	RoleTest	cross.role@example.com	+12345670000	\N	\N	\N	\N	2026-04-28 10:02:23.792036+00	2026-04-28 10:02:23.792036+00	\N
8434c5a8-3f85-4021-921d-5f997efe23d9	\N	E2E	Smoketest	e2e.smoketest@example.com	+12345670001	\N	\N	\N	\N	2026-04-28 10:04:09.770359+00	2026-04-28 10:04:09.770359+00	\N
5eea0a07-00aa-499e-bc50-8e3ec97f2653	\N	Priya	Sharma	agent@sunrisetravel.mu	+12345670099	\N	\N	\N	\N	2026-04-28 10:54:22.727705+00	2026-04-28 10:54:22.727705+00	\N
e5d2d508-99bc-495b-9094-0dc038039a0a	\N	Admin	FlowTest	admin.flowtest@example.com	+12345671111	\N	\N	\N	\N	2026-04-28 11:15:40.377754+00	2026-04-28 11:15:40.377754+00	\N
a7209ea9-a80f-4ddd-a60b-53746b16f31b	\N	Guest	QATest	qa.guest@example.com	+12345670001	\N	\N	\N	\N	2026-04-29 08:05:46.25241+00	2026-04-29 08:05:46.25241+00	\N
ac7e934e-9a12-4125-981e-45b3135b8d7d	\N	Test	User	qa.smoke@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 06:47:21.688073+00	2026-04-30 06:47:21.688073+00	\N
114bedec-7423-4cde-b979-af5097c08896	\N	A	B	qa2@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 06:48:30.964184+00	2026-04-30 06:48:30.964184+00	\N
15ccf9b3-a651-410d-976b-6e673a4f2b50	\N	Adults	Test	qa.adults@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 09:23:45.246961+00	2026-04-30 09:23:45.246961+00	\N
099ebf26-9acc-4d53-a7ed-5d49bd783086	\N	QA	Transfer	qa.transfer@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 09:55:43.51072+00	2026-04-30 09:55:43.51072+00	\N
b98c06ac-4670-4447-89fc-6d15cb553f69	\N	QA	BaseRate	qa.baserate@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 10:17:03.574109+00	2026-04-30 10:17:03.574109+00	\N
3a480513-9de5-4f28-a772-10fd9cf3ad92	\N	QA	Bands	qa.bands@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 10:26:27.304786+00	2026-04-30 10:26:27.304786+00	\N
feee6b2d-be82-40fa-a46c-133704cd4212	\N	QA	Peach	qa.peach@example.com	+23012345678	\N	\N	\N	\N	2026-04-30 12:00:28.733482+00	2026-04-30 12:00:28.733482+00	\N
deb5aa04-45a0-473d-833b-2cf83a4a60bd	\N	Anush	Raj	jkv@hshs.com	7015664715	\N	\N	\N	\N	2026-04-30 12:03:14.761657+00	2026-04-30 12:03:14.761657+00	\N
1a93f960-138b-4615-9039-3ceb84756550	\N	QA	PG	qa.pg@example.com	+23012345678	\N	\N	\N	\N	2026-05-01 05:26:43.370078+00	2026-05-01 05:26:43.370078+00	\N
ebaa3986-76fc-4b30-8410-a5a1948af801	\N	QA	Test	qa@example.com	+23012345678	\N	\N	\N	\N	2026-05-01 05:59:14.744376+00	2026-05-01 05:59:14.744376+00	\N
\.


--
-- Data for Name: day_trip_highlights; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.day_trip_highlights (id, day_trip_id, text, display_order) FROM stdin;
fa72ccff-f1c5-4ecd-9906-d1c59c4149f0	50000000-0000-0000-0000-000000000001	Lion Walk	0
d46b187d-ceca-40af-af49-19cebc10bd57	50000000-0000-0000-0000-000000000001	Zip Line	1
9451ff3f-e118-4d30-b739-eebed7e52bb8	50000000-0000-0000-0000-000000000001	Giraffe Feeding	2
d7e9909f-1d88-4723-859c-5390edf8eb93	50000000-0000-0000-0000-000000000001	African Savannah	3
12a24647-c092-4492-b74e-745ca854826b	50000000-0000-0000-0000-000000000002	Sea Turtles	0
bf51c764-a039-48e1-9198-e4a0cac1add5	50000000-0000-0000-0000-000000000002	Glass-Bottom Boat	1
7485ff6f-b590-4eeb-8e84-0c7acb2d68b6	50000000-0000-0000-0000-000000000002	Reef Sharks	2
811c17a0-b7f8-46e7-93ac-25e5da76233c	50000000-0000-0000-0000-000000000002	Coral Gardens	3
df2510fd-d9d6-4e3e-8f87-c7014dc5d077	50000000-0000-0000-0000-000000000003	Giant Water Lilies	0
6c2e43d2-6ab7-4a2f-bece-c649aa4be803	50000000-0000-0000-0000-000000000003	Blue Penny Museum	1
cb2e598b-c130-4ede-9716-bb6ab7a1ce64	50000000-0000-0000-0000-000000000003	Central Market	2
0d4aaff7-3a71-42f4-a9a0-b4a79d5de4c1	50000000-0000-0000-0000-000000000004	Private Charter	0
3ad42893-2753-4acf-849a-37bffa0affc8	50000000-0000-0000-0000-000000000004	Lobster Dinner	1
a74a3438-a69c-4b7f-9896-5e1be89c10a3	50000000-0000-0000-0000-000000000004	Champagne Sunset	2
\.


--
-- Data for Name: day_trip_itinerary_stops; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.day_trip_itinerary_stops (id, day_trip_id, stop_order, title, time_label, location, description) FROM stdin;
c491ff9e-6cb6-49c5-a208-0ec92dae9edb	50000000-0000-0000-0000-000000000001	0	Hotel Pick-Up	07:30	\N	Your guide meets you at your hotel reception.
31be8b92-661b-443c-a57e-461f36c3eba2	50000000-0000-0000-0000-000000000001	1	Casela Arrival & Savannah Walk	09:00	\N	Enter Casela World of Adventures and start with the African savannah walk.
bc6093a4-9c47-4154-890e-7447bee742c6	50000000-0000-0000-0000-000000000001	2	Lion Walk Experience	10:30	\N	Walk alongside sub-adult lions with experienced handlers in a safe environment.
d690dc36-cbaf-48a0-bb36-f542e7dfedc9	50000000-0000-0000-0000-000000000001	3	Giraffe Feeding & Lunch	12:30	\N	Feed the resident giraffes then enjoy lunch at the on-site restaurant.
59fe3cae-f1f0-4130-838d-81b8c8cf9790	50000000-0000-0000-0000-000000000001	4	Free Time & Optional Activities	14:00	\N	Choose from zip-lining, quad biking, rhino encounter or simply explore the park.
e86f86e2-4072-4e93-8b7e-0059732003b2	50000000-0000-0000-0000-000000000001	5	Return Transfer	16:30	\N	Comfortable return to your hotel.
346693cb-623f-4533-8bfc-fa973e79c94a	50000000-0000-0000-0000-000000000002	0	Hotel Pick-Up	07:30	\N	Transfer to the south coast.
1ebd4816-c201-4fc9-bb1f-ea58c06f0dbd	50000000-0000-0000-0000-000000000002	1	Glass-Bottom Boat Tour	09:30	\N	30-minute glass-bottom boat ride over the coral reef.
ff523232-03b0-4e7e-a723-b44c60413096	50000000-0000-0000-0000-000000000002	2	Snorkeling in the Lagoon	10:15	\N	Guided snorkel session with equipment provided — spot sea turtles and tropical fish.
33aead36-e7bc-4acd-84e2-6f3a73633627	50000000-0000-0000-0000-000000000002	3	Beach Time	12:00	\N	Relax on the white-sand beach at Blue Bay.
30885edd-5af6-4d61-8ead-d6513d886f9f	50000000-0000-0000-0000-000000000002	4	BBQ Seafood Lunch	13:00	\N	Fresh seafood BBQ buffet served beachside.
f475f094-df6a-441c-a8eb-663d75972943	50000000-0000-0000-0000-000000000002	5	Return Transfer	15:30	\N	Return to your hotel.
\.


--
-- Data for Name: day_trip_pickup_zones; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.day_trip_pickup_zones (id, day_trip_id, zone_name, hotel_name, pickup_time_from, pickup_time_to, sort_order) FROM stdin;
71ef5d9a-5faa-4068-816e-3a7e5e204c47	50000000-0000-0000-0000-000000000001	Grand Baie / Pereybere	\N	07:30:00	08:00:00	0
f4a31454-316a-4996-ace7-2dde26d73dbe	50000000-0000-0000-0000-000000000001	Port Louis / Bagatelle	\N	08:00:00	08:30:00	1
325eb0bd-08ff-4613-b748-2ab5dcd3945b	50000000-0000-0000-0000-000000000001	Flic en Flac / Wolmar	\N	07:00:00	07:30:00	2
a2adf795-d1f0-4ae7-ba53-cdada42bd8e4	50000000-0000-0000-0000-000000000002	Grand Baie / Pereybere	\N	07:30:00	08:00:00	0
4f4f2854-72a8-4dd3-9243-c2936af0bee7	50000000-0000-0000-0000-000000000002	Mahebourg / Blue Bay	\N	08:30:00	09:00:00	1
38ee14d7-1562-4167-8e35-d5f28d06b5d6	50000000-0000-0000-0000-000000000003	Grand Baie / Pereybere	\N	08:00:00	08:30:00	0
ca5da378-52ed-4b17-9673-3c77564247a1	50000000-0000-0000-0000-000000000003	Port Louis Centre	\N	08:30:00	09:00:00	1
8ef5c654-e836-46e8-b0c6-9aa259fe7656	50000000-0000-0000-0000-000000000004	Flic en Flac / Wolmar	\N	17:00:00	17:30:00	0
c40d03eb-468c-4df3-8295-46a58d756855	50000000-0000-0000-0000-000000000004	Grand Baie / Pereybere	\N	17:00:00	17:30:00	1
\.


--
-- Data for Name: day_trips; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.day_trips (id, supplier_id, title, slug, description, trip_type, region, duration, adult_price_cents, child_price_cents, max_pax, includes, excludes, cover_image_url, gallery_urls, status, created_at, updated_at, deleted_at, theme, net_rate_per_pax_cents, markup_pct, price_per_vehicle_cents) FROM stdin;
50000000-0000-0000-0000-000000000001	10000000-0000-0000-0000-000000000001	Casela Nature Parks Adventure	casela-nature-parks	Spend a thrilling day at Casela World of Adventures — zip-line through the canopy, walk with lions, feed giraffes and explore the authentic African savannah. One of Mauritius's most popular family attractions.	SHARED	WEST	FULL_DAY	280000	180000	40	{"Round-trip transfers","Park entrance","Lion walk (1 round)","Lunch voucher","Bottled water"}	{"Optional activities (zip-line, quad bike, rhino encounter)","Personal expenses"}	/images/pexels-colourclouds-34264091.jpg	\N	ACTIVE	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	NATURE	0	0.00	0
50000000-0000-0000-0000-000000000002	10000000-0000-0000-0000-000000000002	Blue Bay Marine Park Snorkel & Beach	blue-bay-marine-snorkel	Explore the crystal-clear waters of Blue Bay Marine Park — a protected UNESCO lagoon teeming with sea turtles, reef sharks and over 50 species of hard coral. Spend the afternoon relaxing on the white-sand beach.	SHARED	SOUTH	FULL_DAY	220000	140000	20	{"Return transfers","Snorkeling equipment","Glass-bottom boat (30 min)","Seafood BBQ lunch","Soft drinks"}	{Towels,Sunscreen,"Alcoholic beverages"}	/images/pexels-cemil-tuyloglu-3443668-28885219.jpg	\N	ACTIVE	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	SEA_ACTIVITIES	0	0.00	0
50000000-0000-0000-0000-000000000003	10000000-0000-0000-0000-000000000001	Pamplemousses & Port Louis City Tour	pamplemousses-port-louis	A cultural half-day combining the historic Sir Seewoosagur Ramgoolam Botanical Garden with a guided walk through Port Louis — the Caudan Waterfront, the Blue Penny Museum, and the bustling Central Market.	SHARED	NORTH	HALF_DAY	150000	90000	30	{"Air-conditioned minivan","Bilingual guide","Botanical garden entrance","Blue Penny Museum entry"}	{Lunch,"Personal shopping"}	/images/pexels-vince-34732389.jpg	\N	ACTIVE	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	CULTURAL	0	0.00	0
50000000-0000-0000-0000-000000000004	10000000-0000-0000-0000-000000000002	Private Catamaran Sunset Cruise	private-catamaran-sunset	Charter an entire luxury catamaran for your group. Watch the sun set over the Indian Ocean with cocktails in hand, then dine under the stars on fresh-caught lobster and seafood. The ultimate Mauritius experience.	PRIVATE	WEST	HALF_DAY	650000	400000	12	{"Exclusive catamaran charter","Welcome champagne","4-course seafood dinner","Open bar","Hotel transfers"}	{Gratuities,"Fuel surcharge beyond 3 hours"}	/images/xavier-coiffic-yFSDYHAfhrI-unsplash.jpg	\N	ACTIVE	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	BEACH	0	0.00	0
dbdd5536-a4a1-4eff-9c35-3c04e522de64	\N	Chamarel Seven-Coloured Earths Discovery	chamarel-seven-coloured-earths-discovery-d3c10e	Visit the famous Seven Coloured Earths in Chamarel and the breath-taking Chamarel waterfall. Includes lunch at a local restaurant.	SHARED	NORTH	FULL_DAY	18000	9000	15	{}	{}		\N	ACTIVE	2026-04-28 11:15:00.268304+00	2026-04-28 11:15:00.268304+00	\N	\N	0	0.00	0
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	init schema	SQL	V1__init_schema.sql	1767473720	ambiance	2026-04-27 12:46:00.673247	150	t
2	2	seed data	SQL	V2__seed_data.sql	-470955033	ambiance	2026-04-27 12:46:00.848925	19	t
3	3	gap closure	SQL	V3__gap_closure.sql	1323737375	ambiance	2026-04-27 12:46:00.881849	31	t
4	4	auth enhancements	SQL	V4__auth_enhancements.sql	1523868490	ambiance	2026-04-27 12:46:00.919264	4	t
5	5	test seed	SQL	V5__test_seed.sql	1591735438	ambiance	2026-04-27 12:48:19.641511	39	t
6	6	transfer pricing	SQL	V6__transfer_pricing.sql	-2003058404	ambiance	2026-04-27 17:02:45.37379	19	t
7	7	peach payments	SQL	V7__peach_payments.sql	-2020191494	ambiance	2026-04-28 14:44:48.731436	19	t
8	8	remove driver module	SQL	V8__remove_driver_module.sql	-574389137	ambiance	2026-04-29 13:21:23.736689	26	t
9	9	otp password reset	SQL	V9__otp_password_reset.sql	1670621493	ambiance	2026-04-29 16:20:01.856212	18	t
10	10	transfer includes excludes	SQL	V10__transfer_includes_excludes.sql	-173845333	ambiance	2026-04-30 14:58:56.884701	14	t
\.


--
-- Data for Name: invoices; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.invoices (id, booking_id, invoice_number, status, subtotal_cents, vat_cents, total_cents, pdf_url, issued_at, due_date, voided_at, voided_reason, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: leads; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.leads (id, name, email, phone, message, source, is_converted, created_at) FROM stdin;
ca17bb0e-cd5d-4616-9a83-44d15163781f	Antoine Dupont	antoine.dupont@example.fr	+33 6 98 76 54 32	We are a family of 4 planning a 10-day trip in July. Interested in a full island tour package.	contact_form	f	2026-04-27 07:18:19.663585+00
d8ee5532-3f6b-42dc-822d-ca9f17a214ba	Mei Lin	mei.lin@example.sg	+65 9123 4567	Looking for luxury car rental + driver for 7 days, arriving 1 June.	contact_form	f	2026-04-27 07:18:19.663585+00
812308c5-5a76-4331-b986-b55b52d07b97	James Okafor	james.okafor@example.ng	+234 801 234 5678	Corporate group of 12, need transfers from SSR airport and team excursions.	website_chat	f	2026-04-27 07:18:19.663585+00
\.


--
-- Data for Name: login_attempts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.login_attempts (id, email, ip_address, attempted_at, success) FROM stdin;
be8a5a3a-a0a0-497e-ab4a-9cf50acfdc44	agent@sunrisetravel.mu	0:0:0:0:0:0:0:1	2026-04-29 10:50:41.97721+00	t
bfb80638-0a5a-481c-9af9-204b8b7dc97f	gautam.nagpal05@gmail.com	0:0:0:0:0:0:0:1	2026-04-30 06:44:40.099572+00	t
93635ac6-dc6d-4dbe-886a-295df36ebc51	nagpal.gautam@orangemantra.in	0:0:0:0:0:0:0:1	2026-04-30 07:54:29.349253+00	t
6aeb0856-4de6-4444-8752-4a8d1e952f1b	gautamnagpal70156@gmail.com	0:0:0:0:0:0:0:1	2026-04-28 07:23:12.171546+00	t
a6dc0059-fe95-4c7c-9c36-ed929fc0690b	admin@ambianceholidays.mu	0:0:0:0:0:0:0:1	2026-05-01 09:48:24.243211+00	t
4096f1c3-a887-410b-ae62-f25977ba3987	jkv@hshs.com	0:0:0:0:0:0:0:1	2026-05-01 09:48:42.049329+00	t
\.


--
-- Data for Name: newsletter_subscribers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.newsletter_subscribers (id, email, is_active, subscribed_at, unsubscribed_at, confirmation_token_hash, confirmed_at) FROM stdin;
7d6c538b-762d-409b-a52f-24d253a736ec	newsletter1@example.com	t	2026-04-27 07:18:19.663585+00	\N	\N	2026-03-28 07:18:19.663585+00
011b1591-da2d-4683-8e3f-3dc35e05b1b0	newsletter2@example.com	t	2026-04-27 07:18:19.663585+00	\N	\N	2026-04-13 07:18:19.663585+00
778aa8fb-ee45-4e76-accd-8e42c3e235eb	newsletter3@example.com	f	2026-04-27 07:18:19.663585+00	\N	\N	\N
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notifications (id, user_id, booking_id, channel, status, subject, body, recipient, sent_at, error_msg, created_at) FROM stdin;
d386fb21-d255-4c57-ba82-eb433765dfa9	\N	b610e654-c8cf-4bc1-a7e9-cafd151a1f4f	EMAIL	FAILED	Booking Confirmed — AMB-088931	Dear Gautam,\n\nYour booking AMB-088931 has been confirmed.\nService Date: 2026-04-27\nTotal: Rs 1610\n\nThank you for choosing Ambiance Holidays.\n	nagpal.gautam@orangemnatra.in	\N	Authentication failed	2026-04-27 08:43:57.224558+00
4c436135-3f55-44c1-abcc-f432f4cb8443	\N	66388ea1-07bf-4491-be43-4ff8a9c984db	EMAIL	FAILED	Booking Confirmed — AMB-027261	Dear Gautam,\n\nYour booking AMB-027261 has been confirmed.\nService Date: 2026-04-27\nTotal: Rs 115\n\nThank you for choosing Ambiance Holidays.\n	nagpal.gautam@orangemnatra.in	\N	Authentication failed	2026-04-27 12:20:16.046674+00
762e74f2-75b4-4daf-a765-9c6799edd894	\N	3526d195-18e0-4eea-81a5-e0e23c6f85c5	EMAIL	FAILED	Booking Confirmed — AMB-033444	Dear Gautam,\n\nYour booking AMB-033444 has been confirmed.\nService Date: 2026-04-27\nTotal: Rs 115\n\nThank you for choosing Ambiance Holidays.\n	gautamnagpal70156@gmail.com	\N	Authentication failed	2026-04-27 13:19:41.031301+00
4a8af512-543a-4a18-a4a1-5bc6031159ba	\N	c3721856-acd0-4372-83f9-f56df26fdea1	EMAIL	FAILED	Booking Confirmed — AMB-792440	Dear Gautam,\n\nYour booking AMB-792440 has been confirmed.\nService Date: 2026-04-28\nTotal: Rs 8740\n\nThank you for choosing Ambiance Holidays.\n	gautamnagpal70156@gmail.com	\N	Authentication failed	2026-04-28 05:08:26.264042+00
095c5cb3-ded0-4cb5-b643-8a514b77fc6a	\N	1ba0aa87-9c56-48fb-aacc-b2d515646e26	EMAIL	FAILED	Booking Confirmed — AMB-847671	Dear Gautam,\n\nYour booking AMB-847671 has been confirmed.\nService Date: 2026-04-28\nTotal: Rs 4370\n\nThank you for choosing Ambiance Holidays.\n	gautamnagpal70156@gmail.com	\N	Authentication failed	2026-04-28 06:29:14.465609+00
d458ad75-77d8-43fe-a3ee-511d03489a00	\N	e1b3b23b-58c4-491f-b87d-d38267946c0f	EMAIL	FAILED	Booking Confirmed — AMB-681132	Dear Gautam,\n\nYour booking AMB-681132 has been confirmed.\nService Date: 2026-04-28\nTotal: Rs 4370\n\nThank you for choosing Ambiance Holidays.\n	gautamnagpal70156@gmail.com	\N	Authentication failed	2026-04-28 07:22:18.587639+00
48d0de63-82f4-437b-8c01-cf1496672689	\N	b3eb1cc4-8240-4ac8-821c-2ad3c3c1d8db	EMAIL	FAILED	Booking Confirmed — AMB-537274	Dear Gautam,\n\nYour booking AMB-537274 has been confirmed.\nService Date: 2026-04-28\nTotal: Rs 115\n\nThank you for choosing Ambiance Holidays.\n	gautamnagpal70156@gmail.com	\N	Authentication failed	2026-04-28 07:24:57.636705+00
124a205a-4f47-465c-8035-f4edf74ac404	\N	56e16be4-06a3-499a-8425-252c2a283f56	EMAIL	FAILED	Booking Confirmed — AMB-197409	Dear Gautam,\n\nYour booking AMB-197409 has been confirmed.\nService Date: 2026-04-28\nTotal: Rs 17940\n\nThank you for choosing Ambiance Holidays.\n	gautamnagpal70156@gmail.com	\N	Authentication failed	2026-04-28 09:22:01.611377+00
97fb8f1f-60c2-44f2-94fc-0e6deaf6434c	\N	455ccb3f-85cf-49db-927f-7aae7ee1871e	EMAIL	FAILED	Booking Confirmed — AMB-828097	Dear Cross,\n\nYour booking AMB-828097 has been confirmed.\nService Date: 2026-12-15\nTotal: Rs 5500\n\nThank you for choosing Ambiance Holidays.\n	cross.role@example.com	\N	Authentication failed	2026-04-28 10:02:23.810604+00
9a536187-1d25-4846-b62b-56947e7efebe	\N	3faf03cf-8110-4f47-81da-996e0ab976ea	EMAIL	FAILED	Booking Confirmed — AMB-672580	Dear E2E,\n\nYour booking AMB-672580 has been confirmed.\nService Date: 2026-12-20\nTotal: Rs 5060\n\nThank you for choosing Ambiance Holidays.\n	e2e.smoketest@example.com	\N	Authentication failed	2026-04-28 10:04:09.776429+00
9b15344a-5df1-4be7-81b7-2c8762d614aa	\N	da5300e1-54e7-48d6-9b5c-ae2e67cc9a59	EMAIL	FAILED	Booking Confirmed — AMB-763015	Dear Priya,\n\nYour booking AMB-763015 has been confirmed.\nService Date: 2026-12-22\nTotal: Rs 2530\n\nThank you for choosing Ambiance Holidays.\n	agent@sunrisetravel.mu	\N	Authentication failed	2026-04-28 10:54:22.755211+00
c657c636-2767-4383-8a91-1f541bc2c3d7	\N	4aae022d-8199-48ac-b1f4-e9a3cc549163	EMAIL	FAILED	Booking Confirmed — AMB-897834	Dear Priya,\n\nYour booking AMB-897834 has been confirmed.\nService Date: 2026-12-29\nTotal: Rs 5500\n\nThank you for choosing Ambiance Holidays.\n	agent@sunrisetravel.mu	\N	Authentication failed	2026-04-28 10:58:55.734215+00
dd787cef-d192-4f1f-8397-185d95414bff	\N	1cb504e7-50f4-49ce-9b08-dbabff644c93	EMAIL	FAILED	Booking Confirmed — AMB-120259	Dear Admin,\n\nYour booking AMB-120259 has been confirmed.\nService Date: 2027-01-10\nTotal: Rs 563\n\nThank you for choosing Ambiance Holidays.\n	admin.flowtest@example.com	\N	Authentication failed	2026-04-28 11:15:40.390553+00
c2736bee-d313-4852-bbf6-e5c74554f6e6	\N	fe35748e-6c80-4568-b58d-2221fd1df82b	EMAIL	FAILED	Booking Confirmed — AMB-267261	Dear Priya,\n\nYour booking AMB-267261 has been confirmed.\nService Date: 2027-02-14\nTotal: Rs 225\n\nThank you for choosing Ambiance Holidays.\n	agent@sunrisetravel.mu	\N	Authentication failed	2026-04-28 12:16:00.74928+00
4c7b864d-bfac-421f-a98f-c8d25c9b5d31	\N	56a43100-47e7-4004-807b-afab815600bc	EMAIL	FAILED	Booking Confirmed — AMB-491525	Dear Guest,\n\nYour booking AMB-491525 has been confirmed.\nService Date: 2027-03-15\nTotal: Rs 6440\n\nThank you for choosing Ambiance Holidays.\n	qa.guest@example.com	\N	Authentication failed	2026-04-29 08:05:46.269924+00
2a5e31e7-443b-4bd3-a0fb-4c1b810a37fe	\N	db9a785a-8461-4afe-a89d-d291a1a65627	EMAIL	SENT	Booking Confirmed — AMB-857519	Dear Test,\n\nYour booking AMB-857519 has been confirmed.\nService Date: 2026-06-15\nTotal: Rs 3220\n\nThank you for choosing Ambiance Holidays.\n	qa.smoke@example.com	2026-04-30 06:47:26.215982+00	\N	2026-04-30 06:47:21.697229+00
35053607-af43-4f60-9f55-0b21341c38be	\N	616c004f-9098-4591-a17c-bda59f9c6acd	EMAIL	SENT	Booking Confirmed — AMB-802295	Dear A,\n\nYour booking AMB-802295 has been confirmed.\nService Date: 2026-07-15\nTotal: Rs 3220\n\nThank you for choosing Ambiance Holidays.\n	qa2@example.com	2026-04-30 06:48:35.781223+00	\N	2026-04-30 06:48:30.971235+00
174711bf-da25-49f0-a81e-4fd2398a1eff	\N	f2e62ccd-53f6-4cd6-b19f-0fe30968541f	EMAIL	SENT	Booking Confirmed — AMB-965967	Dear Adults,\n\nYour booking AMB-965967 has been confirmed.\nService Date: 2026-06-15\nTotal: Rs 4830\n\nThank you for choosing Ambiance Holidays.\n	qa.adults@example.com	2026-04-30 09:23:49.99314+00	\N	2026-04-30 09:23:45.252448+00
7273d83a-8b41-4ee5-86ef-e5902904e0c1	\N	e5fa9cff-7c94-4b30-9049-9c1299450ff5	EMAIL	SENT	Booking Confirmed — AMB-004323	Dear QA,\n\nYour booking AMB-004323 has been confirmed.\nService Date: 2026-06-15\nTotal: Rs 161\n\nThank you for choosing Ambiance Holidays.\n	qa.transfer@example.com	2026-04-30 09:55:53.644171+00	\N	2026-04-30 09:55:43.527484+00
a5aa76d7-7584-4fdc-9290-f1aba1fb4c31	\N	ecfa530f-3392-4082-ba07-f58c60ad1b8e	EMAIL	SENT	Booking Confirmed — AMB-520343	Dear QA,\n\nYour booking AMB-520343 has been confirmed.\nService Date: 2026-06-15\nTotal: Rs 161\n\nThank you for choosing Ambiance Holidays.\n	qa.baserate@example.com	2026-04-30 10:17:08.41406+00	\N	2026-04-30 10:17:03.590245+00
4a1e04db-fad0-4363-85c4-eed32a277e20	\N	ab0e6418-d013-4280-ae70-e55560915e36	EMAIL	SENT	Booking Confirmed — AMB-861342	Dear QA,\n\nYour booking AMB-861342 has been confirmed.\nService Date: 2026-06-15\nTotal: Rs 138\n\nThank you for choosing Ambiance Holidays.\n	qa.bands@example.com	2026-04-30 10:26:32.678118+00	\N	2026-04-30 10:26:27.319501+00
9d388352-3627-40a7-88cc-1a43942704c0	\N	efb73144-4034-46c8-bcf2-c743e34f068e	EMAIL	SENT	Booking Confirmed — AMB-085561	Dear Anush,\n\nYour booking AMB-085561 has been confirmed.\nService Date: 2026-05-01\nTotal: Rs 8970\n\nThank you for choosing Ambiance Holidays.\n	jkv@hshs.com	2026-05-01 08:21:26.997956+00	\N	2026-05-01 08:21:19.159451+00
ce7d7641-2ca4-482d-b619-d566ce456d9a	\N	e50e3906-20af-4d00-9c8b-c044ce2b5b1f	EMAIL	SENT	Booking Confirmed — AMB-814724	Dear Anush,\n\nYour booking AMB-814724 has been confirmed.\nService Date: 2026-05-01\nTotal: Rs 8970\n\nThank you for choosing Ambiance Holidays.\n	jkv@hshs.com	2026-05-01 08:25:22.660091+00	\N	2026-05-01 08:25:17.778934+00
6a1258ae-08da-46bd-9ca5-17513a92f106	\N	570bdc3a-f681-4c24-b8ba-69cf5e495b34	EMAIL	SENT	Booking Confirmed — AMB-553524	Dear Anush,\n\nYour booking AMB-553524 has been confirmed.\nService Date: 2026-05-01\nTotal: Rs 8970\n\nThank you for choosing Ambiance Holidays.\n	jkv@hshs.com	2026-05-01 08:26:35.124331+00	\N	2026-05-01 08:26:30.725486+00
57ea7e7e-82be-4e05-88ac-cdff9f384a22	\N	6f0cdca2-17c6-4878-b567-998c754c552e	EMAIL	SENT	Booking Confirmed — AMB-205865	Dear Anush,\n\nYour booking AMB-205865 has been confirmed.\nService Date: 2026-05-01\nTotal: Rs 8970\n\nThank you for choosing Ambiance Holidays.\n	jkv@hshs.com	2026-05-01 09:51:11.710192+00	\N	2026-05-01 09:51:06.861471+00
\.


--
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.password_reset_tokens (id, user_id, token_hash, expires_at, used_at, created_at, attempts) FROM stdin;
ed97c482-2c7e-499a-8b2c-993267e49188	00000000-0000-0000-0000-000000000005	vRFBqaD8XN4HodEviEy+TpUxw+914tvw+iwkHuP+S/Q=	2026-04-29 11:00:34.869317+00	2026-04-29 10:50:39.227755+00	2026-04-29 10:50:34.869327+00	0
6442941c-c524-483c-80a3-903313bb6fbd	00000000-0000-0000-0000-000000000005	YRdWExR+y6TvnuD7bskzFEgzFc2MDVKud9+X0nfuhC8=	2026-04-29 11:00:39.385826+00	2026-04-29 10:50:41.920987+00	2026-04-29 10:50:39.385828+00	0
97844308-0763-48e0-a93a-c2bbb218b7b6	25c70058-b889-4132-a38e-67de5bd60db1	G3/gAmh0R249b5aiTwhvrbv9GPm64bL1PIwf/lqG3rc=	2026-04-27 08:25:06.221569+00	2026-04-29 10:52:19.869031+00	2026-04-27 07:25:06.221576+00	0
62a02fd2-c362-4c32-a1a5-d2451a7bb8b2	25c70058-b889-4132-a38e-67de5bd60db1	UPdw/M5UpV2u9WwV0AeWImV42kctAkaR5TA+Dvw0MLg=	2026-04-27 13:29:07.813785+00	2026-04-29 10:52:19.869031+00	2026-04-27 12:29:07.813804+00	0
9dbe6359-8a15-4002-90c7-3cb548bbe9be	25c70058-b889-4132-a38e-67de5bd60db1	DYwT/z3GBEkbEn30TiEiVd03f1/PYgwtG9lP7MjQ1is=	2026-04-29 11:37:29.426565+00	2026-04-29 10:52:19.869031+00	2026-04-29 10:37:29.426602+00	0
53ec5671-1bd7-457d-ac4f-0bc0bc2d3c38	25c70058-b889-4132-a38e-67de5bd60db1	B358pRqKDb8/nxxFZg1js8IDcJNzAM9iVL9TNe9i8RI=	2026-04-29 11:02:19.873488+00	2026-04-29 10:55:31.936405+00	2026-04-29 10:52:19.873493+00	0
26050266-1052-4455-96e4-35f936e9df7c	25c70058-b889-4132-a38e-67de5bd60db1	vSSyPg4chbIYmudUfhsidqHZzqOW4VlKsevwKDB2xuM=	2026-04-29 11:05:59.60704+00	2026-04-29 10:57:41.298635+00	2026-04-29 10:55:59.607052+00	0
39a3d0a4-7d3b-4c27-9651-662e5c3faaa9	25c70058-b889-4132-a38e-67de5bd60db1	iTEdp3SFKdAbZuMnFaJ4gZ45AhlDM9NR50MIdRynIfk=	2026-04-29 11:07:41.309652+00	2026-04-29 11:04:06.859465+00	2026-04-29 10:57:41.30966+00	0
4bdd5ba9-fc67-4818-a213-260ca27ecd8c	25c70058-b889-4132-a38e-67de5bd60db1	FM6hCWWCgL6zwwmfNrJzZxprLDZWwr9e0Q5XbcIFCBM=	2026-04-29 11:14:06.885656+00	2026-04-29 11:20:49.009156+00	2026-04-29 11:04:06.885671+00	0
a40306ce-6fe8-458e-96c8-fb62c742e117	2fec982b-1835-47d2-a843-03110112af50	c2Yk/H/yms7tIZnph38GZ2oRzRTGJwYOVxL+6XmDfiU=	2026-04-29 11:15:13.066147+00	2026-04-30 06:33:17.962373+00	2026-04-29 11:05:13.066155+00	0
17b6aba4-3168-4f5f-804e-631c7989e6c3	25c70058-b889-4132-a38e-67de5bd60db1	YpTeJfebbX2llS/qsSgj2NyWrGFq/6GuUBuOdpNdQpc=	2026-04-29 11:30:49.015104+00	2026-04-30 06:36:31.850194+00	2026-04-29 11:20:49.015113+00	0
9cbc192b-fb11-414b-997a-738c1f2badd8	25c70058-b889-4132-a38e-67de5bd60db1	sywA65Pa+7bcbJRODPTAW6fyI6aXSUkHKPvnP8zdkz4=	2026-04-30 06:46:31.857242+00	2026-04-30 06:37:03.721238+00	2026-04-30 06:36:31.857251+00	0
04de7ca2-dabc-4e9e-b06d-69fa2c489acd	2fec982b-1835-47d2-a843-03110112af50	FUtW1RPFrm1RiZdywcKJ4y+cy9alGihqKV8FfRPm89w=	2026-04-30 06:43:17.995233+00	2026-04-30 06:41:58.77992+00	2026-04-30 06:33:17.995244+00	0
84023271-f8c7-4e97-8202-dbe6f81fab21	2fec982b-1835-47d2-a843-03110112af50	FbKcDJYW7o8uMhRuohLIXyDapRGvRtjZqwAwspB12DQ=	2026-04-30 06:51:58.785399+00	2026-04-30 06:42:24.015333+00	2026-04-30 06:41:58.785402+00	0
4382c16d-ddb6-4f18-829a-58b6863ea7c2	2fec982b-1835-47d2-a843-03110112af50	w4NGgWRU3M0nImytC2zcgZf6cYtFHlZa5vIiLMOO0tI=	2026-04-30 06:54:01.082325+00	2026-04-30 06:44:33.421821+00	2026-04-30 06:44:01.082327+00	0
aa55597c-57a1-4318-bb6a-1e531cc473fa	2fec982b-1835-47d2-a843-03110112af50	iM5tb8X5+HjPNl4HoitsGvVrXDHxSH8Ii0PIvfAUMeU=	2026-04-30 06:54:33.422834+00	2026-04-30 06:44:40.030235+00	2026-04-30 06:44:33.422836+00	0
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.payments (id, booking_id, stripe_payment_intent, stripe_charge_id, method, amount_cents, currency, status, refunded_cents, stripe_refund_id, paid_at, refunded_at, metadata, created_at, updated_at, peach_checkout_id, peach_payment_id, peach_result_code, peach_result_desc) FROM stdin;
e14c6f39-d386-4c86-8d46-eae48aef5da1	b0000000-0000-0000-0000-000000000001	\N	\N	STRIPE	506000	MUR	SUCCEEDED	0	\N	2026-04-17 07:18:19.663585+00	\N	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N
c9f61d65-f09f-462a-a57b-44f70bead479	b0000000-0000-0000-0000-000000000002	\N	\N	BANK_TRANSFER	1534400	MUR	SUCCEEDED	0	\N	2026-04-20 07:18:19.663585+00	\N	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N
59e8a5fa-61f3-4d4a-b4e3-b9b1a41168e3	b0000000-0000-0000-0000-000000000004	\N	\N	STRIPE	736000	MUR	REFUNDED	0	\N	2026-04-19 07:18:19.663585+00	\N	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N	\N
1307ab57-030d-4b85-bbfe-e7b18a68a360	83778b5a-1a57-4ffe-9673-114d966dd6d6	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-04-30 12:00:28.74739+00	2026-04-30 12:00:28.74739+00	AMB-660619	\N	\N	\N
577d23a5-638e-4b3d-bd7c-33daeb1f3239	bf2f6b76-515f-4311-ab00-c812efeba212	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-04-30 12:00:40.732006+00	2026-04-30 12:00:40.732006+00	AMB-392834	\N	\N	\N
859c12e9-08f1-4593-b9c0-6245694c00d5	9e24952f-c3dc-4c91-93e0-78bbd29b5161	\N	\N	PEACH	43240	USD	PENDING	0	\N	\N	\N	\N	2026-04-30 12:03:14.772169+00	2026-04-30 12:03:14.77217+00	AMB-736475	\N	\N	\N
231f1943-e2db-498f-bf0b-ad4f4bb0e839	3f1d7a53-63ab-4faf-9351-f07ec60cc1a3	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:26:43.378201+00	2026-05-01 05:26:43.378201+00	AMB-904392	\N	\N	\N
c7aa5579-2bc8-4e2d-a8d5-16fbd03620ec	09b810f6-a8b3-4da5-aa96-b5437f9b098d	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:39:42.601952+00	2026-05-01 05:39:42.601952+00	AMB-566367	\N	\N	\N
3a1edfd6-4eee-46cd-8cc9-e5a1bd4d9c6c	25fa813e-e5fa-4b56-9084-b39ad6f91d3c	\N	\N	PEACH	549240	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:43:23.012873+00	2026-05-01 05:43:23.012873+00	AMB-121427	\N	\N	\N
90c72089-97bc-4d52-a39a-e2eec206b3fe	02a4fe4b-f751-4f62-b197-d237b5331740	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:47:02.03381+00	2026-05-01 05:47:02.03381+00	AMB-436161	\N	\N	\N
36d198d6-845b-496f-ab79-980ab9da79ad	6120ae3f-402b-4077-98bc-e7b663800ae0	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:47:14.770028+00	2026-05-01 05:47:14.770028+00	AMB-935811	\N	\N	\N
40a0a838-f19e-44e6-9841-19ac2dadd3a9	5e33eec6-7d30-499a-8742-f5028bcc5b7c	\N	\N	PEACH	549240	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:50:50.661391+00	2026-05-01 05:50:50.661392+00	AMB-432585	\N	\N	\N
9e7b5aea-8a8b-4272-9e8a-fc087afbd8cf	dc0868a6-a270-4b8b-a6a9-b340af742273	\N	\N	PEACH	549240	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 05:56:19.017293+00	2026-05-01 05:56:19.017294+00	AMB-208494	\N	\N	\N
f8c59f68-7690-4e02-9723-885000d72a42	fb9ef279-f1a9-42b0-a97d-30fc9f418b37	\N	\N	PEACH	644000	ZAR	PENDING	0	\N	\N	\N	\N	2026-05-01 05:59:14.757524+00	2026-05-01 05:59:14.757524+00	AMB-448707	\N	\N	\N
0ed9daf2-de8b-4b92-ae53-fde0c3c6d725	c760e252-c448-4d53-a1da-7bedbfb78749	\N	\N	PEACH	644000	ZAR	PENDING	0	\N	\N	\N	\N	2026-05-01 07:12:35.584719+00	2026-05-01 07:12:35.584719+00	AMB-629694	\N	\N	\N
84dd9909-abf7-4aa5-bc3c-f844bc2d2676	420af25e-fe48-4f50-8fdb-bbbd9992d17e	\N	\N	PEACH	549240	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 07:22:51.365338+00	2026-05-01 07:22:51.365338+00	AMB-274224	\N	\N	\N
2428ed9b-f3a0-43dc-9370-0461ce780313	762b6810-bd47-46f2-879e-8ae72a314df4	\N	\N	PEACH	644000	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 07:42:22.396238+00	2026-05-01 07:42:22.396239+00	0f04290a67f440ce887341bff12dfe60	\N	\N	\N
67e0589a-fab7-4534-b9e6-96a267cbb8fc	21447da6-93aa-4b04-a825-47495f4394cd	\N	\N	PEACH	549240	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 07:44:10.869336+00	2026-05-01 07:44:10.869337+00	796264c719e6456193aba33701ae8b4a	\N	\N	\N
da0bd635-4249-431f-9245-03ec3efc9063	020e0cd4-d44d-4911-9727-2477816be834	\N	\N	PEACH	897000	USD	PENDING	0	\N	\N	\N	\N	2026-05-01 07:47:32.923233+00	2026-05-01 07:47:32.923234+00	d5df9afbe0d1490fb9e4ed740b02f428	\N	\N	\N
a4f0e3e3-7c65-42b1-b3b7-00bdce01741d	54349f33-b98f-4a43-accc-6b2de0b8ce6c	\N	\N	PEACH	897000	USD	FAILED	0	\N	\N	\N	\N	2026-05-01 07:50:10.574481+00	2026-05-01 08:00:14.726308+00	b61c5a12e0b84785a1b720a0275a8034	8ac7a4a19de066d9019de284c01e1b98	\N	\N
8550e680-9627-4687-acd1-1747330b28c5	f42d7a66-98e2-4ae3-857b-998e6c290bfe	\N	\N	PEACH	897000	USD	FAILED	0	\N	\N	\N	\N	2026-05-01 08:01:35.041757+00	2026-05-01 08:02:12.453934+00	be0c169420764cb6b3ce0e347a3bc42e	8ac7a4a09dde1d72019de28f2cdf2482	\N	\N
c7e6a8a0-271a-4e50-a54b-28fa454d827e	efb73144-4034-46c8-bcf2-c743e34f068e	\N	\N	PEACH	897000	USD	SUCCEEDED	0	\N	2026-05-01 08:21:19.14127+00	\N	\N	2026-05-01 08:20:29.55922+00	2026-05-01 08:21:19.159845+00	99fba4d239934b738d71ed3fc0c02ace	8ac7a4a19de066d9019de2a0afd759bb	000.100.110	Request successfully processed in 'Merchant in Integrator Test Mode'
026bd44c-c415-439e-9c3e-b113772f8a2c	e50e3906-20af-4d00-9c8b-c044ce2b5b1f	\N	\N	PEACH	897000	USD	SUCCEEDED	0	\N	2026-05-01 08:25:17.768838+00	\N	\N	2026-05-01 08:23:41.651059+00	2026-05-01 08:25:17.780267+00	29076e292e3b41b3851e3ada04b59dea	8ac7a4a29dde1d8f019de2a44f3440ee	000.100.110	Request successfully processed in 'Merchant in Integrator Test Mode'
5bcd3557-8c5c-4b13-88ac-7c1665ff2311	570bdc3a-f681-4c24-b8ba-69cf5e495b34	\N	\N	PEACH	897000	USD	SUCCEEDED	0	\N	2026-05-01 08:26:30.718541+00	\N	\N	2026-05-01 08:25:59.920878+00	2026-05-01 08:26:30.725979+00	e48f2901b551408f899237dffb05b829	8ac7a49f9de0671d019de2a56b625418	000.100.110	Request successfully processed in 'Merchant in Integrator Test Mode'
eaacd127-a145-471e-90b3-7fce1f5e913f	6f0cdca2-17c6-4878-b567-998c754c552e	\N	\N	PEACH	897000	USD	SUCCEEDED	0	\N	2026-05-01 09:51:06.856041+00	\N	\N	2026-05-01 09:50:13.454094+00	2026-05-01 09:51:06.862607+00	bfc091e76d284717af9a04dabe534e02	8ac7a4a29dde1d8f019de2f2decc19c4	000.100.110	Request successfully processed in 'Merchant in Integrator Test Mode'
\.


--
-- Data for Name: product_sessions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.product_sessions (id, product_type, product_id, label, price_adult_cents, price_child_cents, price_infant_cents, created_at) FROM stdin;
\.


--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.refresh_tokens (id, user_id, token_hash, expires_at, revoked_at, created_at) FROM stdin;
9b1401b5-87cd-40a2-b17e-592b843fdfb4	00000000-0000-0000-0000-000000000001	JY+2JUWV+tvH/maZUerq4Bum7DK3cLVcbsgVoECKd78=	2026-05-27 07:24:02.78635+00	\N	2026-04-27 07:24:02.786361+00
ea6fd38e-c13a-4030-8ea2-0dffe39ac19f	00000000-0000-0000-0000-000000000001	RahOTuaw5iVoU8O+Af1XTYU+7PHWjpw+1FbCRFdviNk=	2026-05-27 07:24:19.02747+00	2026-04-27 07:45:30.932157+00	2026-04-27 07:24:19.027472+00
3feaf23d-0d80-4c13-a57b-55344d0fc9cd	00000000-0000-0000-0000-000000000005	vJMa3SZt4g1MrJ51R5Dg0q7zUfl1nUSJtD5MxinE2VE=	2026-05-27 07:27:18.241012+00	2026-04-27 08:38:51.99633+00	2026-04-27 07:27:18.241014+00
0923443d-0a1e-49b9-9308-e3493a1a385a	00000000-0000-0000-0000-000000000001	ZaPAY16ysuIjThajmjjt5OPgmOonsOFImSIb5zdct4o=	2026-05-27 08:39:30.715727+00	2026-04-27 12:31:17.202526+00	2026-04-27 08:39:30.71573+00
0e655c1e-f051-45aa-960f-5dba8d0ab46e	2fec982b-1835-47d2-a843-03110112af50	mxevF0JqvAf2P57+0Tl8W2OSq2cMZ6W3q2PznBLyn8g=	2026-05-27 12:31:50.780564+00	2026-04-27 13:22:08.730583+00	2026-04-27 12:31:50.780568+00
ccca13a3-f466-4d9e-b568-b2901ce48316	00000000-0000-0000-0000-000000000001	xEH6fbB7WVLLcFoZVXtymM0X2rBMxnmc7csqkDMwabc=	2026-05-27 13:22:47.83502+00	2026-04-28 04:44:11.169005+00	2026-04-27 13:22:47.835023+00
1c3bafe2-954b-489a-8e3d-4f5170ccfe5f	00000000-0000-0000-0000-000000000001	e5ePG+mmoIRwKqmjlzFUcDgH4lnB2tmwHQq34iPa2hE=	2026-05-28 04:44:14.016444+00	2026-04-28 04:44:31.493622+00	2026-04-28 04:44:14.016446+00
d3969a3b-1d3c-4bde-b591-46cd345245ae	00000000-0000-0000-0000-000000000001	pLX/dcUOjrc+Bh/w+7gO40+iNLN6RnK2hMVXADU/WQs=	2026-05-28 04:45:49.40519+00	2026-04-28 04:45:54.178634+00	2026-04-28 04:45:49.405193+00
c93dd955-6bbf-4346-8800-0b4fdef148c1	00000000-0000-0000-0000-000000000001	DmF+3m/T2x6EGT/9VJtnfFlewR/y+u/xSaAy7LI5ZiM=	2026-05-27 12:31:26.850345+00	2026-04-28 04:48:50.904151+00	2026-04-27 12:31:26.850355+00
747723fb-7e36-47fd-ac5d-a263b249e575	2fec982b-1835-47d2-a843-03110112af50	3Tk6QnDcw4jshWcf9VHezrP+7ULNUL+PIIEXzbLNnZM=	2026-05-28 04:46:10.266026+00	2026-04-28 06:28:12.819639+00	2026-04-28 04:46:10.266029+00
8ed20a36-63b6-498f-af3a-f33c5953e91f	2fec982b-1835-47d2-a843-03110112af50	WW4yZZGdmb8aEtw2hopUu/wMYUUr7ZMylWYk36Z+Ho0=	2026-05-28 05:15:22.780507+00	2026-04-28 06:30:08.190973+00	2026-04-28 05:15:22.78051+00
c2fb8cd5-3009-4091-b82f-14ae789f8363	00000000-0000-0000-0000-000000000001	9QqdLQ8Me3spUAnrVe7Zx7oe5N+GOs6Ki585gPGHU90=	2026-05-28 04:49:41.392903+00	2026-04-28 06:49:48.781615+00	2026-04-28 04:49:41.392908+00
170df1e4-cc07-4019-9948-30febeb46864	00000000-0000-0000-0000-000000000001	qDa/8JX5bGTym//0AcnF4jk/PCQ/wWtXgbvPIWMaf3Q=	2026-05-28 06:30:57.129233+00	2026-04-28 07:22:50.058366+00	2026-04-28 06:30:57.129236+00
554ebff5-0995-46c2-8760-e65c81bcfdb3	2fec982b-1835-47d2-a843-03110112af50	EWhC5QQ/41YpuXm5aJAXiyRBFP4xCNpDl214ly15PDc=	2026-05-28 06:50:02.133595+00	2026-04-28 07:25:20.431863+00	2026-04-28 06:50:02.133598+00
ac3d5033-586f-4821-9c71-d9d0f9ea9518	2fec982b-1835-47d2-a843-03110112af50	/KJvFzyZe437KZK7y2UwYbkGvhMbxKiGQlr9sX/cyDg=	2026-05-28 07:23:12.179051+00	2026-04-28 08:51:20.185556+00	2026-04-28 07:23:12.179053+00
c4a1ad4e-3e56-44ff-9e99-bfeef0fee35f	00000000-0000-0000-0000-000000000001	+xlX5MgO+qTuWHeMvwPdSNrGnwiBBoavYMO1n94fJPU=	2026-05-28 09:59:46.393629+00	\N	2026-04-28 09:59:46.393632+00
3eba2654-c129-4a92-9809-6fbd3194c487	00000000-0000-0000-0000-000000000001	LBCZSKHRoe7Sji7weG3H4jE0AXBYGpaDZn4mQIUcZsE=	2026-05-28 10:01:32.165631+00	\N	2026-04-28 10:01:32.165634+00
887560b7-7d82-427b-b9c6-bdda65889b4c	00000000-0000-0000-0000-000000000001	TPo1b4nGGh6dH7j1cdUZuAIUXKSNWDu9CBX1jduBbvs=	2026-05-28 10:01:32.240082+00	\N	2026-04-28 10:01:32.240084+00
b6e0c30e-1eca-48ef-8898-c3bd7989841e	00000000-0000-0000-0000-000000000001	i+F9mla5caxJgIjO6cXcilAlipsSOrsfUThSnGKSQ9k=	2026-05-28 10:01:32.317305+00	\N	2026-04-28 10:01:32.317308+00
422fa9a1-3ac2-43a3-9783-4901f269ad2a	00000000-0000-0000-0000-000000000001	eYrFUWzU3Ej29C+qFO4GP1eN0HH/3SEF4rXmWk4deGA=	2026-05-28 10:02:23.726396+00	\N	2026-04-28 10:02:23.726398+00
4e3e0836-98da-4f1e-aa74-1981ee19a5c4	00000000-0000-0000-0000-000000000001	xSEkBVTV7ngKOQay1899C6m7zfz6PBXDVivyJkPZemU=	2026-05-28 10:04:32.302112+00	\N	2026-04-28 10:04:32.302115+00
de6aa9e2-8592-418f-bbab-66e8e7552bd9	00000000-0000-0000-0000-000000000001	riBcbYQ9dUr0WnNJ6dwJJQuODkxVwM3kaCH7UrKnU6k=	2026-05-28 07:25:43.369508+00	2026-04-28 10:12:23.389643+00	2026-04-28 07:25:43.369512+00
d377c126-ac1a-45a6-bdec-6a9b8924d3e2	00000000-0000-0000-0000-000000000001	0dKtioPwI7LPoF3n4U7CAVQ3e9n8PoYEns8SESqY/Qk=	2026-05-28 08:51:23.424845+00	2026-04-28 10:14:38.612241+00	2026-04-28 08:51:23.424848+00
ab67b634-c578-46a8-b746-e11ba7eadc81	00000000-0000-0000-0000-000000000005	Qn5WfTmlWnitGIt0tkueBK59QPalIFGrJ06hD5B+xuU=	2026-05-28 10:08:03.395661+00	2026-04-28 10:15:17.734588+00	2026-04-28 10:08:03.395663+00
d4761382-1929-4cdf-a674-4291469de791	2fec982b-1835-47d2-a843-03110112af50	bzhmZY9RVxWkxLCZ8fixOzPddrjLVHC0P2m8b8NT40g=	2026-05-28 06:28:29.51606+00	2026-04-28 10:17:19.395764+00	2026-04-28 06:28:29.516063+00
ed012742-2c08-4b20-a729-2d78e3154ad3	2fec982b-1835-47d2-a843-03110112af50	mMe06kgR7jT7ZLnw3yYCMQzKUODG6wMNPIFXJ2W09WU=	2026-05-28 10:17:19.40026+00	2026-04-28 10:41:56.277399+00	2026-04-28 10:17:19.40028+00
ca1659a0-6f0f-4d72-9257-71bacc393cbb	00000000-0000-0000-0000-000000000005	EzNHLwMbsmzIwqaCUMkl7SkuMthYjXG1jark259UEo0=	2026-05-28 10:29:26.87474+00	2026-04-28 10:54:41.3901+00	2026-04-28 10:29:26.874751+00
a6ca3c32-a41f-4577-a2d6-cff7ee4bd912	00000000-0000-0000-0000-000000000001	hgV8Jhxm3XJPT2Qhka6ObKLWFhjzmZDGJOyaCs8LH8I=	2026-05-28 10:54:49.868595+00	\N	2026-04-28 10:54:49.868598+00
dc0494b7-c55d-4b68-b158-c4662ad24b1c	2fec982b-1835-47d2-a843-03110112af50	V5yriOodyHHdSSUzvS0GZRL/n1E3cs8pnQYoVwwWfiM=	2026-05-28 10:41:56.286586+00	2026-04-28 10:59:47.948567+00	2026-04-28 10:41:56.286592+00
7283e86e-6fdc-4a11-8f61-3e4f44ff2e19	00000000-0000-0000-0000-000000000001	PrzwLxhdgm7WPSqgpG7IMY2NSsyg6ThBrBvUotz6w10=	2026-05-28 10:12:59.591668+00	2026-04-28 11:06:18.149478+00	2026-04-28 10:12:59.59167+00
e72aa8ee-43e9-4767-8cf1-781578b45ff5	00000000-0000-0000-0000-000000000001	kc464LqLuDhayY7F8u8bNP+N1UgAd5F2c2LOabkmims=	2026-05-28 11:14:17.874202+00	\N	2026-04-28 11:14:17.874205+00
68cbe898-aa47-4fc2-ae9b-be1dce8d0506	00000000-0000-0000-0000-000000000001	Tm4ASFauefH7LnxhhA8kRxMUCJDFVfAsnYAY3w+ZAKk=	2026-05-28 11:15:21.763223+00	\N	2026-04-28 11:15:21.763226+00
25e5f536-6cee-4787-b698-65d58b96512f	2fec982b-1835-47d2-a843-03110112af50	J6TZZb+y774Qzw+srJjZl4KBqQ8iTaOt1sY7Rj1UQSA=	2026-05-28 10:59:47.956199+00	2026-04-28 11:16:02.320239+00	2026-04-28 10:59:47.956201+00
3b855d1d-71be-4658-b6e6-deba72c4580c	00000000-0000-0000-0000-000000000001	E6VBPU5UoPcSyfIaQ75OMST4OaXnLY3f21eejIdumfo=	2026-05-28 10:14:38.614206+00	2026-04-28 11:20:46.886872+00	2026-04-28 10:14:38.61421+00
4effe5d3-5e19-4826-93ef-1e00826c735d	2fec982b-1835-47d2-a843-03110112af50	CvAk7di2O5KGCjFhzr5b/lidyqsS6B7Zy17FLky98Ec=	2026-05-28 11:16:02.323909+00	2026-04-28 11:40:49.160049+00	2026-04-28 11:16:02.323913+00
c962746b-db72-4422-ac97-c37e599ff04c	2fec982b-1835-47d2-a843-03110112af50	24k6lnEI9bXZAOpp+N3AHe2SGFvJgmYAVRL7fEzgVww=	2026-05-28 11:40:49.185163+00	2026-04-28 11:58:10.315372+00	2026-04-28 11:40:49.185166+00
c7aab50c-fd38-4dcf-a7d5-95ede96a4465	00000000-0000-0000-0000-000000000001	/2oAb0i/URME4xJg19Zfb8O3fYaPbt/bUPH4WUaEX0Q=	2026-05-28 12:16:28.697281+00	\N	2026-04-28 12:16:28.697284+00
65196aaf-0b05-426c-8db5-e91aba4fda5c	00000000-0000-0000-0000-000000000001	/66VGGgx43IPH1JVv4FBpNzorDPw6CyaHgEuElFWuYs=	2026-05-28 12:17:23.687463+00	\N	2026-04-28 12:17:23.687466+00
b39aa275-d704-4c5e-a35c-021c307aa6df	2fec982b-1835-47d2-a843-03110112af50	iePWJZM4Wvp7ZXQBziKQQgN7PxCVmQIWSdZWRhKWIis=	2026-05-28 11:58:10.331409+00	2026-04-29 05:51:50.862274+00	2026-04-28 11:58:10.331413+00
ad7b2f6d-1a49-49bc-aec1-79cf5a1dd628	00000000-0000-0000-0000-000000000001	2pOZNidKhejLdsp7+WPDmvQP9KklcYutR3wbiNcoigA=	2026-05-28 11:06:18.151895+00	2026-04-29 06:05:49.740444+00	2026-04-28 11:06:18.151897+00
09eb6625-b3b1-4246-98bb-94ee97967879	00000000-0000-0000-0000-000000000001	lJURdVY1gCQqeK/Y3f4Vo+NVuL0SkxtIHrQBnVXS7h8=	2026-05-29 06:05:49.745846+00	2026-04-29 06:06:06.326281+00	2026-04-29 06:05:49.745849+00
b746bf6a-a172-4e50-a89b-78178f3c80a2	2fec982b-1835-47d2-a843-03110112af50	0zbVpcBqYE2RKa/wgcZgo4fiBFzLbdw/syHi/xGDf1w=	2026-05-29 05:51:50.877245+00	2026-04-29 06:07:17.448808+00	2026-04-29 05:51:50.877254+00
c75d690e-55ce-4e18-9adc-bd568e50a790	00000000-0000-0000-0000-000000000001	yLIK80ebMA+vdc6d8kmP1U/SA/83aaG9xx+x7I41BM0=	2026-05-29 06:09:27.960969+00	\N	2026-04-29 06:09:27.960972+00
7b59cef9-0744-43e0-a5d5-66872a425bf0	00000000-0000-0000-0000-000000000001	F9P3BPFRYcTesCF9hOWyc35/6LU80Js5uu2EPEDvBn8=	2026-05-28 11:20:46.89557+00	2026-04-29 06:09:27.958609+00	2026-04-28 11:20:46.895584+00
f5fc3a20-b092-41f3-83bd-1a6e4f19f861	00000000-0000-0000-0000-000000000001	3pwgPh4p+5/+75dTKjmClxfAKe8wSDPsYTJp6jxMwjU=	2026-05-29 06:09:48.194996+00	\N	2026-04-29 06:09:48.194999+00
68fa07a1-de82-4d8c-960f-ebb3eccc4341	2fec982b-1835-47d2-a843-03110112af50	s7ywsmxjTjU8+sNURp9xRnzqgaB1tzuz5YmCkLdcrU8=	2026-05-29 06:07:17.4539+00	2026-04-29 06:23:10.298052+00	2026-04-29 06:07:17.453905+00
38dc4942-7bfd-4d0d-877a-daddc194de88	00000000-0000-0000-0000-000000000001	plqYjsqO5YDD+HYGqO0j2DN31AlsDRUOSQJcsvl1sdA=	2026-05-29 06:06:31.697068+00	2026-04-29 06:23:12.813056+00	2026-04-29 06:06:31.697071+00
73488fbc-2b3b-4f09-af92-c6354df41089	00000000-0000-0000-0000-000000000001	visq9WgzfIULkaiw80rw0l108rK4AF31/Z4L0V9GfGM=	2026-05-29 06:23:12.815104+00	2026-04-29 06:40:11.902876+00	2026-04-29 06:23:12.815108+00
572b5b64-f8eb-42f0-b974-f6456f8dfb03	2fec982b-1835-47d2-a843-03110112af50	fM2FNt4+QGqG0IUU8q03oFPUNvOqO86tvxvDNr5bS4U=	2026-05-29 06:23:10.303013+00	2026-04-29 07:40:23.494756+00	2026-04-29 06:23:10.30303+00
18d42795-b6a8-42da-91df-b491d09d3c5b	00000000-0000-0000-0000-000000000005	2BXQzKqjW1QR+S6WUX0qQOnH4hrwTQW89yygOlk8L6g=	2026-05-27 07:26:58.627896+00	2026-04-29 10:50:38.519498+00	2026-04-27 07:26:58.627899+00
2f048ed8-85ed-4dac-a943-a3560ca39cc7	25c70058-b889-4132-a38e-67de5bd60db1	j+Pt6O44Udzx7fG8YjMnF1S9uQSFdQRwfKTMmhkMN30=	2026-05-28 10:13:43.11662+00	2026-04-29 10:55:31.876366+00	2026-04-28 10:13:43.116622+00
75125589-87b1-4d3c-b552-6cd796375954	00000000-0000-0000-0000-000000000001	qaVndxYJwMkwyHHKtSvWsd99qnFdBDgZhIj7tZkOByM=	2026-05-29 06:28:05.60829+00	\N	2026-04-29 06:28:05.608293+00
b5763da4-1c99-4762-8e3b-ff5183526aa2	00000000-0000-0000-0000-000000000001	4rk/JIkJw67itXPzbEwi327xiTlRuujB6bzoYseEo8Y=	2026-05-29 06:29:08.585265+00	\N	2026-04-29 06:29:08.585268+00
6f5a050a-d33a-47c9-888c-bc2175813bbe	00000000-0000-0000-0000-000000000001	YA5Mf/mqyq2p6zn57URPLOMJfrHxWMbwzrPaDP50Geo=	2026-05-29 06:38:59.811344+00	\N	2026-04-29 06:38:59.811347+00
bf227b6f-d9c3-43ce-96c6-f1d72e479237	00000000-0000-0000-0000-000000000001	E50mwGzRyGlUkXSW6L0v3Ed3K8/hLXh7US5jxO61zjU=	2026-05-29 06:41:38.869817+00	\N	2026-04-29 06:41:38.869829+00
08459955-5ee7-4d24-bda0-b2b15595012c	00000000-0000-0000-0000-000000000001	e37JmheCF9dBG7YYyO6tsrK/EM9G+dEu0mrGdnJMAe8=	2026-05-29 06:41:54.356669+00	\N	2026-04-29 06:41:54.356672+00
be10408c-c918-44a7-80c1-cbecb4fa70bb	00000000-0000-0000-0000-000000000001	6n91NyXRvQrvrXq41gw0OIq6jTbpOrVmElZ2CoAX39I=	2026-05-29 07:38:33.386752+00	\N	2026-04-29 07:38:33.386755+00
e0e77087-7c7f-4aae-af89-c249ffbe81a4	00000000-0000-0000-0000-000000000001	pOWvQwvUOgayffFRblt56NwvWI3j0xZ3SXFaWGG5vfc=	2026-05-29 07:39:58.66814+00	\N	2026-04-29 07:39:58.668164+00
8aafa959-a5a0-4e6d-925c-f677ceadf473	00000000-0000-0000-0000-000000000001	RjX+B9wrhYvD+u+chIdk+Y7L5fpt/COaYSijgLQe9qI=	2026-05-29 06:40:11.912641+00	2026-04-29 07:40:22.221458+00	2026-04-29 06:40:11.912647+00
29e1d403-c5e3-4374-85d3-135b62abc9e1	2fec982b-1835-47d2-a843-03110112af50	fcubkrDz2aOTvKFP/yxczwEz9PX/Ym/dE527cAMAFdg=	2026-05-29 07:40:23.514104+00	2026-04-29 07:41:59.263805+00	2026-04-29 07:40:23.514109+00
11f9703c-aa54-4a4e-a3f3-2d6cec325dc3	00000000-0000-0000-0000-000000000001	3Zz4m51faBC6jbQiuAO+px0kMQFNlnvK6H2y247xNKA=	2026-05-29 07:51:39.614859+00	\N	2026-04-29 07:51:39.614872+00
192008e1-2e66-4914-8cd9-f6dcdaf63ca3	00000000-0000-0000-0000-000000000001	aMRxlbdncLRw1e9eMEqdMMclHUfaWJ71ZQ0dCfpWnR4=	2026-05-29 07:42:02.972885+00	2026-04-29 08:08:25.178615+00	2026-04-29 07:42:02.972888+00
a5e6a150-11d3-4d22-a157-fd3109320695	00000000-0000-0000-0000-000000000001	fZ0g+ftfYffmclkXcaYbnst/fANLGdfOxnOj31LiuSQ=	2026-05-29 08:10:15.356946+00	\N	2026-04-29 08:10:15.356948+00
9255e321-ce7b-463e-a188-95db3dedde1e	00000000-0000-0000-0000-000000000001	yW421sNnACjjZ3VpryEAVueGmlY29KKK/hUl2pB5W6s=	2026-05-29 08:08:25.181039+00	2026-04-29 09:00:20.577643+00	2026-04-29 08:08:25.181042+00
cde8ea2a-be51-45a7-803a-720eedc059c7	00000000-0000-0000-0000-000000000001	loltw72ceyJI3Oo9kdjZjLAfVshXcXZvQ5MgsgmzDlA=	2026-05-29 08:07:43.403454+00	2026-04-29 09:51:35.319458+00	2026-04-29 08:07:43.403457+00
24eede12-0d4e-4adf-8d35-2ce605653519	00000000-0000-0000-0000-000000000001	BrtmUR7Ftq4bIhgIrGkSAWaV5o+IK4WInhH72Z3rmnE=	2026-05-29 09:51:35.484468+00	\N	2026-04-29 09:51:35.48447+00
deef10d8-b149-45d3-a623-deb7be712840	00000000-0000-0000-0000-000000000001	euuergRE2Ba9+Sq+dV8kk3EqST6ULtRmVq4EJrk+UTM=	2026-05-29 09:00:20.586762+00	2026-04-29 09:51:35.47844+00	2026-04-29 09:00:20.586772+00
c93d8aff-ee31-40b6-b958-98c9a7bc4dc0	00000000-0000-0000-0000-000000000001	KdaHXn56nXn/ZNFHe0TXP4tPoPZRO0HbF384YTJ3Yjw=	2026-05-29 09:51:35.486351+00	2026-04-29 10:27:43.63758+00	2026-04-29 09:51:35.486353+00
fefd4b65-136f-4451-a556-55c3f55f367d	00000000-0000-0000-0000-000000000001	3oZjopvQ0I54KIEWM7UenNuf2XWHK9mYf73VFqA/vhs=	2026-05-29 10:27:43.646682+00	2026-04-29 10:36:45.135119+00	2026-04-29 10:27:43.646691+00
2a3434f5-96c2-4c65-808c-ebd4288ab4fa	00000000-0000-0000-0000-000000000001	njvmvgZ/fD0TsheBHMyzggY6x4vKQxHIuFahkPsiYHM=	2026-05-29 10:46:10.072231+00	\N	2026-04-29 10:46:10.07227+00
62ef7463-47a8-425f-9423-ca222059585c	00000000-0000-0000-0000-000000000001	9Ky1KZH9aIvk1MBHd+H0ALlgfoa1Dz+QMuQWbx2Q3ho=	2026-05-29 09:51:35.339234+00	2026-04-29 10:46:10.070149+00	2026-04-29 09:51:35.339241+00
5f819c85-cf80-4801-9180-394f2824ca82	00000000-0000-0000-0000-000000000001	k6XdOduPf0m/8rP2Wv5YfAshmEvII7ASZK6cJSCvXxQ=	2026-05-29 07:40:22.231765+00	2026-04-29 10:46:10.219071+00	2026-04-29 07:40:22.231768+00
3089cece-cee2-434b-a2c8-01d6feb797ce	00000000-0000-0000-0000-000000000005	gFT4bm5cpAmTykwrb4kIr2t36vd9xLEDLrLJKXec7Tk=	2026-05-28 09:36:19.816461+00	2026-04-29 10:50:38.519498+00	2026-04-28 09:36:19.816493+00
b9910db0-a8a2-4b0f-9635-9af6cacc97b5	00000000-0000-0000-0000-000000000005	TURDAehV8BIsYLI//KbBehOLzRnF0oyf1TU7c4AWGwQ=	2026-05-28 09:58:42.160857+00	2026-04-29 10:50:38.519498+00	2026-04-28 09:58:42.160871+00
49a37b18-b20b-4abd-b0d2-e3789c188b48	00000000-0000-0000-0000-000000000005	8SJOcTJ7kZU1kRAYQgWn4Prk4DjqnKEauYqubHWqlB8=	2026-05-28 09:58:59.829004+00	2026-04-29 10:50:38.519498+00	2026-04-28 09:58:59.829007+00
6124614e-85a1-4f7d-a320-87dfd012f7ab	00000000-0000-0000-0000-000000000005	rxE8aSw8houP32GBfJQOHZaLdCdxb5ctx92MoNqv+0s=	2026-05-28 10:02:23.666368+00	2026-04-29 10:50:38.519498+00	2026-04-28 10:02:23.666371+00
a0507fa0-7f1a-47bb-be7e-523c0a0173ee	00000000-0000-0000-0000-000000000005	AY0rlxaOK0s2orUtxYePoi5Ce63fRYkrq9Il7rwNlA4=	2026-05-28 10:15:17.738598+00	2026-04-29 10:50:38.519498+00	2026-04-28 10:15:17.7386+00
fa3bdfa6-df64-44c7-9583-55c8f91a2697	00000000-0000-0000-0000-000000000005	T0saXc/5+U/dLSi4CJhjJTw3U17R7dpUVfHMK3WMLCY=	2026-05-28 10:54:41.4215+00	2026-04-29 10:50:38.519498+00	2026-04-28 10:54:41.421514+00
a0d6525f-a3ec-44a8-825c-33dcc7772eac	00000000-0000-0000-0000-000000000005	KfpAhXggXFbvjceyziBGlqEhAvIOxcIp546sQoc4qss=	2026-05-28 10:55:33.898198+00	2026-04-29 10:50:38.519498+00	2026-04-28 10:55:33.8982+00
f7e23f7d-39f3-4216-ab29-4886f99a42f6	00000000-0000-0000-0000-000000000005	LknaRWNjvJmCVVw4u/6Iewn2z17gWPY2x+TVywTriv4=	2026-05-28 10:58:13.298456+00	2026-04-29 10:50:38.519498+00	2026-04-28 10:58:13.298469+00
d475da46-a274-442a-8451-f029c240bc1e	00000000-0000-0000-0000-000000000005	bQoODRFRhXLK//ImjkDPjHIBOxjcoA9oxKKgyTHWFt8=	2026-05-28 11:15:40.312206+00	2026-04-29 10:50:38.519498+00	2026-04-28 11:15:40.312209+00
71b88aa1-1c4d-4bec-ba12-d884ccf7a123	00000000-0000-0000-0000-000000000005	ZmKxiQZu6V/8OzcmiLXW/wAKmbACMBkaobDH89hy0yM=	2026-05-28 12:15:07.216259+00	2026-04-29 10:50:38.519498+00	2026-04-28 12:15:07.216262+00
339994dc-9455-4b76-96d6-9c2f1c87db51	00000000-0000-0000-0000-000000000005	DZD0wppiv8kBmWqA7vKFuT80kdz/9PpmUpc6BSvRVHw=	2026-05-29 08:06:10.336802+00	2026-04-29 10:50:38.519498+00	2026-04-29 08:06:10.336804+00
c3523c6f-0b38-4230-aaf8-fc05bbda3a5e	00000000-0000-0000-0000-000000000005	WC6z7s/WqkxePDr1KwPdV98WVa3YXAM/51MhEgN9n8U=	2026-05-29 10:50:39.363093+00	2026-04-29 10:50:41.8807+00	2026-04-29 10:50:39.363099+00
6f493124-74f7-4a14-8ed4-56d1f93c635b	00000000-0000-0000-0000-000000000005	PfLZjqCM9LIzVK1Jy5lFzm/hcKLt4noOpgmM9GSKc4Y=	2026-05-29 10:50:41.981776+00	\N	2026-04-29 10:50:41.981778+00
c099e32a-0be8-4a39-b9f9-bc3e5a2cb529	25c70058-b889-4132-a38e-67de5bd60db1	yHKvUSBs5nzz2lGwa/2nGIpA7A8iWFMhxEI8yn0FQ8U=	2026-05-29 10:55:54.631081+00	2026-04-29 10:56:56.365069+00	2026-04-29 10:55:54.631092+00
30c755ad-7caf-428e-9c0b-735b16101315	00000000-0000-0000-0000-000000000001	rDVwsfEeQ8iK1VfnrEaqlXxJH3ZsZ3LG9ftdZJwWIIY=	2026-05-29 10:46:10.224207+00	2026-04-30 06:04:12.042599+00	2026-04-29 10:46:10.22421+00
fd4fd10f-5beb-4447-813e-270f9870f095	00000000-0000-0000-0000-000000000001	73uRm9XgUDpb7JBqVCHHCTRwsrg86fYGv/AG8RJvjD8=	2026-05-30 06:44:01.058323+00	\N	2026-04-30 06:44:01.058325+00
12044988-f0b5-4bd2-8bd3-45cf3444f80a	2fec982b-1835-47d2-a843-03110112af50	WJQQ81gvrGxSkLCijNvKJc5WONEzeeRQpGVY9bjZnmM=	2026-05-30 06:44:33.243721+00	2026-04-30 06:44:33.27496+00	2026-04-30 06:44:33.243724+00
6a94eda4-3ea4-49b9-b093-0560aecfa265	00000000-0000-0000-0000-000000000001	2d+EHdj3YGFXXmvH8jDOVfpc8KuPQ0la/NpE1HJOat4=	2026-05-30 06:44:33.39957+00	\N	2026-04-30 06:44:33.399572+00
387a6ecf-a6ba-4f89-be2f-9b7efd85335d	2fec982b-1835-47d2-a843-03110112af50	JP4SDhKuobi62aN4l5gQFzqBRZNXEAC265i0JjyS8yE=	2026-05-30 06:44:33.279535+00	2026-04-30 06:44:39.999435+00	2026-04-30 06:44:33.279538+00
a2f10636-fdc4-47a9-a3a0-0c9ee1522092	2fec982b-1835-47d2-a843-03110112af50	4xJ06/xboCL+Rv/pDyChkIBIOJhJ1qcnfwI3CYtrMRY=	2026-05-30 06:42:24.097465+00	2026-04-30 06:44:39.999435+00	2026-04-30 06:42:24.097476+00
809c381d-ca9f-4c90-ae08-bfcb53c78657	2fec982b-1835-47d2-a843-03110112af50	kFu1gKn/UBFIJHBdYWdZm0j54F5u5/DLhBeNjPM54t4=	2026-05-30 06:42:59.395733+00	2026-04-30 06:44:39.999435+00	2026-04-30 06:42:59.395737+00
cf0e6e04-eaec-43db-b83a-428797f65e5b	2fec982b-1835-47d2-a843-03110112af50	VV8cml8IJc6x0jKjRUgr8PbIVTlEHkLsxJwOpPnG1/w=	2026-05-30 06:44:00.890415+00	2026-04-30 06:44:39.999435+00	2026-04-30 06:44:00.890417+00
bc0899a6-dfe6-4310-9bd7-614533edacad	2fec982b-1835-47d2-a843-03110112af50	yZKH8N8SLrv2twGip1+0h5gB929EmUd3EKQmew8c/A0=	2026-05-30 06:44:09.069128+00	2026-04-30 06:44:39.999435+00	2026-04-30 06:44:09.069142+00
19abf12b-901c-41aa-b9c4-08816a76f2a5	2fec982b-1835-47d2-a843-03110112af50	TG9Cn03YrzMNjsFWK4fAvezJdM5yGOtE2frZl8aUBg4=	2026-05-30 06:44:40.10475+00	\N	2026-04-30 06:44:40.104753+00
871e9759-68de-4e0d-a682-872d46550098	00000000-0000-0000-0000-000000000001	nzuxI8vfacJql7qI1Qs7DanD3eQIk7rO9wP50PEVJmE=	2026-05-30 06:50:02.140142+00	\N	2026-04-30 06:50:02.140145+00
2c251d51-fc11-4c2f-8732-977fd0b9e75d	00000000-0000-0000-0000-000000000001	tV2MzNkn4krWvAYI1l2hPEKr8DX+l8w0mKXjf4asF0I=	2026-05-30 06:04:12.078619+00	2026-04-30 07:50:18.860812+00	2026-04-30 06:04:12.078633+00
aca26884-f450-4546-b8b4-24cc7130e0d9	00000000-0000-0000-0000-000000000001	jxoJ/xC/Yp1hMdmfeaP5ODKsDYygA1HMJ67JnnaHBAo=	2026-05-30 07:47:21.770411+00	\N	2026-04-30 07:47:21.770414+00
a60a4469-cc69-49d5-98c6-0a12d8165300	00000000-0000-0000-0000-000000000001	XYiKdnPFLMB/MLVsvjUPEfR+3oWKzmT1VNXVNoQNPFU=	2026-05-30 07:50:18.86304+00	\N	2026-04-30 07:50:18.863044+00
ce0ee369-426a-4440-960e-17a576ce7dc3	00000000-0000-0000-0000-000000000001	q/jSUCsokykCI5+tIJo79HuQZ8SdsZg6k5t1nkxEW04=	2026-05-30 07:50:14.684581+00	2026-04-30 07:53:38.08762+00	2026-04-30 07:50:14.684584+00
d89bf649-41d5-46ff-ae0f-1e7d2ee265f7	25c70058-b889-4132-a38e-67de5bd60db1	EM3ne8utacDBMxCRxbYA4HduioVAeqsXbdukA8bLAQQ=	2026-05-30 08:24:17.830884+00	\N	2026-04-30 08:24:17.830888+00
0f84e231-7205-4f20-a837-68ea9185ac79	25c70058-b889-4132-a38e-67de5bd60db1	qD6j4r7waaYQyKzye9nKyXvHPC5gyPx/AsAAuo70ssk=	2026-05-30 07:54:29.353703+00	2026-04-30 08:24:17.827284+00	2026-04-30 07:54:29.353706+00
ab6b8ced-b01a-4ca2-bdb6-b4c6b8987490	00000000-0000-0000-0000-000000000001	6ZMY3RzVA9C1D4EWsUHfy+Haidvi4HpDySsimMDJKp4=	2026-05-30 08:32:32.665962+00	2026-04-30 08:33:01.790238+00	2026-04-30 08:32:32.665965+00
0df17de4-c624-4952-a9fb-f95ed562e26e	00000000-0000-0000-0000-000000000001	REgdo+EtzD1lPThWA9xJtmtaxN48buhAYZeYAI7r/os=	2026-05-30 08:33:01.791327+00	2026-04-30 08:33:11.090241+00	2026-04-30 08:33:01.79133+00
7c860a37-77f6-454e-b29b-4fb594cdb7b0	00000000-0000-0000-0000-000000000001	MIeBG1Q6shgWUT2tryJP9CCMeABdvDuGjgmnqzq0kUE=	2026-05-30 08:33:11.110046+00	2026-04-30 08:33:12.421033+00	2026-04-30 08:33:11.110055+00
019477b8-d2fb-4742-bd79-af5974b217a9	00000000-0000-0000-0000-000000000001	DiY0Zyymnu+UOQOBXa4MC/EjwWUIvqbmQyuy7rr/tls=	2026-05-30 08:33:12.422565+00	2026-04-30 08:36:05.192767+00	2026-04-30 08:33:12.422568+00
77c186f2-aecc-4135-b448-4189e55f30bc	25c70058-b889-4132-a38e-67de5bd60db1	/gi5p0QzLfVRUwkUmCgOAHWwVJfUjtup3My6EfzujHs=	2026-05-30 08:24:17.830884+00	2026-04-30 08:36:56.33009+00	2026-04-30 08:24:17.830888+00
3fde0620-cb2c-42ec-ba6d-2f15c1d6a54a	00000000-0000-0000-0000-000000000001	Te+JMwr/djywu/mHn2bAGGAuhi2alSwCqgKyXrdYnFg=	2026-05-30 08:37:02.305691+00	2026-04-30 08:40:45.430264+00	2026-04-30 08:37:02.305693+00
01001b91-e7e7-421e-befc-230e249b0f7d	00000000-0000-0000-0000-000000000001	heCuUjDoqhTNq2xCcEO0VFSSIQ5dMJi9rDmR2rs1YMg=	2026-05-30 08:42:41.543776+00	2026-04-30 09:15:02.66966+00	2026-04-30 08:42:41.54378+00
2e4c9fd2-0920-4487-a5c2-9548ffc4e6f7	00000000-0000-0000-0000-000000000001	5joWRFgsOjbicTejOYTffmTb9znKREwlkk9yRXwTsxg=	2026-05-30 09:15:02.67348+00	2026-04-30 09:30:11.664155+00	2026-04-30 09:15:02.673536+00
02f5e2ee-2095-4d70-b12f-c820897b25f1	c4d61168-bb5d-482e-a556-c63afda6103b	na089+O349egm+Nr+dXnyfsjpxYJz0zgmWjCqhLan4s=	2026-05-30 09:15:43.261241+00	2026-04-30 09:31:05.914517+00	2026-04-30 09:15:43.261244+00
f0285a40-d4bf-461a-8e47-c4c58f75c3ad	00000000-0000-0000-0000-000000000001	AqW/ysxR09MESsKiT4SwP3dw5cDIFxU7ZKidTquoStA=	2026-05-30 09:29:21.208391+00	\N	2026-04-30 09:29:21.208402+00
17c306ef-32ef-42a8-8022-bd406353ae64	00000000-0000-0000-0000-000000000001	R8Ox5K8eYILYLyVkudGD8+kP+2N5g8PHk8LLMZLlgvE=	2026-05-30 09:29:30.159431+00	\N	2026-04-30 09:29:30.159434+00
7cf9553a-d23e-4292-852c-82d3d13d3eea	c4d61168-bb5d-482e-a556-c63afda6103b	bd1MCWtZQ2VA8ijZ6ld1m5uono8io84umOdzbVlF6D0=	2026-05-30 09:31:05.92095+00	2026-04-30 09:56:25.933215+00	2026-04-30 09:31:05.920953+00
416f4f0d-67ec-49de-b7c0-62ee8bf9d774	00000000-0000-0000-0000-000000000001	fl4o5ZZBLFh6zsLGBj6Tl5gMRBQ2rRiUBHP1pz5QFWM=	2026-05-30 09:30:11.668055+00	2026-04-30 09:56:28.025348+00	2026-04-30 09:30:11.668058+00
b14c90a6-bb5e-487f-b0da-abff43f4c49d	c4d61168-bb5d-482e-a556-c63afda6103b	5yu5cvKmm6CmdCRwHkStEJU8bm0QTM1DIauzF5Johvc=	2026-05-30 09:56:25.948999+00	2026-04-30 10:18:01.157147+00	2026-04-30 09:56:25.949007+00
7f25ba77-70e5-4be1-a412-d3cd573e51fa	00000000-0000-0000-0000-000000000001	3t6EjOREiyDL/2Wrr8uXekgllkS012jlDJ+dZdQo7C4=	2026-05-30 09:56:28.028643+00	2026-04-30 10:18:05.388742+00	2026-04-30 09:56:28.028648+00
6578582c-fc37-4abc-b130-a335d62aed16	00000000-0000-0000-0000-000000000001	g1hvjpp5nSbwuiBLVCx0t/yufka06KtLIZKcXbHJslI=	2026-05-30 10:26:26.725615+00	\N	2026-04-30 10:26:26.725627+00
d3df3055-9f3f-48d9-adc3-70a80b31138e	c4d61168-bb5d-482e-a556-c63afda6103b	BJ0Tdqta5OQXtDKGV3YcbtQZDkL0NCiqeNXejbJIpZ8=	2026-05-30 10:18:01.175777+00	2026-04-30 10:33:27.096484+00	2026-04-30 10:18:01.175787+00
339be030-77a2-4aef-be4b-b151ce8474cb	00000000-0000-0000-0000-000000000001	BLtrUYg3fqf0NXmbs7/4O4OM9Y2K+cGCY3gWtr66wrY=	2026-05-30 10:18:05.390177+00	2026-04-30 10:36:51.675624+00	2026-04-30 10:18:05.390179+00
71b2c1a6-445e-4335-862b-9400fcf22f58	c4d61168-bb5d-482e-a556-c63afda6103b	a1xxxzfQLoXbUYOwUbxuFBVQr4Zq/khTLnF7AGHkECg=	2026-05-30 10:59:40.769526+00	\N	2026-04-30 10:59:40.769539+00
9bac8f7f-c52e-4489-8896-b71e5ebca642	c4d61168-bb5d-482e-a556-c63afda6103b	hpNhUJJZzRYPQhgh0K1rYmyRaNZ/2nve4FM74HWzeqQ=	2026-05-30 10:59:40.769543+00	\N	2026-04-30 10:59:40.769546+00
c4dfb874-fffd-424b-92ee-eb1da1ef4166	c4d61168-bb5d-482e-a556-c63afda6103b	0PBlBtn6mWXSkC2NcelCQ7U9pcz1XWYvOkVcXI4pdQY=	2026-05-31 07:43:43.831736+00	2026-05-01 08:01:04.403428+00	2026-05-01 07:43:43.831746+00
88c0411b-fd37-4669-b00d-2066401ac3a2	c4d61168-bb5d-482e-a556-c63afda6103b	kDiZmW7bwfuykGLls15xB1G9b014VOKcnwKFuzlB+Ac=	2026-05-30 10:33:27.106208+00	2026-04-30 10:59:40.747555+00	2026-04-30 10:33:27.10621+00
dbf38c6b-689f-4d4f-b10e-7cc86d9437cd	00000000-0000-0000-0000-000000000001	Ig8/s+L58yURIwXFgdLditzxH/kz0z4yjXikHjkC9t0=	2026-05-30 12:01:08.871939+00	\N	2026-04-30 12:01:08.871995+00
bd5510bc-7ed4-4e0e-94fe-d8aa82daf5e1	00000000-0000-0000-0000-000000000001	QmHDODdSuc0om9YLyG4Z1XMXkfI9ivG08nTySsedrx4=	2026-05-30 10:36:51.688619+00	2026-04-30 12:01:08.809999+00	2026-04-30 10:36:51.688631+00
e6ce1fcc-6017-4b74-b8e5-194438d87134	c4d61168-bb5d-482e-a556-c63afda6103b	Zk4EiqlBJX5ssNX7RKRcxcMfYLvA5zjGhLjvgz9BXCQ=	2026-05-30 12:01:09.063008+00	\N	2026-04-30 12:01:09.063013+00
d59508a9-3744-42d2-92da-2c0c4dd76732	c4d61168-bb5d-482e-a556-c63afda6103b	Og4bgkNN7AMhfwLQ8q3a4p+Q97h4OsQ1Wrvzx2Wk+D0=	2026-05-30 10:59:40.769525+00	2026-04-30 12:01:09.035116+00	2026-04-30 10:59:40.769538+00
6ec37069-cbdb-4eba-9e6c-c74840b8f762	c4d61168-bb5d-482e-a556-c63afda6103b	BBuKrbC9vk/qZX0S18Y+sdduIKwuU4ann6TKyB0baGI=	2026-05-30 12:24:04.991617+00	\N	2026-04-30 12:24:04.99162+00
59a9e88a-ecd8-4040-baff-f13c6aedd17f	c4d61168-bb5d-482e-a556-c63afda6103b	yTfVAfXwmyB2PYq6MuZ0qvBiy4uIhyS95ZrVG9WBKec=	2026-05-31 08:01:04.421839+00	2026-05-01 08:17:23.108353+00	2026-05-01 08:01:04.421851+00
2d01d0d1-6d3a-4d6e-a16d-8707b0568614	c4d61168-bb5d-482e-a556-c63afda6103b	NNaoib1PM9uVtWa73tjocjEm0ICNZYDy6z0u2yWpLF8=	2026-05-30 12:01:09.076087+00	2026-04-30 12:24:04.983822+00	2026-04-30 12:01:09.076089+00
31686f1e-06da-4bd5-9ac5-3a5bc4975231	c4d61168-bb5d-482e-a556-c63afda6103b	qmE1p3aIdaFFeT12GYZrpvpB9RJonse20l4A3Ta7PgM=	2026-05-30 12:57:22.525207+00	\N	2026-04-30 12:57:22.525213+00
3343b84b-280e-4c83-b312-5479f7632a65	c4d61168-bb5d-482e-a556-c63afda6103b	j4XS7W5GTHBazYtTyoDx2m+Zsom4yUwgKCKIpFq3HH8=	2026-05-30 12:24:04.991781+00	2026-04-30 12:57:22.520453+00	2026-04-30 12:24:04.991785+00
070f774f-9bf1-45f3-a520-c0725343d5e7	c4d61168-bb5d-482e-a556-c63afda6103b	CLzU3OBl6Fe+i00icLcXpX4T9EWJ6i6t1lEOV3ny4tE=	2026-05-31 08:33:32.901083+00	\N	2026-05-01 08:33:32.901086+00
e4c490ba-0706-4f04-8db5-7c10f46617b6	c4d61168-bb5d-482e-a556-c63afda6103b	CU4kx3vveVLJeOImGVCk7+l/Dy91UHoezTVjw8AANGg=	2026-05-31 08:17:23.136207+00	2026-05-01 08:33:32.8914+00	2026-05-01 08:17:23.136217+00
68af4c10-342e-4106-a013-4b5d61fcb2e4	00000000-0000-0000-0000-000000000001	pOTQkWpbiuENEgHWgMkhBBpR0hJpp//VC2H6fThiGQ4=	2026-05-31 08:33:32.909205+00	\N	2026-05-01 08:33:32.909207+00
1bd2f2b9-a56a-4f02-976b-dde40562edee	00000000-0000-0000-0000-000000000001	oZjvw5AgF9iCQUbp7jhuFgc+lICRlw1oYgJeKOhFKiU=	2026-05-31 07:52:24.102833+00	2026-05-01 08:33:32.905997+00	2026-05-01 07:52:24.10285+00
57759427-c535-470d-978b-00aaa3a32354	00000000-0000-0000-0000-000000000001	HsxSv5uTRNnSCNGmAEC6rfN5UcTCdG88GjiD81OoybQ=	2026-05-31 05:42:40.074295+00	2026-05-01 06:00:49.93518+00	2026-05-01 05:42:40.074304+00
8bc004a1-4ed8-4bfb-9d82-cf9b76f372b4	c4d61168-bb5d-482e-a556-c63afda6103b	ws/LlXrKQTlXGkv05I7vAn3hI1Rawgj+lI1DdVvQwoY=	2026-05-31 05:42:56.427726+00	2026-05-01 06:00:49.93518+00	2026-05-01 05:42:56.427728+00
60013269-e5ba-47fe-802f-9555b5069fbc	c4d61168-bb5d-482e-a556-c63afda6103b	Pbw0ijle00ERhBh3GELMMIrh1cezGc1nIWwdEEaAOBg=	2026-05-31 06:00:49.95359+00	2026-05-01 06:56:45.839725+00	2026-05-01 06:00:49.953592+00
12d5d31d-8192-4254-8011-74c3e087645b	00000000-0000-0000-0000-000000000001	fdmytH+K6CPU8iz6clj/AWySTklkXdQnPBlquU47BjE=	2026-05-31 06:00:49.947375+00	2026-05-01 06:56:45.839723+00	2026-05-01 06:00:49.947387+00
dda4fdcc-8304-4e14-9e7e-044965637c04	00000000-0000-0000-0000-000000000001	h+9NnBdcDs486N35l/ooQNpPoYnnUD+tjguIdGwNrwg=	2026-05-31 06:56:45.842308+00	2026-05-01 07:15:16.823802+00	2026-05-01 06:56:45.84231+00
a5cb572a-a2f9-4cc2-be82-ca88fb9d2b34	c4d61168-bb5d-482e-a556-c63afda6103b	22Bq/zRX5A2gax6IkKiyhw0+FYIszEV4p7HwG770fTc=	2026-05-31 06:56:45.843965+00	2026-05-01 07:15:16.823802+00	2026-05-01 06:56:45.843967+00
32fd43fd-b52b-49fd-87f5-de151f9b61dc	c4d61168-bb5d-482e-a556-c63afda6103b	ekS2yv/BwLqI6BxCv5Tuqy/7ELwmB0NWxir32Lya6vM=	2026-05-31 07:15:16.835488+00	2026-05-01 07:43:43.812814+00	2026-05-01 07:15:16.835609+00
2046143a-c112-4af6-a877-f595280122dd	00000000-0000-0000-0000-000000000001	UI0a0AZUTWkKlYqZ1PetRT9sINw/l10WbWxtndXh/EQ=	2026-05-31 07:15:16.830947+00	2026-05-01 07:52:24.082135+00	2026-05-01 07:15:16.83095+00
f875185e-88d6-4ea4-a100-da24ea73368f	00000000-0000-0000-0000-000000000001	HkK4B21TJ3SejUmjpCL29/JnnveoaXBQY6WGxjVEmjk=	2026-05-31 09:48:24.246039+00	\N	2026-05-01 09:48:24.246041+00
b7c3733d-922d-4a9c-bfbe-2a97260da6dd	c4d61168-bb5d-482e-a556-c63afda6103b	0eA5oz/c3Hj7quEzxxpugGbGfRcyBFwgTfSlW2Ec/uI=	2026-05-31 09:48:42.0523+00	\N	2026-05-01 09:48:42.052302+00
\.


--
-- Data for Name: suppliers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.suppliers (id, name, contact_name, email, phone, country, address, notes, is_active, created_at, updated_at, deleted_at) FROM stdin;
10000000-0000-0000-0000-000000000001	Island Leisure Ltd	Raj Patel	raj@islandleisure.mu	+230 4651 2233	Mauritius	\N	\N	t	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N
10000000-0000-0000-0000-000000000002	Blue Lagoon Water Sports	Kevin Li	kevin@bluelagoon.mu	+230 4638 9900	Mauritius	\N	\N	t	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N
\.


--
-- Data for Name: system_settings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.system_settings (key, value, data_type, description, updated_by, updated_at) FROM stdin;
cutoff_time_local	18:00	STRING	Same-day booking cutoff (Mauritius local time)	\N	2026-04-27 07:16:00.692993+00
invoice_prefix	INV	STRING	Invoice number prefix	\N	2026-04-27 07:16:00.692993+00
vat_rate	15.00	DECIMAL	VAT percentage applied to all bookings	\N	2026-04-27 07:16:00.886051+00
same_day_cutoff_hour	18	INTEGER	Same-day booking cutoff hour (Mauritius, UTC+4)	\N	2026-04-27 07:16:00.886051+00
cart_ttl_days	7	INTEGER	Cart item TTL in days	\N	2026-04-27 07:16:00.886051+00
cancellation_free_hours	24	INTEGER	Hours before service: free cancellation	\N	2026-04-27 07:16:00.886051+00
cancellation_50pct_hours	12	INTEGER	Hours before service: 50% fee	\N	2026-04-27 07:16:00.886051+00
cancellation_75pct_hours	3	INTEGER	Hours before service: 75% fee	\N	2026-04-27 07:16:00.886051+00
cancellation_100pct_hours	2	INTEGER	Hours before service: 100% fee (no refund)	\N	2026-04-27 07:16:00.886051+00
head_office_address	Draper Avenue Quatre Bornes Mauritius	STRING	Head office address	\N	2026-04-27 07:16:00.886051+00
phone_1	+230 5285 0500	STRING	Primary contact phone	\N	2026-04-27 07:16:00.886051+00
phone_2	+230 4608423	STRING	Secondary contact phone	\N	2026-04-27 07:16:00.886051+00
email_general	info@ambianceholidays.com	STRING	General enquiries email	\N	2026-04-27 07:16:00.886051+00
email_reservations	reservation@ambianceholidays.com	STRING	Reservations email	\N	2026-04-27 07:16:00.886051+00
currency	MUR	STRING	Billing currency code	\N	2026-04-27 07:16:00.886051+00
currency_symbol	Rs	STRING	Currency symbol for display	\N	2026-04-27 07:16:00.886051+00
default_commission	10.00	DECIMAL	Default agent commission rate	\N	2026-04-27 07:16:00.886051+00
max_markup_percent	100.00	DECIMAL	Maximum allowed agent markup %	\N	2026-04-27 07:16:00.886051+00
\.


--
-- Data for Name: tour_itinerary_stops; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tour_itinerary_stops (id, tour_id, stop_time, title, description, sort_order) FROM stdin;
7854e3e0-67b8-4bb7-be77-1a2bd357ab25	6dd05f51-58da-49d5-83aa-5d2e45859b63	07:00	Hotel Pick-Up	Your driver meets you at reception for a comfortable transfer to the east coast.	0
05409279-ff66-4f1b-8b6b-ef029492845e	6dd05f51-58da-49d5-83aa-5d2e45859b63	08:30	Speedboat Departure	Board our 10-seat speedboat at Trou d'Eau Douce jetty for a 15-minute crossing.	1
158386f9-17f2-432e-addc-f34388637ef0	6dd05f51-58da-49d5-83aa-5d2e45859b63	09:00	Beach Time & Snorkeling	Free time on the pristine beach. Snorkeling equipment is available.	2
278aca6c-0c70-492b-9535-a2e0d1b20c54	6dd05f51-58da-49d5-83aa-5d2e45859b63	13:00	BBQ Seafood Lunch	Enjoy a lavish BBQ buffet with fresh-caught seafood, salads and tropical desserts.	3
abfdff1d-9732-4b2e-8c79-a727f64b4366	6dd05f51-58da-49d5-83aa-5d2e45859b63	15:30	Return Transfer	Speedboat back to Trou d'Eau Douce, then comfortable transfer to your hotel.	4
34a38040-2630-4f79-99a6-54913ff653e1	54d55904-cf37-46d6-bae0-0dba770a6d08	08:30	Cap Malheureux Chapel	Visit the iconic red-roofed Notre-Dame Auxiliatrice chapel with views over Coin de Mire island.	0
3d33e565-794c-4bec-b551-52903fa1ae78	54d55904-cf37-46d6-bae0-0dba770a6d08	10:00	Grand Baie Promenade	Explore the vibrant marina and local boutiques of Mauritius's most popular resort town.	1
e03e52d4-e93e-4a37-b7b4-ed326bd98485	54d55904-cf37-46d6-bae0-0dba770a6d08	12:30	Traditional Lunch	Lunch at a family-run restaurant serving authentic Mauritian cuisine.	2
69989640-14d9-4a39-81d3-cf54fb7f2e4e	54d55904-cf37-46d6-bae0-0dba770a6d08	14:00	Pamplemousses Botanical Garden	Stroll through one of the oldest botanical gardens in the Southern Hemisphere.	3
1ee18d6a-bdf0-4e5f-af97-ca16c09b708b	54d55904-cf37-46d6-bae0-0dba770a6d08	16:30	Hotel Drop-Off	Comfortable return transfer to your accommodation.	4
643e38e9-c7c4-4a71-826a-5ce23f114ef7	270611e9-5140-496a-b7cf-9b179a0e535f	07:30	Hotel Pick-Up	Early start for the drive south. Enjoy the changing landscape as the mountains come into view.	0
20ea9317-dc2f-44ee-986a-8188f4aeec03	270611e9-5140-496a-b7cf-9b179a0e535f	09:30	Chamarel Coloured Earths	Witness the extraordinary seven-coloured earth formation and the Chamarel Waterfall.	1
319ed6cb-a799-4516-a948-df672b5b5c71	270611e9-5140-496a-b7cf-9b179a0e535f	11:00	Black River Gorges Viewpoint	Panoramic views over Mauritius's largest national park from the dramatic gorge lookout.	2
9586ff15-8699-4f47-9486-7d95c580d400	270611e9-5140-496a-b7cf-9b179a0e535f	13:00	Local Lunch	Enjoy a traditional rougaille and dholl puri at a local family restaurant.	3
922bcdf3-eeb3-4774-9503-bd5e32b75e1f	270611e9-5140-496a-b7cf-9b179a0e535f	14:30	Le Morne Brabant	Visit the UNESCO World Heritage Site and hear the powerful story of the escaped slaves who sought refuge here.	4
4a288a52-80ec-4947-85d4-8657423b5a66	270611e9-5140-496a-b7cf-9b179a0e535f	16:30	Ganga Talao / Grand Bassin	The sacred crater lake pilgrimage site, surrounded by towering statues of Hindu deities.	5
\.


--
-- Data for Name: tour_pickup_zones; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tour_pickup_zones (id, tour_id, zone_name, extra_cents, pickup_time, sort_order, pickup_time_from, pickup_time_to) FROM stdin;
fc2bb61c-fa24-45f0-84d7-e8e14a10a6c6	6dd05f51-58da-49d5-83aa-5d2e45859b63	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
b7e90916-f1e4-46ad-8662-e20b50398c80	54d55904-cf37-46d6-bae0-0dba770a6d08	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
20eacc46-d9e0-43cf-bb9c-03491ceb130f	22d175ec-6bb9-45ae-9559-44b8cf1839c2	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
6e7fe8da-9fde-48ee-9a19-e64278f24ad0	270611e9-5140-496a-b7cf-9b179a0e535f	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
32e7e6ab-1b49-4995-9a4e-73e9b53a56d2	80444f52-4fc6-4222-88c0-0f59ec87c795	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
ed5e5335-eeba-4717-b9a0-a5feaa5f514f	c9d8a32e-df92-4d42-94e6-4822139be769	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
8eadf95b-0d58-4ec0-8daa-536d392a2385	bb779850-7054-4b43-ae3b-a7501c9fc940	Grand Baie / Pereybere	0	07:30:00	0	\N	\N
bbfa5014-87d2-4144-80e3-7400ffac3ad6	6dd05f51-58da-49d5-83aa-5d2e45859b63	Flic en Flac / Wolmar	2000	07:00:00	1	\N	\N
e9f3702a-23bb-499f-bcb7-f55cfc06377f	22d175ec-6bb9-45ae-9559-44b8cf1839c2	Flic en Flac / Wolmar	2000	07:00:00	1	\N	\N
0a182cbe-a496-46ac-8498-38431d84b8f2	270611e9-5140-496a-b7cf-9b179a0e535f	Flic en Flac / Wolmar	2000	07:00:00	1	\N	\N
06e713a2-8022-48ea-a17d-e3e103b85ffb	80444f52-4fc6-4222-88c0-0f59ec87c795	Flic en Flac / Wolmar	2000	07:00:00	1	\N	\N
18225442-c7a7-4c76-b4b5-5140df93a218	c9d8a32e-df92-4d42-94e6-4822139be769	Flic en Flac / Wolmar	2000	07:00:00	1	\N	\N
2c3b976f-daca-4af6-8504-2278187fa560	bb779850-7054-4b43-ae3b-a7501c9fc940	Flic en Flac / Wolmar	2000	07:00:00	1	\N	\N
5ceb70d4-ecc4-4a3a-9d32-4427d4b4ffe1	6dd05f51-58da-49d5-83aa-5d2e45859b63	Port Louis / Bagatelle	1500	07:15:00	2	\N	\N
79f44486-f0ad-43ad-aa6c-e6eee23da09c	54d55904-cf37-46d6-bae0-0dba770a6d08	Port Louis / Bagatelle	1500	07:15:00	2	\N	\N
9bd4278c-7b8b-439b-bd43-babdd74b22f6	22d175ec-6bb9-45ae-9559-44b8cf1839c2	Port Louis / Bagatelle	1500	07:15:00	2	\N	\N
2acea609-135e-492c-8b35-c9008c399f56	270611e9-5140-496a-b7cf-9b179a0e535f	Port Louis / Bagatelle	1500	07:15:00	2	\N	\N
87aebb3a-5c08-4654-8280-6387858262de	80444f52-4fc6-4222-88c0-0f59ec87c795	Port Louis / Bagatelle	1500	07:15:00	2	\N	\N
d33caba9-7b97-42fd-950c-e6f0563d134d	c9d8a32e-df92-4d42-94e6-4822139be769	Port Louis / Bagatelle	1500	07:15:00	2	\N	\N
\.


--
-- Data for Name: tours; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tours (id, supplier_id, title, slug, description, category, region, duration, duration_hours, adult_price_cents, child_price_cents, infant_price_cents, min_pax, max_pax, includes, excludes, important_notes, cover_image_url, gallery_urls, status, created_by_id, created_at, updated_at, deleted_at, availability_mode, theme) FROM stdin;
6dd05f51-58da-49d5-83aa-5d2e45859b63	\N	Île aux Cerfs Full Day Excursion	ile-aux-cerfs-full-day	Discover the jewel of the Indian Ocean — Île aux Cerfs. This uninhabited paradise island boasts powder-white beaches, turquoise lagoons and world-class water sports. Spend a full day basking, snorkelling and exploring at your leisure.	SEA	EAST	FULL_DAY	8.0	280000	175000	0	2	30	{"Return speedboat transfer","Snorkeling equipment","BBQ seafood lunch","Non-alcoholic beverages","Hotel pick-up & drop-off"}	{"Alcoholic drinks","Personal watersports (jet-ski, parasailing)","Travel insurance"}	{"Bring sunscreen and a hat","Hotel pick-up starts from 07:00","Tour operates daily subject to weather"}	/images/pexels-asadphoto-3319712.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	always	BEACH
54d55904-cf37-46d6-bae0-0dba770a6d08	\N	North Coast Heritage & Beaches	north-coast-heritage	Explore the charming north of Mauritius — from the iconic red-roofed chapel at Cap Malheureux overlooking Coin de Mire island, to the vibrant market town of Grand Baie and the historic colonial sugar estate at Mon Plaisir. A perfect blend of culture, history and stunning coastal scenery.	LAND	NORTH	FULL_DAY	7.0	195000	120000	0	2	20	{"Air-conditioned minivan","Professional bilingual guide","Entrance fees","Light lunch","Hotel transfers"}	{"Personal shopping",Gratuities}	{"Comfortable walking shoes recommended","Pick-up between 08:00–09:00"}	/images/pexels-vince-34732389.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	always	CULTURAL
22d175ec-6bb9-45ae-9559-44b8cf1839c2	\N	Sunset Dinner Cruise	sunset-dinner-cruise	Watch the sun melt into the Indian Ocean from the deck of our luxury catamaran. Sip cocktails as the sky turns shades of amber and rose, then enjoy a freshly prepared 3-course dinner under the stars. This is Mauritius at its most magical.	SEA	WEST	HALF_DAY	3.5	320000	200000	0	2	24	{"Welcome cocktail","3-course dinner","Open bar (wine, beer, soft drinks)","Live Sega music","Hotel transfers"}	{Gratuities,"Personal expenses"}	{"Departure 17:30, return ~21:00","Smart-casual dress code","Minimum age 5 years"}	/images/xavier-coiffic-yFSDYHAfhrI-unsplash.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	always	BEACH
270611e9-5140-496a-b7cf-9b179a0e535f	\N	South Island Safari	south-island-safari	Journey through the wild and dramatic south of Mauritius. Visit the sacred Ganga Talao crater lake, the stunning Chamarel Coloured Earths, Chamarel Waterfall, the Black River Gorges viewpoints, and the iconic Le Morne Brabant — a UNESCO World Heritage Site.	LAND	SOUTH	FULL_DAY	9.0	245000	155000	0	2	20	{"Air-conditioned 4×4 vehicle","Expert local guide","Entrance fees","Traditional Mauritian lunch","Hotel transfers"}	{"Rum distillery tasting (optional extra)",Gratuities}	{"Wear comfortable shoes for short walks","Bring a light jacket for the gorges","Pick-up from 07:30"}	/images/pexels-zakh-36731926.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	always	NATURE
80444f52-4fc6-4222-88c0-0f59ec87c795	\N	Snorkeling & Underwater World	snorkeling-underwater-world	Plunge into the warm, crystal-clear waters of the Indian Ocean and discover a dazzling world of coral gardens, tropical fish, sea turtles and rays. Our expert guides will take you to the best snorkeling spots in the Blue Bay Marine Park and Île aux Bénitiers.	SEA	SOUTH	HALF_DAY	4.0	180000	110000	0	2	16	{"Professional snorkeling guide","Full snorkeling equipment","Life vests","Light refreshments on board","Hotel transfers"}	{"Underwater camera hire",Towels}	{"Non-swimmers welcome with life vest","Minimum age 6 years","Wear biodegradable sunscreen only"}	/images/pexels-cemil-tuyloglu-3443668-28885219.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	always	SEA_ACTIVITIES
c9d8a32e-df92-4d42-94e6-4822139be769	\N	Watersports Adventure Day	watersports-adventure-day	Crave thrills on the water? This action-packed day bundles the best of Mauritius's adrenaline watersports — jet-skiing, parasailing, kite-surfing intro, glass-bottom boat ride, and more. Suitable for beginners and experienced adventurers alike.	SEA	NORTH	FULL_DAY	6.0	420000	280000	0	2	12	{"Jet-ski 30 mins","Parasailing flight","Glass-bottom boat tour","Kite-surf introduction (45 min)","BBQ lunch","Hotel transfers"}	{"Underwater scooter (optional +Rs 1500)","Travel insurance"}	{"Minimum age 12 years","Participants must be able to swim","Booking at least 24h in advance required"}	/images/pexels-ahmet-kadioglu-650478141-32552944.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	always	ADVENTURE
bb779850-7054-4b43-ae3b-a7501c9fc940	\N	Le Morne & Kite Beach Day	le-morne-kite-beach-day	Spend a glorious day at the foot of the majestic Le Morne Brabant mountain. Relax on one of Mauritius's most photographed beaches, watch world-class kite-surfers in the famous kite lagoon, and explore the UNESCO heritage site with a local guide.	LAND	SOUTH	FULL_DAY	8.0	220000	140000	0	2	18	{"Transport in air-conditioned vehicle","Local heritage guide","Kite Beach access","Picnic lunch","Hotel transfers"}	{"Kite-surfing lessons (book separately)","Personal expenses"}	{"Bring swimming gear","Best visited Oct–April for kite-surfers","Departs 08:00"}	/images/pexels-asadphoto-3320533.jpg	\N	ACTIVE	\N	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00	\N	on_request	BEACH
\.


--
-- Data for Name: transfer_pricing_tiers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transfer_pricing_tiers (id, label, min_km, max_km, price_cents, is_active, sort_order, created_at, updated_at, includes, excludes) FROM stdin;
4ef78519-5a10-461c-aa0a-25c86df0b5db	Short Distance (0 – 20 km)	0	20	2000	t	1	2026-04-27 11:32:45.388537+00	2026-04-27 11:32:45.388537+00	{}	{}
f0e98cfd-a3d0-44ba-a690-34b930544935	Long Distance (21 km+)	21	\N	10000	t	2	2026-04-27 11:32:45.388537+00	2026-04-27 11:32:45.388537+00	{}	{}
29ac6075-64b6-4e7b-bc68-71c0429fc753	QA Test Tier	0	50	15000	t	99	2026-04-30 09:29:21.272022+00	2026-04-30 09:29:21.273035+00	{"Meet & greet","Bottled water",Wi-Fi}	{Gratuity,"Personal expenses"}
\.


--
-- Data for Name: transfer_rates; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transfer_rates (id, route_id, rate_name, price_cents, created_at) FROM stdin;
\.


--
-- Data for Name: transfer_routes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transfer_routes (id, from_location, to_location, trip_type, car_category, base_price_cents, est_duration_mins, est_km, is_active, created_at, updated_at) FROM stdin;
62cd0fbf-2a9f-463f-a3b3-0c590e3ff8da	SSR Airport	Grand Baie	ROUND_TRIP	STANDARD	290000	55	55	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
82dfd163-590f-4099-978a-6e720d57ea9f	SSR Airport	Le Morne	ONE_WAY	STANDARD	210000	80	90	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
f2a65bf7-4548-46d2-bbde-4415a85da7b2	SSR Airport	Port Louis	ONE_WAY	STANDARD	120000	45	48	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
11c7d793-a7b1-407e-a9a2-4e9dfee1c357	SSR Airport	Grand Baie	ONE_WAY	LUXURY	300000	55	55	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
f1c53c5e-8029-4f9d-a008-aca041307d72	SSR Airport	Flic en Flac	ONE_WAY	STANDARD	180000	70	75	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
d47d4dba-4f87-49f5-bdb2-67568f6f04db	SSR Airport	Mahebourg	ONE_WAY	ECONOMY	90000	20	18	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
ba80e9e4-dc91-4cd0-bdb5-a7f6b251b5be	Grand Baie	SSR Airport	ONE_WAY	STANDARD	155000	50	55	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
9385c7e6-365f-43ed-b18a-f2cb59d6ba4f	SSR Airport	Port Louis	ONE_WAY	LUXURY	280000	45	48	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
f9679f64-e618-4fd4-8d34-e564bbfafce9	SSR Airport	Tamarin / Black River	ONE_WAY	STANDARD	190000	75	80	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
af76d0af-250b-4a84-90f7-041358ab5ef0	SSR Airport	Flic en Flac	ROUND_TRIP	STANDARD	340000	70	75	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
c17d3a4e-c5fb-4f00-a471-31b643825216	Port Louis	Grand Baie	ONE_WAY	ECONOMY	80000	35	28	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
311f8e99-1062-43f0-94e5-c27858882a12	SSR Airport	Grand Baie	ONE_WAY	ECONOMY	180000	55	55	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
b2ff2e3f-3d65-4eac-b851-7bf95b12fba5	Flic en Flac	Le Morne	ONE_WAY	ECONOMY	70000	30	25	t	2026-04-27 07:16:00.85958+00	2026-04-27 07:16:00.85958+00
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, email, password_hash, first_name, last_name, phone, whatsapp, role, is_active, email_verified, last_login_at, created_at, updated_at, deleted_at, verification_token_hash, verification_token_expires_at) FROM stdin;
00000000-0000-0000-0000-000000000002	ops@ambianceholidays.mu	$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$7DgD3bDKNcTZIPWKfXwzwtEOvCstsKDfdAjVCRrNTJI	Operations	Manager	+230 5285 0501	\N	ADMIN_OPS	t	t	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N
00000000-0000-0000-0000-000000000003	fleet@ambianceholidays.mu	$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$7DgD3bDKNcTZIPWKfXwzwtEOvCstsKDfdAjVCRrNTJI	Fleet	Manager	+230 5285 0502	\N	FLEET_MANAGER	t	t	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N
00000000-0000-0000-0000-000000000004	john.doe@example.com	$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$2FiEm/TKWSreWRRAmhOnCtOMUWyX6FalZeVMrfIqbig	John	Doe	+44 7700 900123	\N	GUEST	t	t	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N
00000000-0000-0000-0000-000000000006	sarah.martin@example.com	$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$2FiEm/TKWSreWRRAmhOnCtOMUWyX6FalZeVMrfIqbig	Sarah	Martin	+33 6 12 34 56 78	\N	GUEST	t	t	\N	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N
25c70058-b889-4132-a38e-67de5bd60db1	nagpal.gautam@orangemantra.in	$argon2id$v=19$m=65536,t=3,p=4$sqsBDu9sjsi90bnk7mEHvw$/RPPNN6Y+mhXAOb9HuaV4966CO+c0SS8pQfHyKHak6w	Gautam	Nagpal	\N	\N	B2B_AGENT	t	f	2026-04-30 07:54:29.302406+00	2026-04-27 07:22:44.920175+00	2026-04-30 06:37:03.737634+00	\N	glFZT1MBZtW5TBCEtJZOkLlFyAYjAM/NYC7uSiq3SUw=	2026-04-28 07:22:44.92582+00
00000000-0000-0000-0000-000000000005	agent@sunrisetravel.mu	$argon2id$v=19$m=65536,t=3,p=4$OceNXqVSZ8EpYKj9L0gZPA$OgCZlhTxkYMEElFSHJxFh50Sb8EQmyr4MTAG3Caa3MA	Priya	Sharma	+230 5712 3456	\N	B2B_AGENT	t	t	2026-04-29 10:50:41.939608+00	2026-04-27 07:18:19.663585+00	2026-04-29 10:50:41.923918+00	\N	\N	\N
69afc204-0bdc-4dff-a68b-7534bbdf70c6	nayaz@example.com	$argon2id$v=19$m=65536,t=3,p=4$9J1gDqSGL3OoAaieCZeNbQ$DixCwnFzlIrul15T6G45BAbJarndO29fvfcc5gO6vMs	Nayaz	Tollun	\N	\N	B2B_AGENT	t	f	\N	2026-04-30 08:03:20.920503+00	2026-04-30 08:03:30.841489+00	\N	I3hoHcwgLJ34Q4ags+UcUIXHmqvcLQdRoNtO0JHkP/o=	2026-05-01 08:03:20.925976+00
2fec982b-1835-47d2-a843-03110112af50	gautam.nagpal05@gmail.com	$argon2id$v=19$m=65536,t=3,p=4$YKZqH401GGxxOYaHd4w3+w$7obPMqiOdRMaViCxOy5kNpYwMYWtcXIsiORqUl+UGYs	Gautam	Nagppal	\N	\N	B2B_AGENT	t	f	2026-04-30 06:44:40.064077+00	2026-04-27 12:30:02.874333+00	2026-04-30 06:44:40.033384+00	\N	NC9wGwcZlXZQFYR6VdtXVS/0+gLc32NUNOF7FcSYGs0=	2026-04-28 12:30:02.885311+00
00000000-0000-0000-0000-000000000001	admin@ambianceholidays.mu	$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$7DgD3bDKNcTZIPWKfXwzwtEOvCstsKDfdAjVCRrNTJI	Super	Admin	+230 5285 0500	\N	SUPER_ADMIN	t	t	2026-05-01 09:48:23.457363+00	2026-04-27 07:18:19.663585+00	2026-04-27 07:18:19.663585+00	\N	\N	\N
c4d61168-bb5d-482e-a556-c63afda6103b	jkv@hshs.com	$argon2id$v=19$m=65536,t=3,p=4$3T5tlBw6hkC4rcAG6MTX5Q$xKMSMmbp5IJHcEwjIFTdZHd/FgmsclAPfW9v9SCZTL8	Anush	Raj	\N	\N	B2B_AGENT	t	f	2026-05-01 09:48:42.007721+00	2026-04-30 08:41:50.901674+00	2026-04-30 08:42:00.823072+00	\N	nMBY1XKv9ylLxs/4BQqnYxc+0bflbk5kMNkJhM+LOE8=	2026-05-01 08:41:50.903169+00
\.


--
-- Name: audit_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.audit_logs_id_seq', 1, false);


--
-- Name: activity_variants activity_variants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_variants
    ADD CONSTRAINT activity_variants_pkey PRIMARY KEY (id);


--
-- Name: agents agents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents
    ADD CONSTRAINT agents_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: booking_extras booking_extras_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_extras
    ADD CONSTRAINT booking_extras_pkey PRIMARY KEY (id);


--
-- Name: booking_items booking_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_items
    ADD CONSTRAINT booking_items_pkey PRIMARY KEY (id);


--
-- Name: bookings bookings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_pkey PRIMARY KEY (id);


--
-- Name: bookings bookings_reference_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_reference_key UNIQUE (reference);


--
-- Name: car_availability car_availability_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car_availability
    ADD CONSTRAINT car_availability_pkey PRIMARY KEY (id);


--
-- Name: car_extra_services car_extra_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car_extra_services
    ADD CONSTRAINT car_extra_services_pkey PRIMARY KEY (id);


--
-- Name: car_rates car_rates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car_rates
    ADD CONSTRAINT car_rates_pkey PRIMARY KEY (id);


--
-- Name: cars cars_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cars
    ADD CONSTRAINT cars_pkey PRIMARY KEY (id);


--
-- Name: cars cars_registration_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cars
    ADD CONSTRAINT cars_registration_no_key UNIQUE (registration_no);


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (id);


--
-- Name: day_trip_highlights day_trip_highlights_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trip_highlights
    ADD CONSTRAINT day_trip_highlights_pkey PRIMARY KEY (id);


--
-- Name: day_trip_itinerary_stops day_trip_itinerary_stops_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trip_itinerary_stops
    ADD CONSTRAINT day_trip_itinerary_stops_pkey PRIMARY KEY (id);


--
-- Name: day_trip_pickup_zones day_trip_pickup_zones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trip_pickup_zones
    ADD CONSTRAINT day_trip_pickup_zones_pkey PRIMARY KEY (id);


--
-- Name: day_trips day_trips_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trips
    ADD CONSTRAINT day_trips_pkey PRIMARY KEY (id);


--
-- Name: day_trips day_trips_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trips
    ADD CONSTRAINT day_trips_slug_key UNIQUE (slug);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: invoices invoices_invoice_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_invoice_number_key UNIQUE (invoice_number);


--
-- Name: invoices invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (id);


--
-- Name: leads leads_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leads
    ADD CONSTRAINT leads_pkey PRIMARY KEY (id);


--
-- Name: login_attempts login_attempts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login_attempts
    ADD CONSTRAINT login_attempts_pkey PRIMARY KEY (id);


--
-- Name: newsletter_subscribers newsletter_subscribers_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.newsletter_subscribers
    ADD CONSTRAINT newsletter_subscribers_email_key UNIQUE (email);


--
-- Name: newsletter_subscribers newsletter_subscribers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.newsletter_subscribers
    ADD CONSTRAINT newsletter_subscribers_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: payments payments_stripe_payment_intent_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_stripe_payment_intent_key UNIQUE (stripe_payment_intent);


--
-- Name: product_sessions product_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_sessions
    ADD CONSTRAINT product_sessions_pkey PRIMARY KEY (id);


--
-- Name: product_sessions product_sessions_product_type_product_id_label_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_sessions
    ADD CONSTRAINT product_sessions_product_type_product_id_label_key UNIQUE (product_type, product_id, label);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_token_hash_key UNIQUE (token_hash);


--
-- Name: suppliers suppliers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (id);


--
-- Name: system_settings system_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT system_settings_pkey PRIMARY KEY (key);


--
-- Name: tour_itinerary_stops tour_itinerary_stops_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tour_itinerary_stops
    ADD CONSTRAINT tour_itinerary_stops_pkey PRIMARY KEY (id);


--
-- Name: tour_pickup_zones tour_pickup_zones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tour_pickup_zones
    ADD CONSTRAINT tour_pickup_zones_pkey PRIMARY KEY (id);


--
-- Name: tours tours_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_pkey PRIMARY KEY (id);


--
-- Name: tours tours_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_slug_key UNIQUE (slug);


--
-- Name: transfer_pricing_tiers transfer_pricing_tiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_pricing_tiers
    ADD CONSTRAINT transfer_pricing_tiers_pkey PRIMARY KEY (id);


--
-- Name: transfer_rates transfer_rates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_rates
    ADD CONSTRAINT transfer_rates_pkey PRIMARY KEY (id);


--
-- Name: transfer_routes transfer_routes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_routes
    ADD CONSTRAINT transfer_routes_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_activity_variants_item; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_activity_variants_item ON public.activity_variants USING btree (booking_item_id);


--
-- Name: idx_agents_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agents_status ON public.agents USING btree (status) WHERE (deleted_at IS NULL);


--
-- Name: idx_agents_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_agents_user_id ON public.agents USING btree (user_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_audit_actor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_actor ON public.audit_logs USING btree (actor_id);


--
-- Name: idx_audit_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_created ON public.audit_logs USING btree (created_at DESC);


--
-- Name: idx_audit_entity; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_entity ON public.audit_logs USING btree (entity_type, entity_id);


--
-- Name: idx_booking_items_booking; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_booking_items_booking ON public.booking_items USING btree (booking_id);


--
-- Name: idx_booking_items_ref; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_booking_items_ref ON public.booking_items USING btree (ref_id);


--
-- Name: idx_bookings_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bookings_agent ON public.bookings USING btree (agent_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_bookings_customer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bookings_customer ON public.bookings USING btree (customer_id) WHERE (deleted_at IS NULL);


--
-- Name: idx_bookings_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bookings_date ON public.bookings USING btree (service_date) WHERE (deleted_at IS NULL);


--
-- Name: idx_bookings_enquiry; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bookings_enquiry ON public.bookings USING btree (is_enquiry) WHERE ((deleted_at IS NULL) AND (is_enquiry = true));


--
-- Name: idx_bookings_reference; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bookings_reference ON public.bookings USING btree (reference);


--
-- Name: idx_bookings_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bookings_status ON public.bookings USING btree (status) WHERE (deleted_at IS NULL);


--
-- Name: idx_car_avail_car_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_car_avail_car_date ON public.car_availability USING btree (car_id, date_from, date_to);


--
-- Name: idx_car_extras_car; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_car_extras_car ON public.car_extra_services USING btree (car_id);


--
-- Name: idx_car_rates_car_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_car_rates_car_id ON public.car_rates USING btree (car_id);


--
-- Name: idx_cars_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cars_category ON public.cars USING btree (category) WHERE (deleted_at IS NULL);


--
-- Name: idx_cars_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cars_status ON public.cars USING btree (status) WHERE (deleted_at IS NULL);


--
-- Name: idx_cars_usage; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cars_usage ON public.cars USING btree (usage_type) WHERE (deleted_at IS NULL);


--
-- Name: idx_cart_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_agent ON public.cart_items USING btree (agent_id) WHERE (agent_id IS NOT NULL);


--
-- Name: idx_cart_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_expires ON public.cart_items USING btree (expires_at);


--
-- Name: idx_cart_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_session ON public.cart_items USING btree (session_key);


--
-- Name: idx_customers_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_customers_email ON public.customers USING btree (email) WHERE (deleted_at IS NULL);


--
-- Name: idx_day_trips_slug; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_day_trips_slug ON public.day_trips USING btree (slug) WHERE (deleted_at IS NULL);


--
-- Name: idx_dt_highlights; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dt_highlights ON public.day_trip_highlights USING btree (day_trip_id);


--
-- Name: idx_dt_itinerary; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dt_itinerary ON public.day_trip_itinerary_stops USING btree (day_trip_id, stop_order);


--
-- Name: idx_dt_pickup_zones; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dt_pickup_zones ON public.day_trip_pickup_zones USING btree (day_trip_id);


--
-- Name: idx_invoices_booking; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_invoices_booking ON public.invoices USING btree (booking_id);


--
-- Name: idx_invoices_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_invoices_number ON public.invoices USING btree (invoice_number);


--
-- Name: idx_itinerary_tour; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_itinerary_tour ON public.tour_itinerary_stops USING btree (tour_id);


--
-- Name: idx_login_attempts_email_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_login_attempts_email_time ON public.login_attempts USING btree (email, attempted_at DESC);


--
-- Name: idx_notif_booking; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notif_booking ON public.notifications USING btree (booking_id);


--
-- Name: idx_notif_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notif_user ON public.notifications USING btree (user_id);


--
-- Name: idx_payments_booking; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payments_booking ON public.payments USING btree (booking_id);


--
-- Name: idx_payments_intent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payments_intent ON public.payments USING btree (stripe_payment_intent);


--
-- Name: idx_payments_peach_checkout; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_payments_peach_checkout ON public.payments USING btree (peach_checkout_id) WHERE (peach_checkout_id IS NOT NULL);


--
-- Name: idx_product_sessions; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_product_sessions ON public.product_sessions USING btree (product_type, product_id);


--
-- Name: idx_pwd_reset_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pwd_reset_hash ON public.password_reset_tokens USING btree (token_hash);


--
-- Name: idx_pwd_reset_user_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pwd_reset_user_active ON public.password_reset_tokens USING btree (user_id, expires_at) WHERE (used_at IS NULL);


--
-- Name: idx_refresh_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refresh_hash ON public.refresh_tokens USING btree (token_hash);


--
-- Name: idx_refresh_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refresh_user ON public.refresh_tokens USING btree (user_id);


--
-- Name: idx_tour_pickup_zones_tour; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tour_pickup_zones_tour ON public.tour_pickup_zones USING btree (tour_id);


--
-- Name: idx_tours_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tours_category ON public.tours USING btree (category) WHERE (deleted_at IS NULL);


--
-- Name: idx_tours_fts; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tours_fts ON public.tours USING gin (to_tsvector('english'::regconfig, (((title)::text || ' '::text) || COALESCE(description, ''::text))));


--
-- Name: idx_tours_slug; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tours_slug ON public.tours USING btree (slug) WHERE (deleted_at IS NULL);


--
-- Name: idx_tours_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tours_status ON public.tours USING btree (status) WHERE (deleted_at IS NULL);


--
-- Name: idx_transfer_rates_route; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_rates_route ON public.transfer_rates USING btree (route_id);


--
-- Name: idx_transfer_routes; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_routes ON public.transfer_routes USING btree (from_location, to_location, trip_type);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_email ON public.users USING btree (email) WHERE (deleted_at IS NULL);


--
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_role ON public.users USING btree (role) WHERE (deleted_at IS NULL);


--
-- Name: activity_variants activity_variants_booking_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_variants
    ADD CONSTRAINT activity_variants_booking_item_id_fkey FOREIGN KEY (booking_item_id) REFERENCES public.booking_items(id) ON DELETE CASCADE;


--
-- Name: agents agents_approved_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents
    ADD CONSTRAINT agents_approved_by_id_fkey FOREIGN KEY (approved_by_id) REFERENCES public.users(id);


--
-- Name: agents agents_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents
    ADD CONSTRAINT agents_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: booking_extras booking_extras_booking_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_extras
    ADD CONSTRAINT booking_extras_booking_item_id_fkey FOREIGN KEY (booking_item_id) REFERENCES public.booking_items(id) ON DELETE CASCADE;


--
-- Name: booking_items booking_items_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_items
    ADD CONSTRAINT booking_items_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.bookings(id) ON DELETE CASCADE;


--
-- Name: bookings bookings_agent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_agent_id_fkey FOREIGN KEY (agent_id) REFERENCES public.agents(id);


--
-- Name: bookings bookings_cancelled_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_cancelled_by_id_fkey FOREIGN KEY (cancelled_by_id) REFERENCES public.users(id);


--
-- Name: bookings bookings_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: bookings bookings_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id);


--
-- Name: car_availability car_availability_car_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car_availability
    ADD CONSTRAINT car_availability_car_id_fkey FOREIGN KEY (car_id) REFERENCES public.cars(id) ON DELETE CASCADE;


--
-- Name: car_extra_services car_extra_services_car_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car_extra_services
    ADD CONSTRAINT car_extra_services_car_id_fkey FOREIGN KEY (car_id) REFERENCES public.cars(id) ON DELETE CASCADE;


--
-- Name: car_rates car_rates_car_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.car_rates
    ADD CONSTRAINT car_rates_car_id_fkey FOREIGN KEY (car_id) REFERENCES public.cars(id) ON DELETE CASCADE;


--
-- Name: cars cars_supplier_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cars
    ADD CONSTRAINT cars_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES public.suppliers(id);


--
-- Name: cart_items cart_items_agent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_agent_id_fkey FOREIGN KEY (agent_id) REFERENCES public.agents(id);


--
-- Name: customers customers_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: day_trip_highlights day_trip_highlights_day_trip_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trip_highlights
    ADD CONSTRAINT day_trip_highlights_day_trip_id_fkey FOREIGN KEY (day_trip_id) REFERENCES public.day_trips(id) ON DELETE CASCADE;


--
-- Name: day_trip_itinerary_stops day_trip_itinerary_stops_day_trip_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trip_itinerary_stops
    ADD CONSTRAINT day_trip_itinerary_stops_day_trip_id_fkey FOREIGN KEY (day_trip_id) REFERENCES public.day_trips(id) ON DELETE CASCADE;


--
-- Name: day_trip_pickup_zones day_trip_pickup_zones_day_trip_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trip_pickup_zones
    ADD CONSTRAINT day_trip_pickup_zones_day_trip_id_fkey FOREIGN KEY (day_trip_id) REFERENCES public.day_trips(id) ON DELETE CASCADE;


--
-- Name: day_trips day_trips_supplier_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.day_trips
    ADD CONSTRAINT day_trips_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES public.suppliers(id);


--
-- Name: invoices invoices_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.bookings(id);


--
-- Name: notifications notifications_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.bookings(id);


--
-- Name: notifications notifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: password_reset_tokens password_reset_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: payments payments_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_booking_id_fkey FOREIGN KEY (booking_id) REFERENCES public.bookings(id);


--
-- Name: refresh_tokens refresh_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: system_settings system_settings_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT system_settings_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: tour_itinerary_stops tour_itinerary_stops_tour_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tour_itinerary_stops
    ADD CONSTRAINT tour_itinerary_stops_tour_id_fkey FOREIGN KEY (tour_id) REFERENCES public.tours(id) ON DELETE CASCADE;


--
-- Name: tour_pickup_zones tour_pickup_zones_tour_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tour_pickup_zones
    ADD CONSTRAINT tour_pickup_zones_tour_id_fkey FOREIGN KEY (tour_id) REFERENCES public.tours(id) ON DELETE CASCADE;


--
-- Name: tours tours_created_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: tours tours_supplier_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_supplier_id_fkey FOREIGN KEY (supplier_id) REFERENCES public.suppliers(id);


--
-- Name: transfer_rates transfer_rates_route_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_rates
    ADD CONSTRAINT transfer_rates_route_id_fkey FOREIGN KEY (route_id) REFERENCES public.transfer_routes(id) ON DELETE CASCADE;


--
-- Name: audit_logs; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

--
-- Name: audit_logs audit_no_delete; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY audit_no_delete ON public.audit_logs AS RESTRICTIVE FOR DELETE USING (false);


--
-- PostgreSQL database dump complete
--

\unrestrict MQ5UAEilOON8lsfibSnLS8PpD8NnVdnoxxRs2Q7hyohKb3ThawqcMeVgwJStqfS


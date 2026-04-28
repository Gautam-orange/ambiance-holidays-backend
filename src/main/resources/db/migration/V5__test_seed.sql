-- ============================================================
-- V5: Test seed — users, agents, drivers, bookings, day trips
-- Idempotent: all inserts use ON CONFLICT DO NOTHING
-- ============================================================

-- ============================================================
-- USERS
-- Passwords:
--   super_admin / ops / fleet  → Admin@123
--   customer                   → User@123
--   agent user                 → Agent@123
-- ============================================================
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, role, is_active, email_verified) VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin@ambianceholidays.mu',
   '$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$7DgD3bDKNcTZIPWKfXwzwtEOvCstsKDfdAjVCRrNTJI',
   'Super', 'Admin', '+230 5285 0500', 'SUPER_ADMIN', true, true),
  ('00000000-0000-0000-0000-000000000002', 'ops@ambianceholidays.mu',
   '$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$7DgD3bDKNcTZIPWKfXwzwtEOvCstsKDfdAjVCRrNTJI',
   'Operations', 'Manager', '+230 5285 0501', 'ADMIN_OPS', true, true),
  ('00000000-0000-0000-0000-000000000003', 'fleet@ambianceholidays.mu',
   '$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$7DgD3bDKNcTZIPWKfXwzwtEOvCstsKDfdAjVCRrNTJI',
   'Fleet', 'Manager', '+230 5285 0502', 'FLEET_MANAGER', true, true),
  ('00000000-0000-0000-0000-000000000004', 'john.doe@example.com',
   '$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$2FiEm/TKWSreWRRAmhOnCtOMUWyX6FalZeVMrfIqbig',
   'John', 'Doe', '+44 7700 900123', 'GUEST', true, true),
  ('00000000-0000-0000-0000-000000000005', 'agent@sunrisetravel.mu',
   '$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$0dQy4ob7A08Guxr83uj/YK6/Iz7qiHPPPtzydyrxTCM',
   'Priya', 'Sharma', '+230 5712 3456', 'B2B_AGENT', true, true),
  ('00000000-0000-0000-0000-000000000006', 'sarah.martin@example.com',
   '$argon2id$v=19$m=65536,t=3,p=4$YW1iaWFuY2VTYWx0MDEhIQ$2FiEm/TKWSreWRRAmhOnCtOMUWyX6FalZeVMrfIqbig',
   'Sarah', 'Martin', '+33 6 12 34 56 78', 'GUEST', true, true)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- SUPPLIER
-- ============================================================
INSERT INTO suppliers (id, name, contact_name, email, phone, country, is_active) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Island Leisure Ltd', 'Raj Patel', 'raj@islandleisure.mu', '+230 4651 2233', 'Mauritius', true),
  ('10000000-0000-0000-0000-000000000002', 'Blue Lagoon Water Sports', 'Kevin Li', 'kevin@bluelagoon.mu', '+230 4638 9900', 'Mauritius', true)
ON CONFLICT DO NOTHING;

-- ============================================================
-- AGENTS
-- ============================================================
INSERT INTO agents (id, user_id, company_name, country, city, address, business_type, tier, status,
                    markup_percent, commission_rate, credit_limit, total_bookings, approved_at, approved_by_id) VALUES
  ('20000000-0000-0000-0000-000000000001',
   '00000000-0000-0000-0000-000000000005',
   'Sunrise Travel Agency', 'Mauritius', 'Port Louis',
   '12 Royal Road, Port Louis',
   'TRAVEL_AGENCY', 'GOLD', 'ACTIVE',
   10.00, 12.00, 500000, 47,
   NOW() - INTERVAL '90 days',
   '00000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

-- ============================================================
-- CUSTOMERS
-- ============================================================
INSERT INTO customers (id, user_id, first_name, last_name, email, phone, nationality, passport_no) VALUES
  ('30000000-0000-0000-0000-000000000001',
   '00000000-0000-0000-0000-000000000004',
   'John', 'Doe', 'john.doe@example.com', '+44 7700 900123', 'British', 'GB123456789'),
  ('30000000-0000-0000-0000-000000000002',
   '00000000-0000-0000-0000-000000000006',
   'Sarah', 'Martin', 'sarah.martin@example.com', '+33 6 12 34 56 78', 'French', 'FR987654321'),
  ('30000000-0000-0000-0000-000000000003',
   NULL,
   'Marco', 'Rossi', 'marco.rossi@example.com', '+39 347 123 4567', 'Italian', 'IT567890123')
ON CONFLICT DO NOTHING;

-- ============================================================
-- DRIVERS
-- ============================================================
INSERT INTO drivers (id, code, first_name, last_name, phone, email, license_no, license_expiry,
                     experience_years, status, photo_url, is_active) VALUES
  ('40000000-0000-0000-0000-000000000001', 'DRV-001',
   'Ravi', 'Gokhool', '+230 5423 1111', 'ravi@ambianceholidays.mu',
   'LIC-MU-4412', '2027-06-30', 8, 'FREE',
   '/images/pexels-ian-panelo-5192474.jpg', true),
  ('40000000-0000-0000-0000-000000000002', 'DRV-002',
   'Dinesh', 'Ramkhelawon', '+230 5434 2222', 'dinesh@ambianceholidays.mu',
   'LIC-MU-5523', '2026-12-31', 5, 'FREE',
   '/images/pexels-umaraffan499-88212.jpg', true),
  ('40000000-0000-0000-0000-000000000003', 'DRV-003',
   'Kevin', 'Ah-Seek', '+230 5445 3333', 'kevin@ambianceholidays.mu',
   'LIC-MU-6634', '2028-03-15', 12, 'BOOKED',
   '/images/pexels-iamluisao-12652909.jpg', true),
  ('40000000-0000-0000-0000-000000000004', 'DRV-004',
   'Vishal', 'Nursinghen', '+230 5456 4444', 'vishal@ambianceholidays.mu',
   'LIC-MU-7745', '2027-09-20', 3, 'OFF_DUTY',
   '/images/pexels-quang-nguyen-vinh-222549-3355735.jpg', false)
ON CONFLICT DO NOTHING;

-- ============================================================
-- DAY TRIPS
-- ============================================================
INSERT INTO day_trips (id, supplier_id, title, slug, description, trip_type, region, duration,
                        adult_price_cents, child_price_cents, max_pax, theme,
                        includes, excludes, cover_image_url, status) VALUES
  ('50000000-0000-0000-0000-000000000001',
   '10000000-0000-0000-0000-000000000001',
   'Casela Nature Parks Adventure',
   'casela-nature-parks',
   'Spend a thrilling day at Casela World of Adventures — zip-line through the canopy, walk with lions, feed giraffes and explore the authentic African savannah. One of Mauritius''s most popular family attractions.',
   'SHARED', 'WEST', 'FULL_DAY',
   280000, 180000, 40, 'NATURE',
   ARRAY['Round-trip transfers','Park entrance','Lion walk (1 round)','Lunch voucher','Bottled water'],
   ARRAY['Optional activities (zip-line, quad bike, rhino encounter)','Personal expenses'],
   '/images/pexels-colourclouds-34264091.jpg', 'ACTIVE'),
  ('50000000-0000-0000-0000-000000000002',
   '10000000-0000-0000-0000-000000000002',
   'Blue Bay Marine Park Snorkel & Beach',
   'blue-bay-marine-snorkel',
   'Explore the crystal-clear waters of Blue Bay Marine Park — a protected UNESCO lagoon teeming with sea turtles, reef sharks and over 50 species of hard coral. Spend the afternoon relaxing on the white-sand beach.',
   'SHARED', 'SOUTH', 'FULL_DAY',
   220000, 140000, 20, 'SEA_ACTIVITIES',
   ARRAY['Return transfers','Snorkeling equipment','Glass-bottom boat (30 min)','Seafood BBQ lunch','Soft drinks'],
   ARRAY['Towels','Sunscreen','Alcoholic beverages'],
   '/images/pexels-cemil-tuyloglu-3443668-28885219.jpg', 'ACTIVE'),
  ('50000000-0000-0000-0000-000000000003',
   '10000000-0000-0000-0000-000000000001',
   'Pamplemousses & Port Louis City Tour',
   'pamplemousses-port-louis',
   'A cultural half-day combining the historic Sir Seewoosagur Ramgoolam Botanical Garden with a guided walk through Port Louis — the Caudan Waterfront, the Blue Penny Museum, and the bustling Central Market.',
   'SHARED', 'NORTH', 'HALF_DAY',
   150000, 90000, 30, 'CULTURAL',
   ARRAY['Air-conditioned minivan','Bilingual guide','Botanical garden entrance','Blue Penny Museum entry'],
   ARRAY['Lunch','Personal shopping'],
   '/images/pexels-vince-34732389.jpg', 'ACTIVE'),
  ('50000000-0000-0000-0000-000000000004',
   '10000000-0000-0000-0000-000000000002',
   'Private Catamaran Sunset Cruise',
   'private-catamaran-sunset',
   'Charter an entire luxury catamaran for your group. Watch the sun set over the Indian Ocean with cocktails in hand, then dine under the stars on fresh-caught lobster and seafood. The ultimate Mauritius experience.',
   'PRIVATE', 'WEST', 'HALF_DAY',
   650000, 400000, 12, 'BEACH',
   ARRAY['Exclusive catamaran charter','Welcome champagne','4-course seafood dinner','Open bar','Hotel transfers'],
   ARRAY['Gratuities','Fuel surcharge beyond 3 hours'],
   '/images/xavier-coiffic-yFSDYHAfhrI-unsplash.jpg', 'ACTIVE')
ON CONFLICT (slug) DO NOTHING;

-- Day trip highlights
INSERT INTO day_trip_highlights (day_trip_id, text, display_order) VALUES
  ('50000000-0000-0000-0000-000000000001', 'Lion Walk', 0),
  ('50000000-0000-0000-0000-000000000001', 'Zip Line', 1),
  ('50000000-0000-0000-0000-000000000001', 'Giraffe Feeding', 2),
  ('50000000-0000-0000-0000-000000000001', 'African Savannah', 3),
  ('50000000-0000-0000-0000-000000000002', 'Sea Turtles', 0),
  ('50000000-0000-0000-0000-000000000002', 'Glass-Bottom Boat', 1),
  ('50000000-0000-0000-0000-000000000002', 'Reef Sharks', 2),
  ('50000000-0000-0000-0000-000000000002', 'Coral Gardens', 3),
  ('50000000-0000-0000-0000-000000000003', 'Giant Water Lilies', 0),
  ('50000000-0000-0000-0000-000000000003', 'Blue Penny Museum', 1),
  ('50000000-0000-0000-0000-000000000003', 'Central Market', 2),
  ('50000000-0000-0000-0000-000000000004', 'Private Charter', 0),
  ('50000000-0000-0000-0000-000000000004', 'Lobster Dinner', 1),
  ('50000000-0000-0000-0000-000000000004', 'Champagne Sunset', 2)
ON CONFLICT DO NOTHING;

-- Day trip pickup zones
INSERT INTO day_trip_pickup_zones (day_trip_id, zone_name, pickup_time_from, pickup_time_to, sort_order) VALUES
  ('50000000-0000-0000-0000-000000000001', 'Grand Baie / Pereybere', '07:30', '08:00', 0),
  ('50000000-0000-0000-0000-000000000001', 'Port Louis / Bagatelle', '08:00', '08:30', 1),
  ('50000000-0000-0000-0000-000000000001', 'Flic en Flac / Wolmar', '07:00', '07:30', 2),
  ('50000000-0000-0000-0000-000000000002', 'Grand Baie / Pereybere', '07:30', '08:00', 0),
  ('50000000-0000-0000-0000-000000000002', 'Mahebourg / Blue Bay', '08:30', '09:00', 1),
  ('50000000-0000-0000-0000-000000000003', 'Grand Baie / Pereybere', '08:00', '08:30', 0),
  ('50000000-0000-0000-0000-000000000003', 'Port Louis Centre', '08:30', '09:00', 1),
  ('50000000-0000-0000-0000-000000000004', 'Flic en Flac / Wolmar', '17:00', '17:30', 0),
  ('50000000-0000-0000-0000-000000000004', 'Grand Baie / Pereybere', '17:00', '17:30', 1)
ON CONFLICT DO NOTHING;

-- Day trip itinerary
INSERT INTO day_trip_itinerary_stops (day_trip_id, stop_order, title, time_label, description) VALUES
  ('50000000-0000-0000-0000-000000000001', 0, 'Hotel Pick-Up', '07:30', 'Your guide meets you at your hotel reception.'),
  ('50000000-0000-0000-0000-000000000001', 1, 'Casela Arrival & Savannah Walk', '09:00', 'Enter Casela World of Adventures and start with the African savannah walk.'),
  ('50000000-0000-0000-0000-000000000001', 2, 'Lion Walk Experience', '10:30', 'Walk alongside sub-adult lions with experienced handlers in a safe environment.'),
  ('50000000-0000-0000-0000-000000000001', 3, 'Giraffe Feeding & Lunch', '12:30', 'Feed the resident giraffes then enjoy lunch at the on-site restaurant.'),
  ('50000000-0000-0000-0000-000000000001', 4, 'Free Time & Optional Activities', '14:00', 'Choose from zip-lining, quad biking, rhino encounter or simply explore the park.'),
  ('50000000-0000-0000-0000-000000000001', 5, 'Return Transfer', '16:30', 'Comfortable return to your hotel.'),
  ('50000000-0000-0000-0000-000000000002', 0, 'Hotel Pick-Up', '07:30', 'Transfer to the south coast.'),
  ('50000000-0000-0000-0000-000000000002', 1, 'Glass-Bottom Boat Tour', '09:30', '30-minute glass-bottom boat ride over the coral reef.'),
  ('50000000-0000-0000-0000-000000000002', 2, 'Snorkeling in the Lagoon', '10:15', 'Guided snorkel session with equipment provided — spot sea turtles and tropical fish.'),
  ('50000000-0000-0000-0000-000000000002', 3, 'Beach Time', '12:00', 'Relax on the white-sand beach at Blue Bay.'),
  ('50000000-0000-0000-0000-000000000002', 4, 'BBQ Seafood Lunch', '13:00', 'Fresh seafood BBQ buffet served beachside.'),
  ('50000000-0000-0000-0000-000000000002', 5, 'Return Transfer', '15:30', 'Return to your hotel.')
ON CONFLICT DO NOTHING;

-- ============================================================
-- TOURS: add themes to existing tours
-- ============================================================
UPDATE tours SET theme = 'SEA_ACTIVITIES', availability_mode = 'always' WHERE slug = 'coastal-catamaran-day';
UPDATE tours SET theme = 'BEACH',           availability_mode = 'always' WHERE slug = 'ile-aux-cerfs-full-day';
UPDATE tours SET theme = 'CULTURAL',        availability_mode = 'always' WHERE slug = 'north-coast-heritage';
UPDATE tours SET theme = 'BEACH',           availability_mode = 'always' WHERE slug = 'sunset-dinner-cruise';
UPDATE tours SET theme = 'NATURE',          availability_mode = 'always' WHERE slug = 'south-island-safari';
UPDATE tours SET theme = 'SEA_ACTIVITIES',  availability_mode = 'always' WHERE slug = 'snorkeling-underwater-world';
UPDATE tours SET theme = 'ADVENTURE',       availability_mode = 'always' WHERE slug = 'watersports-adventure-day';
UPDATE tours SET theme = 'BEACH',           availability_mode = 'on_request' WHERE slug = 'le-morne-kite-beach-day';

-- ============================================================
-- CARS: add PER_KM transfer rates for BOTH cars
-- ============================================================
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'PER_KM', 180 FROM cars WHERE registration_no = 'MU-1234-BMW'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'PER_KM');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'PER_KM', 220 FROM cars WHERE registration_no = 'MU-LUX-001'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'PER_KM');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'PER_KM', 160 FROM cars WHERE registration_no = 'MU-SUV-002'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'PER_KM');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'PER_KM', 300 FROM cars WHERE registration_no = 'MU-VAN-006'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'PER_KM');

-- Car extra services
INSERT INTO car_extra_services (car_id, name, price_cents, display_order)
SELECT c.id, 'Baby Seat', 1500, 0 FROM cars c WHERE c.registration_no IN ('MU-LUX-001','MU-SUV-002','MU-STD-004','MU-ECO-005','MU-VAN-006')
  AND NOT EXISTS (SELECT 1 FROM car_extra_services e WHERE e.car_id = c.id AND e.name = 'Baby Seat');
INSERT INTO car_extra_services (car_id, name, price_cents, display_order)
SELECT c.id, 'Additional Driver', 2500, 1 FROM cars c WHERE c.registration_no IN ('MU-PRE-003','MU-STD-004','MU-ECO-005')
  AND NOT EXISTS (SELECT 1 FROM car_extra_services e WHERE e.car_id = c.id AND e.name = 'Additional Driver');
INSERT INTO car_extra_services (car_id, name, price_cents, display_order)
SELECT c.id, 'WiFi Dongle', 800, 2 FROM cars c WHERE c.registration_no IN ('MU-LUX-001','MU-PRE-003','MU-SUV-002')
  AND NOT EXISTS (SELECT 1 FROM car_extra_services e WHERE e.car_id = c.id AND e.name = 'WiFi Dongle');

-- ============================================================
-- BOOKINGS (3 realistic bookings for testing)
-- ============================================================

-- Booking 1: Confirmed rental booking by John Doe (direct customer)
INSERT INTO bookings (id, reference, customer_id, agent_id, status,
                       subtotal_cents, markup_cents, commission_cents, vat_cents, total_cents,
                       vat_rate, markup_rate, commission_rate,
                       service_date, created_by_id, is_enquiry)
VALUES (
  'b0000000-0000-0000-0000-000000000001',
  'AMB-2026-0001',
  '30000000-0000-0000-0000-000000000001',
  NULL, 'CONFIRMED',
  440000, 0, 0, 66000, 506000,
  15.00, 0.00, 0.00,
  '2026-05-10',
  NULL, false
) ON CONFLICT DO NOTHING;

INSERT INTO booking_items (id, booking_id, item_type, ref_id, quantity,
                            unit_price_cents, total_cents, service_date,
                            pax_adults, pax_children, rental_days, instance_index)
SELECT
  'c1000000-0000-0000-0000-000000000001',
  'b0000000-0000-0000-0000-000000000001',
  'CAR_RENTAL', c.id, 1,
  220000, 440000,
  '2026-05-10',
  2, 0, 2, 1
FROM cars c WHERE c.registration_no = 'MU-STD-004'
ON CONFLICT DO NOTHING;

-- Booking 2: Agent booking with tour (Ile aux Cerfs) — CONFIRMED
INSERT INTO bookings (id, reference, customer_id, agent_id, status,
                       subtotal_cents, markup_cents, commission_cents, vat_cents, total_cents,
                       vat_rate, markup_rate, commission_rate,
                       service_date, created_by_id, is_enquiry)
VALUES (
  'b0000000-0000-0000-0000-000000000002',
  'AMB-2026-0002',
  '30000000-0000-0000-0000-000000000002',
  '20000000-0000-0000-0000-000000000001',
  'CONFIRMED',
  1120000, 112000, 134400, 168000, 1534400,
  15.00, 10.00, 12.00,
  '2026-05-15',
  '00000000-0000-0000-0000-000000000005',
  false
) ON CONFLICT DO NOTHING;

INSERT INTO booking_items (id, booking_id, item_type, ref_id, quantity,
                            unit_price_cents, total_cents, service_date,
                            pax_adults, pax_children, instance_index)
SELECT
  'c1000000-0000-0000-0000-000000000002',
  'b0000000-0000-0000-0000-000000000002',
  'TOUR', t.id, 1,
  1120000, 1120000,
  '2026-05-15',
  3, 1, 1
FROM tours t WHERE t.slug = 'ile-aux-cerfs-full-day'
ON CONFLICT DO NOTHING;

-- Booking 3: Pending enquiry (on_request tour) — isEnquiry=true
INSERT INTO bookings (id, reference, customer_id, agent_id, status,
                       subtotal_cents, markup_cents, commission_cents, vat_cents, total_cents,
                       vat_rate, markup_rate, commission_rate,
                       service_date, created_by_id, is_enquiry)
VALUES (
  'b0000000-0000-0000-0000-000000000003',
  'AMB-2026-0003',
  '30000000-0000-0000-0000-000000000003',
  NULL, 'PENDING',
  880000, 0, 0, 132000, 1012000,
  15.00, 0.00, 0.00,
  '2026-06-01',
  NULL, true
) ON CONFLICT DO NOTHING;

INSERT INTO booking_items (id, booking_id, item_type, ref_id, quantity,
                            unit_price_cents, total_cents, service_date,
                            pax_adults, pax_children, instance_index)
SELECT
  'c1000000-0000-0000-0000-000000000003',
  'b0000000-0000-0000-0000-000000000003',
  'TOUR', t.id, 1,
  880000, 880000,
  '2026-06-01',
  4, 0, 1
FROM tours t WHERE t.slug = 'le-morne-kite-beach-day'
ON CONFLICT DO NOTHING;

-- Booking 4: Cancelled booking by customer
INSERT INTO bookings (id, reference, customer_id, agent_id, status,
                       subtotal_cents, markup_cents, commission_cents, vat_cents, total_cents,
                       vat_rate, markup_rate, commission_rate,
                       service_date, created_by_id, is_enquiry,
                       cancel_reason, cancelled_at, cancelled_by_id, cancelled_by_type)
VALUES (
  'b0000000-0000-0000-0000-000000000004',
  'AMB-2026-0004',
  '30000000-0000-0000-0000-000000000001',
  NULL, 'CANCELLED',
  640000, 0, 0, 96000, 736000,
  15.00, 0.00, 0.00,
  '2026-04-20',
  NULL, false,
  'Change of travel plans',
  NOW() - INTERVAL '5 days',
  NULL, 'CUSTOMER'
) ON CONFLICT DO NOTHING;

INSERT INTO booking_items (id, booking_id, item_type, ref_id, quantity,
                            unit_price_cents, total_cents, service_date,
                            pax_adults, pax_children, instance_index)
SELECT
  'c1000000-0000-0000-0000-000000000004',
  'b0000000-0000-0000-0000-000000000004',
  'TOUR', t.id, 1,
  640000, 640000,
  '2026-04-20',
  2, 0, 1
FROM tours t WHERE t.slug = 'sunset-dinner-cruise'
ON CONFLICT DO NOTHING;

-- ============================================================
-- PAYMENTS
-- ============================================================
INSERT INTO payments (booking_id, method, amount_cents, currency, status, paid_at)
VALUES
  ('b0000000-0000-0000-0000-000000000001', 'STRIPE', 506000, 'MUR', 'SUCCEEDED', NOW() - INTERVAL '10 days'),
  ('b0000000-0000-0000-0000-000000000002', 'BANK_TRANSFER', 1534400, 'MUR', 'SUCCEEDED', NOW() - INTERVAL '7 days'),
  ('b0000000-0000-0000-0000-000000000004', 'STRIPE', 736000, 'MUR', 'REFUNDED', NOW() - INTERVAL '8 days')
ON CONFLICT DO NOTHING;

-- ============================================================
-- DRIVER ASSIGNMENT (for booking 2)
-- ============================================================
INSERT INTO driver_assignments (driver_id, booking_item_id, car_id,
                                  start_at, end_at, pickup_address, dropoff_address,
                                  assigned_by_id)
SELECT
  '40000000-0000-0000-0000-000000000003',
  'c1000000-0000-0000-0000-000000000002',
  NULL,
  '2026-05-15 07:00:00+04',
  '2026-05-15 17:00:00+04',
  'Le Turquoise Hotel, Grand Baie',
  'Trou d''Eau Douce Jetty',
  '00000000-0000-0000-0000-000000000003'
ON CONFLICT DO NOTHING;

-- ============================================================
-- LEADS
-- ============================================================
INSERT INTO leads (name, email, phone, message, source) VALUES
  ('Antoine Dupont', 'antoine.dupont@example.fr', '+33 6 98 76 54 32',
   'We are a family of 4 planning a 10-day trip in July. Interested in a full island tour package.',
   'contact_form'),
  ('Mei Lin', 'mei.lin@example.sg', '+65 9123 4567',
   'Looking for luxury car rental + driver for 7 days, arriving 1 June.',
   'contact_form'),
  ('James Okafor', 'james.okafor@example.ng', '+234 801 234 5678',
   'Corporate group of 12, need transfers from SSR airport and team excursions.',
   'website_chat')
ON CONFLICT DO NOTHING;

-- ============================================================
-- NEWSLETTER
-- ============================================================
INSERT INTO newsletter_subscribers (email, is_active, confirmed_at) VALUES
  ('newsletter1@example.com', true, NOW() - INTERVAL '30 days'),
  ('newsletter2@example.com', true, NOW() - INTERVAL '14 days'),
  ('newsletter3@example.com', false, NULL)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- V2: Rich seed data — idempotent (safe to re-run)
-- ============================================================

-- Update existing records with stock images (always safe to re-run)
UPDATE tours SET
  cover_image_url = '/images/pexels-candewesth-32251307.jpg',
  description     = 'Spend a full day sailing the turquoise waters of the Indian Ocean aboard our luxury catamaran. Snorkel over vibrant coral reefs, swim in crystal-clear lagoons, and enjoy a sumptuous BBQ lunch on board. An unforgettable Mauritius experience.',
  includes        = ARRAY['BBQ lunch & drinks','Snorkeling equipment','Life jackets','Professional guide','Hotel transfers'],
  excludes        = ARRAY['Personal expenses','Towels','Sunscreen'],
  important_notes = ARRAY['Departure 08:30 from Grand Baie','Minimum 2 passengers required','Not recommended for guests with motion sickness']
WHERE slug = 'coastal-catamaran-day';

UPDATE cars SET
  cover_image_url = 'https://images.unsplash.com/photo-1606016159991-dfe4f2746ad5?auto=format&fit=crop&w=800&q=80',
  description     = 'The ultimate luxury SUV. The BMW X7 offers unparalleled comfort with 7 seats, a panoramic sunroof, and the latest in-car technology. Perfect for large families or executive transfers.',
  includes        = ARRAY['Professional driver on request','Fuel included (first 100km)','Comprehensive insurance','GPS Navigation','24/7 roadside assistance'],
  excludes        = ARRAY['Additional fuel beyond 100km','Airport pickup surcharge','Child seats (request in advance)']
WHERE registration_no = 'MU-1234-BMW';

UPDATE cars SET
  cover_image_url = 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80',
  description     = 'The Ford EcoSport is a compact and nimble crossover, ideal for navigating Mauritius''s varied terrain. Great fuel economy and easy parking make it perfect for solo travelers or couples.',
  includes        = ARRAY['Comprehensive insurance','GPS Navigation','Air conditioning'],
  excludes        = ARRAY['Fuel','Driver (self-drive only)','Toll fees']
WHERE registration_no = 'MU-123';

UPDATE transfer_routes SET est_duration_mins = 50, est_km = 55
WHERE from_location = 'SSR Airport' AND to_location = 'Grand Baie' AND car_category = 'STANDARD'::car_category AND est_km IS NULL;

-- ============================================================
-- CARS — ON CONFLICT (registration_no) DO NOTHING
-- ============================================================
INSERT INTO cars (registration_no, name, category, usage_type, year, passenger_capacity, luggage_capacity, has_ac, is_automatic, fuel_type, color, description, cover_image_url, includes, excludes, status) VALUES
  ('MU-LUX-001', 'Mercedes-Benz E-Class', 'LUXURY', 'BOTH', 2023, 4, 3, true, true, 'Petrol', 'Obsidian Black',
   'The Mercedes E-Class redefines executive travel. With plush leather interiors, MBUX infotainment and a whisper-quiet ride, every journey feels first class.',
   'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80',
   ARRAY['Professional chauffeur available','Fuel included (first 150km)','Comprehensive insurance','GPS Navigation','Chilled water & refreshments'],
   ARRAY['Additional fuel beyond 150km','Airport fast-track'], 'ACTIVE'),
  ('MU-SUV-002', 'Toyota Land Cruiser Prado', 'SUV', 'BOTH', 2022, 7, 5, true, true, 'Diesel', 'Pearl White',
   'Conquer every corner of Mauritius in the Land Cruiser Prado. This full-size 4×4 SUV handles mountain roads, coastal tracks and city streets with equal ease.',
   'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80',
   ARRAY['7-seat configuration','Comprehensive insurance','GPS Navigation','Air conditioning','Professional driver optional'],
   ARRAY['Fuel','Off-road excursion surcharge'], 'ACTIVE'),
  ('MU-PRE-003', 'Audi A4', 'PREMIUM', 'RENTAL', 2023, 5, 3, true, true, 'Petrol', 'Glacier White',
   'The Audi A4 combines sportiness with sophistication. Its quattro all-wheel drive and refined cabin make it the ideal choice for discerning travellers.',
   'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=800&q=80',
   ARRAY['Comprehensive insurance','GPS Navigation','Air conditioning','Roadside assistance'],
   ARRAY['Fuel','Driver'], 'ACTIVE'),
  ('MU-STD-004', 'Toyota Corolla Cross', 'STANDARD', 'RENTAL', 2022, 5, 2, true, true, 'Petrol', 'Silver',
   'Reliable, comfortable, and fuel-efficient. The Corolla Cross is our most popular choice for families and couples exploring the island at their own pace.',
   'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?auto=format&fit=crop&w=800&q=80',
   ARRAY['Comprehensive insurance','GPS Navigation','Air conditioning'],
   ARRAY['Fuel','Driver','Child seats (available on request)'], 'ACTIVE'),
  ('MU-ECO-005', 'Suzuki Swift', 'ECONOMY', 'RENTAL', 2023, 4, 1, true, true, 'Petrol', 'Red',
   'The zippiest way to discover Mauritius. The Suzuki Swift is compact, economical and easy to park — perfect for solo travellers or couples on a budget.',
   'https://images.unsplash.com/photo-1609521263047-f8f205293f24?auto=format&fit=crop&w=800&q=80',
   ARRAY['Comprehensive insurance','Air conditioning'],
   ARRAY['GPS (available on request)','Fuel','Driver'], 'ACTIVE'),
  ('MU-VAN-006', 'Toyota HiAce Commuter', 'MINIVAN', 'BOTH', 2022, 12, 8, true, false, 'Diesel', 'White',
   'The HiAce Commuter is the gold standard for group travel in Mauritius. Spacious, air-conditioned and reliable — ideal for corporate groups, wedding parties and large families.',
   'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80',
   ARRAY['Professional driver','Fuel included','Comprehensive insurance','Air conditioning','Luggage trailer available'],
   ARRAY['Overtime after 8 hours','Toll fees'], 'ACTIVE')
ON CONFLICT (registration_no) DO NOTHING;

-- Car rates (insert only if car has no DAILY rate yet)
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'DAILY', 650000 FROM cars WHERE registration_no = 'MU-LUX-001'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'DAILY');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'DAILY', 480000 FROM cars WHERE registration_no = 'MU-SUV-002'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'DAILY');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'DAILY', 380000 FROM cars WHERE registration_no = 'MU-PRE-003'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'DAILY');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'DAILY', 220000 FROM cars WHERE registration_no = 'MU-STD-004'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'DAILY');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'DAILY', 140000 FROM cars WHERE registration_no = 'MU-ECO-005'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'DAILY');
INSERT INTO car_rates (car_id, period, amount_cents)
SELECT id, 'DAILY', 580000 FROM cars WHERE registration_no = 'MU-VAN-006'
  AND NOT EXISTS (SELECT 1 FROM car_rates r WHERE r.car_id = cars.id AND r.period = 'DAILY');

-- ============================================================
-- TOURS — ON CONFLICT (slug) DO NOTHING
-- ============================================================
INSERT INTO tours (title, slug, description, category, region, duration, duration_hours, adult_price_cents, child_price_cents, infant_price_cents, min_pax, max_pax, includes, excludes, important_notes, cover_image_url, status) VALUES
  ('Île aux Cerfs Full Day Excursion', 'ile-aux-cerfs-full-day',
   'Discover the jewel of the Indian Ocean — Île aux Cerfs. This uninhabited paradise island boasts powder-white beaches, turquoise lagoons and world-class water sports. Spend a full day basking, snorkelling and exploring at your leisure.',
   'SEA', 'EAST', 'FULL_DAY', 8.0, 280000, 175000, 0, 2, 30,
   ARRAY['Return speedboat transfer','Snorkeling equipment','BBQ seafood lunch','Non-alcoholic beverages','Hotel pick-up & drop-off'],
   ARRAY['Alcoholic drinks','Personal watersports (jet-ski, parasailing)','Travel insurance'],
   ARRAY['Bring sunscreen and a hat','Hotel pick-up starts from 07:00','Tour operates daily subject to weather'],
   '/images/pexels-asadphoto-3319712.jpg', 'ACTIVE'),
  ('North Coast Heritage & Beaches', 'north-coast-heritage',
   'Explore the charming north of Mauritius — from the iconic red-roofed chapel at Cap Malheureux overlooking Coin de Mire island, to the vibrant market town of Grand Baie and the historic colonial sugar estate at Mon Plaisir. A perfect blend of culture, history and stunning coastal scenery.',
   'LAND', 'NORTH', 'FULL_DAY', 7.0, 195000, 120000, 0, 2, 20,
   ARRAY['Air-conditioned minivan','Professional bilingual guide','Entrance fees','Light lunch','Hotel transfers'],
   ARRAY['Personal shopping','Gratuities'],
   ARRAY['Comfortable walking shoes recommended','Pick-up between 08:00–09:00'],
   '/images/pexels-vince-34732389.jpg', 'ACTIVE'),
  ('Sunset Dinner Cruise', 'sunset-dinner-cruise',
   'Watch the sun melt into the Indian Ocean from the deck of our luxury catamaran. Sip cocktails as the sky turns shades of amber and rose, then enjoy a freshly prepared 3-course dinner under the stars. This is Mauritius at its most magical.',
   'SEA', 'WEST', 'HALF_DAY', 3.5, 320000, 200000, 0, 2, 24,
   ARRAY['Welcome cocktail','3-course dinner','Open bar (wine, beer, soft drinks)','Live Sega music','Hotel transfers'],
   ARRAY['Gratuities','Personal expenses'],
   ARRAY['Departure 17:30, return ~21:00','Smart-casual dress code','Minimum age 5 years'],
   '/images/xavier-coiffic-yFSDYHAfhrI-unsplash.jpg', 'ACTIVE'),
  ('South Island Safari', 'south-island-safari',
   'Journey through the wild and dramatic south of Mauritius. Visit the sacred Ganga Talao crater lake, the stunning Chamarel Coloured Earths, Chamarel Waterfall, the Black River Gorges viewpoints, and the iconic Le Morne Brabant — a UNESCO World Heritage Site.',
   'LAND', 'SOUTH', 'FULL_DAY', 9.0, 245000, 155000, 0, 2, 20,
   ARRAY['Air-conditioned 4×4 vehicle','Expert local guide','Entrance fees','Traditional Mauritian lunch','Hotel transfers'],
   ARRAY['Rum distillery tasting (optional extra)','Gratuities'],
   ARRAY['Wear comfortable shoes for short walks','Bring a light jacket for the gorges','Pick-up from 07:30'],
   '/images/pexels-zakh-36731926.jpg', 'ACTIVE'),
  ('Snorkeling & Underwater World', 'snorkeling-underwater-world',
   'Plunge into the warm, crystal-clear waters of the Indian Ocean and discover a dazzling world of coral gardens, tropical fish, sea turtles and rays. Our expert guides will take you to the best snorkeling spots in the Blue Bay Marine Park and Île aux Bénitiers.',
   'SEA', 'SOUTH', 'HALF_DAY', 4.0, 180000, 110000, 0, 2, 16,
   ARRAY['Professional snorkeling guide','Full snorkeling equipment','Life vests','Light refreshments on board','Hotel transfers'],
   ARRAY['Underwater camera hire','Towels'],
   ARRAY['Non-swimmers welcome with life vest','Minimum age 6 years','Wear biodegradable sunscreen only'],
   '/images/pexels-cemil-tuyloglu-3443668-28885219.jpg', 'ACTIVE'),
  ('Watersports Adventure Day', 'watersports-adventure-day',
   'Crave thrills on the water? This action-packed day bundles the best of Mauritius''s adrenaline watersports — jet-skiing, parasailing, kite-surfing intro, glass-bottom boat ride, and more. Suitable for beginners and experienced adventurers alike.',
   'SEA', 'NORTH', 'FULL_DAY', 6.0, 420000, 280000, 0, 2, 12,
   ARRAY['Jet-ski 30 mins','Parasailing flight','Glass-bottom boat tour','Kite-surf introduction (45 min)','BBQ lunch','Hotel transfers'],
   ARRAY['Underwater scooter (optional +Rs 1500)','Travel insurance'],
   ARRAY['Minimum age 12 years','Participants must be able to swim','Booking at least 24h in advance required'],
   '/images/pexels-ahmet-kadioglu-650478141-32552944.jpg', 'ACTIVE'),
  ('Le Morne & Kite Beach Day', 'le-morne-kite-beach-day',
   'Spend a glorious day at the foot of the majestic Le Morne Brabant mountain. Relax on one of Mauritius''s most photographed beaches, watch world-class kite-surfers in the famous kite lagoon, and explore the UNESCO heritage site with a local guide.',
   'LAND', 'SOUTH', 'FULL_DAY', 8.0, 220000, 140000, 0, 2, 18,
   ARRAY['Transport in air-conditioned vehicle','Local heritage guide','Kite Beach access','Picnic lunch','Hotel transfers'],
   ARRAY['Kite-surfing lessons (book separately)','Personal expenses'],
   ARRAY['Bring swimming gear','Best visited Oct–April for kite-surfers','Departs 08:00'],
   '/images/pexels-asadphoto-3320533.jpg', 'ACTIVE')
ON CONFLICT (slug) DO NOTHING;

-- Itinerary stops (only if tour has none yet)
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '07:00', 'Hotel Pick-Up', 'Your driver meets you at reception for a comfortable transfer to the east coast.', 0 FROM tours t WHERE t.slug = 'ile-aux-cerfs-full-day'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '08:30', 'Speedboat Departure', 'Board our 10-seat speedboat at Trou d''Eau Douce jetty for a 15-minute crossing.', 1 FROM tours t WHERE t.slug = 'ile-aux-cerfs-full-day'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 1);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '09:00', 'Beach Time & Snorkeling', 'Free time on the pristine beach. Snorkeling equipment is available.', 2 FROM tours t WHERE t.slug = 'ile-aux-cerfs-full-day'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 2);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '13:00', 'BBQ Seafood Lunch', 'Enjoy a lavish BBQ buffet with fresh-caught seafood, salads and tropical desserts.', 3 FROM tours t WHERE t.slug = 'ile-aux-cerfs-full-day'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 3);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '15:30', 'Return Transfer', 'Speedboat back to Trou d''Eau Douce, then comfortable transfer to your hotel.', 4 FROM tours t WHERE t.slug = 'ile-aux-cerfs-full-day'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 4);

INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '08:30', 'Cap Malheureux Chapel', 'Visit the iconic red-roofed Notre-Dame Auxiliatrice chapel with views over Coin de Mire island.', 0 FROM tours t WHERE t.slug = 'north-coast-heritage'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '10:00', 'Grand Baie Promenade', 'Explore the vibrant marina and local boutiques of Mauritius''s most popular resort town.', 1 FROM tours t WHERE t.slug = 'north-coast-heritage'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 1);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '12:30', 'Traditional Lunch', 'Lunch at a family-run restaurant serving authentic Mauritian cuisine.', 2 FROM tours t WHERE t.slug = 'north-coast-heritage'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 2);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '14:00', 'Pamplemousses Botanical Garden', 'Stroll through one of the oldest botanical gardens in the Southern Hemisphere.', 3 FROM tours t WHERE t.slug = 'north-coast-heritage'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 3);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '16:30', 'Hotel Drop-Off', 'Comfortable return transfer to your accommodation.', 4 FROM tours t WHERE t.slug = 'north-coast-heritage'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 4);

INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '07:30', 'Hotel Pick-Up', 'Early start for the drive south. Enjoy the changing landscape as the mountains come into view.', 0 FROM tours t WHERE t.slug = 'south-island-safari'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '09:30', 'Chamarel Coloured Earths', 'Witness the extraordinary seven-coloured earth formation and the Chamarel Waterfall.', 1 FROM tours t WHERE t.slug = 'south-island-safari'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 1);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '11:00', 'Black River Gorges Viewpoint', 'Panoramic views over Mauritius''s largest national park from the dramatic gorge lookout.', 2 FROM tours t WHERE t.slug = 'south-island-safari'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 2);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '13:00', 'Local Lunch', 'Enjoy a traditional rougaille and dholl puri at a local family restaurant.', 3 FROM tours t WHERE t.slug = 'south-island-safari'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 3);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '14:30', 'Le Morne Brabant', 'Visit the UNESCO World Heritage Site and hear the powerful story of the escaped slaves who sought refuge here.', 4 FROM tours t WHERE t.slug = 'south-island-safari'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 4);
INSERT INTO tour_itinerary_stops (tour_id, stop_time, title, description, sort_order)
SELECT t.id, '16:30', 'Ganga Talao / Grand Bassin', 'The sacred crater lake pilgrimage site, surrounded by towering statues of Hindu deities.', 5 FROM tours t WHERE t.slug = 'south-island-safari'
  AND NOT EXISTS (SELECT 1 FROM tour_itinerary_stops s WHERE s.tour_id = t.id AND s.sort_order = 5);

-- Pickup zones (only if tour has none yet)
INSERT INTO tour_pickup_zones (tour_id, zone_name, extra_cents, pickup_time, sort_order)
SELECT t.id, 'Grand Baie / Pereybere', 0, '07:30', 0 FROM tours t
WHERE t.slug IN ('ile-aux-cerfs-full-day','north-coast-heritage','south-island-safari','sunset-dinner-cruise','snorkeling-underwater-world','watersports-adventure-day','le-morne-kite-beach-day','coastal-catamaran-day')
  AND NOT EXISTS (SELECT 1 FROM tour_pickup_zones z WHERE z.tour_id = t.id);

INSERT INTO tour_pickup_zones (tour_id, zone_name, extra_cents, pickup_time, sort_order)
SELECT t.id, 'Flic en Flac / Wolmar', 2000, '07:00', 1 FROM tours t
WHERE t.slug IN ('ile-aux-cerfs-full-day','south-island-safari','sunset-dinner-cruise','snorkeling-underwater-world','watersports-adventure-day','le-morne-kite-beach-day','coastal-catamaran-day')
  AND NOT EXISTS (SELECT 1 FROM tour_pickup_zones z WHERE z.tour_id = t.id AND z.sort_order = 1);

INSERT INTO tour_pickup_zones (tour_id, zone_name, extra_cents, pickup_time, sort_order)
SELECT t.id, 'Port Louis / Bagatelle', 1500, '07:15', 2 FROM tours t
WHERE t.slug IN ('ile-aux-cerfs-full-day','north-coast-heritage','south-island-safari','sunset-dinner-cruise','snorkeling-underwater-world','watersports-adventure-day')
  AND NOT EXISTS (SELECT 1 FROM tour_pickup_zones z WHERE z.tour_id = t.id AND z.sort_order = 2);

-- Transfer routes (only if no existing route matches from+to+category+type)
INSERT INTO transfer_routes (from_location, to_location, trip_type, car_category, base_price_cents, est_duration_mins, est_km, is_active)
SELECT v.* FROM (VALUES
  ('SSR Airport','Port Louis',           'ONE_WAY'::transfer_trip_type,'STANDARD'::car_category,120000,45,48,true),
  ('SSR Airport','Flic en Flac',         'ONE_WAY'::transfer_trip_type,'STANDARD'::car_category,180000,70,75,true),
  ('SSR Airport','Le Morne',             'ONE_WAY'::transfer_trip_type,'STANDARD'::car_category,210000,80,90,true),
  ('SSR Airport','Mahebourg',            'ONE_WAY'::transfer_trip_type,'ECONOMY'::car_category, 90000,20,18,true),
  ('SSR Airport','Grand Baie',           'ONE_WAY'::transfer_trip_type,'LUXURY'::car_category, 300000,55,55,true),
  ('SSR Airport','Tamarin / Black River','ONE_WAY'::transfer_trip_type,'STANDARD'::car_category,190000,75,80,true),
  ('Grand Baie', 'SSR Airport',          'ONE_WAY'::transfer_trip_type,'STANDARD'::car_category,155000,50,55,true),
  ('Port Louis', 'Grand Baie',           'ONE_WAY'::transfer_trip_type,'ECONOMY'::car_category,  80000,35,28,true),
  ('Flic en Flac','Le Morne',            'ONE_WAY'::transfer_trip_type,'ECONOMY'::car_category,  70000,30,25,true),
  ('SSR Airport','Grand Baie',           'ONE_WAY'::transfer_trip_type,'ECONOMY'::car_category, 180000,55,55,true),
  ('SSR Airport','Port Louis',           'ONE_WAY'::transfer_trip_type,'LUXURY'::car_category,  280000,45,48,true),
  ('SSR Airport','Grand Baie',           'ROUND_TRIP'::transfer_trip_type,'STANDARD'::car_category,290000,55,55,true),
  ('SSR Airport','Flic en Flac',         'ROUND_TRIP'::transfer_trip_type,'STANDARD'::car_category,340000,70,75,true)
) AS v(from_location, to_location, trip_type, car_category, base_price_cents, est_duration_mins, est_km, is_active)
WHERE NOT EXISTS (
  SELECT 1 FROM transfer_routes r
  WHERE r.from_location = v.from_location
    AND r.to_location   = v.to_location
    AND r.trip_type     = v.trip_type
    AND r.car_category  = v.car_category
);

-- V35: Seed Earth 2026 country capitals as landing points
-- Ensures every country has a capital city location so players from different
-- countries have a landing place on Earth 2026.

-- Only insert capitals that are not already in the locations table.
-- V30 already seeded: New York City, London, Tokyo.

-- United States - Washington D.C.
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'White House', 'Washington D.C.', 'D.C.', 'USA', 'Capital',
  'Capital of the United States. Political center of the world''s largest economy.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Washington D.C.');

-- China - Beijing
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Forbidden City', 'Beijing', NULL, 'China', 'Capital',
  'Capital of China. Seat of the world''s most populous nation and a center of technology.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Beijing');

-- Russia - Moscow
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Red Square', 'Moscow', NULL, 'Russia', 'Capital',
  'Capital of Russia. Historic and political center of the largest country by area.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Moscow');

-- United Kingdom - London (already seeded by V30, but ensure it has Capital type)
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Westminster', 'London', NULL, 'UK', 'Capital',
  'Capital of the United Kingdom. Major financial and diplomatic center.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'London' AND type = 'Capital');

-- Germany - Berlin
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Reichstag', 'Berlin', NULL, 'Germany', 'Capital',
  'Capital of Germany. Political heart of Europe''s largest economy.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Berlin');

-- France - Paris
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Elysee Palace', 'Paris', NULL, 'France', 'Capital',
  'Capital of France. Cultural and diplomatic center of Western Europe.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Paris');

-- Japan - Tokyo (already seeded by V30, but ensure it has Capital type)
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Imperial Palace', 'Tokyo', NULL, 'Japan', 'Capital',
  'Capital of Japan. Futuristic metropolis and technological leader.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Tokyo' AND type = 'Capital');

-- India - New Delhi
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'India Gate', 'New Delhi', NULL, 'India', 'Capital',
  'Capital of India. Political center of the world''s most populous democracy.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'New Delhi');

-- Brazil - Brasilia
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Planalto Palace', 'Brasilia', NULL, 'Brazil', 'Capital',
  'Capital of Brazil. Planned city and political center of South America.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Brasilia');

-- Canada - Ottawa
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Parliament Hill', 'Ottawa', 'ON', 'Canada', 'Capital',
  'Capital of Canada. Political center of North America''s second-largest country.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Ottawa');

-- Australia - Canberra
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Parliament House', 'Canberra', 'ACT', 'Australia', 'Capital',
  'Capital of Australia. Planned capital city of the island continent.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Canberra');

-- South Korea - Seoul
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Gyeongbokgung Palace', 'Seoul', NULL, 'South Korea', 'Capital',
  'Capital of South Korea. Technological powerhouse and cultural hub of East Asia.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Seoul');

-- Mexico - Mexico City
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'National Palace', 'Mexico City', NULL, 'Mexico', 'Capital',
  'Capital of Mexico. One of the largest cities in the world with rich cultural heritage.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Mexico City');

-- Italy - Rome
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Quirinal Palace', 'Rome', NULL, 'Italy', 'Capital',
  'Capital of Italy. Ancient city and cultural leader of Southern Europe.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Rome');

-- Spain - Madrid
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Royal Palace', 'Madrid', NULL, 'Spain', 'Capital',
  'Capital of Spain. Cultural and political center of the Iberian Peninsula.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Madrid');

-- Nigeria - Abuja
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Aso Rock', 'Abuja', NULL, 'Nigeria', 'Capital',
  'Capital of Nigeria. Political center of Africa''s most populous nation.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Abuja');

-- Saudi Arabia - Riyadh
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Masmak Fortress', 'Riyadh', NULL, 'Saudi Arabia', 'Capital',
  'Capital of Saudi Arabia. Political and economic center of the Middle East.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Riyadh');

-- Turkey - Ankara
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Ankara Castle', 'Ankara', NULL, 'Turkey', 'Capital',
  'Capital of Turkey. Strategic city bridging Europe and Asia.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Ankara');

-- Argentina - Buenos Aires
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Casa Rosada', 'Buenos Aires', NULL, 'Argentina', 'Capital',
  'Capital of Argentina. Cultural and political center of the Rio de la Plata region.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Buenos Aires');

-- South Africa - Pretoria
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Union Buildings', 'Pretoria', NULL, 'South Africa', 'Capital',
  'Administrative capital of South Africa. Political center of the most industrialized African nation.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Pretoria');

-- Indonesia - Jakarta
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'Istana Merdeka', 'Jakarta', NULL, 'Indonesia', 'Capital',
  'Capital of Indonesia. Political center of the world''s largest archipelago nation.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Jakarta');

-- European Union - Brussels
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret)
SELECT 'European Commission', 'Brussels', NULL, 'Belgium', 'Capital',
  'De facto capital of the European Union. Seat of the EU institutions and international diplomacy.', FALSE, NULL, FALSE
WHERE NOT EXISTS (SELECT 1 FROM earth2026_locations WHERE city = 'Brussels');

-- Update Earth planet location count
UPDATE earth2026_planets SET location_count = (SELECT COUNT(*) FROM earth2026_locations) WHERE id = 1;

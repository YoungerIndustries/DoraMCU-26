-- V37: Seed Earth 2026 transport routes between all capital cities
-- Routes connect capitals via Younger Industries offices, allowing players
-- to book transportation between any two connected locations.

-- 1. Create the earth2026_transport_routes table
CREATE TABLE earth2026_transport_routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    from_location_id BIGINT NOT NULL,
    to_location_id BIGINT NOT NULL,
    from_city VARCHAR(100) NOT NULL,
    to_city VARCHAR(100) NOT NULL,
    from_country VARCHAR(100) NOT NULL,
    to_country VARCHAR(100) NOT NULL,
    planet_id BIGINT NOT NULL,
    vehicle_type VARCHAR(100) NOT NULL,
    travel_time_minutes INT NOT NULL,
    ticket_cost BIGINT NOT NULL,
    min_tech_level VARCHAR(10) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    bidirectional BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_transport_route_from ON earth2026_transport_routes(from_location_id);
CREATE INDEX idx_transport_route_to ON earth2026_transport_routes(to_location_id);
CREATE INDEX idx_transport_route_planet ON earth2026_transport_routes(planet_id);

-- 2. Seed transport routes connecting all 22 capitals
-- Routes are bidirectional by default, so each INSERT creates travel in both directions.
-- Vehicle types reflect TL5 (current Earth technology) transportation.

-- North America routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Washington D.C. to Ottawa - North American Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Washington D.C.' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Ottawa' AND type = 'Capital' LIMIT 1),
       'Washington D.C.', 'Ottawa', 'USA', 'Canada', 1, 'Commercial Airliner',
       120, 500, 'TL5',
       'Direct flight connecting the US and Canadian capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Washington D.C. to Mexico City - Americas Corridor',
       (SELECT id FROM earth2026_locations WHERE city = 'Washington D.C.' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Mexico City' AND type = 'Capital' LIMIT 1),
       'Washington D.C.', 'Mexico City', 'USA', 'Mexico', 1, 'Commercial Airliner',
       180, 600, 'TL5',
       'Direct flight connecting the US and Mexican capitals. Operates via Younger Industries offices.';

-- Europe routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'London to Berlin - European Express',
       (SELECT id FROM earth2026_locations WHERE city = 'London' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Berlin' AND type = 'Capital' LIMIT 1),
       'London', 'Berlin', 'UK', 'Germany', 1, 'High-Speed Rail',
       180, 400, 'TL5',
       'High-speed rail connection between UK and German capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'London to Paris - Channel Express',
       (SELECT id FROM earth2026_locations WHERE city = 'London' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Paris' AND type = 'Capital' LIMIT 1),
       'London', 'Paris', 'UK', 'France', 1, 'High-Speed Rail',
       75, 350, 'TL5',
       'Eurostar high-speed rail between London and Paris. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Berlin to Brussels - EU Connector',
       (SELECT id FROM earth2026_locations WHERE city = 'Berlin' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Brussels' AND type = 'Capital' LIMIT 1),
       'Berlin', 'Brussels', 'Germany', 'Belgium', 1, 'High-Speed Rail',
       150, 380, 'TL5',
       'High-speed rail connecting Germany and the EU capital. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Paris to Rome - Mediterranean Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Paris' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Rome' AND type = 'Capital' LIMIT 1),
       'Paris', 'Rome', 'France', 'Italy', 1, 'Commercial Airliner',
       120, 420, 'TL5',
       'Direct flight between French and Italian capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Madrid to Brussels - Iberian Link',
       (SELECT id FROM earth2026_locations WHERE city = 'Madrid' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Brussels' AND type = 'Capital' LIMIT 1),
       'Madrid', 'Brussels', 'Spain', 'Belgium', 1, 'Commercial Airliner',
       120, 400, 'TL5',
       'Direct flight between Spanish and EU capitals. Operates via Younger Industries offices.';

-- Asia routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Tokyo to Seoul - East Asian Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Tokyo' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Seoul' AND type = 'Capital' LIMIT 1),
       'Tokyo', 'Seoul', 'Japan', 'South Korea', 1, 'Commercial Airliner',
       90, 380, 'TL5',
       'Direct flight between Japanese and Korean capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Beijing to Tokyo - Pacific Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Beijing' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Tokyo' AND type = 'Capital' LIMIT 1),
       'Beijing', 'Tokyo', 'China', 'Japan', 1, 'Commercial Airliner',
       210, 550, 'TL5',
       'Direct flight between Chinese and Japanese capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'New Delhi to Jakarta - South Asian Link',
       (SELECT id FROM earth2026_locations WHERE city = 'New Delhi' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Jakarta' AND type = 'Capital' LIMIT 1),
       'New Delhi', 'Jakarta', 'India', 'Indonesia', 1, 'Commercial Airliner',
       240, 500, 'TL5',
       'Direct flight between Indian and Indonesian capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Beijing to New Delhi - Asian Corridor',
       (SELECT id FROM earth2026_locations WHERE city = 'Beijing' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'New Delhi' AND type = 'Capital' LIMIT 1),
       'Beijing', 'New Delhi', 'China', 'India', 1, 'Commercial Airliner',
       240, 520, 'TL5',
       'Direct flight between Chinese and Indian capitals. Operates via Younger Industries offices.';

-- Russia and Middle East routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Moscow to Ankara - Eurasian Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Moscow' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Ankara' AND type = 'Capital' LIMIT 1),
       'Moscow', 'Ankara', 'Russia', 'Turkey', 1, 'Commercial Airliner',
       180, 480, 'TL5',
       'Direct flight between Russian and Turkish capitals. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Ankara to Riyadh - Middle East Link',
       (SELECT id FROM earth2026_locations WHERE city = 'Ankara' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Riyadh' AND type = 'Capital' LIMIT 1),
       'Ankara', 'Riyadh', 'Turkey', 'Saudi Arabia', 1, 'Commercial Airliner',
       150, 450, 'TL5',
       'Direct flight between Turkish and Saudi capitals. Operates via Younger Industries offices.';

-- South America routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Brasilia to Buenos Aires - South American Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Brasilia' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Buenos Aires' AND type = 'Capital' LIMIT 1),
       'Brasilia', 'Buenos Aires', 'Brazil', 'Argentina', 1, 'Commercial Airliner',
       240, 550, 'TL5',
       'Direct flight between Brazilian and Argentine capitals. Operates via Younger Industries offices.';

-- Africa routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Abuja to Pretoria - African Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Abuja' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Pretoria' AND type = 'Capital' LIMIT 1),
       'Abuja', 'Pretoria', 'Nigeria', 'South Africa', 1, 'Commercial Airliner',
       240, 500, 'TL5',
       'Direct flight between Nigerian and South African capitals. Operates via Younger Industries offices.';

-- Oceania routes
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Canberra to Tokyo - Pacific Link',
       (SELECT id FROM earth2026_locations WHERE city = 'Canberra' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Tokyo' AND type = 'Capital' LIMIT 1),
       'Canberra', 'Tokyo', 'Australia', 'Japan', 1, 'Commercial Airliner',
       480, 750, 'TL5',
       'Direct flight between Australian and Japanese capitals. Operates via Younger Industries offices.';

-- Intercontinental hub routes (connecting continents via major hubs)
INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Washington D.C. to London - Transatlantic Express',
       (SELECT id FROM earth2026_locations WHERE city = 'Washington D.C.' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'London' AND type = 'Capital' LIMIT 1),
       'Washington D.C.', 'London', 'USA', 'UK', 1, 'Commercial Airliner',
       330, 800, 'TL5',
       'Transatlantic flight connecting North America and Europe. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'London to Moscow - Eurasian Corridor',
       (SELECT id FROM earth2026_locations WHERE city = 'London' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Moscow' AND type = 'Capital' LIMIT 1),
       'London', 'Moscow', 'UK', 'Russia', 1, 'Commercial Airliner',
       240, 600, 'TL5',
       'Direct flight connecting Europe and Russia. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Washington D.C. to Brasilia - Americas Southern Route',
       (SELECT id FROM earth2026_locations WHERE city = 'Washington D.C.' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Brasilia' AND type = 'Capital' LIMIT 1),
       'Washington D.C.', 'Brasilia', 'USA', 'Brazil', 1, 'Commercial Airliner',
       420, 700, 'TL5',
       'Direct flight connecting North and South America. Operates via Younger Industries offices.';

INSERT INTO earth2026_transport_routes (name, from_location_id, to_location_id, from_city, to_city,
                                         from_country, to_country, planet_id, vehicle_type,
                                         travel_time_minutes, ticket_cost, min_tech_level, description)
SELECT 'Riyadh to Abuja - Middle East Africa Link',
       (SELECT id FROM earth2026_locations WHERE city = 'Riyadh' AND type = 'Capital' LIMIT 1),
       (SELECT id FROM earth2026_locations WHERE city = 'Abuja' AND type = 'Capital' LIMIT 1),
       'Riyadh', 'Abuja', 'Saudi Arabia', 'Nigeria', 1, 'Commercial Airliner',
       270, 550, 'TL5',
       'Direct flight connecting the Middle East and Africa. Operates via Younger Industries offices.';

-- Update planet template route count
UPDATE planet_templates SET route_count = (SELECT COUNT(*) FROM earth2026_transport_routes)
WHERE name = 'Earth 2026';

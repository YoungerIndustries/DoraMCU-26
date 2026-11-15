-- V36: Seed Younger Industries offices at all capital landing points
-- Each capital city gets a Younger Industries branch office providing
-- transportation booking, cargo services, and communications.

-- 1. Create the younger_offices table
CREATE TABLE younger_offices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    location_id BIGINT NOT NULL,
    planet_id BIGINT NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    services VARCHAR(200) NOT NULL,
    description TEXT,
    manager_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_younger_office_location ON younger_offices(location_id);
CREATE INDEX idx_younger_office_planet ON younger_offices(planet_id);

-- 2. Create the planet_templates table
CREATE TABLE planet_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    template_type VARCHAR(50) NOT NULL,
    description TEXT,
    country_count INT,
    capital_count INT,
    office_count INT,
    route_count INT,
    default_tech_level VARCHAR(10),
    includes_offices BOOLEAN NOT NULL DEFAULT TRUE,
    includes_routes BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    source_planet_id BIGINT
);

CREATE INDEX idx_planet_template_name ON planet_templates(name);
CREATE INDEX idx_planet_template_type ON planet_templates(template_type);

-- 3. Seed the Earth 2026 planet template
INSERT INTO planet_templates (name, template_type, description, country_count, capital_count,
                               office_count, route_count, default_tech_level, includes_offices, includes_routes, active, source_planet_id)
VALUES ('Earth 2026', 'Earth',
        'Default Earth-like planet template with 22 countries, capital cities, Younger Industries offices, and inter-capital transport routes. ' +
        'Suitable for creating fully-built planets in any galaxy.',
        22, 22, 22, 0, 'TL5', TRUE, TRUE, TRUE, 1);

-- 4. Seed Younger Industries offices at each capital landing point
-- Offices are linked to the capital locations seeded by V35 migration.

-- United States - Washington D.C.
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Washington D.C.', id, 1, 'Washington D.C.', 'USA',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the United States capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Washington D.C.' AND type = 'Capital';

-- China - Beijing
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Beijing', id, 1, 'Beijing', 'China',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the China capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Beijing' AND type = 'Capital';

-- Russia - Moscow
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Moscow', id, 1, 'Moscow', 'Russia',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Russia capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Moscow' AND type = 'Capital';

-- United Kingdom - London
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - London', id, 1, 'London', 'UK',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the UK capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'London' AND type = 'Capital';

-- Germany - Berlin
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Berlin', id, 1, 'Berlin', 'Germany',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Germany capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Berlin' AND type = 'Capital';

-- France - Paris
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Paris', id, 1, 'Paris', 'France',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the France capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Paris' AND type = 'Capital';

-- Japan - Tokyo
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Tokyo', id, 1, 'Tokyo', 'Japan',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Japan capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Tokyo' AND type = 'Capital';

-- India - New Delhi
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - New Delhi', id, 1, 'New Delhi', 'India',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the India capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'New Delhi' AND type = 'Capital';

-- Brazil - Brasilia
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Brasilia', id, 1, 'Brasilia', 'Brazil',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Brazil capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Brasilia' AND type = 'Capital';

-- Canada - Ottawa
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Ottawa', id, 1, 'Ottawa', 'Canada',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Canada capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Ottawa' AND type = 'Capital';

-- Australia - Canberra
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Canberra', id, 1, 'Canberra', 'Australia',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Australia capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Canberra' AND type = 'Capital';

-- South Korea - Seoul
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Seoul', id, 1, 'Seoul', 'South Korea',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the South Korea capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Seoul' AND type = 'Capital';

-- Mexico - Mexico City
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Mexico City', id, 1, 'Mexico City', 'Mexico',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Mexico capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Mexico City' AND type = 'Capital';

-- Italy - Rome
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Rome', id, 1, 'Rome', 'Italy',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Italy capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Rome' AND type = 'Capital';

-- Spain - Madrid
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Madrid', id, 1, 'Madrid', 'Spain',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Spain capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Madrid' AND type = 'Capital';

-- Nigeria - Abuja
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Abuja', id, 1, 'Abuja', 'Nigeria',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Nigeria capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Abuja' AND type = 'Capital';

-- Saudi Arabia - Riyadh
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Riyadh', id, 1, 'Riyadh', 'Saudi Arabia',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Saudi Arabia capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Riyadh' AND type = 'Capital';

-- Turkey - Ankara
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Ankara', id, 1, 'Ankara', 'Turkey',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Turkey capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Ankara' AND type = 'Capital';

-- Argentina - Buenos Aires
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Buenos Aires', id, 1, 'Buenos Aires', 'Argentina',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Argentina capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Buenos Aires' AND type = 'Capital';

-- South Africa - Pretoria
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Pretoria', id, 1, 'Pretoria', 'South Africa',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the South Africa capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Pretoria' AND type = 'Capital';

-- Indonesia - Jakarta
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Jakarta', id, 1, 'Jakarta', 'Indonesia',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the Indonesia capital. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Jakarta' AND type = 'Capital';

-- European Union - Brussels
INSERT INTO younger_offices (name, location_id, planet_id, city, country, services, description)
SELECT 'Younger Industries - Brussels', id, 1, 'Brussels', 'Belgium',
       'transport,cargo,communications,intergalactic_travel',
       'Younger Industries branch office serving the European Union. ' +
       'Provides transportation booking, cargo services, communications, and intergalactic travel services.'
FROM earth2026_locations WHERE city = 'Brussels' AND type = 'Capital';

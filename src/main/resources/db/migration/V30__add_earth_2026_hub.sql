-- Earth 2026 Hub: Add tables for Earth 2026 characters, locations, and scenarios
-- This migration creates the single-planet Earth hub where all new characters start
-- before transitioning to other game galaxies

-- Earth 2026 Characters table
CREATE TABLE earth2026_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL DEFAULT 25,
    occupation VARCHAR(100),
    background TEXT,
    str_score INT NOT NULL DEFAULT 10,
    dex_score INT NOT NULL DEFAULT 10,
    con_score INT NOT NULL DEFAULT 10,
    int_score INT NOT NULL DEFAULT 10,
    wis_score INT NOT NULL DEFAULT 10,
    cha_score INT NOT NULL DEFAULT 10,
    hit_points INT NOT NULL DEFAULT 10,
    max_hit_points INT NOT NULL DEFAULT 10,
    experience_points INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    earth_location VARCHAR(200),
    arrival_date_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    supernatural_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    sanity_score INT,
    target_galaxy VARCHAR(100),
    has_transitioned BOOLEAN NOT NULL DEFAULT FALSE,
    equipment TEXT,
    conditions JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME
);

CREATE INDEX idx_earth2026_character_name ON earth2026_characters(name);

-- Earth 2026 Locations table
CREATE TABLE earth2026_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    has_portal BOOLEAN NOT NULL DEFAULT FALSE,
    portal_galaxy VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DOUBLE,
    longitude DOUBLE
);

CREATE INDEX idx_earth2026_location_name ON earth2026_locations(name);
CREATE INDEX idx_earth2026_location_city ON earth2026_locations(city);

-- Earth 2026 Scenarios table
CREATE TABLE earth2026_scenarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    difficulty INT NOT NULL DEFAULT 1,
    min_level INT NOT NULL DEFAULT 1,
    xp_reward INT NOT NULL DEFAULT 0,
    location_id BIGINT,
    supernatural BOOLEAN NOT NULL DEFAULT FALSE,
    sanity_loss INT,
    party_size INT,
    duration_minutes INT,
    started_at DATETIME,
    completed_at DATETIME,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    FOREIGN KEY (location_id) REFERENCES earth2026_locations(id)
);

CREATE INDEX idx_earth2026_scenario_name ON earth2026_scenarios(name);
CREATE INDEX idx_earth2026_scenario_status ON earth2026_scenarios(status);

-- Seed Earth locations
INSERT INTO earth2026_locations (name, city, state, country, type, description, has_portal, portal_galaxy, is_secret) VALUES
('San Diego Convention Center', 'San Diego', 'CA', 'USA', 'Landmark', 'Starting location for new characters. A bustling convention center where characters first arrive.', FALSE, NULL, FALSE),
('Younger Industries HQ', 'San Diego', 'CA', 'USA', 'Corporate', 'Headquarters of Younger Industries. CEO Sif Younger operates from here.', FALSE, NULL, FALSE),
('The Portal Room', 'San Diego', 'CA', 'USA', 'Portal', 'A secret room containing doors to other galaxies. Characters can choose their destination here.', TRUE, 'Milky Way', TRUE),
('Area 51', 'Rachel', 'NV', 'USA', 'Secret Base', 'Top-secret military base. Rumored to have alien technology and portals.', TRUE, 'The Void', TRUE),
('New York City', 'New York', 'NY', 'USA', 'City', 'The city that never sleeps. Full of opportunities and mysteries.', FALSE, NULL, FALSE),
('London', 'London', NULL, 'UK', 'City', 'Historic city with ancient secrets and modern technology.', FALSE, NULL, FALSE),
('Tokyo', 'Tokyo', NULL, 'Japan', 'City', 'Futuristic metropolis blending tradition with cutting-edge technology.', FALSE, NULL, FALSE),
('Cthulhu''s Call', 'Arkham', 'MA', 'USA', 'Supernatural', 'A mysterious location where the veil between worlds is thin.', TRUE, 'Earth 2026', TRUE);

-- Seed Earth scenarios
INSERT INTO earth2026_scenarios (name, description, type, difficulty, min_level, xp_reward, location_id, supernatural) VALUES
('The Missing Package', 'A package has gone missing from Younger Industries. Investigate and recover it.', 'Investigation', 2, 1, 50, 2, FALSE),
('Convention Mystery', 'Strange things are happening at the convention center. Discover what''s going on.', 'Mystery', 3, 1, 75, 1, FALSE),
('First Contact', 'An alien artifact has been discovered. Determine its origin and purpose.', 'Supernatural', 5, 3, 150, 4, TRUE),
('Street Smarts', 'Navigate the streets of New York and complete a series of challenges.', 'Social', 2, 1, 50, 5, FALSE),
('The Portal Guardian', 'A mysterious guardian blocks the portal. Prove yourself worthy to pass through.', 'Combat', 4, 2, 100, 3, FALSE),
('Whispers in the Dark', 'Strange whispers lead you to a hidden cult. Investigate before it''s too late.', 'Supernatural', 6, 4, 200, 8, TRUE);

-- Add Earth 2026 as the default galaxy for new users
-- (Handled in code: AuthenticationController defaults to Earth 2026)

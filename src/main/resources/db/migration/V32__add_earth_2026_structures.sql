-- V32: Add Earth 2026 structures table and seed WWII-era battle environment data

-- Structures table for buildings, fortifications, and installations
CREATE TABLE earth2026_structures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    location_id BIGINT NOT NULL,
    structure_type VARCHAR(100) NOT NULL,
    tech_level INT NOT NULL,
    defense_rating INT NOT NULL DEFAULT 50,
    max_capacity INT NOT NULL DEFAULT 10,
    current_capacity INT NOT NULL DEFAULT 0,
    provides_cover BOOLEAN NOT NULL DEFAULT FALSE,
    cover_bonus INT NOT NULL DEFAULT 0,
    is_strategic BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_earth2026_structure_location (location_id),
    INDEX idx_earth2026_structure_type (structure_type),
    INDEX idx_earth2026_structure_tl (tech_level)
);

-- Seed WWII-era structures for existing Earth 2026 locations
-- Assuming location IDs from V30 migration:
-- 1: San Diego Convention Center
-- 2: The Portal Room
-- 3: Area 51
-- 4: New York Times Square
-- 5: London Underground
-- 6: Berlin Brandenburg Gate
-- 7: Tokyo Shibuya Crossing
-- 8: Cthulhu's Call

-- San Diego (location 1) - WWII Pacific theater
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Naval Base Building', 1, 'Command Center', 4, 70, 20, TRUE, 3, TRUE, 'WWII-era naval command center'),
('Harbor Dock', 1, 'Port', 4, 40, 50, FALSE, 0, TRUE, 'Main harbor dock for ships'),
('Barracks Block A', 1, 'Barracks', 4, 50, 100, TRUE, 2, FALSE, 'Infantry barracks'),
('Airfield Runway', 1, 'Airfield', 4, 30, 10, FALSE, 0, TRUE, 'Military airfield runway');

-- Area 51 (location 3) - Secret military base
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Hangar 51', 3, 'Bunker', 6, 80, 30, TRUE, 5, TRUE, 'Secret aircraft hangar'),
('Control Tower', 3, 'Command Center', 5, 60, 15, TRUE, 3, TRUE, 'Flight operations control'),
('Underground Lab', 3, 'Research Lab', 6, 90, 20, TRUE, 4, TRUE, 'Classified research facility');

-- New York Times Square (location 4) - Urban combat zone
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Empire State Building', 4, 'City', 5, 60, 200, TRUE, 3, TRUE, 'Iconic skyscraper, strategic vantage point'),
('Times Square Plaza', 4, 'City', 5, 30, 500, FALSE, 0, FALSE, 'Open plaza, limited cover'),
('Subway Station', 4, 'Underground', 5, 50, 100, TRUE, 4, FALSE, 'Underground transit, good cover');

-- London Underground (location 5) - European theater
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Tube Station Command', 5, 'Command Center', 4, 65, 25, TRUE, 4, TRUE, 'Underground command post'),
('Bunker Complex', 5, 'Bunker', 4, 80, 50, TRUE, 5, TRUE, 'Reinforced underground bunker'),
('Bridge Crossing', 5, 'Bridge', 4, 40, 30, FALSE, 0, TRUE, 'Strategic bridge over Thames');

-- Berlin Brandenburg Gate (location 6) - Eastern front
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Brandenburg Gate', 6, 'City', 4, 45, 50, TRUE, 2, TRUE, 'Iconic gate, symbolic objective'),
('U-boat Pen', 6, 'Bunker', 4, 85, 40, TRUE, 5, TRUE, 'Reinforced concrete U-boat shelter'),
('Railway Station', 6, 'Command Center', 4, 55, 80, TRUE, 3, TRUE, 'Major transport hub');

-- Tokyo Shibuya Crossing (location 7) - Pacific island hopping
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Crossing Plaza', 7, 'City', 4, 35, 300, FALSE, 0, FALSE, 'Open crossing, minimal cover'),
('Fortification Wall', 7, 'Bunker', 4, 75, 20, TRUE, 4, TRUE, 'Defensive wall with firing positions'),
('Port Facility', 7, 'Port', 4, 50, 60, FALSE, 0, TRUE, 'Ship loading and supply port');

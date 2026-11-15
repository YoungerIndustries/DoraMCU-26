-- V44: Add home_structure_id to earth2026_characters and seed apartment structures

-- 1. Add home_structure_id column to characters table
ALTER TABLE earth2026_characters ADD COLUMN home_structure_id BIGINT;

-- 2. Seed apartment structures for San Diego locations
-- San Diego Convention Center (location 1) - starting area apartments
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Harbor View Apartments', 1, 'Apartment', 5, 20, 100, TRUE, 2, FALSE, 'Modern apartment complex near the convention center. This is where new characters arrive and make their home on Earth 2026.'),
('Gaslamp Quarter Loft', 1, 'Apartment', 5, 15, 50, TRUE, 1, FALSE, 'Stylish loft apartment in the historic Gaslamp Quarter of San Diego.'),
('La Jolla Beach House', 1, 'Apartment', 6, 25, 30, TRUE, 2, FALSE, 'Upscale beachfront residence in La Jolla, San Diego.');

-- Younger Industries HQ (location 2) - corporate housing
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Younger Industries Dormitory', 2, 'Apartment', 6, 40, 80, TRUE, 3, FALSE, 'Corporate housing for Younger Industries employees and associates.');

-- The Portal Room (location 3) - secret safehouse
INSERT INTO earth2026_structures (name, location_id, structure_type, tech_level, defense_rating, max_capacity, provides_cover, cover_bonus, is_strategic, description) VALUES
('Portal Safehouse', 3, 'Apartment', 6, 50, 10, TRUE, 4, TRUE, 'A hidden safehouse near the portal room. Used by those who know the secrets.');

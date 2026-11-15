-- V54: Add entity-linked AI character fields for ships, stations, vehicles, and facilities
-- These fields link AI characters to game entities, representing their computer modules
-- as connections to the AI network

ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_ship_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_station_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_vehicle_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_facility_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_entity_id VARCHAR(100);

-- Add indexes for entity-linked AI character queries
-- Supports efficient lookups by entity ID and network topology queries

CREATE INDEX IF NOT EXISTS idx_ai_chars_linked_ship ON ai_characters(linked_ship_id);
CREATE INDEX IF NOT EXISTS idx_ai_chars_linked_station ON ai_characters(linked_station_id);
CREATE INDEX IF NOT EXISTS idx_ai_chars_linked_vehicle ON ai_characters(linked_vehicle_id);
CREATE INDEX IF NOT EXISTS idx_ai_chars_linked_facility ON ai_characters(linked_facility_id);
CREATE INDEX IF NOT EXISTS idx_ai_chars_linked_entity ON ai_characters(linked_entity_id);
CREATE INDEX IF NOT EXISTS idx_ai_chars_type_empire_active ON ai_characters(character_type, empire_name, active);

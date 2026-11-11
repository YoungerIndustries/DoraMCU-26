-- V24: Add character home location tracking
-- Enables persistent home location for characters across galaxies,
-- so characters return to their home when not actively in a session.

-- Add home location columns to DoraT characters
ALTER TABLE DoraT_characters ADD COLUMN IF NOT EXISTS home_location_id BIGINT;
ALTER TABLE DoraT_characters ADD COLUMN IF NOT EXISTS home_location_type VARCHAR(50);
ALTER TABLE DoraT_characters ADD COLUMN IF NOT EXISTS is_ai_controlled BOOLEAN DEFAULT FALSE;

-- Add home location columns to Captain's Log characters
ALTER TABLE cl_characters ADD COLUMN IF NOT EXISTS home_location_id VARCHAR(255);
ALTER TABLE cl_characters ADD COLUMN IF NOT EXISTS home_location_type VARCHAR(50);
ALTER TABLE cl_characters ADD COLUMN IF NOT EXISTS is_ai_controlled BOOLEAN DEFAULT FALSE;

-- Add galaxy tracking to crew members
ALTER TABLE cl_crew_members ADD COLUMN IF NOT EXISTS galaxy_name VARCHAR(255) DEFAULT 'Milky Way';
ALTER TABLE cl_crew_members ADD COLUMN IF NOT EXISTS owner_player_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_cl_crew_galaxy ON cl_crew_members(galaxy_name);
CREATE INDEX IF NOT EXISTS idx_cl_crew_owner ON cl_crew_members(owner_player_id);

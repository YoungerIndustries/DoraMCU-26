-- V101: Add 3D position fields to ai_characters for spatial visibility in the game world

-- Add spatial coordinate columns (nullable to allow gradual adoption)
ALTER TABLE ai_characters ADD COLUMN pos_x DOUBLE;
ALTER TABLE ai_characters ADD COLUMN pos_y DOUBLE;
ALTER TABLE ai_characters ADD COLUMN pos_z DOUBLE;

-- Add galaxy_name for spatial scoping (separate from empire_name for flexibility)
ALTER TABLE ai_characters ADD COLUMN galaxy_name VARCHAR(100);

-- Composite index for proximity queries: galaxy + admin flag + active status
CREATE INDEX idx_ai_character_galaxy_admin_active ON ai_characters (galaxy_name, ai_admin, active);

-- Backfill galaxy_name from empire_name for existing records
UPDATE ai_characters SET galaxy_name = empire_name WHERE galaxy_name IS NULL;

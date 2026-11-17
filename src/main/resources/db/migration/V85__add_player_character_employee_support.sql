-- V85: Add player character employee support for corporation HUD login flow
-- Adds is_main and module columns to earth2026_characters for character roster management
-- Adds office_id for HQ office assignment tracking
-- Adds player_character_id to corporate_hiring and makes ai_character_id nullable
-- for player characters registered as corporation employees

-- 1. Add is_main flag to earth2026_characters (marks the player's primary character)
ALTER TABLE earth2026_characters ADD COLUMN IF NOT EXISTS is_main BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Add module column to earth2026_characters (game module assignment)
ALTER TABLE earth2026_characters ADD COLUMN IF NOT EXISTS module VARCHAR(50) DEFAULT 'EARTH_2026';

-- 3. Add office_id to earth2026_characters (HQ office assignment)
ALTER TABLE earth2026_characters ADD COLUMN IF NOT EXISTS office_id BIGINT;

-- 4. Add player_character_id to corporate_hiring (for player character employees)
ALTER TABLE corporate_hiring ADD COLUMN IF NOT EXISTS player_character_id BIGINT;

-- 5. Make ai_character_id nullable to allow player characters (mutually exclusive with player_character_id)
ALTER TABLE corporate_hiring MODIFY COLUMN ai_character_id BIGINT NULL;

-- 6. Add indexes for the new columns
CREATE INDEX IF NOT EXISTS idx_earth2026_char_is_main ON earth2026_characters(is_main);
CREATE INDEX IF NOT EXISTS idx_earth2026_char_module ON earth2026_characters(module);
CREATE INDEX IF NOT EXISTS idx_earth2026_char_office ON earth2026_characters(office_id);
CREATE INDEX IF NOT EXISTS idx_corporate_hiring_player_char ON corporate_hiring(player_character_id);

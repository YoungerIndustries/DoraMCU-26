-- V51: Add ai_character_id to e26_monsters for AI-driven monster behavior
ALTER TABLE e26_monsters ADD COLUMN IF NOT EXISTS ai_character_id BIGINT;
CREATE INDEX idx_e26_monster_ai ON e26_monsters(ai_character_id);

-- V48: Extend ai_characters table for polymorphic AI support
-- Adds fields for companions, monsters, and NPCs to have AI-driven behavior

ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS character_type VARCHAR(20) DEFAULT 'HUMAN_LEADER';
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS species_id VARCHAR(50);
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS companion_type VARCHAR(20);
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_monster_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS linked_npc_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS bond_score INT DEFAULT 0;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS training_level VARCHAR(20) DEFAULT 'UNTRAINED';
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS owner_character_id BIGINT;
ALTER TABLE ai_characters ADD COLUMN IF NOT EXISTS personality_traits VARCHAR(500);

-- Set existing records to HUMAN_LEADER type
UPDATE ai_characters SET character_type = 'HUMAN_LEADER' WHERE character_type IS NULL;

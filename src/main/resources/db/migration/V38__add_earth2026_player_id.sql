-- Add player_id column to earth2026_characters to link characters to their owning users
-- This enables proper galaxy transitions with real user associations in PlayerCharacterRegistry

ALTER TABLE earth2026_characters
    ADD COLUMN player_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Reference to the owning Player (User). Used for galaxy transitions.';

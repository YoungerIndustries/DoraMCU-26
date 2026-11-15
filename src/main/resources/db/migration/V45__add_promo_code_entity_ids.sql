-- Add corporation_id and character_id columns to promo_codes table
-- These fields allow CORPORATION and CHARACTER code types to reference their associated entities

ALTER TABLE promo_codes ADD COLUMN corporation_id BIGINT;
ALTER TABLE promo_codes ADD COLUMN character_id BIGINT;

-- Seed example corporation code tied to a player (corporationId references the player table id)
-- This code will navigate the player to their corporation screen when validated
INSERT INTO promo_codes (code, description, code_type, max_uses, used_count, active, corporation_id)
VALUES ('CORP001', 'Corporation access code — takes you to your corporation screen', 'CORPORATION', NULL, 0, 1, NULL);

-- Seed example character code tied to a character (characterId references the earth2026_characters table id)
-- This code will land the player on Earth with the associated character when validated
INSERT INTO promo_codes (code, description, code_type, max_uses, used_count, active, character_id)
VALUES ('CHAR001', 'Character access code — lands you on Earth with your character', 'CHARACTER', NULL, 0, 1, NULL);

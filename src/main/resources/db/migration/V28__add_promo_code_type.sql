-- Add code_type column to promo_codes to separate ADMIN and PLAYER promo codes
-- Default existing codes to PLAYER for backward compatibility
ALTER TABLE promo_codes ADD COLUMN code_type VARCHAR(10) NOT NULL DEFAULT 'PLAYER';

-- Update existing seed codes to PLAYER type
UPDATE promo_codes SET code_type = 'PLAYER' WHERE code IN ('WELCOME2026', 'EARLYBIRD');

-- Insert RESET admin promo code - triggers full galaxy reset and repopulation
INSERT INTO promo_codes (code, description, code_type, maxUses, used_count, active)
VALUES ('RESET', 'Admin reset code - cleans galaxy, characters, and player data then repopulates seed data', 'ADMIN', NULL, 0, TRUE);

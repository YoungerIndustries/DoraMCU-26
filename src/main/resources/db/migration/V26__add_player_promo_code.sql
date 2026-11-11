-- Add promo_code column to players table for tracking registration promo codes
ALTER TABLE players ADD COLUMN promo_code VARCHAR(50);

-- Add unique constraint to ensure promo codes are only used once
ALTER TABLE players ADD CONSTRAINT uk_player_promo_code UNIQUE (promo_code);

-- Add display_name column to players table for player display names
ALTER TABLE players ADD COLUMN display_name VARCHAR(255);

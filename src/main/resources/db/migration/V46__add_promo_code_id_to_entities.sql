-- V46: Add promo_code_id columns to players, earth2026_characters, and eve_corporations tables
-- and backfill existing records with generated promo codes so they can be used for landing.

-- 1. Add player_id column to promo_codes table (for PLAYER-type codes)
ALTER TABLE promo_codes ADD COLUMN player_id BIGINT;

-- 2. Add promo_code_id column to players table
ALTER TABLE players ADD COLUMN promo_code_id BIGINT;

-- 3. Add promo_code_id column to earth2026_characters table
ALTER TABLE earth2026_characters ADD COLUMN promo_code_id BIGINT;

-- 4. Add promo_code_id column to eve_corporations table
ALTER TABLE eve_corporations ADD COLUMN promo_code_id BIGINT;

-- 5. Backfill: Generate PLAYER-type promo codes for existing players that don't have one
-- Each player gets a permanent landing key based on their empire name
INSERT INTO promo_codes (code, description, codeType, maxUses, used_count, active, player_id)
SELECT
    'PL' || SUBSTR(REPLACE(REPLACE(COALESCE(p.empire_name, p.email), ' ', '_'), '@', '_'), 1, 8) || SUBSTR(SHA1(CAST(p.id AS VARCHAR)), 1, 4),
    'Player landing key for ' || COALESCE(p.empire_name, p.email),
    'PLAYER',
    NULL,
    0,
    TRUE,
    p.id
FROM players p
WHERE NOT EXISTS (
    SELECT 1 FROM promo_codes pc WHERE pc.player_id = p.id AND pc.codeType = 'PLAYER'
);

-- 6. Update players.promo_code_id to reference the generated promo codes
UPDATE players SET promo_code_id = (
    SELECT pc.id FROM promo_codes pc WHERE pc.player_id = players.id AND pc.codeType = 'PLAYER' LIMIT 1
) WHERE EXISTS (
    SELECT 1 FROM promo_codes pc WHERE pc.player_id = players.id AND pc.codeType = 'PLAYER'
);

-- 7. Backfill: Generate CORPORATION-type promo codes for existing corporations
INSERT INTO promo_codes (code, description, codeType, maxUses, used_count, active, corporation_id)
SELECT
    'CO' || COALESCE(ec.ticker, 'CORP') || SUBSTR(SHA1(CAST(ec.id AS VARCHAR)), 1, 4),
    'Corporation landing key for ' || ec.name,
    'CORPORATION',
    NULL,
    0,
    TRUE,
    ec.id
FROM eve_corporations ec
WHERE NOT EXISTS (
    SELECT 1 FROM promo_codes pc WHERE pc.corporation_id = ec.id AND pc.codeType = 'CORPORATION'
);

-- 8. Update eve_corporations.promo_code_id to reference the generated promo codes
UPDATE eve_corporations SET promo_code_id = (
    SELECT pc.id FROM promo_codes pc WHERE pc.corporation_id = eve_corporations.id AND pc.codeType = 'CORPORATION' LIMIT 1
) WHERE EXISTS (
    SELECT 1 FROM promo_codes pc WHERE pc.corporation_id = eve_corporations.id AND pc.codeType = 'CORPORATION'
);

-- 9. Backfill: Generate CHARACTER-type promo codes for existing Earth 2026 characters
INSERT INTO promo_codes (code, description, codeType, maxUses, used_count, active, character_id)
SELECT
    'CH' || SUBSTR(REPLACE(REPLACE(COALESCE(ec2.name, 'CHAR'), ' ', '_'), '@', '_'), 1, 6) || SUBSTR(SHA1(CAST(ec2.id AS VARCHAR)), 1, 4),
    'Character landing key for ' || COALESCE(ec2.name, 'Unknown'),
    'CHARACTER',
    NULL,
    0,
    TRUE,
    ec2.id
FROM earth2026_characters ec2
WHERE NOT EXISTS (
    SELECT 1 FROM promo_codes pc WHERE pc.character_id = ec2.id AND pc.codeType = 'CHARACTER'
) AND ec2.has_transitioned = FALSE;

-- 10. Update earth2026_characters.promo_code_id to reference the generated promo codes
UPDATE earth2026_characters SET promo_code_id = (
    SELECT pc.id FROM promo_codes pc WHERE pc.character_id = earth2026_characters.id AND pc.codeType = 'CHARACTER' LIMIT 1
) WHERE EXISTS (
    SELECT 1 FROM promo_codes pc WHERE pc.character_id = earth2026_characters.id AND pc.codeType = 'CHARACTER'
);

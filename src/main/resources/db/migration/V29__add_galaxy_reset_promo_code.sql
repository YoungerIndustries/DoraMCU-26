-- Insert GALAXY_RESET admin promo code - clears all galaxy data but retains players and characters, then regenerates Milky Way
INSERT INTO promo_codes (code, description, code_type, maxUses, used_count, active)
VALUES ('GALAXY_RESET', 'Admin galaxy reset - cleans galaxy database but retains all characters and players, then regenerates Milky Way', 'ADMIN', NULL, 0, TRUE);

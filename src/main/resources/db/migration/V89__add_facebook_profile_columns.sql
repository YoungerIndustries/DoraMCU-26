-- V89: Add Facebook profile columns to players table
-- Supports Facebook login integration: users provide their Facebook profile URL or ID,
-- and the system fetches public profile data via the Facebook Graph API.

-- 1. Facebook user ID linked to this player (unique constraint prevents duplicate linking)
ALTER TABLE players ADD COLUMN facebook_profile_id VARCHAR(64) NULL;

-- 2. Full Facebook profile URL for this player
ALTER TABLE players ADD COLUMN facebook_profile_url VARCHAR(500) NULL;

-- 3. URL of the player's profile picture from Facebook
ALTER TABLE players ADD COLUMN profile_picture_url VARCHAR(500) NULL;

-- 4. Player's location from Facebook profile
ALTER TABLE players ADD COLUMN location VARCHAR(200) NULL;

-- 5. Player's bio/about section from Facebook profile
ALTER TABLE players ADD COLUMN bio VARCHAR(1000) NULL;

-- 6. Player's birthday from Facebook profile
ALTER TABLE players ADD COLUMN birthday VARCHAR(50) NULL;

-- 7. Add unique index on facebook_profile_id to enforce one-to-one linkage
CREATE UNIQUE INDEX uk_players_facebook_profile_id ON players(facebook_profile_id);

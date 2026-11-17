-- V109: Advanced quest system
-- Adds: quest chains, multi-step quests, rich rewards (credits, items, reputation, unlocks)

-- ============================================================================
-- Extend ai_admin_quests with chain and reward fields
-- ============================================================================

ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS parent_quest_id BIGINT;
ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS chain_id VARCHAR(50);
ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS chain_order INT;
ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS reward_credits INT DEFAULT 0;
ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS reward_reputation INT DEFAULT 0;
ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS reward_item_id VARCHAR(100);
ALTER TABLE ai_admin_quests ADD COLUMN IF NOT EXISTS reward_title VARCHAR(100);

-- Foreign key for quest chain ordering
ALTER TABLE ai_admin_quests ADD CONSTRAINT fk_quest_parent
    FOREIGN KEY (parent_quest_id) REFERENCES ai_admin_quests(id);

-- Indexes for chain queries
CREATE INDEX IF NOT EXISTS idx_quest_chain_id ON ai_admin_quests(chain_id);
CREATE INDEX IF NOT EXISTS idx_quest_parent_id ON ai_admin_quests(parent_quest_id);

-- ============================================================================
-- Quest steps table: multi-step quests with ordered sub-objectives
-- ============================================================================

CREATE TABLE IF NOT EXISTS ai_admin_quest_steps (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL REFERENCES ai_admin_quests(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    target_count INT DEFAULT 1,
    current_count INT DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quest_step_quest_id ON ai_admin_quest_steps(quest_id);
CREATE INDEX IF NOT EXISTS idx_quest_step_order ON ai_admin_quest_steps(quest_id, step_order);

-- ============================================================================
-- Quest rewards table: multiple rewards per quest
-- ============================================================================

CREATE TABLE IF NOT EXISTS ai_admin_quest_rewards (
    id BIGSERIAL PRIMARY KEY,
    quest_id BIGINT NOT NULL REFERENCES ai_admin_quests(id) ON DELETE CASCADE,
    reward_type VARCHAR(50) NOT NULL,
    reward_value INT DEFAULT 0,
    reward_item_id VARCHAR(100),
    reward_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quest_reward_quest_id ON ai_admin_quest_rewards(quest_id);

-- ============================================================================
-- Seed example quest chain: "Galaxy Defense Initiative" (3-part chain)
-- Given by US Military Commander (ID 57)
-- ============================================================================

-- Chain quest 1: Reconnaissance
INSERT INTO ai_admin_quests (id, quest_giver_id, player_id, title, description, quest_type, status,
    xp_reward, disposition_reward, target_description, target_count, current_count,
    chain_id, chain_order, parent_quest_id,
    reward_credits, reward_reputation, reward_item_id, reward_title)
VALUES (
    10001, 57, NULL, 'Reconnaissance Mission',
    'Scout the perimeter of the Milky Way sector and report any anomalies.',
    'ESPIONAGE', 'ASSIGNED',
    25, 2, 'Anomalies detected', 3, 0,
    'galaxy-defense', 1, NULL,
    500, 5, NULL, 'Scout')
ON CONFLICT (id) DO NOTHING;

-- Chain quest 2: Mobilization (unlocked after quest 1)
INSERT INTO ai_admin_quests (id, quest_giver_id, player_id, title, description, quest_type, status,
    xp_reward, disposition_reward, target_description, target_count, current_count,
    chain_id, chain_order, parent_quest_id,
    reward_credits, reward_reputation, reward_item_id, reward_title)
VALUES (
    10002, 57, NULL, 'Fleet Mobilization',
    'Coordinate with local commanders to mobilize defense fleets.',
    'COMBAT', 'ASSIGNED',
    50, 3, 'Fleets mobilized', 5, 0,
    'galaxy-defense', 2, 10001,
    1000, 10, NULL, 'Tactician')
ON CONFLICT (id) DO NOTHING;

-- Chain quest 3: Engagement (unlocked after quest 2)
INSERT INTO ai_admin_quests (id, quest_giver_id, player_id, title, description, quest_type, status,
    xp_reward, disposition_reward, target_description, target_count, current_count,
    chain_id, chain_order, parent_quest_id,
    reward_credits, reward_reputation, reward_item_id, reward_title)
VALUES (
    10003, 57, NULL, 'Defensive Engagement',
    'Lead the defense against the threat and secure the sector.',
    'COMBAT', 'ASSIGNED',
    100, 5, 'Hostiles neutralized', 10, 0,
    'galaxy-defense', 3, 10002,
    2000, 20, 'shield_generator', 'Defender')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- Seed quest steps for multi-step quest example
-- ============================================================================

-- Steps for Reconnaissance Mission (quest 10001)
INSERT INTO ai_admin_quest_steps (quest_id, step_order, title, description, target_count, current_count, completed)
VALUES
(10001, 1, 'Gather Intelligence', 'Collect reports from local outposts.', 3, 0, FALSE),
(10001, 2, 'Analyze Data', 'Process the intelligence reports for patterns.', 1, 0, FALSE),
(10001, 3, 'Submit Report', 'Deliver the final reconnaissance report to command.', 1, 0, FALSE)
ON CONFLICT DO NOTHING;

-- Steps for Fleet Mobilization (quest 10002)
INSERT INTO ai_admin_quest_steps (quest_id, step_order, title, description, target_count, current_count, completed)
VALUES
(10002, 1, 'Contact Commanders', 'Reach out to local fleet commanders.', 5, 0, FALSE),
(10002, 2, 'Coordinate Resources', 'Allocate ships and supplies.', 3, 0, FALSE),
(10002, 3, 'Deploy Fleets', 'Position fleets at strategic points.', 5, 0, FALSE)
ON CONFLICT DO NOTHING;

-- Steps for Defensive Engagement (quest 10003)
INSERT INTO ai_admin_quest_steps (quest_id, step_order, title, description, target_count, current_count, completed)
VALUES
(10003, 1, 'Establish Perimeter', 'Set up defensive lines.', 4, 0, FALSE),
(10003, 2, 'Engage Hostiles', 'Fight and neutralize the threat.', 10, 0, FALSE),
(10003, 3, 'Secure Sector', 'Ensure the area is safe after combat.', 1, 0, FALSE)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- Seed quest rewards for rich reward examples
-- ============================================================================

INSERT INTO ai_admin_quest_rewards (quest_id, reward_type, reward_value, reward_item_id, reward_description)
VALUES
-- Reconnaissance Mission rewards
(10001, 'CREDITS', 500, NULL, '500 credits for successful reconnaissance'),
(10001, 'XP', 25, NULL, '25 experience points'),
(10001, 'DISPOSITION', 2, NULL, 'Improved relationship with Military Commander'),
(10001, 'REPUTATION', 5, NULL, '+5 reputation in the military community'),
(10001, 'TITLE_UNLOCK', 0, 'scout', 'Unlock the "Scout" title'),

-- Fleet Mobilization rewards
(10002, 'CREDITS', 1000, NULL, '1000 credits for fleet coordination'),
(10002, 'XP', 50, NULL, '50 experience points'),
(10002, 'DISPOSITION', 3, NULL, 'Improved relationship with Military Commander'),
(10002, 'REPUTATION', 10, NULL, '+10 reputation'),
(10002, 'ITEM_UNLOCK', 0, 'fleet_comm_badge', 'Unlock the Fleet Comm Badge item'),
(10002, 'TITLE_UNLOCK', 0, 'tactician', 'Unlock the "Tactician" title'),

-- Defensive Engagement rewards
(10003, 'CREDITS', 2000, NULL, '2000 credits for sector defense'),
(10003, 'XP', 100, NULL, '100 experience points'),
(10003, 'DISPOSITION', 5, NULL, 'Significantly improved relationship'),
(10003, 'REPUTATION', 20, NULL, '+20 reputation'),
(10003, 'ITEM_UNLOCK', 0, 'shield_generator', 'Unlock the Shield Generator item'),
(10003, 'TITLE_UNLOCK', 0, 'defender', 'Unlock the "Defender" title')
ON CONFLICT DO NOTHING;

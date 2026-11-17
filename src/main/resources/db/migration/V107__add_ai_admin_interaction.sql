-- V107: Add AI admin player interaction features
-- Enables dialogue, quests, and dynamic relationships between players and AI admins

-- Add disposition score to ai_characters for tracking relationships with players
ALTER TABLE ai_characters ADD COLUMN disposition_score INT DEFAULT 0;

-- Create ai_admin_quests table for quest tracking
CREATE TABLE ai_admin_quests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quest_giver_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    quest_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    xp_reward INT NOT NULL DEFAULT 10,
    disposition_reward INT NOT NULL DEFAULT 1,
    target_description VARCHAR(500),
    target_count INT NOT NULL DEFAULT 1,
    current_count INT NOT NULL DEFAULT 0,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (quest_giver_id) REFERENCES ai_characters(id),
    FOREIGN KEY (player_id) REFERENCES players(id)
);

-- Indexes for common queries
CREATE INDEX idx_ai_admin_quests_player ON ai_admin_quests(player_id);
CREATE INDEX idx_ai_admin_quests_giver ON ai_admin_quests(quest_giver_id);
CREATE INDEX idx_ai_admin_quests_status ON ai_admin_quests(status);

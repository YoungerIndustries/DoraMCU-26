-- D&D 5e campaign management tables
-- dnd5e_campaigns: campaign data with name, DM, setting, and active status
-- dnd5e_campaign_characters: links characters to campaigns with role tracking
-- dnd5e_sessions: individual play sessions within a campaign

CREATE TABLE dnd5e_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    dm_name VARCHAR(100) NOT NULL,
    setting_name VARCHAR(100),
    starting_level INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_dnd5e_campaigns_name ON dnd5e_campaigns(name);

CREATE TABLE dnd5e_campaign_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    character_id BIGINT NOT NULL,
    role VARCHAR(50),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES dnd5e_campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES dnd5e_characters(id) ON DELETE CASCADE
);

CREATE INDEX idx_dnd5e_campaign_characters_campaign ON dnd5e_campaign_characters(campaign_id);
CREATE INDEX idx_dnd5e_campaign_characters_character ON dnd5e_campaign_characters(character_id);

CREATE TABLE dnd5e_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    session_name VARCHAR(200) NOT NULL,
    session_number INT NOT NULL DEFAULT 1,
    session_date DATE NOT NULL,
    notes TEXT,
    xp_awarded INT NOT NULL DEFAULT 0,
    duration_minutes INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES dnd5e_campaigns(id) ON DELETE CASCADE
);

CREATE INDEX idx_dnd5e_sessions_campaign ON dnd5e_sessions(campaign_id);
CREATE INDEX idx_dnd5e_sessions_date ON dnd5e_sessions(session_date);

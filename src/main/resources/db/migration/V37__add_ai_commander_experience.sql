-- V37: Add AI commander experience tracking and hiring system

-- 1. Extend ai_characters table with experience and hiring fields
ALTER TABLE ai_characters
    ADD COLUMN total_experience INT NOT NULL DEFAULT 0,
    ADD COLUMN leadership_rating INT DEFAULT 5,
    ADD COLUMN total_battles INT NOT NULL DEFAULT 0,
    ADD COLUMN total_victories INT NOT NULL DEFAULT 0,
    ADD COLUMN hirable BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN daily_salary BIGINT DEFAULT 100,
    ADD COLUMN hiring_status VARCHAR(20) DEFAULT 'AVAILABLE',
    ADD COLUMN primary_skill VARCHAR(50);

-- 2. Create ai_commander_experiences table
CREATE TABLE ai_commander_experiences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_character_id BIGINT NOT NULL,
    game_module_name VARCHAR(50) NOT NULL,
    battle_type VARCHAR(50) NOT NULL,
    battle_description VARCHAR(500),
    battle_location VARCHAR(200),
    outcome VARCHAR(20) NOT NULL,
    experience_points INT NOT NULL DEFAULT 0,
    leadership_bonus INT DEFAULT 0,
    skill_improved VARCHAR(50),
    enemies_defeated INT DEFAULT 0,
    friendly_losses INT DEFAULT 0,
    exceptional_performance BOOLEAN DEFAULT FALSE,
    battle_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exp_ai_character FOREIGN KEY (ai_character_id) REFERENCES ai_characters(id)
);

CREATE INDEX idx_ai_commander_exp_char ON ai_commander_experiences(ai_character_id);
CREATE INDEX idx_ai_commander_exp_game ON ai_commander_experiences(game_module_name);
CREATE INDEX idx_ai_commander_exp_date ON ai_commander_experiences(battle_date);

-- 3. Create ai_commander_hiring table
CREATE TABLE ai_commander_hiring (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_character_id BIGINT NOT NULL,
    player_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    daily_salary BIGINT NOT NULL,
    contract_duration_days INT,
    hire_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP,
    battles_fought INT NOT NULL DEFAULT 0,
    victories INT NOT NULL DEFAULT 0,
    performance_rating DOUBLE DEFAULT 5.0,
    performance_notes VARCHAR(500),
    CONSTRAINT fk_hire_ai_character FOREIGN KEY (ai_character_id) REFERENCES ai_characters(id)
);

CREATE INDEX idx_ai_cmd_hire_player ON ai_commander_hiring(player_id);
CREATE INDEX idx_ai_cmd_hire_commander ON ai_commander_hiring(ai_character_id);
CREATE INDEX idx_ai_cmd_hire_status ON ai_commander_hiring(status);

-- 4. Seed: Mark existing AI characters as hirable with experience-based leadership
UPDATE ai_characters SET
    leadership_rating = CASE
        WHEN role = 'Leader' THEN 8
        WHEN role = 'Governor' THEN 6
        WHEN role = 'Captain' THEN 7
        ELSE 5
    END,
    primary_skill = CASE
        WHEN role = 'Leader' THEN 'Strategy'
        WHEN role = 'Governor' THEN 'Administration'
        WHEN role = 'Captain' THEN 'Tactics'
        ELSE NULL
    END,
    daily_salary = CASE
        WHEN role = 'Leader' THEN 500
        WHEN role = 'Governor' THEN 200
        WHEN role = 'Captain' THEN 300
        ELSE 100
    END;

-- 5. Mark key empire leaders as unavailable for hiring (they run the empire)
UPDATE ai_characters SET hirable = FALSE, hiring_status = 'UNAVAILABLE'
WHERE role = 'Leader' AND empire_name IN ('Interstellar Alliance');

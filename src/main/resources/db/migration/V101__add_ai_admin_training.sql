-- V101: Add AI admin training tables

CREATE TABLE ai_admin_training_scenarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    difficulty VARCHAR(15) NOT NULL,
    incident_type VARCHAR(20) NOT NULL,
    severity VARCHAR(10) NOT NULL,
    scenario_data TEXT NOT NULL,
    time_limit_minutes INT NOT NULL DEFAULT 30,
    xp_reward INT NOT NULL DEFAULT 50,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trnscn_difficulty ON ai_admin_training_scenarios(difficulty);
CREATE INDEX idx_trnscn_type ON ai_admin_training_scenarios(incident_type);
CREATE INDEX idx_trnscn_active ON ai_admin_training_scenarios(is_active);

CREATE TABLE ai_admin_training_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_admin_id BIGINT NOT NULL,
    scenario_id BIGINT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'NOT_STARTED',
    score INT NOT NULL DEFAULT 0,
    max_score INT NOT NULL DEFAULT 100,
    feedback TEXT,
    actions_taken TEXT,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trnsn_admin ON ai_admin_training_sessions(ai_admin_id);
CREATE INDEX idx_trnsn_scenario ON ai_admin_training_sessions(scenario_id);
CREATE INDEX idx_trnsn_status ON ai_admin_training_sessions(status);
CREATE INDEX idx_trnsn_created ON ai_admin_training_sessions(created_at);

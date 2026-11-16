-- V69: Add character training tours and crew management tables
-- Supports the Character Training Panel and Crew system in the corporation module.
-- New tables: character_training_tour, crew, crew_member
-- Schema changes: corporate_hiring.crew_id, corporate_contracts.crew_id

-- 1. Character training tour table
CREATE TABLE IF NOT EXISTS character_training_tour (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL,
    character_type VARCHAR(20) NOT NULL,
    traveller_character_id INT,
    ai_character_id BIGINT,
    hiring_id BIGINT,
    skill VARCHAR(50) NOT NULL,
    profession VARCHAR(50) NOT NULL,
    duration_years INT NOT NULL,
    turns_elapsed INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    skill_level_before INT,
    skill_level_after INT,
    xp_awarded INT,
    credits_cost BIGINT NOT NULL,
    narrative TEXT,
    events_summary TEXT,
    age_at_start INT,
    age_at_end INT
);

CREATE INDEX idx_training_tour_corporation ON character_training_tour(corporation_id);
CREATE INDEX idx_training_tour_status ON character_training_tour(status);
CREATE INDEX idx_training_tour_traveller ON character_training_tour(traveller_character_id);
CREATE INDEX idx_training_tour_ai_character ON character_training_tour(ai_character_id);

-- 2. Crew table
CREATE TABLE IF NOT EXISTS crew (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    leader_hiring_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contract_id BIGINT,
    description VARCHAR(500)
);

CREATE INDEX idx_crew_corporation ON crew(corporation_id);
CREATE INDEX idx_crew_status ON crew(status);
CREATE INDEX idx_crew_leader ON crew(leader_hiring_id);
CREATE INDEX idx_crew_contract ON crew(contract_id);

-- 3. Crew member table
CREATE TABLE IF NOT EXISTS crew_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crew_id BIGINT NOT NULL,
    hiring_id BIGINT NOT NULL,
    is_leader BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_crew_member_crew FOREIGN KEY (crew_id) REFERENCES crew(id)
);

CREATE INDEX idx_crew_member_crew ON crew_member(crew_id);
CREATE INDEX idx_crew_member_hiring ON crew_member(hiring_id);

-- 4. Add crew_id to corporate_hiring (links hire to crew; null = general employee pool)
ALTER TABLE corporate_hiring ADD COLUMN IF NOT EXISTS crew_id BIGINT;

-- 5. Add crew_id to corporate_contracts (links contract to assigned crew group)
ALTER TABLE corporate_contracts ADD COLUMN IF NOT EXISTS crew_id BIGINT;

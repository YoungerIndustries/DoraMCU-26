-- V49: Create e26_companions table for pets, familiars, and working animals
CREATE TABLE IF NOT EXISTS e26_companions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    species_id VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    owner_character_id BIGINT NOT NULL,
    ai_character_id BIGINT,
    companion_type VARCHAR(20) NOT NULL,
    str INT NOT NULL DEFAULT 10,
    dex INT NOT NULL DEFAULT 10,
    con INT NOT NULL DEFAULT 10,
    intel INT NOT NULL DEFAULT 10,
    wis INT NOT NULL DEFAULT 10,
    cha INT NOT NULL DEFAULT 10,
    hit_points INT NOT NULL DEFAULT 10,
    max_hit_points INT NOT NULL DEFAULT 10,
    bond_score INT DEFAULT 0,
    training_level VARCHAR(20) NOT NULL DEFAULT 'UNTRAINED',
    tricks TEXT,
    abilities TEXT,
    equipment TEXT,
    health VARCHAR(20) NOT NULL DEFAULT 'HEALTHY',
    location VARCHAR(20) NOT NULL DEFAULT 'FOLLOWS_OWNER',
    acquired_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_e26_companion_owner ON e26_companions(owner_character_id);
CREATE INDEX idx_e26_companion_species ON e26_companions(species_id);
CREATE INDEX idx_e26_companion_type ON e26_companions(companion_type);
CREATE INDEX idx_e26_companion_ai ON e26_companions(ai_character_id);

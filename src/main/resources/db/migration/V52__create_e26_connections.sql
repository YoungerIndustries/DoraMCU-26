-- V52: Create e26_connections table for universal relationship network
CREATE TABLE IF NOT EXISTS e26_connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_type VARCHAR(20) NOT NULL,
    source_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    connection_type VARCHAR(20) NOT NULL,
    strength INT DEFAULT 50,
    secrecy_level INT DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(1000)
);

CREATE INDEX idx_e26_connection_source ON e26_connections(source_type, source_id);
CREATE INDEX idx_e26_connection_target ON e26_connections(target_type, target_id);
CREATE INDEX idx_e26_connection_type ON e26_connections(connection_type);
CREATE INDEX idx_e26_connection_active ON e26_connections(active);

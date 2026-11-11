-- V31: Add Earth 2026 MMOG tables for parties, chat, and online status

-- Party table for group play
CREATE TABLE earth2026_parties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    location_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL,
    max_members INT NOT NULL DEFAULT 6,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    scenario_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    is_recruiting BOOLEAN NOT NULL DEFAULT TRUE
);

-- Indexes for parties
CREATE INDEX idx_earth2026_party_name ON earth2026_parties(name);
CREATE INDEX idx_earth2026_party_location ON earth2026_parties(location_id);

-- Chat messages table for public, private, and party messaging
CREATE TABLE earth2026_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT,
    location_id BIGINT,
    party_id BIGINT,
    content TEXT NOT NULL,
    message_type VARCHAR(10) NOT NULL DEFAULT 'public',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for chat
CREATE INDEX idx_earth2026_chat_sender ON earth2026_chat_messages(sender_id);
CREATE INDEX idx_earth2026_chat_location ON earth2026_chat_messages(location_id);
CREATE INDEX idx_earth2026_chat_created ON earth2026_chat_messages(created_at);

-- Online status table for real-time presence tracking
CREATE TABLE earth2026_online_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL UNIQUE,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    location_id BIGINT,
    activity VARCHAR(50) DEFAULT 'idle',
    last_seen_at TIMESTAMP NULL,
    connected_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

-- Indexes for online status
CREATE INDEX idx_earth2026_status_online ON earth2026_online_status(is_online);
CREATE INDEX idx_earth2026_status_location ON earth2026_online_status(location_id);

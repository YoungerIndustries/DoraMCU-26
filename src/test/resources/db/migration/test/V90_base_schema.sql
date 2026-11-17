-- Base schema for migration tests (H2-compatible)
-- Mirrors the state of ai_characters and ai_admin_orders tables after V40,
-- before V90 and V91 are applied.

CREATE TABLE ai_characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    empire_name VARCHAR(100) NOT NULL,
    linked_player_id VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    can_promote_to_co_server BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    total_experience INT NOT NULL DEFAULT 0,
    leadership_rating INT DEFAULT 5,
    total_battles INT DEFAULT 0,
    total_victories INT DEFAULT 0,
    hirable BOOLEAN NOT NULL DEFAULT TRUE,
    daily_salary BIGINT DEFAULT 100,
    ai_admin BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE ai_admin_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submitted_by VARCHAR(100) NOT NULL,
    order_text VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);

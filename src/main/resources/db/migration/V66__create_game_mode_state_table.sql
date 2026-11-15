-- Create game_mode_state table for persisting game mode module state across server restarts

CREATE TABLE game_mode_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    galaxy_name VARCHAR(100) NOT NULL,
    game_mode VARCHAR(50) NOT NULL,
    state_json TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_galaxy_mode (galaxy_name, game_mode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

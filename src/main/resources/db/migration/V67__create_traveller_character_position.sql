-- Create traveller_character_position table for persisting character hex sector positions across server restarts

CREATE TABLE traveller_character_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id VARCHAR(100) NOT NULL,
    hex_sector VARCHAR(20) NOT NULL,
    system_name VARCHAR(200),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_character_id (character_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create promo_codes table for managing valid promo codes
CREATE TABLE promo_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    maxUses INT,
    used_count INT NOT NULL DEFAULT 0,
    expiry_date DATETIME,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    bonuses VARCHAR(1000)
);

-- Insert default promo codes for initial testing
INSERT INTO promo_codes (code, description, maxUses, used_count, active)
VALUES ('WELCOME2026', 'Welcome bonus for new players in 2026', NULL, 0, TRUE);

INSERT INTO promo_codes (code, description, maxUses, used_count, active)
VALUES ('EARLYBIRD', 'Early bird special - first 100 players', 100, 0, TRUE);

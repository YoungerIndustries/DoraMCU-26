-- V72: Add office rental economy and HQ notification system
-- Supports rent payment tracking, eviction mechanics, and corporation notifications

CREATE TABLE IF NOT EXISTS office_rent_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL,
    office_unit_id BIGINT NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_type VARCHAR(30) NOT NULL DEFAULT 'RENT',
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    payment_date TIMESTAMP NULL,
    due_date TIMESTAMP NULL,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    late_fee DECIMAL(19, 4) NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orp_corporation ON office_rent_payments(corporation_id);
CREATE INDEX IF NOT EXISTS idx_orp_office_unit ON office_rent_payments(office_unit_id);
CREATE INDEX IF NOT EXISTS idx_orp_payment_date ON office_rent_payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_orp_status ON office_rent_payments(status);

CREATE TABLE IF NOT EXISTS hq_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    corporation_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    office_unit_id BIGINT NULL,
    meeting_id BIGINT NULL,
    rent_payment_id BIGINT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_hn_corporation ON hq_notifications(corporation_id);
CREATE INDEX IF NOT EXISTS idx_hn_type ON hq_notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_hn_read ON hq_notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_hn_created ON hq_notifications(created_at);

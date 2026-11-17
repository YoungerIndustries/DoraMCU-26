-- Create ai_admin_notifications table for AI admin dispatch notifications
CREATE TABLE ai_admin_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_admin_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    order_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL
);

-- Index for querying notifications by AI admin
CREATE INDEX idx_aan_admin ON ai_admin_notifications(ai_admin_id);

-- Index for filtering by notification type
CREATE INDEX idx_aan_type ON ai_admin_notifications(notification_type);

-- Index for unread notification queries
CREATE INDEX idx_aan_read ON ai_admin_notifications(is_read);

-- Index for ordering by creation time
CREATE INDEX idx_aan_created ON ai_admin_notifications(created_at);

-- Index for querying notifications by related order
CREATE INDEX idx_aan_order ON ai_admin_notifications(order_id);

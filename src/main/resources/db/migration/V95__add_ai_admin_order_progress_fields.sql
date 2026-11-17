-- Add progress tracking fields to ai_admin_orders for real-time order progress monitoring

ALTER TABLE ai_admin_orders
    ADD COLUMN progress_percent INT DEFAULT 0;

ALTER TABLE ai_admin_orders
    ADD COLUMN current_test_name VARCHAR(200) NULL;

ALTER TABLE ai_admin_orders
    ADD COLUMN tests_completed INT DEFAULT 0;

ALTER TABLE ai_admin_orders
    ADD COLUMN tests_total INT DEFAULT 0;

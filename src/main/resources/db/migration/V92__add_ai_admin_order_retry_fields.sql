-- Add retry tracking fields to ai_admin_orders for the dispatch retry mechanism
ALTER TABLE ai_admin_orders ADD COLUMN retry_count INT NOT NULL DEFAULT 0;
ALTER TABLE ai_admin_orders ADD COLUMN max_retries INT NOT NULL DEFAULT 3;

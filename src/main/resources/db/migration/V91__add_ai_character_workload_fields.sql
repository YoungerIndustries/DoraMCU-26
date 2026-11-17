-- V91: Add AI admin workload tracking fields to ai_characters table
-- Supports the AiAdminDispatchService for load balancing and order assignment tracking.

-- 1. ID of the current work order being processed by this AI admin (null when idle)
ALTER TABLE ai_characters ADD COLUMN current_order_id BIGINT NULL;

-- 2. Number of active orders currently assigned to this AI admin (default 0)
ALTER TABLE ai_characters ADD COLUMN active_order_count INT NOT NULL DEFAULT 0;

-- 3. Index for finding available AI admins (ai_admin = true, active = true, current_order_id IS NULL)
CREATE INDEX idx_ai_characters_admin_available ON ai_characters(ai_admin, active, current_order_id, active_order_count);

-- 4. Index for empire-scoped AI admin lookup
CREATE INDEX idx_ai_characters_admin_empire ON ai_characters(ai_admin, active, empire_name, current_order_id, active_order_count);

-- V90: Add AI admin dispatch fields to ai_admin_orders table
-- Supports the AiAdminDispatchService that routes pending work orders to available AI admin characters.

-- 1. ID of the AI admin character assigned to execute this order
ALTER TABLE ai_admin_orders ADD COLUMN assigned_ai_admin_id BIGINT NULL;

-- 2. Add index for efficient lookup of orders by assigned AI admin
CREATE INDEX idx_ai_admin_orders_assigned_admin ON ai_admin_orders(assigned_ai_admin_id);

-- 3. Add index for efficient dispatch scanning (status + unassigned filter)
CREATE INDEX idx_ai_admin_orders_status_assigned ON ai_admin_orders(status, assigned_ai_admin_id);

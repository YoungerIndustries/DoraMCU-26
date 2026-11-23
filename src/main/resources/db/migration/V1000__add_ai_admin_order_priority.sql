-- Add priority column to ai_admin_orders for galaxy-based dispatch ordering
ALTER TABLE ai_admin_orders ADD COLUMN priority INT DEFAULT 999;

-- Backfill Earth 2026 orders to priority 1 (highest priority)
UPDATE ai_admin_orders SET priority = 1 WHERE target_galaxy = 'Earth 2026';

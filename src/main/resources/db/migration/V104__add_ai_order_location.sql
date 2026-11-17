-- V104: Add location fields to AI admin orders and home position to AI characters
-- Enables order-aware positioning: orders can specify target locations,
-- and AI admins have home positions to return to after completing orders

-- ============================================================
-- Add target location fields to ai_admin_orders
-- ============================================================
ALTER TABLE ai_admin_orders ADD COLUMN target_galaxy VARCHAR(100);
ALTER TABLE ai_admin_orders ADD COLUMN target_pos_x DOUBLE;
ALTER TABLE ai_admin_orders ADD COLUMN target_pos_y DOUBLE;
ALTER TABLE ai_admin_orders ADD COLUMN target_pos_z DOUBLE;

-- ============================================================
-- Add home position fields to ai_characters
-- Home position is where the AI admin returns after completing an order
-- ============================================================
ALTER TABLE ai_characters ADD COLUMN home_pos_x DOUBLE;
ALTER TABLE ai_characters ADD COLUMN home_pos_y DOUBLE;
ALTER TABLE ai_characters ADD COLUMN home_pos_z DOUBLE;

-- ============================================================
-- Backfill home positions from current positions (V102 seeded data)
-- ============================================================
UPDATE ai_characters SET
  home_pos_x = pos_x,
  home_pos_y = pos_y,
  home_pos_z = pos_z
WHERE pos_x IS NOT NULL AND home_pos_x IS NULL;

-- Add production_chain_id to ai_admin_orders for linking orders to production chains
ALTER TABLE ai_admin_orders ADD COLUMN production_chain_id BIGINT;
CREATE INDEX idx_ai_admin_orders_production_chain_id ON ai_admin_orders(production_chain_id);

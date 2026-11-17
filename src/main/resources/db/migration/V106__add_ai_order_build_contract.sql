-- V106: Link AI admin orders to build contracts for economic production integration
-- Allows AI admins to execute build contracts (mine, process, assemble, construct, repair) as work orders

-- Add build_contract_id to ai_admin_orders
ALTER TABLE ai_admin_orders ADD COLUMN build_contract_id BIGINT;

-- Index for quick lookup of orders by build contract
CREATE INDEX idx_ai_admin_orders_build_contract ON ai_admin_orders(build_contract_id);

-- Add foreign key constraint (optional, as build contracts may be deleted independently)
ALTER TABLE ai_admin_orders
    ADD CONSTRAINT fk_ai_order_build_contract
    FOREIGN KEY (build_contract_id) REFERENCES build_contracts(id);

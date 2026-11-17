-- Add missing fields to production_chain_tracking table for Phase 6 controller compatibility

ALTER TABLE production_chain_tracking ADD COLUMN target_item_name VARCHAR(200);
ALTER TABLE production_chain_tracking ADD COLUMN target_item_type VARCHAR(50);
ALTER TABLE production_chain_tracking ADD COLUMN completed_contracts INT DEFAULT 0;
ALTER TABLE production_chain_tracking ADD COLUMN failed_contracts INT DEFAULT 0;
ALTER TABLE production_chain_tracking ADD COLUMN progress_percent INT DEFAULT 0;
ALTER TABLE production_chain_tracking ADD COLUMN phases_json TEXT;
ALTER TABLE production_chain_tracking ADD COLUMN galaxy_id BIGINT;
ALTER TABLE production_chain_tracking ADD COLUMN updated_at TIMESTAMP;

CREATE INDEX idx_production_chain_galaxy ON production_chain_tracking(galaxy_id);

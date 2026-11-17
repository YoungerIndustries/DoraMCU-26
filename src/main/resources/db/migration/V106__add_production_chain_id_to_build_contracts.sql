-- Add production_chain_id column to build_contracts table
-- Links each build contract to its parent production chain

ALTER TABLE build_contracts ADD COLUMN production_chain_id BIGINT;

CREATE INDEX idx_build_contracts_chain ON build_contracts(production_chain_id);

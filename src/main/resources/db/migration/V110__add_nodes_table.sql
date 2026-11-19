-- V110: Add nodes table for brain network device registration
-- Nodes are classified as Neuron (PDA), Node (tablet), or BrainCell (desktop)
-- They register with the BrainStem and participate in the distributed game server network

CREATE TABLE IF NOT EXISTS nodes (
    id BIGSERIAL PRIMARY KEY,
    node_id VARCHAR(255) UNIQUE NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    cpu_cores INTEGER,
    ram_mb INTEGER,
    gpu_tier VARCHAR(20),
    ip_address VARCHAR(45),
    port INTEGER,
    status VARCHAR(50) DEFAULT 'active',
    registered_at TIMESTAMP WITHOUT TIME ZONE,
    last_heartbeat TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_nodes_node_id ON nodes(node_id);
CREATE INDEX IF NOT EXISTS idx_nodes_device_id ON nodes(device_id);
CREATE INDEX IF NOT EXISTS idx_nodes_node_type ON nodes(node_type);
CREATE INDEX IF NOT EXISTS idx_nodes_status ON nodes(status);
CREATE INDEX IF NOT EXISTS idx_nodes_last_heartbeat ON nodes(last_heartbeat);

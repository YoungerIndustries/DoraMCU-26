-- V79: Extend real estate with commercial properties

CREATE TABLE commercial_properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_name VARCHAR(200) NOT NULL,
    property_type VARCHAR(50) NOT NULL,
    location_id BIGINT,
    planet_id BIGINT,
    square_footage BIGINT,
    zoning_type VARCHAR(50),
    property_value DECIMAL(15, 2),
    monthly_income DECIMAL(12, 2),
    appreciation_rate DECIMAL(5, 2),
    maintenance_cost DECIMAL(12, 2),
    lease_term_months INT,
    tenant_id BIGINT,
    tenant_name VARCHAR(100),
    lease_start_date TIMESTAMP,
    lease_end_date TIMESTAMP,
    owner_id BIGINT NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    latitude DOUBLE,
    longitude DOUBLE,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_commercial_properties_type ON commercial_properties(property_type);
CREATE INDEX idx_commercial_properties_owner ON commercial_properties(owner_id);
CREATE INDEX idx_commercial_properties_status ON commercial_properties(status);
CREATE INDEX idx_commercial_properties_planet ON commercial_properties(planet_id);

-- Add commercial property fields to existing property_listings
ALTER TABLE property_listings ADD COLUMN square_footage BIGINT;
ALTER TABLE property_listings ADD COLUMN zoning_type VARCHAR(50);
ALTER TABLE property_listings ADD COLUMN monthly_income DECIMAL(12, 2);
ALTER TABLE property_listings ADD COLUMN appreciation_rate DECIMAL(5, 2);

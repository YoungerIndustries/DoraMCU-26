-- V53: Create e26_organizations and e26_org_members tables for universal organization system

CREATE TABLE IF NOT EXISTS e26_organizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    org_type VARCHAR(20) NOT NULL,
    leader_type VARCHAR(20),
    leader_id BIGINT,
    hierarchy_level INT DEFAULT 1,
    reputation INT DEFAULT 50,
    wealth INT DEFAULT 50,
    secrecy_level INT DEFAULT 0,
    home_structure_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_e26_organization_type ON e26_organizations(org_type);
CREATE INDEX idx_e26_organization_leader ON e26_organizations(leader_type, leader_id);
CREATE INDEX idx_e26_organization_active ON e26_organizations(active);

CREATE TABLE IF NOT EXISTS e26_org_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    member_type VARCHAR(20) NOT NULL,
    member_id BIGINT NOT NULL,
    role VARCHAR(50),
    rank INT DEFAULT 1,
    loyalty INT DEFAULT 50,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (organization_id) REFERENCES e26_organizations(id)
);

CREATE INDEX idx_e26_org_member_org ON e26_org_members(organization_id);
CREATE INDEX idx_e26_org_member_member ON e26_org_members(member_type, member_id);
CREATE INDEX idx_e26_org_member_active ON e26_org_members(active);

-- V40: Add AI admin testing system tables
-- These tables store test orders, reports, and individual test case results
-- for the AI admin character that can test new features from inside the running program

ALTER TABLE ai_characters ADD COLUMN ai_admin BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE ai_admin_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submitted_by VARCHAR(100) NOT NULL,
    order_text VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);

CREATE TABLE ai_admin_test_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    summary VARCHAR(2000),
    total_tests INT NOT NULL DEFAULT 0,
    passed INT NOT NULL DEFAULT 0,
    failed INT NOT NULL DEFAULT 0,
    skipped INT NOT NULL DEFAULT 0,
    report_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_order FOREIGN KEY (order_id) REFERENCES ai_admin_orders(id)
);

CREATE TABLE ai_admin_test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    status VARCHAR(10) NOT NULL DEFAULT 'PASSED',
    duration_ms BIGINT NOT NULL DEFAULT 0,
    error_message VARCHAR(2000),
    CONSTRAINT fk_testcase_report FOREIGN KEY (report_id) REFERENCES ai_admin_test_reports(id)
);

-- Seed the AI admin character
INSERT INTO ai_characters (name, role, empire_name, linked_player_id, active, can_promote_to_co_server, description, total_experience, leadership_rating, hirable, daily_salary, ai_admin)
SELECT 100, 'AI Admin Tester', 'System Administration', NULL, TRUE, FALSE,
 'Special AI character with admin privileges that executes automated tests on the running system and generates detailed reports.',
 0, 10, FALSE, 0, TRUE
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE name = 'Dora Admin Tester');

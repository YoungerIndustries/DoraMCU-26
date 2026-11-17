package com.yisy.modules.doramcu.admin.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for V90, V91, and V92 Flyway migrations.
 * <p>
 * Uses an H2 in-memory database to verify that the migration SQL scripts
 * (V90, V91, and V92) apply correctly and produce the expected schema changes.
 * Each test creates a fresh in-memory database, builds the base tables that
 * the migrations depend on, and then applies the migration scripts directly.
 * </p>
 */
@DisplayName("AI Admin Dispatch Migration Tests")
class AiAdminDispatchMigrationTest {

    /**
     * Creates a fresh H2 in-memory database with the base tables that V90, V91, V92, and V94
     * migrations depend on, then applies all migration scripts.
     * Each test gets its own isolated database instance.
     */
    private DataSource createDatabaseWithMigrations() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("migration_test_" + System.currentTimeMillis())
                .addScript("db/migration/test/V90_base_schema.sql")
                .build();

        // Apply V90, V91, V92, V94, V95, and V96 migration scripts
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/migration/V90__add_ai_admin_dispatch_fields.sql"));
        populator.addScript(new ClassPathResource("db/migration/V91__add_ai_character_workload_fields.sql"));
        populator.addScript(new ClassPathResource("db/migration/V92__add_ai_admin_order_retry_fields.sql"));
        populator.addScript(new ClassPathResource("db/migration/V94__add_ai_admin_order_llm_response.sql"));
        populator.addScript(new ClassPathResource("db/migration/V95__add_ai_admin_order_progress_fields.sql"));
        populator.addScript(new ClassPathResource("db/migration/V96__add_ai_admin_incidents.sql"));
        populator.execute(dataSource);

        return dataSource;
    }

    @Test
    @DisplayName("V90 should add assigned_ai_admin_id column to ai_admin_orders")
    void v90_shouldAddAssignedAiAdminIdColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME = 'ASSIGNED_AI_ADMIN_ID'"
            );

            assertTrue(rs.next(), "assigned_ai_admin_id column should exist");
            assertEquals("BIGINT", rs.getString("DATA_TYPE"));
            assertEquals("YES", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V90 should create indexes on ai_admin_orders")
    void v90_shouldCreateIndexes() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND INDEX_NAME LIKE 'IDX_AI_ADMIN_ORDERS%'"
            );

            Set<String> indexNames = new HashSet<>();
            while (rs.next()) {
                indexNames.add(rs.getString("INDEX_NAME"));
            }

            assertTrue(indexNames.stream().anyMatch(n -> n.contains("ASSIGNED_ADMIN")),
                    "idx_ai_admin_orders_assigned_admin index should exist");
            assertTrue(indexNames.stream().anyMatch(n -> n.contains("STATUS_ASSIGNED")),
                    "idx_ai_admin_orders_status_assigned index should exist");
        }
    }

    @Test
    @DisplayName("V91 should add current_order_id column to ai_characters")
    void v91_shouldAddCurrentOrderIdColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_CHARACTERS' AND COLUMN_NAME = 'CURRENT_ORDER_ID'"
            );

            assertTrue(rs.next(), "current_order_id column should exist");
            assertEquals("BIGINT", rs.getString("DATA_TYPE"));
            assertEquals("YES", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V91 should add active_order_count column to ai_characters with default 0")
    void v91_shouldAddActiveOrderCountColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_CHARACTERS' AND COLUMN_NAME = 'ACTIVE_ORDER_COUNT'"
            );

            assertTrue(rs.next(), "active_order_count column should exist");
            assertEquals("INTEGER", rs.getString("DATA_TYPE"));
            assertEquals("NO", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V91 should create composite indexes on ai_characters")
    void v91_shouldCreateCompositeIndexes() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES " +
                "WHERE TABLE_NAME = 'AI_CHARACTERS' AND INDEX_NAME LIKE 'IDX_AI_CHARACTERS_ADMIN%'"
            );

            Set<String> indexNames = new HashSet<>();
            while (rs.next()) {
                indexNames.add(rs.getString("INDEX_NAME"));
            }

            assertTrue(indexNames.stream().anyMatch(n -> n.contains("ADMIN_AVAILABLE")),
                    "idx_ai_characters_admin_available index should exist");
            assertTrue(indexNames.stream().anyMatch(n -> n.contains("ADMIN_EMPIRE")),
                    "idx_ai_characters_admin_empire index should exist");
        }
    }

    @Test
    @DisplayName("V90 and V91 should allow inserting data with new columns")
    void migrations_shouldAllowDataInsertion() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Insert into ai_admin_orders with assigned_ai_admin_id
            stmt.execute(
                "INSERT INTO ai_admin_orders (submitted_by, order_text, status, assigned_ai_admin_id) " +
                "VALUES ('test_admin', 'Test order', 'PENDING', 42)"
            );

            ResultSet orderRs = stmt.executeQuery(
                "SELECT assigned_ai_admin_id FROM ai_admin_orders WHERE id = 1"
            );
            assertTrue(orderRs.next());
            assertEquals(42L, orderRs.getLong("assigned_ai_admin_id"));

            // Insert into ai_characters with workload fields
            stmt.execute(
                "INSERT INTO ai_characters (name, role, empire_name, active, ai_admin, current_order_id, active_order_count) " +
                "VALUES ('Test AI', 'Overseer', 'Test Empire', TRUE, TRUE, 1, 1)"
            );

            ResultSet charRs = stmt.executeQuery(
                "SELECT current_order_id, active_order_count FROM ai_characters WHERE name = 'Test AI'"
            );
            assertTrue(charRs.next());
            assertEquals(1L, charRs.getLong("current_order_id"));
            assertEquals(1, charRs.getInt("active_order_count"));
        }
    }

    @Test
    @DisplayName("New columns should accept NULL values correctly")
    void newColumns_shouldAcceptNullValues() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Insert without assigned_ai_admin_id (should be NULL)
            stmt.execute(
                "INSERT INTO ai_admin_orders (submitted_by, order_text, status) " +
                "VALUES ('test_admin', 'Test order', 'PENDING')"
            );

            ResultSet orderRs = stmt.executeQuery(
                "SELECT assigned_ai_admin_id FROM ai_admin_orders WHERE id = 1"
            );
            assertTrue(orderRs.next());
            // H2 may return 0 for NULL BIGINT; check with IS NULL query instead
            ResultSet nullCheck = stmt.executeQuery(
                "SELECT COUNT(*) AS cnt FROM ai_admin_orders WHERE id = 1 AND assigned_ai_admin_id IS NULL"
            );
            assertTrue(nullCheck.next());
            assertEquals(1, nullCheck.getInt("cnt"), "assigned_ai_admin_id should be NULL when not provided");

            // Insert without current_order_id (should be NULL)
            stmt.execute(
                "INSERT INTO ai_characters (name, role, empire_name, active, ai_admin) " +
                "VALUES ('Test AI', 'Overseer', 'Test Empire', TRUE, TRUE)"
            );

            ResultSet nullCheck2 = stmt.executeQuery(
                "SELECT COUNT(*) AS cnt FROM ai_characters WHERE name = 'Test AI' AND current_order_id IS NULL"
            );
            assertTrue(nullCheck2.next());
            assertEquals(1, nullCheck2.getInt("cnt"), "current_order_id should be NULL when not provided");

            // active_order_count should default to 0
            ResultSet charRs = stmt.executeQuery(
                "SELECT active_order_count FROM ai_characters WHERE name = 'Test AI'"
            );
            assertTrue(charRs.next());
            assertEquals(0, charRs.getInt("active_order_count"), "active_order_count should default to 0");
        }
    }

    @Test
    @DisplayName("V92 should add retry_count column to ai_admin_orders")
    void v92_shouldAddRetryCountColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME = 'RETRY_COUNT'"
            );

            assertTrue(rs.next(), "retry_count column should exist");
            assertEquals("INTEGER", rs.getString("DATA_TYPE"));
            assertEquals("NO", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V92 should add max_retries column to ai_admin_orders")
    void v92_shouldAddMaxRetriesColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME = 'MAX_RETRIES'"
            );

            assertTrue(rs.next(), "max_retries column should exist");
            assertEquals("INTEGER", rs.getString("DATA_TYPE"));
            assertEquals("NO", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V92 retry columns should have correct defaults")
    void v92_shouldHaveCorrectDefaults() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "INSERT INTO ai_admin_orders (submitted_by, order_text, status) " +
                "VALUES ('test_admin', 'Test order', 'PENDING')"
            );

            ResultSet rs = stmt.executeQuery(
                "SELECT retry_count, max_retries FROM ai_admin_orders WHERE id = 1"
            );
            assertTrue(rs.next());
            assertEquals(0, rs.getInt("retry_count"), "retry_count should default to 0");
            assertEquals(3, rs.getInt("max_retries"), "max_retries should default to 3");
        }
    }

    @Test
    @DisplayName("V94 should add llm_response column to ai_admin_orders")
    void v94_shouldAddLlmResponseColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME = 'LLM_RESPONSE'"
            );

            assertTrue(rs.next(), "llm_response column should exist");
            assertEquals("YES", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V94 llm_response should accept text data")
    void v94_llmResponse_shouldAcceptTextData() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "INSERT INTO ai_admin_orders (submitted_by, order_text, status, llm_response) " +
                "VALUES ('test_admin', 'Test order', 'PENDING', '{\"action\":\"DEPLOY\",\"reasoning\":\"Test\"}')"
            );

            ResultSet rs = stmt.executeQuery(
                "SELECT llm_response FROM ai_admin_orders WHERE id = 1"
            );
            assertTrue(rs.next());
            String response = rs.getString("llm_response");
            assertNotNull(response);
            assertTrue(response.contains("DEPLOY"));
        }
    }

    @Test
    @DisplayName("V95 should add progress_percent column to ai_admin_orders")
    void v95_shouldAddProgressPercentColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME = 'PROGRESS_PERCENT'"
            );

            assertTrue(rs.next(), "progress_percent column should exist");
            assertEquals("0", rs.getString("COLUMN_DEFAULT"), "progress_percent should default to 0");
        }
    }

    @Test
    @DisplayName("V95 should add current_test_name column to ai_admin_orders")
    void v95_shouldAddCurrentTestNameColumn() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME = 'CURRENT_TEST_NAME'"
            );

            assertTrue(rs.next(), "current_test_name column should exist");
            assertEquals("YES", rs.getString("IS_NULLABLE"));
        }
    }

    @Test
    @DisplayName("V95 should add tests_completed and tests_total columns")
    void v95_shouldAddTestsCompletedAndTotalColumns() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, COLUMN_DEFAULT " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_ORDERS' AND COLUMN_NAME IN ('TESTS_COMPLETED', 'TESTS_TOTAL')"
            );

            Set<String> found = new HashSet<>();
            while (rs.next()) {
                found.add(rs.getString("COLUMN_NAME"));
                assertEquals("0", rs.getString("COLUMN_DEFAULT"), rs.getString("COLUMN_NAME") + " should default to 0");
            }
            assertTrue(found.contains("TESTS_COMPLETED"), "tests_completed column should exist");
            assertTrue(found.contains("TESTS_TOTAL"), "tests_total column should exist");
        }
    }

    @Test
    @DisplayName("V95 progress fields should accept data")
    void v95_progressFields_shouldAcceptData() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "INSERT INTO ai_admin_orders (submitted_by, order_text, status, progress_percent, " +
                "current_test_name, tests_completed, tests_total) " +
                "VALUES ('test_admin', 'Test order', 'RUNNING', 50, 'test_api_health', 5, 10)"
            );

            ResultSet rs = stmt.executeQuery(
                "SELECT progress_percent, current_test_name, tests_completed, tests_total " +
                "FROM ai_admin_orders WHERE id = 1"
            );
            assertTrue(rs.next());
            assertEquals(50, rs.getInt("progress_percent"));
            assertEquals("test_api_health", rs.getString("current_test_name"));
            assertEquals(5, rs.getInt("tests_completed"));
            assertEquals(10, rs.getInt("tests_total"));
        }
    }

    @Test
    @DisplayName("V96 should create ai_admin_incidents table")
    void v96_shouldCreateAiAdminIncidentsTable() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AI_ADMIN_INCIDENTS'"
            );
            assertTrue(rs.next(), "ai_admin_incidents table should exist");
        }
    }

    @Test
    @DisplayName("V96 should have all required columns")
    void v96_shouldHaveAllRequiredColumns() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'AI_ADMIN_INCIDENTS' " +
                "ORDER BY COLUMN_NAME"
            );

            Set<String> found = new HashSet<>();
            while (rs.next()) {
                found.add(rs.getString("COLUMN_NAME"));
            }
            assertTrue(found.contains("ID"), "id column should exist");
            assertTrue(found.contains("INCIDENT_TYPE"), "incident_type column should exist");
            assertTrue(found.contains("SEVERITY"), "severity column should exist");
            assertTrue(found.contains("PRIORITY"), "priority column should exist");
            assertTrue(found.contains("STATUS"), "status column should exist");
            assertTrue(found.contains("TITLE"), "title column should exist");
            assertTrue(found.contains("DESCRIPTION"), "description column should exist");
            assertTrue(found.contains("GALAXY_NAME"), "galaxy_name column should exist");
            assertTrue(found.contains("ASSIGNED_AI_ADMIN_ID"), "assigned_ai_admin_id column should exist");
            assertTrue(found.contains("INVESTIGATOR_NOTES"), "investigator_notes column should exist");
            assertTrue(found.contains("RESOLUTION_ACTION"), "resolution_action column should exist");
            assertTrue(found.contains("ROOT_CAUSE"), "root_cause column should exist");
            assertTrue(found.contains("RELATED_ORDER_ID"), "related_order_id column should exist");
            assertTrue(found.contains("CREATED_AT"), "created_at column should exist");
            assertTrue(found.contains("UPDATED_AT"), "updated_at column should exist");
            assertTrue(found.contains("RESOLVED_AT"), "resolved_at column should exist");
            assertTrue(found.contains("ESCALATED_AT"), "escalated_at column should exist");
        }
    }

    @Test
    @DisplayName("V96 should accept incident data")
    void v96_shouldAcceptIncidentData() throws Exception {
        DataSource dataSource = createDatabaseWithMigrations();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "INSERT INTO ai_admin_incidents (incident_type, severity, priority, status, " +
                "title, description, galaxy_name, assigned_ai_admin_id, created_at, updated_at) " +
                "VALUES ('HEALTH', 'CRITICAL', 10, 'OPEN', 'Low fleet', 'Fleet below threshold', " +
                "'Alpha Centauri', 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
            );

            ResultSet rs = stmt.executeQuery(
                "SELECT incident_type, severity, priority, status, galaxy_name, assigned_ai_admin_id " +
                "FROM ai_admin_incidents WHERE id = 1"
            );
            assertTrue(rs.next());
            assertEquals("HEALTH", rs.getString("incident_type"));
            assertEquals("CRITICAL", rs.getString("severity"));
            assertEquals(10, rs.getInt("priority"));
            assertEquals("OPEN", rs.getString("status"));
            assertEquals("Alpha Centauri", rs.getString("galaxy_name"));
            assertEquals(100, rs.getLong("assigned_ai_admin_id"));
        }
    }
}

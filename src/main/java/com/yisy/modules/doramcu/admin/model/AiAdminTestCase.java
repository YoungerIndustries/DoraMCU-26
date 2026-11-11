package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing an individual test case result within a test report.
 */
@Entity
@Table(name = "ai_admin_test_cases")
@EntityListeners(com.yisy.modules.doramcu.neuron.service.CdcEntityListener.class)
public class AiAdminTestCase {

    public enum TestStatus {
        PASSED,
        FAILED,
        SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The report this test case belongs to.
     */
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    /**
     * Human-readable name of the test.
     */
    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    /**
     * Category of the test: API, GAME, AI_CHAT, AUTH, ADMIN.
     */
    @Column(length = 50)
    private String category;

    /**
     * Result status of this test case.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TestStatus status = TestStatus.PASSED;

    /**
     * How long the test took in milliseconds.
     */
    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    /**
     * Error message if the test failed.
     */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    public AiAdminTestCase() {
    }

    public AiAdminTestCase(Long reportId, String testName, String category) {
        this.reportId = reportId;
        this.testName = testName;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

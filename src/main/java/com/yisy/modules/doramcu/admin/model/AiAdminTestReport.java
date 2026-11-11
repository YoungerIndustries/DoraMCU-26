package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

/**
 * JPA entity representing a test report generated after executing an AI admin order.
 */
@Entity
@Table(name = "ai_admin_test_reports")
@EntityListeners(com.yisy.modules.doramcu.neuron.service.CdcEntityListener.class)
public class AiAdminTestReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The order this report belongs to.
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * AI-generated summary of the test results.
     */
    @Column(length = 2000)
    private String summary;

    /**
     * Total number of tests executed.
     */
    @Column(name = "total_tests", nullable = false)
    private int totalTests;

    /**
     * Number of tests that passed.
     */
    @Column(nullable = false)
    private int passed;

    /**
     * Number of tests that failed.
     */
    @Column(nullable = false)
    private int failed;

    /**
     * Number of tests that were skipped.
     */
    @Column(nullable = false)
    private int skipped;

    /**
     * Full structured report as JSON.
     */
    @Column(name = "report_json", columnDefinition = "TEXT")
    private String reportJson;

    /**
     * When the report was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public AiAdminTestReport() {
    }

    public AiAdminTestReport(Long orderId) {
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public String getReportJson() {
        return reportJson;
    }

    public void setReportJson(String reportJson) {
        this.reportJson = reportJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.yisy.modules.doramcu.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for a work order created by the Galaxy Overseer.
 * <p>
 * Carries the order status, text, timing, and associated report summary
 * so admin dashboards can display and track galaxy work orders.
 * </p>
 */
public class WorkOrderDto {

    private Long id;
    private String submittedBy;
    private String orderText;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer passedTests;
    private Integer failedTests;
    private Integer totalTests;

    public WorkOrderDto() {
    }

    public WorkOrderDto(Long id, String submittedBy, String orderText, String status,
                        LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.submittedBy = submittedBy;
        this.orderText = orderText;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(Integer passedTests) {
        this.passedTests = passedTests;
    }

    public Integer getFailedTests() {
        return failedTests;
    }

    public void setFailedTests(Integer failedTests) {
        this.failedTests = failedTests;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }
}

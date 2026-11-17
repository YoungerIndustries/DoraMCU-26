package com.yisy.modules.doramcu.admin.model;
import com.yisy.modules.doramcu.model.user.User;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

/**
 * JPA entity representing an AI admin test order.
 * An order is submitted by an admin user and triggers automated testing.
 */
@Entity
@Table(name = "ai_admin_orders")
@EntityListeners(com.yisy.modules.doramcu.neuron.service.CdcEntityListener.class)
public class AiAdminOrder {

    public enum OrderStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Username of the admin who submitted this order.
     */
    @Column(nullable = false, length = 100)
    private String submittedBy;

    /**
     * Natural language description of what to test.
     */
    @Column(nullable = false, length = 2000)
    private String orderText;

    /**
     * ID of the AI admin character assigned to execute this order.
     * Set by the dispatch service when routing the order to an available AI admin.
     */
    @Column(name = "assigned_ai_admin_id")
    private Long assignedAiAdminId;

    /**
     * Number of times this order has been retried after failure.
     */
    @Column(name = "retry_count")
    private int retryCount = 0;

    /**
     * Maximum number of retries before the order is permanently failed.
     */
    @Column(name = "max_retries")
    private int maxRetries = 3;

    /**
     * Raw LLM response when this order was analyzed by the LLM.
     * Stored for auditability and debugging purposes.
     */
    @Column(name = "llm_response", columnDefinition = "TEXT")
    private String llmResponse;

    /**
     * Target galaxy for this order. The AI admin will move to this galaxy to execute the order.
     */
    @Column(name = "target_galaxy", length = 100)
    private String targetGalaxy;

    /**
     * Target X coordinate for this order. The AI admin will move to this position.
     */
    @Column(name = "target_pos_x")
    private Double targetPosX;

    /**
     * Target Y coordinate for this order.
     */
    @Column(name = "target_pos_y")
    private Double targetPosY;

    /**
     * Target Z coordinate for this order.
     */
    @Column(name = "target_pos_z")
    private Double targetPosZ;

    /**
     * Current status of the order.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * When the order was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * When the order completed (success or failure).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public AiAdminOrder() {
    }

    public AiAdminOrder(String submittedBy, String orderText) {
        this.submittedBy = submittedBy;
        this.orderText = orderText;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
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

    public Long getAssignedAiAdminId() {
        return assignedAiAdminId;
    }

    public void setAssignedAiAdminId(Long assignedAiAdminId) {
        this.assignedAiAdminId = assignedAiAdminId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getLlmResponse() {
        return llmResponse;
    }

    public void setLlmResponse(String llmResponse) {
        this.llmResponse = llmResponse;
    }

    public String getTargetGalaxy() {
        return targetGalaxy;
    }

    public void setTargetGalaxy(String targetGalaxy) {
        this.targetGalaxy = targetGalaxy;
    }

    public Double getTargetPosX() {
        return targetPosX;
    }

    public void setTargetPosX(Double targetPosX) {
        this.targetPosX = targetPosX;
    }

    public Double getTargetPosY() {
        return targetPosY;
    }

    public void setTargetPosY(Double targetPosY) {
        this.targetPosY = targetPosY;
    }

    public Double getTargetPosZ() {
        return targetPosZ;
    }

    public void setTargetPosZ(Double targetPosZ) {
        this.targetPosZ = targetPosZ;
    }

    /**
     * Check if this order has a target location specified.
     */
    public boolean hasTargetLocation() {
        return targetPosX != null && targetPosY != null && targetPosZ != null;
    }
}

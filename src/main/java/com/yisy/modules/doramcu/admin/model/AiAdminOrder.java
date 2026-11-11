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
}

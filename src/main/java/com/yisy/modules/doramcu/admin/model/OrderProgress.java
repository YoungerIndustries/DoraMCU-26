package com.yisy.modules.doramcu.admin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks the progress of AI admin orders.
 */
@Entity
@Table(name = "order_progress")
public class OrderProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "status")
    private String status;

    @Column(name = "progress_percent")
    private int progressPercent;

    @Column(name = "message")
    private String message;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OrderProgress() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.yisy.modules.doramcu.corporation.stock.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_orders", indexes = {
        @Index(name = "idx_stock_orders_listing", columnList = "stock_listing_id"),
        @Index(name = "idx_stock_orders_player", columnList = "player_id"),
        @Index(name = "idx_stock_orders_status", columnList = "status"),
        @Index(name = "idx_stock_orders_expires", columnList = "expires_at")
})
public class StockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_listing_id", nullable = false)
    private Long stockListingId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "order_type", nullable = false, length = 10)
    private String orderType; // "BUY" or "SELL"

    @Column(name = "order_style", nullable = false, length = 10)
    private String orderStyle; // "MARKET" or "LIMIT"

    @Column(name = "limit_price", precision = 15, scale = 2)
    private BigDecimal limitPrice;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "filled_quantity", nullable = false)
    private Long filledQuantity = 0L;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "PENDING", "FILLED", "PARTIAL", "CANCELLED"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7); // Default 7-day expiration
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStockListingId() {
        return stockListingId;
    }

    public void setStockListingId(Long stockListingId) {
        this.stockListingId = stockListingId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStyle() {
        return orderStyle;
    }

    public void setOrderStyle(String orderStyle) {
        this.orderStyle = orderStyle;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(Long filledQuantity) {
        this.filledQuantity = filledQuantity;
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

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getRemainingQuantity() {
        return quantity - filledQuantity;
    }

    @Override
    public String toString() {
        return "StockOrder{" +
                "id=" + id +
                ", stockListingId=" + stockListingId +
                ", playerId=" + playerId +
                ", orderType='" + orderType + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                '}';
    }
}

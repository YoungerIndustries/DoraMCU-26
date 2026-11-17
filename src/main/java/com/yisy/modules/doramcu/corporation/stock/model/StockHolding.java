package com.yisy.modules.doramcu.corporation.stock.model;
import com.yisy.modules.doramcu.model.players.Player;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_holdings", indexes = {
        @Index(name = "idx_stock_holdings_owner", columnList = "owner_id, owner_type"),
        @Index(name = "idx_stock_holdings_stock", columnList = "stock_listing_id")
})
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "owner_type", nullable = false, length = 20)
    private String ownerType; // "PLAYER" or "CORPORATION"

    @Column(name = "stock_listing_id", nullable = false)
    private Long stockListingId;

    @Column(name = "shares_owned", nullable = false)
    private Long sharesOwned;

    @Column(name = "average_buy_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal averageBuyPrice;

    @Column(name = "total_dividends_received", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDividendsReceived = BigDecimal.ZERO;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public Long getStockListingId() {
        return stockListingId;
    }

    public void setStockListingId(Long stockListingId) {
        this.stockListingId = stockListingId;
    }

    public Long getSharesOwned() {
        return sharesOwned;
    }

    public void setSharesOwned(Long sharesOwned) {
        this.sharesOwned = sharesOwned;
    }

    public BigDecimal getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public BigDecimal getTotalDividendsReceived() {
        return totalDividendsReceived;
    }

    public void setTotalDividendsReceived(BigDecimal totalDividendsReceived) {
        this.totalDividendsReceived = totalDividendsReceived;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "StockHolding{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", ownerType='" + ownerType + '\'' +
                ", sharesOwned=" + sharesOwned +
                ", averageBuyPrice=" + averageBuyPrice +
                '}';
    }
}

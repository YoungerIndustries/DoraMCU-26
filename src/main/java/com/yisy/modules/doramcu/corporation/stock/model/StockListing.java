package com.yisy.modules.doramcu.corporation.stock.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_listings", indexes = {
        @Index(name = "idx_stock_listings_corporation", columnList = "corporation_id", unique = true),
        @Index(name = "idx_stock_listings_ticker", columnList = "ticker_symbol", unique = true),
        @Index(name = "idx_stock_listings_active", columnList = "is_active")
})
public class StockListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corporation_id", nullable = false, unique = true)
    private Long corporationId;

    @Column(name = "ticker_symbol", nullable = false, length = 10, unique = true)
    private String tickerSymbol;

    @Column(name = "total_shares", nullable = false)
    private Long totalShares;

    @Column(name = "public_shares", nullable = false)
    private Long publicShares;

    @Column(name = "initial_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialPrice;

    @Column(name = "current_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "day_high", precision = 15, scale = 2)
    private BigDecimal dayHigh;

    @Column(name = "day_low", precision = 15, scale = 2)
    private BigDecimal dayLow;

    @Column(name = "volume_24h", nullable = false)
    private Long volume24h = 0L;

    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "dividend_yield")
    private Double dividendYield;

    @Column(name = "ipo_date", nullable = false)
    private LocalDateTime ipoDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public Long getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(Long totalShares) {
        this.totalShares = totalShares;
    }

    public Long getPublicShares() {
        return publicShares;
    }

    public void setPublicShares(Long publicShares) {
        this.publicShares = publicShares;
    }

    public BigDecimal getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(BigDecimal initialPrice) {
        this.initialPrice = initialPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getDayHigh() {
        return dayHigh;
    }

    public void setDayHigh(BigDecimal dayHigh) {
        this.dayHigh = dayHigh;
    }

    public BigDecimal getDayLow() {
        return dayLow;
    }

    public void setDayLow(BigDecimal dayLow) {
        this.dayLow = dayLow;
    }

    public Long getVolume24h() {
        return volume24h;
    }

    public void setVolume24h(Long volume24h) {
        this.volume24h = volume24h;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public Double getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(Double dividendYield) {
        this.dividendYield = dividendYield;
    }

    public LocalDateTime getIpoDate() {
        return ipoDate;
    }

    public void setIpoDate(LocalDateTime ipoDate) {
        this.ipoDate = ipoDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastPriceUpdate() {
        return lastPriceUpdate;
    }

    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) {
        this.lastPriceUpdate = lastPriceUpdate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "StockListing{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", tickerSymbol='" + tickerSymbol + '\'' +
                ", currentPrice=" + currentPrice +
                ", marketCap=" + marketCap +
                ", isActive=" + isActive +
                '}';
    }
}

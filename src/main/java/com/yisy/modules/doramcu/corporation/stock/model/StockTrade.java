package com.yisy.modules.doramcu.corporation.stock.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_trades", indexes = {
        @Index(name = "idx_stock_trades_listing", columnList = "stock_listing_id"),
        @Index(name = "idx_stock_trades_buyer", columnList = "buyer_id"),
        @Index(name = "idx_stock_trades_seller", columnList = "seller_id"),
        @Index(name = "idx_stock_trades_timestamp", columnList = "timestamp")
})
public class StockTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_listing_id", nullable = false)
    private Long stockListingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "shares_traded", nullable = false)
    private Long sharesTraded;

    @Column(name = "price_per_share", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerShare;

    @Column(name = "total_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "market_tax", nullable = false, precision = 20, scale = 2)
    private BigDecimal marketTax;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

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

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getSharesTraded() {
        return sharesTraded;
    }

    public void setSharesTraded(Long sharesTraded) {
        this.sharesTraded = sharesTraded;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getMarketTax() {
        return marketTax;
    }

    public void setMarketTax(BigDecimal marketTax) {
        this.marketTax = marketTax;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StockTrade{" +
                "id=" + id +
                ", stockListingId=" + stockListingId +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", sharesTraded=" + sharesTraded +
                ", pricePerShare=" + pricePerShare +
                '}';
    }
}

package com.yisy.modules.doramcu.corporation.stock.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dividend_payments", indexes = {
        @Index(name = "idx_dividend_payments_listing", columnList = "stock_listing_id"),
        @Index(name = "idx_dividend_payments_quarter", columnList = "quarter"),
        @Index(name = "idx_dividend_payments_date", columnList = "payment_date")
})
public class DividendPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_listing_id", nullable = false)
    private Long stockListingId;

    @Column(name = "amount_per_share", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPerShare;

    @Column(name = "total_paid", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "quarter", nullable = false, length = 10)
    private String quarter; // e.g. "Q1-2026"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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

    public BigDecimal getAmountPerShare() {
        return amountPerShare;
    }

    public void setAmountPerShare(BigDecimal amountPerShare) {
        this.amountPerShare = amountPerShare;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "DividendPayment{" +
                "id=" + id +
                ", stockListingId=" + stockListingId +
                ", amountPerShare=" + amountPerShare +
                ", totalPaid=" + totalPaid +
                ", quarter='" + quarter + '\'' +
                '}';
    }
}

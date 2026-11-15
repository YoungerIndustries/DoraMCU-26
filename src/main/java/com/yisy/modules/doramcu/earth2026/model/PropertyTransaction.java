package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a property transaction (buy, rent, sell).
 * <p>
 * Records all real estate transactions for audit and economic tracking.
 * </p>
 */
@Entity
@Table(name = "property_transactions", indexes = {
        @Index(name = "idx_pt_character", columnList = "character_id"),
        @Index(name = "idx_pt_property", columnList = "property_id"),
        @Index(name = "idx_pt_type", columnList = "type"),
        @Index(name = "idx_pt_status", columnList = "status")
})
public class PropertyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Character ID involved in the transaction.
     */
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    /**
     * Property ID (OfficeUnit or ApartmentUnit ID).
     */
    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    /**
     * Transaction type: BUY, RENT, SELL, RELEASE.
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * Transaction amount.
     */
    @Column(nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * Transaction status: COMPLETED, PENDING, FAILED.
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * Human-readable message about the transaction.
     */
    @Column(length = 500)
    private String message;

    /**
     * When the transaction was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PropertyTransaction() {
    }

    public PropertyTransaction(Long characterId, Long propertyId, String type, BigDecimal amount) {
        this.characterId = characterId;
        this.propertyId = propertyId;
        this.type = type;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PropertyTransaction{id=" + id + ", type='" + type + "', amount=" + amount +
                ", status='" + status + "'}";
    }
}

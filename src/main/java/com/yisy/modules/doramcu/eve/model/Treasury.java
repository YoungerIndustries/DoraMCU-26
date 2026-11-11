package com.yisy.modules.doramcu.eve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.math.BigDecimal;

/**
 * JPA entity representing a treasury holding for a player or corporation.
 * Tracks precious metal holdings (e.g., silver) with quantity and price.
 */
@Entity
@Table(name = "treasuries")
@EntityListeners(CdcEntityListener.class)
public class Treasury {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Resource type, e.g., "SILVER", "GOLD", "PLATINUM".
     */
    @Column(nullable = false, length = 30)
    private String resourceType;

    /**
     * Quantity in ounces.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityOunces;

    /**
     * Purchase price per ounce in USD.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerOunce;

    /**
     * Owner identifier (player ID or corporation ID).
     */
    @Column(nullable = false, length = 50)
    private String ownerId;

    /**
     * Owner type: "PLAYER" or "CORPORATION".
     */
    @Column(nullable = false, length = 20)
    private String ownerType;

    public Treasury() {
    }

    public Treasury(String resourceType, BigDecimal quantityOunces, BigDecimal pricePerOunce,
                    String ownerId, String ownerType) {
        this.resourceType = resourceType;
        this.quantityOunces = quantityOunces;
        this.pricePerOunce = pricePerOunce;
        this.ownerId = ownerId;
        this.ownerType = ownerType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public BigDecimal getQuantityOunces() {
        return quantityOunces;
    }

    public void setQuantityOunces(BigDecimal quantityOunces) {
        this.quantityOunces = quantityOunces;
    }

    public BigDecimal getPricePerOunce() {
        return pricePerOunce;
    }

    public void setPricePerOunce(BigDecimal pricePerOunce) {
        this.pricePerOunce = pricePerOunce;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    /**
     * Calculate the total value of this treasury holding.
     */
    public BigDecimal getTotalValue() {
        return quantityOunces.multiply(pricePerOunce);
    }

    @Override
    public String toString() {
        return "Treasury{id=" + id + ", resource='" + resourceType + "', quantity=" +
                quantityOunces + " oz, price=$" + pricePerOunce + "/oz, total=$" + getTotalValue() +
                ", owner=" + ownerId + "}";
    }
}

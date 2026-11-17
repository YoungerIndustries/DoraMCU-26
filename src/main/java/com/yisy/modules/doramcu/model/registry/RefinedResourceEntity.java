package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for refined space resources produced by processing raw resources.
 * <p>
 * Covers METAL_ALLOY, POWER_CELL, FUEL, COMPOSITE, and NUTRIENT.
 * </p>
 */
@Entity
@Table(name = "refined_resource", indexes = {
        @Index(name = "idx_rf_resource_type", columnList = "resource_type")
})
public class RefinedResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "resource_type", nullable = false, unique = true, length = 50)
    private String resourceType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "primary_use", columnDefinition = "TEXT")
    private String primaryUse;

    public RefinedResourceEntity() {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getPrimaryUse() {
        return primaryUse;
    }

    public void setPrimaryUse(String primaryUse) {
        this.primaryUse = primaryUse;
    }
}

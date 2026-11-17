package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for industry types on planet surfaces.
 * <p>
 * Covers 24 industry types across PRIMARY, SECONDARY, and TERTIARY production levels.
 * </p>
 */
@Entity
@Table(name = "industry_type", indexes = {
        @Index(name = "idx_it_production_level", columnList = "production_level")
})
public class IndustryTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "industry_code", nullable = false, unique = true, length = 50)
    private String industryCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "production_level", nullable = false, length = 20)
    private String productionLevel;

    @Column(name = "construction_cost", nullable = false)
    private int constructionCost;

    @Column(name = "maintenance_cost", nullable = false)
    private int maintenanceCost;

    @Column(name = "required_terrain", columnDefinition = "JSON")
    private String requiredTerrain;

    public IndustryTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProductionLevel() {
        return productionLevel;
    }

    public void setProductionLevel(String productionLevel) {
        this.productionLevel = productionLevel;
    }

    public int getConstructionCost() {
        return constructionCost;
    }

    public void setConstructionCost(int constructionCost) {
        this.constructionCost = constructionCost;
    }

    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(int maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public String getRequiredTerrain() {
        return requiredTerrain;
    }

    public void setRequiredTerrain(String requiredTerrain) {
        this.requiredTerrain = requiredTerrain;
    }
}

package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for ship module types.
 * <p>
 * Covers 18 module types across PROPULSION, COMMAND, WEAPONS, DEFENSE, and SUPPORT categories.
 * </p>
 */
@Entity
@Table(name = "module_type", indexes = {
        @Index(name = "idx_mt_category", columnList = "category")
})
public class ModuleTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "module_code", nullable = false, unique = true, length = 50)
    private String moduleCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(name = "power_requirement", nullable = false)
    private int powerRequirement;

    @Column(name = "base_cost_per_ton", nullable = false)
    private int baseCostPerTon;

    @Column(name = "is_critical", nullable = false)
    private boolean isCritical;

    public ModuleTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPowerRequirement() {
        return powerRequirement;
    }

    public void setPowerRequirement(int powerRequirement) {
        this.powerRequirement = powerRequirement;
    }

    public int getBaseCostPerTon() {
        return baseCostPerTon;
    }

    public void setBaseCostPerTon(int baseCostPerTon) {
        this.baseCostPerTon = baseCostPerTon;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setCritical(boolean critical) {
        isCritical = critical;
    }
}

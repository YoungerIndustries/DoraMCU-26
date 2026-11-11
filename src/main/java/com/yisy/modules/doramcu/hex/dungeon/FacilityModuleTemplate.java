package com.yisy.modules.doramcu.hex.dungeon;

import jakarta.persistence.*;

/**
 * JPA entity representing a reusable facility module template.
 * <p>
 * A facility module template defines a pre-built facility section (mining shaft,
 * power generator, lab wing, etc.) that can be instantiated multiple times when
 * building planet facilities. Each template stores its shape, compartment layout,
 * and connection points as serialized JSON data.
 * </p>
 * <p>
 * Templates are stored in the database and used to create facilities with 3D structure
 * instead of flat enum-based types.
 * </p>
 */
@Entity
@Table(name = "facility_module_templates", indexes = {
        @Index(name = "idx_facility_module_type", columnList = "type"),
        @Index(name = "idx_facility_module_name", columnList = "name")
})
public class FacilityModuleTemplate extends ModuleTemplate {

    /**
     * Functional type of a facility module section.
     */
    public enum FacilityModuleType {
        /** Mining shaft for resource extraction */
        MINING_SHAFT,
        /** Power generation facility */
        POWER_GENERATOR,
        /** Research laboratory wing */
        RESEARCH_LAB,
        /** Storage warehouse */
        WAREHOUSE,
        /** Command and control center */
        COMMAND_CENTER,
        /** Worker quarters */
        WORKER_QUARTERS,
        /** Processing plant */
        PROCESSING_PLANT,
        /** Defense turret emplacement */
        DEFENSE_TURRET,
        /** Communications array */
        COMMS_ARRAY,
        /** Medical facility */
        MEDICAL_BAY,
        /** Corridor connecting facility sections */
        CORRIDOR,
        /** Empty or unassigned section */
        EMPTY;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FacilityModuleType type;

    /**
     * Mineral production bonus per turn.
     */
    @Column
    private int mineralBonus;

    /**
     * Energy production bonus per turn.
     */
    @Column
    private int energyBonus;

    /**
     * Food production bonus per turn.
     */
    @Column
    private int foodBonus;

    /**
     * Research output bonus per turn.
     */
    @Column
    private int researchBonus;

    /**
     * Defense bonus provided by this module.
     */
    @Column
    private int defenseBonus;

    /**
     * Worker capacity required by this module.
     */
    @Column
    private int workerCapacity;

    // ---- Getters and Setters ----

    /**
     * @return the facility module type
     */
    public FacilityModuleType getType() {
        return type;
    }

    /**
     * Sets the facility module type.
     *
     * @param type the module type
     */
    public void setType(FacilityModuleType type) {
        this.type = type;
    }

    /**
     * @return the mineral bonus per turn
     */
    public int getMineralBonus() {
        return mineralBonus;
    }

    /**
     * Sets the mineral bonus per turn.
     *
     * @param mineralBonus the bonus
     */
    public void setMineralBonus(int mineralBonus) {
        this.mineralBonus = mineralBonus;
    }

    /**
     * @return the energy bonus per turn
     */
    public int getEnergyBonus() {
        return energyBonus;
    }

    /**
     * Sets the energy bonus per turn.
     *
     * @param energyBonus the bonus
     */
    public void setEnergyBonus(int energyBonus) {
        this.energyBonus = energyBonus;
    }

    /**
     * @return the food bonus per turn
     */
    public int getFoodBonus() {
        return foodBonus;
    }

    /**
     * Sets the food bonus per turn.
     *
     * @param foodBonus the bonus
     */
    public void setFoodBonus(int foodBonus) {
        this.foodBonus = foodBonus;
    }

    /**
     * @return the research bonus per turn
     */
    public int getResearchBonus() {
        return researchBonus;
    }

    /**
     * Sets the research bonus per turn.
     *
     * @param researchBonus the bonus
     */
    public void setResearchBonus(int researchBonus) {
        this.researchBonus = researchBonus;
    }

    /**
     * @return the defense bonus
     */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /**
     * Sets the defense bonus.
     *
     * @param defenseBonus the bonus
     */
    public void setDefenseBonus(int defenseBonus) {
        this.defenseBonus = defenseBonus;
    }

    /**
     * @return the worker capacity required
     */
    public int getWorkerCapacity() {
        return workerCapacity;
    }

    /**
     * Sets the worker capacity.
     *
     * @param workerCapacity the capacity
     */
    public void setWorkerCapacity(int workerCapacity) {
        this.workerCapacity = workerCapacity;
    }

    @Override
    public String toString() {
        return String.format("FacilityModuleTemplate{id=%d, name='%s', type=%s, dimensions=%dx%dx%d}",
                getId(), getName(), type, getWidth(), getHeight(), getDepth());
    }
}

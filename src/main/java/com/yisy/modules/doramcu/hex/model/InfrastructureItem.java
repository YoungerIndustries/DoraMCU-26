package com.yisy.modules.doramcu.hex.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a buildable infrastructure item in the game.
 * <p>
 * Covers planet facilities, space stations, and station modules.
 * Each item has material costs, labor costs, production stats, bonuses,
 * and a contract breakdown that defines how it is modularized for crew groups.
 * </p>
 * <p>
 * Key mechanics:
 * - Item Type: PLANET_FACILITY, SPACE_STATION, STATION_MODULE
 * - Material Costs: mineral cost, energy cost, raw/refined resources, construction modules
 * - Labor Costs: total crew-days and credit cost
 * - Production Stats: raw/refined resource output per turn, module upkeep
 * - Bonuses: mineral, energy, food, research, defense bonuses
 * - Contract Breakdown: JSON array of sub-contracts for modularized construction
 * </p>
 */
@Entity
@Table(name = "infrastructure_items", indexes = {
        @Index(name = "idx_infrastructure_items_type", columnList = "item_type"),
        @Index(name = "idx_infrastructure_items_name", columnList = "item_name")
})
public class InfrastructureItem {

    public enum InfrastructureItemType {
        PLANET_FACILITY("Planet Facility", "Ground-based facility on a planet"),
        SPACE_STATION("Space Station", "Orbital structure in a solar system"),
        STATION_MODULE("Station Module", "Modular component of a space station");

        private final String displayName;
        private final String description;

        InfrastructureItemType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Type of item: planet facility, space station, or station module.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private InfrastructureItemType itemType;

    /**
     * Name of the item (e.g., MINING_FACILITY, MILITARY station, COMMAND_MODULE).
     */
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    /**
     * Description of the item.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    // --- Material Costs ---

    /**
     * Base mineral cost to build this item.
     */
    @Column(name = "mineral_cost")
    private Integer mineralCost;

    /**
     * Base energy cost to build this item.
     */
    @Column(name = "energy_cost")
    private Integer energyCost;

    /**
     * JSON string specifying raw resource requirements.
     * Example: {"ORE": 2, "CRYSTAL": 1}
     */
    @Column(name = "raw_resources_json", columnDefinition = "TEXT")
    private String rawResourcesJson;

    /**
     * JSON string specifying refined resource requirements.
     * Example: {"METAL_ALLOY": 2, "COMPOSITE": 1}
     */
    @Column(name = "refined_resources_json", columnDefinition = "TEXT")
    private String refinedResourcesJson;

    /**
     * JSON string specifying construction module requirements.
     * Example: {"HULL_MODULE": 2, "WEAPON_MODULE": 1}
     */
    @Column(name = "construction_modules_json", columnDefinition = "TEXT")
    private String constructionModulesJson;

    // --- Labor Costs ---

    /**
     * Total crew-days required to build this item.
     */
    @Column(name = "total_labor_crew_days")
    private Integer totalLaborCrewDays;

    /**
     * Total labor cost in credits.
     */
    @Column(name = "labor_cost_credits", precision = 15, scale = 2)
    private BigDecimal laborCostCredits;

    // --- Production Stats ---

    /**
     * JSON string specifying raw resource production per turn.
     * Example: {"ORE": 3, "GAS": 1}
     */
    @Column(name = "raw_production_json", columnDefinition = "TEXT")
    private String rawProductionJson;

    /**
     * JSON string specifying refined resource production per turn.
     * Example: {"METAL_ALLOY": 1}
     */
    @Column(name = "refined_production_json", columnDefinition = "TEXT")
    private String refinedProductionJson;

    /**
     * JSON string specifying module upkeep consumed per turn.
     * Example: {"HULL_MODULE": 1, "SHIELD_MODULE": 1}
     */
    @Column(name = "module_upkeep_json", columnDefinition = "TEXT")
    private String moduleUpkeepJson;

    /**
     * Credit income generated per turn.
     */
    @Column(name = "credit_income_per_turn")
    private Integer creditIncomePerTurn;

    /**
     * Upkeep cost in credits per turn.
     */
    @Column(name = "upkeep_cost_per_turn")
    private Integer upkeepCostPerTurn;

    // --- Bonuses ---

    /**
     * Mineral production bonus.
     */
    @Column(name = "mineral_bonus")
    private Integer mineralBonus;

    /**
     * Energy production bonus.
     */
    @Column(name = "energy_bonus")
    private Integer energyBonus;

    /**
     * Food production bonus.
     */
    @Column(name = "food_bonus")
    private Integer foodBonus;

    /**
     * Research bonus.
     */
    @Column(name = "research_bonus")
    private Integer researchBonus;

    /**
     * Defense bonus.
     */
    @Column(name = "defense_bonus")
    private Integer defenseBonus;

    // --- Construction ---

    /**
     * Total build time in turns.
     */
    @Column(name = "build_time_turns")
    private Integer buildTimeTurns;

    /**
     * Number of parallel crew groups needed for optimal construction.
     */
    @Column(name = "required_crew_groups")
    private Integer requiredCrewGroups;

    /**
     * JSON array of sub-contracts that make up this item's construction.
     * Each sub-contract can be executed by 1 leader + 3 crew members.
     */
    @Column(name = "contract_breakdown_json", columnDefinition = "TEXT")
    private String contractBreakdownJson;

    // --- Stats ---

    /**
     * Hull points (for stations and modules).
     */
    @Column(name = "hull_points")
    private Integer hullPoints;

    /**
     * Shield points (for stations and modules).
     */
    @Column(name = "shield_points")
    private Integer shieldPoints;

    /**
     * Number of module slots (for stations).
     */
    @Column(name = "module_slots")
    private Integer moduleSlots;

    /**
     * Number of docking ports.
     */
    @Column(name = "docking_ports")
    private Integer dockingPorts;

    /**
     * When the record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (mineralBonus == null) mineralBonus = 0;
        if (energyBonus == null) energyBonus = 0;
        if (foodBonus == null) foodBonus = 0;
        if (researchBonus == null) researchBonus = 0;
        if (defenseBonus == null) defenseBonus = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InfrastructureItemType getItemType() {
        return itemType;
    }

    public void setItemType(InfrastructureItemType itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMineralCost() {
        return mineralCost;
    }

    public void setMineralCost(Integer mineralCost) {
        this.mineralCost = mineralCost;
    }

    public Integer getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(Integer energyCost) {
        this.energyCost = energyCost;
    }

    public String getRawResourcesJson() {
        return rawResourcesJson;
    }

    public void setRawResourcesJson(String rawResourcesJson) {
        this.rawResourcesJson = rawResourcesJson;
    }

    public String getRefinedResourcesJson() {
        return refinedResourcesJson;
    }

    public void setRefinedResourcesJson(String refinedResourcesJson) {
        this.refinedResourcesJson = refinedResourcesJson;
    }

    public String getConstructionModulesJson() {
        return constructionModulesJson;
    }

    public void setConstructionModulesJson(String constructionModulesJson) {
        this.constructionModulesJson = constructionModulesJson;
    }

    public Integer getTotalLaborCrewDays() {
        return totalLaborCrewDays;
    }

    public void setTotalLaborCrewDays(Integer totalLaborCrewDays) {
        this.totalLaborCrewDays = totalLaborCrewDays;
    }

    public BigDecimal getLaborCostCredits() {
        return laborCostCredits;
    }

    public void setLaborCostCredits(BigDecimal laborCostCredits) {
        this.laborCostCredits = laborCostCredits;
    }

    public String getRawProductionJson() {
        return rawProductionJson;
    }

    public void setRawProductionJson(String rawProductionJson) {
        this.rawProductionJson = rawProductionJson;
    }

    public String getRefinedProductionJson() {
        return refinedProductionJson;
    }

    public void setRefinedProductionJson(String refinedProductionJson) {
        this.refinedProductionJson = refinedProductionJson;
    }

    public String getModuleUpkeepJson() {
        return moduleUpkeepJson;
    }

    public void setModuleUpkeepJson(String moduleUpkeepJson) {
        this.moduleUpkeepJson = moduleUpkeepJson;
    }

    public Integer getCreditIncomePerTurn() {
        return creditIncomePerTurn;
    }

    public void setCreditIncomePerTurn(Integer creditIncomePerTurn) {
        this.creditIncomePerTurn = creditIncomePerTurn;
    }

    public Integer getUpkeepCostPerTurn() {
        return upkeepCostPerTurn;
    }

    public void setUpkeepCostPerTurn(Integer upkeepCostPerTurn) {
        this.upkeepCostPerTurn = upkeepCostPerTurn;
    }

    public Integer getMineralBonus() {
        return mineralBonus;
    }

    public void setMineralBonus(Integer mineralBonus) {
        this.mineralBonus = mineralBonus;
    }

    public Integer getEnergyBonus() {
        return energyBonus;
    }

    public void setEnergyBonus(Integer energyBonus) {
        this.energyBonus = energyBonus;
    }

    public Integer getFoodBonus() {
        return foodBonus;
    }

    public void setFoodBonus(Integer foodBonus) {
        this.foodBonus = foodBonus;
    }

    public Integer getResearchBonus() {
        return researchBonus;
    }

    public void setResearchBonus(Integer researchBonus) {
        this.researchBonus = researchBonus;
    }

    public Integer getDefenseBonus() {
        return defenseBonus;
    }

    public void setDefenseBonus(Integer defenseBonus) {
        this.defenseBonus = defenseBonus;
    }

    public Integer getBuildTimeTurns() {
        return buildTimeTurns;
    }

    public void setBuildTimeTurns(Integer buildTimeTurns) {
        this.buildTimeTurns = buildTimeTurns;
    }

    public Integer getRequiredCrewGroups() {
        return requiredCrewGroups;
    }

    public void setRequiredCrewGroups(Integer requiredCrewGroups) {
        this.requiredCrewGroups = requiredCrewGroups;
    }

    public String getContractBreakdownJson() {
        return contractBreakdownJson;
    }

    public void setContractBreakdownJson(String contractBreakdownJson) {
        this.contractBreakdownJson = contractBreakdownJson;
    }

    public Integer getHullPoints() {
        return hullPoints;
    }

    public void setHullPoints(Integer hullPoints) {
        this.hullPoints = hullPoints;
    }

    public Integer getShieldPoints() {
        return shieldPoints;
    }

    public void setShieldPoints(Integer shieldPoints) {
        this.shieldPoints = shieldPoints;
    }

    public Integer getModuleSlots() {
        return moduleSlots;
    }

    public void setModuleSlots(Integer moduleSlots) {
        this.moduleSlots = moduleSlots;
    }

    public Integer getDockingPorts() {
        return dockingPorts;
    }

    public void setDockingPorts(Integer dockingPorts) {
        this.dockingPorts = dockingPorts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "InfrastructureItem{name='" + itemName + "', type=" + itemType +
                ", mineralCost=" + mineralCost + ", energyCost=" + energyCost + "}";
    }
}

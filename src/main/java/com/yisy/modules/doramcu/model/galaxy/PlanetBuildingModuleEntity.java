package com.yisy.modules.doramcu.model.galaxy;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.dungeon.Dungeon;

import com.yisy.modules.doramcu.hex.planet.hullmodule.HullModule;
import com.yisy.modules.doramcu.model.misc.Planet;


import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a generic planet-building module.
 * <p>
 * Each module is categorized by tech level and module category, with generic
 * names and base stats. Game-specific lore names are provided by
 * {@link com.yisy.modules.doramcu.techlevel.TechLevelAdapter} implementations.
 * </p>
 */
@Entity
@Table(name = "planet_building_modules", indexes = {
    @Index(name = "idx_module_tech_level", columnList = "tech_level"),
    @Index(name = "idx_module_category", columnList = "category"),
    @Index(name = "idx_module_tech_category", columnList = "tech_level,category")
})
public class PlanetBuildingModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Technology level this module belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tech_level", nullable = false, length = 10)
    private TechLevel techLevel;

    /** Category of the module (SETTLEMENT, INDUSTRY, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ModuleCategory category;

    /** Generic type identifier (e.g., "VILLAGE", "MINE", "CART"). */
    @Column(name = "generic_type", nullable = false, length = 50)
    private String genericType;

    /** Generic display name for the module. */
    @Column(name = "generic_name", nullable = false, length = 100)
    private String genericName;

    /** Game-specific lore name (nullable, overridden by adapter). */
    @Column(name = "lore_name", length = 100)
    private String loreName;

    /** Base cost to build this module. */
    @Column(name = "base_cost", nullable = false)
    private int baseCost;

    /** Base capacity or output of this module. */
    @Column(name = "base_capacity", nullable = false)
    private int baseCapacity;

    /** Description of the module. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Optional game type filter (nullable = available for all games). */
    @Column(name = "game_type", length = 30)
    private String gameType;

    // === Dungeon-specific fields (nullable, used for D&D objects) ===

    /** Challenge Rating for monsters (e.g., 0.125, 1, 5, 30). */
    @Column(name = "cr")
    private Double cr;

    /** Rarity for magic items/treasure (Common, Uncommon, Rare, Very Rare, Legendary, Artifact). */
    @Column(name = "rarity", length = 20)
    private String rarity;

    /** DC for traps/skills (e.g., 10, 15, 20). */
    @Column(name = "dc")
    private Integer dc;

    /** Damage type for traps/weapons (e.g., "poison", "bludgeoning", "fire"). */
    @Column(name = "damage_type", length = 20)
    private String damageType;

    /** Damage expression (e.g., "2d6", "4d8+4"). */
    @Column(name = "damage_expr", length = 20)
    private String damageExpr;

    /** Spell level (0-9) or null for non-spells. */
    @Column(name = "spell_level")
    private Integer spellLevel;

    /** Spell school (e.g., "Abjuration", "Evocation", "Necromancy"). */
    @Column(name = "spell_school", length = 20)
    private String spellSchool;

    /** Monster/creature type (e.g., "Humanoid", "Dragon", "Undead", "Beast"). */
    @Column(name = "creature_type", length = 20)
    private String creatureType;

    /** Size (Tiny, Small, Medium, Large, Huge, Gargantuan). */
    @Column(name = "size", length = 15)
    private String size;

    /** HP for monsters/objects. */
    @Column(name = "hp")
    private Integer hp;

    /** AC for monsters/armor/objects. */
    @Column(name = "ac")
    private Integer ac;

    /** Gold piece value for treasure. */
    @Column(name = "gp_value")
    private Integer gpValue;

    /** Weight in pounds for gear/items. */
    @Column(name = "weight_lb")
    private Integer weightLb;

    // === Animal-specific fields (nullable, used for ANIMAL category) ===

    /** Milk yield in liters per day. */
    @Column(name = "milk_yield")
    private Double milkYield;

    /** Wool yield in kg per year. */
    @Column(name = "wool_yield")
    private Double woolYield;

    /** Carrying capacity in kg (for transport animals). */
    @Column(name = "carrying_capacity")
    private Integer carryingCapacity;

    /** Egg rate (eggs per week). */
    @Column(name = "egg_rate")
    private Integer eggRate;

    /** Meat yield in kg per animal. */
    @Column(name = "meat_yield")
    private Double meatYield;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PlanetBuildingModuleEntity() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Convenience constructor for seeding data.
     */
    public PlanetBuildingModuleEntity(TechLevel techLevel, ModuleCategory category,
                                      String genericType, String genericName,
                                      int baseCost, int baseCapacity, String description) {
        this();
        this.techLevel = techLevel;
        this.category = category;
        this.genericType = genericType;
        this.genericName = genericName;
        this.baseCost = baseCost;
        this.baseCapacity = baseCapacity;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TechLevel getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(TechLevel techLevel) {
        this.techLevel = techLevel;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public void setCategory(ModuleCategory category) {
        this.category = category;
    }

    public String getGenericType() {
        return genericType;
    }

    public void setGenericType(String genericType) {
        this.genericType = genericType;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getLoreName() {
        return loreName;
    }

    public void setLoreName(String loreName) {
        this.loreName = loreName;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(int baseCost) {
        this.baseCost = baseCost;
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public void setBaseCapacity(int baseCapacity) {
        this.baseCapacity = baseCapacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // === Dungeon-specific getters and setters ===

    public Double getCr() {
        return cr;
    }

    public void setCr(Double cr) {
        this.cr = cr;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public Integer getDc() {
        return dc;
    }

    public void setDc(Integer dc) {
        this.dc = dc;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getDamageExpr() {
        return damageExpr;
    }

    public void setDamageExpr(String damageExpr) {
        this.damageExpr = damageExpr;
    }

    public Integer getSpellLevel() {
        return spellLevel;
    }

    public void setSpellLevel(Integer spellLevel) {
        this.spellLevel = spellLevel;
    }

    public String getSpellSchool() {
        return spellSchool;
    }

    public void setSpellSchool(String spellSchool) {
        this.spellSchool = spellSchool;
    }

    public String getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(String creatureType) {
        this.creatureType = creatureType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getAc() {
        return ac;
    }

    public void setAc(Integer ac) {
        this.ac = ac;
    }

    public Integer getGpValue() {
        return gpValue;
    }

    public void setGpValue(Integer gpValue) {
        this.gpValue = gpValue;
    }

    public Integer getWeightLb() {
        return weightLb;
    }

    public void setWeightLb(Integer weightLb) {
        this.weightLb = weightLb;
    }

    // === Animal-specific getters and setters ===

    public Double getMilkYield() {
        return milkYield;
    }

    public void setMilkYield(Double milkYield) {
        this.milkYield = milkYield;
    }

    public Double getWoolYield() {
        return woolYield;
    }

    public void setWoolYield(Double woolYield) {
        this.woolYield = woolYield;
    }

    public Integer getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(Integer carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public Integer getEggRate() {
        return eggRate;
    }

    public void setEggRate(Integer eggRate) {
        this.eggRate = eggRate;
    }

    public Double getMeatYield() {
        return meatYield;
    }

    public void setMeatYield(Double meatYield) {
        this.meatYield = meatYield;
    }
}

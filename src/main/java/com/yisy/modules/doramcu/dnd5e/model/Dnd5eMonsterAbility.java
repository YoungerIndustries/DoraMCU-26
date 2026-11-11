package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Monster Ability (2024 MM).
 * <p>
 * Monster abilities include attacks (melee, ranged), spells, legendary actions,
 * lair actions, and special traits that a monster can use in combat.
 * </p>
 * <p>
 * Key mechanics:
 * - Type: Attack (Melee/Ranged), Spell, Legendary Action, Lair Action, Regional Effect, Trait, Action, Reaction, Bonus Action
 * - Name: The ability name (e.g., "Bite", "Fire Breath", "Legendary Resistance")
 * - Attack Bonus: To-hit bonus for attacks (e.g., +5)
 * - Damage: Damage expression (e.g., "2d6 + 3 fire")
 * - Save DC: Saving throw DC for spells and breath weapons
 * - Range: Attack or spell range in feet
 * - Uses: Number of uses per long rest (0 if unlimited)
 * - Recharge: Recharge on a roll (e.g., "5-6" means recharge on 5-6)
 * - Description: Full mechanical effect of the ability
 * </p>
 */
@Entity
@Table(name = "dnd5e_monster_abilities", indexes = {
        @Index(name = "idx_dnd5e_monster_ability_monster", columnList = "monsterName"),
        @Index(name = "idx_dnd5e_monster_ability_type", columnList = "type"),
        @Index(name = "idx_dnd5e_monster_ability_name", columnList = "monsterName, name")
})
public class Dnd5eMonsterAbility {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The monster this ability belongs to (by name).
     */
    @Column(name = "monsterName", nullable = false, length = 100)
    private String monsterName;

    /**
     * Type of ability: Attack, Spell, Legendary Action, Lair Action, Regional Effect, Trait, Action, Reaction, Bonus Action.
     */
    @Column(nullable = false, length = 30)
    private String type;

    /**
     * Subtype for attacks: Melee, Ranged.
     */
    @Column(name = "subtype", length = 20)
    private String subtype;

    /**
     * Name of the ability (e.g., "Bite", "Fire Breath", "Legendary Resistance").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Full description of the ability's mechanical effects.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * To-hit bonus for attacks (e.g., +5).
     */
    @Column(name = "attackBonus")
    private int attackBonus = 0;

    /**
     * Damage expression (e.g., "2d6 + 3 fire").
     */
    @Column(length = 100)
    private String damage;

    /**
     * Damage type (e.g., "Piercing", "Fire", "Necrotic").
     */
    @Column(name = "damageType", length = 30)
    private String damageType;

    /**
     * Saving throw DC for spells and breath weapons.
     */
    @Column(name = "saveDC")
    private int saveDC = 0;

    /**
     * Saving throw ability for spells and breath weapons (STR, DEX, CON, INT, WIS, CHA).
     */
    @Column(name = "saveAbility", length = 10)
    private String saveAbility;

    /**
     * Range in feet (e.g., 5 for melee, 60 for ranged).
     */
    @Column(nullable = false)
    private int range = 5;

    /**
     * Number of uses per long rest (0 if unlimited).
     */
    @Column(name = "usesPerLongRest")
    private int usesPerLongRest = 0;

    /**
     * Recharge on a roll (e.g., "5-6" means recharge on 5-6).
     */
    @Column(length = 10)
    private String recharge;

    /**
     * Whether this ability is active and available.
     */
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public void setAttackBonus(int attackBonus) {
        this.attackBonus = attackBonus;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public int getSaveDC() {
        return saveDC;
    }

    public void setSaveDC(int saveDC) {
        this.saveDC = saveDC;
    }

    public String getSaveAbility() {
        return saveAbility;
    }

    public void setSaveAbility(String saveAbility) {
        this.saveAbility = saveAbility;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getUsesPerLongRest() {
        return usesPerLongRest;
    }

    public void setUsesPerLongRest(int usesPerLongRest) {
        this.usesPerLongRest = usesPerLongRest;
    }

    public String getRecharge() {
        return recharge;
    }

    public void setRecharge(String recharge) {
        this.recharge = recharge;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eMonsterAbility{monster='" + monsterName + "', type='" + type + "', name='" + name + "'}";
    }
}

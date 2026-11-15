package com.yisy.modules.doramcu.traveller.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Traveller Monster Ability.
 * <p>
 * Monster abilities include attacks (melee, ranged), tech abilities, legendary actions,
 * lair actions, and special traits that a monster can use in combat.
 * </p>
 */
@Entity
@Table(name = "traveller_monster_abilities", indexes = {
        @Index(name = "idx_traveller_monster_ability_monster", columnList = "monsterName"),
        @Index(name = "idx_traveller_monster_ability_type", columnList = "type"),
        @Index(name = "idx_traveller_monster_ability_name", columnList = "monsterName, name")
})
public class TravellerMonsterAbility {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "monsterName", nullable = false, length = 100)
    private String monsterName;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "subtype", length = 20)
    private String subtype;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "attackBonus")
    private int attackBonus = 0;

    @Column(length = 100)
    private String damage;

    @Column(name = "damageType", length = 30)
    private String damageType;

    @Column(name = "saveDC")
    private int saveDC = 0;

    @Column(name = "saveAbility", length = 10)
    private String saveAbility;

    @Column(nullable = false)
    private int range = 5;

    @Column(name = "usesPerLongRest")
    private int usesPerLongRest = 0;

    @Column(length = 10)
    private String recharge;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMonsterName() { return monsterName; }
    public void setMonsterName(String monsterName) { this.monsterName = monsterName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }

    public String getDamage() { return damage; }
    public void setDamage(String damage) { this.damage = damage; }

    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }

    public int getSaveDC() { return saveDC; }
    public void setSaveDC(int saveDC) { this.saveDC = saveDC; }

    public String getSaveAbility() { return saveAbility; }
    public void setSaveAbility(String saveAbility) { this.saveAbility = saveAbility; }

    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }

    public int getUsesPerLongRest() { return usesPerLongRest; }
    public void setUsesPerLongRest(int usesPerLongRest) { this.usesPerLongRest = usesPerLongRest; }

    public String getRecharge() { return recharge; }
    public void setRecharge(String recharge) { this.recharge = recharge; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "TravellerMonsterAbility{monster='" + monsterName + "', type='" + type + "', name='" + name + "'}";
    }
}

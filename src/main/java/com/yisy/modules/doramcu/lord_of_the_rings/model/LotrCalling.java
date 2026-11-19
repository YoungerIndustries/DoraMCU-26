package com.yisy.modules.doramcu.lord_of_the_rings.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Lord of the Rings Calling (character class).
 * <p>
 * Callings define a character's role and abilities. LOTR callings include:
 * Scout, Warrior, Ranger, Bard, Mage, Healer, and more.
 * Each calling has a hit die, primary abilities, and special powers.
 * </p>
 */
@Entity
@Table(name = "lotr_callings", indexes = {
        @Index(name = "idx_lotr_calling_name", columnList = "name")
})
public class LotrCalling {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hit_die", nullable = false, length = 10)
    private String hitDie;

    @Column(name = "primary_ability", nullable = false, length = 10)
    private String primaryAbility;

    @Column(name = "armor_proficiency", length = 50)
    private String armorProficiency;

    @Column(name = "weapon_proficiency", length = 100)
    private String weaponProficiency;

    @Column(name = "skill_slots_1", columnDefinition = "TEXT")
    private String skillSlots1;

    @Column(name = "power_description", columnDefinition = "TEXT")
    private String powerDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getHitDie() {
        return hitDie;
    }

    public void setHitDie(String hitDie) {
        this.hitDie = hitDie;
    }

    public String getPrimaryAbility() {
        return primaryAbility;
    }

    public void setPrimaryAbility(String primaryAbility) {
        this.primaryAbility = primaryAbility;
    }

    public String getArmorProficiency() {
        return armorProficiency;
    }

    public void setArmorProficiency(String armorProficiency) {
        this.armorProficiency = armorProficiency;
    }

    public String getWeaponProficiency() {
        return weaponProficiency;
    }

    public void setWeaponProficiency(String weaponProficiency) {
        this.weaponProficiency = weaponProficiency;
    }

    public String getSkillSlots1() {
        return skillSlots1;
    }

    public void setSkillSlots1(String skillSlots1) {
        this.skillSlots1 = skillSlots1;
    }

    public String getPowerDescription() {
        return powerDescription;
    }

    public void setPowerDescription(String powerDescription) {
        this.powerDescription = powerDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LotrCalling{name='" + name + "', hitDie='" + hitDie + "'}";
    }
}

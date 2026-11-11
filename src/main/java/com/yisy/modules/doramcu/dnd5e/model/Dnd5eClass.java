package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Class (2024 PHB).
 * <p>
 * Classes define a character's combat and roleplaying abilities, including
 * hit dice, primary ability scores, proficiencies, and spellcasting capabilities.
 * </p>
 * <p>
 * Key mechanics:
 * - Hit Die: The die used for hit point gains (d6, d8, d10, d12)
 * - Primary Ability: The main ability score for the class (STR, DEX, INT, WIS, CHA)
 * - Proficiencies: Armor, weapon, tool proficiencies granted by the class
 * - Spellcasting: Whether the class is a full caster, half caster, or pact magician
 * - Spellcasting Ability: The ability score used for spell attacks and DCs
 * - Saving Throw Proficiencies: Two saving throw proficiencies granted by the class
 * - Subclasses: Available at level 2, defining a specialization path
 * </p>
 */
@Entity
@Table(name = "dnd5e_classes", indexes = {
        @Index(name = "idx_dnd5e_class_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_class_hitdie", columnList = "hitDie"),
        @Index(name = "idx_dnd5e_class_primary_ability", columnList = "primaryAbility")
})
public class Dnd5eClass {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the class (e.g., "Barbarian", "Wizard", "Rogue").
     */
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    /**
     * Full description of the class's lore and roleplaying guidance.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Hit die for the class (d6, d8, d10, d12).
     */
    @Column(name = "hitDie", nullable = false, length = 5)
    private String hitDie;

    /**
     * Primary ability score for the class (STR, DEX, CON, INT, WIS, CHA).
     */
    @Column(name = "primaryAbility", nullable = false, length = 10)
    private String primaryAbility;

    /**
     * Armor proficiencies granted by this class (comma-separated, e.g., "Light armor, Medium armor, Shields").
     */
    @Column(name = "armorProficiencies", length = 300)
    private String armorProficiencies;

    /**
     * Weapon proficiencies granted by this class (comma-separated, e.g., "Simple weapons, Martial weapons").
     */
    @Column(name = "weaponProficiencies", nullable = false, length = 300)
    private String weaponProficiencies;

    /**
     * Tool proficiencies granted by this class (comma-separated, e.g., "Thieves' tools, Herbalism kit").
     */
    @Column(name = "toolProficiencies", length = 300)
    private String toolProficiencies;

    /**
     * Saving throw proficiencies granted by this class (comma-separated, e.g., "Strength, Constitution").
     */
    @Column(name = "savingThrowProficiencies", nullable = false, length = 100)
    private String savingThrowProficiencies;

    /**
     * Whether this class has spellcasting.
     */
    @Column(name = "hasSpellcasting", nullable = false)
    private boolean hasSpellcasting = false;

    /**
     * Spellcasting type: "Full", "Half", "Pact", "Spellcasting", "Eldritch Burst", or "None".
     */
    @Column(name = "spellcastingType", length = 30)
    private String spellcastingType;

    /**
     * Spellcasting ability score (INT, WIS, CHA, or "None").
     */
    @Column(name = "spellcastingAbility", length = 10)
    private String spellcastingAbility;

    /**
     * Maximum spell level the class can cast (0-9, 0 if no spellcasting).
     */
    @Column(name = "maxSpellLevel")
    private int maxSpellLevel = 0;

    /**
     * Available subclass names (comma-separated, e.g., "Path of the Berserker, Path of the Totem Warrior").
     */
    @Column(name = "subclasses", columnDefinition = "TEXT")
    private String subclasses;

    /**
     * Whether this class is active and available for use.
     */
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a class with all fields.
     */
    public Dnd5eClass(String name, String description, String hitDie, String primaryAbility,
                      String armorProficiencies, String weaponProficiencies, String toolProficiencies,
                      String savingThrowProficiencies, boolean hasSpellcasting, String spellcastingType,
                      String spellcastingAbility, int maxSpellLevel, String subclasses) {
        this.name = name;
        this.description = description;
        this.hitDie = hitDie;
        this.primaryAbility = primaryAbility;
        this.armorProficiencies = armorProficiencies;
        this.weaponProficiencies = weaponProficiencies;
        this.toolProficiencies = toolProficiencies;
        this.savingThrowProficiencies = savingThrowProficiencies;
        this.hasSpellcasting = hasSpellcasting;
        this.spellcastingType = spellcastingType;
        this.spellcastingAbility = spellcastingAbility;
        this.maxSpellLevel = maxSpellLevel;
        this.subclasses = subclasses;
        this.active = true;
    }

    public Dnd5eClass() {
    }

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

    public String getArmorProficiencies() {
        return armorProficiencies;
    }

    public void setArmorProficiencies(String armorProficiencies) {
        this.armorProficiencies = armorProficiencies;
    }

    public String getWeaponProficiencies() {
        return weaponProficiencies;
    }

    public void setWeaponProficiencies(String weaponProficiencies) {
        this.weaponProficiencies = weaponProficiencies;
    }

    public String getToolProficiencies() {
        return toolProficiencies;
    }

    public void setToolProficiencies(String toolProficiencies) {
        this.toolProficiencies = toolProficiencies;
    }

    public String getSavingThrowProficiencies() {
        return savingThrowProficiencies;
    }

    public void setSavingThrowProficiencies(String savingThrowProficiencies) {
        this.savingThrowProficiencies = savingThrowProficiencies;
    }

    public boolean isHasSpellcasting() {
        return hasSpellcasting;
    }

    public void setHasSpellcasting(boolean hasSpellcasting) {
        this.hasSpellcasting = hasSpellcasting;
    }

    public String getSpellcastingType() {
        return spellcastingType;
    }

    public void setSpellcastingType(String spellcastingType) {
        this.spellcastingType = spellcastingType;
    }

    public String getSpellcastingAbility() {
        return spellcastingAbility;
    }

    public void setSpellcastingAbility(String spellcastingAbility) {
        this.spellcastingAbility = spellcastingAbility;
    }

    public int getMaxSpellLevel() {
        return maxSpellLevel;
    }

    public void setMaxSpellLevel(int maxSpellLevel) {
        this.maxSpellLevel = maxSpellLevel;
    }

    public String getSubclasses() {
        return subclasses;
    }

    public void setSubclasses(String subclasses) {
        this.subclasses = subclasses;
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
        return "Dnd5eClass{name='" + name + "', hitDie='" + hitDie + "', primaryAbility='" + primaryAbility + "'}";
    }
}

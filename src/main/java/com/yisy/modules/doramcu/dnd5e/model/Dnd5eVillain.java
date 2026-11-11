package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a D&D 5e Villain from Villainous Options UA 2026.
 * <p>
 * Villains are powerful antagonists with unique mechanics including lair actions,
 * regional effects, minions, and progression paths (e.g., Lich/Death Knight feats).
 * </p>
 * <p>
 * Key mechanics:
 * - Villain Type: Lich, Death Knight, Archfey, Dragon, Demon Lord, etc.
 * - Challenge Rating: CR for encounter balancing
 * - Lair Actions: Special actions available in the villain's lair
 * - Regional Effects: Effects that permeate the villain's territory
 * - Minions: Creatures the villain can summon or command
 * - Motives: The villain's goals and driving forces
 * - Resources: Hit points, spell slots, legendary resistances
 * </p>
 */
@Entity
@Table(name = "dnd5e_villains", indexes = {
        @Index(name = "idx_dnd5e_villain_name", columnList = "name"),
        @Index(name = "idx_dnd5e_villain_type", columnList = "villainType"),
        @Index(name = "idx_dnd5e_villain_cr", columnList = "challengeRating")
})
public class Dnd5eVillain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Type of villain: Lich, Death Knight, Archfey, Dragon, Demon Lord, etc.
     */
    @Column(name = "villain_type", nullable = false, length = 50)
    private String villainType;

    /**
     * Challenge Rating of the villain (0-30).
     */
    @Column(name = "challenge_rating", nullable = false)
    private int challengeRating;

    /**
     * Description of the villain's lore and appearance.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * The villain's primary motive or goal.
     */
    @Column(nullable = false, length = 500)
    private String motive;

    /**
     * Hit Points of the villain.
     */
    @Column(name = "hit_points", nullable = false)
    private int hitPoints;

    /**
     * Armor Class of the villain.
     */
    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    /**
     * Legendary Resistances remaining.
     */
    @Column(name = "legendary_resistances", nullable = false)
    private int legendaryResistances = 3;

    /**
     * Maximum Legendary Resistances.
     */
    @Column(name = "max_legendary_resistances", nullable = false)
    private int maxLegendaryResistances = 3;

    /**
     * Action Surge availability.
     */
    @Column(name = "action_surge", nullable = false)
    private boolean actionSurge = false;

    /**
     * Lair action description.
     */
    @Column(name = "lair_action", columnDefinition = "TEXT")
    private String lairAction;

    /**
     * Regional effect description.
     */
    @Column(name = "regional_effect", columnDefinition = "TEXT")
    private String regionalEffect;

    /**
     * Minion types the villain can command (comma-separated).
     */
    @Column(name = "minion_types", length = 500)
    private String minionTypes;

    /**
     * Special abilities or traits (comma-separated).
     */
    @Column(name = "special_abilities", length = 500)
    private String specialAbilities;

    /**
     * Spellcasting ability: INT, WIS, or CHA.
     */
    @Column(name = "spellcasting_ability", length = 10)
    private String spellcastingAbility;

    /**
     * Spell save DC for the villain.
     */
    @Column(name = "spell_save_dc")
    private Integer spellSaveDc;

    /**
     * Whether this villain is a Lich with phylactery.
     */
    @Column(name = "is_lich", nullable = false)
    private boolean isLich = false;

    /**
     * Phylactery description (for Liches).
     */
    @Column(name = "phylactery", length = 500)
    private String phylactery;

    /**
     * Whether this villain is a Death Knight.
     */
    @Column(name = "is_death_knight", nullable = false)
    private boolean isDeathKnight = false;

    /**
     * Death Knight's original class (if applicable).
     */
    @Column(name = "death_knight_class", length = 50)
    private String deathKnightClass;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVillainType() {
        return villainType;
    }

    public void setVillainType(String villainType) {
        this.villainType = villainType;
    }

    public int getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(int challengeRating) {
        this.challengeRating = Math.max(0, Math.min(30, challengeRating));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMotive() {
        return motive;
    }

    public void setMotive(String motive) {
        this.motive = motive;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = Math.max(0, hitPoints);
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public int getLegendaryResistances() {
        return legendaryResistances;
    }

    public void setLegendaryResistances(int legendaryResistances) {
        this.legendaryResistances = Math.max(0, Math.min(maxLegendaryResistances, legendaryResistances));
    }

    public int getMaxLegendaryResistances() {
        return maxLegendaryResistances;
    }

    public void setMaxLegendaryResistances(int maxLegendaryResistances) {
        this.maxLegendaryResistances = Math.max(0, maxLegendaryResistances);
    }

    public boolean isActionSurge() {
        return actionSurge;
    }

    public void setActionSurge(boolean actionSurge) {
        this.actionSurge = actionSurge;
    }

    public String getLairAction() {
        return lairAction;
    }

    public void setLairAction(String lairAction) {
        this.lairAction = lairAction;
    }

    public String getRegionalEffect() {
        return regionalEffect;
    }

    public void setRegionalEffect(String regionalEffect) {
        this.regionalEffect = regionalEffect;
    }

    public String getMinionTypes() {
        return minionTypes;
    }

    public void setMinionTypes(String minionTypes) {
        this.minionTypes = minionTypes;
    }

    public String getSpecialAbilities() {
        return specialAbilities;
    }

    public void setSpecialAbilities(String specialAbilities) {
        this.specialAbilities = specialAbilities;
    }

    public String getSpellcastingAbility() {
        return spellcastingAbility;
    }

    public void setSpellcastingAbility(String spellcastingAbility) {
        this.spellcastingAbility = spellcastingAbility;
    }

    public Integer getSpellSaveDc() {
        return spellSaveDc;
    }

    public void setSpellSaveDc(Integer spellSaveDc) {
        this.spellSaveDc = spellSaveDc;
    }

    public boolean isLich() {
        return isLich;
    }

    public void setLich(boolean lich) {
        isLich = lich;
    }

    public String getPhylactery() {
        return phylactery;
    }

    public void setPhylactery(String phylactery) {
        this.phylactery = phylactery;
    }

    public boolean isDeathKnight() {
        return isDeathKnight;
    }

    public void setDeathKnight(boolean deathKnight) {
        isDeathKnight = deathKnight;
    }

    public String getDeathKnightClass() {
        return deathKnightClass;
    }

    public void setDeathKnightClass(String deathKnightClass) {
        this.deathKnightClass = deathKnightClass;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Dnd5eVillain{name='" + name + "', type='" + villainType + "', CR=" + challengeRating + "}";
    }
}

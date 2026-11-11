package com.yisy.modules.doramcu.dnd5e.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a D&D 5e Monster (2024 MM).
 * <p>
 * Monsters are creatures that DMs use for encounters, from common vermin to ancient dragons.
 * Each monster has a Challenge Rating (CR) that determines its difficulty and XP value.
 * </p>
 * <p>
 * Key mechanics:
 * - CR: Challenge Rating (0 to 30), determines encounter difficulty
 * - XP: Experience points awarded for defeating the monster
 * - Size: Tiny, Small, Medium, Large, Huge, Gargantuan
 * - Type: Aberration, Beast, Celestial, Construct, Dragon, Elemental, Fey, Giant, Humanoid, Monstrosity, Plant, Undead
 * - Alignment: Lawful Good to Chaotic Evil
 * - Stats: STR, DEX, CON, INT, WIS, CHA (1-30)
 * - AC: Armor Class (10-30)
 * - HP: Hit Points
 * - Speed: Movement speed in feet (walk, fly, swim, climb)
 * - Immunities: Damage types the monster is immune to
 * - Resistances: Damage types the monster resists (half damage)
 * - Vulnerabilities: Damage types the monster is vulnerable to (double damage)
 * - Senses: Darkvision, blindsight, tremorsense, truesight
 * - Lair Actions: Special actions in the monster's lair
 * - Legendary Actions: Special actions at the end of other creatures' turns
 * - Regional Effects: Effects in the monster's territory (CR 15+)
 * </p>
 */
@Entity
@Table(name = "dnd5e_monsters", indexes = {
        @Index(name = "idx_dnd5e_monster_name", columnList = "name", unique = true),
        @Index(name = "idx_dnd5e_monster_cr", columnList = "cr"),
        @Index(name = "idx_dnd5e_monster_type", columnList = "type"),
        @Index(name = "idx_dnd5e_monster_size", columnList = "size"),
        @Index(name = "idx_dnd5e_monster_xp", columnList = "xp")
})
public class Dnd5eMonster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the monster (e.g., "Goblin", "Red Dragon", "Lich").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Challenge Rating (0-30).
     */
    @Column(name = "cr", nullable = false)
    private double cr;

    /**
     * Experience points awarded for defeating this monster.
     */
    @Column(name = "xp", nullable = false)
    private int xp;

    /**
     * Size: Tiny, Small, Medium, Large, Huge, Gargantuan.
     */
    @Column(nullable = false, length = 20)
    private String size;

    /**
     * Type: Aberration, Beast, Celestial, Construct, Dragon, Elemental, Fey, Giant, Humanoid, Monstrosity, Plant, Undead.
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * Subtype for Humanoids (e.g., "Human", "Elf", "Orc").
     */
    @Column(name = "subtype", length = 50)
    private String subtype;

    /**
     * Alignment: Lawful Good, Neutral Good, Chaotic Good, Lawful Neutral, True Neutral, Chaotic Neutral, Lawful Evil, Neutral Evil, Chaotic Evil.
     */
    @Column(nullable = false, length = 20)
    private String alignment;

    /**
     * Armor Class.
     */
    @Column(name = "ac", nullable = false)
    private int ac;

    /**
     * Hit Points.
     */
    @Column(name = "hp", nullable = false)
    private int hp;

    /**
     * Walking speed in feet.
     */
    @Column(name = "speed", nullable = false)
    private int speed;

    /**
     * Flying speed in feet (0 if no flight).
     */
    @Column(name = "flySpeed")
    private int flySpeed = 0;

    /**
     * Swimming speed in feet (0 if no swimming).
     */
    @Column(name = "swimSpeed")
    private int swimSpeed = 0;

    /**
     * Climbing speed in feet (0 if no climbing).
     */
    @Column(name = "climbSpeed")
    private int climbSpeed = 0;

    /**
     * Strength score (1-30).
     */
    @Column(nullable = false)
    private int str;

    /**
     * Dexterity score (1-30).
     */
    @Column(nullable = false)
    private int dex;

    /**
     * Constitution score (1-30).
     */
    @Column(nullable = false)
    private int con;

    /**
     * Intelligence score (1-30).
     */
    @Column(nullable = false)
    private int intel;

    /**
     * Wisdom score (1-30).
     */
    @Column(nullable = false)
    private int wis;

    /**
     * Charisma score (1-30).
     */
    @Column(nullable = false)
    private int cha;

    /**
     * Saving throw bonuses (pipe-separated, e.g., "STR +5 | CON +4 | WIS +3").
     */
    @Column(name = "savingThrows", length = 200)
    private String savingThrows;

    /**
     * Skill bonuses (pipe-separated, e.g., "Athletics +5 | Stealth +9").
     */
    @Column(name = "skills", length = 300)
    private String skills;

    /**
     * Damage immunities (comma-separated, e.g., "Poison, Necrotic").
     */
    @Column(name = "damageImmunities", length = 200)
    private String damageImmunities;

    /**
     * Damage resistances (comma-separated, e.g., "Bludgeoning, Piercing, Slashing").
     */
    @Column(name = "damageResistances", length = 200)
    private String damageResistances;

    /**
     * Damage vulnerabilities (comma-separated, e.g., "Radiant").
     */
    @Column(name = "damageVulnerabilities", length = 200)
    private String damageVulnerabilities;

    /**
     * Condition immunities (comma-separated, e.g., "Charmed, Frightened, Poisoned").
     */
    @Column(name = "conditionImmunities", length = 200)
    private String conditionImmunities;

    /**
     * Senses (e.g., "Darkvision 60ft", "Blindsight 30ft", "Truesight 120ft").
     */
    @Column(nullable = false, length = 200)
    private String senses;

    /**
     * Languages spoken.
     */
    @Column(nullable = false, length = 200)
    private String languages;

    /**
     * Full description of the monster's lore and appearance.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether this monster has lair actions.
     */
    @Column(name = "hasLairActions", nullable = false)
    private boolean hasLairActions = false;

    /**
     * Whether this monster has legendary actions.
     */
    @Column(name = "hasLegendaryActions", nullable = false)
    private boolean hasLegendaryActions = false;

    /**
     * Whether this monster has regional effects (CR 15+).
     */
    @Column(name = "hasRegionalEffects", nullable = false)
    private boolean hasRegionalEffects = false;

    /**
     * Whether this monster is active and available.
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCr() {
        return cr;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(int flySpeed) {
        this.flySpeed = flySpeed;
    }

    public int getSwimSpeed() {
        return swimSpeed;
    }

    public void setSwimSpeed(int swimSpeed) {
        this.swimSpeed = swimSpeed;
    }

    public int getClimbSpeed() {
        return climbSpeed;
    }

    public void setClimbSpeed(int climbSpeed) {
        this.climbSpeed = climbSpeed;
    }

    public int getStr() {
        return str;
    }

    public void setStr(int str) {
        this.str = str;
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
    }

    public int getCon() {
        return con;
    }

    public void setCon(int con) {
        this.con = con;
    }

    public int getIntel() {
        return intel;
    }

    public void setIntel(int intel) {
        this.intel = intel;
    }

    public int getWis() {
        return wis;
    }

    public void setWis(int wis) {
        this.wis = wis;
    }

    public int getCha() {
        return cha;
    }

    public void setCha(int cha) {
        this.cha = cha;
    }

    public String getSavingThrows() {
        return savingThrows;
    }

    public void setSavingThrows(String savingThrows) {
        this.savingThrows = savingThrows;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getDamageImmunities() {
        return damageImmunities;
    }

    public void setDamageImmunities(String damageImmunities) {
        this.damageImmunities = damageImmunities;
    }

    public String getDamageResistances() {
        return damageResistances;
    }

    public void setDamageResistances(String damageResistances) {
        this.damageResistances = damageResistances;
    }

    public String getDamageVulnerabilities() {
        return damageVulnerabilities;
    }

    public void setDamageVulnerabilities(String damageVulnerabilities) {
        this.damageVulnerabilities = damageVulnerabilities;
    }

    public String getConditionImmunities() {
        return conditionImmunities;
    }

    public void setConditionImmunities(String conditionImmunities) {
        this.conditionImmunities = conditionImmunities;
    }

    public String getSenses() {
        return senses;
    }

    public void setSenses(String senses) {
        this.senses = senses;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasLairActions() {
        return hasLairActions;
    }

    public void setHasLairActions(boolean hasLairActions) {
        this.hasLairActions = hasLairActions;
    }

    public boolean isHasLegendaryActions() {
        return hasLegendaryActions;
    }

    public void setHasLegendaryActions(boolean hasLegendaryActions) {
        this.hasLegendaryActions = hasLegendaryActions;
    }

    public boolean isHasRegionalEffects() {
        return hasRegionalEffects;
    }

    public void setHasRegionalEffects(boolean hasRegionalEffects) {
        this.hasRegionalEffects = hasRegionalEffects;
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
        return "Dnd5eMonster{name='" + name + "', CR=" + cr + ", XP=" + xp + "}";
    }
}

package com.yisy.modules.doramcu.earth2026.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an Earth 2026 Monster (near-future threat).
 * <p>
 * Monsters are hostile entities that players encounter, from street gangs and
 * mercenaries to rogue AI, bio-engineered creatures, and supernatural entities.
 * Each monster has a Threat Level (1-10) that determines its difficulty.
 * </p>
 * <p>
 * Key mechanics:
 * - Threat Level: 1 to 10, replaces D&D Challenge Rating
 * - XP: Experience points awarded for defeating the monster
 * - Size: Tiny (drone), Small (dog), Medium (human), Large (vehicle), Huge (building), Gargantuan (facility)
 * - Type: Human, Military, Criminal, AI, Bio-Engineered, Alien, Supernatural, Vehicle, Drone, Robot
 * - Alignment: Lawful Good to Chaotic Evil (same as D&D for cross-galaxy compatibility)
 * - Stats: STR, DEX, CON, INT, WIS, CHA (1-20, Earth 2026 scale)
 * - AC: Armor Class (10-25)
 * - HP: Hit Points
 * - Speed: Movement speed in meters (walk, fly, swim, climb)
 * - Immunities: Damage types the monster is immune to (ballistic, energy, chemical, explosive, kinetic)
 * - Resistances: Damage types the monster resists (half damage)
 * - Vulnerabilities: Damage types the monster is vulnerable to (double damage)
 * - Senses: Night vision, thermal, motion detection, surveillance
 * - Special Abilities: JSON-encoded abilities specific to Earth 2026
 * </p>
 */
@Entity
@Table(name = "e26_monsters", indexes = {
        @Index(name = "idx_e26_monster_name", columnList = "name", unique = true),
        @Index(name = "idx_e26_monster_threat_level", columnList = "threatLevel"),
        @Index(name = "idx_e26_monster_type", columnList = "type"),
        @Index(name = "idx_e26_monster_size", columnList = "size"),
        @Index(name = "idx_e26_monster_xp", columnList = "xp")
})
public class E26Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the monster (e.g., "Street Gang", "SWAT Team", "Rogue AI", "Bio-Weapon").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Threat Level (1-10). Replaces D&D Challenge Rating.
     */
    @Column(name = "threat_level", nullable = false)
    private int threatLevel;

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
     * Type: Human, Military, Criminal, AI, Bio-Engineered, Alien, Supernatural, Vehicle, Drone, Robot.
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * Subtype for more specific categorization (e.g., "Gang", "Special Forces", "Hacker").
     */
    @Column(name = "subtype", length = 50)
    private String subtype;

    /**
     * Alignment: Lawful Good to Chaotic Evil.
     */
    @Column(nullable = false, length = 20)
    private String alignment;

    /**
     * Armor Class (10-25).
     */
    @Column(name = "ac", nullable = false)
    private int ac;

    /**
     * Hit Points.
     */
    @Column(name = "hp", nullable = false)
    private int hp;

    /**
     * Walking speed in meters.
     */
    @Column(name = "speed", nullable = false)
    private int speed;

    /**
     * Flying speed in meters (0 if no flight).
     */
    @Column(name = "flySpeed")
    private int flySpeed = 0;

    /**
     * Swimming speed in meters (0 if no swimming).
     */
    @Column(name = "swimSpeed")
    private int swimSpeed = 0;

    /**
     * Climbing speed in meters (0 if no climbing).
     */
    @Column(name = "climbSpeed")
    private int climbSpeed = 0;

    /**
     * Strength score (1-20).
     */
    @Column(nullable = false)
    private int str;

    /**
     * Dexterity score (1-20).
     */
    @Column(nullable = false)
    private int dex;

    /**
     * Constitution score (1-20).
     */
    @Column(nullable = false)
    private int con;

    /**
     * Intelligence score (1-20).
     */
    @Column(nullable = false)
    private int intel;

    /**
     * Wisdom score (1-20).
     */
    @Column(nullable = false)
    private int wis;

    /**
     * Charisma score (1-20).
     */
    @Column(nullable = false)
    private int cha;

    /**
     * Skill bonuses (pipe-separated, e.g., "Stealth +5 | Persuasion +3").
     */
    @Column(name = "skills", length = 300)
    private String skills;

    /**
     * Damage immunities (comma-separated, e.g., "Ballistic, Energy").
     */
    @Column(name = "damageImmunities", length = 200)
    private String damageImmunities;

    /**
     * Damage resistances (comma-separated, e.g., "Chemical, Explosive").
     */
    @Column(name = "damageResistances", length = 200)
    private String damageResistances;

    /**
     * Damage vulnerabilities (comma-separated, e.g., "Energy").
     */
    @Column(name = "damageVulnerabilities", length = 200)
    private String damageVulnerabilities;

    /**
     * Condition immunities (comma-separated, e.g., "Poisoned, Frightened").
     */
    @Column(name = "conditionImmunities", length = 200)
    private String conditionImmunities;

    /**
     * Senses (e.g., "Night Vision 30m", "Thermal Vision", "Motion Detection").
     */
    @Column(nullable = false, length = 200)
    private String senses;

    /**
     * Full description of the monster's lore and appearance.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Special abilities in JSON format.
     */
    @Column(name = "specialAbilities", columnDefinition = "TEXT")
    private String specialAbilities = "[]";

    /**
     * Whether this monster is active and available.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether this monster is fictional (true) or a real-world animal (false).
     * Defaults to true for backward compatibility with existing monsters.
     */
    @Column(name = "is_fictional", nullable = false)
    private boolean isFictional = true;

    /**
     * Reference to the AiCharacter that controls this monster's AI behavior.
     * Optional link for AI-driven patrol, attack patterns, and decision-making.
     */
    @Column(name = "ai_character_id")
    private Long aiCharacterId;

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

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = Math.max(1, Math.min(10, threatLevel));
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
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
        this.hp = Math.max(1, hp);
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
        this.str = Math.max(1, Math.min(20, str));
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = Math.max(1, Math.min(20, dex));
    }

    public int getCon() {
        return con;
    }

    public void setCon(int con) {
        this.con = Math.max(1, Math.min(20, con));
    }

    public int getIntel() {
        return intel;
    }

    public void setIntel(int intel) {
        this.intel = Math.max(1, Math.min(20, intel));
    }

    public int getWis() {
        return wis;
    }

    public void setWis(int wis) {
        this.wis = Math.max(1, Math.min(20, wis));
    }

    public int getCha() {
        return cha;
    }

    public void setCha(int cha) {
        this.cha = Math.max(1, Math.min(20, cha));
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecialAbilities() {
        return specialAbilities;
    }

    public void setSpecialAbilities(String specialAbilities) {
        this.specialAbilities = specialAbilities;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFictional() {
        return isFictional;
    }

    public void setFictional(boolean fictional) {
        isFictional = fictional;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "E26Monster{name='" + name + "', TL=" + threatLevel + ", XP=" + xp + "}";
    }
}

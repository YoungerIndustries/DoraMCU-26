package com.yisy.modules.doramcu.traveller.model;

import com.yisy.modules.doramcu.gamecore.AiBehaviorLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a Traveller Monster.
 * <p>
 * Monsters in Traveller range from alien wildlife to void creatures, robots, and mutants.
 * Each monster has a Threat Level (1-10) that determines its danger and XP value.
 * </p>
 * <p>
 * Key mechanics:
 * - Threat Level: 1-10, determines encounter difficulty
 * - XP: Experience points awarded for defeating the monster
 * - Size: Tiny, Small, Medium, Large, Huge, Gargantuan
 * - Type: Alien, Beast, Construct, Humanoid, Mutant, Robot, Void Creature
 * - Alignment: Lawful Good to Chaotic Evil
 * - Stats: STR, DEX, CON, INT, WIS, CHA (1-20)
 * - AC: Armor Class (10-30)
 * - HP: Hit Points
 * - Speed: Movement speed in feet (walk, fly, swim, climb)
 * - Immunities: Damage types the monster is immune to
 * - Resistances: Damage types the monster resists (half damage)
 * - Vulnerabilities: Damage types the monster is vulnerable to (double damage)
 * - Senses: Darkvision, infrared, motion detection, etc.
 * - AI Behavior Level: Determines AI complexity (NONE to FULL)
 * </p>
 */
@Entity
@Table(name = "traveller_monsters", indexes = {
        @Index(name = "idx_traveller_monster_name", columnList = "name", unique = true),
        @Index(name = "idx_traveller_monster_threat", columnList = "threatLevel"),
        @Index(name = "idx_traveller_monster_type", columnList = "type"),
        @Index(name = "idx_traveller_monster_size", columnList = "size"),
        @Index(name = "idx_traveller_monster_xp", columnList = "xp"),
        @Index(name = "idx_traveller_monster_ai_behavior", columnList = "aiBehaviorLevel")
})
public class TravellerMonster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the monster (e.g., "Alien Scout", "Protean Beast", "Void Creature").
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Threat Level (1-10).
     */
    @Column(name = "threatLevel", nullable = false)
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
     * Type: Alien, Beast, Construct, Humanoid, Mutant, Robot, Void Creature.
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * Subtype (e.g., "Insectoid", "Reptilian", "Humanoid").
     */
    @Column(name = "subtype", length = 50)
    private String subtype;

    /**
     * Alignment: Lawful Good to Chaotic Evil.
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
     * Senses (e.g., "Darkvision 60ft", "Infrared Vision", "Motion Detection").
     */
    @Column(nullable = false, length = 200)
    private String senses;

    /**
     * Full description of the monster's lore and appearance.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * AI behavior level for this monster, derived from intelligence score.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_behavior_level", nullable = false, length = 20)
    private AiBehaviorLevel aiBehaviorLevel = AiBehaviorLevel.NONE;

    /**
     * Reference to an AiCharacter entity (only set when aiBehaviorLevel is FULL).
     */
    @Column(name = "ai_character_id")
    private Long aiCharacterId;

    /**
     * Whether this monster is fictional (true) or a real-world animal (false).
     */
    @Column(name = "is_fictional", nullable = false)
    private boolean isFictional = true;

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
        this.ac = Math.max(10, Math.min(30, ac));
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
        this.speed = Math.max(0, speed);
    }

    public int getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(int flySpeed) {
        this.flySpeed = Math.max(0, flySpeed);
    }

    public int getSwimSpeed() {
        return swimSpeed;
    }

    public void setSwimSpeed(int swimSpeed) {
        this.swimSpeed = Math.max(0, swimSpeed);
    }

    public int getClimbSpeed() {
        return climbSpeed;
    }

    public void setClimbSpeed(int climbSpeed) {
        this.climbSpeed = Math.max(0, climbSpeed);
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

    public AiBehaviorLevel getAiBehaviorLevel() {
        return aiBehaviorLevel;
    }

    public void setAiBehaviorLevel(AiBehaviorLevel aiBehaviorLevel) {
        this.aiBehaviorLevel = aiBehaviorLevel;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public boolean isFictional() {
        return isFictional;
    }

    public void setFictional(boolean fictional) {
        isFictional = fictional;
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
        return "TravellerMonster{name='" + name + "', TL=" + threatLevel + ", XP=" + xp + "}";
    }
}

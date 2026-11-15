package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a companion in Earth 2026.
 * <p>
 * Companions are pets, service animals, familiars, mounts, and working animals
 * that belong to characters. They have their own stats, bond scores, training levels,
 * and can have AI-driven behavior through a linked AiCharacter.
 * </p>
 * <p>
 * Key mechanics:
 * - Name & Species: Companion identity (Dog, Cat, Horse, Wolf, etc.)
 * - Companion Type: Pet, Service Animal, Familiar, Mount, Working Animal
 * - Stats: STR, DEX, CON, INT, WIS, CHA for simplified animal stats
 * - Hit Points: Health of the companion
 * - Bond Score: 0-100, relationship strength with owner
 * - Training Level: Untrained, Basic, Advanced, Master
 * - Tricks: JSON array of learned tricks
 * - Abilities: JSON array of special abilities (e.g., "Scent Tracking", "Alert")
 * - Equipment: JSON array of items (collar, harness, magical items)
 * - Health: Healthy, Injured, Sick, Deceased
 * - Location: Follows owner or stays at home
 * - AI Character: Optional link to AiCharacter for AI-driven behavior
 * </p>
 */
@Entity
@Table(name = "e26_companions", indexes = {
        @Index(name = "idx_e26_companion_owner", columnList = "ownerCharacterId"),
        @Index(name = "idx_e26_companion_species", columnList = "speciesId"),
        @Index(name = "idx_e26_companion_type", columnList = "companionType"),
        @Index(name = "idx_e26_companion_ai", columnList = "aiCharacterId")
})
public class Earth2026Companion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Name of the companion (e.g., "Buddy", "Whiskers", "Shadow").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Species identifier (e.g., "DOG", "CAT", "HORSE", "WOLF", "BEAR", "EAGLE").
     */
    @Column(name = "species_id", nullable = false, length = 50)
    private String speciesId;

    /**
     * Breed or variety of the companion (e.g., "Golden Retriever", "Siamese", "Thoroughbred").
     */
    @Column(length = 100)
    private String breed;

    /**
     * Reference to the owner's Earth2026Character.
     */
    @Column(name = "owner_character_id", nullable = false)
    private Long ownerCharacterId;

    /**
     * Reference to the linked AiCharacter for AI-driven behavior.
     */
    @Column(name = "ai_character_id")
    private Long aiCharacterId;

    /**
     * Type of companion: Pet, Service Animal, Familiar, Mount, Working Animal.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "companion_type", nullable = false, length = 20)
    private CompanionType companionType;

    /**
     * Strength stat of the companion.
     */
    @Column(nullable = false)
    private int str = 10;

    /**
     * Dexterity stat of the companion.
     */
    @Column(nullable = false)
    private int dex = 10;

    /**
     * Constitution stat of the companion.
     */
    @Column(nullable = false)
    private int con = 10;

    /**
     * Intelligence stat of the companion.
     */
    @Column(nullable = false)
    private int intel = 10;

    /**
     * Wisdom stat of the companion.
     */
    @Column(nullable = false)
    private int wis = 10;

    /**
     * Charisma stat of the companion.
     */
    @Column(nullable = false)
    private int cha = 10;

    /**
     * Current Hit Points of the companion.
     */
    @Column(name = "hit_points", nullable = false)
    private int hitPoints = 10;

    /**
     * Maximum Hit Points of the companion.
     */
    @Column(name = "max_hit_points", nullable = false)
    private int maxHitPoints = 10;

    /**
     * Bond score with owner (0-100). Higher means stronger relationship.
     */
    @Column(name = "bond_score")
    private int bondScore = 0;

    /**
     * Training level: Untrained, Basic, Advanced, Master.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "training_level", nullable = false, length = 20)
    private TrainingLevel trainingLevel = TrainingLevel.UNTRAINED;

    /**
     * JSON array of learned tricks (e.g., ["Sit", "Stay", "Fetch"]).
     */
    @Column(columnDefinition = "TEXT")
    private String tricks = "[]";

    /**
     * JSON array of special abilities (e.g., ["Scent Tracking", "Alert"]).
     */
    @Column(columnDefinition = "TEXT")
    private String abilities = "[]";

    /**
     * JSON array of equipment items (e.g., ["Collar", "Harness", "Magic Bell"]).
     */
    @Column(columnDefinition = "TEXT")
    private String equipment = "[]";

    /**
     * Current health status: Healthy, Injured, Sick, Deceased.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HealthStatus health = HealthStatus.HEALTHY;

    /**
     * Location: follows owner or stays at home.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LocationType location = LocationType.FOLLOWS_OWNER;

    /**
     * Date when the companion was acquired.
     */
    @Column(name = "acquired_date", nullable = false)
    private LocalDateTime acquiredDate = LocalDateTime.now();

    /**
     * Whether the companion is active (not deceased or abandoned).
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Type of companion.
     */
    public enum CompanionType {
        /** Pet: Dog, Cat, Bird, Fish, Reptile — provides morale bonus, small utility. */
        PET,
        /** Service Animal: Guide dog, hearing dog, medical alert — provides mechanical bonuses. */
        SERVICE_ANIMAL,
        /** Familiar: Magical companion with spell-casting proxy. */
        FAMILIAR,
        /** Mount: Horse, motorcycle sidecar pet — transportation bonus. */
        MOUNT,
        /** Working Animal: Guard dog, farm animal — economic/utility bonus. */
        WORKING_ANIMAL
    }

    /**
     * Training level of the companion.
     */
    public enum TrainingLevel {
        /** No training, wild or untrained behavior. */
        UNTRAINED,
        /** Basic obedience and simple commands. */
        BASIC,
        /** Advanced training with complex commands and tactics. */
        ADVANCED,
        /** Master-level training with autonomous decision-making. */
        MASTER
    }

    /**
     * Health status of the companion.
     */
    public enum HealthStatus {
        /** Companion is healthy and active. */
        HEALTHY,
        /** Companion is injured but recoverable. */
        INJURED,
        /** Companion is sick and needs care. */
        SICK,
        /** Companion has died. */
        DECEASED
    }

    /**
     * Location type of the companion.
     */
    public enum LocationType {
        /** Companion follows the owner everywhere. */
        FOLLOWS_OWNER,
        /** Companion stays at the owner's home. */
        STAYS_AT_HOME
    }

    public Earth2026Companion() {
    }

    public Earth2026Companion(String name, String speciesId, Long ownerCharacterId, CompanionType companionType) {
        this.name = name;
        this.speciesId = speciesId;
        this.ownerCharacterId = ownerCharacterId;
        this.companionType = companionType;
    }

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

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Long getOwnerCharacterId() {
        return ownerCharacterId;
    }

    public void setOwnerCharacterId(Long ownerCharacterId) {
        this.ownerCharacterId = ownerCharacterId;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public CompanionType getCompanionType() {
        return companionType;
    }

    public void setCompanionType(CompanionType companionType) {
        this.companionType = companionType;
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

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = Math.max(0, Math.min(hitPoints, maxHitPoints));
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }

    public int getBondScore() {
        return bondScore;
    }

    public void setBondScore(int bondScore) {
        this.bondScore = Math.max(0, Math.min(100, bondScore));
    }

    public TrainingLevel getTrainingLevel() {
        return trainingLevel;
    }

    public void setTrainingLevel(TrainingLevel trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    public String getTricks() {
        return tricks;
    }

    public void setTricks(String tricks) {
        this.tricks = tricks;
    }

    public String getAbilities() {
        return abilities;
    }

    public void setAbilities(String abilities) {
        this.abilities = abilities;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public HealthStatus getHealth() {
        return health;
    }

    public void setHealth(HealthStatus health) {
        this.health = health;
    }

    public LocationType getLocation() {
        return location;
    }

    public void setLocation(LocationType location) {
        this.location = location;
    }

    public LocalDateTime getAcquiredDate() {
        return acquiredDate;
    }

    public void setAcquiredDate(LocalDateTime acquiredDate) {
        this.acquiredDate = acquiredDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Heals the companion by the specified amount.
     */
    public void heal(int amount) {
        this.hitPoints = Math.min(this.hitPoints + amount, this.maxHitPoints);
        if (this.hitPoints > 0 && this.health == HealthStatus.DECEASED) {
            this.health = HealthStatus.HEALTHY;
        }
    }

    /**
     * Damages the companion by the specified amount.
     */
    public void takeDamage(int amount) {
        this.hitPoints = Math.max(0, this.hitPoints - amount);
        if (this.hitPoints == 0) {
            this.health = HealthStatus.DECEASED;
        } else if (this.hitPoints < this.maxHitPoints / 2) {
            this.health = HealthStatus.INJURED;
        }
    }

    /**
     * Increases the bond score with the owner.
     */
    public void increaseBond(int amount) {
        this.bondScore = Math.min(100, this.bondScore + amount);
    }

    /**
     * Decreases the bond score with the owner.
     */
    public void decreaseBond(int amount) {
        this.bondScore = Math.max(0, this.bondScore - amount);
    }

    @Override
    public String toString() {
        return "Earth2026Companion{id=" + id + ", name='" + name + "', species='" + speciesId +
                "', type=" + companionType + ", bond=" + bondScore +
                ", training=" + trainingLevel + ", health=" + health + "}";
    }
}

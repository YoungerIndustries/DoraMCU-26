package com.yisy.modules.doramcu.earth2026.survival.encounter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity for survival encounter records in Earth2026.
 * <p>
 * Tracks encounters with monsters, including loot drops and outcomes.
 * </p>
 */
@Entity
@Table(name = "survival_encounters", indexes = {
        @Index(name = "idx_survival_encounter_character", columnList = "characterId"),
        @Index(name = "idx_survival_encounter_monster", columnList = "monsterId")
})
public class SurvivalEncounter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Reference to the character.
     */
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    /**
     * Reference to the monster (E26Monster).
     */
    @Column(name = "monster_id")
    private Long monsterId;

    /**
     * Monster name.
     */
    @Column(name = "monster_name", length = 100)
    private String monsterName;

    /**
     * Encounter outcome: WON, LOST, FLED.
     */
    @Column(nullable = false, length = 10)
    private String outcome;

    /**
     * XP gained from the encounter.
     */
    @Column(name = "xp_gained", nullable = false)
    private int xpGained = 0;

    /**
     * Damage taken by the character.
     */
    @Column(name = "damage_taken", nullable = false)
    private int damageTaken = 0;

    /**
     * Loot drops as JSON (resource names and quantities).
     */
    @Column(name = "loot_json", columnDefinition = "JSON")
    private String lootJson;

    /**
     * Location of the encounter.
     */
    @Column(length = 200)
    private String location;

    /**
     * Whether the encounter was at night.
     */
    @Column(name = "is_night")
    private boolean isNight;

    /**
     * When the encounter occurred.
     */
    @Column(name = "encounter_time", nullable = false)
    private LocalDateTime encounterTime;

    public SurvivalEncounter() {
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public Long getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(Long monsterId) {
        this.monsterId = monsterId;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public int getXpGained() {
        return xpGained;
    }

    public void setXpGained(int xpGained) {
        this.xpGained = xpGained;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public void setDamageTaken(int damageTaken) {
        this.damageTaken = damageTaken;
    }

    public String getLootJson() {
        return lootJson;
    }

    public void setLootJson(String lootJson) {
        this.lootJson = lootJson;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isNight() {
        return isNight;
    }

    public void setNight(boolean night) {
        isNight = night;
    }

    public LocalDateTime getEncounterTime() {
        return encounterTime;
    }

    public void setEncounterTime(LocalDateTime encounterTime) {
        this.encounterTime = encounterTime;
    }
}

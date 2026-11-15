package com.yisy.modules.doramcu.traveller.aircombat;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity for air combat missions.
 */
@Entity
@Table(name = "traveller_air_combat_mission")
public class AirCombatMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String characterId;

    /** Mission type: AIR_TO_AIR, AIR_TO_GROUND, INTERDICTION, RECON */
    @Column(nullable = false)
    private String missionType;

    @Column(nullable = false)
    private String title;

    private String description;

    /** Status: ACTIVE, COMPLETED, FAILED */
    @Column(nullable = false)
    private String status;

    /** Enemy count */
    private int enemyCount;

    /** Enemy type */
    private String enemyType;

    /** XP reward */
    private int xpReward;

    /** Credit reward */
    private int creditReward;

    /** Difficulty (1-10) */
    private int difficulty;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public AirCombatMission() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = Objects.requireNonNull(characterId); }

    public String getMissionType() { return missionType; }
    public void setMissionType(String missionType) { this.missionType = Objects.requireNonNull(missionType); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = Objects.requireNonNull(title); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = Objects.requireNonNull(status); }

    public int getEnemyCount() { return enemyCount; }
    public void setEnemyCount(int enemyCount) { this.enemyCount = enemyCount; }

    public String getEnemyType() { return enemyType; }
    public void setEnemyType(String enemyType) { this.enemyType = enemyType; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public int getCreditReward() { return creditReward; }
    public void setCreditReward(int creditReward) { this.creditReward = creditReward; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

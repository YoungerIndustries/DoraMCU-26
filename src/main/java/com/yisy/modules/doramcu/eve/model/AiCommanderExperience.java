package com.yisy.modules.doramcu.eve.model;

import jakarta.persistence.*;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single battle experience record for an AI commander.
 * <p>
 * Each record captures the outcome of a battle or combat encounter, including
 * the game module, battle type, result, experience points earned, and any
 * skill improvements. This allows AI commanders to accumulate experience over
 * time, improving their effectiveness and making them more valuable for hiring.
 * </p>
 * <p>
 * Linked to {@link AiCharacter} via {@code aiCharacterId}, so experience persists
 * across battles and game sessions. Commanders with more experience have higher
 * leadership ratings and better traits.
 * </p>
 */
@Entity
@Table(name = "ai_commander_experiences", indexes = {
        @Index(name = "idx_ai_commander_exp_char", columnList = "aiCharacterId"),
        @Index(name = "idx_ai_commander_exp_game", columnList = "gameModuleName"),
        @Index(name = "idx_ai_commander_exp_date", columnList = "battleDate")
})
@EntityListeners(CdcEntityListener.class)
public class AiCommanderExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The AI character this experience belongs to.
     */
    @Column(name = "ai_character_id", nullable = false)
    private Long aiCharacterId;

    /**
     * Game module that generated this experience (e.g., "THIRD_REICH", "TRAVELLER", "SFB").
     */
    @Column(name = "game_module_name", nullable = false, length = 50)
    private String gameModuleName;

    /**
     * Type of battle or combat (e.g., "Ground Battle", "Space Battle", "Air Battle").
     */
    @Column(name = "battle_type", nullable = false, length = 50)
    private String battleType;

    /**
     * Brief description of the battle or encounter.
     */
    @Column(name = "battle_description", length = 500)
    private String battleDescription;

    /**
     * Location or sector where the battle took place.
     */
    @Column(name = "battle_location", length = 200)
    private String battleLocation;

    /**
     * Outcome of the battle.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 20)
    private Outcome outcome;

    /**
     * Experience points earned from this battle.
     */
    @Column(name = "experience_points", nullable = false)
    private int experiencePoints = 0;

    /**
     * Leadership bonus gained from this battle (can be negative for failures).
     */
    @Column(name = "leadership_bonus")
    private int leadershipBonus = 0;

    /**
     * Skill that improved, if any (e.g., "Armor Tactics", "Naval Combat", "Air Combat").
     */
    @Column(name = "skill_improved", length = 50)
    private String skillImproved;

    /**
     * Number of enemy units destroyed or defeated.
     */
    @Column(name = "enemies_defeated")
    private int enemiesDefeated = 0;

    /**
     * Number of friendly units lost.
     */
    @Column(name = "friendly_losses")
    private int friendlyLosses = 0;

    /**
     * Whether the commander showed exceptional performance.
     */
    @Column(name = "exceptional_performance")
    private boolean exceptionalPerformance = false;

    /**
     * Date and time of the battle.
     */
    @Column(name = "battle_date", nullable = false)
    private LocalDateTime battleDate;

    /**
     * Possible outcomes of a battle.
     */
    public enum Outcome {
        /** Decisive victory with minimal losses. */
        VICTORY,
        /** Pyrrhic victory with heavy losses. */
        PYRRHIC_VICTORY,
        /** Stalemate or draw. */
        STALEMATE,
        /** Defeat but orderly retreat. */
        DEFEAT,
        /** Rout with heavy losses. */
        ROUTE
    }

    public AiCommanderExperience() {
    }

    public AiCommanderExperience(Long aiCharacterId, String gameModuleName, String battleType, Outcome outcome) {
        this.aiCharacterId = aiCharacterId;
        this.gameModuleName = gameModuleName;
        this.battleType = battleType;
        this.outcome = outcome;
        this.battleDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public String getGameModuleName() {
        return gameModuleName;
    }

    public void setGameModuleName(String gameModuleName) {
        this.gameModuleName = gameModuleName;
    }

    public String getBattleType() {
        return battleType;
    }

    public void setBattleType(String battleType) {
        this.battleType = battleType;
    }

    public String getBattleDescription() {
        return battleDescription;
    }

    public void setBattleDescription(String battleDescription) {
        this.battleDescription = battleDescription;
    }

    public String getBattleLocation() {
        return battleLocation;
    }

    public void setBattleLocation(String battleLocation) {
        this.battleLocation = battleLocation;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getLeadershipBonus() {
        return leadershipBonus;
    }

    public void setLeadershipBonus(int leadershipBonus) {
        this.leadershipBonus = leadershipBonus;
    }

    public String getSkillImproved() {
        return skillImproved;
    }

    public void setSkillImproved(String skillImproved) {
        this.skillImproved = skillImproved;
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public void setEnemiesDefeated(int enemiesDefeated) {
        this.enemiesDefeated = enemiesDefeated;
    }

    public int getFriendlyLosses() {
        return friendlyLosses;
    }

    public void setFriendlyLosses(int friendlyLosses) {
        this.friendlyLosses = friendlyLosses;
    }

    public boolean isExceptionalPerformance() {
        return exceptionalPerformance;
    }

    public void setExceptionalPerformance(boolean exceptionalPerformance) {
        this.exceptionalPerformance = exceptionalPerformance;
    }

    public LocalDateTime getBattleDate() {
        return battleDate;
    }

    public void setBattleDate(LocalDateTime battleDate) {
        this.battleDate = battleDate;
    }

    @Override
    public String toString() {
        return "AiCommanderExperience{" +
                "id=" + id +
                ", aiCharacterId=" + aiCharacterId +
                ", game='" + gameModuleName + '\'' +
                ", type='" + battleType + '\'' +
                ", outcome=" + outcome +
                ", xp=" + experiencePoints +
                ", date=" + battleDate +
                '}';
    }
}

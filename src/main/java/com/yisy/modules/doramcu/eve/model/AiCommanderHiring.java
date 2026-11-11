package com.yisy.modules.doramcu.eve.model;

import jakarta.persistence.*;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity representing a hiring relationship between a player and an AI commander.
 * <p>
 * When a player hires an AI commander, this record tracks the relationship including
 * the hire date, salary, contract duration, and performance history. The AI commander
 * remains linked to its original {@link AiCharacter} but is now under the player's
 * control for battles and operations.
 * </p>
 * <p>
 * A hired commander retains all accumulated experience and can continue gaining
 * experience while serving the player. If the contract expires or is terminated,
 * the commander returns to AI control.
 * </p>
 */
@Entity
@Table(name = "ai_commander_hiring", indexes = {
        @Index(name = "idx_ai_cmd_hire_player", columnList = "playerId"),
        @Index(name = "idx_ai_cmd_hire_commander", columnList = "aiCharacterId"),
        @Index(name = "idx_ai_cmd_hire_status", columnList = "status")
})
@EntityListeners(CdcEntityListener.class)
public class AiCommanderHiring {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The AI character being hired.
     */
    @Column(name = "ai_character_id", nullable = false)
    private Long aiCharacterId;

    /**
     * The player who hired the commander.
     */
    @Column(name = "player_id", nullable = false, length = 50)
    private String playerId;

    /**
     * Current status of the hiring contract.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    /**
     * Daily salary for the commander (in game credits).
     */
    @Column(name = "daily_salary", nullable = false)
    private long dailySalary;

    /**
     * Contract duration in days.
     */
    @Column(name = "contract_duration_days")
    private int contractDurationDays;

    /**
     * Date the commander was hired.
     */
    @Column(name = "hire_date", nullable = false)
    private LocalDateTime hireDate;

    /**
     * Date the contract expires (if fixed-term).
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * Total battles fought under this contract.
     */
    @Column(name = "battles_fought")
    private int battlesFought = 0;

    /**
     * Total victories under this contract.
     */
    @Column(name = "victories")
    private int victories = 0;

    /**
     * Average performance rating (1-10).
     */
    @Column(name = "performance_rating")
    private double performanceRating = 5.0;

    /**
     * Notes about the commander's performance.
     */
    @Column(name = "performance_notes", length = 500)
    private String performanceNotes;

    /**
     * Status of the hiring contract.
     */
    public enum Status {
        /** Contract is active; commander serves the player. */
        ACTIVE,
        /** Contract expired; commander returned to AI control. */
        EXPIRED,
        /** Contract terminated by player. */
        TERMINATED,
        /** Contract terminated by system (e.g., commander death). */
        DISCHARGED
    }

    public AiCommanderHiring() {
    }

    public AiCommanderHiring(Long aiCharacterId, String playerId, long dailySalary) {
        this.aiCharacterId = aiCharacterId;
        this.playerId = playerId;
        this.dailySalary = dailySalary;
        this.status = Status.ACTIVE;
        this.hireDate = LocalDateTime.now();
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

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getDailySalary() {
        return dailySalary;
    }

    public void setDailySalary(long dailySalary) {
        this.dailySalary = dailySalary;
    }

    public int getContractDurationDays() {
        return contractDurationDays;
    }

    public void setContractDurationDays(int contractDurationDays) {
        this.contractDurationDays = contractDurationDays;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getBattlesFought() {
        return battlesFought;
    }

    public void setBattlesFought(int battlesFought) {
        this.battlesFought = battlesFought;
    }

    public int getVictories() {
        return victories;
    }

    public void setVictories(int victories) {
        this.victories = victories;
    }

    public double getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(double performanceRating) {
        this.performanceRating = Math.max(1.0, Math.min(10.0, performanceRating));
    }

    public String getPerformanceNotes() {
        return performanceNotes;
    }

    public void setPerformanceNotes(String performanceNotes) {
        this.performanceNotes = performanceNotes;
    }

    /**
     * Checks whether this contract is currently active.
     */
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    /**
     * Checks whether the contract has expired.
     */
    public boolean isExpired() {
        if (status != Status.ACTIVE) return true;
        if (expiryDate == null) return false;
        return LocalDateTime.now().isAfter(expiryDate);
    }

    @Override
    public String toString() {
        return "AiCommanderHiring{" +
                "id=" + id +
                ", aiCharacterId=" + aiCharacterId +
                ", playerId='" + playerId + '\'' +
                ", status=" + status +
                ", salary=" + dailySalary +
                ", battles=" + battlesFought +
                ", victories=" + victories +
                '}';
    }
}

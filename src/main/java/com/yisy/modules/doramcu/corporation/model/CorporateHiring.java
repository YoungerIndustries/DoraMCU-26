package com.yisy.modules.doramcu.corporation.model;

import com.yisy.modules.doramcu.model.misc.CareerLevel;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity tracking AI character hires within the corporation.
 * <p>
 * Links to existing AiCharacter entities and records the position, status,
 * salary, and performance rating for each hire.
 * </p>
 */
@Entity
@Table(name = "corporate_hiring", indexes = {
        @Index(name = "idx_corporate_hiring_corporation", columnList = "corporationId"),
        @Index(name = "idx_corporate_hiring_ai_character", columnList = "aiCharacterId"),
        @Index(name = "idx_corporate_hiring_status", columnList = "status")
})
public class CorporateHiring {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Column(name = "ai_character_id", nullable = false)
    private Long aiCharacterId;

    @Column(name = "player_character_id")
    private Long playerCharacterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CorporatePosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "hire_date", nullable = false)
    private LocalDateTime hireDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "daily_salary", nullable = false)
    private Long dailySalary;

    @Column(name = "performance_rating")
    private Double performanceRating;

    // ---- Career progression fields ----

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", nullable = false, length = 10)
    private CareerLevel careerLevel = CareerLevel.JUNIOR;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "corporate_hiring_skills", joinColumns = @JoinColumn(name = "hiring_id"))
    @MapKeyColumn(name = "skill_name")
    @Column(name = "skill_level")
    private Map<String, Integer> skills = new HashMap<>();

    // ---- Crew assignment ----

    /**
     * Links this hire to a Crew group.
     * Null means the hire is in the general employee pool.
     * Non-null means the hire is assigned to a crew.
     */
    @Column(name = "crew_id")
    private Long crewId;

    /**
     * Hiring status lifecycle.
     */
    public enum Status {
        /** Currently active employment */
        ACTIVE,
        /** Contract expired */
        EXPIRED,
        /** Employment terminated */
        TERMINATED
    }

    public CorporateHiring() {
    }

    public CorporateHiring(Long corporationId, Long aiCharacterId, CorporatePosition position, Long dailySalary) {
        this.corporationId = corporationId;
        this.aiCharacterId = aiCharacterId;
        this.position = position;
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

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public Long getPlayerCharacterId() {
        return playerCharacterId;
    }

    public void setPlayerCharacterId(Long playerCharacterId) {
        this.playerCharacterId = playerCharacterId;
    }

    public CorporatePosition getPosition() {
        return position;
    }

    public void setPosition(CorporatePosition position) {
        this.position = position;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public Long getDailySalary() {
        return dailySalary;
    }

    public void setDailySalary(Long dailySalary) {
        this.dailySalary = dailySalary;
    }

    public Double getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(Double performanceRating) {
        this.performanceRating = performanceRating;
    }

    // ---- Career progression getters and setters ----

    public CareerLevel getCareerLevel() {
        return careerLevel;
    }

    public void setCareerLevel(CareerLevel careerLevel) {
        this.careerLevel = careerLevel;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, Integer> skills) {
        this.skills = skills;
    }

    public Long getCrewId() {
        return crewId;
    }

    public void setCrewId(Long crewId) {
        this.crewId = crewId;
    }

    /**
     * Returns the XP threshold for the next level.
     */
    public int getLevelThreshold() {
        return careerLevel.getNextLevelThreshold();
    }

    /**
     * Checks if the hire is eligible for promotion (XP meets next level threshold).
     */
    public boolean isEligibleForPromotion() {
        return !careerLevel.isMaxLevel() && xpEarned >= careerLevel.getNextLevelThreshold();
    }

    /**
     * Promotes the hire to the next career level.
     * Requires meeting the XP threshold.
     * @return true if promoted, false if already at max or not eligible
     */
    public boolean promote() {
        if (careerLevel.isMaxLevel()) {
            return false;
        }
        CareerLevel next = careerLevel.nextLevel();
        if (next == null || xpEarned < next.getXpThreshold()) {
            return false;
        }
        this.careerLevel = next;
        // Adjust salary based on new level multiplier
        double oldMultiplier = CareerLevel.values()[this.careerLevel.ordinal() - 1].getSalaryMultiplier();
        double newMultiplier = this.careerLevel.getSalaryMultiplier();
        this.dailySalary = Math.round(this.dailySalary / oldMultiplier * newMultiplier);
        return true;
    }

    /**
     * Demotes the hire to the previous career level.
     * @return true if demoted, false if already at minimum
     */
    public boolean demote() {
        if (this.careerLevel == CareerLevel.JUNIOR) {
            return false;
        }
        CareerLevel prev = careerLevel.previousLevel();
        if (prev == null) {
            return false;
        }
        double oldMultiplier = this.careerLevel.getSalaryMultiplier();
        this.careerLevel = prev;
        double newMultiplier = this.careerLevel.getSalaryMultiplier();
        this.dailySalary = Math.round(this.dailySalary / oldMultiplier * newMultiplier);
        return true;
    }

    /**
     * Returns the effective salary with career level multiplier applied.
     */
    public long getEffectiveSalary() {
        return Math.round(this.dailySalary * careerLevel.getSalaryMultiplier());
    }

    /**
     * Gets the skill level for a given skill name, defaulting to 1.
     */
    public int getSkillLevel(String skillName) {
        return skills.getOrDefault(skillName, 1);
    }

    /**
     * Sets the skill level for a given skill name, clamped between 1-10.
     */
    public void setSkillLevel(String skillName, int level) {
        skills.put(skillName, Math.max(1, Math.min(10, level)));
    }

    /**
     * Check if this hiring is currently active.
     */
    public boolean isActive() {
        return status == Status.ACTIVE && (expiryDate == null || LocalDateTime.now().isBefore(expiryDate));
    }

    @Override
    public String toString() {
        return "CorporateHiring{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", aiCharacterId=" + aiCharacterId +
                ", position=" + position +
                ", status=" + status +
                ", salary=" + dailySalary +
                ", careerLevel=" + careerLevel +
                ", xpEarned=" + xpEarned +
                ", skills=" + skills +
                '}';
    }
}

package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity tracking character training tours within the corporation.
 * <p>
 * Both player characters (TravellerCharacter) and AI character employees
 * (AiCharacter via CorporateHiring) can enroll in 1–4 year training tours
 * to advance their skills. The tour can be simulated instantly via AI-driven
 * narrative generation, aging the character and recording the experience
 * in their character book.
 * </p>
 */
@Entity
@Table(name = "character_training_tour", indexes = {
        @Index(name = "idx_training_tour_corporation", columnList = "corporationId"),
        @Index(name = "idx_training_tour_status", columnList = "status"),
        @Index(name = "idx_training_tour_traveller", columnList = "travellerCharacterId"),
        @Index(name = "idx_training_tour_ai_character", columnList = "aiCharacterId")
})
public class CharacterTrainingTour {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_type", nullable = false, length = 20)
    private CharacterType characterType;

    @Column(name = "traveller_character_id")
    private Integer travellerCharacterId;

    @Column(name = "ai_character_id")
    private Long aiCharacterId;

    @Column(name = "hiring_id")
    private Long hiringId;

    @Column(nullable = false, length = 50)
    private String skill;

    @Column(nullable = false, length = 50)
    private String profession;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @Column(name = "turns_elapsed", nullable = false)
    private Integer turnsElapsed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "skill_level_before")
    private Integer skillLevelBefore = 1;

    @Column(name = "skill_level_after")
    private Integer skillLevelAfter;

    @Column(name = "xp_awarded")
    private Integer xpAwarded;

    @Column(name = "credits_cost", nullable = false)
    private Long creditsCost;

    @Column(columnDefinition = "TEXT")
    private String narrative;

    @Column(name = "events_summary", columnDefinition = "TEXT")
    private String eventsSummary;

    @Column(name = "age_at_start")
    private Integer ageAtStart;

    @Column(name = "age_at_end")
    private Integer ageAtEnd;

    /**
     * Type of character enrolled in training.
     */
    public enum CharacterType {
        /** Player character (TravellerCharacter) */
        PLAYER_CHARACTER,
        /** AI character (AiCharacter via CorporateHiring) */
        AI_CHARACTER
    }

    /**
     * Training tour lifecycle status.
     */
    public enum Status {
        /** Enrolled, awaiting simulation or turn progression */
        ENROLLED,
        /** Instant simulation is in progress */
        SIMULATING,
        /** Tour completed successfully */
        COMPLETED,
        /** Tour failed (e.g., character dismissed for cause) */
        FAILED,
        /** Character dismissed from training early */
        DISMISSED
    }

    public CharacterTrainingTour() {
    }

    public CharacterTrainingTour(Long corporationId, CharacterType characterType, String skill,
                                 String profession, int durationYears, long creditsCost) {
        this.corporationId = corporationId;
        this.characterType = characterType;
        this.skill = skill;
        this.profession = profession;
        this.durationYears = durationYears;
        this.creditsCost = creditsCost;
        this.status = Status.ENROLLED;
        this.enrolledAt = LocalDateTime.now();
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

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public Integer getTravellerCharacterId() {
        return travellerCharacterId;
    }

    public void setTravellerCharacterId(Integer travellerCharacterId) {
        this.travellerCharacterId = travellerCharacterId;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public Long getHiringId() {
        return hiringId;
    }

    public void setHiringId(Long hiringId) {
        this.hiringId = hiringId;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public Integer getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(Integer durationYears) {
        this.durationYears = durationYears;
    }

    public Integer getTurnsElapsed() {
        return turnsElapsed;
    }

    public void setTurnsElapsed(Integer turnsElapsed) {
        this.turnsElapsed = turnsElapsed;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getSkillLevelBefore() {
        return skillLevelBefore;
    }

    public void setSkillLevelBefore(Integer skillLevelBefore) {
        this.skillLevelBefore = skillLevelBefore;
    }

    public Integer getSkillLevelAfter() {
        return skillLevelAfter;
    }

    public void setSkillLevelAfter(Integer skillLevelAfter) {
        this.skillLevelAfter = skillLevelAfter;
    }

    public Integer getXpAwarded() {
        return xpAwarded;
    }

    public void setXpAwarded(Integer xpAwarded) {
        this.xpAwarded = xpAwarded;
    }

    public Long getCreditsCost() {
        return creditsCost;
    }

    public void setCreditsCost(Long creditsCost) {
        this.creditsCost = creditsCost;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getEventsSummary() {
        return eventsSummary;
    }

    public void setEventsSummary(String eventsSummary) {
        this.eventsSummary = eventsSummary;
    }

    public Integer getAgeAtStart() {
        return ageAtStart;
    }

    public void setAgeAtStart(Integer ageAtStart) {
        this.ageAtStart = ageAtStart;
    }

    public Integer getAgeAtEnd() {
        return ageAtEnd;
    }

    public void setAgeAtEnd(Integer ageAtEnd) {
        this.ageAtEnd = ageAtEnd;
    }

    @Override
    public String toString() {
        return "CharacterTrainingTour{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", characterType=" + characterType +
                ", skill='" + skill + '\'' +
                ", profession='" + profession + '\'' +
                ", durationYears=" + durationYears +
                ", status=" + status +
                '}';
    }
}

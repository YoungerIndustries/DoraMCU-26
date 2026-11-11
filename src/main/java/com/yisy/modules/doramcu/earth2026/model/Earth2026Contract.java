package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a contract on a planet.
 * <p>
 * Contracts are agreements between characters, companies, or governments
 * that define work, trade, or service obligations. Characters can accept
 * contracts to earn money, XP, and reputation.
 * </p>
 */
@Entity
@Table(name = "earth2026_contracts", indexes = {
        @Index(name = "idx_earth2026_contract_status", columnList = "status"),
        @Index(name = "idx_earth2026_contract_planet", columnList = "planetId"),
        @Index(name = "idx_earth2026_contract_assignee", columnList = "assigneeId")
})
public class Earth2026Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Contract title (e.g., "Security Detail", "Trade Negotiation").
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Contract type (e.g., "Employment", "Freelance", "Government", "Military", "Trade").
     */
    @Column(nullable = false, length = 100)
    private String type;

    /**
     * Description of the contract terms.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Payment amount in USD.
     */
    @Column(name = "payment_usd", nullable = false)
    private Integer paymentUsd;

    /**
     * XP reward for completing this contract.
     */
    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    /**
     * Required minimum character level.
     */
    @Column(name = "min_level", nullable = false)
    private Integer minLevel;

    /**
     * Required minimum STR score.
     */
    @Column(name = "min_str")
    private Integer minStr;

    /**
     * Required minimum INT score.
     */
    @Column(name = "min_int")
    private Integer minInt;

    /**
     * Required minimum CHA score.
     */
    @Column(name = "min_cha")
    private Integer minCha;

    /**
     * Difficulty rating (1-10).
     */
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    /**
     * Duration in turns.
     */
    @Column(name = "duration_turns", nullable = false)
    private Integer durationTurns;

    /**
     * Status: "available", "active", "completed", "failed", "expired".
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Character ID assigned to this contract.
     */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /**
     * Planet this contract is on.
     */
    @Column(name = "planet_id", nullable = false)
    private Long planetId;

    /**
     * Country this contract is in (nullable for planet-wide contracts).
     */
    @Column(name = "country_id")
    private Long countryId;

    /**
     * Location ID where the contract takes place.
     */
    @Column(name = "location_id")
    private Long locationId;

    /**
     * Company or organization offering the contract.
     */
    @Column(name = "offering_org", length = 200)
    private String offeringOrg;

    /**
     * Expiry date for the contract.
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * Whether this contract is currently available for acceptance.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Completion timestamp.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Earth2026Contract() {
    }

    public Earth2026Contract(String title, String type, Integer paymentUsd, Long planetId) {
        this.title = title;
        this.type = type;
        this.paymentUsd = paymentUsd;
        this.planetId = planetId;
        this.xpReward = 50;
        this.minLevel = 1;
        this.difficulty = 3;
        this.durationTurns = 1;
        this.status = "available";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPaymentUsd() {
        return paymentUsd;
    }

    public void setPaymentUsd(Integer paymentUsd) {
        this.paymentUsd = paymentUsd;
    }

    public Integer getXpReward() {
        return xpReward;
    }

    public void setXpReward(Integer xpReward) {
        this.xpReward = xpReward;
    }

    public Integer getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Integer minLevel) {
        this.minLevel = minLevel;
    }

    public Integer getMinStr() {
        return minStr;
    }

    public void setMinStr(Integer minStr) {
        this.minStr = minStr;
    }

    public Integer getMinInt() {
        return minInt;
    }

    public void setMinInt(Integer minInt) {
        this.minInt = minInt;
    }

    public Integer getMinCha() {
        return minCha;
    }

    public void setMinCha(Integer minCha) {
        this.minCha = minCha;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDurationTurns() {
        return durationTurns;
    }

    public void setDurationTurns(Integer durationTurns) {
        this.durationTurns = durationTurns;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getOfferingOrg() {
        return offeringOrg;
    }

    public void setOfferingOrg(String offeringOrg) {
        this.offeringOrg = offeringOrg;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Check if this contract has expired.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if this contract is available for acceptance.
     *
     * @return true if available
     */
    public boolean isAvailable() {
        return "available".equals(status) && !isExpired();
    }

    @Override
    public String toString() {
        return "Earth2026Contract{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", payment=" + paymentUsd +
                ", xp=" + xpReward +
                ", status='" + status + '\'' +
                '}';
    }
}

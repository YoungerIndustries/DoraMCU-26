package com.yisy.modules.doramcu.eve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.yisy.modules.doramcu.neuron.service.CdcEntityListener;

/**
 * JPA entity representing an AI character in the game.
 * An AI character can lead an empire, make decisions, and act as a co-server.
 */
@Entity
@Table(name = "ai_characters")
@EntityListeners(CdcEntityListener.class)
public class AiCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Display name of the AI character, e.g., "Sif Younger".
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Role within the empire, e.g., "Leader", "Advisor", "General".
     */
    @Column(nullable = false, length = 50)
    private String role;

    /**
     * Empire name this AI character belongs to.
     */
    @Column(nullable = false, length = 100)
    private String empireName;

    /**
     * Player ID this AI character is linked to (for account binding).
     */
    @Column(length = 50)
    private String linkedPlayerId;

    /**
     * Whether this AI character is active and making decisions.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether this AI character can promote to co-server if the main server fails.
     */
    @Column(nullable = false)
    private boolean canPromoteToCoServer = false;

    /**
     * Description of the AI character's personality and behavior.
     */
    @Column(length = 500)
    private String description;

    /**
     * Total accumulated experience points from all battles.
     */
    @Column(name = "total_experience", nullable = false)
    private int totalExperience = 0;

    /**
     * Current leadership rating (1-10), improved by experience.
     */
    @Column(name = "leadership_rating")
    private int leadershipRating = 5;

    /**
     * Total number of battles participated in.
     */
    @Column(name = "total_battles")
    private int totalBattles = 0;

    /**
     * Total number of victories.
     */
    @Column(name = "total_victories")
    private int totalVictories = 0;

    /**
     * Whether this AI character can be hired by players.
     */
    @Column(name = "hirable", nullable = false)
    private boolean hirable = true;

    /**
     * Daily salary expected if hired (in game credits).
     */
    @Column(name = "daily_salary")
    private long dailySalary = 100;

    /**
     * Current hiring status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "hiring_status", length = 20)
    private HiringStatus hiringStatus = HiringStatus.AVAILABLE;

    /**
     * Primary skill specialty (e.g., "Armor Tactics", "Naval Combat", "Air Combat").
     */
    @Column(name = "primary_skill", length = 50)
    private String primarySkill;

    /**
     * Whether this AI character is the special admin tester with admin privileges.
     */
    @Column(name = "ai_admin", nullable = false)
    private boolean aiAdmin = false;

    /**
     * Hiring status of the AI character.
     */
    public enum HiringStatus {
        /** Available for hiring. */
        AVAILABLE,
        /** Currently hired by a player. */
        HIRED,
        /** Not available for hiring (e.g., key empire leader). */
        UNAVAILABLE
    }

    public AiCharacter() {
    }

    public AiCharacter(String name, String role, String empireName) {
        this.name = name;
        this.role = role;
        this.empireName = empireName;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }

    public String getLinkedPlayerId() {
        return linkedPlayerId;
    }

    public void setLinkedPlayerId(String linkedPlayerId) {
        this.linkedPlayerId = linkedPlayerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCanPromoteToCoServer() {
        return canPromoteToCoServer;
    }

    public void setCanPromoteToCoServer(boolean canPromoteToCoServer) {
        this.canPromoteToCoServer = canPromoteToCoServer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    public int getLeadershipRating() {
        return leadershipRating;
    }

    public void setLeadershipRating(int leadershipRating) {
        this.leadershipRating = Math.max(1, Math.min(10, leadershipRating));
    }

    public int getTotalBattles() {
        return totalBattles;
    }

    public void setTotalBattles(int totalBattles) {
        this.totalBattles = totalBattles;
    }

    public int getTotalVictories() {
        return totalVictories;
    }

    public void setTotalVictories(int totalVictories) {
        this.totalVictories = totalVictories;
    }

    public boolean isHirable() {
        return hirable;
    }

    public void setHirable(boolean hirable) {
        this.hirable = hirable;
    }

    public long getDailySalary() {
        return dailySalary;
    }

    public void setDailySalary(long dailySalary) {
        this.dailySalary = dailySalary;
    }

    public HiringStatus getHiringStatus() {
        return hiringStatus;
    }

    public void setHiringStatus(HiringStatus hiringStatus) {
        this.hiringStatus = hiringStatus;
    }

    public String getPrimarySkill() {
        return primarySkill;
    }

    public void setPrimarySkill(String primarySkill) {
        this.primarySkill = primarySkill;
    }

    public boolean isAiAdmin() {
        return aiAdmin;
    }

    public void setAiAdmin(boolean aiAdmin) {
        this.aiAdmin = aiAdmin;
    }

    /**
     * Adds experience points and updates leadership rating if threshold reached.
     */
    public void addExperience(int xp) {
        this.totalExperience += xp;
        // Leadership increases every 100 XP, max 10
        int newLeadership = 5 + (totalExperience / 100);
        this.leadershipRating = Math.min(10, newLeadership);
    }

    /**
     * Records a battle victory.
     */
    public void recordVictory() {
        this.totalBattles++;
        this.totalVictories++;
    }

    /**
     * Records a battle defeat.
     */
    public void recordDefeat() {
        this.totalBattles++;
    }

    @Override
    public String toString() {
        return "AiCharacter{id=" + id + ", name='" + name + "', role='" + role +
                "', empire='" + empireName + "', xp=" + totalExperience +
                ", leadership=" + leadershipRating + ", battles=" + totalBattles +
                ", victories=" + totalVictories + ", hiring=" + hiringStatus + "}";
    }
}

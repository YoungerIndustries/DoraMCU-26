package com.yisy.modules.doramcu.zero_zero_seven.model;

import jakarta.persistence.*;

/**
 * Represents a hybrid agent in the 007 game.
 * <p>
 * Agents can operate in two modes: COVERT (spy missions, intelligence gathering)
 * and OVERT (combat deployment). Same entity but different capabilities depending
 * on mission context. Each agent has unique skills: stealth, charisma, combat, technical.
 * </p>
 */
@Entity
@Table(name = "bond_agents")
public class BondAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Code name for covert operations. */
    @Column(name = "code_name", length = 50)
    private String codeName;

    /** Description/background of the agent. */
    @Column(length = 500)
    private String description;

    /** Operating mode: COVERT (spy missions) or OVERT (combat). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AgentMode mode = AgentMode.COVERT;

    /** Stealth skill (1-10). */
    @Column(nullable = false)
    private int stealth = 5;

    /** Charisma skill (1-10). */
    @Column(nullable = false)
    private int charisma = 5;

    /** Combat skill (1-10). */
    @Column(nullable = false)
    private int combat = 5;

    /** Technical skill (1-10). */
    @Column(nullable = false)
    private int technical = 5;

    /** Current status of the agent. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentStatus status = AgentStatus.ACTIVE;

    /** Owner empire ID. */
    @Column(name = "owner_empire_id", nullable = false)
    private Long ownerEmpireId;

    /** Current operation ID (nullable). */
    @Column(name = "current_operation_id")
    private Long currentOperationId;

    /** Number of completed missions. */
    @Column(name = "missions_completed", nullable = false)
    private int missionsCompleted = 0;

    /** Agent experience level. */
    @Column(nullable = false)
    private int experienceLevel = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private BondGameSession gameSession;

    public BondAgent() {
    }

    public BondAgent(String name, String codeName, BondGameSession gameSession) {
        this.name = name;
        this.codeName = codeName;
        this.gameSession = gameSession;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AgentMode getMode() { return mode; }
    public void setMode(AgentMode mode) { this.mode = mode; }

    public int getStealth() { return stealth; }
    public void setStealth(int stealth) { this.stealth = Math.max(1, Math.min(10, stealth)); }

    public int getCharisma() { return charisma; }
    public void setCharisma(int charisma) { this.charisma = Math.max(1, Math.min(10, charisma)); }

    public int getCombat() { return combat; }
    public void setCombat(int combat) { this.combat = Math.max(1, Math.min(10, combat)); }

    public int getTechnical() { return technical; }
    public void setTechnical(int technical) { this.technical = Math.max(1, Math.min(10, technical)); }

    public AgentStatus getStatus() { return status; }
    public void setStatus(AgentStatus status) { this.status = status; }

    public Long getOwnerEmpireId() { return ownerEmpireId; }
    public void setOwnerEmpireId(Long ownerEmpireId) { this.ownerEmpireId = ownerEmpireId; }

    public Long getCurrentOperationId() { return currentOperationId; }
    public void setCurrentOperationId(Long currentOperationId) { this.currentOperationId = currentOperationId; }

    public int getMissionsCompleted() { return missionsCompleted; }
    public void setMissionsCompleted(int missionsCompleted) { this.missionsCompleted = missionsCompleted; }

    public int getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(int experienceLevel) { this.experienceLevel = experienceLevel; }

    public BondGameSession getGameSession() { return gameSession; }
    public void setGameSession(BondGameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Calculate the agent's overall skill score.
     */
    public int getTotalSkills() {
        return stealth + charisma + combat + technical;
    }

    /**
     * Check if the agent is available for assignment.
     */
    public boolean isAvailable() {
        return status == AgentStatus.ACTIVE && currentOperationId == null;
    }

    public enum AgentMode {
        COVERT,
        OVERT
    }

    public enum AgentStatus {
        ACTIVE,
        ON_MISSION,
        CAPTURED,
        RETIRED,
        DECEASED
    }
}

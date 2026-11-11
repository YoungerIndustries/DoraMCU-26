package com.yisy.modules.doramcu.fourx;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for 4X game sessions.
 * <p>
 * A session represents an active 4X game with multiple factions,
 * tracking the current turn, game state, and unlock progress.
 * </p>
 */
@Entity
@Table(name = "fourx_sessions")
public class FourxSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String gameType;

    @Column(length = 50)
    private String difficultyLevel;

    @Column(nullable = false)
    private int currentTurn = 0;

    @Column(nullable = false, length = 20)
    private String status = "CREATED";

    /** The player who created the session. */
    @Column(nullable = false)
    private Long createdByPlayerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    /** Whether exploration is unlocked. */
    @Column(nullable = false)
    private boolean explorationUnlocked = true;

    /** Whether expansion is unlocked. */
    @Column(nullable = false)
    private boolean expansionUnlocked = false;

    /** Whether exploitation is unlocked. */
    @Column(nullable = false)
    private boolean exploitationUnlocked = false;

    /** Whether extermination is unlocked. */
    @Column(nullable = false)
    private boolean exterminationUnlocked = false;

    /** Whether diplomacy is unlocked. */
    @Column(nullable = false)
    private boolean diplomacyUnlocked = false;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FourxFactionEntity> factions = new ArrayList<>();

    public FourxSessionEntity() {
    }

    public FourxSessionEntity(String sessionId, String name, Long createdByPlayerId) {
        this.sessionId = sessionId;
        this.name = name;
        this.createdByPlayerId = createdByPlayerId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedByPlayerId() { return createdByPlayerId; }
    public void setCreatedByPlayerId(Long createdByPlayerId) { this.createdByPlayerId = createdByPlayerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public boolean isExplorationUnlocked() { return explorationUnlocked; }
    public void setExplorationUnlocked(boolean explorationUnlocked) { this.explorationUnlocked = explorationUnlocked; }

    public boolean isExpansionUnlocked() { return expansionUnlocked; }
    public void setExpansionUnlocked(boolean expansionUnlocked) { this.expansionUnlocked = expansionUnlocked; }

    public boolean isExploitationUnlocked() { return exploitationUnlocked; }
    public void setExploitationUnlocked(boolean exploitationUnlocked) { this.exploitationUnlocked = exploitationUnlocked; }

    public boolean isExterminationUnlocked() { return exterminationUnlocked; }
    public void setExterminationUnlocked(boolean exterminationUnlocked) { this.exterminationUnlocked = exterminationUnlocked; }

    public boolean isDiplomacyUnlocked() { return diplomacyUnlocked; }
    public void setDiplomacyUnlocked(boolean diplomacyUnlocked) { this.diplomacyUnlocked = diplomacyUnlocked; }

    public List<FourxFactionEntity> getFactions() { return factions; }
    public void setFactions(List<FourxFactionEntity> factions) { this.factions = factions; }

    /**
     * Get the unlock state as a DTO.
     */
    public FourxUnlockState getUnlockState() {
        FourxUnlockState state = new FourxUnlockState();
        state.setExplorationUnlocked(explorationUnlocked);
        state.setExpansionUnlocked(expansionUnlocked);
        state.setExploitationUnlocked(exploitationUnlocked);
        state.setExterminationUnlocked(exterminationUnlocked);
        state.setDiplomacyUnlocked(diplomacyUnlocked);
        return state;
    }

    /**
     * Advance to the next turn.
     */
    public void advanceTurn() {
        this.currentTurn++;
    }

    /**
     * Start the game session.
     */
    public void start() {
        this.status = "ACTIVE";
        this.startedAt = LocalDateTime.now();
    }

    /**
     * End the game session.
     */
    public void end() {
        this.status = "ENDED";
        this.endedAt = LocalDateTime.now();
    }
}

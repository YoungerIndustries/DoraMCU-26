package com.yisy.modules.doramcu.model.session;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity for persisting game session state to the database.
 * <p>
 * Stores serialized game state so sessions survive server restarts.
 * The {@code game_state} column contains a JSON representation of the
 * in-memory {@code hex.Game} object.
 * </p>
 */
@Entity
@Table(name = "game_sessions_persisted",
        indexes = {
                @Index(name = "idx_game_session_last_activity", columnList = "lastActivityAt")
        })
public class PersistedGameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;

    @Column(name = "session_name", nullable = false, length = 255)
    private String sessionName;

    @Column(name = "game_state", columnDefinition = "LONGTEXT")
    private String gameState;

    @Column(name = "player_ids", length = 1000)
    private String playerIds;

    /**
     * The galaxy name this session is associated with (for multi-galaxy portal travel).
     */
    @Column(name = "galaxy_name", length = 255)
    private String galaxyName;

    /**
     * The game type for this session (e.g., STAR_WARS, TRAVELLER).
     */
    @Column(name = "game_type", length = 50)
    private String gameType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    public PersistedGameSession() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(String playerIds) {
        this.playerIds = playerIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}

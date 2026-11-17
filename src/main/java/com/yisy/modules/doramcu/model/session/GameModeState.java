package com.yisy.modules.doramcu.model.session;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for persisting game mode module state across server restarts.
 * <p>
 * Each row stores the JSON-serialized state of a single game mode module
 * for a specific galaxy. This allows modules to restore their in-memory
 * state after a server restart instead of reinitializing from scratch.
 * </p>
 */
@Entity
@Table(name = "game_mode_state", indexes = {
        @Index(name = "uk_galaxy_mode", columnList = "galaxy_name, game_mode", unique = true)
})
public class GameModeState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "galaxy_name", nullable = false, length = 100)
    private String galaxyName;

    @Column(name = "game_mode", nullable = false, length = 50)
    private String gameMode;

    @Column(name = "state_json", columnDefinition = "TEXT")
    private String stateJson;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public GameModeState() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getStateJson() {
        return stateJson;
    }

    public void setStateJson(String stateJson) {
        this.stateJson = stateJson;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

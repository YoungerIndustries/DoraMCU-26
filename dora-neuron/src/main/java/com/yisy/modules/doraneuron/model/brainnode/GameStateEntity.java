package com.yisy.modules.doraneuron.model.brainnode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for persisting game state on the Brain Node.
 */
@Entity
@Table(name = "brainnode_game_state")
public class GameStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false, unique = true)
    private String gameId;

    @Column(name = "game_state_json", columnDefinition = "TEXT")
    private String gameStateJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "synced_with_pi")
    private boolean syncedWithPi;

    public GameStateEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameStateJson() {
        return gameStateJson;
    }

    public void setGameStateJson(String gameStateJson) {
        this.gameStateJson = gameStateJson;
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

    public boolean isSyncedWithPi() {
        return syncedWithPi;
    }

    public void setSyncedWithPi(boolean syncedWithPi) {
        this.syncedWithPi = syncedWithPi;
    }
}

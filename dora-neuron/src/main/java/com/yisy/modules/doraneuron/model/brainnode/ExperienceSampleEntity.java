package com.yisy.modules.doraneuron.model.brainnode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for storing experience samples on the Brain Node.
 */
@Entity
@Table(name = "brainnode_experience_sample")
public class ExperienceSampleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "input_json", columnDefinition = "TEXT")
    private String inputJson;

    @Column(name = "output_json", columnDefinition = "TEXT")
    private String outputJson;

    @Column(name = "reward")
    private double reward;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "synced_with_pi")
    private boolean syncedWithPi;

    public ExperienceSampleEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getInputJson() {
        return inputJson;
    }

    public void setInputJson(String inputJson) {
        this.inputJson = inputJson;
    }

    public String getOutputJson() {
        return outputJson;
    }

    public void setOutputJson(String outputJson) {
        this.outputJson = outputJson;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSyncedWithPi() {
        return syncedWithPi;
    }

    public void setSyncedWithPi(boolean syncedWithPi) {
        this.syncedWithPi = syncedWithPi;
    }
}

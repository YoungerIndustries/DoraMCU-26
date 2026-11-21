package com.yisy.modules.doramcu.fullthrust.entity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for a player.
 */
@Entity
@Table(name = "fullthrust_players")
public class PlayerEntity {

    @Id
    @Column(length = 36)
    private String id;

    private String name;

    private String faction;

    private int credits = 1000;

    private boolean isAI = false;

    private String aiDifficulty = "medium";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    @ElementCollection
    @CollectionTable(name = "player_ships", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "ship_id", length = 36)
    private List<String> shipIds = new ArrayList<>();

    public PlayerEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public boolean isAI() { return isAI; }
    public void setAI(boolean isAI) { this.isAI = isAI; }
    public String getAiDifficulty() { return aiDifficulty; }
    public void setAiDifficulty(String aiDifficulty) { this.aiDifficulty = aiDifficulty; }
    public GameSessionEntity getGameSession() { return gameSession; }
    public void setGameSession(GameSessionEntity gameSession) { this.gameSession = gameSession; }
    public List<String> getShipIds() { return shipIds; }
    public void setShipIds(List<String> shipIds) { this.shipIds = shipIds; }
}

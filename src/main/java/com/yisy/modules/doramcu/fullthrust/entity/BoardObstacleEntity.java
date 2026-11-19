package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

/**
 * JPA entity for board obstacles (asteroids, etc.).
 */
@Entity
@Table(name = "board_obstacles")
public class BoardObstacleEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int positionQ;

    private int positionR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    public BoardObstacleEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPositionQ() { return positionQ; }
    public void setPositionQ(int positionQ) { this.positionQ = positionQ; }
    public int getPositionR() { return positionR; }
    public void setPositionR(int positionR) { this.positionR = positionR; }
    public GameSessionEntity getGameSession() { return gameSession; }
    public void setGameSession(GameSessionEntity gameSession) { this.gameSession = gameSession; }
}

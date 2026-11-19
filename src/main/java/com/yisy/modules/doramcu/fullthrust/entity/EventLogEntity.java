package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

/**
 * JPA entity for event log entries.
 */
@Entity
@Table(name = "event_log")
public class EventLogEntity {

    @Id
    @GeneratedValue
    private Long id;

    private int turnNumber;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    public EventLogEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public GameSessionEntity getGameSession() { return gameSession; }
    public void setGameSession(GameSessionEntity gameSession) { this.gameSession = gameSession; }
}

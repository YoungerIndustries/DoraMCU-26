package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

/**
 * JPA entity for a missile in flight.
 */
@Entity
@Table(name = "missiles")
public class MissileEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 36)
    private String ownerId;

    private String ownerName;

    private int positionQ;

    private int positionR;

    private int heading;

    private int speed;

    private int damage;

    private int turnsRemaining;

    private boolean hit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    public MissileEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public int getPositionQ() { return positionQ; }
    public void setPositionQ(int positionQ) { this.positionQ = positionQ; }
    public int getPositionR() { return positionR; }
    public void setPositionR(int positionR) { this.positionR = positionR; }
    public int getHeading() { return heading; }
    public void setHeading(int heading) { this.heading = heading; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getTurnsRemaining() { return turnsRemaining; }
    public void setTurnsRemaining(int turnsRemaining) { this.turnsRemaining = turnsRemaining; }
    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }
    public GameSessionEntity getGameSession() { return gameSession; }
    public void setGameSession(GameSessionEntity gameSession) { this.gameSession = gameSession; }
}

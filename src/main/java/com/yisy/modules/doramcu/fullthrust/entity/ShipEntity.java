package com.yisy.modules.doramcu.fullthrust.entity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA entity for a ship instance.
 */
@Entity
@Table(name = "ships")
public class ShipEntity {

    @Id
    @Column(length = 36)
    private String id;

    private String name;

    @Column(length = 36)
    private String shipClassId;

    @Column(length = 36)
    private String playerId;

    private int positionQ;

    private int positionR;

    private int heading;

    private int speed;

    private int currentHull;

    private int maxHull;

    private int currentShield;

    private int maxShield;

    private int currentFuel;

    private int maxFuel;

    private int armorValue;

    private boolean destroyed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL)
    private List<WeaponEntity> weapons = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ship_damaged_systems", joinColumns = @JoinColumn(name = "ship_id"))
    @Column(name = "system_name")
    private Set<String> damagedSystems = new HashSet<>();

    public ShipEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShipClassId() { return shipClassId; }
    public void setShipClassId(String shipClassId) { this.shipClassId = shipClassId; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public int getPositionQ() { return positionQ; }
    public void setPositionQ(int positionQ) { this.positionQ = positionQ; }
    public int getPositionR() { return positionR; }
    public void setPositionR(int positionR) { this.positionR = positionR; }
    public int getHeading() { return heading; }
    public void setHeading(int heading) { this.heading = heading; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getCurrentHull() { return currentHull; }
    public void setCurrentHull(int currentHull) { this.currentHull = currentHull; }
    public int getMaxHull() { return maxHull; }
    public void setMaxHull(int maxHull) { this.maxHull = maxHull; }
    public int getCurrentShield() { return currentShield; }
    public void setCurrentShield(int currentShield) { this.currentShield = currentShield; }
    public int getMaxShield() { return maxShield; }
    public void setMaxShield(int maxShield) { this.maxShield = maxShield; }
    public int getCurrentFuel() { return currentFuel; }
    public void setCurrentFuel(int currentFuel) { this.currentFuel = currentFuel; }
    public int getMaxFuel() { return maxFuel; }
    public void setMaxFuel(int maxFuel) { this.maxFuel = maxFuel; }
    public int getArmorValue() { return armorValue; }
    public void setArmorValue(int armorValue) { this.armorValue = armorValue; }
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public GameSessionEntity getGameSession() { return gameSession; }
    public void setGameSession(GameSessionEntity gameSession) { this.gameSession = gameSession; }
    public List<WeaponEntity> getWeapons() { return weapons; }
    public void setWeapons(List<WeaponEntity> weapons) { this.weapons = weapons; }
    public Set<String> getDamagedSystems() { return damagedSystems; }
    public void setDamagedSystems(Set<String> damagedSystems) { this.damagedSystems = damagedSystems; }
}

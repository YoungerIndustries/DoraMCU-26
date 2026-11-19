package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for a ship class definition.
 */
@Entity
@Table(name = "ship_classes")
public class ShipClassEntity {

    @Id
    @Column(length = 50)
    private String id;

    private String name;

    private int hullPoints;

    private int armorValue;

    private int maxSpeed;

    private int acceleration;

    private int deceleration;

    private int maxHeadingChange;

    private int shieldPoints;

    private int shieldRegeneration;

    private int fuelCapacity;

    private int crew;

    private int cost;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionEntity gameSession;

    @OneToMany(mappedBy = "shipClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeaponSlotEntity> weaponSlots = new ArrayList<>();

    public ShipClassEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHullPoints() { return hullPoints; }
    public void setHullPoints(int hullPoints) { this.hullPoints = hullPoints; }
    public int getArmorValue() { return armorValue; }
    public void setArmorValue(int armorValue) { this.armorValue = armorValue; }
    public int getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }
    public int getAcceleration() { return acceleration; }
    public void setAcceleration(int acceleration) { this.acceleration = acceleration; }
    public int getDeceleration() { return deceleration; }
    public void setDeceleration(int deceleration) { this.deceleration = deceleration; }
    public int getMaxHeadingChange() { return maxHeadingChange; }
    public void setMaxHeadingChange(int maxHeadingChange) { this.maxHeadingChange = maxHeadingChange; }
    public int getShieldPoints() { return shieldPoints; }
    public void setShieldPoints(int shieldPoints) { this.shieldPoints = shieldPoints; }
    public int getShieldRegeneration() { return shieldRegeneration; }
    public void setShieldRegeneration(int shieldRegeneration) { this.shieldRegeneration = shieldRegeneration; }
    public int getFuelCapacity() { return fuelCapacity; }
    public void setFuelCapacity(int fuelCapacity) { this.fuelCapacity = fuelCapacity; }
    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public GameSessionEntity getGameSession() { return gameSession; }
    public void setGameSession(GameSessionEntity gameSession) { this.gameSession = gameSession; }
    public List<WeaponSlotEntity> getWeaponSlots() { return weaponSlots; }
    public void setWeaponSlots(List<WeaponSlotEntity> weaponSlots) { this.weaponSlots = weaponSlots; }
}

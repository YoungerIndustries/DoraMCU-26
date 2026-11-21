package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

/**
 * JPA entity for a weapon on a ship.
 */
@Entity
@Table(name = "weapons")
public class WeaponEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int arc;

    private String type;

    private int damage;

    @Column(name = "`range`")
    private int range;

    private int reloadTime;

    private int currentReload;

    private boolean disabled;

    @Column(name = "ship_id", length = 36)
    private String shipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false, insertable = false, updatable = false)
    private ShipEntity ship;

    public WeaponEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getArc() { return arc; }
    public void setArc(int arc) { this.arc = arc; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    public int getReloadTime() { return reloadTime; }
    public void setReloadTime(int reloadTime) { this.reloadTime = reloadTime; }
    public int getCurrentReload() { return currentReload; }
    public void setCurrentReload(int currentReload) { this.currentReload = currentReload; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public String getShipId() { return shipId; }
    public void setShipId(String shipId) { this.shipId = shipId; }
    public ShipEntity getShip() { return ship; }
    public void setShip(ShipEntity ship) { this.ship = ship; }
}

package com.yisy.modules.doramcu.fullthrust.entity;

import jakarta.persistence.*;

/**
 * JPA entity for a weapon slot on a ship class.
 */
@Entity
@Table(name = "weapon_slots")
public class WeaponSlotEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int arc;

    private String weaponType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_class_id", nullable = false)
    private ShipClassEntity shipClass;

    public WeaponSlotEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getArc() { return arc; }
    public void setArc(int arc) { this.arc = arc; }
    public String getWeaponType() { return weaponType; }
    public void setWeaponType(String weaponType) { this.weaponType = weaponType; }
    public ShipClassEntity getShipClass() { return shipClass; }
    public void setShipClass(ShipClassEntity shipClass) { this.shipClass = shipClass; }
}

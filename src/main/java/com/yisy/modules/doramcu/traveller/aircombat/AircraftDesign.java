package com.yisy.modules.doramcu.traveller.aircombat;

import jakarta.persistence.*;

import java.util.*;

/**
 * JPA entity for aircraft designs in Traveller air combat.
 */
@Entity
@Table(name = "traveller_aircraft_design")
public class AircraftDesign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Aircraft type: FIGHTER, BOMBER, TRANSPORT, RECON */
    @Column(nullable = false)
    private String type;

    /** Tonnage */
    @Column(nullable = false)
    private int tonnage;

    /** Thrust (G) */
    @Column(nullable = false)
    private int thrust;

    /** Armor class */
    @Column(nullable = false)
    private int armor;

    /** Hit points */
    @Column(nullable = false)
    private int hitPoints;

    /** Speed (km/s) */
    @Column(nullable = false)
    private int speed;

    /** Weapon loadout description */
    private String weapons;

    /** Fuel capacity (hours) */
    private int fuelCapacity;

    /** Crew required */
    private int crew;

    /** Tech level */
    private int techLevel;

    /** Construction cost */
    private long cost;

    public AircraftDesign() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getTonnage() { return tonnage; }
    public void setTonnage(int tonnage) { this.tonnage = tonnage; }

    public int getThrust() { return thrust; }
    public void setThrust(int thrust) { this.thrust = thrust; }

    public int getArmor() { return armor; }
    public void setArmor(int armor) { this.armor = armor; }

    public int getHitPoints() { return hitPoints; }
    public void setHitPoints(int hitPoints) { this.hitPoints = hitPoints; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public String getWeapons() { return weapons; }
    public void setWeapons(String weapons) { this.weapons = weapons; }

    public int getFuelCapacity() { return fuelCapacity; }
    public void setFuelCapacity(int fuelCapacity) { this.fuelCapacity = fuelCapacity; }

    public int getCrew() { return crew; }
    public void setCrew(int crew) { this.crew = crew; }

    public int getTechLevel() { return techLevel; }
    public void setTechLevel(int techLevel) { this.techLevel = techLevel; }

    public long getCost() { return cost; }
    public void setCost(long cost) { this.cost = cost; }
}

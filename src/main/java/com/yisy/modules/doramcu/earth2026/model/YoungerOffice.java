package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a Younger Industries branch office.
 * <p>
 * Each office is located at a capital city landing point and provides in-game
 * services such as transportation booking, intergalactic travel, cargo services,
 * and communications. Offices serve as the player's access point for Younger
 * Industries services on every planet.
 * </p>
 */
@Entity
@Table(name = "younger_offices", indexes = {
        @Index(name = "idx_younger_office_location", columnList = "location_id"),
        @Index(name = "idx_younger_office_planet", columnList = "planet_id")
})
public class YoungerOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Office name (e.g., "Younger Industries - Washington D.C.").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The location this office is situated at.
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * The planet this office operates on.
     */
    @Column(name = "planet_id", nullable = false)
    private Long planetId;

    /**
     * City where the office is located.
     */
    @Column(nullable = false, length = 100)
    private String city;

    /**
     * Country where the office is located.
     */
    @Column(nullable = false, length = 100)
    private String country;

    /**
     * Comma-separated list of services offered (e.g., "transport,cargo,communications").
     */
    @Column(nullable = false, length = 200)
    private String services;

    /**
     * Description of the office and its services.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * AI character ID managing this office.
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * Whether the office is currently operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    public YoungerOffice() {
    }

    public YoungerOffice(String name, Long locationId, Long planetId, String city, String country, String services) {
        this.name = name;
        this.locationId = locationId;
        this.planetId = planetId;
        this.city = city;
        this.country = country;
        this.services = services;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "YoungerOffice{id=" + id + ", name='" + name + "', city='" + city +
                "', country='" + country + "', services='" + services + "'}";
    }
}

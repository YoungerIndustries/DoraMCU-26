package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a location on Earth in the Earth 2026 game.
 * <p>
 * Locations include cities, landmarks, and secret locations where characters
 * can interact, find scenarios, and transition to other game galaxies.
 * </p>
 */
@Entity
@Table(name = "earth2026_locations", indexes = {
        @Index(name = "idx_earth2026_location_name", columnList = "name"),
        @Index(name = "idx_earth2026_location_city", columnList = "city")
})
public class Earth2026Location {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Location name (e.g., "San Diego Convention Center", "Area 51").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * City where the location is situated.
     */
    @Column(nullable = false, length = 100)
    private String city;

    /**
     * State or province.
     */
    @Column(length = 100)
    private String state;

    /**
     * Country.
     */
    @Column(nullable = false, length = 100)
    private String country;

    /**
     * Location type (e.g., "City", "Landmark", "Secret Base", "Portal", "Shop").
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * Description of the location.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether this location has a portal/door to another galaxy.
     */
    @Column(name = "has_portal", nullable = false)
    private boolean hasPortal = false;

    /**
     * The target galaxy for the portal, if hasPortal is true.
     */
    @Column(name = "portal_galaxy", length = 100)
    private String portalGalaxy;

    /**
     * Whether the location is currently active and accessible.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether this location is a secret location (not visible on default map).
     */
    @Column(name = "is_secret", nullable = false)
    private boolean isSecret = false;

    /**
     * Latitude coordinate.
     */
    @Column
    private Double latitude;

    /**
     * Longitude coordinate.
     */
    @Column
    private Double longitude;

    public Earth2026Location() {
    }

    public Earth2026Location(String name, String city, String country, String type) {
        this.name = name;
        this.city = city;
        this.country = country;
        this.type = type;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasPortal() {
        return hasPortal;
    }

    public void setHasPortal(boolean hasPortal) {
        this.hasPortal = hasPortal;
    }

    public String getPortalGalaxy() {
        return portalGalaxy;
    }

    public void setPortalGalaxy(String portalGalaxy) {
        this.portalGalaxy = portalGalaxy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Earth2026Location{id=" + id + ", name='" + name + "', city='" + city +
                "', type='" + type + "', portal=" + hasPortal + "}";
    }
}

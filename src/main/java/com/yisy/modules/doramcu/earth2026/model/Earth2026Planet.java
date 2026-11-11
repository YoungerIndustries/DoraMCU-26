package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a complete planet object for the Earth 2026 MMOG.
 * <p>
 * This is the master planet object that other games request through PlanetService.
 * When a game asks for a planet, it gets back a fully populated Earth2026Planet
 * with all countries, regions, economy, jobs, and AI-controlled leaders.
 * </p>
 * <p>
 * The planet is automatically populated by AI characters that handle all planet
 * functions: government, economy, military, trade, and diplomacy. Each country
 * has an AI leader, each region has an AI governor, and the planet has an AI
 * overseer that coordinates inter-country relations.
 * </p>
 * <p>
 * Hierarchy: Planet → Countries → Regions → Locations → Structures
 * </p>
 */
@Entity
@Table(name = "earth2026_planets", indexes = {
        @Index(name = "idx_earth2026_planet_name", columnList = "name", unique = true)
})
public class Earth2026Planet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Planet name (e.g., "Earth", "Terra", "New Earth").
     */
    @Column(nullable = false, length = 200, unique = true)
    private String name;

    /**
     * Planet type (e.g., "Terrestrial", "Gas Giant", "Ocean World").
     */
    @Column(name = "planet_type", nullable = false, length = 50)
    private String planetType;

    /**
     * Base tech level of the planet.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "base_tech_level", nullable = false)
    private TechLevel baseTechLevel;

    /**
     * Time period template for this planet.
     */
    @Column(name = "time_period", nullable = false, length = 50)
    private String timePeriod;

    /**
     * Total population in billions.
     */
    @Column(name = "total_population_billions", nullable = false)
    private Double totalPopulationBillions;

    /**
     * Number of countries on this planet.
     */
    @Column(name = "country_count", nullable = false)
    private Integer countryCount;

    /**
     * Number of regions on this planet.
     */
    @Column(name = "region_count", nullable = false)
    private Integer regionCount;

    /**
     * Number of locations on this planet.
     */
    @Column(name = "location_count", nullable = false)
    private Integer locationCount;

    /**
     * AI Character ID that oversees the entire planet.
     */
    @Column(name = "ai_overseer_id")
    private Long aiOverseerId;

    /**
     * Overseer name (display name of the planet AI controller).
     */
    @Column(name = "overseer_name", length = 200)
    private String overseerName;

    /**
     * Whether this planet is currently active and available for games.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether the planet is fully initialized with all data.
     */
    @Column(name = "is_initialized", nullable = false)
    private boolean isInitialized = false;

    /**
     * Description of the planet.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Average temperature in Celsius.
     */
    @Column(name = "avg_temperature_c")
    private Double avgTemperatureC;

    /**
     * Gravity in G (1.0 = Earth standard).
     */
    @Column(nullable = false)
    private Double gravity;

    /**
     * Atmosphere type (e.g., "Nitrogen-Oxygen", "Carbon Dioxide").
     */
    @Column(length = 100)
    private String atmosphere;

    /**
     * Radius in kilometers.
     */
    @Column(name = "radius_km")
    private Double radiusKm;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Earth2026Planet() {
    }

    public Earth2026Planet(String name, String timePeriod) {
        this.name = name;
        this.planetType = "Terrestrial";
        this.baseTechLevel = TechLevel.TL5;
        this.timePeriod = timePeriod;
        this.totalPopulationBillions = 8.0;
        this.countryCount = 0;
        this.regionCount = 0;
        this.locationCount = 0;
        this.gravity = 1.0;
        this.avgTemperatureC = 15.0;
        this.atmosphere = "Nitrogen-Oxygen";
        this.radiusKm = 6371.0;
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

    public String getPlanetType() {
        return planetType;
    }

    public void setPlanetType(String planetType) {
        this.planetType = planetType;
    }

    public TechLevel getBaseTechLevel() {
        return baseTechLevel;
    }

    public void setBaseTechLevel(TechLevel baseTechLevel) {
        this.baseTechLevel = baseTechLevel;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Double getTotalPopulationBillions() {
        return totalPopulationBillions;
    }

    public void setTotalPopulationBillions(Double totalPopulationBillions) {
        this.totalPopulationBillions = totalPopulationBillions;
    }

    public Integer getCountryCount() {
        return countryCount;
    }

    public void setCountryCount(Integer countryCount) {
        this.countryCount = countryCount;
    }

    public Integer getRegionCount() {
        return regionCount;
    }

    public void setRegionCount(Integer regionCount) {
        this.regionCount = regionCount;
    }

    public Integer getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(Integer locationCount) {
        this.locationCount = locationCount;
    }

    public Long getAiOverseerId() {
        return aiOverseerId;
    }

    public void setAiOverseerId(Long aiOverseerId) {
        this.aiOverseerId = aiOverseerId;
    }

    public String getOverseerName() {
        return overseerName;
    }

    public void setOverseerName(String overseerName) {
        this.overseerName = overseerName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAvgTemperatureC() {
        return avgTemperatureC;
    }

    public void setAvgTemperatureC(Double avgTemperatureC) {
        this.avgTemperatureC = avgTemperatureC;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public String getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(String atmosphere) {
        this.atmosphere = atmosphere;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if this planet is ready for game use.
     * A planet is ready when it's initialized, active, and has at least one country.
     *
     * @return true if ready
     */
    public boolean isReadyForGame() {
        return isInitialized && active && countryCount > 0;
    }

    @Override
    public String toString() {
        return "Earth2026Planet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + planetType + '\'' +
                ", techLevel=" + baseTechLevel +
                ", period='" + timePeriod + '\'' +
                ", countries=" + countryCount +
                ", regions=" + regionCount +
                ", locations=" + locationCount +
                ", ready=" + isReadyForGame() +
                '}';
    }
}

package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a galaxy in the 4X space game.
 * <p>
 * A galaxy contains 100 2D sectors arranged in a 10x10 grid.
 * Each sector is a 3D cube of 100x100x100 dimensions containing solar systems.
 * </p>
 */
@Entity
@Table(name = "galaxies")
public class Galaxy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalSectors = 100;

    @Column(nullable = false)
    private double hyperlaneDensity;

    // Galaxy generation parameters (added by V17_1 migration)
    private Long seed;
    private Integer systemCount;
    private Integer planetsPerSystem;
    private Double moonChance;
    private Double asteroidBeltChance;
    private Double gasGiantChance;
    private Double nebulaChance;
    private Double blackHoleChance;
    private Double cometChance;
    private Double spaceStationChance;
    private String gameType;
    private String difficultyLevel;
    private String theme;
    private String description;
    private Long galaxyOverseerId;

    // Additional galaxy metadata
    private String displayName;
    private Integer hexSystemId;
    private Double timeCompressionRatio;
    private String timezone;
    private java.time.LocalDateTime createdAt;

    @OneToMany(mappedBy = "galaxy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolarSystem> solarSystems = new ArrayList<>();

    @OneToMany(mappedBy = "galaxy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sector> sectors = new ArrayList<>();

    public Galaxy() {
    }

    public Galaxy(Long id, String name) {
        this.id = id;
        this.name = name;
        this.hyperlaneDensity = 1.0;
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

    public int getTotalSectors() {
        return totalSectors;
    }

    public void setTotalSectors(int totalSectors) {
        this.totalSectors = totalSectors;
    }

    public double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    public void setHyperlaneDensity(double hyperlaneDensity) {
        this.hyperlaneDensity = hyperlaneDensity;
    }

    public List<SolarSystem> getSolarSystems() {
        return solarSystems;
    }

    public void setSolarSystems(List<SolarSystem> solarSystems) {
        this.solarSystems = solarSystems;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public Long getSeed() { return seed; }
    public void setSeed(Long seed) { this.seed = seed; }
    public void setSeed(long seed) { this.seed = seed; }

    public Integer getSystemCount() { return systemCount; }
    public void setSystemCount(Integer systemCount) { this.systemCount = systemCount; }

    public Integer getPlanetsPerSystem() { return planetsPerSystem; }
    public void setPlanetsPerSystem(Integer planetsPerSystem) { this.planetsPerSystem = planetsPerSystem; }

    public Double getMoonChance() { return moonChance; }
    public void setMoonChance(Double moonChance) { this.moonChance = moonChance; }

    public Double getAsteroidBeltChance() { return asteroidBeltChance; }
    public void setAsteroidBeltChance(Double asteroidBeltChance) { this.asteroidBeltChance = asteroidBeltChance; }

    public Double getGasGiantChance() { return gasGiantChance; }
    public void setGasGiantChance(Double gasGiantChance) { this.gasGiantChance = gasGiantChance; }

    public Double getNebulaChance() { return nebulaChance; }
    public void setNebulaChance(Double nebulaChance) { this.nebulaChance = nebulaChance; }

    public Double getBlackHoleChance() { return blackHoleChance; }
    public void setBlackHoleChance(Double blackHoleChance) { this.blackHoleChance = blackHoleChance; }

    public Double getCometChance() { return cometChance; }
    public void setCometChance(Double cometChance) { this.cometChance = cometChance; }

    public Double getSpaceStationChance() { return spaceStationChance; }
    public void setSpaceStationChance(Double spaceStationChance) { this.spaceStationChance = spaceStationChance; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getGalaxyOverseerId() { return galaxyOverseerId; }
    public void setGalaxyOverseerId(Long galaxyOverseerId) { this.galaxyOverseerId = galaxyOverseerId; }

    // Additional galaxy metadata accessors

    public String getDisplayName() { return displayName != null && !displayName.isBlank() ? displayName : name; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getGalaxyName() { return name; }
    public void setGalaxyName(String galaxyName) { this.name = galaxyName; }

    public Integer getHexSystemId() { return hexSystemId; }
    public void setHexSystemId(Integer hexSystemId) { this.hexSystemId = hexSystemId; }
    public void setHexSystemId(int hexSystemId) { this.hexSystemId = hexSystemId; }

    public Double getTimeCompressionRatio() { return timeCompressionRatio; }
    public void setTimeCompressionRatio(Double timeCompressionRatio) { this.timeCompressionRatio = timeCompressionRatio; }
    public void setTimeCompressionRatio(double timeCompressionRatio) { this.timeCompressionRatio = timeCompressionRatio; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Returns the corporation that owns this galaxy — always null for model.Galaxy */
    public com.yisy.modules.doramcu.eve.model.EveCorporation getOwnedByCorporation() {
        return null;
    }

    @Override
    public String toString() {
        return "Galaxy{id=" + id + ", name='" + name + "', sectors=" + totalSectors + ", systems=" + solarSystems.size() + "}";
    }
}

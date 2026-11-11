package com.yisy.modules.doramcu.dto;

import java.time.LocalDateTime;

/**
 * DTO for galaxy metadata returned by admin API.
 * <p>
 * Contains all generation parameters and metadata needed to display
 * galaxy configuration in admin UI and support galaxy regeneration.
 * </p>
 */
public class GalaxyInfoDto {

    private Long id;
    private String name;
    private int totalSectors;
    private double hyperlaneDensity;
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
    private LocalDateTime createdAt;
    private String description;
    private String gameType;
    private String difficultyLevel;
    private String theme;
    private Long galaxyOverseerId;

    public GalaxyInfoDto() {
    }

    public GalaxyInfoDto(
            Long id, String name, int totalSectors, double hyperlaneDensity,
            Long seed, Integer systemCount, Integer planetsPerSystem,
            Double moonChance, Double asteroidBeltChance, Double gasGiantChance,
            Double nebulaChance, Double blackHoleChance, Double cometChance,
            Double spaceStationChance, LocalDateTime createdAt,
            String description, String gameType, String difficultyLevel, String theme) {
        this.id = id;
        this.name = name;
        this.totalSectors = totalSectors;
        this.hyperlaneDensity = hyperlaneDensity;
        this.seed = seed;
        this.systemCount = systemCount;
        this.planetsPerSystem = planetsPerSystem;
        this.moonChance = moonChance;
        this.asteroidBeltChance = asteroidBeltChance;
        this.gasGiantChance = gasGiantChance;
        this.nebulaChance = nebulaChance;
        this.blackHoleChance = blackHoleChance;
        this.cometChance = cometChance;
        this.spaceStationChance = spaceStationChance;
        this.createdAt = createdAt;
        this.description = description;
        this.gameType = gameType;
        this.difficultyLevel = difficultyLevel;
        this.theme = theme;
    }

    // --- Getters and Setters ---

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

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Integer getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(Integer systemCount) {
        this.systemCount = systemCount;
    }

    public Integer getPlanetsPerSystem() {
        return planetsPerSystem;
    }

    public void setPlanetsPerSystem(Integer planetsPerSystem) {
        this.planetsPerSystem = planetsPerSystem;
    }

    public Double getMoonChance() {
        return moonChance;
    }

    public void setMoonChance(Double moonChance) {
        this.moonChance = moonChance;
    }

    public Double getAsteroidBeltChance() {
        return asteroidBeltChance;
    }

    public void setAsteroidBeltChance(Double asteroidBeltChance) {
        this.asteroidBeltChance = asteroidBeltChance;
    }

    public Double getGasGiantChance() {
        return gasGiantChance;
    }

    public void setGasGiantChance(Double gasGiantChance) {
        this.gasGiantChance = gasGiantChance;
    }

    public Double getNebulaChance() {
        return nebulaChance;
    }

    public void setNebulaChance(Double nebulaChance) {
        this.nebulaChance = nebulaChance;
    }

    public Double getBlackHoleChance() {
        return blackHoleChance;
    }

    public void setBlackHoleChance(Double blackHoleChance) {
        this.blackHoleChance = blackHoleChance;
    }

    public Double getCometChance() {
        return cometChance;
    }

    public void setCometChance(Double cometChance) {
        this.cometChance = cometChance;
    }

    public Double getSpaceStationChance() {
        return spaceStationChance;
    }

    public void setSpaceStationChance(Double spaceStationChance) {
        this.spaceStationChance = spaceStationChance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Long getGalaxyOverseerId() {
        return galaxyOverseerId;
    }

    public void setGalaxyOverseerId(Long galaxyOverseerId) {
        this.galaxyOverseerId = galaxyOverseerId;
    }
}

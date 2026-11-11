package com.yisy.modules.doramcu.dto;

/**
 * Response DTO for galaxy template metadata.
 * <p>
 * Returned by the template listing endpoint so admins can discover
 * available templates before creating a galaxy.
 * </p>
 */
public class GalaxyTemplateDto {

    /**
     * Enum name of the template (e.g., "TRAVELLER_CLASSIC").
     */
    private String name;

    /**
     * Human-readable display name (e.g., "Traveller Classic").
     */
    private String displayName;

    /**
     * Description of the template's characteristics.
     */
    private String description;

    /**
     * Game type associated with the template (e.g., "DoraT", "Captain's Log").
     */
    private String gameType;

    /**
     * Difficulty level (e.g., "easy", "medium", "hard").
     */
    private String difficultyLevel;

    /**
     * Sector grid size (e.g., 10 for 10x10 grid).
     */
    private Integer sectorSize;

    /**
     * Number of empires to generate.
     */
    private Integer empireCount;

    /**
     * Galaxy shape (e.g., "spiral", "elliptical", "irregular").
     */
    private String galaxyShape;

    /**
     * Naming convention (e.g., "astronomical", "mythological", "star").
     */
    private String namingConvention;

    /**
     * Hyperlane density multiplier.
     */
    private Double hyperlaneDensity;

    /**
     * Civilization density [0.0, 1.0].
     */
    private Double civilizationDensity;

    /**
     * Resource distribution [0.0, 1.0].
     */
    private Double resourceDistribution;

    /**
     * Default constructor for JSON deserialization.
     */
    public GalaxyTemplateDto() {
    }

    /**
     * Constructor with all fields.
     */
    public GalaxyTemplateDto(String name, String displayName, String description,
                              String gameType, String difficultyLevel, Integer sectorSize,
                              Integer empireCount, String galaxyShape, String namingConvention,
                              Double hyperlaneDensity, Double civilizationDensity,
                              Double resourceDistribution) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.gameType = gameType;
        this.difficultyLevel = difficultyLevel;
        this.sectorSize = sectorSize;
        this.empireCount = empireCount;
        this.galaxyShape = galaxyShape;
        this.namingConvention = namingConvention;
        this.hyperlaneDensity = hyperlaneDensity;
        this.civilizationDensity = civilizationDensity;
        this.resourceDistribution = resourceDistribution;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Integer getSectorSize() {
        return sectorSize;
    }

    public void setSectorSize(Integer sectorSize) {
        this.sectorSize = sectorSize;
    }

    public Integer getEmpireCount() {
        return empireCount;
    }

    public void setEmpireCount(Integer empireCount) {
        this.empireCount = empireCount;
    }

    public String getGalaxyShape() {
        return galaxyShape;
    }

    public void setGalaxyShape(String galaxyShape) {
        this.galaxyShape = galaxyShape;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(String namingConvention) {
        this.namingConvention = namingConvention;
    }

    public Double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    public void setHyperlaneDensity(Double hyperlaneDensity) {
        this.hyperlaneDensity = hyperlaneDensity;
    }

    public Double getCivilizationDensity() {
        return civilizationDensity;
    }

    public void setCivilizationDensity(Double civilizationDensity) {
        this.civilizationDensity = civilizationDensity;
    }

    public Double getResourceDistribution() {
        return resourceDistribution;
    }

    public void setResourceDistribution(Double resourceDistribution) {
        this.resourceDistribution = resourceDistribution;
    }
}

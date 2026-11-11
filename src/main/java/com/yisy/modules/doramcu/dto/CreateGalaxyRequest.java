package com.yisy.modules.doramcu.dto;

/**
 * Request body for creating a new custom galaxy.
 * <p>
 * All parameters are optional — missing values will use the default template
 * from {@link com.yisy.modules.doramcu.hex.GalaxyFactory#defaultConfig()}.
 * </p>
 */
public class CreateGalaxyRequest {

    /**
     * Name of the galaxy (required).
     */
    private String name;

    /**
     * Predefined template to use as base configuration (optional).
     * <p>
     * Values: "TRAVELLER_CLASSIC", "STAR_TREK", "FRONTIER", "VOID", "CUSTOM".
     * When provided, the template's config is used as the base; any additional
     * fields in this request override the template values.
     * </p>
     */
    private String template;

    /**
     * Random seed for reproducibility (optional — auto-generated if omitted).
     */
    private Long seed;

    /**
     * Number of solar systems to generate (optional).
     */
    private Integer systemCount;

    /**
     * Planets per solar system (optional).
     */
    private Integer planetsPerSystem;

    /**
     * Probability of moons [0.0, 1.0] (optional).
     */
    private Double moonChance;

    /**
     * Probability of asteroid belts [0.0, 1.0] (optional).
     */
    private Double asteroidBeltChance;

    /**
     * Probability of gas giants [0.0, 1.0] (optional).
     */
    private Double gasGiantChance;

    /**
     * Probability of nebulas [0.0, 1.0] (optional).
     */
    private Double nebulaChance;

    /**
     * Probability of black holes [0.0, 1.0] (optional).
     */
    private Double blackHoleChance;

    /**
     * Probability of comets [0.0, 1.0] (optional).
     */
    private Double cometChance;

    /**
     * Probability of space stations [0.0, 1.0] (optional).
     */
    private Double spaceStationChance;

    /**
     * Hyperlane density [0.5, 2.75] (optional).
     */
    private Double hyperlaneDensity;

    /**
     * Short lore description of the galaxy (optional).
     */
    private String description;

    /**
     * Game type: "DoraT", "CaptainsLog", or "Hybrid" (optional).
     */
    private String gameType;

    /**
     * Difficulty level: "easy", "medium", "hard" (optional).
     */
    private String difficultyLevel;

    /**
     * Visual and narrative theme (optional).
     */
    private String theme;

    /**
     * Whether this is a custom theme that should be researched via web search + AI.
     * When true, the system searches the internet for information about the theme
     * and uses AI to generate rules and galaxy configuration.
     */
    private boolean customTheme;

    /**
     * Campaign theme for RPG-style games (optional).
     * Examples: "D&D", "Sci-Fi Horror", "Space Western".
     */
    private String campaignTheme;

    /**
     * Number of adventure hooks to seed in the galaxy (optional).
     */
    private Integer adventureHookCount;

    /**
     * NPC population density for RPG-style games [0.0, 1.0] (optional).
     */
    private Double npcDensity;

    /**
     * System theme for RPG-style games (optional).
     * Examples: "Death World", "Garden World", "Hellworld".
     */
    private String systemTheme;

    /**
     * Whether to enable the 4X layer for progressive unlock (optional).
     * When true, 4X features unlock as the player reaches milestones.
     */
    private boolean enable4xLayer;

    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
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

    public Double getHyperlaneDensity() {
        return hyperlaneDensity;
    }

    public void setHyperlaneDensity(Double hyperlaneDensity) {
        this.hyperlaneDensity = hyperlaneDensity;
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

    public boolean isCustomTheme() {
        return customTheme;
    }

    public void setCustomTheme(boolean customTheme) {
        this.customTheme = customTheme;
    }

    public String getCampaignTheme() {
        return campaignTheme;
    }

    public void setCampaignTheme(String campaignTheme) {
        this.campaignTheme = campaignTheme;
    }

    public Integer getAdventureHookCount() {
        return adventureHookCount;
    }

    public void setAdventureHookCount(Integer adventureHookCount) {
        this.adventureHookCount = adventureHookCount;
    }

    public Double getNpcDensity() {
        return npcDensity;
    }

    public void setNpcDensity(Double npcDensity) {
        this.npcDensity = npcDensity;
    }

    public String getSystemTheme() {
        return systemTheme;
    }

    public void setSystemTheme(String systemTheme) {
        this.systemTheme = systemTheme;
    }

    public boolean isEnable4xLayer() {
        return enable4xLayer;
    }

    public void setEnable4xLayer(boolean enable4xLayer) {
        this.enable4xLayer = enable4xLayer;
    }
}

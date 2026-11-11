package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity representing the economic system of a planet.
 * <p>
 * The economy tracks planet-wide economic indicators, market conditions,
 * trade routes, and resource availability. Similar to Traveller's WorldEconomy
 * but for a full planet with modern economic models.
 * </p>
 * <p>
 * Other games can query the economy to understand market conditions,
 * available resources, and trade opportunities on a planet.
 * </p>
 */
@Entity
@Table(name = "earth2026_economies", indexes = {
        @Index(name = "idx_earth2026_economy_planet", columnList = "planetId", unique = true)
})
public class Earth2026Economy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Planet this economy belongs to.
     */
    @Column(name = "planet_id", nullable = false, unique = true)
    private Long planetId;

    /**
     * Overall GDP in trillions USD.
     */
    @Column(name = "gdp_trillions", nullable = false)
    private Double gdpTrillions;

    /**
     * GDP growth rate (annual percentage).
     */
    @Column(name = "growth_rate")
    private Double growthRate;

    /**
     * Inflation rate (annual percentage).
     */
    @Column(name = "inflation_rate")
    private Double inflationRate;

    /**
     * Unemployment rate (percentage).
     */
    @Column(name = "unemployment_rate")
    private Double unemploymentRate;

    /**
     * Market size indicator (1-10).
     */
    @Column(name = "market_size", nullable = false)
    private Integer marketSize;

    /**
     * Trade openness (1-10, higher = more open trade).
     */
    @Column(name = "trade_openness", nullable = false)
    private Integer tradeOpenness;

    /**
     * Technology index (1-10).
     */
    @Column(name = "technology_index", nullable = false)
    private Integer technologyIndex;

    /**
     * Resource abundance (1-10).
     */
    @Column(name = "resource_abundance", nullable = false)
    private Integer resourceAbundance;

    /**
     * Financial stability (1-10).
     */
    @Column(name = "financial_stability", nullable = false)
    private Integer financialStability;

    /**
     * Base price level multiplier (1.0 = baseline).
     */
    @Column(name = "price_level", nullable = false)
    private Double priceLevel;

    /**
     * Market conditions: "booming", "stable", "declining", "recession", "depression".
     */
    @Column(name = "market_condition", nullable = false, length = 50)
    private String marketCondition;

    /**
     * Dominant industries (comma-separated, e.g., "Technology,Finance,Manufacturing").
     */
    @Column(name = "dominant_industries", length = 500)
    private String dominantIndustries;

    /**
     * Export commodities (comma-separated).
     */
    @Column(name = "export_commodities", length = 500)
    private String exportCommodities;

    /**
     * Import commodities (comma-separated).
     */
    @Column(name = "import_commodities", length = 500)
    private String importCommodities;

    /**
     * Whether trade is restricted on this planet.
     */
    @Column(name = "trade_restricted", nullable = false)
    private boolean tradeRestricted;

    /**
     * Whether this economy is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

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

    public Earth2026Economy() {
    }

    public Earth2026Economy(Long planetId) {
        this.planetId = planetId;
        this.gdpTrillions = 100.0;
        this.growthRate = 2.0;
        this.inflationRate = 2.5;
        this.unemploymentRate = 5.0;
        this.marketSize = 5;
        this.tradeOpenness = 5;
        this.technologyIndex = 5;
        this.resourceAbundance = 5;
        this.financialStability = 5;
        this.priceLevel = 1.0;
        this.marketCondition = "stable";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public Double getGdpTrillions() {
        return gdpTrillions;
    }

    public void setGdpTrillions(Double gdpTrillions) {
        this.gdpTrillions = gdpTrillions;
    }

    public Double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(Double growthRate) {
        this.growthRate = growthRate;
    }

    public Double getInflationRate() {
        return inflationRate;
    }

    public void setInflationRate(Double inflationRate) {
        this.inflationRate = inflationRate;
    }

    public Double getUnemploymentRate() {
        return unemploymentRate;
    }

    public void setUnemploymentRate(Double unemploymentRate) {
        this.unemploymentRate = unemploymentRate;
    }

    public Integer getMarketSize() {
        return marketSize;
    }

    public void setMarketSize(Integer marketSize) {
        this.marketSize = marketSize;
    }

    public Integer getTradeOpenness() {
        return tradeOpenness;
    }

    public void setTradeOpenness(Integer tradeOpenness) {
        this.tradeOpenness = tradeOpenness;
    }

    public Integer getTechnologyIndex() {
        return technologyIndex;
    }

    public void setTechnologyIndex(Integer technologyIndex) {
        this.technologyIndex = technologyIndex;
    }

    public Integer getResourceAbundance() {
        return resourceAbundance;
    }

    public void setResourceAbundance(Integer resourceAbundance) {
        this.resourceAbundance = resourceAbundance;
    }

    public Integer getFinancialStability() {
        return financialStability;
    }

    public void setFinancialStability(Integer financialStability) {
        this.financialStability = financialStability;
    }

    public Double getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(Double priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getMarketCondition() {
        return marketCondition;
    }

    public void setMarketCondition(String marketCondition) {
        this.marketCondition = marketCondition;
    }

    public String getDominantIndustries() {
        return dominantIndustries;
    }

    public void setDominantIndustries(String dominantIndustries) {
        this.dominantIndustries = dominantIndustries;
    }

    public String getExportCommodities() {
        return exportCommodities;
    }

    public void setExportCommodities(String exportCommodities) {
        this.exportCommodities = exportCommodities;
    }

    public String getImportCommodities() {
        return importCommodities;
    }

    public void setImportCommodities(String importCommodities) {
        this.importCommodities = importCommodities;
    }

    public boolean isTradeRestricted() {
        return tradeRestricted;
    }

    public void setTradeRestricted(boolean tradeRestricted) {
        this.tradeRestricted = tradeRestricted;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
     * Calculate the overall economic health score (1-10).
     * Based on growth, stability, employment, and market size.
     *
     @return health score from 1-10
     */
    public double getHealthScore() {
        double growthScore = Math.min(10, Math.max(1, (growthRate + 5) * 1.0));
        double stabilityScore = financialStability;
        double employmentScore = Math.max(1, 10 - unemploymentRate);
        return (growthScore * 0.25 + stabilityScore * 0.3 + employmentScore * 0.25 + marketSize * 0.2);
    }

    @Override
    public String toString() {
        return "Earth2026Economy{" +
                "id=" + id +
                ", planetId=" + planetId +
                ", gdp=" + gdpTrillions + "T" +
                ", growth=" + growthRate + "%" +
                ", market='" + marketCondition + '\'' +
                ", health=" + String.format("%.1f", getHealthScore()) +
                '}';
    }
}

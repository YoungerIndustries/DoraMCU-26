package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a region within a country on Earth.
 * <p>
 * Regions are sub-country divisions like states, provinces, prefectures, or territories.
 * Each region belongs to a country and contains locations.
 * </p>
 */
@Entity
@Table(name = "earth2026_regions", indexes = {
        @Index(name = "idx_earth2026_region_name", columnList = "name"),
        @Index(name = "idx_earth2026_region_country", columnList = "countryId")
})
public class Earth2026Region {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Region name (e.g., "California", "Bavaria", "Tokyo").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Region type (e.g., "State", "Province", "Territory", "Prefecture").
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * Country this region belongs to.
     */
    @Column(name = "country_id", nullable = false)
    private Long countryId;

    /**
     * Population in millions.
     */
    @Column(name = "population_millions")
    private Double populationMillions;

    /**
     * GDP in billions USD.
     */
    @Column(name = "gdp_billions")
    private Double gdpBillions;

    /**
     * Tech level of the region (can differ from country's base TL).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tech_level")
    private TechLevel techLevel;

    /**
     * Capital city of the region.
     */
    @Column(name = "capital_city", length = 100)
    private String capitalCity;

    /**
     * Whether this region is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * AI Character ID that governs this region.
     */
    @Column(name = "ai_governor_id")
    private Long aiGovernorId;

    /**
     * Governor name.
     */
    @Column(name = "governor_name", length = 200)
    private String governorName;

    /**
     * Governor title (e.g., "Governor", "Premier", "Lieutenant Governor").
     */
    @Column(name = "governor_title", length = 100)
    private String governorTitle;

    /**
     * Description of the region.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

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

    public Earth2026Region() {
    }

    public Earth2026Region(String name, String type, Long countryId) {
        this.name = name;
        this.type = type;
        this.countryId = countryId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Double getPopulationMillions() {
        return populationMillions;
    }

    public void setPopulationMillions(Double populationMillions) {
        this.populationMillions = populationMillions;
    }

    public Double getGdpBillions() {
        return gdpBillions;
    }

    public void setGdpBillions(Double gdpBillions) {
        this.gdpBillions = gdpBillions;
    }

    public TechLevel getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(TechLevel techLevel) {
        this.techLevel = techLevel;
    }

    public String getCapitalCity() {
        return capitalCity;
    }

    public void setCapitalCity(String capitalCity) {
        this.capitalCity = capitalCity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getAiGovernorId() {
        return aiGovernorId;
    }

    public void setAiGovernorId(Long aiGovernorId) {
        this.aiGovernorId = aiGovernorId;
    }

    public String getGovernorName() {
        return governorName;
    }

    public void setGovernorName(String governorName) {
        this.governorName = governorName;
    }

    public String getGovernorTitle() {
        return governorTitle;
    }

    public void setGovernorTitle(String governorTitle) {
        this.governorTitle = governorTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return "Earth2026Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", countryId=" + countryId +
                ", population=" + populationMillions + "M" +
                '}';
    }
}

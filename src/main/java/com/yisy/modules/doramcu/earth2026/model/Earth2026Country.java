package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a country on Earth in the Earth 2026 game.
 * <p>
 * Countries are the top-level political divisions of a planet, each with its own
 * government, economy, military, and AI-controlled leader. Similar to the Traveller
 * Empire model but for planetary-scale nations.
 * </p>
 * <p>
 * Each country contains regions (states/provinces), which contain locations,
 * which contain structures. This hierarchy allows games to request planet data
 * at any level of granularity.
 * </p>
 */
@Entity
@Table(name = "earth2026_countries", indexes = {
        @Index(name = "idx_earth2026_country_name", columnList = "name"),
        @Index(name = "idx_earth2026_country_code", columnList = "isoCode", unique = true),
        @Index(name = "idx_earth2026_country_planet", columnList = "planetId")
})
public class Earth2026Country {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Country name (e.g., "United States", "Germany", "Japan").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "JP").
     */
    @Column(name = "iso_code", nullable = false, length = 2, unique = true)
    private String isoCode;

    /**
     * ISO 3166-1 alpha-3 country code (e.g., "USA", "DEU", "JPN").
     */
    @Column(name = "iso_code_3", length = 3)
    private String isoCode3;

    /**
     * Capital city name.
     */
    @Column(name = "capital_city", length = 100)
    private String capitalCity;

    /**
     * Government type (e.g., "Federal Republic", "Constitutional Monarchy", "Parliamentary Democracy").
     */
    @Column(name = "government_type", nullable = false, length = 100)
    private String governmentType;

    /**
     * Political ideology (e.g., "Liberal Democracy", "Social Democracy", "Authoritarian").
     */
    @Column(name = "political_ideology", length = 100)
    private String politicalIdeology;

    /**
     * Population in millions.
     */
    @Column(name = "population_millions", nullable = false)
    private Double populationMillions;

    /**
     * GDP in trillions USD.
     */
    @Column(name = "gdp_trillions", nullable = false)
    private Double gdpTrillions;

    /**
     * GDP per capita in USD.
     */
    @Column(name = "gdp_per_capita", nullable = false)
    private Integer gdpPerCapita;

    /**
     * Tech level of the country (TL0-TL20).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tech_level", nullable = false)
    private TechLevel techLevel;

    /**
     * Military strength rating (1-10).
     */
    @Column(name = "military_strength", nullable = false)
    private Integer militaryStrength;

    /**
     * Economic power rating (1-10).
     */
    @Column(name = "economic_power", nullable = false)
    private Integer economicPower;

    /**
     * Diplomatic influence rating (1-10).
     */
    @Column(name = "diplomatic_influence", nullable = false)
    private Integer diplomaticInfluence;

    /**
     * Whether this country is currently active and playable.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * AI Character ID that controls this country (the head of state/government).
     */
    @Column(name = "ai_leader_id")
    private Long aiLeaderId;

    /**
     * Leader name (display name of the head of state).
     */
    @Column(name = "leader_name", length = 200)
    private String leaderName;

    /**
     * Leader title (e.g., "President", "Prime Minister", "Chancellor").
     */
    @Column(name = "leader_title", length = 100)
    private String leaderTitle;

    /**
     * Planet ID this country belongs to.
     */
    @Column(name = "planet_id", nullable = false)
    private Long planetId;

    /**
     * Description of the country.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Continent or major geographic region.
     */
    @Column(length = 100)
    private String continent;

    /**
     * Official language(s).
     */
    @Column(name = "official_languages", length = 500)
    private String officialLanguages;

    /**
     * Currency name and code (e.g., "USD - US Dollar").
     */
    @Column(length = 100)
    private String currency;

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

    public Earth2026Country() {
    }

    public Earth2026Country(String name, String isoCode, String governmentType, Long planetId) {
        this.name = name;
        this.isoCode = isoCode;
        this.governmentType = governmentType;
        this.planetId = planetId;
        this.techLevel = TechLevel.TL5;
        this.populationMillions = 1.0;
        this.gdpTrillions = 0.01;
        this.gdpPerCapita = 10000;
        this.militaryStrength = 3;
        this.economicPower = 3;
        this.diplomaticInfluence = 3;
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

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getIsoCode3() {
        return isoCode3;
    }

    public void setIsoCode3(String isoCode3) {
        this.isoCode3 = isoCode3;
    }

    public String getCapitalCity() {
        return capitalCity;
    }

    public void setCapitalCity(String capitalCity) {
        this.capitalCity = capitalCity;
    }

    public String getGovernmentType() {
        return governmentType;
    }

    public void setGovernmentType(String governmentType) {
        this.governmentType = governmentType;
    }

    public String getPoliticalIdeology() {
        return politicalIdeology;
    }

    public void setPoliticalIdeology(String politicalIdeology) {
        this.politicalIdeology = politicalIdeology;
    }

    public Double getPopulationMillions() {
        return populationMillions;
    }

    public void setPopulationMillions(Double populationMillions) {
        this.populationMillions = populationMillions;
    }

    public Double getGdpTrillions() {
        return gdpTrillions;
    }

    public void setGdpTrillions(Double gdpTrillions) {
        this.gdpTrillions = gdpTrillions;
    }

    public Integer getGdpPerCapita() {
        return gdpPerCapita;
    }

    public void setGdpPerCapita(Integer gdpPerCapita) {
        this.gdpPerCapita = gdpPerCapita;
    }

    public TechLevel getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(TechLevel techLevel) {
        this.techLevel = techLevel;
    }

    public Integer getMilitaryStrength() {
        return militaryStrength;
    }

    public void setMilitaryStrength(Integer militaryStrength) {
        this.militaryStrength = militaryStrength;
    }

    public Integer getEconomicPower() {
        return economicPower;
    }

    public void setEconomicPower(Integer economicPower) {
        this.economicPower = economicPower;
    }

    public Integer getDiplomaticInfluence() {
        return diplomaticInfluence;
    }

    public void setDiplomaticInfluence(Integer diplomaticInfluence) {
        this.diplomaticInfluence = diplomaticInfluence;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getAiLeaderId() {
        return aiLeaderId;
    }

    public void setAiLeaderId(Long aiLeaderId) {
        this.aiLeaderId = aiLeaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLeaderTitle() {
        return leaderTitle;
    }

    public void setLeaderTitle(String leaderTitle) {
        this.leaderTitle = leaderTitle;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getOfficialLanguages() {
        return officialLanguages;
    }

    public void setOfficialLanguages(String officialLanguages) {
        this.officialLanguages = officialLanguages;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
     * Calculate the overall power score of this country.
     * Weighted average of military, economic, and diplomatic power.
     *
     * @return power score from 1-10
     */
    public double getPowerScore() {
        return (militaryStrength * 0.4 + economicPower * 0.35 + diplomaticInfluence * 0.25);
    }

    @Override
    public String toString() {
        return "Earth2026Country{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isoCode='" + isoCode + '\'' +
                ", government='" + governmentType + '\'' +
                ", population=" + populationMillions + "M" +
                ", gdp=" + gdpTrillions + "T" +
                ", techLevel=" + techLevel +
                ", power=" + String.format("%.1f", getPowerScore()) +
                '}';
    }
}

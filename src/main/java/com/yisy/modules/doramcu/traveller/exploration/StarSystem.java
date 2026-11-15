package com.yisy.modules.doramcu.traveller.exploration;

import jakarta.persistence.*;

import java.util.*;

/**
 * JPA entity representing a star system in the Traveller universe.
 * <p>
 * Contains information about the star, planets, trade goods, and hazards.
 * Used for navigation and exploration gameplay.
 * </p>
 */
@Entity
@Table(name = "traveller_star_system")
public class StarSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** System name (e.g., "Sol", "Rokuga") */
    @Column(nullable = false)
    private String name;

    /** Hex sector coordinates (e.g., "1717") */
    @Column(nullable = false)
    private String hexSector;

    /** Star type (e.g., "M0", "G2", "K5") */
    private String starType;

    /** Number of planets */
    @Column(nullable = false)
    private int planetCount;

    /** Primary planet index (0-based) */
    private int primaryPlanet;

    /** World modifiers (e.g., "A456789AB") */
    private String worldModifiers;

    /** Population level (0-9) */
    private int population;

    /** Government type (0-9, A=Feudal, B=Confederacy, etc.) */
    private String government;

    /** Law level (0-9, A=Anarchy) */
    private String lawLevel;

    /** Alliance level */
    private int alliance;

    /** Starports available (comma-separated, e.g., "A,B,C") */
    private String starports;

    /** Trade codes (comma-separated, e.g., "Agri,PoOr,HiTe") */
    private String tradeCodes;

    /** Available commodities (comma-separated) */
    private String commodities;

    /** Hazards present (comma-separated, e.g., "Rift,Cloud") */
    private String hazards;

    /** Wealth level (0-9) */
    private int wealth;

    /** Tech level (0-15) */
    private int techLevel;

    /** Government alignment */
    private String governmentAlignment;

    public StarSystem() {
    }

    public StarSystem(String name, String hexSector) {
        this.name = Objects.requireNonNull(name);
        this.hexSector = Objects.requireNonNull(hexSector);
        this.planetCount = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }

    public String getHexSector() { return hexSector; }
    public void setHexSector(String hexSector) { this.hexSector = Objects.requireNonNull(hexSector); }

    public String getStarType() { return starType; }
    public void setStarType(String starType) { this.starType = starType; }

    public int getPlanetCount() { return planetCount; }
    public void setPlanetCount(int planetCount) { this.planetCount = planetCount; }

    public int getPrimaryPlanet() { return primaryPlanet; }
    public void setPrimaryPlanet(int primaryPlanet) { this.primaryPlanet = primaryPlanet; }

    public String getWorldModifiers() { return worldModifiers; }
    public void setWorldModifiers(String worldModifiers) { this.worldModifiers = worldModifiers; }

    public int getPopulation() { return population; }
    public void setPopulation(int population) { this.population = population; }

    public String getGovernment() { return government; }
    public void setGovernment(String government) { this.government = government; }

    public String getLawLevel() { return lawLevel; }
    public void setLawLevel(String lawLevel) { this.lawLevel = lawLevel; }

    public int getAlliance() { return alliance; }
    public void setAlliance(int alliance) { this.alliance = alliance; }

    public String getStarports() { return starports; }
    public void setStarports(String starports) { this.starports = starports; }

    public String getTradeCodes() { return tradeCodes; }
    public void setTradeCodes(String tradeCodes) { this.tradeCodes = tradeCodes; }

    public String getCommodities() { return commodities; }
    public void setCommodities(String commodities) { this.commodities = commodities; }

    public String getHazards() { return hazards; }
    public void setHazards(String hazards) { this.hazards = hazards; }

    public int getWealth() { return wealth; }
    public void setWealth(int wealth) { this.wealth = wealth; }

    public int getTechLevel() { return techLevel; }
    public void setTechLevel(int techLevel) { this.techLevel = techLevel; }

    public String getGovernmentAlignment() { return governmentAlignment; }
    public void setGovernmentAlignment(String governmentAlignment) { this.governmentAlignment = governmentAlignment; }

    /**
     * Get list of commodities.
     */
    public List<String> getCommodityList() {
        if (commodities == null || commodities.isEmpty()) return Collections.emptyList();
        return Arrays.asList(commodities.split(","));
    }

    /**
     * Get list of hazards.
     */
    public List<String> getHazardList() {
        if (hazards == null || hazards.isEmpty()) return Collections.emptyList();
        return Arrays.asList(hazards.split(","));
    }

    /**
     * Get list of trade codes.
     */
    public List<String> getTradeCodeList() {
        if (tradeCodes == null || tradeCodes.isEmpty()) return Collections.emptyList();
        return Arrays.asList(tradeCodes.split(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarSystem that = (StarSystem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StarSystem{" +
                "name='" + name + '\'' +
                ", hexSector='" + hexSector + '\'' +
                '}';
    }
}

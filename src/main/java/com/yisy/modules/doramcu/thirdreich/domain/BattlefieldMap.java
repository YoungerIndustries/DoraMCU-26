package com.yisy.modules.doramcu.thirdreich.domain;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.earth2026.model.BiosphereContext;
import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.model.TerrainElevationMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A complete 3D battlefield map combining terrain, structures, biosphere,
 * and unit positions for the Third Reich combat engine.
 * <p>
 * This is the central model for 3D battlefield representation. It aggregates:
 * <ul>
 *   <li>Terrain elevation data from TerrainElevationMap</li>
 *   <li>Building layouts from Earth2026BattleEnvironment.StructureInfo</li>
 *   <li>Environmental effects from BiosphereContext</li>
 *   <li>Unit placements with 3D coordinates</li>
 * </ul>
 * </p>
 */
public class BattlefieldMap {

    private final String id;
    private final String name;
    private final Long locationId;
    private final String locationName;
    private final String city;
    private final String country;

    // Terrain data
    private final TerrainElevationMap terrainElevationMap;
    private final TerrainType terrainType;
    private final Double groundElevation;

    // Biosphere data
    private final BiosphereContext biosphereContext;

    // Structures
    private final List<Earth2026BattleEnvironment.StructureInfo> structures;

    // Unit placements
    private final List<UnitPlacement> attackerPlacements;
    private final List<UnitPlacement> defenderPlacements;

    // Tech level
    private final int baseTechLevel;
    private final String timePeriod;

    // Metadata
    private final LocalDateTime createdAt;

    private BattlefieldMap(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.name = builder.name;
        this.locationId = builder.locationId;
        this.locationName = builder.locationName;
        this.city = builder.city;
        this.country = builder.country;
        this.terrainElevationMap = builder.terrainElevationMap;
        this.terrainType = builder.terrainType != null ? builder.terrainType : TerrainType.PLAINS;
        this.groundElevation = builder.groundElevation;
        this.biosphereContext = builder.biosphereContext;
        this.structures = builder.structures != null ? new ArrayList<>(builder.structures) : new ArrayList<>();
        this.attackerPlacements = builder.attackerPlacements != null ?
                new ArrayList<>(builder.attackerPlacements) : new ArrayList<>();
        this.defenderPlacements = builder.defenderPlacements != null ?
                new ArrayList<>(builder.defenderPlacements) : new ArrayList<>();
        this.baseTechLevel = builder.baseTechLevel;
        this.timePeriod = builder.timePeriod;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    // ---- Accessors ----

    public String getId() { return id; }
    public String getName() { return name; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public String getCity() { return city; }
    public String getCountry() { return country; }

    public TerrainElevationMap getTerrainElevationMap() { return terrainElevationMap; }
    public TerrainType getTerrainType() { return terrainType; }
    public Double getGroundElevation() { return groundElevation; }

    public BiosphereContext getBiosphereContext() { return biosphereContext; }

    public List<Earth2026BattleEnvironment.StructureInfo> getStructures() { return structures; }

    public List<UnitPlacement> getAttackerPlacements() { return attackerPlacements; }
    public List<UnitPlacement> getDefenderPlacements() { return defenderPlacements; }

    public int getBaseTechLevel() { return baseTechLevel; }
    public String getTimePeriod() { return timePeriod; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ---- Mutators ----

    public void addAttackerPlacement(UnitPlacement placement) {
        attackerPlacements.add(placement);
    }

    public void addDefenderPlacement(UnitPlacement placement) {
        defenderPlacements.add(placement);
    }

    public void removeAttackerPlacement(String unitId) {
        attackerPlacements.removeIf(p -> p.getUnit().getId().equals(unitId));
    }

    public void removeDefenderPlacement(String unitId) {
        defenderPlacements.removeIf(p -> p.getUnit().getId().equals(unitId));
    }

    // ---- Queries ----

    /**
     * Gets all unit placements (both teams).
     */
    public List<UnitPlacement> getAllPlacements() {
        List<UnitPlacement> all = new ArrayList<>();
        all.addAll(attackerPlacements);
        all.addAll(defenderPlacements);
        return all;
    }

    /**
     * Gets the number of attacker units.
     */
    public int getAttackerUnitCount() {
        return attackerPlacements.size();
    }

    /**
     * Gets the number of defender units.
     */
    public int getDefenderUnitCount() {
        return defenderPlacements.size();
    }

    /**
     * Checks if a position is occupied by any unit.
     */
    public boolean isPositionOccupied(double x, double y, double radius) {
        for (UnitPlacement placement : getAllPlacements()) {
            double dx = placement.getX() - x;
            double dy = placement.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < radius) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets structures with 3D coordinates.
     */
    public List<Earth2026BattleEnvironment.StructureInfo> getStructuresWithCoordinates() {
        return structures.stream()
                .filter(Earth2026BattleEnvironment.StructureInfo::hasSpatialCoordinates)
                .toList();
    }

    @Override
    public String toString() {
        return "BattlefieldMap{id='" + id + "', name='" + name + "', location='" + locationName +
                "', terrain=" + terrainType + ", structures=" + structures.size() +
                ", attackers=" + attackerPlacements.size() + ", defenders=" + defenderPlacements.size() + "}";
    }

    // ---- Builder ----

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private Long locationId;
        private String locationName;
        private String city;
        private String country;
        private TerrainElevationMap terrainElevationMap;
        private TerrainType terrainType;
        private Double groundElevation;
        private BiosphereContext biosphereContext;
        private List<Earth2026BattleEnvironment.StructureInfo> structures;
        private List<UnitPlacement> attackerPlacements;
        private List<UnitPlacement> defenderPlacements;
        private int baseTechLevel;
        private String timePeriod;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder locationId(Long locationId) { this.locationId = locationId; return this; }
        public Builder locationName(String locationName) { this.locationName = locationName; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder terrainElevationMap(TerrainElevationMap terrainElevationMap) {
            this.terrainElevationMap = terrainElevationMap; return this;
        }
        public Builder terrainType(TerrainType terrainType) { this.terrainType = terrainType; return this; }
        public Builder groundElevation(Double groundElevation) { this.groundElevation = groundElevation; return this; }
        public Builder biosphereContext(BiosphereContext biosphereContext) {
            this.biosphereContext = biosphereContext; return this;
        }
        public Builder structures(List<Earth2026BattleEnvironment.StructureInfo> structures) {
            this.structures = structures; return this;
        }
        public Builder attackerPlacements(List<UnitPlacement> attackerPlacements) {
            this.attackerPlacements = attackerPlacements; return this;
        }
        public Builder defenderPlacements(List<UnitPlacement> defenderPlacements) {
            this.defenderPlacements = defenderPlacements; return this;
        }
        public Builder baseTechLevel(int baseTechLevel) { this.baseTechLevel = baseTechLevel; return this; }
        public Builder timePeriod(String timePeriod) { this.timePeriod = timePeriod; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public BattlefieldMap build() {
            return new BattlefieldMap(this);
        }
    }
}

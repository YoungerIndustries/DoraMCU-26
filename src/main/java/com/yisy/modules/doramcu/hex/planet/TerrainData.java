package com.yisy.modules.doramcu.hex.planet;

import java.util.Objects;

/**
 * Holds terrain and biome data for a single hex cell on a planet surface.
 * <p>
 * This class is designed to be stored as the {@code payload} of a {@link com.yisy.modules.doramcu.hex.Hex3D}
 * cell on the planet's crust layer. It provides:
 * <ul>
 *   <li>The {@link Biome} describing terrain type, temperature, moisture, and altitude</li>
 *   <li>Optional settlement reference if a settlement occupies this cell</li>
 *   <li>Optional industry reference if an industry is built on this cell</li>
 *   <li>Infrastructure data (roads, rails, stations)</li>
 * </ul>
 * <p>
 * Usage pattern:
 * <pre>
 * {@code
 * TerrainData terrain = new TerrainData(biome);
 * hex3DCell.place(terrain);  // Hex3D payload now holds terrain data
 * }
 * </pre>
 */
public class TerrainData {

    private final Biome biome;
    private int planetId;
    private String settlementId;
    private String industryId;
    private InfrastructureType infrastructure;
    private int waterLevel;

    /**
     * Infrastructure types that can be built on a hex cell.
     */
    public enum InfrastructureType {
        /** No infrastructure */
        NONE,
        /** Road — allows land vehicle movement */
        ROAD,
        /** Rail — faster land vehicle movement */
        RAIL,
        /** Station — cargo loading/unloading point */
        STATION,
        /** Port — water cargo loading/unloading */
        PORT,
        /** Airport — air cargo transport */
        AIRPORT
    }

    /**
     * Creates terrain data with the given biome.
     *
     * @param biome the biome describing this cell's terrain and climate
     */
    public TerrainData(Biome biome) {
        if (biome == null) {
            throw new IllegalArgumentException("Biome must not be null");
        }
        this.biome = biome;
        this.planetId = -1;
        this.settlementId = null;
        this.industryId = null;
        this.infrastructure = InfrastructureType.NONE;
        this.waterLevel = 0;
    }

    /** @return the biome for this cell */
    public Biome getBiome() {
        return biome;
    }

    /** @return the terrain type */
    public TerrainType getTerrainType() {
        return biome.getTerrainType();
    }

    /** @return the planet ID where this terrain cell is located */
    public int getPlanetId() {
        return planetId;
    }

    /**
     * Sets the planet ID for this terrain cell.
     *
     * @param planetId the planet ID
     */
    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    /**
     * Sets the settlement occupying this cell.
     *
     * @param settlementId the unique ID of the settlement, or null to remove
     */
    public void setSettlement(String settlementId) {
        this.settlementId = settlementId;
    }

    /** @return the settlement ID, or null if no settlement */
    public String getSettlementId() {
        return settlementId;
    }

    /** @return true if a settlement occupies this cell */
    public boolean hasSettlement() {
        return settlementId != null;
    }

    /**
     * Sets the industry on this cell.
     *
     * @param industryId the unique ID of the industry, or null to remove
     */
    public void setIndustry(String industryId) {
        this.industryId = industryId;
    }

    /** @return the industry ID, or null if no industry */
    public String getIndustryId() {
        return industryId;
    }

    /** @return true if an industry is built on this cell */
    public boolean hasIndustry() {
        return industryId != null;
    }

    /** @return the infrastructure type on this cell */
    public InfrastructureType getInfrastructure() {
        return infrastructure;
    }

    /**
     * Sets the infrastructure type on this cell.
     *
     * @param infrastructure the new infrastructure type
     */
    public void setInfrastructure(InfrastructureType infrastructure) {
        if (infrastructure == null) {
            throw new IllegalArgumentException("Infrastructure type must not be null");
        }
        this.infrastructure = infrastructure;
    }

    /** @return the water level (0 = dry, higher = deeper water) */
    public int getWaterLevel() {
        return waterLevel;
    }

    /**
     * Sets the water level for this cell.
     *
     * @param waterLevel the water level (0 = dry)
     */
    public void setWaterLevel(int waterLevel) {
        if (waterLevel < 0) {
            throw new IllegalArgumentException("Water level must be non-negative: " + waterLevel);
        }
        this.waterLevel = waterLevel;
    }

    /**
     * @return true if this cell is passable by land vehicles
     * Considers terrain type and infrastructure.
     */
    public boolean isPassableByLand() {
        if (!biome.getTerrainType().isPassableByLand()) return false;
        // Roads and rails make terrain passable even if marginal
        if (infrastructure == InfrastructureType.ROAD || infrastructure == InfrastructureType.RAIL) {
            return true;
        }
        return biome.getTerrainType().isPassableByLand();
    }

    /**
     * @return the movement cost for land vehicles on this cell
     * Returns 0.0 if not passable.
     */
    public double getLandMovementCost() {
        if (!isPassableByLand()) return 0.0;
        double cost = biome.getTerrainType().getMovementCost();
        // Roads reduce movement cost
        if (infrastructure == InfrastructureType.ROAD) cost *= 0.7;
        // Rails reduce movement cost further
        if (infrastructure == InfrastructureType.RAIL) cost *= 0.5;
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerrainData that = (TerrainData) o;
        return Objects.equals(biome, that.biome) &&
                Objects.equals(settlementId, that.settlementId) &&
                Objects.equals(industryId, that.industryId) &&
                infrastructure == that.infrastructure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(biome, settlementId, industryId, infrastructure);
    }

    @Override
    public String toString() {
        return String.format("TerrainData{%s, settlement=%s, industry=%s, infra=%s}",
                biome.getName(), settlementId, industryId, infrastructure);
    }
}

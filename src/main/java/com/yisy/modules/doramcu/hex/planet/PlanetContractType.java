package com.yisy.modules.doramcu.hex.planet;

/**
 * Types of contracts that can be issued for planet surface operations.
 * <p>
 * Each contract type corresponds to a specific building or development action
 * on a planet surface. Contracts define the work to be done, the resources
 * required, and the rewards for completion.
 * </p>
 */
public enum PlanetContractType {

    /**
     * Found a new settlement on the planet surface.
     * Requires: suitable terrain, resources for initial population.
     */
    FOUND_SETTLEMENT("Found Settlement", "Establish a new settlement on the planet surface"),

    /**
     * Expand an existing settlement to a larger size.
     * Requires: cargo delivery to meet growth goals.
     */
    EXPAND_SETTLEMENT("Expand Settlement", "Grow an existing settlement to the next size tier"),

    /**
     * Build a primary industry (mine, farm, quarry).
     * Requires: suitable terrain, raw materials.
     */
    BUILD_PRIMARY_INDUSTRY("Build Primary Industry", "Construct a mine, farm, or quarry"),

    /**
     * Build a secondary industry (factory, refinery).
     * Requires: primary industry output, infrastructure.
     */
    BUILD_SECONDARY_INDUSTRY("Build Secondary Industry", "Construct a factory or refinery"),

    /**
     * Build a tertiary industry (advanced factory).
     * Requires: secondary industry output, high tech level.
     */
    BUILD_TERTIARY_INDUSTRY("Build Tertiary Industry", "Construct an advanced manufacturing facility"),

    /**
     * Construct a road between two hex cells.
     * Requires: adjacent land cells, construction materials.
     */
    BUILD_ROAD("Build Road", "Construct a road connecting two locations"),

    /**
     * Construct a rail line between two hex cells.
     * Requires: road or land, advanced materials.
     */
    BUILD_RAIL("Build Rail", "Construct a rail line for faster transport"),

    /**
     * Build a cargo station near a settlement or industry.
     * Requires: nearby settlement or industry, construction materials.
     */
    BUILD_STATION("Build Station", "Construct a cargo loading station"),

    /**
     * Build a port on a water-adjacent hex.
     * Requires: shallow water or beach terrain.
     */
    BUILD_PORT("Build Port", "Construct a seaport for ship traffic"),

    /**
     * Deploy a surface vehicle on a route.
     * Requires: stations at start and end points.
     */
    DEPLOY_VEHICLE("Deploy Vehicle", "Assign a vehicle to a transport route"),

    /**
     * Establish a cargo delivery route between two stations.
     * Requires: stations, vehicles, road/rail connection.
     */
    ESTABLISH_ROUTE("Establish Route", "Create a new cargo delivery route"),

    /**
     * Generate biomes for an unexplored planet.
     * Requires: exploration fleet, survey equipment.
     */
    SURVEY_PLANET("Survey Planet", "Map and analyze an unexplored planet"),

    /**
     * Upgrade infrastructure on a hex cell.
     * Requires: existing infrastructure, materials.
     */
    UPGRADE_INFRASTRUCTURE("Upgrade Infrastructure", "Improve existing infrastructure"),

    /**
     * Defend a settlement from threats.
     * Requires: military units, strategic position.
     */
    DEFEND_SETTLEMENT("Defend Settlement", "Protect a settlement from threats"),

    /**
     * Extract resources from a specific hex cell.
     * Requires: mining equipment, suitable terrain.
     */
    EXTRACT_RESOURCES("Extract Resources", "Mine or harvest resources from terrain");

    private final String label;
    private final String description;

    PlanetContractType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /** @return the human-readable label */
    public String getLabel() {
        return label;
    }

    /** @return the description of this contract type */
    public String getDescription() {
        return description;
    }
}

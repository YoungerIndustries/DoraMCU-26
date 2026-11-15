package com.yisy.modules.doramcu.hex;

/**
 * Types of game objects that can be requested for construction in a galaxy.
 * <p>
 * The Galaxy Overlord uses these types when submitting build requests to the
 * DoraMCU game module interface. Each type corresponds to a different construction
 * complexity and resource requirement.
 * </p>
 */
public enum BuildObjectType {

    /**
     * A single star within a solar system.
     */
    STAR,

    /**
     * A complete solar system with star, planets, moons, and celestial bodies.
     */
    SOLAR_SYSTEM,

    /**
     * A single planet within a solar system.
     */
    PLANET,

    /**
     * A moon orbiting a planet.
     */
    MOON,

    /**
     * An asteroid belt in a solar system.
     */
    ASTEROID_BELT,

    /**
     * A gas giant planet.
     */
    GAS_GIANT,

    /**
     * A nebula region spanning multiple sectors.
     */
    NEBULA,

    /**
     * A black hole celestial object.
     */
    BLACK_HOLE,

    /**
     * A comet object.
     */
    COMET,

    /**
     * A space station at a specific coordinate.
     */
    SPACE_STATION,

    /**
     * A hyperlane connection between two solar systems.
     */
    HYPERLANE,

    /**
     * An entire sector (10x10 subsector grid) with all its systems.
     */
    SECTOR,

    /**
     * An empire with capital planet and starting facilities.
     */
    EMPIRE,

    /**
     * A complete city on a planet surface, including buildings, roads, and
     * points of interest suitable for 3D adventuring.
     */
    CITY_3D,

    /**
     * A Younger Industries office at a capital or landing point.
     */
    YOUNGER_OFFICE,

    /**
     * A transport route connecting two locations.
     */
    TRANSPORT_ROUTE,

    /**
     * Planet surface terrain generation (biomes, hex cells).
     */
    PLANET_SURFACE,

    /**
     * A settlement on a planet surface.
     */
    SETTLEMENT,

    /**
     * An industry facility on a planet surface.
     */
    INDUSTRY,

    /**
     * Infrastructure (road, rail) connecting hex cells.
     */
    INFRASTRUCTURE
}

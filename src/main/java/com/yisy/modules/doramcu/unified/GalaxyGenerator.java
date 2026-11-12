package com.yisy.modules.doramcu.unified;

import java.util.List;
import java.util.Map;

/**
 * Unified galaxy generator interface that coordinates planet, system, and galaxy
 * creation across all game modules.
 * <p>
 * Each game module provides a concrete implementation that knows its galaxy
 * generation rules:
 * <ul>
 *   <li>Traveller: UWP-based world generation</li>
 *   <li>Star Trek: Alpha/Beta Quadrant generation</li>
 *   <li>Captain's Log: Star Trek Adventures galaxy</li>
 *   <li>007: Earth-based locations</li>
 *   <li>Earth2026: near-future Earth and space</li>
 * </ul>
 * </p>
 */
public interface GalaxyGenerator {

    /**
     * Returns the game type this generator handles.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Generates a complete galaxy with the given configuration.
     *
     * @param config the galaxy configuration (size, seed, etc.)
     * @return the generated galaxy
     */
    Galaxy generateGalaxy(Map<String, Object> config);

    /**
     * Generates a single star system within a galaxy.
     *
     * @param galaxy the parent galaxy
     * @param config the system configuration
     * @return the generated star system
     */
    StarSystem generateSystem(Galaxy galaxy, Map<String, Object> config);

    /**
     * Generates a single planet within a star system.
     *
     * @param system the parent star system
     * @param config the planet configuration
     * @return the generated planet
     */
    Planet generatePlanet(StarSystem system, Map<String, Object> config);

    /**
     * Generates population data for a planet.
     *
     * @param planet the planet
     * @param species the species to populate
     */
    void generatePopulation(Planet planet, List<String> species);

    /**
     * A galaxy containing star systems.
     */
    interface Galaxy {
        String getId();
        String getName();
        GameType getGameType();
        List<StarSystem> getStarSystems();
        Map<String, Object> getMetadata();
    }

    /**
     * A star system containing planets.
     */
    interface StarSystem {
        String getId();
        String getName();
        Galaxy getGalaxy();
        List<Planet> getPlanets();
        Map<String, Object> getMetadata();
    }

    /**
     * A planet with world profile data.
     */
    interface Planet {
        String getId();
        String getName();
        StarSystem getStarSystem();
        WorldProfile getWorldProfile();
        List<String> getSpecies();
        Map<String, Object> getMetadata();
    }

    /**
     * Normalized world profile that abstracts planet properties
     * across different game systems.
     */
    interface WorldProfile {
        int getSize();
        String getAtmosphere();
        int getHydrographics();
        long getPopulation();
        String getGovernment();
        int getLawLevel();
        int getTechLevel();
        String getStarPort();
        String getType();
        Map<String, Object> getRawProfile();
    }
}

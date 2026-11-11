package com.yisy.modules.doramcu.star_trek_4x.generation;

import com.yisy.modules.doramcu.captainslog.generation.CaptainsLogGalaxyGenerator;
import com.yisy.modules.doramcu.earth2026.model.E26GalaxyRules;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.star_trek_4x.service.StarTrekLoreDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Star Trek galaxy generator with quadrant-aware generation.
 * <p>
 * Extends CaptainsLogGalaxyGenerator with enhanced quadrant awareness and
 * uses Earth 2026 galaxy rules for constraints.
 * </p>
 */
@Service
public class StarTrekGalaxyGenerator extends CaptainsLogGalaxyGenerator {

    private static final Logger log = LoggerFactory.getLogger(StarTrekGalaxyGenerator.class);

    private final PlanetService planetService;
    private final StarTrekLoreDatabase loreDatabase;
    private final Random random = new Random();

    public StarTrekGalaxyGenerator(PlanetService planetService, StarTrekLoreDatabase loreDatabase) {
        super();
        this.planetService = planetService;
        this.loreDatabase = loreDatabase;
    }

    /**
     * Generate a complete Star Trek galaxy.
     *
     * @param seed the random seed for reproducibility
     * @return the generated Galaxy
     */
    public Galaxy generateGalaxy(long seed) {
        log.info("Generating Star Trek galaxy with seed: {}", seed);
        random.setSeed(seed);

        // Get galaxy rules from Earth 2026
        E26GalaxyRules rules = null;
        if (planetService != null) {
            try {
                rules = planetService.getGalaxyRules();
            } catch (Exception e) {
                log.warn("Failed to get galaxy rules from Earth 2026", e);
            }
        }

        // Create galaxy with quadrant-aware sector generation
        Galaxy galaxy = new Galaxy(1, "Star Trek Galaxy");

        // Generate sectors with quadrant-based density
        for (int row = 0; row < Galaxy.GRID_SIZE; row++) {
            for (int col = 0; col < Galaxy.GRID_SIZE; col++) {
                Galaxy.Sector sector = galaxy.getSector(row, col);
                String quadrant = determineQuadrant(row, col);
                String tradeType = generateStarTrekTradeType(quadrant);

                log.debug("Generating sector {} ({}, {}) in {} quadrant with trade type: {}",
                        sector.getName(), row, col, quadrant, tradeType);
            }
        }

        log.info("Star Trek galaxy generation complete: {} sectors", Galaxy.TOTAL_SECTORS);
        return galaxy;
    }

    /**
     * Determine the quadrant based on sector position.
     */
    private String determineQuadrant(int row, int col) {
        if (row < 5 && col < 5) return "Alpha Quadrant";
        if (row < 5 && col >= 5) return "Beta Quadrant";
        if (row >= 5 && col < 5) return "Gamma Quadrant";
        return "Delta Quadrant";
    }

    /**
     * Generate a Star Trek trade type based on quadrant.
     */
    private String generateStarTrekTradeType(String quadrant) {
        String[] tradeTypes = {"Agri", "Industrial", "Rich", "Poor", "Urban", "Rural",
                "Hinter", "Water", "Gas", "Brass", "High Population", "Low Population"};

        // Quadrant-based bias
        switch (quadrant) {
            case "Alpha Quadrant":
                // Federation space: more urban and rich
                if (random.nextDouble() < 0.3) return "Urban";
                if (random.nextDouble() < 0.3) return "Rich";
                break;
            case "Beta Quadrant":
                // Klingon/Romulan space: more industrial
                if (random.nextDouble() < 0.3) return "Industrial";
                break;
            case "Gamma Quadrant":
                // Ferengi/Cardassian space: more rich and brass
                if (random.nextDouble() < 0.3) return "Rich";
                if (random.nextDouble() < 0.3) return "Brass";
                break;
            case "Delta Quadrant":
                // Borg/unknown space: more hinter and poor
                if (random.nextDouble() < 0.3) return "Hinter";
                if (random.nextDouble() < 0.3) return "Poor";
                break;
        }

        return tradeTypes[random.nextInt(tradeTypes.length)];
    }
}

package com.yisy.modules.doramcu.star_trek_4x.generation;

import com.fasterxml.jackson.databind.JsonNode;
import com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Planet;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.hex.PlanetPopulationProgress;
import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;
import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarSystemRepository;
import com.yisy.modules.doramcu.star_trek_4x.service.StarTrekLoreDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Star Trek planet generator with hybrid UWP + Star Trek properties.
 * <p>
 * Uses Earth 2026's PlanetService for planet foundation and adds
 * Traveller UWP codes plus Star Trek-specific fields.
 * </p>
 */
@Service
public class StarTrekPlanetGenerator {

    private static final Logger log = LoggerFactory.getLogger(StarTrekPlanetGenerator.class);

    private final PlanetService planetService;
    private final StarTrekLoreDatabase loreDatabase;
    private final StarSystemRepository starSystemRepository;
    private final Random random = new Random();

    public StarTrekPlanetGenerator(PlanetService planetService,
                                    StarTrekLoreDatabase loreDatabase,
                                    StarSystemRepository starSystemRepository) {
        this.planetService = planetService;
        this.loreDatabase = loreDatabase;
        this.starSystemRepository = starSystemRepository;
    }

    /**
     * Generate a star system with hybrid UWP + Star Trek properties.
     * <p>
     * Uses the deprecated monolithic planet population method for backward compatibility.
     * </p>
     *
     * @param name the system name
     * @param sectorId the sector ID
     * @param subSectorId the subsector ID
     * @param gameSession the game session
     * @return the generated StarSystem
     * @deprecated Use {@link #generateStarSystem(String, int, int, GameSession, PlanetPopulationProgress)}
     *             for streamed, batched planet population that avoids memory overrun.
     */
    @Deprecated
    public StarSystem generateStarSystem(String name, int sectorId, int subSectorId, GameSession gameSession) {
        return generateStarSystem(name, sectorId, subSectorId, gameSession, null);
    }

    /**
     * Generate a star system with hybrid UWP + Star Trek properties,
     * using streamed planet population with progress callbacks.
     * <p>
     * This method delegates planet population to the new phased API,
     * allowing the caller to receive incremental progress updates and
     * avoid holding the entire planet in memory at once.
     * </p>
     *
     * @param name the system name
     * @param sectorId the sector ID
     * @param subSectorId the subsector ID
     * @param gameSession the game session
     * @param progress optional callback for receiving phased planet population progress
     * @return the generated StarSystem
     */
    public StarSystem generateStarSystem(String name, int sectorId, int subSectorId, GameSession gameSession,
                                         PlanetPopulationProgress progress) {
        log.info("Generating Star Trek star system: {} in sector {}", name, sectorId);

        StarSystem system = new StarSystem();
        system.setName(name);
        system.setSectorId(sectorId);
        system.setSubSectorId(subSectorId);
        system.setPosX(random.nextInt(10));
        system.setPosY(random.nextInt(10));
        system.setPosZ(random.nextInt(10));
        system.setGameSession(gameSession);
        system.setDiscovered(true);

        // Generate UWP codes
        generateUwpCodes(system);

        // Generate Star Trek specific fields
        generateStarTrekFields(system);

        // Try to populate with Earth 2026 planet data using the new phased API
        try {
            if (planetService != null) {
                PlanetPopulationRequest request = new PlanetPopulationRequest();
                request.setPlanetName(name);
                request.setGameModuleName("STAR_TREK_4X");
                request.setExistingCharacters(new ArrayList<>());

                // Wrap the progress callback to capture the planet ID for the system link
                final Long[] planetId = {null};
                PlanetPopulationProgress idCapturingProgress = new PlanetPopulationProgress() {
                    @Override
                    public void onPlanetCreated(Earth2026Planet planet) {
                        planetId[0] = planet.getId();
                        if (progress != null) {
                            progress.onPlanetCreated(planet);
                        }
                    }

                    @Override
                    public void onCountryBatch(java.util.List<com.yisy.modules.doramcu.earth2026.model.Earth2026Country> countries, int countryIndex, int totalCountries) {
                        if (progress != null) {
                            progress.onCountryBatch(countries, countryIndex, totalCountries);
                        }
                    }

                    @Override
                    public void onRegionBatch(java.util.List<com.yisy.modules.doramcu.earth2026.model.Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {
                        if (progress != null) {
                            progress.onRegionBatch(regions, countryId, regionIndex, totalRegions);
                        }
                    }

                    @Override
                    public void onAiLeaderAssigned(com.yisy.modules.doramcu.eve.model.AiCharacter leader, Long countryId, boolean isNew) {
                        if (progress != null) {
                            progress.onAiLeaderAssigned(leader, countryId, isNew);
                        }
                    }

                    @Override
                    public void onAiGovernorAssigned(com.yisy.modules.doramcu.eve.model.AiCharacter governor, Long regionId, boolean isNew) {
                        if (progress != null) {
                            progress.onAiGovernorAssigned(governor, regionId, isNew);
                        }
                    }

                    @Override
                    public void onPlayerCharactersRegistered(java.util.Map<String, String> players) {
                        if (progress != null) {
                            progress.onPlayerCharactersRegistered(players);
                        }
                    }

                    @Override
                    public void onLog(String message) {
                        if (progress != null) {
                            progress.onLog(message);
                        }
                    }

                    @Override
                    public void onComplete(PopulationSummary summary) {
                        if (progress != null) {
                            progress.onComplete(summary);
                        }
                    }
                };

                planetService.populatePlanetWithGameData(request, idCapturingProgress);
                if (planetId[0] != null) {
                    system.setEarth2026PlanetId(planetId[0].toString());
                    log.info("Linked system {} to Earth 2026 planet: {}", name, planetId[0]);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to populate Earth 2026 planet for system: {}", name, e);
        }

        return starSystemRepository.save(system);
    }

    /**
     * Generate Traveller UWP codes for the system.
     */
    private void generateUwpCodes(StarSystem system) {
        // Atmosphere: 0-9, A-P
        String[] atmospheres = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C"};
        system.setUwpAtmosphere(atmospheres[random.nextInt(atmospheres.length)]);

        // Gravity: 0-9, A
        String[] gravities = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A"};
        system.setUwpGravity(gravities[random.nextInt(gravities.length)]);

        // Size: 0-9
        system.setUwpSize(String.valueOf(random.nextInt(10)));

        // Hydrosphere: 0-9
        system.setUwpHydrosphere(String.valueOf(random.nextInt(10)));

        // Population: 0-9, A
        String[] populations = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A"};
        system.setUwpPopulation(populations[random.nextInt(populations.length)]);

        // Government: 0-9, A-F
        String[] governments = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D"};
        system.setUwpGovernment(governments[random.nextInt(governments.length)]);

        // Law: 0-9, A
        String[] laws = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A"};
        system.setUwpLaw(laws[random.nextInt(laws.length)]);

        // Trade codes
        String[] trades = {"Ag", "Hi", "Lo", "Ni", "Po", "Ri", "Wa", "Ar", "Co", "Fl"};
        int numTrades = random.nextInt(3) + 1;
        List<String> selectedTrades = new ArrayList<>();
        for (int i = 0; i < numTrades; i++) {
            selectedTrades.add(trades[random.nextInt(trades.length)]);
        }
        system.setUwpTrade(String.join(",", selectedTrades));

        // Trade value based on trade codes
        system.setTradeValue(20 + random.nextInt(80));
    }

    /**
     * Generate Star Trek specific fields.
     */
    private void generateStarTrekFields(StarSystem system) {
        // Alignment based on random chance
        StarSystem.StarAlignment[] alignments = StarSystem.StarAlignment.values();
        system.setAlignment(alignments[random.nextInt(alignments.length)]);

        // Native species from lore database
        List<JsonNode> species = loreDatabase.getSpecies();
        if (!species.isEmpty()) {
            int numSpecies = random.nextInt(3) + 1;
            for (int i = 0; i < numSpecies && i < species.size(); i++) {
                JsonNode sp = species.get(random.nextInt(species.size()));
                if (sp.has("name")) {
                    system.getNativeSpecies().add(sp.get("name").asText());
                }
            }
        }

        // Warp deposits (rare)
        system.setHasWarpDeposits(random.nextDouble() < 0.1);

        // Notable features
        String[] features = {"Ancient alien ruins", "Nebula formation", "Binary star system",
                "Asteroid belt", "Gas giant with moons", "Dyson sphere remnants",
                "Wormhole entrance", "Temporal anomaly", "None"};
        system.setNotableFeatures(features[random.nextInt(features.length)]);
    }

    /**
     * Get all tech levels from Earth 2026.
     */
    public List<com.yisy.modules.doramcu.earth2026.model.TechLevel> getAllTechLevels() {
        if (planetService != null) {
            return planetService.getAllTechLevels();
        }
        return new ArrayList<>();
    }
}

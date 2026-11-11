package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.dto.JoinSessionResponse;
import com.yisy.modules.doramcu.earth2026.dto.PlanetCharacterInfo;
import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.repository.*;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.PlanetPopulationProgress;
import com.yisy.modules.doramcu.model.GameSession;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.service.GalaxyMetricsService;
import com.yisy.modules.doramcu.service.GameSessionService;
import com.yisy.modules.doramcu.unified.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing Earth 2026 gameplay.
 * <p>
 * Handles character creation, location management, scenario progression,
 * and galaxy transitions for the Earth 2026 hub.
 * </p>
 * <p>
 * Implements {@link PlanetService} so that other game modules can consume
 * Earth-based operations without depending on Earth 2026 directly.
 * </p>
 */
@Service
public class Earth2026Service implements PlanetService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026Service.class);

    private final Earth2026CharacterRepository characterRepository;
    private final Earth2026LocationRepository locationRepository;
    private final Earth2026ScenarioRepository scenarioRepository;
    private final Earth2026StructureRepository structureRepository;
    private final Earth2026CountryRepository countryRepository;
    private final Earth2026RegionRepository regionRepository;
    private final Earth2026EconomyRepository economyRepository;
    private final Earth2026JobRepository jobRepository;
    private final Earth2026ContractRepository contractRepository;
    private final Earth2026PlanetRepository planetRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final Earth2026RulesDatabaseService rulesDatabaseService;
    private final AiPlayerService aiPlayerService;
    private final GameSessionService gameSessionService;
    private final GalaxyEventService galaxyEventService;
    private final GalaxyMetricsService galaxyMetricsService;

    public Earth2026Service(Earth2026CharacterRepository characterRepository,
                            Earth2026LocationRepository locationRepository,
                            Earth2026ScenarioRepository scenarioRepository,
                            Earth2026StructureRepository structureRepository,
                            Earth2026CountryRepository countryRepository,
                            Earth2026RegionRepository regionRepository,
                            Earth2026EconomyRepository economyRepository,
                            Earth2026JobRepository jobRepository,
                            Earth2026ContractRepository contractRepository,
                            Earth2026PlanetRepository planetRepository,
                            AiCharacterRepository aiCharacterRepository,
                            Earth2026RulesDatabaseService rulesDatabaseService,
                            AiPlayerService aiPlayerService,
                            GameSessionService gameSessionService,
                            GalaxyEventService galaxyEventService,
                            GalaxyMetricsService galaxyMetricsService) {
        this.characterRepository = characterRepository;
        this.locationRepository = locationRepository;
        this.scenarioRepository = scenarioRepository;
        this.structureRepository = structureRepository;
        this.countryRepository = countryRepository;
        this.regionRepository = regionRepository;
        this.economyRepository = economyRepository;
        this.jobRepository = jobRepository;
        this.contractRepository = contractRepository;
        this.planetRepository = planetRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.rulesDatabaseService = rulesDatabaseService;
        this.aiPlayerService = aiPlayerService;
        this.gameSessionService = gameSessionService;
        this.galaxyEventService = galaxyEventService;
        this.galaxyMetricsService = galaxyMetricsService;
    }

    /**
     * Creates a new Earth 2026 character.
     *
     * @param name the character's name
     * @param age the character's age
     * @param occupation the character's occupation
     * @param playerId the owning player (User) ID
     * @return the created character
     */
    @Transactional
    public Earth2026Character createCharacter(String name, int age, String occupation, Long playerId) {
        Earth2026Character character = new Earth2026Character(name, age, occupation);
        character.setPlayerId(playerId);
        character.setCreatedAt(LocalDateTime.now());
        character.setUpdatedAt(LocalDateTime.now());
        character.setEarthLocation("San Diego, CA");
        character = characterRepository.save(character);
        log.info("Created Earth 2026 character: {} (age={}, occupation={}, playerId={})", name, age, occupation, playerId);
        return character;
    }

    /**
     * Creates a new Earth 2026 character without a player association (for AI characters).
     *
     * @param name the character's name
     * @param age the character's age
     * @param occupation the character's occupation
     * @return the created character
     */
    @Transactional
    public Earth2026Character createCharacter(String name, int age, String occupation) {
        return createCharacter(name, age, occupation, null);
    }

    /**
     * Gets a character by ID.
     *
     * @param id the character ID
     * @return the character if found
     */
    @Override
    public Optional<Earth2026Character> getCharacter(Long id) {
        return characterRepository.findById(id);
    }

    /**
     * Lists all characters that have not yet transitioned to another galaxy.
     *
     * @return list of characters still on Earth
     */
    @Override
    public List<Earth2026Character> getCharactersOnEarth() {
        return characterRepository.findByHasTransitionedFalse();
    }

    /**
     * List all characters currently at a specific Earth location.
     *
     * @param locationName the location name
     * @return list of characters at that location
     */
    @Override
    public List<Earth2026Character> getCharactersAtLocation(String locationName) {
        return characterRepository.findByEarthLocation(locationName);
    }

    /**
     * Updates a character's location on Earth.
     *
     * @param characterId the character ID
     * @param location the new location
     * @return the updated character
     */
    @Transactional
    public Optional<Earth2026Character> moveCharacter(Long characterId, String location) {
        return characterRepository.findById(characterId)
                .map(character -> {
                    character.setEarthLocation(location);
                    character.setUpdatedAt(LocalDateTime.now());
                    characterRepository.save(character);
                    log.info("Character {} moved to {}", character.getName(), location);
                    return character;
                });
    }

    /**
     * Sets the target galaxy for a character, preparing for transition.
     *
     * @param characterId the character ID
     * @param targetGalaxy the target galaxy name
     * @return the updated character
     */
    @Transactional
    public Optional<Earth2026Character> setTargetGalaxy(Long characterId, String targetGalaxy) {
        return characterRepository.findById(characterId)
                .map(character -> {
                    if (character.isHasTransitioned()) {
                        throw new IllegalStateException("Character has already transitioned to another galaxy");
                    }
                    character.setTargetGalaxy(targetGalaxy);
                    character.setUpdatedAt(LocalDateTime.now());
                    characterRepository.save(character);
                    log.info("Character {} selected target galaxy: {}", character.getName(), targetGalaxy);
                    return character;
                });
    }

    /**
     * Completes the galaxy transition for a character.
     * <p>
     * This is the bridge method that connects Earth 2026 character creation to the
     * galaxy game. It performs four steps:
     * <ol>
     *   <li>Validates the character has a target galaxy selected</li>
     *   <li>Creates an AiCharacter record in the galaxy linked to the character</li>
     *   <li>Joins the character to the active galaxy game session as a player</li>
     *   <li>Marks the character as transitioned</li>
     * </ol>
     * </p>
     * <p>
     * The character's six ability scores (STR, DEX, CON, INT, WIS, CHA) are used to
     * determine the starting difficulty and role in the galaxy game:
     * <ul>
     *   <li>High INT → Advisor role (strategic decisions)</li>
     *   <li>High STR → General role (combat focus)</li>
     *   <li>High CHA → Leader role (diplomacy focus)</li>
     *   <li>Otherwise → Explorer role (balanced)</li>
     * </ul>
     * </p>
     *
     * @param characterId the character ID
     * @return the updated character with transition flag set
     */
    @Transactional
    public Optional<Earth2026Character> completeTransition(Long characterId) {
        return characterRepository.findById(characterId)
                .map(character -> {
                    if (character.isHasTransitioned()) {
                        throw new IllegalStateException("Character has already transitioned");
                    }
                    if (character.getTargetGalaxy() == null) {
                        throw new IllegalStateException("No target galaxy selected");
                    }

                    String targetGalaxy = character.getTargetGalaxy();
                    String characterName = character.getName();

                    // Step 1: Determine the character's role based on ability scores
                    String role = determineRoleFromScores(character);
                    log.info("Character {} transitioning with role: {}", characterName, role);

                    // Step 2: Determine difficulty based on character level and stats
                    AiPlayerService.Difficulty difficulty = determineDifficulty(character);
                    log.info("Character {} transitioning with difficulty: {}", characterName, difficulty);

                    // Step 3: Create the AiCharacter record in the galaxy
                    AiCharacter aiCharacter = createAiCharacterForTransition(character, role, targetGalaxy);
                    aiCharacterRepository.save(aiCharacter);
                    log.info("Created AiCharacter {} for galaxy {}", aiCharacter.getName(), targetGalaxy);

                    // Step 4: Join the active galaxy game session
                    try {
                        GameSession defaultSession = gameSessionService.getDefaultSession();
                        if (defaultSession != null) {
                            JoinSessionResponse joinResponse = aiPlayerService.createAiPlayer(
                                    defaultSession.getSessionId(), characterName, difficulty);
                            log.info("Character {} joined galaxy session {} as player {}",
                                    characterName, joinResponse.getSessionId(), joinResponse.getPlayerId());
                        } else {
                            log.warn("No active galaxy game session found for character {} — transition flag set but not joined to session",
                                    characterName);
                        }
                    } catch (Exception e) {
                        log.error("Failed to join character {} to galaxy session: {}",
                                characterName, e.getMessage(), e);
                        // Still mark as transitioned — the character record exists and can be joined later
                    }

                    // Step 5: Mark the character as transitioned
                    character.setHasTransitioned(true);
                    character.setUpdatedAt(LocalDateTime.now());
                    characterRepository.save(character);
                    log.info("Character {} transitioned to galaxy: {}", characterName, targetGalaxy);

                    // Publish CHARACTER_TRANSITIONED event
                    galaxyEventService.publish(GalaxyEventType.CHARACTER_TRANSITIONED, targetGalaxy, null,
                            "characterId", String.valueOf(character.getId()),
                            "characterName", characterName,
                            "role", role,
                            "difficulty", difficulty.name());

                    // Increment Prometheus transition counter
                    galaxyMetricsService.incrementCharacterTransitioned();

                    return character;
                });
    }

    /**
     * Determines the galaxy role based on the character's six ability scores.
     * <p>
     * The highest score determines the role:
     * <ul>
     *   <li>INT highest → Advisor (strategic decisions)</li>
     *   <li>STR highest → General (combat focus)</li>
     *   <li>CHA highest → Leader (diplomacy focus)</li>
     *   <li>Otherwise → Explorer (balanced)</li>
     * </ul>
     * </p>
     *
     * @param character the Earth 2026 character
     * @return the role string for the galaxy AiCharacter
     */
    private String determineRoleFromScores(Earth2026Character character) {
        int str = character.getStrScore();
        int dex = character.getDexScore();
        int con = character.getConScore();
        int intel = character.getIntScore();
        int wis = character.getWisScore();
        int cha = character.getChaScore();

        int max = Math.max(Math.max(Math.max(str, dex), Math.max(con, Math.max(intel, wis))), cha);

        if (intel == max) {
            return "Advisor";
        } else if (str == max) {
            return "General";
        } else if (cha == max) {
            return "Leader";
        } else {
            return "Explorer";
        }
    }

    /**
     * Determines the AI difficulty based on the character's level and total ability scores.
     * <p>
     * Higher levels and total scores produce harder difficulty:
     * <ul>
     *   <li>Level >= 5 or total scores >= 72 → HARD</li>
     *   <li>Level >= 3 or total scores >= 60 → MEDIUM</li>
     *   <li>Otherwise → EASY</li>
     * </ul>
     * </p>
     *
     * @param character the Earth 2026 character
     * @return the difficulty level for the galaxy game
     */
    private AiPlayerService.Difficulty determineDifficulty(Earth2026Character character) {
        int totalScores = character.getStrScore() + character.getDexScore() +
                character.getConScore() + character.getIntScore() +
                character.getWisScore() + character.getChaScore();
        int level = character.getLevel();

        if (level >= 5 || totalScores >= 72) {
            return AiPlayerService.Difficulty.HARD;
        } else if (level >= 3 || totalScores >= 60) {
            return AiPlayerService.Difficulty.MEDIUM;
        } else {
            return AiPlayerService.Difficulty.EASY;
        }
    }

    /**
     * Creates an AiCharacter record for a transitioning Earth 2026 character.
     * <p>
     * The AiCharacter is linked to the character via linkedPlayerId and carries
     * the character's name, determined role, and target galaxy as the empire name.
     * </p>
     *
     * @param character the Earth 2026 character
     * @param role the determined role (Advisor, General, Leader, Explorer)
     * @param targetGalaxy the target galaxy name
     * @return the new AiCharacter entity (not yet saved)
     */
    private AiCharacter createAiCharacterForTransition(Earth2026Character character, String role, String targetGalaxy) {
        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(character.getName());
        aiCharacter.setRole(role);
        aiCharacter.setEmpireName(targetGalaxy);

        // Link to the Earth 2026 character via player ID
        if (character.getPlayerId() != null) {
            aiCharacter.setLinkedPlayerId(character.getPlayerId().toString());
        }

        // Map ability scores to leadership rating (1-10 scale)
        int leadership = Math.max(1, Math.min(10, (character.getIntScore() + character.getWisScore()) / 4));
        aiCharacter.setLeadershipRating(leadership);

        // Set experience based on Earth 2026 XP
        aiCharacter.setTotalExperience(character.getExperiencePoints());

        // Mark as active and not hirable (player-controlled)
        aiCharacter.setActive(true);
        aiCharacter.setHirable(false);
        aiCharacter.setHiringStatus(AiCharacter.HiringStatus.UNAVAILABLE);

        // Build description from character background
        String description = character.getBackground();
        if (description == null || description.isBlank()) {
            description = character.getName() + " — " + character.getOccupation() +
                    " from Earth 2026, transitioning to " + targetGalaxy + " as " + role;
        }
        aiCharacter.setDescription(description);

        return aiCharacter;
    }

    /**
     * Lists all available scenarios for a character's level.
     *
     * @param level the character's level
     * @return list of available scenarios
     */
    @Override
    public List<Earth2026Scenario> getAvailableScenarios(int level) {
        return scenarioRepository.findByMinLevelLessThanEqualAndActiveTrue(level);
    }

    /**
     * Starts a scenario.
     *
     * @param scenarioId the scenario ID
     * @return the updated scenario
     */
    @Transactional
    public Optional<Earth2026Scenario> startScenario(Long scenarioId) {
        return scenarioRepository.findById(scenarioId)
                .map(scenario -> {
                    if (scenario.getStatus() != Earth2026Scenario.ScenarioStatus.AVAILABLE) {
                        throw new IllegalStateException("Scenario is not available: " + scenario.getStatus());
                    }
                    scenario.setStatus(Earth2026Scenario.ScenarioStatus.IN_PROGRESS);
                    scenario.setStartedAt(LocalDateTime.now());
                    scenario.setUpdatedAt(LocalDateTime.now());
                    scenarioRepository.save(scenario);
                    log.info("Scenario '{}' started", scenario.getName());
                    return scenario;
                });
    }

    /**
     * Completes a scenario.
     *
     * @param scenarioId the scenario ID
     * @param success whether the scenario was completed successfully
     * @return the updated scenario
     */
    @Transactional
    public Optional<Earth2026Scenario> completeScenario(Long scenarioId, boolean success) {
        return scenarioRepository.findById(scenarioId)
                .map(scenario -> {
                    scenario.setStatus(success ? Earth2026Scenario.ScenarioStatus.COMPLETED : Earth2026Scenario.ScenarioStatus.FAILED);
                    scenario.setCompletedAt(LocalDateTime.now());
                    scenario.setUpdatedAt(LocalDateTime.now());
                    scenarioRepository.save(scenario);
                    log.info("Scenario '{}' completed: {}", scenario.getName(), success ? "success" : "failed");
                    return scenario;
                });
    }

    /**
     * Awards XP to a character.
     *
     * @param characterId the character ID
     * @param xp the XP to award
     * @return the updated character
     */
    @Transactional
    public Optional<Earth2026Character> awardXp(Long characterId, int xp) {
        return characterRepository.findById(characterId)
                .map(character -> {
                    character.setExperiencePoints(character.getExperiencePoints() + xp);
                    // Check for level up
                    int newLevel = calculateLevel(character.getExperiencePoints());
                    if (newLevel > character.getLevel()) {
                        character.setLevel(newLevel);
                        character.setMaxHitPoints(character.getMaxHitPoints() + (character.getConScore() - 10) / 2 + 1);
                        character.setHitPoints(character.getMaxHitPoints());
                        log.info("Character {} leveled up to {}", character.getName(), newLevel);
                    }
                    character.setUpdatedAt(LocalDateTime.now());
                    characterRepository.save(character);
                    return character;
                });
    }

    /**
     * Calculates the level based on XP.
     * Simple formula: level = 1 + (XP / 100).
     */
    private int calculateLevel(int xp) {
        return 1 + (xp / 100);
    }

    /**
     * Lists all locations.
     *
     * @return list of all locations
     */
    @Override
    public List<Earth2026Location> getAllLocations() {
        return locationRepository.findByActiveTrue();
    }

    /**
     * Lists all portal locations (doors to other galaxies).
     *
     * @return list of portal locations
     */
    @Override
    public List<Earth2026Location> getPortalLocations() {
        return locationRepository.findByHasPortalTrue();
    }

    /**
     * Gets a location by ID.
     *
     * @param id the location ID
     * @return the location if found
     */
    @Override
    public Optional<Earth2026Location> getLocation(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Find locations near the given coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @param radiusKm  the search radius in kilometers
     * @return list of nearby locations
     */
    @Override
    public List<Earth2026Location> getLocationsNearby(double latitude, double longitude, double radiusKm) {
        return locationRepository.findByActiveTrue().stream()
                .filter(loc -> loc.getLatitude() != null && loc.getLongitude() != null)
                .filter(loc -> haversineDistance(latitude, longitude, loc.getLatitude(), loc.getLongitude()) <= radiusKm)
                .toList();
    }

    /**
     * Calculate the distance between two points on Earth using the Haversine formula.
     *
     * @param lat1 latitude of point 1
     * @param lon1 longitude of point 1
     * @param lat2 latitude of point 2
     * @param lon2 longitude of point 2
     * @return distance in kilometers
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Enables supernatural mode for a character (for Cthulhu/D&D extensions).
     *
     * @param characterId the character ID
     * @return the updated character
     */
    @Transactional
    public Optional<Earth2026Character> enableSupernatural(Long characterId) {
        return characterRepository.findById(characterId)
                .map(character -> {
                    character.setSupernaturalEnabled(true);
                    character.setSanityScore(100);
                    character.setUpdatedAt(LocalDateTime.now());
                    characterRepository.save(character);
                    log.info("Supernatural mode enabled for character {}", character.getName());
                    return character;
                });
    }

    /**
     * Reduces a character's sanity (for Cthulhu-style scenarios).
     *
     * @param characterId the character ID
     * @param amount the amount to reduce sanity by
     * @return the updated character
     */
    @Transactional
    public Optional<Earth2026Character> reduceSanity(Long characterId, int amount) {
        return characterRepository.findById(characterId)
                .map(character -> {
                    if (!character.isSupernaturalEnabled()) {
                        throw new IllegalStateException("Supernatural mode is not enabled");
                    }
                    Integer currentSanity = character.getSanityScore();
                    if (currentSanity == null) {
                        currentSanity = 100;
                    }
                    int newSanity = Math.max(0, currentSanity - amount);
                    character.setSanityScore(newSanity);
                    character.setUpdatedAt(LocalDateTime.now());
                    characterRepository.save(character);
                    log.info("Character {} sanity reduced by {} to {}", character.getName(), amount, newSanity);
                    if (newSanity == 0) {
                        log.warn("Character {} has lost all sanity!", character.getName());
                    }
                    return character;
                });
    }

    // ------------------------------------------------------------------
    // PlanetService: Stat mapping
    // ------------------------------------------------------------------

    /**
     * Map Earth 2026 ability scores to a target game's stat system.
     *
     * @param character the Earth 2026 character
     * @param gameType  the target game type
     * @return a map of game-specific stat names to values
     */
    @Override
    public Map<String, Integer> mapStatsToGame(Earth2026Character character, GameType gameType) {
        Map<String, Integer> stats = new HashMap<>();
        switch (gameType) {
            case DND_5E:
            case EARTH_2026:
                // Direct 1:1 mapping — same 6 ability scores
                stats.put("STR", character.getStrScore());
                stats.put("DEX", character.getDexScore());
                stats.put("CON", character.getConScore());
                stats.put("INT", character.getIntScore());
                stats.put("WIS", character.getWisScore());
                stats.put("CHA", character.getChaScore());
                break;

            case CAPTAINS_LOG:
                // Star Trek mapping: STR→Strength, DEX→Fitness, INT→Reason, WIS→Insight, CHA→Presence, CON→Constitution
                stats.put("Strength", character.getStrScore());
                stats.put("Fitness", character.getDexScore());
                stats.put("Reason", character.getIntScore());
                stats.put("Insight", character.getWisScore());
                stats.put("Presence", character.getChaScore());
                stats.put("Constitution", character.getConScore());
                break;

            case TRAVELLER:
                // Traveller D6 stats derived from Earth 2026 scores (scale 1-20 → 2-18 → 2D6+modifier)
                stats.put("STR", mapToTravellerStat(character.getStrScore()));
                stats.put("DEX", mapToTravellerStat(character.getDexScore()));
                stats.put("END", mapToTravellerStat(character.getConScore()));
                stats.put("INT", mapToTravellerStat(character.getIntScore()));
                stats.put("EDU", mapToTravellerStat(character.getWisScore()));
                stats.put("SOC", mapToTravellerStat(character.getChaScore()));
                break;

            case DORAMCU_4X:
                // 4X empire strategy: simplified to leadership, diplomacy, industry, science
                stats.put("Leadership", (character.getStrScore() + character.getChaScore()) / 2);
                stats.put("Diplomacy", (character.getChaScore() + character.getWisScore()) / 2);
                stats.put("Industry", (character.getStrScore() + character.getDexScore()) / 2);
                stats.put("Science", (character.getIntScore() + character.getWisScore()) / 2);
                break;

            case THIRD_REICH:
                // WWII combat: command, tactics, logistics
                stats.put("Command", character.getChaScore());
                stats.put("Tactics", character.getIntScore());
                stats.put("Logistics", character.getWisScore());
                break;

            default:
                // Fallback to direct mapping
                stats.put("STR", character.getStrScore());
                stats.put("DEX", character.getDexScore());
                stats.put("CON", character.getConScore());
                stats.put("INT", character.getIntScore());
                stats.put("WIS", character.getWisScore());
                stats.put("CHA", character.getChaScore());
                break;
        }
        log.debug("Mapped stats for {} to {}: {}", character.getName(), gameType.getDisplayName(), stats);
        return stats;
    }

    /**
     * Map an Earth 2026 score (1-20) to a Traveller D6 stat (2-18).
     */
    private int mapToTravellerStat(int earthScore) {
        // Scale 1-20 to 2-18 range
        return Math.max(2, Math.min(18, 2 + (earthScore - 1) * 16 / 19));
    }

    // ------------------------------------------------------------------
    // PlanetService: Tech Level operations
    // ------------------------------------------------------------------

    @Override
    public List<TechLevel> getAllTechLevels() {
        return List.of(TechLevel.values());
    }

    @Override
    public Optional<TechLevel> getTechLevel(int level) {
        try {
            return Optional.of(TechLevel.fromLevel(level));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<TimePeriodTemplate> getAllTimePeriodTemplates() {
        return List.of(TimePeriodTemplate.values());
    }

    @Override
    public Optional<TimePeriodTemplate> getTimePeriodTemplate(String name) {
        try {
            return Optional.of(TimePeriodTemplate.fromName(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------
    // PlanetService: Structure operations
    // ------------------------------------------------------------------

    @Override
    public List<Earth2026Structure> getStructuresAtLocation(Long locationId) {
        return structureRepository.findByLocationIdAndActiveTrue(locationId);
    }

    @Override
    public Optional<Earth2026Structure> getStructure(Long id) {
        return structureRepository.findById(id);
    }

    // ------------------------------------------------------------------
    // PlanetService: Battle Environment operations
    // ------------------------------------------------------------------

    @Override
    public Earth2026BattleEnvironment buildBattleEnvironment(Long locationId,
                                                               String timePeriod,
                                                               String terrainType,
                                                               String weather) {
        // 1. Get the location
        Earth2026Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));

        // 2. Get the time period template
        TimePeriodTemplate template = TimePeriodTemplate.fromName(timePeriod);

        // 3. Get the base tech level from the template
        int baseTechLevel = template.getBaseTechLevel().getLevel();

        // 4. Parse weather condition
        Earth2026BattleEnvironment.WeatherCondition weatherCondition;
        try {
            weatherCondition = Earth2026BattleEnvironment.WeatherCondition.valueOf(
                    weather != null ? weather.toUpperCase() : "CLEAR");
        } catch (IllegalArgumentException e) {
            weatherCondition = Earth2026BattleEnvironment.WeatherCondition.CLEAR;
        }

        // 5. Get structures at this location
        List<Earth2026Structure> structures = structureRepository.findByLocationIdAndActiveTrue(locationId);
        List<Earth2026BattleEnvironment.StructureInfo> structureInfos = structures.stream()
                .map(s -> new Earth2026BattleEnvironment.StructureInfo(
                        s.getId(), s.getName(), s.getStructureType(), s.getTechLevel(),
                        s.getDefenseRating(), s.getMaxCapacity(), s.isProvidesCover(),
                        s.getCoverBonus(), s.isStrategic()))
                .toList();

        // 6. Get available units for this tech level
        List<Earth2026BattleEnvironment.UnitTypeInfo> availableUnits =
                getAvailableUnitsForTechLevel(baseTechLevel);

        // 7. Determine terrain type
        String resolvedTerrain = terrainType != null ? terrainType.toUpperCase() : "URBAN";

        log.info("Built battle environment for location {} ({}), period={}, terrain={}, weather={}",
                locationId, location.getName(), timePeriod, resolvedTerrain, weatherCondition);

        return new Earth2026BattleEnvironment(
                location.getId(), location.getName(), location.getCity(),
                location.getCountry(), location.getLatitude(), location.getLongitude(),
                baseTechLevel, template.getName(), resolvedTerrain,
                weatherCondition, structureInfos, availableUnits);
    }

    @Override
    public Earth2026BattleEnvironment buildBattleEnvironment(Long locationId, String timePeriod) {
        return buildBattleEnvironment(locationId, timePeriod, "URBAN", "CLEAR");
    }

    @Override
    public List<Earth2026BattleEnvironment.UnitTypeInfo> getAvailableUnitsForTechLevel(int techLevel) {
        List<Earth2026BattleEnvironment.UnitTypeInfo> units = new ArrayList<>();

        // TL0-TL1: Primitive units
        if (techLevel >= 0) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Infantry", "Basic infantry with primitive weapons", 0, 1, 1, 3));
        }

        // TL1: Cavalry
        if (techLevel >= 1) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Cavalry", "Animal-mounted warriors", 1, 2, 1, 5));
        }

        // TL2: Knights
        if (techLevel >= 2) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Knights", "Armored cavalry", 2, 3, 3, 4));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Archers", "Ranged infantry with bows", 2, 2, 1, 3));
        }

        // TL3: Musketeers
        if (techLevel >= 3) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Musketeers", "Gunpowder infantry", 3, 3, 1, 3));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Artillery", "Early cannons", 3, 5, 1, 1));
        }

        // TL4: Industrial era units (WWII)
        if (techLevel >= 4) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Riflemen", "Bolt-action rifle infantry", 4, 3, 2, 3));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Machine Gun", "Machine gun team", 4, 4, 2, 2));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Tanks", "Armored tanks", 4, 6, 5, 4));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Artillery", "Field artillery", 4, 7, 2, 1));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Anti-Tank", "Anti-tank guns", 4, 5, 3, 2));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Fighters", "Fighter aircraft", 4, 5, 2, 6));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Bombers", "Bombing aircraft", 4, 7, 1, 5));
        }

        // TL5: Modern era units
        if (techLevel >= 5) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Special Forces", "Elite special operations", 5, 5, 4, 4));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "APC", "Armored personnel carrier", 5, 4, 5, 5));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Helicopters", "Attack helicopters", 5, 6, 3, 7));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Missiles", "Surface-to-surface missiles", 5, 9, 1, 0));
        }

        // TL6: Advanced era units
        if (techLevel >= 6) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Drones", "Combat drones", 6, 5, 2, 6));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Stealth Aircraft", "Stealth bombers", 6, 8, 4, 8));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Nuclear", "Nuclear weapons", 6, 20, 0, 0));
        }

        // TL7: Pre-stellar units
        if (techLevel >= 7) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Laser Infantry", "Laser-armed troops", 7, 6, 5, 4));
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Space Marines", "Orbital deployment troops", 7, 7, 6, 5));
        }

        // TL8+: Interstellar units
        if (techLevel >= 8) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Spacecraft", "Space combat vessels", 8, 10, 8, 9));
        }

        // TL10+: Jump drive units
        if (techLevel >= 10) {
            units.add(new Earth2026BattleEnvironment.UnitTypeInfo(
                    "Jump Ship", "Interstellar jump-capable vessel", 10, 15, 12, 10));
        }

        return units;
    }

    // ------------------------------------------------------------------
    // Planet operations
    // ------------------------------------------------------------------

    @Override
    public Optional<Earth2026Planet> getPlanet(String planetName) {
        return planetRepository.findByNameAndActiveTrue(planetName);
    }

    @Override
    public Optional<Earth2026Planet> getPlanet(Long id) {
        return planetRepository.findById(id);
    }

    @Override
    public List<Earth2026Planet> getAllPlanets() {
        return planetRepository.findAll();
    }

    // ------------------------------------------------------------------
    // Country operations
    // ------------------------------------------------------------------

    @Override
    public List<Earth2026Country> getCountries(Long planetId) {
        return countryRepository.findByPlanetIdAndActiveTrue(planetId);
    }

    @Override
    public Page<Earth2026Country> getCountries(Long planetId, Pageable pageable) {
        return countryRepository.findByPlanetIdAndActiveTrue(planetId, pageable);
    }

    @Override
    public long countCountries(Long planetId) {
        return countryRepository.countByPlanetIdAndActiveTrue(planetId);
    }

    @Override
    public Optional<Earth2026Country> getCountry(Long id) {
        return countryRepository.findById(id);
    }

    @Override
    public Optional<Earth2026Country> getCountryByIsoCode(String isoCode) {
        Earth2026Country country = countryRepository.findByIsoCode(isoCode.toUpperCase());
        return Optional.ofNullable(country);
    }

    @Override
    public List<Earth2026Country> getCountriesByContinent(String continent) {
        return countryRepository.findByContinent(continent);
    }

    // ------------------------------------------------------------------
    // Region operations
    // ------------------------------------------------------------------

    @Override
    public List<Earth2026Region> getRegions(Long countryId) {
        return regionRepository.findByCountryIdAndActiveTrue(countryId);
    }

    @Override
    public Page<Earth2026Region> getRegions(Long countryId, Pageable pageable) {
        return regionRepository.findByCountryIdAndActiveTrue(countryId, pageable);
    }

    @Override
    public long countRegions(Long countryId) {
        return regionRepository.countByCountryIdAndActiveTrue(countryId);
    }

    @Override
    public Optional<Earth2026Region> getRegion(Long id) {
        return regionRepository.findById(id);
    }

    // ------------------------------------------------------------------
    // Economy operations
    // ------------------------------------------------------------------

    @Override
    public Optional<Earth2026Economy> getEconomy(Long planetId) {
        return economyRepository.findByPlanetId(planetId);
    }

    // ------------------------------------------------------------------
    // Job operations
    // ------------------------------------------------------------------

    @Override
    public List<Earth2026Job> getJobs(Long planetId) {
        return jobRepository.findByPlanetIdAndActiveTrue(planetId);
    }

    @Override
    public Page<Earth2026Job> getJobs(Long planetId, Pageable pageable) {
        return jobRepository.findByPlanetIdAndActiveTrue(planetId, pageable);
    }

    @Override
    public long countJobs(Long planetId) {
        return jobRepository.countByPlanetIdAndActiveTrue(planetId);
    }

    @Override
    public List<Earth2026Job> getJobsByCategory(Long planetId, String category) {
        return jobRepository.findByPlanetIdAndCategory(planetId, category);
    }

    @Override
    public Optional<Earth2026Job> getJob(Long id) {
        return jobRepository.findById(id);
    }

    // ------------------------------------------------------------------
    // Contract operations
    // ------------------------------------------------------------------

    @Override
    public List<Earth2026Contract> getAvailableContracts(Long planetId) {
        return contractRepository.findByPlanetIdAndStatusAndActiveTrue(planetId, "available");
    }

    @Override
    public Page<Earth2026Contract> getAvailableContracts(Long planetId, Pageable pageable) {
        return contractRepository.findByPlanetIdAndStatusAndActiveTrue(planetId, "available", pageable);
    }

    @Override
    public long countAvailableContracts(Long planetId) {
        return contractRepository.countByPlanetIdAndStatusAndActiveTrue(planetId, "available");
    }

    @Override
    public Optional<Earth2026Contract> getContract(Long id) {
        return contractRepository.findById(id);
    }

    @Override
    public List<Earth2026Contract> getContractsByCharacter(Long characterId) {
        return contractRepository.findByAssigneeId(characterId);
    }

    // ------------------------------------------------------------------
    // AI Character operations
    // ------------------------------------------------------------------

    @Override
    public Optional<AiCharacter> getCountryAiLeader(Long countryId) {
        Earth2026Country country = countryRepository.findById(countryId).orElse(null);
        if (country == null || country.getAiLeaderId() == null) {
            return Optional.empty();
        }
        return aiCharacterRepository.findById(country.getAiLeaderId());
    }

    @Override
    public Optional<AiCharacter> getRegionAiGovernor(Long regionId) {
        Earth2026Region region = regionRepository.findById(regionId).orElse(null);
        if (region == null || region.getAiGovernorId() == null) {
            return Optional.empty();
        }
        return aiCharacterRepository.findById(region.getAiGovernorId());
    }

    @Override
    public List<AiCharacter> getAllCountryAiLeaders(Long planetId) {
        List<Earth2026Country> countries = countryRepository.findByPlanetIdAndActiveTrue(planetId);
        List<AiCharacter> leaders = new java.util.ArrayList<>();
        for (Earth2026Country country : countries) {
            if (country.getAiLeaderId() != null) {
                aiCharacterRepository.findById(country.getAiLeaderId()).ifPresent(leaders::add);
            }
        }
        return leaders;
    }

    @Override
    public Page<AiCharacter> getAllCountryAiLeaders(Long planetId, Pageable pageable) {
        Page<Earth2026Country> countries = countryRepository.findByPlanetIdAndActiveTrue(planetId, pageable);
        List<AiCharacter> leaders = new ArrayList<>();
        for (Earth2026Country country : countries.getContent()) {
            if (country.getAiLeaderId() != null) {
                aiCharacterRepository.findById(country.getAiLeaderId()).ifPresent(leaders::add);
            }
        }
        return new PageImpl<>(leaders, pageable, countries.getTotalElements());
    }

    @Override
    public List<AiCharacter> getAllRegionAiGovernors(Long countryId) {
        List<Earth2026Region> regions = regionRepository.findByCountryIdAndActiveTrue(countryId);
        List<AiCharacter> governors = new java.util.ArrayList<>();
        for (Earth2026Region region : regions) {
            if (region.getAiGovernorId() != null) {
                aiCharacterRepository.findById(region.getAiGovernorId()).ifPresent(governors::add);
            }
        }
        return governors;
    }

    @Override
    public Page<AiCharacter> getAllRegionAiGovernors(Long countryId, Pageable pageable) {
        Page<Earth2026Region> regions = regionRepository.findByCountryIdAndActiveTrue(countryId, pageable);
        List<AiCharacter> governors = new ArrayList<>();
        for (Earth2026Region region : regions.getContent()) {
            if (region.getAiGovernorId() != null) {
                aiCharacterRepository.findById(region.getAiGovernorId()).ifPresent(governors::add);
            }
        }
        return new PageImpl<>(governors, pageable, regions.getTotalElements());
    }

    @Transactional
    @Override
    public Optional<Earth2026Country> assignCountryAiLeader(Long countryId, Long aiCharacterId) {
        return countryRepository.findById(countryId)
                .map(country -> {
                    AiCharacter aiChar = aiCharacterRepository.findById(aiCharacterId).orElse(null);
                    if (aiChar == null) {
                        throw new IllegalArgumentException("AI character not found: " + aiCharacterId);
                    }
                    country.setAiLeaderId(aiCharacterId);
                    country.setLeaderName(aiChar.getName());
                    country.setLeaderTitle(aiChar.getRole());
                    countryRepository.save(country);
                    log.info("Assigned AI character '{}' as leader of country '{}'", aiChar.getName(), country.getName());
                    return country;
                });
    }

    @Transactional
    @Override
    public Optional<Earth2026Region> assignRegionAiGovernor(Long regionId, Long aiCharacterId) {
        return regionRepository.findById(regionId)
                .map(region -> {
                    AiCharacter aiChar = aiCharacterRepository.findById(aiCharacterId).orElse(null);
                    if (aiChar == null) {
                        throw new IllegalArgumentException("AI character not found: " + aiCharacterId);
                    }
                    region.setAiGovernorId(aiCharacterId);
                    region.setGovernorName(aiChar.getName());
                    region.setGovernorTitle(aiChar.getRole());
                    regionRepository.save(region);
                    log.info("Assigned AI character '{}' as governor of region '{}'", aiChar.getName(), region.getName());
                    return region;
                });
    }

    // ------------------------------------------------------------------
    // PlanetService: Rules Database operations
    // ------------------------------------------------------------------

    @Override
    public List<E26Species> getAllSpecies() {
        return rulesDatabaseService.getAllSpecies();
    }

    @Override
    public Optional<E26Species> getSpecies(String id) {
        return rulesDatabaseService.getSpecies(id);
    }

    @Override
    public List<E26Species> getSpeciesByGameModule(String gameModule) {
        return rulesDatabaseService.getSpeciesByGameModule(gameModule);
    }

    @Override
    public String generateAiCharacterName(String speciesId, String role) {
        return rulesDatabaseService.generateAiCharacterName(speciesId, role);
    }

    @Override
    public E26GalaxyRules getGalaxyRules() {
        return rulesDatabaseService.getGalaxyRules();
    }

    @Override
    public E26SystemRules getSystemRules() {
        return rulesDatabaseService.getSystemRules();
    }

    @Override
    public E26PlanetRules getPlanetRules() {
        return rulesDatabaseService.getPlanetRules();
    }

    // ------------------------------------------------------------------
    // PlanetService: Planet Population from Game Data
    // ------------------------------------------------------------------

    /**
     * Batch size for country population — keeps memory bounded per callback.
     */
    private static final int COUNTRY_BATCH_SIZE = 10;

    /**
     * Batch size for region population — keeps memory bounded per callback.
     */
    private static final int REGION_BATCH_SIZE = 20;

    /**
     * Deprecated monolithic population method — delegates to the phased version
     * and collects all results into a single response object for backward compatibility.
     */
    @Deprecated
    @Transactional
    @Override
    public com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse populatePlanetWithGameData(
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request) {

        com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse response =
                new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse();

        // Collect all phased results into the old response DTO
        final List<Earth2026Country> allCountries = new ArrayList<>();
        final List<Earth2026Region> allRegions = new ArrayList<>();
        final List<AiCharacter> newAiCharacters = new ArrayList<>();
        final Map<String, Long> reusedAiCharacterIds = new HashMap<>();
        final Map<String, String> registeredPlayerCharacters = new HashMap<>();
        final com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse.PopulationSummary summary =
                new com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse.PopulationSummary();

        PlanetPopulationProgress collectingProgress = new PlanetPopulationProgress() {
            @Override
            public void onPlanetCreated(Earth2026Planet planet) {
                response.setPlanet(planet);
            }

            @Override
            public void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries) {
                allCountries.addAll(countries);
            }

            @Override
            public void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions) {
                allRegions.addAll(regions);
            }

            @Override
            public void onAiLeaderAssigned(AiCharacter leader, Long countryId, boolean isNew) {
                if (isNew) {
                    newAiCharacters.add(leader);
                }
            }

            @Override
            public void onAiGovernorAssigned(AiCharacter governor, Long regionId, boolean isNew) {
                if (isNew) {
                    newAiCharacters.add(governor);
                }
            }

            @Override
            public void onPlayerCharactersRegistered(Map<String, String> players) {
                registeredPlayerCharacters.putAll(players);
            }

            @Override
            public void onComplete(PlanetPopulationProgress.PopulationSummary popSummary) {
                summary.setPlanetFound(popSummary.isPlanetFound());
                summary.setTotalGameCharacters(popSummary.getTotalGameCharacters());
                summary.setCountriesWithLeaders(popSummary.getCountriesWithLeaders());
                summary.setRegionsWithGovernors(popSummary.getRegionsWithGovernors());
                summary.setAiCharactersReused(popSummary.getAiCharactersReused());
                summary.setNewAiCharactersGenerated(popSummary.getNewAiCharactersGenerated());
                summary.setPlayerCharactersRegistered(popSummary.getPlayerCharactersRegistered());
            }
        };

        populatePlanetWithGameData(request, collectingProgress);

        response.setCountries(allCountries);
        response.setRegions(allRegions);
        response.setNewAiCharacters(newAiCharacters);
        response.setReusedAiCharacterIds(reusedAiCharacterIds);
        response.setRegisteredPlayerCharacters(registeredPlayerCharacters);
        response.setSummary(summary);

        return response;
    }

    /**
     * Phased planet population with progress callbacks.
     * <p>
     * This method builds the planet in discrete phases, calling back to the
     * {@code progress} callback after each phase. This allows the requesting
     * galaxy to receive data incrementally, preventing memory overrun.
     * </p>
     */
    @Transactional
    @Override
    public void populatePlanetWithGameData(
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request,
            PlanetPopulationProgress progress) {

        // Initialize tracking
        Map<String, Long> reusedAiCharacterIds = new HashMap<>();
        Map<String, String> registeredPlayerCharacters = new HashMap<>();
        List<AiCharacter> newAiCharacters = new ArrayList<>();

        // Separate existing characters into AI and player characters
        List<PlanetCharacterInfo> existingChars = request.getExistingCharacters();
        int totalGameCharacters = existingChars != null ? existingChars.size() : 0;

        List<PlanetCharacterInfo> aiCharsFromGame = new ArrayList<>();
        List<PlanetCharacterInfo> playerCharsFromGame = new ArrayList<>();

        if (existingChars != null) {
            for (PlanetCharacterInfo charInfo : existingChars) {
                if (charInfo != null && charInfo.isActive()) {
                    if (charInfo.isPlayerControlled()) {
                        playerCharsFromGame.add(charInfo);
                    } else {
                        aiCharsFromGame.add(charInfo);
                    }
                }
            }
        }

        // Build lookup maps for existing AI characters by role and by name
        Map<String, PlanetCharacterInfo> aiByRole = new HashMap<>();
        Map<String, PlanetCharacterInfo> aiByName = new HashMap<>();
        for (PlanetCharacterInfo ai : aiCharsFromGame) {
            if (ai.getRole() != null) {
                aiByRole.put(ai.getRole().toLowerCase(), ai);
            }
            if (ai.getName() != null) {
                aiByName.put(ai.getName().toLowerCase(), ai);
            }
        }

        // Determine species for new AI character generation
        String speciesId = request.getSpeciesIdForNewAi();
        if (speciesId == null || speciesId.isBlank()) {
            speciesId = "Human";
        }

        // ---------------------------------------------------------------
        // Phase 1: Planet Foundation
        // ---------------------------------------------------------------
        Earth2026Planet planet = findOrCreatePlanet(request);
        boolean planetFound = planet.getId() != null;

        if (progress != null) {
            progress.onPlanetCreated(planet);
            if (planetFound) {
                progress.onLog("Planet '{}' found and reused.".formatted(planet.getName()));
            } else {
                progress.onLog("Planet '{}' created.".formatted(planet.getName()));
            }
        }

        // ---------------------------------------------------------------
        // Phase 2: Countries with AI Leaders (batched)
        // ---------------------------------------------------------------
        List<Earth2026Country> countries = countryRepository.findByPlanetIdAndActiveTrue(planet.getId());
        int countriesWithLeaders = 0;
        int aiReused = 0;

        for (int i = 0; i < countries.size(); i += COUNTRY_BATCH_SIZE) {
            int end = Math.min(i + COUNTRY_BATCH_SIZE, countries.size());
            List<Earth2026Country> batch = countries.subList(i, end);

            for (Earth2026Country country : batch) {
                AiCharacter leader = null;
                boolean isNew = false;

                // Try to find an existing AI character for this country
                if (!request.isForceRegenerate()) {
                    // First try by country name match
                    for (PlanetCharacterInfo ai : aiCharsFromGame) {
                        if (ai.getCountryOrRegion() != null &&
                                ai.getCountryOrRegion().equalsIgnoreCase(country.getName())) {
                            leader = findOrCreateAiCharacter(ai, speciesId);
                            reusedAiCharacterIds.put(ai.getGameId(), leader.getId());
                            aiReused++;
                            break;
                        }
                    }

                    // If no country-specific match, try to find a Leader role
                    if (leader == null && country.getAiLeaderId() == null) {
                        PlanetCharacterInfo leaderInfo = aiByRole.get("leader");
                        if (leaderInfo != null) {
                            leader = findOrCreateAiCharacter(leaderInfo, speciesId);
                            reusedAiCharacterIds.put(leaderInfo.getGameId(), leader.getId());
                            aiReused++;
                        }
                    }
                }

                // If still no leader, find or create one (reusing existing AI Admins with experience)
                if (leader == null && country.getAiLeaderId() == null) {
                    leader = findOrCreateAiCharacterByRole("Leader", country.getName(),
                            "AI leader for " + country.getName() + " on " + planet.getName(), speciesId);
                    // Only count as new if this is a freshly created character (0 experience and no prior battles)
                    if (leader.getTotalExperience() == 0 && leader.getTotalBattles() == 0) {
                        newAiCharacters.add(leader);
                    }
                    isNew = (leader.getTotalExperience() == 0 && leader.getTotalBattles() == 0);
                }

                // Assign the leader to the country
                if (leader != null) {
                    country.setAiLeaderId(leader.getId());
                    country.setLeaderName(leader.getName());
                    country.setLeaderTitle(leader.getRole());
                    countryRepository.save(country);
                    countriesWithLeaders++;

                    if (progress != null) {
                        progress.onAiLeaderAssigned(leader, country.getId(), isNew);
                    }
                }
            }

            if (progress != null) {
                progress.onCountryBatch(batch, i + 1, countries.size());
            }
        }

        // ---------------------------------------------------------------
        // Phase 3: Regions with AI Governors (batched per country)
        // ---------------------------------------------------------------
        int regionsWithGovernors = 0;

        for (Earth2026Country country : countries) {
            List<Earth2026Region> regions = regionRepository.findByCountryIdAndActiveTrue(country.getId());

            for (int i = 0; i < regions.size(); i += REGION_BATCH_SIZE) {
                int end = Math.min(i + REGION_BATCH_SIZE, regions.size());
                List<Earth2026Region> batch = regions.subList(i, end);

                for (Earth2026Region region : batch) {
                    AiCharacter governor = null;
                    boolean isNew = false;

                    // Try to find an existing AI character for this region
                    if (!request.isForceRegenerate() && region.getAiGovernorId() == null) {
                        for (PlanetCharacterInfo ai : aiCharsFromGame) {
                            if (ai.getCountryOrRegion() != null &&
                                    ai.getCountryOrRegion().equalsIgnoreCase(region.getName())) {
                                governor = findOrCreateAiCharacter(ai, speciesId);
                                reusedAiCharacterIds.put(ai.getGameId(), governor.getId());
                                aiReused++;
                                break;
                            }
                        }

                        // If no region-specific match, try to find a Governor role
                        if (governor == null) {
                            PlanetCharacterInfo govInfo = aiByRole.get("governor");
                            if (govInfo != null) {
                                governor = findOrCreateAiCharacter(govInfo, speciesId);
                                reusedAiCharacterIds.put(govInfo.getGameId(), governor.getId());
                                aiReused++;
                            }
                        }
                    }

                    // If still no governor, find or create one (reusing existing AI Admins with experience)
                    if (governor == null && region.getAiGovernorId() == null) {
                        String regionCountryName = countryRepository.findById(region.getCountryId())
                                .map(Earth2026Country::getName).orElse("Unknown");
                        governor = findOrCreateAiCharacterByRole("Governor", regionCountryName,
                                "AI governor for " + region.getName(), speciesId);
                        // Only count as new if this is a freshly created character (0 experience and no prior battles)
                        if (governor.getTotalExperience() == 0 && governor.getTotalBattles() == 0) {
                            newAiCharacters.add(governor);
                        }
                        isNew = (governor.getTotalExperience() == 0 && governor.getTotalBattles() == 0);
                    }

                    // Assign the governor to the region
                    if (governor != null) {
                        region.setAiGovernorId(governor.getId());
                        region.setGovernorName(governor.getName());
                        region.setGovernorTitle(governor.getRole());
                        regionRepository.save(region);
                        regionsWithGovernors++;

                        if (progress != null) {
                            progress.onAiGovernorAssigned(governor, region.getId(), isNew);
                        }
                    }
                }

                if (progress != null) {
                    progress.onRegionBatch(batch, country.getId(), i + 1, regions.size());
                }
            }
        }

        // ---------------------------------------------------------------
        // Phase 4: Register Player Characters
        // ---------------------------------------------------------------
        int playersRegistered = 0;
        for (PlanetCharacterInfo player : playerCharsFromGame) {
            registeredPlayerCharacters.put(player.getGameId(), player.getName());
            playersRegistered++;
        }

        if (progress != null) {
            progress.onPlayerCharactersRegistered(registeredPlayerCharacters);
        }

        // ---------------------------------------------------------------
        // Phase 5: Planet Overseer
        // ---------------------------------------------------------------
        if (planet.getAiOverseerId() == null) {
            AiCharacter overseer = findOrCreateAiCharacterByRole("Overseer", planet.getName(),
                    "AI overseer for planet " + planet.getName(), speciesId);
            planet.setAiOverseerId(overseer.getId());
            planet.setOverseerName(overseer.getName());
            // Only count as new if this is a freshly created character (0 experience and no prior battles)
            if (overseer.getTotalExperience() == 0 && overseer.getTotalBattles() == 0) {
                newAiCharacters.add(overseer);
            }
            planetRepository.save(planet);

            if (progress != null) {
                if (overseer.getTotalExperience() > 0 || overseer.getTotalBattles() > 0) {
                    progress.onLog("Planet overseer assigned (reused with experience): " + overseer.getName() +
                            " (xp=" + overseer.getTotalExperience() + ", leadership=" + overseer.getLeadershipRating() + ")");
                } else {
                    progress.onLog("Planet overseer assigned: " + overseer.getName());
                }
            }
        }

        // ---------------------------------------------------------------
        // Phase 6: Finalize Planet Stats
        // ---------------------------------------------------------------
        int totalRegionCount = 0;
        for (Earth2026Country country : countries) {
            totalRegionCount += regionRepository.findByCountryIdAndActiveTrue(country.getId()).size();
        }

        planet.setCountryCount(countries.size());
        planet.setRegionCount(totalRegionCount);
        planet.setInitialized(true);
        planet.setUpdatedAt(java.time.LocalDateTime.now());
        planetRepository.save(planet);

        // ---------------------------------------------------------------
        // Phase 7: Completion Summary
        // ---------------------------------------------------------------
        if (progress != null) {
            PlanetPopulationProgress.PopulationSummary summary = new PlanetPopulationProgress.PopulationSummary();
            summary.setPlanetFound(planetFound);
            summary.setTotalGameCharacters(totalGameCharacters);
            summary.setCountriesWithLeaders(countriesWithLeaders);
            summary.setRegionsWithGovernors(regionsWithGovernors);
            summary.setAiCharactersReused(aiReused);
            summary.setNewAiCharactersGenerated(newAiCharacters.size());
            summary.setPlayerCharactersRegistered(playersRegistered);
            progress.onComplete(summary);
        }

        log.info("Populated planet '{}' with game data: {} total chars, {} players, {} AI reused, {} AI new",
                planet.getName(), totalGameCharacters, playersRegistered, aiReused, newAiCharacters.size());
    }

    /**
     * Find or create a planet based on the request.
     */
    private Earth2026Planet findOrCreatePlanet(
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request) {

        // Try to find existing planet
        Optional<Earth2026Planet> existing = planetRepository.findByNameAndActiveTrue(request.getPlanetName());
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new planet
        Earth2026Planet planet = new Earth2026Planet();
        planet.setName(request.getPlanetName());
        planet.setPlanetType(request.getPlanetType() != null ? request.getPlanetType() : "Terrestrial");

        // Set tech level
        if (request.getTechLevel() != null) {
            try {
                planet.setBaseTechLevel(TechLevel.fromLevel(request.getTechLevel()));
            } catch (IllegalArgumentException e) {
                planet.setBaseTechLevel(TechLevel.TL5);
            }
        } else {
            planet.setBaseTechLevel(TechLevel.TL5);
        }

        planet.setTimePeriod(request.getTimePeriod() != null ? request.getTimePeriod() : "Modern");
        planet.setGravity(1.0);
        planet.setAvgTemperatureC(15.0);
        planet.setAtmosphere("Nitrogen-Oxygen");
        planet.setRadiusKm(6371.0);
        planet.setTotalPopulationBillions(8.0);
        planet.setCountryCount(0);
        planet.setRegionCount(0);
        planet.setLocationCount(0);
        planet.setActive(true);
        planet.setInitialized(false);
        planet.setCreatedAt(java.time.LocalDateTime.now());
        planet.setUpdatedAt(java.time.LocalDateTime.now());

        log.info("Created new planet: {} (type={}, techLevel={})",
                planet.getName(), planet.getPlanetType(), planet.getBaseTechLevel());

        return planetRepository.save(planet);
    }

    /**
     * Find or create an AI character from game character info.
     * <p>
     * Prioritizes reusing existing AI characters to retain their accumulated experience
     * (totalExperience, leadershipRating, totalBattles, totalVictories) across planet creations.
     * </p>
     * <p>
     * Reuse strategy:
     * </p>
     * <ol>
     *   <li>Exact name match — returns the existing character with all experience intact</li>
     *   <li>Role-based pool reuse — finds an existing AI with the same role, ordered by experience (highest first)</li>
     *   <li>Create new — only if no existing AI character matches by name or role</li>
     * </ol>
     *
     * @param charInfo the character info from the game
     * @param defaultSpecies the default species for name generation
     * @return the found or newly created AI character (persisted)
     */
    private AiCharacter findOrCreateAiCharacter(PlanetCharacterInfo charInfo, String defaultSpecies) {
        String roleName = charInfo.getRole() != null ? charInfo.getRole() : "Advisor";

        // Strategy 1: Exact name match — return existing character with all experience intact
        Optional<AiCharacter> existingByName = aiCharacterRepository.findByName(charInfo.getName());
        if (existingByName.isPresent()) {
            AiCharacter found = existingByName.get();
            log.debug("Reused existing AI character by name: {} (xp={}, leadership={})",
                    found.getName(), found.getTotalExperience(), found.getLeadershipRating());
            return found;
        }

        // Strategy 2: Role-based pool reuse — find existing AI with the same role, ordered by experience
        // This ensures the same AI Admins are reused across planets with retained experience
        List<AiCharacter> candidatesByRole = aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc(roleName);
        if (!candidatesByRole.isEmpty()) {
            // Pick the candidate with the highest experience
            AiCharacter reused = candidatesByRole.get(0);
            // Update the empire name and description to reflect the new planet assignment
            // while preserving all experience data
            if (charInfo.getEmpireName() != null && !charInfo.getEmpireName().equals(reused.getEmpireName())) {
                reused.setEmpireName(charInfo.getEmpireName());
            }
            if (charInfo.getDescription() != null) {
                reused.setDescription(charInfo.getDescription());
            }
            aiCharacterRepository.save(reused);
            log.info("Reused existing AI character by role '{}': {} (xp={}, leadership={}, battles={}, victories={})",
                    roleName, reused.getName(), reused.getTotalExperience(), reused.getLeadershipRating(),
                    reused.getTotalBattles(), reused.getTotalVictories());
            return reused;
        }

        // Strategy 3: Create new AI character — only when no existing match found
        AiCharacter aiChar = new AiCharacter();
        aiChar.setName(charInfo.getName());
        aiChar.setRole(roleName);
        aiChar.setEmpireName(charInfo.getEmpireName() != null ? charInfo.getEmpireName() : "Unknown");
        aiChar.setDescription(charInfo.getDescription() != null ? charInfo.getDescription() :
                "AI character from game data: " + charInfo.getName());
        aiChar.setActive(true);
        aiChar.setCanPromoteToCoServer(false);
        // Initialize experience at 0 for new characters
        aiChar.setTotalExperience(0);
        aiChar.setLeadershipRating(5);
        aiChar.setTotalBattles(0);
        aiChar.setTotalVictories(0);

        log.info("Created new AI character: {} (role={}, empire={})",
                aiChar.getName(), aiChar.getRole(), aiChar.getEmpireName());

        return aiCharacterRepository.save(aiChar);
    }

    /**
     * Finds or creates an AI character for a specific role on a planet,
     * prioritizing reuse of existing AI Admins with retained experience.
     * <p>
     * Used when generating AI characters for roles that don't have corresponding
     * game character info (e.g., new Leader/Governor for a country/region).
     * </p>
     *
     * @param role the role to fill (e.g., "Leader", "Governor", "Overseer")
     * @param empireName the empire/planet name for the new assignment
     * @param description description of the AI character's role
     * @param speciesId the species ID for name generation
     * @return the found or newly created AI character (persisted)
     */
    private AiCharacter findOrCreateAiCharacterByRole(String role, String empireName, String description, String speciesId) {
        // Strategy 1: Role-based pool reuse — find existing AI with the same role, ordered by experience
        List<AiCharacter> candidatesByRole = aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc(role);
        if (!candidatesByRole.isEmpty()) {
            // Pick the candidate with the highest experience
            AiCharacter reused = candidatesByRole.get(0);
            // Update the empire name and description to reflect the new planet assignment
            // while preserving all experience data
            reused.setEmpireName(empireName);
            reused.setDescription(description);
            aiCharacterRepository.save(reused);
            log.info("Reused existing AI character by role '{}': {} (xp={}, leadership={}, battles={}, victories={})",
                    role, reused.getName(), reused.getTotalExperience(), reused.getLeadershipRating(),
                    reused.getTotalBattles(), reused.getTotalVictories());
            return reused;
        }

        // Strategy 2: Create new AI character with generated name
        String generatedName = generateAiCharacterName(speciesId, role);
        AiCharacter aiChar = new AiCharacter(generatedName, role, empireName);
        aiChar.setDescription(description);
        aiChar.setActive(true);
        aiChar.setCanPromoteToCoServer(false);
        // Initialize experience at 0 for new characters
        aiChar.setTotalExperience(0);
        aiChar.setLeadershipRating(5);
        aiChar.setTotalBattles(0);
        aiChar.setTotalVictories(0);

        log.info("Created new AI character: {} (role={}, empire={})",
                aiChar.getName(), aiChar.getRole(), aiChar.getEmpireName());

        return aiCharacterRepository.save(aiChar);
    }
}

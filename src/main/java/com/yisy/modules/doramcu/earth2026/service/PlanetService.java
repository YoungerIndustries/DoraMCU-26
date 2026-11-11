package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.unified.GameType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared service interface for Earth-based operations that other game modules
 * can consume without depending on the Earth 2026 module directly.
 * <p>
 * This interface allows game modules (D&D 5e, Star Trek, Traveller, etc.) to
 * look up Earth characters, find Earth locations, and map Earth 2026 stats
 * to their game-specific stat systems.
 * </p>
 * <p>
 * Earth 2026Service implements this interface, providing the actual Earth
 * data and operations. Other games inject PlanetService for dependency isolation.
 * </p>
 */
public interface PlanetService {

    // ------------------------------------------------------------------
    // Character operations
    // ------------------------------------------------------------------

    /**
     * Look up an Earth 2026 character by ID.
     *
     * @param id the character ID
     * @return the character if found
     */
    Optional<Earth2026Character> getCharacter(Long id);

    /**
     * List all characters currently at a specific Earth location.
     *
     * @param locationName the location name
     * @return list of characters at that location
     */
    List<Earth2026Character> getCharactersAtLocation(String locationName);

    /**
     * List all characters that have not yet transitioned to another galaxy.
     *
     * @return list of characters still on Earth
     */
    List<Earth2026Character> getCharactersOnEarth();

    // ------------------------------------------------------------------
    // Location operations
    // ------------------------------------------------------------------

    /**
     * Get all Earth locations.
     *
     * @return list of all locations
     */
    List<Earth2026Location> getAllLocations();

    /**
     * Get an Earth location by ID.
     *
     * @param id the location ID
     * @return the location if found
     */
    Optional<Earth2026Location> getLocation(Long id);

    /**
     * Find locations near the given coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @param radiusKm  the search radius in kilometers
     * @return list of nearby locations
     */
    List<Earth2026Location> getLocationsNearby(double latitude, double longitude, double radiusKm);

    /**
     * Get all portal locations that allow galaxy transitions.
     *
     * @return list of portal locations
     */
    List<Earth2026Location> getPortalLocations();

    // ------------------------------------------------------------------
    // Scenario operations
    // ------------------------------------------------------------------

    /**
     * Get available scenarios for a character's level.
     *
     * @param level the character's level
     * @return list of available scenarios
     */
    List<Earth2026Scenario> getAvailableScenarios(int level);

    // ------------------------------------------------------------------
    // Stat mapping
    // ------------------------------------------------------------------

    /**
     * Map Earth 2026 ability scores to a target game's stat system.
     * <p>
     * For example, D&D 5e uses a direct 1:1 mapping (same 6 scores),
     * while Star Trek maps STR→Strength, DEX→Fitness, INT→Reason, etc.
     * </p>
     *
     * @param character the Earth 2026 character
     * @param gameType  the target game type
     * @return a map of game-specific stat names to values
     */
    Map<String, Integer> mapStatsToGame(Earth2026Character character, GameType gameType);

    // ------------------------------------------------------------------
    // Tech Level operations
    // ------------------------------------------------------------------

    /**
     * Get all available Tech Levels.
     *
     * @return list of all tech levels
     */
    List<TechLevel> getAllTechLevels();

    /**
     * Get a Tech Level by numeric value.
     *
     * @param level the numeric TL (0-20)
     * @return the tech level if valid
     */
    Optional<TechLevel> getTechLevel(int level);

    /**
     * Get all available time period templates.
     *
     * @return list of all time period templates
     */
    List<TimePeriodTemplate> getAllTimePeriodTemplates();

    /**
     * Get a time period template by name.
     *
     * @param name the template name (case-insensitive)
     * @return the template if found
     */
    Optional<TimePeriodTemplate> getTimePeriodTemplate(String name);

    // ------------------------------------------------------------------
    // Structure operations
    // ------------------------------------------------------------------

    /**
     * Get all structures at a location.
     *
     * @param locationId the location ID
     * @return list of structures
     */
    List<Earth2026Structure> getStructuresAtLocation(Long locationId);

    /**
     * Get a structure by ID.
     *
     * @param id the structure ID
     * @return the structure if found
     */
    Optional<Earth2026Structure> getStructure(Long id);

    // ------------------------------------------------------------------
    // Battle Environment operations
    // ------------------------------------------------------------------

    /**
     * Build a complete battle environment for a location and time period.
     * <p>
     * This is the main method that games (e.g., Third Reich) call to get
     * everything needed to build a battle: terrain, structures, weather,
     * available units, and map coordinates.
     * </p>
     *
     * @param locationId   the Earth location ID
     * @param timePeriod   the time period template name (e.g., "WWII", "MODERN")
     * @param terrainType  the terrain type (e.g., "URBAN", "PLAINS")
     * @param weather      the weather condition (e.g., "CLEAR", "RAIN")
     * @return the complete battle environment
     */
    Earth2026BattleEnvironment buildBattleEnvironment(Long locationId,
                                                       String timePeriod,
                                                       String terrainType,
                                                       String weather);

    /**
     * Build a battle environment using a time period template's default terrain.
     *
     * @param locationId   the Earth location ID
     * @param timePeriod   the time period template name
     * @return the complete battle environment
     */
    Earth2026BattleEnvironment buildBattleEnvironment(Long locationId, String timePeriod);

    /**
     * Get available unit types for a given tech level.
     *
     * @param techLevel the tech level
     * @return list of available unit types
     */
    List<Earth2026BattleEnvironment.UnitTypeInfo> getAvailableUnitsForTechLevel(int techLevel);

    // ------------------------------------------------------------------
    // Planet operations
    // ------------------------------------------------------------------

    /**
     * Get a complete planet object by name.
     * <p>
     * This is the main method that other games call to get a fully populated
     * planet with all countries, regions, economy, and AI-controlled leaders.
     * </p>
     *
     * @param planetName the planet name (e.g., "Earth")
     * @return the planet if found
     */
    Optional<Earth2026Planet> getPlanet(String planetName);

    /**
     * Get a planet by ID.
     *
     * @param id the planet ID
     * @return the planet if found
     */
    Optional<Earth2026Planet> getPlanet(Long id);

    /**
     * List all available planets.
     *
     * @return list of all active planets
     */
    List<Earth2026Planet> getAllPlanets();

    // ------------------------------------------------------------------
    // Country operations
    // ------------------------------------------------------------------

    /**
     * Get all countries on a planet.
     *
     * @param planetId the planet ID
     * @return list of all active countries
     */
    List<Earth2026Country> getCountries(Long planetId);

    /**
     * Get a paginated list of countries on a planet.
     * <p>
     * Use this method instead of {@link #getCountries(Long)} when the planet
     * has many countries to avoid loading all entities into memory at once.
     * </p>
     *
     * @param planetId the planet ID
     * @param pageable pagination and sorting parameters
     * @return a page of active countries
     */
    Page<Earth2026Country> getCountries(Long planetId, Pageable pageable);

    /**
     * Count the active countries on a planet without loading them.
     *
     * @param planetId the planet ID
     * @return number of active countries
     */
    long countCountries(Long planetId);

    /**
     * Get a country by ID.
     *
     * @param id the country ID
     * @return the country if found
     */
    Optional<Earth2026Country> getCountry(Long id);

    /**
     * Get a country by ISO code.
     *
     * @param isoCode the ISO 3166-1 alpha-2 code
     * @return the country if found
     */
    Optional<Earth2026Country> getCountryByIsoCode(String isoCode);

    /**
     * Get countries by continent.
     *
     * @param continent the continent name
     * @return list of countries on that continent
     */
    List<Earth2026Country> getCountriesByContinent(String continent);

    // ------------------------------------------------------------------
    // Region operations
    // ------------------------------------------------------------------

    /**
     * Get all regions in a country.
     *
     * @param countryId the country ID
     * @return list of all active regions
     */
    List<Earth2026Region> getRegions(Long countryId);

    /**
     * Get a paginated list of regions in a country.
     * <p>
     * Use this method instead of {@link #getRegions(Long)} when the country
     * has many regions to avoid loading all entities into memory at once.
     * </p>
     *
     * @param countryId the country ID
     * @param pageable pagination and sorting parameters
     * @return a page of active regions
     */
    Page<Earth2026Region> getRegions(Long countryId, Pageable pageable);

    /**
     * Count the active regions in a country without loading them.
     *
     * @param countryId the country ID
     * @return number of active regions
     */
    long countRegions(Long countryId);

    /**
     * Get a region by ID.
     *
     * @param id the region ID
     * @return the region if found
     */
    Optional<Earth2026Region> getRegion(Long id);

    // ------------------------------------------------------------------
    // Economy operations
    // ------------------------------------------------------------------

    /**
     * Get the economy for a planet.
     *
     * @param planetId the planet ID
     * @return the economy if found
     */
    Optional<Earth2026Economy> getEconomy(Long planetId);

    // ------------------------------------------------------------------
    // Job operations
    // ------------------------------------------------------------------

    /**
     * Get all jobs on a planet.
     *
     * @param planetId the planet ID
     * @return list of all active jobs
     */
    List<Earth2026Job> getJobs(Long planetId);

    /**
     * Get a paginated list of jobs on a planet.
     * <p>
     * Use this method instead of {@link #getJobs(Long)} when the planet
     * has many jobs to avoid loading all entities into memory at once.
     * </p>
     *
     * @param planetId the planet ID
     * @param pageable pagination and sorting parameters
     * @return a page of active jobs
     */
    Page<Earth2026Job> getJobs(Long planetId, Pageable pageable);

    /**
     * Count the active jobs on a planet without loading them.
     *
     * @param planetId the planet ID
     * @return number of active jobs
     */
    long countJobs(Long planetId);

    /**
     * Get jobs by category.
     *
     * @param planetId the planet ID
     * @param category the job category
     * @return list of jobs in that category
     */
    List<Earth2026Job> getJobsByCategory(Long planetId, String category);

    /**
     * Get a job by ID.
     *
     * @param id the job ID
     * @return the job if found
     */
    Optional<Earth2026Job> getJob(Long id);

    // ------------------------------------------------------------------
    // Contract operations
    // ------------------------------------------------------------------

    /**
     * Get all available contracts on a planet.
     *
     * @param planetId the planet ID
     * @return list of available contracts
     */
    List<Earth2026Contract> getAvailableContracts(Long planetId);

    /**
     * Get a paginated list of available contracts on a planet.
     * <p>
     * Use this method instead of {@link #getAvailableContracts(Long)} when the planet
     * has many contracts to avoid loading all entities into memory at once.
     * </p>
     *
     * @param planetId the planet ID
     * @param pageable pagination and sorting parameters
     * @return a page of available contracts
     */
    Page<Earth2026Contract> getAvailableContracts(Long planetId, Pageable pageable);

    /**
     * Count the available contracts on a planet without loading them.
     *
     * @param planetId the planet ID
     * @return number of available contracts
     */
    long countAvailableContracts(Long planetId);

    /**
     * Get a contract by ID.
     *
     * @param id the contract ID
     * @return the contract if found
     */
    Optional<Earth2026Contract> getContract(Long id);

    /**
     * Get contracts assigned to a character.
     *
     * @param characterId the character ID
     * @return list of contracts
     */
    List<Earth2026Contract> getContractsByCharacter(Long characterId);

    // ------------------------------------------------------------------
    // AI Character operations
    // ------------------------------------------------------------------

    /**
     * Get the AI character that controls a country.
     *
     * @param countryId the country ID
     * @return the AI character if assigned
     */
    Optional<com.yisy.modules.doramcu.eve.model.AiCharacter> getCountryAiLeader(Long countryId);

    /**
     * Get the AI character that governs a region.
     *
     * @param regionId the region ID
     * @return the AI character if assigned
     */
    Optional<com.yisy.modules.doramcu.eve.model.AiCharacter> getRegionAiGovernor(Long regionId);

    /**
     * Get all AI characters assigned to countries on a planet.
     *
     * @param planetId the planet ID
     * @return list of AI country leaders
     */
    List<com.yisy.modules.doramcu.eve.model.AiCharacter> getAllCountryAiLeaders(Long planetId);

    /**
     * Get a paginated list of AI characters assigned to countries on a planet.
     * <p>
     * Use this method instead of {@link #getAllCountryAiLeaders(Long)} when the planet
     * has many countries to avoid loading all entities into memory at once.
     * </p>
     *
     * @param planetId the planet ID
     * @param pageable pagination and sorting parameters
     * @return a page of AI country leaders
     */
    Page<com.yisy.modules.doramcu.eve.model.AiCharacter> getAllCountryAiLeaders(Long planetId, Pageable pageable);

    /**
     * Get all AI characters assigned to regions in a country.
     *
     * @param countryId the country ID
     * @return list of AI region governors
     */
    List<com.yisy.modules.doramcu.eve.model.AiCharacter> getAllRegionAiGovernors(Long countryId);

    /**
     * Get a paginated list of AI characters assigned to regions in a country.
     * <p>
     * Use this method instead of {@link #getAllRegionAiGovernors(Long)} when the country
     * has many regions to avoid loading all entities into memory at once.
     * </p>
     *
     * @param countryId the country ID
     * @param pageable pagination and sorting parameters
     * @return a page of AI region governors
     */
    Page<com.yisy.modules.doramcu.eve.model.AiCharacter> getAllRegionAiGovernors(Long countryId, Pageable pageable);

    /**
     * Assign an AI character as the leader of a country.
     *
     * @param countryId the country ID
     * @param aiCharacterId the AI character ID
     * @return the updated country
     */
    Optional<Earth2026Country> assignCountryAiLeader(Long countryId, Long aiCharacterId);

    /**
     * Assign an AI character as the governor of a region.
     *
     * @param regionId the region ID
     * @param aiCharacterId the AI character ID
     * @return the updated region
     */
    Optional<Earth2026Region> assignRegionAiGovernor(Long regionId, Long aiCharacterId);

    // ------------------------------------------------------------------
    // Rules Database operations
    // ------------------------------------------------------------------

    /**
     * Get all species from the rules database.
     *
     * @return list of all species
     */
    List<E26Species> getAllSpecies();

    /**
     * Get a species by ID (case-insensitive).
     *
     * @param id the species ID
     * @return the species if found
     */
    Optional<E26Species> getSpecies(String id);

    /**
     * Get species by game module (e.g., "TRAVELLER", "CAPTAINS_LOG", "BABYLON5").
     *
     * @param gameModule the game module name
     * @return list of species for that game module
     */
    List<E26Species> getSpeciesByGameModule(String gameModule);

    /**
     * Generate an AI character name for a given species and role.
     * <p>
     * This is the main method that games use to get AI character names
     * that match their game's lore when requesting a planet or moon.
     * </p>
     *
     * @param speciesId the species ID
     * @param role      the role (e.g., "Leader", "Governor", "Captain")
     * @return a generated name with title
     */
    String generateAiCharacterName(String speciesId, String role);

    /**
     * Get the galaxy creation rules.
     *
     * @return the galaxy rules
     */
    E26GalaxyRules getGalaxyRules();

    /**
     * Get the star system creation rules.
     *
     * @return the system rules
     */
    E26SystemRules getSystemRules();

    /**
     * Get the planet creation rules.
     *
     * @return the planet rules
     */
    E26PlanetRules getPlanetRules();

    // ------------------------------------------------------------------
    // Planet Population from Game Data
    // ------------------------------------------------------------------

    /**
     * Populate a planet using game-provided character data.
     * <p>
     * This is the main method that other games call when they need a fully
     * populated planet or celestial body. The game passes all existing characters
     * (player-controlled and AI) that are already on the planet in its database.
     * Earth 2026 then:
     * </p>
     * <ol>
     *   <li>Finds or creates the planet object</li>
     *   <li>Reuses existing AI characters for country/region leadership roles</li>
     *   <li>Registers player characters on the planet</li>
     *   <li>Generates only the new AI characters needed for unfilled roles</li>
     *   <li>Returns the complete planet with all assignments</li>
     * </ol>
     * <p>
     * The requesting game identifies itself via {@code gameModuleName} so Earth 2026
     * can use the appropriate species name database for generating any new AI
     * character names that match the game's lore.
     * </p>
     *
     * @param request the planet population request containing game character data
     * @return the fully populated planet response with character assignments
     * @deprecated Use {@link #populatePlanetWithGameData(PlanetPopulationRequest, com.yisy.modules.doramcu.hex.PlanetPopulationProgress)} instead
     *             for streamed, batched population that avoids memory overrun on large planets.
     */
    @Deprecated
    com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationResponse populatePlanetWithGameData(
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request);

    /**
     * Populate a planet using game-provided character data, streaming progress
     * back via the provided callback.
     * <p>
     * This is the preferred method for planet population. Unlike the monolithic
     * {@link #populatePlanetWithGameData(PlanetPopulationRequest)} method, this
     * version builds the planet in phases and batches, calling back to the
     * {@code progress} callback after each phase completes. This allows the
     * requesting galaxy to receive planet data incrementally as it is built,
     * preventing memory overrun on large planets with many countries and regions.
     * </p>
     * <p>
     * The population proceeds in phases:
     * </p>
     * <ol>
     *   <li>Planet foundation — basic metadata (type, tech level, atmosphere)</li>
     *   <li>Countries — batched with their AI leaders</li>
     *   <li>Regions — batched per country with their AI governors</li>
     *   <li>Player characters — registered on the planet</li>
     *   <li>Planet overseer — assigned if needed</li>
     *   <li>Completion — final summary with counts</li>
     * </ol>
     * <p>
     * If {@code progress} is {@code null}, the method behaves like the deprecated
     * monolithic version but does not return a response object.
     * </p>
     *
     * @param request the planet population request containing game character data
     * @param progress optional callback for receiving phased progress updates
     */
    void populatePlanetWithGameData(
            com.yisy.modules.doramcu.earth2026.dto.PlanetPopulationRequest request,
            com.yisy.modules.doramcu.hex.PlanetPopulationProgress progress);
}

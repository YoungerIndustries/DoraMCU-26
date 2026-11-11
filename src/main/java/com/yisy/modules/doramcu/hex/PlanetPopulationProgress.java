package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Planet;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Region;
import com.yisy.modules.doramcu.eve.model.AiCharacter;

import java.util.List;
import java.util.Map;

/**
 * Callback interface for planet population progress reporting.
 * <p>
 * Implementations receive progress updates during planet population,
 * allowing the requesting galaxy to receive the planet data in modular
 * chunks as it is being built, rather than waiting for the entire planet
 * to be constructed in memory at once. This prevents memory overrun on
 * large planets with many countries and regions.
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
 */
public interface PlanetPopulationProgress {

    /**
     * Called when planet foundation is created.
     * <p>
     * The planet object contains only basic metadata: name, type, tech level,
     * atmosphere, gravity, etc. No countries or regions are populated yet.
     * </p>
     *
     * @param planet the planet with basic metadata
     */
    void onPlanetCreated(Earth2026Planet planet);

    /**
     * Called when a batch of countries has been populated.
     * <p>
     * Each country in the batch includes its AI leader assignment.
     * The batch size is typically small (e.g., 10-20 countries) to keep
     * memory usage bounded.
     * </p>
     *
     * @param countries the batch of countries with their AI leaders
     * @param countryIndex the 1-based index of the first country in this batch
     * @param totalCountries the total number of countries on the planet
     */
    void onCountryBatch(List<Earth2026Country> countries, int countryIndex, int totalCountries);

    /**
     * Called when a batch of regions has been populated for a specific country.
     * <p>
     * Each region in the batch includes its AI governor assignment.
     * </p>
     *
     * @param regions the batch of regions with their AI governors
     * @param countryId the country ID these regions belong to
     * @param regionIndex the 1-based index of the first region in this batch
     * @param totalRegions the total number of regions in this country
     */
    void onRegionBatch(List<Earth2026Region> regions, Long countryId, int regionIndex, int totalRegions);

    /**
     * Called when an AI leader is assigned to a country.
     * <p>
     * This provides fine-grained progress within a country batch.
     * </p>
     *
     * @param leader the AI character assigned as leader
     * @param countryId the country ID
     * @param isNew true if this is a newly generated AI character, false if reused from game data
     */
    default void onAiLeaderAssigned(AiCharacter leader, Long countryId, boolean isNew) {
        // Default no-op; override to capture individual leader assignments
    }

    /**
     * Called when an AI governor is assigned to a region.
     * <p>
     * This provides fine-grained progress within a region batch.
     * </p>
     *
     * @param governor the AI character assigned as governor
     * @param regionId the region ID
     * @param isNew true if this is a newly generated AI character, false if reused from game data
     */
    default void onAiGovernorAssigned(AiCharacter governor, Long regionId, boolean isNew) {
        // Default no-op; override to capture individual governor assignments
    }

    /**
     * Called when player characters have been registered on the planet.
     *
     * @param registeredPlayerCharacters map of game character ID to character name
     */
    default void onPlayerCharactersRegistered(Map<String, String> registeredPlayerCharacters) {
        // Default no-op; override to capture player character registration
    }

    /**
     * Called when a log message should be appended to the build log.
     * <p>
     * Implementations may display this in a text area, console, or other
     * logging surface.
     * </p>
     *
     * @param message the log message text
     */
    default void onLog(String message) {
        // Default no-op; override to capture log messages
    }

    /**
     * Called when planet population is complete.
     * <p>
     * The summary contains aggregate counts of all operations performed.
     * </p>
     *
     * @param summary the final population summary
     */
    void onComplete(PopulationSummary summary);

    /**
     * Summary of the planet population operation.
     */
    class PopulationSummary {
        private boolean planetFound;
        private int totalGameCharacters;
        private int countriesWithLeaders;
        private int regionsWithGovernors;
        private int aiCharactersReused;
        private int newAiCharactersGenerated;
        private int playerCharactersRegistered;

        public boolean isPlanetFound() {
            return planetFound;
        }

        public void setPlanetFound(boolean planetFound) {
            this.planetFound = planetFound;
        }

        public int getTotalGameCharacters() {
            return totalGameCharacters;
        }

        public void setTotalGameCharacters(int totalGameCharacters) {
            this.totalGameCharacters = totalGameCharacters;
        }

        public int getCountriesWithLeaders() {
            return countriesWithLeaders;
        }

        public void setCountriesWithLeaders(int countriesWithLeaders) {
            this.countriesWithLeaders = countriesWithLeaders;
        }

        public int getRegionsWithGovernors() {
            return regionsWithGovernors;
        }

        public void setRegionsWithGovernors(int regionsWithGovernors) {
            this.regionsWithGovernors = regionsWithGovernors;
        }

        public int getAiCharactersReused() {
            return aiCharactersReused;
        }

        public void setAiCharactersReused(int aiCharactersReused) {
            this.aiCharactersReused = aiCharactersReused;
        }

        public int getNewAiCharactersGenerated() {
            return newAiCharactersGenerated;
        }

        public void setNewAiCharactersGenerated(int newAiCharactersGenerated) {
            this.newAiCharactersGenerated = newAiCharactersGenerated;
        }

        public int getPlayerCharactersRegistered() {
            return playerCharactersRegistered;
        }

        public void setPlayerCharactersRegistered(int playerCharactersRegistered) {
            this.playerCharactersRegistered = playerCharactersRegistered;
        }
    }
}

package com.yisy.modules.doramcu.earth2026.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for a game to provide its existing planet data to Earth 2026.
 * <p>
 * When a game requests a planet or celestial body from Earth 2026, it passes
 * this object containing all characters (both player-controlled and AI) that
 * already exist on that planet in the game's database. Earth 2026 uses this
 * data to:
 * </p>
 * <ul>
 *   <li>Reuse existing AI characters for country/region leadership roles</li>
 *   <li>Avoid creating duplicate characters</li>
 *   <li>Generate only the new AI characters needed for unfilled roles</li>
 *   <li>Map game-specific character data to Earth 2026 planet structure</li>
 * </ul>
 * <p>
 * The requesting game identifies itself via {@code gameModuleName} so Earth 2026
 * can use the appropriate species name database for generating any new AI
 * character names.
 * </p>
 */
public class PlanetPopulationRequest {

    /**
     * Name of the requesting game module (e.g., "TRAVELLER", "CAPTAINS_LOG", "BABYLON5").
     * Used to determine which species name database to use for new AI characters.
     */
    private String gameModuleName;

    /**
     * The planet or celestial body name (e.g., "Earth", "Terra", "Minbar").
     */
    private String planetName;

    /**
     * The planet type (e.g., "Terrestrial", "Gas Giant", "Moon").
     */
    private String planetType;

    /**
     * Tech level of the planet (0-20, Traveller-style).
     */
    private Integer techLevel;

    /**
     * Time period template name (e.g., "Modern", "World War II", "Space Age").
     */
    private String timePeriod;

    /**
     * All characters currently on this planet in the requesting game's database.
     * Includes both player characters and AI characters.
     */
    private List<PlanetCharacterInfo> existingCharacters;

    /**
     * Species ID to use for generating new AI characters.
     * If null, Earth 2026 defaults to "Human".
     */
    private String speciesIdForNewAi;

    /**
     * Game-specific planet metadata (custom rules, modifiers, etc.).
     * Earth 2026 stores these for reference but does not interpret them.
     */
    private Map<String, Object> gamePlanetMetadata;

    /**
     * Whether to force regeneration of all AI characters, ignoring existing ones.
     * Useful for reset scenarios.
     */
    private boolean forceRegenerate;

    public PlanetPopulationRequest() {
    }

    public PlanetPopulationRequest(String gameModuleName, String planetName) {
        this.gameModuleName = gameModuleName;
        this.planetName = planetName;
        this.planetType = "Terrestrial";
        this.techLevel = 5;
        this.forceRegenerate = false;
    }

    public String getGameModuleName() {
        return gameModuleName;
    }

    public void setGameModuleName(String gameModuleName) {
        this.gameModuleName = gameModuleName;
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public String getPlanetType() {
        return planetType;
    }

    public void setPlanetType(String planetType) {
        this.planetType = planetType;
    }

    public Integer getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(Integer techLevel) {
        this.techLevel = techLevel;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public List<PlanetCharacterInfo> getExistingCharacters() {
        return existingCharacters;
    }

    public void setExistingCharacters(List<PlanetCharacterInfo> existingCharacters) {
        this.existingCharacters = existingCharacters;
    }

    public String getSpeciesIdForNewAi() {
        return speciesIdForNewAi;
    }

    public void setSpeciesIdForNewAi(String speciesIdForNewAi) {
        this.speciesIdForNewAi = speciesIdForNewAi;
    }

    public Map<String, Object> getGamePlanetMetadata() {
        return gamePlanetMetadata;
    }

    public void setGamePlanetMetadata(Map<String, Object> gamePlanetMetadata) {
        this.gamePlanetMetadata = gamePlanetMetadata;
    }

    public boolean isForceRegenerate() {
        return forceRegenerate;
    }

    public void setForceRegenerate(boolean forceRegenerate) {
        this.forceRegenerate = forceRegenerate;
    }

    @Override
    public String toString() {
        return "PlanetPopulationRequest{" +
                "game='" + gameModuleName + '\'' +
                ", planet='" + planetName + '\'' +
                ", type='" + planetType + '\'' +
                ", techLevel=" + techLevel +
                ", period='" + timePeriod + '\'' +
                ", characters=" + (existingCharacters != null ? existingCharacters.size() : 0) +
                ", speciesForNewAi='" + speciesIdForNewAi + '\'' +
                ", forceRegenerate=" + forceRegenerate +
                '}';
    }
}

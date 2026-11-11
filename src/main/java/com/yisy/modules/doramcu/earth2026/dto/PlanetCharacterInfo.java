package com.yisy.modules.doramcu.earth2026.dto;

import java.util.Map;

/**
 * DTO representing a single character from a game's planet data.
 * <p>
 * When a game requests a planet from Earth 2026, it passes a list of these
 * objects containing all player characters and AI characters already existing
 * on that planet in the game's database. Earth 2026 uses this data to avoid
 * creating duplicate characters and only generates new AI characters for
 * roles not yet filled.
 * </p>
 * <p>
 * Each character includes its game-specific ID, name, role, species/race,
 * and any game-specific attributes. The {@code isPlayerControlled} flag
 * distinguishes player characters from AI characters.
 * </p>
 */
public class PlanetCharacterInfo {

    /**
     * The character's ID in the requesting game's database.
     * Used to reference the character back to the game.
     */
    private String gameId;

    /**
     * Display name of the character.
     */
    private String name;

    /**
     * Role on the planet (e.g., "Leader", "Governor", "Captain", "Adventurer").
     */
    private String role;

    /**
     * Species or race ID from the Earth 2026 rules database
     * (e.g., "Human", "Aslan", "Klingon", "Narn").
     */
    private String speciesId;

    /**
     * Whether this character is controlled by a human player.
     * If false, this is an AI character.
     */
    private boolean isPlayerControlled;

    /**
     * The empire, faction, or organization this character belongs to.
     */
    private String empireName;

    /**
     * The country or region this character is associated with on the planet.
     */
    private String countryOrRegion;

    /**
     * Game-specific attributes (stats, skills, etc.) as key-value pairs.
     * Earth 2026 stores these for reference but does not interpret them.
     */
    private Map<String, Object> gameAttributes;

    /**
     * Description or personality notes for AI characters.
     */
    private String description;

    /**
     * Whether this character is currently active on the planet.
     */
    private boolean active;

    public PlanetCharacterInfo() {
    }

    public PlanetCharacterInfo(String gameId, String name, String role, String speciesId) {
        this.gameId = gameId;
        this.name = name;
        this.role = role;
        this.speciesId = speciesId;
        this.isPlayerControlled = false;
        this.active = true;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public boolean isPlayerControlled() {
        return isPlayerControlled;
    }

    public void setPlayerControlled(boolean playerControlled) {
        isPlayerControlled = playerControlled;
    }

    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(String empireName) {
        this.empireName = empireName;
    }

    public String getCountryOrRegion() {
        return countryOrRegion;
    }

    public void setCountryOrRegion(String countryOrRegion) {
        this.countryOrRegion = countryOrRegion;
    }

    public Map<String, Object> getGameAttributes() {
        return gameAttributes;
    }

    public void setGameAttributes(Map<String, Object> gameAttributes) {
        this.gameAttributes = gameAttributes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "PlanetCharacterInfo{" +
                "gameId='" + gameId + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", species='" + speciesId + '\'' +
                ", player=" + isPlayerControlled +
                ", empire='" + empireName + '\'' +
                ", active=" + active +
                '}';
    }
}

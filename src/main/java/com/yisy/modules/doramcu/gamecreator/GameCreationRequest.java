package com.yisy.modules.doramcu.gamecreator;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for creating a new game via the GameCreatorService.
 * <p>
 * The player selects a lore/universe and checks boxes for the desired game modes.
 * If the lore doesn't exist, it will be fetched from the internet automatically.
 * </p>
 */
public class GameCreationRequest {

    /**
     * Name of the lore/universe (e.g., "Star Wars", "Dune", "Warhammer 40K").
     * If not found in the database, it will be fetched from the internet.
     */
    @NotBlank(message = "Lore name is required")
    private String loreName;

    /**
     * Custom name for the game/galaxy (optional — defaults to lore name + " Galaxy").
     */
    private String gameName;

    /**
     * Selected game modes (at least one required).
     * Each mode represents a checkbox the player selected:
     * RPG, 4X, Tycoon, Tactical, Survival, Sandbox, MMORPG, Roguelike, Simulation, Adventure.
     */
    @NotEmpty(message = "At least one game mode must be selected")
    private List<GameMode> gameModes;

    /**
     * Optional: override the number of solar systems.
     */
    private Integer systemCount;

    /**
     * Optional: override the difficulty level.
     */
    private String difficultyLevel;

    /**
     * Optional: whether to enable the 4X progressive unlock layer.
     */
    private boolean enable4xLayer;

    // --- Getters and Setters ---

    public String getLoreName() {
        return loreName;
    }

    public void setLoreName(String loreName) {
        this.loreName = loreName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public List<GameMode> getGameModes() {
        return gameModes;
    }

    public void setGameModes(List<GameMode> gameModes) {
        this.gameModes = gameModes;
    }

    public Integer getSystemCount() {
        return systemCount;
    }

    public void setSystemCount(Integer systemCount) {
        this.systemCount = systemCount;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public boolean isEnable4xLayer() {
        return enable4xLayer;
    }

    public void setEnable4xLayer(boolean enable4xLayer) {
        this.enable4xLayer = enable4xLayer;
    }
}

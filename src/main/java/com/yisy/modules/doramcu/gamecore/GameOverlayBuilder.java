package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;

/**
 * Interface for game-type-specific overlay builders.
 * <p>
 * Each game module (Star Wars, Traveller, D&D, etc.) implements this interface
 * to add themed content on top of the base 4X galaxy structure: faction-based
 * empires, lore-specific solar systems, species, and game-specific data.
 * </p>
 * <p>
 * The overlay builder receives an already-generated galaxy with sectors, systems,
 * planets, and generic empires from the base 4X layer, and enriches it with
 * game-specific content.
 * </p>
 */
public interface GameOverlayBuilder {

    /**
     * Returns the game type this builder provides overlay content for.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Builds the game-specific overlay on top of an existing galaxy.
     * <p>
     * This method is called after the base 4X galaxy is generated and persisted.
     * The builder adds themed empires, lore systems, and game-specific data.
     * </p>
     *
     * @param galaxy the in-memory galaxy to enrich
     * @param config the galaxy generation configuration
     */
    void buildOverlay(Galaxy galaxy, GalaxyGenerationConfig config);
}

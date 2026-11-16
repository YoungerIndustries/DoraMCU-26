package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.hex.Galaxy;

/**
 * Interface for game-type-specific AI NPC population.
 * <p>
 * Each game module (Star Wars, Traveller, D&D, etc.) implements this interface
 * to create and register AI NPCs as active characters in the galaxy.
 * NPCs are registered via {@link com.yisy.modules.doramcu.service.AiEntityRegistrationService}
 * as {@link com.yisy.modules.doramcu.model.AiCharacter} entities.
 * </p>
 * <p>
 * The NPC populator receives an already-generated galaxy with overlay content
 * and creates themed AI characters (Yoda, Traveller NPCs, D&D monsters, etc.)
 * that perform activities in the galaxy.
 * </p>
 */
public interface AiNpcPopulator {

    /**
     * Returns the game type this populator provides AI NPCs for.
     *
     * @return the game type
     */
    GameType getGameType();

    /**
     * Populates the galaxy with AI NPCs.
     * <p>
     * This method is called after the base 4X galaxy and game overlay are built.
     * The populator creates AI characters and registers them as active entities.
     * </p>
     *
     * @param galaxy the in-memory galaxy to populate NPCs into
     */
    void populateNpcs(Galaxy galaxy);
}

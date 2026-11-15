package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.hex.manager.GameContext;

/**
 * SPI interface for recording AI commander battle experience.
 * <p>
 * This interface allows hex domain objects to record battle experience
 * and advance training levels without creating a circular dependency
 * on the service layer.
 * </p>
 */
public interface AiCommanderExperienceSpi {

	/**
	 * Records battle experience for an AI character.
	 *
	 * @param aiCharacterId the AI character ID
	 * @param gameModuleName the game module name
	 * @param battleType the type of battle
	 * @param outcome the battle outcome
	 * @param description optional description
	 * @param location optional location
	 * @return the saved experience record
	 */
	AiCommanderExperience recordBattleExperience(
			Long aiCharacterId, String gameModuleName, String battleType,
			AiCommanderExperience.Outcome outcome, String description, String location);

	/**
	 * Advances the training level of an AI character if XP threshold is reached.
	 *
	 * @param aiCharacterId the AI character ID
	 * @return true if the training level was advanced
	 */
	boolean advanceTrainingLevel(Long aiCharacterId);

	/**
	 * Sets the game context for AI Core tier synchronization.
	 *
	 * @param gameContext the game context
	 */
	void setGameContext(GameContext gameContext);
}

package com.yisy.modules.doramcu.thirdreich.adapter;

import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;

import java.util.List;
import java.util.Map;

/**
 * Interface for adapting game-specific ground units to the 3R4 combat engine.
 * <p>
 * Each game system (Star Trek, Traveller, DoraMCU 4X) provides an adapter
 * that converts its units to 3R4 equivalents, resolves battles, and maps
 * results back to the game's domain.
 * </p>
 */
public interface GroundCombatAdapter {

    /**
     * Converts game-specific units to 3R4 ground units.
     *
     * @param gameUnits the game-specific units (type depends on the game)
     * @return list of 3R4 ground units
     */
    List<ThirdReichGroundUnit> to3R4Units(List<?> gameUnits);

    /**
     * Converts a single game-specific unit to a 3R4 ground unit.
     *
     * @param gameUnit the game-specific unit
     * @return the 3R4 ground unit
     */
    ThirdReichGroundUnit to3R4Unit(Object gameUnit);

    /**
     * Maps a 3R4 battle result back to the game's domain.
     *
     * @param result the 3R4 battle result
     * @return a map containing the mapped result data
     */
    Map<String, Object> mapResult(ThirdReichBattleResult result);

    /**
     * Converts a game character to an AI commander.
     *
     * @param gameCharacter the game-specific character
     * @return the AI commander
     */
    AICommander toCommander(Object gameCharacter);

    /**
     * Returns the source game name for this adapter.
     *
     * @return the game name
     */
    String getSourceGame();
}

package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.FourxUnlockState;
import com.yisy.modules.doramcu.model.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service that tracks progressive unlock of 4X layers in RPG-first games.
 * <p>
 * Each 4X feature (Exploration, Diplomacy, Economy, Combat) starts locked
 * and unlocks when the player reaches the corresponding milestone:
 * <ul>
 *   <li>Exploration: 5 systems discovered</li>
 *   <li>Diplomacy: 1 NPC contact</li>
 *   <li>Economy: 3 colonies established</li>
 *   <li>Combat: 1 battle fought</li>
 * </ul>
 * </p>
 * <p>
 * The service is idempotent — calling milestone methods multiple times
 * does not cause duplicate unlocks.
 * </p>
 */
@Service
public class FourxProgressiveUnlockService {

    private static final Logger log = LoggerFactory.getLogger(FourxProgressiveUnlockService.class);

    /**
     * Records that a new system has been discovered by the player.
     * <p>
     * When the total reaches {@link FourxUnlockState#EXPLORATION_THRESHOLD},
     * the Exploration 4X layer is unlocked.
     * </p>
     *
     * @param session the current game session
     * @return true if the Exploration layer was just unlocked, false otherwise
     */
    public boolean recordSystemDiscovered(GameSession session) {
        FourxUnlockState state = session.getFourxUnlockState();
        int previous = state.getSystemsDiscovered();
        state.setSystemsDiscovered(previous + 1);

        if (!state.isExplorationUnlocked() && state.getSystemsDiscovered() >= FourxUnlockState.EXPLORATION_THRESHOLD) {
            state.setExplorationUnlocked(true);
            log.info("Exploration 4X layer unlocked for session {} after discovering {} systems",
                    session.getSessionId(), state.getSystemsDiscovered());
            return true;
        }

        return false;
    }

    /**
     * Records that the player has made contact with an NPC faction.
     * <p>
     * When the total reaches {@link FourxUnlockState#DIPLOMACY_THRESHOLD},
     * the Diplomacy 4X layer is unlocked.
     * </p>
     *
     * @param session the current game session
     * @return true if the Diplomacy layer was just unlocked, false otherwise
     */
    public boolean recordNpcContact(GameSession session) {
        FourxUnlockState state = session.getFourxUnlockState();
        int previous = state.getNpcContacts();
        state.setNpcContacts(previous + 1);

        if (!state.isDiplomacyUnlocked() && state.getNpcContacts() >= FourxUnlockState.DIPLOMACY_THRESHOLD) {
            state.setDiplomacyUnlocked(true);
            log.info("Diplomacy 4X layer unlocked for session {} after {} NPC contacts",
                    session.getSessionId(), state.getNpcContacts());
            return true;
        }

        return false;
    }

    /**
     * Records that the player has established a new colony.
     * <p>
     * When the total reaches {@link FourxUnlockState#ECONOMY_THRESHOLD},
     * the Economy 4X layer is unlocked.
     * </p>
     *
     * @param session the current game session
     * @return true if the Economy layer was just unlocked, false otherwise
     */
    public boolean recordColonyEstablished(GameSession session) {
        FourxUnlockState state = session.getFourxUnlockState();
        int previous = state.getColoniesEstablished();
        state.setColoniesEstablished(previous + 1);

        if (!state.isEconomyUnlocked() && state.getColoniesEstablished() >= FourxUnlockState.ECONOMY_THRESHOLD) {
            state.setEconomyUnlocked(true);
            log.info("Economy 4X layer unlocked for session {} after establishing {} colonies",
                    session.getSessionId(), state.getColoniesEstablished());
            return true;
        }

        return false;
    }

    /**
     * Records that the player has fought a battle.
     * <p>
     * When the total reaches {@link FourxUnlockState#COMBAT_THRESHOLD},
     * the Combat 4X layer is unlocked.
     * </p>
     *
     * @param session the current game session
     * @return true if the Combat layer was just unlocked, false otherwise
     */
    public boolean recordBattleFought(GameSession session) {
        FourxUnlockState state = session.getFourxUnlockState();
        int previous = state.getBattlesFought();
        state.setBattlesFought(previous + 1);

        if (!state.isCombatUnlocked() && state.getBattlesFought() >= FourxUnlockState.COMBAT_THRESHOLD) {
            state.setCombatUnlocked(true);
            log.info("Combat 4X layer unlocked for session {} after {} battles",
                    session.getSessionId(), state.getBattlesFought());
            return true;
        }

        return false;
    }

    /**
     * Checks if the Exploration 4X layer is unlocked for the given session.
     *
     * @param session the game session
     * @return true if exploration is unlocked
     */
    public boolean isExplorationUnlocked(GameSession session) {
        return session.getFourxUnlockState().isExplorationUnlocked();
    }

    /**
     * Checks if the Diplomacy 4X layer is unlocked for the given session.
     *
     * @param session the game session
     * @return true if diplomacy is unlocked
     */
    public boolean isDiplomacyUnlocked(GameSession session) {
        return session.getFourxUnlockState().isDiplomacyUnlocked();
    }

    /**
     * Checks if the Economy 4X layer is unlocked for the given session.
     *
     * @param session the game session
     * @return true if economy is unlocked
     */
    public boolean isEconomyUnlocked(GameSession session) {
        return session.getFourxUnlockState().isEconomyUnlocked();
    }

    /**
     * Checks if the Combat 4X layer is unlocked for the given session.
     *
     * @param session the game session
     * @return true if combat is unlocked
     */
    public boolean isCombatUnlocked(GameSession session) {
        return session.getFourxUnlockState().isCombatUnlocked();
    }

    /**
     * Checks if any 4X layer is unlocked for the given session.
     *
     * @param session the game session
     * @return true if at least one 4X layer is unlocked
     */
    public boolean isAnyUnlocked(GameSession session) {
        return session.getFourxUnlockState().isAnyUnlocked();
    }

    /**
     * Checks if all 4X layers are unlocked for the given session.
     *
     * @param session the game session
     * @return true if all 4X layers are unlocked
     */
    public boolean isAllUnlocked(GameSession session) {
        return session.getFourxUnlockState().isAllUnlocked();
    }

    /**
     * Returns the current unlock state for the given session.
     *
     * @param session the game session
     * @return the fourx unlock state
     */
    public FourxUnlockState getUnlockState(GameSession session) {
        return session.getFourxUnlockState();
    }
}

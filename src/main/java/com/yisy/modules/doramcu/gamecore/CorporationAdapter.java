package com.yisy.modules.doramcu.gamecore;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.util.List;

/**
 * Interface for game modules to provide universe-specific corporation contracts.
 * <p>
 * Each game module implements this interface to generate contracts that fit its
 * universe's lore and mechanics. For example, Starship Troopers provides bug hunts
 * and planetary defense contracts, while Traveller provides trade deals and exploration missions.
 * </p>
 * <p>
 * If a module does not implement this interface, the {@link UniversalCorporationAdapter}
 * provides generic contracts as a fallback.
 * </p>
 */
public interface CorporationAdapter {

    /**
     * Returns the game type this adapter serves.
     *
     * @return the game type enum value
     */
    GameType getGameType();

    /**
     * Generates a list of universe-specific contracts.
     *
     * @param count number of contracts to generate
     * @return list of corporate contracts for this universe
     */
    List<CorporateContract> generateContracts(int count);

    /**
     * Returns the contract types available in this universe.
     * May include universe-specific types beyond the generic TRADE/SECURITY/LOGISTICS/MISSION.
     *
     * @return list of contract type strings
     */
    List<String> getContractTypes();

    /**
     * Generates a lore-appropriate title for a contract based on its type and difficulty.
     *
     * @param type the contract type (e.g., "TRADE", "SECURITY")
     * @param difficulty the contract difficulty (1-10)
     * @return a human-readable contract title
     */
    String getContractTitle(String type, int difficulty);

    /**
     * Calculates a performance modifier for crew working on contracts from a different universe.
     * <p>
     * Same-universe crew gets a bonus (+10%), different-universe crew gets a small penalty (-5%).
     * </p>
     *
     * @param crewOrigin the game type of the crew member's origin
     * @param contractOrigin the game type of the contract's origin
     * @return performance modifier (e.g., 1.1 for +10%, 0.95 for -5%)
     */
    default double getPerformanceModifier(GameType crewOrigin, GameType contractOrigin) {
        if (crewOrigin == contractOrigin) {
            return 1.1; // +10% same-universe bonus
        }
        return 0.95; // -5% different-universe penalty
    }

    /**
     * Returns the display name for this universe.
     *
     * @return human-readable universe name
     */
    default String getUniverseDisplayName() {
        return getGameType().getDisplayName();
    }
}

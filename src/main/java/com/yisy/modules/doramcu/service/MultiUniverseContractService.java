package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.babylon5.Babylon5CorporationAdapter;
import com.yisy.modules.doramcu.dnd5e.Dnd5eCorporationAdapter;
import com.yisy.modules.doramcu.earth2026.Earth2026CorporationAdapter;
import com.yisy.modules.doramcu.fps.FpsCorporationAdapter;
import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.gamecore.UniversalCorporationAdapter;
import com.yisy.modules.doramcu.hex.planet.TransportTycoonCorporationAdapter;
import com.yisy.modules.doramcu.mechwarrior.MechWarriorCorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.sfb.SfbCorporationAdapter;
import com.yisy.modules.doramcu.star_trek_4x.StarTrek4xCorporationAdapter;
import com.yisy.modules.doramcu.starwars.StarWarsCorporationAdapter;
import com.yisy.modules.doramcu.starshiptroopers.StarshipTroopersCorporationAdapter;
import com.yisy.modules.doramcu.traveller.TravellerCorporationAdapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.zero_zero_seven.ZeroZeroSevenCorporationAdapter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that manages contracts across all game universes.
 * <p>
 * Delegates contract generation to universe-specific {@link CorporationAdapter} implementations.
 * Provides cross-universe performance modifiers and contract browsing by universe.
 * </p>
 */
@Service
public class MultiUniverseContractService {

    private final Map<GameType, CorporationAdapter> adapters;

    public MultiUniverseContractService() {
        // Register all universe-specific adapters
        List<CorporationAdapter> adapterList = new ArrayList<>();
        adapterList.add(new TravellerCorporationAdapter());
        adapterList.add(new StarshipTroopersCorporationAdapter());
        adapterList.add(new Babylon5CorporationAdapter());
        adapterList.add(new Dnd5eCorporationAdapter());
        adapterList.add(new Earth2026CorporationAdapter());
        adapterList.add(new StarTrek4xCorporationAdapter());
        adapterList.add(new SfbCorporationAdapter());
        adapterList.add(new ZeroZeroSevenCorporationAdapter());
        adapterList.add(new FpsCorporationAdapter());
        adapterList.add(new MechWarriorCorporationAdapter());
        adapterList.add(new StarWarsCorporationAdapter());
        adapterList.add(new TransportTycoonCorporationAdapter());

        // Universal fallback for any game type not explicitly registered
        UniversalCorporationAdapter universalAdapter = new UniversalCorporationAdapter();

        this.adapters = Stream.concat(
                        adapterList.stream(),
                        Stream.of(universalAdapter)
                ).collect(Collectors.toMap(
                        CorporationAdapter::getGameType,
                        Function.identity(),
                        (a, b) -> a // keep first in case of duplicates
                ));
    }

    /**
     * Generates contracts for a specific universe.
     *
     * @param gameType the game type to generate contracts for
     * @param count    number of contracts to generate
     * @return list of contracts from the specified universe
     */
    public List<CorporateContract> generateContracts(GameType gameType, int count) {
        CorporationAdapter adapter = adapters.getOrDefault(gameType, new UniversalCorporationAdapter());
        return adapter.generateContracts(count);
    }

    /**
     * Generates contracts from all universes.
     *
     * @param perType number of contracts per universe
     * @return list of contracts from all universes
     */
    public List<CorporateContract> generateAllUniverseContracts(int perType) {
        List<CorporateContract> allContracts = new ArrayList<>();
        for (CorporationAdapter adapter : adapters.values()) {
            allContracts.addAll(adapter.generateContracts(perType));
        }
        return allContracts;
    }

    /**
     * Returns the available contract types for a specific universe.
     *
     * @param gameType the game type
     * @return list of contract type strings
     */
    public List<String> getContractTypes(GameType gameType) {
        CorporationAdapter adapter = adapters.getOrDefault(gameType, new UniversalCorporationAdapter());
        return adapter.getContractTypes();
    }

    /**
     * Calculates a performance modifier for crew working on contracts from a different universe.
     *
     * @param crewOrigin     the game type of the crew member's origin
     * @param contractOrigin the game type of the contract's origin
     * @return performance modifier (e.g., 1.1 for +10%, 0.95 for -5%)
     */
    public double getPerformanceModifier(GameType crewOrigin, GameType contractOrigin) {
        if (crewOrigin == contractOrigin) {
            return 1.1; // +10% same-universe bonus
        }
        return 0.95; // -5% different-universe penalty
    }

    /**
     * Returns all registered game types with corporation support.
     *
     * @return list of game types
     */
    public List<GameType> getSupportedGameTypes() {
        return new ArrayList<>(adapters.keySet());
    }

    /**
     * Returns the adapter for a specific game type.
     *
     * @param gameType the game type
     * @return the corporation adapter, or universal fallback if not found
     */
    public CorporationAdapter getAdapter(GameType gameType) {
        return adapters.getOrDefault(gameType, new UniversalCorporationAdapter());
    }
}

package com.yisy.modules.doramcu.starwars;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Star Wars universe.
 * Provides smuggling runs, rebel supply, and imperial contracts.
 */
@Component
public class StarWarsCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Smuggling Run Deal", "Hutt Commerce Agreement", "Outer Rim Trade Route",
            "Coruscant Market Contract", "Spice Trade Operation"
    };

    private static final String[] SECURITY_TITLES = {
            "Rebel Cell Defense", "Imperial Patrol Op", "Mandalorian Mercenary Deal",
            "Sith Threat Response", "Jedi Protection Detail"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Rebel Supply Run", "Death Star Evacuation", "Starfighter Resupply Deal",
            "Base Construction Logistics", "Hyperlane Transport Mission"
    };

    private static final String[] MISSION_TITLES = {
            "Droid Recovery Mission", "Holocron Search Deal", "Force Sensitivity Investigation",
            "Planetary Liberation Op", "Galactic Senate Envoy"
    };

    @Override
    public GameType getGameType() {
        return GameType.STAR_WARS;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1500 + RANDOM.nextInt(7500);
            int xp = difficulty * 75 + RANDOM.nextInt(300);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Star Wars " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.STAR_WARS);
            contract.setUniverseDisplayName("Star Wars");

            contracts.add(contract);
        }

        return contracts;
    }

    @Override
    public List<String> getContractTypes() {
        return List.of("TRADE", "SECURITY", "LOGISTICS", "MISSION");
    }

    @Override
    public String getContractTitle(String type, int difficulty) {
        String[] titles;
        switch (type.toUpperCase()) {
            case "TRADE": titles = TRADE_TITLES; break;
            case "SECURITY": titles = SECURITY_TITLES; break;
            case "LOGISTICS": titles = LOGISTICS_TITLES; break;
            default: titles = MISSION_TITLES; break;
        }
        String base = titles[RANDOM.nextInt(titles.length)];
        if (difficulty >= 8) return base + " (Galactic)";
        if (difficulty >= 5) return base + " (Sector)";
        return base + " (Local)";
    }
}

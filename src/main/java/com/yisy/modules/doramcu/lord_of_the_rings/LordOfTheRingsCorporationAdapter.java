package com.yisy.modules.doramcu.lord_of_the_rings;

import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Lord of the Rings universe.
 * Provides trade routes, realm defense, and quest contracts.
 */
@Component
public class LordOfTheRingsCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Trade Route Deal", "Gondor Commerce Agreement", "Rohan Supply Contract",
            "Elven Craftsmanship Deal", "Dwarven Mining Operation"
    };

    private static final String[] SECURITY_TITLES = {
            "Realm Defense Op", "Orc Patrol Response", "Ranger Escort Deal",
            "Mordor Threat Response", "White Council Protection"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Supply Run to Gondor", "Rohan Reinforcement Deal", "Elven Aid Transport",
            "Fortress Construction Logistics", "Beacon Network Mission"
    };

    private static final String[] MISSION_TITLES = {
            "Ring Recovery Mission", "Palantír Search Deal", "Ancient Artifact Investigation",
            "Realm Liberation Op", "Council of Elrond Envoy"
    };

    @Override
    public GameType getGameType() {
        return GameType.LORD_OF_THE_RINGS;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1000 + RANDOM.nextInt(5000);
            int xp = difficulty * 50 + RANDOM.nextInt(200);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Lord of the Rings " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.LORD_OF_THE_RINGS);
            contract.setUniverseDisplayName("Lord of the Rings");

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
        if (difficulty >= 5) return base + " (Realm)";
        return base + " (Local)";
    }
}

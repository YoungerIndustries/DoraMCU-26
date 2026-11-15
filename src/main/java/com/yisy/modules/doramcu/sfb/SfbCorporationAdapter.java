package com.yisy.modules.doramcu.sfb;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Star Frontiers Battle universe.
 * Provides frontier exploration, pirate suppression, and colony supply contracts.
 */
@Component
public class SfbCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Frontier Trade Route", "Colony Supply Deal", "Resource Extraction Contract",
            "Outpost Commerce Agreement", "Interstellar Freight Deal"
    };

    private static final String[] SECURITY_TITLES = {
            "Pirate Suppression Op", "Border Patrol Mission", "Raid Response Detail",
            "Outpost Defense Contract", "Smuggling Interdiction"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Colony Resupply Run", "Shipyard Delivery Deal", "Emergency Evacuation Mission",
            "Supply Drop Operation", "Frontier Transport Contract"
    };

    private static final String[] MISSION_TITLES = {
            "Exploration Survey Mission", "First Contact Protocol", "Alien Artifact Recovery",
            "Frontier Scout Assignment", "Diplomatic Envoy Deal"
    };

    @Override
    public GameType getGameType() {
        return GameType.SFB;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1100 + RANDOM.nextInt(5500);
            int xp = difficulty * 55 + RANDOM.nextInt(220);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Star Frontiers " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.SFB);
            contract.setUniverseDisplayName("Star Frontiers");

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
        if (difficulty >= 8) return base + " (Critical)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Routine)";
    }
}

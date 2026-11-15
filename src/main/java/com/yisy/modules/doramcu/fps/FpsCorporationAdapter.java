package com.yisy.modules.doramcu.fps;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the FPS universe.
 * Provides tactical operations and objective missions.
 */
@Component
public class FpsCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Ammunition Supply Deal", "Equipment Procurement", "Intel Broker Contract",
            "Safehouse Network Deal", "Black Market Operation"
    };

    private static final String[] SECURITY_TITLES = {
            "Hostage Rescue Mission", "Bomb Defusal Operation", "VIP Extraction Detail",
            "Perimeter Defense Contract", "Counter-Terrorism Op"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Supply Drop Mission", "Evacuation Coordination", "Armory Resupply Deal",
            "Recon Transport Operation", "Field Base Setup"
    };

    private static final String[] MISSION_TITLES = {
            "Objective Assault Mission", "Stealth Infiltration Op", "Intel Gathering Deal",
            "Sabotage Operation", "Special Forces Assignment"
    };

    @Override
    public GameType getGameType() {
        return GameType.FPS;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 900 + RANDOM.nextInt(4500);
            int xp = difficulty * 45 + RANDOM.nextInt(200);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("An FPS " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.FPS);
            contract.setUniverseDisplayName("FPS");

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
        if (difficulty >= 8) return base + " (Hardcore)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Casual)";
    }
}

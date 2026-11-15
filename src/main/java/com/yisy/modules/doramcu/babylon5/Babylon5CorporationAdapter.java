package com.yisy.modules.doramcu.babylon5;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Babylon 5 universe.
 * Provides diplomatic missions, station operations, and shadow intelligence contracts.
 */
@Component
public class Babylon5CorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Station Supply Contract", "Minbari Trade Agreement", "Centauri Commerce Deal",
            "Narn Resource Exchange", "EarthForce Provisioning"
    };

    private static final String[] SECURITY_TITLES = {
            "Shadow Threat Response", "Minbari Border Patrol", "Terran Alliance Defense",
            "Ranger Intelligence Op", "Station Security Detail"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Docking Port Resupply", "Evacuation Transport", "Refugee Relief Mission",
            "Station Maintenance Run", "Hyperlane Supply Chain"
    };

    private static final String[] MISSION_TITLES = {
            "Diplomatic Envoy Mission", "First Contact Protocol", "Shadow Intelligence Gathering",
            "Ranger Assignment", "Interplanetary Coalition Deal"
    };

    @Override
    public GameType getGameType() {
        return GameType.BABYLON_5;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1200 + RANDOM.nextInt(6000);
            int xp = difficulty * 60 + RANDOM.nextInt(250);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Babylon 5 " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.BABYLON_5);
            contract.setUniverseDisplayName("Babylon 5");

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
        if (difficulty >= 8) return base + " (Urgent)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Routine)";
    }
}

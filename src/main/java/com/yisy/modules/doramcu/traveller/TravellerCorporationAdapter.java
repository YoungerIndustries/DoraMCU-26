package com.yisy.modules.doramcu.traveller;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Traveller universe.
 * Provides trade deals, exploration contracts, and diplomacy missions.
 */
@Component
public class TravellerCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Spinward Trade Route", "Core Worlds Commodity Deal", "Rimworld Resource Exchange",
            "Void Market Contract", "Astrogation Supply Run"
    };

    private static final String[] SECURITY_TITLES = {
            "Mongoose Patrol Duty", "Pirate Suppression Operation", "Subsector Security Detail",
            "Refugee Convoy Escort", "Smuggling Interdiction Mission"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Way Station Resupply", "Shipyard Freight Delivery", "Colony Outfitting Run",
            "Trade Port Logistics", "Emergency Supply Drop"
    };

    private static final String[] MISSION_TITLES = {
            "Exploration Survey Mission", "Diplomatic Envoy Assignment", "Ancient Ruins Expedition",
            "Alien Contact Protocol", "Subsector Intelligence Gathering"
    };

    @Override
    public GameType getGameType() {
        return GameType.TRAVELLER;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1500 + RANDOM.nextInt(8000);
            int xp = difficulty * 75 + RANDOM.nextInt(300);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Traveller " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.TRAVELLER);
            contract.setUniverseDisplayName("Traveller");

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
            case "TRADE":
                titles = TRADE_TITLES;
                break;
            case "SECURITY":
                titles = SECURITY_TITLES;
                break;
            case "LOGISTICS":
                titles = LOGISTICS_TITLES;
                break;
            case "MISSION":
                titles = MISSION_TITLES;
                break;
            default:
                titles = MISSION_TITLES;
                break;
        }
        String base = titles[RANDOM.nextInt(titles.length)];
        if (difficulty >= 8) {
            return base + " (Urgent)";
        } else if (difficulty >= 5) {
            return base + " (Standard)";
        }
        return base + " (Routine)";
    }
}

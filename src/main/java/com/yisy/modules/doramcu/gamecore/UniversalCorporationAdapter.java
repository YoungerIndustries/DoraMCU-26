package com.yisy.modules.doramcu.gamecore;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Default fallback adapter for modules that don't implement {@link CorporationAdapter}.
 * <p>
 * Generates generic contracts using the standard contract types (TRADE, SECURITY, LOGISTICS, MISSION)
 * with generic titles. This ensures every universe has at least basic contract support.
 * </p>
 */
@Component
public class UniversalCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Interstellar Trade Route", "Commodity Exchange Deal", "Resource Distribution Agreement",
            "Market Expansion Contract", "Trade Embargo Enforcement"
    };

    private static final String[] SECURITY_TITLES = {
            "Security Detail Assignment", "Threat Assessment Operation", "Perimeter Defense Contract",
            "Asset Protection Mission", "Hostile Entity Neutralization"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Supply Chain Operation", "Freight Transport Contract", "Warehouse Management Deal",
            "Distribution Network Setup", "Resource Allocation Mission"
    };

    private static final String[] MISSION_TITLES = {
            "Special Operations Contract", "Intelligence Gathering Mission", "Diplomatic Envoy Assignment",
            "Research Expedition Deal", "Emergency Response Operation"
    };

    @Override
    public GameType getGameType() {
        return GameType.DORAMCU_4X; // Universal fallback
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
            contract.setDescription("A " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L); // Available to all
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.DORAMCU_4X);
            contract.setUniverseDisplayName("Universal");

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
            return base + " (Critical)";
        } else if (difficulty >= 5) {
            return base + " (Standard)";
        }
        return base + " (Routine)";
    }

    @Override
    public String getUniverseDisplayName() {
        return "Universal";
    }
}

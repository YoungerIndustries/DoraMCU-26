package com.yisy.modules.doramcu.earth2026;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Earth 2026 universe.
 * Provides corporate espionage, tech development, and property deals.
 */
@Component
public class Earth2026CorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Corporate Merger Deal", "Tech Startup Investment", "Real Estate Acquisition",
            "Stock Market Operation", "Patent Licensing Agreement"
    };

    private static final String[] SECURITY_TITLES = {
            "Corporate Espionage Op", "Data Breach Response", "Executive Protection Detail",
            "Rival Company Sabotage", "Cybersecurity Defense"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Supply Chain Optimization", "Warehouse Automation Deal", "Distribution Network Setup",
            "Logistics Fleet Contract", "Inventory Management System"
    };

    private static final String[] MISSION_TITLES = {
            "Tech Development Project", "Government Contract Bid", "Research & Development Deal",
            "Product Launch Mission", "Market Research Operation"
    };

    @Override
    public GameType getGameType() {
        return GameType.EARTH_2026;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 800 + RANDOM.nextInt(4000);
            int xp = difficulty * 40 + RANDOM.nextInt(200);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("An Earth 2026 " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.EARTH_2026);
            contract.setUniverseDisplayName("Earth 2026");

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
        if (difficulty >= 8) return base + " (High Priority)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Routine)";
    }
}

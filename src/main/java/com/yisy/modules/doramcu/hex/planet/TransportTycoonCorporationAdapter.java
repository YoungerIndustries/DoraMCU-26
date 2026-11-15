package com.yisy.modules.doramcu.hex.planet;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Transport Tycoon universe.
 * Provides route management, freight delivery, and passenger service contracts.
 */
@Component
public class TransportTycoonCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Freight Route Deal", "Passenger Service Contract", "Cargo Exchange Agreement",
            "Shipping Lane Contract", "Logistics Hub Deal"
    };

    private static final String[] SECURITY_TITLES = {
            "Convoy Escort Mission", "Pirate Patrol Op", "Cargo Protection Detail",
            "Route Security Contract", "Theft Prevention Deal"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Supply Chain Operation", "Warehouse Management Deal", "Distribution Network Setup",
            "Fleet Maintenance Contract", "Route Optimization Mission"
    };

    private static final String[] MISSION_TITLES = {
            "New Route Survey", "Infrastructure Development", "Market Expansion Deal",
            "Competitive Analysis Mission", "Economic Forecasting Contract"
    };

    @Override
    public GameType getGameType() {
        return GameType.TRANSPORT_TYCOON;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 700 + RANDOM.nextInt(3500);
            int xp = difficulty * 35 + RANDOM.nextInt(150);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Transport Tycoon " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.TRANSPORT_TYCOON);
            contract.setUniverseDisplayName("Transport Tycoon");

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
        if (difficulty >= 8) return base + " (Premium)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Basic)";
    }
}

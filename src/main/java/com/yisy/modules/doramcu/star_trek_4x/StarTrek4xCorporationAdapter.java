package com.yisy.modules.doramcu.star_trek_4x;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Star Trek 4X universe.
 * Provides first contact, sector patrol, and federation missions.
 */
@Component
public class StarTrek4xCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Federation Trade Agreement", "Romulan Commerce Deal", "Cardassian Resource Exchange",
            "Bajoran Supply Contract", "Ferengi Barter Operation"
    };

    private static final String[] SECURITY_TITLES = {
            "Klingon Border Patrol", "Romulan Neutral Zone Op", "Cardassian Occupation Response",
            "Maquis Suppression Mission", "Starfleet Defense Detail"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Starbase Resupply Run", "Colony Outfitting Deal", "Relief Convoy Mission",
            "Transporter Network Setup", "Warp Core Maintenance"
    };

    private static final String[] MISSION_TITLES = {
            "First Contact Protocol", "Voyager Exploration Mission", "Diplomatic Envoy Assignment",
            "Scientific Research Deal", "Humanitarian Aid Operation"
    };

    @Override
    public GameType getGameType() {
        return GameType.STAR_TREK_4X;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1300 + RANDOM.nextInt(7000);
            int xp = difficulty * 65 + RANDOM.nextInt(280);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Star Trek 4X " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.STAR_TREK_4X);
            contract.setUniverseDisplayName("Star Trek 4X");

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
        if (difficulty >= 8) return base + " (Red Alert)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Routine)";
    }
}

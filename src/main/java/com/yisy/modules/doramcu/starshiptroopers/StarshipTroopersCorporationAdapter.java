package com.yisy.modules.doramcu.starshiptroopers;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Starship Troopers universe.
 * Provides bug hunts, planetary defense, and recruitment drives.
 */
@Component
public class StarshipTroopersCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Morale Supply Run", "Ration Distribution Deal", "Equipment Procurement Contract",
            "Colony Trade Agreement", "Resource Extraction Deal"
    };

    private static final String[] SECURITY_TITLES = {
            "Bug Hunt Operation", "Planetary Defense Detail", "Mobile Infantry Patrol",
            "Arachnid Nest Assault", "Perimeter Security Sweep"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Drop Pod Resupply", "Frontline Ammo Delivery", "Troop Transport Mission",
            "Base Construction Logistics", "Evacuation Coordination"
    };

    private static final String[] MISSION_TITLES = {
            "Recruitment Drive", "Fed World Inspection", "Brain Bug Intel Mission",
            "Feeder World Survey", "Federal Envoy Assignment"
    };

    @Override
    public GameType getGameType() {
        return GameType.STARSHIP_TROOPERS;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 2000 + RANDOM.nextInt(10000);
            int xp = difficulty * 100 + RANDOM.nextInt(400);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A Starship Troopers " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.STARSHIP_TROOPERS);
            contract.setUniverseDisplayName("Starship Troopers");

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
}

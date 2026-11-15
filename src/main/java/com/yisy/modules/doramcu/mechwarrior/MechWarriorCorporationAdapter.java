package com.yisy.modules.doramcu.mechwarrior;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the MechWarrior universe.
 * Provides mercenary contracts, clan warfare, and dropship logistics.
 */
@Component
public class MechWarriorCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Mercenary Company Deal", "Mech Parts Trade", "House Alliance Contract",
            "Scavenger Operation", "Salvage Rights Agreement"
    };

    private static final String[] SECURITY_TITLES = {
            "Clan Warfare Operation", "Border Skirmish Detail", "Mech Assault Mission",
            "Dropship Defense Contract", "Guerrilla Warfare Op"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Dropship Resupply Run", "Mech Bay Maintenance", "Ammo Convoy Escort",
            "Field Repair Mission", "Supply Line Protection"
    };

    private static final String[] MISSION_TITLES = {
            "Commando Raid Mission", "Intelligence Gathering Op", "Diplomatic Envoy Deal",
            "Battlefield Recovery Deal", "Strategic Bombardment Mission"
    };

    @Override
    public GameType getGameType() {
        return GameType.MECHWARRIOR;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1600 + RANDOM.nextInt(8000);
            int xp = difficulty * 80 + RANDOM.nextInt(350);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A MechWarrior " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.MECHWARRIOR);
            contract.setUniverseDisplayName("MechWarrior");

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
        if (difficulty >= 8) return base + " (Warlord)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Routine)";
    }
}

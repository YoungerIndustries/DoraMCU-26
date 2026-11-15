package com.yisy.modules.doramcu.dnd5e;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the D&D 5e universe.
 * Provides dungeon contracts, guild quests, and artifact recovery missions.
 */
@Component
public class Dnd5eCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Guild Merchant Deal", "Magic Item Trade", "Dragon Hoard Recovery",
            "Dwarven Mine Contract", "Elven Enchantment Exchange"
    };

    private static final String[] SECURITY_TITLES = {
            "Dungeon Clear Operation", "Dragon Slaying Mission", "Undead Extermination",
            "Rider Ambush Defense", "Castle Guard Detail"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Supply Caravan Escort", "Portal Maintenance", "Healer Network Setup",
            "Spell Component Delivery", "Quest Item Transport"
    };

    private static final String[] MISSION_TITLES = {
            "Artifact Recovery Quest", "Planar Expedition", "Divine Prophecy Mission",
            "Ancient Ruins Exploration", "Necromancer Investigation"
    };

    @Override
    public GameType getGameType() {
        return GameType.DND_5E;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1000 + RANDOM.nextInt(5000);
            int xp = difficulty * 100 + RANDOM.nextInt(500);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A D&D 5e " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.DND_5E);
            contract.setUniverseDisplayName("D&D 5e");

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
        if (difficulty >= 8) return base + " (Legendary)";
        if (difficulty >= 5) return base + " (Epic)";
        return base + " (Standard)";
    }
}

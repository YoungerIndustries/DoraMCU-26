package com.yisy.modules.doramcu.warhammer40k;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateContract.ContractType;
import com.yisy.modules.doramcu.corporation.model.CorporateContract.Status;
import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the Warhammer 40K universe.
 * Provides crusade, defense, exploration, and purge contracts.
 */
@Component
public class Warhammer40kCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] CRUSADE_TITLES = {
            "Imperial Crusade", "Exterminatus Mission", "Heresy Purge",
            "WAAAGH! Repulsion", "Necron Tomb Assault"
    };

    private static final String[] DEFENSE_TITLES = {
            "Planetary Defense", "Space Hulk Containment", "Warp Storm Shielding",
            "Tyranid Incursion Response", "Chaos Raid Repulsion"
    };

    private static final String[] EXPLORATION_TITLES = {
            "Warp Route Survey", "Death World Expedition", "Ruins Exploration",
            "Astropathic Relay Installation", "Forge World Contact"
    };

    private static final String[] PURGE_TITLES = {
            "Genestealer Cult Eradication", "Chaos Cult Suppression", "Heretic Execution",
            "Abomination Destruction", "Xenos Artifact Recovery"
    };

    @Override
    public GameType getGameType() {
        return GameType.WARHAMMER_40K;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        ContractType[] types = {ContractType.MISSION, ContractType.SECURITY, ContractType.TRADE, ContractType.LOGISTICS};

        for (int i = 0; i < count; i++) {
            ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            String title = getContractTitle(type.name(), difficulty);

            CorporateContract contract = new CorporateContract();
            contract.setTitle(title);
            contract.setType(type);
            contract.setDifficulty(difficulty);
            contract.setPaymentCredits(difficulty * 10000);
            contract.setXpReward(difficulty * 100);
            contract.setDurationTurns(difficulty);
            contract.setTurnsRemaining(difficulty);
            contract.setStatus(Status.AVAILABLE);
            contract.setCorporationId(1L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setExpiryDate(LocalDateTime.now().plusDays(difficulty * 2));
            contract.setGameType(GameType.WARHAMMER_40K);
            contract.setUniverseDisplayName("Warhammer 40K");

            contracts.add(contract);
        }

        return contracts;
    }

    @Override
    public List<String> getContractTypes() {
        return List.of("MISSION", "SECURITY", "TRADE", "LOGISTICS");
    }

    @Override
    public String getContractTitle(String type, int difficulty) {
        String[] titles;
        switch (type.toUpperCase()) {
            case "MISSION": titles = CRUSADE_TITLES; break;
            case "SECURITY": titles = DEFENSE_TITLES; break;
            case "TRADE": titles = EXPLORATION_TITLES; break;
            default: titles = PURGE_TITLES; break;
        }
        String base = titles[RANDOM.nextInt(titles.length)];
        if (difficulty >= 8) return base + " (High Priority)";
        if (difficulty >= 5) return base + " (Standard)";
        return base + " (Routine)";
    }
}

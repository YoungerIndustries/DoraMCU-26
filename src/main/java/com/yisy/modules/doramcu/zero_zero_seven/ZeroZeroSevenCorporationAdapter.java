package com.yisy.modules.doramcu.zero_zero_seven;
import org.springframework.stereotype.Component;

import com.yisy.modules.doramcu.gamecore.CorporationAdapter;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.gamecore.GameType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CorporationAdapter for the 007 universe.
 * Provides espionage contracts, asset recruitment, and intelligence gathering.
 */
@Component
public class ZeroZeroSevenCorporationAdapter implements CorporationAdapter {

    private static final Random RANDOM = new Random();

    private static final String[] TRADE_TITLES = {
            "Arms Dealer Negotiation", "Black Market Intelligence", "Front Company Setup",
            "Offshore Account Deal", "Diplomatic Immunity Contract"
    };

    private static final String[] SECURITY_TITLES = {
            "Assassination Prevention", "Villain Lair Raid", "Defector Extraction",
            "Safehouse Security Detail", "Counter-Espionage Operation"
    };

    private static final String[] LOGISTICS_TITLES = {
            "Gadget Procurement Deal", "Covert Transport Mission", "Surveillance Network Setup",
            "Equipment Drop Operation", "Escape Route Planning"
    };

    private static final String[] MISSION_TITLES = {
            "Asset Recruitment Mission", "Intelligence Gathering Op", "Diplomatic Envoy Assignment",
            "Sabotage Prevention Deal", "Double Agent Investigation"
    };

    @Override
    public GameType getGameType() {
        return GameType.ZERO_ZERO_SEVEN;
    }

    @Override
    public List<CorporateContract> generateContracts(int count) {
        List<CorporateContract> contracts = new ArrayList<>();
        CorporateContract.ContractType[] types = CorporateContract.ContractType.values();

        for (int i = 0; i < count; i++) {
            CorporateContract.ContractType type = types[RANDOM.nextInt(types.length)];
            int difficulty = RANDOM.nextInt(10) + 1;
            int payment = difficulty * 1400 + RANDOM.nextInt(7000);
            int xp = difficulty * 70 + RANDOM.nextInt(300);
            int duration = Math.max(1, difficulty - RANDOM.nextInt(3));

            CorporateContract contract = new CorporateContract();
            contract.setTitle(getContractTitle(type.name(), difficulty));
            contract.setType(type);
            contract.setDescription("A 007 " + type.name().toLowerCase() + " contract with difficulty " + difficulty + ".");
            contract.setPaymentCredits(payment);
            contract.setXpReward(xp);
            contract.setDifficulty(difficulty);
            contract.setDurationTurns(duration);
            contract.setStatus(CorporateContract.Status.AVAILABLE);
            contract.setCorporationId(0L);
            contract.setCreatedAt(LocalDateTime.now());
            contract.setTurnsRemaining(duration);
            contract.setProgressPercent(0.0);
            contract.setGameType(GameType.ZERO_ZERO_SEVEN);
            contract.setUniverseDisplayName("007");

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
        if (difficulty >= 8) return base + " (Top Secret)";
        if (difficulty >= 5) return base + " (Classified)";
        return base + " (Confidential)";
    }
}

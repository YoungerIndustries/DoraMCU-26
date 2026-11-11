package com.yisy.modules.doramcu.zero_zero_seven.generation;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondEmpireRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes 8-12 major 1950s powers with correct attributes.
 * <p>
 * Creates empires representing the major Cold War powers: USA, USSR, UK, France,
 * China, Japan, West Germany, East Germany, India, Italy, Brazil, Australia.
 * </p>
 */
@Service
public class BondEmpireGenerator {

    private final BondEmpireRepository empireRepository;
    private final BondGameSessionRepository gameSessionRepository;

    public BondEmpireGenerator(BondEmpireRepository empireRepository,
                               BondGameSessionRepository gameSessionRepository) {
        this.empireRepository = empireRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Generate all empires for a game session.
     *
     * @param gameSessionId the game session
     * @return list of generated empires
     */
    @Transactional
    public List<BondEmpire> generateEmpires(Long gameSessionId) {
        BondGameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        List<BondEmpire> empires = new ArrayList<>();
        EmpireData[] data = getColdWarEmpires();

        for (EmpireData d : data) {
            BondEmpire empire = new BondEmpire(d.name, d.isoCode, d.alignment, session);
            empire.setDescription(d.description);
            empire.setIntelligenceBudget(d.intelBudget);
            empire.setMilitaryStrength(d.military);
            empire.setEconomicPower(d.economy);
            empire.setDiplomaticInfluence(d.diplomacy);
            empires.add(empireRepository.save(empire));
        }

        return empires;
    }

    private static class EmpireData {
        String name;
        String isoCode;
        BondEmpire.ColdWarAlignment alignment;
        String description;
        int intelBudget;
        int military;
        int economy;
        int diplomacy;

        EmpireData(String name, String isoCode, BondEmpire.ColdWarAlignment alignment,
                   String description, int intelBudget, int military, int economy, int diplomacy) {
            this.name = name;
            this.isoCode = isoCode;
            this.alignment = alignment;
            this.description = description;
            this.intelBudget = intelBudget;
            this.military = military;
            this.economy = economy;
            this.diplomacy = diplomacy;
        }
    }

    private EmpireData[] getColdWarEmpires() {
        return new EmpireData[]{
                new EmpireData("United States", "US", BondEmpire.ColdWarAlignment.WESTERN,
                        "World superpower leading the Western Alliance", 10, 10, 10, 9),
                new EmpireData("Soviet Union", "SU", BondEmpire.ColdWarAlignment.SOVIET,
                        "Communist superpower leading the Eastern Bloc", 10, 10, 8, 8),
                new EmpireData("United Kingdom", "UK", BondEmpire.ColdWarAlignment.WESTERN,
                        "Former empire with strong intelligence services", 9, 7, 7, 8),
                new EmpireData("France", "FR", BondEmpire.ColdWarAlignment.WESTERN,
                        "European power with colonial interests", 7, 6, 7, 7),
                new EmpireData("China", "CN", BondEmpire.ColdWarAlignment.SOVIET,
                        "Rising communist power in East Asia", 6, 8, 5, 6),
                new EmpireData("Japan", "JP", BondEmpire.ColdWarAlignment.WESTERN,
                        "Rebuilding nation under US influence", 5, 4, 7, 5),
                new EmpireData("West Germany", "WG", BondEmpire.ColdWarAlignment.WESTERN,
                        "Divided Germany, Western sector", 6, 5, 7, 6),
                new EmpireData("East Germany", "EG", BondEmpire.ColdWarAlignment.SOVIET,
                        "Divided Germany, Eastern sector", 5, 5, 4, 4),
                new EmpireData("India", "IN", BondEmpire.ColdWarAlignment.NON_ALIGNED,
                        "Largest democracy, non-aligned movement leader", 5, 5, 5, 7),
                new EmpireData("Italy", "IT", BondEmpire.ColdWarAlignment.WESTERN,
                        "Mediterranean power, NATO member", 5, 4, 6, 5),
                new EmpireData("Brazil", "BR", BondEmpire.ColdWarAlignment.NON_ALIGNED,
                        "South American leader", 4, 4, 5, 4),
                new EmpireData("Australia", "AU", BondEmpire.ColdWarAlignment.WESTERN,
                        "Pacific nation, ANZUS alliance member", 4, 4, 5, 4)
        };
    }
}

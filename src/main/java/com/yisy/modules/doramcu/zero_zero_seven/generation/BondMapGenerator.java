package com.yisy.modules.doramcu.zero_zero_seven.generation;

import com.yisy.modules.doramcu.earth2026.model.TechLevel;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondTerritoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a hex map of Earth regions with 1950s geography.
 * <p>
 * Creates territories representing major Cold War regions: Western Europe, Eastern Bloc,
 * North America, Asia, Middle East, Africa, South America, and Oceania.
 * </p>
 */
@Service
public class BondMapGenerator {

    private final BondTerritoryRepository territoryRepository;
    private final BondGameSessionRepository gameSessionRepository;
    private final Random random;

    public BondMapGenerator(BondTerritoryRepository territoryRepository,
                            BondGameSessionRepository gameSessionRepository) {
        this.territoryRepository = territoryRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.random = new Random();
    }

    /**
     * Generate the hex map for a game session.
     *
     * @param gameSessionId the game session
     * @return list of generated territories
     */
    @Transactional
    public List<BondTerritory> generateMap(Long gameSessionId) {
        BondGameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        List<BondTerritory> territories = new ArrayList<>();
        String[][] regions = getColdWarRegions();

        int startX = 0;
        int startY = 0;
        for (String[] region : regions) {
            BondTerritory territory = new BondTerritory(
                    region[0], startX, startY, session
            );
            territory.setDescription(region.length > 1 ? region[1] : "");
            territory.setMinerals(Integer.parseInt(region.length > 2 ? region[2] : "5"));
            territory.setEnergy(Integer.parseInt(region.length > 3 ? region[3] : "5"));
            territory.setFood(Integer.parseInt(region.length > 4 ? region[4] : "5"));
            territory.setTechLevel(TechLevel.TL5);
            territory.setPopulation(random.nextInt(50) + 10);

            territories.add(territoryRepository.save(territory));
            startX++;
            if (startX >= 6) {
                startX = 0;
                startY++;
            }
        }

        return territories;
    }

    /**
     * Get Cold War era regions for the hex map.
     * Each region: [name, description, minerals, energy, food]
     */
    private String[][] getColdWarRegions() {
        return new String[][]{
                {"North America", "USA and Canada", "8", "9", "7"},
                {"Western Europe", "UK, France, West Germany", "6", "7", "6"},
                {"Eastern Europe", "USSR satellite states", "5", "6", "5"},
                {"Soviet Union", "USSR heartland", "9", "8", "6"},
                {"Middle East", "Oil-rich region", "7", "9", "3"},
                {"South America", "Brazil, Argentina, etc.", "6", "5", "8"},
                {"East Asia", "China, Japan, Korea", "5", "6", "7"},
                {"Southeast Asia", "Vietnam, Indonesia", "4", "4", "6"},
                {"South Asia", "India, Pakistan", "4", "3", "7"},
                {"Africa", "Sub-Saharan Africa", "7", "5", "5"},
                {"North Africa", "Egypt, Morocco", "5", "6", "4"},
                {"Oceania", "Australia, New Zealand", "6", "5", "5"},
                {"Scandinavia", "Sweden, Norway, Denmark", "5", "6", "4"},
                {"Central America", "Mexico, Central America", "4", "4", "6"},
                {"Central Asia", "Kazakhstan, etc.", "6", "5", "3"},
                {"Pacific Islands", "Island nations", "3", "3", "4"},
                {"Arctic", "Northern territories", "4", "3", "2"},
                {"Caribbean", "Island nations", "3", "3", "5"},
                {"Balkans", "Yugoslavia, Greece", "4", "4", "4"},
                {"Far East", "Siberia, Alaska", "7", "5", "2"},
                {"Central Africa", "Congo, etc.", "6", "4", "5"},
                {"East Africa", "Kenya, Ethiopia", "5", "4", "5"},
                {"West Africa", "Nigeria, Ghana", "5", "4", "6"},
                {"South Africa", "Southern Africa", "6", "5", "5"},
        };
    }
}

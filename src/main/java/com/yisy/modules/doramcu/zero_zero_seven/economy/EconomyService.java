package com.yisy.modules.doramcu.zero_zero_seven.economy;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondEmpireRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondTerritoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for calculating economy and resources per turn.
 * <p>
 * Handles income calculation, expenses, and trade profits based on
 * controlled territories and empire attributes.
 * </p>
 */
@Service
public class EconomyService {

    private final BondEmpireRepository empireRepository;
    private final BondTerritoryRepository territoryRepository;

    public EconomyService(BondEmpireRepository empireRepository,
                          BondTerritoryRepository territoryRepository) {
        this.empireRepository = empireRepository;
        this.territoryRepository = territoryRepository;
    }

    /**
     * Calculate economy output for an empire.
     *
     * @param empireId the empire
     * @return economy output per turn
     */
    public int calculateEconomyOutput(Long empireId) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));

        List<BondTerritory> territories = territoryRepository.findByOwnerEmpireId(empireId);
        int baseOutput = empire.getEconomicPower() * 10;
        int territoryBonus = territories.stream()
                .mapToInt(t -> t.getTotalResources() / 2)
                .sum();

        return baseOutput + territoryBonus;
    }

    /**
     * Calculate intelligence budget for an empire.
     *
     * @param empireId the empire
     * @return intelligence budget per turn
     */
    public int calculateIntelligenceBudget(Long empireId) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));

        return empire.getIntelligenceBudget() + empire.getSpyNetworkCount() * 5;
    }

    /**
     * Process economy for all empires at the end of a turn.
     *
     * @param gameSessionId the game session
     */
    @Transactional
    public void processTurnEconomy(Long gameSessionId) {
        List<BondEmpire> empires = empireRepository.findByGameSessionId(gameSessionId);

        for (BondEmpire empire : empires) {
            int output = calculateEconomyOutput(empire.getId());
            empire.setEconomyOutput(output);
            empire.setResearchPoints(empire.getResearchPoints() + output / 4);
            empireRepository.save(empire);
        }
    }

    /**
     * Calculate trade profit between two empires.
     *
     * @param empire1Id first empire
     * @param empire2Id second empire
     * @return trade profit
     */
    public int calculateTradeProfit(Long empire1Id, Long empire2Id) {
        BondEmpire empire1 = empireRepository.findById(empire1Id)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empire1Id));
        BondEmpire empire2 = empireRepository.findById(empire2Id)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empire2Id));

        return (empire1.getEconomicPower() + empire2.getEconomicPower()) / 2;
    }
}

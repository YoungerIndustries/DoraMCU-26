package com.yisy.modules.doramcu.zero_zero_seven.economy;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondTerritoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing resources across territories.
 * <p>
 * Handles minerals, energy, food, and intelligence budget allocation.
 * </p>
 */
@Service
public class ResourceService {

    private final BondTerritoryRepository territoryRepository;

    public ResourceService(BondTerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    /**
     * Get total minerals for an empire.
     *
     * @param ownerEmpireId the empire
     * @return total minerals
     */
    public int getTotalMinerals(Long ownerEmpireId) {
        List<BondTerritory> territories = territoryRepository.findByOwnerEmpireId(ownerEmpireId);
        return territories.stream().mapToInt(BondTerritory::getMinerals).sum();
    }

    /**
     * Get total energy for an empire.
     *
     * @param ownerEmpireId the empire
     * @return total energy
     */
    public int getTotalEnergy(Long ownerEmpireId) {
        List<BondTerritory> territories = territoryRepository.findByOwnerEmpireId(ownerEmpireId);
        return territories.stream().mapToInt(BondTerritory::getEnergy).sum();
    }

    /**
     * Get total food for an empire.
     *
     * @param ownerEmpireId the empire
     * @return total food
     */
    public int getTotalFood(Long ownerEmpireId) {
        List<BondTerritory> territories = territoryRepository.findByOwnerEmpireId(ownerEmpireId);
        return territories.stream().mapToInt(BondTerritory::getFood).sum();
    }

    /**
     * Get total resources for an empire.
     *
     * @param ownerEmpireId the empire
     * @return total resources
     */
    public int getTotalResources(Long ownerEmpireId) {
        return getTotalMinerals(ownerEmpireId) + getTotalEnergy(ownerEmpireId) + getTotalFood(ownerEmpireId);
    }
}

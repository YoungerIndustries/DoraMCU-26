package com.yisy.modules.doramcu.zero_zero_seven.territory;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondEmpireRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondTerritoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for expanding empire control to adjacent hexes.
 * <p>
 * Handles territorial expansion mechanics, including checking adjacency
 * and updating empire controlled territories.
 * </p>
 */
@Service
public class ExpansionService {

    private final TerritoryService territoryService;
    private final BondTerritoryRepository territoryRepository;
    private final BondEmpireRepository empireRepository;

    public ExpansionService(TerritoryService territoryService,
                            BondTerritoryRepository territoryRepository,
                            BondEmpireRepository empireRepository) {
        this.territoryService = territoryService;
        this.territoryRepository = territoryRepository;
        this.empireRepository = empireRepository;
    }

    /**
     * Expand empire control to an adjacent territory.
     *
     * @param empireId the expanding empire
     * @param targetTerritoryId the territory to capture
     * @return the captured territory
     */
    @Transactional
    public BondTerritory expandToTerritory(Long empireId, Long targetTerritoryId) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));

        BondTerritory target = territoryRepository.findById(targetTerritoryId)
                .orElseThrow(() -> new IllegalArgumentException("Territory not found: " + targetTerritoryId));

        // Check if the target is adjacent to any controlled territory
        boolean adjacent = false;
        for (Long controlledId : empire.getControlledTerritories()) {
            BondTerritory controlled = territoryRepository.findById(controlledId).orElse(null);
            if (controlled != null) {
                int[][] adjacentHexes = territoryService.getAdjacentHexes(controlled.getHexX(), controlled.getHexY());
                for (int[] hex : adjacentHexes) {
                    if (hex[0] == target.getHexX() && hex[1] == target.getHexY()) {
                        adjacent = true;
                        break;
                    }
                }
            }
            if (adjacent) break;
        }

        if (!adjacent && !target.isUncontrolled()) {
            throw new IllegalStateException("Target territory is not adjacent to controlled territory");
        }

        // Transfer ownership
        target.setOwnerEmpireId(empireId);
        BondTerritory saved = territoryRepository.save(target);

        // Update empire's controlled territories
        if (!empire.getControlledTerritories().contains(targetTerritoryId)) {
            empire.getControlledTerritories().add(targetTerritoryId);
            empireRepository.save(empire);
        }

        return saved;
    }

    /**
     * Get expandable territories for an empire (adjacent uncontrolled or weak territories).
     *
     * @param empireId the empire
     * @return list of expandable territories
     */
    public List<BondTerritory> getExpandableTerritories(Long empireId) {
        BondEmpire empire = empireRepository.findById(empireId)
                .orElseThrow(() -> new IllegalArgumentException("Empire not found: " + empireId));

        List<BondTerritory> expandable = new ArrayList<>();
        List<BondTerritory> allTerritories = territoryRepository.findAll();

        for (Long controlledId : empire.getControlledTerritories()) {
            BondTerritory controlled = allTerritories.stream()
                    .filter(t -> t.getId().equals(controlledId))
                    .findFirst().orElse(null);
            if (controlled != null) {
                List<BondTerritory> adjacent = territoryService.getAdjacentTerritories(
                        controlled.getGameSession().getId(), controlled.getHexX(), controlled.getHexY());
                for (BondTerritory t : adjacent) {
                    if (!expandable.contains(t) && (t.isUncontrolled() || t.getOwnerEmpireId().equals(empireId) == false)) {
                        expandable.add(t);
                    }
                }
            }
        }

        return expandable;
    }
}

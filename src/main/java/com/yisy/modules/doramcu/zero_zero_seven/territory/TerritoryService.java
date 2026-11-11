package com.yisy.modules.doramcu.zero_zero_seven.territory;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondTerritoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing hex-based territories.
 * <p>
 * Handles territory ownership, resource collection, and map navigation.
 * </p>
 */
@Service
public class TerritoryService {

    private final BondTerritoryRepository territoryRepository;

    public TerritoryService(BondTerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    /**
     * Get all territories for a game session.
     *
     * @param gameSessionId the game session
     * @return list of territories
     */
    public List<BondTerritory> getAllTerritories(Long gameSessionId) {
        return territoryRepository.findByGameSessionId(gameSessionId);
    }

    /**
     * Get territories controlled by an empire.
     *
     * @param ownerEmpireId the empire
     * @return list of controlled territories
     */
    public List<BondTerritory> getControlledTerritories(Long ownerEmpireId) {
        return territoryRepository.findByOwnerEmpireId(ownerEmpireId);
    }

    /**
     * Get a territory by hex coordinates.
     *
     * @param gameSessionId the game session
     * @param hexX hex X coordinate
     * @param hexY hex Y coordinate
     * @return optional territory
     */
    public Optional<BondTerritory> getTerritoryAtHex(Long gameSessionId, int hexX, int hexY) {
        return territoryRepository.findByGameSessionIdAndHexXAndHexY(gameSessionId, hexX, hexY);
    }

    /**
     * Transfer territory ownership.
     *
     * @param territoryId the territory
     * @param newOwnerId the new owner empire
     * @return the updated territory
     */
    public BondTerritory transferOwnership(Long territoryId, Long newOwnerId) {
        BondTerritory territory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> new IllegalArgumentException("Territory not found: " + territoryId));

        territory.setOwnerEmpireId(newOwnerId);
        return territoryRepository.save(territory);
    }

    /**
     * Get adjacent hex coordinates.
     *
     * @param hexX current hex X
     * @param hexY current hex Y
     * @return array of adjacent hex coordinates [x, y]
     */
    public int[][] getAdjacentHexes(int hexX, int hexY) {
        // Odd-r offset coordinates for hex grid
        boolean isOdd = hexX % 2 != 0;
        return isOdd ? new int[][]{
                {hexX - 1, hexY}, {hexX + 1, hexY},
                {hexX, hexY - 1}, {hexX, hexY + 1},
                {hexX - 1, hexY + 1}, {hexX + 1, hexY + 1}
        } : new int[][]{
                {hexX - 1, hexY}, {hexX + 1, hexY},
                {hexX, hexY - 1}, {hexX, hexY + 1},
                {hexX - 1, hexY - 1}, {hexX + 1, hexY - 1}
        };
    }

    /**
     * Get adjacent territories.
     *
     * @param gameSessionId the game session
     * @param hexX current hex X
     * @param hexY current hex Y
     * @return list of adjacent territories
     */
    public List<BondTerritory> getAdjacentTerritories(Long gameSessionId, int hexX, int hexY) {
        int[][] adjacent = getAdjacentHexes(hexX, hexY);
        return territoryRepository.findByGameSessionId(gameSessionId).stream()
                .filter(t -> {
                    for (int[] coord : adjacent) {
                        if (t.getHexX() == coord[0] && t.getHexY() == coord[1]) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }
}

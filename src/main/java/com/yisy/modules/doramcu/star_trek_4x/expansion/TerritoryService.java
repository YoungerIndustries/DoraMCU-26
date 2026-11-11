package com.yisy.modules.doramcu.star_trek_4x.expansion;

import com.yisy.modules.doramcu.star_trek_4x.model.GameSession;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.GameSessionRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for managing sector control and Zone of Control.
 * <p>
 * Handles sector claiming, territorial disputes, and integration with SfbCampaignEngine.
 * </p>
 */
@Service
public class TerritoryService {

    private static final Logger log = LoggerFactory.getLogger(TerritoryService.class);

    private final StarTrekFactionRepository factionRepository;
    private final GameSessionRepository gameSessionRepository;

    public TerritoryService(StarTrekFactionRepository factionRepository,
                             GameSessionRepository gameSessionRepository) {
        this.factionRepository = factionRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Claim a sector for a faction.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction ID
     * @param sectorId      the sector ID
     * @return claim result
     */
    public Map<String, Object> claimSector(Long gameSessionId, Long factionId, int sectorId) {
        log.info("Claiming sector {} for faction {}", sectorId, factionId);

        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        StarTrekFaction faction = factionRepository.findById(factionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + factionId));

        // Check if sector is already controlled
        if (faction.getControlledSectors().contains(sectorId)) {
            return Map.of(
                    "success", false,
                    "reason", "Sector already controlled by this faction",
                    "sectorId", sectorId
            );
        }

        // Claim the sector
        faction.getControlledSectors().add(sectorId);
        factionRepository.save(faction);

        return Map.of(
                "success", true,
                "sectorId", sectorId,
                "factionName", faction.getName(),
                "totalSectors", faction.getControlledSectors().size(),
                "message", "Sector " + sectorId + " claimed by " + faction.getName()
        );
    }

    /**
     * Check if a sector is contested.
     *
     * @param gameSessionId the game session ID
     * @param sectorId      the sector ID
     * @return true if contested
     */
    public boolean isSectorContested(Long gameSessionId, int sectorId) {
        long controllingFactions = factionRepository.findAll().stream()
                .filter(f -> f.getGameSession().getId().equals(gameSessionId))
                .filter(f -> f.getControlledSectors().contains(sectorId))
                .count();

        return controllingFactions > 1;
    }

    /**
     * Get the controlling faction of a sector.
     *
     * @param gameSessionId the game session ID
     * @param sectorId      the sector ID
     * @return controlling faction or null
     */
    public StarTrekFaction getControllingFaction(Long gameSessionId, int sectorId) {
        return factionRepository.findAll().stream()
                .filter(f -> f.getGameSession().getId().equals(gameSessionId))
                .filter(f -> f.getControlledSectors().contains(sectorId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Calculate Zone of Control for a faction.
     * <p>
     * Zone of Control extends 1 hex from controlled sectors.
     * </p>
     *
     * @param factionId the faction ID
     * @return list of ZOC sector IDs
     */
    public java.util.List<Integer> calculateZoneOfControl(Long factionId) {
        StarTrekFaction faction = factionRepository.findById(factionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + factionId));

        java.util.Set<Integer> zoc = new java.util.HashSet<>(faction.getControlledSectors());

        // Add adjacent sectors to ZOC
        for (int sectorId : faction.getControlledSectors()) {
            int row = sectorId / 10;
            int col = sectorId % 10;

            // Add adjacent sectors
            if (row > 0) zoc.add((row - 1) * 10 + col);
            if (row < 9) zoc.add((row + 1) * 10 + col);
            if (col > 0) zoc.add(row * 10 + (col - 1));
            if (col < 9) zoc.add(row * 10 + (col + 1));
        }

        return java.util.List.copyOf(zoc);
    }
}

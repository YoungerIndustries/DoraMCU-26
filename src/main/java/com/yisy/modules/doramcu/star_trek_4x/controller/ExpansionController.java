package com.yisy.modules.doramcu.star_trek_4x.controller;

import com.yisy.modules.doramcu.star_trek_4x.expansion.ExpansionService;
import com.yisy.modules.doramcu.star_trek_4x.model.Colony;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for the Expansion pillar.
 * <p>
 * Provides endpoints for colony establishment, starbase construction,
 * sector claiming, and colony management.
 * </p>
 */
@RestController
@RequestMapping("/api/star-trek-4x/expansion")
public class ExpansionController {

    private final ExpansionService expansionService;

    public ExpansionController(ExpansionService expansionService) {
        this.expansionService = expansionService;
    }

    /**
     * Establish a colony on a discovered system.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction establishing the colony
     * @param systemId      the system ID
     * @param colonyName    the colony name
     * @return the established colony
     */
    @PostMapping("/colony")
    public ResponseEntity<Colony> establishColony(
            @RequestParam Long gameSessionId,
            @RequestParam Long factionId,
            @RequestParam Long systemId,
            @RequestParam String colonyName) {
        Colony colony = expansionService.establishColony(gameSessionId, factionId, systemId, colonyName);
        return ResponseEntity.ok(colony);
    }

    /**
     * Build a starbase in a system.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction building the starbase
     * @param systemId      the system ID
     * @param starbaseName  the starbase name
     * @return starbase details
     */
    @PostMapping("/starbase")
    public ResponseEntity<Map<String, Object>> buildStarbase(
            @RequestParam Long gameSessionId,
            @RequestParam Long factionId,
            @RequestParam Long systemId,
            @RequestParam String starbaseName) {
        Map<String, Object> result = expansionService.buildStarbase(gameSessionId, factionId, systemId, starbaseName);
        return ResponseEntity.ok(result);
    }

    /**
     * Claim a sector for a faction.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction claiming the sector
     * @param sectorId      the sector ID
     * @return claim result
     */
    @PostMapping("/claim-sector")
    public ResponseEntity<Map<String, Object>> claimSector(
            @RequestParam Long gameSessionId,
            @RequestParam Long factionId,
            @RequestParam int sectorId) {
        Map<String, Object> result = expansionService.claimSector(gameSessionId, factionId, sectorId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all colonies for a faction.
     *
     * @param gameSessionId the game session ID
     * @param factionId     the faction ID
     * @return list of colonies
     */
    @GetMapping("/colonies")
    public ResponseEntity<List<Colony>> getColonies(
            @RequestParam Long gameSessionId,
            @RequestParam Long factionId) {
        List<Colony> colonies = expansionService.getColonies(gameSessionId, factionId);
        return ResponseEntity.ok(colonies);
    }

    /**
     * Get all colonies in a sector.
     *
     * @param gameSessionId the game session ID
     * @param sectorId      the sector ID
     * @return list of colonies
     */
    @GetMapping("/colonies/sector")
    public ResponseEntity<List<Colony>> getColoniesInSector(
            @RequestParam Long gameSessionId,
            @RequestParam int sectorId) {
        List<Colony> colonies = expansionService.getColoniesInSector(gameSessionId, sectorId);
        return ResponseEntity.ok(colonies);
    }

    /**
     * Process colony growth for a turn.
     *
     * @param gameSessionId the game session ID
     * @return success message
     */
    @PostMapping("/process-growth")
    public ResponseEntity<Map<String, String>> processColonyGrowth(
            @RequestParam Long gameSessionId) {
        expansionService.processColonyGrowth(gameSessionId);
        return ResponseEntity.ok(Map.of("message", "Colony growth processed for game session " + gameSessionId));
    }
}

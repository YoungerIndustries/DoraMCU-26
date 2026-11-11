package com.yisy.modules.doramcu.star_trek_4x.controller;

import com.yisy.modules.doramcu.captainslog.model.StarTrekSpecies;
import com.yisy.modules.doramcu.star_trek_4x.exploration.ExplorationService;
import com.yisy.modules.doramcu.star_trek_4x.exploration.FirstContactService;
import com.yisy.modules.doramcu.star_trek_4x.exploration.ScientificMissionService;
import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarSystemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for the Exploration pillar.
 * <p>
 * Provides endpoints for sector scanning, system discovery, first contact scenarios,
 * and scientific missions.
 * </p>
 */
@RestController
@RequestMapping("/api/star-trek-4x/exploration")
public class ExplorationController {

    private final ExplorationService explorationService;
    private final FirstContactService firstContactService;
    private final ScientificMissionService scientificMissionService;
    private final StarSystemRepository starSystemRepository;

    public ExplorationController(ExplorationService explorationService,
                                  FirstContactService firstContactService,
                                  ScientificMissionService scientificMissionService,
                                  StarSystemRepository starSystemRepository) {
        this.explorationService = explorationService;
        this.firstContactService = firstContactService;
        this.scientificMissionService = scientificMissionService;
        this.starSystemRepository = starSystemRepository;
    }

    /**
     * Scan a sector to discover star systems.
     *
     * @param gameSessionId the game session ID
     * @param sectorId      the sector ID to scan
     * @return list of discovered systems
     */
    @PostMapping("/scan")
    public ResponseEntity<List<StarSystem>> scanSector(
            @RequestParam Long gameSessionId,
            @RequestParam int sectorId) {
        List<StarSystem> discovered = explorationService.scanSector(gameSessionId, sectorId);
        return ResponseEntity.ok(discovered);
    }

    /**
     * Get all discovered systems for a game session.
     *
     * @param gameSessionId the game session ID
     * @return list of discovered systems
     */
    @GetMapping("/systems")
    public ResponseEntity<List<StarSystem>> getDiscoveredSystems(
            @RequestParam Long gameSessionId) {
        List<StarSystem> systems = explorationService.getDiscoveredSystems(gameSessionId);
        return ResponseEntity.ok(systems);
    }

    /**
     * Get exploration report for a specific system.
     *
     * @param systemId the system ID
     * @return exploration report
     */
    @GetMapping("/report/{systemId}")
    public ResponseEntity<Map<String, Object>> getExplorationReport(
            @PathVariable Long systemId) {
        Map<String, Object> report = explorationService.getExplorationReport(systemId);
        return ResponseEntity.ok(report);
    }

    /**
     * Generate a first contact scenario for a discovered system.
     *
     * @param systemId the system ID
     * @return first contact scenario
     */
    @PostMapping("/first-contact/{systemId}")
    public ResponseEntity<Map<String, Object>> generateFirstContact(
            @PathVariable Long systemId) {
        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));
        Map<String, Object> scenario = firstContactService.generateFirstContactScenario(system);
        return ResponseEntity.ok(scenario);
    }

    /**
     * Get all available species for first contact.
     *
     * @return list of species
     */
    @GetMapping("/species")
    public ResponseEntity<List<StarTrekSpecies>> getAvailableSpecies() {
        List<StarTrekSpecies> species = firstContactService.getAvailableSpecies();
        return ResponseEntity.ok(species);
    }

    /**
     * Generate a scientific mission for a discovered system.
     *
     * @param systemId the system ID
     * @return scientific mission details
     */
    @PostMapping("/scientific-mission/{systemId}")
    public ResponseEntity<Map<String, Object>> generateScientificMission(
            @PathVariable Long systemId) {
        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));
        Map<String, Object> mission = scientificMissionService.generateScientificMission(system);
        return ResponseEntity.ok(mission);
    }

    /**
     * Get all available mission types.
     *
     * @return list of mission types
     */
    @GetMapping("/mission-types")
    public ResponseEntity<String[]> getMissionTypes() {
        String[] types = scientificMissionService.getMissionTypes();
        return ResponseEntity.ok(types);
    }

    /**
     * Calculate distance between two systems.
     *
     * @param fromSystemId the source system ID
     * @param toSystemId   the destination system ID
     * @return distance in hex units
     */
    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam Long fromSystemId,
            @RequestParam Long toSystemId) {
        StarSystem from = starSystemRepository.findById(fromSystemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + fromSystemId));
        StarSystem to = starSystemRepository.findById(toSystemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + toSystemId));
        int distance = explorationService.calculateDistance(from, to);
        return ResponseEntity.ok(Map.of(
                "fromSystemId", fromSystemId,
                "toSystemId", toSystemId,
                "distance", distance
        ));
    }
}

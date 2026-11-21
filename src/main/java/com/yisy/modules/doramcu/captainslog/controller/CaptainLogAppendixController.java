package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.MissionDevelopmentService;
import com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log appendix endpoints (Allies, Adversaries, Locations, Starships).
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides oracle rolls for adversaries, allies, locations, starship encounters, and maintenance events.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogAppendixController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogAppendixController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogAppendixController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    // ---- Maintenance / Downtime ----

    /**
     * Rolls for a random maintenance event during downtime.
     * <p>
     * Returns a maintenance event from the Appendix IV table.
     * </p>
     */
    @GetMapping("/oracle/maintenance")
    public ResponseEntity<Map<String, String>> rollMaintenance() {
        log.debug("GET /api/v1/captains-log/oracle/maintenance");
        ProbabilityMatrixService.MaintenanceEvent event =
                captainLogService.getProbabilityMatrixService().rollMaintenanceEvent();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("event", event.name());
        result.put("description", event.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Rolls for a random alien polity or faction.
     * <p>
     * Returns a polity from the Appendix IV table.
     * </p>
     */
    @GetMapping("/oracle/polity")
    public ResponseEntity<Map<String, String>> rollPolity() {
        log.debug("GET /api/v1/captains-log/oracle/polity");
        ProbabilityMatrixService.AlienPolity polity =
                captainLogService.getProbabilityMatrixService().rollAlienPolity();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("polity", polity.name());
        result.put("description", polity.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Generates a complete downtime profile.
     * <p>
     * Combines a maintenance event with a related polity for downtime activities.
     * </p>
     */
    @GetMapping("/oracle/downtime")
    public ResponseEntity<Map<String, String>> rollDowntime() {
        log.debug("GET /api/v1/captains-log/oracle/downtime");
        Map<String, String> profile =
                captainLogService.getProbabilityMatrixService().generateDowntimeProfile();
        return ResponseEntity.ok(profile);
    }

    // ---- Appendix II: Allies and Adversaries ----

    /**
     * Rolls for a random adversary.
     * <p>
     * Returns an adversary type from the Appendix II table.
     * </p>
     */
    @GetMapping("/oracle/adversary")
    public ResponseEntity<Map<String, String>> rollAdversary() {
        log.debug("GET /api/v1/captains-log/oracle/adversary");
        ProbabilityMatrixService.Adversary adversary =
                captainLogService.getProbabilityMatrixService().rollAdversary();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("adversary", adversary.name());
        result.put("description", adversary.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Rolls for a random ally.
     * <p>
     * Returns an ally type from the Appendix II table.
     * </p>
     */
    @GetMapping("/oracle/ally")
    public ResponseEntity<Map<String, String>> rollAlly() {
        log.debug("GET /api/v1/captains-log/oracle/ally");
        ProbabilityMatrixService.Ally ally =
                captainLogService.getProbabilityMatrixService().rollAlly();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("ally", ally.name());
        result.put("description", ally.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Generates a complete NPC profile.
     * <p>
     * Combines an ally/adversary with a role and motivation.
     * </p>
     */
    @GetMapping("/oracle/npc")
    public ResponseEntity<Map<String, String>> generateNpc() {
        log.debug("GET /api/v1/captains-log/oracle/npc");
        ProbabilityMatrixService.Adversary adversary =
                captainLogService.getProbabilityMatrixService().rollAdversary();
        Map<String, String> profile = new java.util.LinkedHashMap<>();
        profile.put("type", adversary.name());
        profile.put("description", adversary.getDescription());
        return ResponseEntity.ok(profile);
    }

    // ---- Appendix III: Locations and Starships ----

    /**
     * Rolls for a random location.
     * <p>
     * Returns a location type from the Appendix III table.
     * </p>
     */
    @GetMapping("/oracle/location")
    public ResponseEntity<Map<String, String>> rollLocation() {
        log.debug("GET /api/v1/captains-log/oracle/location");
        ProbabilityMatrixService.LocationType location =
                captainLogService.getProbabilityMatrixService().rollLocationType();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("location", location.name());
        result.put("description", location.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Rolls for a random starship type.
     * <p>
     * Returns a starship type from the Appendix III table.
     * </p>
     */
    @GetMapping("/oracle/starship")
    public ResponseEntity<Map<String, String>> rollStarship() {
        log.debug("GET /api/v1/captains-log/oracle/starship");
        ProbabilityMatrixService.StarshipClass starship =
                captainLogService.getProbabilityMatrixService().rollStarshipClass();
        Map<String, String> result = new java.util.LinkedHashMap<>();
        result.put("starship", starship.name());
        result.put("description", starship.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * Generates a complete starship encounter profile.
     * <p>
     * Combines a starship type with a crew and cargo.
     * </p>
     */
    @GetMapping("/oracle/starship-encounter")
    public ResponseEntity<Map<String, String>> generateStarshipEncounter() {
        log.debug("GET /api/v1/captains-log/oracle/starship-encounter");
        Map<String, String> profile =
                captainLogService.getProbabilityMatrixService().generateStarshipEncounter();
        return ResponseEntity.ok(profile);
    }

    // ---- Mission Development ----

    /**
     * Rolls a random mission development from all categories.
     */
    @GetMapping("/missions/development")
    public ResponseEntity<MissionDevelopmentService.DevelopmentDefinition> rollDevelopment() {
        log.debug("GET /api/v1/captains-log/missions/development");
        return ResponseEntity.ok(captainLogService.getMissionDevelopmentService().rollDevelopment());
    }

    /**
     * Rolls a development from a specific category.
     */
    @GetMapping("/missions/development/{category}")
    public ResponseEntity<MissionDevelopmentService.DevelopmentDefinition> rollDevelopmentByCategory(@PathVariable String category) {
        log.debug("GET /api/v1/captains-log/missions/development/{}", category);
        MissionDevelopmentService.Category cat;
        try {
            cat = MissionDevelopmentService.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        return ResponseEntity.ok(captainLogService.getMissionDevelopmentService().rollDevelopment(cat));
    }

    /**
     * Rolls a development based on the mission type.
     */
    @GetMapping("/missions/development/mission/{missionType}")
    public ResponseEntity<MissionDevelopmentService.DevelopmentDefinition> rollDevelopmentByMissionType(@PathVariable String missionType) {
        log.debug("GET /api/v1/captains-log/missions/development/mission/{}", missionType);
        CaptainLogMission.MissionType mType;
        try {
            mType = CaptainLogMission.MissionType.valueOf(missionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
        return ResponseEntity.ok(captainLogService.getMissionDevelopmentService().rollDevelopmentForMission(mType));
    }

    /**
     * Lists all development categories.
     */
    @GetMapping("/missions/development/categories")
    public ResponseEntity<List<Map<String, String>>> listDevelopmentCategories() {
        log.debug("GET /api/v1/captains-log/missions/development/categories");
        return ResponseEntity.ok(captainLogService.getMissionDevelopmentService().listCategories());
    }

    /**
     * Lists all developments for a given category.
     */
    @GetMapping("/missions/development/list/{category}")
    public ResponseEntity<List<MissionDevelopmentService.DevelopmentDefinition>> listDevelopments(@PathVariable String category) {
        log.debug("GET /api/v1/captains-log/missions/development/list/{}", category);
        MissionDevelopmentService.Category cat;
        try {
            cat = MissionDevelopmentService.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }
        return ResponseEntity.ok(captainLogService.getMissionDevelopmentService().getDevelopmentsByCategory(cat));
    }

    /**
     * Lists all available developments across all categories.
     */
    @GetMapping("/missions/development/all")
    public ResponseEntity<Map<MissionDevelopmentService.Category, List<MissionDevelopmentService.DevelopmentDefinition>>> listAllDevelopments() {
        log.debug("GET /api/v1/captains-log/missions/development/all");
        return ResponseEntity.ok(captainLogService.getMissionDevelopmentService().getAllDevelopments());
    }
}

package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.ComplicationResolutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Captain's Log complication resolution endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides complication and advantage resolution by category and mission type.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogComplicationController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogComplicationController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogComplicationController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Resolves a complication by category.
     * <p>
     * Returns a context-aware complication from the specified category table.
     * Categories: COMBAT, EXPLORATION, DIPLOMACY, SCIENCE, PERSONAL, CRISIS, GENERAL
     * </p>
     */
    @GetMapping("/complication/resolve/{category}")
    public ResponseEntity<Map<String, String>> resolveComplication(@PathVariable String category) {
        log.debug("GET /api/v1/captains-log/complication/resolve/{}", category);
        ComplicationResolutionService.Category cat;
        try {
            cat = ComplicationResolutionService.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid category. Use: COMBAT, EXPLORATION, DIPLOMACY, SCIENCE, PERSONAL, CRISIS, GENERAL"));
        }
        Map<String, String> result = captainLogService.getComplicationResolutionService().resolveComplication(cat);
        return ResponseEntity.ok(result);
    }

    /**
     * Resolves a complication by mission type.
     * <p>
     * Infers the complication category from the mission type and rolls on the appropriate table.
     * </p>
     */
    @GetMapping("/complication/resolve/mission/{missionType}")
    public ResponseEntity<Map<String, String>> resolveComplicationByMissionType(@PathVariable String missionType) {
        log.debug("GET /api/v1/captains-log/complication/resolve/mission/{}", missionType);
        CaptainLogMission.MissionType mType;
        try {
            mType = CaptainLogMission.MissionType.valueOf(missionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid mission type."));
        }
        Map<String, String> result = captainLogService.getComplicationResolutionService().resolveComplication(mType);
        return ResponseEntity.ok(result);
    }

    /**
     * Resolves an advantage by category.
     * <p>
     * Returns a context-aware advantage from the specified category table.
     * </p>
     */
    @GetMapping("/advantage/resolve/{category}")
    public ResponseEntity<Map<String, String>> resolveAdvantage(@PathVariable String category) {
        log.debug("GET /api/v1/captains-log/advantage/resolve/{}", category);
        ComplicationResolutionService.Category cat;
        try {
            cat = ComplicationResolutionService.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid category. Use: COMBAT, EXPLORATION, DIPLOMACY, SCIENCE, PERSONAL, CRISIS, GENERAL"));
        }
        Map<String, String> result = captainLogService.getComplicationResolutionService().resolveAdvantage(cat);
        return ResponseEntity.ok(result);
    }

    /**
     * Resolves an advantage by mission type.
     * <p>
     * Infers the advantage category from the mission type and rolls on the appropriate table.
     * </p>
     */
    @GetMapping("/advantage/resolve/mission/{missionType}")
    public ResponseEntity<Map<String, String>> resolveAdvantageByMissionType(@PathVariable String missionType) {
        log.debug("GET /api/v1/captains-log/advantage/resolve/mission/{}", missionType);
        CaptainLogMission.MissionType mType;
        try {
            mType = CaptainLogMission.MissionType.valueOf(missionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid mission type."));
        }
        Map<String, String> result = captainLogService.getComplicationResolutionService().resolveAdvantage(mType);
        return ResponseEntity.ok(result);
    }

    /**
     * Lists all complication categories.
     */
    @GetMapping("/complication/categories")
    public ResponseEntity<List<Map<String, String>>> listComplicationCategories() {
        log.debug("GET /api/v1/captains-log/complication/categories");
        List<Map<String, String>> categories =
                captainLogService.getComplicationResolutionService().listCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Lists all complications for a given category.
     */
    @GetMapping("/complication/list/{category}")
    public ResponseEntity<List<Map<String, String>>> listComplications(@PathVariable String category) {
        log.debug("GET /api/v1/captains-log/complication/list/{}", category);
        ComplicationResolutionService.Category cat;
        try {
            cat = ComplicationResolutionService.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of(Map.of("error", "Invalid category.")));
        }
        List<Map<String, String>> complications =
                captainLogService.getComplicationResolutionService().listComplications(cat);
        return ResponseEntity.ok(complications);
    }

    /**
     * Lists all advantages for a given category.
     */
    @GetMapping("/advantage/list/{category}")
    public ResponseEntity<List<Map<String, String>>> listAdvantages(@PathVariable String category) {
        log.debug("GET /api/v1/captains-log/advantage/list/{}", category);
        ComplicationResolutionService.Category cat;
        try {
            cat = ComplicationResolutionService.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of(Map.of("error", "Invalid category.")));
        }
        List<Map<String, String>> advantages =
                captainLogService.getComplicationResolutionService().listAdvantages(cat);
        return ResponseEntity.ok(advantages);
    }
}

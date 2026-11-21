package com.yisy.modules.doramcu.captainslog.controller;

import com.yisy.modules.doramcu.captainslog.dto.StarshipCreationChoiceRequest;
import com.yisy.modules.doramcu.captainslog.dto.StarshipCreationStartRequest;
import com.yisy.modules.doramcu.captainslog.dto.StarshipCreationStepResponse;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.StarshipCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Captain's Log starship creation endpoints.
 * Extracted from CaptainLogController to reduce god class size.
 * <p>
 * Provides the starship creation wizard flow.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogStarshipCreationController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogStarshipCreationController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogStarshipCreationController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    /**
     * Starts a new starship creation session.
     */
    @PostMapping("/starships/create/start")
    public ResponseEntity<StarshipCreationStepResponse> startStarshipCreation(@RequestBody StarshipCreationStartRequest request) {
        log.debug("POST /api/v1/captains-log/starships/create/start - creating: {}", request.getName());
        StarshipCreationStepResponse response = captainLogService.getStarshipCreationService().startCreation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Makes a choice in the starship creation wizard.
     */
    @PostMapping("/starships/create/{sessionId}/choice")
    public ResponseEntity<Object> makeStarshipCreationChoice(@PathVariable Long sessionId,
                                                              @RequestBody StarshipCreationChoiceRequest request) {
        log.debug("POST /api/v1/captains-log/starships/create/{}/choice", sessionId);
        Object result = captainLogService.getStarshipCreationService().makeChoice(sessionId, request);
        if (result instanceof CaptainLogStarship) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Lists all available ship classes for creation.
     */
    @GetMapping("/starships/create/classes")
    public ResponseEntity<List<StarshipCreationService.ShipClassDefinition>> listStarshipCreationClasses() {
        log.debug("GET /api/v1/captains-log/starships/create/classes");
        return ResponseEntity.ok(captainLogService.getStarshipCreationService().getAvailableShipClasses());
    }

    /**
     * Lists all available ship traits for creation.
     */
    @GetMapping("/starships/create/traits")
    public ResponseEntity<List<String>> listStarshipCreationTraits() {
        log.debug("GET /api/v1/captains-log/starships/create/traits");
        return ResponseEntity.ok(captainLogService.getStarshipCreationService().getAvailableTraits());
    }
}

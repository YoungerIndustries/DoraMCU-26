package com.yisy.modules.doramcu.earth2026.controller;
import com.yisy.modules.doramcu.ai.client.AiRestClient.SynthesizeResponse;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.earth2026.model.HqPortal;
import com.yisy.modules.doramcu.earth2026.service.HqPortalService;
import com.yisy.modules.doramcu.earth2026.service.PortalTravelService;
import com.yisy.modules.doramcu.earth2026.service.PortalTravelService.PortalTravelResult;
import com.yisy.modules.doramcu.gamecore.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for managing portal rooms within the Younger Industries HQ.
 * Each portal connects to a different game module galaxy.
 */
@RestController
@RequestMapping("/api/v1/earth2026/hq-portals")
@CrossOrigin(origins = "*")
public class HqPortalController {

    private static final Logger log = LoggerFactory.getLogger(HqPortalController.class);

    private final HqPortalService hqPortalService;
    private final PortalTravelService portalTravelService;

    public HqPortalController(HqPortalService hqPortalService, PortalTravelService portalTravelService) {
        this.hqPortalService = hqPortalService;
        this.portalTravelService = portalTravelService;
    }

    /**
     * GET /api/v1/earth2026/hq-portals?hqId=1
     * Get all portals for a specific HQ.
     */
    @GetMapping
    public List<HqPortal> getPortals(@RequestParam(required = false) Long hqId,
                                      @RequestParam(defaultValue = "true") boolean activeOnly) {
        if (hqId != null) {
            return activeOnly ? hqPortalService.getActivePortalsByHq(hqId) : hqPortalService.getPortalsByHq(hqId);
        }
        return hqPortalService.getAllActivePortals();
    }

    /**
     * GET /api/v1/earth2026/hq-portals/{id}
     * Get a specific portal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HqPortal> getPortal(@PathVariable Long id) {
        return hqPortalService.getPortalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/earth2026/hq-portals/hq/{hqId}/game/{gameType}
     * Get a specific portal by HQ and game type.
     */
    @GetMapping("/hq/{hqId}/game/{gameType}")
    public ResponseEntity<HqPortal> getPortalByGameType(@PathVariable Long hqId,
                                                         @PathVariable String gameType) {
        try {
            GameType gt = GameType.valueOf(gameType.toUpperCase());
            return hqPortalService.getPortalByGameType(hqId, gt)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/v1/earth2026/hq-portals/summary?hqId=1
     * Get a summary of all portals grouped by basement level.
     */
    @GetMapping("/summary")
    public Map<String, Object> getPortalSummary(@RequestParam(defaultValue = "1") Long hqId) {
        List<HqPortal> allPortals = hqPortalService.getPortalsByHq(hqId);
        List<HqPortal> activePortals = hqPortalService.getActivePortalsByHq(hqId);
        return Map.of(
                "hqId", hqId,
                "totalPortals", allPortals.size(),
                "activePortals", activePortals.size(),
                "portals", allPortals
        );
    }

    /**
     * POST /api/v1/earth2026/hq-portals
     * Create a new portal.
     */
    @PostMapping
    public HqPortal createPortal(@RequestBody HqPortal portal) {
        return hqPortalService.createPortal(portal);
    }

    /**
     * PUT /api/v1/earth2026/hq-portals/{id}
     * Update an existing portal.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HqPortal> updatePortal(@PathVariable Long id, @RequestBody HqPortal updated) {
        return hqPortalService.updatePortal(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/v1/earth2026/hq-portals/{id}/toggle
     * Toggle the active state of a portal.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<HqPortal> togglePortal(@PathVariable Long id) {
        return hqPortalService.togglePortalActive(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/earth2026/hq-portals/{id}
     * Delete a portal.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortal(@PathVariable Long id) {
        if (hqPortalService.deletePortal(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/v1/earth2026/hq-portals/{id}/travel
     * Travel through a portal to the target galaxy.
     * <p>
     * This endpoint finds the galaxy for the portal's GameType, creates or reuses
     * a game session for that galaxy, joins the player, and returns the session URL.
     * </p>
     *
     * @param id the portal ID
     * @param request body with optional playerId and playerName
     * @return travel result with session info and navigation URL
     */
    @PostMapping("/{id}/travel")
    public ResponseEntity<?> travelThroughPortal(@PathVariable Long id,
                                                  @RequestBody(required = false) Map<String, Object> request) {
        HqPortal portal = hqPortalService.getPortalById(id).orElse(null);
        if (portal == null) {
            return ResponseEntity.notFound().build();
        }

        if (!portal.isActive()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Portal is offline",
                    "message", portal.getPortalName() + " is currently offline"
            ));
        }

        Long playerId = null;
        if (request != null && request.get("playerId") != null) {
            Object pidObj = request.get("playerId");
            if (pidObj instanceof Number) {
                playerId = ((Number) pidObj).longValue();
            } else if (pidObj instanceof String) {
                try {
                    playerId = Long.parseLong(((String) pidObj).trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid playerId string: {}", pidObj);
                }
            }
        }
        String playerName = request != null && request.get("playerName") != null
                ? request.get("playerName").toString() : "Guest";

        log.info("Travel request: player '{}' through portal '{}' -> {}",
                playerName, portal.getPortalName(), portal.getGameType());

        try {
            PortalTravelResult result = portalTravelService.travelToGalaxy(
                    portal.getGameType(), playerId, playerName);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", result.getMessage(),
                        "message", result.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            log.error("Portal travel failed for portal {}: {}", portal.getPortalName(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Travel failed",
                    "message", e.getMessage()
            ));
        }
    }
}

package com.yisy.modules.doramcu.controller.galaxy;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.admin.dto.AiAdminVisibilityDTO;

import com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService;
import com.yisy.modules.doramcu.service.galaxy.Galaxy3DService;
import com.yisy.modules.doramcu.service.galaxy.Galaxy3DService.CelestialBody3D;
import com.yisy.modules.doramcu.service.galaxy.Galaxy3DService.Position3D;
import com.yisy.modules.doramcu.service.galaxy.Galaxy3DService.CameraPreset;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for 3D galaxy visualization data.
 * <p>
 * Provides endpoints for WebGL/Three.js clients to fetch galaxy data
 * for immersive 3D galaxy exploration.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/galaxy-3d")
public class Galaxy3DController {

    private final Galaxy3DService galaxy3DService;
    private final AiAdminVisibilityService aiAdminVisibilityService;

    public Galaxy3DController(Galaxy3DService galaxy3DService,
                              AiAdminVisibilityService aiAdminVisibilityService) {
        this.galaxy3DService = galaxy3DService;
        this.aiAdminVisibilityService = aiAdminVisibilityService;
    }

    /**
     * Get all available body types.
     */
    @GetMapping("/body-types")
    public List<Galaxy3DService.BodyType> getBodyTypes() {
        return galaxy3DService.getBodyTypes();
    }

    /**
     * Get all available color schemes.
     */
    @GetMapping("/color-schemes")
    public List<Galaxy3DService.ColorScheme> getColorSchemes() {
        return galaxy3DService.getColorSchemes();
    }

    /**
     * Get 3D galaxy data for a specific galaxy.
     */
    @GetMapping("/galaxy/{galaxyName}")
    public ResponseEntity<?> getGalaxy3DData(@PathVariable String galaxyName,
                                              @RequestParam(defaultValue = "realistic") String colorScheme) {
        if (galaxyName == null || galaxyName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Galaxy name is required"));
        }

        Galaxy3DService.ColorScheme scheme;
        try {
            scheme = Galaxy3DService.ColorScheme.valueOf(colorScheme.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid color scheme: " + colorScheme));
        }

        return galaxy3DService.getGalaxy3DData(galaxyName, scheme)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get camera presets.
     */
    @GetMapping("/camera-presets")
    public List<CameraPreset> getCameraPresets() {
        return galaxy3DService.getCameraPresets();
    }

    /**
     * Get bodies filtered by type for a galaxy.
     */
    @GetMapping("/galaxy/{galaxyName}/bodies/{type}")
    public ResponseEntity<?> getBodiesByType(@PathVariable String galaxyName,
                                              @PathVariable String type) {
        Galaxy3DService.BodyType bodyType;
        try {
            bodyType = Galaxy3DService.BodyType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid body type: " + type));
        }

        List<CelestialBody3D> bodies = galaxy3DService.getBodiesByType(galaxyName, bodyType);
        return ResponseEntity.ok(bodies);
    }

    /**
     * Get bodies near a position.
     */
    @GetMapping("/galaxy/{galaxyName}/bodies/near")
    public ResponseEntity<?> getBodiesNearPosition(@PathVariable String galaxyName,
                                                    @RequestParam double x,
                                                    @RequestParam double y,
                                                    @RequestParam double z,
                                                    @RequestParam(defaultValue = "100") double radius) {
        Position3D position = new Position3D(x, y, z);
        List<CelestialBody3D> bodies = galaxy3DService.getBodiesNearPosition(galaxyName, position, radius);
        return ResponseEntity.ok(bodies);
    }

    /**
     * Get galaxy statistics.
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return galaxy3DService.getStatistics();
    }

    /**
     * Clear the cache.
     */
    @DeleteMapping("/cache")
    public ResponseEntity<?> clearCache() {
        galaxy3DService.clearCache();
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Remove a galaxy from cache.
     */
    @DeleteMapping("/cache/{galaxyName}")
    public ResponseEntity<?> removeGalaxyCache(@PathVariable String galaxyName) {
        galaxy3DService.removeGalaxyCache(galaxyName);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Get AI admins near a player's position in a galaxy.
     * Only returns admins within the specified radius.
     */
    @GetMapping("/galaxy/{galaxyName}/ai-admins/near")
    public ResponseEntity<?> getAiAdminsNear(@PathVariable String galaxyName,
                                              @RequestParam double x,
                                              @RequestParam double y,
                                              @RequestParam double z,
                                              @RequestParam(defaultValue = "50") double radius,
                                              @RequestParam(required = false) Boolean availableOnly) {
        List<AiAdminVisibilityDTO> admins;
        if (Boolean.TRUE.equals(availableOnly)) {
            admins = aiAdminVisibilityService
                    .getAiAdminsNearPlayerByAvailability(galaxyName, x, y, z, radius, true);
        } else {
            admins = aiAdminVisibilityService
                    .getAiAdminsNearPlayer(galaxyName, x, y, z, radius);
        }
        return ResponseEntity.ok(admins);
    }

    /**
     * Get all AI admins in a galaxy (for initial load).
     */
    @GetMapping("/galaxy/{galaxyName}/ai-admins")
    public ResponseEntity<?> getAllAiAdminsInGalaxy(@PathVariable String galaxyName) {
        List<AiAdminVisibilityDTO> admins = aiAdminVisibilityService
                .getAllAiAdminsInGalaxy(galaxyName);
        return ResponseEntity.ok(admins);
    }

    /**
     * Get available (idle) AI admins in a galaxy.
     */
    @GetMapping("/galaxy/{galaxyName}/ai-admins/available")
    public ResponseEntity<?> getAvailableAiAdminsInGalaxy(@PathVariable String galaxyName) {
        List<AiAdminVisibilityDTO> admins = aiAdminVisibilityService
                .getAvailableAiAdminsInGalaxy(galaxyName);
        return ResponseEntity.ok(admins);
    }

    /**
     * Get a specific AI admin by ID with visibility information.
     */
    @GetMapping("/ai-admins/{adminId}")
    public ResponseEntity<?> getAiAdminById(@PathVariable Long adminId) {
        return aiAdminVisibilityService.getAiAdminById(adminId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

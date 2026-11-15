package com.yisy.modules.doramcu.earth2026.survival;

import com.yisy.modules.doramcu.earth2026.survival.dto.ScavengeResultDto;
import com.yisy.modules.doramcu.earth2026.survival.dto.SurvivalStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Earth2026 survival mode.
 * <p>
 * Provides endpoints for survival status, actions (eat, drink, rest, scavenge),
 * and resource management.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/survival")
public class Earth2026SurvivalController {

    private final SurvivalService survivalService;
    private final ResourceService resourceService;
    private final SurvivalStateRepository stateRepository;

    public Earth2026SurvivalController(SurvivalService survivalService,
                              ResourceService resourceService,
                              SurvivalStateRepository stateRepository) {
        this.survivalService = survivalService;
        this.resourceService = resourceService;
        this.stateRepository = stateRepository;
    }

    /**
     * Get survival status for a character.
     *
     * @param characterId the character ID
     * @return the survival status
     */
    @GetMapping("/{characterId}/status")
    public ResponseEntity<SurvivalStatusDto> getStatus(@PathVariable Long characterId) {
        SurvivalStatusDto status = survivalService.getStatus(characterId);
        return ResponseEntity.ok(status);
    }

    /**
     * Eat a food resource.
     *
     * @param characterId the character ID
     * @param resourceName the name of the food resource
     * @return the updated survival status
     */
    @PostMapping("/{characterId}/eat")
    public ResponseEntity<SurvivalStatusDto> eat(
            @PathVariable Long characterId,
            @RequestParam String resourceName) {
        SurvivalStatusDto status = survivalService.eat(characterId, resourceName);
        return ResponseEntity.ok(status);
    }

    /**
     * Drink a water resource.
     *
     * @param characterId the character ID
     * @param resourceName the name of the water resource
     * @return the updated survival status
     */
    @PostMapping("/{characterId}/drink")
    public ResponseEntity<SurvivalStatusDto> drink(
            @PathVariable Long characterId,
            @RequestParam String resourceName) {
        SurvivalStatusDto status = survivalService.drink(characterId, resourceName);
        return ResponseEntity.ok(status);
    }

    /**
     * Rest to reduce fatigue.
     *
     * @param characterId the character ID
     * @param hours number of hours to rest (default 1)
     * @return the updated survival status
     */
    @PostMapping("/{characterId}/rest")
    public ResponseEntity<SurvivalStatusDto> rest(
            @PathVariable Long characterId,
            @RequestParam(defaultValue = "1") int hours) {
        SurvivalStatusDto status = survivalService.rest(characterId, hours);
        return ResponseEntity.ok(status);
    }

    /**
     * Scavenge for resources at a location.
     *
     * @param characterId the character ID
     * @param location the location name (optional)
     * @return the scavenge result
     */
    @PostMapping("/{characterId}/scavenge")
    public ResponseEntity<ScavengeResultDto> scavenge(
            @PathVariable Long characterId,
            @RequestParam(required = false) String location) {
        ScavengeResultDto result = survivalService.scavenge(characterId, location);
        return ResponseEntity.ok(result);
    }

    /**
     * List all available resources.
     *
     * @return list of all resources
     */
    @GetMapping("/resources")
    public ResponseEntity<List<SurvivalResource>> getResources() {
        List<SurvivalResource> resources = resourceService.getAllResources();
        return ResponseEntity.ok(resources);
    }

    /**
     * List resources by category.
     *
     * @param category the resource category
     * @return list of resources in the category
     */
    @GetMapping("/resources/category/{category}")
    public ResponseEntity<List<SurvivalResource>> getResourcesByCategory(@PathVariable String category) {
        List<SurvivalResource> resources = resourceService.getResourcesByCategory(category);
        return ResponseEntity.ok(resources);
    }

    /**
     * Toggle survival mode for a character.
     *
     * @param characterId the character ID
     * @param active whether to enable or disable survival mode
     * @return status response
     */
    @PostMapping("/{characterId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSurvivalMode(
            @PathVariable Long characterId,
            @RequestParam(defaultValue = "true") boolean active) {
        SurvivalState state = survivalService.getOrCreateState(characterId);
        state.setActive(active);
        stateRepository.save(state);

        Map<String, Object> response = new HashMap<>();
        response.put("characterId", characterId);
        response.put("survivalMode", active);
        response.put("message", active ? "Survival mode enabled" : "Survival mode disabled");
        return ResponseEntity.ok(response);
    }
}

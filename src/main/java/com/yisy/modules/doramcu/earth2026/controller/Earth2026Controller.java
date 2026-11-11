package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Scenario;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Earth 2026 gameplay.
 * <p>
 * Provides endpoints for character creation, location management,
 * scenario progression, and galaxy transitions.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026")
public class Earth2026Controller {

    private final Earth2026Service earth2026Service;

    public Earth2026Controller(Earth2026Service earth2026Service) {
        this.earth2026Service = earth2026Service;
    }

    // ==================== Characters ====================

    @PostMapping("/characters")
    public ResponseEntity<?> createCharacter(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Character name is required"));
        }
        int age = request.get("age") != null ? (int) request.get("age") : 25;
        String occupation = request.get("occupation") != null ? (String) request.get("occupation") : null;
        Long playerId = request.get("playerId") != null ? ((Number) request.get("playerId")).longValue() : null;
        Earth2026Character character = earth2026Service.createCharacter(name, age, occupation, playerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }

    @GetMapping("/characters")
    public ResponseEntity<List<Earth2026Character>> getCharactersOnEarth() {
        return ResponseEntity.ok(earth2026Service.getCharactersOnEarth());
    }

    @GetMapping("/characters/{id}")
    public ResponseEntity<?> getCharacter(@PathVariable Long id) {
        return earth2026Service.getCharacter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/characters/{id}/location")
    public ResponseEntity<?> moveCharacter(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String location = request.get("location");
        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required"));
        }
        return earth2026Service.moveCharacter(id, location)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Galaxy Transitions ====================

    @PutMapping("/characters/{id}/target-galaxy")
    public ResponseEntity<?> setTargetGalaxy(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String targetGalaxy = request.get("targetGalaxy");
        if (targetGalaxy == null || targetGalaxy.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target galaxy is required"));
        }
        try {
            return earth2026Service.setTargetGalaxy(id, targetGalaxy)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/characters/{id}/transition")
    public ResponseEntity<?> completeTransition(@PathVariable Long id) {
        try {
            return earth2026Service.completeTransition(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Scenarios ====================

    @GetMapping("/scenarios")
    public ResponseEntity<List<Earth2026Scenario>> getAvailableScenarios(@RequestParam(defaultValue = "1") int level) {
        return ResponseEntity.ok(earth2026Service.getAvailableScenarios(level));
    }

    @PostMapping("/scenarios/{id}/start")
    public ResponseEntity<?> startScenario(@PathVariable Long id) {
        try {
            return earth2026Service.startScenario(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/scenarios/{id}/complete")
    public ResponseEntity<?> completeScenario(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Boolean success = request.get("success");
        if (success == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Success flag is required"));
        }
        return earth2026Service.completeScenario(id, success)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== XP ====================

    @PostMapping("/characters/{id}/award-xp")
    public ResponseEntity<?> awardXp(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer xp = request.get("xp");
        if (xp == null || xp <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "XP must be a positive number"));
        }
        return earth2026Service.awardXp(id, xp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Locations ====================

    @GetMapping("/locations")
    public ResponseEntity<List<Earth2026Location>> getAllLocations() {
        return ResponseEntity.ok(earth2026Service.getAllLocations());
    }

    @GetMapping("/locations/portals")
    public ResponseEntity<List<Earth2026Location>> getPortalLocations() {
        return ResponseEntity.ok(earth2026Service.getPortalLocations());
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<?> getLocation(@PathVariable Long id) {
        return earth2026Service.getLocation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Supernatural ====================

    @PostMapping("/characters/{id}/enable-supernatural")
    public ResponseEntity<?> enableSupernatural(@PathVariable Long id) {
        return earth2026Service.enableSupernatural(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/characters/{id}/reduce-sanity")
    public ResponseEntity<?> reduceSanity(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer amount = request.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount must be a positive number"));
        }
        try {
            return earth2026Service.reduceSanity(id, amount)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

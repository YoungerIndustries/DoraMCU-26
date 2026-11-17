package com.yisy.modules.doramcu.controller.game;

import com.yisy.modules.doramcu.service.game.CrossGameService;
import com.yisy.modules.doramcu.service.game.CrossGameService.Ability;
import com.yisy.modules.doramcu.service.game.CrossGameService.CharacterSummary;
import com.yisy.modules.doramcu.service.game.CrossGameService.CrossGameCharacter;
import com.yisy.modules.doramcu.service.game.CrossGameService.PortRecord;
import com.yisy.modules.doramcu.service.game.CrossGameService.Universe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Optional;

/**
 * REST controller for cross-game character operations.
 * <p>
 * Provides endpoints for creating, porting, and managing characters
 * that span multiple game universes.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/cross-game")
public class CrossGameController {

    private final CrossGameService crossGameService;

    public CrossGameController(CrossGameService crossGameService) {
        this.crossGameService = crossGameService;
    }

    /**
     * Get all supported universes.
     */
    @GetMapping("/universes")
    public List<Universe> getUniverses() {
        return crossGameService.getSupportedUniverses();
    }

    /**
     * Get all ability types.
     */
    @GetMapping("/abilities")
    public List<Ability> getAbilities() {
        return crossGameService.getAbilities();
    }

    /**
     * Create a new cross-game character.
     */
    @PostMapping("/characters")
    public ResponseEntity<?> createCharacter(@RequestBody CreateCharacterRequest request) {
        if (request.id == null || request.id.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Character ID is required"));
        }
        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Character name is required"));
        }
        Universe universe = request.universe != null ? request.universe : Universe.GENERIC;
        Map<Ability, Integer> scores = request.scores != null ? request.scores : new EnumMap<>(Ability.class);
        Optional<CrossGameCharacter> result = crossGameService.createCharacter(request.id, request.name, universe, scores);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to create character"));
    }

    /**
     * Get a character by ID.
     */
    @GetMapping("/characters/{id}")
    public ResponseEntity<?> getCharacter(@PathVariable String id) {
        return crossGameService.getCharacter(id)
                .map(c -> ResponseEntity.ok(c))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * List all characters.
     */
    @GetMapping("/characters")
    public List<CharacterSummary> getAllCharacters() {
        return crossGameService.getAllCharacters();
    }

    /**
     * Get characters by current universe.
     */
    @GetMapping("/characters/universe/{universe}")
    public List<CharacterSummary> getCharactersByUniverse(@PathVariable String universe) {
        try {
            return crossGameService.getCharactersByUniverse(Universe.valueOf(universe.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get characters by original universe.
     */
    @GetMapping("/characters/original/{universe}")
    public List<CharacterSummary> getCharactersByOriginalUniverse(@PathVariable String universe) {
        try {
            return crossGameService.getCharactersByOriginalUniverse(Universe.valueOf(universe.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Search characters by name.
     */
    @GetMapping("/characters/search/{query}")
    public List<CharacterSummary> searchCharacters(@PathVariable String query) {
        return crossGameService.searchCharacters(query);
    }

    /**
     * Port a character to a different universe.
     */
    @PostMapping("/characters/{id}/port")
    public ResponseEntity<?> portCharacter(@PathVariable String id,
                                            @RequestBody PortRequest request) {
        Optional<PortRecord> result = crossGameService.portCharacter(id, request.targetUniverse, request.notes);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to port character"));
    }

    /**
     * Get port history for a character.
     */
    @GetMapping("/characters/{id}/port-history")
    public List<PortRecord> getPortHistory(@PathVariable String id) {
        return crossGameService.getPortHistory(id);
    }

    /**
     * Add a career entry to a character.
     */
    @PostMapping("/characters/{id}/career")
    public ResponseEntity<?> addCareer(@PathVariable String id,
                                        @RequestBody CareerRequest request) {
        boolean added = crossGameService.addCareer(id, request.career);
        if (added) {
            return ResponseEntity.ok(Map.of("success", true, "career", request.career));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Character not found"));
    }

    /**
     * Update a character's scores.
     */
    @PutMapping("/characters/{id}/scores")
    public ResponseEntity<?> updateScores(@PathVariable String id,
                                           @RequestBody Map<Ability, Integer> scores) {
        boolean updated = crossGameService.updateScores(id, scores);
        if (updated) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Character not found"));
    }

    /**
     * Set universe-specific data for a character.
     */
    @PostMapping("/characters/{id}/universe-data")
    public ResponseEntity<?> setUniverseData(@PathVariable String id,
                                              @RequestBody UniverseDataRequest request) {
        boolean set = crossGameService.setUniverseData(id, request.universe, request.data);
        if (set) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Character not found"));
    }

    /**
     * Map scores to a target universe.
     */
    @PostMapping("/map-scores")
    public Map<Ability, Integer> mapScores(@RequestBody Map<Ability, Integer> scores) {
        return crossGameService.mapScoresToUniverse(scores, Universe.GENERIC);
    }

    /**
     * Remove a character.
     */
    @DeleteMapping("/characters/{id}")
    public ResponseEntity<?> removeCharacter(@PathVariable String id) {
        boolean removed = crossGameService.removeCharacter(id);
        if (removed) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get cross-game character statistics.
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return crossGameService.getStatistics();
    }

    /**
     * Clear all characters.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearAll() {
        crossGameService.clearAll();
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Request body for creating a character.
     */
    public static class CreateCharacterRequest {
        public String id;
        public String name;
        public Universe universe;
        public Map<Ability, Integer> scores;
    }

    /**
     * Request body for porting a character.
     */
    public static class PortRequest {
        public Universe targetUniverse;
        public String notes;
    }

    /**
     * Request body for adding a career.
     */
    public static class CareerRequest {
        public String career;
    }

    /**
     * Request body for setting universe data.
     */
    public static class UniverseDataRequest {
        public Universe universe;
        public Map<String, Object> data;
    }
}

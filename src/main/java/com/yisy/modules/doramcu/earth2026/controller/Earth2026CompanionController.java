package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Companion;
import com.yisy.modules.doramcu.earth2026.service.Earth2026CompanionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Earth 2026 Companions (pets, familiars, working animals).
 * <p>
 * Provides endpoints for companion CRUD, bond management, training progression,
 * and health operations.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/companions")
public class Earth2026CompanionController {

    private final Earth2026CompanionService companionService;

    public Earth2026CompanionController(Earth2026CompanionService companionService) {
        this.companionService = companionService;
    }

    // --- CRUD Endpoints ---

    @GetMapping
    public List<Earth2026Companion> getAllCompanions() {
        return companionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Earth2026Companion> getCompanion(@PathVariable Long id) {
        return companionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCompanion(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Companion name is required"));
            }
            String speciesId = (String) request.get("speciesId");
            if (speciesId == null || speciesId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Species ID is required"));
            }
            Long ownerCharacterId = Long.valueOf(request.get("ownerCharacterId").toString());
            String companionTypeStr = (String) request.getOrDefault("companionType", "PET");

            Earth2026Companion.CompanionType companionType = Earth2026Companion.CompanionType.valueOf(companionTypeStr);
            Earth2026Companion companion = new Earth2026Companion(
                    name.trim(), speciesId.trim(), ownerCharacterId, companionType
            );

            if (request.containsKey("breed")) {
                companion.setBreed((String) request.get("breed"));
            }
            if (request.containsKey("aiCharacterId")) {
                companion.setAiCharacterId(Long.valueOf(request.get("aiCharacterId").toString()));
            }

            Earth2026Companion saved = companionService.save(companion);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompanion(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<Earth2026Companion> optional = companionService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Earth2026Companion companion = optional.get();

        if (request.containsKey("name")) {
            companion.setName((String) request.get("name"));
        }
        if (request.containsKey("breed")) {
            companion.setBreed((String) request.get("breed"));
        }
        if (request.containsKey("companionType")) {
            companion.setCompanionType(Earth2026Companion.CompanionType.valueOf((String) request.get("companionType")));
        }
        if (request.containsKey("location")) {
            companion.setLocation(Earth2026Companion.LocationType.valueOf((String) request.get("location")));
        }
        if (request.containsKey("str")) {
            companion.setStr(Integer.valueOf(request.get("str").toString()));
        }
        if (request.containsKey("dex")) {
            companion.setDex(Integer.valueOf(request.get("dex").toString()));
        }
        if (request.containsKey("con")) {
            companion.setCon(Integer.valueOf(request.get("con").toString()));
        }
        if (request.containsKey("intel")) {
            companion.setIntel(Integer.valueOf(request.get("intel").toString()));
        }
        if (request.containsKey("wis")) {
            companion.setWis(Integer.valueOf(request.get("wis").toString()));
        }
        if (request.containsKey("cha")) {
            companion.setCha(Integer.valueOf(request.get("cha").toString()));
        }
        if (request.containsKey("tricks")) {
            companion.setTricks((String) request.get("tricks"));
        }
        if (request.containsKey("abilities")) {
            companion.setAbilities((String) request.get("abilities"));
        }
        if (request.containsKey("equipment")) {
            companion.setEquipment((String) request.get("equipment"));
        }

        return ResponseEntity.ok(companionService.save(companion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompanion(@PathVariable Long id) {
        Optional<Earth2026Companion> optional = companionService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        companionService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Companion deleted"));
    }

    // --- Query Endpoints ---

    @GetMapping("/owner/{ownerCharacterId}")
    public List<Earth2026Companion> getCompanionsByOwner(@PathVariable Long ownerCharacterId) {
        return companionService.findByOwnerCharacterId(ownerCharacterId);
    }

    @GetMapping("/owner/{ownerCharacterId}/active")
    public List<Earth2026Companion> getActiveCompanionsByOwner(@PathVariable Long ownerCharacterId) {
        return companionService.findActiveByOwnerCharacterId(ownerCharacterId);
    }

    @GetMapping("/species/{speciesId}")
    public List<Earth2026Companion> getCompanionsBySpecies(@PathVariable String speciesId) {
        return companionService.findBySpeciesId(speciesId);
    }

    @GetMapping("/type/{companionType}")
    public List<Earth2026Companion> getCompanionsByType(@PathVariable String companionType) {
        return companionService.findByCompanionType(Earth2026Companion.CompanionType.valueOf(companionType));
    }

    // --- Bond Management Endpoints ---

    @PatchMapping("/{id}/bond/increase")
    public ResponseEntity<?> increaseBond(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        int amount = Integer.parseInt(request.getOrDefault("amount", 5).toString());
        return companionService.increaseBond(id, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/bond/decrease")
    public ResponseEntity<?> decreaseBond(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        int amount = Integer.parseInt(request.getOrDefault("amount", 5).toString());
        return companionService.decreaseBond(id, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Training Endpoints ---

    @PatchMapping("/{id}/training/advance")
    public ResponseEntity<?> advanceTraining(@PathVariable Long id) {
        return companionService.advanceTraining(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Health Endpoints ---

    @PatchMapping("/{id}/heal")
    public ResponseEntity<?> healCompanion(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        int amount = Integer.parseInt(request.getOrDefault("amount", 10).toString());
        return companionService.heal(id, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/damage")
    public ResponseEntity<?> damageCompanion(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        int amount = Integer.parseInt(request.getOrDefault("amount", 5).toString());
        return companionService.damage(id, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

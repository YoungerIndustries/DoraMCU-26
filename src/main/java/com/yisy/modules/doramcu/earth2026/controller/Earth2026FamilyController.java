package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Family;
import com.yisy.modules.doramcu.earth2026.model.Earth2026FamilyMember;
import com.yisy.modules.doramcu.earth2026.service.Earth2026FamilyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Earth 2026 Families.
 * <p>
 * Provides endpoints for family CRUD, member management, bond operations,
 * and family events.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/families")
public class Earth2026FamilyController {

    private final Earth2026FamilyService familyService;

    public Earth2026FamilyController(Earth2026FamilyService familyService) {
        this.familyService = familyService;
    }

    // --- Family CRUD Endpoints ---

    @GetMapping
    public List<Earth2026Family> getAllFamilies() {
        return familyService.findActiveFamilies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Earth2026Family> getFamily(@PathVariable Long id) {
        return familyService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/{name}")
    public List<Earth2026Family> searchFamilies(@PathVariable String name) {
        return familyService.findByName(name);
    }

    @PostMapping
    public ResponseEntity<?> createFamily(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Family name is required"));
            }

            Earth2026Family family = new Earth2026Family(name.trim());

            if (request.containsKey("description")) {
                family.setDescription((String) request.get("description"));
            }
            if (request.containsKey("homeStructureId")) {
                family.setHomeStructureId(Long.valueOf(request.get("homeStructureId").toString()));
            }
            if (request.containsKey("familyWealth")) {
                family.setFamilyWealth(Integer.valueOf(request.get("familyWealth").toString()));
            }
            if (request.containsKey("familyReputation")) {
                family.setFamilyReputation(Integer.valueOf(request.get("familyReputation").toString()));
            }
            if (request.containsKey("familyTraits")) {
                family.setFamilyTraits((String) request.get("familyTraits"));
            }

            Earth2026Family saved = familyService.saveFamily(family);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFamily(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<Earth2026Family> optional = familyService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Earth2026Family family = optional.get();

        if (request.containsKey("name")) {
            family.setName((String) request.get("name"));
        }
        if (request.containsKey("description")) {
            family.setDescription((String) request.get("description"));
        }
        if (request.containsKey("homeStructureId")) {
            family.setHomeStructureId(Long.valueOf(request.get("homeStructureId").toString()));
        }
        if (request.containsKey("familyWealth")) {
            family.setFamilyWealth(Integer.valueOf(request.get("familyWealth").toString()));
        }
        if (request.containsKey("familyReputation")) {
            family.setFamilyReputation(Integer.valueOf(request.get("familyReputation").toString()));
        }
        if (request.containsKey("familyTraits")) {
            family.setFamilyTraits((String) request.get("familyTraits"));
        }
        if (request.containsKey("active")) {
            family.setActive((Boolean) request.get("active"));
        }

        return ResponseEntity.ok(familyService.saveFamily(family));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFamily(@PathVariable Long id) {
        Optional<Earth2026Family> optional = familyService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        familyService.deleteFamily(id);
        return ResponseEntity.ok(Map.of("message", "Family deleted"));
    }

    // --- Member Management Endpoints ---

    @GetMapping("/{familyId}/members")
    public List<Earth2026FamilyMember> getFamilyMembers(@PathVariable Long familyId) {
        return familyService.findActiveMembersByFamilyId(familyId);
    }

    @GetMapping("/character/{characterId}")
    public List<Earth2026FamilyMember> getCharacterFamilies(@PathVariable Long characterId) {
        return familyService.findActiveMembersByCharacterId(characterId);
    }

    @PostMapping("/{familyId}/members")
    public ResponseEntity<?> addMember(@PathVariable Long familyId, @RequestBody Map<String, Object> request) {
        try {
            String relationshipTypeStr = (String) request.get("relationshipType");
            if (relationshipTypeStr == null || relationshipTypeStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Relationship type is required"));
            }

            Long characterId = request.containsKey("characterId") ?
                    Long.valueOf(request.get("characterId").toString()) : null;
            Long npcId = request.containsKey("npcId") ?
                    Long.valueOf(request.get("npcId").toString()) : null;

            if (characterId == null && npcId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Either characterId or npcId is required"));
            }

            Earth2026FamilyMember.RelationshipType relationshipType =
                    Earth2026FamilyMember.RelationshipType.valueOf(relationshipTypeStr.trim());

            return familyService.addMember(familyId, characterId, npcId, relationshipType)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long memberId) {
        boolean removed = familyService.removeMember(memberId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Member removed"));
        }
        return ResponseEntity.notFound().build();
    }

    // --- Bond Management Endpoints ---

    @PatchMapping("/members/{memberId}/bond/increase")
    public ResponseEntity<?> increaseBond(@PathVariable Long memberId, @RequestBody Map<String, Object> request) {
        int amount = Integer.parseInt(request.getOrDefault("amount", 5).toString());
        return familyService.increaseBond(memberId, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/members/{memberId}/bond/decrease")
    public ResponseEntity<?> decreaseBond(@PathVariable Long memberId, @RequestBody Map<String, Object> request) {
        int amount = Integer.parseInt(request.getOrDefault("amount", 5).toString());
        return familyService.decreaseBond(memberId, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Family Events Endpoints ---

    @PostMapping("/{familyId}/events/gathering")
    public ResponseEntity<?> familyGathering(@PathVariable Long familyId, @RequestBody Map<String, Object> request) {
        int bondIncrease = Integer.parseInt(request.getOrDefault("bondIncrease", 10).toString());
        return familyService.familyGathering(familyId, bondIncrease)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

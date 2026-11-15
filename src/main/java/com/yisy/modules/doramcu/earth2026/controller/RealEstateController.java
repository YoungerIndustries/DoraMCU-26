package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.service.CharacterLandingService;
import com.yisy.modules.doramcu.earth2026.service.Earth2026BuildingInstanceService;
import com.yisy.modules.doramcu.earth2026.service.Earth2026RealEstateService;
import com.yisy.modules.doramcu.gamecore.BuildingInstanceService;
import com.yisy.modules.doramcu.gamecore.RealEstateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for real estate operations.
 * <p>
 * Provides endpoints for listing buildings, properties, buying/renting,
 * viewing market data, and managing dungeon instances.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/real-estate")
public class RealEstateController {

    private final Earth2026RealEstateService realEstateService;
    private final Earth2026BuildingInstanceService buildingInstanceService;
    private final CharacterLandingService characterLandingService;

    public RealEstateController(Earth2026RealEstateService realEstateService,
                                 Earth2026BuildingInstanceService buildingInstanceService,
                                 CharacterLandingService characterLandingService) {
        this.realEstateService = realEstateService;
        this.buildingInstanceService = buildingInstanceService;
        this.characterLandingService = characterLandingService;
    }

    @GetMapping("/buildings")
    public ResponseEntity<List<BuildingInstanceService.BuildingInstance>> listBuildings() {
        return ResponseEntity.ok(buildingInstanceService.listBuildings());
    }

    @GetMapping("/buildings/{id}")
    public ResponseEntity<BuildingInstanceService.BuildingInstance> getBuilding(@PathVariable Long id) {
        BuildingInstanceService.BuildingInstance building = buildingInstanceService.getBuilding(id);
        if (building == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(building);
    }

    @GetMapping("/buildings/{id}/floors")
    public ResponseEntity<List<BuildingInstanceService.FloorInstance>> getFloors(@PathVariable Long id) {
        return ResponseEntity.ok(buildingInstanceService.getFloors(id));
    }

    @GetMapping("/properties")
    public ResponseEntity<List<RealEstateService.PropertyListing>> listProperties(
            @RequestParam(required = false) String type) {
        List<RealEstateService.PropertyListing> properties;
        if (type != null && !type.isEmpty()) {
            properties = realEstateService.listPropertiesByType(type);
        } else {
            properties = realEstateService.listProperties();
        }
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/properties/{id}")
    public ResponseEntity<RealEstateService.PropertyListing> getProperty(@PathVariable Long id) {
        RealEstateService.PropertyListing listing = realEstateService.getPropertyListing(id);
        if (listing == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(listing);
    }

    @PostMapping("/buy")
    public ResponseEntity<RealEstateService.PropertyTransaction> buyProperty(
            @RequestParam Long characterId, @RequestParam Long propertyId) {
        RealEstateService.PropertyTransaction tx = realEstateService.buyProperty(characterId, propertyId);
        if (tx == null || "FAILED".equals(tx.getStatus())) {
            return ResponseEntity.badRequest().body(tx);
        }
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/rent")
    public ResponseEntity<RealEstateService.PropertyTransaction> rentProperty(
            @RequestParam Long characterId, @RequestParam Long propertyId) {
        RealEstateService.PropertyTransaction tx = realEstateService.rentProperty(characterId, propertyId);
        if (tx == null || "FAILED".equals(tx.getStatus())) {
            return ResponseEntity.badRequest().body(tx);
        }
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/release")
    public ResponseEntity<RealEstateService.PropertyTransaction> releaseProperty(
            @RequestParam Long characterId, @RequestParam Long propertyId) {
        RealEstateService.PropertyTransaction tx = realEstateService.releaseProperty(characterId, propertyId);
        if (tx == null || "FAILED".equals(tx.getStatus())) {
            return ResponseEntity.badRequest().body(tx);
        }
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/market")
    public ResponseEntity<RealEstateService.MarketData> getMarketData() {
        return ResponseEntity.ok(realEstateService.getMarketData());
    }

    @GetMapping("/owned/{characterId}")
    public ResponseEntity<List<RealEstateService.PropertyListing>> getOwnedProperties(@PathVariable Long characterId) {
        return ResponseEntity.ok(realEstateService.getOwnedProperties(characterId));
    }

    @PostMapping("/dungeon-instances")
    public ResponseEntity<BuildingInstanceService.DungeonInstance> createDungeonInstance(
            @RequestParam Long buildingId,
            @RequestParam int basementLevel,
            @RequestParam String templateId) {
        BuildingInstanceService.DungeonInstance instance =
                buildingInstanceService.configureBasement(buildingId, basementLevel, templateId);
        if (instance == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(instance);
    }

    @GetMapping("/dungeon-instances/{id}")
    public ResponseEntity<BuildingInstanceService.DungeonInstance> getDungeonInstance(@PathVariable Long id) {
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.getDungeonInstance(id);
        if (instance == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(instance);
    }

    @GetMapping("/buildings/{id}/dungeon-instances")
    public ResponseEntity<List<BuildingInstanceService.DungeonInstance>> getBuildingDungeonInstances(@PathVariable Long id) {
        return ResponseEntity.ok(buildingInstanceService.getDungeonInstances(id));
    }

    @DeleteMapping("/dungeon-instances/{id}")
    public ResponseEntity<Boolean> removeDungeonInstance(@PathVariable Long id) {
        boolean removed = buildingInstanceService.removeDungeonInstance(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(true);
    }

    @GetMapping("/dungeon-templates")
    public ResponseEntity<List<BuildingInstanceService.DungeonInstanceTemplate>> listDungeonTemplates() {
        return ResponseEntity.ok(buildingInstanceService.listDungeonTemplates());
    }

    @GetMapping("/dungeon-templates/{templateId}")
    public ResponseEntity<BuildingInstanceService.DungeonInstanceTemplate> getDungeonTemplate(@PathVariable String templateId) {
        BuildingInstanceService.DungeonInstanceTemplate template = buildingInstanceService.getDungeonTemplate(templateId);
        if (template == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(template);
    }

    @PostMapping("/land-character")
    public ResponseEntity<Map<String, Object>> landCharacter(@RequestParam Long characterId) {
        var office = characterLandingService.landCharacterAtHQ(characterId);
        Map<String, Object> result = new HashMap<>();
        if (office != null) {
            result.put("success", true);
            result.put("office", office);
            result.put("message", "Character landed at Younger Industries HQ with free office assignment");
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "No available office found for character landing");
            return ResponseEntity.badRequest().body(result);
        }
    }
}

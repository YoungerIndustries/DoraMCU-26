package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.service.ApartmentBuildingService;
import com.yisy.modules.doramcu.earth2026.service.ApartmentRentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for apartment building management.
 * <p>
 * Handles building creation, rent collection, and rent status queries.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/buildings")
public class ApartmentBuildingController {

    private final ApartmentBuildingService apartmentBuildingService;
    private final ApartmentRentService apartmentRentService;

    public ApartmentBuildingController(ApartmentBuildingService apartmentBuildingService,
                                        ApartmentRentService apartmentRentService) {
        this.apartmentBuildingService = apartmentBuildingService;
        this.apartmentRentService = apartmentRentService;
    }

    /**
     * Build a new apartment building for a corporation.
     *
     * @param requestBody map with "corporationId", "name", and optional "locationId"
     * @return the created building info
     */
    @PostMapping("/apartment")
    public ResponseEntity<?> buildApartment(@RequestBody Map<String, Object> requestBody) {
        try {
            Long corporationId = Long.valueOf(requestBody.get("corporationId").toString());
            String name = requestBody.get("name").toString();
            Long locationId = requestBody.containsKey("locationId")
                    ? Long.valueOf(requestBody.get("locationId").toString())
                    : null;

            var building = apartmentBuildingService.buildApartmentBuilding(corporationId, name, locationId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "buildingId", building.getId(),
                    "name", building.getName(),
                    "totalApartments", building.getTotalApartments(),
                    "constructionCost", ApartmentBuildingService.CONSTRUCTION_COST
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get rent status for a character.
     *
     * @param characterId the character ID
     * @return rent status info
     */
    @GetMapping("/rent/status/{characterId}")
    public ResponseEntity<Map<String, Object>> getRentStatus(@PathVariable Long characterId) {
        return ResponseEntity.ok(apartmentRentService.getRentStatus(characterId));
    }

    /**
     * Collect rent from all occupied apartments.
     * Called by the game turn processor.
     *
     * @return collection summary
     */
    @PostMapping("/rent/collect")
    public ResponseEntity<Map<String, Object>> collectRent() {
        return ResponseEntity.ok(apartmentRentService.collectRent());
    }
}

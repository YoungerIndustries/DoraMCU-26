package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.Industry;
import com.yisy.modules.doramcu.hex.planet.IndustryType;
import com.yisy.modules.doramcu.hex.planet.Settlement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for industry and building blueprint data.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexPlanetBlueprintController {

    private static final Logger log = LoggerFactory.getLogger(HexPlanetBlueprintController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;

    public HexPlanetBlueprintController(DefaultPlanetSurfaceBuilder planetSurfaceBuilder) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
    }

    /**
     * Get blueprint data for an industry.
     * <p>
     * Returns the industry type, input/output cargo, construction cost,
     * maintenance cost, and interior room template information.
     * </p>
     *
     * @param planetId the planet ID
     * @param industryId the industry ID
     * @return the industry blueprint data
     */
    @GetMapping("/{planetId}/industry/blueprint/{industryId}")
    public ResponseEntity<Map<String, Object>> getIndustryBlueprint(
            @PathVariable int planetId,
            @PathVariable String industryId) {

        Planet planet = new Planet(planetId);
        List<Industry> industries = planetSurfaceBuilder.getAllIndustries(planet);

        Industry industry = null;
        for (Industry ind : industries) {
            if (ind.getId().equals(industryId)) {
                industry = ind;
                break;
            }
        }

        if (industry == null) {
            return ResponseEntity.notFound().build();
        }

        IndustryType type = industry.getType();
        Map<String, Object> blueprint = new LinkedHashMap<>();
        blueprint.put("id", industry.getId());
        blueprint.put("name", type.getDisplayName());
        blueprint.put("type", type.name());
        blueprint.put("displayName", type.getDisplayName());
        blueprint.put("level", type.getLevel().name());
        blueprint.put("inputCargo", type.getInput());
        blueprint.put("outputCargo", type.getOutput());
        blueprint.put("constructionCost", type.getConstructionCost());
        blueprint.put("maintenanceCost", type.getMaintenanceCost());
        blueprint.put("requiredTerrain", type.getRequiredTerrain());
        blueprint.put("active", industry.isActive());
        blueprint.put("dungeonId", industry.getDungeonId());

        return ResponseEntity.ok(blueprint);
    }

    /**
     * Get blueprint data for a residential building.
     * <p>
     * Returns the building type, floor count, room templates,
     * dungeon levels, and occupancy information.
     * </p>
     *
     * @param planetId the planet ID
     * @param buildingId the building ID (format: RES-{settlementId})
     * @return the building blueprint data
     */
    @GetMapping("/{planetId}/building/blueprint/{buildingId}")
    public ResponseEntity<Map<String, Object>> getBuildingBlueprint(
            @PathVariable int planetId,
            @PathVariable String buildingId) {

        // Extract settlement ID from building ID (format: RES-{settlementId})
        String settlementId = buildingId.startsWith("RES-") ? buildingId.substring(4) : buildingId;

        Planet planet = new Planet(planetId);
        List<Settlement> settlements = planetSurfaceBuilder.getAllSettlements(planet);

        Settlement settlement = null;
        for (Settlement s : settlements) {
            if (s.getId().equals(settlementId)) {
                settlement = s;
                break;
            }
        }

        if (settlement == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> blueprint = new LinkedHashMap<>();
        blueprint.put("id", buildingId);
        blueprint.put("name", settlement.getName() + " Apartments");
        blueprint.put("buildingType", "APARTMENT_BUILDING");
        blueprint.put("floors", 10);
        blueprint.put("basementLevels", 10);
        blueprint.put("totalApartments", 40);
        blueprint.put("occupiedApartments", 20 + (settlement.getId().hashCode() % 21));
        blueprint.put("dungeonIds", settlement.getDungeonIds());
        blueprint.put("rooms", List.of(
                "Lobby", "Elevator", "Mail Room",
                "Apartment Unit 1", "Apartment Unit 2", "Apartment Unit 3",
                "Rooftop", "Basement Entrance", "Dungeon Hub",
                "Storage", "Gym", "Laundry", "Security Office"
        ));

        return ResponseEntity.ok(blueprint);
    }
}

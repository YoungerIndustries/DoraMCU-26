package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.model.PlanetTemplate;
import com.yisy.modules.doramcu.earth2026.model.TerrainElevationMap;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026LocationRepository;
import com.yisy.modules.doramcu.earth2026.repository.PlanetTemplateRepository;
import com.yisy.modules.doramcu.earth2026.service.PlanetStructureService;
import com.yisy.modules.doramcu.earth2026.service.PlanetTerrainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for planet terrain generation.
 */
@RestController
@RequestMapping("/api/earth2026/terrain")
public class PlanetTerrainController {

    private final PlanetTerrainService planetTerrainService;
    private final PlanetStructureService planetStructureService;
    private final PlanetTemplateRepository planetTemplateRepository;
    private final Earth2026LocationRepository locationRepository;

    public PlanetTerrainController(PlanetTerrainService planetTerrainService,
                                    PlanetStructureService planetStructureService,
                                    PlanetTemplateRepository planetTemplateRepository,
                                    Earth2026LocationRepository locationRepository) {
        this.planetTerrainService = planetTerrainService;
        this.planetStructureService = planetStructureService;
        this.planetTemplateRepository = planetTemplateRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Generate terrain for a planet with structures.
     */
    @PostMapping("/generate")
    public TerrainElevationMap generatePlanetTerrain(
            @RequestBody Map<String, Object> body) {
        Long planetTemplateId = (Long) body.get("planetTemplateId");
        Integer width = (Integer) body.getOrDefault("width", 100);
        Integer height = (Integer) body.getOrDefault("height", 100);
        Double cellSize = (Double) body.getOrDefault("cellSize", 10.0);

        PlanetTemplate template = planetTemplateId != null ?
                planetTemplateRepository.findById(planetTemplateId).orElse(null) : null;
        List<PlanetStructure> structures = template != null ?
                planetStructureService.getStructuresByPlanet(template.getId()) : List.of();

        return planetTerrainService.generatePlanetTerrain(template, structures, width, height, cellSize);
    }

    /**
     * Generate terrain for a location.
     */
    @GetMapping("/location/{locationId}")
    public ResponseEntity<TerrainElevationMap> generateLocationTerrain(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "100") int width,
            @RequestParam(defaultValue = "100") int height,
            @RequestParam(defaultValue = "10.0") double cellSize) {
        Earth2026Location location = locationRepository.findById(locationId).orElse(null);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        TerrainElevationMap terrain = planetTerrainService.generateLocationTerrain(location, width, height, cellSize);
        return ResponseEntity.ok(terrain);
    }

    /**
     * Analyze terrain for mine suggestions.
     */
    @GetMapping("/location/{locationId}/mine-suggestions")
    public ResponseEntity<List<MineSuggestion>> getMineSuggestions(@PathVariable Long locationId) {
        Earth2026Location location = locationRepository.findById(locationId).orElse(null);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        List<MineSuggestion> suggestions = planetTerrainService.analyzeTerrainForMines(location);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get terrain data for a planet by ID.
     */
    @GetMapping("/planet/{planetId}")
    public ResponseEntity<TerrainElevationMap> getPlanetTerrain(
            @PathVariable Long planetId,
            @RequestParam(defaultValue = "100") int width,
            @RequestParam(defaultValue = "100") int height,
            @RequestParam(defaultValue = "10.0") double cellSize) {
        List<PlanetStructure> structures = planetStructureService.getStructuresByPlanet(planetId);
        TerrainElevationMap terrain = planetTerrainService.generatePlanetTerrain(
                null, structures, width, height, cellSize);
        return ResponseEntity.ok(terrain);
    }
}

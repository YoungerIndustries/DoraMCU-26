package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.PlanetSurfacePersistenceService;
import com.yisy.modules.doramcu.hex.planet.fps.TycoonFpsBridgeService;
import com.yisy.modules.doramcu.hex.planet.fps.dto.FpsWorldStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for FPS world state endpoints.
 * Extracted from HexPlanetSurfaceController to reduce god class size.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexPlanetFpsController {

    private static final Logger log = LoggerFactory.getLogger(HexPlanetFpsController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlanetSurfacePersistenceService persistenceService;
    private final TycoonFpsBridgeService fpsBridgeService;

    public HexPlanetFpsController(
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
            PlanetSurfacePersistenceService persistenceService,
            TycoonFpsBridgeService fpsBridgeService) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.persistenceService = persistenceService;
        this.fpsBridgeService = fpsBridgeService;
    }

    /**
     * Get the FPS world state for 3D rendering.
     * <p>
     * Returns vehicles, industries, stations, and residential buildings
     * with 3D coordinates for the Babylon.js renderer.
     * </p>
     *
     * @param planetId the planet ID
     * @return the complete world state for 3D rendering
     */
    @GetMapping("/{planetId}/fps/world-state")
    public ResponseEntity<FpsWorldStateDto> getFpsWorldState(@PathVariable int planetId) {
        log.info("GET /api/v1/planets/{}/fps/world-state", planetId);

        Planet planet = new Planet(planetId);
        if (planetSurfaceBuilder.getAllTerrainData().isEmpty() && persistenceService.hasPlanetData(planetId)) {
            persistenceService.loadPlanetSurface(planet, planetSurfaceBuilder);
        }

        FpsWorldStateDto state = fpsBridgeService.getWorldState(planetSurfaceBuilder, planetId, 0);
        return ResponseEntity.ok(state);
    }
}

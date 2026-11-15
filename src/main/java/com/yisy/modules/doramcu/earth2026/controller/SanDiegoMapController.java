package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.SanDiegoTransitRoute;
import com.yisy.modules.doramcu.earth2026.service.SanDiegoMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for San Diego 3D map data.
 * Provides buildings, terrain, and transit routes for the San Diego game map.
 */
@RestController
@RequestMapping("/api/v1/san-diego-map")
public class SanDiegoMapController {

    private final SanDiegoMapService mapService;

    public SanDiegoMapController(SanDiegoMapService mapService) {
        this.mapService = mapService;
    }

    /**
     * Get complete San Diego map data (buildings, terrain, routes).
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getMapData() {
        return ResponseEntity.ok(mapService.getCompleteMapData());
    }

    /**
     * Get 3D building data for the map.
     */
    @GetMapping("/buildings")
    public ResponseEntity<List<Map<String, Object>>> getBuildings() {
        return ResponseEntity.ok(mapService.get3DBuildings());
    }

    /**
     * Get terrain data for the map area.
     */
    @GetMapping("/terrain")
    public ResponseEntity<Map<String, Object>> getTerrain() {
        return ResponseEntity.ok(mapService.getTerrainData());
    }

    /**
     * Get all transit routes.
     */
    @GetMapping("/routes")
    public ResponseEntity<List<SanDiegoTransitRoute>> getRoutes() {
        return ResponseEntity.ok(mapService.getAllRoutes());
    }

    /**
     * Get routes from a specific location.
     */
    @GetMapping("/routes/from/{location}")
    public ResponseEntity<List<SanDiegoTransitRoute>> getRoutesFrom(@PathVariable String location) {
        return ResponseEntity.ok(mapService.getRoutesFrom(location));
    }

    /**
     * Get routes to a specific location.
     */
    @GetMapping("/routes/to/{location}")
    public ResponseEntity<List<SanDiegoTransitRoute>> getRoutesTo(@PathVariable String location) {
        return ResponseEntity.ok(mapService.getRoutesTo(location));
    }

    /**
     * Get the path from Younger Industries HQ to Convention Center.
     */
    @GetMapping("/path/hq-to-convention-center")
    public ResponseEntity<Map<String, Object>> getHqToConventionCenterPath() {
        return ResponseEntity.ok(mapService.getHqToConventionCenterPath());
    }
}

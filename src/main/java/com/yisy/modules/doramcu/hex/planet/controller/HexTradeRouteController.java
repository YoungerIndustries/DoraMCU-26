package com.yisy.modules.doramcu.hex.planet.controller;

import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.Station;
import com.yisy.modules.doramcu.hex.planet.DefaultPlanetSurfaceBuilder;
import com.yisy.modules.doramcu.hex.planet.trade.PlayerTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for planet trade route endpoints.
 * Covers: creating, listing, toggling, and deleting trade routes between stations.
 */
@RestController
@RequestMapping("/api/v1/planets")
public class HexTradeRouteController {

    private static final Logger log = LoggerFactory.getLogger(HexTradeRouteController.class);

    private final DefaultPlanetSurfaceBuilder planetSurfaceBuilder;
    private final PlayerTradeService playerTradeService;

    public HexTradeRouteController(
            DefaultPlanetSurfaceBuilder planetSurfaceBuilder,
            PlayerTradeService playerTradeService) {
        this.planetSurfaceBuilder = planetSurfaceBuilder;
        this.playerTradeService = playerTradeService;
    }

    // ========== Trade Route Endpoints ==========

    /**
     * Create a trade route between two stations.
     */
    @PostMapping("/{planetId}/trade/route")
    public ResponseEntity<Map<String, Object>> createTradeRoute(
            @PathVariable Long planetId,
            @RequestBody Map<String, String> request) {
        try {
            String playerId = request.getOrDefault("playerId", "PLAYER");
            String fromStationName = request.get("fromStation");
            String toStationName = request.get("toStation");
            String cargoTypeName = request.getOrDefault("cargoType", "MAIL");

            if (fromStationName == null || toStationName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "fromStation and toStation are required"));
            }

            // Look up stations by name from planet surface builder
            Planet planet = new Planet(planetId.intValue());
            List<Station> allStations = planetSurfaceBuilder.getStations(planet, "*");
            Station fromStation = allStations.stream()
                    .filter(s -> s.getName().equals(fromStationName)).findFirst().orElse(null);
            Station toStation = allStations.stream()
                    .filter(s -> s.getName().equals(toStationName)).findFirst().orElse(null);
            if (fromStation == null || toStation == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Station not found"));
            }

            CargoType cargoType = CargoType.valueOf(cargoTypeName);

            PlayerTradeService.TradeRoute route =
                    playerTradeService.createRoute(playerId, fromStation, toStation, cargoType);

            return ResponseEntity.ok(Map.of(
                    "routeId", route.getId(),
                    "playerId", route.getPlayerId(),
                    "fromStation", fromStation.getName(),
                    "toStation", toStation.getName(),
                    "cargoType", cargoType.getDisplayName(),
                    "active", route.isActive()
            ));
        } catch (Exception e) {
            log.error("Failed to create trade route: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all trade routes for a player.
     */
    @GetMapping("/{planetId}/trade/routes")
    public ResponseEntity<List<Map<String, Object>>> getPlayerTradeRoutes(
            @PathVariable Long planetId,
            @RequestParam(defaultValue = "PLAYER") String playerId) {
        List<Map<String, Object>> routes = new ArrayList<>();
        for (PlayerTradeService.TradeRoute route : playerTradeService.getPlayerRoutes(playerId)) {
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("routeId", route.getId());
            routeData.put("fromStation", route.getFromStation().getName());
            routeData.put("toStation", route.getToStation().getName());
            routeData.put("cargoType", route.getCargoType().getDisplayName());
            routeData.put("active", route.isActive());
            routeData.put("totalIncome", route.getTotalIncome());
            routeData.put("turnsOperated", route.getTurnsOperated());
            routes.add(routeData);
        }
        return ResponseEntity.ok(routes);
    }

    /**
     * Process trade routes for one turn.
     */
    @PostMapping("/{planetId}/trade/turn")
    public ResponseEntity<Map<String, Object>> processTradeTurn(@PathVariable Long planetId) {
        double totalIncome = playerTradeService.processAllTurns();
        return ResponseEntity.ok(Map.of(
                "totalIncome", totalIncome,
                "routesProcessed", playerTradeService.getPlayerRoutes("PLAYER").size()
        ));
    }

    /**
     * Toggle a trade route active/inactive.
     */
    @PostMapping("/{planetId}/trade/route/{routeId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleTradeRoute(
            @PathVariable Long planetId,
            @PathVariable String routeId) {
        playerTradeService.toggleRoute(routeId);
        PlayerTradeService.TradeRoute route = playerTradeService.getRoute(routeId);
        return ResponseEntity.ok(Map.of(
                "routeId", routeId,
                "active", route != null && route.isActive()
        ));
    }

    /**
     * Delete a trade route.
     */
    @DeleteMapping("/{planetId}/trade/route/{routeId}")
    public ResponseEntity<Map<String, Object>> deleteTradeRoute(
            @PathVariable Long planetId,
            @PathVariable String routeId) {
        playerTradeService.deleteRoute(routeId);
        return ResponseEntity.ok(Map.of("routeId", routeId, "deleted", true));
    }
}

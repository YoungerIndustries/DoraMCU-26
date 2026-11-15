package com.yisy.modules.doramcu.googlemaps;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for Google Maps integration.
 * <p>
 * Provides endpoints for geocoding, elevation data, and terrain grids.
 * All endpoints return empty results if Google Maps API is not configured.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/google-maps")
public class GoogleMapsController {

    private final GoogleMapsService googleMapsService;

    public GoogleMapsController(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    /**
     * Geocode an address to get coordinates.
     *
     * @param address the address to geocode
     * @return coordinates and formatted address
     */
    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String address) {
        return googleMapsService.geocodeAddress(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of(
                        "error", "Google Maps API not configured or address not found",
                        "address", address
                )));
    }

    /**
     * Get elevation data for a location.
     *
     * @param lat latitude
     * @param lng longitude
     * @return elevation data in meters
     */
    @GetMapping("/elevation")
    public ResponseEntity<?> elevation(@RequestParam double lat, @RequestParam double lng) {
        return googleMapsService.getElevation(lat, lng)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of(
                        "error", "Google Maps API not configured or elevation not available",
                        "lat", lat,
                        "lng", lng
                )));
    }

    /**
     * Get elevation data along a path.
     *
     * @param path JSON array of [lat, lng] coordinates
     * @return list of elevation data for each point
     */
    @PostMapping("/elevation/path")
    public ResponseEntity<?> elevationPath(@RequestBody double[][] path) {
        List<Map<String, Object>> elevations = googleMapsService.getElevationAlongPath(path);
        return ResponseEntity.ok(elevations);
    }

    /**
     * Reverse geocode coordinates to get an address.
     *
     * @param lat latitude
     * @param lng longitude
     * @return formatted address and components
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lng) {
        return googleMapsService.reverseGeocode(lat, lng)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of(
                        "error", "Google Maps API not configured or address not found",
                        "lat", lat,
                        "lng", lng
                )));
    }

    /**
     * Get terrain elevation grid for a rectangular area.
     *
     * @param northLat northern boundary
     * @param southLat southern boundary
     * @param westLng western boundary
     * @param eastLng eastern boundary
     * @param gridSize grid resolution (default 16)
     * @return terrain grid with elevation data
     */
    @GetMapping("/terrain")
    public ResponseEntity<?> terrain(
            @RequestParam double northLat,
            @RequestParam double southLat,
            @RequestParam double westLng,
            @RequestParam double eastLng,
            @RequestParam(defaultValue = "16") int gridSize) {
        Map<String, Object> terrain = googleMapsService.getTerrainGrid(
                northLat, southLat, westLng, eastLng, gridSize);
        return ResponseEntity.ok(terrain);
    }

    /**
     * Check if Google Maps API is available.
     *
     * @return status information
     */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        boolean available = googleMapsService.isApiAvailable();
        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "Google Maps API is configured and available" :
                        "Google Maps API is not configured. Set google.maps.api-key and google.maps.enabled=true"
        ));
    }
}

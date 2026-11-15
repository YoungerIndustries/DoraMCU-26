package com.yisy.modules.doramcu.googlemaps;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.ElevationApi;
import com.google.maps.PendingResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.LatLng;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Service for Google Maps API integration providing geocoding and elevation data.
 * <p>
 * This service wraps the Google Maps Java client library to provide:
 * - Geocoding: Convert addresses to latitude/longitude coordinates
 * - Elevation: Get terrain elevation data for any location on Earth
 * - Reverse Geocoding: Convert coordinates to human-readable addresses
 * </p>
 * <p>
 * All methods gracefully handle missing API keys by returning empty results
 * and logging warnings, allowing the application to function without Google Maps.
 * </p>
 */
@Service
public class GoogleMapsService {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsService.class);

    private final GoogleMapsConfig config;
    private final GeoApiContext context;

    public GoogleMapsService(GoogleMapsConfig config, @Nullable GeoApiContext context) {
        this.config = config;
        this.context = context;
    }

    /**
     * Geocode an address to get latitude and longitude coordinates.
     *
     * @param address the address to geocode (e.g., "4906 Campo Road, Spring Valley, CA 91977")
     * @return Optional containing the coordinates as a map with "lat" and "lng" keys
     */
    public Optional<Map<String, Object>> geocodeAddress(String address) {
        if (!config.isEnabled() || context == null) {
            log.warn("Google Maps API not configured. Cannot geocode address: {}", address);
            return Optional.empty();
        }

        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results != null && results.length > 0) {
                GeocodingResult result = results[0];
                Map<String, Object> coords = new LinkedHashMap<>();
                coords.put("lat", result.geometry.location.lat);
                coords.put("lng", result.geometry.location.lng);
                coords.put("formattedAddress", result.formattedAddress);
                coords.put("placeId", result.placeId);
                coords.put("types", result.types);
                log.info("Geocoded '{}' to lat={}, lng={}", address, 
                        result.geometry.location.lat, result.geometry.location.lng);
                return Optional.of(coords);
            }
            log.warn("No geocoding results for address: {}", address);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to geocode address: {}", address, e);
            return Optional.empty();
        }
    }

    /**
     * Get elevation data for a single location.
     *
     * @param lat latitude
     * @param lng longitude
     * @return Optional containing elevation data as a map with "elevation", "location", and "resolution" keys
     */
    public Optional<Map<String, Object>> getElevation(double lat, double lng) {
        if (!config.isEnabled() || context == null) {
            log.warn("Google Maps API not configured. Cannot get elevation for ({}, {})", lat, lng);
            return Optional.empty();
        }

        try {
            PendingResult<ElevationResult> pendingResult = ElevationApi.getByPoint(context, new LatLng(lat, lng));
            ElevationResult result = pendingResult.await();
            if (result != null) {
                Map<String, Object> elevation = new LinkedHashMap<>();
                elevation.put("elevation", result.elevation);
                elevation.put("locationLat", result.location.lat);
                elevation.put("locationLng", result.location.lng);
                elevation.put("resolution", result.resolution);
                log.debug("Elevation at ({}, {}): {}m (resolution: {}m)", 
                        lat, lng, result.elevation, result.resolution);
                return Optional.of(elevation);
            }
            log.warn("No elevation data for ({}, {})", lat, lng);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get elevation for ({}, {})", lat, lng, e);
            return Optional.empty();
        }
    }

    /**
     * Get elevation data for multiple locations along a path.
     *
     * @param path array of [lat, lng] coordinates
     * @return List of elevation data maps for each point in the path
     */
    public List<Map<String, Object>> getElevationAlongPath(double[][] path) {
        if (!config.isEnabled() || context == null) {
            log.warn("Google Maps API not configured. Cannot get elevation along path");
            return new ArrayList<>();
        }

        List<Map<String, Object>> elevations = new ArrayList<>();
        try {
            LatLng[] latLngs = new LatLng[path.length];
            for (int i = 0; i < path.length; i++) {
                latLngs[i] = new LatLng(path[i][0], path[i][1]);
            }

            // Elevation API supports up to 512 points per request
            // Split into chunks if necessary
            int batchSize = 512;
            for (int i = 0; i < latLngs.length; i += batchSize) {
                int end = Math.min(i + batchSize, latLngs.length);
                LatLng[] batch = new LatLng[end - i];
                System.arraycopy(latLngs, i, batch, 0, end - i);

                PendingResult<ElevationResult[]> pendingResult = ElevationApi.getByPoints(context, batch);
                ElevationResult[] results = pendingResult.await();
                if (results != null) {
                    for (ElevationResult result : results) {
                        Map<String, Object> elevation = new LinkedHashMap<>();
                        elevation.put("elevation", result.elevation);
                        elevation.put("locationLat", result.location.lat);
                        elevation.put("locationLng", result.location.lng);
                        elevation.put("resolution", result.resolution);
                        elevations.add(elevation);
                    }
                }
            }
            log.info("Got elevation data for {} points along path", elevations.size());
        } catch (Exception e) {
            log.error("Failed to get elevation along path", e);
        }
        return elevations;
    }

    /**
     * Reverse geocode coordinates to get a human-readable address.
     *
     * @param lat latitude
     * @param lng longitude
     * @return Optional containing address information
     */
    public Optional<Map<String, Object>> reverseGeocode(double lat, double lng) {
        if (!config.isEnabled() || context == null) {
            log.warn("Google Maps API not configured. Cannot reverse geocode ({}, {})", lat, lng);
            return Optional.empty();
        }

        try {
            GeocodingResult[] results = GeocodingApi.reverseGeocode(context, new LatLng(lat, lng)).await();
            if (results != null && results.length > 0) {
                GeocodingResult result = results[0];
                Map<String, Object> address = new LinkedHashMap<>();
                address.put("formattedAddress", result.formattedAddress);
                address.put("placeId", result.placeId);
                address.put("types", result.types);
                address.put("addressComponents", result.addressComponents);
                log.info("Reverse geocoded ({}, {}) to '{}'", lat, lng, result.formattedAddress);
                return Optional.of(address);
            }
            log.warn("No reverse geocoding results for ({}, {})", lat, lng);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to reverse geocode ({}, {})", lat, lng, e);
            return Optional.empty();
        }
    }

    /**
     * Get terrain data for a rectangular area by sampling elevation points.
     *
     * @param northLat northern boundary latitude
     * @param southLat southern boundary latitude
     * @param westLng western boundary longitude
     * @param eastLng eastern boundary longitude
     * @param gridSize number of grid points in each dimension (e.g., 16 for 16x16 grid)
     * @return Map containing terrain grid data
     */
    public Map<String, Object> getTerrainGrid(double northLat, double southLat, 
                                               double westLng, double eastLng, int gridSize) {
        Map<String, Object> terrain = new LinkedHashMap<>();
        terrain.put("northLat", northLat);
        terrain.put("southLat", southLat);
        terrain.put("westLng", westLng);
        terrain.put("eastLng", eastLng);
        terrain.put("gridSize", gridSize);

        if (!config.isEnabled() || context == null) {
            log.warn("Google Maps API not configured. Returning empty terrain grid");
            terrain.put("elevations", new double[gridSize][gridSize]);
            return terrain;
        }

        double[][] elevations = new double[gridSize][gridSize];
        double latStep = (northLat - southLat) / (gridSize - 1);
        double lngStep = (eastLng - westLng) / (gridSize - 1);

        // Collect all points for batch processing
        List<LatLng> points = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double lat = southLat + i * latStep;
                double lng = westLng + j * lngStep;
                points.add(new LatLng(lat, lng));
            }
        }

        try {
            // Process in batches of 512
            int batchSize = 512;
            int pointIndex = 0;
            for (int i = 0; i < points.size(); i += batchSize) {
                int end = Math.min(i + batchSize, points.size());
                LatLng[] batch = points.subList(i, end).toArray(new LatLng[0]);
                
                PendingResult<ElevationResult[]> pendingResult = ElevationApi.getByPoints(context, batch);
                ElevationResult[] results = pendingResult.await();
                if (results != null) {
                    for (ElevationResult result : results) {
                        if (pointIndex < gridSize * gridSize) {
                            int row = pointIndex / gridSize;
                            int col = pointIndex % gridSize;
                            elevations[row][col] = result.elevation;
                            pointIndex++;
                        }
                    }
                }
            }
            log.info("Generated {}x{} terrain grid with {} elevation points", 
                    gridSize, gridSize, pointIndex);
        } catch (Exception e) {
            log.error("Failed to generate terrain grid", e);
        }

        terrain.put("elevations", elevations);
        return terrain;
    }

    /**
     * Check if Google Maps API is configured and enabled.
     */
    public boolean isApiAvailable() {
        return config.isEnabled() && context != null;
    }
}

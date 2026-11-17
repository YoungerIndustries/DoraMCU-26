package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages FPS map definitions.
 * <p>
 * Provides default maps and allows game modules to add themed maps.
 * Maps include spawn points, collision data, and power-up locations.
 * </p>
 */
@Service
public class FpsMapService {

    private static final Logger log = LoggerFactory.getLogger(FpsMapService.class);

    private final Map<String, FpsMap> maps;

    public FpsMapService() {
        this.maps = new ConcurrentHashMap<>();
        initializeDefaultMaps();
    }

    /**
     * Initialize the default map set.
     */
    private void initializeDefaultMaps() {
        // Default arena map
        FpsMap arena = new FpsMap("Arena", 100, 100, 20);
        arena.setDescription("A simple square arena for testing");
        arena.getSpawnPoints().add(new FpsMap.SpawnPoint(-40, 0, -40, 0));
        arena.getSpawnPoints().add(new FpsMap.SpawnPoint(40, 0, 40, 1));
        arena.getSpawnPoints().add(new FpsMap.SpawnPoint(-40, 0, 40, 0));
        arena.getSpawnPoints().add(new FpsMap.SpawnPoint(40, 0, -40, 1));
        arena.getCollisionBoxes().add(new FpsMap.CollisionBox(-10, -5, 0, 5, -10, -5));
        arena.getPowerUpLocations().add(new FpsMap.PowerUpLocation(0, 0, 0, "health"));
        // Capture points for CTP mode: 3 symmetric points
        arena.getCapturePoints().add(new FpsMap.CapturePoint("A", 0, 0, 0, 10));
        arena.getCapturePoints().add(new FpsMap.CapturePoint("B", -30, 0, -30, 10));
        arena.getCapturePoints().add(new FpsMap.CapturePoint("C", 30, 0, 30, 10));
        addMap(arena);

        // Crossfire map
        FpsMap crossfire = new FpsMap("Crossfire", 150, 150, 30);
        crossfire.setDescription("Cross-shaped map with multiple lanes");
        crossfire.getSpawnPoints().add(new FpsMap.SpawnPoint(-60, 0, -60, 0));
        crossfire.getSpawnPoints().add(new FpsMap.SpawnPoint(60, 0, 60, 1));
        crossfire.getSpawnPoints().add(new FpsMap.SpawnPoint(-60, 0, 60, 0));
        crossfire.getSpawnPoints().add(new FpsMap.SpawnPoint(60, 0, -60, 1));
        crossfire.getSpawnPoints().add(new FpsMap.SpawnPoint(0, 0, -60, 0));
        crossfire.getSpawnPoints().add(new FpsMap.SpawnPoint(0, 0, 60, 1));
        crossfire.getCollisionBoxes().add(new FpsMap.CollisionBox(-20, 20, 0, 10, -5, 5));
        crossfire.getCollisionBoxes().add(new FpsMap.CollisionBox(-5, 5, 0, 10, -20, 20));
        crossfire.getPowerUpLocations().add(new FpsMap.PowerUpLocation(-30, 0, 0, "ammo"));
        crossfire.getPowerUpLocations().add(new FpsMap.PowerUpLocation(30, 0, 0, "health"));
        // Capture points for CTP mode: 5 points across the cross
        crossfire.getCapturePoints().add(new FpsMap.CapturePoint("A", 0, 0, 0, 12));
        crossfire.getCapturePoints().add(new FpsMap.CapturePoint("B", -40, 0, -40, 12));
        crossfire.getCapturePoints().add(new FpsMap.CapturePoint("C", 40, 0, 40, 12));
        crossfire.getCapturePoints().add(new FpsMap.CapturePoint("D", -40, 0, 40, 12));
        crossfire.getCapturePoints().add(new FpsMap.CapturePoint("E", 40, 0, -40, 12));
        addMap(crossfire);
    }

    /**
     * Add a map to the registry.
     */
    public void addMap(FpsMap map) {
        maps.put(map.getId(), map);
        log.debug("Registered map: {}", map.getName());
    }

    /**
     * Get a map by ID.
     */
    public Optional<FpsMap> getMap(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(maps.get(id));
    }

    /**
     * List all available maps.
     */
    public List<FpsMap> getAllMaps() {
        return new ArrayList<>(maps.values());
    }

    /**
     * Get a map by its ID.
     */
    public Optional<FpsMap> getMapById(String id) {
        return Optional.ofNullable(maps.get(id));
    }

    /**
     * Get maps by theme.
     */
    public List<FpsMap> getMapsByTheme(String theme) {
        return maps.values().stream()
                .filter(m -> theme.equals(m.getTheme()))
                .toList();
    }

    /**
     * Remove a map from the registry.
     */
    public boolean removeMap(String id) {
        FpsMap removed = maps.remove(id);
        if (removed != null) {
            log.info("Removed map: {}", removed.getName());
        }
        return removed != null;
    }
}

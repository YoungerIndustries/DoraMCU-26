package com.yisy.modules.doramcu.fps.core.model;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Defines an FPS map with spawn points, collision data, and power-up locations.
 */
@Component
public class FpsMap {

    public record SpawnPoint(double x, double y, double z, int team) {
    }

    public record CollisionBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
    }

    public record PowerUpLocation(double x, double y, double z, String type) {
    }

    /** A control point for Capture the Point game mode. */
    public record CapturePoint(String id, double x, double y, double z, double radius) {
    }

    /** A transition point connecting this map to another map. */
    public record TransitionPoint(String id, double x, double y, double z,
                                  String targetMapId, String targetTransitionId, String label) {
    }

    /** A scene object placed within the map (NPC, item, trigger, etc.). */
    public record SceneObject(String id, String type, double x, double y, double z,
                              double rotationY, double scaleX, double scaleY, double scaleZ,
                              String label, String dataJson) {
    }

    private String id;
    private String name;
    private String description;
    private List<SpawnPoint> spawnPoints;
    private List<CollisionBox> collisionBoxes;
    private List<PowerUpLocation> powerUpLocations;
    private List<CapturePoint> capturePoints;
    private List<TransitionPoint> transitionPoints;
    private List<SceneObject> sceneObjects;
    private double mapWidth;
    private double mapDepth;
    private double mapHeight;
    private String theme;

    public FpsMap() {
        this.spawnPoints = new ArrayList<>();
        this.collisionBoxes = new ArrayList<>();
        this.powerUpLocations = new ArrayList<>();
        this.capturePoints = new ArrayList<>();
        this.transitionPoints = new ArrayList<>();
        this.sceneObjects = new ArrayList<>();
    }

    public FpsMap(String name, double width, double depth, double height) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.mapWidth = width;
        this.mapDepth = depth;
        this.mapHeight = height;
        this.spawnPoints = new ArrayList<>();
        this.collisionBoxes = new ArrayList<>();
        this.powerUpLocations = new ArrayList<>();
        this.capturePoints = new ArrayList<>();
        this.transitionPoints = new ArrayList<>();
        this.sceneObjects = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<SpawnPoint> getSpawnPoints() { return spawnPoints; }
    public void setSpawnPoints(List<SpawnPoint> spawnPoints) { this.spawnPoints = spawnPoints; }
    public List<CollisionBox> getCollisionBoxes() { return collisionBoxes; }
    public void setCollisionBoxes(List<CollisionBox> collisionBoxes) { this.collisionBoxes = collisionBoxes; }
    public List<PowerUpLocation> getPowerUpLocations() { return powerUpLocations; }
    public void setPowerUpLocations(List<PowerUpLocation> powerUpLocations) { this.powerUpLocations = powerUpLocations; }
    public double getMapWidth() { return mapWidth; }
    public void setMapWidth(double mapWidth) { this.mapWidth = mapWidth; }
    public double getMapDepth() { return mapDepth; }
    public void setMapDepth(double mapDepth) { this.mapDepth = mapDepth; }
    public double getMapHeight() { return mapHeight; }
    public void setMapHeight(double mapHeight) { this.mapHeight = mapHeight; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public List<CapturePoint> getCapturePoints() { return capturePoints; }
    public void setCapturePoints(List<CapturePoint> capturePoints) { this.capturePoints = capturePoints; }
    public List<TransitionPoint> getTransitionPoints() { return transitionPoints; }
    public void setTransitionPoints(List<TransitionPoint> transitionPoints) { this.transitionPoints = transitionPoints; }
    public List<SceneObject> getSceneObjects() { return sceneObjects; }
    public void setSceneObjects(List<SceneObject> sceneObjects) { this.sceneObjects = sceneObjects; }
}

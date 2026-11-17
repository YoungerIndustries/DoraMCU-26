package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.service.GameObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bridge service that connects {@link FpsMap} scene object placements
 * with {@link GameObjectService} database-driven object definitions.
 * <p>
 * Enriches {@link FpsMap.SceneObject} placement records with visual properties
 * (color, scale, model path) from the {@code GameObjectEntity} database,
 * producing a combined list suitable for JSON serialization to the FPS client.
 * </p>
 */
@Service
public class FpsSceneObjectService {

    private static final Logger log = LoggerFactory.getLogger(FpsSceneObjectService.class);

    private final GameObjectService gameObjectService;
    private final FpsMapService fpsMapService;

    public FpsSceneObjectService(GameObjectService gameObjectService, FpsMapService fpsMapService) {
        this.gameObjectService = gameObjectService;
        this.fpsMapService = fpsMapService;
    }

    /**
     * Combined DTO that merges FpsMap.SceneObject placement data with
     * GameObjectService.SceneObjectDto visual properties for client serialization.
     */
    public static class EnrichedSceneObject {
        private String objectUuid;
        private String objectType;
        private String objectSubtype;
        private String name;
        private String gameModule;
        private String model3dPath;
        private String texturePath;
        private Double scaleFactor;
        private String colorHex;
        private Long skinId;
        // Placement data from FpsMap.SceneObject
        private double x;
        private double y;
        private double z;
        private double rotationY;
        private double scaleX;
        private double scaleY;
        private double scaleZ;
        private String label;

        public EnrichedSceneObject() {
        }

        public String getObjectUuid() { return objectUuid; }
        public void setObjectUuid(String objectUuid) { this.objectUuid = objectUuid; }
        public String getObjectType() { return objectType; }
        public void setObjectType(String objectType) { this.objectType = objectType; }
        public String getObjectSubtype() { return objectSubtype; }
        public void setObjectSubtype(String objectSubtype) { this.objectSubtype = objectSubtype; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGameModule() { return gameModule; }
        public void setGameModule(String gameModule) { this.gameModule = gameModule; }
        public String getModel3dPath() { return model3dPath; }
        public void setModel3dPath(String model3dPath) { this.model3dPath = model3dPath; }
        public String getTexturePath() { return texturePath; }
        public void setTexturePath(String texturePath) { this.texturePath = texturePath; }
        public Double getScaleFactor() { return scaleFactor; }
        public void setScaleFactor(Double scaleFactor) { this.scaleFactor = scaleFactor; }
        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }
        public Long getSkinId() { return skinId; }
        public void setSkinId(Long skinId) { this.skinId = skinId; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public double getZ() { return z; }
        public void setZ(double z) { this.z = z; }
        public double getRotationY() { return rotationY; }
        public void setRotationY(double rotationY) { this.rotationY = rotationY; }
        public double getScaleX() { return scaleX; }
        public void setScaleX(double scaleX) { this.scaleX = scaleX; }
        public double getScaleY() { return scaleY; }
        public void setScaleY(double scaleY) { this.scaleY = scaleY; }
        public double getScaleZ() { return scaleZ; }
        public void setScaleZ(double scaleZ) { this.scaleZ = scaleZ; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    /**
     * Get all enriched scene objects for a specific map.
     * Combines FpsMap.SceneObject placements with GameObjectEntity database properties.
     * Returns empty list if map has no scene objects or database is unavailable.
     */
    public List<EnrichedSceneObject> getSceneObjectsForMap(String mapId) {
        FpsMap map = fpsMapService.getMapById(mapId)
                .orElse(null);
        if (map == null) {
            log.warn("Map '{}' not found for scene object query", mapId);
            return Collections.emptyList();
        }

        List<FpsMap.SceneObject> placements = map.getSceneObjects();
        if (placements == null || placements.isEmpty()) {
            return Collections.emptyList();
        }

        return enrichPlacements(placements);
    }

    /**
     * Get scene objects for a map by name.
     */
    public List<EnrichedSceneObject> getSceneObjectsForMapByName(String mapName) {
        FpsMap map = fpsMapService.getMap(mapName)
                .orElse(null);
        if (map == null) {
            log.warn("Map '{}' not found for scene object query", mapName);
            return Collections.emptyList();
        }

        List<FpsMap.SceneObject> placements = map.getSceneObjects();
        if (placements == null || placements.isEmpty()) {
            return Collections.emptyList();
        }

        return enrichPlacements(placements);
    }

    /**
     * Get default procedural objects for a theme when no map-specific placements exist.
     * Returns all objects registered for the theme, without placement coordinates.
     * The client can use these to decide which procedural geometry to generate.
     */
    public List<GameObjectService.SceneObjectDto> getDefaultObjectsForTheme(String theme) {
        if (theme == null || theme.isEmpty()) {
            return Collections.emptyList();
        }
        return gameObjectService.getObjectsForTheme(theme);
    }

    /**
     * Get default objects for a theme filtered by object type.
     */
    public List<GameObjectService.SceneObjectDto> getDefaultObjectsForType(String theme, String type) {
        if (theme == null || type == null) {
            return Collections.emptyList();
        }
        return gameObjectService.getObjectsForType(theme, type);
    }

    /**
     * Get a summary of available objects for a map's theme.
     */
    public GameObjectService.SceneObjectSummary getThemeSummary(String mapId) {
        FpsMap map = fpsMapService.getMapById(mapId).orElse(null);
        if (map == null || map.getTheme() == null) {
            GameObjectService.SceneObjectSummary empty = new GameObjectService.SceneObjectSummary();
            empty.setGameModule("unknown");
            empty.setTotalCount(0);
            empty.setObjectTypes(Collections.emptyList());
            empty.setCountByType(Collections.emptyMap());
            return empty;
        }
        return gameObjectService.getSceneObjectSummary(map.getTheme());
    }

    /**
     * Enrich a list of SceneObject placements with database properties.
     * Skips placements whose objectUuid is not found in the database.
     */
    private List<EnrichedSceneObject> enrichPlacements(List<FpsMap.SceneObject> placements) {
        List<EnrichedSceneObject> enriched = new ArrayList<>();

        for (FpsMap.SceneObject placement : placements) {
            GameObjectService.SceneObjectDto dto = gameObjectService.getObjectByUuid(placement.id())
                    .orElse(null);

            if (dto == null) {
                log.warn("Scene object placement references unknown ID '{}', skipping", placement.id());
                continue;
            }

            EnrichedSceneObject enrichedObject = new EnrichedSceneObject();
            // Database properties
            enrichedObject.setObjectUuid(dto.getObjectUuid());
            enrichedObject.setObjectType(dto.getObjectType());
            enrichedObject.setObjectSubtype(dto.getObjectSubtype());
            enrichedObject.setName(dto.getName());
            enrichedObject.setGameModule(dto.getGameModule());
            enrichedObject.setModel3dPath(dto.getModel3dPath());
            enrichedObject.setTexturePath(dto.getTexturePath());
            enrichedObject.setScaleFactor(dto.getScaleFactor());
            enrichedObject.setColorHex(dto.getColorHex());
            enrichedObject.setSkinId(dto.getSkinId());
            // Placement data
            enrichedObject.setX(placement.x());
            enrichedObject.setY(placement.y());
            enrichedObject.setZ(placement.z());
            enrichedObject.setRotationY(placement.rotationY());
            enrichedObject.setScaleX(placement.scaleX());
            enrichedObject.setScaleY(placement.scaleY());
            enrichedObject.setScaleZ(placement.scaleZ());
            enrichedObject.setLabel(placement.label());

            enriched.add(enrichedObject);
        }

        log.debug("Enriched {} scene objects (skipped {} missing UUIDs)", enriched.size(), placements.size() - enriched.size());
        return enriched;
    }
}

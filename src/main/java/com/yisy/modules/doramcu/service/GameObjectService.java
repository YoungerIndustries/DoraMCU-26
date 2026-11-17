package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.registry.GameObjectEntity;
import com.yisy.modules.doramcu.repository.registry.GameObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for {@link GameObjectEntity} that wraps the repository
 * and provides DTOs suitable for JSON serialization to the FPS client.
 * <p>
 * Used by the FPS scene decoration system to query objects by theme
 * and type, enabling data-driven 3D prop placement.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class GameObjectService {

    private static final Logger log = LoggerFactory.getLogger(GameObjectService.class);

    private final GameObjectRepository repository;

    public GameObjectService(GameObjectRepository repository) {
        this.repository = repository;
    }

    // --- DTO Classes ---

    /**
     * Lightweight DTO for serializing game object data to the FPS client.
     * Contains only the fields needed for procedural mesh generation.
     */
    public static class SceneObjectDto {
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

        public SceneObjectDto() {
        }

        public SceneObjectDto(GameObjectEntity entity) {
            if (entity != null) {
                this.objectUuid = entity.getObjectUuid();
                this.objectType = entity.getObjectType();
                this.objectSubtype = entity.getObjectSubtype();
                this.name = entity.getName();
                this.gameModule = entity.getGameModule();
                this.model3dPath = entity.getModel3dPath();
                this.texturePath = entity.getTexturePath();
                this.scaleFactor = entity.getScaleFactor() != null ? entity.getScaleFactor() : 1.0;
                this.colorHex = entity.getColorHex();
                this.skinId = entity.getSkinId();
            }
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
    }

    /**
     * Summary DTO for a theme's scene objects, used for debugging and admin views.
     */
    public static class SceneObjectSummary {
        private String gameModule;
        private long totalCount;
        private List<String> objectTypes;
        private Map<String, Long> countByType;

        public SceneObjectSummary() {
        }

        public String getGameModule() { return gameModule; }
        public void setGameModule(String gameModule) { this.gameModule = gameModule; }
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public List<String> getObjectTypes() { return objectTypes; }
        public void setObjectTypes(List<String> objectTypes) { this.objectTypes = objectTypes; }
        public Map<String, Long> getCountByType() { return countByType; }
        public void setCountByType(Map<String, Long> countByType) { this.countByType = countByType; }
    }

    // --- Public Methods ---

    /**
     * Get all scene objects for a specific game module (theme).
     * Returns empty list if no objects found — allows graceful degradation to procedural decoration.
     */
    public List<SceneObjectDto> getObjectsForTheme(String theme) {
        try {
            return repository.findByGameModule(theme).stream()
                    .map(SceneObjectDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to query objects for theme '{}': {}", theme, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get scene objects for a specific theme and object type.
     * E.g., getObjectsForType("Earth2026", "tree") returns all tree objects for Earth2026.
     */
    public List<SceneObjectDto> getObjectsForType(String theme, String type) {
        try {
            return repository.findByGameModuleAndObjectType(theme, type).stream()
                    .map(SceneObjectDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to query objects for theme '{}', type '{}': {}", theme, type, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get a single object by its UUID.
     * Returns empty if not found — caller should skip the placement with a warning.
     */
    public Optional<SceneObjectDto> getObjectByUuid(String uuid) {
        try {
            return repository.findByObjectUuid(uuid).map(SceneObjectDto::new);
        } catch (Exception e) {
            log.warn("Failed to query object by UUID '{}': {}", uuid, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get a summary of all scene objects for a theme.
     * Useful for admin/debug views to see what objects are available.
     */
    public SceneObjectSummary getSceneObjectSummary(String theme) {
        SceneObjectSummary summary = new SceneObjectSummary();
        summary.setGameModule(theme);

        try {
            List<GameObjectEntity> objects = repository.findByGameModule(theme);
            summary.setTotalCount(objects.size());
            summary.setObjectTypes(repository.findDistinctObjectTypesByGameModule(theme));
            summary.setCountByType(objects.stream()
                    .collect(Collectors.groupingBy(GameObjectEntity::getObjectType, Collectors.counting())));
        } catch (Exception e) {
            log.warn("Failed to build summary for theme '{}': {}", theme, e.getMessage());
            summary.setTotalCount(0);
            summary.setObjectTypes(Collections.emptyList());
            summary.setCountByType(Collections.emptyMap());
        }

        return summary;
    }

    /**
     * Get all objects for multiple game modules at once.
     */
    public List<SceneObjectDto> getObjectsForThemes(List<String> themes) {
        try {
            return repository.findByGameModuleIn(themes).stream()
                    .map(SceneObjectDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to query objects for themes {}: {}", themes, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Count objects for a game module.
     */
    public long countByTheme(String theme) {
        try {
            return repository.countByGameModule(theme);
        } catch (Exception e) {
            log.warn("Failed to count objects for theme '{}': {}", theme, e.getMessage());
            return 0;
        }
    }
}

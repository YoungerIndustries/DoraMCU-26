package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the universal game object registry.
 * <p>
 * Every renderable object in the game — planets, ships, weapons, characters,
 * resources, industries, and more — is registered here with a unique UUID
 * and type hierarchy. This enables cross-system references and unified
 * rendering pipelines.
 * </p>
 */
@Entity
@Table(name = "game_object", indexes = {
        @Index(name = "idx_go_object_type", columnList = "object_type"),
        @Index(name = "idx_go_object_subtype", columnList = "object_subtype"),
        @Index(name = "idx_go_game_module", columnList = "game_module")
})
public class GameObjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "object_uuid", nullable = false, unique = true, length = 36)
    private String objectUuid;

    @Column(name = "object_type", nullable = false, length = 50)
    private String objectType;

    @Column(name = "object_subtype", length = 50)
    private String objectSubtype;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "game_module", length = 50)
    private String gameModule;

    // --- 3D rendering data ---

    @Column(name = "model_3d_path", length = 500)
    private String model3dPath;

    @Column(name = "texture_path", length = 500)
    private String texturePath;

    @Column(name = "skin_id")
    private Long skinId;

    @Column(name = "decal_ids", columnDefinition = "TEXT")
    private String decalIds;

    @Column(name = "scale_factor")
    private Double scaleFactor = 1.0;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    // --- Resource costs for production ---

    @Column(name = "resource_costs", columnDefinition = "TEXT")
    private String resourceCosts;

    @Column(name = "refined_resource_costs", columnDefinition = "TEXT")
    private String refinedResourceCosts;

    @Column(name = "strategic_resource_requirements", columnDefinition = "TEXT")
    private String strategicResourceRequirements;

    @Column(name = "cargo_costs", columnDefinition = "TEXT")
    private String cargoCosts;

    @Column(name = "construction_cost")
    private Integer constructionCost;

    @Column(name = "maintenance_cost")
    private Integer maintenanceCost;

    @Column(name = "build_time_turns")
    private Integer buildTimeTurns;

    // --- Production chain metadata ---

    @Column(name = "production_level", length = 20)
    private String productionLevel;

    @Column(name = "requires_industry", length = 50)
    private String requiresIndustry;

    @Column(name = "produced_by_industry", length = 50)
    private String producedByIndustry;

    // --- Cross-system references ---

    @Column(name = "equivalent_objects", columnDefinition = "TEXT")
    private String equivalentObjects;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GameObjectEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.objectUuid = UUID.randomUUID().toString();
    }

    // --- Accessors ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjectUuid() {
        return objectUuid;
    }

    public void setObjectUuid(String objectUuid) {
        this.objectUuid = objectUuid;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectSubtype() {
        return objectSubtype;
    }

    public void setObjectSubtype(String objectSubtype) {
        this.objectSubtype = objectSubtype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameModule() {
        return gameModule;
    }

    public void setGameModule(String gameModule) {
        this.gameModule = gameModule;
    }

    public String getModel3dPath() {
        return model3dPath;
    }

    public void setModel3dPath(String model3dPath) {
        this.model3dPath = model3dPath;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public Long getSkinId() {
        return skinId;
    }

    public void setSkinId(Long skinId) {
        this.skinId = skinId;
    }

    public String getDecalIds() {
        return decalIds;
    }

    public void setDecalIds(String decalIds) {
        this.decalIds = decalIds;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(Double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getResourceCosts() {
        return resourceCosts;
    }

    public void setResourceCosts(String resourceCosts) {
        this.resourceCosts = resourceCosts;
    }

    public String getRefinedResourceCosts() {
        return refinedResourceCosts;
    }

    public void setRefinedResourceCosts(String refinedResourceCosts) {
        this.refinedResourceCosts = refinedResourceCosts;
    }

    public String getStrategicResourceRequirements() {
        return strategicResourceRequirements;
    }

    public void setStrategicResourceRequirements(String strategicResourceRequirements) {
        this.strategicResourceRequirements = strategicResourceRequirements;
    }

    public String getCargoCosts() {
        return cargoCosts;
    }

    public void setCargoCosts(String cargoCosts) {
        this.cargoCosts = cargoCosts;
    }

    public Integer getConstructionCost() {
        return constructionCost;
    }

    public void setConstructionCost(Integer constructionCost) {
        this.constructionCost = constructionCost;
    }

    public Integer getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setMaintenanceCost(Integer maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public Integer getBuildTimeTurns() {
        return buildTimeTurns;
    }

    public void setBuildTimeTurns(Integer buildTimeTurns) {
        this.buildTimeTurns = buildTimeTurns;
    }

    public String getProductionLevel() {
        return productionLevel;
    }

    public void setProductionLevel(String productionLevel) {
        this.productionLevel = productionLevel;
    }

    public String getRequiresIndustry() {
        return requiresIndustry;
    }

    public void setRequiresIndustry(String requiresIndustry) {
        this.requiresIndustry = requiresIndustry;
    }

    public String getProducedByIndustry() {
        return producedByIndustry;
    }

    public void setProducedByIndustry(String producedByIndustry) {
        this.producedByIndustry = producedByIndustry;
    }

    public String getEquivalentObjects() {
        return equivalentObjects;
    }

    public void setEquivalentObjects(String equivalentObjects) {
        this.equivalentObjects = equivalentObjects;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

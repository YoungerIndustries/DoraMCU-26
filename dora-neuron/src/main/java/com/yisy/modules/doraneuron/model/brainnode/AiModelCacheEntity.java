package com.yisy.modules.doraneuron.model.brainnode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity for caching AI model metadata on the Brain Node.
 */
@Entity
@Table(name = "brainnode_ai_model_cache")
public class AiModelCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id", nullable = false, unique = true)
    private String modelId;

    @Column(name = "model_path")
    private String modelPath;

    @Column(name = "version")
    private int version;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    public AiModelCacheEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }
}

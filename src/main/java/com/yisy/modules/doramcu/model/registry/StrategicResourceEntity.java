package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for strategic resources that gate advanced capabilities.
 * <p>
 * Covers ELERIUM, ANTI_MATTER, THULIUM, DURANTIUM, and PROMETHION.
 * </p>
 */
@Entity
@Table(name = "strategic_resource", indexes = {
        @Index(name = "idx_sr_resource_type", columnList = "resource_type")
})
public class StrategicResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "resource_type", nullable = false, unique = true, length = 50)
    private String resourceType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "gates_capabilities", columnDefinition = "TEXT")
    private String gatesCapabilities;

    public StrategicResourceEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGatesCapabilities() {
        return gatesCapabilities;
    }

    public void setGatesCapabilities(String gatesCapabilities) {
        this.gatesCapabilities = gatesCapabilities;
    }
}

package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for celestial body types in the galaxy.
 * <p>
 * Covers STAR, PLANET, MOON, ASTEROID, COMET, GAS_GIANT, NEBULA, BLACK_HOLE, WORMHOLE, and HYPERLANE.
 * </p>
 */
@Entity
@Table(name = "celestial_body_type", indexes = {
        @Index(name = "idx_cbt_body_type", columnList = "body_type")
})
public class CelestialBodyTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "body_type", nullable = false, unique = true, length = 50)
    private String bodyType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "can_have_resources", nullable = false)
    private boolean canHaveResources;

    @Column(name = "can_have_industries", nullable = false)
    private boolean canHaveIndustries;

    @Column(name = "can_have_stations", nullable = false)
    private boolean canHaveStations;

    public CelestialBodyTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isCanHaveResources() {
        return canHaveResources;
    }

    public void setCanHaveResources(boolean canHaveResources) {
        this.canHaveResources = canHaveResources;
    }

    public boolean isCanHaveIndustries() {
        return canHaveIndustries;
    }

    public void setCanHaveIndustries(boolean canHaveIndustries) {
        this.canHaveIndustries = canHaveIndustries;
    }

    public boolean isCanHaveStations() {
        return canHaveStations;
    }

    public void setCanHaveStations(boolean canHaveStations) {
        this.canHaveStations = canHaveStations;
    }
}

package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity that tracks the lifecycle state of every galaxy object.
 * <p>
 * The Galaxy Overlord uses this table to manage the state of all objects in the
 * galaxy. Only objects that are needed for player interactions are fully built
 * in memory. The state table records whether each object is mapped (known to
 * exist), built (fully constructed), damaged, or destroyed (from conflict).
 * </p>
 * <p>
 * This enables:
 * <ul>
 *   <li>On-demand building: The Overlord builds objects only when players approach</li>
 *   <li>State persistence: Object state survives server restarts</li>
 *   <li>Destruction tracking: Cities and transport routes destroyed from conflict
 *       are recorded and can be rebuilt</li>
 *   <li>Serialization: All object metadata is stored as JSON for flexibility</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "galaxy_object_states", indexes = {
    @Index(name = "idx_galaxy_obj_galaxy", columnList = "galaxy_name"),
    @Index(name = "idx_galaxy_obj_type", columnList = "object_type"),
    @Index(name = "idx_galaxy_obj_state", columnList = "object_state"),
    @Index(name = "idx_galaxy_obj_position", columnList = "sector_row,sector_col"),
    @Index(name = "idx_galaxy_obj_destroyed", columnList = "object_state,destroyed_at")
})
public class GalaxyObjectState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The galaxy this object belongs to. */
    @Column(name = "galaxy_name", nullable = false)
    private String galaxyName;

    /** Type of the object (PLANET, CITY_3D, SPACE_STATION, TRANSPORT_ROUTE, etc.). */
    @Column(name = "object_type", nullable = false, length = 50)
    private String objectType;

    /** Unique identifier for this object within its type and galaxy. */
    @Column(name = "object_id", nullable = false, length = 255)
    private String objectId;

    /** Human-readable name of the object. */
    @Column(name = "object_name", length = 255)
    private String objectName;

    /** Current lifecycle state of the object. */
    @Enumerated(EnumType.STRING)
    @Column(name = "object_state", nullable = false, length = 20)
    private ObjectState state = ObjectState.MAPPED;

    /** Sector row in the 10x10 grid (0-9) — used for proximity queries. */
    @Column(name = "sector_row")
    private Integer sectorRow;

    /** Sector column in the 10x10 grid (0-9) — used for proximity queries. */
    @Column(name = "sector_col")
    private Integer sectorCol;

    /** Subsector row (0-9) — finer-grained position. */
    @Column(name = "sub_sector_row")
    private Integer subSectorRow;

    /** Subsector column (0-9) — finer-grained position. */
    @Column(name = "sub_sector_col")
    private Integer subSectorCol;

    /** JSON-serialized metadata for the object (properties, stats, etc.). */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadataJson;

    /** The planet this object resides on (for surface objects like cities). */
    @Column(name = "planet_name", length = 255)
    private String planetName;

    /** The solar system this object resides in. */
    @Column(name = "solar_system_id")
    private Long solarSystemId;

    /** Who/what destroyed this object (empire name, event type, etc.). */
    @Column(name = "destroyed_by", length = 255)
    private String destroyedBy;

    /** When the object was destroyed (null if not destroyed). */
    @Column(name = "destroyed_at")
    private LocalDateTime destroyedAt;

    /** When the object was last rebuilt (null if never rebuilt). */
    @Column(name = "rebuilt_at")
    private LocalDateTime rebuiltAt;

    /** Number of times this object has been rebuilt. */
    @Column(name = "rebuild_count", nullable = false)
    private int rebuildCount = 0;

    /** Whether this object requires full 3D detail for player interaction. */
    @Column(name = "requires_3d", nullable = false)
    private boolean requires3D = false;

    /** Whether the 3D detail has been built. */
    @Column(name = "has_3d_detail", nullable = false)
    private boolean has3DDetail = false;

    /** When this record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lifecycle states for galaxy objects.
     */
    public enum ObjectState {
        /** Object is known to exist (mapped during galaxy creation) but not yet built. */
        MAPPED,

        /** Object is fully built and operational. */
        BUILT,

        /** Object is currently being built (in-progress). */
        BUILDING,

        /** Object is damaged but still functional. */
        DAMAGED,

        /** Object has been destroyed (from conflict, natural disaster, etc.). */
        DESTROYED,

        /** Object is scheduled for rebuilding. */
        SCHEDULED_FOR_REBUILD
    }

    public GalaxyObjectState() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public ObjectState getState() {
        return state;
    }

    public void setState(ObjectState state) {
        this.state = state;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getSectorRow() {
        return sectorRow;
    }

    public void setSectorRow(Integer sectorRow) {
        this.sectorRow = sectorRow;
    }

    public Integer getSectorCol() {
        return sectorCol;
    }

    public void setSectorCol(Integer sectorCol) {
        this.sectorCol = sectorCol;
    }

    public Integer getSubSectorRow() {
        return subSectorRow;
    }

    public void setSubSectorRow(Integer subSectorRow) {
        this.subSectorRow = subSectorRow;
    }

    public Integer getSubSectorCol() {
        return subSectorCol;
    }

    public void setSubSectorCol(Integer subSectorCol) {
        this.subSectorCol = subSectorCol;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public Long getSolarSystemId() {
        return solarSystemId;
    }

    public void setSolarSystemId(Long solarSystemId) {
        this.solarSystemId = solarSystemId;
    }

    public String getDestroyedBy() {
        return destroyedBy;
    }

    public void setDestroyedBy(String destroyedBy) {
        this.destroyedBy = destroyedBy;
    }

    public LocalDateTime getDestroyedAt() {
        return destroyedAt;
    }

    public void setDestroyedAt(LocalDateTime destroyedAt) {
        this.destroyedAt = destroyedAt;
    }

    public LocalDateTime getRebuiltAt() {
        return rebuiltAt;
    }

    public void setRebuiltAt(LocalDateTime rebuiltAt) {
        this.rebuiltAt = rebuiltAt;
    }

    public int getRebuildCount() {
        return rebuildCount;
    }

    public void setRebuildCount(int rebuildCount) {
        this.rebuildCount = rebuildCount;
    }

    public boolean isRequires3D() {
        return requires3D;
    }

    public void setRequires3D(boolean requires3D) {
        this.requires3D = requires3D;
    }

    public boolean isHas3DDetail() {
        return has3DDetail;
    }

    public void setHas3DDetail(boolean has3DDetail) {
        this.has3DDetail = has3DDetail;
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

    /**
     * Records that this object was destroyed.
     *
     * @param destroyedBy who or what destroyed the object
     */
    public void markDestroyed(String destroyedBy) {
        this.state = ObjectState.DESTROYED;
        this.destroyedBy = destroyedBy;
        this.destroyedAt = LocalDateTime.now();
        this.has3DDetail = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records that this object was rebuilt.
     */
    public void markRebuilt() {
        this.state = ObjectState.BUILT;
        this.rebuiltAt = LocalDateTime.now();
        this.rebuildCount++;
        this.destroyedAt = null;
        this.destroyedBy = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records that this object is being built.
     */
    public void markBuilding() {
        this.state = ObjectState.BUILDING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records that this object is fully built.
     */
    public void markBuilt() {
        this.state = ObjectState.BUILT;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records that this object is damaged.
     */
    public void markDamaged() {
        this.state = ObjectState.DAMAGED;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "GalaxyObjectState{" +
                "id=" + id +
                ", galaxy='" + galaxyName + '\'' +
                ", type='" + objectType + '\'' +
                ", id='" + objectId + '\'' +
                ", state=" + state +
                ", planet='" + planetName + '\'' +
                '}';
    }
}

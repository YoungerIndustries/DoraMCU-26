package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for terrain types on planet surfaces.
 */
@Entity
@Table(name = "terrain_type", indexes = {
        @Index(name = "idx_tt_terrain_code", columnList = "terrain_code")
})
public class TerrainTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "terrain_code", nullable = false, unique = true, length = 50)
    private String terrainCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "supports_industry", nullable = false)
    private boolean supportsIndustry;

    public TerrainTypeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTerrainCode() {
        return terrainCode;
    }

    public void setTerrainCode(String terrainCode) {
        this.terrainCode = terrainCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public boolean isSupportsIndustry() {
        return supportsIndustry;
    }

    public void setSupportsIndustry(boolean supportsIndustry) {
        this.supportsIndustry = supportsIndustry;
    }
}

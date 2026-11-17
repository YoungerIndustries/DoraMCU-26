package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for visual skins in the object registry.
 * <p>
 * Separate from SkinEntity which handles runtime ship skins; this table stores
 * skin definitions in the universal object registry.
 * </p>
 */
@Entity
@Table(name = "skin", indexes = {
        @Index(name = "idx_sk_skin_code", columnList = "skin_code")
})
public class SkinRegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "skin_code", nullable = false, unique = true, length = 50)
    private String skinCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "texture_path", length = 500)
    private String texturePath;

    @Column(name = "color_palette", columnDefinition = "JSON")
    private String colorPalette;

    @Column(name = "applicable_object_types", columnDefinition = "JSON")
    private String applicableObjectTypes;

    public SkinRegistryEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkinCode() {
        return skinCode;
    }

    public void setSkinCode(String skinCode) {
        this.skinCode = skinCode;
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

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public String getColorPalette() {
        return colorPalette;
    }

    public void setColorPalette(String colorPalette) {
        this.colorPalette = colorPalette;
    }

    public String getApplicableObjectTypes() {
        return applicableObjectTypes;
    }

    public void setApplicableObjectTypes(String applicableObjectTypes) {
        this.applicableObjectTypes = applicableObjectTypes;
    }
}

package com.yisy.modules.doramcu.model.registry;

import jakarta.persistence.*;

/**
 * JPA entity for decals in the object registry.
 */
@Entity
@Table(name = "decal", indexes = {
        @Index(name = "idx_dc_decal_code", columnList = "decal_code")
})
public class DecalRegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "decal_code", nullable = false, unique = true, length = 50)
    private String decalCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "texture_path", length = 500)
    private String texturePath;

    @Column(name = "decal_type", length = 20)
    private String decalType;

    public DecalRegistryEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecalCode() {
        return decalCode;
    }

    public void setDecalCode(String decalCode) {
        this.decalCode = decalCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public String getDecalType() {
        return decalType;
    }

    public void setDecalType(String decalType) {
        this.decalType = decalType;
    }
}

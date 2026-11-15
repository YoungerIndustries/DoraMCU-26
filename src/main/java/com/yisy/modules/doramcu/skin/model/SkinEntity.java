package com.yisy.modules.doramcu.skin.model;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.DamageOverlay;

import com.yisy.modules.doramcu.hex.DamageTier;
import com.yisy.modules.doramcu.hex.SkinType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA entity representing a visual skin for an in-game asset.
 * <p>
 * A skin defines the appearance of a ship, space station, ground vehicle, or building,
 * including color scheme, texture references, faction association, default decals,
 * and damage overlay descriptions.
 * </p>
 */
@Entity
@Table(name = "skins", indexes = {
        @Index(name = "idx_skin_id", columnList = "skinId", unique = true),
        @Index(name = "idx_skin_type", columnList = "type"),
        @Index(name = "idx_skin_faction", columnList = "factionId")
})
public class SkinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "skin_id", nullable = false, length = 100, unique = true)
    private String skinId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkinType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "primary_color", nullable = false, length = 20)
    private String primaryColor = "#808080";

    @Column(name = "secondary_color", nullable = false, length = 20)
    private String secondaryColor = "#606060";

    @Column(name = "accent_color", nullable = false, length = 20)
    private String accentColor = "#4040ff";

    @Column(name = "texture_reference", length = 500)
    private String textureReference;

    @Column(name = "faction_id", length = 100)
    private String factionId;

    @OneToMany(mappedBy = "skin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DecalEntity> defaultDecals = new ArrayList<>();

    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    @CollectionTable(name = "skin_damage_overlays", joinColumns = @JoinColumn(name = "skin_id"))
    private Map<DamageTier, String> damageOverlay = new HashMap<>();

    public SkinEntity() {
    }

    public SkinEntity(String skinId, SkinType type) {
        this.skinId = skinId;
        this.type = type;
        this.name = skinId;
        this.description = "";
        this.textureReference = "";
    }

    // ---- Accessors ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkinId() {
        return skinId;
    }

    public void setSkinId(String skinId) {
        this.skinId = skinId;
    }

    public SkinType getType() {
        return type;
    }

    public void setType(SkinType type) {
        this.type = type;
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

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getTextureReference() {
        return textureReference;
    }

    public void setTextureReference(String textureReference) {
        this.textureReference = textureReference;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public List<DecalEntity> getDefaultDecals() {
        return defaultDecals;
    }

    public void setDefaultDecals(List<DecalEntity> defaultDecals) {
        this.defaultDecals = defaultDecals;
    }

    public Map<DamageTier, String> getDamageOverlay() {
        return damageOverlay;
    }

    public void setDamageOverlay(Map<DamageTier, String> damageOverlay) {
        this.damageOverlay = damageOverlay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinEntity that = (SkinEntity) o;
        return skinId != null && skinId.equals(that.skinId);
    }

    @Override
    public int hashCode() {
        return skinId != null ? skinId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SkinEntity{" +
                "id=" + id +
                ", skinId='" + skinId + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}

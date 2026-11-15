package com.yisy.modules.doramcu.skin.model;

import com.yisy.modules.doramcu.hex.Skin;

import jakarta.persistence.*;

/**
 * JPA entity representing a color variant of a base skin.
 * <p>
 * A variant allows a base skin to have multiple color schemes without duplicating
 * the full skin definition. Only the overridden colors are stored; the rest are
 * inherited from the base skin at resolution time.
 * </p>
 */
@Entity
@Table(name = "skin_variants", indexes = {
        @Index(name = "idx_variant_id", columnList = "variantId", unique = true),
        @Index(name = "idx_variant_base_skin", columnList = "baseSkinId")
})
public class SkinVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "variant_id", nullable = false, length = 100, unique = true)
    private String variantId;

    @Column(name = "base_skin_id", nullable = false, length = 100)
    private String baseSkinId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_skin_id", referencedColumnName = "skin_id", insertable = false, updatable = false)
    private SkinEntity baseSkin;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @Column(name = "accent_color", length = 20)
    private String accentColor;

    @Column(name = "texture_reference", length = 500)
    private String textureReference;

    public SkinVariantEntity() {
    }

    public SkinVariantEntity(String variantId, String baseSkinId) {
        this.variantId = variantId;
        this.baseSkinId = baseSkinId;
        this.name = variantId;
        this.description = "";
    }

    // ---- Accessors ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getBaseSkinId() {
        return baseSkinId;
    }

    public void setBaseSkinId(String baseSkinId) {
        this.baseSkinId = baseSkinId;
    }

    public SkinEntity getBaseSkin() {
        return baseSkin;
    }

    public void setBaseSkin(SkinEntity baseSkin) {
        this.baseSkin = baseSkin;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinVariantEntity that = (SkinVariantEntity) o;
        return variantId != null && variantId.equals(that.variantId);
    }

    @Override
    public int hashCode() {
        return variantId != null ? variantId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SkinVariantEntity{" +
                "variantId='" + variantId + '\'' +
                ", baseSkinId='" + baseSkinId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

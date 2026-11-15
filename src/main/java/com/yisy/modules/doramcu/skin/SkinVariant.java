package com.yisy.modules.doramcu.skin;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.DamageOverlay;

import java.util.Objects;

/**
 * Represents a color variant of a base skin.
 * <p>
 * A variant allows a base skin to have multiple color schemes without duplicating
 * the full skin definition. For example, a military vehicle skin can have desert camo,
 * arctic camo, forest camo, and urban grey variants, all sharing the same decals,
 * damage overlays, and texture references but with different colors.
 * </p>
 * <p>
 * When resolved, a variant merges its color overrides with the base skin to produce
 * a complete {@link Skin} instance.
 * </p>
 */
public class SkinVariant {

    private final String id;
    private final String baseSkinId;
    private String name;
    private String description;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String textureReference;

    /**
     * Creates a new variant for the given base skin.
     *
     * @param id the unique identifier for this variant
     * @param baseSkinId the ID of the base skin this variant extends
     */
    public SkinVariant(String id, String baseSkinId) {
        this.id = Objects.requireNonNull(id, "Variant ID must not be null");
        this.baseSkinId = Objects.requireNonNull(baseSkinId, "Base skin ID must not be null");
        this.name = id;
        this.description = "";
        this.primaryColor = null;
        this.secondaryColor = null;
        this.accentColor = null;
        this.textureReference = null;
    }

    // ---- Accessors ----

    /** @return the unique identifier of this variant */
    public String getId() {
        return id;
    }

    /** @return the ID of the base skin this variant extends */
    public String getBaseSkinId() {
        return baseSkinId;
    }

    /** @return the display name of this variant */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this variant.
     *
     * @param name the display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the description of this variant */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this variant.
     *
     * @param description the description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the primary color override, or null if the base skin's color should be used.
     *
     * @return the primary color hex string or null
     */
    public String getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Sets the primary color override.
     * If null, the base skin's primary color will be used.
     *
     * @param primaryColor the color as a hex string, or null to inherit from base
     */
    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * Returns the secondary color override, or null if the base skin's color should be used.
     *
     * @return the secondary color hex string or null
     */
    public String getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * Sets the secondary color override.
     * If null, the base skin's secondary color will be used.
     *
     * @param secondaryColor the color as a hex string, or null to inherit from base
     */
    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    /**
     * Returns the accent color override, or null if the base skin's color should be used.
     *
     * @return the accent color hex string or null
     */
    public String getAccentColor() {
        return accentColor;
    }

    /**
     * Sets the accent color override.
     * If null, the base skin's accent color will be used.
     *
     * @param accentColor the color as a hex string, or null to inherit from base
     */
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    /**
     * Returns the texture reference override, or null if the base skin's texture should be used.
     *
     * @return the texture reference or null
     */
    public String getTextureReference() {
        return textureReference;
    }

    /**
     * Sets the texture reference override.
     * If null, the base skin's texture reference will be used.
     *
     * @param textureReference the texture path or identifier, or null to inherit from base
     */
    public void setTextureReference(String textureReference) {
        this.textureReference = textureReference;
    }

    /**
     * Resolves this variant against the base skin to produce a complete Skin.
     * The resolved skin inherits all properties from the base skin, with overrides
     * applied for any non-null values in this variant.
     *
     * @param baseSkin the base skin to resolve against
     * @return a new Skin with merged base + variant properties
     */
    public Skin resolve(Skin baseSkin) {
        Objects.requireNonNull(baseSkin, "Base skin must not be null");
        Skin resolved = new Skin(id, baseSkin.getType());
        resolved.setName(name != null && !name.isEmpty() ? name : baseSkin.getName() + " (" + id + ")");
        resolved.setDescription(description != null ? description : baseSkin.getDescription());
        resolved.setPrimaryColor(primaryColor != null ? primaryColor : baseSkin.getPrimaryColor());
        resolved.setSecondaryColor(secondaryColor != null ? secondaryColor : baseSkin.getSecondaryColor());
        resolved.setAccentColor(accentColor != null ? accentColor : baseSkin.getAccentColor());
        resolved.setTextureReference(textureReference != null ? textureReference : baseSkin.getTextureReference());
        resolved.setFactionId(baseSkin.getFactionId());
        // Copy damage overlay from base
        if (baseSkin.getDamageOverlay() != null) {
            resolved.setDamageOverlay(new DamageOverlay(baseSkin.getDamageOverlay().getAllDescriptions()));
        }
        return resolved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkinVariant that = (SkinVariant) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SkinVariant{" +
                "id='" + id + '\'' +
                ", baseSkinId='" + baseSkinId + '\'' +
                ", name='" + name + '\'' +
                ", primaryColor='" + primaryColor + '\'' +
                '}';
    }
}

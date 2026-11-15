package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a visual skin for an in-game asset.
 * <p>
 * A skin defines the appearance of a ship, space station, ground vehicle, or building,
 * including color scheme, texture references, faction association, default decals,
 * and damage overlay descriptions.
 * </p>
 * <p>
 * Skins are shared across all game modules via the central {@code doramcu.skin} package.
 * They are serializable to JSON for persistence and API transport.
 * </p>
 */
public class Skin {

    private final String id;
    private final SkinType type;
    private String name;
    private String description;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String textureReference;
    private String factionId;
    private final List<Decal> defaultDecals;
    private DamageOverlay damageOverlay;

    /**
     * Creates a new skin with the given ID and type.
     *
     * @param id the unique identifier for this skin
     * @param type the type of asset this skin applies to
     */
    public Skin(String id, SkinType type) {
        this.id = Objects.requireNonNull(id, "Skin ID must not be null");
        this.type = Objects.requireNonNull(type, "Skin type must not be null");
        this.name = id;
        this.description = "";
        this.primaryColor = "#808080";
        this.secondaryColor = "#606060";
        this.accentColor = "#4040ff";
        this.textureReference = "";
        this.factionId = null;
        this.defaultDecals = new ArrayList<>();
        this.damageOverlay = new DamageOverlay();
    }

    // ---- Accessors ----

    /** @return the unique identifier of this skin */
    public String getId() {
        return id;
    }

    /** @return the asset type this skin applies to */
    public SkinType getType() {
        return type;
    }

    /** @return the display name of this skin */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this skin.
     *
     * @param name the display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the description of this skin */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this skin.
     *
     * @param description the description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the primary color as a hex string (e.g., "#FFFFFF").
     * This is the dominant color of the asset's surface.
     *
     * @return the primary color hex string
     */
    public String getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Sets the primary color.
     *
     * @param primaryColor the color as a hex string
     */
    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * Returns the secondary color as a hex string.
     * This is used for secondary panels, trim, and accents.
     *
     * @return the secondary color hex string
     */
    public String getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * Sets the secondary color.
     *
     * @param secondaryColor the color as a hex string
     */
    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    /**
     * Returns the accent color as a hex string.
     * This is used for highlights, lights, and decorative elements.
     *
     * @return the accent color hex string
     */
    public String getAccentColor() {
        return accentColor;
    }

    /**
     * Sets the accent color.
     *
     * @param accentColor the color as a hex string
     */
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    /**
     * Returns the texture reference string.
     * This is a path or identifier for the texture asset file.
     *
     * @return the texture reference
     */
    public String getTextureReference() {
        return textureReference;
    }

    /**
     * Sets the texture reference.
     *
     * @param textureReference the texture path or identifier
     */
    public void setTextureReference(String textureReference) {
        this.textureReference = textureReference;
    }

    /**
     * Returns the faction ID associated with this skin, or null if not faction-specific.
     *
     * @return the faction ID or null
     */
    public String getFactionId() {
        return factionId;
    }

    /**
     * Sets the faction ID for this skin.
     *
     * @param factionId the faction identifier
     */
    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    /**
     * Returns the default decals for this skin.
     * These decals are automatically applied when the skin is assigned to an asset.
     *
     * @return unmodifiable list of default decals
     */
    public List<Decal> getDefaultDecals() {
        return Collections.unmodifiableList(defaultDecals);
    }

    /**
     * Adds a default decal to this skin.
     *
     * @param decal the decal to add
     */
    public void addDefaultDecal(Decal decal) {
        if (decal == null) {
            throw new IllegalArgumentException("Decal must not be null");
        }
        defaultDecals.add(decal);
    }

    /**
     * Returns the damage overlay configuration for this skin.
     *
     * @return the damage overlay
     */
    public DamageOverlay getDamageOverlay() {
        return damageOverlay;
    }

    /**
     * Sets the damage overlay configuration.
     *
     * @param damageOverlay the damage overlay mapping tiers to descriptions
     */
    public void setDamageOverlay(DamageOverlay damageOverlay) {
        this.damageOverlay = Objects.requireNonNull(damageOverlay, "Damage overlay must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skin skin = (Skin) o;
        return id.equals(skin.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Skin{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", primaryColor='" + primaryColor + '\'' +
                '}';
    }
}

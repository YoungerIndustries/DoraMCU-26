package com.yisy.modules.doramcu.skin;

import com.yisy.modules.doramcu.hex.Skin;

import java.util.Objects;

/**
 * Represents a faction's default skin theme.
 * <p>
 * A faction skin theme defines the default appearance for all units belonging to
 * a faction, including the default skin reference, color palette, and emblem symbol.
 * Units inherit the faction's skin unless explicitly overridden.
 * </p>
 * <p>
 * This class is used by {@link Skin} to provide faction-specific defaults and by
 * {@link com.yisy.modules.doramcu.skin.SkinService} to manage faction skin assignments.
 * </p>
 */
public class FactionSkinTheme {

    private final String factionId;
    private String name;
    private String defaultSkinId;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String emblemSymbol;

    /**
     * Creates a new faction skin theme with the given faction ID.
     *
     * @param factionId the unique identifier for this faction
     */
    public FactionSkinTheme(String factionId) {
        this.factionId = Objects.requireNonNull(factionId, "Faction ID must not be null");
        this.name = factionId;
        this.defaultSkinId = "";
        this.primaryColor = "#808080";
        this.secondaryColor = "#606060";
        this.accentColor = "#4040ff";
        this.emblemSymbol = "";
    }

    // ---- Accessors ----

    /** @return the unique faction identifier */
    public String getFactionId() {
        return factionId;
    }

    /** @return the display name of this faction */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this faction.
     *
     * @param name the display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the default skin ID for this faction.
     * Units without an explicit skin will use this skin.
     *
     * @return the default skin ID
     */
    public String getDefaultSkinId() {
        return defaultSkinId;
    }

    /**
     * Sets the default skin ID for this faction.
     *
     * @param defaultSkinId the skin ID to use as default
     */
    public void setDefaultSkinId(String defaultSkinId) {
        this.defaultSkinId = defaultSkinId;
    }

    /**
     * Returns the primary color of this faction's palette.
     *
     * @return the primary color hex string
     */
    public String getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Sets the primary color of this faction's palette.
     *
     * @param primaryColor the color as a hex string
     */
    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * Returns the secondary color of this faction's palette.
     *
     * @return the secondary color hex string
     */
    public String getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * Sets the secondary color of this faction's palette.
     *
     * @param secondaryColor the color as a hex string
     */
    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    /**
     * Returns the accent color of this faction's palette.
     *
     * @return the accent color hex string
     */
    public String getAccentColor() {
        return accentColor;
    }

    /**
     * Sets the accent color of this faction's palette.
     *
     * @param accentColor the color as a hex string
     */
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    /**
     * Returns the emblem symbol reference for this faction.
     * This is used for faction emblem decals.
     *
     * @return the emblem symbol reference
     */
    public String getEmblemSymbol() {
        return emblemSymbol;
    }

    /**
     * Sets the emblem symbol reference for this faction.
     *
     * @param emblemSymbol the symbol reference (e.g., "federation_insignia")
     */
    public void setEmblemSymbol(String emblemSymbol) {
        this.emblemSymbol = emblemSymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionSkinTheme that = (FactionSkinTheme) o;
        return factionId.equals(that.factionId);
    }

    @Override
    public int hashCode() {
        return factionId.hashCode();
    }

    @Override
    public String toString() {
        return "FactionSkinTheme{" +
                "factionId='" + factionId + '\'' +
                ", name='" + name + '\'' +
                ", defaultSkinId='" + defaultSkinId + '\'' +
                '}';
    }
}

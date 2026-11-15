package com.yisy.modules.doramcu.hex;

import java.util.Objects;

/**
 * Represents a decal that can be placed on an asset surface.
 * <p>
 * Decals provide visual identification markings such as call signs, unit IDs,
 * faction emblems, and insignias. Each decal has a type, content, and a position
 * template that determines where it appears on the asset.
 * </p>
 * <p>
 * Decals are associated with a {@link Skin} and can be customized per asset instance.
 * </p>
 */
public class Decal {

    private final String id;
    private final DecalType type;
    private String content;
    private String position;
    private double size;
    private double rotation;
    private double opacity;
    private String color;

    /**
     * Creates a new decal with the given ID and type.
     *
     * @param id the unique identifier for this decal
     * @param type the type of decal (text, symbol, or faction emblem)
     */
    public Decal(String id, DecalType type) {
        this.id = Objects.requireNonNull(id, "Decal ID must not be null");
        this.type = Objects.requireNonNull(type, "Decal type must not be null");
        this.content = "";
        this.position = "";
        this.size = 1.0;
        this.rotation = 0.0;
        this.opacity = 1.0;
        this.color = "#000000";
    }

    // ---- Accessors ----

    /** @return the unique identifier of this decal */
    public String getId() {
        return id;
    }

    /** @return the type of this decal */
    public DecalType getType() {
        return type;
    }

    /**
     * Returns the content of this decal.
     * <ul>
     *   <li>For TEXT: the text string (e.g., "USS ENTERPRISE", "NCC-1701")</li>
     *   <li>For SYMBOL: the symbol reference (e.g., "fleet_insignia", "company_logo")</li>
     *   <li>For FACTION_EMBLEM: the faction emblem reference (e.g., "federation", "klingon")</li>
     * </ul>
     *
     * @return the content string
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this decal.
     *
     * @param content the content string
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the position template name where this decal is placed.
     * See {@link DecalPosition} for predefined position names.
     *
     * @return the position name
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the position template name.
     *
     * @param position the position name (e.g., "nose", "hull_port", "turret")
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Returns the scale factor for this decal (1.0 = default size).
     *
     * @return the size multiplier
     */
    public double getSize() {
        return size;
    }

    /**
     * Sets the scale factor for this decal.
     *
     * @param size the size multiplier (0.1 to 5.0)
     */
    public void setSize(double size) {
        this.size = Math.max(0.1, Math.min(5.0, size));
    }

    /**
     * Returns the rotation angle in degrees (0-360).
     *
     * @return the rotation in degrees
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Sets the rotation angle in degrees.
     *
     * @param rotation the rotation in degrees (0-360)
     */
    public void setRotation(double rotation) {
        this.rotation = rotation % 360.0;
    }

    /**
     * Returns the opacity of this decal (0.0 = fully transparent, 1.0 = fully opaque).
     *
     * @return the opacity value
     */
    public double getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity of this decal.
     *
     * @param opacity the opacity value (0.0 to 1.0)
     */
    public void setOpacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    /**
     * Returns the color of this decal as a hex string (e.g., "#FF0000").
     *
     * @return the color hex string
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of this decal.
     *
     * @param color the color as a hex string (e.g., "#FF0000")
     */
    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Decal decal = (Decal) o;
        return id.equals(decal.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Decal{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}

package com.yisy.modules.doramcu.hex;

/**
 * Describes a single layer within a {@link HexLayer} stack.
 * <p>
 * Each layer represents one level of the onion-skin model:
 * <pre>
 *   Layer N (outermost)  → Armor plating / exterior hull
 *   Layer N-1            → Structural frame
 *   ...
 *   Layer 1              → Interior bulkheads
 *   Layer 0 (innermost)  → Empty space / compartments
 * </pre>
 * <p>
 * Damage propagates inward through layers: when the outermost layer is destroyed,
 * the next layer becomes exposed and takes damage.
 * </p>
 */
public class LayerData {

    /**
     * The type of layer, determining its role in the hull structure.
     */
    public enum LayerType {
        /** Outermost armor plating — absorbs incoming damage */
        ARMOR,

        /** Structural frame — provides integrity to the hull shape */
        STRUCTURAL,

        /** Interior bulkheads — separate compartments */
        BULKHEAD,

        /** Empty compartment space — where components and crew reside */
        COMPARTMENT
    }

    private Material material;
    private final LayerType type;
    private int hitpoints;
    private int maxHitpoints;
    private HexState state = HexState.EMPTY;

    /**
     * Creates a new layer with the given type and material.
     * Hitpoints are derived from the material's ordinal value.
     *
     * @param type the layer type (ARMOR, STRUCTURAL, BULKHEAD, COMPARTMENT)
     * @param material the material of this layer
     */
    public LayerData(LayerType type, Material material) {
        this.type = type;
        this.material = material;
        this.maxHitpoints = material.ordinal();
        this.hitpoints = this.maxHitpoints;
    }

    /**
     * Creates a new layer with the given type, material, and custom hitpoints.
     *
     * @param type the layer type
     * @param material the material of this layer
     * @param hitpoints the initial (and max) hitpoints
     */
    public LayerData(LayerType type, Material material, int hitpoints) {
        this.type = type;
        this.material = material;
        this.maxHitpoints = hitpoints;
        this.hitpoints = hitpoints;
    }

    // ---- Accessors ----

    /** @return the material of this layer */
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the material of this layer, updating hitpoints accordingly.
     *
     * @param material the new material
     */
    public void setMaterial(Material material) {
        this.material = material;
        this.maxHitpoints = material.ordinal();
        this.hitpoints = Math.min(this.hitpoints, this.maxHitpoints);
    }

    /** @return the layer type */
    public LayerType getType() {
        return type;
    }

    /** @return the current hitpoints */
    public int getHitpoints() {
        return hitpoints;
    }

    /** @return the maximum hitpoints */
    public int getMaxHitpoints() {
        return maxHitpoints;
    }

    /** @return the current state of this layer */
    public HexState getState() {
        return state;
    }

    // ---- Damage ----

    /**
     * Applies damage to this layer.
     *
     * @param damage the amount of damage
     * @return true if the layer was destroyed by this damage
     */
    public boolean damage(int damage) {
        if (state == HexState.DESTROYED) {
            return false;
        }
        int oldHitpoints = this.hitpoints;
        this.hitpoints = Math.max(0, this.hitpoints - damage);
        if (this.hitpoints == 0 && oldHitpoints > 0) {
            this.state = HexState.DESTROYED;
            return true;
        }
        return false;
    }

    /**
     * @return true if this layer is destroyed
     */
    public boolean isDestroyed() {
        return state == HexState.DESTROYED;
    }

    /**
     * @return true if this layer is damaged (hitpoints below max)
     */
    public boolean isDamaged() {
        return hitpoints < maxHitpoints && hitpoints > 0;
    }

    /**
     * @return true if this layer is fully intact
     */
    public boolean isIntact() {
        return hitpoints == maxHitpoints;
    }

    /**
     * Returns the health ratio of this layer (0.0 to 1.0).
     *
     * @return health ratio
     */
    public double getHealthRatio() {
        if (maxHitpoints <= 0) {
            return 1.0;
        }
        return (double) hitpoints / maxHitpoints;
    }

    // ---- Repair ----

    /**
     * Repairs this layer, restoring hitpoints.
     *
     * @param amount the amount to repair
     */
    public void repair(int amount) {
        this.hitpoints = Math.min(maxHitpoints, this.hitpoints + amount);
        if (this.hitpoints > 0) {
            this.state = HexState.EMPTY;
        }
    }

    /**
     * Fully repairs this layer.
     */
    public void fullRepair() {
        this.hitpoints = maxHitpoints;
        this.state = HexState.EMPTY;
    }

    // ---- Object contract ----

    @Override
    public String toString() {
        return String.format("LayerData{type=%s, material=%s, hp=%d/%d, state=%s}",
                type, material, hitpoints, maxHitpoints, state);
    }
}

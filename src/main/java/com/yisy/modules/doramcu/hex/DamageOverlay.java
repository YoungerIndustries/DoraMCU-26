package com.yisy.modules.doramcu.hex;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maps damage tiers to visual overlay descriptions for a specific skin.
 * <p>
 * Each {@link DamageTier} has an associated description that defines how damage
 * appears on the asset surface at that severity level. For example:
 * </p>
 * <ul>
 *   <li>PRISTINE → "No visible damage"</li>
 *   <li>LIGHT → "Light scorch marks on surface"</li>
 *   <li>MODERATE → "Dented plating with exposed wiring"</li>
 *   <li>HEAVY → "Breach with structural deformation"</li>
 *   <li>CRITICAL → "Catastrophic failure, hull fragmentation"</li>
 * </ul>
 * <p>
 * This class is used by {@link com.yisy.modules.doramcu.hex.M3space} to compute
 * the visual overlay for each cell based on its current hitpoint ratio.
 * </p>
 */
public class DamageOverlay {

    private final Map<DamageTier, String> tierDescriptions;

    /**
     * Creates an empty damage overlay.
     */
    public DamageOverlay() {
        this.tierDescriptions = new HashMap<>();
    }

    /**
     * Creates a damage overlay with the given descriptions.
     *
     * @param tierDescriptions map from damage tier to visual description
     */
    public DamageOverlay(Map<DamageTier, String> tierDescriptions) {
        this.tierDescriptions = new HashMap<>(Objects.requireNonNull(tierDescriptions));
    }

    /**
     * Sets the visual description for a specific damage tier.
     *
     * @param tier the damage tier
     * @param description the visual overlay description
     */
    public void setTierDescription(DamageTier tier, String description) {
        tierDescriptions.put(Objects.requireNonNull(tier), description);
    }

    /**
     * Returns the visual description for the given damage tier.
     *
     * @param tier the damage tier
     * @return the description, or null if not configured for this tier
     */
    public String getDescription(DamageTier tier) {
        return tierDescriptions.get(tier);
    }

    /**
     * Returns the unmodifiable map of all tier descriptions.
     *
     * @return all tier descriptions
     */
    public Map<DamageTier, String> getAllDescriptions() {
        return Map.copyOf(tierDescriptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DamageOverlay that = (DamageOverlay) o;
        return tierDescriptions.equals(that.tierDescriptions);
    }

    @Override
    public int hashCode() {
        return tierDescriptions.hashCode();
    }

    @Override
    public String toString() {
        return "DamageOverlay{" + tierDescriptions + "}";
    }
}

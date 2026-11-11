package com.yisy.modules.doramcu.starshiptroopers.model;

import java.util.List;

/**
 * Represents special equipment (mines, sensors, heavy weapons) in the game.
 */
public record SpecialEquipment(
    String id,
    String name,
    String type,
    int attackStrength,
    int maxPerUnit,
    Integer taxStrength,
    int range,
    List<String> specialEffects,
    List<String> restrictions,
    String description
) {
    public SpecialEquipment {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Equipment id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Equipment name must not be blank");
        if (attackStrength < 0)
            throw new IllegalArgumentException("Attack strength must be non-negative");
        if (range < 0)
            throw new IllegalArgumentException("Range must be non-negative");
    }

    public boolean isExplosive() {
        return "Explosive".equals(type) || "Demolition".equals(type) || "Nuclear".equals(type);
    }

    public boolean isSensor() {
        return "Sensor".equals(type);
    }

    public boolean isHeavyWeapon() {
        return "Heavy Weapon".equals(type);
    }
}

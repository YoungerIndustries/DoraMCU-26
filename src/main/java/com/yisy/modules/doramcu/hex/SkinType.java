package com.yisy.modules.doramcu.hex;

/**
 * Enumerates the types of assets that can have skins applied.
 * <p>
 * Each type corresponds to a category of in-game asset:
 * ships, space stations, ground vehicles, and buildings.
 * </p>
 */
public enum SkinType {
    /**
     * Skins for spacecraft and warships.
     */
    SHIP,

    /**
     * Skins for orbital and planetary space stations.
     */
    SPACE_STATION,

    /**
     * Skins for ground combat and transport vehicles.
     */
    GROUND_VEHICLE,

    /**
     * Skins for buildings and structures.
     */
    BUILDING
}

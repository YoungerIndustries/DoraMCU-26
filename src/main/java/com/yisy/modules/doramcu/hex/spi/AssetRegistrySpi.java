package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.hex.SkinType;

/**
 * SPI for registering hex assets (ships, stations, vehicles, buildings)
 * so the skin system can look them up by type and ID.
 * <p>
 * Used by hex builders to register newly created assets without depending
 * on the concrete {@code AssetRegistry} in the skin package.
 * </p>
 * <p>
 * Non-Spring code can access the registry via {@link SkinProviders#getAssetRegistry()}.
 * </p>
 */
public interface AssetRegistrySpi {

    /**
     * Registers an asset in the registry.
     *
     * @param type the asset type (SHIP, SPACE_STATION, etc.)
     * @param id the unique asset ID
     * @param asset the asset instance
     */
    void register(SkinType type, int id, Object asset);

    /**
     * Unregisters an asset from the registry.
     *
     * @param type the asset type
     * @param id the asset ID
     * @return the removed asset, or null if not found
     */
    Object unregister(SkinType type, int id);

    /**
     * Looks up an asset by type and ID.
     *
     * @param type the asset type
     * @param id the asset ID
     * @return the asset instance, or null if not found
     */
    Object get(SkinType type, int id);

    /**
     * Checks if an asset exists in the registry.
     *
     * @param type the asset type
     * @param id the asset ID
     * @return true if the asset is registered
     */
    boolean exists(SkinType type, int id);

    /**
     * Type-safe getter for an asset.
     *
     * @param type the asset type
     * @param id the asset ID
     * @param clazz the expected class
     * @param <T> the asset type
     * @return the asset cast to the expected type, or null if not found
     */
    <T> T getAs(SkinType type, int id, Class<T> clazz);
}

package com.yisy.modules.doramcu.skin;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.SkinType;
import com.yisy.modules.doramcu.hex.spi.AssetRegistrySpi;
import com.yisy.modules.doramcu.hex.spi.SkinProviders;

import com.yisy.modules.doramcu.hex.BuildingHull;
import com.yisy.modules.doramcu.hex.GroundVehicle;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for in-memory hex assets (Ship, SpaceStation, GroundVehicle, BuildingHull).
 * <p>
 * Since these assets are not JPA entities, the skin system needs a way to look them up
 * by type and ID to assign skins and query visual state. Game modules register their
 * assets with this registry when they are created.
 * </p>
 * <p>
 * Non-Spring code (e.g., plain Java classes like {@link com.yisy.modules.doramcu.hex.Game})
 * can access the registry via {@link #getInstance()} after Spring initialization.
 * </p>
 */
@Component
public class AssetRegistry implements AssetRegistrySpi {

    /** Static holder for access from non-Spring-managed code. */
    private static volatile AssetRegistrySpi instance;

    private final Map<SkinType, Map<Integer, Object>> assets = new ConcurrentHashMap<>();

    /**
     * Returns the Spring-managed singleton instance for use by non-Spring code.
     *
     * @return the registry instance, or null if Spring has not initialized it yet
     */
    public static AssetRegistrySpi getInstance() {
        return instance;
    }

    /**
     * Sets the static instance reference after Spring injection.
     */
    @Autowired
    public void setInstance(@Lazy AssetRegistry self) {
        instance = self;
        SkinProviders.setAssetRegistry(self);
    }

    /**
     * Registers an asset in the registry.
     *
     * @param type the asset type (SHIP, SPACE_STATION, etc.)
     * @param id the unique asset ID
     * @param asset the asset instance
     */
    public void register(SkinType type, int id, Object asset) {
        assets.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(id, asset);
    }

    /**
     * Unregisters an asset from the registry.
     *
     * @param type the asset type
     * @param id the asset ID
     * @return the removed asset, or null if not found
     */
    public Object unregister(SkinType type, int id) {
        Map<Integer, Object> byId = assets.get(type);
        if (byId == null) {
            return null;
        }
        return byId.remove(id);
    }

    /**
     * Looks up an asset by type and ID.
     *
     * @param type the asset type
     * @param id the asset ID
     * @return the asset instance, or null if not found
     */
    public Object get(SkinType type, int id) {
        Map<Integer, Object> byId = assets.get(type);
        if (byId == null) {
            return null;
        }
        return byId.get(id);
    }

    /**
     * Checks if an asset exists in the registry.
     *
     * @param type the asset type
     * @param id the asset ID
     * @return true if the asset is registered
     */
    public boolean exists(SkinType type, int id) {
        Map<Integer, Object> byId = assets.get(type);
        if (byId == null) {
            return false;
        }
        return byId.containsKey(id);
    }

    /**
     * Type-safe getter for Ship.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(SkinType type, int id, Class<T> clazz) {
        Object asset = get(type, id);
        if (asset == null) {
            return null;
        }
        if (!clazz.isInstance(asset)) {
            throw new ClassCastException("Asset is not of type " + clazz.getSimpleName());
        }
        return clazz.cast(asset);
    }
}

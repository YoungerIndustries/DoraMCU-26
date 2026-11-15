package com.yisy.modules.doramcu.hex.spi;

/**
 * Static provider holder for skin-related SPI instances.
 * <p>
 * Allows non-Spring code (e.g., plain Java builders) to access
 * the skin registry and lookup services without depending on
 * the concrete implementations in the skin package.
 * </p>
 * <p>
 * The concrete implementations ({@code AssetRegistry} and {@code SkinService})
 * set their instances here during Spring initialization.
 * </p>
 */
public final class SkinProviders {

    private static volatile AssetRegistrySpi assetRegistry;
    private static volatile SkinLookupSpi skinLookup;

    private SkinProviders() {
        // utility
    }

    /**
     * Sets the asset registry instance (called by Spring-managed AssetRegistry).
     */
    public static void setAssetRegistry(AssetRegistrySpi registry) {
        assetRegistry = registry;
    }

    /**
     * Returns the asset registry instance, or null if not yet initialized.
     */
    public static AssetRegistrySpi getAssetRegistry() {
        return assetRegistry;
    }

    /**
     * Sets the skin lookup instance (called by Spring-managed SkinService).
     */
    public static void setSkinLookup(SkinLookupSpi lookup) {
        skinLookup = lookup;
    }

    /**
     * Returns the skin lookup instance, or null if not yet initialized.
     */
    public static SkinLookupSpi getSkinLookup() {
        return skinLookup;
    }
}

package com.yisy.modules.doramcu.hex.spi;

import com.yisy.modules.doramcu.hex.Skin;
import com.yisy.modules.doramcu.hex.SkinType;

/**
 * SPI for looking up default skins by name.
 * <p>
 * Used by hex builders to assign default skins to newly created assets
 * without depending on the concrete {@code SkinService} in the skin package.
 * </p>
 * <p>
 * Non-Spring code can access the lookup via {@link SkinProviders#getSkinLookup()}.
 * </p>
 */
public interface SkinLookupSpi {

    /**
     * Looks up a skin by its unique ID.
     *
     * @param id the skin ID
     * @return the skin, or null if not found
     */
    Skin getSkin(String id);
}

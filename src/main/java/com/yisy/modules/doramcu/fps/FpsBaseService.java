package com.yisy.modules.doramcu.fps;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class that game modules extend to provide themed FPS content.
 * <p>
 * Each game module (Traveller, Babylon5, Earth2026) can extend this class
 * to contribute themed weapons, enemies, and maps to the FPS framework.
 * </p>
 * <p>
 * Example:
 * <pre>
 * @Service
public class TravellerFpsService extends FpsBaseService {
 *     &#64;Override
 *     public String getTheme() { return "Traveller"; }
 *
 *     &#64;Override
 *     public List&lt;FpsWeapon&gt; getWeapons() {
 *         return List.of(
 *             new FpsWeapon("Laser Pistol", FpsWeapon.WeaponType.PISTOL, 30, 5.0, 1.5, 20),
 *             new FpsWeapon("Laser Rifle", FpsWeapon.WeaponType.RIFLE, 40, 8.0, 2.0, 40)
 *         );
 *     }
 * }
 * </pre>
 * </p>
 */
public abstract class FpsBaseService {

    /**
     * Returns the theme name for this game module (e.g., "Traveller", "Babylon5").
     */
    public abstract String getTheme();

    /**
     * Returns themed weapons for this game module.
     * Override to provide custom weapons.
     */
    public List<FpsWeapon> getWeapons() {
        return new ArrayList<>();
    }

    /**
     * Returns themed enemies for this game module.
     * Override to provide custom enemies.
     */
    public List<FpsEnemy> getEnemies() {
        return new ArrayList<>();
    }

    /**
     * Returns themed maps for this game module.
     * Override to provide custom maps.
     */
    public List<FpsMap> getMaps() {
        return new ArrayList<>();
    }

    /**
     * Register this module's content with the FPS services.
     * Called automatically when the module is initialized.
     */
    public void registerContent(FpsWeaponService weaponService, FpsMapService mapService) {
        // Register weapons
        for (FpsWeapon weapon : getWeapons()) {
            weaponService.addWeapon(weapon);
        }

        // Register maps
        for (FpsMap map : getMaps()) {
            map.setTheme(getTheme());
            mapService.addMap(map);
        }
    }
}

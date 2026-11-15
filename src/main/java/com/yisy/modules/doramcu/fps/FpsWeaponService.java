package com.yisy.modules.doramcu.fps;

import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * Manages FPS weapon definitions.
 * <p>
 * Provides default weapons and allows game modules to add themed weapons.
 * Weapons are stored in memory and can be extended by game module implementations.
 * </p>
 */
@Service
public class FpsWeaponService {

    private static final Logger log = LoggerFactory.getLogger(FpsWeaponService.class);

    private final Map<String, FpsWeapon> weapons;

    public FpsWeaponService() {
        this.weapons = new ConcurrentHashMap<>();
        initializeDefaultWeapons();
    }

    /**
     * Initialize the default weapon set.
     */
    private void initializeDefaultWeapons() {
        addWeapon(new FpsWeapon("Pistol", FpsWeapon.WeaponType.PISTOL, 25, 4.0, 1.5, 15));
        addWeapon(new FpsWeapon("Assault Rifle", FpsWeapon.WeaponType.RIFLE, 20, 10.0, 2.0, 30));
        addWeapon(new FpsWeapon("Shotgun", FpsWeapon.WeaponType.SHOTGUN, 50, 1.5, 2.5, 8));
        addWeapon(new FpsWeapon("Machine Gun", FpsWeapon.WeaponType.MACHINE_GUN, 15, 15.0, 3.0, 100));
        addWeapon(new FpsWeapon("Sniper Rifle", FpsWeapon.WeaponType.SNIPER, 80, 1.0, 3.0, 5));
        addWeapon(new FpsWeapon("Rocket Launcher", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 150, 0.5, 4.0, 4));
    }

    /**
     * Add a weapon to the registry.
     */
    public void addWeapon(FpsWeapon weapon) {
        weapons.put(weapon.getId(), weapon);
        log.debug("Registered weapon: {} ({})", weapon.getName(), weapon.getType());
    }

    /**
     * Get a weapon by ID.
     */
    public Optional<FpsWeapon> getWeapon(String id) {
        return Optional.ofNullable(weapons.get(id));
    }

    /**
     * List all available weapons.
     */
    public List<FpsWeapon> getAllWeapons() {
        return new ArrayList<>(weapons.values());
    }

    /**
     * Get weapons by type.
     */
    public List<FpsWeapon> getWeaponsByType(FpsWeapon.WeaponType type) {
        return weapons.values().stream()
                .filter(w -> w.getType() == type)
                .toList();
    }

    /**
     * Remove a weapon from the registry.
     */
    public boolean removeWeapon(String id) {
        FpsWeapon removed = weapons.remove(id);
        if (removed != null) {
            log.info("Removed weapon: {}", removed.getName());
        }
        return removed != null;
    }
}

package com.yisy.modules.doramcu.dnd5e.integration;

import com.yisy.modules.doramcu.fps.FpsBaseService;
import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides DnD5e-themed FPS content: swords, spells, monsters, dungeon maps.
 * <p>
 * Extends FpsBaseService with DnD-specific maps, weapons, and enemies.
 * </p>
 */
@Component
public class Dnd5eFpsService extends FpsBaseService {

    @Override
    public String getTheme() {
        return "DnD5e";
    }

    @Override
    public List<FpsWeapon> getWeapons() {
        List<FpsWeapon> weapons = new ArrayList<>();
        weapons.add(createWeapon("Longsword", FpsWeapon.WeaponType.RIFLE, 8, 1.5, 1.0, 100));
        weapons.add(createWeapon("Longbow", FpsWeapon.WeaponType.RIFLE, 10, 1.0, 1.0, 20));
        weapons.add(createWeapon("Warhammer", FpsWeapon.WeaponType.RIFLE, 10, 1.5, 1.0, 100));
        weapons.add(createWeapon("Dagger", FpsWeapon.WeaponType.PISTOL, 4, 2.0, 0.5, 200));
        weapons.add(createWeapon("Crossbow", FpsWeapon.WeaponType.RIFLE, 8, 0.8, 1.5, 30));
        weapons.add(createWeapon("Fireball", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 25, 0.5, 3.0, 5));
        weapons.add(createWeapon("Lightning Bolt", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 20, 0.5, 2.0, 5));
        weapons.add(createWeapon("Magic Missile", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 12, 1.0, 1.0, 10));
        return weapons;
    }

    private FpsWeapon createWeapon(String name, FpsWeapon.WeaponType type, int damage, double fireRate, double reloadTime, int magSize) {
        FpsWeapon w = new FpsWeapon();
        w.setName(name);
        w.setType(type);
        w.setDamage(damage);
        w.setFireRate(fireRate);
        w.setReloadTime(reloadTime);
        w.setMagSize(magSize);
        w.setReserveAmmo(magSize * 5);
        return w;
    }

    @Override
    public List<FpsEnemy> getEnemies() {
        List<FpsEnemy> enemies = new ArrayList<>();
        enemies.add(createEnemy("Goblin", 7, 3.0, FpsEnemy.EnemyBehavior.PATROL, 3));
        enemies.add(createEnemy("Orc", 15, 2.0, FpsEnemy.EnemyBehavior.CHASE, 6));
        enemies.add(createEnemy("Skeleton", 10, 2.0, FpsEnemy.EnemyBehavior.ATTACK, 4));
        enemies.add(createEnemy("Zombie", 22, 1.0, FpsEnemy.EnemyBehavior.CHASE, 5));
        enemies.add(createEnemy("Troll", 50, 2.0, FpsEnemy.EnemyBehavior.ATTACK, 10));
        enemies.add(createEnemy("Dragon", 200, 4.0, FpsEnemy.EnemyBehavior.AMBUSH, 25));
        enemies.add(createEnemy("Giant", 80, 2.0, FpsEnemy.EnemyBehavior.CHASE, 15));
        return enemies;
    }

    private FpsEnemy createEnemy(String name, int health, double speed, FpsEnemy.EnemyBehavior behavior, int damage) {
        FpsEnemy e = new FpsEnemy();
        e.setName(name);
        e.setHealth(health);
        e.setSpeed(speed);
        e.setBehavior(behavior);
        e.setDamage(damage);
        e.setAttackRange(2.0);
        e.setAttackCooldown(1.0);
        e.setExperienceReward(health);
        return e;
    }

    @Override
    public List<FpsMap> getMaps() {
        List<FpsMap> maps = new ArrayList<>();
        maps.add(createMap("Dragon's Lair", 100.0, 50.0, 30.0));
        maps.add(createMap("Forgotten Forest", 200.0, 200.0, 50.0));
        maps.add(createMap("Crimson Keep", 80.0, 80.0, 40.0));
        return maps;
    }

    private FpsMap createMap(String name, double width, double length, double height) {
        FpsMap m = new FpsMap(name, width, length, height);
        return m;
    }
}

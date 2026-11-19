package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import com.yisy.modules.doramcu.fps.FpsBaseService;
import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides Lord of the Rings-themed FPS content: bows, swords, spells, monsters, and Middle-earth maps.
 * <p>
 * Extends FpsBaseService with LOTR-specific weapons, enemies, and maps inspired by Tolkien's legendarium
 * and existing LOTR video games (Shadow of Mordor, War in the North).
 * </p>
 */
@Component
public class LordOfTheRingsFpsService extends FpsBaseService {

    @Override
    public String getTheme() {
        return "Lord of the Rings";
    }

    @Override
    public List<FpsWeapon> getWeapons() {
        List<FpsWeapon> weapons = new ArrayList<>();
        // Bows
        weapons.add(createWeapon("Elven Longbow", FpsWeapon.WeaponType.RIFLE, 15, 1.2, 2.0, 24, true, 300.0, 120.0, 0.5, 0.3,
                "High-accuracy bow crafted by Elves of Lothlórien"));
        weapons.add(createWeapon("Gondor War Bow", FpsWeapon.WeaponType.RIFLE, 12, 1.5, 1.5, 20, true, 250.0, 80.0, 0.8, 0.5,
                "Standard issue bow of the Rangers of Gondor"));
        weapons.add(createWeapon("Crossbow", FpsWeapon.WeaponType.RIFLE, 10, 0.8, 3.0, 10, false, 200.0, 60.0, 1.0, 0.6,
                "Slow reload but accurate; used by Dwarves and Men"));
        // Melee weapons (mapped to rapid-fire RIFLE type)
        weapons.add(createWeapon("Rohirrim Longsword", FpsWeapon.WeaponType.RIFLE, 10, 2.0, 0.0, 999, true, 0.0, 5.0, 0.3, 0.2,
                "Swift blade of the horse-lords of Rohan"));
        weapons.add(createWeapon("Dwarf Warhammer", FpsWeapon.WeaponType.RIFLE, 14, 1.5, 0.0, 999, true, 0.0, 5.0, 0.6, 0.4,
                "Heavy blunt weapon that shatters armor"));
        weapons.add(createWeapon("Uruk-hai Axe", FpsWeapon.WeaponType.RIFLE, 12, 1.8, 0.0, 999, true, 0.0, 5.0, 0.5, 0.3,
                "Fast melee weapon of Isengard's elite troops"));
        weapons.add(createWeapon("Hobbit Dagger", FpsWeapon.WeaponType.PISTOL, 5, 3.0, 0.0, 999, true, 0.0, 3.0, 0.2, 0.1,
                "Quick sidearm favored by stealthy Hobbits"));
        // Spell weapons
        weapons.add(createWeapon("Wizard Staff", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 30, 0.5, 4.0, 5, false, 150.0, 60.0, 1.5, 1.0,
                "Channels magical energy as a devastating projectile"));
        weapons.add(createWeapon("Fireball Spell", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 40, 0.3, 5.0, 3, false, 120.0, 50.0, 2.0, 1.5,
                "Area damage spell that engulfs enemies in flame"));
        // Heavy weapons
        weapons.add(createWeapon("Ent Mace", FpsWeapon.WeaponType.MACHINE_GUN, 20, 0.8, 0.0, 999, true, 0.0, 10.0, 1.0, 0.8,
                "Slow but devastating weapon of the Ents"));
        return weapons;
    }

    private FpsWeapon createWeapon(String name, FpsWeapon.WeaponType type, int damage, double fireRate,
                                    double reloadTime, int magSize, boolean hitScan, double projectileSpeed,
                                    double range, double recoilPitch, double recoilYaw, String description) {
        FpsWeapon w = new FpsWeapon();
        w.setName(name);
        w.setType(type);
        w.setDamage(damage);
        w.setFireRate(fireRate);
        w.setReloadTime(reloadTime);
        w.setMagSize(magSize);
        w.setReserveAmmo(magSize == 999 ? 999 : magSize * 5);
        w.setHitScan(hitScan);
        w.setProjectileSpeed(projectileSpeed);
        w.setRange(range);
        w.setRecoilPitch(recoilPitch);
        w.setRecoilYaw(recoilYaw);
        w.setDescription(description);
        return w;
    }

    @Override
    public List<FpsEnemy> getEnemies() {
        List<FpsEnemy> enemies = new ArrayList<>();
        // Orcs and minions
        enemies.add(createEnemy("Orc Grunt", 30, 3.5, FpsEnemy.EnemyBehavior.CHASE, 8, 2.0, 1.0, "Orc Scrap", 0.4, 50,
                "Common foot soldier of Mordor"));
        enemies.add(createEnemy("Uruk-hai", 60, 3.0, FpsEnemy.EnemyBehavior.ATTACK, 15, 3.0, 0.8, "Uruk Blade", 0.3, 120,
                "Elite warrior bred by Saruman in Isengard"));
        enemies.add(createEnemy("Easterling", 40, 3.2, FpsEnemy.EnemyBehavior.PATROL, 10, 2.5, 1.0, "Eastern Coin", 0.3, 70,
                "Mercenary from the lands of Rhûn"));
        enemies.add(createEnemy("Haradrim", 45, 2.8, FpsEnemy.EnemyBehavior.FLANK, 12, 2.5, 1.0, "Harad Spice", 0.3, 80,
                "Southern warrior serving Sauron"));
        // Beasts
        enemies.add(createEnemy("Warg", 50, 6.0, FpsEnemy.EnemyBehavior.AMBUSH, 12, 3.0, 0.5, "Wang Teeth", 0.5, 100,
                "Giant wolf ridden by Orc captains"));
        enemies.add(createEnemy("Giant Spider", 80, 4.5, FpsEnemy.EnemyBehavior.AMBUSH, 18, 4.0, 0.7, "Spider Silk", 0.4, 200,
                "Shelob's kin, lurking in dark passes"));
        enemies.add(createEnemy("Troll", 150, 1.5, FpsEnemy.EnemyBehavior.ATTACK, 25, 3.0, 1.5, "Troll Club", 0.2, 300,
                "Slow but immensely strong cave troll"));
        // Bosses
        enemies.add(createEnemy("Nazgûl", 200, 5.0, FpsEnemy.EnemyBehavior.AMBUSH, 30, 5.0, 0.5, "Black Blade", 0.1, 500,
                "Wraith of the Ringwraiths, fearsome and relentless"));
        enemies.add(createEnemy("Balrog", 500, 4.0, FpsEnemy.EnemyBehavior.CHASE, 50, 6.0, 0.3, "Flame Whip", 0.05, 1000,
                "Demon of ancient power, wreathed in fire and shadow"));
        // Allied/neutral
        enemies.add(createEnemy("Ent", 300, 1.0, FpsEnemy.EnemyBehavior.PATROL, 35, 4.0, 2.0, "Ent Branch", 0.1, 400,
                "Ancient treeherd, slow but devastating in anger"));
        return enemies;
    }

    private FpsEnemy createEnemy(String name, int health, double speed, FpsEnemy.EnemyBehavior behavior,
                                  int damage, double attackRange, double attackCooldown, String dropItem,
                                  double dropChance, int experienceReward, String description) {
        FpsEnemy e = new FpsEnemy();
        e.setName(name);
        e.setHealth(health);
        e.setSpeed(speed);
        e.setBehavior(behavior);
        e.setDamage(damage);
        e.setAttackRange(attackRange);
        e.setAttackCooldown(attackCooldown);
        e.setDropItem(dropItem);
        e.setDropChance(dropChance);
        e.setExperienceReward(experienceReward);
        e.setDescription(description);
        return e;
    }

    @Override
    public List<FpsMap> getMaps() {
        List<FpsMap> maps = new ArrayList<>();
        // Fortress defenses
        maps.add(createMap("Helm's Deep", "Fortress Defense", 120.0, 100.0, 40.0,
                "Cliff-side fortress of Rohan under siege by Isengard's armies"));
        maps.add(createMap("Minas Tirith", "City Siege", 150.0, 120.0, 60.0,
                "White city of Gondor with multiple concentric walls"));
        // Dungeons and caves
        maps.add(createMap("Moria", "Dungeon Crawl", 100.0, 80.0, 30.0,
                "Underground Dwarf kingdom infested with Orcs and worse"));
        maps.add(createMap("Cirith Ungol", "Mountain Pass", 60.0, 40.0, 80.0,
                "Spider-infested mountain pass above Shelob's lair"));
        // Forests
        maps.add(createMap("Fangorn Forest", "Forest Ambush", 200.0, 200.0, 50.0,
                "Ancient forest home of the Ents"));
        maps.add(createMap("Mirkwood", "Dark Forest", 180.0, 160.0, 45.0,
                "Enchanted forest filled with giant spiders"));
        // Open battlefields
        maps.add(createMap("Mordor Plains", "Open Battlefield", 250.0, 200.0, 40.0,
                "Volcanic wasteland before the gates of Mordor"));
        maps.add(createMap("Dagorlad", "River Battlefield", 180.0, 100.0, 30.0,
                "Plains before Mordor's gates, site of the Last Alliance"));
        // Peaceful lands
        maps.add(createMap("The Shire", "Peaceful Homeland", 80.0, 80.0, 20.0,
                "Rolling green hills of Hobbiton"));
        maps.add(createMap("Rivendell", "Elven Sanctuary", 100.0, 80.0, 30.0,
                "Hidden valley fortress of Elrond"));
        // Industrial
        maps.add(createMap("Isengard", "Industrial Complex", 120.0, 100.0, 50.0,
                "Tree-lined fortress corrupted by Saruman's industry"));
        return maps;
    }

    private FpsMap createMap(String name, String theme, double width, double length, double height, String description) {
        FpsMap m = new FpsMap(name, width, length, height);
        m.setTheme(theme);
        m.setDescription(description);
        return m;
    }
}

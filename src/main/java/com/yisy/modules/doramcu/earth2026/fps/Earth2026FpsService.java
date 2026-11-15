package com.yisy.modules.doramcu.earth2026.fps;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.fps.FpsBaseService;
import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;

import java.util.Arrays;
import java.util.List;

/**
 * Earth2026-themed FPS content: modern firearms, tactical gear, and urban combat maps.
 */

@Service
public class Earth2026FpsService extends FpsBaseService {

    @Override
    public String getTheme() {
        return "Earth2026";
    }

    @Override
    public List<FpsWeapon> getWeapons() {
        // M4 Assault Rifle - standard issue
        FpsWeapon m4Rifle = new FpsWeapon("M4 Assault Rifle", FpsWeapon.WeaponType.RIFLE, 30, 10.0, 2.2, 30);
        m4Rifle.setHitScan(true);
        m4Rifle.setRange(90.0);
        m4Rifle.setDescription("Standard M4 carbine with 5.56mm ammunition.");

        // Desert Eagle - heavy pistol
        FpsWeapon desertEagle = new FpsWeapon("Desert Eagle", FpsWeapon.WeaponType.PISTOL, 50, 3.0, 1.8, 7);
        desertEagle.setHitScan(true);
        desertEagle.setRange(30.0);
        desertEagle.setDescription(".50 AE Desert Eagle for close-range stopping power.");

        // MP5 Submachine Gun - CQB weapon
        FpsWeapon mp5 = new FpsWeapon("MP5 Submachine Gun", FpsWeapon.WeaponType.RIFLE, 22, 15.0, 1.5, 30);
        mp5.setHitScan(true);
        mp5.setRange(40.0);
        mp5.setDescription("Compact MP5 for close-quarters battle.");

        // M24 Sniper - precision rifle
        FpsWeapon m24Sniper = new FpsWeapon("M24 Sniper", FpsWeapon.WeaponType.SNIPER, 90, 0.8, 4.0, 5);
        m24Sniper.setHitScan(true);
        m24Sniper.setRange(250.0);
        m24Sniper.setDescription(".308 M24 sniper rifle for long-range elimination.");

        // M79 Grenade Launcher - area weapon
        FpsWeapon m79 = new FpsWeapon("M79 Grenade Launcher", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 120, 0.6, 3.5, 1);
        m79.setHitScan(false);
        m79.setProjectileSpeed(30.0);
        m79.setRange(50.0);
        m79.setDescription("20mm M79 grenade launcher for area denial.");

        // M249 SAW - light machine gun
        FpsWeapon m249 = new FpsWeapon("M249 SAW", FpsWeapon.WeaponType.MACHINE_GUN, 25, 12.0, 4.0, 100);
        m249.setHitScan(true);
        m249.setRange(80.0);
        m249.setDescription("Squad Automatic Weapon for sustained suppressive fire.");

        return Arrays.asList(m4Rifle, desertEagle, mp5, m24Sniper, m79, m249);
    }

    @Override
    public List<FpsEnemy> getEnemies() {
        // Corporate Mercenary
        FpsEnemy mercenary = new FpsEnemy("Corporate Mercenary", 100, 3.0, FpsEnemy.EnemyBehavior.ATTACK, 25);
        mercenary.setAttackRange(50.0);
        mercenary.setAttackCooldown(1.5);
        mercenary.setExperienceReward(120);
        mercenary.setDescription("Well-armed corporate security mercenary.");

        // Riot Police
        FpsEnemy riotPolice = new FpsEnemy("Riot Police", 80, 2.5, FpsEnemy.EnemyBehavior.PATROL, 15);
        riotPolice.setAttackRange(30.0);
        riotPolice.setAttackCooldown(2.0);
        riotPolice.setExperienceReward(80);
        riotPolice.setDescription("Armored riot police with crowd control weapons.");

        // Insurgent
        FpsEnemy insurgent = new FpsEnemy("Insurgent", 70, 4.0, FpsEnemy.EnemyBehavior.AMBUSH, 20);
        insurgent.setAttackRange(40.0);
        insurgent.setAttackCooldown(1.2);
        insurgent.setExperienceReward(100);
        insurgent.setDescription("Guerrilla fighter using ambush tactics.");

        // Heavy Gunner
        FpsEnemy heavyGunner = new FpsEnemy("Heavy Gunner", 150, 2.0, FpsEnemy.EnemyBehavior.CHASE, 35);
        heavyGunner.setAttackRange(60.0);
        heavyGunner.setAttackCooldown(2.5);
        heavyGunner.setExperienceReward(180);
        heavyGunner.setDescription("Heavily armed enemy with automatic weapons.");

        return Arrays.asList(mercenary, riotPolice, insurgent, heavyGunner);
    }

    @Override
    public List<FpsMap> getMaps() {
        // Urban Combat - San Diego Streets
        FpsMap urbanCombat = new FpsMap("San Diego Streets", 100, 100, 15);
        urbanCombat.setDescription("Street-level urban combat in San Diego with buildings, vehicles, and cover.");
        urbanCombat.setTheme("Earth2026");
        urbanCombat.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-40, 1.7, -40, 0),
            new FpsMap.SpawnPoint(40, 1.7, -40, 1),
            new FpsMap.SpawnPoint(-40, 1.7, 40, 0),
            new FpsMap.SpawnPoint(40, 1.7, 40, 1)
        ));
        urbanCombat.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-10, 10, 0, 5, -10, 10),
            new FpsMap.CollisionBox(-25, -15, 0, 4, -25, -15),
            new FpsMap.CollisionBox(15, 25, 0, 4, 15, 25),
            new FpsMap.CollisionBox(-25, -15, 0, 3, 15, 25),
            new FpsMap.CollisionBox(15, 25, 0, 3, -25, -15)
        ));

        // Younger Industries HQ - Corporate infiltration
        FpsMap corporateHq = new FpsMap("Younger Industries HQ", 80, 80, 10);
        corporateHq.setDescription("Infiltration of Younger Industries headquarters in San Diego. Multi-floor corporate building with security systems.");
        corporateHq.setTheme("Earth2026");
        corporateHq.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-30, 1.7, -30, 0),
            new FpsMap.SpawnPoint(30, 1.7, -30, 1),
            new FpsMap.SpawnPoint(-30, 1.7, 30, 0),
            new FpsMap.SpawnPoint(30, 1.7, 30, 1)
        ));
        corporateHq.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-5, 5, 0, 3, -5, 5),
            new FpsMap.CollisionBox(-20, -10, 0, 2, -20, -10),
            new FpsMap.CollisionBox(10, 20, 0, 2, 10, 20)
        ));

        // San Diego Convention Center - Large indoor arena
        FpsMap conventionCenter = new FpsMap("San Diego Convention Center", 120, 100, 12);
        conventionCenter.setDescription("Massive convention hall with exhibition booths, hallways, and multiple levels. Perfect for large-scale firefights.");
        conventionCenter.setTheme("Earth2026");
        conventionCenter.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-50, 1.7, -40, 0),
            new FpsMap.SpawnPoint(50, 1.7, -40, 1),
            new FpsMap.SpawnPoint(-50, 1.7, 40, 0),
            new FpsMap.SpawnPoint(50, 1.7, 40, 1),
            new FpsMap.SpawnPoint(0, 1.7, 0, 2)
        ));
        conventionCenter.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-30, -20, 0, 5, -30, -20),
            new FpsMap.CollisionBox(20, 30, 0, 5, 20, 30),
            new FpsMap.CollisionBox(-10, 10, 0, 3, -10, 10),
            new FpsMap.CollisionBox(-40, -30, 0, 4, 30, 40),
            new FpsMap.CollisionBox(30, 40, 0, 4, -40, -30)
        ));

        // Area 51 - Secret military base
        FpsMap area51 = new FpsMap("Area 51", 150, 120, 15);
        area51.setDescription("Classified military installation in the Nevada desert. Defend against waves of corporate mercenaries and supernatural entities.");
        area51.setTheme("Earth2026");
        area51.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-60, 1.7, -50, 0),
            new FpsMap.SpawnPoint(60, 1.7, -50, 1),
            new FpsMap.SpawnPoint(-60, 1.7, 50, 0),
            new FpsMap.SpawnPoint(60, 1.7, 50, 1)
        ));
        area51.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-20, 20, 0, 5, -20, 20),
            new FpsMap.CollisionBox(-50, -40, 0, 3, -50, -40),
            new FpsMap.CollisionBox(40, 50, 0, 3, 40, 50),
            new FpsMap.CollisionBox(-30, -20, 0, 4, 30, 40),
            new FpsMap.CollisionBox(20, 30, 0, 4, -40, -30)
        ));

        // NYC Rooftops - Vertical combat
        FpsMap nycRooftops = new FpsMap("NYC Rooftops", 90, 90, 20);
        nycRooftops.setDescription("Rooftop combat across Manhattan skyscrapers. Use ziplines and gaps between buildings for tactical advantage.");
        nycRooftops.setTheme("Earth2026");
        nycRooftops.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-35, 1.7, -35, 0),
            new FpsMap.SpawnPoint(35, 1.7, -35, 1),
            new FpsMap.SpawnPoint(-35, 1.7, 35, 0),
            new FpsMap.SpawnPoint(35, 1.7, 35, 1)
        ));
        nycRooftops.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-15, -5, 0, 3, -15, -5),
            new FpsMap.CollisionBox(5, 15, 0, 3, 5, 15),
            new FpsMap.CollisionBox(-25, -15, 0, 2, 25, 35),
            new FpsMap.CollisionBox(15, 25, 0, 2, -35, -25)
        ));

        // Portal Room - Supernatural dimension
        FpsMap portalRoom = new FpsMap("Portal Room", 60, 60, 8);
        portalRoom.setDescription("The mysterious portal room where dimensions intersect. Reality shifts and supernatural enemies spawn from rifts.");
        portalRoom.setTheme("Earth2026");
        portalRoom.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-20, 1.7, -20, 0),
            new FpsMap.SpawnPoint(20, 1.7, -20, 1),
            new FpsMap.SpawnPoint(-20, 1.7, 20, 0),
            new FpsMap.SpawnPoint(20, 1.7, 20, 1)
        ));
        portalRoom.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-5, 5, 0, 2, -5, 5),
            new FpsMap.CollisionBox(-15, -10, 0, 1.5, -15, -10),
            new FpsMap.CollisionBox(10, 15, 0, 1.5, 10, 15)
        ));

        // London Underground - Subway combat
        FpsMap londonUnderground = new FpsMap("London Underground", 110, 80, 6);
        londonUnderground.setDescription("Dark tunnels and platforms of the London Underground. Limited visibility, close-quarters combat.");
        londonUnderground.setTheme("Earth2026");
        londonUnderground.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-45, 1.7, -30, 0),
            new FpsMap.SpawnPoint(45, 1.7, -30, 1),
            new FpsMap.SpawnPoint(-45, 1.7, 30, 0),
            new FpsMap.SpawnPoint(45, 1.7, 30, 1)
        ));
        londonUnderground.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-20, 20, 0, 3, -10, 10),
            new FpsMap.CollisionBox(-40, -30, 0, 2, -20, -10),
            new FpsMap.CollisionBox(30, 40, 0, 2, 10, 20)
        ));

        // Tokyo Neon - Cyberpunk streets
        FpsMap tokyoNeon = new FpsMap("Tokyo Neon District", 100, 100, 12);
        tokyoNeon.setDescription("Neon-lit streets of Tokyo's entertainment district. Rain-slicked surfaces and holographic advertisements.");
        tokyoNeon.setTheme("Earth2026");
        tokyoNeon.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-40, 1.7, -40, 0),
            new FpsMap.SpawnPoint(40, 1.7, -40, 1),
            new FpsMap.SpawnPoint(-40, 1.7, 40, 0),
            new FpsMap.SpawnPoint(40, 1.7, 40, 1)
        ));
        tokyoNeon.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-15, -5, 0, 4, -15, -5),
            new FpsMap.CollisionBox(5, 15, 0, 4, 5, 15),
            new FpsMap.CollisionBox(-30, -20, 0, 3, 20, 30),
            new FpsMap.CollisionBox(20, 30, 0, 3, -30, -20)
        ));

        // Cthulhu's Call - Lovecraftian horror
        FpsMap cthulhuCall = new FpsMap("Cthulhu's Call", 130, 110, 15);
        cthulhuCall.setDescription("A nightmarish dimension where Cthulhu's influence warps reality. Sanity drains as you fight eldritch horrors.");
        cthulhuCall.setTheme("Earth2026");
        cthulhuCall.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-55, 1.7, -45, 0),
            new FpsMap.SpawnPoint(55, 1.7, -45, 1),
            new FpsMap.SpawnPoint(-55, 1.7, 45, 0),
            new FpsMap.SpawnPoint(55, 1.7, 45, 1)
        ));
        cthulhuCall.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-25, 25, 0, 5, -25, 25),
            new FpsMap.CollisionBox(-50, -40, 0, 3, -50, -40),
            new FpsMap.CollisionBox(40, 50, 0, 3, 40, 50),
            new FpsMap.CollisionBox(-35, -25, 0, 4, 35, 45),
            new FpsMap.CollisionBox(25, 35, 0, 4, -45, -35)
        ));

        return Arrays.asList(urbanCombat, corporateHq, conventionCenter, area51, nycRooftops, portalRoom, londonUnderground, tokyoNeon, cthulhuCall);
    }
}

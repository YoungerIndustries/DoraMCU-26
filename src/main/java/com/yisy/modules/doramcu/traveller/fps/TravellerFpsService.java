package com.yisy.modules.doramcu.traveller.fps;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.fps.FpsBaseService;
import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;

import java.util.Arrays;
import java.util.List;

/**
 * Traveller-themed FPS content: space marine weapons, alien enemies, and space station maps.
 */

@Service
public class TravellerFpsService extends FpsBaseService {

    @Override
    public String getTheme() {
        return "Traveller";
    }

    @Override
    public List<FpsWeapon> getWeapons() {
        // Laser Pistol - standard sidearm
        FpsWeapon laserPistol = new FpsWeapon("Laser Pistol", FpsWeapon.WeaponType.PISTOL, 30, 5.0, 1.5, 20);
        laserPistol.setHitScan(true);
        laserPistol.setRange(50.0);
        laserPistol.setDescription("Standard-issue laser pistol used by Traveller soldiers.");

        // Laser Rifle - primary weapon
        FpsWeapon laserRifle = new FpsWeapon("Laser Rifle", FpsWeapon.WeaponType.RIFLE, 40, 8.0, 2.0, 40);
        laserRifle.setHitScan(true);
        laserRifle.setRange(100.0);
        laserRifle.setDescription("High-energy laser rifle for medium-range combat.");

        // Heavy Laser - squad support weapon
        FpsWeapon heavyLaser = new FpsWeapon("Heavy Laser", FpsWeapon.WeaponType.MACHINE_GUN, 35, 12.0, 3.0, 60);
        heavyLaser.setHitScan(true);
        heavyLaser.setRange(80.0);
        heavyLaser.setDescription("Heavy repeating laser for suppressive fire.");

        // Gauss Rifle - anti-armor
        FpsWeapon gaussRifle = new FpsWeapon("Gauss Rifle", FpsWeapon.WeaponType.SNIPER, 80, 1.5, 3.5, 10);
        gaussRifle.setHitScan(false);
        gaussRifle.setProjectileSpeed(200.0);
        gaussRifle.setRange(200.0);
        gaussRifle.setDescription("Electromagnetic accelerator rifle for long-range precision shots.");

        // Grenade Launcher - area denial
        FpsWeapon grenadeLauncher = new FpsWeapon("Grenade Launcher", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 100, 0.8, 4.0, 4);
        grenadeLauncher.setHitScan(false);
        grenadeLauncher.setProjectileSpeed(40.0);
        grenadeLauncher.setRange(60.0);
        grenadeLauncher.setDescription("Under-barrel grenade launcher for area denial.");

        return Arrays.asList(laserPistol, laserRifle, heavyLaser, gaussRifle, grenadeLauncher);
    }

    @Override
    public List<FpsEnemy> getEnemies() {
        // Melnihite Warrior
        FpsEnemy melnihite = new FpsEnemy("Melnihite Warrior", 120, 3.0, FpsEnemy.EnemyBehavior.ATTACK, 25);
        melnihite.setAttackRange(50.0);
        melnihite.setAttackCooldown(1.5);
        melnihite.setExperienceReward(150);
        melnihite.setDescription("Fanatic Melnihite warrior with energy weapons.");

        // Solomani Guard
        FpsEnemy solomani = new FpsEnemy("Solomani Guard", 100, 2.5, FpsEnemy.EnemyBehavior.PATROL, 20);
        solomani.setAttackRange(40.0);
        solomani.setAttackCooldown(2.0);
        solomani.setExperienceReward(100);
        melnihite.setDescription("Elite Solomani Royal Guard soldier.");

        // Alien Scout
        FpsEnemy alienScout = new FpsEnemy("Alien Scout", 80, 5.0, FpsEnemy.EnemyBehavior.FLANK, 15);
        alienScout.setAttackRange(30.0);
        alienScout.setAttackCooldown(1.0);
        alienScout.setExperienceReward(120);
        alienScout.setDescription("Fast alien scout that flanks from the sides.");

        // War Bot
        FpsEnemy warBot = new FpsEnemy("War Bot", 200, 2.0, FpsEnemy.EnemyBehavior.CHASE, 35);
        warBot.setAttackRange(60.0);
        warBot.setAttackCooldown(2.5);
        warBot.setExperienceReward(200);
        warBot.setDescription("Heavily armored combat robot.");

        return Arrays.asList(melnihite, solomani, alienScout, warBot);
    }

    @Override
    public List<FpsMap> getMaps() {
        // Space Station map
        FpsMap spaceStation = new FpsMap("Space Station", 120, 120, 15);
        spaceStation.setDescription("Combat aboard a sprawling space station with zero-G corridors.");
        spaceStation.setTheme("Traveller");
        spaceStation.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-50, 1.7, -50, 0),
            new FpsMap.SpawnPoint(50, 1.7, -50, 1),
            new FpsMap.SpawnPoint(-50, 1.7, 50, 0),
            new FpsMap.SpawnPoint(50, 1.7, 50, 1)
        ));
        spaceStation.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-10, 10, 0, 5, -10, 10),
            new FpsMap.CollisionBox(-30, -20, 0, 3, -30, -20),
            new FpsMap.CollisionBox(20, 30, 0, 3, 20, 30)
        ));

        // Planet Surface map
        FpsMap planetSurface = new FpsMap("Planet Surface", 150, 150, 20);
        planetSurface.setDescription("Open planet surface combat with rocky terrain and alien ruins.");
        planetSurface.setTheme("Traveller");
        planetSurface.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-60, 1.7, -60, 0),
            new FpsMap.SpawnPoint(60, 1.7, -60, 1),
            new FpsMap.SpawnPoint(-60, 1.7, 60, 0),
            new FpsMap.SpawnPoint(60, 1.7, 60, 1)
        ));
        planetSurface.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-15, 15, 0, 4, -15, 15),
            new FpsMap.CollisionBox(-40, -30, 0, 2, 40, 50),
            new FpsMap.CollisionBox(30, 40, 0, 2, -50, -40)
        ));

        return Arrays.asList(spaceStation, planetSurface);
    }
}

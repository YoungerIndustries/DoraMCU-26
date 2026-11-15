package com.yisy.modules.doramcu.babylon5.fps;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.fps.FpsBaseService;
import com.yisy.modules.doramcu.fps.core.model.FpsEnemy;
import com.yisy.modules.doramcu.fps.core.model.FpsMap;
import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;

import java.util.Arrays;
import java.util.List;

/**
 * Babylon5-themed FPS content: psi corps abilities, faction weapons, and station maps.
 */

@Service
public class Babylon5FpsService extends FpsBaseService {

    @Override
    public String getTheme() {
        return "Babylon5";
    }

    @Override
    public List<FpsWeapon> getWeapons() {
        // EAS Pulse Rifle - Earth Alliance standard
        FpsWeapon pulseRifle = new FpsWeapon("EAS Pulse Rifle", FpsWeapon.WeaponType.RIFLE, 35, 10.0, 2.0, 30);
        pulseRifle.setHitScan(true);
        pulseRifle.setRange(80.0);
        pulseRifle.setDescription("Standard Earth Alliance Security pulse rifle.");

        // Minbari Energy Staff - energy weapon
        FpsWeapon energyStaff = new FpsWeapon("Minbari Energy Staff", FpsWeapon.WeaponType.RIFLE, 45, 6.0, 2.5, 25);
        energyStaff.setHitScan(true);
        energyStaff.setRange(60.0);
        energyStaff.setDescription("Minbari energy weapon with devastating power.");

        // Narn Plasma Pistol - sidearm
        FpsWeapon plasmaPistol = new FpsWeapon("Narn Plasma Pistol", FpsWeapon.WeaponType.PISTOL, 30, 4.0, 1.5, 15);
        plasmaPistol.setHitScan(true);
        plasmaPistol.setRange(40.0);
        plasmaPistol.setDescription("Narn rebel plasma pistol.");

        // Psi Blast - special ability weapon
        FpsWeapon psiBlast = new FpsWeapon("Psi Blast", FpsWeapon.WeaponType.ROCKET_LAUNCHER, 60, 1.0, 5.0, 5);
        psiBlast.setHitScan(false);
        psiBlast.setProjectileSpeed(80.0);
        psiBlast.setRange(30.0);
        psiBlast.setDescription("Psionic blast from a trained Psi Corp operative.");

        // Centauri Blaster - energy sidearm
        FpsWeapon centauriBlaster = new FpsWeapon("Centauri Blaster", FpsWeapon.WeaponType.PISTOL, 25, 6.0, 1.8, 20);
        centauriBlaster.setHitScan(true);
        centauriBlaster.setRange(45.0);
        centauriBlaster.setDescription("Elegant Centauri energy blaster.");

        return Arrays.asList(pulseRifle, energyStaff, plasmaPistol, psiBlast, centauriBlaster);
    }

    @Override
    public List<FpsEnemy> getEnemies() {
        // Shadow Agent
        FpsEnemy shadowAgent = new FpsEnemy("Shadow Agent", 150, 4.0, FpsEnemy.EnemyBehavior.AMBUSH, 30);
        shadowAgent.setAttackRange(50.0);
        shadowAgent.setAttackCooldown(1.5);
        shadowAgent.setExperienceReward(200);
        shadowAgent.setDescription("Mysterious Shadow agent with advanced weaponry.");

        // Minbari Warrior
        FpsEnemy minbariWarrior = new FpsEnemy("Minbari Warrior", 130, 2.5, FpsEnemy.EnemyBehavior.ATTACK, 35);
        minbariWarrior.setAttackRange(60.0);
        minbariWarrior.setAttackCooldown(2.0);
        minbariWarrior.setExperienceReward(150);
        minbariWarrior.setDescription("Elite Minbari warrior caste member.");

        // Ribran Mercenary
        FpsEnemy ribranMerc = new FpsEnemy("RibrÃƒÂ¡n Mercenary", 100, 3.5, FpsEnemy.EnemyBehavior.FLANK, 20);
        ribranMerc.setAttackRange(40.0);
        ribranMerc.setAttackCooldown(1.2);
        ribranMerc.setExperienceReward(120);
        ribranMerc.setDescription("RibrÃƒÂ¡n mercenary working for the highest bidder.");

        // Dominion Drone
        FpsEnemy dominionDrone = new FpsEnemy("Dominion Drone", 80, 3.0, FpsEnemy.EnemyBehavior.CHASE, 15);
        dominionDrone.setAttackRange(35.0);
        dominionDrone.setAttackCooldown(1.0);
        dominionDrone.setExperienceReward(80);
        dominionDrone.setDescription("Warrior Caste drone following orders without question.");

        return Arrays.asList(shadowAgent, minbariWarrior, ribranMerc, dominionDrone);
    }

    @Override
    public List<FpsMap> getMaps() {
        // Babylon 5 Station
        FpsMap babylonStation = new FpsMap("Babylon 5", 130, 130, 12);
        babylonStation.setDescription("Combat aboard the Babylon 5 space station.");
        babylonStation.setTheme("Babylon5");
        babylonStation.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-55, 1.7, -55, 0),
            new FpsMap.SpawnPoint(55, 1.7, -55, 1),
            new FpsMap.SpawnPoint(-55, 1.7, 55, 0),
            new FpsMap.SpawnPoint(55, 1.7, 55, 1)
        ));
        babylonStation.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-8, 8, 0, 4, -8, 8),
            new FpsMap.CollisionBox(-35, -25, 0, 3, -35, -25),
            new FpsMap.CollisionBox(25, 35, 0, 3, 25, 35),
            new FpsMap.CollisionBox(-15, -5, 0, 2, 30, 40)
        ));

        // Zhan's Bar
        FpsMap zhansBar = new FpsMap("Zhan's Bar", 60, 60, 8);
        zhansBar.setDescription("Close-quarters combat in the station's most famous bar.");
        zhansBar.setTheme("Babylon5");
        zhansBar.setSpawnPoints(Arrays.asList(
            new FpsMap.SpawnPoint(-25, 1.7, -25, 0),
            new FpsMap.SpawnPoint(25, 1.7, -25, 1),
            new FpsMap.SpawnPoint(-25, 1.7, 25, 0),
            new FpsMap.SpawnPoint(25, 1.7, 25, 1)
        ));
        zhansBar.setCollisionBoxes(Arrays.asList(
            new FpsMap.CollisionBox(-5, 5, 0, 3, -5, 5),
            new FpsMap.CollisionBox(-15, -10, 0, 2, -10, -5)
        ));

        return Arrays.asList(babylonStation, zhansBar);
    }
}

package com.yisy.modules.doramcu.fps.core;

import com.yisy.modules.doramcu.fps.core.model.FpsWeapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsPlayerStateTest {

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorInitializesNulls() {
            FpsPlayerState player = new FpsPlayerState();
            assertNull(player.getPlayerId());
            assertNull(player.getPlayerName());
            assertEquals(0, player.getHealth());
            assertEquals(0, player.getTeam());
            assertFalse(player.isAlive());
        }

        @Test
        void fullConstructorSetsAllFields() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            assertEquals("p1", player.getPlayerId());
            assertEquals("Hero", player.getPlayerName());
            assertEquals(0, player.getTeam());
            assertEquals(100, player.getHealth());
            assertEquals(100, player.getMaxHealth());
            assertEquals(30, player.getAmmo());
            assertEquals(120, player.getReserveAmmo());
            assertTrue(player.isAlive());
            assertEquals(0.0, player.getGroundY());
        }
    }

    @Nested
    class PositionTests {
        @Test
        void setPositionSetsAllCoordinates() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setPosition(10.0, 5.0, -20.0);
            assertEquals(10.0, player.getPosX());
            assertEquals(5.0, player.getPosY());
            assertEquals(-20.0, player.getPosZ());
        }

        @Test
        void setIndividualCoordinates() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setPosX(1.0);
            player.setPosY(2.0);
            player.setPosZ(3.0);
            assertEquals(1.0, player.getPosX());
            assertEquals(2.0, player.getPosY());
            assertEquals(3.0, player.getPosZ());
        }

        @Test
        void setVelocitySetsAllComponents() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setVelocity(1.0, 2.0, 3.0);
            assertEquals(1.0, player.getVelX());
            assertEquals(2.0, player.getVelY());
            assertEquals(3.0, player.getVelZ());
        }
    }

    @Nested
    class HealthTests {
        @Test
        void setHealthClampsToMax() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setHealth(200);
            assertEquals(100, player.getHealth());
        }

        @Test
        void setHealthClampsToZero() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setHealth(-50);
            assertEquals(0, player.getHealth());
            assertFalse(player.isAlive());
        }

        @Test
        void damageKillsPlayer() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setHealth(50);
            player.setHealth(0);
            assertFalse(player.isAlive());
        }

        @Test
        void maxHealthCanBeChanged() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setMaxHealth(200);
            player.setHealth(200);
            assertEquals(200, player.getHealth());
        }
    }

    @Nested
    class AmmoTests {
        @Test
        void ammoCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setAmmo(50);
            assertEquals(50, player.getAmmo());
        }

        @Test
        void reserveAmmoCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setReserveAmmo(200);
            assertEquals(200, player.getReserveAmmo());
        }
    }

    @Nested
    class CombatStateTests {
        @Test
        void reloadStateCanBeToggled() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            assertFalse(player.isReloading());
            player.setReloading(true);
            assertTrue(player.isReloading());
        }

        @Test
        void sprintStateCanBeToggled() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            assertFalse(player.isSprinting());
            player.setSprinting(true);
            assertTrue(player.isSprinting());
        }

        @Test
        void crouchStateCanBeToggled() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            assertFalse(player.isCrouching());
            player.setCrouching(true);
            assertTrue(player.isCrouching());
        }

        @Test
        void jumpStateCanBeToggled() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            assertFalse(player.isJumping());
            player.setJumping(true);
            assertTrue(player.isJumping());
        }
    }

    @Nested
    class StatsTests {
        @Test
        void addKillIncrementsKills() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.addKill();
            player.addKill();
            assertEquals(2, player.getKills());
        }

        @Test
        void addDeathIncrementsDeaths() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.addDeath();
            assertEquals(1, player.getDeaths());
        }

        @Test
        void assistsCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setAssists(5);
            assertEquals(5, player.getAssists());
        }

        @Test
        void killsAndDeathsCanBeSetDirectly() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setKills(10);
            player.setDeaths(3);
            assertEquals(10, player.getKills());
            assertEquals(3, player.getDeaths());
        }
    }

    @Nested
    class AimTests {
        @Test
        void pitchAndYawCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setPitch(0.5);
            player.setYaw(1.0);
            assertEquals(0.5, player.getPitch());
            assertEquals(1.0, player.getYaw());
        }
    }

    @Nested
    class RespawnTests {
        @Test
        void respawnRestoresHealthAndPosition() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setHealth(0);
            FpsWeapon weapon = new FpsWeapon("Pistol", FpsWeapon.WeaponType.PISTOL, 25, 4.0, 1.5, 15);
            player.respawn(10.0, 5.0, 20.0, weapon);
            assertTrue(player.isAlive());
            assertEquals(100, player.getHealth());
            assertEquals(10.0, player.getPosX());
            assertEquals(5.0, player.getPosY());
            assertEquals(20.0, player.getPosZ());
            assertEquals(15, player.getAmmo());
            assertFalse(player.isReloading());
        }

        @Test
        void respawnWithNullWeaponUsesDefaultAmmo() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setHealth(0);
            player.respawn(0, 0, 0, null);
            assertTrue(player.isAlive());
            assertEquals(30, player.getAmmo());
        }

        @Test
        void respawnResetsVelocityAndAim() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setVelocity(10, 10, 10);
            player.setPitch(1.0);
            player.setYaw(2.0);
            player.setHealth(0);
            player.respawn(0, 0, 0, null);
            assertEquals(0, player.getVelX());
            assertEquals(0, player.getVelY());
            assertEquals(0, player.getVelZ());
            assertEquals(0, player.getPitch());
            assertEquals(0, player.getYaw());
        }
    }

    @Nested
    class MiscTests {
        @Test
        void pingCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setPing(50);
            assertEquals(50, player.getPing());
        }

        @Test
        void lastSeenTickCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setLastSeenTick(100);
            assertEquals(100, player.getLastSeenTick());
        }

        @Test
        void groundYCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setGroundY(5.0);
            assertEquals(5.0, player.getGroundY());
        }

        @Test
        void weaponIdCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setCurrentWeaponId("rifle");
            assertEquals("rifle", player.getCurrentWeaponId());
        }

        @Test
        void fireTimeCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setLastFireTime(1.5);
            assertEquals(1.5, player.getLastFireTime());
        }

        @Test
        void reloadTimeCanBeSet() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setLastReloadTime(2.0);
            assertEquals(2.0, player.getLastReloadTime());
        }

        @Test
        void teamCanBeChanged() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setTeam(1);
            assertEquals(1, player.getTeam());
        }

        @Test
        void aliveCanBeSetDirectly() {
            FpsPlayerState player = new FpsPlayerState("p1", "Hero", 0);
            player.setAlive(false);
            assertFalse(player.isAlive());
            player.setAlive(true);
            assertTrue(player.isAlive());
        }
    }
}

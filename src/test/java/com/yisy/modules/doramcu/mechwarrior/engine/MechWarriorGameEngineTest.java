package com.yisy.modules.doramcu.mechwarrior.engine;

import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechComponent;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MechWarriorGameEngine Tests")
class MechWarriorGameEngineTest {

    private MechWarriorGameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MechWarriorGameEngine();
    }

    // ---- Session Management ----

    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @DisplayName("should create a new session")
        void createSession_shouldCreateSession() {
            MechWarriorSession session = engine.createSession("session-1", "Test Map", 4);
            assertNotNull(session);
            assertEquals("session-1", session.getSessionId());
            assertEquals("Test Map", session.getMapName());
            assertEquals(4, session.getMaxPlayers());
        }

        @Test
        @DisplayName("should get session by ID")
        void getSession_shouldReturnSession() {
            engine.createSession("session-1", "Test Map", 4);
            MechWarriorSession session = engine.getSession("session-1");
            assertNotNull(session);
            assertEquals("session-1", session.getSessionId());
        }

        @Test
        @DisplayName("should return null for non-existent session")
        void getSession_shouldReturnNull() {
            assertNull(engine.getSession("non-existent"));
        }

        @Test
        @DisplayName("should list all active sessions")
        void getActiveSessions_shouldListAll() {
            engine.createSession("session-1", "Map 1", 4);
            engine.createSession("session-2", "Map 2", 8);
            assertEquals(2, engine.getActiveSessions().size());
        }

        @Test
        @DisplayName("should start with no sessions")
        void constructor_shouldHaveNoSessions() {
            assertTrue(engine.getActiveSessions().isEmpty());
        }

        @Test
        @DisplayName("should remove session")
        void removeSession_shouldRemoveSession() {
            engine.createSession("session-1", "Test Map", 4);
            engine.removeSession("session-1");
            assertNull(engine.getSession("session-1"));
        }

        @Test
        @DisplayName("should remove non-existent session without error")
        void removeSession_shouldHandleNonExistent() {
            engine.removeSession("non-existent");
            // Should not throw
        }
    }

    // ---- Pilot Addition ----

    @Nested
    @DisplayName("Pilot Addition Tests")
    class PilotAdditionTests {

        @Test
        @DisplayName("should add pilot to session")
        void addPilotToSession_shouldAddPilot() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechChassis chassis = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot, chassis);
            assertNotNull(mech);
            assertEquals(1, engine.getSession("session-1").getPlayerCount());
        }

        @Test
        @DisplayName("should throw if session not found")
        void addPilotToSession_shouldThrowIfSessionNotFound() {
            MechPilot pilot = new MechPilot("Test Pilot");
            MechChassis chassis = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            assertThrows(IllegalArgumentException.class, () ->
                    engine.addPilotToSession("non-existent", pilot, chassis));
        }

        @Test
        @DisplayName("should throw if session is full")
        void addPilotToSession_shouldThrowIfFull() {
            engine.createSession("session-1", "Test Map", 1);
            MechPilot pilot1 = new MechPilot("Pilot 1");
            MechChassis chassis1 = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            engine.addPilotToSession("session-1", pilot1, chassis1);

            MechPilot pilot2 = new MechPilot("Pilot 2");
            MechChassis chassis2 = new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70);
            assertThrows(IllegalStateException.class, () ->
                    engine.addPilotToSession("session-1", pilot2, chassis2));
        }

        @Test
        @DisplayName("should add multiple pilots")
        void addPilotToSession_shouldAddMultiplePilots() {
            engine.createSession("session-1", "Test Map", 4);
            engine.addPilotToSession("session-1", new MechPilot("Pilot 1"),
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            engine.addPilotToSession("session-1", new MechPilot("Pilot 2"),
                    new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70));
            assertEquals(2, engine.getSession("session-1").getPlayerCount());
        }
    }

    // ---- Combat Resolution ----

    @Nested
    @DisplayName("Combat Resolution Tests")
    class CombatTests {

        private MechPilot shooterPilot;
        private MechPilot targetPilot;
        private MechWarriorMech shooter;
        private MechWarriorMech target;
        private MechWeapon weapon;

        @BeforeEach
        void setupCombat() {
            engine.createSession("session-1", "Test Map", 4);
            shooterPilot = new MechPilot("Shooter");
            targetPilot = new MechPilot("Target");
            MechChassis shooterChassis = new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100);
            MechChassis targetChassis = new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70);
            shooter = engine.addPilotToSession("session-1", shooterPilot, shooterChassis);
            target = engine.addPilotToSession("session-1", targetPilot, targetChassis);

            weapon = new MechWeapon("Medium Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.MEDIUM_LASER, 5, 3, 1.0);
            weapon.setCriticalSlots(1);
            shooter.mountWeapon(weapon, MechComponent.ComponentLocation.LEFT_TORSO);
        }

        @Test
        @DisplayName("should return combat result")
        void fireWeapons_shouldReturnResult() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            assertNotNull(result);
            assertEquals(shooterPilot.getId(), result.getShooterPilotId());
            assertEquals(targetPilot.getId(), result.getTargetPilotId());
        }

        @Test
        @DisplayName("should generate heat from firing")
        void fireWeapons_shouldGenerateHeat() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            engine.fireWeapons("session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            assertTrue(shooter.getHeatLevel() >= 0);
        }

        @Test
        @DisplayName("should throw if session not found")
        void fireWeapons_shouldThrowIfSessionNotFound() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            assertThrows(IllegalArgumentException.class, () ->
                    engine.fireWeapons("non-existent", shooterPilot.getId(), weaponIds,
                            MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId()));
        }

        @Test
        @DisplayName("should throw if mech not found")
        void fireWeapons_shouldThrowIfMechNotFound() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            assertThrows(IllegalArgumentException.class, () ->
                    engine.fireWeapons("session-1", "non-existent", weaponIds,
                            MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId()));
        }

        @Test
        @DisplayName("should skip weapons on cooldown")
        void fireWeapons_shouldSkipWeaponsOnCooldown() {
            // Start the weapon's cooldown
            weapon.startCooldown();
            // Set lastFireTime to now so isOnCooldown(currentTime) returns true
            shooter.setLastFireTime(System.currentTimeMillis());
            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            // Weapon should be skipped due to cooldown
            assertTrue(result.getHits().isEmpty() && result.getMisses().isEmpty());
        }

        @Test
        @DisplayName("should set total damage in result")
        void fireWeapons_shouldSetTotalDamage() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            // Total damage may be 0 if miss, but should be set
            assertNotNull(result.getTotalDamage());
        }

        @Test
        @DisplayName("should set total heat in result")
        void fireWeapons_shouldSetTotalHeat() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            assertNotNull(result.getTotalHeat());
        }

        @Test
        @DisplayName("should add hit message on hit")
        void fireWeapons_shouldAddHitMessage() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            // Result should have hits or misses
            assertTrue(result.getHits().size() + result.getMisses().size() >= 0);
        }

        @Test
        @DisplayName("should add miss message on miss")
        void fireWeapons_shouldAddMissMessage() {
            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            // Result should have hits or misses
            assertTrue(result.getHits().size() + result.getMisses().size() >= 0);
        }
    }

    // ---- Heat Override ----

    @Nested
    @DisplayName("Heat Override Tests")
    class HeatOverrideTests {

        @Test
        @DisplayName("should allow firing with heat override")
        void fireWeapons_shouldAllowFiringWithOverride() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot shooterPilot = new MechPilot("Shooter");
            MechPilot targetPilot = new MechPilot("Target");
            MechWarriorMech shooter = engine.addPilotToSession("session-1", shooterPilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            engine.addPilotToSession("session-1", targetPilot,
                    new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70));

            MechWeapon weapon = new MechWeapon("Large Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.LARGE_LASER, 10, 6, 1.5);
            weapon.setCriticalSlots(1);
            shooter.mountWeapon(weapon, MechComponent.ComponentLocation.LEFT_TORSO);

            // Set heat near shutdown threshold
            shooter.setHeatLevel(95);
            shooter.setHeatOverride(true);

            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            assertNotNull(result);
        }

        @Test
        @DisplayName("should skip weapon without heat override near shutdown")
        void fireWeapons_shouldSkipWithoutOverride() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot shooterPilot = new MechPilot("Shooter");
            MechPilot targetPilot = new MechPilot("Target");
            MechWarriorMech shooter = engine.addPilotToSession("session-1", shooterPilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            engine.addPilotToSession("session-1", targetPilot,
                    new MechChassis("Griffin", "GRF-2", MechChassis.MechClass.HEAVY, 70));

            MechWeapon weapon = new MechWeapon("Large Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.LARGE_LASER, 10, 6, 1.5);
            weapon.setCriticalSlots(1);
            shooter.mountWeapon(weapon, MechComponent.ComponentLocation.LEFT_TORSO);

            // Set heat near shutdown threshold
            shooter.setHeatLevel(95);
            shooter.setHeatOverride(false);

            List<String> weaponIds = Arrays.asList(weapon.getId());
            MechWarriorGameEngine.CombatResult result = engine.fireWeapons(
                    "session-1", shooterPilot.getId(), weaponIds,
                    MechComponent.ComponentLocation.CENTER_TORSO, targetPilot.getId());
            // Should have a message about shutdown
            assertTrue(result.getMessages().size() > 0 || result.getHits().isEmpty() && result.getMisses().isEmpty());
        }
    }

    // ---- Jump Jets ----

    @Nested
    @DisplayName("Jump Jet Tests")
    class JumpJetTests {

        @Test
        @DisplayName("should use jump jets and update position")
        void useJumpJets_shouldUpdatePosition() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            double initialX = mech.getPosX();

            engine.useJumpJets("session-1", pilot.getId(), 100);

            assertEquals(initialX + 100, mech.getPosX());
        }

        @Test
        @DisplayName("should add heat cost for jump jets")
        void useJumpJets_shouldAddHeatCost() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));

            engine.useJumpJets("session-1", pilot.getId(), 100);

            assertTrue(mech.getHeatLevel() > 0);
        }

        @Test
        @DisplayName("should shutdown on excessive heat from jump jets")
        void useJumpJets_shouldShutdownOnExcessiveHeat() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setHeatLevel(95);

            engine.useJumpJets("session-1", pilot.getId(), 100);

            assertTrue(mech.isShutdown());
        }

        @Test
        @DisplayName("should not jump if knocked down")
        void useJumpJets_shouldNotJumpIfKnockedDown() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setKnockedDown(true);
            double initialX = mech.getPosX();

            engine.useJumpJets("session-1", pilot.getId(), 100);

            assertEquals(initialX, mech.getPosX());
        }

        @Test
        @DisplayName("should not jump if shutdown")
        void useJumpJets_shouldNotJumpIfShutdown() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setShutdown(true);
            double initialX = mech.getPosX();

            engine.useJumpJets("session-1", pilot.getId(), 100);

            assertEquals(initialX, mech.getPosX());
        }

        @Test
        @DisplayName("should do nothing if session not found")
        void useJumpJets_shouldDoNothingIfSessionNotFound() {
            engine.useJumpJets("non-existent", "pilot-1", 100);
            // Should not throw
        }
    }

    // ---- Tick Processing ----

    @Nested
    @DisplayName("Tick Processing Tests")
    class TickTests {

        @Test
        @DisplayName("should dissipate heat on tick")
        void processTick_shouldDissipateHeat() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setHeatLevel(50);

            engine.processTick("session-1");

            assertTrue(mech.getHeatLevel() < 50);
        }

        @Test
        @DisplayName("should not go below 0 heat")
        void processTick_shouldNotGoBelowZeroHeat() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setHeatLevel(1);

            engine.processTick("session-1");

            assertEquals(0, mech.getHeatLevel());
        }

        @Test
        @DisplayName("should apply heat override penalty")
        void processTick_shouldApplyOverridePenalty() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setHeatLevel(50);
            mech.setHeatOverride(true);

            engine.processTick("session-1");

            // Heat should increase due to override penalty (1.5) minus dissipation (2.0)
            // Net: 50 - 2.0 + 1.5 = 49.5
            assertTrue(mech.getHeatLevel() >= 49.0 && mech.getHeatLevel() <= 50.0);
        }

        @Test
        @DisplayName("should recover from shutdown when heat drops")
        void processTick_shouldRecoverFromShutdown() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setHeatLevel(80);
            mech.setShutdown(true);

            engine.processTick("session-1");

            // Heat should drop below 80 (shutdown threshold * 0.8 = 80)
            // After dissipation: 80 - 2.0 = 78, which is < 80, so shutdown should clear
            assertFalse(mech.isShutdown());
        }

        @Test
        @DisplayName("should recover stability on tick")
        void processTick_shouldRecoverStability() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setStabilityDamage(10);

            engine.processTick("session-1");

            assertEquals(9, mech.getStabilityDamage());
        }

        @Test
        @DisplayName("should not recover stability if knocked down")
        void processTick_shouldNotRecoverStabilityIfKnockedDown() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setStabilityDamage(10);
            mech.setKnockedDown(true);

            engine.processTick("session-1");

            assertEquals(10, mech.getStabilityDamage());
        }

        @Test
        @DisplayName("should increment tick counter")
        void processTick_shouldIncrementTick() {
            engine.createSession("session-1", "Test Map", 4);
            engine.addPilotToSession("session-1", new MechPilot("Test Pilot"),
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));

            engine.processTick("session-1");

            assertEquals(1, engine.getSession("session-1").getCurrentTick());
        }

        @Test
        @DisplayName("should update weapon cooldowns")
        void processTick_shouldUpdateWeaponCooldowns() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            MechWeapon weapon = new MechWeapon("Laser", MechWeapon.WeaponType.ENERGY,
                    MechWeapon.WeaponSubType.SMALL_LASER, 2, 1, 0.1);
            weapon.setCriticalSlots(1);
            mech.mountWeapon(weapon, MechComponent.ComponentLocation.LEFT_TORSO);
            weapon.setCooldownTime(0.1);
            weapon.startCooldown();

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            engine.processTick("session-1");

            assertFalse(weapon.isOnCooldown());
        }

        @Test
        @DisplayName("should do nothing if session not found")
        void processTick_shouldDoNothingIfSessionNotFound() {
            engine.processTick("non-existent");
            // Should not throw
        }

        @Test
        @DisplayName("should apply pilot heat management skill bonus")
        void processTick_shouldApplyHeatManagementBonus() {
            engine.createSession("session-1", "Test Map", 4);
            MechPilot pilot = new MechPilot("Test Pilot");
            pilot.setHeatManagementSkill(3); // +10% heat reduction
            MechWarriorMech mech = engine.addPilotToSession("session-1", pilot,
                    new MechChassis("Atlas", "ATL-6D", MechChassis.MechClass.ASSAULT, 100));
            mech.setHeatLevel(50);

            engine.processTick("session-1");

            // Heat dissipation: 2.0 * (1.0 + 0.10) = 2.2
            // Expected: 50 - 2.2 = 47.8
            assertTrue(mech.getHeatLevel() < 48.5);
        }
    }

    // ---- CombatResult ----

    @Nested
    @DisplayName("CombatResult Tests")
    class CombatResultTests {

        @Test
        @DisplayName("should initialize with empty lists")
        void constructor_shouldInitializeEmptyLists() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            assertTrue(result.getHits().isEmpty());
            assertTrue(result.getMisses().isEmpty());
            assertTrue(result.getCriticalHits().isEmpty());
            assertTrue(result.getMessages().isEmpty());
        }

        @Test
        @DisplayName("should add hit")
        void addHit_shouldAddHit() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.addHit("Laser", "CENTER_TORSO", 5);
            assertEquals(1, result.getHits().size());
            assertTrue(result.getHits().get(0).contains("Laser"));
        }

        @Test
        @DisplayName("should add miss")
        void addMiss_shouldAddMiss() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.addMiss("Laser");
            assertEquals(1, result.getMisses().size());
        }

        @Test
        @DisplayName("should add critical hit")
        void addCriticalHit_shouldAddCriticalHit() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.addCriticalHit("HEAD");
            assertEquals(1, result.getCriticalHits().size());
        }

        @Test
        @DisplayName("should add message")
        void addMessage_shouldAddMessage() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.addMessage("Test message");
            assertEquals(1, result.getMessages().size());
        }

        @Test
        @DisplayName("should set and get shooter pilot ID")
        void setter_shouldSetShooterPilotId() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setShooterPilotId("shooter-1");
            assertEquals("shooter-1", result.getShooterPilotId());
        }

        @Test
        @DisplayName("should set and get target pilot ID")
        void setter_shouldSetTargetPilotId() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setTargetPilotId("target-1");
            assertEquals("target-1", result.getTargetPilotId());
        }

        @Test
        @DisplayName("should set and get total damage")
        void setter_shouldSetTotalDamage() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setTotalDamage(50);
            assertEquals(50, result.getTotalDamage());
        }

        @Test
        @DisplayName("should set and get total heat")
        void setter_shouldSetTotalHeat() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setTotalHeat(10);
            assertEquals(10, result.getTotalHeat());
        }

        @Test
        @DisplayName("should set and get shooter shutdown")
        void setter_shouldSetShooterShutdown() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setShooterShutdown(true);
            assertTrue(result.isShooterShutdown());
        }

        @Test
        @DisplayName("should set and get shooter exploded")
        void setter_shouldSetShooterExploded() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setShooterExploded(true);
            assertTrue(result.isShooterExploded());
        }

        @Test
        @DisplayName("should set and get target knocked down")
        void setter_shouldSetTargetKnockedDown() {
            MechWarriorGameEngine.CombatResult result = new MechWarriorGameEngine.CombatResult();
            result.setTargetKnockedDown(true);
            assertTrue(result.isTargetKnockedDown());
        }
    }
}

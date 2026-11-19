package com.yisy.modules.doramcu.fullthrust.service;

import com.yisy.modules.doramcu.fullthrust.model.GameSession;
import com.yisy.modules.doramcu.fullthrust.model.Ship;
import com.yisy.modules.doramcu.fullthrust.model.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the fire buttons feature — weapon arc and range validation,
 * weapon state checks, and damage application via GameService.fireWeapon().
 */
class FireButtonsTest {

    private GameService gameService;

    private GameService createGameService() {
        PersistenceService persistenceService = mock(PersistenceService.class);
        AIService aiService = mock(AIService.class);
        PlayerAccountService playerAccountService = mock(PlayerAccountService.class);
        return new GameService(persistenceService, aiService, playerAccountService);
    }

    private String sessionId;
    private String player1Id;
    private String player2Id;
    private String ship1Id;
    private String ship2Id;

    /**
     * Set up a game with two players, each with a ship, ready for firing.
     */
    @BeforeEach
    void setUp() {
        gameService = createGameService();
        // Create game
        GameSession session = gameService.createGame("Fire Test");
        sessionId = session.getId();

        // Add two players
        gameService.addPlayer(sessionId, "Player1", "Federation");
        player1Id = session.getPlayers().keySet().stream().findFirst().orElseThrow();

        gameService.addPlayer(sessionId, "Player2", "Empire");
        List<String> playerIds = List.copyOf(session.getPlayers().keySet());
        player2Id = playerIds.get(1);

        // Deploy ships — scout has 2 lasers (range 6)
        // Ship1 at (0,0), Ship2 at (3,0) — distance 3, within laser range
        gameService.deployShip(sessionId, player1Id, "scout", "Ship1", 0, 0);
        gameService.deployShip(sessionId, player2Id, "scout", "Ship2", 3, 0);

        // Get ship IDs
        ship1Id = session.getShips().keySet().stream().findFirst().orElseThrow();
        List<String> shipIds = List.copyOf(session.getShips().keySet());
        ship2Id = shipIds.get(1);

        // Start the game
        gameService.startGame(sessionId);
    }

    @Test
    void fireWeapon_laserDealsDamage() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();
        Ship attacker = session.getShips().get(ship1Id);
        Ship target = session.getShips().get(ship2Id);

        int targetHullBefore = target.getCurrentHull();
        int targetShieldBefore = target.getCurrentShield();

        // Fire weapon 0 (forward laser) at target
        // Ship1 heading 0 (East), Ship2 at (3,0) — direction East, in forward arc
        attacker.setHeading(0);

        gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0);

        // Target should have taken damage
        GameSession after = gameService.getSession(sessionId).orElseThrow();
        Ship targetAfter = after.getShips().get(ship2Id);
        int totalDamageTaken = (targetHullBefore - targetAfter.getCurrentHull())
                + (targetShieldBefore - targetAfter.getCurrentShield());
        assertTrue(totalDamageTaken > 0, "Target should have taken damage");
    }

    @Test
    void fireWeapon_targetOutOfRange_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Move target far away — beyond laser range (6)
        Ship target = session.getShips().get(ship2Id);
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(10, 0));

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when target is out of range");
    }

    @Test
    void fireWeapon_targetInRange_boundary() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Place target exactly at laser range (6 hexes away)
        Ship target = session.getShips().get(ship2Id);
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(6, 0));

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        // Should not throw — target is exactly at range boundary
        assertDoesNotThrow(() ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should allow firing at exact range boundary");
    }

    @Test
    void fireWeapon_targetJustBeyondRange_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Place target just beyond laser range (7 hexes away, laser range = 6)
        Ship target = session.getShips().get(ship2Id);
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(7, 0));

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when target is just beyond range");
    }

    @Test
    void fireWeapon_targetNotInArc_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Place target behind the attacker — not in forward arc
        Ship target = session.getShips().get(ship2Id);
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(-3, 0));

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0); // Facing East, target is West

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when target is not in weapon's firing arc");
    }

    @Test
    void fireWeapon_targetInCorrectArc_succeeds() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Place target in front of attacker
        Ship target = session.getShips().get(ship2Id);
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(3, 0));

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0); // Facing East, target is East

        assertDoesNotThrow(() ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should allow firing when target is in correct arc");
    }

    @Test
    void fireWeapon_disabledWeapon_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);
        attacker.getWeapons().get(0).setDisabled(true);

        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when weapon is disabled");
    }

    @Test
    void fireWeapon_reloadingWeapon_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);
        // Simulate weapon reloading
        attacker.getWeapons().get(0).setCurrentReload(2);

        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when weapon is reloading");
    }

    @Test
    void fireWeapon_weaponStartsReloadingAfterFire() {
        // Use a fighter which has a missile (reloadTime=2) — lasers have reloadTime=0
        GameSession session = gameService.createGame("Reload Test");
        String rlId = session.getId();

        gameService.addPlayer(rlId, "P1", "Federation");
        gameService.addPlayer(rlId, "P2", "Empire");
        List<String> pIds = List.copyOf(session.getPlayers().keySet());

        gameService.deployShip(rlId, pIds.get(0), "fighter", "Fighter1", 0, 0);
        gameService.deployShip(rlId, pIds.get(1), "scout", "Scout1", 3, 0);
        List<String> sIds = List.copyOf(session.getShips().keySet());

        gameService.startGame(rlId);

        Ship attacker = session.getShips().get(sIds.get(0));
        attacker.setHeading(0);

        // Missile at index 2 has reloadTime=2
        Weapon weapon = attacker.getWeapons().get(2);
        assertEquals("missile", weapon.getType());
        assertFalse(weapon.isReloading(), "Weapon should not be reloading before fire");

        gameService.fireWeapon(rlId, sIds.get(0), sIds.get(1), 2);

        GameSession after = gameService.getSession(rlId).orElseThrow();
        Weapon weaponAfter = after.getShips().get(sIds.get(0)).getWeapons().get(2);
        assertTrue(weaponAfter.isReloading(), "Weapon should be reloading after fire");
    }

    @Test
    void fireWeapon_friendlyFire_throwsException() {
        // Create a fresh game and deploy two ships for player1 before starting
        GameSession session = gameService.createGame("Friendly Fire Test");
        String ffId = session.getId();

        gameService.addPlayer(ffId, "P1", "Federation");
        gameService.addPlayer(ffId, "P2", "Empire");
        List<String> pIds = List.copyOf(session.getPlayers().keySet());

        // Deploy two ships for player1
        gameService.deployShip(ffId, pIds.get(0), "scout", "ShipA", 0, 0);
        gameService.deployShip(ffId, pIds.get(0), "scout", "ShipB", 2, 0);
        gameService.deployShip(ffId, pIds.get(1), "scout", "ShipC", 5, 0);
        List<String> sIds = List.copyOf(session.getShips().keySet());

        gameService.startGame(ffId);

        Ship attacker = session.getShips().get(sIds.get(0));
        attacker.setHeading(0);

        // ShipA and ShipB belong to the same player — friendly fire should be blocked
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(ffId, sIds.get(0), sIds.get(1), 0),
                "Should throw when firing at friendly ship");
    }

    @Test
    void fireWeapon_destroyedAttacker_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setDestroyed(true);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when attacker is destroyed");
    }

    @Test
    void fireWeapon_destroyedTarget_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship target = session.getShips().get(ship2Id);
        target.setDestroyed(true);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when target is destroyed");
    }

    @Test
    void fireWeapon_invalidWeaponIndex_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 99),
                "Should throw for invalid weapon index");
    }

    @Test
    void fireWeapon_negativeWeaponIndex_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, -1),
                "Should throw for negative weapon index");
    }

    @Test
    void fireWeapon_notYourTurn_throwsException() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Set current player to player2, but try to fire player1's ship
        session.setCurrentPlayerId(player2Id);

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Should throw when it's not the player's turn");
    }

    @Test
    void fireWeapon_gameNotStarted_throwsException() {
        // Create a new game but don't start it
        GameSession session = gameService.createGame("Not Started");
        String notStartedId = session.getId();

        gameService.addPlayer(notStartedId, "P1", "Federation");
        gameService.addPlayer(notStartedId, "P2", "Empire");
        List<String> pIds = List.copyOf(session.getPlayers().keySet());

        gameService.deployShip(notStartedId, pIds.get(0), "scout", "S1", 0, 0);
        gameService.deployShip(notStartedId, pIds.get(1), "scout", "S2", 3, 0);
        List<String> sIds = List.copyOf(session.getShips().keySet());

        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon(notStartedId, sIds.get(0), sIds.get(1), 0),
                "Should throw when game is not started");
    }

    @Test
    void fireWeapon_missileCreatesMissile() {
        // Use a fighter which has a missile
        GameSession session = gameService.createGame("Missile Test");
        String msId = session.getId();

        gameService.addPlayer(msId, "P1", "Federation");
        gameService.addPlayer(msId, "P2", "Empire");
        List<String> pIds = List.copyOf(session.getPlayers().keySet());

        gameService.deployShip(msId, pIds.get(0), "fighter", "Fighter1", 0, 0);
        gameService.deployShip(msId, pIds.get(1), "scout", "Scout1", 3, 0);
        List<String> sIds = List.copyOf(session.getShips().keySet());

        gameService.startGame(msId);

        Ship attacker = session.getShips().get(sIds.get(0));
        attacker.setHeading(0);

        // Fighter has 2 lasers + 1 missile at index 2
        assertEquals("missile", attacker.getWeapons().get(2).getType());

        int missilesBefore = session.getActiveMissiles().size();
        gameService.fireWeapon(msId, sIds.get(0), sIds.get(1), 2);

        GameSession after = gameService.getSession(msId).orElseThrow();
        assertTrue(after.getActiveMissiles().size() > missilesBefore,
                "Missile should be created when firing missile weapon");
    }

    @Test
    void fireWeapon_laserDoesNotCreateMissile() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        int missilesBefore = session.getActiveMissiles().size();
        gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0);

        GameSession after = gameService.getSession(sessionId).orElseThrow();
        assertEquals(missilesBefore, after.getActiveMissiles().size(),
                "Laser should not create a missile object");
    }

    @Test
    void fireWeapon_shipCanDestroyTarget() {
        // Set up a scenario where target has very low hull
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship target = session.getShips().get(ship2Id);
        target.setCurrentShield(0);
        target.setCurrentHull(1); // Very low hull

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0);

        GameSession after = gameService.getSession(sessionId).orElseThrow();
        Ship targetAfter = after.getShips().get(ship2Id);
        // Target may or may not be destroyed depending on damage roll,
        // but hull should be reduced
        assertTrue(targetAfter.getCurrentHull() < target.getCurrentHull()
                || targetAfter.isDestroyed(),
                "Target hull should be reduced or destroyed");
    }

    @Test
    void fireWeapon_logsEvent() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        int logSizeBefore = session.getEventLog().size();
        gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0);

        GameSession after = gameService.getSession(sessionId).orElseThrow();
        assertTrue(after.getEventLog().size() > logSizeBefore,
                "Firing should log an event");

        // Check that the log contains fire-related info
        String lastEvent = after.getEventLog().get(after.getEventLog().size() - 1);
        assertTrue(lastEvent.toLowerCase().contains("fired")
                || lastEvent.toLowerCase().contains("launched"),
                "Event log should mention firing or launching");
    }

    @Test
    void fireWeapon_nonExistentSession_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                gameService.fireWeapon("non-existent", ship1Id, ship2Id, 0),
                "Should throw for non-existent session");
    }

    @Test
    void fireWeapon_nonExistentShip_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, "non-existent", ship2Id, 0),
                "Should throw for non-existent ship");
    }

    @Test
    void fireWeapon_nonExistentTarget_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, "non-existent", 0),
                "Should throw for non-existent target");
    }

    @Test
    void fireWeapon_missileRangeAllowsLongerDistance() {
        // Missile has range 12, laser has range 6
        // Place target at distance 8 — beyond laser range but within missile range
        GameSession session = gameService.createGame("Missile Range Test");
        String msId = session.getId();

        gameService.addPlayer(msId, "P1", "Federation");
        gameService.addPlayer(msId, "P2", "Empire");
        List<String> pIds = List.copyOf(session.getPlayers().keySet());

        gameService.deployShip(msId, pIds.get(0), "fighter", "Fighter1", 0, 0);
        gameService.deployShip(msId, pIds.get(1), "scout", "Scout1", 8, 0);
        List<String> sIds = List.copyOf(session.getShips().keySet());

        gameService.startGame(msId);

        Ship attacker = session.getShips().get(sIds.get(0));
        attacker.setHeading(0);

        // Laser (index 0, range 6) should fail at distance 8
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(msId, sIds.get(0), sIds.get(1), 0),
                "Laser should not reach target at distance 8");

        // Missile (index 2, range 12) should succeed at distance 8
        assertDoesNotThrow(() ->
                gameService.fireWeapon(msId, sIds.get(0), sIds.get(1), 2),
                "Missile should reach target at distance 8");
    }

    @Test
    void fireWeapon_torpedoRangeTest() {
        // Torpedo has range 8
        GameSession session = gameService.createGame("Torpedo Range Test");
        String msId = session.getId();

        gameService.addPlayer(msId, "P1", "Federation");
        gameService.addPlayer(msId, "P2", "Empire");
        List<String> pIds = List.copyOf(session.getPlayers().keySet());

        gameService.deployShip(msId, pIds.get(0), "destroyer", "Destroyer1", 0, 0);
        gameService.deployShip(msId, pIds.get(1), "scout", "Scout1", 8, 0);
        List<String> sIds = List.copyOf(session.getShips().keySet());

        gameService.startGame(msId);

        Ship attacker = session.getShips().get(sIds.get(0));
        attacker.setHeading(0);

        // Destroyer has: laser(arc 0), missile(arc 2), torpedo(arc 1)
        // Torpedo at index 2, arc 1 (Front-Right), range 8
        // Target at (8,0) is direction East (0), relative to heading 0
        // Arc diff = |0 - 1| = 1, which is <= 1, so it's in arc
        assertDoesNotThrow(() ->
                gameService.fireWeapon(msId, sIds.get(0), sIds.get(1), 2),
                "Torpedo should reach target at distance 8");

        // Now test torpedo out of range — use laser (arc 0, range 6) for a clean test
        // Place target at distance 7 — beyond laser range (6)
        Ship target = session.getShips().get(sIds.get(1));
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(7, 0));

        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(msId, sIds.get(0), sIds.get(1), 0),
                "Laser should not reach target at distance 7");
    }

    @Test
    void fireWeapon_aftWeaponFiresAtRearTarget() {
        // Scout has forward laser (arc 0) and aft laser (arc 3)
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        // Place target behind attacker
        Ship target = session.getShips().get(ship2Id);
        target.setPosition(new com.yisy.modules.doramcu.fullthrust.model.HexPosition(-3, 0));

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0); // Facing East, target is West (behind)

        // Forward weapon (index 0, arc 0) should NOT reach target behind
        assertThrows(IllegalArgumentException.class, () ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0),
                "Forward weapon should not fire at target behind");

        // Aft weapon (index 1, arc 3) should reach target behind
        assertDoesNotThrow(() ->
                gameService.fireWeapon(sessionId, ship1Id, ship2Id, 1),
                "Aft weapon should fire at target behind");
    }

    @Test
    void fireWeapon_shieldAbsorbsDamageFirst() {
        GameSession session = gameService.getSession(sessionId).orElseThrow();

        Ship target = session.getShips().get(ship2Id);
        int shieldBefore = target.getCurrentShield();
        int hullBefore = target.getCurrentHull();

        Ship attacker = session.getShips().get(ship1Id);
        attacker.setHeading(0);

        gameService.fireWeapon(sessionId, ship1Id, ship2Id, 0);

        GameSession after = gameService.getSession(sessionId).orElseThrow();
        Ship targetAfter = after.getShips().get(ship2Id);

        int shieldLost = shieldBefore - targetAfter.getCurrentShield();
        int hullLost = hullBefore - targetAfter.getCurrentHull();

        // If shields were active, they should absorb damage first
        if (shieldBefore > 0) {
            assertTrue(shieldLost > 0 || hullLost > 0,
                    "Some damage should be dealt");
            // Hull should not take damage until shields are depleted
            if (targetAfter.getCurrentShield() > 0) {
                assertEquals(0, hullLost,
                        "Hull should not take damage while shields are active");
            }
        }
    }
}

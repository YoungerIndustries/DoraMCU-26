package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.SfbHexPosition;
import com.yisy.modules.doramcu.sfb.model.SfbShip;
import com.yisy.modules.doramcu.sfb.service.ImpulseManager;
import com.yisy.modules.doramcu.sfb.service.MovementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MovementService Tests")
class MovementServiceTest {

    private MovementService createMovementService() {
        return new MovementService();
    }

    private SfbShip createShip(int speed) {
        SfbShip ship = new SfbShip();
        ship.setId("test-ship");
        ship.setName("Test Ship");
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setSpeed(speed);
        ship.setSide(0);
        return ship;
    }

    // --- ImpulseManager Tests ---

    @Test
    @DisplayName("Speed 32 moves on 31 impulses")
    void speed32Moves31Impulses() {
        ImpulseManager manager = new ImpulseManager();
        assertEquals(31, manager.getMovesPerTurn(32), "Speed 32 should move on 31 impulses");
        assertEquals(31, manager.getHexesPerTurn(32), "Speed 32 should move 31 hexes per turn");
    }

    @Test
    @DisplayName("Speed 16 moves on 15 impulses")
    void speed16Moves15Impulses() {
        ImpulseManager manager = new ImpulseManager();
        assertEquals(15, manager.getMovesPerTurn(16), "Speed 16 should move on 15 impulses");
        assertEquals(15, manager.getHexesPerTurn(16), "Speed 16 should move 15 hexes per turn");
    }

    @Test
    @DisplayName("Speed 8 moves on 4 impulses")
    void speed8Moves4Impulses() {
        ImpulseManager manager = new ImpulseManager();
        assertEquals(4, manager.getMovesPerTurn(8), "Speed 8 should move on 4 impulses");
        assertEquals(4, manager.getHexesPerTurn(8), "Speed 8 should move 4 hexes per turn");
    }

    @Test
    @DisplayName("Speed 4 moves on 2 impulses")
    void speed4Moves2Impulses() {
        ImpulseManager manager = new ImpulseManager();
        assertEquals(2, manager.getMovesPerTurn(4), "Speed 4 should move on 2 impulses");
        assertEquals(2, manager.getHexesPerTurn(4), "Speed 4 should move 2 hexes per turn");
    }

    @Test
    @DisplayName("Speed 1 moves on 1 impulse (impulse 32)")
    void speed1Moves1Impulse() {
        ImpulseManager manager = new ImpulseManager();
        assertEquals(1, manager.getMovesPerTurn(1), "Speed 1 should move on 1 impulse");
        assertTrue(manager.movesOnImpulse(1, 32), "Speed 1 should move on impulse 32");
        assertFalse(manager.movesOnImpulse(1, 1), "Speed 1 should not move on impulse 1");
    }

    @Test
    @DisplayName("Speed 32 does not move on impulse 1")
    void speed32DoesNotMoveOnImpulse1() {
        ImpulseManager manager = new ImpulseManager();
        assertFalse(manager.movesOnImpulse(32, 1), "Speed 32 should not move on impulse 1");
        assertTrue(manager.movesOnImpulse(32, 2), "Speed 32 should move on impulse 2");
    }

    @Test
    @DisplayName("All speeds move on impulse 32")
    void allSpeedsMoveOnImpulse32() {
        ImpulseManager manager = new ImpulseManager();
        for (int speed = 1; speed <= 32; speed++) {
            assertTrue(manager.movesOnImpulse(speed, 32), 
                    "Speed " + speed + " should move on impulse 32");
        }
    }

    @Test
    @DisplayName("Speed 16 moves on correct impulses from rules")
    void speed16ImpulseMapping() {
        ImpulseManager manager = new ImpulseManager();
        // From movement.json: speed 16 moves on 4,6,8,10,12,14,16,18,20,22,24,26,28,30,32
        assertTrue(manager.movesOnImpulse(16, 4));
        assertTrue(manager.movesOnImpulse(16, 6));
        assertTrue(manager.movesOnImpulse(16, 8));
        assertTrue(manager.movesOnImpulse(16, 32));
        assertFalse(manager.movesOnImpulse(16, 1));
        assertFalse(manager.movesOnImpulse(16, 3));
    }

    @Test
    @DisplayName("Speed 8 moves on impulses 8, 16, 24, 32")
    void speed8ImpulseMapping() {
        ImpulseManager manager = new ImpulseManager();
        assertTrue(manager.movesOnImpulse(8, 8));
        assertTrue(manager.movesOnImpulse(8, 16));
        assertTrue(manager.movesOnImpulse(8, 24));
        assertTrue(manager.movesOnImpulse(8, 32));
        assertFalse(manager.movesOnImpulse(8, 4));
        assertFalse(manager.movesOnImpulse(8, 12));
    }

    // --- MovementService Tests ---

    @Test
    @DisplayName("moveShip moves ship on correct impulse for speed 16")
    void moveShipSpeed16() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(16);
        ship.setPosition(new SfbHexPosition(0, 0, 0));

        // Speed 16 moves on impulse 4, 6, 8, ...
        assertFalse(service.moveShip(ship, 1), "Should not move on impulse 1");
        assertFalse(service.moveShip(ship, 3), "Should not move on impulse 3");
        assertTrue(service.moveShip(ship, 4), "Should move on impulse 4");
        assertEquals(1, ship.getHexesMovedThisTurn(), "Should have moved 1 hex");
    }

    @Test
    @DisplayName("moveShip moves ship on correct impulse for speed 32")
    void moveShipSpeed32() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(32);
        ship.setPosition(new SfbHexPosition(0, 0, 0));

        // Speed 32 moves on every impulse except 1
        assertFalse(service.moveShip(ship, 1), "Should not move on impulse 1");
        assertTrue(service.moveShip(ship, 2), "Should move on impulse 2");
        assertTrue(service.moveShip(ship, 3), "Should move on impulse 3");
        assertEquals(2, ship.getHexesMovedThisTurn(), "Should have moved 2 hexes");
    }

    @Test
    @DisplayName("moveShip moves ship on correct impulse for speed 8")
    void moveShipSpeed8() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(8);
        ship.setPosition(new SfbHexPosition(0, 0, 0));

        // Speed 8 moves on impulses 8, 16, 24, 32
        assertFalse(service.moveShip(ship, 4), "Should not move on impulse 4");
        assertTrue(service.moveShip(ship, 8), "Should move on impulse 8");
        assertFalse(service.moveShip(ship, 12), "Should not move on impulse 12");
        assertTrue(service.moveShip(ship, 16), "Should move on impulse 16");
        assertEquals(2, ship.getHexesMovedThisTurn(), "Should have moved 2 hexes");
    }

    @Test
    @DisplayName("Faster ship moves more hexes than slower ship in same turn")
    void fasterShipMovesMoreHexes() {
        MovementService service = createMovementService();
        SfbShip fastShip = createShip(32);
        fastShip.setPosition(new SfbHexPosition(0, 0, 0));
        SfbShip slowShip = createShip(8);
        slowShip.setPosition(new SfbHexPosition(0, 0, 0));

        // Simulate a full turn
        for (int impulse = 1; impulse <= 32; impulse++) {
            service.moveShip(fastShip, impulse);
            service.moveShip(slowShip, impulse);
        }

        assertTrue(fastShip.getHexesMovedThisTurn() > slowShip.getHexesMovedThisTurn(),
                "Speed 32 should move more hexes than speed 8");
        assertEquals(31, fastShip.getHexesMovedThisTurn(), "Speed 32 should move 31 hexes");
        assertEquals(4, slowShip.getHexesMovedThisTurn(), "Speed 8 should move 4 hexes");
    }

    @Test
    @DisplayName("Ship position changes correctly with movement")
    void shipPositionChanges() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(32);
        ship.setPosition(new SfbHexPosition(0, 0, 0)); // facing 0 = right

        service.moveShip(ship, 2);
        service.moveShip(ship, 3);
        service.moveShip(ship, 4);

        SfbHexPosition pos = ship.getPosition();
        assertEquals(3, pos.getQ(), "Q should increase by 3 for 3 moves right");
        assertEquals(0, pos.getR(), "R should stay 0 for straight movement");
    }

    @Test
    @DisplayName("Crippled ship does not move")
    void crippledShipDoesNotMove() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(32);
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setCrippled(true);

        assertFalse(service.moveShip(ship, 2), "Crippled ship should not move");
        assertEquals(0, ship.getHexesMovedThisTurn());
    }

    @Test
    @DisplayName("Destroyed ship does not move")
    void destroyedShipDoesNotMove() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(32);
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setDestroyed(true);

        assertFalse(service.moveShip(ship, 2), "Destroyed ship should not move");
        assertEquals(0, ship.getHexesMovedThisTurn());
    }

    @Test
    @DisplayName("Ship without position does not move")
    void shipWithoutPositionDoesNotMove() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(32);
        ship.setPosition(null); // Remove position

        assertFalse(service.moveShip(ship, 2), "Ship without position should not move");
    }

    // --- Acceleration Tests ---

    @Test
    @DisplayName("Acceleration limited to 4 points per turn")
    void accelerationLimited() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(16);
        ship.resetMovementTracking(); // previousSpeed = 16

        int newSpeed = service.setSpeedWithAcceleration(ship, 32);
        assertEquals(20, newSpeed, "Speed 16 -> 32 should be clamped to 20 (16+4)");
    }

    @Test
    @DisplayName("Deceleration limited to 4 points per turn")
    void decelerationLimited() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(32);
        ship.resetMovementTracking(); // previousSpeed = 32

        int newSpeed = service.setSpeedWithAcceleration(ship, 1);
        assertEquals(28, newSpeed, "Speed 32 -> 1 should be clamped to 28 (32-4)");
    }

    @Test
    @DisplayName("Small speed change allowed")
    void smallSpeedChangeAllowed() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(16);
        ship.resetMovementTracking(); // previousSpeed = 16

        int newSpeed = service.setSpeedWithAcceleration(ship, 20);
        assertEquals(20, newSpeed, "Speed 16 -> 20 should be allowed (within 4 points)");
    }

    @Test
    @DisplayName("Max speed change is 4")
    void maxSpeedChangeIs4() {
        ImpulseManager manager = new ImpulseManager();
        assertEquals(4, manager.getMaxSpeedChange());
    }

    // --- Turn Mode Tests ---

    @Test
    @DisplayName("Turn mode 1 gives full movement")
    void turnMode1FullMovement() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(16);
        ship.setTurnMode(1);

        int maxHexes = service.getMaxMoveHexes(ship);
        assertEquals(15, maxHexes, "Turn mode 1 should give full movement (15 hexes for speed 16)");
    }

    @Test
    @DisplayName("Turn mode 2 reduces movement")
    void turnMode2ReducedMovement() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(16);
        ship.setTurnMode(2);

        int maxHexes = service.getMaxMoveHexes(ship);
        assertEquals(7, maxHexes, "Turn mode 2 should halve movement (7 hexes for speed 16)");
    }

    @Test
    @DisplayName("Turn mode 3 gives minimal movement")
    void turnMode3MinimalMovement() {
        MovementService service = createMovementService();
        SfbShip ship = createShip(16);
        ship.setTurnMode(3);

        int maxHexes = service.getMaxMoveHexes(ship);
        assertEquals(1, maxHexes, "Turn mode 3 should give 1 hex movement");
    }

    // --- Turning Allowance Tests ---

    @Test
    @DisplayName("Turning allowance depends on speed and turn mode")
    void turningAllowance() {
        MovementService service = createMovementService();
        
        SfbShip fastShip = createShip(24);
        fastShip.setTurnMode(1);
        assertEquals(1, service.getTurningAllowance(fastShip), "Speed 24, mode 1: 1 direction");

        SfbShip mediumShip = createShip(16);
        mediumShip.setTurnMode(1);
        assertEquals(2, service.getTurningAllowance(mediumShip), "Speed 16, mode 1: 2 directions");

        SfbShip slowShip = createShip(8);
        slowShip.setTurnMode(1);
        assertEquals(3, service.getTurningAllowance(slowShip), "Speed 8, mode 1: 3 directions");
    }

    // --- HET Tests ---

    @Test
    @DisplayName("HET requires speed 16+")
    void hetRequiresSpeed16() {
        MovementService service = createMovementService();
        
        SfbShip fastShip = createShip(16);
        assertTrue(service.canPerformHET(fastShip), "Speed 16 should allow HET");

        SfbShip slowShip = createShip(8);
        assertFalse(service.canPerformHET(slowShip), "Speed 8 should not allow HET");
    }

    // --- Distance and Range Band Tests ---

    @Test
    @DisplayName("Distance between ships is calculated correctly")
    void distanceBetweenShips() {
        MovementService service = createMovementService();
        
        SfbShip ship1 = createShip(16);
        ship1.setPosition(new SfbHexPosition(0, 0, 0));
        
        SfbShip ship2 = createShip(16);
        ship2.setPosition(new SfbHexPosition(5, 0, 0));

        assertEquals(5, service.distanceBetween(ship1, ship2));
    }

    @Test
    @DisplayName("Range band calculation matches SFB rules")
    void rangeBandCalculation() {
        MovementService service = createMovementService();
        
        SfbShip ship1 = createShip(16);
        ship1.setPosition(new SfbHexPosition(0, 0, 0));
        
        SfbShip ship2 = createShip(16);
        
        // Point blank (same hex)
        ship2.setPosition(new SfbHexPosition(0, 0, 0));
        assertEquals(0, service.getRangeBand(ship1, ship2), "Same hex = point blank");

        // Range band 1 (1-2 hexes)
        ship2.setPosition(new SfbHexPosition(2, 0, 0));
        assertEquals(1, service.getRangeBand(ship1, ship2), "2 hexes = range band 1");

        // Range band 2 (3-5 hexes)
        ship2.setPosition(new SfbHexPosition(5, 0, 0));
        assertEquals(2, service.getRangeBand(ship1, ship2), "5 hexes = range band 2");

        // Range band 3 (6-8 hexes)
        ship2.setPosition(new SfbHexPosition(8, 0, 0));
        assertEquals(3, service.getRangeBand(ship1, ship2), "8 hexes = range band 3");

        // Range band 4 (9-12 hexes)
        ship2.setPosition(new SfbHexPosition(12, 0, 0));
        assertEquals(4, service.getRangeBand(ship1, ship2), "12 hexes = range band 4");

        // Range band 5 (13-15 hexes)
        ship2.setPosition(new SfbHexPosition(15, 0, 0));
        assertEquals(5, service.getRangeBand(ship1, ship2), "15 hexes = range band 5");

        // Range band 6 (16+ hexes)
        ship2.setPosition(new SfbHexPosition(20, 0, 0));
        assertEquals(6, service.getRangeBand(ship1, ship2), "20 hexes = range band 6");
    }

    // --- Movement Tracking Tests ---

    @Test
    @DisplayName("resetMovementTracking saves speed and clears hexes")
    void resetMovementTracking() {
        SfbShip ship = createShip(20);
        ship.setHexesMovedThisTurn(10);
        
        ship.resetMovementTracking();
        
        assertEquals(20, ship.getPreviousSpeed(), "Previous speed should be saved");
        assertEquals(0, ship.getHexesMovedThisTurn(), "Hexes moved should be reset");
    }

    @Test
    @DisplayName("recordHexMoved increments hexes moved")
    void recordHexMoved() {
        SfbShip ship = createShip(16);
        ship.setHexesMovedThisTurn(0);
        
        ship.recordHexMoved();
        assertEquals(1, ship.getHexesMovedThisTurn());
        
        ship.recordHexMoved();
        assertEquals(2, ship.getHexesMovedThisTurn());
    }

    @Test
    @DisplayName("Hexes per turn matches speed expectations")
    void hexesPerTurnMatchesSpeed() {
        MovementService service = createMovementService();
        
        SfbShip fastShip = createShip(32);
        assertEquals(31, service.getHexesPerTurn(fastShip));
        
        SfbShip mediumShip = createShip(16);
        assertEquals(15, service.getHexesPerTurn(mediumShip));
        
        SfbShip slowShip = createShip(8);
        assertEquals(4, service.getHexesPerTurn(slowShip));
        
        SfbShip verySlowShip = createShip(1);
        assertEquals(1, service.getHexesPerTurn(verySlowShip));
    }
}

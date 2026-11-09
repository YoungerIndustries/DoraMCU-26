package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbHexPosition Tests")
class SfbHexPositionTest {

    @Test
    @DisplayName("Constructor sets initial values")
    void constructorSetsInitialValues() {
        SfbHexPosition pos = new SfbHexPosition(5, 3, 2);
        assertEquals(5, pos.getQ());
        assertEquals(3, pos.getR());
        assertEquals(2, pos.getFacing());
    }

    @Test
    @DisplayName("Default constructor initializes to zero")
    void defaultConstructorInitializesToZero() {
        SfbHexPosition pos = new SfbHexPosition();
        assertEquals(0, pos.getQ());
        assertEquals(0, pos.getR());
        assertEquals(0, pos.getFacing());
    }

    @Test
    @DisplayName("Facing wraps around at 6")
    void facingWrapsAround() {
        SfbHexPosition pos = new SfbHexPosition(0, 0, 8);
        assertEquals(2, pos.getFacing());
    }

    @Test
    @DisplayName("Distance calculates correctly")
    void distanceCalculatesCorrectly() {
        SfbHexPosition pos1 = new SfbHexPosition(0, 0, 0);
        SfbHexPosition pos2 = new SfbHexPosition(3, 0, 0);
        assertEquals(3, pos1.distanceTo(pos2));
    }

    @Test
    @DisplayName("Distance is symmetric")
    void distanceIsSymmetric() {
        SfbHexPosition pos1 = new SfbHexPosition(2, 3, 0);
        SfbHexPosition pos2 = new SfbHexPosition(5, 1, 0);
        assertEquals(pos1.distanceTo(pos2), pos2.distanceTo(pos1));
    }

    @Test
    @DisplayName("Move in facing direction")
    void moveInFacingDirection() {
        SfbHexPosition pos = new SfbHexPosition(0, 0, 0); // facing Right
        pos.move(3);
        assertEquals(3, pos.getQ());
        assertEquals(0, pos.getR());
    }

    @Test
    @DisplayName("Turn changes facing")
    void turnChangesFacing() {
        SfbHexPosition pos = new SfbHexPosition(0, 0, 0);
        pos.turn(2);
        assertEquals(2, pos.getFacing());
    }

    @Test
    @DisplayName("Turn wraps around")
    void turnWrapsAround() {
        SfbHexPosition pos = new SfbHexPosition(0, 0, 5);
        pos.turn(2);
        assertEquals(1, pos.getFacing());
    }
}

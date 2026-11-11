package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Characteristics Tests")
class B5CharacteristicsTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("All attributes default to 0")
        void defaults() {
            B5Characteristics chars = new B5Characteristics();
            assertEquals(0, chars.getST());
            assertEquals(0, chars.getDX());
            assertEquals(0, chars.getEN());
            assertEquals(0, chars.getIN());
            assertEquals(0, chars.getED());
            assertEquals(0, chars.getSO());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets all 6 attributes")
        void setsAllAttributes() {
            B5Characteristics chars = new B5Characteristics(10, 9, 8, 7, 6, 5);
            assertEquals(10, chars.getST());
            assertEquals(9, chars.getDX());
            assertEquals(8, chars.getEN());
            assertEquals(7, chars.getIN());
            assertEquals(6, chars.getED());
            assertEquals(5, chars.getSO());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Set ST")
        void setST() {
            B5Characteristics chars = new B5Characteristics();
            chars.setST(12);
            assertEquals(12, chars.getST());
        }

        @Test
        @DisplayName("Set DX")
        void setDX() {
            B5Characteristics chars = new B5Characteristics();
            chars.setDX(11);
            assertEquals(11, chars.getDX());
        }

        @Test
        @DisplayName("Set EN")
        void setEN() {
            B5Characteristics chars = new B5Characteristics();
            chars.setEN(10);
            assertEquals(10, chars.getEN());
        }

        @Test
        @DisplayName("Set IN")
        void setIN() {
            B5Characteristics chars = new B5Characteristics();
            chars.setIN(9);
            assertEquals(9, chars.getIN());
        }

        @Test
        @DisplayName("Set ED")
        void setED() {
            B5Characteristics chars = new B5Characteristics();
            chars.setED(8);
            assertEquals(8, chars.getED());
        }

        @Test
        @DisplayName("Set SO")
        void setSO() {
            B5Characteristics chars = new B5Characteristics();
            chars.setSO(7);
            assertEquals(7, chars.getSO());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes all attributes")
        void toStringContent() {
            B5Characteristics chars = new B5Characteristics(10, 9, 8, 7, 6, 5);
            String result = chars.toString();
            assertTrue(result.contains("ST=10"));
            assertTrue(result.contains("DX=9"));
            assertTrue(result.contains("EN=8"));
            assertTrue(result.contains("IN=7"));
            assertTrue(result.contains("ED=6"));
            assertTrue(result.contains("SO=5"));
        }
    }
}

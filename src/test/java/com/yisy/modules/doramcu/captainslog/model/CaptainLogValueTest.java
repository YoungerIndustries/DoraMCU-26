package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogValue Tests")
class CaptainLogValueTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogValue value = new CaptainLogValue();
            assertNull(value.getId());
            assertNull(value.getCharacter());
            assertNull(value.getDescription());
            assertFalse(value.isUsed());
            assertFalse(value.isChallenged());
        }

        @Test
        @DisplayName("Parameterized constructor sets description")
        void parameterized() {
            CaptainLogValue value = new CaptainLogValue("Protect the innocent");
            assertEquals("Protect the innocent", value.getDescription());
        }
    }

    @Nested
    @DisplayName("Used flag")
    class UsedFlag {

        @Test
        @DisplayName("Default is not used")
        void defaultNotUsed() {
            CaptainLogValue value = new CaptainLogValue();
            assertFalse(value.isUsed());
        }

        @Test
        @DisplayName("Set used")
        void setUsed() {
            CaptainLogValue value = new CaptainLogValue();
            value.setUsed(true);
            assertTrue(value.isUsed());
        }
    }

    @Nested
    @DisplayName("Challenged flag")
    class ChallengedFlag {

        @Test
        @DisplayName("Default is not challenged")
        void defaultNotChallenged() {
            CaptainLogValue value = new CaptainLogValue();
            assertFalse(value.isChallenged());
        }

        @Test
        @DisplayName("Set challenged")
        void setChallenged() {
            CaptainLogValue value = new CaptainLogValue();
            value.setChallenged(true);
            assertTrue(value.isChallenged());
        }
    }

    @Nested
    @DisplayName("Description")
    class Description {

        @Test
        @DisplayName("Set description")
        void setDescription() {
            CaptainLogValue value = new CaptainLogValue();
            value.setDescription("Seek knowledge");
            assertEquals("Seek knowledge", value.getDescription());
        }

        @Test
        @DisplayName("toString includes description")
        void toStringIncludesDescription() {
            CaptainLogValue value = new CaptainLogValue("Protect the innocent");
            assertTrue(value.toString().contains("Protect the innocent"));
        }
    }
}

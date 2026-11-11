package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogEquipment Tests")
class CaptainLogEquipmentTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            assertNull(equip.getId());
            assertNull(equip.getCharacter());
            assertNull(equip.getName());
            assertNull(equip.getDescription());
            assertNull(equip.getCategory());
            assertNull(equip.getBoonOn());
            assertTrue(equip.isActive());
        }
    }

    @Nested
    @DisplayName("Name and description")
    class NameAndDescription {

        @Test
        @DisplayName("Set name and description")
        void setNameAndDescription() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            equip.setName("Phaser Type-2");
            equip.setDescription("Standard issue phaser rifle");
            assertEquals("Phaser Type-2", equip.getName());
            assertEquals("Standard issue phaser rifle", equip.getDescription());
        }
    }

    @Nested
    @DisplayName("Category")
    class Category {

        @Test
        @DisplayName("Set category")
        void setCategory() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            equip.setCategory("WEAPON");
            assertEquals("WEAPON", equip.getCategory());
        }

        @Test
        @DisplayName("Various categories")
        void variousCategories() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            equip.setCategory("TOOL");
            assertEquals("TOOL", equip.getCategory());
            equip.setCategory("COMMUNICATION");
            assertEquals("COMMUNICATION", equip.getCategory());
            equip.setCategory("MEDICAL");
            assertEquals("MEDICAL", equip.getCategory());
        }
    }

    @Nested
    @DisplayName("Boon")
    class Boon {

        @Test
        @DisplayName("Set boon on")
        void setBoonOn() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            equip.setBoonOn("SECURITY checks");
            assertEquals("SECURITY checks", equip.getBoonOn());
        }

        @Test
        @DisplayName("Null boon on")
        void nullBoonOn() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            assertNull(equip.getBoonOn());
        }
    }

    @Nested
    @DisplayName("Active status")
    class ActiveStatus {

        @Test
        @DisplayName("Default is active")
        void defaultActive() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            assertTrue(equip.isActive());
        }

        @Test
        @DisplayName("Set active to false")
        void setActiveFalse() {
            CaptainLogEquipment equip = new CaptainLogEquipment();
            equip.setActive(false);
            assertFalse(equip.isActive());
        }
    }
}

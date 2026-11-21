package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wormhole JPA Entity Tests")
class WormholeModelTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize createdAt")
        void defaultConstructor_shouldInitializeCreatedAt() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            Wormhole wormhole = new Wormhole();
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(wormhole.getCreatedAt());
            assertTrue(wormhole.getCreatedAt().isAfter(before) || wormhole.getCreatedAt().isEqual(before));
            assertTrue(wormhole.getCreatedAt().isBefore(after) || wormhole.getCreatedAt().isEqual(after));
        }

        @Test
        @DisplayName("Default constructor should set isActive to true")
        void defaultConstructor_shouldSetActiveTrue() {
            Wormhole wormhole = new Wormhole();
            assertTrue(wormhole.isActive());
        }

        @Test
        @DisplayName("Default constructor should null id")
        void defaultConstructor_shouldNullId() {
            Wormhole wormhole = new Wormhole();
            assertNull(wormhole.getId());
        }
    }

    @Nested
    @DisplayName("Basic fields")
    class BasicFieldsTests {

        @Test
        @DisplayName("Id getter and setter")
        void id_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(42L);
            assertEquals(42L, wormhole.getId());
        }

        @Test
        @DisplayName("GalaxyName getter and setter")
        void galaxyName_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setGalaxyName("Milky Way");
            assertEquals("Milky Way", wormhole.getGalaxyName());
        }

        @Test
        @DisplayName("SolarSystemId getter and setter")
        void solarSystemId_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setSolarSystemId(100L);
            assertEquals(100L, wormhole.getSolarSystemId());
        }

        @Test
        @DisplayName("Name getter and setter")
        void name_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setName("Alpha Gateway");
            assertEquals("Alpha Gateway", wormhole.getName());
        }

        @Test
        @DisplayName("ConnectedWormholeId getter and setter")
        void connectedWormholeId_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setConnectedWormholeId(99L);
            assertEquals(99L, wormhole.getConnectedWormholeId());
        }

        @Test
        @DisplayName("ConnectedWormholeId should be null by default")
        void connectedWormholeId_defaultNull() {
            Wormhole wormhole = new Wormhole();
            assertNull(wormhole.getConnectedWormholeId());
        }

        @Test
        @DisplayName("CreatedAt getter and setter")
        void createdAt_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            LocalDateTime time = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
            wormhole.setCreatedAt(time);
            assertEquals(time, wormhole.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Active state")
    class ActiveStateTests {

        @Test
        @DisplayName("isActive should return true by default")
        void isActive_defaultTrue() {
            Wormhole wormhole = new Wormhole();
            assertTrue(wormhole.isActive());
        }

        @Test
        @DisplayName("setActive should update active state")
        void setActive_shouldUpdateState() {
            Wormhole wormhole = new Wormhole();
            assertTrue(wormhole.isActive());
            wormhole.setActive(false);
            assertFalse(wormhole.isActive());
            wormhole.setActive(true);
            assertTrue(wormhole.isActive());
        }
    }

    @Nested
    @DisplayName("Sector coordinates")
    class SectorCoordinatesTests {

        @Test
        @DisplayName("SectorRow getter and setter")
        void sectorRow_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setSectorRow(5);
            assertEquals(5, wormhole.getSectorRow());
        }

        @Test
        @DisplayName("SectorCol getter and setter")
        void sectorCol_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setSectorCol(3);
            assertEquals(3, wormhole.getSectorCol());
        }

        @Test
        @DisplayName("SubSectorRow getter and setter")
        void subSectorRow_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setSubSectorRow(7);
            assertEquals(7, wormhole.getSubSectorRow());
        }

        @Test
        @DisplayName("SubSectorCol getter and setter")
        void subSectorCol_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setSubSectorCol(2);
            assertEquals(2, wormhole.getSubSectorCol());
        }

        @Test
        @DisplayName("PosX getter and setter")
        void posX_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setPosX(4);
            assertEquals(4, wormhole.getPosX());
        }

        @Test
        @DisplayName("PosY getter and setter")
        void posY_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setPosY(6);
            assertEquals(6, wormhole.getPosY());
        }

        @Test
        @DisplayName("PosZ getter and setter")
        void posZ_getterAndSetter() {
            Wormhole wormhole = new Wormhole();
            wormhole.setPosZ(1);
            assertEquals(1, wormhole.getPosZ());
        }

        @Test
        @DisplayName("All coordinates should default to 0")
        void coordinates_defaultZero() {
            Wormhole wormhole = new Wormhole();
            assertEquals(0, wormhole.getSectorRow());
            assertEquals(0, wormhole.getSectorCol());
            assertEquals(0, wormhole.getSubSectorRow());
            assertEquals(0, wormhole.getSubSectorCol());
            assertEquals(0, wormhole.getPosX());
            assertEquals(0, wormhole.getPosY());
            assertEquals(0, wormhole.getPosZ());
        }
    }

    @Nested
    @DisplayName("Combined fields")
    class CombinedFieldsTests {

        @Test
        @DisplayName("Setting all fields should persist correctly")
        void setAllFields_shouldPersistCorrectly() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Andromeda");
            wormhole.setSolarSystemId(50L);
            wormhole.setSectorRow(3);
            wormhole.setSectorCol(7);
            wormhole.setSubSectorRow(2);
            wormhole.setSubSectorCol(8);
            wormhole.setPosX(5);
            wormhole.setPosY(4);
            wormhole.setPosZ(1);
            wormhole.setName("Andromeda Gateway");
            wormhole.setActive(false);
            wormhole.setConnectedWormholeId(2L);

            assertEquals(1L, wormhole.getId());
            assertEquals("Andromeda", wormhole.getGalaxyName());
            assertEquals(50L, wormhole.getSolarSystemId());
            assertEquals(3, wormhole.getSectorRow());
            assertEquals(7, wormhole.getSectorCol());
            assertEquals(2, wormhole.getSubSectorRow());
            assertEquals(8, wormhole.getSubSectorCol());
            assertEquals(5, wormhole.getPosX());
            assertEquals(4, wormhole.getPosY());
            assertEquals(1, wormhole.getPosZ());
            assertEquals("Andromeda Gateway", wormhole.getName());
            assertFalse(wormhole.isActive());
            assertEquals(2L, wormhole.getConnectedWormholeId());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include id and galaxyName")
        void toString_shouldIncludeIdAndGalaxyName() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");

            String str = wormhole.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("galaxyName='Milky Way'"));
        }

        @Test
        @DisplayName("toString should include name")
        void toString_shouldIncludeName() {
            Wormhole wormhole = new Wormhole();
            wormhole.setName("Alpha Gateway");

            String str = wormhole.toString();
            assertTrue(str.contains("name='Alpha Gateway'"));
        }

        @Test
        @DisplayName("toString should include isActive")
        void toString_shouldIncludeIsActive() {
            Wormhole wormhole = new Wormhole();
            wormhole.setActive(false);

            String str = wormhole.toString();
            assertTrue(str.contains("isActive=false"));
        }

        @Test
        @DisplayName("toString should include connectedWormholeId")
        void toString_shouldIncludeConnectedWormholeId() {
            Wormhole wormhole = new Wormhole();
            wormhole.setConnectedWormholeId(99L);

            String str = wormhole.toString();
            assertTrue(str.contains("connectedWormholeId=99"));
        }

        @Test
        @DisplayName("toString should not throw with null fields")
        void toString_shouldNotThrowWithNullFields() {
            Wormhole wormhole = new Wormhole();
            assertDoesNotThrow(wormhole::toString);
        }

        @Test
        @DisplayName("toString should start with Wormhole{")
        void toString_shouldStartWithWormhole() {
            Wormhole wormhole = new Wormhole();
            String str = wormhole.toString();
            assertTrue(str.startsWith("Wormhole{"));
            assertTrue(str.endsWith("}"));
        }
    }
}

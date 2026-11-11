package com.yisy.modules.doramcu.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wormhole DTO Tests")
class WormholeDtoTest {

    // ---- WormholeInfoDto ----

    @Nested
    @DisplayName("WormholeInfoDto")
    class WormholeInfoDtoTests {

        @Test
        @DisplayName("Default constructor should initialize all fields to null/false/0")
        void defaultConstructor_shouldInitializeDefaults() {
            WormholeInfoDto dto = new WormholeInfoDto();

            assertNull(dto.getId());
            assertNull(dto.getGalaxyName());
            assertNull(dto.getSolarSystemId());
            assertEquals(0, dto.getSectorRow());
            assertEquals(0, dto.getSectorCol());
            assertEquals(0, dto.getSubSectorRow());
            assertEquals(0, dto.getSubSectorCol());
            assertEquals(0, dto.getPosX());
            assertEquals(0, dto.getPosY());
            assertEquals(0, dto.getPosZ());
            assertNull(dto.getName());
            assertFalse(dto.isActive());
            assertNull(dto.getConnectedWormholeId());
            assertNull(dto.getConnectedGalaxyName());
            assertNull(dto.getCreatedAt());
        }

        @Test
        @DisplayName("All getters and setters should work")
        void allGettersAndSetters_shouldWork() {
            WormholeInfoDto dto = new WormholeInfoDto();
            LocalDateTime createdAt = LocalDateTime.of(2025, 6, 15, 10, 30, 0);

            dto.setId(1L);
            dto.setGalaxyName("Milky Way");
            dto.setSolarSystemId(50L);
            dto.setSectorRow(3);
            dto.setSectorCol(7);
            dto.setSubSectorRow(2);
            dto.setSubSectorCol(8);
            dto.setPosX(5);
            dto.setPosY(4);
            dto.setPosZ(1);
            dto.setName("Milky Way Gateway");
            dto.setActive(true);
            dto.setConnectedWormholeId(2L);
            dto.setConnectedGalaxyName("Andromeda");
            dto.setCreatedAt(createdAt);

            assertEquals(1L, dto.getId());
            assertEquals("Milky Way", dto.getGalaxyName());
            assertEquals(50L, dto.getSolarSystemId());
            assertEquals(3, dto.getSectorRow());
            assertEquals(7, dto.getSectorCol());
            assertEquals(2, dto.getSubSectorRow());
            assertEquals(8, dto.getSubSectorCol());
            assertEquals(5, dto.getPosX());
            assertEquals(4, dto.getPosY());
            assertEquals(1, dto.getPosZ());
            assertEquals("Milky Way Gateway", dto.getName());
            assertTrue(dto.isActive());
            assertEquals(2L, dto.getConnectedWormholeId());
            assertEquals("Andromeda", dto.getConnectedGalaxyName());
            assertEquals(createdAt, dto.getCreatedAt());
        }

        @Test
        @DisplayName("setActive should toggle active state")
        void setActive_shouldToggleState() {
            WormholeInfoDto dto = new WormholeInfoDto();
            assertFalse(dto.isActive());
            dto.setActive(true);
            assertTrue(dto.isActive());
            dto.setActive(false);
            assertFalse(dto.isActive());
        }
    }

    // ---- WormholeConnectionDto ----

    @Nested
    @DisplayName("WormholeConnectionDto")
    class WormholeConnectionDtoTests {

        @Test
        @DisplayName("Default constructor should initialize all fields to null")
        void defaultConstructor_shouldInitializeDefaults() {
            WormholeConnectionDto dto = new WormholeConnectionDto();

            assertNull(dto.getWormholeA());
            assertNull(dto.getWormholeB());
            assertNull(dto.getGalaxyA());
            assertNull(dto.getGalaxyB());
        }

        @Test
        @DisplayName("All getters and setters should work")
        void allGettersAndSetters_shouldWork() {
            WormholeConnectionDto dto = new WormholeConnectionDto();
            WormholeInfoDto whA = new WormholeInfoDto();
            WormholeInfoDto whB = new WormholeInfoDto();

            dto.setWormholeA(whA);
            dto.setWormholeB(whB);
            dto.setGalaxyA("Milky Way");
            dto.setGalaxyB("Andromeda");

            assertSame(whA, dto.getWormholeA());
            assertSame(whB, dto.getWormholeB());
            assertEquals("Milky Way", dto.getGalaxyA());
            assertEquals("Andromeda", dto.getGalaxyB());
        }

        @Test
        @DisplayName("Wormhole DTOs should be settable independently")
        void wormholes_shouldBeSettableIndependently() {
            WormholeConnectionDto dto = new WormholeConnectionDto();
            WormholeInfoDto whA = new WormholeInfoDto();
            whA.setGalaxyName("Milky Way");

            dto.setWormholeA(whA);
            assertEquals("Milky Way", dto.getWormholeA().getGalaxyName());
            assertNull(dto.getWormholeB());
        }
    }

    // ---- WormholePairRequest ----

    @Nested
    @DisplayName("WormholePairRequest")
    class WormholePairRequestTests {

        @Test
        @DisplayName("Default constructor should initialize all fields to null")
        void defaultConstructor_shouldInitializeDefaults() {
            WormholePairRequest request = new WormholePairRequest();

            assertNull(request.getGalaxyA());
            assertNull(request.getGalaxyB());
            assertNull(request.getSolarSystemIdA());
            assertNull(request.getSolarSystemIdB());
            assertNull(request.getNameA());
            assertNull(request.getNameB());
        }

        @Test
        @DisplayName("GalaxyA getter and setter")
        void galaxyA_getterAndSetter() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            assertEquals("Milky Way", request.getGalaxyA());
        }

        @Test
        @DisplayName("GalaxyB getter and setter")
        void galaxyB_getterAndSetter() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyB("Andromeda");
            assertEquals("Andromeda", request.getGalaxyB());
        }

        @Test
        @DisplayName("SolarSystemIdA getter and setter")
        void solarSystemIdA_getterAndSetter() {
            WormholePairRequest request = new WormholePairRequest();
            request.setSolarSystemIdA(100L);
            assertEquals(100L, request.getSolarSystemIdA());
        }

        @Test
        @DisplayName("SolarSystemIdB getter and setter")
        void solarSystemIdB_getterAndSetter() {
            WormholePairRequest request = new WormholePairRequest();
            request.setSolarSystemIdB(200L);
            assertEquals(200L, request.getSolarSystemIdB());
        }

        @Test
        @DisplayName("NameA getter and setter")
        void nameA_getterAndSetter() {
            WormholePairRequest request = new WormholePairRequest();
            request.setNameA("Alpha Gate");
            assertEquals("Alpha Gate", request.getNameA());
        }

        @Test
        @DisplayName("NameB getter and setter")
        void nameB_getterAndSetter() {
            WormholePairRequest request = new WormholePairRequest();
            request.setNameB("Beta Gate");
            assertEquals("Beta Gate", request.getNameB());
        }

        @Test
        @DisplayName("All fields should be settable together")
        void allFields_shouldBeSettableTogether() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");
            request.setSolarSystemIdA(100L);
            request.setSolarSystemIdB(200L);
            request.setNameA("Alpha Gate");
            request.setNameB("Beta Gate");

            assertEquals("Milky Way", request.getGalaxyA());
            assertEquals("Andromeda", request.getGalaxyB());
            assertEquals(100L, request.getSolarSystemIdA());
            assertEquals(200L, request.getSolarSystemIdB());
            assertEquals("Alpha Gate", request.getNameA());
            assertEquals("Beta Gate", request.getNameB());
        }

        @Test
        @DisplayName("Only galaxy names should be settable (minimal request)")
        void minimalRequest_shouldWork() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");

            assertEquals("Milky Way", request.getGalaxyA());
            assertEquals("Andromeda", request.getGalaxyB());
            assertNull(request.getSolarSystemIdA());
            assertNull(request.getSolarSystemIdB());
            assertNull(request.getNameA());
            assertNull(request.getNameB());
        }
    }

    // ---- WormholeTravelRequest ----

    @Nested
    @DisplayName("WormholeTravelRequest")
    class WormholeTravelRequestTests {

        @Test
        @DisplayName("Default constructor should initialize playerName to null")
        void defaultConstructor_shouldInitializeDefaults() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            assertNull(request.getPlayerName());
        }

        @Test
        @DisplayName("PlayerName getter and setter")
        void playerName_getterAndSetter() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("CaptainKirk");
            assertEquals("CaptainKirk", request.getPlayerName());
        }

        @Test
        @DisplayName("PlayerName should be updatable")
        void playerName_shouldBeUpdatable() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("Player1");
            assertEquals("Player1", request.getPlayerName());
            request.setPlayerName("Player2");
            assertEquals("Player2", request.getPlayerName());
        }
    }

    // ---- WormholeTravelResponse ----

    @Nested
    @DisplayName("WormholeTravelResponse")
    class WormholeTravelResponseTests {

        @Test
        @DisplayName("Default constructor should initialize all fields to null/false")
        void defaultConstructor_shouldInitializeDefaults() {
            WormholeTravelResponse response = new WormholeTravelResponse();

            assertNull(response.getPlayerName());
            assertNull(response.getFromGalaxy());
            assertNull(response.getToGalaxy());
            assertFalse(response.isConverted());
            assertNull(response.getConversionDetails());
        }

        @Test
        @DisplayName("All getters and setters should work")
        void allGettersAndSetters_shouldWork() {
            WormholeTravelResponse response = new WormholeTravelResponse();

            response.setPlayerName("CaptainKirk");
            response.setFromGalaxy("Milky Way");
            response.setToGalaxy("Andromeda");
            response.setConverted(true);
            response.setConversionDetails("DoraT to Captain's Log conversion");

            assertEquals("CaptainKirk", response.getPlayerName());
            assertEquals("Milky Way", response.getFromGalaxy());
            assertEquals("Andromeda", response.getToGalaxy());
            assertTrue(response.isConverted());
            assertEquals("DoraT to Captain's Log conversion", response.getConversionDetails());
        }

        @Test
        @DisplayName("Converted should toggle correctly")
        void converted_shouldToggleCorrectly() {
            WormholeTravelResponse response = new WormholeTravelResponse();
            assertFalse(response.isConverted());
            response.setConverted(true);
            assertTrue(response.isConverted());
            response.setConverted(false);
            assertFalse(response.isConverted());
        }

        @Test
        @DisplayName("ConversionDetails should be independently settable")
        void conversionDetails_shouldBeIndependentlySettable() {
            WormholeTravelResponse response = new WormholeTravelResponse();
            response.setConversionDetails("Some details");
            assertEquals("Some details", response.getConversionDetails());
            response.setConversionDetails(null);
            assertNull(response.getConversionDetails());
        }

        @Test
        @DisplayName("From and To galaxy should be independently settable")
        void fromAndToGalaxy_shouldBeIndependentlySettable() {
            WormholeTravelResponse response = new WormholeTravelResponse();
            response.setFromGalaxy("Milky Way");
            assertEquals("Milky Way", response.getFromGalaxy());
            assertNull(response.getToGalaxy());

            response.setToGalaxy("Andromeda");
            assertEquals("Andromeda", response.getToGalaxy());
            assertEquals("Milky Way", response.getFromGalaxy());
        }
    }
}

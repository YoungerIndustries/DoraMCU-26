package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlanetStructure Tests")
class PlanetStructureTest {

    @Nested
    @DisplayName("StructureType Enum")
    class StructureTypeTests {

        @Test
        @DisplayName("should have all expected structure types")
        void shouldHaveAllExpectedTypes() {
            PlanetStructure.StructureType[] types = PlanetStructure.StructureType.values();
            assertEquals(6, types.length);
        }

        @Test
        @DisplayName("should return correct display name for MINE")
        void shouldReturnDisplayNameForMine() {
            assertEquals("Mine", PlanetStructure.StructureType.MINE.getDisplayName());
        }

        @Test
        @DisplayName("should return correct description for MINE")
        void shouldReturnDescriptionForMine() {
            assertEquals("Extracts raw materials from the planet",
                    PlanetStructure.StructureType.MINE.getDescription());
        }

        @Test
        @DisplayName("should return correct display name for SPACEPORT")
        void shouldReturnDisplayNameForSpaceport() {
            assertEquals("Spaceport", PlanetStructure.StructureType.SPACEPORT.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for FACTORY")
        void shouldReturnDisplayNameForFactory() {
            assertEquals("Factory", PlanetStructure.StructureType.FACTORY.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for EQUIPMENT_STORE")
        void shouldReturnDisplayNameForEquipmentStore() {
            assertEquals("Equipment Store",
                    PlanetStructure.StructureType.EQUIPMENT_STORE.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for TRANSPORT_STATION")
        void shouldReturnDisplayNameForTransportStation() {
            assertEquals("Transport Station",
                    PlanetStructure.StructureType.TRANSPORT_STATION.getDisplayName());
        }

        @Test
        @DisplayName("should return correct display name for RESIDENCE")
        void shouldReturnDisplayNameForResidence() {
            assertEquals("Residence", PlanetStructure.StructureType.RESIDENCE.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Entity Fields")
    class EntityFieldTests {

        @Test
        @DisplayName("should set and get name")
        void shouldSetAndGetName() {
            PlanetStructure structure = new PlanetStructure();
            structure.setName("Test Mine");
            assertEquals("Test Mine", structure.getName());
        }

        @Test
        @DisplayName("should set and get structure type")
        void shouldSetAndGetStructureType() {
            PlanetStructure structure = new PlanetStructure();
            structure.setStructureType(PlanetStructure.StructureType.MINE);
            assertEquals(PlanetStructure.StructureType.MINE, structure.getStructureType());
        }

        @Test
        @DisplayName("should set and get planet ID")
        void shouldSetAndGetPlanetId() {
            PlanetStructure structure = new PlanetStructure();
            structure.setPlanetId(1L);
            assertEquals(1L, structure.getPlanetId());
        }

        @Test
        @DisplayName("should set and get latitude")
        void shouldSetAndGetLatitude() {
            PlanetStructure structure = new PlanetStructure();
            structure.setLatitude(32.7157);
            assertEquals(32.7157, structure.getLatitude());
        }

        @Test
        @DisplayName("should set and get longitude")
        void shouldSetAndGetLongitude() {
            PlanetStructure structure = new PlanetStructure();
            structure.setLongitude(-117.1611);
            assertEquals(-117.1611, structure.getLongitude());
        }

        @Test
        @DisplayName("should set and get elevation")
        void shouldSetAndGetElevation() {
            PlanetStructure structure = new PlanetStructure();
            structure.setElevation(500.0);
            assertEquals(500.0, structure.getElevation());
        }

        @Test
        @DisplayName("should set and get resources JSON")
        void shouldSetAndGetResourcesJson() {
            PlanetStructure structure = new PlanetStructure();
            String json = "{\"iron_ore\": 5000, \"steel\": 0}";
            structure.setResourcesJson(json);
            assertEquals(json, structure.getResourcesJson());
        }

        @Test
        @DisplayName("should set and get capacity")
        void shouldSetAndGetCapacity() {
            PlanetStructure structure = new PlanetStructure();
            structure.setCapacity(1000);
            assertEquals(1000, structure.getCapacity());
        }

        @Test
        @DisplayName("should set and get active state")
        void shouldSetAndGetActive() {
            PlanetStructure structure = new PlanetStructure();
            assertTrue(structure.isActive(), "Default should be active");
            structure.setActive(false);
            assertFalse(structure.isActive());
        }

        @Test
        @DisplayName("should set and get description")
        void shouldSetAndGetDescription() {
            PlanetStructure structure = new PlanetStructure();
            structure.setDescription("A test mine");
            assertEquals("A test mine", structure.getDescription());
        }

        @Test
        @DisplayName("should set and get ID")
        void shouldSetAndGetId() {
            PlanetStructure structure = new PlanetStructure();
            structure.setId(42L);
            assertEquals(42L, structure.getId());
        }

        @Test
        @DisplayName("should set and get null elevation")
        void shouldHandleNullElevation() {
            PlanetStructure structure = new PlanetStructure();
            structure.setElevation(null);
            assertNull(structure.getElevation());
        }

        @Test
        @DisplayName("should set and get null capacity")
        void shouldHandleNullCapacity() {
            PlanetStructure structure = new PlanetStructure();
            structure.setCapacity(null);
            assertNull(structure.getCapacity());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("should set created and updated timestamps on create")
        void shouldSetTimestampsOnCreate() {
            PlanetStructure structure = new PlanetStructure();
            structure.onCreate();

            assertNotNull(structure.getCreatedAt());
            assertNotNull(structure.getUpdatedAt());
        }

        @Test
        @DisplayName("should update timestamp on update")
        void shouldUpdateTimestampOnUpdate() {
            PlanetStructure structure = new PlanetStructure();
            structure.onCreate();
            LocalDateTime created = structure.getCreatedAt();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }

            structure.onUpdate();
            assertEquals(created, structure.getCreatedAt(), "createdAt should not change");
            assertNotEquals(structure.getCreatedAt(), structure.getUpdatedAt(),
                    "updatedAt should be newer than createdAt");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include name and type in toString")
        void shouldIncludeNameAndType() {
            PlanetStructure structure = new PlanetStructure();
            structure.setName("Test Mine");
            structure.setStructureType(PlanetStructure.StructureType.MINE);
            structure.setPlanetId(1L);
            structure.setActive(true);

            String str = structure.toString();
            assertTrue(str.contains("Test Mine"));
            assertTrue(str.contains("MINE"));
            assertTrue(str.contains("planetId=1"));
            assertTrue(str.contains("active=true"));
        }
    }

    @Nested
    @DisplayName("Full Structure Creation")
    class FullStructureTests {

        @Test
        @DisplayName("should create a complete mine structure")
        void shouldCreateCompleteMine() {
            PlanetStructure structure = new PlanetStructure();
            structure.setId(1L);
            structure.setName("San Diego Iron Mine");
            structure.setStructureType(PlanetStructure.StructureType.MINE);
            structure.setPlanetId(1L);
            structure.setLatitude(32.8657);
            structure.setLongitude(-117.2611);
            structure.setElevation(500.0);
            structure.setResourcesJson("{\"iron_ore\": 5000, \"steel\": 0}");
            structure.setCapacity(1000);
            structure.setActive(true);
            structure.setDescription("Large iron ore mine");

            assertEquals(1L, structure.getId());
            assertEquals("San Diego Iron Mine", structure.getName());
            assertEquals(PlanetStructure.StructureType.MINE, structure.getStructureType());
            assertEquals(1L, structure.getPlanetId());
            assertEquals(32.8657, structure.getLatitude());
            assertEquals(-117.2611, structure.getLongitude());
            assertEquals(500.0, structure.getElevation());
            assertEquals("{\"iron_ore\": 5000, \"steel\": 0}", structure.getResourcesJson());
            assertEquals(1000, structure.getCapacity());
            assertTrue(structure.isActive());
            assertEquals("Large iron ore mine", structure.getDescription());
        }

        @Test
        @DisplayName("should create a spaceport structure")
        void shouldCreateSpaceport() {
            PlanetStructure structure = new PlanetStructure();
            structure.setStructureType(PlanetStructure.StructureType.SPACEPORT);
            structure.setPlanetId(1L);
            structure.setLatitude(32.5657);
            structure.setLongitude(-117.3611);
            structure.setCapacity(5000);

            assertEquals(PlanetStructure.StructureType.SPACEPORT, structure.getStructureType());
            assertEquals(5000, structure.getCapacity());
        }

        @Test
        @DisplayName("should create a factory structure")
        void shouldCreateFactory() {
            PlanetStructure structure = new PlanetStructure();
            structure.setStructureType(PlanetStructure.StructureType.FACTORY);
            structure.setResourcesJson("{\"steel\": 0, \"iron_ore\": 0}");

            assertEquals(PlanetStructure.StructureType.FACTORY, structure.getStructureType());
            assertEquals("{\"steel\": 0, \"iron_ore\": 0}", structure.getResourcesJson());
        }
    }
}

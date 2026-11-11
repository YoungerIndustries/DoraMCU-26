package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompartmentType Extensions Tests")
class CompartmentTypeTest {

    @Nested
    @DisplayName("Dungeon Compartment Types")
    class DungeonTypeTests {

        @Test
        @DisplayName("should have THRONES_ROOM type")
        void thronesRoom_exists() {
            assertEquals("THRONES_ROOM", CompartmentType.THRONES_ROOM.name());
        }

        @Test
        @DisplayName("should have TREASURE_VAULT type")
        void treasureVault_exists() {
            assertEquals("TREASURE_VAULT", CompartmentType.TREASURE_VAULT.name());
        }

        @Test
        @DisplayName("should have LIBRARY type")
        void library_exists() {
            assertEquals("LIBRARY", CompartmentType.LIBRARY.name());
        }

        @Test
        @DisplayName("should have PRISON type")
        void prison_exists() {
            assertEquals("PRISON", CompartmentType.PRISON.name());
        }

        @Test
        @DisplayName("should have ARMORY type")
        void armory_exists() {
            assertEquals("ARMORY", CompartmentType.ARMORY.name());
        }

        @Test
        @DisplayName("should have TRAP_ROOM type")
        void trapRoom_exists() {
            assertEquals("TRAP_ROOM", CompartmentType.TRAP_ROOM.name());
        }

        @Test
        @DisplayName("should have CRYPT type")
        void crypt_exists() {
            assertEquals("CRYPT", CompartmentType.CRYPT.name());
        }

        @Test
        @DisplayName("should have INTERSECTION type")
        void intersection_exists() {
            assertEquals("INTERSECTION", CompartmentType.INTERSECTION.name());
        }
    }

    @Nested
    @DisplayName("Facility Compartment Types")
    class FacilityTypeTests {

        @Test
        @DisplayName("should have MINING_SHAFT type")
        void miningShaft_exists() {
            assertEquals("MINING_SHAFT", CompartmentType.MINING_SHAFT.name());
        }

        @Test
        @DisplayName("should have POWER_GENERATOR type")
        void powerGenerator_exists() {
            assertEquals("POWER_GENERATOR", CompartmentType.POWER_GENERATOR.name());
        }

        @Test
        @DisplayName("should have WAREHOUSE type")
        void warehouse_exists() {
            assertEquals("WAREHOUSE", CompartmentType.WAREHOUSE.name());
        }

        @Test
        @DisplayName("should have PROCESSING_PLANT type")
        void processingPlant_exists() {
            assertEquals("PROCESSING_PLANT", CompartmentType.PROCESSING_PLANT.name());
        }

        @Test
        @DisplayName("should have WORKER_QUARTERS type")
        void workerQuarters_exists() {
            assertEquals("WORKER_QUARTERS", CompartmentType.WORKER_QUARTERS.name());
        }

        @Test
        @DisplayName("should have COMMS_ARRAY type")
        void commsArray_exists() {
            assertEquals("COMMS_ARRAY", CompartmentType.COMMS_ARRAY.name());
        }
    }

    @Nested
    @DisplayName("fromString Method")
    class FromStringTests {

        @Test
        @DisplayName("should parse exact match")
        void fromString_exactMatch() {
            assertEquals(CompartmentType.COMMAND, CompartmentType.fromString("COMMAND"));
            assertEquals(CompartmentType.ENGINE, CompartmentType.fromString("ENGINE"));
            assertEquals(CompartmentType.CARGO, CompartmentType.fromString("CARGO"));
        }

        @Test
        @DisplayName("should handle case insensitivity")
        void fromString_caseInsensitive() {
            assertEquals(CompartmentType.COMMAND, CompartmentType.fromString("command"));
            assertEquals(CompartmentType.ENGINE, CompartmentType.fromString("Engine"));
        }

        @Test
        @DisplayName("should parse dungeon types")
        void fromString_dungeonTypes() {
            assertEquals(CompartmentType.THRONES_ROOM, CompartmentType.fromString("THRONES_ROOM"));
            assertEquals(CompartmentType.TREASURE_VAULT, CompartmentType.fromString("TREASURE_VAULT"));
            assertEquals(CompartmentType.LIBRARY, CompartmentType.fromString("LIBRARY"));
        }

        @Test
        @DisplayName("should parse facility types")
        void fromString_facilityTypes() {
            assertEquals(CompartmentType.MINING_SHAFT, CompartmentType.fromString("MINING_SHAFT"));
            assertEquals(CompartmentType.POWER_GENERATOR, CompartmentType.fromString("POWER_GENERATOR"));
            assertEquals(CompartmentType.WAREHOUSE, CompartmentType.fromString("WAREHOUSE"));
        }

        @Test
        @DisplayName("should handle alias mappings")
        void fromString_aliases() {
            assertEquals(CompartmentType.THRONES_ROOM, CompartmentType.fromString("BOSS_CHAMBER"));
            assertEquals(CompartmentType.CARGO, CompartmentType.fromString("STORAGE"));
            assertEquals(CompartmentType.TRANSIT, CompartmentType.fromString("CORRIDOR"));
            assertEquals(CompartmentType.LIBRARY, CompartmentType.fromString("SANCTUM"));
            assertEquals(CompartmentType.ARMORY, CompartmentType.fromString("WORKSHOP"));
            assertEquals(CompartmentType.CRYPT, CompartmentType.fromString("PIT"));
        }

        @Test
        @DisplayName("should return EMPTY for unknown types")
        void fromString_unknown() {
            assertEquals(CompartmentType.EMPTY, CompartmentType.fromString("UNKNOWN_TYPE"));
        }

        @Test
        @DisplayName("should return EMPTY for null input")
        void fromString_null() {
            assertEquals(CompartmentType.EMPTY, CompartmentType.fromString(null));
        }

        @Test
        @DisplayName("should return EMPTY for blank input")
        void fromString_blank() {
            assertEquals(CompartmentType.EMPTY, CompartmentType.fromString("  "));
        }
    }

    @Nested
    @DisplayName("Enum Values")
    class EnumValuesTests {

        @Test
        @DisplayName("should have expected number of values")
        void values_count() {
            CompartmentType[] values = CompartmentType.values();
            // Original ship types (11) + dungeon types (8) + facility types (6) = 25
            assertTrue(values.length >= 20, "Should have at least 20 compartment types, got: " + values.length);
        }

        @Test
        @DisplayName("should include original ship types")
        void values_originalShipTypes() {
            assertEquals(CompartmentType.COMMAND, CompartmentType.valueOf("COMMAND"));
            assertEquals(CompartmentType.ENGINE, CompartmentType.valueOf("ENGINE"));
            assertEquals(CompartmentType.CARGO, CompartmentType.valueOf("CARGO"));
            assertEquals(CompartmentType.WEAPONS, CompartmentType.valueOf("WEAPONS"));
            assertEquals(CompartmentType.DEFENSE, CompartmentType.valueOf("DEFENSE"));
            assertEquals(CompartmentType.CREW, CompartmentType.valueOf("CREW"));
            assertEquals(CompartmentType.RESEARCH, CompartmentType.valueOf("RESEARCH"));
            assertEquals(CompartmentType.TRANSIT, CompartmentType.valueOf("TRANSIT"));
            assertEquals(CompartmentType.EMPTY, CompartmentType.valueOf("EMPTY"));
        }
    }
}

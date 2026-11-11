package com.yisy.modules.doramcu.earth2026.model;

import com.yisy.modules.doramcu.earth2026.model.TechLevel.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TechLevel Enum Tests")
class TechLevelTest {

    @Nested
    @DisplayName("Enum values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("should have 21 tech levels (TL0-TL20)")
        void values_shouldHave21Levels() {
            assertEquals(21, TechLevel.values().length);
        }

        @Test
        @DisplayName("TL0 should be Stone Age")
        void TL0_shouldBeStoneAge() {
            assertEquals(0, TechLevel.TL0.getLevel());
            assertEquals("Stone Age", TechLevel.TL0.getName());
            assertEquals(WeaponLevel.PRIMITIVE, TechLevel.TL0.getWeaponLevel());
            assertEquals(TransportLevel.FOOT, TechLevel.TL0.getTransportLevel());
            assertEquals(CommsLevel.SMOKE_SIGNAL, TechLevel.TL0.getCommsLevel());
            assertEquals(ComputingLevel.NONE, TechLevel.TL0.getComputingLevel());
            assertEquals(MedicineLevel.HERBAL, TechLevel.TL0.getMedicineLevel());
            assertEquals(IndustryLevel.HAND, TechLevel.TL0.getIndustryLevel());
        }

        @Test
        @DisplayName("TL4 should be Industrial")
        void TL4_shouldBeIndustrial() {
            assertEquals(4, TechLevel.TL4.getLevel());
            assertEquals("Industrial", TechLevel.TL4.getName());
            assertEquals(WeaponLevel.RIFLE, TechLevel.TL4.getWeaponLevel());
            assertEquals(TransportLevel.STEAM, TechLevel.TL4.getTransportLevel());
        }

        @Test
        @DisplayName("TL5 should be Modern")
        void TL5_shouldBeModern() {
            assertEquals(5, TechLevel.TL5.getLevel());
            assertEquals("Modern", TechLevel.TL5.getName());
            assertEquals(WeaponLevel.AUTO, TechLevel.TL5.getWeaponLevel());
        }

        @Test
        @DisplayName("TL10 should be Early Stellar")
        void TL10_shouldBeEarlyStellar() {
            assertEquals(10, TechLevel.TL10.getLevel());
            assertEquals("Early Stellar", TechLevel.TL10.getName());
            assertEquals(TransportLevel.JUMP_DRIVE, TechLevel.TL10.getTransportLevel());
        }

        @Test
        @DisplayName("TL20 should be Magic Tech")
        void TL20_shouldBeMagicTech() {
            assertEquals(20, TechLevel.TL20.getLevel());
            assertEquals("Magic Tech", TechLevel.TL20.getName());
            assertEquals(WeaponLevel.MAGIC, TechLevel.TL20.getWeaponLevel());
        }
    }

    @Nested
    @DisplayName("fromLevel Tests")
    class FromLevelTests {

        @Test
        @DisplayName("should find TL0")
        void fromLevel_0() {
            assertEquals(TechLevel.TL0, TechLevel.fromLevel(0));
        }

        @Test
        @DisplayName("should find TL10")
        void fromLevel_10() {
            assertEquals(TechLevel.TL10, TechLevel.fromLevel(10));
        }

        @Test
        @DisplayName("should find TL20")
        void fromLevel_20() {
            assertEquals(TechLevel.TL20, TechLevel.fromLevel(20));
        }

        @Test
        @DisplayName("should throw for negative level")
        void fromLevel_negative() {
            assertThrows(IllegalArgumentException.class, () -> TechLevel.fromLevel(-1));
        }

        @Test
        @DisplayName("should throw for level 21")
        void fromLevel_21() {
            assertThrows(IllegalArgumentException.class, () -> TechLevel.fromLevel(21));
        }

        @Test
        @DisplayName("should throw for level 100")
        void fromLevel_100() {
            assertThrows(IllegalArgumentException.class, () -> TechLevel.fromLevel(100));
        }
    }

    @Nested
    @DisplayName("Category checks Tests")
    class CategoryChecksTests {

        @Test
        @DisplayName("TL0-TL3 should be pre-industrial")
        void isPreIndustrial() {
            assertTrue(TechLevel.TL0.isPreIndustrial());
            assertTrue(TechLevel.TL1.isPreIndustrial());
            assertTrue(TechLevel.TL2.isPreIndustrial());
            assertTrue(TechLevel.TL3.isPreIndustrial());
            assertFalse(TechLevel.TL4.isPreIndustrial());
        }

        @Test
        @DisplayName("TL4-TL6 should be industrial")
        void isIndustrial() {
            assertFalse(TechLevel.TL3.isIndustrial());
            assertTrue(TechLevel.TL4.isIndustrial());
            assertTrue(TechLevel.TL5.isIndustrial());
            assertTrue(TechLevel.TL6.isIndustrial());
            assertFalse(TechLevel.TL7.isIndustrial());
        }

        @Test
        @DisplayName("TL7-TL9 should be pre-stellar")
        void isPreStellar() {
            assertFalse(TechLevel.TL6.isPreStellar());
            assertTrue(TechLevel.TL7.isPreStellar());
            assertTrue(TechLevel.TL8.isPreStellar());
            assertTrue(TechLevel.TL9.isPreStellar());
            assertFalse(TechLevel.TL10.isPreStellar());
        }

        @Test
        @DisplayName("TL10+ should be stellar")
        void isStellar() {
            assertFalse(TechLevel.TL9.isStellar());
            assertTrue(TechLevel.TL10.isStellar());
            assertTrue(TechLevel.TL15.isStellar());
            assertTrue(TechLevel.TL20.isStellar());
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should format as TL{level} ({name})")
        void toString_format() {
            assertEquals("TL0 (Stone Age)", TechLevel.TL0.toString());
            assertEquals("TL4 (Industrial)", TechLevel.TL4.toString());
            assertEquals("TL20 (Magic Tech)", TechLevel.TL20.toString());
        }
    }

    @Nested
    @DisplayName("WeaponLevel Tests")
    class WeaponLevelTests {

        @Test
        @DisplayName("should have descriptions")
        void weaponLevel_descriptions() {
            assertNotNull(WeaponLevel.PRIMITIVE.getDescription());
            assertNotNull(WeaponLevel.RIFLE.getDescription());
            assertNotNull(WeaponLevel.NUCLEAR.getDescription());
            assertNotNull(WeaponLevel.MAGIC.getDescription());
        }

        @Test
        @DisplayName("should have 21 weapon levels")
        void weaponLevel_count() {
            assertEquals(21, WeaponLevel.values().length);
        }
    }

    @Nested
    @DisplayName("TransportLevel Tests")
    class TransportLevelTests {

        @Test
        @DisplayName("should have descriptions")
        void transportLevel_descriptions() {
            assertNotNull(TransportLevel.FOOT.getDescription());
            assertNotNull(TransportLevel.JET.getDescription());
            assertNotNull(TransportLevel.MAGIC.getDescription());
        }

        @Test
        @DisplayName("should have 21 transport levels")
        void transportLevel_count() {
            assertEquals(21, TransportLevel.values().length);
        }
    }

    @Nested
    @DisplayName("CommsLevel Tests")
    class CommsLevelTests {

        @Test
        @DisplayName("should have descriptions")
        void commsLevel_descriptions() {
            assertNotNull(CommsLevel.SMOKE_SIGNAL.getDescription());
            assertNotNull(CommsLevel.SATELLITE.getDescription());
        }

        @Test
        @DisplayName("should have 21 comms levels")
        void commsLevel_count() {
            assertEquals(21, CommsLevel.values().length);
        }
    }

    @Nested
    @DisplayName("ComputingLevel Tests")
    class ComputingLevelTests {

        @Test
        @DisplayName("should have descriptions")
        void computingLevel_descriptions() {
            assertNotNull(ComputingLevel.NONE.getDescription());
            assertNotNull(ComputingLevel.QUANTUM.getDescription());
        }

        @Test
        @DisplayName("should have 19 computing levels")
        void computingLevel_count() {
            assertEquals(19, ComputingLevel.values().length);
        }
    }

    @Nested
    @DisplayName("MedicineLevel Tests")
    class MedicineLevelTests {

        @Test
        @DisplayName("should have descriptions")
        void medicineLevel_descriptions() {
            assertNotNull(MedicineLevel.HERBAL.getDescription());
            assertNotNull(MedicineLevel.GENE_THERAPY.getDescription());
        }

        @Test
        @DisplayName("should have 21 medicine levels")
        void medicineLevel_count() {
            assertEquals(21, MedicineLevel.values().length);
        }
    }

    @Nested
    @DisplayName("IndustryLevel Tests")
    class IndustryLevelTests {

        @Test
        @DisplayName("should have descriptions")
        void industryLevel_descriptions() {
            assertNotNull(IndustryLevel.HAND.getDescription());
            assertNotNull(IndustryLevel.ANTIMATTER.getDescription());
        }

        @Test
        @DisplayName("should have 21 industry levels")
        void industryLevel_count() {
            assertEquals(21, IndustryLevel.values().length);
        }
    }
}

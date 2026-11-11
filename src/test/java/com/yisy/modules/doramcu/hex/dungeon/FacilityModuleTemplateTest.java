package com.yisy.modules.doramcu.hex.dungeon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FacilityModuleTemplate Tests")
class FacilityModuleTemplateTest {

    @Nested
    @DisplayName("FacilityModuleType Enum")
    class FacilityModuleTypeTests {

        @Test
        @DisplayName("should have all expected module types")
        void enumValues_shouldHaveAllTypes() {
            FacilityModuleTemplate.FacilityModuleType[] values = FacilityModuleTemplate.FacilityModuleType.values();
            assertEquals(12, values.length);
        }

        @Test
        @DisplayName("should include MINING_SHAFT type")
        void enumValues_miningShaft() {
            assertEquals("MINING_SHAFT", FacilityModuleTemplate.FacilityModuleType.MINING_SHAFT.name());
        }

        @Test
        @DisplayName("should include POWER_GENERATOR type")
        void enumValues_powerGenerator() {
            assertEquals("POWER_GENERATOR", FacilityModuleTemplate.FacilityModuleType.POWER_GENERATOR.name());
        }
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get type")
        void type_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setType(FacilityModuleTemplate.FacilityModuleType.MINING_SHAFT);
            assertEquals(FacilityModuleTemplate.FacilityModuleType.MINING_SHAFT, t.getType());
        }

        @Test
        @DisplayName("should set and get mineral bonus")
        void mineralBonus_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setMineralBonus(10);
            assertEquals(10, t.getMineralBonus());
        }

        @Test
        @DisplayName("should set and get energy bonus")
        void energyBonus_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setEnergyBonus(15);
            assertEquals(15, t.getEnergyBonus());
        }

        @Test
        @DisplayName("should set and get food bonus")
        void foodBonus_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setFoodBonus(5);
            assertEquals(5, t.getFoodBonus());
        }

        @Test
        @DisplayName("should set and get research bonus")
        void researchBonus_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setResearchBonus(12);
            assertEquals(12, t.getResearchBonus());
        }

        @Test
        @DisplayName("should set and get defense bonus")
        void defenseBonus_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setDefenseBonus(8);
            assertEquals(8, t.getDefenseBonus());
        }

        @Test
        @DisplayName("should set and get worker capacity")
        void workerCapacity_shouldWork() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setWorkerCapacity(20);
            assertEquals(20, t.getWorkerCapacity());
        }

        @Test
        @DisplayName("should inherit name from ModuleTemplate")
        void name_inherited() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setName("Mining Shaft");
            assertEquals("Mining Shaft", t.getName());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("should produce meaningful string representation")
        void toString_shouldProduceOutput() {
            FacilityModuleTemplate t = new FacilityModuleTemplate();
            t.setId(1L);
            t.setName("Mining Shaft");
            t.setType(FacilityModuleTemplate.FacilityModuleType.MINING_SHAFT);
            t.setWidth(8);
            t.setHeight(8);
            t.setDepth(3);
            String s = t.toString();
            assertTrue(s.contains("Mining Shaft"));
            assertTrue(s.contains("MINING_SHAFT"));
            assertTrue(s.contains("8x8x3"));
        }
    }
}

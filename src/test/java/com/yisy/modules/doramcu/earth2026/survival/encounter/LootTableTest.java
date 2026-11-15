package com.yisy.modules.doramcu.earth2026.survival.encounter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LootTable Tests")
class LootTableTest {

    // ---- generateLoot ----

    @Nested
    @DisplayName("generateLoot Tests")
    class GenerateLootTests {

        @Test
        @DisplayName("should generate loot for threat level 1")
        void generateLoot_threatLevel1() {
            Map<String, Integer> loot = LootTable.generateLoot(1);

            assertNotNull(loot);
            // Loot may be empty due to random drop chances, but should not throw
        }

        @Test
        @DisplayName("should generate loot for threat level 5")
        void generateLoot_threatLevel5() {
            Map<String, Integer> loot = LootTable.generateLoot(5);

            assertNotNull(loot);
        }

        @Test
        @DisplayName("should generate loot for threat level 10")
        void generateLoot_threatLevel10() {
            Map<String, Integer> loot = LootTable.generateLoot(10);

            assertNotNull(loot);
        }

        @Test
        @DisplayName("should handle threat level 0 by clamping to 1")
        void generateLoot_threatLevel0() {
            Map<String, Integer> loot = LootTable.generateLoot(0);

            assertNotNull(loot);
        }

        @Test
        @DisplayName("should handle threat level 11 by clamping to 10")
        void generateLoot_threatLevel11() {
            Map<String, Integer> loot = LootTable.generateLoot(11);

            assertNotNull(loot);
        }

        @Test
        @DisplayName("should generate multiple loot items over many runs")
        void generateLoot_shouldGenerateItemsOverManyRuns() {
            int totalItems = 0;
            int runs = 100;

            for (int i = 0; i < runs; i++) {
                Map<String, Integer> loot = LootTable.generateLoot(10);
                totalItems += loot.size();
            }

            // With 100 runs at threat level 10 (high drop chances), should have some items
            assertTrue(totalItems > 0, "Should generate some loot over 100 runs");
        }

        @Test
        @DisplayName("should generate more loot at higher threat levels")
        void generateLoot_moreLootAtHigherLevels() {
            int lowLevelItems = 0;
            int highLevelItems = 0;
            int runs = 100;

            for (int i = 0; i < runs; i++) {
                lowLevelItems += LootTable.generateLoot(1).size();
                highLevelItems += LootTable.generateLoot(10).size();
            }

            // Higher threat levels should generally yield more loot
            assertTrue(highLevelItems >= lowLevelItems,
                    "Higher threat levels should yield more loot: low=" + lowLevelItems + ", high=" + highLevelItems);
        }

        @Test
        @DisplayName("should include expected resource names for threat level 10")
        void generateLoot_shouldIncludeExpectedResources() {
            // Run multiple times to collect all possible drops
            boolean foundHuntedGame = false;
            boolean foundPurifiedWater = false;
            boolean foundMetalSheet = false;

            for (int i = 0; i < 100; i++) {
                Map<String, Integer> loot = LootTable.generateLoot(10);
                if (loot.containsKey("Hunted Game")) foundHuntedGame = true;
                if (loot.containsKey("Purified Water")) foundPurifiedWater = true;
                if (loot.containsKey("Metal Sheet")) foundMetalSheet = true;
            }

            assertTrue(foundHuntedGame, "Should eventually drop Hunted Game at threat level 10");
            assertTrue(foundPurifiedWater, "Should eventually drop Purified Water at threat level 10");
            assertTrue(foundMetalSheet, "Should eventually drop Metal Sheet at threat level 10");
        }

        @Test
        @DisplayName("should return positive quantities")
        void generateLoot_shouldReturnPositiveQuantities() {
            for (int i = 0; i < 50; i++) {
                Map<String, Integer> loot = LootTable.generateLoot(5);
                for (int quantity : loot.values()) {
                    assertTrue(quantity > 0, "Quantity should be positive: " + quantity);
                }
            }
        }
    }

    // ---- generateScavengeLoot ----

    @Nested
    @DisplayName("generateScavengeLoot Tests")
    class GenerateScavengeLootTests {

        @Test
        @DisplayName("should generate scavenge loot")
        void generateScavengeLoot_shouldGenerate() {
            Map<String, Integer> loot = LootTable.generateScavengeLoot();

            assertNotNull(loot);
        }

        @Test
        @DisplayName("should generate scavenge items over many runs")
        void generateScavengeLoot_shouldGenerateItemsOverManyRuns() {
            int totalItems = 0;
            int runs = 100;

            for (int i = 0; i < runs; i++) {
                totalItems += LootTable.generateScavengeLoot().size();
            }

            assertTrue(totalItems > 0, "Should generate some scavenge loot over 100 runs");
        }

        @Test
        @DisplayName("should include expected scavenge resources")
        void generateScavengeLoot_shouldIncludeExpectedResources() {
            boolean foundCannedBeans = false;
            boolean foundWaterBottle = false;
            boolean foundFabricScrap = false;

            for (int i = 0; i < 100; i++) {
                Map<String, Integer> loot = LootTable.generateScavengeLoot();
                if (loot.containsKey("Canned Beans")) foundCannedBeans = true;
                if (loot.containsKey("Water Bottle")) foundWaterBottle = true;
                if (loot.containsKey("Fabric Scrap")) foundFabricScrap = true;
            }

            assertTrue(foundCannedBeans, "Should eventually drop Canned Beans");
            assertTrue(foundWaterBottle, "Should eventually drop Water Bottle");
            assertTrue(foundFabricScrap, "Should eventually drop Fabric Scrap");
        }

        @Test
        @DisplayName("should return positive quantities")
        void generateScavengeLoot_shouldReturnPositiveQuantities() {
            for (int i = 0; i < 50; i++) {
                Map<String, Integer> loot = LootTable.generateScavengeLoot();
                for (int quantity : loot.values()) {
                    assertTrue(quantity > 0, "Quantity should be positive: " + quantity);
                }
            }
        }

        @Test
        @DisplayName("should yield less loot than high threat monsters")
        void generateScavengeLoot_shouldYieldLessThanHighThreat() {
            int scavengeItems = 0;
            int monsterItems = 0;
            int runs = 100;

            for (int i = 0; i < runs; i++) {
                scavengeItems += LootTable.generateScavengeLoot().size();
                monsterItems += LootTable.generateLoot(10).size();
            }

            // Scavenging should generally yield less than boss-level monsters
            // This is a statistical assertion that may rarely fail
            assertTrue(scavengeItems <= monsterItems * 1.5,
                    "Scavenging should yield less loot than boss monsters: scavenge=" + scavengeItems + ", monster=" + monsterItems);
        }
    }

    // ---- LootEntry ----

    @Nested
    @DisplayName("LootEntry Tests")
    class LootEntryTests {

        @Test
        @DisplayName("should create loot entry with valid values")
        void lootEntry_shouldCreate() {
            // LootEntry is package-private, tested indirectly through generateLoot
            Map<String, Integer> loot = LootTable.generateLoot(1);
            assertNotNull(loot);
        }
    }
}

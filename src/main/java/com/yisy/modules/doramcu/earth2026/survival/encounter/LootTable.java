package com.yisy.modules.doramcu.earth2026.survival.encounter;

import java.util.*;

/**
 * Loot table for survival encounters.
 * <p>
 * Defines what resources monsters and scavenging locations can drop.
 * </p>
 */
public class LootTable {

    /**
     * Loot entry with a resource name and drop chance.
     */
    public static class LootEntry {
        final String resourceName;
        final int minQuantity;
        final int maxQuantity;
        final double dropChance;

        LootEntry(String resourceName, int minQuantity, int maxQuantity, double dropChance) {
            this.resourceName = resourceName;
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.dropChance = dropChance;
        }
    }

    private static final Random random = new Random();

    // Loot tables by threat level
    private static final Map<Integer, List<LootEntry>> LOOT_TABLES = new HashMap<>();

    static {
        // Threat level 1-2: weak enemies
        LOOT_TABLES.put(1, List.of(
                new LootEntry("Fabric Scrap", 1, 2, 0.6),
                new LootEntry("Wild Berries", 1, 1, 0.4),
                new LootEntry("Rope", 1, 1, 0.2)
        ));
        LOOT_TABLES.put(2, List.of(
                new LootEntry("Fabric Scrap", 1, 3, 0.7),
                new LootEntry("Canned Beans", 1, 1, 0.5),
                new LootEntry("Wire", 1, 1, 0.3),
                new LootEntry("Bandages", 1, 1, 0.3)
        ));

        // Threat level 3-4: moderate enemies
        LOOT_TABLES.put(3, List.of(
                new LootEntry("Canned Tuna", 1, 2, 0.6),
                new LootEntry("Water Bottle", 1, 1, 0.5),
                new LootEntry("Duct Tape", 1, 2, 0.4),
                new LootEntry("Metal Sheet", 1, 1, 0.3),
                new LootEntry("Bandages", 1, 2, 0.4)
        ));
        LOOT_TABLES.put(4, List.of(
                new LootEntry("Dried Meat", 1, 2, 0.5),
                new LootEntry("Water Bottle", 1, 2, 0.6),
                new LootEntry("Wood Plank", 2, 4, 0.5),
                new LootEntry("First Aid Kit", 1, 1, 0.3),
                new LootEntry("Flashlight", 1, 1, 0.2)
        ));

        // Threat level 5-6: strong enemies
        LOOT_TABLES.put(5, List.of(
                new LootEntry("Dried Meat", 2, 3, 0.7),
                new LootEntry("Large Water Jug", 1, 1, 0.5),
                new LootEntry("Metal Sheet", 2, 3, 0.5),
                new LootEntry("First Aid Kit", 1, 1, 0.4),
                new LootEntry("Knife", 1, 1, 0.2),
                new LootEntry("Antibiotics", 1, 1, 0.15)
        ));
        LOOT_TABLES.put(6, List.of(
                new LootEntry("Hunted Game", 2, 3, 0.6),
                new LootEntry("Purified Water", 1, 2, 0.5),
                new LootEntry("Concrete Block", 2, 4, 0.4),
                new LootEntry("Antibiotics", 1, 1, 0.3),
                new LootEntry("Firearm", 1, 1, 0.1),
                new LootEntry("Stimulant", 1, 1, 0.15)
        ));

        // Threat level 7-8: very strong enemies
        LOOT_TABLES.put(7, List.of(
                new LootEntry("Hunted Game", 3, 4, 0.8),
                new LootEntry("Purified Water", 2, 3, 0.6),
                new LootEntry("Metal Sheet", 3, 5, 0.6),
                new LootEntry("Antibiotics", 1, 2, 0.5),
                new LootEntry("Stimulant", 1, 1, 0.3),
                new LootEntry("Firearm", 1, 1, 0.2)
        ));
        LOOT_TABLES.put(8, List.of(
                new LootEntry("Hunted Game", 3, 5, 0.8),
                new LootEntry("Purified Water", 2, 3, 0.7),
                new LootEntry("Concrete Block", 3, 5, 0.5),
                new LootEntry("Antibiotics", 2, 3, 0.6),
                new LootEntry("Stimulant", 1, 2, 0.4),
                new LootEntry("Firearm", 1, 1, 0.3)
        ));

        // Threat level 9-10: boss-level enemies
        LOOT_TABLES.put(9, List.of(
                new LootEntry("Hunted Game", 4, 6, 0.9),
                new LootEntry("Purified Water", 3, 4, 0.8),
                new LootEntry("Metal Sheet", 5, 8, 0.7),
                new LootEntry("Antibiotics", 2, 4, 0.7),
                new LootEntry("Stimulant", 2, 3, 0.5),
                new LootEntry("Firearm", 1, 2, 0.4)
        ));
        LOOT_TABLES.put(10, List.of(
                new LootEntry("Hunted Game", 5, 8, 1.0),
                new LootEntry("Purified Water", 4, 6, 0.9),
                new LootEntry("Metal Sheet", 5, 10, 0.8),
                new LootEntry("Antibiotics", 3, 5, 0.8),
                new LootEntry("Stimulant", 2, 4, 0.6),
                new LootEntry("Firearm", 2, 3, 0.5)
        ));
    }

    /**
     * Generate loot drops for a given threat level.
     *
     * @param threatLevel the monster's threat level (1-10)
     * @return map of resource name to quantity
     */
    public static Map<String, Integer> generateLoot(int threatLevel) {
        Map<String, Integer> loot = new HashMap<>();

        // Get the loot table for this threat level
        List<LootEntry> entries = LOOT_TABLES.get(threatLevel);
        if (entries == null) {
            entries = LOOT_TABLES.get(Math.min(10, Math.max(1, threatLevel)));
        }

        for (LootEntry entry : entries) {
            if (random.nextDouble() < entry.dropChance) {
                int quantity = entry.minQuantity + random.nextInt(entry.maxQuantity - entry.minQuantity + 1);
                loot.merge(entry.resourceName, quantity, Integer::sum);
            }
        }

        return loot;
    }

    /**
     * Generate scavenging loot (smaller amounts, common items).
     *
     * @return map of resource name to quantity
     */
    public static Map<String, Integer> generateScavengeLoot() {
        Map<String, Integer> loot = new HashMap<>();

        // Scavenging yields common items
        List<LootEntry> entries = List.of(
                new LootEntry("Canned Beans", 1, 2, 0.5),
                new LootEntry("Water Bottle", 1, 1, 0.4),
                new LootEntry("Fabric Scrap", 1, 3, 0.6),
                new LootEntry("Wood Plank", 1, 2, 0.3),
                new LootEntry("Bandages", 1, 1, 0.3),
                new LootEntry("Duct Tape", 1, 1, 0.2),
                new LootEntry("Rope", 1, 1, 0.2)
        );

        for (LootEntry entry : entries) {
            if (random.nextDouble() < entry.dropChance) {
                int quantity = entry.minQuantity + random.nextInt(entry.maxQuantity - entry.minQuantity + 1);
                loot.merge(entry.resourceName, quantity, Integer::sum);
            }
        }

        return loot;
    }
}

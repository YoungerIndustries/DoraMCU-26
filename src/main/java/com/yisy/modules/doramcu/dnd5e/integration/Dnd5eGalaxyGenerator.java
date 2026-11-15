package com.yisy.modules.doramcu.dnd5e.integration;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.planet.TerrainType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Generates DnD5e-themed regions and dungeons using the GalaxyGenerator pattern.
 * <p>
 * Creates dungeon complexes, ancient ruins, and magical sites
 * instead of space-based galaxy structures.
 * </p>
 */
@Component
public class Dnd5eGalaxyGenerator {

    private static final List<String> DUNGEON_NAMES = Arrays.asList(
            "Dragon's Lair", "Ancient Crypt", "Elven Ruins", "Dwarven Hold",
            "Necromancer's Tower", "Goblin Cave", "Undead Fortress", "Lich's Sanctum",
            "Giant's Keep", "Demon Portal", "Feywild Crossing", "Abyssal Gate"
    );

    private static final List<String> REGION_NAMES = Arrays.asList(
            "Forgotten Forest", "Cursed Swamp", "Dragon Mountains", "Shadowfen",
            "Crystal Caverns", "Haunted Moor", "Volcanic Wastes", "Enchanted Glade"
    );

    private final Random random = new Random();

    /**
     * Generate a DnD5e region with dungeons and points of interest.
     *
     * @param planet the planet to generate on
     * @param seed   random seed for reproducibility
     * @return list of generated hex positions with dungeon data
     */
    public List<Map<String, Object>> generateRegion(Planet planet, long seed) {
        Random rand = new Random(seed);
        List<Map<String, Object>> results = new ArrayList<>();

        // Generate dungeons
        int numDungeons = 3 + rand.nextInt(5);
        for (int i = 0; i < numDungeons; i++) {
            Hex3D hex = generateHex(planet, rand);
            String dungeonName = DUNGEON_NAMES.get(rand.nextInt(DUNGEON_NAMES.size()));

            Map<String, Object> dungeon = new HashMap<>();
            dungeon.put("hex", hex);
            dungeon.put("name", dungeonName);
            dungeon.put("type", "dungeon");
            dungeon.put("levels", 1 + rand.nextInt(5));
            dungeon.put("treasureLevel", rand.nextInt(10) + 1);
            dungeon.put("monsterCR", rand.nextInt(20) + 1);
            results.add(dungeon);
        }

        // Generate regions
        int numRegions = 2 + rand.nextInt(4);
        for (int i = 0; i < numRegions; i++) {
            Hex3D hex = generateHex(planet, rand);
            String regionName = REGION_NAMES.get(rand.nextInt(REGION_NAMES.size()));

            Map<String, Object> region = new HashMap<>();
            region.put("hex", hex);
            region.put("name", regionName);
            region.put("type", "region");
            region.put("terrain", TerrainType.values()[rand.nextInt(TerrainType.values().length)]);
            results.add(region);
        }

        return results;
    }

    private Hex3D generateHex(Planet planet, Random rand) {
        int x = rand.nextInt((int) planet.getRadius()) - (int) planet.getRadius() / 2;
        int y = rand.nextInt((int) planet.getRadius()) - (int) planet.getRadius() / 2;
        int z = -(x + y);
        return new Hex3D(x, y, z);
    }
}

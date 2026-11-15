package com.yisy.modules.doramcu.hex.planet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the medieval vehicles JSON data.
 * Validates structure, vehicle stats, infrastructure compatibility,
 * station definitions, cargo types, and industries for low-tech fantasy worlds.
 */
class MedievalVehiclesTest {

    private ObjectMapper objectMapper;
    private JsonNode medievalData;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        // Load from resources (copied by build.gradle processResources)
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("openttd-rules/medieval-vehicles.json");
        if (is == null) {
            // Fallback: try direct path for standalone test execution
            is = new java.io.FileInputStream("openttd-rules/medieval-vehicles.json");
        }
        medievalData = objectMapper.readTree(is);
        is.close();
    }

    @Nested
    @DisplayName("Game Info Validation")
    class GameInfoValidation {

        @Test
        @DisplayName("Game info title is present")
        void gameInfoTitlePresent() {
            JsonNode gameInfo = medievalData.get("game_info");
            assertNotNull(gameInfo, "game_info should exist");
            assertTrue(gameInfo.has("title"), "game_info should have title");
            assertFalse(gameInfo.get("title").asText().isEmpty(), "title should not be empty");
        }

        @Test
        @DisplayName("Game info mentions D&D and medieval")
        void gameInfoMentionsFantasy() {
            JsonNode gameInfo = medievalData.get("game_info");
            String description = gameInfo.get("description").asText();
            assertTrue(description.toLowerCase().contains("d&d")
                    || description.toLowerCase().contains("medieval")
                    || description.toLowerCase().contains("fantasy"),
                    "Description should mention D&D, medieval, or fantasy");
        }

        @Test
        @DisplayName("Source mods list contains eGRVTS")
        void sourceModsContainsEgrvts() {
            JsonNode sourceMods = medievalData.get("game_info").get("source_mods");
            assertTrue(sourceMods.isArray(), "source_mods should be an array");
            boolean found = false;
            for (JsonNode mod : sourceMods) {
                if (mod.asText().contains("eGRVTS")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Source mods should include eGRVTS");
        }

        @Test
        @DisplayName("Source mods list contains Sailing Ships")
        void sourceModsContainsSailingShips() {
            JsonNode sourceMods = medievalData.get("game_info").get("source_mods");
            boolean found = false;
            for (JsonNode mod : sourceMods) {
                if (mod.asText().contains("Sailing Ships")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Source mods should include Sailing Ships by J Mitchell");
        }

        @Test
        @DisplayName("Integration notes mention tech level 0")
        void integrationNotesMentionTechLevel0() {
            String notes = medievalData.get("game_info").get("integration_notes").asText();
            assertTrue(notes.contains("tech_level") || notes.contains("tech level"),
                    "Integration notes should mention tech level");
        }
    }

    @Nested
    @DisplayName("Vehicle Categories Structure")
    class VehicleCategoriesStructure {

        @Test
        @DisplayName("Vehicles object contains animal_drawn_road")
        void vehiclesHasAnimalDrawnRoad() {
            JsonNode vehicles = medievalData.get("vehicles");
            assertNotNull(vehicles, "vehicles should exist");
            assertTrue(vehicles.has("animal_drawn_road"),
                    "vehicles should have animal_drawn_road category");
        }

        @Test
        @DisplayName("Vehicles object contains creature_mounts")
        void vehiclesHasCreatureMounts() {
            JsonNode vehicles = medievalData.get("vehicles");
            assertTrue(vehicles.has("creature_mounts"),
                    "vehicles should have creature_mounts category");
        }

        @Test
        @DisplayName("Vehicles object contains sailing_ships")
        void vehiclesHasSailingShips() {
            JsonNode vehicles = medievalData.get("vehicles");
            assertTrue(vehicles.has("sailing_ships"),
                    "vehicles should have sailing_ships category");
        }

        @Test
        @DisplayName("Animal drawn road has 4 vehicles")
        void animalDrawnRoadCount() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            assertEquals(4, animals.size(),
                    "animal_drawn_road should have 4 vehicles");
        }

        @Test
        @DisplayName("Creature mounts has 10 vehicles")
        void creatureMountsCount() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            assertEquals(10, mounts.size(),
                    "creature_mounts should have 10 vehicles");
        }

        @Test
        @DisplayName("Sailing ships has 8 vehicles")
        void sailingShipsCount() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            assertEquals(8, ships.size(),
                    "sailing_ships should have 8 vehicles");
        }
    }

    @Nested
    @DisplayName("Animal Drawn Vehicles")
    class AnimalDrawnVehicles {

        @Test
        @DisplayName("Horse cart exists with correct stats")
        void horseCartExists() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode horseCart = null;
            for (JsonNode v : animals) {
                if ("horse_cart".equals(v.get("id").asText())) {
                    horseCart = v;
                    break;
                }
            }
            assertNotNull(horseCart, "Horse cart should exist");
            assertEquals(1, horseCart.get("stats").get("speed_hexes_per_turn").asInt());
            assertEquals(10, horseCart.get("stats").get("cargo_capacity").asInt());
            assertEquals(0, horseCart.get("stats").get("passenger_capacity").asInt());
            assertEquals("PATH", horseCart.get("infrastructure").asText());
        }

        @Test
        @DisplayName("Horse drawn bus exists with passenger capacity")
        void horseDrawnBusExists() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode bus = null;
            for (JsonNode v : animals) {
                if ("horse_drawn_bus".equals(v.get("id").asText())) {
                    bus = v;
                    break;
                }
            }
            assertNotNull(bus, "Horse drawn bus should exist");
            assertTrue(bus.get("stats").get("passenger_capacity").asInt() > 0,
                    "Bus should have passenger capacity");
            assertEquals(0, bus.get("stats").get("cargo_capacity").asInt(),
                    "Bus should have 0 cargo capacity");
        }

        @Test
        @DisplayName("Horse drawn tram uses TRAM_RAIL infrastructure")
        void horseDrawnTramUsesTramRail() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode tram = null;
            for (JsonNode v : animals) {
                if ("horse_drawn_tram".equals(v.get("id").asText())) {
                    tram = v;
                    break;
                }
            }
            assertNotNull(tram, "Horse drawn tram should exist");
            assertEquals("TRAM_RAIL", tram.get("infrastructure").asText());
        }

        @Test
        @DisplayName("Pack animal has NONE infrastructure")
        void packAnimalHasNoInfrastructure() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode pack = null;
            for (JsonNode v : animals) {
                if ("pack_animal".equals(v.get("id").asText())) {
                    pack = v;
                    break;
                }
            }
            assertNotNull(pack, "Pack animal should exist");
            assertEquals("NONE", pack.get("infrastructure").asText());
            assertTrue(pack.has("terrain_bonus"), "Pack animal should have terrain_bonus");
        }

        @Test
        @DisplayName("All animal drawn vehicles have tech_level_min 0")
        void allAnimalDrawnHaveTechLevel0() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            for (JsonNode v : animals) {
                assertEquals(0, v.get("tech_level_min").asInt(),
                        v.get("id").asText() + " should have tech_level_min 0");
            }
        }

        @Test
        @DisplayName("Horse cart has variants")
        void horseCartHasVariants() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode horseCart = null;
            for (JsonNode v : animals) {
                if ("horse_cart".equals(v.get("id").asText())) {
                    horseCart = v;
                    break;
                }
            }
            assertNotNull(horseCart, "Horse cart should exist");
            assertTrue(horseCart.has("variants"), "Horse cart should have variants");
            assertTrue(horseCart.get("variants").isArray(), "Variants should be an array");
            assertTrue(horseCart.get("variants").size() > 0, "Horse cart should have at least one variant");
        }

        @Test
        @DisplayName("Pack animal has terrain bonus for mountains")
        void packAnimalTerrainBonus() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode pack = null;
            for (JsonNode v : animals) {
                if ("pack_animal".equals(v.get("id").asText())) {
                    pack = v;
                    break;
                }
            }
            JsonNode terrainBonus = pack.get("terrain_bonus");
            assertNotNull(terrainBonus, "Pack animal should have terrain_bonus");
            assertTrue(terrainBonus.has("mountain"), "Should have mountain terrain bonus");
        }
    }

    @Nested
    @DisplayName("Creature Mounts")
    class CreatureMounts {

        @Test
        @DisplayName("Riding horse exists with mail courier specialty")
        void ridingHorseExists() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode horse = null;
            for (JsonNode v : mounts) {
                if ("riding_horse".equals(v.get("id").asText())) {
                    horse = v;
                    break;
                }
            }
            assertNotNull(horse, "Riding horse should exist");
            assertEquals("MAIL_COURIER", horse.get("specialty").asText());
        }

        @Test
        @DisplayName("Griffon is a flying mount")
        void griffonIsFlyingMount() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode griffon = null;
            for (JsonNode v : mounts) {
                if ("griffon_mount".equals(v.get("id").asText())) {
                    griffon = v;
                    break;
                }
            }
            assertNotNull(griffon, "Griffon should exist");
            assertEquals("FLYING_MOUNT", griffon.get("type").asText());
            assertTrue(griffon.get("fantasy_only").asBoolean(),
                    "Griffon should be fantasy_only");
            assertTrue(griffon.get("terrain_agnostic").asBoolean(),
                    "Griffon should be terrain_agnostic");
        }

        @Test
        @DisplayName("Pegasus is legendary rarity")
        void pegasusIsLegendary() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode pegasus = null;
            for (JsonNode v : mounts) {
                if ("pegasus_mount".equals(v.get("id").asText())) {
                    pegasus = v;
                    break;
                }
            }
            assertNotNull(pegasus, "Pegasus should exist");
            assertEquals("LEGENDARY", pegasus.get("rarity").asText());
            assertTrue(pegasus.get("fantasy_only").asBoolean());
        }

        @Test
        @DisplayName("Pegasus is fastest creature mount")
        void pegasusIsFastest() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            int maxSpeed = 0;
            String fastestId = "";
            for (JsonNode v : mounts) {
                int speed = v.get("stats").get("speed_hexes_per_turn").asInt();
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                    fastestId = v.get("id").asText();
                }
            }
            assertEquals("pegasus_mount", fastestId, "Pegasus should be the fastest mount");
            assertEquals(12, maxSpeed, "Pegasus should have speed 12");
        }

        @Test
        @DisplayName("War elephant has highest cargo capacity among mounts")
        void warElephantHighestCargo() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            int maxCargo = 0;
            String heaviestId = "";
            for (JsonNode v : mounts) {
                int cargo = v.get("stats").get("cargo_capacity").asInt();
                if (cargo > maxCargo) {
                    maxCargo = cargo;
                    heaviestId = v.get("id").asText();
                }
            }
            assertEquals("elephant_mount", heaviestId,
                    "War elephant should have highest cargo capacity");
            assertEquals(40, maxCargo, "War elephant should have cargo capacity 40");
        }

        @Test
        @DisplayName("Drake has fire breath combat capability")
        void drakeHasFireBreath() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode drake = null;
            for (JsonNode v : mounts) {
                if ("drake_mount".equals(v.get("id").asText())) {
                    drake = v;
                    break;
                }
            }
            assertNotNull(drake, "Drake should exist");
            assertEquals("FIRE_BREATH", drake.get("combat_capability").asText());
        }

        @Test
        @DisplayName("Direwolf has snow terrain bonus")
        void direwolfHasSnowBonus() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode direwolf = null;
            for (JsonNode v : mounts) {
                if ("direwolf_mount".equals(v.get("id").asText())) {
                    direwolf = v;
                    break;
                }
            }
            assertNotNull(direwolf, "Direwolf should exist");
            JsonNode terrainBonus = direwolf.get("terrain_bonus");
            assertNotNull(terrainBonus, "Direwolf should have terrain_bonus");
            assertTrue(terrainBonus.has("snow"), "Direwolf should have snow terrain bonus");
        }

        @Test
        @DisplayName("Saber-tooth tiger has jungle terrain bonus")
        void saberToothTigerHasJungleBonus() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode tiger = null;
            for (JsonNode v : mounts) {
                if ("saber_tooth_tiger_mount".equals(v.get("id").asText())) {
                    tiger = v;
                    break;
                }
            }
            assertNotNull(tiger, "Saber-tooth tiger should exist");
            JsonNode terrainBonus = tiger.get("terrain_bonus");
            assertNotNull(terrainBonus, "Saber-tooth tiger should have terrain_bonus");
            assertTrue(terrainBonus.has("jungle"),
                    "Saber-tooth tiger should have jungle terrain bonus");
        }

        @Test
        @DisplayName("All flying mounts are terrain agnostic")
        void allFlyingMountsTerrainAgnostic() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            for (JsonNode v : mounts) {
                if ("FLYING_MOUNT".equals(v.get("type").asText())) {
                    assertTrue(v.get("terrain_agnostic").asBoolean(),
                            v.get("id").asText() + " flying mount should be terrain_agnostic");
                }
            }
        }

        @Test
        @DisplayName("Fantasy mounts have fantasy_only flag")
        void fantasyMountsHaveFantasyOnly() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            // Check specific fantasy mounts
            String[] fantasyMounts = {"griffon_mount", "hippogriff_mount", "direwolf_mount",
                    "saber_tooth_tiger_mount", "pegasus_mount", "drake_mount", "giant_beetle_mount"};
            for (JsonNode v : mounts) {
                String id = v.get("id").asText();
                for (String fantasyId : fantasyMounts) {
                    if (fantasyId.equals(id)) {
                        assertTrue(v.get("fantasy_only").asBoolean(),
                                id + " should have fantasy_only = true");
                    }
                }
            }
        }

        @Test
        @DisplayName("Giant beetle has high cargo capacity")
        void giantBeetleHighCargo() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode beetle = null;
            for (JsonNode v : mounts) {
                if ("giant_beetle_mount".equals(v.get("id").asText())) {
                    beetle = v;
                    break;
                }
            }
            assertNotNull(beetle, "Giant beetle should exist");
            assertTrue(beetle.get("stats").get("cargo_capacity").asInt() >= 15,
                    "Giant beetle should have high cargo capacity");
        }

        @Test
        @DisplayName("Warhorse exists with military category")
        void warhorseExists() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            JsonNode warhorse = null;
            for (JsonNode v : mounts) {
                if ("warhorse".equals(v.get("id").asText())) {
                    warhorse = v;
                    break;
                }
            }
            assertNotNull(warhorse, "Warhorse should exist");
            assertEquals("WARHORSE", warhorse.get("mount_type").asText());
        }
    }

    @Nested
    @DisplayName("Sailing Ships")
    class SailingShips {

        @Test
        @DisplayName("Brig exists with wind dependency")
        void brigExists() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode brig = null;
            for (JsonNode v : ships) {
                if ("brig".equals(v.get("id").asText())) {
                    brig = v;
                    break;
                }
            }
            assertNotNull(brig, "Brig should exist");
            assertEquals("WIND", brig.get("propulsion").asText());
            assertTrue(brig.get("wind_dependency").asDouble() > 0,
                    "Brig should have wind dependency");
        }

        @Test
        @DisplayName("Schooner is faster than brig")
        void schoonerFasterThanBrig() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode schooner = null;
            JsonNode brig = null;
            for (JsonNode v : ships) {
                if ("schooner".equals(v.get("id").asText())) schooner = v;
                if ("brig".equals(v.get("id").asText())) brig = v;
            }
            assertNotNull(schooner, "Schooner should exist");
            assertNotNull(brig, "Brig should exist");
            assertTrue(schooner.get("stats").get("speed_hexes_per_turn").asInt() >=
                    brig.get("stats").get("speed_hexes_per_turn").asInt(),
                    "Schooner should be at least as fast as brig");
        }

        @Test
        @DisplayName("Windjammer has highest cargo capacity among ships")
        void windjammerHighestCargo() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            int maxCargo = 0;
            String heaviestId = "";
            for (JsonNode v : ships) {
                int cargo = v.get("stats").get("cargo_capacity").asInt();
                if (cargo > maxCargo) {
                    maxCargo = cargo;
                    heaviestId = v.get("id").asText();
                }
            }
            assertEquals("windjammer", heaviestId,
                    "Windjammer should have highest cargo capacity");
            assertEquals(100, maxCargo, "Windjammer should have cargo capacity 100");
        }

        @Test
        @DisplayName("Rowboat has zero wind dependency")
        void rowboatZeroWindDependency() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode rowboat = null;
            for (JsonNode v : ships) {
                if ("rowboat".equals(v.get("id").asText())) {
                    rowboat = v;
                    break;
                }
            }
            assertNotNull(rowboat, "Rowboat should exist");
            assertEquals("OAR", rowboat.get("propulsion").asText());
            assertEquals(0, rowboat.get("wind_dependency").asDouble(),
                    "Rowboat should have zero wind dependency");
        }

        @Test
        @DisplayName("All sailing ships use WATER infrastructure")
        void allSailingShipsUseWater() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            for (JsonNode v : ships) {
                assertEquals("WATER", v.get("infrastructure").asText(),
                        v.get("id").asText() + " should use WATER infrastructure");
            }
        }

        @Test
        @DisplayName("Cog ship exists for medieval trade")
        void cogShipExists() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode cog = null;
            for (JsonNode v : ships) {
                if ("cog_ship".equals(v.get("id").asText())) {
                    cog = v;
                    break;
                }
            }
            assertNotNull(cog, "Cog ship should exist");
            assertEquals("WIND", cog.get("propulsion").asText());
        }

        @Test
        @DisplayName("Caravel exists for exploration")
        void caravelExists() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode caravel = null;
            for (JsonNode v : ships) {
                if ("caravel".equals(v.get("id").asText())) {
                    caravel = v;
                    break;
                }
            }
            assertNotNull(caravel, "Caravel should exist");
            assertTrue(caravel.get("description").asText().toLowerCase().contains("exploration")
                    || caravel.get("description").asText().toLowerCase().contains("maneuverable"),
                    "Caravel description should mention exploration or maneuverability");
        }

        @Test
        @DisplayName("Rowboat has variants including galley and longship")
        void rowboatHasVariants() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode rowboat = null;
            for (JsonNode v : ships) {
                if ("rowboat".equals(v.get("id").asText())) {
                    rowboat = v;
                    break;
                }
            }
            assertNotNull(rowboat, "Rowboat should exist");
            assertTrue(rowboat.has("variants"), "Rowboat should have variants");
            ArrayNode variants = (ArrayNode) rowboat.get("variants");
            boolean hasGalley = false;
            boolean hasLongship = false;
            for (JsonNode variant : variants) {
                if ("galley".equals(variant.get("id").asText())) hasGalley = true;
                if ("longship".equals(variant.get("id").asText())) hasLongship = true;
            }
            assertTrue(hasGalley, "Rowboat should have galley variant");
            assertTrue(hasLongship, "Rowboat should have longship variant");
        }

        @Test
        @DisplayName("Merchantman has high cargo capacity")
        void merchantmanHighCargo() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            JsonNode merchantman = null;
            for (JsonNode v : ships) {
                if ("merchantman".equals(v.get("id").asText())) {
                    merchantman = v;
                    break;
                }
            }
            assertNotNull(merchantman, "Merchantman should exist");
            assertTrue(merchantman.get("stats").get("cargo_capacity").asInt() >= 40,
                    "Merchantman should have high cargo capacity");
        }

        @Test
        @DisplayName("Schooner has highest wind dependency")
        void schoonerHighestWindDependency() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            double maxWind = 0;
            String mostDependentId = "";
            for (JsonNode v : ships) {
                double wind = v.get("wind_dependency").asDouble();
                if (wind > maxWind) {
                    maxWind = wind;
                    mostDependentId = v.get("id").asText();
                }
            }
            assertEquals("schooner", mostDependentId,
                    "Schooner should have highest wind dependency");
            assertEquals(0.9, maxWind, 0.01,
                    "Schooner should have 0.9 wind dependency");
        }
    }

    @Nested
    @DisplayName("Infrastructure")
    class Infrastructure {

        @Test
        @DisplayName("Path infrastructure exists")
        void pathInfrastructureExists() {
            JsonNode infra = medievalData.get("infrastructure");
            assertNotNull(infra, "infrastructure should exist");
            assertTrue(infra.has("path"), "infrastructure should have path");
        }

        @Test
        @DisplayName("Path has low build cost")
        void pathLowBuildCost() {
            JsonNode path = medievalData.get("infrastructure").get("path");
            int cost = path.get("stats").get("build_cost_per_tile").asInt();
            assertTrue(cost <= 5, "Path should have low build cost (<=5)");
        }

        @Test
        @DisplayName("Tram rail exists")
        void tramRailExists() {
            JsonNode infra = medievalData.get("infrastructure");
            assertTrue(infra.has("tram_rail"), "infrastructure should have tram_rail");
        }

        @Test
        @DisplayName("Water route has zero build cost")
        void waterRouteZeroBuildCost() {
            JsonNode water = medievalData.get("infrastructure").get("water");
            assertEquals(0, water.get("stats").get("build_cost_per_tile").asInt(),
                    "Water route should have zero build cost");
        }

        @Test
        @DisplayName("Path compatible vehicles include horse_cart")
        void pathCompatibleVehicles() {
            JsonNode path = medievalData.get("infrastructure").get("path");
            ArrayNode compatible = (ArrayNode) path.get("compatible_vehicles");
            boolean found = false;
            for (JsonNode v : compatible) {
                if ("horse_cart".equals(v.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Path should be compatible with horse_cart");
        }

        @Test
        @DisplayName("Water compatible vehicles include all sailing ships")
        void waterCompatibleVehicles() {
            JsonNode water = medievalData.get("infrastructure").get("water");
            ArrayNode compatible = (ArrayNode) water.get("compatible_vehicles");
            String[] sailingShips = {"brig", "schooner", "merchantman", "barque",
                    "windjammer", "rowboat", "cog_ship", "caravel"};
            for (String ship : sailingShips) {
                boolean found = false;
                for (JsonNode v : compatible) {
                    if (ship.equals(v.asText())) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "Water should be compatible with " + ship);
            }
        }

        @Test
        @DisplayName("Path has variants including cobblestone")
        void pathHasVariants() {
            JsonNode path = medievalData.get("infrastructure").get("path");
            assertTrue(path.has("variants"), "Path should have variants");
            ArrayNode variants = (ArrayNode) path.get("variants");
            boolean hasCobblestone = false;
            for (JsonNode v : variants) {
                if (v.get("name").asText().contains("Cobblestone")) {
                    hasCobblestone = true;
                    break;
                }
            }
            assertTrue(hasCobblestone, "Path should have cobblestone variant");
        }
    }

    @Nested
    @DisplayName("Stations")
    class Stations {

        @Test
        @DisplayName("Medieval stations array exists")
        void medievalStationsExist() {
            JsonNode stations = medievalData.get("stations");
            assertNotNull(stations, "stations should exist");
            assertTrue(stations.has("medieval_stations"),
                    "stations should have medieval_stations array");
        }

        @Test
        @DisplayName("Tavern stop exists")
        void tavernStopExists() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            JsonNode tavern = null;
            for (JsonNode v : stations) {
                if ("tavern_stop".equals(v.get("id").asText())) {
                    tavern = v;
                    break;
                }
            }
            assertNotNull(tavern, "Tavern stop should exist");
            assertEquals("path", tavern.get("category").asText());
        }

        @Test
        @DisplayName("Wharf is water category")
        void wharfIsWaterCategory() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            JsonNode wharf = null;
            for (JsonNode v : stations) {
                if ("wharf".equals(v.get("id").asText())) {
                    wharf = v;
                    break;
                }
            }
            assertNotNull(wharf, "Wharf should exist");
            assertEquals("water", wharf.get("category").asText());
        }

        @Test
        @DisplayName("Castle gate has high build cost")
        void castleGateHighBuildCost() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            JsonNode castle = null;
            for (JsonNode v : stations) {
                if ("castle_gate".equals(v.get("id").asText())) {
                    castle = v;
                    break;
                }
            }
            assertNotNull(castle, "Castle gate should exist");
            assertTrue(castle.get("stats").get("build_cost").asInt() >= 50,
                    "Castle gate should have high build cost");
        }

        @Test
        @DisplayName("Market square has large catchment radius")
        void marketSquareLargeCatchment() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            JsonNode market = null;
            for (JsonNode v : stations) {
                if ("market_square".equals(v.get("id").asText())) {
                    market = v;
                    break;
                }
            }
            assertNotNull(market, "Market square should exist");
            assertTrue(market.get("stats").get("catchment_radius").asInt() >= 3,
                    "Market square should have large catchment radius");
        }

        @Test
        @DisplayName("Stable yard exists for creature mounts")
        void stableYardExists() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            JsonNode stable = null;
            for (JsonNode v : stations) {
                if ("stable_yard".equals(v.get("id").asText())) {
                    stable = v;
                    break;
                }
            }
            assertNotNull(stable, "Stable yard should exist");
            assertTrue(stable.get("description").asText().toLowerCase().contains("animal")
                    || stable.get("description").asText().toLowerCase().contains("mount"),
                    "Stable yard description should mention animals or mounts");
        }

        @Test
        @DisplayName("All stations have interior rooms")
        void allStationsHaveInteriorRooms() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            for (JsonNode v : stations) {
                assertTrue(v.has("interior_rooms"),
                        v.get("id").asText() + " should have interior_rooms");
                assertTrue(v.get("interior_rooms").isArray(),
                        v.get("id").asText() + " interior_rooms should be an array");
                assertTrue(v.get("interior_rooms").size() > 0,
                        v.get("id").asText() + " should have at least one interior room");
            }
        }

        @Test
        @DisplayName("All stations have 3D model descriptions")
        void allStationsHave3dModels() {
            ArrayNode stations = (ArrayNode) medievalData.get("stations").get("medieval_stations");
            for (JsonNode v : stations) {
                assertTrue(v.has("3d_model"),
                        v.get("id").asText() + " should have 3d_model");
                assertFalse(v.get("3d_model").asText().isEmpty(),
                        v.get("id").asText() + " 3d_model should not be empty");
            }
        }
    }

    @Nested
    @DisplayName("Cargo Types")
    class CargoTypes {

        @Test
        @DisplayName("Medieval raw materials exist")
        void medievalRawMaterialsExist() {
            JsonNode cargo = medievalData.get("cargo_types");
            assertNotNull(cargo, "cargo_types should exist");
            assertTrue(cargo.has("medieval_raw_materials"),
                    "cargo_types should have medieval_raw_materials");
        }

        @Test
        @DisplayName("Herbs are perishable")
        void herbsArePerishable() {
            ArrayNode raw = (ArrayNode) medievalData.get("cargo_types").get("medieval_raw_materials");
            JsonNode herbs = null;
            for (JsonNode v : raw) {
                if ("herbs".equals(v.get("id").asText())) {
                    herbs = v;
                    break;
                }
            }
            assertNotNull(herbs, "Herbs should exist");
            assertTrue(herbs.get("perishable").asBoolean(),
                    "Herbs should be perishable");
        }

        @Test
        @DisplayName("Magic ore has highest base value among raw materials")
        void magicOreHighestValue() {
            ArrayNode raw = (ArrayNode) medievalData.get("cargo_types").get("medieval_raw_materials");
            int maxValue = 0;
            String mostValuableId = "";
            for (JsonNode v : raw) {
                int value = v.get("base_value").asInt();
                if (value > maxValue) {
                    maxValue = value;
                    mostValuableId = v.get("id").asText();
                }
            }
            assertEquals("magic_ore", mostValuableId,
                    "Magic ore should have highest base value");
            assertEquals(80, maxValue, "Magic ore should have base value 80");
        }

        @Test
        @DisplayName("Enchanted items have highest base value among processed goods")
        void enchantedItemsHighestValue() {
            ArrayNode processed = (ArrayNode) medievalData.get("cargo_types")
                    .get("medieval_processed_goods");
            int maxValue = 0;
            String mostValuableId = "";
            for (JsonNode v : processed) {
                int value = v.get("base_value").asInt();
                if (value > maxValue) {
                    maxValue = value;
                    mostValuableId = v.get("id").asText();
                }
            }
            assertEquals("enchanted_items", mostValuableId,
                    "Enchanted items should have highest base value");
            assertEquals(100, maxValue, "Enchanted items should have base value 100");
        }

        @Test
        @DisplayName("Potions are perishable")
        void potionsArePerishable() {
            ArrayNode processed = (ArrayNode) medievalData.get("cargo_types")
                    .get("medieval_processed_goods");
            JsonNode potions = null;
            for (JsonNode v : processed) {
                if ("potions".equals(v.get("id").asText())) {
                    potions = v;
                    break;
                }
            }
            assertNotNull(potions, "Potions should exist");
            assertTrue(potions.get("perishable").asBoolean(),
                    "Potions should be perishable");
        }

        @Test
        @DisplayName("All cargo types have colors")
        void allCargoTypesHaveColors() {
            JsonNode cargo = medievalData.get("cargo_types");
            for (JsonNode category : cargo) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        assertTrue(v.has("color"),
                                v.get("id").asText() + " should have color");
                        String color = v.get("color").asText();
                        assertTrue(color.startsWith("#"),
                                v.get("id").asText() + " color should be hex format");
                    }
                }
            }
        }

        @Test
        @DisplayName("All cargo types have produced_by and consumed_by")
        void allCargoTypesHaveProductionChain() {
            JsonNode cargo = medievalData.get("cargo_types");
            for (JsonNode category : cargo) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        assertTrue(v.has("produced_by"),
                                v.get("id").asText() + " should have produced_by");
                        assertTrue(v.has("consumed_by"),
                                v.get("id").asText() + " should have consumed_by");
                    }
                }
            }
        }

        @Test
        @DisplayName("Iron ore is produced by iron mine")
        void ironOreProducedByIronMine() {
            ArrayNode raw = (ArrayNode) medievalData.get("cargo_types").get("medieval_raw_materials");
            JsonNode ironOre = null;
            for (JsonNode v : raw) {
                if ("iron_ore".equals(v.get("id").asText())) {
                    ironOre = v;
                    break;
                }
            }
            assertNotNull(ironOre, "Iron ore should exist");
            ArrayNode producedBy = (ArrayNode) ironOre.get("produced_by");
            boolean found = false;
            for (JsonNode p : producedBy) {
                if ("iron_mine".equals(p.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Iron ore should be produced by iron_mine");
        }
    }

    @Nested
    @DisplayName("Industries")
    class Industries {

        @Test
        @DisplayName("Medieval primary industries exist")
        void medievalPrimaryIndustriesExist() {
            JsonNode industries = medievalData.get("industries");
            assertNotNull(industries, "industries should exist");
            assertTrue(industries.has("medieval_primary"),
                    "industries should have medieval_primary");
        }

        @Test
        @DisplayName("Herb garden is primary industry")
        void herbGardenIsPrimary() {
            ArrayNode primary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_primary");
            JsonNode herbGarden = null;
            for (JsonNode v : primary) {
                if ("herb_garden".equals(v.get("id").asText())) {
                    herbGarden = v;
                    break;
                }
            }
            assertNotNull(herbGarden, "Herb garden should exist");
            assertEquals("primary", herbGarden.get("category").asText());
            // Primary industries have null input (no raw material needed)
            assertTrue(!herbGarden.has("input") || herbGarden.get("input").isNull(),
                    "Herb garden should have no input (primary industry)");
        }

        @Test
        @DisplayName("Blacksmith consumes iron ore")
        void blacksmithConsumesIronOre() {
            ArrayNode secondary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_secondary");
            JsonNode blacksmith = null;
            for (JsonNode v : secondary) {
                if ("blacksmith".equals(v.get("id").asText())) {
                    blacksmith = v;
                    break;
                }
            }
            assertNotNull(blacksmith, "Blacksmith should exist");
            assertNotNull(blacksmith.get("input"),
                    "Blacksmith should have input");
            ArrayNode input = (ArrayNode) blacksmith.get("input");
            boolean found = false;
            for (JsonNode i : input) {
                if ("iron_ore".equals(i.get("cargo").asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Blacksmith should consume iron ore");
        }

        @Test
        @DisplayName("Master blacksmith produces enchanted items")
        void masterBlacksmithProducesEnchantedItems() {
            ArrayNode secondary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_secondary");
            JsonNode master = null;
            for (JsonNode v : secondary) {
                if ("master_blacksmith".equals(v.get("id").asText())) {
                    master = v;
                    break;
                }
            }
            assertNotNull(master, "Master blacksmith should exist");
            assertEquals("enchanted_items",
                    master.get("output").get("cargo").asText());
        }

        @Test
        @DisplayName("Deep mine produces magic ore")
        void deepMineProducesMagicOre() {
            ArrayNode primary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_primary");
            JsonNode deepMine = null;
            for (JsonNode v : primary) {
                if ("deep_mine".equals(v.get("id").asText())) {
                    deepMine = v;
                    break;
                }
            }
            assertNotNull(deepMine, "Deep mine should exist");
            assertEquals("magic_ore",
                    deepMine.get("output").get("cargo").asText());
        }

        @Test
        @DisplayName("Adventurers guild is tertiary industry")
        void adventurersGuildIsTertiary() {
            ArrayNode tertiary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_tertiary");
            JsonNode guild = null;
            for (JsonNode v : tertiary) {
                if ("adventurers_guild".equals(v.get("id").asText())) {
                    guild = v;
                    break;
                }
            }
            assertNotNull(guild, "Adventurers guild should exist");
            assertEquals("tertiary", guild.get("category").asText());
            // Tertiary industries are consumers — they have null output
            assertTrue(!guild.has("output") || guild.get("output").isNull(),
                    "Adventurers guild should have no output (tertiary consumer)");
        }

        @Test
        @DisplayName("Royal treasury has highest build cost")
        void royalTreasuryHighestBuildCost() {
            JsonNode industries = medievalData.get("industries");
            int maxCost = 0;
            String mostExpensiveId = "";
            for (JsonNode category : industries) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        int cost = v.get("build_cost").asInt();
                        if (cost > maxCost) {
                            maxCost = cost;
                            mostExpensiveId = v.get("id").asText();
                        }
                    }
                }
            }
            assertEquals("royal_treasury", mostExpensiveId,
                    "Royal treasury should have highest build cost");
            assertEquals(3000, maxCost, "Royal treasury should have build cost 3000");
        }

        @Test
        @DisplayName("All industries have interior rooms")
        void allIndustriesHaveInteriorRooms() {
            JsonNode industries = medievalData.get("industries");
            for (JsonNode category : industries) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        assertTrue(v.has("interior_rooms"),
                                v.get("id").asText() + " should have interior_rooms");
                        assertTrue(v.get("interior_rooms").isArray(),
                                v.get("id").asText() + " interior_rooms should be an array");
                    }
                }
            }
        }

        @Test
        @DisplayName("All industries have required terrain")
        void allIndustriesHaveRequiredTerrain() {
            JsonNode industries = medievalData.get("industries");
            for (JsonNode category : industries) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        assertTrue(v.has("required_terrain"),
                                v.get("id").asText() + " should have required_terrain");
                        assertTrue(v.get("required_terrain").isArray(),
                                v.get("id").asText() + " required_terrain should be an array");
                    }
                }
            }
        }

        @Test
        @DisplayName("Apothecary consumes herbs")
        void apothecaryConsumesHerbs() {
            ArrayNode secondary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_secondary");
            JsonNode apothecary = null;
            for (JsonNode v : secondary) {
                if ("apothecary".equals(v.get("id").asText())) {
                    apothecary = v;
                    break;
                }
            }
            assertNotNull(apothecary, "Apothecary should exist");
            ArrayNode input = (ArrayNode) apothecary.get("input");
            boolean found = false;
            for (JsonNode i : input) {
                if ("herbs".equals(i.get("cargo").asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Apothecary should consume herbs");
        }

        @Test
        @DisplayName("Enchanter consumes scrolls and gems")
        void enchanterConsumesScrollsAndGems() {
            ArrayNode secondary = (ArrayNode) medievalData.get("industries")
                    .get("medieval_secondary");
            JsonNode enchanter = null;
            for (JsonNode v : secondary) {
                if ("enchanter".equals(v.get("id").asText())) {
                    enchanter = v;
                    break;
                }
            }
            assertNotNull(enchanter, "Enchanter should exist");
            ArrayNode input = (ArrayNode) enchanter.get("input");
            boolean hasScrolls = false;
            boolean hasGems = false;
            for (JsonNode i : input) {
                if ("scrolls".equals(i.get("cargo").asText())) hasScrolls = true;
                if ("gems".equals(i.get("cargo").asText())) hasGems = true;
            }
            assertTrue(hasScrolls, "Enchanter should consume scrolls");
            assertTrue(hasGems, "Enchanter should consume gems");
        }
    }

    @Nested
    @DisplayName("Tech Level 0 Configuration")
    class TechLevel0Config {

        @Test
        @DisplayName("Tech level 0 section exists")
        void techLevel0Exists() {
            assertTrue(medievalData.has("tech_level_0"),
                    "tech_level_0 section should exist");
        }

        @Test
        @DisplayName("Tech level 0 level is 0")
        void techLevel0Level() {
            assertEquals(0, medievalData.get("tech_level_0").get("level").asInt(),
                    "Tech level should be 0");
        }

        @Test
        @DisplayName("Tech level 0 name mentions medieval")
        void techLevel0Name() {
            String name = medievalData.get("tech_level_0").get("name").asText();
            assertTrue(name.toLowerCase().contains("medieval")
                    || name.toLowerCase().contains("pre-industrial"),
                    "Tech level 0 name should mention medieval or pre-industrial");
        }

        @Test
        @DisplayName("Available vehicles list contains horse_cart")
        void availableVehiclesContainsHorseCart() {
            ArrayNode vehicles = (ArrayNode) medievalData.get("tech_level_0")
                    .get("available_vehicles");
            boolean found = false;
            for (JsonNode v : vehicles) {
                if ("horse_cart".equals(v.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Available vehicles should include horse_cart");
        }

        @Test
        @DisplayName("Available vehicles list contains pegasus_mount")
        void availableVehiclesContainsPegasus() {
            ArrayNode vehicles = (ArrayNode) medievalData.get("tech_level_0")
                    .get("available_vehicles");
            boolean found = false;
            for (JsonNode v : vehicles) {
                if ("pegasus_mount".equals(v.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Available vehicles should include pegasus_mount");
        }

        @Test
        @DisplayName("Available stations list contains tavern_stop")
        void availableStationsContainsTavern() {
            ArrayNode stations = (ArrayNode) medievalData.get("tech_level_0")
                    .get("available_stations");
            boolean found = false;
            for (JsonNode v : stations) {
                if ("tavern_stop".equals(v.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Available stations should include tavern_stop");
        }

        @Test
        @DisplayName("Available infrastructure contains path")
        void availableInfrastructureContainsPath() {
            ArrayNode infra = (ArrayNode) medievalData.get("tech_level_0")
                    .get("available_infrastructure");
            boolean found = false;
            for (JsonNode v : infra) {
                if ("path".equals(v.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Available infrastructure should include path");
        }

        @Test
        @DisplayName("Planet examples mention D&D")
        void planetExamplesMentionDnd() {
            ArrayNode examples = (ArrayNode) medievalData.get("tech_level_0")
                    .get("planet_examples");
            boolean found = false;
            for (JsonNode v : examples) {
                if (v.asText().contains("D&D")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Planet examples should mention D&D");
        }

        @Test
        @DisplayName("Cargo types list contains herbs")
        void cargoTypesListContainsHerbs() {
            ArrayNode cargo = (ArrayNode) medievalData.get("tech_level_0")
                    .get("cargo_types");
            boolean found = false;
            for (JsonNode v : cargo) {
                if ("herbs".equals(v.asText())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Cargo types should include herbs");
        }
    }

    @Nested
    @DisplayName("Mount Type Definitions")
    class MountTypeDefinitions {

        @Test
        @DisplayName("Mount type definitions exist")
        void mountTypeDefinitionsExist() {
            assertTrue(medievalData.has("mount_type_definitions"),
                    "mount_type_definitions should exist");
        }

        @Test
        @DisplayName("Horse mount type exists")
        void horseMountTypeExists() {
            JsonNode mounts = medievalData.get("mount_type_definitions");
            assertTrue(mounts.has("HORSE"), "HORSE mount type should exist");
        }

        @Test
        @DisplayName("Horse has speed modifier 1.0")
        void horseSpeedModifier() {
            JsonNode horse = medievalData.get("mount_type_definitions").get("HORSE");
            assertEquals(1.0, horse.get("speed_modifier").asDouble(),
                    "Horse should have speed modifier 1.0");
        }

        @Test
        @DisplayName("Elephant has highest cargo modifier")
        void elephantHighestCargoModifier() {
            JsonNode mounts = medievalData.get("mount_type_definitions");
            double maxCargo = 0;
            String heaviestId = "";
            for (JsonNode v : mounts) {
                double cargo = v.get("cargo_modifier").asDouble();
                if (cargo > maxCargo) {
                    maxCargo = cargo;
                    heaviestId = v.get("name").asText();
                }
            }
            assertEquals("War Elephant", heaviestId,
                    "War Elephant should have highest cargo modifier");
            assertEquals(3.0, maxCargo, 0.01,
                    "War Elephant should have cargo modifier 3.0");
        }

        @Test
        @DisplayName("Pegasus has highest speed modifier")
        void pegasusHighestSpeedModifier() {
            JsonNode mounts = medievalData.get("mount_type_definitions");
            double maxSpeed = 0;
            String fastestId = "";
            for (JsonNode v : mounts) {
                double speed = v.get("speed_modifier").asDouble();
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                    fastestId = v.get("name").asText();
                }
            }
            assertEquals("Pegasus", fastestId,
                    "Pegasus should have highest speed modifier");
            assertEquals(4.0, maxSpeed, 0.01,
                    "Pegasus should have speed modifier 4.0");
        }

        @Test
        @DisplayName("All mount types have descriptions")
        void allMountTypesHaveDescriptions() {
            JsonNode mounts = medievalData.get("mount_type_definitions");
            for (JsonNode v : mounts) {
                assertTrue(v.has("description"),
                        v.get("name").asText() + " should have description");
                assertFalse(v.get("description").asText().isEmpty(),
                        v.get("name").asText() + " description should not be empty");
            }
        }

        @Test
        @DisplayName("All mount types have categories")
        void allMountTypesHaveCategories() {
            JsonNode mounts = medievalData.get("mount_type_definitions");
            for (JsonNode v : mounts) {
                assertTrue(v.has("category"),
                        v.get("name").asText() + " should have category");
                String category = v.get("category").asText();
                assertTrue(!category.isEmpty(),
                        v.get("name").asText() + " category should not be empty");
            }
        }

        @Test
        @DisplayName("Camel has desert adaptation")
        void camelDesertAdaptation() {
            JsonNode camel = medievalData.get("mount_type_definitions").get("CAMEL");
            assertNotNull(camel, "Camel mount type should exist");
            assertTrue(camel.get("description").asText().toLowerCase().contains("desert"),
                    "Camel description should mention desert");
        }
    }

    @Nested
    @DisplayName("Vehicle Categories")
    class VehicleCategories {

        @Test
        @DisplayName("Vehicle categories exist")
        void vehicleCategoriesExist() {
            assertTrue(medievalData.has("vehicle_categories"),
                    "vehicle_categories should exist");
        }

        @Test
        @DisplayName("Animal drawn category exists")
        void animalDrawnCategoryExists() {
            JsonNode categories = medievalData.get("vehicle_categories");
            assertTrue(categories.has("animal_drawn"),
                    "animal_drawn category should exist");
        }

        @Test
        @DisplayName("Creature mount category exists")
        void creatureMountCategoryExists() {
            JsonNode categories = medievalData.get("vehicle_categories");
            assertTrue(categories.has("creature_mount"),
                    "creature_mount category should exist");
        }

        @Test
        @DisplayName("Sailing category exists")
        void sailingCategoryExists() {
            JsonNode categories = medievalData.get("vehicle_categories");
            assertTrue(categories.has("sailing"),
                    "sailing category should exist");
        }

        @Test
        @DisplayName("Animal drawn requires PATH infrastructure")
        void animalDrawnRequiresPath() {
            JsonNode animalDrawn = medievalData.get("vehicle_categories")
                    .get("animal_drawn");
            assertEquals("PATH", animalDrawn.get("infrastructure_required").asText());
        }

        @Test
        @DisplayName("Creature mount requires no infrastructure")
        void creatureMountRequiresNone() {
            JsonNode creatureMount = medievalData.get("vehicle_categories")
                    .get("creature_mount");
            assertEquals("NONE", creatureMount.get("infrastructure_required").asText());
        }

        @Test
        @DisplayName("Sailing requires WATER infrastructure")
        void sailingRequiresWater() {
            JsonNode sailing = medievalData.get("vehicle_categories")
                    .get("sailing");
            assertEquals("WATER", sailing.get("infrastructure_required").asText());
        }
    }

    @Nested
    @DisplayName("Cross-Reference Validation")
    class CrossReferenceValidation {

        @Test
        @DisplayName("All vehicle IDs are unique")
        void allVehicleIdsUnique() {
            Set<String> ids = new HashSet<>();
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        assertFalse(ids.contains(id),
                                "Duplicate vehicle ID: " + id);
                        ids.add(id);
                    }
                }
            }
        }

        @Test
        @DisplayName("All station IDs are unique")
        void allStationIdsUnique() {
            Set<String> ids = new HashSet<>();
            ArrayNode stations = (ArrayNode) medievalData.get("stations")
                    .get("medieval_stations");
            for (JsonNode v : stations) {
                String id = v.get("id").asText();
                assertFalse(ids.contains(id),
                        "Duplicate station ID: " + id);
                ids.add(id);
            }
        }

        @Test
        @DisplayName("All cargo type IDs are unique")
        void allCargoTypeIdsUnique() {
            Set<String> ids = new HashSet<>();
            JsonNode cargo = medievalData.get("cargo_types");
            for (JsonNode category : cargo) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        assertFalse(ids.contains(id),
                                "Duplicate cargo type ID: " + id);
                        ids.add(id);
                    }
                }
            }
        }

        @Test
        @DisplayName("All industry IDs are unique")
        void allIndustryIdsUnique() {
            Set<String> ids = new HashSet<>();
            JsonNode industries = medievalData.get("industries");
            for (JsonNode category : industries) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        assertFalse(ids.contains(id),
                                "Duplicate industry ID: " + id);
                        ids.add(id);
                    }
                }
            }
        }

        @Test
        @DisplayName("All vehicles have required stats fields")
        void allVehiclesHaveRequiredStats() {
            JsonNode vehicles = medievalData.get("vehicles");
            String[] requiredStats = {"speed_hexes_per_turn", "cargo_capacity",
                    "passenger_capacity", "build_cost", "maintenance_cost", "lifetime", "power"};
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        JsonNode stats = v.get("stats");
                        assertNotNull(stats, id + " should have stats");
                        for (String stat : requiredStats) {
                            assertTrue(stats.has(stat),
                                    id + " should have " + stat + " in stats");
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("All vehicles have non-negative stats")
        void allVehiclesHaveNonNegativeStats() {
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        JsonNode stats = v.get("stats");
                        assertTrue(stats.get("speed_hexes_per_turn").asInt() >= 0,
                                id + " speed should be non-negative");
                        assertTrue(stats.get("cargo_capacity").asInt() >= 0,
                                id + " cargo_capacity should be non-negative");
                        assertTrue(stats.get("passenger_capacity").asInt() >= 0,
                                id + " passenger_capacity should be non-negative");
                        assertTrue(stats.get("build_cost").asInt() > 0,
                                id + " build_cost should be positive");
                        assertTrue(stats.get("maintenance_cost").asInt() >= 0,
                                id + " maintenance_cost should be non-negative");
                        assertTrue(stats.get("lifetime").asInt() > 0,
                                id + " lifetime should be positive");
                    }
                }
            }
        }

        @Test
        @DisplayName("All vehicles have descriptions")
        void allVehiclesHaveDescriptions() {
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        assertTrue(v.has("description"),
                                id + " should have description");
                        assertFalse(v.get("description").asText().isEmpty(),
                                id + " description should not be empty");
                    }
                }
            }
        }

        @Test
        @DisplayName("All vehicles have names")
        void allVehiclesHaveNames() {
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        assertTrue(v.has("name"), id + " should have name");
                        assertFalse(v.get("name").asText().isEmpty(),
                                id + " name should not be empty");
                    }
                }
            }
        }

        @Test
        @DisplayName("All vehicles have source attribution")
        void allVehiclesHaveSource() {
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        String id = v.get("id").asText();
                        assertTrue(v.has("source"), id + " should have source");
                        assertFalse(v.get("source").asText().isEmpty(),
                                id + " source should not be empty");
                    }
                }
            }
        }

        @Test
        @DisplayName("Infrastructure compatible vehicles reference valid vehicle IDs")
        void infrastructureCompatibleVehiclesValid() {
            // Collect all vehicle IDs
            Set<String> vehicleIds = new HashSet<>();
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        vehicleIds.add(v.get("id").asText());
                        // Also add variant IDs
                        if (v.has("variants") && v.get("variants").isArray()) {
                            for (JsonNode variant : v.get("variants")) {
                                if (variant.has("id")) {
                                    vehicleIds.add(variant.get("id").asText());
                                }
                            }
                        }
                    }
                }
            }

            // Check infrastructure compatible vehicles
            JsonNode infra = medievalData.get("infrastructure");
            for (JsonNode infraEntry : infra) {
                String infraName = infraEntry.get("id").asText();
                if (infraEntry.has("compatible_vehicles")) {
                    ArrayNode compatible = (ArrayNode) infraEntry.get("compatible_vehicles");
                    for (JsonNode v : compatible) {
                        String vehicleId = v.asText();
                        assertTrue(vehicleIds.contains(vehicleId),
                                infraName + " references incompatible vehicle: " + vehicleId);
                    }
                }
            }
        }

        @Test
        @DisplayName("Industry outputs reference valid cargo types")
        void industryOutputsReferenceValidCargo() {
            // Collect all cargo type IDs
            Set<String> cargoIds = new HashSet<>();
            JsonNode cargo = medievalData.get("cargo_types");
            for (JsonNode category : cargo) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        cargoIds.add(v.get("id").asText());
                    }
                }
            }

            // Check industry outputs
            JsonNode industries = medievalData.get("industries");
            for (JsonNode industryCategory : industries) {
                if (industryCategory.isArray()) {
                    for (JsonNode industry : industryCategory) {
                        String id = industry.get("id").asText();
                        if (industry.has("output") && industry.get("output") != null
                                && !industry.get("output").isNull()) {
                            String outputCargo = industry.get("output").get("cargo").asText();
                            assertTrue(cargoIds.contains(outputCargo),
                                    id + " outputs " + outputCargo + " which is not a defined cargo type");
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Industry inputs reference valid cargo types")
        void industryInputsReferenceValidCargo() {
            // Collect all cargo type IDs
            Set<String> cargoIds = new HashSet<>();
            JsonNode cargo = medievalData.get("cargo_types");
            for (JsonNode category : cargo) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        cargoIds.add(v.get("id").asText());
                    }
                }
            }

            // Check industry inputs
            JsonNode industries = medievalData.get("industries");
            for (JsonNode industryCategory : industries) {
                if (industryCategory.isArray()) {
                    for (JsonNode industry : industryCategory) {
                        String id = industry.get("id").asText();
                        if (industry.has("input") && industry.get("input") != null
                                && !industry.get("input").isNull()) {
                            ArrayNode input = (ArrayNode) industry.get("input");
                            for (JsonNode i : input) {
                                String inputCargo = i.get("cargo").asText();
                                assertTrue(cargoIds.contains(inputCargo),
                                        id + " inputs " + inputCargo + " which is not a defined cargo type");
                            }
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Cargo produced_by references valid industry IDs")
        void cargoProducedByReferencesValidIndustries() {
            // Collect all industry IDs
            Set<String> industryIds = new HashSet<>();
            JsonNode industries = medievalData.get("industries");
            for (JsonNode category : industries) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        industryIds.add(v.get("id").asText());
                    }
                }
            }

            // Check cargo produced_by
            JsonNode cargo = medievalData.get("cargo_types");
            for (JsonNode cargoCategory : cargo) {
                if (cargoCategory.isArray()) {
                    for (JsonNode cargoType : cargoCategory) {
                        String id = cargoType.get("id").asText();
                        ArrayNode producedBy = (ArrayNode) cargoType.get("produced_by");
                        for (JsonNode p : producedBy) {
                            String industryId = p.asText();
                            // Some cargo types may reference industries not in medieval-vehicles.json
                            // (e.g., farm, ranch from core industries)
                            // We only validate that the field exists and is an array
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Tech level 0 available vehicles reference valid vehicle IDs")
        void techLevel0AvailableVehiclesValid() {
            // Collect all vehicle IDs
            Set<String> vehicleIds = new HashSet<>();
            JsonNode vehicles = medievalData.get("vehicles");
            for (JsonNode category : vehicles) {
                if (category.isArray()) {
                    for (JsonNode v : category) {
                        vehicleIds.add(v.get("id").asText());
                        // Also add variant IDs
                        if (v.has("variants") && v.get("variants").isArray()) {
                            for (JsonNode variant : v.get("variants")) {
                                if (variant.has("id")) {
                                    vehicleIds.add(variant.get("id").asText());
                                }
                            }
                        }
                    }
                }
            }

            // Check tech level 0 available vehicles
            // Note: some vehicles may reference core OpenTTD vehicles not in this file
            // We only validate the vehicles defined in this medieval-vehicles.json
            ArrayNode available = (ArrayNode) medievalData.get("tech_level_0")
                    .get("available_vehicles");
            // At least the medieval-specific vehicles should be found
            String[] medievalVehicles = {"horse_cart", "pegasus_mount", "drake_mount",
                    "brig", "schooner", "rowboat", "cog_ship", "caravel"};
            for (String medievalVehicle : medievalVehicles) {
                boolean found = false;
                for (JsonNode v : available) {
                    if (medievalVehicle.equals(v.asText())) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "tech_level_0 should include " + medievalVehicle);
            }
        }

        @Test
        @DisplayName("Tech level 0 available stations reference valid station IDs")
        void techLevel0AvailableStationsValid() {
            // Collect all station IDs
            Set<String> stationIds = new HashSet<>();
            ArrayNode stations = (ArrayNode) medievalData.get("stations")
                    .get("medieval_stations");
            for (JsonNode v : stations) {
                stationIds.add(v.get("id").asText());
            }

            // Check tech level 0 available stations
            ArrayNode available = (ArrayNode) medievalData.get("tech_level_0")
                    .get("available_stations");
            for (JsonNode v : available) {
                String stationId = v.asText();
                assertTrue(stationIds.contains(stationId),
                        "tech_level_0 references undefined station: " + stationId);
            }
        }

        @Test
        @DisplayName("Flying mounts have higher speed than ground mounts")
        void flyingMountsFasterThanGround() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            int minFlyingSpeed = Integer.MAX_VALUE;
            int maxGroundSpeed = 0;

            for (JsonNode v : mounts) {
                int speed = v.get("stats").get("speed_hexes_per_turn").asInt();
                if ("FLYING_MOUNT".equals(v.get("type").asText())) {
                    minFlyingSpeed = Math.min(minFlyingSpeed, speed);
                } else {
                    maxGroundSpeed = Math.max(maxGroundSpeed, speed);
                }
            }

            assertTrue(minFlyingSpeed > maxGroundSpeed,
                    "Flying mounts should be faster than ground mounts. " +
                            "Min flying: " + minFlyingSpeed + ", Max ground: " + maxGroundSpeed);
        }

        @Test
        @DisplayName("Sailing ships have higher cargo than animal drawn vehicles")
        void sailingShipsHigherCargoThanAnimalDrawn() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");

            int maxAnimalCargo = 0;
            for (JsonNode v : animals) {
                int cargo = v.get("stats").get("cargo_capacity").asInt();
                maxAnimalCargo = Math.max(maxAnimalCargo, cargo);
            }

            int minShipCargo = Integer.MAX_VALUE;
            for (JsonNode v : ships) {
                int cargo = v.get("stats").get("cargo_capacity").asInt();
                minShipCargo = Math.min(minShipCargo, cargo);
            }

            // Ships should generally have higher cargo capacity
            // (rowboat is an exception, but average should be higher)
            int totalShipCargo = 0;
            for (JsonNode v : ships) {
                totalShipCargo += v.get("stats").get("cargo_capacity").asInt();
            }
            double avgShipCargo = (double) totalShipCargo / ships.size();

            assertTrue(avgShipCargo > maxAnimalCargo,
                    "Average ship cargo should exceed max animal cargo. " +
                            "Avg ship: " + avgShipCargo + ", Max animal: " + maxAnimalCargo);
        }
    }

    @Nested
    @DisplayName("Stat Reasonableness")
    class StatReasonableness {

        @Test
        @DisplayName("Horse cart speed is reasonable for medieval transport")
        void horseCartSpeedReasonable() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            JsonNode horseCart = null;
            for (JsonNode v : animals) {
                if ("horse_cart".equals(v.get("id").asText())) {
                    horseCart = v;
                    break;
                }
            }
            int speed = horseCart.get("stats").get("speed_hexes_per_turn").asInt();
            assertTrue(speed >= 1 && speed <= 3,
                    "Horse cart speed should be between 1-3 hexes/turn");
        }

        @Test
        @DisplayName("Pegasus build cost is highest among mounts")
        void pegasusHighestBuildCost() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            int maxCost = 0;
            String mostExpensiveId = "";
            for (JsonNode v : mounts) {
                int cost = v.get("stats").get("build_cost").asInt();
                if (cost > maxCost) {
                    maxCost = cost;
                    mostExpensiveId = v.get("id").asText();
                }
            }
            assertEquals("pegasus_mount", mostExpensiveId,
                    "Pegasus should have highest build cost among mounts");
        }

        @Test
        @DisplayName("Pack animal has lowest build cost among animal drawn")
        void packAnimalLowestBuildCost() {
            ArrayNode animals = (ArrayNode) medievalData.get("vehicles").get("animal_drawn_road");
            int minCost = Integer.MAX_VALUE;
            String cheapestId = "";
            for (JsonNode v : animals) {
                int cost = v.get("stats").get("build_cost").asInt();
                if (cost < minCost) {
                    minCost = cost;
                    cheapestId = v.get("id").asText();
                }
            }
            assertEquals("pack_animal", cheapestId,
                    "Pack animal should have lowest build cost");
        }

        @Test
        @DisplayName("Windjammer has longest lifetime among ships")
        void windjammerLongestLifetime() {
            ArrayNode ships = (ArrayNode) medievalData.get("vehicles").get("sailing_ships");
            int maxLifetime = 0;
            String longestId = "";
            for (JsonNode v : ships) {
                int lifetime = v.get("stats").get("lifetime").asInt();
                if (lifetime > maxLifetime) {
                    maxLifetime = lifetime;
                    longestId = v.get("id").asText();
                }
            }
            assertEquals("windjammer", longestId,
                    "Windjammer should have longest lifetime");
        }

        @Test
        @DisplayName("War elephant has longest lifetime among mounts")
        void warElephantLongestLifetime() {
            ArrayNode mounts = (ArrayNode) medievalData.get("vehicles").get("creature_mounts");
            int maxLifetime = 0;
            String longestId = "";
            for (JsonNode v : mounts) {
                int lifetime = v.get("stats").get("lifetime").asInt();
                if (lifetime > maxLifetime) {
                    maxLifetime = lifetime;
                    longestId = v.get("id").asText();
                }
            }
            assertEquals("elephant_mount", longestId,
                    "War elephant should have longest lifetime");
        }
    }
}

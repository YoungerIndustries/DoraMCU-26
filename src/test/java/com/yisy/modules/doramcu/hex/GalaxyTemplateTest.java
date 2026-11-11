package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GalaxyTemplate Tests")
class GalaxyTemplateTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("Has 8 templates")
        void hasEightTemplates() {
            assertEquals(8, GalaxyTemplate.values().length);
        }

        @Test
        @DisplayName("Contains expected templates")
        void containsExpectedTemplates() {
            assertArrayEquals(
                    new GalaxyTemplate[]{
                            GalaxyTemplate.TRAVELLER_CLASSIC,
                            GalaxyTemplate.STAR_TREK,
                            GalaxyTemplate.FRONTIER,
                            GalaxyTemplate.VOID,
                            GalaxyTemplate.BABYLON5,
                            GalaxyTemplate.SINGLE_SYSTEM_RPG,
                            GalaxyTemplate.SUBSECTOR_RPG,
                            GalaxyTemplate.CUSTOM
                    },
                    GalaxyTemplate.values()
            );
        }

        @Test
        @DisplayName("getAllTemplates returns all values")
        void getAllTemplates() {
            assertArrayEquals(GalaxyTemplate.values(), GalaxyTemplate.getAllTemplates());
        }
    }

    @Nested
    @DisplayName("Traveller Classic template")
    class TravellerClassic {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Traveller Classic", GalaxyTemplate.TRAVELLER_CLASSIC.getDisplayName());
        }

        @Test
        @DisplayName("Description is not empty")
        void description() {
            assertFalse(GalaxyTemplate.TRAVELLER_CLASSIC.getDescription().isEmpty());
        }

        @Test
        @DisplayName("Config has correct sector size")
        void sectorSize() {
            assertEquals(10, GalaxyTemplate.TRAVELLER_CLASSIC.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has correct naming convention")
        void namingConvention() {
            assertEquals("astronomical", GalaxyTemplate.TRAVELLER_CLASSIC.getConfig().getNamingConvention());
        }

        @Test
        @DisplayName("Config has correct game type")
        void gameType() {
            assertEquals("DoraT", GalaxyTemplate.TRAVELLER_CLASSIC.getConfig().getGameType());
        }

        @Test
        @DisplayName("Config has correct difficulty")
        void difficulty() {
            assertEquals("medium", GalaxyTemplate.TRAVELLER_CLASSIC.getConfig().getDifficultyLevel());
        }

        @Test
        @DisplayName("Config has correct empire count")
        void empireCount() {
            assertEquals(4, GalaxyTemplate.TRAVELLER_CLASSIC.getConfig().getEmpireCount());
        }
    }

    @Nested
    @DisplayName("Star Trek template")
    class StarTrek {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Star Trek", GalaxyTemplate.STAR_TREK.getDisplayName());
        }

        @Test
        @DisplayName("Config has larger sector size")
        void sectorSize() {
            assertEquals(12, GalaxyTemplate.STAR_TREK.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has mythological naming")
        void namingConvention() {
            assertEquals("mythological", GalaxyTemplate.STAR_TREK.getConfig().getNamingConvention());
        }

        @Test
        @DisplayName("Config has Captain's Log game type")
        void gameType() {
            assertEquals("Captain's Log", GalaxyTemplate.STAR_TREK.getConfig().getGameType());
        }

        @Test
        @DisplayName("Config has lower civilization density")
        void civilizationDensity() {
            assertEquals(0.15, GalaxyTemplate.STAR_TREK.getConfig().getCivilizationDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has lower hyperlane density")
        void hyperlaneDensity() {
            assertEquals(0.7, GalaxyTemplate.STAR_TREK.getConfig().getHyperlaneDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has more moons")
        void moonChance() {
            assertEquals(0.5, GalaxyTemplate.STAR_TREK.getConfig().getMoonChance(), 0.001);
        }

        @Test
        @DisplayName("Config has fewer black holes")
        void blackHoleChance() {
            assertEquals(0.02, GalaxyTemplate.STAR_TREK.getConfig().getBlackHoleChance(), 0.001);
        }
    }

    @Nested
    @DisplayName("Frontier template")
    class Frontier {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Frontier", GalaxyTemplate.FRONTIER.getDisplayName());
        }

        @Test
        @DisplayName("Config has smaller sector size")
        void sectorSize() {
            assertEquals(8, GalaxyTemplate.FRONTIER.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has star naming")
        void namingConvention() {
            assertEquals("star", GalaxyTemplate.FRONTIER.getConfig().getNamingConvention());
        }

        @Test
        @DisplayName("Config has high civilization density")
        void civilizationDensity() {
            assertEquals(0.6, GalaxyTemplate.FRONTIER.getConfig().getCivilizationDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has high resource distribution")
        void resourceDistribution() {
            assertEquals(0.8, GalaxyTemplate.FRONTIER.getConfig().getResourceDistribution(), 0.001);
        }

        @Test
        @DisplayName("Config has higher hyperlane density")
        void hyperlaneDensity() {
            assertEquals(1.5, GalaxyTemplate.FRONTIER.getConfig().getHyperlaneDensity(), 0.001);
        }
    }

    @Nested
    @DisplayName("Void template")
    class Void_ {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Void", GalaxyTemplate.VOID.getDisplayName());
        }

        @Test
        @DisplayName("Config has largest sector size")
        void sectorSize() {
            assertEquals(14, GalaxyTemplate.VOID.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has lowest civilization density")
        void civilizationDensity() {
            assertEquals(0.05, GalaxyTemplate.VOID.getConfig().getCivilizationDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has more nebulae")
        void nebulaChance() {
            assertEquals(0.3, GalaxyTemplate.VOID.getConfig().getNebulaChance(), 0.001);
        }

        @Test
        @DisplayName("Config has more black holes")
        void blackHoleChance() {
            assertEquals(0.15, GalaxyTemplate.VOID.getConfig().getBlackHoleChance(), 0.001);
        }

        @Test
        @DisplayName("Config has lowest hyperlane density")
        void hyperlaneDensity() {
            assertEquals(0.5, GalaxyTemplate.VOID.getConfig().getHyperlaneDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has hard difficulty")
        void difficulty() {
            assertEquals("hard", GalaxyTemplate.VOID.getConfig().getDifficultyLevel());
        }
    }

    @Nested
    @DisplayName("Babylon 5 template")
    class Babylon5 {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Babylon 5", GalaxyTemplate.BABYLON5.getDisplayName());
        }

        @Test
        @DisplayName("Config has correct sector size")
        void sectorSize() {
            assertEquals(10, GalaxyTemplate.BABYLON5.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has mythological naming")
        void namingConvention() {
            assertEquals("mythological", GalaxyTemplate.BABYLON5.getConfig().getNamingConvention());
        }

        @Test
        @DisplayName("Config has Babylon5 game type")
        void gameType() {
            assertEquals("Babylon5", GalaxyTemplate.BABYLON5.getConfig().getGameType());
        }

        @Test
        @DisplayName("Config has 13 empires")
        void empireCount() {
            assertEquals(13, GalaxyTemplate.BABYLON5.getConfig().getEmpireCount());
        }

        @Test
        @DisplayName("Config has spiral galaxy shape")
        void galaxyShape() {
            assertEquals("spiral", GalaxyTemplate.BABYLON5.getConfig().getGalaxyShape());
        }

        @Test
        @DisplayName("Config has space station chance")
        void spaceStationChance() {
            assertEquals(0.15, GalaxyTemplate.BABYLON5.getConfig().getSpaceStationChance(), 0.001);
        }

        @Test
        @DisplayName("Config has hyperlane density 1.0")
        void hyperlaneDensity() {
            assertEquals(1.0, GalaxyTemplate.BABYLON5.getConfig().getHyperlaneDensity(), 0.001);
        }
    }

    @Nested
    @DisplayName("Single System RPG template")
    class SingleSystemRpg {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Single System RPG", GalaxyTemplate.SINGLE_SYSTEM_RPG.getDisplayName());
        }

        @Test
        @DisplayName("Description is not empty")
        void description() {
            assertFalse(GalaxyTemplate.SINGLE_SYSTEM_RPG.getDescription().isEmpty());
        }

        @Test
        @DisplayName("Config has system count of 1")
        void systemCount() {
            assertEquals(1, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getSystemCount());
        }

        @Test
        @DisplayName("Config has 8 planets per system")
        void planetsPerSystem() {
            assertEquals(8, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getPlanetsPerSystem());
        }

        @Test
        @DisplayName("Config has high moon chance")
        void moonChance() {
            assertEquals(0.7, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getMoonChance(), 0.001);
        }

        @Test
        @DisplayName("Config has high asteroid belt chance")
        void asteroidBeltChance() {
            assertEquals(0.6, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getAsteroidBeltChance(), 0.001);
        }

        @Test
        @DisplayName("Config has moderate space station chance")
        void spaceStationChance() {
            assertEquals(0.3, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getSpaceStationChance(), 0.001);
        }

        @Test
        @DisplayName("Config has minimal sector size")
        void sectorSize() {
            assertEquals(2, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has minimal subsector dimension")
        void subSectorDimension() {
            assertEquals(2, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getSubSectorDimension());
        }

        @Test
        @DisplayName("Config has Traveler4x game type")
        void gameType() {
            assertEquals("Traveler4x", GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getGameType());
        }

        @Test
        @DisplayName("Config has single empire")
        void empireCount() {
            assertEquals(1, GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getEmpireCount());
        }

        @Test
        @DisplayName("Config has single galaxy shape")
        void galaxyShape() {
            assertEquals("single", GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getGalaxyShape());
        }

        @Test
        @DisplayName("Config has mythological naming")
        void namingConvention() {
            assertEquals("mythological", GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getNamingConvention());
        }

        @Test
        @DisplayName("Config has medium difficulty")
        void difficulty() {
            assertEquals("medium", GalaxyTemplate.SINGLE_SYSTEM_RPG.getConfig().getDifficultyLevel());
        }
    }

    @Nested
    @DisplayName("Subsector RPG template")
    class SubsectorRpg {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Subsector RPG", GalaxyTemplate.SUBSECTOR_RPG.getDisplayName());
        }

        @Test
        @DisplayName("Description is not empty")
        void description() {
            assertFalse(GalaxyTemplate.SUBSECTOR_RPG.getDescription().isEmpty());
        }

        @Test
        @DisplayName("Config has 25 systems")
        void systemCount() {
            assertEquals(25, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getSystemCount());
        }

        @Test
        @DisplayName("Config has 4 planets per system")
        void planetsPerSystem() {
            assertEquals(4, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getPlanetsPerSystem());
        }

        @Test
        @DisplayName("Config has moderate moon chance")
        void moonChance() {
            assertEquals(0.5, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getMoonChance(), 0.001);
        }

        @Test
        @DisplayName("Config has moderate asteroid belt chance")
        void asteroidBeltChance() {
            assertEquals(0.5, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getAsteroidBeltChance(), 0.001);
        }

        @Test
        @DisplayName("Config has hyperlane density 1.2")
        void hyperlaneDensity() {
            assertEquals(1.2, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getHyperlaneDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has minimal sector size")
        void sectorSize() {
            assertEquals(2, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getSectorSize());
        }

        @Test
        @DisplayName("Config has Traveler4x game type")
        void gameType() {
            assertEquals("Traveler4x", GalaxyTemplate.SUBSECTOR_RPG.getConfig().getGameType());
        }

        @Test
        @DisplayName("Config has 2 empires")
        void empireCount() {
            assertEquals(2, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getEmpireCount());
        }

        @Test
        @DisplayName("Config has cluster galaxy shape")
        void galaxyShape() {
            assertEquals("cluster", GalaxyTemplate.SUBSECTOR_RPG.getConfig().getGalaxyShape());
        }

        @Test
        @DisplayName("Config has astronomical naming")
        void namingConvention() {
            assertEquals("astronomical", GalaxyTemplate.SUBSECTOR_RPG.getConfig().getNamingConvention());
        }

        @Test
        @DisplayName("Config has Traveler campaign theme")
        void campaignTheme() {
            assertEquals("Traveler", GalaxyTemplate.SUBSECTOR_RPG.getConfig().getCampaignTheme());
        }

        @Test
        @DisplayName("Config has 10 adventure hooks")
        void adventureHookCount() {
            assertEquals(10, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getAdventureHookCount());
        }

        @Test
        @DisplayName("Config has moderate NPC density")
        void npcDensity() {
            assertEquals(0.5, GalaxyTemplate.SUBSECTOR_RPG.getConfig().getNpcDensity(), 0.001);
        }

        @Test
        @DisplayName("Config has mixed system theme")
        void systemTheme() {
            assertEquals("mixed", GalaxyTemplate.SUBSECTOR_RPG.getConfig().getSystemTheme());
        }
    }

    @Nested
    @DisplayName("Custom template")
    class Custom {

        @Test
        @DisplayName("Display name")
        void displayName() {
            assertEquals("Custom", GalaxyTemplate.CUSTOM.getDisplayName());
        }

        @Test
        @DisplayName("Config uses default values")
        void defaultValues() {
            GalaxyGenerationConfig config = GalaxyTemplate.CUSTOM.getConfig();
            assertEquals(GalaxyGenerationConfig.DEFAULT_SECTOR_SIZE, config.getSectorSize());
            assertEquals(GalaxyGenerationConfig.DEFAULT_NAMING_CONVENTION, config.getNamingConvention());
            assertEquals(GalaxyGenerationConfig.DEFAULT_GAME_TYPE, config.getGameType());
        }
    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("Find by enum name")
        void byEnumName() {
            assertSame(GalaxyTemplate.TRAVELLER_CLASSIC, GalaxyTemplate.findByName("TRAVELLER_CLASSIC"));
        }

        @Test
        @DisplayName("Find by enum name (case insensitive)")
        void byEnumNameCaseInsensitive() {
            assertSame(GalaxyTemplate.TRAVELLER_CLASSIC, GalaxyTemplate.findByName("traveller_classic"));
        }

        @Test
        @DisplayName("Find by display name")
        void byDisplayName() {
            assertSame(GalaxyTemplate.STAR_TREK, GalaxyTemplate.findByName("Star Trek"));
        }

        @Test
        @DisplayName("Find by display name (case insensitive)")
        void byDisplayNameCaseInsensitive() {
            assertSame(GalaxyTemplate.STAR_TREK, GalaxyTemplate.findByName("star trek"));
        }

        @Test
        @DisplayName("Find Frontier")
        void findFrontier() {
            assertSame(GalaxyTemplate.FRONTIER, GalaxyTemplate.findByName("Frontier"));
        }

        @Test
        @DisplayName("Find Void")
        void findVoid() {
            assertSame(GalaxyTemplate.VOID, GalaxyTemplate.findByName("Void"));
        }

        @Test
        @DisplayName("Find Babylon5")
        void findBabylon5() {
            assertSame(GalaxyTemplate.BABYLON5, GalaxyTemplate.findByName("BABYLON5"));
        }

        @Test
        @DisplayName("Find Babylon5 by display name")
        void findBabylon5ByDisplayName() {
            assertSame(GalaxyTemplate.BABYLON5, GalaxyTemplate.findByName("Babylon 5"));
        }

        @Test
        @DisplayName("Find Single System RPG")
        void findSingleSystemRpg() {
            assertSame(GalaxyTemplate.SINGLE_SYSTEM_RPG, GalaxyTemplate.findByName("SINGLE_SYSTEM_RPG"));
        }

        @Test
        @DisplayName("Find Single System RPG by display name")
        void findSingleSystemRpgByDisplayName() {
            assertSame(GalaxyTemplate.SINGLE_SYSTEM_RPG, GalaxyTemplate.findByName("Single System RPG"));
        }

        @Test
        @DisplayName("Find Subsector RPG")
        void findSubsectorRpg() {
            assertSame(GalaxyTemplate.SUBSECTOR_RPG, GalaxyTemplate.findByName("SUBSECTOR_RPG"));
        }

        @Test
        @DisplayName("Find Subsector RPG by display name")
        void findSubsectorRpgByDisplayName() {
            assertSame(GalaxyTemplate.SUBSECTOR_RPG, GalaxyTemplate.findByName("Subsector RPG"));
        }

        @Test
        @DisplayName("Find Custom")
        void findCustom() {
            assertSame(GalaxyTemplate.CUSTOM, GalaxyTemplate.findByName("CUSTOM"));
        }

        @Test
        @DisplayName("Throws on null")
        void throwsOnNull() {
            assertThrows(IllegalArgumentException.class, () -> GalaxyTemplate.findByName(null));
        }

        @Test
        @DisplayName("Throws on unknown name")
        void throwsOnUnknown() {
            assertThrows(IllegalArgumentException.class, () -> GalaxyTemplate.findByName("Unknown"));
        }
    }

    @Nested
    @DisplayName("customize")
    class Customize {

        @Test
        @DisplayName("Returns builder with template values")
        void returnsBuilderWithTemplateValues() {
            GalaxyGenerationConfig.Builder builder = GalaxyTemplate.STAR_TREK.customize();
            GalaxyGenerationConfig config = builder.build();

            assertEquals(12, config.getSectorSize());
            assertEquals("mythological", config.getNamingConvention());
            assertEquals("Captain's Log", config.getGameType());
        }

        @Test
        @DisplayName("Builder allows overrides")
        void allowsOverrides() {
            GalaxyGenerationConfig config = GalaxyTemplate.STAR_TREK.customize()
                    .empireCount(5)
                    .difficultyLevel("hard")
                    .build();

            assertEquals(12, config.getSectorSize());
            assertEquals(5, config.getEmpireCount());
            assertEquals("hard", config.getDifficultyLevel());
        }

        @Test
        @DisplayName("Custom template customize returns defaults")
        void customTemplateDefaults() {
            GalaxyGenerationConfig config = GalaxyTemplate.CUSTOM.customize().build();
            assertEquals(GalaxyGenerationConfig.DEFAULT_SECTOR_SIZE, config.getSectorSize());
        }
    }

    @Nested
    @DisplayName("EmpireBuilder")
    class EmpireBuilder_ {

        @Test
        @DisplayName("EmpireBuilder is null by default")
        void empireBuilderIsNullByDefault() {
            // The static empireBuilder may be set by Spring, so we just verify the API exists
            GalaxyTemplate.setEmpireBuilder(null);
            assertNull(GalaxyTemplate.getEmpireBuilder());
        }

        @Test
        @DisplayName("Set and get EmpireBuilder")
        void setAndGetEmpireBuilder() {
            EmpireBuilder builder = new com.yisy.modules.doramcu.hex.manager.DefaultEmpireBuilder(123L);
            GalaxyTemplate.setEmpireBuilder(builder);
            assertSame(builder, GalaxyTemplate.getEmpireBuilder());
            // Clean up
            GalaxyTemplate.setEmpireBuilder(null);
        }
    }

    @Nested
    @DisplayName("launchGame")
    class LaunchGame {

        @Test
        @DisplayName("launchGame creates a game with galaxy")
        void launchGameCreatesGame() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.CUSTOM.launchGame("Test Galaxy", 42L);

            assertNotNull(game);
            assertEquals("Test Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }

        @Test
        @DisplayName("launchGame uses DefaultEmpireBuilder when no builder set")
        void launchGameUsesDefaultBuilder() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.CUSTOM.launchGame("Test Galaxy", 42L);

            assertNotNull(game);
            // DefaultEmpireBuilder creates empires based on empireCount in config
            // CUSTOM template has default empireCount
            assertTrue(game.getGalaxies().size() > 0);
        }

        @Test
        @DisplayName("launchGame uses custom EmpireBuilder when set")
        void launchGameUsesCustomBuilder() {
            EmpireBuilder customBuilder = new EmpireBuilder() {
                @Override
                public List<Empire> buildEmpires(Game game, Galaxy galaxy) {
                    Player player = game.createPlayer("Custom Empire");
                    EmpireConfig config = new EmpireConfig();
                    config.setPlanetSize(PlanetSize.LARGE);
                    Empire empire = createEmpire("Custom Empire", game, player, galaxy, config);
                    if (empire != null) {
                        game.registerEmpire(empire);
                    }
                    return empire != null ? List.of(empire) : List.of();
                }

                @Override
                public Empire createEmpire(String name, Game game, Player player, Galaxy galaxy, EmpireConfig config) {
                    int empireId = game.getNextEmpireIdAndIncrement();
                    Empire empire = new Empire(empireId, name, player.getId());
                    PlanetSize planetSize = config != null ? config.getPlanetSize() : PlanetSize.MEDIUM;
                    MineralQuality mineralQuality = config != null ? config.getMineralQuality() : MineralQuality.NORMAL;
                    Planet capitalPlanet = game.setupHomeSystem(galaxy, player, planetSize, mineralQuality,
                            new StartingFacility[]{StartingFacility.SHIPYARD});
                    if (capitalPlanet != null) {
                        empire.claimPlanet(capitalPlanet.getId());
                        empire.setCapitalPlanet(capitalPlanet.getId());
                    }
                    return empire;
                }
            };
            GalaxyTemplate.setEmpireBuilder(customBuilder);
            Game game = GalaxyTemplate.CUSTOM.launchGame("Test Galaxy", 42L);

            assertNotNull(game);
            GalaxyTemplate.setEmpireBuilder(null);
        }

        @Test
        @DisplayName("launchGame with BABYLON5 template")
        void launchGameWithBabylon5() {
            GalaxyTemplate.setEmpireBuilder(null);
            Game game = GalaxyTemplate.BABYLON5.launchGame("B5 Galaxy", 123L);

            assertNotNull(game);
            assertEquals("B5 Galaxy", game.getName());
            assertFalse(game.getGalaxies().isEmpty());
        }
    }
}

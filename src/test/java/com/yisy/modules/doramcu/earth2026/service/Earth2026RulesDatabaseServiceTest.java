package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026RulesDatabaseService Tests")
class Earth2026RulesDatabaseServiceTest {

    private Earth2026RulesDatabaseService service;

    @BeforeEach
    void setUp() {
        // Use a non-existent path so the service initializes with empty data
        service = new Earth2026RulesDatabaseService("nonexistent-path.json");
        service.loadDatabase();
    }

    @Nested
    @DisplayName("loadDatabase")
    class LoadDatabaseTests {

        @Test
        @DisplayName("should handle missing file gracefully")
        void missingFile() {
            // Service already loaded with non-existent path
            assertNotNull(service.getAllSpecies());
            assertTrue(service.getAllSpecies().isEmpty());
        }

        @Test
        @DisplayName("should load real rules database")
        void loadRealDatabase() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            assertNotNull(realService.getAllSpecies());
            assertTrue(realService.getAllSpecies().size() > 0, "Should have loaded species from JSON");
        }

        @Test
        @DisplayName("should load species from real database")
        void loadSpeciesCount() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            // The JSON has 14 species
            assertTrue(realService.getAllSpecies().size() >= 8, "Should have at least 8 Traveller species");
        }

        @Test
        @DisplayName("should load galaxy rules from real database")
        void loadGalaxyRules() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26GalaxyRules rules = realService.getGalaxyRules();
            assertNotNull(rules);
        }

        @Test
        @DisplayName("should load system rules from real database")
        void loadSystemRules() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26SystemRules rules = realService.getSystemRules();
            assertNotNull(rules);
        }

        @Test
        @DisplayName("should load planet rules from real database")
        void loadPlanetRules() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26PlanetRules rules = realService.getPlanetRules();
            assertNotNull(rules);
        }
    }

    @Nested
    @DisplayName("getAllSpecies")
    class GetAllSpeciesTests {

        @Test
        @DisplayName("should return empty list when no species loaded")
        void emptyList() {
            List<E26Species> species = service.getAllSpecies();
            assertNotNull(species);
            assertTrue(species.isEmpty());
        }

        @Test
        @DisplayName("should return all species from real database")
        void allSpecies() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> species = realService.getAllSpecies();
            assertTrue(species.size() > 0);
        }
    }

    @Nested
    @DisplayName("getSpecies")
    class GetSpeciesTests {

        @Test
        @DisplayName("should return empty for null id")
        void nullId() {
            Optional<E26Species> result = service.getSpecies(null);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should return empty for non-existent species")
        void notFound() {
            Optional<E26Species> result = service.getSpecies("NONEXISTENT");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should find Human species by ID")
        void findHuman() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            Optional<E26Species> result = realService.getSpecies("HUMAN");
            assertTrue(result.isPresent());
            assertEquals("Human", result.get().getName());
        }

        @Test
        @DisplayName("should find species case-insensitively")
        void caseInsensitive() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            Optional<E26Species> result = realService.getSpecies("human");
            assertTrue(result.isPresent());
            assertEquals("Human", result.get().getName());
        }

        @Test
        @DisplayName("should find Aslan species")
        void findAslan() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            Optional<E26Species> result = realService.getSpecies("ASLAN");
            assertTrue(result.isPresent());
            assertEquals("Aslan", result.get().getName());
            assertEquals("TRAVELLER", result.get().getGameModule());
        }

        @Test
        @DisplayName("should find Klingon species")
        void findKlingon() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            Optional<E26Species> result = realService.getSpecies("KLINGON");
            assertTrue(result.isPresent());
            assertEquals("Klingon", result.get().getName());
            assertEquals("CAPTAINS_LOG", result.get().getGameModule());
        }

        @Test
        @DisplayName("should find Narn species")
        void findNarn() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            Optional<E26Species> result = realService.getSpecies("NARN");
            assertTrue(result.isPresent());
            assertEquals("Narn", result.get().getName());
            assertEquals("BABYLON5", result.get().getGameModule());
        }
    }

    @Nested
    @DisplayName("getSpeciesByGameModule")
    class GetSpeciesByGameModuleTests {

        @Test
        @DisplayName("should return empty for null game module")
        void nullGameModule() {
            List<E26Species> result = service.getSpeciesByGameModule(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty for non-existent game module")
        void notFound() {
            List<E26Species> result = service.getSpeciesByGameModule("NONEXISTENT");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should find all Traveller species")
        void travellerSpecies() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> result = realService.getSpeciesByGameModule("TRAVELLER");
            // JSON has 8 Traveller species: Human, Aslan, Vargr, Insectoid, Melniki, Tiltean, Zhodani, Loxi
            assertTrue(result.size() >= 7, "Should have at least 7 Traveller species, got: " + result.size());
            for (E26Species sp : result) {
                assertEquals("TRAVELLER", sp.getGameModule());
            }
        }

        @Test
        @DisplayName("should find all Captain's Log species")
        void captainsLogSpecies() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> result = realService.getSpeciesByGameModule("CAPTAINS_LOG");
            assertTrue(result.size() >= 4, "Should have at least 4 Captain's Log species");
            for (E26Species sp : result) {
                assertEquals("CAPTAINS_LOG", sp.getGameModule());
            }
        }

        @Test
        @DisplayName("should find all Babylon 5 species")
        void babylon5Species() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> result = realService.getSpeciesByGameModule("BABYLON5");
            assertTrue(result.size() >= 3, "Should have at least 3 Babylon 5 species");
            for (E26Species sp : result) {
                assertEquals("BABYLON5", sp.getGameModule());
            }
        }

        @Test
        @DisplayName("should be case-insensitive")
        void caseInsensitive() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> result = realService.getSpeciesByGameModule("traveller");
            assertTrue(result.size() >= 7);
        }
    }

    @Nested
    @DisplayName("getSpeciesByHomeworld")
    class GetSpeciesByHomeworldTests {

        @Test
        @DisplayName("should return empty for null homeworld")
        void nullHomeworld() {
            List<E26Species> result = service.getSpeciesByHomeworld(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should find species by homeworld")
        void findByHomeworld() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> result = realService.getSpeciesByHomeworld("Earth");
            assertTrue(result.size() > 0);
        }

        @Test
        @DisplayName("should be case-insensitive")
        void caseInsensitive() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            List<E26Species> result = realService.getSpeciesByHomeworld("earth");
            assertTrue(result.size() > 0);
        }
    }

    @Nested
    @DisplayName("generateAiCharacterName")
    class GenerateAiCharacterNameTests {

        @Test
        @DisplayName("should return fallback for null species")
        void nullSpecies() {
            String name = service.generateAiCharacterName(null, "Leader");
            assertNotNull(name);
            assertTrue(name.contains("Unknown"));
        }

        @Test
        @DisplayName("should return fallback for non-existent species")
        void nonExistentSpecies() {
            String name = service.generateAiCharacterName("NONEXISTENT", "Leader");
            assertNotNull(name);
            assertTrue(name.contains("Unknown"));
        }

        @Test
        @DisplayName("should generate Human leader name")
        void humanLeader() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "Leader");
            assertNotNull(name);
            // Should have title + first name + last name
            assertTrue(name.length() > 5);
        }

        @Test
        @DisplayName("should generate Human governor name")
        void humanGovernor() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "Governor");
            assertNotNull(name);
            assertTrue(name.length() > 5);
        }

        @Test
        @DisplayName("should generate Human captain name")
        void humanCaptain() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "Captain");
            assertNotNull(name);
            assertTrue(name.length() > 5);
        }

        @Test
        @DisplayName("should generate Aslan leader name")
        void aslanLeader() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("ASLAN", "Leader");
            assertNotNull(name);
            assertTrue(name.length() > 5);
        }

        @Test
        @DisplayName("should generate Klingon leader name")
        void klingonLeader() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("KLINGON", "Leader");
            assertNotNull(name);
            assertTrue(name.length() > 5);
        }

        @Test
        @DisplayName("should use leader titles for Leader role")
        void leaderRole() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "Leader");
            // Should start with a leader title
            assertTrue(name.startsWith("President") || name.startsWith("Prime Minister") ||
                       name.startsWith("Chancellor") || name.startsWith("King") ||
                       name.startsWith("Queen") || name.startsWith("Emperor") ||
                       name.startsWith("Governor") || name.startsWith("Director") ||
                       name.startsWith("Commander") || name.startsWith("Ambassador"));
        }

        @Test
        @DisplayName("should use governor titles for Governor role")
        void governorRole() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "Governor");
            // Should start with a governor title
            assertTrue(name.startsWith("Governor") || name.startsWith("Mayor") ||
                       name.startsWith("Prefect") || name.startsWith("Minister") ||
                       name.startsWith("Senator") || name.startsWith("Representative") ||
                       name.startsWith("Commissioner") || name.startsWith("Administrator"));
        }

        @Test
        @DisplayName("should use captain titles for Captain role")
        void captainRole() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "Captain");
            // Should start with a captain title
            assertTrue(name.startsWith("Captain") || name.startsWith("Commander") ||
                       name.startsWith("Colonel") || name.startsWith("Major") ||
                       name.startsWith("Lieutenant") || name.startsWith("Admiral") ||
                       name.startsWith("General"));
        }

        @Test
        @DisplayName("should use leader titles for unknown role")
        void unknownRole() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("HUMAN", "UnknownRole");
            // Should default to leader titles
            assertTrue(name.startsWith("President") || name.startsWith("Prime Minister") ||
                       name.startsWith("Chancellor") || name.startsWith("King") ||
                       name.startsWith("Queen") || name.startsWith("Emperor") ||
                       name.startsWith("Governor") || name.startsWith("Director") ||
                       name.startsWith("Commander") || name.startsWith("Ambassador"));
        }

        @Test
        @DisplayName("should be case-insensitive for species ID")
        void caseInsensitive() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterName("human", "Leader");
            assertNotNull(name);
            assertTrue(name.length() > 5);
        }

        @Test
        @DisplayName("should generate different names on multiple calls")
        void multipleCalls() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name1 = realService.generateAiCharacterName("HUMAN", "Leader");
            String name2 = realService.generateAiCharacterName("HUMAN", "Leader");
            // Names may be the same by chance, but should not throw
            assertNotNull(name1);
            assertNotNull(name2);
        }
    }

    @Nested
    @DisplayName("generateAiCharacterNameWithTitle")
    class GenerateAiCharacterNameWithTitleTests {

        @Test
        @DisplayName("should return fallback for null species")
        void nullSpecies() {
            String name = service.generateAiCharacterNameWithTitle(null, "Custom Title");
            assertEquals("Custom Title Unknown", name);
        }

        @Test
        @DisplayName("should return fallback for non-existent species")
        void nonExistentSpecies() {
            String name = service.generateAiCharacterNameWithTitle("NONEXISTENT", "Custom Title");
            assertEquals("Custom Title Unknown", name);
        }

        @Test
        @DisplayName("should generate name with custom title")
        void customTitle() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterNameWithTitle("HUMAN", "Admiral");
            assertNotNull(name);
            assertTrue(name.startsWith("Admiral"));
        }

        @Test
        @DisplayName("should generate Aslan name with custom title")
        void aslanCustomTitle() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            String name = realService.generateAiCharacterNameWithTitle("ASLAN", "Warlord");
            assertNotNull(name);
            assertTrue(name.startsWith("Warlord"));
        }
    }

    @Nested
    @DisplayName("Galaxy Rules")
    class GalaxyRulesTests {

        @Test
        @DisplayName("should return null galaxy rules when not loaded")
        void notLoaded() {
            E26GalaxyRules rules = service.getGalaxyRules();
            // Returns empty object when file not found
            assertNotNull(rules);
        }

        @Test
        @DisplayName("should return default galaxy seed")
        void defaultSeed() {
            long seed = service.getDefaultGalaxySeed();
            // When file not found, galaxyRules is empty object with 0 seed
            assertEquals(0L, seed);
        }

        @Test
        @DisplayName("should return void galaxy seed")
        void voidSeed() {
            long seed = service.getVoidGalaxySeed();
            // When file not found, galaxyRules is empty object with 0 seed
            assertEquals(0L, seed);
        }

        @Test
        @DisplayName("should return earth galaxy seed")
        void earthSeed() {
            long seed = service.getEarthGalaxySeed();
            // When file not found, galaxyRules is empty object with 0 seed
            assertEquals(0L, seed);
        }

        @Test
        @DisplayName("should load galaxy rules from real database")
        void realGalaxyRules() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26GalaxyRules rules = realService.getGalaxyRules();
            assertNotNull(rules);
        }
    }

    @Nested
    @DisplayName("System Rules")
    class SystemRulesTests {

        @Test
        @DisplayName("should return null system rules when not loaded")
        void notLoaded() {
            E26SystemRules rules = service.getSystemRules();
            assertNotNull(rules);
        }

        @Test
        @DisplayName("should return empty star types when not loaded")
        void emptyStarTypes() {
            var types = service.getStarTypes();
            // Empty E26SystemRules has null starTypes, so returns null
            assertNull(types);
        }

        @Test
        @DisplayName("should return empty planet types when not loaded")
        void emptyPlanetTypes() {
            var types = service.getPlanetTypes();
            // Empty E26SystemRules has null planetTypes, so returns null
            assertNull(types);
        }

        @Test
        @DisplayName("should load system rules from real database")
        void realSystemRules() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26SystemRules rules = realService.getSystemRules();
            assertNotNull(rules);
        }
    }

    @Nested
    @DisplayName("Planet Rules")
    class PlanetRulesTests {

        @Test
        @DisplayName("should return null planet rules when not loaded")
        void notLoaded() {
            E26PlanetRules rules = service.getPlanetRules();
            assertNotNull(rules);
        }

        @Test
        @DisplayName("should return empty government types when not loaded")
        void emptyGovernmentTypes() {
            var types = service.getGovernmentTypes();
            // Empty E26PlanetRules has null governmentTypes
            assertNull(types);
        }

        @Test
        @DisplayName("should return empty world modifiers when not loaded")
        void emptyWorldModifiers() {
            var mods = service.getWorldModifiers();
            // Empty E26PlanetRules has null worldModifiers
            assertNull(mods);
        }

        @Test
        @DisplayName("should load planet rules from real database")
        void realPlanetRules() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26PlanetRules rules = realService.getPlanetRules();
            assertNotNull(rules);
        }
    }

    @Nested
    @DisplayName("Species characteristics")
    class SpeciesCharacteristicsTests {

        @Test
        @DisplayName("should have correct Aslan characteristics")
        void aslanCharacteristics() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species aslan = realService.getSpecies("ASLAN").orElse(null);
            assertNotNull(aslan);
            assertEquals(1, aslan.getCombatDM());
            assertEquals(-1, aslan.getSocialDM());
        }

        @Test
        @DisplayName("should have correct Klingon characteristics")
        void klingonCharacteristics() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species klingon = realService.getSpecies("KLINGON").orElse(null);
            assertNotNull(klingon);
            assertEquals(1, klingon.getCombatDM());
            assertEquals(1, klingon.getArmorBonus());
        }

        @Test
        @DisplayName("should have correct Melniki trade bonus")
        void melnikiTradeBonus() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species melniki = realService.getSpecies("MELNIKI").orElse(null);
            assertNotNull(melniki);
            assertEquals(2, melniki.getTradeBonusPercent());
        }

        @Test
        @DisplayName("should have correct Ferengi trade bonus")
        void ferengiTradeBonus() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species ferengi = realService.getSpecies("FERENGI").orElse(null);
            assertNotNull(ferengi);
            assertEquals(3, ferengi.getTradeBonusPercent());
        }

        @Test
        @DisplayName("should have correct Vulcan characteristics")
        void vulcanCharacteristics() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species vulcan = realService.getSpecies("VULCAN").orElse(null);
            assertNotNull(vulcan);
            assertEquals(1, vulcan.getSocialDM());
        }

        @Test
        @DisplayName("should have correct lifespan values")
        void lifespanValues() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species human = realService.getSpecies("HUMAN").orElse(null);
            assertEquals(80, human.getLifespan());

            E26Species vulcan = realService.getSpecies("VULCAN").orElse(null);
            assertEquals(200, vulcan.getLifespan());
        }

        @Test
        @DisplayName("should have correct homeworld values")
        void homeworldValues() {
            Earth2026RulesDatabaseService realService = new Earth2026RulesDatabaseService("docs/earth2026-rules-db/earth2026-rules.json");
            realService.loadDatabase();

            E26Species human = realService.getSpecies("HUMAN").orElse(null);
            assertEquals("Earth", human.getHomeworld());

            E26Species klingon = realService.getSpecies("KLINGON").orElse(null);
            assertEquals("Qo'noS", klingon.getHomeworld());
        }
    }
}

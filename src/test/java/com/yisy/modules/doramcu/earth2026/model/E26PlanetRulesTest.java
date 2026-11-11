package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E26PlanetRules Tests")
class E26PlanetRulesTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty rules")
        void noArgConstructor() {
            E26PlanetRules rules = new E26PlanetRules();
            assertNull(rules.getGovernmentTypes());
            assertNull(rules.getLawLevels());
            assertNull(rules.getWorldModifiers());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get government types")
        void governmentTypes() {
            E26PlanetRules rules = new E26PlanetRules();
            List<E26PlanetRules.E26GovernmentType> govts = List.of(new E26PlanetRules.E26GovernmentType());
            rules.setGovernmentTypes(govts);

            assertSame(govts, rules.getGovernmentTypes());
        }

        @Test
        @DisplayName("should set and get law levels")
        void lawLevels() {
            E26PlanetRules rules = new E26PlanetRules();
            E26PlanetRules.E26LawLevels law = new E26PlanetRules.E26LawLevels();
            rules.setLawLevels(law);

            assertSame(law, rules.getLawLevels());
        }

        @Test
        @DisplayName("should set and get world modifiers")
        void worldModifiers() {
            E26PlanetRules rules = new E26PlanetRules();
            List<E26PlanetRules.E26WorldModifier> mods = List.of(new E26PlanetRules.E26WorldModifier());
            rules.setWorldModifiers(mods);

            assertSame(mods, rules.getWorldModifiers());
        }
    }

    @Nested
    @DisplayName("E26GovernmentType")
    class GovernmentTypeTests {

        @Test
        @DisplayName("no-arg constructor should create empty government type")
        void noArgConstructor() {
            E26PlanetRules.E26GovernmentType govt = new E26PlanetRules.E26GovernmentType();
            assertNull(govt.getId());
            assertNull(govt.getName());
            assertEquals(0, govt.getLawLevel());
            assertEquals(0, govt.getStability());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26PlanetRules.E26GovernmentType govt = new E26PlanetRules.E26GovernmentType();
            govt.setId("DEMOCRACY");
            govt.setName("Democracy");
            govt.setLawLevel(8);
            govt.setStability(7);

            assertEquals("DEMOCRACY", govt.getId());
            assertEquals("Democracy", govt.getName());
            assertEquals(8, govt.getLawLevel());
            assertEquals(7, govt.getStability());
        }

        @Test
        @DisplayName("should support dictatorship government type")
        void dictatorshipType() {
            E26PlanetRules.E26GovernmentType govt = new E26PlanetRules.E26GovernmentType();
            govt.setId("DICTATORSHIP");
            govt.setName("Dictatorship");
            govt.setLawLevel(2);

            assertEquals("DICTATORSHIP", govt.getId());
            assertEquals(2, govt.getLawLevel());
        }

        @Test
        @DisplayName("should support anarchy government type")
        void anarchyType() {
            E26PlanetRules.E26GovernmentType govt = new E26PlanetRules.E26GovernmentType();
            govt.setId("ANARCHY");
            govt.setName("Anarchy");
            govt.setLawLevel(0);
            govt.setStability(1);

            assertEquals("ANARCHY", govt.getId());
            assertEquals(0, govt.getLawLevel());
        }
    }

    @Nested
    @DisplayName("E26LawLevels")
    class LawLevelTests {

        @Test
        @DisplayName("no-arg constructor should create empty law levels")
        void noArgConstructor() {
            E26PlanetRules.E26LawLevels law = new E26PlanetRules.E26LawLevels();
            assertEquals(0, law.getMin());
            assertEquals(0, law.getMax());
            assertNull(law.getDescriptions());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26PlanetRules.E26LawLevels law = new E26PlanetRules.E26LawLevels();
            law.setMin(0);
            law.setMax(9);

            Map<String, String> descriptions = new HashMap<>();
            descriptions.put("0", "Anarchy");
            descriptions.put("9", "Totalitarian");
            law.setDescriptions(descriptions);

            assertEquals(0, law.getMin());
            assertEquals(9, law.getMax());
            assertEquals(2, law.getDescriptions().size());
            assertEquals("Anarchy", law.getDescriptions().get("0"));
            assertEquals("Totalitarian", law.getDescriptions().get("9"));
        }

        @Test
        @DisplayName("should support multiple law level descriptions")
        void multipleDescriptions() {
            E26PlanetRules.E26LawLevels law = new E26PlanetRules.E26LawLevels();
            Map<String, String> descriptions = new HashMap<>();
            descriptions.put("0", "Anarchy");
            descriptions.put("4", "Moderate");
            descriptions.put("9", "Totalitarian");
            law.setDescriptions(descriptions);

            assertEquals(3, law.getDescriptions().size());
        }
    }

    @Nested
    @DisplayName("E26WorldModifier")
    class WorldModifierTests {

        @Test
        @DisplayName("no-arg constructor should create empty modifier")
        void noArgConstructor() {
            E26PlanetRules.E26WorldModifier mod = new E26PlanetRules.E26WorldModifier();
            assertNull(mod.getId());
            assertNull(mod.getName());
            assertNull(mod.getDescription());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26PlanetRules.E26WorldModifier mod = new E26PlanetRules.E26WorldModifier();
            mod.setId("BARREN");
            mod.setName("Barren");
            mod.setDescription("Lifeless world with no atmosphere");

            assertEquals("BARREN", mod.getId());
            assertEquals("Barren", mod.getName());
            assertEquals("Lifeless world with no atmosphere", mod.getDescription());
        }

        @Test
        @DisplayName("should support water world modifier")
        void waterWorldModifier() {
            E26PlanetRules.E26WorldModifier mod = new E26PlanetRules.E26WorldModifier();
            mod.setId("WATER");
            mod.setName("Water World");

            assertEquals("WATER", mod.getId());
        }

        @Test
        @DisplayName("should support gas giant modifier")
        void gasGiantModifier() {
            E26PlanetRules.E26WorldModifier mod = new E26PlanetRules.E26WorldModifier();
            mod.setId("GAS_GIANT");
            mod.setName("Gas Giant");

            assertEquals("GAS_GIANT", mod.getId());
        }
    }

    @Nested
    @DisplayName("Complete Rules")
    class CompleteRulesTests {

        @Test
        @DisplayName("should hold all fields for planet creation")
        void completeRules() {
            E26PlanetRules rules = new E26PlanetRules();

            E26PlanetRules.E26GovernmentType govt = new E26PlanetRules.E26GovernmentType();
            govt.setId("DEMOCRACY");
            govt.setName("Democracy");
            rules.setGovernmentTypes(List.of(govt));

            E26PlanetRules.E26LawLevels law = new E26PlanetRules.E26LawLevels();
            law.setMin(0);
            law.setMax(9);
            rules.setLawLevels(law);

            E26PlanetRules.E26WorldModifier mod = new E26PlanetRules.E26WorldModifier();
            mod.setId("BARREN");
            mod.setName("Barren");
            rules.setWorldModifiers(List.of(mod));

            assertNotNull(rules.getGovernmentTypes());
            assertNotNull(rules.getLawLevels());
            assertNotNull(rules.getWorldModifiers());
            assertEquals("DEMOCRACY", rules.getGovernmentTypes().get(0).getId());
            assertEquals(0, rules.getLawLevels().getMin());
            assertEquals("BARREN", rules.getWorldModifiers().get(0).getId());
        }
    }
}

package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E26GalaxyRules Tests")
class E26GalaxyRulesTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty rules")
        void noArgConstructor() {
            E26GalaxyRules rules = new E26GalaxyRules();
            assertEquals(0L, rules.getDefaultGalaxySeed());
            assertEquals(0L, rules.getVoidGalaxySeed());
            assertEquals(0L, rules.getEarthGalaxySeed());
            assertEquals(0L, rules.getTestGalaxySeed());
            assertEquals(0, rules.getMaxSectorsPerGalaxy());
            assertEquals(0, rules.getMaxSystemsPerSector());
            assertEquals(0, rules.getMaxPlanetsPerSystem());
            assertEquals(0, rules.getMaxMoonsPerPlanet());
            assertEquals(0, rules.getDefaultTechLevel());
            assertEquals(0, rules.getMinTechLevel());
            assertEquals(0, rules.getMaxTechLevel());
            assertNull(rules.getGalaxyTypes());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get galaxy seeds")
        void galaxySeeds() {
            E26GalaxyRules rules = new E26GalaxyRules();
            rules.setDefaultGalaxySeed(42L);
            rules.setVoidGalaxySeed(54321L);
            rules.setEarthGalaxySeed(12345L);
            rules.setTestGalaxySeed(999L);

            assertEquals(42L, rules.getDefaultGalaxySeed());
            assertEquals(54321L, rules.getVoidGalaxySeed());
            assertEquals(12345L, rules.getEarthGalaxySeed());
            assertEquals(999L, rules.getTestGalaxySeed());
        }

        @Test
        @DisplayName("should set and get max values")
        void maxValues() {
            E26GalaxyRules rules = new E26GalaxyRules();
            rules.setMaxSectorsPerGalaxy(100);
            rules.setMaxSystemsPerSector(50);
            rules.setMaxPlanetsPerSystem(10);
            rules.setMaxMoonsPerPlanet(5);

            assertEquals(100, rules.getMaxSectorsPerGalaxy());
            assertEquals(50, rules.getMaxSystemsPerSector());
            assertEquals(10, rules.getMaxPlanetsPerSystem());
            assertEquals(5, rules.getMaxMoonsPerPlanet());
        }

        @Test
        @DisplayName("should set and get tech level range")
        void techLevelRange() {
            E26GalaxyRules rules = new E26GalaxyRules();
            rules.setDefaultTechLevel(5);
            rules.setMinTechLevel(0);
            rules.setMaxTechLevel(20);

            assertEquals(5, rules.getDefaultTechLevel());
            assertEquals(0, rules.getMinTechLevel());
            assertEquals(20, rules.getMaxTechLevel());
        }

        @Test
        @DisplayName("should set and get galaxy types")
        void galaxyTypes() {
            E26GalaxyRules rules = new E26GalaxyRules();
            List<E26GalaxyRules.E26GalaxyType> types = List.of(new E26GalaxyRules.E26GalaxyType());
            rules.setGalaxyTypes(types);

            assertSame(types, rules.getGalaxyTypes());
        }
    }

    @Nested
    @DisplayName("E26GalaxyType")
    class GalaxyTypeTests {

        @Test
        @DisplayName("no-arg constructor should create empty type")
        void noArgConstructor() {
            E26GalaxyRules.E26GalaxyType type = new E26GalaxyRules.E26GalaxyType();
            assertNull(type.getId());
            assertNull(type.getName());
            assertNull(type.getDescription());
            assertEquals(0, type.getMaxArms());
            assertEquals(0, type.getMaxSystemsPerArm());
            assertEquals(0, type.getMaxSystemsPerCore());
            assertEquals(0, type.getMaxSystems());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26GalaxyRules.E26GalaxyType type = new E26GalaxyRules.E26GalaxyType();
            type.setId("SPIRAL");
            type.setName("Spiral Galaxy");
            type.setDescription("A spiral-shaped galaxy");
            type.setMaxArms(4);
            type.setMaxSystemsPerArm(1000);
            type.setMaxSystemsPerCore(500);
            type.setMaxSystems(4500);

            assertEquals("SPIRAL", type.getId());
            assertEquals("Spiral Galaxy", type.getName());
            assertEquals("A spiral-shaped galaxy", type.getDescription());
            assertEquals(4, type.getMaxArms());
            assertEquals(1000, type.getMaxSystemsPerArm());
            assertEquals(500, type.getMaxSystemsPerCore());
            assertEquals(4500, type.getMaxSystems());
        }

        @Test
        @DisplayName("should support elliptical galaxy type")
        void ellipticalType() {
            E26GalaxyRules.E26GalaxyType type = new E26GalaxyRules.E26GalaxyType();
            type.setId("ELLIPTICAL");
            type.setName("Elliptical Galaxy");
            type.setMaxArms(0);

            assertEquals("ELLIPTICAL", type.getId());
            assertEquals(0, type.getMaxArms());
        }

        @Test
        @DisplayName("should support irregular galaxy type")
        void irregularType() {
            E26GalaxyRules.E26GalaxyType type = new E26GalaxyRules.E26GalaxyType();
            type.setId("IRREGULAR");
            type.setName("Irregular Galaxy");

            assertEquals("IRREGULAR", type.getId());
        }
    }

    @Nested
    @DisplayName("Complete Rules")
    class CompleteRulesTests {

        @Test
        @DisplayName("should hold all fields for galaxy creation")
        void completeRules() {
            E26GalaxyRules rules = new E26GalaxyRules();
            rules.setDefaultGalaxySeed(42L);
            rules.setVoidGalaxySeed(54321L);
            rules.setEarthGalaxySeed(12345L);
            rules.setTestGalaxySeed(999L);
            rules.setMaxSectorsPerGalaxy(100);
            rules.setMaxSystemsPerSector(50);
            rules.setMaxPlanetsPerSystem(10);
            rules.setMaxMoonsPerPlanet(5);
            rules.setDefaultTechLevel(5);
            rules.setMinTechLevel(0);
            rules.setMaxTechLevel(20);

            E26GalaxyRules.E26GalaxyType type = new E26GalaxyRules.E26GalaxyType();
            type.setId("SPIRAL");
            type.setName("Spiral");
            rules.setGalaxyTypes(List.of(type));

            assertNotNull(rules.getGalaxyTypes());
            assertEquals(1, rules.getGalaxyTypes().size());
            assertEquals("SPIRAL", rules.getGalaxyTypes().get(0).getId());
        }
    }
}

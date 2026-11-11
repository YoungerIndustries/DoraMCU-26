package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E26SystemRules Tests")
class E26SystemRulesTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty rules")
        void noArgConstructor() {
            E26SystemRules rules = new E26SystemRules();
            assertNull(rules.getStarTypes());
            assertNull(rules.getPlanetTypes());
            assertNull(rules.getHabitableZone());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get star types")
        void starTypes() {
            E26SystemRules rules = new E26SystemRules();
            List<E26SystemRules.E26StarType> stars = List.of(new E26SystemRules.E26StarType());
            rules.setStarTypes(stars);

            assertSame(stars, rules.getStarTypes());
        }

        @Test
        @DisplayName("should set and get planet types")
        void planetTypes() {
            E26SystemRules rules = new E26SystemRules();
            List<E26SystemRules.E26PlanetType> planets = List.of(new E26SystemRules.E26PlanetType());
            rules.setPlanetTypes(planets);

            assertSame(planets, rules.getPlanetTypes());
        }

        @Test
        @DisplayName("should set and get habitable zone")
        void habitableZone() {
            E26SystemRules rules = new E26SystemRules();
            E26SystemRules.E26HabitableZone zone = new E26SystemRules.E26HabitableZone();
            rules.setHabitableZone(zone);

            assertSame(zone, rules.getHabitableZone());
        }
    }

    @Nested
    @DisplayName("E26StarType")
    class StarTypeTests {

        @Test
        @DisplayName("no-arg constructor should create empty star type")
        void noArgConstructor() {
            E26SystemRules.E26StarType star = new E26SystemRules.E26StarType();
            assertNull(star.getId());
            assertNull(star.getName());
            assertNull(star.getMass());
            assertNull(star.getLifespan());
            assertNull(star.getTemperature());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26SystemRules.E26StarType star = new E26SystemRules.E26StarType();
            star.setId("G");
            star.setName("G-type Main Sequence");
            star.setMass("0.84-1.15 solar masses");
            star.setLifespan("10-12 billion years");
            star.setTemperature("5,200-6,000 K");

            assertEquals("G", star.getId());
            assertEquals("G-type Main Sequence", star.getName());
            assertEquals("0.84-1.15 solar masses", star.getMass());
            assertEquals("10-12 billion years", star.getLifespan());
            assertEquals("5,200-6,000 K", star.getTemperature());
        }

        @Test
        @DisplayName("should support O-type star")
        void oTypeStar() {
            E26SystemRules.E26StarType star = new E26SystemRules.E26StarType();
            star.setId("O");
            star.setName("O-type Main Sequence");
            star.setTemperature("30,000-50,000 K");

            assertEquals("O", star.getId());
        }

        @Test
        @DisplayName("should support M-type star")
        void mTypeStar() {
            E26SystemRules.E26StarType star = new E26SystemRules.E26StarType();
            star.setId("M");
            star.setName("M-type Main Sequence");
            star.setTemperature("2,400-3,700 K");

            assertEquals("M", star.getId());
        }
    }

    @Nested
    @DisplayName("E26PlanetType")
    class PlanetTypeTests {

        @Test
        @DisplayName("no-arg constructor should create empty planet type")
        void noArgConstructor() {
            E26SystemRules.E26PlanetType planet = new E26SystemRules.E26PlanetType();
            assertNull(planet.getId());
            assertNull(planet.getName());
            assertNull(planet.getDescription());
            assertEquals(0.0, planet.getMinRadius());
            assertEquals(0.0, planet.getMaxRadius());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26SystemRules.E26PlanetType planet = new E26SystemRules.E26PlanetType();
            planet.setId("TERRESTRIAL");
            planet.setName("Terrestrial Planet");
            planet.setDescription("Rocky planet with solid surface");
            planet.setMinRadius(0.1);
            planet.setMaxRadius(2.0);

            assertEquals("TERRESTRIAL", planet.getId());
            assertEquals("Terrestrial Planet", planet.getName());
            assertEquals("Rocky planet with solid surface", planet.getDescription());
            assertEquals(0.1, planet.getMinRadius());
            assertEquals(2.0, planet.getMaxRadius());
        }

        @Test
        @DisplayName("should support gas giant planet type")
        void gasGiantType() {
            E26SystemRules.E26PlanetType planet = new E26SystemRules.E26PlanetType();
            planet.setId("GAS_GIANT");
            planet.setName("Gas Giant");
            planet.setMinRadius(10.0);
            planet.setMaxRadius(30.0);

            assertEquals("GAS_GIANT", planet.getId());
            assertEquals(10.0, planet.getMinRadius());
        }

        @Test
        @DisplayName("should support ice giant planet type")
        void iceGiantType() {
            E26SystemRules.E26PlanetType planet = new E26SystemRules.E26PlanetType();
            planet.setId("ICE_GIANT");
            planet.setName("Ice Giant");

            assertEquals("ICE_GIANT", planet.getId());
        }
    }

    @Nested
    @DisplayName("E26HabitableZone")
    class HabitableZoneTests {

        @Test
        @DisplayName("no-arg constructor should create empty zone")
        void noArgConstructor() {
            E26SystemRules.E26HabitableZone zone = new E26SystemRules.E26HabitableZone();
            assertEquals(0.0, zone.getMinDistance());
            assertEquals(0.0, zone.getMaxDistance());
            assertNull(zone.getUnit());
        }

        @Test
        @DisplayName("should set and get all fields")
        void allFields() {
            E26SystemRules.E26HabitableZone zone = new E26SystemRules.E26HabitableZone();
            zone.setMinDistance(0.95);
            zone.setMaxDistance(1.37);
            zone.setUnit("AU");

            assertEquals(0.95, zone.getMinDistance());
            assertEquals(1.37, zone.getMaxDistance());
            assertEquals("AU", zone.getUnit());
        }

        @Test
        @DisplayName("should support different units")
        void differentUnits() {
            E26SystemRules.E26HabitableZone zone = new E26SystemRules.E26HabitableZone();
            zone.setUnit("km");
            assertEquals("km", zone.getUnit());
        }
    }

    @Nested
    @DisplayName("Complete Rules")
    class CompleteRulesTests {

        @Test
        @DisplayName("should hold all fields for system creation")
        void completeRules() {
            E26SystemRules rules = new E26SystemRules();

            E26SystemRules.E26StarType star = new E26SystemRules.E26StarType();
            star.setId("G");
            star.setName("G-type");
            rules.setStarTypes(List.of(star));

            E26SystemRules.E26PlanetType planet = new E26SystemRules.E26PlanetType();
            planet.setId("TERRESTRIAL");
            planet.setName("Terrestrial");
            rules.setPlanetTypes(List.of(planet));

            E26SystemRules.E26HabitableZone zone = new E26SystemRules.E26HabitableZone();
            zone.setMinDistance(0.95);
            zone.setMaxDistance(1.37);
            zone.setUnit("AU");
            rules.setHabitableZone(zone);

            assertNotNull(rules.getStarTypes());
            assertNotNull(rules.getPlanetTypes());
            assertNotNull(rules.getHabitableZone());
            assertEquals("G", rules.getStarTypes().get(0).getId());
            assertEquals("TERRESTRIAL", rules.getPlanetTypes().get(0).getId());
            assertEquals("AU", rules.getHabitableZone().getUnit());
        }
    }
}

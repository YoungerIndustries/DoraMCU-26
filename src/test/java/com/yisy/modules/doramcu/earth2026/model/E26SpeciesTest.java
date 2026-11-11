package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E26Species Tests")
class E26SpeciesTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty species")
        void noArgConstructor() {
            E26Species species = new E26Species();
            assertNull(species.getId());
            assertNull(species.getName());
            assertNull(species.getDescription());
            assertNull(species.getGameModule());
            assertNull(species.getCharacteristics());
            assertEquals(0, species.getCombatDM());
            assertEquals(0, species.getSocialDM());
            assertEquals(0, species.getNavigationDM());
            assertEquals(0, species.getArmorBonus());
            assertEquals(0, species.getTradeBonusPercent());
            assertEquals(0, species.getLifespan());
            assertNull(species.getHomeworld());
            assertNull(species.getNameDatabase());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all basic fields")
        void basicFields() {
            E26Species species = new E26Species();
            species.setId("HUMAN");
            species.setName("Human");
            species.setDescription("The dominant species of Earth");
            species.setGameModule("EARTH_2026");
            species.setHomeworld("Earth");

            assertEquals("HUMAN", species.getId());
            assertEquals("Human", species.getName());
            assertEquals("The dominant species of Earth", species.getDescription());
            assertEquals("EARTH_2026", species.getGameModule());
            assertEquals("Earth", species.getHomeworld());
        }

        @Test
        @DisplayName("should set and get characteristics map")
        void characteristics() {
            E26Species species = new E26Species();
            Map<String, Integer> chars = new HashMap<>();
            chars.put("ST", 1);
            chars.put("DX", 0);
            chars.put("EN", 1);
            species.setCharacteristics(chars);

            assertEquals(3, species.getCharacteristics().size());
            assertEquals(1, species.getCharacteristics().get("ST"));
            assertEquals(0, species.getCharacteristics().get("DX"));
            assertEquals(1, species.getCharacteristics().get("EN"));
        }

        @Test
        @DisplayName("should set and get DM values")
        void dmValues() {
            E26Species species = new E26Species();
            species.setCombatDM(1);
            species.setSocialDM(-1);
            species.setNavigationDM(0);

            assertEquals(1, species.getCombatDM());
            assertEquals(-1, species.getSocialDM());
            assertEquals(0, species.getNavigationDM());
        }

        @Test
        @DisplayName("should set and get bonus values")
        void bonusValues() {
            E26Species species = new E26Species();
            species.setArmorBonus(1);
            species.setTradeBonusPercent(3);
            species.setLifespan(80);

            assertEquals(1, species.getArmorBonus());
            assertEquals(3, species.getTradeBonusPercent());
            assertEquals(80, species.getLifespan());
        }

        @Test
        @DisplayName("should set and get name database")
        void nameDatabase() {
            E26Species species = new E26Species();
            E26NameDatabase db = new E26NameDatabase();
            species.setNameDatabase(db);

            assertSame(db, species.getNameDatabase());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should return name and id")
        void toStringFormat() {
            E26Species species = new E26Species();
            species.setId("ASLAN");
            species.setName("Aslan");

            assertEquals("Aslan (ASLAN)", species.toString());
        }

        @Test
        @DisplayName("should handle null values")
        void toStringNull() {
            E26Species species = new E26Species();
            assertEquals("null (null)", species.toString());
        }
    }

    @Nested
    @DisplayName("Game Module")
    class GameModuleTests {

        @Test
        @DisplayName("should support Traveller game module")
        void travellerModule() {
            E26Species species = new E26Species();
            species.setId("VARGR");
            species.setGameModule("TRAVELLER");
            assertEquals("TRAVELLER", species.getGameModule());
        }

        @Test
        @DisplayName("should support Captain's Log game module")
        void captainsLogModule() {
            E26Species species = new E26Species();
            species.setId("KLINGON");
            species.setGameModule("CAPTAINS_LOG");
            assertEquals("CAPTAINS_LOG", species.getGameModule());
        }

        @Test
        @DisplayName("should support Babylon 5 game module")
        void babylon5Module() {
            E26Species species = new E26Species();
            species.setId("NARN");
            species.setGameModule("BABYLON5");
            assertEquals("BABYLON5", species.getGameModule());
        }
    }
}

package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Race Tests")
class B5RaceTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            B5Race race = new B5Race();
            assertNull(race.getId());
            assertNull(race.getName());
            assertNull(race.getDescription());
            assertNull(race.getHomeworld());
            assertNull(race.getGovernment());
            assertEquals(0, race.getLifespan());
            assertNull(race.getCharacteristics());
            assertNull(race.getSpecialAbilities());
            assertNull(race.getCombatStyle());
            assertEquals(0, race.getTradeBonus());
            assertEquals(0, race.getSocialDM());
            assertEquals(0, race.getNavigationDM());
            assertEquals(0, race.getArmorBonus());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get id")
        void id() {
            B5Race race = new B5Race();
            race.setId("human");
            assertEquals("human", race.getId());
        }

        @Test
        @DisplayName("Set and get name")
        void name() {
            B5Race race = new B5Race();
            race.setName("Human");
            assertEquals("Human", race.getName());
        }

        @Test
        @DisplayName("Set and get description")
        void description() {
            B5Race race = new B5Race();
            race.setDescription("Earth Alliance humans");
            assertEquals("Earth Alliance humans", race.getDescription());
        }

        @Test
        @DisplayName("Set and get homeworld")
        void homeworld() {
            B5Race race = new B5Race();
            race.setHomeworld("Earth");
            assertEquals("Earth", race.getHomeworld());
        }

        @Test
        @DisplayName("Set and get government")
        void government() {
            B5Race race = new B5Race();
            race.setGovernment("Democracy");
            assertEquals("Democracy", race.getGovernment());
        }

        @Test
        @DisplayName("Set and get lifespan")
        void lifespan() {
            B5Race race = new B5Race();
            race.setLifespan(80);
            assertEquals(80, race.getLifespan());
        }

        @Test
        @DisplayName("Set and get combat style")
        void combatStyle() {
            B5Race race = new B5Race();
            race.setCombatStyle("Firearms");
            assertEquals("Firearms", race.getCombatStyle());
        }

        @Test
        @DisplayName("Set and get trade bonus")
        void tradeBonus() {
            B5Race race = new B5Race();
            race.setTradeBonus(5);
            assertEquals(5, race.getTradeBonus());
        }

        @Test
        @DisplayName("Set and get social DM")
        void socialDM() {
            B5Race race = new B5Race();
            race.setSocialDM(2);
            assertEquals(2, race.getSocialDM());
        }

        @Test
        @DisplayName("Set and get navigation DM")
        void navigationDM() {
            B5Race race = new B5Race();
            race.setNavigationDM(-1);
            assertEquals(-1, race.getNavigationDM());
        }

        @Test
        @DisplayName("Set and get armor bonus")
        void armorBonus() {
            B5Race race = new B5Race();
            race.setArmorBonus(1);
            assertEquals(1, race.getArmorBonus());
        }
    }

    @Nested
    @DisplayName("Complex fields")
    class ComplexFields {

        @Test
        @DisplayName("Set and get characteristics")
        void characteristics() {
            B5Race race = new B5Race();
            B5Characteristics chars = new B5Characteristics(8, 8, 8, 8, 8, 8);
            race.setCharacteristics(chars);
            assertSame(chars, race.getCharacteristics());
        }

        @Test
        @DisplayName("Set and get special abilities")
        void specialAbilities() {
            B5Race race = new B5Race();
            List<String> abilities = Arrays.asList("Psionics", "Enhanced Vision");
            race.setSpecialAbilities(abilities);
            assertSame(abilities, race.getSpecialAbilities());
            assertEquals(2, race.getSpecialAbilities().size());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes id, name, and homeworld")
        void toStringContent() {
            B5Race race = new B5Race();
            race.setId("narn");
            race.setName("Narn");
            race.setHomeworld("Narn");
            String result = race.toString();
            assertTrue(result.contains("narn"));
            assertTrue(result.contains("Narn"));
        }
    }
}

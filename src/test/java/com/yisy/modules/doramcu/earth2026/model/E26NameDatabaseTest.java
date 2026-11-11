package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E26NameDatabase Tests")
class E26NameDatabaseTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty name database")
        void noArgConstructor() {
            E26NameDatabase db = new E26NameDatabase();
            assertNull(db.getMaleFirstNames());
            assertNull(db.getFemaleFirstNames());
            assertNull(db.getLastNames());
            assertNull(db.getTitlePrefixes());
            assertNull(db.getTitleSuffixes());
            assertNull(db.getAiLeaderTitles());
            assertNull(db.getAiGovernorTitles());
            assertNull(db.getAiCaptainTitles());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get male first names")
        void maleFirstNames() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> males = List.of("James", "John", "Robert");
            db.setMaleFirstNames(males);

            assertSame(males, db.getMaleFirstNames());
            assertEquals(3, db.getMaleFirstNames().size());
        }

        @Test
        @DisplayName("should set and get female first names")
        void femaleFirstNames() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> females = List.of("Mary", "Patricia", "Jennifer");
            db.setFemaleFirstNames(females);

            assertSame(females, db.getFemaleFirstNames());
            assertEquals(3, db.getFemaleFirstNames().size());
        }

        @Test
        @DisplayName("should set and get last names")
        void lastNames() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> lasts = List.of("Smith", "Johnson", "Williams");
            db.setLastNames(lasts);

            assertSame(lasts, db.getLastNames());
            assertEquals(3, db.getLastNames().size());
        }

        @Test
        @DisplayName("should set and get title prefixes")
        void titlePrefixes() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> prefixes = List.of("Mr.", "Mrs.", "Dr.");
            db.setTitlePrefixes(prefixes);

            assertSame(prefixes, db.getTitlePrefixes());
        }

        @Test
        @DisplayName("should set and get title suffixes")
        void titleSuffixes() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> suffixes = List.of("Jr.", "Sr.", "III");
            db.setTitleSuffixes(suffixes);

            assertSame(suffixes, db.getTitleSuffixes());
        }

        @Test
        @DisplayName("should set and get AI leader titles")
        void aiLeaderTitles() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> leaders = List.of("President", "Prime Minister", "Chancellor");
            db.setAiLeaderTitles(leaders);

            assertSame(leaders, db.getAiLeaderTitles());
        }

        @Test
        @DisplayName("should set and get AI governor titles")
        void aiGovernorTitles() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> governors = List.of("Governor", "Mayor", "Prefect");
            db.setAiGovernorTitles(governors);

            assertSame(governors, db.getAiGovernorTitles());
        }

        @Test
        @DisplayName("should set and get AI captain titles")
        void aiCaptainTitles() {
            E26NameDatabase db = new E26NameDatabase();
            List<String> captains = List.of("Captain", "Commander", "Colonel");
            db.setAiCaptainTitles(captains);

            assertSame(captains, db.getAiCaptainTitles());
        }
    }

    @Nested
    @DisplayName("Complete Name Database")
    class CompleteDatabaseTests {

        @Test
        @DisplayName("should hold all fields for a species")
        void completeDatabase() {
            E26NameDatabase db = new E26NameDatabase();
            db.setMaleFirstNames(List.of("James", "John"));
            db.setFemaleFirstNames(List.of("Mary", "Patricia"));
            db.setLastNames(List.of("Smith", "Johnson"));
            db.setTitlePrefixes(List.of("Mr.", "Dr."));
            db.setTitleSuffixes(List.of("Jr.", "Sr."));
            db.setAiLeaderTitles(List.of("President", "Chancellor"));
            db.setAiGovernorTitles(List.of("Governor", "Mayor"));
            db.setAiCaptainTitles(List.of("Captain", "Commander"));

            assertNotNull(db.getMaleFirstNames());
            assertNotNull(db.getFemaleFirstNames());
            assertNotNull(db.getLastNames());
            assertNotNull(db.getTitlePrefixes());
            assertNotNull(db.getTitleSuffixes());
            assertNotNull(db.getAiLeaderTitles());
            assertNotNull(db.getAiGovernorTitles());
            assertNotNull(db.getAiCaptainTitles());
        }

        @Test
        @DisplayName("should handle empty lists")
        void emptyLists() {
            E26NameDatabase db = new E26NameDatabase();
            db.setMaleFirstNames(List.of());
            db.setFemaleFirstNames(List.of());
            db.setLastNames(List.of());

            assertTrue(db.getMaleFirstNames().isEmpty());
            assertTrue(db.getFemaleFirstNames().isEmpty());
            assertTrue(db.getLastNames().isEmpty());
        }
    }
}

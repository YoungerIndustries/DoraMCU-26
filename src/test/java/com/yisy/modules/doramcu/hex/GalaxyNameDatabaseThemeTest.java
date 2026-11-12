package com.yisy.modules.doramcu.hex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GalaxyNameDatabase Theme Tests")
class GalaxyNameDatabaseThemeTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Loads all names without theme filter")
        void loadsAllNames() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertTrue(db.getNameCount() > 50, "Should load all ~140 names");
        }

        @Test
        @DisplayName("No active theme by default")
        void noActiveTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertNull(db.getActiveTheme());
        }

        @Test
        @DisplayName("Has available themes")
        void hasAvailableThemes() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            Set<String> themes = db.getAvailableThemes();
            assertFalse(themes.isEmpty());
            assertTrue(themes.contains("astronomical"));
            assertTrue(themes.contains("mythological"));
            assertTrue(themes.contains("star"));
        }
    }

    @Nested
    @DisplayName("Theme-filtered constructor")
    class ThemeFilteredConstructor {

        @Test
        @DisplayName("Astronomical theme loads astronomical names only")
        void astronomicalTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "astronomical");
            assertEquals("astronomical", db.getActiveTheme());
            assertTrue(db.getNameCount() > 30, "Astronomical theme should have ~47 names");
            // All names should be from the astronomical theme
            List<String> names = db.getAllNames();
            assertTrue(names.contains("Andromeda"));
            assertTrue(names.contains("Milky Way"));
            assertFalse(names.contains("Asgard"), "Should not contain mythological names");
            assertFalse(names.contains("Sirius"), "Should not contain star names");
        }

        @Test
        @DisplayName("Mythological theme loads mythological names only")
        void mythologicalTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "mythological");
            assertEquals("mythological", db.getActiveTheme());
            assertTrue(db.getNameCount() > 20, "Mythological theme should have ~35 names");
            List<String> names = db.getAllNames();
            assertTrue(names.contains("Asgard"));
            assertTrue(names.contains("Olympus"));
            assertFalse(names.contains("Andromeda"), "Should not contain astronomical names");
        }

        @Test
        @DisplayName("Star theme loads star names only")
        void starTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "star");
            assertEquals("star", db.getActiveTheme());
            assertTrue(db.getNameCount() > 30, "Star theme should have ~47 names");
            List<String> names = db.getAllNames();
            assertTrue(names.contains("Sirius"));
            assertTrue(names.contains("Vega"));
            assertFalse(names.contains("Asgard"), "Should not contain mythological names");
        }

        @Test
        @DisplayName("Unknown theme falls back to all names")
        void unknownThemeFallsBack() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "unknown");
            assertNull(db.getActiveTheme());
            assertEquals(340, db.getNameCount(), "Should load all names");
        }

        @Test
        @DisplayName("Null theme loads all names")
        void nullThemeLoadsAll() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", null);
            assertNull(db.getActiveTheme());
            assertTrue(db.getNameCount() > 50);
        }
    }

    @Nested
    @DisplayName("getNamesByTheme")
    class GetNamesByTheme {

        @Test
        @DisplayName("Returns names for valid theme")
        void returnsNamesForValidTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            List<String> astro = db.getNamesByTheme("astronomical");
            assertFalse(astro.isEmpty());
            assertTrue(astro.contains("Andromeda"));
        }

        @Test
        @DisplayName("Returns empty list for unknown theme")
        void returnsEmptyForUnknownTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertTrue(db.getNamesByTheme("unknown").isEmpty());
        }

        @Test
        @DisplayName("Returns empty list for null theme")
        void returnsEmptyForNullTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertTrue(db.getNamesByTheme(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("setActiveTheme")
    class SetActiveTheme {

        @Test
        @DisplayName("Switch to astronomical theme")
        void switchToAstronomical() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            db.setActiveTheme("astronomical");
            assertEquals("astronomical", db.getActiveTheme());
            List<String> names = db.getAllNames();
            assertTrue(names.contains("Andromeda"));
            assertFalse(names.contains("Asgard"));
        }

        @Test
        @DisplayName("Switch to mythological theme")
        void switchToMythological() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            db.setActiveTheme("mythological");
            assertEquals("mythological", db.getActiveTheme());
            List<String> names = db.getAllNames();
            assertTrue(names.contains("Asgard"));
            assertFalse(names.contains("Andromeda"));
        }

        @Test
        @DisplayName("Switch to star theme")
        void switchToStar() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            db.setActiveTheme("star");
            assertEquals("star", db.getActiveTheme());
            List<String> names = db.getAllNames();
            assertTrue(names.contains("Sirius"));
        }

        @Test
        @DisplayName("Switch to null resets to all names")
        void switchToNull() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "astronomical");
            db.setActiveTheme(null);
            assertNull(db.getActiveTheme());
            assertTrue(db.getNameCount() > 50);
        }

        @Test
        @DisplayName("Switch to unknown theme resets to all names")
        void switchToUnknown() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "astronomical");
            db.setActiveTheme("unknown");
            assertNull(db.getActiveTheme());
            assertTrue(db.getNameCount() > 50);
        }
    }

    @Nested
    @DisplayName("Theme name counts")
    class ThemeNameCounts {

        @Test
        @DisplayName("Astronomical theme has correct count")
        void astronomicalCount() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertEquals(258, db.getNamesByTheme("astronomical").size());
        }

        @Test
        @DisplayName("Mythological theme has correct count")
        void mythologicalCount() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertEquals(38, db.getNamesByTheme("mythological").size());
        }

        @Test
        @DisplayName("Star theme has correct count")
        void starCount() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            assertEquals(47, db.getNamesByTheme("star").size());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("Includes theme in toString")
        void includesTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase("NGC", "astronomical");
            String str = db.toString();
            assertTrue(str.contains("theme='astronomical'"));
        }

        @Test
        @DisplayName("Null theme in toString")
        void nullTheme() {
            GalaxyNameDatabase db = new GalaxyNameDatabase();
            String str = db.toString();
            assertTrue(str.contains("theme='null'"));
        }
    }
}

package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Location Tests")
class Earth2026LocationTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty location")
        void noArgConstructor() {
            Earth2026Location location = new Earth2026Location();
            assertNull(location.getId());
            assertNull(location.getName());
            assertNull(location.getCity());
            assertNull(location.getState());
            assertNull(location.getCountry());
            assertNull(location.getType());
            assertNull(location.getDescription());
            assertFalse(location.isHasPortal());
            assertNull(location.getPortalGalaxy());
            assertTrue(location.isActive());
            assertFalse(location.isSecret());
            assertNull(location.getLatitude());
            assertNull(location.getLongitude());
        }

        @Test
        @DisplayName("full constructor should set name, city, country, type")
        void fullConstructor() {
            Earth2026Location location = new Earth2026Location("San Diego", "San Diego", "USA", "City");
            assertEquals("San Diego", location.getName());
            assertEquals("San Diego", location.getCity());
            assertEquals("USA", location.getCountry());
            assertEquals("City", location.getType());
            assertTrue(location.isActive());
            assertFalse(location.isHasPortal());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get name")
        void name() {
            Earth2026Location location = new Earth2026Location();
            location.setName("Area 51");
            assertEquals("Area 51", location.getName());
        }

        @Test
        @DisplayName("should set and get city")
        void city() {
            Earth2026Location location = new Earth2026Location();
            location.setCity("Rachel");
            assertEquals("Rachel", location.getCity());
        }

        @Test
        @DisplayName("should set and get state")
        void state() {
            Earth2026Location location = new Earth2026Location();
            location.setState("Nevada");
            assertEquals("Nevada", location.getState());
        }

        @Test
        @DisplayName("should set and get country")
        void country() {
            Earth2026Location location = new Earth2026Location();
            location.setCountry("USA");
            assertEquals("USA", location.getCountry());
        }

        @Test
        @DisplayName("should set and get type")
        void type() {
            Earth2026Location location = new Earth2026Location();
            location.setType("Secret Base");
            assertEquals("Secret Base", location.getType());
        }

        @Test
        @DisplayName("should set and get description")
        void description() {
            Earth2026Location location = new Earth2026Location();
            location.setDescription("Top secret military base");
            assertEquals("Top secret military base", location.getDescription());
        }

        @Test
        @DisplayName("should set and get latitude")
        void latitude() {
            Earth2026Location location = new Earth2026Location();
            location.setLatitude(37.235);
            assertEquals(37.235, location.getLatitude(), 0.001);
        }

        @Test
        @DisplayName("should set and get longitude")
        void longitude() {
            Earth2026Location location = new Earth2026Location();
            location.setLongitude(-115.811);
            assertEquals(-115.811, location.getLongitude(), 0.001);
        }
    }

    @Nested
    @DisplayName("Portal")
    class PortalTests {

        @Test
        @DisplayName("should set and get portal flag")
        void hasPortal() {
            Earth2026Location location = new Earth2026Location();
            location.setHasPortal(true);
            assertTrue(location.isHasPortal());
        }

        @Test
        @DisplayName("should set and get portal galaxy")
        void portalGalaxy() {
            Earth2026Location location = new Earth2026Location();
            location.setPortalGalaxy("Milky Way");
            assertEquals("Milky Way", location.getPortalGalaxy());
        }

        @Test
        @DisplayName("portal location should have galaxy set")
        void portalWithGalaxy() {
            Earth2026Location location = new Earth2026Location();
            location.setHasPortal(true);
            location.setPortalGalaxy("The Void");
            assertTrue(location.isHasPortal());
            assertEquals("The Void", location.getPortalGalaxy());
        }
    }

    @Nested
    @DisplayName("Active and Secret")
    class ActiveSecretTests {

        @Test
        @DisplayName("should set and get active flag")
        void active() {
            Earth2026Location location = new Earth2026Location();
            assertTrue(location.isActive());
            location.setActive(false);
            assertFalse(location.isActive());
        }

        @Test
        @DisplayName("should set and get secret flag")
        void secret() {
            Earth2026Location location = new Earth2026Location();
            assertFalse(location.isSecret());
            location.setSecret(true);
            assertTrue(location.isSecret());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Location location = new Earth2026Location("NYC", "New York", "USA", "City");
            location.setId(3L);
            location.setHasPortal(true);

            String str = location.toString();
            assertTrue(str.contains("id=3"));
            assertTrue(str.contains("name='NYC'"));
            assertTrue(str.contains("city='New York'"));
            assertTrue(str.contains("type='City'"));
            assertTrue(str.contains("portal=true"));
        }
    }
}

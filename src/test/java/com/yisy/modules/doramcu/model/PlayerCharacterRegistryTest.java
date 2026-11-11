package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerCharacterRegistry")
class PlayerCharacterRegistryTest {

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        return user;
    }

    @Nested
    @DisplayName("Constructor and defaults")
    class Constructor {

        @Test
        @DisplayName("default constructor sets null id")
        void defaultConstructor() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            assertNull(entry.getId());
        }

        @Test
        @DisplayName("default isAiControlled is false")
        void defaultAiControlled() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            assertFalse(entry.isAiControlled());
        }

        @Test
        @DisplayName("createdAt is null before persist")
        void createdAtNullBeforePersist() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            assertNull(entry.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Getters and setters")
    class GettersSetters {

        @Test
        @DisplayName("id getter and setter")
        void id() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setId(42L);
            assertEquals(42L, entry.getId());
        }

        @Test
        @DisplayName("player getter and setter")
        void player() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            User user = createUser();
            entry.setPlayer(user);
            assertEquals(user, entry.getPlayer());
        }

        @Test
        @DisplayName("characterType getter and setter")
        void characterType() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setCharacterType(CharacterType.TRAVELLER);
            assertEquals(CharacterType.TRAVELLER, entry.getCharacterType());
        }

        @Test
        @DisplayName("characterId getter and setter")
        void characterId() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setCharacterId(100L);
            assertEquals(100L, entry.getCharacterId());
        }

        @Test
        @DisplayName("characterName getter and setter")
        void characterName() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setCharacterName("Alex");
            assertEquals("Alex", entry.getCharacterName());
        }

        @Test
        @DisplayName("characterRole getter and setter")
        void characterRole() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setCharacterRole(CharacterRole.CREW);
            assertEquals(CharacterRole.CREW, entry.getCharacterRole());
        }

        @Test
        @DisplayName("galaxyName getter and setter")
        void galaxyName() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setGalaxyName("Andromeda");
            assertEquals("Andromeda", entry.getGalaxyName());
        }

        @Test
        @DisplayName("aiControlled getter and setter")
        void aiControlled() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setAiControlled(true);
            assertTrue(entry.isAiControlled());
        }

        @Test
        @DisplayName("homeLocationId getter and setter")
        void homeLocationId() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setHomeLocationId("station-1");
            assertEquals("station-1", entry.getHomeLocationId());
        }

        @Test
        @DisplayName("homeLocationType getter and setter")
        void homeLocationType() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setHomeLocationType("CREW_QUARTERS");
            assertEquals("CREW_QUARTERS", entry.getHomeLocationType());
        }

        @Test
        @DisplayName("createdAt getter and setter")
        void createdAt() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
            entry.setCreatedAt(now);
            assertEquals(now, entry.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("prePersist")
    class PrePersist {

        @Test
        @DisplayName("sets createdAt when null")
        void setsCreatedAtWhenNull() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            assertNull(entry.getCreatedAt());
            entry.prePersist();
            assertNotNull(entry.getCreatedAt());
        }

        @Test
        @DisplayName("does not overwrite existing createdAt")
        void doesNotOverwrite() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            LocalDateTime existing = LocalDateTime.of(2025, 1, 1, 12, 0);
            entry.setCreatedAt(existing);
            entry.prePersist();
            assertEquals(existing, entry.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("includes character name and role")
        void includesFields() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setCharacterName("Alex");
            entry.setCharacterRole(CharacterRole.CREW);
            entry.setGalaxyName("Andromeda");
            entry.setCharacterType(CharacterType.TRAVELLER);
            entry.setCharacterId(100L);

            String str = entry.toString();

            assertTrue(str.contains("PlayerCharacterRegistry"));
            assertTrue(str.contains("Alex"));
            assertTrue(str.contains("CREW"));
            assertTrue(str.contains("Andromeda"));
        }

        @Test
        @DisplayName("handles null player")
        void handlesNullPlayer() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            String str = entry.toString();
            assertTrue(str.contains("null"));
        }

        @Test
        @DisplayName("handles non-null player")
        void handlesNonNullPlayer() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setPlayer(createUser());
            String str = entry.toString();
            assertTrue(str.contains("testuser"));
        }
    }

    @Nested
    @DisplayName("Combined fields")
    class Combined {

        @Test
        @DisplayName("all fields set independently")
        void allFieldsIndependent() {
            PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
            entry.setId(1L);
            entry.setPlayer(createUser());
            entry.setCharacterType(CharacterType.AI_CREW);
            entry.setCharacterId(50L);
            entry.setCharacterName("Borg");
            entry.setCharacterRole(CharacterRole.CREW);
            entry.setGalaxyName("Milky Way");
            entry.setAiControlled(true);
            entry.setHomeLocationId("wormhole-1");
            entry.setHomeLocationType("WORMHOLE_GATE");

            assertEquals(1L, entry.getId());
            assertEquals(CharacterType.AI_CREW, entry.getCharacterType());
            assertEquals(50L, entry.getCharacterId());
            assertEquals("Borg", entry.getCharacterName());
            assertEquals(CharacterRole.CREW, entry.getCharacterRole());
            assertEquals("Milky Way", entry.getGalaxyName());
            assertTrue(entry.isAiControlled());
            assertEquals("wormhole-1", entry.getHomeLocationId());
            assertEquals("WORMHOLE_GATE", entry.getHomeLocationType());
        }
    }
}

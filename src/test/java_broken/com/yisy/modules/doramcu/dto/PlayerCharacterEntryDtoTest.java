package com.yisy.modules.doramcu.dto;

import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.model.CharacterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerCharacterEntryDto")
class PlayerCharacterEntryDtoTest {

    @Nested
    @DisplayName("Defaults")
    class Defaults {

        @Test
        @DisplayName("no-arg constructor sets null defaults")
        void noArgDefaults() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            assertNull(dto.getId());
            assertNull(dto.getPlayerId());
            assertNull(dto.getPlayerName());
            assertNull(dto.getCharacterType());
            assertNull(dto.getCharacterId());
            assertNull(dto.getCharacterName());
            assertNull(dto.getCharacterRole());
            assertNull(dto.getGalaxyName());
            assertFalse(dto.isAiControlled());
            assertNull(dto.getHomeLocationId());
            assertNull(dto.getHomeLocationType());
            assertNull(dto.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Getters and setters")
    class GettersSetters {

        @Test
        @DisplayName("id getter and setter")
        void id() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setId(1L);
            assertEquals(1L, dto.getId());
        }

        @Test
        @DisplayName("playerId getter and setter")
        void playerId() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setPlayerId(10L);
            assertEquals(10L, dto.getPlayerId());
        }

        @Test
        @DisplayName("playerName getter and setter")
        void playerName() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setPlayerName("testuser");
            assertEquals("testuser", dto.getPlayerName());
        }

        @Test
        @DisplayName("characterType getter and setter")
        void characterType() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setCharacterType(CharacterType.TRAVELLER);
            assertEquals(CharacterType.TRAVELLER, dto.getCharacterType());
        }

        @Test
        @DisplayName("characterId getter and setter")
        void characterId() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setCharacterId(100L);
            assertEquals(100L, dto.getCharacterId());
        }

        @Test
        @DisplayName("characterName getter and setter")
        void characterName() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setCharacterName("Alex");
            assertEquals("Alex", dto.getCharacterName());
        }

        @Test
        @DisplayName("characterRole getter and setter")
        void characterRole() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setCharacterRole(CharacterRole.CREW);
            assertEquals(CharacterRole.CREW, dto.getCharacterRole());
        }

        @Test
        @DisplayName("galaxyName getter and setter")
        void galaxyName() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setGalaxyName("Andromeda");
            assertEquals("Andromeda", dto.getGalaxyName());
        }

        @Test
        @DisplayName("aiControlled getter and setter")
        void aiControlled() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setAiControlled(true);
            assertTrue(dto.isAiControlled());
        }

        @Test
        @DisplayName("homeLocationId getter and setter")
        void homeLocationId() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setHomeLocationId("station-1");
            assertEquals("station-1", dto.getHomeLocationId());
        }

        @Test
        @DisplayName("homeLocationType getter and setter")
        void homeLocationType() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setHomeLocationType("CREW_QUARTERS");
            assertEquals("CREW_QUARTERS", dto.getHomeLocationType());
        }

        @Test
        @DisplayName("createdAt getter and setter")
        void createdAt() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
            dto.setCreatedAt(now);
            assertEquals(now, dto.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Combined")
    class Combined {

        @Test
        @DisplayName("all fields set independently")
        void allFieldsIndependent() {
            PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
            dto.setId(1L);
            dto.setPlayerId(10L);
            dto.setPlayerName("testuser");
            dto.setCharacterType(CharacterType.AI_CREW);
            dto.setCharacterId(50L);
            dto.setCharacterName("Borg");
            dto.setCharacterRole(CharacterRole.CREW);
            dto.setGalaxyName("Milky Way");
            dto.setAiControlled(true);
            dto.setHomeLocationId("wormhole-1");
            dto.setHomeLocationType("WORMHOLE_GATE");
            dto.setCreatedAt(LocalDateTime.of(2025, 6, 15, 8, 30));

            assertEquals(1L, dto.getId());
            assertEquals(10L, dto.getPlayerId());
            assertEquals("testuser", dto.getPlayerName());
            assertEquals(CharacterType.AI_CREW, dto.getCharacterType());
            assertEquals(50L, dto.getCharacterId());
            assertEquals("Borg", dto.getCharacterName());
            assertEquals(CharacterRole.CREW, dto.getCharacterRole());
            assertEquals("Milky Way", dto.getGalaxyName());
            assertTrue(dto.isAiControlled());
            assertEquals("wormhole-1", dto.getHomeLocationId());
            assertEquals("WORMHOLE_GATE", dto.getHomeLocationType());
        }
    }
}

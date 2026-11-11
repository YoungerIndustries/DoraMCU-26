package com.yisy.modules.doramcu.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Galaxy DTOs Tests")
class GalaxyDtoTest {

    @Nested
    @DisplayName("CreateGalaxyRequest")
    class CreateGalaxyRequestTest {

        @Test
        @DisplayName("no-arg constructor should initialize all fields to null")
        void noArgConstructor_shouldInitializeNull() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();

            assertNull(request.getName());
            assertNull(request.getSeed());
            assertNull(request.getSystemCount());
            assertNull(request.getPlanetsPerSystem());
            assertNull(request.getMoonChance());
            assertNull(request.getAsteroidBeltChance());
            assertNull(request.getGasGiantChance());
            assertNull(request.getNebulaChance());
            assertNull(request.getBlackHoleChance());
            assertNull(request.getCometChance());
            assertNull(request.getSpaceStationChance());
            assertNull(request.getHyperlaneDensity());
            assertNull(request.getDescription());
            assertNull(request.getGameType());
            assertNull(request.getDifficultyLevel());
            assertNull(request.getTheme());
        }

        @Test
        @DisplayName("name getter and setter should work")
        void name_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setName("Andromeda");
            assertEquals("Andromeda", request.getName());
        }

        @Test
        @DisplayName("seed getter and setter should work")
        void seed_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setSeed(12345L);
            assertEquals(12345L, request.getSeed());
        }

        @Test
        @DisplayName("systemCount getter and setter should work")
        void systemCount_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setSystemCount(500);
            assertEquals(500, request.getSystemCount());
        }

        @Test
        @DisplayName("planetsPerSystem getter and setter should work")
        void planetsPerSystem_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setPlanetsPerSystem(7);
            assertEquals(7, request.getPlanetsPerSystem());
        }

        @Test
        @DisplayName("moonChance getter and setter should work")
        void moonChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setMoonChance(0.6);
            assertEquals(0.6, request.getMoonChance());
        }

        @Test
        @DisplayName("asteroidBeltChance getter and setter should work")
        void asteroidBeltChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setAsteroidBeltChance(0.7);
            assertEquals(0.7, request.getAsteroidBeltChance());
        }

        @Test
        @DisplayName("gasGiantChance getter and setter should work")
        void gasGiantChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setGasGiantChance(0.4);
            assertEquals(0.4, request.getGasGiantChance());
        }

        @Test
        @DisplayName("nebulaChance getter and setter should work")
        void nebulaChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setNebulaChance(0.2);
            assertEquals(0.2, request.getNebulaChance());
        }

        @Test
        @DisplayName("blackHoleChance getter and setter should work")
        void blackHoleChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setBlackHoleChance(0.1);
            assertEquals(0.1, request.getBlackHoleChance());
        }

        @Test
        @DisplayName("cometChance getter and setter should work")
        void cometChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setCometChance(0.3);
            assertEquals(0.3, request.getCometChance());
        }

        @Test
        @DisplayName("spaceStationChance getter and setter should work")
        void spaceStationChance_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setSpaceStationChance(0.15);
            assertEquals(0.15, request.getSpaceStationChance());
        }

        @Test
        @DisplayName("hyperlaneDensity getter and setter should work")
        void hyperlaneDensity_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setHyperlaneDensity(1.5);
            assertEquals(1.5, request.getHyperlaneDensity());
        }

        @Test
        @DisplayName("description getter and setter should work")
        void description_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setDescription("A distant galaxy");
            assertEquals("A distant galaxy", request.getDescription());
        }

        @Test
        @DisplayName("gameType getter and setter should work")
        void gameType_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setGameType("CaptainsLog");
            assertEquals("CaptainsLog", request.getGameType());
        }

        @Test
        @DisplayName("difficultyLevel getter and setter should work")
        void difficultyLevel_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setDifficultyLevel("hard");
            assertEquals("hard", request.getDifficultyLevel());
        }

        @Test
        @DisplayName("theme getter and setter should work")
        void theme_getterAndSetter() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setTheme("Mythological");
            assertEquals("Mythological", request.getTheme());
        }

        @Test
        @DisplayName("all fields should be settable together")
        void allFields_shouldBeSettableTogether() {
            CreateGalaxyRequest request = new CreateGalaxyRequest();
            request.setName("Andromeda");
            request.setSeed(999L);
            request.setSystemCount(300);
            request.setPlanetsPerSystem(4);
            request.setMoonChance(0.5);
            request.setAsteroidBeltChance(0.6);
            request.setGasGiantChance(0.3);
            request.setNebulaChance(0.2);
            request.setBlackHoleChance(0.1);
            request.setCometChance(0.25);
            request.setSpaceStationChance(0.12);
            request.setHyperlaneDensity(1.2);
            request.setDescription("Test galaxy");
            request.setGameType("CaptainsLog");
            request.setDifficultyLevel("easy");
            request.setTheme("Sci-Fi");

            assertEquals("Andromeda", request.getName());
            assertEquals(999L, request.getSeed());
            assertEquals(300, request.getSystemCount());
            assertEquals(4, request.getPlanetsPerSystem());
            assertEquals(0.5, request.getMoonChance());
            assertEquals(0.6, request.getAsteroidBeltChance());
            assertEquals(0.3, request.getGasGiantChance());
            assertEquals(0.2, request.getNebulaChance());
            assertEquals(0.1, request.getBlackHoleChance());
            assertEquals(0.25, request.getCometChance());
            assertEquals(0.12, request.getSpaceStationChance());
            assertEquals(1.2, request.getHyperlaneDensity());
            assertEquals("Test galaxy", request.getDescription());
            assertEquals("CaptainsLog", request.getGameType());
            assertEquals("easy", request.getDifficultyLevel());
            assertEquals("Sci-Fi", request.getTheme());
        }
    }

    @Nested
    @DisplayName("GalaxyInfoDto")
    class GalaxyInfoDtoTest {

        @Test
        @DisplayName("no-arg constructor should initialize defaults")
        void noArgConstructor_shouldInitializeDefaults() {
            GalaxyInfoDto dto = new GalaxyInfoDto();

            assertNull(dto.getId());
            assertNull(dto.getName());
            assertEquals(0, dto.getTotalSectors());
            assertEquals(0.0, dto.getHyperlaneDensity());
            assertNull(dto.getSeed());
            assertNull(dto.getSystemCount());
            assertNull(dto.getPlanetsPerSystem());
            assertNull(dto.getMoonChance());
            assertNull(dto.getCreatedAt());
            assertNull(dto.getDescription());
            assertNull(dto.getGameType());
            assertNull(dto.getDifficultyLevel());
            assertNull(dto.getTheme());
        }

        @Test
        @DisplayName("all-args constructor should set all fields")
        void allArgsConstructor_shouldSetAllFields() {
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

            GalaxyInfoDto dto = new GalaxyInfoDto(
                    1L, "Milky Way", 100, 1.0,
                    42L, 500, 5,
                    0.4, 0.5, 0.3,
                    0.15, 0.05, 0.2,
                    0.1, createdAt,
                    "Our home galaxy", "DoraT", "medium", "Sci-Fi Classic"
            );

            assertEquals(1L, dto.getId());
            assertEquals("Milky Way", dto.getName());
            assertEquals(100, dto.getTotalSectors());
            assertEquals(1.0, dto.getHyperlaneDensity());
            assertEquals(42L, dto.getSeed());
            assertEquals(500, dto.getSystemCount());
            assertEquals(5, dto.getPlanetsPerSystem());
            assertEquals(0.4, dto.getMoonChance());
            assertEquals(0.5, dto.getAsteroidBeltChance());
            assertEquals(0.3, dto.getGasGiantChance());
            assertEquals(0.15, dto.getNebulaChance());
            assertEquals(0.05, dto.getBlackHoleChance());
            assertEquals(0.2, dto.getCometChance());
            assertEquals(0.1, dto.getSpaceStationChance());
            assertEquals(createdAt, dto.getCreatedAt());
            assertEquals("Our home galaxy", dto.getDescription());
            assertEquals("DoraT", dto.getGameType());
            assertEquals("medium", dto.getDifficultyLevel());
            assertEquals("Sci-Fi Classic", dto.getTheme());
        }

        @Test
        @DisplayName("id getter and setter should work")
        void id_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setId(42L);
            assertEquals(42L, dto.getId());
        }

        @Test
        @DisplayName("name getter and setter should work")
        void name_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setName("Andromeda");
            assertEquals("Andromeda", dto.getName());
        }

        @Test
        @DisplayName("totalSectors getter and setter should work")
        void totalSectors_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setTotalSectors(200);
            assertEquals(200, dto.getTotalSectors());
        }

        @Test
        @DisplayName("hyperlaneDensity getter and setter should work")
        void hyperlaneDensity_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setHyperlaneDensity(1.5);
            assertEquals(1.5, dto.getHyperlaneDensity());
        }

        @Test
        @DisplayName("seed getter and setter should work")
        void seed_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setSeed(12345L);
            assertEquals(12345L, dto.getSeed());
        }

        @Test
        @DisplayName("systemCount getter and setter should work")
        void systemCount_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setSystemCount(300);
            assertEquals(300, dto.getSystemCount());
        }

        @Test
        @DisplayName("planetsPerSystem getter and setter should work")
        void planetsPerSystem_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setPlanetsPerSystem(7);
            assertEquals(7, dto.getPlanetsPerSystem());
        }

        @Test
        @DisplayName("moonChance getter and setter should work")
        void moonChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setMoonChance(0.6);
            assertEquals(0.6, dto.getMoonChance());
        }

        @Test
        @DisplayName("asteroidBeltChance getter and setter should work")
        void asteroidBeltChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setAsteroidBeltChance(0.7);
            assertEquals(0.7, dto.getAsteroidBeltChance());
        }

        @Test
        @DisplayName("gasGiantChance getter and setter should work")
        void gasGiantChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setGasGiantChance(0.4);
            assertEquals(0.4, dto.getGasGiantChance());
        }

        @Test
        @DisplayName("nebulaChance getter and setter should work")
        void nebulaChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setNebulaChance(0.2);
            assertEquals(0.2, dto.getNebulaChance());
        }

        @Test
        @DisplayName("blackHoleChance getter and setter should work")
        void blackHoleChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setBlackHoleChance(0.1);
            assertEquals(0.1, dto.getBlackHoleChance());
        }

        @Test
        @DisplayName("cometChance getter and setter should work")
        void cometChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setCometChance(0.3);
            assertEquals(0.3, dto.getCometChance());
        }

        @Test
        @DisplayName("spaceStationChance getter and setter should work")
        void spaceStationChance_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setSpaceStationChance(0.15);
            assertEquals(0.15, dto.getSpaceStationChance());
        }

        @Test
        @DisplayName("createdAt getter and setter should work")
        void createdAt_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0, 0);
            dto.setCreatedAt(now);
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("description getter and setter should work")
        void description_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setDescription("Test description");
            assertEquals("Test description", dto.getDescription());
        }

        @Test
        @DisplayName("gameType getter and setter should work")
        void gameType_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setGameType("CaptainsLog");
            assertEquals("CaptainsLog", dto.getGameType());
        }

        @Test
        @DisplayName("difficultyLevel getter and setter should work")
        void difficultyLevel_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setDifficultyLevel("hard");
            assertEquals("hard", dto.getDifficultyLevel());
        }

        @Test
        @DisplayName("theme getter and setter should work")
        void theme_getterAndSetter() {
            GalaxyInfoDto dto = new GalaxyInfoDto();
            dto.setTheme("Mythological");
            assertEquals("Mythological", dto.getTheme());
        }
    }
}

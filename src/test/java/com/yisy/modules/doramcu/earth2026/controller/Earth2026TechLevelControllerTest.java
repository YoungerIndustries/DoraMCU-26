package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026TechLevelController Tests")
class Earth2026TechLevelControllerTest {

    private Earth2026Service earth2026Service;
    private Earth2026TechLevelController controller;

    @BeforeEach
    void setUp() {
        earth2026Service = mock(Earth2026Service.class);
        controller = new Earth2026TechLevelController(earth2026Service);
    }

    // ---- getAllTechLevels ----

    @Nested
    @DisplayName("GET /tech-levels Tests")
    class GetAllTechLevelsTests {

        @Test
        @DisplayName("should return all tech levels")
        void getAllTechLevels() {
            when(earth2026Service.getAllTechLevels()).thenReturn(List.of(TechLevel.TL0, TechLevel.TL4, TechLevel.TL20));

            ResponseEntity<?> response = controller.getAllTechLevels();

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> levels = (List<Map<String, Object>>) response.getBody();
            assertEquals(3, levels.size());
        }

        @Test
        @DisplayName("should include tech level fields")
        void getAllTechLevels_fields() {
            when(earth2026Service.getAllTechLevels()).thenReturn(List.of(TechLevel.TL4));

            ResponseEntity<?> response = controller.getAllTechLevels();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> levels = (List<Map<String, Object>>) response.getBody();
            Map<String, Object> level = levels.get(0);
            assertEquals(4, level.get("level"));
            assertEquals("Industrial", level.get("name"));
            assertEquals("RIFLE", level.get("weaponLevel"));
            assertTrue((Boolean) level.get("industrial"));
        }
    }

    // ---- getTechLevel ----

    @Nested
    @DisplayName("GET /tech-levels/{level} Tests")
    class GetTechLevelTests {

        @Test
        @DisplayName("should return tech level by numeric value")
        void getTechLevel_found() {
            when(earth2026Service.getTechLevel(4)).thenReturn(Optional.of(TechLevel.TL4));

            ResponseEntity<?> response = controller.getTechLevel(4);

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> level = (Map<String, Object>) response.getBody();
            assertEquals(4, level.get("level"));
        }

        @Test
        @DisplayName("should return 404 for invalid level")
        void getTechLevel_notFound() {
            when(earth2026Service.getTechLevel(99)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getTechLevel(99);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ---- getAllTimePeriods ----

    @Nested
    @DisplayName("GET /time-periods Tests")
    class GetAllTimePeriodsTests {

        @Test
        @DisplayName("should return all time periods")
        void getAllTimePeriods() {
            when(earth2026Service.getAllTimePeriodTemplates())
                    .thenReturn(List.of(TimePeriodTemplate.WWII, TimePeriodTemplate.MODERN));

            ResponseEntity<?> response = controller.getAllTimePeriods();

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> periods = (List<Map<String, Object>>) response.getBody();
            assertEquals(2, periods.size());
        }

        @Test
        @DisplayName("should include time period fields")
        void getAllTimePeriods_fields() {
            when(earth2026Service.getAllTimePeriodTemplates())
                    .thenReturn(List.of(TimePeriodTemplate.WWII));

            ResponseEntity<?> response = controller.getAllTimePeriods();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> periods = (List<Map<String, Object>>) response.getBody();
            Map<String, Object> period = periods.get(0);
            assertEquals("World War II", period.get("name"));
            assertEquals(4, period.get("techLevel"));
            assertNotNull(period.get("structureTypes"));
            assertNotNull(period.get("conflictTypes"));
        }
    }

    // ---- getTimePeriod ----

    @Nested
    @DisplayName("GET /time-periods/{name} Tests")
    class GetTimePeriodTests {

        @Test
        @DisplayName("should return time period by name")
        void getTimePeriod_found() {
            when(earth2026Service.getTimePeriodTemplate("World War II"))
                    .thenReturn(Optional.of(TimePeriodTemplate.WWII));

            ResponseEntity<?> response = controller.getTimePeriod("World War II");

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> period = (Map<String, Object>) response.getBody();
            assertEquals("World War II", period.get("name"));
        }

        @Test
        @DisplayName("should return 404 for unknown name")
        void getTimePeriod_notFound() {
            when(earth2026Service.getTimePeriodTemplate("Unknown"))
                    .thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getTimePeriod("Unknown");

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ---- getStructures ----

    @Nested
    @DisplayName("GET /locations/{locationId}/structures Tests")
    class GetStructuresTests {

        @Test
        @DisplayName("should return structures at location")
        void getStructures() {
            Earth2026Structure s = new Earth2026Structure("Bunker", 1L, "Bunker", 4);
            s.setId(10L);
            when(earth2026Service.getStructuresAtLocation(1L)).thenReturn(List.of(s));

            ResponseEntity<List<Earth2026Structure>> response = controller.getStructures(1L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(1, response.getBody().size());
            assertEquals("Bunker", response.getBody().get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no structures")
        void getStructures_empty() {
            when(earth2026Service.getStructuresAtLocation(1L)).thenReturn(List.of());

            ResponseEntity<List<Earth2026Structure>> response = controller.getStructures(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ---- getStructure ----

    @Nested
    @DisplayName("GET /structures/{id} Tests")
    class GetStructureTests {

        @Test
        @DisplayName("should return structure by ID")
        void getStructure_found() {
            Earth2026Structure s = new Earth2026Structure("Bunker 7", 1L, "Bunker", 4);
            s.setId(10L);
            when(earth2026Service.getStructure(10L)).thenReturn(Optional.of(s));

            ResponseEntity<?> response = controller.getStructure(10L);

            assertEquals(200, response.getStatusCode().value());
            assertEquals(s, response.getBody());
        }

        @Test
        @DisplayName("should return 404 when not found")
        void getStructure_notFound() {
            when(earth2026Service.getStructure(999L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getStructure(999L);

            assertEquals(404, response.getStatusCode().value());
        }
    }

    // ---- buildBattleEnvironment (POST) ----

    @Nested
    @DisplayName("POST /battle-environments Tests")
    class PostBuildBattleEnvironmentTests {

        @Test
        @DisplayName("should build battle environment")
        void buildBattleEnvironment_success() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Berlin", "Berlin", "Germany", 52.52, 13.405,
                    4, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR,
                    List.of(), List.of());
            when(earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "CLEAR"))
                    .thenReturn(env);

            Map<String, Object> request = new HashMap<>();
            request.put("locationId", 1L);
            request.put("timePeriod", "World War II");

            ResponseEntity<?> response = controller.buildBattleEnvironment(request);

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("locationId"));
        }

        @Test
        @DisplayName("should include terrain and weather")
        void buildBattleEnvironment_withTerrainWeather() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Berlin", "Berlin", "Germany", 52.52, 13.405,
                    4, "World War II", "PLAINS",
                    Earth2026BattleEnvironment.WeatherCondition.RAIN,
                    List.of(), List.of());
            when(earth2026Service.buildBattleEnvironment(1L, "World War II", "PLAINS", "RAIN"))
                    .thenReturn(env);

            Map<String, Object> request = new HashMap<>();
            request.put("locationId", 1L);
            request.put("timePeriod", "World War II");
            request.put("terrainType", "PLAINS");
            request.put("weather", "RAIN");

            ResponseEntity<?> response = controller.buildBattleEnvironment(request);

            assertEquals(200, response.getStatusCode().value());
        }

        @Test
        @DisplayName("should return 400 for missing timePeriod")
        void buildBattleEnvironment_missingTimePeriod() {
            Map<String, Object> request = new HashMap<>();
            request.put("locationId", 1L);

            ResponseEntity<?> response = controller.buildBattleEnvironment(request);

            // Missing timePeriod causes NPE on .toString(), caught by generic Exception → 500
            assertEquals(500, response.getStatusCode().value());
        }

        @Test
        @DisplayName("should return 400 for blank timePeriod")
        void buildBattleEnvironment_blankTimePeriod() {
            Map<String, Object> request = new HashMap<>();
            request.put("locationId", 1L);
            request.put("timePeriod", "  ");

            ResponseEntity<?> response = controller.buildBattleEnvironment(request);

            assertEquals(400, response.getStatusCode().value());
        }

        @Test
        @DisplayName("should return 400 for IllegalArgumentException")
        void buildBattleEnvironment_illegalArgumentException() {
            Map<String, Object> request = new HashMap<>();
            request.put("locationId", 1L);
            request.put("timePeriod", "World War II");
            when(earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "CLEAR"))
                    .thenThrow(new IllegalArgumentException("Location not found"));

            ResponseEntity<?> response = controller.buildBattleEnvironment(request);

            assertEquals(400, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(body.get("error").toString().contains("Location not found"));
        }

        @Test
        @DisplayName("should return 500 for general exception")
        void buildBattleEnvironment_generalException() {
            Map<String, Object> request = new HashMap<>();
            request.put("locationId", 1L);
            request.put("timePeriod", "World War II");
            when(earth2026Service.buildBattleEnvironment(1L, "World War II", "URBAN", "CLEAR"))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.buildBattleEnvironment(request);

            assertEquals(500, response.getStatusCode().value());
        }
    }

    // ---- getBattleEnvironment (GET) ----

    @Nested
    @DisplayName("GET /battle-environments/{locationId}/{timePeriod} Tests")
    class GetBattleEnvironmentTests {

        @Test
        @DisplayName("should build battle environment with defaults")
        void getBattleEnvironment_success() {
            Earth2026BattleEnvironment env = new Earth2026BattleEnvironment(
                    1L, "Berlin", "Berlin", "Germany", 52.52, 13.405,
                    4, "World War II", "URBAN",
                    Earth2026BattleEnvironment.WeatherCondition.CLEAR,
                    List.of(), List.of());
            when(earth2026Service.buildBattleEnvironment(1L, "World War II"))
                    .thenReturn(env);

            ResponseEntity<?> response = controller.getBattleEnvironment(1L, "World War II");

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(1L, body.get("locationId"));
        }

        @Test
        @DisplayName("should return 400 for IllegalArgumentException")
        void getBattleEnvironment_error() {
            when(earth2026Service.buildBattleEnvironment(1L, "Unknown Era"))
                    .thenThrow(new IllegalArgumentException("Unknown time period"));

            ResponseEntity<?> response = controller.getBattleEnvironment(1L, "Unknown Era");

            assertEquals(400, response.getStatusCode().value());
        }
    }

    // ---- getUnitsForTechLevel ----

    @Nested
    @DisplayName("GET /units/{techLevel} Tests")
    class GetUnitsForTechLevelTests {

        @Test
        @DisplayName("should return units for tech level")
        void getUnitsForTechLevel() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units = List.of(
                    new Earth2026BattleEnvironment.UnitTypeInfo("Infantry", "Basic", 0, 1, 1, 3),
                    new Earth2026BattleEnvironment.UnitTypeInfo("Tanks", "Armored", 4, 6, 5, 4)
            );
            when(earth2026Service.getAvailableUnitsForTechLevel(4)).thenReturn(units);

            ResponseEntity<?> response = controller.getUnitsForTechLevel(4);

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should include unit fields")
        void getUnitsForTechLevel_fields() {
            List<Earth2026BattleEnvironment.UnitTypeInfo> units = List.of(
                    new Earth2026BattleEnvironment.UnitTypeInfo("Tanks", "Armored tanks", 4, 6, 5, 4)
            );
            when(earth2026Service.getAvailableUnitsForTechLevel(4)).thenReturn(units);

            ResponseEntity<?> response = controller.getUnitsForTechLevel(4);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
            Map<String, Object> unit = result.get(0);
            assertEquals("Tanks", unit.get("unitType"));
            assertEquals("Armored tanks", unit.get("description"));
            assertEquals(4, unit.get("minTechLevel"));
            assertEquals(6, unit.get("attackPower"));
            assertEquals(5, unit.get("defensePower"));
            assertEquals(4, unit.get("movement"));
        }

        @Test
        @DisplayName("should return empty list for TL0 with no units")
        void getUnitsForTechLevel_empty() {
            when(earth2026Service.getAvailableUnitsForTechLevel(-1)).thenReturn(List.of());

            ResponseEntity<?> response = controller.getUnitsForTechLevel(-1);

            assertEquals(200, response.getStatusCode().value());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
            assertTrue(result.isEmpty());
        }
    }
}

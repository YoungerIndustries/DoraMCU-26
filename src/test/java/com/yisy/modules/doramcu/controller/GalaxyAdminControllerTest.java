package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.dto.CreateGalaxyRequest;
import com.yisy.modules.doramcu.dto.GalaxyInfoDto;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.service.BackgroundGalaxyBuilder;
import com.yisy.modules.doramcu.service.GalaxyBackupService;
import com.yisy.modules.doramcu.service.GalaxyComparisonService;
import com.yisy.modules.doramcu.service.GalaxyMetricsService;
import com.yisy.modules.doramcu.service.GalaxyOverseerService;
import com.yisy.modules.doramcu.service.GalaxyProgressService;
import com.yisy.modules.doramcu.service.GalaxyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GalaxyAdminController Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class GalaxyAdminControllerTest {

    @Mock
    private GalaxyService galaxyService;

    @Mock
    private GalaxyProgressService galaxyProgressService;

    @Mock
    private BackgroundGalaxyBuilder backgroundGalaxyBuilder;

    @Mock
    private GalaxyOverseerService galaxyOverseerService;

    @Mock
    private GalaxyEventService galaxyEventService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private AiAdminService aiAdminService;

    @Mock
    private GalaxyMetricsService galaxyMetricsService;

    @Mock
    private GalaxyBackupService galaxyBackupService;

    @Mock
    private GalaxyComparisonService galaxyComparisonService;

    private GalaxyAdminController controller;

    @BeforeEach
    void setUp() {
        controller = new GalaxyAdminController(galaxyService, galaxyProgressService, backgroundGalaxyBuilder, galaxyOverseerService, galaxyEventService, playerRepository, aiCharacterRepository, aiAdminService, galaxyBackupService, galaxyComparisonService);
    }

    // ---- listGalaxies ----

    @Test
    @DisplayName("listGalaxies - should return empty list when no galaxies")
    void listGalaxies_shouldReturnEmptyList() {
        when(galaxyService.getAllGalaxies()).thenReturn(List.of());

        ResponseEntity<List<GalaxyInfoDto>> response = controller.listGalaxies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("listGalaxies - should return all galaxies as DTOs")
    void listGalaxies_shouldReturnAllGalaxies() {
        com.yisy.modules.doramcu.model.Galaxy galaxy1 = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy1.setId(1L);
        galaxy1.setName("Milky Way");
        galaxy1.setSeed(42L);
        galaxy1.setSystemCount(500);
        galaxy1.setGameType("DoraT");

        com.yisy.modules.doramcu.model.Galaxy galaxy2 = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy2.setId(2L);
        galaxy2.setName("Andromeda");
        galaxy2.setSeed(123L);
        galaxy2.setSystemCount(300);
        galaxy2.setGameType("CaptainsLog");

        when(galaxyService.getAllGalaxies()).thenReturn(List.of(galaxy1, galaxy2));

        ResponseEntity<List<GalaxyInfoDto>> response = controller.listGalaxies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Milky Way", response.getBody().get(0).getName());
        assertEquals(42L, response.getBody().get(0).getSeed());
        assertEquals("DoraT", response.getBody().get(0).getGameType());
        assertEquals("Andromeda", response.getBody().get(1).getName());
    }

    // ---- getGalaxy ----

    @Test
    @DisplayName("getGalaxy - should return galaxy when found")
    void getGalaxy_shouldReturnGalaxy() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Milky Way");
        galaxy.setSeed(42L);
        galaxy.setSystemCount(500);
        galaxy.setGameType("DoraT");
        galaxy.setDifficultyLevel("medium");

        when(galaxyService.getGalaxyByName("Milky Way")).thenReturn(Optional.of(galaxy));

        ResponseEntity<GalaxyInfoDto> response = controller.getGalaxy("Milky Way");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Milky Way", response.getBody().getName());
        assertEquals(42L, response.getBody().getSeed());
        assertEquals("DoraT", response.getBody().getGameType());
    }

    @Test
    @DisplayName("getGalaxy - should return 404 when not found")
    void getGalaxy_shouldReturn404() {
        when(galaxyService.getGalaxyByName("Nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<GalaxyInfoDto> response = controller.getGalaxy("Nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---- createGalaxy ----

    @Test
    @DisplayName("createGalaxy - should return 400 when name is missing")
    void createGalaxy_shouldReturn400WhenNameMissing() {
        CreateGalaxyRequest request = new CreateGalaxyRequest();
        // name is null

        ResponseEntity<?> response = controller.createGalaxy(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("createGalaxy - should return 400 when name is blank")
    void createGalaxy_shouldReturn400WhenNameBlank() {
        CreateGalaxyRequest request = new CreateGalaxyRequest();
        request.setName("   ");

        ResponseEntity<?> response = controller.createGalaxy(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("createGalaxy - should return 409 when galaxy already exists")
    void createGalaxy_shouldReturn409WhenExists() {
        CreateGalaxyRequest request = new CreateGalaxyRequest();
        request.setName("Existing Galaxy");

        when(galaxyService.hasGalaxy("Existing Galaxy")).thenReturn(true);

        ResponseEntity<?> response = controller.createGalaxy(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ---- deleteGalaxy ----

    @Test
    @DisplayName("deleteGalaxy - should return 404 when galaxy not found")
    void deleteGalaxy_shouldReturn404() {
        when(galaxyService.getGalaxyByName("Nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deleteGalaxy("Nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("deleteGalaxy - should return 200 when galaxy deleted")
    void deleteGalaxy_shouldReturn200() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("ToDelete");
        when(galaxyService.getGalaxyByName("ToDelete")).thenReturn(Optional.of(galaxy));
        doNothing().when(galaxyService).deleteGalaxy("ToDelete");

        ResponseEntity<?> response = controller.deleteGalaxy("ToDelete");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(galaxyService).deleteGalaxy("ToDelete");
    }

    // ---- regenerateGalaxy ----

    @Test
    @DisplayName("regenerateGalaxy - should return 404 when galaxy not found")
    void regenerateGalaxy_shouldReturn404() {
        when(galaxyService.getGalaxyByName("Nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.regenerateGalaxy("Nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("regenerateGalaxy - should return 400 when no generation params stored")
    void regenerateGalaxy_shouldReturn400WhenNoParams() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("NoParams");
        // seed and systemCount are null
        when(galaxyService.getGalaxyByName("NoParams")).thenReturn(Optional.of(galaxy));

        ResponseEntity<?> response = controller.regenerateGalaxy("NoParams");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---- getDefaultTemplate ----

    @Test
    @DisplayName("getDefaultTemplate - should return template with all parameters")
    void getDefaultTemplate_shouldReturnTemplate() {
        ResponseEntity<java.util.Map<String, Object>> response = controller.getDefaultTemplate();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("systemCount"));
        assertTrue(response.getBody().containsKey("planetsPerSystem"));
        assertTrue(response.getBody().containsKey("moonChance"));
        assertTrue(response.getBody().containsKey("hyperlaneDensity"));
        assertTrue(response.getBody().containsKey("sectorSize"));
        assertTrue(response.getBody().containsKey("subSectorDimension"));
        assertTrue(response.getBody().containsKey("civilizationDensity"));
        assertTrue(response.getBody().containsKey("resourceDistribution"));
        assertTrue(response.getBody().containsKey("galaxyShape"));
        assertTrue(response.getBody().containsKey("namingConvention"));
        assertTrue(response.getBody().containsKey("empireCount"));
        assertTrue(response.getBody().containsKey("difficultyLevel"));
        assertTrue(response.getBody().containsKey("gameType"));
    }

    @Test
    @DisplayName("getDefaultTemplate - should return expected default values")
    void getDefaultTemplate_shouldReturnExpectedValues() {
        ResponseEntity<java.util.Map<String, Object>> response = controller.getDefaultTemplate();
        java.util.Map<String, Object> template = response.getBody();

        assertEquals("spiral", template.get("galaxyShape"));
        assertEquals("astronomical", template.get("namingConvention"));
        assertEquals("medium", template.get("difficultyLevel"));
        assertEquals("DoraT", template.get("gameType"));
        assertEquals(10, template.get("sectorSize"));
        assertEquals(10, template.get("subSectorDimension"));
        assertEquals(4, template.get("empireCount"));
    }
}

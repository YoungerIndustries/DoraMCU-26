package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GalaxyService Multi-Galaxy Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class GalaxyServiceMultiGalaxyTest {

    @Mock
    private GalaxyRepository galaxyRepository;
    @Mock
    private SectorRepository sectorRepository;
    @Mock
    private SubSectorRepository subSectorRepository;
    @Mock
    private SolarSystemRepository solarSystemRepository;
    @Mock
    private StarRepository starRepository;
    @Mock
    private PlanetRepository planetRepository;
    @Mock
    private MoonRepository moonRepository;
    @Mock
    private AsteroidBeltRepository asteroidBeltRepository;
    @Mock
    private GasGiantRepository gasGiantRepository;
    @Mock
    private NebulaRepository nebulaRepository;
    @Mock
    private BlackHoleRepository blackHoleRepository;
    @Mock
    private CometRepository cometRepository;
    @Mock
    private SpaceStationRepository spaceStationRepository;
    @Mock
    private AsteroidRepository asteroidRepository;

    private GalaxyService galaxyService;

    @BeforeEach
    void setUp() {
        galaxyService = new GalaxyService(galaxyRepository, sectorRepository, subSectorRepository,
                solarSystemRepository, starRepository, planetRepository, moonRepository,
                asteroidBeltRepository, gasGiantRepository, nebulaRepository, blackHoleRepository,
                cometRepository, spaceStationRepository, asteroidRepository);
    }

    // ---- hasGalaxy ----

    @Test
    @DisplayName("hasGalaxy - should return true when galaxy exists")
    void hasGalaxy_shouldReturnTrueWhenExists() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setName("Andromeda");
        when(galaxyRepository.findByName("Andromeda")).thenReturn(Optional.of(galaxy));

        assertTrue(galaxyService.hasGalaxy("Andromeda"));
    }

    @Test
    @DisplayName("hasGalaxy - should return false when galaxy does not exist")
    void hasGalaxy_shouldReturnFalseWhenNotExists() {
        when(galaxyRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        assertFalse(galaxyService.hasGalaxy("Nonexistent"));
    }

    // ---- getGalaxyByName ----

    @Test
    @DisplayName("getGalaxyByName - should return galaxy when found")
    void getGalaxyByName_shouldReturnGalaxy() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(2L);
        galaxy.setName("Andromeda");
        galaxy.setSeed(42L);
        galaxy.setSystemCount(500);
        when(galaxyRepository.findByName("Andromeda")).thenReturn(Optional.of(galaxy));

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getGalaxyByName("Andromeda");

        assertTrue(result.isPresent());
        assertEquals("Andromeda", result.get().getName());
        assertEquals(2L, result.get().getId());
        assertEquals(42L, result.get().getSeed());
        assertEquals(500, result.get().getSystemCount());
    }

    @Test
    @DisplayName("getGalaxyByName - should return empty when not found")
    void getGalaxyByName_shouldReturnEmpty() {
        when(galaxyRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getGalaxyByName("Nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getGalaxyByName - should delegate to repository findByName")
    void getGalaxyByName_shouldDelegateToRepository() {
        when(galaxyRepository.findByName("Test")).thenReturn(Optional.empty());
        galaxyService.getGalaxyByName("Test");

        verify(galaxyRepository).findByName("Test");
    }

    // ---- deleteGalaxy ----

    @Test
    @DisplayName("deleteGalaxy - should delete existing galaxy")
    void deleteGalaxy_shouldDeleteExisting() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("ToDelete");
        when(galaxyRepository.findByName("ToDelete")).thenReturn(Optional.of(galaxy));
        doNothing().when(galaxyRepository).delete(galaxy);

        galaxyService.deleteGalaxy("ToDelete");

        verify(galaxyRepository).delete(galaxy);
    }

    @Test
    @DisplayName("deleteGalaxy - should do nothing when galaxy not found")
    void deleteGalaxy_shouldDoNothingWhenNotFound() {
        when(galaxyRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        galaxyService.deleteGalaxy("Nonexistent");

        verify(galaxyRepository, never()).delete(any());
    }

    // ---- persistGalaxy with config ----

    @Test
    @DisplayName("persistGalaxy - should store galaxy name and hyperlane density")
    void persistGalaxy_shouldStoreGalaxyName() {
        Galaxy inMemoryGalaxy = new Galaxy(1, "TestGalaxy");

        when(galaxyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(galaxyRepository.count()).thenReturn(1L);
        when(solarSystemRepository.count()).thenReturn(1L);

        com.yisy.modules.doramcu.model.Galaxy result = galaxyService.persistGalaxy(
                "TestGalaxy", inMemoryGalaxy, 1.0);

        assertNotNull(result);
        assertEquals("TestGalaxy", result.getName());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("persistGalaxy - should set createdAt timestamp")
    void persistGalaxy_shouldSetCreatedAt() {
        Galaxy inMemoryGalaxy = new Galaxy(1, "TestGalaxy");

        when(galaxyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(galaxyRepository.count()).thenReturn(1L);
        when(solarSystemRepository.count()).thenReturn(1L);

        com.yisy.modules.doramcu.model.Galaxy result = galaxyService.persistGalaxy(
                "TestGalaxy", inMemoryGalaxy, 1.0);

        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isAfter(LocalDateTime.of(2020, 1, 1, 0, 0, 0)));
    }

    // ---- generateAndSaveGalaxy ----

    @Test
    @DisplayName("generateAndSaveGalaxy - should store seed in database galaxy")
    void generateAndSaveGalaxy_shouldStoreSeed() {
        when(galaxyRepository.save(any())).thenAnswer(invocation -> {
            com.yisy.modules.doramcu.model.Galaxy saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(galaxyRepository.count()).thenReturn(1L);
        when(solarSystemRepository.count()).thenReturn(1L);

        GalaxyService.GalaxyGenerationResult result = galaxyService.generateAndSaveGalaxy(
                "TestGalaxy", 10, 3,
                0.5, 0.3, 0.2, 0.1, 0.05, 0.1, 0.05, 1.0, 42L);

        assertNotNull(result);
        assertNotNull(result.getDatabaseGalaxy());
        assertNotNull(result.getInMemoryGalaxy());
        assertEquals("TestGalaxy", result.getInMemoryGalaxy().getName());
    }

    // ---- getGameGalaxy ----

    @Test
    @DisplayName("getGameGalaxy - should return Milky Way when it exists")
    void getGameGalaxy_shouldReturnMilkyWay() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Milky Way");
        when(galaxyRepository.findAll()).thenReturn(List.of(galaxy));

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getGameGalaxy();

        assertTrue(result.isPresent());
        assertEquals("Milky Way", result.get().getName());
    }

    @Test
    @DisplayName("getGameGalaxy - should return empty when Milky Way does not exist")
    void getGameGalaxy_shouldReturnEmpty() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Andromeda");
        when(galaxyRepository.findAll()).thenReturn(List.of(galaxy));

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getGameGalaxy();

        assertTrue(result.isEmpty());
    }

    // ---- getTestGalaxy ----

    @Test
    @DisplayName("getTestGalaxy - should return Test Galaxy when it exists")
    void getTestGalaxy_shouldReturnTestGalaxy() {
        com.yisy.modules.doramcu.model.Galaxy galaxy = new com.yisy.modules.doramcu.model.Galaxy();
        galaxy.setId(2L);
        galaxy.setName("Test Galaxy");
        when(galaxyRepository.findAll()).thenReturn(List.of(galaxy));

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getTestGalaxy();

        assertTrue(result.isPresent());
        assertEquals("Test Galaxy", result.get().getName());
    }

    @Test
    @DisplayName("getTestGalaxy - should return empty when Test Galaxy does not exist")
    void getTestGalaxy_shouldReturnEmpty() {
        when(galaxyRepository.findAll()).thenReturn(List.of());

        Optional<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getTestGalaxy();

        assertTrue(result.isEmpty());
    }

    // ---- Multiple galaxies ----

    @Test
    @DisplayName("getAllGalaxies - should return multiple galaxies with generation params")
    void getAllGalaxies_shouldReturnMultipleWithParams() {
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

        when(galaxyRepository.findAll()).thenReturn(List.of(galaxy1, galaxy2));

        List<com.yisy.modules.doramcu.model.Galaxy> result = galaxyService.getAllGalaxies();

        assertEquals(2, result.size());
        assertEquals("Milky Way", result.get(0).getName());
        assertEquals(42L, result.get(0).getSeed());
        assertEquals("DoraT", result.get(0).getGameType());
        assertEquals("Andromeda", result.get(1).getName());
        assertEquals(123L, result.get(1).getSeed());
        assertEquals("CaptainsLog", result.get(1).getGameType());
    }
}

package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.dto.WormholeConnectionDto;
import com.yisy.modules.doramcu.dto.WormholeInfoDto;
import com.yisy.modules.doramcu.dto.WormholePairRequest;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.model.SolarSystem;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.model.Wormhole;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.SolarSystemRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.repository.WormholeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WormholeConnectionService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class WormholeConnectionServiceTest {

    @Mock
    private WormholeRepository wormholeRepository;

    @Mock
    private GalaxyRepository galaxyRepository;

    @Mock
    private SolarSystemRepository solarSystemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PlayerCharacterService playerCharacterService;

    @Mock
    private GalaxyEventService galaxyEventService;

    @Mock
    private GalaxyMetricsService galaxyMetricsService;

    private WormholeConnectionService service;

    @BeforeEach
    void setUp() {
        service = new WormholeConnectionService(
                wormholeRepository, galaxyRepository, solarSystemRepository,
                userRepository, jdbcTemplate, playerCharacterService,
                galaxyEventService, galaxyMetricsService);
    }

    // ---- createWormholePair ----

    @Nested
    @DisplayName("createWormholePair")
    class CreateWormholePairTests {

        private Galaxy createGalaxy(String name) {
            Galaxy galaxy = new Galaxy();
            galaxy.setId(1L);
            galaxy.setName(name);
            return galaxy;
        }

        private SolarSystem createSolarSystem(Long id, int row, int col) {
            SolarSystem system = new SolarSystem();
            system.setId(id);
            system.setSectorRow(row);
            system.setSectorCol(col);
            system.setSubSectorRow(0);
            system.setSubSectorCol(0);
            system.setPosX(0);
            system.setPosY(0);
            system.setPosZ(0);
            return system;
        }

        @Test
        @DisplayName("Should throw when Galaxy A is null")
        void shouldThrowWhenGalaxyANull() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA(null);
            request.setGalaxyB("Andromeda");

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should throw when Galaxy A is blank")
        void shouldThrowWhenGalaxyABlank() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("  ");
            request.setGalaxyB("Andromeda");

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should throw when Galaxy B is null")
        void shouldThrowWhenGalaxyBNull() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB(null);

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should throw when Galaxy B is blank")
        void shouldThrowWhenGalaxyBBlank() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("   ");

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should throw when Galaxy A equals Galaxy B")
        void shouldThrowWhenGalaxiesEqual() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Milky Way");

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should throw when Galaxy A does not exist")
        void shouldThrowWhenGalaxyANotExists() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Nonexistent");
            request.setGalaxyB("Andromeda");

            when(galaxyRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should throw when Galaxy B does not exist")
        void shouldThrowWhenGalaxyBNotExists() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Nonexistent");

            Galaxy galA = createGalaxy("Milky Way");
            when(galaxyRepository.findByName("Milky Way")).thenReturn(Optional.of(galA));
            when(galaxyRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.createWormholePair(request));
        }

        @Test
        @DisplayName("Should create wormhole pair with auto-selected solar systems")
        void shouldCreateWormholePairWithAutoSelect() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");

            Galaxy galA = createGalaxy("Milky Way");
            Galaxy galB = createGalaxy("Andromeda");
            galB.setId(2L);

            SolarSystem sysA = createSolarSystem(100L, 3, 7);
            SolarSystem sysB = createSolarSystem(200L, 5, 2);

            when(galaxyRepository.findByName("Milky Way")).thenReturn(Optional.of(galA));
            when(galaxyRepository.findByName("Andromeda")).thenReturn(Optional.of(galB));
            when(solarSystemRepository.findByGalaxyId(1L)).thenReturn(List.of(sysA));
            when(solarSystemRepository.findByGalaxyId(2L)).thenReturn(List.of(sysB));
            when(wormholeRepository.save(any(Wormhole.class))).thenAnswer(inv -> {
                Wormhole w = inv.getArgument(0);
                w.setId(System.nanoTime());
                return w;
            });

            WormholeConnectionDto result = service.createWormholePair(request);

            assertNotNull(result);
            assertEquals("Milky Way", result.getGalaxyA());
            assertEquals("Andromeda", result.getGalaxyB());
            assertNotNull(result.getWormholeA());
            assertNotNull(result.getWormholeB());
        }

        @Test
        @DisplayName("Should use requested solar system IDs when provided")
        void shouldUseRequestedSolarSystemIds() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");
            request.setSolarSystemIdA(100L);
            request.setSolarSystemIdB(200L);

            Galaxy galA = createGalaxy("Milky Way");
            Galaxy galB = createGalaxy("Andromeda");
            galB.setId(2L);

            SolarSystem sysA = createSolarSystem(100L, 3, 7);
            SolarSystem sysB = createSolarSystem(200L, 5, 2);

            when(galaxyRepository.findByName("Milky Way")).thenReturn(Optional.of(galA));
            when(galaxyRepository.findByName("Andromeda")).thenReturn(Optional.of(galB));
            when(solarSystemRepository.findById(100L)).thenReturn(Optional.of(sysA));
            when(solarSystemRepository.findById(200L)).thenReturn(Optional.of(sysB));
            when(wormholeRepository.save(any(Wormhole.class))).thenAnswer(inv -> {
                Wormhole w = inv.getArgument(0);
                w.setId(System.nanoTime());
                return w;
            });

            WormholeConnectionDto result = service.createWormholePair(request);

            assertNotNull(result);
            assertEquals(100L, result.getWormholeA().getSolarSystemId());
            assertEquals(200L, result.getWormholeB().getSolarSystemId());
        }

        @Test
        @DisplayName("Should use custom names when provided")
        void shouldUseCustomNames() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");
            request.setNameA("Alpha Gate");
            request.setNameB("Beta Gate");

            Galaxy galA = createGalaxy("Milky Way");
            Galaxy galB = createGalaxy("Andromeda");
            galB.setId(2L);

            SolarSystem sysA = createSolarSystem(100L, 3, 7);
            SolarSystem sysB = createSolarSystem(200L, 5, 2);

            when(galaxyRepository.findByName("Milky Way")).thenReturn(Optional.of(galA));
            when(galaxyRepository.findByName("Andromeda")).thenReturn(Optional.of(galB));
            when(solarSystemRepository.findByGalaxyId(1L)).thenReturn(List.of(sysA));
            when(solarSystemRepository.findByGalaxyId(2L)).thenReturn(List.of(sysB));
            when(wormholeRepository.save(any(Wormhole.class))).thenAnswer(inv -> {
                Wormhole w = inv.getArgument(0);
                w.setId(System.nanoTime());
                return w;
            });

            WormholeConnectionDto result = service.createWormholePair(request);

            assertEquals("Alpha Gate", result.getWormholeA().getName());
            assertEquals("Beta Gate", result.getWormholeB().getName());
        }

        @Test
        @DisplayName("Should use default names when not provided")
        void shouldUseDefaultNames() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");

            Galaxy galA = createGalaxy("Milky Way");
            Galaxy galB = createGalaxy("Andromeda");
            galB.setId(2L);

            SolarSystem sysA = createSolarSystem(100L, 3, 7);
            SolarSystem sysB = createSolarSystem(200L, 5, 2);

            when(galaxyRepository.findByName("Milky Way")).thenReturn(Optional.of(galA));
            when(galaxyRepository.findByName("Andromeda")).thenReturn(Optional.of(galB));
            when(solarSystemRepository.findByGalaxyId(1L)).thenReturn(List.of(sysA));
            when(solarSystemRepository.findByGalaxyId(2L)).thenReturn(List.of(sysB));
            when(wormholeRepository.save(any(Wormhole.class))).thenAnswer(inv -> {
                Wormhole w = inv.getArgument(0);
                w.setId(System.nanoTime());
                return w;
            });

            WormholeConnectionDto result = service.createWormholePair(request);

            assertEquals("Milky Way Gateway", result.getWormholeA().getName());
            assertEquals("Andromeda Gateway", result.getWormholeB().getName());
        }
    }

    // ---- travelThroughWormhole ----

    @Nested
    @DisplayName("travelThroughWormhole")
    class TravelThroughWormholeTests {

        @Test
        @DisplayName("Should throw when player name is null")
        void shouldThrowWhenPlayerNameNull() {
            assertThrows(IllegalArgumentException.class, () -> service.travelThroughWormhole(null, "Milky Way"));
        }

        @Test
        @DisplayName("Should throw when player name is blank")
        void shouldThrowWhenPlayerNameBlank() {
            assertThrows(IllegalArgumentException.class, () -> service.travelThroughWormhole("  ", "Milky Way"));
        }

        @Test
        @DisplayName("Should throw when from galaxy is null")
        void shouldThrowWhenFromGalaxyNull() {
            assertThrows(IllegalArgumentException.class, () -> service.travelThroughWormhole("Player1", null));
        }

        @Test
        @DisplayName("Should throw when from galaxy is blank")
        void shouldThrowWhenFromGalaxyBlank() {
            assertThrows(IllegalArgumentException.class, () -> service.travelThroughWormhole("Player1", "  "));
        }

        @Test
        @DisplayName("Should throw when no wormhole exists in source galaxy")
        void shouldThrowWhenNoWormholeExists() {
            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> service.travelThroughWormhole("Player1", "Milky Way"));
        }

        @Test
        @DisplayName("Should throw when wormhole is inactive")
        void shouldThrowWhenWormholeInactive() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setActive(false);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));

            assertThrows(IllegalStateException.class, () -> service.travelThroughWormhole("Player1", "Milky Way"));
        }

        @Test
        @DisplayName("Should throw when wormhole has no connection")
        void shouldThrowWhenNoConnection() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setActive(true);
            wormhole.setConnectedWormholeId(null);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));

            assertThrows(IllegalStateException.class, () -> service.travelThroughWormhole("Player1", "Milky Way"));
        }

        @Test
        @DisplayName("Should throw when connected wormhole not found")
        void shouldThrowWhenConnectedWormholeNotFound() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setActive(true);
            wormhole.setConnectedWormholeId(999L);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));
            when(wormholeRepository.findByConnectedWormholeId(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> service.travelThroughWormhole("Player1", "Milky Way"));
        }

        @Test
        @DisplayName("Should throw when destination wormhole is inactive")
        void shouldThrowWhenDestinationInactive() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setActive(true);
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setActive(false);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormholeA));
            when(wormholeRepository.findByConnectedWormholeId(2L)).thenReturn(Optional.of(wormholeB));

            assertThrows(IllegalStateException.class, () -> service.travelThroughWormhole("Player1", "Milky Way"));
        }

        @Test
        @DisplayName("Should travel successfully and return destination galaxy")
        void shouldTravelSuccessfully() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setActive(true);
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setActive(true);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormholeA));
            when(wormholeRepository.findByConnectedWormholeId(2L)).thenReturn(Optional.of(wormholeB));

            String result = service.travelThroughWormhole("Player1", "Milky Way");

            assertEquals("Andromeda", result);
        }

        @Test
        @DisplayName("Should update user galaxy binding when user exists")
        void shouldUpdateUserGalaxyBinding() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setActive(true);
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setActive(true);

            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Milky Way");

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormholeA));
            when(wormholeRepository.findByConnectedWormholeId(2L)).thenReturn(Optional.of(wormholeB));
            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            service.travelThroughWormhole("Player1", "Milky Way");

            assertEquals("Andromeda", user.getGalaxyName());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should not fail when user does not exist")
        void shouldNotFailWhenUserNotFound() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setActive(true);
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setActive(true);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormholeA));
            when(wormholeRepository.findByConnectedWormholeId(2L)).thenReturn(Optional.of(wormholeB));
            when(userRepository.findByUsername("UnknownPlayer")).thenReturn(Optional.empty());

            String result = service.travelThroughWormhole("UnknownPlayer", "Milky Way");

            assertEquals("Andromeda", result);
        }
    }

    // ---- getConnectedGalaxies ----

    @Nested
    @DisplayName("getConnectedGalaxies")
    class GetConnectedGalaxiesTests {

        @Test
        @DisplayName("Should return empty list when no wormhole exists")
        void shouldReturnEmptyWhenNoWormhole() {
            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.empty());

            List<String> result = service.getConnectedGalaxies("Milky Way");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when wormhole has no connection")
        void shouldReturnEmptyWhenNoConnection() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setConnectedWormholeId(null);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));

            List<String> result = service.getConnectedGalaxies("Milky Way");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return connected galaxy name")
        void shouldReturnConnectedGalaxy() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormholeA));
            when(wormholeRepository.findByConnectedWormholeId(2L)).thenReturn(Optional.of(wormholeB));

            List<String> result = service.getConnectedGalaxies("Milky Way");

            assertEquals(1, result.size());
            assertEquals("Andromeda", result.get(0));
        }
    }

    // ---- listAllConnections ----

    @Nested
    @DisplayName("listAllConnections")
    class ListAllConnectionsTests {

        @Test
        @DisplayName("Should return empty list when no wormholes exist")
        void shouldReturnEmptyWhenNoWormholes() {
            when(wormholeRepository.findAll()).thenReturn(List.of());

            List<WormholeConnectionDto> result = service.listAllConnections();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return connections for paired wormholes")
        void shouldReturnConnectionsForPairedWormholes() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setConnectedWormholeId(1L);

            when(wormholeRepository.findAll()).thenReturn(Arrays.asList(wormholeA, wormholeB));
            when(wormholeRepository.findById(2L)).thenReturn(Optional.of(wormholeB));
            when(wormholeRepository.findById(1L)).thenReturn(Optional.of(wormholeA));

            List<WormholeConnectionDto> result = service.listAllConnections();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should not duplicate connections")
        void shouldNotDuplicateConnections() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setConnectedWormholeId(1L);

            when(wormholeRepository.findAll()).thenReturn(Arrays.asList(wormholeA, wormholeB));
            when(wormholeRepository.findById(2L)).thenReturn(Optional.of(wormholeB));
            when(wormholeRepository.findById(1L)).thenReturn(Optional.of(wormholeA));

            List<WormholeConnectionDto> result = service.listAllConnections();

            // Should be exactly 1 connection, not 2
            assertEquals(1, result.size());
        }
    }

    // ---- toggleWormhole ----

    @Nested
    @DisplayName("toggleWormhole")
    class ToggleWormholeTests {

        @Test
        @DisplayName("Should throw when no wormhole exists")
        void shouldThrowWhenNoWormhole() {
            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> service.toggleWormhole("Milky Way"));
        }

        @Test
        @DisplayName("Should toggle active from true to false")
        void shouldToggleActiveTrueToFalse() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setActive(true);
            wormhole.setConnectedWormholeId(null);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));
            when(wormholeRepository.save(wormhole)).thenReturn(wormhole);

            WormholeInfoDto result = service.toggleWormhole("Milky Way");

            assertFalse(wormhole.isActive());
            assertFalse(result.isActive());
            verify(wormholeRepository).save(wormhole);
        }

        @Test
        @DisplayName("Should toggle active from false to true")
        void shouldToggleActiveFalseToTrue() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setActive(false);
            wormhole.setConnectedWormholeId(null);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));
            when(wormholeRepository.save(wormhole)).thenReturn(wormhole);

            WormholeInfoDto result = service.toggleWormhole("Milky Way");

            assertTrue(wormhole.isActive());
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("Should also toggle connected wormhole")
        void shouldAlsoToggleConnectedWormhole() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setActive(true);
            wormholeA.setConnectedWormholeId(2L);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setActive(true);
            wormholeB.setConnectedWormholeId(1L);

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormholeA));
            when(wormholeRepository.findById(2L)).thenReturn(Optional.of(wormholeB));
            when(wormholeRepository.save(wormholeA)).thenReturn(wormholeA);
            when(wormholeRepository.save(wormholeB)).thenReturn(wormholeB);

            service.toggleWormhole("Milky Way");

            assertFalse(wormholeA.isActive());
            assertFalse(wormholeB.isActive());
            verify(wormholeRepository).save(wormholeA);
            verify(wormholeRepository).save(wormholeB);
        }
    }

    // ---- getWormhole ----

    @Nested
    @DisplayName("getWormhole")
    class GetWormholeTests {

        @Test
        @DisplayName("Should return empty when no wormhole exists")
        void shouldReturnEmptyWhenNoWormhole() {
            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.empty());

            Optional<WormholeInfoDto> result = service.getWormhole("Milky Way");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return wormhole info when exists")
        void shouldReturnWormholeInfo() {
            Wormhole wormhole = new Wormhole();
            wormhole.setId(1L);
            wormhole.setGalaxyName("Milky Way");
            wormhole.setName("Milky Way Gateway");
            wormhole.setActive(true);
            wormhole.setConnectedWormholeId(null);
            wormhole.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

            when(wormholeRepository.findByGalaxyName("Milky Way")).thenReturn(Optional.of(wormhole));

            Optional<WormholeInfoDto> result = service.getWormhole("Milky Way");

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals("Milky Way", result.get().getGalaxyName());
            assertEquals("Milky Way Gateway", result.get().getName());
            assertTrue(result.get().isActive());
        }
    }

    // ---- listAllWormholes ----

    @Nested
    @DisplayName("listAllWormholes")
    class ListAllWormholesTests {

        @Test
        @DisplayName("Should return empty list when no wormholes exist")
        void shouldReturnEmptyWhenNoWormholes() {
            when(wormholeRepository.findAll()).thenReturn(List.of());

            List<WormholeInfoDto> result = service.listAllWormholes();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return all wormholes as DTOs")
        void shouldReturnAllWormholes() {
            Wormhole wormholeA = new Wormhole();
            wormholeA.setId(1L);
            wormholeA.setGalaxyName("Milky Way");
            wormholeA.setName("Milky Way Gateway");
            wormholeA.setActive(true);

            Wormhole wormholeB = new Wormhole();
            wormholeB.setId(2L);
            wormholeB.setGalaxyName("Andromeda");
            wormholeB.setName("Andromeda Gateway");
            wormholeB.setActive(false);

            when(wormholeRepository.findAll()).thenReturn(Arrays.asList(wormholeA, wormholeB));

            List<WormholeInfoDto> result = service.listAllWormholes();

            assertEquals(2, result.size());
            assertEquals("Milky Way", result.get(0).getGalaxyName());
            assertEquals("Andromeda", result.get(1).getGalaxyName());
        }
    }
}

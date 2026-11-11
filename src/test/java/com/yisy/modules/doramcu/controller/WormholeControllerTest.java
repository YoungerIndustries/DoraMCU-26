package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.WormholeConnectionDto;
import com.yisy.modules.doramcu.dto.WormholeInfoDto;
import com.yisy.modules.doramcu.dto.WormholePairRequest;
import com.yisy.modules.doramcu.service.WormholeConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WormholeController Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class WormholeControllerTest {

    @Mock
    private WormholeConnectionService wormholeConnectionService;

    private WormholeController controller;

    @BeforeEach
    void setUp() {
        controller = new WormholeController(wormholeConnectionService);
    }

    // ---- createWormholePair ----

    @Nested
    @DisplayName("createWormholePair")
    class CreateWormholePairTests {

        @Test
        @DisplayName("Should return 201 CREATED on success")
        void shouldReturnCreatedOnSuccess() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");

            WormholeConnectionDto connectionDto = new WormholeConnectionDto();
            connectionDto.setGalaxyA("Milky Way");
            connectionDto.setGalaxyB("Andromeda");

            when(wormholeConnectionService.createWormholePair(request)).thenReturn(connectionDto);

            ResponseEntity<?> response = controller.createWormholePair(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertInstanceOf(WormholeConnectionDto.class, response.getBody());
            assertEquals("Milky Way", ((WormholeConnectionDto) response.getBody()).getGalaxyA());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST on IllegalArgumentException")
        void shouldReturnBadRequestOnIllegalArgumentException() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA(null);
            request.setGalaxyB("Andromeda");

            when(wormholeConnectionService.createWormholePair(request))
                    .thenThrow(new IllegalArgumentException("Galaxy A name is required"));

            ResponseEntity<?> response = controller.createWormholePair(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertEquals("Galaxy A name is required", ((Map<?, ?>) response.getBody()).get("error"));
        }

        @Test
        @DisplayName("Should return 409 CONFLICT on IllegalStateException")
        void shouldReturnConflictOnIllegalStateException() {
            WormholePairRequest request = new WormholePairRequest();
            request.setGalaxyA("Milky Way");
            request.setGalaxyB("Andromeda");

            when(wormholeConnectionService.createWormholePair(request))
                    .thenThrow(new IllegalStateException("No solar systems found"));

            ResponseEntity<?> response = controller.createWormholePair(request);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertEquals("No solar systems found", ((Map<?, ?>) response.getBody()).get("error"));
        }
    }

    // ---- listWormholes ----

    @Nested
    @DisplayName("listWormholes")
    class ListWormholesTests {

        @Test
        @DisplayName("Should return empty list when no wormholes")
        void shouldReturnEmptyList() {
            when(wormholeConnectionService.listAllWormholes()).thenReturn(List.of());

            ResponseEntity<List<WormholeInfoDto>> response = controller.listWormholes();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return all wormholes")
        void shouldReturnAllWormholes() {
            WormholeInfoDto dto1 = new WormholeInfoDto();
            dto1.setGalaxyName("Milky Way");
            dto1.setName("Milky Way Gateway");

            WormholeInfoDto dto2 = new WormholeInfoDto();
            dto2.setGalaxyName("Andromeda");
            dto2.setName("Andromeda Gateway");

            when(wormholeConnectionService.listAllWormholes()).thenReturn(Arrays.asList(dto1, dto2));

            ResponseEntity<List<WormholeInfoDto>> response = controller.listWormholes();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
            assertEquals("Milky Way", response.getBody().get(0).getGalaxyName());
            assertEquals("Andromeda", response.getBody().get(1).getGalaxyName());
        }
    }

    // ---- getWormhole ----

    @Nested
    @DisplayName("getWormhole")
    class GetWormholeTests {

        @Test
        @DisplayName("Should return 200 OK when wormhole found")
        void shouldReturnOkWhenFound() {
            WormholeInfoDto dto = new WormholeInfoDto();
            dto.setId(1L);
            dto.setGalaxyName("Milky Way");
            dto.setName("Milky Way Gateway");

            when(wormholeConnectionService.getWormhole("Milky Way")).thenReturn(Optional.of(dto));

            ResponseEntity<?> response = controller.getWormhole("Milky Way");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(WormholeInfoDto.class, response.getBody());
            assertEquals("Milky Way", ((WormholeInfoDto) response.getBody()).getGalaxyName());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when wormhole not found")
        void shouldReturnNotFoundWhenNotFound() {
            when(wormholeConnectionService.getWormhole("Nonexistent")).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getWormhole("Nonexistent");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertNotNull(((Map<?, ?>) response.getBody()).get("error"));
        }
    }

    // ---- toggleWormhole ----

    @Nested
    @DisplayName("toggleWormhole")
    class ToggleWormholeTests {

        @Test
        @DisplayName("Should return 200 OK with updated wormhole")
        void shouldReturnOkWithUpdatedWormhole() {
            WormholeInfoDto dto = new WormholeInfoDto();
            dto.setId(1L);
            dto.setGalaxyName("Milky Way");
            dto.setActive(false);

            when(wormholeConnectionService.toggleWormhole("Milky Way")).thenReturn(dto);

            ResponseEntity<?> response = controller.toggleWormhole("Milky Way");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(WormholeInfoDto.class, response.getBody());
            assertFalse(((WormholeInfoDto) response.getBody()).isActive());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when wormhole not found")
        void shouldReturnNotFoundWhenNotFound() {
            when(wormholeConnectionService.toggleWormhole("Nonexistent"))
                    .thenThrow(new IllegalStateException("No wormhole found in galaxy 'Nonexistent'"));

            ResponseEntity<?> response = controller.toggleWormhole("Nonexistent");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertNotNull(((Map<?, ?>) response.getBody()).get("error"));
        }
    }

    // ---- listConnections ----

    @Nested
    @DisplayName("listConnections")
    class ListConnectionsTests {

        @Test
        @DisplayName("Should return empty list when no connections")
        void shouldReturnEmptyList() {
            when(wormholeConnectionService.listAllConnections()).thenReturn(List.of());

            ResponseEntity<List<WormholeConnectionDto>> response = controller.listConnections();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        @DisplayName("Should return all connections")
        void shouldReturnAllConnections() {
            WormholeConnectionDto conn = new WormholeConnectionDto();
            conn.setGalaxyA("Milky Way");
            conn.setGalaxyB("Andromeda");

            when(wormholeConnectionService.listAllConnections()).thenReturn(List.of(conn));

            ResponseEntity<List<WormholeConnectionDto>> response = controller.listConnections();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertEquals("Milky Way", response.getBody().get(0).getGalaxyA());
            assertEquals("Andromeda", response.getBody().get(0).getGalaxyB());
        }
    }
}

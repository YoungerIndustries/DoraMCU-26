package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.dto.WormholeTravelRequest;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WormholeTravelController Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class WormholeTravelControllerTest {

    @Mock
    private WormholeConnectionService wormholeConnectionService;

    @Mock
    private UserRepository userRepository;

    private WormholeTravelController controller;

    @BeforeEach
    void setUp() {
        controller = new WormholeTravelController(wormholeConnectionService, userRepository);
    }

    // ---- travel ----

    @Nested
    @DisplayName("travel")
    class TravelTests {

        @Test
        @DisplayName("Should return 400 when player name is null")
        void shouldReturnBadRequestWhenPlayerNameNull() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName(null);

            ResponseEntity<?> response = controller.travel(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertEquals("Player name is required", ((Map<?, ?>) response.getBody()).get("error"));
        }

        @Test
        @DisplayName("Should return 400 when player name is blank")
        void shouldReturnBadRequestWhenPlayerNameBlank() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("  ");

            ResponseEntity<?> response = controller.travel(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertEquals("Player name is required", ((Map<?, ?>) response.getBody()).get("error"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when player not found")
        void shouldThrowWhenPlayerNotFound() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("UnknownPlayer");

            when(userRepository.findByUsername("UnknownPlayer")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> controller.travel(request));
        }

        @Test
        @DisplayName("Should return 200 OK on successful travel")
        void shouldReturnOkOnSuccessfulTravel() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("Player1");

            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Milky Way");

            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(wormholeConnectionService.travelThroughWormhole("Player1", "Milky Way"))
                    .thenReturn("Andromeda");

            ResponseEntity<?> response = controller.travel(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(com.yisy.modules.doramcu.dto.WormholeTravelResponse.class, response.getBody());
            com.yisy.modules.doramcu.dto.WormholeTravelResponse body =
                    (com.yisy.modules.doramcu.dto.WormholeTravelResponse) response.getBody();
            assertEquals("Player1", body.getPlayerName());
            assertEquals("Milky Way", body.getFromGalaxy());
            assertEquals("Andromeda", body.getToGalaxy());
            assertFalse(body.isConverted());
        }

        @Test
        @DisplayName("Should return 400 when travel fails with IllegalStateException")
        void shouldReturnBadRequestWhenTravelFails() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("Player1");

            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Milky Way");

            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(wormholeConnectionService.travelThroughWormhole("Player1", "Milky Way"))
                    .thenThrow(new IllegalStateException("No wormhole found"));

            ResponseEntity<?> response = controller.travel(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            assertEquals("No wormhole found", ((Map<?, ?>) response.getBody()).get("error"));
        }

        @Test
        @DisplayName("Should call service with correct parameters")
        void shouldCallServiceWithCorrectParameters() {
            WormholeTravelRequest request = new WormholeTravelRequest();
            request.setPlayerName("Player1");

            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Test Galaxy");

            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(wormholeConnectionService.travelThroughWormhole("Player1", "Test Galaxy"))
                    .thenReturn("Andromeda");

            controller.travel(request);

            verify(wormholeConnectionService).travelThroughWormhole("Player1", "Test Galaxy");
        }
    }

    // ---- getConnectedGalaxies ----

    @Nested
    @DisplayName("getConnectedGalaxies")
    class GetConnectedGalaxiesTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when player not found")
        void shouldThrowWhenPlayerNotFound() {
            when(userRepository.findByUsername("UnknownPlayer")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> controller.getConnectedGalaxies("UnknownPlayer"));
        }

        @Test
        @DisplayName("Should return connected galaxies for player")
        void shouldReturnConnectedGalaxies() {
            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Milky Way");

            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(wormholeConnectionService.getConnectedGalaxies("Milky Way"))
                    .thenReturn(List.of("Andromeda", "Triangulum"));

            ResponseEntity<?> response = controller.getConnectedGalaxies("Player1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            Map<?, ?> body = (Map<?, ?>) response.getBody();
            assertEquals("Player1", body.get("playerName"));
            assertEquals("Milky Way", body.get("currentGalaxy"));
            @SuppressWarnings("unchecked")
            List<String> connected = (List<String>) body.get("connectedGalaxies");
            assertEquals(2, connected.size());
            assertEquals("Andromeda", connected.get(0));
            assertEquals("Triangulum", connected.get(1));
        }

        @Test
        @DisplayName("Should return empty connected galaxies when no connections")
        void shouldReturnEmptyConnectedGalaxies() {
            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Milky Way");

            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(wormholeConnectionService.getConnectedGalaxies("Milky Way")).thenReturn(List.of());

            ResponseEntity<?> response = controller.getConnectedGalaxies("Player1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody());
            Map<?, ?> body = (Map<?, ?>) response.getBody();
            @SuppressWarnings("unchecked")
            List<String> connected = (List<String>) body.get("connectedGalaxies");
            assertTrue(connected.isEmpty());
        }

        @Test
        @DisplayName("Should call service with player's galaxy")
        void shouldCallServiceWithPlayerGalaxy() {
            User user = new User();
            user.setUsername("Player1");
            user.setGalaxyName("Test Galaxy");

            when(userRepository.findByUsername("Player1")).thenReturn(Optional.of(user));
            when(wormholeConnectionService.getConnectedGalaxies("Test Galaxy")).thenReturn(List.of());

            controller.getConnectedGalaxies("Player1");

            verify(wormholeConnectionService).getConnectedGalaxies("Test Galaxy");
        }
    }
}

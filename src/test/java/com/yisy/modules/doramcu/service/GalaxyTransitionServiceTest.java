package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.service.Earth2026Service;
import com.yisy.modules.doramcu.gateway.PluginGateway;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.model.CharacterType;
import com.yisy.modules.doramcu.model.PlayerCharacterRegistry;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerCharacterRegistryRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.unified.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GalaxyTransitionService Tests")
class GalaxyTransitionServiceTest {

    private Earth2026Service earth2026Service;
    private PlayerCharacterRegistryRepository registryRepository;
    private PlayerRepository playerRepository;
    private UserRepository userRepository;
    private PluginGateway pluginGateway;
    private GalaxyTransitionService galaxyTransitionService;

    @BeforeEach
    void setUp() {
        earth2026Service = mock(Earth2026Service.class);
        registryRepository = mock(PlayerCharacterRegistryRepository.class);
        playerRepository = mock(PlayerRepository.class);
        userRepository = mock(UserRepository.class);
        pluginGateway = mock(PluginGateway.class);

        galaxyTransitionService = new GalaxyTransitionService(
                earth2026Service, registryRepository, playerRepository, userRepository, pluginGateway
        );
    }

    // ---- transitionToGalaxy ----

    @Nested
    @DisplayName("transitionToGalaxy Tests")
    class TransitionToGalaxyTests {

        @Test
        @DisplayName("should transition character to target galaxy")
        void transitionToGalaxy_shouldTransitionSuccessfully() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("San Diego, CA");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            // Act
            Earth2026Character result = galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            assertNotNull(result);
            assertEquals("Traveler", result.getName());
            assertEquals("Milky Way", result.getTargetGalaxy());
            verify(earth2026Service).completeTransition(1L);
            verify(registryRepository).save(any(PlayerCharacterRegistry.class));
            verify(pluginGateway).loadPlugin(GameType.DORAMCU_4X);
        }

        @Test
        @DisplayName("should create registry entry with correct galaxy name")
        void transitionToGalaxy_shouldSetCorrectGalaxyName() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setTargetGalaxy("The Void");
            character.setEarthLocation("New York, NY");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("The Void")).thenReturn(Optional.of(GameType.TRAVELLER));

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertEquals("The Void", registry.getGalaxyName());
        }

        @Test
        @DisplayName("should create registry entry with DORAMCU_4X character type for Milky Way")
        void transitionToGalaxy_shouldSetCharacterType() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("San Diego, CA");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            // DORAMCU_4X maps to EARTH_2026 in the switch
            assertEquals(CharacterType.EARTH_2026, registry.getCharacterType());
        }

        @Test
        @DisplayName("should create registry entry with character ID and name")
        void transitionToGalaxy_shouldSetCharacterIdAndName() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(42L);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("San Diego, CA");
            when(earth2026Service.getCharacter(42L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(42L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            // Act
            galaxyTransitionService.transitionToGalaxy(42L);

            // Assert
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertEquals(42L, registry.getCharacterId());
            assertEquals("Traveler", registry.getCharacterName());
        }

        @Test
        @DisplayName("should create registry entry with COMMANDER role")
        void transitionToGalaxy_shouldSetCommanderRole() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("San Diego, CA");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertEquals(CharacterRole.COMMANDER, registry.getCharacterRole());
        }

        @Test
        @DisplayName("should set home location from character's Earth location")
        void transitionToGalaxy_shouldSetHomeLocation() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("London, UK");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertEquals("London, UK", registry.getHomeLocationId());
        }

        @Test
        @DisplayName("should throw when character not found")
        void transitionToGalaxy_shouldThrowWhenNotFound() {
            // Arrange
            when(earth2026Service.getCharacter(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> galaxyTransitionService.transitionToGalaxy(999L));
            assertTrue(ex.getMessage().contains("Character not found"));
        }

        @Test
        @DisplayName("should throw when character already transitioned")
        void transitionToGalaxy_shouldThrowWhenAlreadyTransitioned() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Gone", 25, "Test");
            character.setId(1L);
            character.setHasTransitioned(true);
            character.setTargetGalaxy("Milky Way");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> galaxyTransitionService.transitionToGalaxy(1L));
        }

        @Test
        @DisplayName("should throw when no target galaxy selected")
        void transitionToGalaxy_shouldThrowWhenNoTargetGalaxy() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Stuck", 25, "Test");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy(null);
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> galaxyTransitionService.transitionToGalaxy(1L));
        }

        @Test
        @DisplayName("should throw when target galaxy is blank")
        void transitionToGalaxy_shouldThrowWhenTargetGalaxyBlank() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Stuck", 25, "Test");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("   ");
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> galaxyTransitionService.transitionToGalaxy(1L));
        }

        @Test
        @DisplayName("should link User to registry when playerId is set and User exists")
        void transitionToGalaxy_shouldLinkUserWhenPlayerIdSet() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("San Diego, CA");
            character.setPlayerId(42L);
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            User expectedUser = new User();
            expectedUser.setId(42L);
            expectedUser.setUsername("testuser");
            when(userRepository.findById(42L)).thenReturn(Optional.of(expectedUser));

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            verify(userRepository).findById(42L);
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertNotNull(registry.getPlayer());
            assertEquals(42L, registry.getPlayer().getId());
            assertEquals("testuser", registry.getPlayer().getUsername());
        }

        @Test
        @DisplayName("should not link User when playerId is set but User not found")
        void transitionToGalaxy_shouldNotLinkUserWhenNotFound() {
            // Arrange
            Earth2026Character character = new Earth2026Character("Traveler", 25, "Explorer");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("San Diego, CA");
            character.setPlayerId(999L);
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            verify(userRepository).findById(999L);
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertNull(registry.getPlayer());
        }

        @Test
        @DisplayName("should skip User lookup when playerId is null (AI character)")
        void transitionToGalaxy_shouldSkipUserLookupWhenPlayerIdNull() {
            // Arrange
            Earth2026Character character = new Earth2026Character("AI Commander", 30, "General");
            character.setId(1L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("Milky Way");
            character.setEarthLocation("Berlin, Germany");
            character.setPlayerId(null);
            when(earth2026Service.getCharacter(1L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(1L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("Milky Way")).thenReturn(Optional.of(GameType.DORAMCU_4X));

            // Act
            galaxyTransitionService.transitionToGalaxy(1L);

            // Assert
            verify(userRepository, never()).findById(anyLong());
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();
            assertNull(registry.getPlayer());
        }

        @Test
        @DisplayName("should link User and set all registry fields correctly")
        void transitionToGalaxy_shouldLinkUserAndSetAllFields() {
            // Arrange
            Earth2026Character character = new Earth2026Character("John Doe", 28, "Pilot");
            character.setId(10L);
            character.setHasTransitioned(false);
            character.setTargetGalaxy("The Void");
            character.setEarthLocation("Tokyo, Japan");
            character.setPlayerId(5L);
            when(earth2026Service.getCharacter(10L)).thenReturn(Optional.of(character));
            when(earth2026Service.completeTransition(10L)).thenReturn(Optional.of(character));
            when(registryRepository.save(any(PlayerCharacterRegistry.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(pluginGateway.getGameTypeForGalaxy("The Void")).thenReturn(Optional.of(GameType.TRAVELLER));

            User expectedUser = new User();
            expectedUser.setId(5L);
            expectedUser.setUsername("johndoe");
            when(userRepository.findById(5L)).thenReturn(Optional.of(expectedUser));

            // Act
            galaxyTransitionService.transitionToGalaxy(10L);

            // Assert
            ArgumentCaptor<PlayerCharacterRegistry> captor = ArgumentCaptor.forClass(PlayerCharacterRegistry.class);
            verify(registryRepository).save(captor.capture());
            PlayerCharacterRegistry registry = captor.getValue();

            // Verify User link
            assertNotNull(registry.getPlayer());
            assertEquals(5L, registry.getPlayer().getId());

            // Verify all other fields
            assertEquals("The Void", registry.getGalaxyName());
            assertEquals(CharacterType.TRAVELLER, registry.getCharacterType());
            assertEquals(10L, registry.getCharacterId());
            assertEquals("John Doe", registry.getCharacterName());
            assertEquals(CharacterRole.COMMANDER, registry.getCharacterRole());
            assertEquals("Tokyo, Japan", registry.getHomeLocationId());
        }
    }

    // ---- getAvailableGalaxies ----

    @Nested
    @DisplayName("getAvailableGalaxies Tests")
    class GetAvailableGalaxiesTests {

        @Test
        @DisplayName("should return 3 available galaxies")
        void getAvailableGalaxies_shouldReturnThreeGalaxies() {
            // Act
            Map<String, String> galaxies = galaxyTransitionService.getAvailableGalaxies();

            // Assert
            assertEquals(3, galaxies.size());
        }

        @Test
        @DisplayName("should include Milky Way galaxy")
        void getAvailableGalaxies_shouldIncludeMilkyWay() {
            // Act
            Map<String, String> galaxies = galaxyTransitionService.getAvailableGalaxies();

            // Assert
            assertTrue(galaxies.containsKey(GalaxyFactory.GAME_GALAXY_NAME));
            assertTrue(galaxies.get(GalaxyFactory.GAME_GALAXY_NAME).contains("Human"));
            assertTrue(galaxies.get(GalaxyFactory.GAME_GALAXY_NAME).contains("Interstellar Alliance"));
        }

        @Test
        @DisplayName("should include The Void galaxy")
        void getAvailableGalaxies_shouldIncludeTheVoid() {
            // Act
            Map<String, String> galaxies = galaxyTransitionService.getAvailableGalaxies();

            // Assert
            assertTrue(galaxies.containsKey(GalaxyFactory.VOID_GALAXY_NAME));
            assertTrue(galaxies.get(GalaxyFactory.VOID_GALAXY_NAME).contains("Alien"));
        }

        @Test
        @DisplayName("should include Test Galaxy")
        void getAvailableGalaxies_shouldIncludeTestGalaxy() {
            // Act
            Map<String, String> galaxies = galaxyTransitionService.getAvailableGalaxies();

            // Assert
            assertTrue(galaxies.containsKey(GalaxyFactory.TEST_GALAXY_NAME));
            assertTrue(galaxies.get(GalaxyFactory.TEST_GALAXY_NAME).contains("Test"));
        }

        @Test
        @DisplayName("should have descriptions for all galaxies")
        void getAvailableGalaxies_shouldHaveDescriptions() {
            // Act
            Map<String, String> galaxies = galaxyTransitionService.getAvailableGalaxies();

            // Assert
            for (Map.Entry<String, String> entry : galaxies.entrySet()) {
                assertNotNull(entry.getKey(), "Galaxy name should not be null");
                assertNotNull(entry.getValue(), "Galaxy description should not be null");
                assertFalse(entry.getValue().isBlank(),
                        "Galaxy description for '" + entry.getKey() + "' should not be blank");
            }
        }
    }
}

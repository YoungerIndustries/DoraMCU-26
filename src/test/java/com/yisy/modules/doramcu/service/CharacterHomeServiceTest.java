package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CharacterHomeService")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CharacterHomeServiceTest {

    @Mock
    private UserRepository userRepository;

    private CharacterHomeService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new CharacterHomeService(userRepository);
    }

    @Nested
    @DisplayName("getHomeGalaxy")
    class GetHomeGalaxy {

        @Test
        @DisplayName("returns Milky Way when galaxy name is null")
        void returnsDefaultWhenNull() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getHomeGalaxy(1L);

            assertEquals("Milky Way", result);
        }

        @Test
        @DisplayName("returns Milky Way when galaxy name is blank")
        void returnsDefaultWhenBlank() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("   ");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getHomeGalaxy(1L);

            assertEquals("Milky Way", result);
        }

        @Test
        @DisplayName("returns Milky Way when galaxy name is empty")
        void returnsDefaultWhenEmpty() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getHomeGalaxy(1L);

            assertEquals("Milky Way", result);
        }

        @Test
        @DisplayName("returns set galaxy name")
        void returnsSetGalaxy() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("Andromeda");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getHomeGalaxy(1L);

            assertEquals("Andromeda", result);
        }

        @Test
        @DisplayName("throws when player not found")
        void throwsWhenPlayerNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.getHomeGalaxy(999L));
        }

        @Test
        @DisplayName("returns default Earth 2026 for default user")
        void returnsDefaultForDefaultUser() {
            User user = new User(1L, "testuser", "password");
            // galaxyName defaults to "Earth 2026" in User constructor
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getHomeGalaxy(1L);

            assertEquals("Earth 2026", result);
        }
    }

    @Nested
    @DisplayName("setHomeGalaxy")
    class SetHomeGalaxy {

        @Test
        @DisplayName("sets and saves galaxy name")
        void setsAndSavesGalaxy() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("Milky Way");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            service.setHomeGalaxy(1L, "Andromeda");

            assertEquals("Andromeda", user.getGalaxyName());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws when galaxy name is null")
        void throwsWhenNull() {
            assertThrows(IllegalArgumentException.class, () -> service.setHomeGalaxy(1L, null));
        }

        @Test
        @DisplayName("throws when galaxy name is blank")
        void throwsWhenBlank() {
            assertThrows(IllegalArgumentException.class, () -> service.setHomeGalaxy(1L, "   "));
        }

        @Test
        @DisplayName("throws when galaxy name is empty")
        void throwsWhenEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.setHomeGalaxy(1L, ""));
        }

        @Test
        @DisplayName("throws when player not found")
        void throwsWhenPlayerNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.setHomeGalaxy(999L, "Andromeda"));
        }

        @Test
        @DisplayName("changes from one galaxy to another")
        void changesGalaxy() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("Milky Way");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            service.setHomeGalaxy(1L, "Triangulum");

            assertEquals("Triangulum", user.getGalaxyName());
        }
    }

    @Nested
    @DisplayName("getCurrentGalaxy")
    class GetCurrentGalaxy {

        @Test
        @DisplayName("delegates to getHomeGalaxy")
        void delegatesToGetHomeGalaxy() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("Andromeda");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getCurrentGalaxy(1L);

            assertEquals("Andromeda", result);
        }

        @Test
        @DisplayName("returns default when not set")
        void returnsDefaultWhenNotSet() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            String result = service.getCurrentGalaxy(1L);

            assertEquals("Milky Way", result);
        }
    }

    @Nested
    @DisplayName("isAtHome")
    class IsAtHome {

        @Test
        @DisplayName("always returns true")
        void alwaysReturnsTrue() {
            assertTrue(service.isAtHome(1L));
        }

        @Test
        @DisplayName("returns true for any player")
        void returnsTrueForAnyPlayer() {
            assertTrue(service.isAtHome(999L));
        }
    }

    @Nested
    @DisplayName("getHomeLocation")
    class GetHomeLocation {

        @Test
        @DisplayName("returns home location with galaxy name")
        void returnsHomeLocation() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName("Andromeda");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Optional<CharacterHomeService.HomeLocation> result = service.getHomeLocation(1L);

            assertTrue(result.isPresent());
            assertEquals("Andromeda", result.get().getGalaxyName());
            assertNull(result.get().getSolarSystemId());
        }

        @Test
        @DisplayName("returns default galaxy when not set")
        void returnsDefaultGalaxy() {
            User user = new User(1L, "testuser", "password");
            user.setGalaxyName(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Optional<CharacterHomeService.HomeLocation> result = service.getHomeLocation(1L);

            assertTrue(result.isPresent());
            assertEquals("Milky Way", result.get().getGalaxyName());
        }

        @Test
        @DisplayName("returns empty when player not found")
        void returnsEmptyWhenNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<CharacterHomeService.HomeLocation> result = service.getHomeLocation(999L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("HomeLocation")
    class HomeLocation {

        @Test
        @DisplayName("constructor with galaxy name only")
        void constructorWithGalaxyOnly() {
            CharacterHomeService.HomeLocation loc = new CharacterHomeService.HomeLocation("Andromeda");

            assertEquals("Andromeda", loc.getGalaxyName());
            assertNull(loc.getSolarSystemId());
        }

        @Test
        @DisplayName("constructor with galaxy and solar system")
        void constructorWithGalaxyAndSolarSystem() {
            CharacterHomeService.HomeLocation loc = new CharacterHomeService.HomeLocation("Andromeda", 42L);

            assertEquals("Andromeda", loc.getGalaxyName());
            assertEquals(42L, loc.getSolarSystemId());
        }

        @Test
        @DisplayName("toString includes galaxy and solar system")
        void toStringWithSolarSystem() {
            CharacterHomeService.HomeLocation loc = new CharacterHomeService.HomeLocation("Andromeda", 42L);

            String str = loc.toString();

            assertTrue(str.contains("Andromeda"));
            assertTrue(str.contains("42"));
            assertTrue(str.contains("HomeLocation"));
        }

        @Test
        @DisplayName("toString with null solar system")
        void toStringNullSolarSystem() {
            CharacterHomeService.HomeLocation loc = new CharacterHomeService.HomeLocation("Andromeda");

            String str = loc.toString();

            assertTrue(str.contains("Andromeda"));
            assertTrue(str.contains("null"));
        }

        @Test
        @DisplayName("galaxy name is preserved")
        void galaxyNamePreserved() {
            CharacterHomeService.HomeLocation loc = new CharacterHomeService.HomeLocation("Triangulum");

            assertEquals("Triangulum", loc.getGalaxyName());
        }

        @Test
        @DisplayName("solar system id is preserved")
        void solarSystemIdPreserved() {
            CharacterHomeService.HomeLocation loc = new CharacterHomeService.HomeLocation("Triangulum", 123L);

            assertEquals(123L, loc.getSolarSystemId());
        }
    }
}

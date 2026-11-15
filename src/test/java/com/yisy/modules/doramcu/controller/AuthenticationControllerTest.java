package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.MetricsConfig;
import com.yisy.modules.doramcu.dto.LoginRequest;
import com.yisy.modules.doramcu.dto.LoginResponse;
import com.yisy.modules.doramcu.dto.PinLoginRequest;
import com.yisy.modules.doramcu.dto.PlayerSummaryDto;
import com.yisy.modules.doramcu.dto.RegisterRequest;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.service.JwtService;
import com.yisy.modules.doramcu.service.PlayerService;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private Counter loginSuccessCounter;

    @Mock
    private Counter loginFailureCounter;

    @Mock
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private PlayerService playerService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ObjectProvider<PlayerService> playerServiceProvider;

    @Mock
    private ObjectProvider<PlayerRepository> playerRepositoryProvider;

    @Mock
    private ObjectProvider<GalaxyRepository> galaxyRepositoryProvider;

    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        when(metricsConfig.getLoginSuccessCounter()).thenReturn(loginSuccessCounter);
        when(metricsConfig.getLoginFailureCounter()).thenReturn(loginFailureCounter);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(playerServiceProvider.getIfAvailable()).thenReturn(playerService);
        when(playerRepositoryProvider.getIfAvailable()).thenReturn(playerRepository);
        when(galaxyRepositoryProvider.getIfAvailable()).thenReturn(null);

        controller = new AuthenticationController(authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                playerServiceProvider, playerRepositoryProvider, galaxyRepositoryProvider);
    }

    // ---- Login Success ----

    @Test
    @DisplayName("login - should return 200 OK with token on valid credentials")
    void login_shouldReturnOkWithToken() {
        LoginRequest request = new LoginRequest("admin", "password123");
        String expectedToken = "jwt-token-123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("admin")).thenReturn(expectedToken);

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedToken, response.getBody().getToken());
        assertEquals("admin", response.getBody().getUsername());
        verify(loginSuccessCounter).increment();
    }

    @Test
    @DisplayName("login - should generate token with correct username")
    void login_shouldGenerateTokenWithCorrectUsername() {
        LoginRequest request = new LoginRequest("user1", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("user1")).thenReturn("token-for-user1");

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtService).generateToken("user1");
    }

    // ---- Login Failure ----

    @Test
    @DisplayName("login - should return 400 on invalid credentials")
    void login_shouldReturnBadRequestOnInvalidCredentials() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(loginFailureCounter).increment();
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("login - should not generate token on failed authentication")
    void login_shouldNotGenerateTokenOnFailure() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        controller.login(request);

        verify(jwtService, never()).generateToken(anyString());
        verify(loginFailureCounter).increment();
    }

    // ---- Authentication Token Verification ----

    @Test
    @DisplayName("login - should pass correct credentials to authentication manager")
    void login_shouldPassCorrectCredentials() {
        LoginRequest request = new LoginRequest("testuser", "testpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("testuser")).thenReturn("token");

        controller.login(request);

        verify(authenticationManager).authenticate(argThat(token -> {
            UsernamePasswordAuthenticationToken upToken = (UsernamePasswordAuthenticationToken) token;
            return "testuser".equals(upToken.getPrincipal())
                    && "testpass".equals(upToken.getCredentials());
        }));
    }

    // ---- Edge Cases ----

    @Test
    @DisplayName("login - should handle empty username")
    void login_shouldHandleEmptyUsername() {
        LoginRequest request = new LoginRequest("", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("login - should handle empty password")
    void login_shouldHandleEmptyPassword() {
        LoginRequest request = new LoginRequest("admin", "");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("login - should handle null username")
    void login_shouldHandleNullUsername() {
        LoginRequest request = new LoginRequest(null, "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("login - should handle null password")
    void login_shouldHandleNullPassword() {
        LoginRequest request = new LoginRequest("admin", null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---- Registration Success ----

    @Test
    @DisplayName("register - should return 201 CREATED with token on successful registration")
    void register_shouldReturnCreatedWithToken() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-token", ((LoginResponse) response.getBody()).getToken());
        assertEquals("newplayer", ((LoginResponse) response.getBody()).getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - should encode password before saving")
    void register_shouldEncodePassword() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(passwordEncoder).encode("securepass");
    }

    @Test
    @DisplayName("register - should save user with correct username")
    void register_shouldSaveUserWithCorrectUsername() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(userRepository).save(argThat(user -> "newplayer".equals(user.getUsername())));
    }

    // ---- Registration Failure ----

    @Test
    @DisplayName("register - should return 400 when username already exists")
    void register_shouldReturnBadRequestWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("admin", "password");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        var response = controller.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Username already exists", errorBody.get("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register - should not generate token when username exists")
    void register_shouldNotGenerateTokenWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("admin", "password");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        controller.register(request);

        verify(jwtService, never()).generateToken(anyString());
    }

    // ---- Galaxy List ----

    @Test
    @DisplayName("listGalaxies - should return all predefined galaxies")
    void listGalaxies_shouldReturnAllGalaxies() {
        ResponseEntity<List<String>> response = controller.listGalaxies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Production returns 3 galaxies when galaxyRepository is null: Earth 2026, Milky Way, Test Galaxy
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().contains("Earth 2026"));
        assertTrue(response.getBody().contains("Milky Way"));
        assertTrue(response.getBody().contains("Test Galaxy"));
    }

    // ---- Registration with Galaxy Selection ----

    @Test
    @DisplayName("register - should save user with selected galaxy")
    void register_shouldSaveUserWithSelectedGalaxy() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass", "Test Galaxy");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository).save(argThat(user ->
                "newplayer".equals(user.getUsername()) && "Test Galaxy".equals(user.getGalaxyName())
        ));
    }

    @Test
    @DisplayName("register - should default to Earth 2026 when galaxy is null")
    void register_shouldDefaultToEarth2026WhenGalaxyNull() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(userRepository).save(argThat(user ->
                "Earth 2026".equals(user.getGalaxyName())
        ));
    }

    @Test
    @DisplayName("register - should default to Earth 2026 when galaxy is empty")
    void register_shouldDefaultToEarth2026WhenGalaxyEmpty() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass", "");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        controller.register(request);

        verify(userRepository).save(argThat(user ->
                "Earth 2026".equals(user.getGalaxyName())
        ));
    }

    @Test
    @DisplayName("register - should accept any non-blank galaxy name")
    void register_shouldAcceptAnyNonBlankGalaxy() {
        RegisterRequest request = new RegisterRequest("newplayer", "securepass", "Andromeda");

        when(userRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(jwtService.generateToken("newplayer")).thenReturn("new-token");

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository).save(argThat(user ->
                "Andromeda".equals(user.getGalaxyName())
        ));
    }

    // ---- Server Info & PIN Login Tests ----

    @Test
    @DisplayName("getServerInfo - should return server cluster metadata")
    void getServerInfo_shouldReturnServerClusterMetadata() {
        var response = controller.getServerInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ONLINE", response.getBody().getStatus());
        assertEquals("DoraMCU Galactic Cluster Alpha", response.getBody().getServerName());
        assertNotNull(response.getBody().getActiveGalaxies());
    }

    @Test
    @DisplayName("pinLogin - should return bad request when PIN does not exist")
    void pinLogin_shouldReturnBadRequestOnInvalidPin() {
        PlayerService mockPlayerService = mock(PlayerService.class);
        PlayerRepository mockPlayerRepo = mock(PlayerRepository.class);
        when(mockPlayerService.findByLoginPin("999999")).thenReturn(Optional.empty());

        ObjectProvider<PlayerService> mockPlayerServiceProvider = mock(ObjectProvider.class);
        ObjectProvider<PlayerRepository> mockPlayerRepoProvider = mock(ObjectProvider.class);
        ObjectProvider<GalaxyRepository> mockGalaxyRepoProvider = mock(ObjectProvider.class);
        when(mockPlayerServiceProvider.getIfAvailable()).thenReturn(mockPlayerService);
        when(mockPlayerRepoProvider.getIfAvailable()).thenReturn(mockPlayerRepo);
        when(mockGalaxyRepoProvider.getIfAvailable()).thenReturn(null);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                mockPlayerServiceProvider, mockPlayerRepoProvider, mockGalaxyRepoProvider
        );

        var response = authCtrl.pinLogin(new com.yisy.modules.doramcu.dto.PinLoginRequest("999999"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(loginFailureCounter).increment();
    }

    @Test
    @DisplayName("pinLogin - should return token and player details on valid PIN")
    void pinLogin_shouldReturnTokenOnValidPin() {
        PlayerService mockPlayerService = mock(PlayerService.class);
        PlayerRepository mockPlayerRepo = mock(PlayerRepository.class);

        com.yisy.modules.doramcu.model.Player player = new com.yisy.modules.doramcu.model.Player();
        player.setEmail("commander@empire.com");
        player.setLoginPin("123456");
        player.setEmpireName("Terran Federation");
        player.setGovernmentType(com.yisy.modules.doramcu.model.Player.GovernmentType.DEMOCRACY);
        player.setEthos(com.yisy.modules.doramcu.model.Player.Ethos.MATERIALIST);

        when(mockPlayerService.findByLoginPin("123456")).thenReturn(Optional.of(player));
        when(userRepository.findByUsername("commander@empire.com")).thenReturn(Optional.of(new User(1L, "commander@empire.com", "pass")));
        when(jwtService.generateToken("commander@empire.com")).thenReturn("jwt-pin-token");

        ObjectProvider<PlayerService> mockPlayerServiceProvider = mock(ObjectProvider.class);
        ObjectProvider<PlayerRepository> mockPlayerRepoProvider = mock(ObjectProvider.class);
        ObjectProvider<GalaxyRepository> mockGalaxyRepoProvider = mock(ObjectProvider.class);
        when(mockPlayerServiceProvider.getIfAvailable()).thenReturn(mockPlayerService);
        when(mockPlayerRepoProvider.getIfAvailable()).thenReturn(mockPlayerRepo);
        when(mockGalaxyRepoProvider.getIfAvailable()).thenReturn(null);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                mockPlayerServiceProvider, mockPlayerRepoProvider, mockGalaxyRepoProvider
        );

        var response = authCtrl.pinLogin(new com.yisy.modules.doramcu.dto.PinLoginRequest("123456"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse lr = (LoginResponse) response.getBody();
        assertEquals("jwt-pin-token", lr.getToken());
        assertEquals("commander@empire.com", lr.getUsername());
        assertEquals("123456", lr.getPin());
        assertEquals("Terran Federation", lr.getEmpireName());
        assertEquals("DEMOCRACY", lr.getGovernmentType());
        verify(loginSuccessCounter).increment();
    }

    // ---- Player List Tests ----

    @Test
    @DisplayName("listPlayers - should return empty list when no players exist")
    void listPlayers_shouldReturnEmptyList() {
        when(playerRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<PlayerSummaryDto>> response = controller.listPlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @DisplayName("listPlayers - should return all players sorted by empire name")
    void listPlayers_shouldReturnAllPlayersSorted() {
        User user1 = new User(1L, "player1", "pass");
        User user2 = new User(2L, "player2", "pass");

        Player player1 = new Player();
        player1.setId(1L);
        player1.setEmpireName("Zeta Empire");
        player1.setGovernmentType(Player.GovernmentType.AUTHORITARIAN);
        player1.setEthos(Player.Ethos.SPIRITUALIST);
        player1.setUser(user1);
        player1.setAdmin(false);

        Player player2 = new Player();
        player2.setId(2L);
        player2.setEmpireName("Alpha Federation");
        player2.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player2.setEthos(Player.Ethos.MATERIALIST);
        player2.setUser(user2);
        player2.setAdmin(true);

        when(playerRepository.findAll()).thenReturn(List.of(player1, player2));

        ResponseEntity<List<PlayerSummaryDto>> response = controller.listPlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        // Should be sorted alphabetically by empire name
        assertEquals("Alpha Federation", response.getBody().get(0).empireName());
        assertEquals("Zeta Empire", response.getBody().get(1).empireName());
    }

    @Test
    @DisplayName("listPlayers - should include admin flag in player summary")
    void listPlayers_shouldIncludeAdminFlag() {
        User user = new User(1L, "admin_player", "pass");

        Player player = new Player();
        player.setId(1L);
        player.setEmpireName("Admin Empire");
        player.setGovernmentType(Player.GovernmentType.MONARCHY);
        player.setEthos(Player.Ethos.MATERIALIST);
        player.setUser(user);
        player.setAdmin(true);

        when(playerRepository.findAll()).thenReturn(List.of(player));

        ResponseEntity<List<PlayerSummaryDto>> response = controller.listPlayers();

        assertEquals(1, response.getBody().size());
        PlayerSummaryDto dto = response.getBody().get(0);
        assertTrue(dto.isAdmin());
        assertEquals("MONARCHY", dto.governmentType());
        assertEquals("admin_player", dto.commanderName());
    }

    @Test
    @DisplayName("listPlayers - should return empty list when playerRepository is null")
    void listPlayers_shouldReturnEmptyWhenRepositoryNull() {
        ObjectProvider<PlayerRepository> nullProvider = mock(ObjectProvider.class);
        when(nullProvider.getIfAvailable()).thenReturn(null);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                playerServiceProvider, nullProvider, galaxyRepositoryProvider
        );

        ResponseEntity<List<PlayerSummaryDto>> response = authCtrl.listPlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @DisplayName("listPlayers - should use commander name from user when available")
    void listPlayers_shouldUseUserCommanderName() {
        User user = new User(1L, "commander_x", "pass");

        Player player = new Player();
        player.setId(1L);
        player.setEmail("player@example.com");
        player.setEmpireName("Commander Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
        player.setUser(user);
        player.setAdmin(false);

        when(playerRepository.findAll()).thenReturn(List.of(player));

        ResponseEntity<List<PlayerSummaryDto>> response = controller.listPlayers();

        assertEquals(1, response.getBody().size());
        assertEquals("commander_x", response.getBody().get(0).commanderName());
    }

    @Test
    @DisplayName("listPlayers - should use email as commander name when user is null")
    void listPlayers_shouldUseEmailWhenUserNull() {
        Player player = new Player();
        player.setId(1L);
        player.setEmail("no_user@example.com");
        player.setEmpireName("No User Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
        player.setUser(null);

        when(playerRepository.findAll()).thenReturn(List.of(player));

        ResponseEntity<List<PlayerSummaryDto>> response = controller.listPlayers();

        assertEquals(1, response.getBody().size());
        assertEquals("no_user@example.com", response.getBody().get(0).commanderName());
    }

    // ---- Player Login by ID Tests ----

    @Test
    @DisplayName("playerLogin - should return token when player ID is valid")
    void playerLogin_shouldReturnTokenOnValidId() {
        PlayerRepository mockRepo = mock(PlayerRepository.class);
        Player player = new Player();
        player.setId(5L);
        player.setEmail("player5@empire.com");
        player.setLoginPin("5555");
        player.setEmpireName("Player Five Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
        player.setAdmin(false);

        User user = new User(1L, "player5@empire.com", "pass");
        player.setUser(user);

        when(mockRepo.findById(5L)).thenReturn(Optional.of(player));
        when(jwtService.generateToken("player5@empire.com")).thenReturn("player5-token");

        ObjectProvider<PlayerRepository> repoProvider = mock(ObjectProvider.class);
        when(repoProvider.getIfAvailable()).thenReturn(mockRepo);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                playerServiceProvider, repoProvider, galaxyRepositoryProvider
        );

        var response = authCtrl.playerLogin(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse lr = (LoginResponse) response.getBody();
        assertEquals("player5-token", lr.getToken());
        assertEquals("player5@empire.com", lr.getUsername());
        assertEquals("Player Five Empire", lr.getEmpireName());
        verify(loginSuccessCounter).increment();
    }

    @Test
    @DisplayName("playerLogin - should return 404 when player ID not found")
    void playerLogin_shouldReturnNotFound() {
        PlayerRepository mockRepo = mock(PlayerRepository.class);
        when(mockRepo.findById(999L)).thenReturn(Optional.empty());

        ObjectProvider<PlayerRepository> repoProvider = mock(ObjectProvider.class);
        when(repoProvider.getIfAvailable()).thenReturn(mockRepo);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                playerServiceProvider, repoProvider, galaxyRepositoryProvider
        );

        var response = authCtrl.playerLogin(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(loginFailureCounter).increment();
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("playerLogin - should return 500 when playerRepository is null")
    void playerLogin_shouldReturnErrorWhenRepoNull() {
        ObjectProvider<PlayerRepository> nullProvider = mock(ObjectProvider.class);
        when(nullProvider.getIfAvailable()).thenReturn(null);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                playerServiceProvider, nullProvider, galaxyRepositoryProvider
        );

        var response = authCtrl.playerLogin(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ---- Login with Player Info Tests ----

    @Test
    @DisplayName("login - should include player empire name when player exists")
    void login_shouldIncludePlayerEmpireName() {
        LoginRequest request = new LoginRequest("player1@doramcu.local", "password123");

        Player player = new Player();
        player.setId(1L);
        player.setEmail("player1@doramcu.local");
        player.setLoginPin("1234");
        player.setEmpireName("Stellar Republic");
        player.setAdmin(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("player1@doramcu.local")).thenReturn("token");
        when(playerRepository.findByEmail("player1@doramcu.local")).thenReturn(Optional.of(player));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Stellar Republic", response.getBody().getEmpireName());
        assertEquals("1234", response.getBody().getPin());
        assertFalse(response.getBody().isAdmin());
    }

    @Test
    @DisplayName("login - should include admin flag when player is admin")
    void login_shouldIncludeAdminFlag() {
        LoginRequest request = new LoginRequest("admin@doramcu.local", "adminpass");

        Player player = new Player();
        player.setId(1L);
        player.setEmail("admin@doramcu.local");
        player.setEmpireName("Admin Empire");
        player.setAdmin(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("admin@doramcu.local")).thenReturn("admin-token");
        when(playerRepository.findByEmail("admin@doramcu.local")).thenReturn(Optional.of(player));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertTrue(response.getBody().isAdmin());
    }

    @Test
    @DisplayName("login - should look up player by username@doramcu.local fallback")
    void login_shouldLookupPlayerWithDomainFallback() {
        LoginRequest request = new LoginRequest("player1", "password");

        Player player = new Player();
        player.setId(1L);
        player.setEmail("player1@doramcu.local");
        player.setEmpireName("Fallback Empire");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("player1")).thenReturn("token");
        when(playerRepository.findByEmail("player1")).thenReturn(Optional.empty());
        when(playerRepository.findByEmail("player1@doramcu.local")).thenReturn(Optional.of(player));

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals("Fallback Empire", response.getBody().getEmpireName());
    }

    // ---- PIN Login additional tests ----

    @Test
    @DisplayName("pinLogin - should include admin flag for admin player")
    void pinLogin_shouldIncludeAdminFlag() {
        PlayerService mockPlayerService = mock(PlayerService.class);
        PlayerRepository mockPlayerRepo = mock(PlayerRepository.class);

        Player player = new Player();
        player.setEmail("admin_player@empire.com");
        player.setLoginPin("0001");
        player.setEmpireName("Supreme Empire");
        player.setGovernmentType(Player.GovernmentType.AUTHORITARIAN);
        player.setEthos(Player.Ethos.SPIRITUALIST);
        player.setAdmin(true);

        when(mockPlayerService.findByLoginPin("0001")).thenReturn(Optional.of(player));
        when(userRepository.findByUsername("admin_player@empire.com"))
                .thenReturn(Optional.of(new User(1L, "admin_player@empire.com", "pass")));
        when(jwtService.generateToken("admin_player@empire.com")).thenReturn("admin-pin-token");

        ObjectProvider<PlayerService> mockPlayerServiceProvider = mock(ObjectProvider.class);
        ObjectProvider<PlayerRepository> mockPlayerRepoProvider = mock(ObjectProvider.class);
        ObjectProvider<GalaxyRepository> mockGalaxyRepoProvider = mock(ObjectProvider.class);
        when(mockPlayerServiceProvider.getIfAvailable()).thenReturn(mockPlayerService);
        when(mockPlayerRepoProvider.getIfAvailable()).thenReturn(mockPlayerRepo);
        when(mockGalaxyRepoProvider.getIfAvailable()).thenReturn(null);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                mockPlayerServiceProvider, mockPlayerRepoProvider, mockGalaxyRepoProvider
        );

        var response = authCtrl.pinLogin(new PinLoginRequest("0001"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LoginResponse lr = (LoginResponse) response.getBody();
        assertTrue(lr.isAdmin());
        assertEquals("AUTHORITARIAN", lr.getGovernmentType());
        assertEquals("SPIRITUALIST", lr.getEthos());
    }

    @Test
    @DisplayName("pinLogin - should return error when player service not configured")
    void pinLogin_shouldReturnErrorWhenServiceNotConfigured() {
        ObjectProvider<PlayerService> nullServiceProvider = mock(ObjectProvider.class);
        ObjectProvider<PlayerRepository> nullRepoProvider = mock(ObjectProvider.class);
        when(nullServiceProvider.getIfAvailable()).thenReturn(null);
        when(nullRepoProvider.getIfAvailable()).thenReturn(null);

        AuthenticationController authCtrl = new AuthenticationController(
                authenticationManager, jwtService, metricsConfig, userRepository, passwordEncoder,
                nullServiceProvider, nullRepoProvider, galaxyRepositoryProvider
        );

        var response = authCtrl.pinLogin(new PinLoginRequest("1234"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

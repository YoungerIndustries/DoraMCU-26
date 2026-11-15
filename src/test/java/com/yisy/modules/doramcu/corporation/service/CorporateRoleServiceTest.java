package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.ai.EmpireAiCore;
import com.yisy.modules.doramcu.ai.PlayerCloneService;
import com.yisy.modules.doramcu.model.CorporateRole;
import com.yisy.modules.doramcu.service.AiPlayerService;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorporateRoleService Tests")
class CorporateRoleServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerCloneService playerCloneService;

    @Mock
    private AiPlayerService aiPlayerService;

    @InjectMocks
    private CorporateRoleService corporateRoleService;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setEmail("test@example.com");
        testPlayer.setEmpireName("Test Empire");
        testPlayer.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        testPlayer.setEthos(Player.Ethos.MATERIALIST);
        testPlayer.setCurrentRole(CorporateRole.CEO);
        testPlayer.setAiAssistantId(null);
    }

    @Test
    @DisplayName("switchRole should throw when player not found")
    void switchRole_playerNotFound() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                corporateRoleService.switchRole(999L, CorporateRole.VP));
    }

    @Test
    @DisplayName("switchRole should be no-op when already in target role")
    void switchRole_sameRole() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

        Player result = corporateRoleService.switchRole(1L, CorporateRole.CEO);

        assertEquals(CorporateRole.CEO, result.getCurrentRole());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    @DisplayName("switchRole to VP should create AI assistant and update role")
    void switchRole_toVp() {
        testPlayer.setCurrentRole(CorporateRole.CEO);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        com.yisy.modules.doramcu.hex.Player hexPlayer = new com.yisy.modules.doramcu.hex.Player(1, "test@example.com", "Test Empire");
        when(playerCloneService.createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt()))
                .thenReturn(new EmpireAiCore(1, hexPlayer, 5));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = corporateRoleService.switchRole(1L, CorporateRole.VP);

        assertEquals(CorporateRole.VP, result.getCurrentRole());
        assertNotNull(result.getAiAssistantId());
        verify(playerCloneService).createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt());
        verify(playerRepository, atLeast(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("switchRole to CEO should pause AI assistant and update role")
    void switchRole_toCeo() {
        testPlayer.setCurrentRole(CorporateRole.VP);
        testPlayer.setAiAssistantId(1L);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = corporateRoleService.switchRole(1L, CorporateRole.CEO);

        assertEquals(CorporateRole.CEO, result.getCurrentRole());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("switchRole should handle multiple toggles")
    void switchRole_multipleToggles() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        com.yisy.modules.doramcu.hex.Player hexPlayer2 = new com.yisy.modules.doramcu.hex.Player(1, "test@example.com", "Test Empire");
        when(playerCloneService.createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt()))
                .thenReturn(new EmpireAiCore(1, hexPlayer2, 5));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // CEO -> VP
        Player result1 = corporateRoleService.switchRole(1L, CorporateRole.VP);
        assertEquals(CorporateRole.VP, result1.getCurrentRole());

        // VP -> CEO
        Player result2 = corporateRoleService.switchRole(1L, CorporateRole.CEO);
        assertEquals(CorporateRole.CEO, result2.getCurrentRole());

        // CEO -> VP again
        Player result3 = corporateRoleService.switchRole(1L, CorporateRole.VP);
        assertEquals(CorporateRole.VP, result3.getCurrentRole());
    }

    @Test
    @DisplayName("createAiAssistant should skip if assistant already exists")
    void createAiAssistant_alreadyExists() {
        testPlayer.setAiAssistantId(42L);

        Player result = corporateRoleService.createAiAssistant(testPlayer);

        assertEquals(42L, result.getAiAssistantId());
        verify(playerCloneService, never()).createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt());
    }

    @Test
    @DisplayName("createAiAssistant should create clone when no assistant exists")
    void createAiAssistant_createsClone() {
        testPlayer.setAiAssistantId(null);
        com.yisy.modules.doramcu.hex.Player hexPlayer3 = new com.yisy.modules.doramcu.hex.Player(1, "test@example.com", "Test Empire");
        when(playerCloneService.createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt()))
                .thenReturn(new EmpireAiCore(1, hexPlayer3, 5));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = corporateRoleService.createAiAssistant(testPlayer);

        assertNotNull(result.getAiAssistantId());
        verify(playerCloneService).createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt());
    }

    @Test
    @DisplayName("createAiAssistant should handle clone creation failure gracefully")
    void createAiAssistant_cloneFails() {
        testPlayer.setAiAssistantId(null);
        when(playerCloneService.createClone(anyInt(), any(com.yisy.modules.doramcu.hex.Player.class), anyInt()))
                .thenReturn(null);

        Player result = corporateRoleService.createAiAssistant(testPlayer);

        assertNull(result.getAiAssistantId());
    }

    @Test
    @DisplayName("pauseAiAssistant should do nothing if no assistant exists")
    void pauseAiAssistant_noAssistant() {
        testPlayer.setAiAssistantId(null);

        assertDoesNotThrow(() -> corporateRoleService.pauseAiAssistant(testPlayer));
    }

    @Test
    @DisplayName("pauseAiAssistant should log pause when assistant exists")
    void pauseAiAssistant_withAssistant() {
        testPlayer.setAiAssistantId(42L);

        assertDoesNotThrow(() -> corporateRoleService.pauseAiAssistant(testPlayer));
    }

    @Test
    @DisplayName("resumeAiAssistant should do nothing if no assistant exists")
    void resumeAiAssistant_noAssistant() {
        testPlayer.setAiAssistantId(null);

        assertDoesNotThrow(() -> corporateRoleService.resumeAiAssistant(testPlayer));
    }

    @Test
    @DisplayName("resumeAiAssistant should log resume when assistant exists")
    void resumeAiAssistant_withAssistant() {
        testPlayer.setAiAssistantId(42L);

        assertDoesNotThrow(() -> corporateRoleService.resumeAiAssistant(testPlayer));
    }

    @Test
    @DisplayName("getPlayerRole should return role for existing player")
    void getPlayerRole_exists() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

        Optional<CorporateRole> role = corporateRoleService.getPlayerRole(1L);

        assertTrue(role.isPresent());
        assertEquals(CorporateRole.CEO, role.get());
    }

    @Test
    @DisplayName("getPlayerRole should return empty for non-existent player")
    void getPlayerRole_notFound() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CorporateRole> role = corporateRoleService.getPlayerRole(999L);

        assertTrue(role.isEmpty());
    }

    @Test
    @DisplayName("hasAiAssistant should return true when assistant exists")
    void hasAiAssistant_true() {
        testPlayer.setAiAssistantId(42L);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

        assertTrue(corporateRoleService.hasAiAssistant(1L));
    }

    @Test
    @DisplayName("hasAiAssistant should return false when no assistant")
    void hasAiAssistant_false() {
        testPlayer.setAiAssistantId(null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));

        assertFalse(corporateRoleService.hasAiAssistant(1L));
    }

    @Test
    @DisplayName("hasAiAssistant should return false for non-existent player")
    void hasAiAssistant_notFound() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertFalse(corporateRoleService.hasAiAssistant(999L));
    }
}

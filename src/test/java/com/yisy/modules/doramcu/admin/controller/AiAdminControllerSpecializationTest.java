package com.yisy.modules.doramcu.admin.controller;

import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.service.AiAdminCollaborationService;
import com.yisy.modules.doramcu.admin.service.AiAdminIncidentService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AiAdminIncidentController Specialization Tests")
@ExtendWith(MockitoExtension.class)
class AiAdminControllerSpecializationTest {

    @Mock
    private AiAdminIncidentService incidentService;

    @Mock
    private AiAdminCollaborationService collaborationService;

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiAdminIncidentController controller;

    private Player testPlayer;
    private AiCharacter testAdmin;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setEmail("admin@test.com");
        testPlayer.setAdmin(true);

        testAdmin = new AiCharacter();
        testAdmin.setId(1L);
        testAdmin.setName("Test Admin");
        testAdmin.setAiAdmin(true);
        testAdmin.setActive(true);
        testAdmin.setEmpireName("Test Empire");
        testAdmin.setRole("Galaxy Overseer");
        testAdmin.setActiveOrderCount(0);
        testAdmin.setTotalExperience(500);
        testAdmin.setSpecialization(null);

        authentication = new UsernamePasswordAuthenticationToken("admin@test.com", null);
    }

    @Test
    @DisplayName("setSpecialization: 403 for non-admin")
    void setSpecialization_nonAdmin() {
        testPlayer.setAdmin(false);
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));

        ResponseEntity<?> response = controller.setSpecialization(1L, Map.of("specialization", "HEALTH"), authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("setSpecialization: 404 when admin not found")
    void setSpecialization_notFound() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.setSpecialization(999L, Map.of("specialization", "HEALTH"), authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("setSpecialization: 400 for non-admin character")
    void setSpecialization_notAiAdmin() {
        AiCharacter nonAdmin = new AiCharacter();
        nonAdmin.setId(2L);
        nonAdmin.setAiAdmin(false);

        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(2L)).thenReturn(Optional.of(nonAdmin));

        ResponseEntity<?> response = controller.setSpecialization(2L, Map.of("specialization", "HEALTH"), authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("setSpecialization: 400 for invalid specialization value")
    void setSpecialization_invalidValue() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));

        ResponseEntity<?> response = controller.setSpecialization(1L, Map.of("specialization", "INVALID"), authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("setSpecialization: 200 sets HEALTH specialization")
    void setSpecialization_health() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.setSpecialization(1L, Map.of("specialization", "HEALTH"), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("HEALTH", body.get("specialization"));
        assertEquals("Test Admin", body.get("name"));
        assertEquals(AiAdminIncident.IncidentType.HEALTH, testAdmin.getSpecialization());
    }

    @Test
    @DisplayName("setSpecialization: 200 sets SECURITY specialization")
    void setSpecialization_security() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.setSpecialization(1L, Map.of("specialization", "SECURITY"), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("SECURITY", body.get("specialization"));
    }

    @Test
    @DisplayName("setSpecialization: 200 clears specialization with null body")
    void setSpecialization_clear() {
        testAdmin.setSpecialization(AiAdminIncident.IncidentType.HEALTH);

        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.setSpecialization(1L, null, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNull(body.get("specialization"));
        assertNull(testAdmin.getSpecialization());
    }

    @Test
    @DisplayName("setSpecialization: accepts lowercase specialization")
    void setSpecialization_lowercase() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(aiCharacterRepository.save(any(AiCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.setSpecialization(1L, Map.of("specialization", "performance"), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("PERFORMANCE", body.get("specialization"));
    }

    @Test
    @DisplayName("getSpecializationSummary: 403 for non-admin")
    void getSpecializationSummary_nonAdmin() {
        testPlayer.setAdmin(false);
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));

        ResponseEntity<?> response = controller.getSpecializationSummary(authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("getSpecializationSummary: 200 with empty groups")
    void getSpecializationSummary_empty() {
        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findByAiAdminTrueAndActiveTrue()).thenReturn(List.of());

        ResponseEntity<?> response = controller.getSpecializationSummary(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> body = (Map<String, List<Map<String, Object>>>) response.getBody();
        assertTrue(body.containsKey("HEALTH"));
        assertTrue(body.containsKey("PERFORMANCE"));
        assertTrue(body.containsKey("SECURITY"));
        assertTrue(body.containsKey("SYSTEM"));
        assertTrue(body.containsKey("NONE"));
    }

    @Test
    @DisplayName("getSpecializationSummary: 200 groups admins by specialization")
    void getSpecializationSummary_grouped() {
        AiCharacter healthAdmin = new AiCharacter();
        healthAdmin.setId(1L);
        healthAdmin.setName("Health Admin");
        healthAdmin.setAiAdmin(true);
        healthAdmin.setActive(true);
        healthAdmin.setSpecialization(AiAdminIncident.IncidentType.HEALTH);
        healthAdmin.setActiveOrderCount(1);
        healthAdmin.setTotalExperience(1000);

        AiCharacter noSpecAdmin = new AiCharacter();
        noSpecAdmin.setId(2L);
        noSpecAdmin.setName("General Admin");
        noSpecAdmin.setAiAdmin(true);
        noSpecAdmin.setActive(true);
        noSpecAdmin.setSpecialization(null);
        noSpecAdmin.setActiveOrderCount(0);
        noSpecAdmin.setTotalExperience(500);

        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findByAiAdminTrueAndActiveTrue()).thenReturn(List.of(healthAdmin, noSpecAdmin));

        ResponseEntity<?> response = controller.getSpecializationSummary(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> body = (Map<String, List<Map<String, Object>>>) response.getBody();
        assertEquals(1, body.get("HEALTH").size());
        assertEquals("Health Admin", body.get("HEALTH").getFirst().get("name"));
        assertEquals(1, body.get("NONE").size());
        assertEquals("General Admin", body.get("NONE").getFirst().get("name"));
    }

    @Test
    @DisplayName("getSpecializationSummary: includes workload info")
    void getSpecializationSummary_workload() {
        AiCharacter admin = new AiCharacter();
        admin.setId(1L);
        admin.setName("Test Admin");
        admin.setAiAdmin(true);
        admin.setActive(true);
        admin.setSpecialization(AiAdminIncident.IncidentType.SECURITY);
        admin.setActiveOrderCount(3);
        admin.setTotalExperience(2500);

        when(playerRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testPlayer));
        when(aiCharacterRepository.findByAiAdminTrueAndActiveTrue()).thenReturn(List.of(admin));

        ResponseEntity<?> response = controller.getSpecializationSummary(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> body = (Map<String, List<Map<String, Object>>>) response.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> adminMap = body.get("SECURITY").getFirst();
        assertEquals(3, adminMap.get("activeOrderCount"));
        assertEquals(2500, adminMap.get("totalExperience"));
    }
}

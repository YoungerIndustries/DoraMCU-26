package com.yisy.modules.doramcu.controller.galaxy;

import com.yisy.modules.doramcu.admin.dto.AiAdminVisibilityDTO;
import com.yisy.modules.doramcu.service.ai.AiAdminVisibilityService;
import com.yisy.modules.doramcu.service.galaxy.Galaxy3DService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Galaxy3DControllerVisibilityTest {

    @Mock
    private Galaxy3DService galaxy3DService;

    @Mock
    private AiAdminVisibilityService aiAdminVisibilityService;

    private Galaxy3DController controller;

    private AiAdminVisibilityDTO overseerDTO;
    private AiAdminVisibilityDTO adminDTO;

    @BeforeEach
    void setUp() {
        controller = new Galaxy3DController(galaxy3DService, aiAdminVisibilityService);

        overseerDTO = new AiAdminVisibilityDTO();
        overseerDTO.setId(100L);
        overseerDTO.setName("Alpha Overseer");
        overseerDTO.setRole("Overseer");
        overseerDTO.setEmpireName("Alpha");
        overseerDTO.setGalaxyName("Alpha");
        overseerDTO.setPosX(10.0);
        overseerDTO.setPosY(20.0);
        overseerDTO.setPosZ(30.0);
        overseerDTO.setIsAvailable(true);
        overseerDTO.setStatus("AVAILABLE");
        overseerDTO.setTotalExperience(50);

        adminDTO = new AiAdminVisibilityDTO();
        adminDTO.setId(101L);
        adminDTO.setName("Alpha Admin");
        adminDTO.setRole("Advisor");
        adminDTO.setEmpireName("Alpha");
        adminDTO.setGalaxyName("Alpha");
        adminDTO.setPosX(15.0);
        adminDTO.setPosY(25.0);
        adminDTO.setPosZ(35.0);
        adminDTO.setIsAvailable(false);
        adminDTO.setStatus("BUSY");
        adminDTO.setTotalExperience(30);
    }

    // ------------------------------------------------------------------
    // getAiAdminsNear tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminsNear should return admins within radius")
    void getAiAdminsNear_shouldReturnAdminsWithinRadius() {
        when(aiAdminVisibilityService.getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0))
                .thenReturn(List.of(overseerDTO, adminDTO));

        ResponseEntity<?> response = controller.getAiAdminsNear("Alpha", 10.0, 20.0, 30.0, 50.0, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertEquals(2, admins.size());
        assertEquals("Alpha Overseer", admins.get(0).getName());
    }

    @Test
    @DisplayName("getAiAdminsNear should filter by availability when availableOnly=true")
    void getAiAdminsNear_shouldFilterByAvailability() {
        when(aiAdminVisibilityService.getAiAdminsNearPlayerByAvailability("Alpha", 10.0, 20.0, 30.0, 50.0, true))
                .thenReturn(List.of(overseerDTO));

        ResponseEntity<?> response = controller.getAiAdminsNear("Alpha", 10.0, 20.0, 30.0, 50.0, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertEquals(1, admins.size());
        assertTrue(admins.get(0).getIsAvailable());
    }

    @Test
    @DisplayName("getAiAdminsNear should use default radius")
    void getAiAdminsNear_shouldUseDefaultRadius() {
        when(aiAdminVisibilityService.getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0))
                .thenReturn(List.of(overseerDTO));

        ResponseEntity<?> response = controller.getAiAdminsNear("Alpha", 10.0, 20.0, 30.0, 0, null);

        verify(aiAdminVisibilityService).getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0);
    }

    @Test
    @DisplayName("getAiAdminsNear should return empty list when no admins nearby")
    void getAiAdminsNear_shouldReturnEmptyList() {
        when(aiAdminVisibilityService.getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0))
                .thenReturn(List.of());

        ResponseEntity<?> response = controller.getAiAdminsNear("Alpha", 10.0, 20.0, 30.0, 50.0, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertTrue(admins.isEmpty());
    }

    // ------------------------------------------------------------------
    // getAllAiAdminsInGalaxy tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAllAiAdminsInGalaxy should return all admins in galaxy")
    void getAllAiAdminsInGalaxy_shouldReturnAllAdmins() {
        when(aiAdminVisibilityService.getAllAiAdminsInGalaxy("Alpha"))
                .thenReturn(List.of(overseerDTO, adminDTO));

        ResponseEntity<?> response = controller.getAllAiAdminsInGalaxy("Alpha");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertEquals(2, admins.size());
    }

    @Test
    @DisplayName("getAllAiAdminsInGalaxy should return empty list for empty galaxy")
    void getAllAiAdminsInGalaxy_shouldReturnEmptyList() {
        when(aiAdminVisibilityService.getAllAiAdminsInGalaxy("Empty"))
                .thenReturn(List.of());

        ResponseEntity<?> response = controller.getAllAiAdminsInGalaxy("Empty");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertTrue(admins.isEmpty());
    }

    // ------------------------------------------------------------------
    // getAvailableAiAdminsInGalaxy tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAvailableAiAdminsInGalaxy should return only available admins")
    void getAvailableAiAdminsInGalaxy_shouldReturnOnlyAvailable() {
        when(aiAdminVisibilityService.getAvailableAiAdminsInGalaxy("Alpha"))
                .thenReturn(List.of(overseerDTO));

        ResponseEntity<?> response = controller.getAvailableAiAdminsInGalaxy("Alpha");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertEquals(1, admins.size());
        assertTrue(admins.get(0).getIsAvailable());
    }

    @Test
    @DisplayName("getAvailableAiAdminsInGalaxy should return empty when none available")
    void getAvailableAiAdminsInGalaxy_shouldReturnEmptyWhenNone() {
        when(aiAdminVisibilityService.getAvailableAiAdminsInGalaxy("Alpha"))
                .thenReturn(List.of());

        ResponseEntity<?> response = controller.getAvailableAiAdminsInGalaxy("Alpha");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertTrue(admins.isEmpty());
    }

    // ------------------------------------------------------------------
    // getAiAdminById tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminById should return admin when found")
    void getAiAdminById_shouldReturnAdminWhenFound() {
        when(aiAdminVisibilityService.getAiAdminById(100L))
                .thenReturn(Optional.of(overseerDTO));

        ResponseEntity<?> response = controller.getAiAdminById(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(overseerDTO, response.getBody());
        assertEquals("Alpha Overseer", ((AiAdminVisibilityDTO) response.getBody()).getName());
    }

    @Test
    @DisplayName("getAiAdminById should return 404 when not found")
    void getAiAdminById_shouldReturn404WhenNotFound() {
        when(aiAdminVisibilityService.getAiAdminById(999L))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getAiAdminById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ------------------------------------------------------------------
    // DTO field verification tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminsNear should include all DTO fields")
    void getAiAdminsNear_shouldIncludeAllFields() {
        when(aiAdminVisibilityService.getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0))
                .thenReturn(List.of(overseerDTO));

        ResponseEntity<?> response = controller.getAiAdminsNear("Alpha", 10.0, 20.0, 30.0, 50.0, null);

        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        AiAdminVisibilityDTO dto = admins.get(0);

        assertEquals(100L, dto.getId());
        assertEquals("Alpha Overseer", dto.getName());
        assertEquals("Overseer", dto.getRole());
        assertEquals("Alpha", dto.getEmpireName());
        assertEquals("Alpha", dto.getGalaxyName());
        assertEquals(10.0, dto.getPosX());
        assertEquals(20.0, dto.getPosY());
        assertEquals(30.0, dto.getPosZ());
        assertTrue(dto.getIsAvailable());
        assertEquals("AVAILABLE", dto.getStatus());
        assertEquals(50, dto.getTotalExperience());
    }

    @Test
    @DisplayName("getAiAdminsNear should include distance field")
    void getAiAdminsNear_shouldIncludeDistance() {
        overseerDTO.setDistance(5.5);
        when(aiAdminVisibilityService.getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0))
                .thenReturn(List.of(overseerDTO));

        ResponseEntity<?> response = controller.getAiAdminsNear("Alpha", 10.0, 20.0, 30.0, 50.0, null);

        @SuppressWarnings("unchecked")
        List<AiAdminVisibilityDTO> admins = (List<AiAdminVisibilityDTO>) response.getBody();
        assertEquals(5.5, admins.get(0).getDistance());
    }
}

package com.yisy.modules.doramcu.service.ai;

import com.yisy.modules.doramcu.admin.dto.AiAdminVisibilityDTO;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAdminVisibilityServiceTest {

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    private AiAdminVisibilityService visibilityService;

    private AiCharacter overseer;
    private AiCharacter admin;
    private AiCharacter busyAdmin;

    @BeforeEach
    void setUp() {
        visibilityService = new AiAdminVisibilityService(aiCharacterRepository);

        // Galaxy Overseer with position
        overseer = new AiCharacter("Alpha Overseer", "Overseer", "Alpha");
        overseer.setId(100L);
        overseer.setAiAdmin(true);
        overseer.setActive(true);
        overseer.setGalaxyName("Alpha");
        overseer.setPosX(10.0);
        overseer.setPosY(20.0);
        overseer.setPosZ(30.0);
        overseer.setCurrentOrderId(null);
        overseer.setActiveOrderCount(0);
        overseer.setTotalExperience(50);

        // Idle admin with position
        admin = new AiCharacter("Alpha Admin", "Advisor", "Alpha");
        admin.setId(101L);
        admin.setAiAdmin(true);
        admin.setActive(true);
        admin.setGalaxyName("Alpha");
        admin.setPosX(15.0);
        admin.setPosY(25.0);
        admin.setPosZ(35.0);
        admin.setCurrentOrderId(null);
        admin.setActiveOrderCount(0);
        admin.setTotalExperience(30);

        // Busy admin with position
        busyAdmin = new AiCharacter("Busy Admin", "Advisor", "Alpha");
        busyAdmin.setId(102L);
        busyAdmin.setAiAdmin(true);
        busyAdmin.setActive(true);
        busyAdmin.setGalaxyName("Alpha");
        busyAdmin.setPosX(5.0);
        busyAdmin.setPosY(5.0);
        busyAdmin.setPosZ(5.0);
        busyAdmin.setCurrentOrderId(1L);
        busyAdmin.setActiveOrderCount(2);
        busyAdmin.setTotalExperience(100);
    }

    // ------------------------------------------------------------------
    // getAiAdminsNearPlayer tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminsNearPlayer should return admins within radius")
    void getAiAdminsNearPlayer_shouldReturnAdminsWithinRadius() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer, admin));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0);

        assertEquals(2, result.size());
        assertEquals("Alpha Overseer", result.get(0).getName());
        assertEquals("Alpha Admin", result.get(1).getName());
    }

    @Test
    @DisplayName("getAiAdminsNearPlayer should filter by exact distance")
    void getAiAdminsNearPlayer_shouldFilterByDistance() {
        // overseer is at (10,20,30), player at (10,20,30) — distance 0
        // admin is at (15,25,35), player at (10,20,30) — distance ~8.66
        // radius 5 should only include overseer
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer, admin));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 5.0);

        assertEquals(1, result.size());
        assertEquals("Alpha Overseer", result.get(0).getName());
        assertEquals(0.0, result.get(0).getDistance(), 0.01);
    }

    @Test
    @DisplayName("getAiAdminsNearPlayer should return empty for null galaxy")
    void getAiAdminsNearPlayer_shouldReturnEmptyForNullGalaxy() {
        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer(null, 10.0, 20.0, 30.0, 50.0);

        assertTrue(result.isEmpty());
        verifyNoInteractions(aiCharacterRepository);
    }

    @Test
    @DisplayName("getAiAdminsNearPlayer should return empty for blank galaxy")
    void getAiAdminsNearPlayer_shouldReturnEmptyForBlankGalaxy() {
        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer("   ", 10.0, 20.0, 30.0, 50.0);

        assertTrue(result.isEmpty());
        verifyNoInteractions(aiCharacterRepository);
    }

    @Test
    @DisplayName("getAiAdminsNearPlayer should set availability correctly")
    void getAiAdminsNearPlayer_shouldSetAvailability() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer, busyAdmin));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0);

        assertEquals(2, result.size());
        // overseer has no current order — available
        assertTrue(result.get(0).getIsAvailable());
        assertEquals("AVAILABLE", result.get(0).getStatus());
        // busyAdmin has current order — not available
        assertFalse(result.get(1).getIsAvailable());
        assertEquals("BUSY", result.get(1).getStatus());
    }

    @Test
    @DisplayName("getAiAdminsNearPlayer should include experience and position")
    void getAiAdminsNearPlayer_shouldIncludeExperienceAndPosition() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0);

        assertEquals(1, result.size());
        assertEquals(50, result.get(0).getTotalExperience());
        assertEquals(10.0, result.get(0).getPosX());
        assertEquals(20.0, result.get(0).getPosY());
        assertEquals(30.0, result.get(0).getPosZ());
    }

    // ------------------------------------------------------------------
    // getAiAdminsInSameSpace tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminsInSameSpace should use default radius")
    void getAiAdminsInSameSpace_shouldUseDefaultRadius() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsInSameSpace("Alpha", 10.0, 20.0, 30.0);

        assertEquals(1, result.size());
        assertEquals("Alpha Overseer", result.get(0).getName());
    }

    // ------------------------------------------------------------------
    // getAllAiAdminsInGalaxy tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAllAiAdminsInGalaxy should return all admins in galaxy")
    void getAllAiAdminsInGalaxy_shouldReturnAllAdmins() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPositionNotNull("Alpha"))
                .thenReturn(List.of(overseer, admin, busyAdmin));

        List<AiAdminVisibilityDTO> result = visibilityService.getAllAiAdminsInGalaxy("Alpha");

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("getAllAiAdminsInGalaxy should return empty for null galaxy")
    void getAllAiAdminsInGalaxy_shouldReturnEmptyForNullGalaxy() {
        List<AiAdminVisibilityDTO> result = visibilityService.getAllAiAdminsInGalaxy(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(aiCharacterRepository);
    }

    // ------------------------------------------------------------------
    // getAvailableAiAdminsInGalaxy tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAvailableAiAdminsInGalaxy should return only idle admins")
    void getAvailableAiAdminsInGalaxy_shouldReturnOnlyIdleAdmins() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPositionNotNull("Alpha"))
                .thenReturn(List.of(overseer, admin, busyAdmin));

        List<AiAdminVisibilityDTO> result = visibilityService.getAvailableAiAdminsInGalaxy("Alpha");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(AiAdminVisibilityDTO::getIsAvailable));
    }

    @Test
    @DisplayName("getAvailableAiAdminsInGalaxy should return empty when all busy")
    void getAvailableAiAdminsInGalaxy_shouldReturnEmptyWhenAllBusy() {
        overseer.setCurrentOrderId(1L);
        admin.setCurrentOrderId(2L);
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPositionNotNull("Alpha"))
                .thenReturn(List.of(overseer, admin));

        List<AiAdminVisibilityDTO> result = visibilityService.getAvailableAiAdminsInGalaxy("Alpha");

        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------
    // getAiAdminById tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminById should return admin when found")
    void getAiAdminById_shouldReturnAdminWhenFound() {
        when(aiCharacterRepository.findById(100L)).thenReturn(Optional.of(overseer));

        Optional<AiAdminVisibilityDTO> result = visibilityService.getAiAdminById(100L);

        assertTrue(result.isPresent());
        assertEquals("Alpha Overseer", result.get().getName());
        assertEquals(100L, result.get().getId());
    }

    @Test
    @DisplayName("getAiAdminById should return empty when not found")
    void getAiAdminById_shouldReturnEmptyWhenNotFound() {
        when(aiCharacterRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<AiAdminVisibilityDTO> result = visibilityService.getAiAdminById(999L);

        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------------------------
    // getAiAdminsNearPlayerByAvailability tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAiAdminsNearPlayerByAvailability should filter by available only")
    void getAiAdminsNearPlayerByAvailability_shouldFilterAvailableOnly() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer, busyAdmin));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayerByAvailability("Alpha", 10.0, 20.0, 30.0, 50.0, true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsAvailable());
    }

    @Test
    @DisplayName("getAiAdminsNearPlayerByAvailability should return all when availableOnly=false")
    void getAiAdminsNearPlayerByAvailability_shouldReturnAllWhenFalse() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer, busyAdmin));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayerByAvailability("Alpha", 10.0, 20.0, 30.0, 50.0, false);

        assertEquals(2, result.size());
    }

    // ------------------------------------------------------------------
    // DTO field mapping tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DTO should map all fields correctly")
    void dto_shouldMapAllFields() {
        when(aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndGalaxyNameAndPosXBetweenAndPosYBetweenAndPosZBetween(
                        eq("Alpha"), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(overseer));

        List<AiAdminVisibilityDTO> result = visibilityService
                .getAiAdminsNearPlayer("Alpha", 10.0, 20.0, 30.0, 50.0);

        AiAdminVisibilityDTO dto = result.get(0);
        assertEquals(100L, dto.getId());
        assertEquals("Alpha Overseer", dto.getName());
        assertEquals("Overseer", dto.getRole());
        assertEquals("Alpha", dto.getEmpireName());
        assertEquals("Alpha", dto.getGalaxyName());
        assertEquals(10.0, dto.getPosX());
        assertEquals(20.0, dto.getPosY());
        assertEquals(30.0, dto.getPosZ());
        assertEquals("HUMAN_LEADER", dto.getCharacterType());
        assertNull(dto.getCurrentOrderId());
        assertEquals(0, dto.getActiveOrderCount());
        assertEquals(0.0, dto.getDistance(), 0.01);
        assertTrue(dto.getIsAvailable());
        assertEquals("AVAILABLE", dto.getStatus());
        assertEquals(50, dto.getTotalExperience());
        assertNotNull(dto.getLastSeenAt());
    }
}

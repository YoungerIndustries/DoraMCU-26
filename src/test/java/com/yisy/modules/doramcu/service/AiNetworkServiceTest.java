package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.service.ai.AiEntityRegistrationService;
import com.yisy.modules.doramcu.service.ai.AiNetworkService;
import com.yisy.modules.doramcu.service.ai.AiNetworkService.AiNetworkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiNetworkService Tests")
class AiNetworkServiceTest {

    @Mock
    private AiCharacterRepository aiCharacterRepository;

    @Mock
    private AiEntityRegistrationService entityRegistrationService;

    private AiNetworkService aiNetworkService;

    @BeforeEach
    void setUp() {
        aiNetworkService = new AiNetworkService(aiCharacterRepository, entityRegistrationService);
    }

    // --- Helper methods ---

    private AiCharacter createAiCharacter(AiCharacter.CharacterType type, String empireName, boolean active) {
        AiCharacter character = new AiCharacter();
        character.setId(1L);
        character.setName("Test AI");
        character.setRole("Computer");
        character.setEmpireName(empireName);
        character.setCharacterType(type);
        character.setActive(active);
        return character;
    }

    // --- getNetworkStatus Tests ---

    @Test
    @DisplayName("getNetworkStatus should return correct counts for mixed entity types")
    void getNetworkStatus_shouldReturnCorrectCounts() {
        String empireName = "Federation";

        List<AiCharacter> activeEntities = new ArrayList<>();
        activeEntities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        activeEntities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        activeEntities.add(createAiCharacter(AiCharacter.CharacterType.STATION, empireName, true));
        activeEntities.add(createAiCharacter(AiCharacter.CharacterType.VEHICLE, empireName, true));
        activeEntities.add(createAiCharacter(AiCharacter.CharacterType.FACILITY, empireName, true));

        List<AiCharacter> inactiveEntities = new ArrayList<>();
        inactiveEntities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, false));
        inactiveEntities.add(createAiCharacter(AiCharacter.CharacterType.STATION, empireName, false));

        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveTrue(
                List.of(AiCharacter.CharacterType.SHIP, AiCharacter.CharacterType.STATION,
                        AiCharacter.CharacterType.VEHICLE, AiCharacter.CharacterType.FACILITY),
                empireName)).thenReturn(activeEntities);

        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveFalse(
                List.of(AiCharacter.CharacterType.SHIP, AiCharacter.CharacterType.STATION,
                        AiCharacter.CharacterType.VEHICLE, AiCharacter.CharacterType.FACILITY),
                empireName)).thenReturn(inactiveEntities);

        AiNetworkStatus status = aiNetworkService.getNetworkStatus(empireName);

        assertEquals(empireName, status.getEmpireName());
        assertEquals(5, status.getTotalActiveEntities());
        assertEquals(2, status.getTotalInactiveEntities());
        assertEquals(2, status.getActiveShips());
        assertEquals(1, status.getActiveStations());
        assertEquals(1, status.getActiveVehicles());
        assertEquals(1, status.getActiveFacilities());
        assertEquals(1, status.getInactiveShips());
        assertEquals(1, status.getInactiveStations());
        assertEquals(0, status.getInactiveVehicles());
        assertEquals(0, status.getInactiveFacilities());
        assertEquals(7, status.getTotalEntities());
    }

    @Test
    @DisplayName("getNetworkStatus should return zero counts when no entities exist")
    void getNetworkStatus_shouldReturnZeroCountsWhenEmpty() {
        String empireName = "Empty Empire";

        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveTrue(
                anyList(), eq(empireName))).thenReturn(new ArrayList<>());
        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveFalse(
                anyList(), eq(empireName))).thenReturn(new ArrayList<>());

        AiNetworkStatus status = aiNetworkService.getNetworkStatus(empireName);

        assertEquals(empireName, status.getEmpireName());
        assertEquals(0, status.getTotalActiveEntities());
        assertEquals(0, status.getTotalInactiveEntities());
        assertEquals(0, status.getActiveShips());
        assertEquals(0, status.getActiveStations());
        assertEquals(0, status.getActiveVehicles());
        assertEquals(0, status.getActiveFacilities());
        assertEquals(0, status.getTotalEntities());
    }

    // --- getAllNetworksStatus Tests ---

    @Test
    @DisplayName("getAllNetworksStatus should return status for all empires with entities")
    void getAllNetworksStatus_shouldReturnAllEmpires() {
        List<AiCharacter> allEntityCharacters = new ArrayList<>();
        AiCharacter char1 = createAiCharacter(AiCharacter.CharacterType.SHIP, "Federation", true);
        AiCharacter char2 = createAiCharacter(AiCharacter.CharacterType.STATION, "Empire", true);
        allEntityCharacters.add(char1);
        allEntityCharacters.add(char2);

        when(aiCharacterRepository.findByCharacterTypeIn(
                List.of(AiCharacter.CharacterType.SHIP, AiCharacter.CharacterType.STATION,
                        AiCharacter.CharacterType.VEHICLE, AiCharacter.CharacterType.FACILITY)))
                .thenReturn(allEntityCharacters);

        // Mock getNetworkStatus for each empire
        AiNetworkStatus fedStatus = new AiNetworkStatus("Federation", 1, 0, 1, 0, 0, 0, 0, 0, 0, 0);
        AiNetworkStatus empStatus = new AiNetworkStatus("Empire", 1, 0, 0, 1, 0, 0, 0, 0, 0, 0);

        // Since getNetworkStatus calls the repository again, we need to mock those calls too
        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveTrue(anyList(), eq("Federation")))
                .thenReturn(List.of(char1));
        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveFalse(anyList(), eq("Federation")))
                .thenReturn(new ArrayList<>());
        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveTrue(anyList(), eq("Empire")))
                .thenReturn(List.of(char2));
        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveFalse(anyList(), eq("Empire")))
                .thenReturn(new ArrayList<>());

        List<AiNetworkStatus> statuses = aiNetworkService.getAllNetworksStatus();

        assertEquals(2, statuses.size());
        assertTrue(statuses.stream().anyMatch(s -> "Federation".equals(s.getEmpireName())));
        assertTrue(statuses.stream().anyMatch(s -> "Empire".equals(s.getEmpireName())));
    }

    @Test
    @DisplayName("getAllNetworksStatus should return empty list when no entities exist")
    void getAllNetworksStatus_shouldReturnEmptyWhenNoEntities() {
        when(aiCharacterRepository.findByCharacterTypeIn(anyList())).thenReturn(new ArrayList<>());

        List<AiNetworkStatus> statuses = aiNetworkService.getAllNetworksStatus();

        assertTrue(statuses.isEmpty());
    }

    // --- getActiveNetworkEntities Tests ---

    @Test
    @DisplayName("getActiveNetworkEntities should return active entities from registration service")
    void getActiveNetworkEntities_shouldReturnActiveEntities() {
        String empireName = "Federation";
        List<AiCharacter> expected = List.of(
                createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true),
                createAiCharacter(AiCharacter.CharacterType.STATION, empireName, true)
        );

        when(entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName)).thenReturn(expected);

        List<AiCharacter> result = aiNetworkService.getActiveNetworkEntities(empireName);

        assertEquals(2, result.size());
        verify(entityRegistrationService).findAllEntityAiCharactersByEmpire(empireName);
    }

    // --- getAllNetworkEntities Tests ---

    @Test
    @DisplayName("getAllNetworkEntities should return both active and inactive entities")
    void getAllNetworkEntities_shouldReturnAllEntities() {
        String empireName = "Federation";

        List<AiCharacter> active = List.of(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        List<AiCharacter> inactive = List.of(createAiCharacter(AiCharacter.CharacterType.STATION, empireName, false));

        when(entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName)).thenReturn(active);
        when(aiCharacterRepository.findByCharacterTypeInAndEmpireNameAndActiveFalse(anyList(), eq(empireName)))
                .thenReturn(inactive);

        List<AiCharacter> result = aiNetworkService.getAllNetworkEntities(empireName);

        assertEquals(2, result.size());
    }

    // --- getNetworkTopology Tests ---

    @Test
    @DisplayName("getNetworkTopology should group entities by character type")
    void getNetworkTopology_shouldGroupByType() {
        String empireName = "Federation";

        List<AiCharacter> entities = new ArrayList<>();
        entities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        entities.add(createAiCharacter(AiCharacter.CharacterType.SHIP, empireName, true));
        entities.add(createAiCharacter(AiCharacter.CharacterType.STATION, empireName, true));

        when(entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName)).thenReturn(entities);

        Map<AiCharacter.CharacterType, List<AiCharacter>> topology = aiNetworkService.getNetworkTopology(empireName);

        assertEquals(2, topology.size());
        assertEquals(2, topology.get(AiCharacter.CharacterType.SHIP).size());
        assertEquals(1, topology.get(AiCharacter.CharacterType.STATION).size());
    }

    @Test
    @DisplayName("getNetworkTopology should return empty map when no entities")
    void getNetworkTopology_shouldReturnEmptyWhenNoEntities() {
        String empireName = "Empty";
        when(entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName)).thenReturn(new ArrayList<>());

        Map<AiCharacter.CharacterType, List<AiCharacter>> topology = aiNetworkService.getNetworkTopology(empireName);

        assertTrue(topology.isEmpty());
    }

    // --- Connectivity Tests ---

    @Test
    @DisplayName("isShipConnected should return true for active ship AI")
    void isShipConnected_shouldReturnTrueForActive() {
        AiCharacter character = createAiCharacter(AiCharacter.CharacterType.SHIP, "Federation", true);
        when(entityRegistrationService.findShipAiCharacter(1)).thenReturn(java.util.Optional.of(character));

        assertTrue(aiNetworkService.isShipConnected(1));
    }

    @Test
    @DisplayName("isShipConnected should return false for inactive ship AI")
    void isShipConnected_shouldReturnFalseForInactive() {
        AiCharacter character = createAiCharacter(AiCharacter.CharacterType.SHIP, "Federation", false);
        when(entityRegistrationService.findShipAiCharacter(1)).thenReturn(java.util.Optional.of(character));

        assertFalse(aiNetworkService.isShipConnected(1));
    }

    @Test
    @DisplayName("isShipConnected should return false when no ship AI found")
    void isShipConnected_shouldReturnFalseWhenNotFound() {
        when(entityRegistrationService.findShipAiCharacter(1)).thenReturn(java.util.Optional.empty());

        assertFalse(aiNetworkService.isShipConnected(1));
    }

    @Test
    @DisplayName("isStationConnected should return true for active station AI")
    void isStationConnected_shouldReturnTrueForActive() {
        AiCharacter character = createAiCharacter(AiCharacter.CharacterType.STATION, "Federation", true);
        when(entityRegistrationService.findStationAiCharacter(1)).thenReturn(java.util.Optional.of(character));

        assertTrue(aiNetworkService.isStationConnected(1));
    }

    @Test
    @DisplayName("isStationConnected should return false when no station AI found")
    void isStationConnected_shouldReturnFalseWhenNotFound() {
        when(entityRegistrationService.findStationAiCharacter(1)).thenReturn(java.util.Optional.empty());

        assertFalse(aiNetworkService.isStationConnected(1));
    }

    @Test
    @DisplayName("isVehicleConnected should return true for active vehicle AI")
    void isVehicleConnected_shouldReturnTrueForActive() {
        AiCharacter character = createAiCharacter(AiCharacter.CharacterType.VEHICLE, "Federation", true);
        when(entityRegistrationService.findVehicleAiCharacter("vehicle-1")).thenReturn(java.util.Optional.of(character));

        assertTrue(aiNetworkService.isVehicleConnected("vehicle-1"));
    }

    @Test
    @DisplayName("isVehicleConnected should return false when no vehicle AI found")
    void isVehicleConnected_shouldReturnFalseWhenNotFound() {
        when(entityRegistrationService.findVehicleAiCharacter("vehicle-1")).thenReturn(java.util.Optional.empty());

        assertFalse(aiNetworkService.isVehicleConnected("vehicle-1"));
    }

    @Test
    @DisplayName("isFacilityConnected should return true for active facility AI")
    void isFacilityConnected_shouldReturnTrueForActive() {
        AiCharacter character = createAiCharacter(AiCharacter.CharacterType.FACILITY, "Federation", true);
        when(entityRegistrationService.findFacilityAiCharacter(1)).thenReturn(java.util.Optional.of(character));

        assertTrue(aiNetworkService.isFacilityConnected(1));
    }

    @Test
    @DisplayName("isFacilityConnected should return false when no facility AI found")
    void isFacilityConnected_shouldReturnFalseWhenNotFound() {
        when(entityRegistrationService.findFacilityAiCharacter(1)).thenReturn(java.util.Optional.empty());

        assertFalse(aiNetworkService.isFacilityConnected(1));
    }

    // --- AiNetworkStatus Data Class Tests ---

    @Test
    @DisplayName("AiNetworkStatus getTotalEntities should sum active and inactive")
    void aiNetworkStatus_getTotalEntities_shouldSumActiveAndInactive() {
        AiNetworkStatus status = new AiNetworkStatus("Test", 5, 3, 2, 1, 1, 1, 1, 1, 0, 0);

        assertEquals(8, status.getTotalEntities());
    }

    @Test
    @DisplayName("AiNetworkStatus toString should not throw")
    void aiNetworkStatus_toString_shouldNotThrow() {
        AiNetworkStatus status = new AiNetworkStatus("Test", 5, 3, 2, 1, 1, 1, 1, 1, 0, 0);

        String str = status.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test"));
        assertTrue(str.contains("active=5"));
    }
}

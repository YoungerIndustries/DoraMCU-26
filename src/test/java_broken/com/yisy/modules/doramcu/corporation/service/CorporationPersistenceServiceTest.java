package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporationOfficeState;
import com.yisy.modules.doramcu.corporation.model.CorporationSave;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationOfficeStateRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationSaveRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorporationPersistenceServiceTest {

    @Mock
    private CorporationOfficeStateRepository officeStateRepository;
    @Mock
    private CorporationSaveRepository saveRepository;
    @Mock
    private EveCorporationRepository corporationRepository;
    @Mock
    private CorporateContractRepository contractRepository;
    @Mock
    private CorporateHiringRepository hiringRepository;

    @InjectMocks
    private CorporationPersistenceService persistenceService;

    private CorporationOfficeState officeState;
    private EveCorporation corporation;

    @BeforeEach
    void setUp() {
        officeState = new CorporationOfficeState(1L);
        officeState.setOfficeBuildingLevel(3);

        corporation = new EveCorporation();
        corporation.setId(1L);
        corporation.setName("Test Corp");
        corporation.setCreditBalance(50000L);
    }

    // ---- Office State Tests ----

    @Test
    void saveOfficeState_createsNew() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());
        when(officeStateRepository.save(any(CorporationOfficeState.class))).thenAnswer(inv -> inv.getArgument(0));

        persistenceService.saveOfficeState(1L, 5);

        verify(officeStateRepository).save(argThat(state ->
                state.getCorporationId().equals(1L) && state.getOfficeBuildingLevel() == 5));
    }

    @Test
    void saveOfficeState_updatesExisting() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.of(officeState));
        when(officeStateRepository.save(any(CorporationOfficeState.class))).thenAnswer(inv -> inv.getArgument(0));

        persistenceService.saveOfficeState(1L, 10);

        assertEquals(10, officeState.getOfficeBuildingLevel());
        verify(officeStateRepository).save(officeState);
    }

    @Test
    void loadOfficeState_existing() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.of(officeState));

        CorporationOfficeState result = persistenceService.loadOfficeState(1L);

        assertEquals(1L, result.getCorporationId());
        assertEquals(3, result.getOfficeBuildingLevel());
    }

    @Test
    void loadOfficeState_createsDefault() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());
        when(officeStateRepository.save(any(CorporationOfficeState.class))).thenAnswer(inv -> inv.getArgument(0));

        CorporationOfficeState result = persistenceService.loadOfficeState(1L);

        assertEquals(1L, result.getCorporationId());
        assertEquals(0, result.getOfficeBuildingLevel());
        verify(officeStateRepository).save(any(CorporationOfficeState.class));
    }

    @Test
    void getOfficeBuildingLevel_existing() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.of(officeState));

        int level = persistenceService.getOfficeBuildingLevel(1L);

        assertEquals(3, level);
    }

    @Test
    void getOfficeBuildingLevel_notFound() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());

        int level = persistenceService.getOfficeBuildingLevel(1L);

        assertEquals(0, level);
    }

    @Test
    void incrementOfficeBuildingLevel() {
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.of(officeState));
        when(officeStateRepository.save(any(CorporationOfficeState.class))).thenAnswer(inv -> inv.getArgument(0));

        int newLevel = persistenceService.incrementOfficeBuildingLevel(1L);

        assertEquals(4, newLevel);
        verify(officeStateRepository).save(officeState);
    }

    // ---- Save/Load Tests ----

    @Test
    void createSave_withName() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(contractRepository.findByCorporationIdAndStatus(eq(1L), eq(CorporateContract.Status.ACTIVE)))
                .thenReturn(new ArrayList<>());
        when(hiringRepository.findByCorporationIdAndStatus(eq(1L), eq(CorporateHiring.Status.ACTIVE)))
                .thenReturn(new ArrayList<>());
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());
        when(saveRepository.save(any(CorporationSave.class))).thenAnswer(inv -> inv.getArgument(0));

        CorporationSave save = persistenceService.createSave(1L, "My Save");

        assertEquals(1L, save.getCorporationId());
        assertEquals("My Save", save.getSaveName());
        assertEquals(50000L, save.getCreditBalance());
        assertNotNull(save.getActiveContracts());
        assertNotNull(save.getCrewState());
        assertNotNull(save.getOfficeState());
    }

    @Test
    void createSave_autoGeneratedName() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(contractRepository.findByCorporationIdAndStatus(eq(1L), eq(CorporateContract.Status.ACTIVE)))
                .thenReturn(new ArrayList<>());
        when(hiringRepository.findByCorporationIdAndStatus(eq(1L), eq(CorporateHiring.Status.ACTIVE)))
                .thenReturn(new ArrayList<>());
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());
        when(saveRepository.save(any(CorporationSave.class))).thenAnswer(inv -> inv.getArgument(0));

        CorporationSave save = persistenceService.createSave(1L, null);

        assertTrue(save.getSaveName().startsWith("Save "));
    }

    @Test
    void createSave_noCorporation() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.empty());
        when(contractRepository.findByCorporationIdAndStatus(eq(1L), eq(CorporateContract.Status.ACTIVE)))
                .thenReturn(new ArrayList<>());
        when(hiringRepository.findByCorporationIdAndStatus(eq(1L), eq(CorporateHiring.Status.ACTIVE)))
                .thenReturn(new ArrayList<>());
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());
        when(saveRepository.save(any(CorporationSave.class))).thenAnswer(inv -> inv.getArgument(0));

        CorporationSave save = persistenceService.createSave(1L, "No Corp");

        assertEquals(0L, save.getCreditBalance());
    }

    @Test
    void listSaves() {
        List<CorporationSave> saves = new ArrayList<>();
        saves.add(new CorporationSave(1L, "Save 1"));
        saves.add(new CorporationSave(1L, "Save 2"));
        when(saveRepository.findSavesByCorporationId(1L)).thenReturn(saves);

        List<CorporationSave> result = persistenceService.listSaves(1L);

        assertEquals(2, result.size());
    }

    @Test
    void loadSave_success() {
        CorporationSave save = new CorporationSave(1L, "Test Save");
        save.setId(100L);
        save.setCreditBalance(30000L);
        save.setOfficeState("{\"buildingLevel\":2}");

        when(saveRepository.findById(100L)).thenReturn(Optional.of(save));
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any(EveCorporation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(officeStateRepository.findByCorporationId(1L)).thenReturn(Optional.empty());
        when(officeStateRepository.save(any(CorporationOfficeState.class))).thenAnswer(inv -> inv.getArgument(0));

        CorporationSave result = persistenceService.loadSave(1L, 100L);

        assertEquals(100L, result.getId());
        assertEquals(30000L, corporation.getCreditBalance());
        verify(corporationRepository).save(corporation);
    }

    @Test
    void loadSave_notFound() {
        when(saveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> persistenceService.loadSave(1L, 999L));
    }

    @Test
    void loadSave_wrongCorporation() {
        CorporationSave save = new CorporationSave(2L, "Other Corp");
        save.setId(100L);
        when(saveRepository.findById(100L)).thenReturn(Optional.of(save));

        assertThrows(IllegalArgumentException.class, () -> persistenceService.loadSave(1L, 100L));
    }

    @Test
    void deleteSave_success() {
        CorporationSave save = new CorporationSave(1L, "To Delete");
        save.setId(100L);
        when(saveRepository.findById(100L)).thenReturn(Optional.of(save));

        persistenceService.deleteSave(1L, 100L);

        verify(saveRepository).delete(save);
    }

    @Test
    void deleteSave_notFound() {
        when(saveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> persistenceService.deleteSave(1L, 999L));
    }

    @Test
    void deleteSave_wrongCorporation() {
        CorporationSave save = new CorporationSave(2L, "Other Corp");
        save.setId(100L);
        when(saveRepository.findById(100L)).thenReturn(Optional.of(save));

        assertThrows(IllegalArgumentException.class, () -> persistenceService.deleteSave(1L, 100L));
    }
}

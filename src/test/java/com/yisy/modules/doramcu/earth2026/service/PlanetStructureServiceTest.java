package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.PlanetStructure;
import com.yisy.modules.doramcu.earth2026.repository.PlanetStructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("PlanetStructureService Tests")
@ExtendWith(MockitoExtension.class)
class PlanetStructureServiceTest {

    @Mock
    private PlanetStructureRepository planetStructureRepository;

    @InjectMocks
    private PlanetStructureService planetStructureService;

    private PlanetStructure mine;
    private PlanetStructure store;
    private PlanetStructure station;

    @BeforeEach
    void setUp() {
        mine = createStructure(1L, "San Diego Iron Mine", PlanetStructure.StructureType.MINE, 1L,
                32.8657, -117.2611, 500.0, true);
        store = createStructure(2L, "San Diego Equipment Depot", PlanetStructure.StructureType.EQUIPMENT_STORE, 1L,
                32.7057, -117.1511, 20.0, true);
        station = createStructure(3L, "San Diego Central Station", PlanetStructure.StructureType.TRANSPORT_STATION, 1L,
                32.7107, -117.1561, 10.0, true);
    }

    private PlanetStructure createStructure(Long id, String name, PlanetStructure.StructureType type,
                                            Long planetId, double lat, double lon,
                                            double elevation, boolean active) {
        PlanetStructure structure = new PlanetStructure();
        structure.setId(id);
        structure.setName(name);
        structure.setStructureType(type);
        structure.setPlanetId(planetId);
        structure.setLatitude(lat);
        structure.setLongitude(lon);
        structure.setElevation(elevation);
        structure.setActive(active);
        return structure;
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryTests {

        @Test
        @DisplayName("should return all structures")
        void shouldReturnAllStructures() {
            List<PlanetStructure> structures = Arrays.asList(mine, store, station);
            when(planetStructureRepository.findAll()).thenReturn(structures);

            List<PlanetStructure> result = planetStructureService.getAllStructures();

            assertEquals(3, result.size());
            verify(planetStructureRepository).findAll();
        }

        @Test
        @DisplayName("should return structure by ID")
        void shouldReturnStructureById() {
            when(planetStructureRepository.findById(1L)).thenReturn(Optional.of(mine));

            Optional<PlanetStructure> result = planetStructureService.getStructureById(1L);

            assertTrue(result.isPresent());
            assertEquals("San Diego Iron Mine", result.get().getName());
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            when(planetStructureRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<PlanetStructure> result = planetStructureService.getStructureById(999L);

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should return structures by planet ID")
        void shouldReturnStructuresByPlanet() {
            when(planetStructureRepository.findByPlanetId(1L)).thenReturn(Arrays.asList(mine, store));

            List<PlanetStructure> result = planetStructureService.getStructuresByPlanet(1L);

            assertEquals(2, result.size());
            verify(planetStructureRepository).findByPlanetId(1L);
        }

        @Test
        @DisplayName("should return active structures by planet ID")
        void shouldReturnActiveStructuresByPlanet() {
            when(planetStructureRepository.findByPlanetIdAndActive(1L, true)).thenReturn(Arrays.asList(mine));

            List<PlanetStructure> result = planetStructureService.getActiveStructuresByPlanet(1L);

            assertEquals(1, result.size());
            verify(planetStructureRepository).findByPlanetIdAndActive(1L, true);
        }

        @Test
        @DisplayName("should return structures by planet and type")
        void shouldReturnStructuresByPlanetAndType() {
            when(planetStructureRepository.findByPlanetIdAndStructureType(1L, PlanetStructure.StructureType.MINE))
                    .thenReturn(Arrays.asList(mine));

            List<PlanetStructure> result =
                    planetStructureService.getStructuresByPlanetAndType(1L, PlanetStructure.StructureType.MINE);

            assertEquals(1, result.size());
            assertEquals(PlanetStructure.StructureType.MINE, result.get(0).getStructureType());
        }

        @Test
        @DisplayName("should return active structures by planet and type")
        void shouldReturnActiveStructuresByPlanetAndType() {
            when(planetStructureRepository.findByPlanetIdAndStructureTypeAndActive(
                    1L, PlanetStructure.StructureType.MINE, true))
                    .thenReturn(Arrays.asList(mine));

            List<PlanetStructure> result =
                    planetStructureService.getActiveStructuresByPlanetAndType(1L, PlanetStructure.StructureType.MINE);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return structures in geographic bounds")
        void shouldReturnStructuresInBounds() {
            when(planetStructureRepository.findByGeographicBounds(1L, 32.7, 32.9, -117.3, -117.1))
                    .thenReturn(Arrays.asList(mine, store));

            List<PlanetStructure> result =
                    planetStructureService.getStructuresInBounds(1L, 32.7, 32.9, -117.3, -117.1);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return mines by planet")
        void shouldReturnMinesByPlanet() {
            when(planetStructureRepository.findMinesByPlanet(1L)).thenReturn(Arrays.asList(mine));

            List<PlanetStructure> result = planetStructureService.getMinesByPlanet(1L);

            assertEquals(1, result.size());
            assertEquals(PlanetStructure.StructureType.MINE, result.get(0).getStructureType());
        }

        @Test
        @DisplayName("should return equipment stores by planet")
        void shouldReturnEquipmentStoresByPlanet() {
            when(planetStructureRepository.findEquipmentStoresByPlanet(1L)).thenReturn(Arrays.asList(store));

            List<PlanetStructure> result = planetStructureService.getEquipmentStoresByPlanet(1L);

            assertEquals(1, result.size());
            assertEquals(PlanetStructure.StructureType.EQUIPMENT_STORE, result.get(0).getStructureType());
        }

        @Test
        @DisplayName("should return transport stations by planet")
        void shouldReturnTransportStationsByPlanet() {
            when(planetStructureRepository.findTransportStationsByPlanet(1L)).thenReturn(Arrays.asList(station));

            List<PlanetStructure> result = planetStructureService.getTransportStationsByPlanet(1L);

            assertEquals(1, result.size());
            assertEquals(PlanetStructure.StructureType.TRANSPORT_STATION, result.get(0).getStructureType());
        }

        @Test
        @DisplayName("should return empty list when no structures found")
        void shouldReturnEmptyListWhenNotFound() {
            when(planetStructureRepository.findByPlanetId(999L)).thenReturn(List.of());

            List<PlanetStructure> result = planetStructureService.getStructuresByPlanet(999L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Create Structure")
    class CreateTests {

        @Test
        @DisplayName("should create and save a new structure")
        void shouldCreateAndSaveStructure() {
            when(planetStructureRepository.save(any(PlanetStructure.class))).thenReturn(mine);

            PlanetStructure result = planetStructureService.createStructure(mine);

            assertEquals("San Diego Iron Mine", result.getName());
            verify(planetStructureRepository).save(mine);
        }
    }

    @Nested
    @DisplayName("Update Structure")
    class UpdateTests {

        @Test
        @DisplayName("should update existing structure")
        void shouldUpdateExistingStructure() {
            when(planetStructureRepository.findById(1L)).thenReturn(Optional.of(mine));
            when(planetStructureRepository.save(any(PlanetStructure.class))).thenReturn(mine);

            PlanetStructure updated = new PlanetStructure();
            updated.setName("Updated Mine");
            updated.setStructureType(PlanetStructure.StructureType.MINE);
            updated.setElevation(600.0);
            updated.setActive(true);

            Optional<PlanetStructure> result = planetStructureService.updateStructure(1L, updated);

            assertTrue(result.isPresent());
            assertEquals("Updated Mine", result.get().getName());
            assertEquals(600.0, result.get().getElevation());
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            when(planetStructureRepository.findById(999L)).thenReturn(Optional.empty());

            PlanetStructure updated = new PlanetStructure();
            updated.setName("No Update");

            Optional<PlanetStructure> result = planetStructureService.updateStructure(999L, updated);

            assertFalse(result.isPresent());
            verify(planetStructureRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Structure")
    class DeleteTests {

        @Test
        @DisplayName("should delete existing structure and return true")
        void shouldDeleteExistingStructure() {
            when(planetStructureRepository.findById(1L)).thenReturn(Optional.of(mine));
            doNothing().when(planetStructureRepository).delete(mine);

            boolean result = planetStructureService.deleteStructure(1L);

            assertTrue(result);
            verify(planetStructureRepository).delete(mine);
        }

        @Test
        @DisplayName("should return false for non-existent ID")
        void shouldReturnFalseForNonExistentId() {
            when(planetStructureRepository.findById(999L)).thenReturn(Optional.empty());

            boolean result = planetStructureService.deleteStructure(999L);

            assertFalse(result);
            verify(planetStructureRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Toggle Active")
    class ToggleActiveTests {

        @Test
        @DisplayName("should toggle active from true to false")
        void shouldToggleActiveTrueToFalse() {
            mine.setActive(true);
            when(planetStructureRepository.findById(1L)).thenReturn(Optional.of(mine));
            when(planetStructureRepository.save(any(PlanetStructure.class))).thenReturn(mine);

            Optional<PlanetStructure> result = planetStructureService.toggleActive(1L);

            assertTrue(result.isPresent());
            assertFalse(result.get().isActive());
        }

        @Test
        @DisplayName("should toggle active from false to true")
        void shouldToggleActiveFalseToTrue() {
            mine.setActive(false);
            when(planetStructureRepository.findById(1L)).thenReturn(Optional.of(mine));
            when(planetStructureRepository.save(any(PlanetStructure.class))).thenReturn(mine);

            Optional<PlanetStructure> result = planetStructureService.toggleActive(1L);

            assertTrue(result.isPresent());
            assertTrue(result.get().isActive());
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            when(planetStructureRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<PlanetStructure> result = planetStructureService.toggleActive(999L);

            assertFalse(result.isPresent());
        }
    }
}

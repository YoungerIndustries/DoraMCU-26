package com.yisy.modules.doramcu.traveller.shipcustomization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ShipCustomizationService Tests")
@ExtendWith(MockitoExtension.class)
class ShipCustomizationServiceTest {

    @Mock
    private ShipComponentRepository componentRepository;

    @Mock
    private CustomShipDesignRepository designRepository;

    private ShipCustomizationService service;

    @BeforeEach
    void setUp() {
        service = new ShipCustomizationService(componentRepository, designRepository);
    }

    // ---- Component Queries ----

    @Nested
    @DisplayName("Component Query Tests")
    class ComponentQueryTests {

        @Test
        @DisplayName("getAllComponents should return all components")
        void getAllComponents_shouldReturnAll() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            ShipComponent c2 = new ShipComponent("Armor", ComponentType.ARMOR, "Desc", 50, 100000, 6);
            c2.setId(2L);
            when(componentRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

            List<ShipComponent> components = service.getAllComponents();

            assertEquals(2, components.size());
            assertEquals("Laser", components.get(0).getName());
        }

        @Test
        @DisplayName("getComponentsByType should filter by type")
        void getComponentsByType_shouldFilter() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            when(componentRepository.findByType(ComponentType.TURRET_WEAPON)).thenReturn(List.of(c1));

            List<ShipComponent> components = service.getComponentsByType(ComponentType.TURRET_WEAPON);

            assertEquals(1, components.size());
            assertEquals(ComponentType.TURRET_WEAPON, components.get(0).getType());
        }

        @Test
        @DisplayName("getComponentsByTypeAndTech should filter by type and tech level")
        void getComponentsByTypeAndTech_shouldFilter() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            when(componentRepository.findByTypeAndMinTechLevelLessThanEqual(ComponentType.TURRET_WEAPON, 11))
                    .thenReturn(List.of(c1));

            List<ShipComponent> components = service.getComponentsByTypeAndTech(ComponentType.TURRET_WEAPON, 11);

            assertEquals(1, components.size());
        }

        @Test
        @DisplayName("getComponent should return optional")
        void getComponent_shouldReturnOptional() {
            ShipComponent c1 = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            c1.setId(1L);
            when(componentRepository.findById(1L)).thenReturn(Optional.of(c1));

            Optional<ShipComponent> result = service.getComponent(1L);

            assertTrue(result.isPresent());
            assertEquals("Laser", result.get().getName());
        }

        @Test
        @DisplayName("getComponent should return empty for missing id")
        void getComponent_shouldReturnEmpty() {
            when(componentRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<ShipComponent> result = service.getComponent(999L);

            assertFalse(result.isPresent());
        }
    }

    // ---- Design CRUD ----

    @Nested
    @DisplayName("Design CRUD Tests")
    class DesignCrudTests {

        private CustomShipDesign testDesign;

        @BeforeEach
        void createDesign() {
            testDesign = new CustomShipDesign("Test Ship", "player1", 1000, 11);
            testDesign.setId(1L);
            testDesign.setValidated(false);
        }

        @Test
        @DisplayName("createDesign should save and return design")
        void createDesign_shouldSave() {
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.createDesign("Test Ship", "player1", 1000, 11);

            assertNotNull(design);
            assertEquals("Test Ship", design.getName());
            assertEquals("player1", design.getOwnerId());
            assertEquals(1000, design.getTotalTonnage());
            assertEquals(11, design.getTechLevel());
            assertFalse(design.isValidated());
            verify(designRepository).save(any(CustomShipDesign.class));
        }

        @Test
        @DisplayName("getDesign should return optional")
        void getDesign_shouldReturnOptional() {
            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));

            Optional<CustomShipDesign> result = service.getDesign(1L);

            assertTrue(result.isPresent());
            assertEquals("Test Ship", result.get().getName());
        }

        @Test
        @DisplayName("getDesignsByOwner should return list")
        void getDesignsByOwner_shouldReturnList() {
            when(designRepository.findByOwnerId("player1")).thenReturn(List.of(testDesign));

            List<CustomShipDesign> designs = service.getDesignsByOwner("player1");

            assertEquals(1, designs.size());
            assertEquals("player1", designs.get(0).getOwnerId());
        }

        @Test
        @DisplayName("updateDesign should update name and description")
        void updateDesign_shouldUpdate() {
            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.updateDesign(1L, "New Name", "New Description");

            assertEquals("New Name", design.getName());
            assertEquals("New Description", design.getDescription());
        }

        @Test
        @DisplayName("updateDesign should throw for missing id")
        void updateDesign_shouldThrowForMissing() {
            when(designRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    service.updateDesign(999L, "Name", "Desc"));
        }

        @Test
        @DisplayName("deleteDesign should delete existing design")
        void deleteDesign_shouldDelete() {
            when(designRepository.existsById(1L)).thenReturn(true);

            service.deleteDesign(1L);

            verify(designRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deleteDesign should throw for missing id")
        void deleteDesign_shouldThrowForMissing() {
            when(designRepository.existsById(999L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> service.deleteDesign(999L));
        }
    }

    // ---- Component Slot Management ----

    @Nested
    @DisplayName("Component Slot Management Tests")
    class ComponentSlotTests {

        private CustomShipDesign testDesign;
        private ShipComponent testComponent;

        @BeforeEach
        void createEntities() {
            testDesign = new CustomShipDesign("Test Ship", "player1", 1000, 11);
            testDesign.setId(1L);
            testDesign.setValidated(false);

            testComponent = new ShipComponent("Laser", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            testComponent.setId(10L);
        }

        @Test
        @DisplayName("addComponent should add component to slot")
        void addComponent_shouldAdd() {
            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(10L)).thenReturn(Optional.of(testComponent));
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.addComponent(1L, "Turret-1", 10L);

            assertEquals(10L, design.getComponent("Turret-1"));
            assertFalse(design.isValidated());
        }

        @Test
        @DisplayName("addComponent should throw for tech level mismatch")
        void addComponent_shouldThrowForTechMismatch() {
            testComponent.setMinTechLevel(15);
            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(10L)).thenReturn(Optional.of(testComponent));

            assertThrows(IllegalArgumentException.class, () ->
                    service.addComponent(1L, "Turret-1", 10L));
        }

        @Test
        @DisplayName("addComponent should throw for missing design")
        void addComponent_shouldThrowForMissingDesign() {
            when(designRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    service.addComponent(999L, "Turret-1", 10L));
        }

        @Test
        @DisplayName("addComponent should throw for missing component")
        void addComponent_shouldThrowForMissingComponent() {
            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    service.addComponent(1L, "Turret-1", 999L));
        }

        @Test
        @DisplayName("removeComponent should remove from slot")
        void removeComponent_shouldRemove() {
            testDesign.addComponent("Turret-1", 10L);
            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.removeComponent(1L, "Turret-1");

            assertNull(design.getComponent("Turret-1"));
        }
    }

    // ---- Design Validation ----

    @Nested
    @DisplayName("Design Validation Tests")
    class DesignValidationTests {

        private CustomShipDesign testDesign;

        @BeforeEach
        void createDesign() {
            testDesign = new CustomShipDesign("Test Ship", "player1", 1000, 11);
            testDesign.setId(1L);
            testDesign.setValidated(false);
        }

        @Test
        @DisplayName("validateDesign should calculate stats")
        void validateDesign_shouldCalculateStats() {
            ShipComponent drive = new ShipComponent("3G Drive", ComponentType.MANEUVER_DRIVE, "Desc", 150, 600000, 8);
            drive.setId(10L);
            drive.setDriveRating(3);

            ShipComponent power = new ShipComponent("Medium Power Plant", ComponentType.POWER_PLANT, "Desc", 40, 200000, 6);
            power.setId(11L);
            power.setPowerRating(2);

            ShipComponent armor = new ShipComponent("Titanium Steel", ComponentType.ARMOR, "Desc", 50, 100000, 6);
            armor.setId(12L);
            armor.setArmorFactor(2);

            testDesign.addComponent("Drive", 10L);
            testDesign.addComponent("Power", 11L);
            testDesign.addComponent("Armor", 12L);

            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(10L)).thenReturn(Optional.of(drive));
            when(componentRepository.findById(11L)).thenReturn(Optional.of(power));
            when(componentRepository.findById(12L)).thenReturn(Optional.of(armor));
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.validateDesign(1L);

            assertTrue(design.isValidated());
            assertEquals(3, design.getThrust());
            assertEquals(20, design.getEnergyPoints());
            assertTrue(design.getHullPoints() > 0);
        }

        @Test
        @DisplayName("validateDesign should throw for tonnage overflow")
        void validateDesign_shouldThrowForTonnageOverflow() {
            ShipComponent big = new ShipComponent("Big Component", ComponentType.CARGO, "Desc", 2000, 500000, 6);
            big.setId(10L);
            big.setCargoCapacity(100);

            testDesign.addComponent("Cargo", 10L);

            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(10L)).thenReturn(Optional.of(big));

            assertThrows(IllegalArgumentException.class, () -> service.validateDesign(1L));
        }

        @Test
        @DisplayName("validateDesign should throw for missing design")
        void validateDesign_shouldThrowForMissing() {
            when(designRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.validateDesign(999L));
        }

        @Test
        @DisplayName("validateDesign should aggregate cargo capacity")
        void validateDesign_shouldAggregateCargo() {
            ShipComponent cargo1 = new ShipComponent("Cargo 100T", ComponentType.CARGO, "Desc", 100, 50000, 6);
            cargo1.setId(10L);
            cargo1.setCargoCapacity(100);

            ShipComponent cargo2 = new ShipComponent("Cargo 500T", ComponentType.CARGO, "Desc", 500, 250000, 6);
            cargo2.setId(11L);
            cargo2.setCargoCapacity(500);

            ShipComponent power = new ShipComponent("Power", ComponentType.POWER_PLANT, "Desc", 40, 200000, 6);
            power.setId(12L);
            power.setPowerRating(1);

            testDesign.addComponent("Cargo1", 10L);
            testDesign.addComponent("Cargo2", 11L);
            testDesign.addComponent("Power", 12L);

            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(10L)).thenReturn(Optional.of(cargo1));
            when(componentRepository.findById(11L)).thenReturn(Optional.of(cargo2));
            when(componentRepository.findById(12L)).thenReturn(Optional.of(power));
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.validateDesign(1L);

            assertEquals(600, design.getCargoCapacity());
        }

        @Test
        @DisplayName("validateDesign should aggregate crew and marines")
        void validateDesign_shouldAggregateCrewMarines() {
            ShipComponent crew = new ShipComponent("Crew 10", ComponentType.CREW, "Desc", 10, 20000, 6);
            crew.setId(10L);
            crew.setCrewCapacity(10);

            ShipComponent marines = new ShipComponent("Marines 10", ComponentType.MARINES, "Desc", 20, 100000, 6);
            marines.setId(11L);
            marines.setMarineCapacity(10);

            ShipComponent power = new ShipComponent("Power", ComponentType.POWER_PLANT, "Desc", 40, 200000, 6);
            power.setId(12L);
            power.setPowerRating(1);

            testDesign.addComponent("Crew", 10L);
            testDesign.addComponent("Marines", 11L);
            testDesign.addComponent("Power", 12L);

            when(designRepository.findById(1L)).thenReturn(Optional.of(testDesign));
            when(componentRepository.findById(10L)).thenReturn(Optional.of(crew));
            when(componentRepository.findById(11L)).thenReturn(Optional.of(marines));
            when(componentRepository.findById(12L)).thenReturn(Optional.of(power));
            when(designRepository.save(any(CustomShipDesign.class))).thenReturn(testDesign);

            CustomShipDesign design = service.validateDesign(1L);

            assertEquals(10, design.getCrew());
            assertEquals(10, design.getMarines());
        }
    }

    // ---- High Guard Conversion ----

    @Nested
    @DisplayName("High Guard Conversion Tests")
    class HighGuardConversionTests {

        private CustomShipDesign testDesign;

        @BeforeEach
        void createDesign() {
            testDesign = new CustomShipDesign("Test Ship", "player1", 1000, 11);
            testDesign.setId(1L);
            testDesign.setValidated(true);
            testDesign.setEnergyPoints(20);
            testDesign.setCrew(10);
            testDesign.setMarines(5);
            testDesign.setConstructionCost(100000);
        }

        @Test
        @DisplayName("toHighGuardDesign should throw for unvalidated design")
        void toHighGuardDesign_shouldThrowForUnvalidated() {
            testDesign.setValidated(false);

            assertThrows(IllegalStateException.class, () ->
                    service.toHighGuardDesign(testDesign));
        }

        @Test
        @DisplayName("toHighGuardDesign should convert weapon components")
        void toHighGuardDesign_shouldConvertWeapons() {
            ShipComponent laser = new ShipComponent("Pulse Laser Single", ComponentType.TURRET_WEAPON, "Desc", 10, 50000, 6);
            laser.setId(10L);

            ShipComponent missile = new ShipComponent("Missile Bay Single", ComponentType.BAY_WEAPON, "Desc", 20, 100000, 6);
            missile.setId(11L);

            testDesign.addComponent("Turret-1", 10L);
            testDesign.addComponent("Bay-1", 11L);

            when(componentRepository.findById(10L)).thenReturn(Optional.of(laser));
            when(componentRepository.findById(11L)).thenReturn(Optional.of(missile));

            com.yisy.modules.doramcu.traveller.HighGuardShipDesign hgDesign =
                    service.toHighGuardDesign(testDesign);

            assertNotNull(hgDesign);
            assertEquals("Test Ship", hgDesign.getName());
            assertEquals(1000, hgDesign.getTotalTonnage());
            assertEquals(20, hgDesign.getEnergyPoints());
            assertEquals(10, hgDesign.getCrew());
            assertEquals(5, hgDesign.getMarines());
        }

        @Test
        @DisplayName("toHighGuardDesign should handle non-weapon components")
        void toHighGuardDesign_shouldHandleNonWeapons() {
            ShipComponent armor = new ShipComponent("Titanium Steel", ComponentType.ARMOR, "Desc", 50, 100000, 6);
            armor.setId(10L);

            testDesign.addComponent("Armor", 10L);

            when(componentRepository.findById(10L)).thenReturn(Optional.of(armor));

            com.yisy.modules.doramcu.traveller.HighGuardShipDesign hgDesign =
                    service.toHighGuardDesign(testDesign);

            assertNotNull(hgDesign);
            assertEquals("Test Ship", hgDesign.getName());
        }
    }

    // ---- Tonnage Validation ----

    @Nested
    @DisplayName("Tonnage Validation Tests")
    class TonnageValidationTests {

        @Test
        @DisplayName("CustomShipDesign should validate tonnage range")
        void tonnage_shouldBeInRange() {
            CustomShipDesign design = new CustomShipDesign("Test", "p1", 100, 8);
            assertEquals(100, design.getTotalTonnage());

            design.setTotalTonnage(1000000);
            assertEquals(1000000, design.getTotalTonnage());

            assertThrows(IllegalArgumentException.class, () -> design.setTotalTonnage(50));
            assertThrows(IllegalArgumentException.class, () -> design.setTotalTonnage(2000000));
        }

        @Test
        @DisplayName("CustomShipDesign should clamp tech level")
        void techLevel_shouldBeClamped() {
            CustomShipDesign design = new CustomShipDesign("Test", "p1", 1000, 11);
            design.setTechLevel(3);
            assertEquals(6, design.getTechLevel());

            design.setTechLevel(20);
            assertEquals(15, design.getTechLevel());
        }
    }
}

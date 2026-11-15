package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.traveller.HighGuardShipDesign;
import com.yisy.modules.doramcu.traveller.ShipTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardService ShipTemplate Integration Tests")
class HighGuardShipTemplateTest {

    private HighGuardService service;

    @BeforeEach
    void setUp() {
        service = new HighGuardService();
    }

    // ---- listTemplates tests ----

    @Nested
    @DisplayName("listTemplates")
    class ListTemplates {

        @Test
        @DisplayName("Should return all 8 S09 templates")
        void testAllTemplates() {
            List<ShipTemplate> templates = service.listTemplates();
            assertEquals(8, templates.size(), "S09 defines 8 ship classes");
        }

        @Test
        @DisplayName("Should include all expected ship classes")
        void testAllShipClassesPresent() {
            List<ShipTemplate> templates = service.listTemplates();
            List<String> classNames = templates.stream()
                    .map(t -> t.getShipClass().name())
                    .toList();

            assertTrue(classNames.contains("SCOUT"));
            assertTrue(classNames.contains("DESTROYER"));
            assertTrue(classNames.contains("LIGHT_CRUISER"));
            assertTrue(classNames.contains("HEAVY_CRUISER"));
            assertTrue(classNames.contains("BATTLECRUISER"));
            assertTrue(classNames.contains("BATTLESHIP"));
            assertTrue(classNames.contains("CARRIER"));
            assertTrue(classNames.contains("DREADNOUGHT"));
        }

        @Test
        @DisplayName("Should return non-empty weapon lists for each template")
        void testTemplatesHaveWeapons() {
            List<ShipTemplate> templates = service.listTemplates();
            for (ShipTemplate t : templates) {
                assertTrue(t.getTurretWeapons().size() > 0 || t.getBayWeapons().size() > 0,
                        t.getName() + " should have at least one weapon");
            }
        }

        @Test
        @DisplayName("Should return positive tonnage for each template")
        void testTemplatesHaveTonnage() {
            List<ShipTemplate> templates = service.listTemplates();
            for (ShipTemplate t : templates) {
                assertTrue(t.getTotalTonnage() > 0, t.getName() + " should have positive tonnage");
            }
        }
    }

    // ---- createFromTemplate tests ----

    @Nested
    @DisplayName("createFromTemplate")
    class CreateFromTemplate {

        @Test
        @DisplayName("Should create a ship from SCOUT template")
        void testScout() {
            HighGuardShipDesign design = service.createFromTemplate("SCOUT");
            assertNotNull(design);
            assertEquals("Scout/Courier", design.getName());
            assertEquals(2000, design.getTotalTonnage());
        }

        @Test
        @DisplayName("Should create a ship from DESTROYER template")
        void testDestroyer() {
            HighGuardShipDesign design = service.createFromTemplate("DESTROYER");
            assertNotNull(design);
            assertEquals("Destroyer", design.getName());
            assertEquals(8000, design.getTotalTonnage());
        }

        @Test
        @DisplayName("Should create a ship from BATTLESHIP template")
        void testBattleship() {
            HighGuardShipDesign design = service.createFromTemplate("BATTLESHIP");
            assertNotNull(design);
            assertEquals("Battleship", design.getName());
            assertEquals(100000, design.getTotalTonnage());
        }

        @Test
        @DisplayName("Should create a ship from DREADNOUGHT template")
        void testDreadnought() {
            HighGuardShipDesign design = service.createFromTemplate("DREADNOUGHT");
            assertNotNull(design);
            assertEquals("Dreadnought", design.getName());
            assertEquals(1000000, design.getTotalTonnage());
            assertTrue(design.getTurretWeapons().size() > 0, "Dreadnought should have turret weapons");
            assertTrue(design.getBayWeapons().size() > 0, "Dreadnought should have bay weapons");
        }

        @Test
        @DisplayName("Should throw on unknown ship class")
        void testUnknownClass() {
            assertThrows(IllegalArgumentException.class, () -> service.createFromTemplate("UNKNOWN"));
        }

        @Test
        @DisplayName("Should be case-insensitive for ship class")
        void testCaseInsensitive() {
            HighGuardShipDesign design = service.createFromTemplate("scout");
            assertNotNull(design);
            assertEquals("Scout/Courier", design.getName());
        }

        @Test
        @DisplayName("Should assign unique IDs to template-created ships")
        void testUniqueIds() {
            HighGuardShipDesign d1 = service.createFromTemplate("SCOUT");
            HighGuardShipDesign d2 = service.createFromTemplate("DESTROYER");
            assertNotEquals(d1.getId(), d2.getId());
        }

        @Test
        @DisplayName("Should add template ships to the ship registry")
        void testAddedToRegistry() {
            service.createFromTemplate("SCOUT");
            List<HighGuardShipDesign> ships = service.listShips();
            assertEquals(1, ships.size());
            assertEquals("Scout/Courier", ships.get(0).getName());
        }
    }

    // ---- createPresetShip S09 tests ----

    @Nested
    @DisplayName("createPresetShip (S09 templates)")
    class CreatePresetShip {

        @Test
        @DisplayName("Should create from scout preset name")
        void testScoutPreset() {
            HighGuardShipDesign design = service.createPresetShip("scout");
            assertNotNull(design);
            assertEquals("Scout/Courier", design.getName());
        }

        @Test
        @DisplayName("Should create from destroyer preset name")
        void testDestroyerPreset() {
            HighGuardShipDesign design = service.createPresetShip("destroyer");
            assertNotNull(design);
            assertEquals("Destroyer", design.getName());
        }

        @Test
        @DisplayName("Should create from carrier preset name")
        void testCarrierPreset() {
            HighGuardShipDesign design = service.createPresetShip("carrier");
            assertNotNull(design);
            assertEquals("Carrier", design.getName());
        }

        @Test
        @DisplayName("Should create from lightcruiser preset (no spaces)")
        void testLightCruiserPreset() {
            HighGuardShipDesign design = service.createPresetShip("lightcruiser");
            assertNotNull(design);
            assertEquals("Light Cruiser", design.getName());
        }

        @Test
        @DisplayName("Should create from heavy cruiser preset (with space)")
        void testHeavyCruiserPreset() {
            HighGuardShipDesign design = service.createPresetShip("heavy cruiser");
            assertNotNull(design);
            assertEquals("Heavy Cruiser", design.getName());
        }

        @Test
        @DisplayName("Should create from battlecruiser preset")
        void testBattlecruiserPreset() {
            HighGuardShipDesign design = service.createPresetShip("battlecruiser");
            assertNotNull(design);
            assertEquals("Battlecruiser", design.getName());
        }

        @Test
        @DisplayName("Should create from battleship preset")
        void testBattleshipPreset() {
            HighGuardShipDesign design = service.createPresetShip("battleship");
            assertNotNull(design);
            assertEquals("Battleship", design.getName());
        }

        @Test
        @DisplayName("Should create from dreadnought preset (legacy preset takes precedence)")
        void testDreadnoughtPreset() {
            HighGuardShipDesign design = service.createPresetShip("dreadnought");
            assertNotNull(design);
            assertEquals("Dreadnought", design.getName());
            // Legacy dreadnought preset (100,000 tons) takes precedence over S09 template (1,000,000 tons)
            assertEquals(100000, design.getTotalTonnage());
        }

        @Test
        @DisplayName("Should throw on unknown preset name")
        void testUnknownPreset() {
            assertThrows(IllegalArgumentException.class, () -> service.createPresetShip("unknown"));
        }

        @Test
        @DisplayName("Should preserve legacy presets (patrol, cruiser, freighter)")
        void testLegacyPresets() {
            HighGuardShipDesign patrol = service.createPresetShip("patrol");
            assertNotNull(patrol);

            HighGuardShipDesign cruiser = service.createPresetShip("cruiser");
            assertNotNull(cruiser);

            HighGuardShipDesign freighter = service.createPresetShip("freighter");
            assertNotNull(freighter);
        }
    }

    // ---- Template toDesign tests ----

    @Nested
    @DisplayName("ShipTemplate toDesign conversion")
    class ToDesign {

        @Test
        @DisplayName("Should convert template to design with correct properties")
        void testToDesign() {
            ShipTemplate template = ShipTemplate.destroyer();
            HighGuardShipDesign design = template.toDesign(999);

            assertEquals(999, design.getId());
            assertEquals("Destroyer", design.getName());
            assertEquals(8000, design.getTotalTonnage());
            assertEquals(3, design.getManeuverDrive());
            assertEquals(3, design.getJumpDrive());
        }

        @Test
        @DisplayName("Should copy turret weapons from template to design")
        void testTurretWeaponsCopied() {
            ShipTemplate template = ShipTemplate.dreadnought();
            HighGuardShipDesign design = template.toDesign();

            assertEquals(template.getTurretWeapons().size(), design.getTurretWeapons().size());
        }

        @Test
        @DisplayName("Should copy bay weapons from template to design")
        void testBayWeaponsCopied() {
            ShipTemplate template = ShipTemplate.dreadnought();
            HighGuardShipDesign design = template.toDesign();

            assertEquals(template.getBayWeapons().size(), design.getBayWeapons().size());
        }

        @Test
        @DisplayName("Should set armor from template")
        void testArmorSet() {
            ShipTemplate template = ShipTemplate.battleship();
            HighGuardShipDesign design = template.toDesign();

            assertNotNull(design.getArmor());
            assertEquals(template.getArmor(), design.getArmor());
        }

        @Test
        @DisplayName("Should set crew and marines from template")
        void testCrewAndMarines() {
            ShipTemplate template = ShipTemplate.dreadnought();
            HighGuardShipDesign design = template.toDesign();

            assertEquals(2000, design.getCrew());
            assertEquals(800, design.getMarines());
        }
    }

    // ---- Integration scenario tests ----

    @Nested
    @DisplayName("Integration scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should create two template ships and start combat")
        void testTemplateCombatWorkflow() {
            HighGuardShipDesign scout = service.createFromTemplate("SCOUT");
            HighGuardShipDesign destroyer = service.createFromTemplate("DESTROYER");

            var combat = service.startCombat(scout.getId(), destroyer.getId(), "Long");
            assertNotNull(combat);
            assertFalse(combat.isCompleted());
        }

        @Test
        @DisplayName("Should create full fleet from all 8 templates")
        void testFullFleetFromTemplates() {
            String[] classes = {"SCOUT", "DESTROYER", "LIGHT_CRUISER", "HEAVY_CRUISER",
                    "BATTLECRUISER", "BATTLESHIP", "CARRIER", "DREADNOUGHT"};

            for (String cls : classes) {
                HighGuardShipDesign design = service.createFromTemplate(cls);
                assertNotNull(design, cls + " should create a design");
            }

            assertEquals(8, service.listShips().size());
        }

        @Test
        @DisplayName("Should list templates and create from selected one")
        void testListAndCreate() {
            List<ShipTemplate> templates = service.listTemplates();
            ShipTemplate selected = templates.get(0);

            HighGuardShipDesign design = service.createFromTemplate(selected.getShipClass().name());
            assertEquals(selected.getName(), design.getName());
            assertEquals(selected.getTotalTonnage(), design.getTotalTonnage());
        }
    }
}

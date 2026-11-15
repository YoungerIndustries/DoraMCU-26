package com.yisy.modules.doramcu.warhammer40k.service;

import com.yisy.modules.doramcu.warhammer40k.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warhammer40kService")
class Warhammer40kServiceTest {

    private Warhammer40kService service;

    @BeforeEach
    void setUp() {
        service = new Warhammer40kService();
    }

    @Nested
    @DisplayName("Units")
    class Units {

        @Test
        @DisplayName("loads all units from JSON")
        void loadsAllUnits() {
            List<Unit> units = service.getUnits();

            assertNotNull(units);
            assertFalse(units.isEmpty(), "Should load units from JSON");
            assertTrue(units.size() >= 10, "Should load at least 10 units");
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            List<Unit> units = service.getUnits();

            assertThrows(UnsupportedOperationException.class, () -> units.add(null));
        }

        @Test
        @DisplayName("finds unit by id")
        void findsUnitById() {
            Optional<Unit> unit = service.getUnit("space_marine_interceptor");

            assertTrue(unit.isPresent());
            assertEquals("Space Marine Interceptor", unit.get().name());
            assertEquals("imperium", unit.get().faction());
        }

        @Test
        @DisplayName("returns empty for non-existent unit")
        void returnsEmptyForNonExistentUnit() {
            Optional<Unit> unit = service.getUnit("non_existent");

            assertFalse(unit.isPresent());
        }

        @Test
        @DisplayName("filters units by faction")
        void filtersUnitsByFaction() {
            List<Unit> imperiumUnits = service.getUnitsByFaction("imperium");
            List<Unit> chaosUnits = service.getUnitsByFaction("chaos");

            assertFalse(imperiumUnits.isEmpty());
            assertFalse(chaosUnits.isEmpty());

            imperiumUnits.forEach(u -> assertEquals("imperium", u.faction()));
            chaosUnits.forEach(u -> assertEquals("chaos", u.faction()));
        }

        @Test
        @DisplayName("returns empty list for non-existent faction")
        void returnsEmptyForNonExistentFaction() {
            List<Unit> units = service.getUnitsByFaction("non_existent");

            assertTrue(units.isEmpty());
        }

        @Test
        @DisplayName("filters infantry units")
        void filtersInfantry() {
            List<Unit> infantry = service.getInfantry();

            assertFalse(infantry.isEmpty());
            infantry.forEach(u -> assertTrue(u.isInfantry()));
        }

        @Test
        @DisplayName("filters vehicle units")
        void filtersVehicles() {
            List<Unit> vehicles = service.getVehicles();

            assertFalse(vehicles.isEmpty());
            vehicles.forEach(u -> assertTrue(u.isVehicle()));
        }

        @Test
        @DisplayName("infantry and vehicles are disjoint")
        void infantryAndVehiclesDisjoint() {
            List<Unit> infantry = service.getInfantry();
            List<Unit> vehicles = service.getVehicles();
            List<Unit> all = service.getUnits();

            assertEquals(all.size(), infantry.size() + vehicles.size());
        }
    }

    @Nested
    @DisplayName("Factions")
    class Factions {

        @Test
        @DisplayName("loads all factions from JSON")
        void loadsAllFactions() {
            List<Faction> factions = service.getFactions();

            assertNotNull(factions);
            assertEquals(6, factions.size(), "Should load 6 factions");
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            List<Faction> factions = service.getFactions();

            assertThrows(UnsupportedOperationException.class, () -> factions.add(null));
        }

        @Test
        @DisplayName("finds faction by id")
        void findsFactionById() {
            Optional<Faction> faction = service.getFaction("imperium");

            assertTrue(faction.isPresent());
            assertEquals("Imperium of Man", faction.get().name());
            assertEquals("Terra", faction.get().homeWorld());
        }

        @Test
        @DisplayName("returns empty for non-existent faction")
        void returnsEmptyForNonExistentFaction() {
            Optional<Faction> faction = service.getFaction("non_existent");

            assertFalse(faction.isPresent());
        }

        @Test
        @DisplayName("loads all six major factions")
        void loadsAllSixFactions() {
            List<Faction> factions = service.getFactions();

            List<String> ids = factions.stream().map(Faction::id).toList();
            assertTrue(ids.contains("imperium"));
            assertTrue(ids.contains("chaos"));
            assertTrue(ids.contains("orks"));
            assertTrue(ids.contains("necrons"));
            assertTrue(ids.contains("eldar"));
            assertTrue(ids.contains("tyranids"));
        }
    }

    @Nested
    @DisplayName("Scenarios")
    class Scenarios {

        @Test
        @DisplayName("loads all scenarios from JSON")
        void loadsAllScenarios() {
            List<Scenario> scenarios = service.getScenarios();

            assertNotNull(scenarios);
            assertFalse(scenarios.isEmpty(), "Should load scenarios from JSON");
            assertTrue(scenarios.size() >= 5, "Should load at least 5 scenarios");
        }

        @Test
        @DisplayName("returns unmodifiable list")
        void returnsUnmodifiableList() {
            List<Scenario> scenarios = service.getScenarios();

            assertThrows(UnsupportedOperationException.class, () -> scenarios.add(null));
        }

        @Test
        @DisplayName("finds scenario by id")
        void findsScenarioById() {
            List<Scenario> scenarios = service.getScenarios();
            if (scenarios.isEmpty()) return;

            String id = scenarios.get(0).id();
            Optional<Scenario> scenario = service.getScenario(id);

            assertTrue(scenario.isPresent());
            assertEquals(id, scenario.get().id());
        }

        @Test
        @DisplayName("returns empty for non-existent scenario")
        void returnsEmptyForNonExistentScenario() {
            Optional<Scenario> scenario = service.getScenario("non_existent");

            assertFalse(scenario.isPresent());
        }
    }

    @Nested
    @DisplayName("Lore")
    class LoreTest {

        @Test
        @DisplayName("loads lore from JSON")
        void loadsLore() {
            Lore lore = service.getLore();

            assertNotNull(lore);
            assertEquals("Warhammer 40,000", lore.title());
            assertFalse(lore.description().isEmpty());
        }

        @Test
        @DisplayName("loads eras")
        void loadsEras() {
            Lore lore = service.getLore();

            assertNotNull(lore.eras());
            assertFalse(lore.eras().isEmpty(), "Should load eras");
            assertTrue(lore.eras().size() >= 5, "Should load at least 5 eras");
        }

        @Test
        @DisplayName("loads faction lore")
        void loadsFactionLore() {
            Lore lore = service.getLore();

            assertNotNull(lore.factions());
            assertFalse(lore.factions().isEmpty(), "Should load faction lore");
        }

        @Test
        @DisplayName("loads source material")
        void loadsSourceMaterial() {
            Lore lore = service.getLore();

            assertFalse(lore.sourceMaterial().isEmpty(), "Should load source material");
        }
    }

    @Nested
    @DisplayName("Combat Rules")
    class CombatRules {

        @Test
        @DisplayName("loads combat rules from JSON")
        void loadsCombatRules() {
            Map<String, Object> rules = service.getCombatRules();

            assertNotNull(rules);
            assertFalse(rules.isEmpty(), "Should load combat rules");
            assertTrue(rules.containsKey("ground_combat"), "Should have ground combat rules");
        }

        @Test
        @DisplayName("returns unmodifiable map")
        void returnsUnmodifiableMap() {
            Map<String, Object> rules = service.getCombatRules();

            assertThrows(UnsupportedOperationException.class, () -> rules.put("key", "value"));
        }
    }

    @Nested
    @DisplayName("Lazy Loading")
    class LazyLoading {

        @Test
        @DisplayName("caches units after first load")
        void cachesUnits() {
            List<Unit> first = service.getUnits();
            List<Unit> second = service.getUnits();

            assertEquals(first, second, "Should return equivalent cached data");
        }

        @Test
        @DisplayName("caches factions after first load")
        void cachesFactions() {
            List<Faction> first = service.getFactions();
            List<Faction> second = service.getFactions();

            assertEquals(first, second, "Should return equivalent cached data");
        }
    }
}

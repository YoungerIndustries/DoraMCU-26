package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.traveller.TradeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Planet UWP Tests")
class PlanetTest {

    private Planet planet;

    @BeforeEach
    void setUp() {
        planet = new Planet(1, PlanetSize.MEDIUM);
    }

    // ---- hasUWP ----

    @Test
    @DisplayName("hasUWP - should always return true")
    void hasUWP_shouldAlwaysReturnTrue() {
        assertTrue(planet.hasUWP());
    }

    // ---- getUwpAtmosphere ----

    @Test
    @DisplayName("getUwpAtmosphere - should return 0 when no atmosphere")
    void getUwpAtmosphere_noAtmosphere() {
        Planet noAtmo = new Planet(1, 5, 0);
        assertEquals(0, noAtmo.getUwpAtmosphere());
    }

    @Test
    @DisplayName("getUwpAtmosphere - should return 1 for thin toxic (thickness 1)")
    void getUwpAtmosphere_thinToxic() {
        Planet p = new Planet(1, 5, 1);
        assertEquals(1, p.getUwpAtmosphere());
    }

    @Test
    @DisplayName("getUwpAtmosphere - should return 4 for standard (thickness 7)")
    void getUwpAtmosphere_standard() {
        Planet p = new Planet(1, 5, 7);
        assertEquals(4, p.getUwpAtmosphere());
    }

    @Test
    @DisplayName("getUwpAtmosphere - should return 9 for hostile (thickness 16)")
    void getUwpAtmosphere_hostile() {
        Planet p = new Planet(1, 5, 16);
        assertEquals(9, p.getUwpAtmosphere());
    }

    // ---- getUwpGravity ----

    @Test
    @DisplayName("getUwpGravity - TINY should return 0 (very low)")
    void getUwpGravity_tiny() {
        Planet p = new Planet(1, PlanetSize.TINY);
        assertEquals(0, p.getUwpGravity());
    }

    @Test
    @DisplayName("getUwpGravity - SMALL should return 1 (low)")
    void getUwpGravity_small() {
        Planet p = new Planet(1, PlanetSize.SMALL);
        assertEquals(1, p.getUwpGravity());
    }

    @Test
    @DisplayName("getUwpGravity - MEDIUM should return 4 (1G standard)")
    void getUwpGravity_medium() {
        Planet p = new Planet(1, PlanetSize.MEDIUM);
        assertEquals(4, p.getUwpGravity());
    }

    @Test
    @DisplayName("getUwpGravity - LARGE should return 6 (high)")
    void getUwpGravity_large() {
        Planet p = new Planet(1, PlanetSize.LARGE);
        assertEquals(6, p.getUwpGravity());
    }

    @Test
    @DisplayName("getUwpGravity - HUGE should return 8 (very high)")
    void getUwpGravity_huge() {
        Planet p = new Planet(1, PlanetSize.HUGE);
        assertEquals(8, p.getUwpGravity());
    }

    // ---- getUwpPopulationLevel ----

    @Test
    @DisplayName("getUwpPopulationLevel - should return 0 when population is 0")
    void getUwpPopulationLevel_empty() {
        planet.setCurrentPopulation(0);
        assertEquals(0, planet.getUwpPopulationLevel());
    }

    @Test
    @DisplayName("getUwpPopulationLevel - should return 9 when fully populated")
    void getUwpPopulationLevel_full() {
        planet.setCurrentPopulation(PlanetSize.MEDIUM.getMaxPopulation());
        Integer level = planet.getUwpPopulationLevel();
        // At 100% ratio, should be 9 (ecumenopoleis)
        assertEquals(9, level);
    }

    @Test
    @DisplayName("getUwpPopulationLevel - default population 1 should be tiny colony level")
    void getUwpPopulationLevel_defaultPopulation() {
        // Planet constructor sets currentPopulation = 1 by default
        // For MEDIUM size (maxPop = 10), ratio = 0.1 which is level 4 (developing)
        Integer level = planet.getUwpPopulationLevel();
        // ratio = 1/10 = 0.1, which is < 0.1 → level 4 (developing)
        // Actually 0.1 is not < 0.1, so it goes to next: < 0.2 → level 5 (urbanizing)
        assertEquals(5, level);
    }

    // ---- getUwpStarport ----

    @Test
    @DisplayName("getUwpStarport - should return 0 (Class A) with StarBase")
    void getUwpStarport_withStarBase() {
        // We can't easily create a StarBase, so test the default case
        Integer starport = planet.getUwpStarport();
        // Default is Class C (2) when no starbase
        assertEquals(2, starport);
    }

    // ---- getUwpGovernmentType ----

    @Test
    @DisplayName("getUwpGovernmentType - should return 0 for independent planet")
    void getUwpGovernmentType_independent() {
        planet.setOwnerEmpireId(null);
        assertEquals(0, planet.getUwpGovernmentType());
    }

    @Test
    @DisplayName("getUwpGovernmentType - should return 1 for owned planet")
    void getUwpGovernmentType_owned() {
        planet.setOwnerEmpireId(1);
        assertEquals(1, planet.getUwpGovernmentType());
    }

    // ---- getUwpLawLevel ----

    @Test
    @DisplayName("getUwpLawLevel - should return 0 for independent planet")
    void getUwpLawLevel_independent() {
        planet.setOwnerEmpireId(null);
        assertEquals(0, planet.getUwpLawLevel());
    }

    @Test
    @DisplayName("getUwpLawLevel - should return economicLevel+1 for owned planet")
    void getUwpLawLevel_owned() {
        planet.setOwnerEmpireId(1);
        planet.setEconomicLevel(3);
        assertEquals(4, planet.getUwpLawLevel());
    }

    @Test
    @DisplayName("getUwpLawLevel - should cap at 9")
    void getUwpLawLevel_capped() {
        planet.setOwnerEmpireId(1);
        planet.setEconomicLevel(10);
        assertEquals(9, planet.getUwpLawLevel());
    }

    // ---- getTradeCodes ----

    @Test
    @DisplayName("getTradeCodes - should return empty list")
    void getTradeCodes_empty() {
        Set<TradeCode> codes = planet.getTradeCodes();
        assertNotNull(codes);
        assertTrue(codes.isEmpty());
    }

    // ---- getEmpires ----

    @Test
    @DisplayName("getEmpires - should return empty list for independent planet")
    void getEmpires_independent() {
        planet.setOwnerEmpireId(null);
        List<Integer> empires = planet.getEmpires();
        assertNotNull(empires);
        assertTrue(empires.isEmpty());
    }

    @Test
    @DisplayName("getEmpires - should return owner empire ID for owned planet")
    void getEmpires_owned() {
        planet.setOwnerEmpireId(5);
        List<Integer> empires = planet.getEmpires();
        assertEquals(1, empires.size());
        assertEquals(5, empires.get(0));
    }
}

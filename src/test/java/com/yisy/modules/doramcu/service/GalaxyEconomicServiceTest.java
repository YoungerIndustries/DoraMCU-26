package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.EmpireArmy;
import com.yisy.modules.doramcu.hex.EmpireFacilities;
import com.yisy.modules.doramcu.hex.EmpireNavy;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.model.Galaxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalaxyEconomicServiceTest {

    @Mock
    private Galaxy galaxy;

    @Mock
    private Empire empire;

    private GalaxyEconomicService service;

    @BeforeEach
    void setUp() {
        // Service can be instantiated without repositories for testing pure calculation methods
        service = new GalaxyEconomicService(null, null, null, null, null);
    }

    /**
     * Stub the empire's army, navy, and facilities for tests that call calculateEmpireExpenses().
     * Called only from tests that need it to avoid UnnecessaryStubbingException.
     */
    private void stubEmpireComponents() {
        when(empire.getArmy()).thenReturn(new EmpireArmy(1));
        when(empire.getNavy()).thenReturn(new EmpireNavy(1));
        when(empire.getFacilities()).thenReturn(new EmpireFacilities(1));
    }

    // ==================== calculateEmpireRevenue ====================

    @Test
    void calculateEmpireRevenue_noPlanets_returnsZero() {
        when(empire.getPlanetCount()).thenReturn(0);

        BigDecimal revenue = service.calculateEmpireRevenue(empire);

        assertEquals(BigDecimal.ZERO, revenue);
    }

    @Test
    void calculateEmpireRevenue_onePlanet_returnsBaseTax() {
        when(empire.getPlanetCount()).thenReturn(1);

        BigDecimal revenue = service.calculateEmpireRevenue(empire);

        // BASE_PLANET_TAX = 10000
        assertEquals(new BigDecimal("10000"), revenue);
    }

    @Test
    void calculateEmpireRevenue_multiplePlanets_sumsCorrectly() {
        when(empire.getPlanetCount()).thenReturn(5);

        BigDecimal revenue = service.calculateEmpireRevenue(empire);

        // 5 planets * 10000 = 50000
        assertEquals(new BigDecimal("50000"), revenue);
    }

    @Test
    void calculateEmpireRevenue_manyPlanets_scalesLinearly() {
        when(empire.getPlanetCount()).thenReturn(100);

        BigDecimal revenue = service.calculateEmpireRevenue(empire);

        // 100 planets * 10000 = 1000000
        assertEquals(new BigDecimal("1000000"), revenue);
    }

    // ==================== calculateEmpireRevenue with stations ====================

    @Test
    void calculateEmpireRevenue_withStations_emptyList_works() {
        when(empire.getPlanetCount()).thenReturn(2);

        List<SpaceStation> stations = new ArrayList<>();

        // With no stations, revenue is just from planets
        BigDecimal revenue = service.calculateEmpireRevenue(empire, stations);
        assertEquals(new BigDecimal("20000"), revenue);
    }

    @Test
    void calculateEmpireRevenue_withDestroyedStations_skipsDestroyed() {
        when(empire.getPlanetCount()).thenReturn(1);

        SpaceStation destroyedStation = mock(SpaceStation.class);
        when(destroyedStation.isDestroyed()).thenReturn(true);

        List<SpaceStation> stations = new ArrayList<>();
        stations.add(destroyedStation);

        BigDecimal revenue = service.calculateEmpireRevenue(empire, stations);

        // Only planet revenue, no station income from destroyed station
        assertEquals(new BigDecimal("10000"), revenue);
    }

    // ==================== calculateEmpireNetIncome ====================

    @Test
    void calculateEmpireNetIncome_calculatesCorrectly() {
        when(empire.getPlanetCount()).thenReturn(10);
        stubEmpireComponents();

        List<SpaceStation> stations = new ArrayList<>();

        // Revenue = 10 * 10000 = 100000
        // Tax = 100000 * 0.05 = 5000.00
        // Expenses = 0 (empty army/navy/facilities)
        // Net = 100000 - 5000.00 - 0 = 95000.00
        BigDecimal netIncome = service.calculateEmpireNetIncome(empire, stations, new BigDecimal("0.05"));

        assertEquals(new BigDecimal("95000.00"), netIncome);
    }

    @Test
    void calculateEmpireNetIncome_zeroTaxRate_returnsFullRevenue() {
        when(empire.getPlanetCount()).thenReturn(5);
        stubEmpireComponents();

        List<SpaceStation> stations = new ArrayList<>();

        // Revenue = 5 * 10000 = 50000
        // Tax = 0.00 (zero scaled to 2 decimal places)
        // Expenses = 0 (empty army/navy/facilities)
        // Net = 50000 - 0.00 - 0 = 50000.00
        BigDecimal netIncome = service.calculateEmpireNetIncome(empire, stations, BigDecimal.ZERO);

        // Use compareTo for value equality regardless of scale
        assertEquals(0, netIncome.compareTo(BigDecimal.valueOf(50000)));
    }

    // ==================== calculateDefaultImprovementCosts ====================

    @Test
    void calculateDefaultImprovementCosts_returnsInfrastructureCost() {
        when(galaxy.getSystemCount()).thenReturn(100);

        Map<String, BigDecimal> costs = service.calculateDefaultImprovementCosts(galaxy);

        assertFalse(costs.isEmpty());
        assertTrue(costs.containsKey("Infrastructure Maintenance"));
        // 100 systems * 3000 = 300000
        assertEquals(new BigDecimal("300000"), costs.get("Infrastructure Maintenance"));
    }

    @Test
    void calculateDefaultImprovementCosts_nullSystemCount_usesDefault() {
        when(galaxy.getSystemCount()).thenReturn(null);

        Map<String, BigDecimal> costs = service.calculateDefaultImprovementCosts(galaxy);

        // Default system count = 100
        assertEquals(new BigDecimal("300000"), costs.get("Infrastructure Maintenance"));
    }

    @Test
    void calculateDefaultImprovementCosts_fewSystems_lowCost() {
        when(galaxy.getSystemCount()).thenReturn(10);

        Map<String, BigDecimal> costs = service.calculateDefaultImprovementCosts(galaxy);

        // 10 systems * 3000 = 30000
        assertEquals(new BigDecimal("30000"), costs.get("Infrastructure Maintenance"));
    }

    @Test
    void calculateDefaultImprovementCosts_manySystems_highCost() {
        when(galaxy.getSystemCount()).thenReturn(1000);

        Map<String, BigDecimal> costs = service.calculateDefaultImprovementCosts(galaxy);

        // 1000 systems * 3000 = 3000000
        assertEquals(new BigDecimal("3000000"), costs.get("Infrastructure Maintenance"));
    }

    // ==================== Constants ====================

    @Test
    void defaultTaxRate_isFivePercent() {
        assertEquals(new BigDecimal("0.05"), GalaxyEconomicService.DEFAULT_TAX_RATE);
    }

    @Test
    void defaultBondInterestRate_isTenPercent() {
        assertEquals(new BigDecimal("0.10"), GalaxyEconomicService.DEFAULT_BOND_INTEREST_RATE);
    }
}

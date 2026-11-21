package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GalaxyTreasuryCycleTest {

    private Galaxy galaxy;

    @BeforeEach
    void setUp() {
        galaxy = new Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Test Galaxy");
    }

    @Test
    void constructor_setsInitialValues() {
        BigDecimal startBalance = new BigDecimal("10000");
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, startBalance);

        assertEquals(galaxy, cycle.getGalaxy());
        assertEquals(1L, cycle.getCycleNumber());
        assertEquals(startBalance, cycle.getBalanceStart());
        assertEquals(BigDecimal.ZERO, cycle.getTaxRevenue());
        assertEquals(BigDecimal.ZERO, cycle.getImprovementCosts());
        assertEquals(BigDecimal.ZERO, cycle.getSurplusTransferred());
        assertEquals(BigDecimal.ZERO, cycle.getDeficitBonds());
        assertEquals(BigDecimal.ZERO, cycle.getBalanceEnd());
        assertNotNull(cycle.getProcessedAt());
    }

    @Test
    void recordEmpireTax_increasesTaxRevenue() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("5000"));

        assertEquals(new BigDecimal("5000"), cycle.getTaxRevenue());
        assertEquals(new BigDecimal("5000"), cycle.getEmpireTax(1));
    }

    @Test
    void recordEmpireTax_multipleEmpires_sumCorrectly() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        cycle.recordEmpireTax(2, new BigDecimal("3000"));
        cycle.recordEmpireTax(3, new BigDecimal("2000"));

        assertEquals(new BigDecimal("10000"), cycle.getTaxRevenue());
        assertEquals(new BigDecimal("5000"), cycle.getEmpireTax(1));
        assertEquals(new BigDecimal("3000"), cycle.getEmpireTax(2));
        assertEquals(new BigDecimal("2000"), cycle.getEmpireTax(3));
    }

    @Test
    void recordEmpireTax_sameEmpireTwice_mergesAmounts() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        cycle.recordEmpireTax(1, new BigDecimal("3000"));

        assertEquals(new BigDecimal("8000"), cycle.getTaxRevenue());
        assertEquals(new BigDecimal("8000"), cycle.getEmpireTax(1));
    }

    @Test
    void getEmpireTax_unknownEmpire_returnsZero() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, cycle.getEmpireTax(999));
    }

    @Test
    void recordImprovement_increasesImprovementCosts() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordImprovement("Infrastructure", new BigDecimal("3000"));

        assertEquals(new BigDecimal("3000"), cycle.getImprovementCosts());
    }

    @Test
    void recordImprovement_multipleImprovements_sumCorrectly() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordImprovement("Infrastructure", new BigDecimal("3000"));
        cycle.recordImprovement("Security", new BigDecimal("2000"));
        cycle.recordImprovement("Research", new BigDecimal("1000"));

        assertEquals(new BigDecimal("6000"), cycle.getImprovementCosts());
    }

    @Test
    void recordImprovement_sameDescriptionTwice_mergesAmounts() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordImprovement("Infrastructure", new BigDecimal("3000"));
        cycle.recordImprovement("Infrastructure", new BigDecimal("2000"));

        assertEquals(new BigDecimal("5000"), cycle.getImprovementCosts());
    }

    @Test
    void finalizeCycle_computesBalanceEnd_correctly() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, new BigDecimal("10000"));

        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        cycle.recordImprovement("Infrastructure", new BigDecimal("3000"));
        cycle.setSurplusTransferred(new BigDecimal("2000"));
        cycle.setDeficitBonds(BigDecimal.ZERO);

        cycle.finalizeCycle();

        // balanceEnd = balanceStart + taxRevenue - improvementCosts - surplusTransferred
        // Deficit bonds are NOT subtracted — the treasury balance is already reset to 0 when bonds are issued.
        // = 10000 + 5000 - 3000 - 2000 = 10000
        assertEquals(new BigDecimal("10000"), cycle.getBalanceEnd());
    }

    @Test
    void finalizeCycle_withDeficitBonds() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        cycle.recordImprovement("Infrastructure", new BigDecimal("8000"));
        cycle.setSurplusTransferred(BigDecimal.ZERO);
        cycle.setDeficitBonds(new BigDecimal("3000"));

        cycle.finalizeCycle();

        // balanceEnd = balanceStart + taxRevenue - improvementCosts - surplusTransferred
        // Deficit bonds are NOT subtracted again — the treasury balance was already reset to 0 when bonds were issued.
        // = 0 + 5000 - 8000 - 0 = -3000
        assertEquals(new BigDecimal("-3000"), cycle.getBalanceEnd());
    }

    @Test
    void finalizeCycle_withSurplus() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("10000"));
        cycle.recordImprovement("Infrastructure", new BigDecimal("2000"));
        cycle.setSurplusTransferred(new BigDecimal("8000"));
        cycle.setDeficitBonds(BigDecimal.ZERO);

        cycle.finalizeCycle();

        // balanceEnd = 0 + 10000 - 2000 - 8000 - 0 = 0
        assertEquals(BigDecimal.ZERO, cycle.getBalanceEnd());
    }

    @Test
    void getEmpireTaxes_returnsMap() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        cycle.recordEmpireTax(2, new BigDecimal("3000"));

        var map = cycle.getEmpireTaxes();
        assertEquals(2, map.size());
        assertEquals(new BigDecimal("5000"), map.get(1));
        assertEquals(new BigDecimal("3000"), map.get(2));
    }

    @Test
    void getImprovements_returnsMap() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordImprovement("Infrastructure", new BigDecimal("3000"));
        cycle.recordImprovement("Security", new BigDecimal("2000"));

        var map = cycle.getImprovements();
        assertEquals(2, map.size());
        assertEquals(new BigDecimal("3000"), map.get("Infrastructure"));
        assertEquals(new BigDecimal("2000"), map.get("Security"));
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle();

        cycle.setId(42L);
        cycle.setCycleNumber(5L);
        cycle.setTaxRevenue(new BigDecimal("10000"));
        cycle.setImprovementCosts(new BigDecimal("3000"));
        cycle.setSurplusTransferred(new BigDecimal("7000"));
        cycle.setDeficitBonds(new BigDecimal("1000"));
        cycle.setBalanceStart(new BigDecimal("5000"));
        cycle.setBalanceEnd(new BigDecimal("9000"));
        cycle.setNotes("Test cycle");

        assertEquals(42L, cycle.getId());
        assertEquals(5L, cycle.getCycleNumber());
        assertEquals(new BigDecimal("10000"), cycle.getTaxRevenue());
        assertEquals(new BigDecimal("3000"), cycle.getImprovementCosts());
        assertEquals(new BigDecimal("7000"), cycle.getSurplusTransferred());
        assertEquals(new BigDecimal("1000"), cycle.getDeficitBonds());
        assertEquals(new BigDecimal("5000"), cycle.getBalanceStart());
        assertEquals(new BigDecimal("9000"), cycle.getBalanceEnd());
        assertEquals("Test cycle", cycle.getNotes());
    }

    @Test
    void toString_containsKeyFields() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);
        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        String str = cycle.toString();

        assertTrue(str.contains("GalaxyTreasuryCycle"));
        assertTrue(str.contains("cycle=1"));
    }

    @Test
    void emptyCycle_finalizeCycle_balanceEndEqualsStart() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, new BigDecimal("5000"));

        cycle.finalizeCycle();

        // No taxes, improvements, surplus, or deficit — balanceEnd = balanceStart
        assertEquals(new BigDecimal("5000"), cycle.getBalanceEnd());
    }

    @Test
    void finalizeCycle_serializesEmpireTaxBreakdown() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordEmpireTax(1, new BigDecimal("5000"));
        cycle.recordEmpireTax(2, new BigDecimal("3000"));
        cycle.finalizeCycle();

        // empireTaxBreakdown should be populated as JSON
        assertNotNull(cycle.getEmpireTaxBreakdown());
        assertTrue(cycle.getEmpireTaxBreakdown().contains("5000"));
        assertTrue(cycle.getEmpireTaxBreakdown().contains("3000"));
    }

    @Test
    void finalizeCycle_serializesImprovementBreakdown() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        cycle.recordImprovement("Infrastructure", new BigDecimal("3000"));
        cycle.recordImprovement("Security", new BigDecimal("2000"));
        cycle.finalizeCycle();

        // improvementBreakdown should be populated as JSON
        assertNotNull(cycle.getImprovementBreakdown());
        assertTrue(cycle.getImprovementBreakdown().contains("3000"));
        assertTrue(cycle.getImprovementBreakdown().contains("2000"));
    }

    @Test
    void serializeMaps_emptyMaps_doesNotThrow() {
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, 1L, BigDecimal.ZERO);

        // Should not throw on empty maps
        assertDoesNotThrow(() -> cycle.serializeMaps());
        assertNull(cycle.getEmpireTaxBreakdown());
        assertNull(cycle.getImprovementBreakdown());
    }
}

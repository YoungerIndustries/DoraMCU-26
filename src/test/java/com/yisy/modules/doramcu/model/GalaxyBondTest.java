package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GalaxyBondTest {

    private Galaxy galaxy;
    private BigDecimal principal;
    private BigDecimal interestRate;

    @BeforeEach
    void setUp() {
        galaxy = new Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Test Galaxy");
        principal = new BigDecimal("100000");
        interestRate = new BigDecimal("0.10");
    }

    @Test
    void constructor_setsInitialValues() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);

        assertEquals(galaxy, bond.getGalaxy());
        assertEquals(principal, bond.getPrincipal());
        assertEquals(interestRate, bond.getInterestRate());
        assertEquals(BigDecimal.ZERO, bond.getAccruedInterest());
        assertEquals(principal, bond.getTotalOutstanding());
        assertFalse(bond.getRedeemed());
        assertEquals(1L, bond.getIssuedCycle());
        assertNotNull(bond.getIssuedAt());
        assertNull(bond.getRedeemedAt());
    }

    @Test
    void accrueInterest_increasesOutstandingAmount() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);

        bond.accrueInterest(1);

        assertTrue(bond.getAccruedInterest().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(bond.getTotalOutstanding().compareTo(principal) > 0);
        assertFalse(bond.getRedeemed());
    }

    @Test
    void accrueInterest_multipleCycles() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);

        BigDecimal outstandingAfterFirst = bond.getTotalOutstanding();
        bond.accrueInterest(1);
        BigDecimal outstandingAfterSecond = bond.getTotalOutstanding();

        assertTrue(outstandingAfterSecond.compareTo(outstandingAfterFirst) > 0,
                "Outstanding should increase after second accrual");
    }

    @Test
    void accrueInterest_multipleCyclesElapsed() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);

        bond.accrueInterest(5);

        // Accrue 1 cycle for comparison
        GalaxyBond bond2 = new GalaxyBond(galaxy, principal, interestRate, 1L);
        bond2.accrueInterest(1);

        assertTrue(bond.getAccruedInterest().compareTo(bond2.getAccruedInterest()) > 0,
                "5 cycles should accrue more interest than 1 cycle");
    }

    @Test
    void accrueInterest_redeemedBond_doesNotChange() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);
        bond.accrueInterest(1);
        BigDecimal interestBefore = bond.getAccruedInterest();

        bond.setRedeemed(true);
        bond.accrueInterest(1);

        assertEquals(interestBefore, bond.getAccruedInterest(),
                "Interest should not accrue on a redeemed bond");
    }

    @Test
    void redeem_fullAmount_marksRedeemed() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);
        bond.accrueInterest(1);
        BigDecimal totalOutstanding = bond.getTotalOutstanding();

        BigDecimal remaining = bond.redeem(totalOutstanding);

        assertTrue(bond.getRedeemed());
        assertEquals(BigDecimal.ZERO, bond.getTotalOutstanding());
        assertNotNull(bond.getRedeemedAt());
        // remaining should be >= 0 (exact amount used)
        assertTrue(remaining.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void redeem_moreThanOutstanding_returnsRemaining() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);
        bond.accrueInterest(1);
        BigDecimal totalOutstanding = bond.getTotalOutstanding();

        BigDecimal excessAmount = totalOutstanding.multiply(new BigDecimal("2"));
        BigDecimal remaining = bond.redeem(excessAmount);

        assertTrue(bond.getRedeemed());
        assertEquals(BigDecimal.ZERO, bond.getTotalOutstanding());
        // Should return the excess
        assertTrue(remaining.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void redeem_partialAmount_reducesOutstanding() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);
        bond.accrueInterest(1);
        BigDecimal totalOutstanding = bond.getTotalOutstanding();

        BigDecimal halfAmount = totalOutstanding.divide(new BigDecimal("2"));
        BigDecimal remaining = bond.redeem(halfAmount);

        assertFalse(bond.getRedeemed());
        assertEquals(BigDecimal.ZERO, remaining, "Partial redemption returns zero remaining");
        assertTrue(bond.getTotalOutstanding().compareTo(BigDecimal.ZERO) > 0,
                "Outstanding should still be positive after partial redemption");
        assertTrue(bond.getTotalOutstanding().compareTo(totalOutstanding) < 0,
                "Outstanding should be reduced");
    }

    @Test
    void redeem_partialThenFull_completesRedemption() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);
        bond.accrueInterest(1);
        BigDecimal totalOutstanding = bond.getTotalOutstanding();

        // Partial redemption
        BigDecimal halfAmount = totalOutstanding.divide(new BigDecimal("2"));
        bond.redeem(halfAmount);
        assertFalse(bond.getRedeemed());

        // Full redemption of remaining
        BigDecimal remainingOutstanding = bond.getTotalOutstanding();
        bond.redeem(remainingOutstanding);

        assertTrue(bond.getRedeemed());
        assertEquals(BigDecimal.ZERO, bond.getTotalOutstanding());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        GalaxyBond bond = new GalaxyBond();

        bond.setId(42L);
        bond.setPrincipal(new BigDecimal("50000"));
        bond.setInterestRate(new BigDecimal("0.08"));
        bond.setAccruedInterest(new BigDecimal("100"));
        bond.setTotalOutstanding(new BigDecimal("5100"));
        bond.setRedeemed(true);
        bond.setIssuedCycle(5L);
        bond.setRedeemedCycle(10L);
        bond.setNotes("Test bond");

        assertEquals(42L, bond.getId());
        assertEquals(new BigDecimal("50000"), bond.getPrincipal());
        assertEquals(new BigDecimal("0.08"), bond.getInterestRate());
        assertEquals(new BigDecimal("100"), bond.getAccruedInterest());
        assertEquals(new BigDecimal("5100"), bond.getTotalOutstanding());
        assertTrue(bond.getRedeemed());
        assertEquals(5L, bond.getIssuedCycle());
        assertEquals(10L, bond.getRedeemedCycle());
        assertEquals("Test bond", bond.getNotes());
    }

    @Test
    void toString_containsKeyFields() {
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);
        String str = bond.toString();

        assertTrue(str.contains("GalaxyBond"));
        assertTrue(str.contains(principal.toString()));
    }

    @Test
    void accrueInterest_ratePerCycle_isCorrect() {
        // Interest rate 10% per year, 52 cycles per year
        // ratePerCycle = 0.10 / 52 = 0.001923 (rounded to 4 decimal places)
        GalaxyBond bond = new GalaxyBond(galaxy, principal, interestRate, 1L);

        bond.accrueInterest(1);

        // Expected interest: 100000 * (0.10/52) * 1 = 192.31 (approx)
        BigDecimal expectedRatePerCycle = interestRate.divide(BigDecimal.valueOf(52), 4,
                java.math.BigDecimal.ROUND_HALF_UP);
        BigDecimal expectedInterest = principal.multiply(expectedRatePerCycle);
        assertEquals(expectedInterest, bond.getAccruedInterest(),
                "Interest should be calculated as principal * (rate/52) * cycles");
    }
}

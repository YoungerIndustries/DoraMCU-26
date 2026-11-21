package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GalaxyTreasuryTest {

    private Galaxy galaxy;

    @BeforeEach
    void setUp() {
        galaxy = new Galaxy();
        galaxy.setId(1L);
        galaxy.setName("Test Galaxy");
    }

    @Test
    void constructor_setsInitialValues() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);

        assertEquals(galaxy, treasury.getGalaxy());
        assertEquals(BigDecimal.ZERO, treasury.getBalance());
        assertEquals(BigDecimal.ZERO, treasury.getTotalTaxesCollected());
        assertEquals(BigDecimal.ZERO, treasury.getTotalImprovementCosts());
        assertEquals(BigDecimal.ZERO, treasury.getTotalSurplusTransferred());
        assertEquals(BigDecimal.ZERO, treasury.getTotalDeficitBonds());
        assertEquals(new BigDecimal("0.05"), treasury.getTaxRate());
        assertEquals(new BigDecimal("0.10"), treasury.getBondInterestRate());
        assertNull(treasury.getLastProcessedCycle());
        assertNotNull(treasury.getCreatedAt());
        assertNotNull(treasury.getUpdatedAt());
    }

    @Test
    void deposit_increasesBalance() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        BigDecimal amount = new BigDecimal("50000");

        treasury.deposit(amount);

        assertEquals(amount, treasury.getBalance());
    }

    @Test
    void deposit_multipleDeposits_accumulate() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);

        treasury.deposit(new BigDecimal("10000"));
        treasury.deposit(new BigDecimal("20000"));

        assertEquals(new BigDecimal("30000"), treasury.getBalance());
    }

    @Test
    void deposit_nullAmount_doesNotChange() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);

        treasury.deposit(null);

        assertEquals(BigDecimal.ZERO, treasury.getBalance());
    }

    @Test
    void deposit_zeroAmount_doesNotChange() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);

        treasury.deposit(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, treasury.getBalance());
    }

    @Test
    void deposit_negativeAmount_doesNotChange() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);

        treasury.deposit(new BigDecimal("-1000"));

        assertEquals(BigDecimal.ZERO, treasury.getBalance());
    }

    @Test
    void withdraw_sufficientBalance_returnsTrue() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("50000"));

        boolean result = treasury.withdraw(new BigDecimal("20000"));

        assertTrue(result);
        assertEquals(new BigDecimal("30000"), treasury.getBalance());
    }

    @Test
    void withdraw_insufficientBalance_goesNegative() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("10000"));

        boolean result = treasury.withdraw(new BigDecimal("20000"));

        // Withdraw always subtracts; returns false because balance went negative
        assertFalse(result);
        assertEquals(new BigDecimal("-10000"), treasury.getBalance());
    }

    @Test
    void withdraw_exactBalance_returnsTrue() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("50000"));

        boolean result = treasury.withdraw(new BigDecimal("50000"));

        assertTrue(result);
        assertEquals(BigDecimal.ZERO, treasury.getBalance());
    }

    @Test
    void withdraw_nullAmount_noOp_returnsTrue() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("50000"));

        boolean result = treasury.withdraw(null);

        // Null amount is a no-op; balance unchanged, returns true
        assertTrue(result);
        assertEquals(new BigDecimal("50000"), treasury.getBalance());
    }

    @Test
    void withdraw_zeroAmount_noOp_returnsTrue() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("50000"));

        boolean result = treasury.withdraw(BigDecimal.ZERO);

        // Zero amount is a no-op; balance unchanged, returns true
        assertTrue(result);
        assertEquals(new BigDecimal("50000"), treasury.getBalance());
    }

    @Test
    void withdraw_negativeAmount_noOp_returnsTrue() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("50000"));

        boolean result = treasury.withdraw(new BigDecimal("-1000"));

        // Negative amount is a no-op; balance unchanged, returns true
        assertTrue(result);
        assertEquals(new BigDecimal("50000"), treasury.getBalance());
    }

    @Test
    void withdraw_multipleWithdrawals_reduceBalance() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("100000"));

        treasury.withdraw(new BigDecimal("30000"));
        treasury.withdraw(new BigDecimal("20000"));

        assertEquals(new BigDecimal("50000"), treasury.getBalance());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        GalaxyTreasury treasury = new GalaxyTreasury();

        treasury.setId(42L);
        treasury.setBalance(new BigDecimal("50000"));
        treasury.setTotalTaxesCollected(new BigDecimal("100000"));
        treasury.setTotalImprovementCosts(new BigDecimal("30000"));
        treasury.setTotalSurplusTransferred(new BigDecimal("20000"));
        treasury.setTotalDeficitBonds(new BigDecimal("10000"));
        treasury.setTaxRate(new BigDecimal("0.08"));
        treasury.setBondInterestRate(new BigDecimal("0.12"));
        treasury.setLastProcessedCycle(10L);

        assertEquals(42L, treasury.getId());
        assertEquals(new BigDecimal("50000"), treasury.getBalance());
        assertEquals(new BigDecimal("100000"), treasury.getTotalTaxesCollected());
        assertEquals(new BigDecimal("30000"), treasury.getTotalImprovementCosts());
        assertEquals(new BigDecimal("20000"), treasury.getTotalSurplusTransferred());
        assertEquals(new BigDecimal("10000"), treasury.getTotalDeficitBonds());
        assertEquals(new BigDecimal("0.08"), treasury.getTaxRate());
        assertEquals(new BigDecimal("0.12"), treasury.getBondInterestRate());
        assertEquals(10L, treasury.getLastProcessedCycle());
    }

    @Test
    void toString_containsKeyFields() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.deposit(new BigDecimal("50000"));
        String str = treasury.toString();

        assertTrue(str.contains("GalaxyTreasury"));
        assertTrue(str.contains("50000"));
    }

    @Test
    void defaultTaxRate_isFivePercent() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertEquals(new BigDecimal("0.05"), treasury.getTaxRate());
    }

    @Test
    void defaultBondInterestRate_isTenPercent() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertEquals(new BigDecimal("0.10"), treasury.getBondInterestRate());
    }

    @Test
    void setTaxRate_negative_throwsException() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertThrows(IllegalArgumentException.class, () -> treasury.setTaxRate(new BigDecimal("-0.05")));
    }

    @Test
    void setTaxRate_overOne_throwsException() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertThrows(IllegalArgumentException.class, () -> treasury.setTaxRate(new BigDecimal("1.5")));
    }

    @Test
    void setTaxRate_null_throwsException() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertThrows(IllegalArgumentException.class, () -> treasury.setTaxRate(null));
    }

    @Test
    void setTaxRate_validBoundary_zero_ok() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.setTaxRate(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, treasury.getTaxRate());
    }

    @Test
    void setTaxRate_validBoundary_one_ok() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        treasury.setTaxRate(BigDecimal.ONE);
        assertEquals(BigDecimal.ONE, treasury.getTaxRate());
    }

    @Test
    void setBondInterestRate_negative_throwsException() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertThrows(IllegalArgumentException.class, () -> treasury.setBondInterestRate(new BigDecimal("-0.10")));
    }

    @Test
    void setBondInterestRate_overOne_throwsException() {
        GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
        assertThrows(IllegalArgumentException.class, () -> treasury.setBondInterestRate(new BigDecimal("2.0")));
    }
}

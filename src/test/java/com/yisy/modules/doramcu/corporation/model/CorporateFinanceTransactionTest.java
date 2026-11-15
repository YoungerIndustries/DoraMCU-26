package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CorporateFinanceTransaction}.
 */
class CorporateFinanceTransactionTest {

    @Test
    void constructor_setsDefaults() {
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction();
        assertNull(txn.getId());
        assertNotNull(txn.getCreatedAt());
    }

    @Test
    void fullConstructor_setsAllFields() {
        Long corpId = 99L;
        CorporateFinanceTransaction.TransactionType type = CorporateFinanceTransaction.TransactionType.REVENUE;
        Long amount = 5000L;
        String desc = "Test revenue";
        Long refId = 42L;

        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(corpId, type, amount, desc, refId);

        assertEquals(corpId, txn.getCorporationId());
        assertEquals(type, txn.getType());
        assertEquals(amount, txn.getAmount());
        assertEquals(desc, txn.getDescription());
        assertEquals(refId, txn.getReferenceId());
        assertNotNull(txn.getCreatedAt());
    }

    @Test
    void setters_updateFields() {
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction();
        txn.setId(1L);
        txn.setCorporationId(10L);
        txn.setType(CorporateFinanceTransaction.TransactionType.SALARY);
        txn.setAmount(-500L);
        txn.setDescription("Salary payment");
        txn.setReferenceId(5L);
        LocalDateTime now = LocalDateTime.now();
        txn.setCreatedAt(now);

        assertEquals(1L, txn.getId());
        assertEquals(10L, txn.getCorporationId());
        assertEquals(CorporateFinanceTransaction.TransactionType.SALARY, txn.getType());
        assertEquals(-500L, txn.getAmount());
        assertEquals("Salary payment", txn.getDescription());
        assertEquals(5L, txn.getReferenceId());
        assertEquals(now, txn.getCreatedAt());
    }

    @Test
    void transactionType_enumValues() {
        CorporateFinanceTransaction.TransactionType[] types = CorporateFinanceTransaction.TransactionType.values();
        assertEquals(5, types.length);
        assertSame(CorporateFinanceTransaction.TransactionType.REVENUE, types[0]);
        assertSame(CorporateFinanceTransaction.TransactionType.SALARY, types[1]);
        assertSame(CorporateFinanceTransaction.TransactionType.BONUS, types[2]);
        assertSame(CorporateFinanceTransaction.TransactionType.PENALTY, types[3]);
        assertSame(CorporateFinanceTransaction.TransactionType.EXPENSE, types[4]);
    }

    @Test
    void toString_containsFields() {
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                1L, CorporateFinanceTransaction.TransactionType.REVENUE, 1000L, "Test", 2L);
        String str = txn.toString();

        assertTrue(str.contains("corporationId=1"));
        assertTrue(str.contains("type=REVENUE"));
        assertTrue(str.contains("amount=1000"));
        assertTrue(str.contains("description='Test'"));
        assertTrue(str.contains("referenceId=2"));
    }

    @Test
    void negativeAmount_forExpenses() {
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                1L, CorporateFinanceTransaction.TransactionType.SALARY, -200L, "Salary", null);
        assertEquals(-200L, txn.getAmount());
    }

    @Test
    void nullReferenceId_allowed() {
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                1L, CorporateFinanceTransaction.TransactionType.REVENUE, 100L, "No ref", null);
        assertNull(txn.getReferenceId());
    }
}

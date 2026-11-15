package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.repository.CorporateFinanceTransactionRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CorporationEconomyService}.
 */
@ExtendWith(MockitoExtension.class)
class CorporationEconomyServiceTest {

    @Mock
    private EveCorporationRepository corporationRepository;

    @Mock
    private CorporateFinanceTransactionRepository transactionRepository;

    @Mock
    private CorporateHiringRepository hiringRepository;

    @InjectMocks
    private CorporationEconomyService economyService;

    private EveCorporation corporation;

    @BeforeEach
    void setUp() {
        corporation = new EveCorporation("Test Corp", "TEST", "CEO");
        corporation.setId(1L);
        corporation.setCreditBalance(10000L);
    }

    // ---- Credit Corporation Tests ----

    @Test
    void creditCorporation_addsToBalance() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        economyService.creditCorporation(1L, 5000L, "Contract payment", 100L);

        assertEquals(15000L, corporation.getCreditBalance());
        verify(corporationRepository).save(corporation);
        verify(transactionRepository).save(any(CorporateFinanceTransaction.class));
    }

    @Test
    void creditCorporation_withNullBalance_startsFromZero() {
        corporation.setCreditBalance(null);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        economyService.creditCorporation(1L, 5000L, "First revenue", 100L);

        assertEquals(5000L, corporation.getCreditBalance());
    }

    @Test
    void creditCorporation_logsTransaction() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        economyService.creditCorporation(1L, 5000L, "Contract payment", 100L);

        ArgumentCaptor<CorporateFinanceTransaction> captor = ArgumentCaptor.forClass(CorporateFinanceTransaction.class);
        verify(transactionRepository).save(captor.capture());
        CorporateFinanceTransaction txn = captor.getValue();
        assertEquals(1L, txn.getCorporationId());
        assertEquals(TransactionType.REVENUE, txn.getType());
        assertEquals(5000L, txn.getAmount());
        assertEquals("Contract payment", txn.getDescription());
        assertEquals(100L, txn.getReferenceId());
    }

    @Test
    void creditCorporation_throwsOnNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                economyService.creditCorporation(1L, -100L, "Bad", null));
    }

    @Test
    void creditCorporation_throwsOnZeroAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                economyService.creditCorporation(1L, 0L, "Zero", null));
    }

    @Test
    void creditCorporation_throwsOnNotFound() {
        when(corporationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                economyService.creditCorporation(999L, 100L, "Test", null));
    }

    // ---- Debit Corporation Tests ----

    @Test
    void debitCorporation_deductsFromBalance() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        economyService.debitCorporation(1L, 2000L, TransactionType.SALARY, "Salary", 100L);

        assertEquals(8000L, corporation.getCreditBalance());
    }

    @Test
    void debitCorporation_logsNegativeTransaction() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        economyService.debitCorporation(1L, 2000L, TransactionType.PENALTY, "Penalty", 100L);

        ArgumentCaptor<CorporateFinanceTransaction> captor = ArgumentCaptor.forClass(CorporateFinanceTransaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(-2000L, captor.getValue().getAmount());
        assertEquals(TransactionType.PENALTY, captor.getValue().getType());
    }

    @Test
    void debitCorporation_allowsNegativeBalance() {
        corporation.setCreditBalance(500L);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        economyService.debitCorporation(1L, 1000L, TransactionType.EXPENSE, "Big expense", null);

        assertEquals(-500L, corporation.getCreditBalance());
    }

    @Test
    void debitCorporation_throwsOnRevenueType() {
        assertThrows(IllegalArgumentException.class, () ->
                economyService.debitCorporation(1L, 100L, TransactionType.REVENUE, "Bad", null));
    }

    @Test
    void debitCorporation_throwsOnNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                economyService.debitCorporation(1L, -100L, TransactionType.SALARY, "Bad", null));
    }

    // ---- Salary Payment Tests ----

    @Test
    void paySalaries_calculatesBasedOnDuration() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        CorporateHiring hire = new CorporateHiring(1L, 10L, CorporatePosition.SECURITY_CHIEF, 700L);
        hire.setId(1L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(hire));

        Long total = economyService.paySalaries(1L, 100L, 7);

        // dailySalary=700, durationTurns=7, TURNS_PER_SALARY_PERIOD=7
        // hireSalary = 700 * 7 / 7 = 700
        assertEquals(700L, total);
        assertEquals(9300L, corporation.getCreditBalance());
    }

    @Test
    void paySalaries_multipleHires() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        CorporateHiring hire1 = new CorporateHiring(1L, 10L, CorporatePosition.SECURITY_CHIEF, 700L);
        hire1.setId(1L);
        CorporateHiring hire2 = new CorporateHiring(1L, 11L, CorporatePosition.CFO, 1400L);
        hire2.setId(2L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(hire1, hire2));

        Long total = economyService.paySalaries(1L, 100L, 7);

        // hire1: 700*7/7=700, hire2: 1400*7/7=1400, total=2100
        assertEquals(2100L, total);
        assertEquals(7900L, corporation.getCreditBalance());
    }

    @Test
    void paySalaries_zeroDuration_returnsZero() {
        // Zero duration returns early before looking up corporation
        Long total = economyService.paySalaries(1L, 100L, 0);
        assertEquals(0L, total);
        verify(corporationRepository, never()).findById(any());
        verify(hiringRepository, never()).findByCorporationIdAndStatus(any(), any());
    }

    @Test
    void paySalaries_noActiveHires_returnsZero() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());

        Long total = economyService.paySalaries(1L, 100L, 7);
        assertEquals(0L, total);
    }

    @Test
    void paySalaries_minimumOneCredit() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        // Very low salary, short duration — would round to 0
        CorporateHiring hire = new CorporateHiring(1L, 10L, CorporatePosition.ENGINEER, 1L);
        hire.setId(1L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(hire));

        Long total = economyService.paySalaries(1L, 100L, 1);

        // 1 * 1 / 7 = 0, but minimum is 1
        assertEquals(1L, total);
    }

    @Test
    void paySalaries_skipsZeroSalary() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(corporationRepository.save(any())).thenReturn(corporation);

        CorporateHiring hire = new CorporateHiring(1L, 10L, CorporatePosition.ENGINEER, 0L);
        hire.setId(1L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(List.of(hire));

        Long total = economyService.paySalaries(1L, 100L, 7);
        assertEquals(0L, total);
    }

    // ---- Financial Report Tests ----

    @Test
    void getFinancialReport_returnsAllFields() {
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(transactionRepository.sumByCorporationIdAndType(1L, TransactionType.REVENUE)).thenReturn(20000L);
        when(transactionRepository.sumByCorporationIdAndType(1L, TransactionType.SALARY)).thenReturn(-5000L);
        when(transactionRepository.sumByCorporationIdAndType(1L, TransactionType.PENALTY)).thenReturn(0L);
        when(transactionRepository.sumByCorporationIdAndType(1L, TransactionType.EXPENSE)).thenReturn(0L);
        when(transactionRepository.countByCorporationId(1L)).thenReturn(10L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());

        Map<String, Object> report = economyService.getFinancialReport(1L);

        assertEquals(1L, report.get("corporationId"));
        assertEquals("Test Corp", report.get("corporationName"));
        assertEquals(10000L, report.get("creditBalance"));
        assertEquals(20000L, report.get("totalRevenue"));
        assertEquals(5000L, report.get("totalExpenses"));
        assertEquals(15000L, report.get("netProfit"));
        assertEquals(10L, report.get("transactionCount"));
        assertFalse((Boolean) report.get("isInsolvent"));
    }

    @Test
    void getFinancialReport_insolventWhenNegative() {
        corporation.setCreditBalance(-500L);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));
        when(transactionRepository.sumByCorporationIdAndType(anyLong(), any())).thenReturn(0L);
        when(transactionRepository.countByCorporationId(1L)).thenReturn(0L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());

        Map<String, Object> report = economyService.getFinancialReport(1L);

        assertTrue((Boolean) report.get("isInsolvent"));
    }

    @Test
    void getFinancialReport_throwsOnNotFound() {
        when(corporationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                economyService.getFinancialReport(999L));
    }

    // ---- Transaction History Tests ----

    @Test
    void getTransactionHistory_returnsOrderedTransactions() {
        List<CorporateFinanceTransaction> txns = List.of(
                new CorporateFinanceTransaction(1L, TransactionType.REVENUE, 1000L, "Rev", 1L)
        );
        when(transactionRepository.findByCorporationIdOrderByCreatedAtDesc(1L)).thenReturn(txns);

        List<CorporateFinanceTransaction> result = economyService.getTransactionHistory(1L);

        assertEquals(1, result.size());
        assertEquals(TransactionType.REVENUE, result.get(0).getType());
    }

    // ---- Insolvency Tests ----

    @Test
    void isInsolvent_returnsTrueWhenNegative() {
        corporation.setCreditBalance(-100L);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));

        assertTrue(economyService.isInsolvent(1L));
    }

    @Test
    void isInsolvent_returnsFalseWhenPositive() {
        corporation.setCreditBalance(100L);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));

        assertFalse(economyService.isInsolvent(1L));
    }

    @Test
    void isInsolvent_returnsFalseWhenNullBalance() {
        corporation.setCreditBalance(null);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(corporation));

        assertFalse(economyService.isInsolvent(1L));
    }

    @Test
    void isInsolvent_throwsOnNotFound() {
        when(corporationRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
                economyService.isInsolvent(999L));
    }
}

package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for querying CorporateFinanceTransaction entities.
 */
public interface CorporateFinanceTransactionRepository extends JpaRepository<CorporateFinanceTransaction, Long> {

    /**
     * Find all transactions for a specific corporation, ordered by date descending.
     */
    List<CorporateFinanceTransaction> findByCorporationIdOrderByCreatedAtDesc(Long corporationId);

    /**
     * Find all transactions for a specific corporation and type.
     */
    List<CorporateFinanceTransaction> findByCorporationIdAndType(Long corporationId, CorporateFinanceTransaction.TransactionType type);

    /**
     * Find transactions within a date range for a specific corporation.
     */
    List<CorporateFinanceTransaction> findByCorporationIdAndCreatedAtBetween(
            Long corporationId, LocalDateTime start, LocalDateTime end);

    /**
     * Sum all amounts for a specific corporation and type.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CorporateFinanceTransaction t WHERE t.corporationId = :corpId AND t.type = :type")
    Long sumByCorporationIdAndType(@Param("corpId") Long corporationId, @Param("type") CorporateFinanceTransaction.TransactionType type);

    /**
     * Sum all amounts for a specific corporation.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CorporateFinanceTransaction t WHERE t.corporationId = :corpId")
    Long sumByCorporationId(@Param("corpId") Long corporationId);

    /**
     * Count transactions for a specific corporation.
     */
    Long countByCorporationId(Long corporationId);
}

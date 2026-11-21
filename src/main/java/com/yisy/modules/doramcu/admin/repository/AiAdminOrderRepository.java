package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AiAdminOrderRepository extends JpaRepository<AiAdminOrder, Long> {

    List<AiAdminOrder> findByStatus(AiAdminOrder.OrderStatus status);

    List<AiAdminOrder> findBySubmittedBy(String submittedBy);

    List<AiAdminOrder> findBySubmittedByContaining(String submittedBy);

    List<AiAdminOrder> findBySubmittedByContainingAndStatus(String submittedBy, AiAdminOrder.OrderStatus status);

    /** Finds pending orders not yet assigned to an AI admin. */
    List<AiAdminOrder> findByStatusAndAssignedAiAdminIdIsNull(AiAdminOrder.OrderStatus status);

    /** Finds all orders assigned to a specific AI admin. */
    List<AiAdminOrder> findByAssignedAiAdminId(Long aiAdminId);

    /** Finds pending orders submitted by a specific prefix (e.g., "GalaxyOverseer:Alpha") not yet assigned. */
    List<AiAdminOrder> findByStatusAndAssignedAiAdminIdIsNullAndSubmittedBy(AiAdminOrder.OrderStatus status, String submittedBy);

    // --- Analytics queries ---

    /** Count orders by status for health summary. */
    long countByStatus(AiAdminOrder.OrderStatus status);

    /** Count orders by admin and status for performance metrics. */
    long countByAssignedAiAdminIdAndStatus(Long aiAdminId, AiAdminOrder.OrderStatus status);

    /** Find recent orders for a specific admin, ordered by creation time descending. */
    List<AiAdminOrder> findByAssignedAiAdminIdOrderByCreatedAtDesc(Long aiAdminId, Pageable pageable);

    /** Count orders by status since a given date for trend queries. */
    long countByStatusAndCreatedAtAfter(AiAdminOrder.OrderStatus status, LocalDateTime date);

    /** Find orders by status since a given date for trend detail queries. */
    List<AiAdminOrder> findByStatusInAndCreatedAtAfter(List<AiAdminOrder.OrderStatus> statuses, LocalDateTime date, Pageable pageable);

    /** Get orders grouped by status for a date range (used for trend aggregation). */
    @Query("SELECT o.status, COUNT(o) FROM AiAdminOrder o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate GROUP BY o.status")
    List<Object[]> countOrdersByStatusAndDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /** Get average completion duration for completed orders assigned to an admin (in seconds). */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM o.completedAt) - EXTRACT(EPOCH FROM o.createdAt)) FROM AiAdminOrder o WHERE o.assignedAiAdminId = :adminId AND o.status = 'COMPLETED'")
    Double avgCompletionDurationMsByAdminId(@Param("adminId") Long adminId);

    /** Find orders assigned to an admin within a date range. */
    List<AiAdminOrder> findByAssignedAiAdminIdAndCreatedAtBetween(Long aiAdminId, LocalDateTime start, LocalDateTime end);
}

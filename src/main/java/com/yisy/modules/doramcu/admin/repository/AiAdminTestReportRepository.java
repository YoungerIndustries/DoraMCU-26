package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminTestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAdminTestReportRepository extends JpaRepository<AiAdminTestReport, Long> {

    List<AiAdminTestReport> findByOrderId(Long orderId);

    /** Find reports for multiple order IDs at once (batch lookup for analytics). */
    List<AiAdminTestReport> findByOrderIdIn(List<Long> orderIds);

    /** Get total passed/failed/skipped counts for reports created after a date. */
    @Query("SELECT SUM(r.passed), SUM(r.failed), SUM(r.skipped), SUM(r.totalTests) FROM AiAdminTestReport r WHERE r.createdAt >= :date")
    Object[] sumResultsSinceDate(@Param("date") java.time.LocalDateTime date);
}

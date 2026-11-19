package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminHealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AiAdminHealthReport entities.
 */
@Repository
public interface AiAdminHealthReportRepository extends JpaRepository<AiAdminHealthReport, Long> {

    /**
     * Find the latest report for an admin and report type.
     */
    AiAdminHealthReport findByAiAdminIdAndReportTypeOrderByGeneratedAtDesc(Long aiAdminId,
                                                                             AiAdminHealthReport.ReportType reportType);

    /**
     * Find reports for an admin within a date range.
     */
    List<AiAdminHealthReport> findByAiAdminIdAndGeneratedAtBetweenOrderByGeneratedAtDesc(Long aiAdminId,
                                                                                          LocalDateTime start,
                                                                                          LocalDateTime end);

    /**
     * Find reports for an admin by report type.
     */
    List<AiAdminHealthReport> findByAiAdminIdOrderByGeneratedAtDesc(Long aiAdminId);

    /**
     * Count reports for an admin.
     */
    long countByAiAdminId(Long aiAdminId);

    /**
     * Find all daily reports generated after a certain date.
     */
    List<AiAdminHealthReport> findByReportTypeAndGeneratedAtAfterOrderByGeneratedAtDesc(
            AiAdminHealthReport.ReportType reportType, LocalDateTime after);

    /**
     * Delete old reports older than the specified date.
     */
    void deleteByGeneratedAtBefore(LocalDateTime before);

    /**
     * Find latest reports for all admins.
     */
    @Query("SELECT r FROM AiAdminHealthReport r WHERE r.generatedAt = " +
           "(SELECT MAX(r2.generatedAt) FROM AiAdminHealthReport r2 WHERE r2.aiAdminId = r.aiAdminId)")
    List<AiAdminHealthReport> findLatestReportsForAllAdmins();
}

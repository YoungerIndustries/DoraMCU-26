package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiAdminIncidentRepository extends JpaRepository<AiAdminIncident, Long> {

    /**
     * Find incidents by status and galaxy name.
     */
    List<AiAdminIncident> findByStatusAndGalaxyName(AiAdminIncident.Status status, String galaxyName);

    /**
     * Find all incidents for a galaxy, ordered by creation date descending.
     */
    List<AiAdminIncident> findByGalaxyNameOrderByCreatedAtDesc(String galaxyName);

    /**
     * Find incidents assigned to a specific AI admin.
     */
    List<AiAdminIncident> findByAssignedAiAdminIdOrderByCreatedAtDesc(Long assignedAiAdminId);

    /**
     * Find incidents by status list.
     */
    List<AiAdminIncident> findByStatusIn(List<AiAdminIncident.Status> statuses);

    /**
     * Count incidents by status and severity.
     */
    long countByStatusAndSeverity(AiAdminIncident.Status status, AiAdminIncident.Severity severity);

    /**
     * Find open incidents created before a given time for escalation detection.
     */
    List<AiAdminIncident> findByStatusAndCreatedAtBefore(AiAdminIncident.Status status, LocalDateTime before);

    /**
     * Find investigating incidents created before a given time for escalation detection.
     */
    List<AiAdminIncident> findByStatusAndCreatedAtBeforeAndEscalatedAtIsNull(
            AiAdminIncident.Status status, LocalDateTime before);

    /**
     * Get all open incidents across all galaxies.
     */
    @Query("SELECT i FROM AiAdminIncident i WHERE i.status = :status ORDER BY i.priority DESC, i.createdAt DESC")
    List<AiAdminIncident> findOpenIncidents(@Param("status") AiAdminIncident.Status status);

    /**
     * Count incidents by severity.
     */
    long countBySeverity(AiAdminIncident.Severity severity);

    /**
     * Count incidents by status.
     */
    long countByStatus(AiAdminIncident.Status status);
}

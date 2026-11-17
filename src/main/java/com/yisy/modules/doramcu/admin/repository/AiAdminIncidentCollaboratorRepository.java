package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminIncidentCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiAdminIncidentCollaboratorRepository extends JpaRepository<AiAdminIncidentCollaborator, Long> {

    /**
     * Find all collaborators for an incident.
     */
    List<AiAdminIncidentCollaborator> findByIncidentIdOrderByJoinedAtAsc(Long incidentId);

    /**
     * Find collaborators for an incident by status.
     */
    List<AiAdminIncidentCollaborator> findByIncidentIdAndStatus(Long incidentId, AiAdminIncidentCollaborator.Status status);

    /**
     * Find a specific collaborator for an incident.
     */
    Optional<AiAdminIncidentCollaborator> findByIncidentIdAndAiAdminId(Long incidentId, Long aiAdminId);

    /**
     * Find collaborators by AI admin ID.
     */
    List<AiAdminIncidentCollaborator> findByAiAdminIdOrderByJoinedAtDesc(Long aiAdminId);

    /**
     * Find active collaborators for an incident.
     */
    @Query("SELECT c FROM AiAdminIncidentCollaborator c WHERE c.incidentId = :incidentId AND c.status IN ('ACTIVE', 'INVITED')")
    List<AiAdminIncidentCollaborator> findActiveCollaborators(@Param("incidentId") Long incidentId);

    /**
     * Find the lead collaborator for an incident.
     */
    @Query("SELECT c FROM AiAdminIncidentCollaborator c WHERE c.incidentId = :incidentId AND c.role = 'LEAD' AND c.status != 'REMOVED'")
    Optional<AiAdminIncidentCollaborator> findLeadCollaborator(@Param("incidentId") Long incidentId);

    /**
     * Count collaborators by status for an incident.
     */
    long countByIncidentIdAndStatus(Long incidentId, AiAdminIncidentCollaborator.Status status);
}

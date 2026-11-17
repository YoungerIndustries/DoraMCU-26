package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminCollaborationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAdminCollaborationNoteRepository extends JpaRepository<AiAdminCollaborationNote, Long> {

    /**
     * Find all notes for an incident, ordered by creation time.
     */
    List<AiAdminCollaborationNote> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);

    /**
     * Find notes by AI admin ID.
     */
    List<AiAdminCollaborationNote> findByAiAdminIdOrderByCreatedAtDesc(Long aiAdminId);

    /**
     * Count notes for an incident.
     */
    long countByIncidentId(Long incidentId);
}

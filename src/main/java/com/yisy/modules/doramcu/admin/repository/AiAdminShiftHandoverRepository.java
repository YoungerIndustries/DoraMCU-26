package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAdminShiftHandoverRepository extends JpaRepository<AiAdminShiftHandover, Long> {

    /**
     * Find handovers where the admin is the outgoing admin.
     */
    List<AiAdminShiftHandover> findByOutgoingAdminIdOrderByCreatedAtDesc(Long outgoingAdminId);

    /**
     * Find handovers where the admin is the incoming admin.
     */
    List<AiAdminShiftHandover> findByIncomingAdminIdOrderByCreatedAtDesc(Long incomingAdminId);

    /**
     * Find all pending handovers.
     */
    List<AiAdminShiftHandover> findByStatusOrderByCreatedAtDesc(AiAdminShiftHandover.Status status);

    /**
     * Find handovers for a specific galaxy.
     */
    List<AiAdminShiftHandover> findByGalaxyNameOrderByCreatedAtDesc(String galaxyName);

    /**
     * Find handovers for an admin (either outgoing or incoming).
     */
    List<AiAdminShiftHandover> findByOutgoingAdminIdOrIncomingAdminIdOrderByCreatedAtDesc(
            Long outgoingAdminId, Long incomingAdminId);

    /**
     * Count handovers by status.
     */
    long countByStatus(AiAdminShiftHandover.Status status);
}

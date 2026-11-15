package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CorporationInteractionProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for querying corporation interaction proposals.
 */
@Repository
public interface CorporationInteractionProposalRepository extends JpaRepository<CorporationInteractionProposal, Long> {

    /**
     * Find all proposals made by a specific corporation.
     *
     * @param proposerCorpId the proposer's corporation ID
     * @return list of proposals
     */
    List<CorporationInteractionProposal> findByProposerCorpIdOrderByProposedAtDesc(Long proposerCorpId);

    /**
     * Find all proposals received by a specific corporation.
     *
     * @param targetCorpId the target corporation ID
     * @return list of proposals
     */
    List<CorporationInteractionProposal> findByTargetCorpIdOrderByProposedAtDesc(Long targetCorpId);

    /**
     * Find all proposals involving a specific corporation.
     *
     * @param corporationId the corporation ID
     * @return list of proposals
     */
    @Query("SELECT p FROM CorporationInteractionProposal p WHERE p.proposerCorpId = :corpId OR p.targetCorpId = :corpId ORDER BY p.proposedAt DESC")
    List<CorporationInteractionProposal> findByCorporationId(@Param("corpId") Long corporationId);

    /**
     * Find pending proposals for a specific corporation.
     *
     * @param targetCorpId the target corporation ID
     * @return list of pending proposals
     */
    @Query("SELECT p FROM CorporationInteractionProposal p WHERE p.targetCorpId = :corpId AND p.status = 'PENDING' AND p.expiresAt > NOW() ORDER BY p.proposedAt DESC")
    List<CorporationInteractionProposal> findPendingProposals(@Param("corpId") Long targetCorpId);

    /**
     * Find expired proposals that need to be updated.
     *
     * @return list of expired proposals
     */
    @Query("SELECT p FROM CorporationInteractionProposal p WHERE p.status = 'PENDING' AND p.expiresAt < NOW()")
    List<CorporationInteractionProposal> findExpiredProposals();

    /**
     * Check if a proposal already exists between two corporations of a specific type.
     *
     * @param proposerCorpId the proposer's corporation ID
     * @param targetCorpId the target corporation ID
     * @param proposalType the proposal type
     * @return true if a pending proposal exists
     */
    boolean existsByProposerCorpIdAndTargetCorpIdAndProposalTypeAndStatus(
            Long proposerCorpId, Long targetCorpId,
            CorporationInteractionProposal.ProposalType proposalType,
            CorporationInteractionProposal.ProposalStatus status);
}

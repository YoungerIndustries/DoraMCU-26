package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporationInteractionProposal;
import com.yisy.modules.doramcu.corporation.model.CorporationRelationship;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationInteractionProposalRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationRelationshipRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service that handles interactions between corporations:
 * trade agreements, joint ventures, and hostile takeovers.
 * <p>
 * Manages relationship state, proposal lifecycle, and interaction outcomes.
 * </p>
 */
@Service
public class CorporationInteractionService {

    private static final Logger log = LoggerFactory.getLogger(CorporationInteractionService.class);

    /** Default trust increase for positive interactions */
    private static final int TRUST_INCREASE_POSITIVE = 10;

    /** Default trust decrease for negative interactions */
    private static final int TRUST_DECREASE_NEGATIVE = 15;

    /** Cooldown period between hostile takeover attempts (in hours) */
    private static final int TAKEOVER_COOLDOWN_HOURS = 24;

    /** Defender advantage multiplier for hostile takeovers */
    private static final double DEFENDER_ADVANTAGE = 1.5;

    private final CorporationRelationshipRepository relationshipRepository;
    private final CorporationInteractionProposalRepository proposalRepository;
    private final EveCorporationRepository corporationRepository;
    private final CorporateContractRepository contractRepository;
    private final CorporateHiringRepository hiringRepository;

    public CorporationInteractionService(
            CorporationRelationshipRepository relationshipRepository,
            CorporationInteractionProposalRepository proposalRepository,
            EveCorporationRepository corporationRepository,
            CorporateContractRepository contractRepository,
            CorporateHiringRepository hiringRepository) {
        this.relationshipRepository = relationshipRepository;
        this.proposalRepository = proposalRepository;
        this.corporationRepository = corporationRepository;
        this.contractRepository = contractRepository;
        this.hiringRepository = hiringRepository;
    }

    // ---- Relationship Management ----

    /**
     * Get all relationships for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of relationships
     */
    @Transactional(readOnly = true)
    public List<CorporationRelationship> getRelationships(Long corporationId) {
        return relationshipRepository.findByCorporationId(corporationId);
    }

    /**
     * Get the relationship between two corporations, creating a neutral one if it doesn't exist.
     *
     * @param corp1Id the first corporation ID
     * @param corp2Id the second corporation ID
     * @return the relationship
     */
    @Transactional
    public CorporationRelationship getOrCreateRelationship(Long corp1Id, Long corp2Id) {
        Optional<CorporationRelationship> existing = relationshipRepository.findByBothCorporations(corp1Id, corp2Id);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Ensure consistent ordering for unique constraint
        Long first = Math.min(corp1Id, corp2Id);
        Long second = Math.max(corp1Id, corp2Id);

        CorporationRelationship relationship = new CorporationRelationship(first, second);
        return relationshipRepository.save(relationship);
    }

    /**
     * Get interaction history for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of proposals (most recent first)
     */
    @Transactional(readOnly = true)
    public List<CorporationInteractionProposal> getInteractionHistory(Long corporationId) {
        return proposalRepository.findByCorporationId(corporationId);
    }

    /**
     * Get pending proposals for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of pending proposals
     */
    @Transactional(readOnly = true)
    public List<CorporationInteractionProposal> getPendingProposals(Long corporationId) {
        return proposalRepository.findPendingProposals(corporationId);
    }

    // ---- Trade Agreements ----

    /**
     * Propose a trade agreement between two corporations.
     *
     * @param fromCorpId the proposing corporation ID
     * @param toCorpId   the target corporation ID
     * @return the created proposal
     * @throws IllegalArgumentException if proposal already exists or corporations not found
     */
    @Transactional
    public CorporationInteractionProposal proposeTradeAgreement(Long fromCorpId, Long toCorpId) {
        validateCorporationsExist(fromCorpId, toCorpId);

        // Check for existing pending proposal
        boolean exists = proposalRepository.existsByProposerCorpIdAndTargetCorpIdAndProposalTypeAndStatus(
                fromCorpId, toCorpId,
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT,
                CorporationInteractionProposal.ProposalStatus.PENDING);
        if (exists) {
            throw new IllegalArgumentException("A pending trade agreement proposal already exists");
        }

        // Check existing relationship
        CorporationRelationship relationship = getOrCreateRelationship(fromCorpId, toCorpId);
        if (relationship.isHostile()) {
            throw new IllegalArgumentException("Cannot propose trade agreement with a hostile corporation");
        }

        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, fromCorpId, toCorpId);
        proposal.setDescription("Trade agreement proposal: mutual trade benefits including shared contract pool and reduced hiring costs");
        return proposalRepository.save(proposal);
    }

    /**
     * Accept a trade agreement proposal.
     *
     * @param corpId     the accepting corporation ID (target)
     * @param proposalId the proposal ID
     * @return the updated relationship
     * @throws IllegalArgumentException if proposal not found or invalid
     */
    @Transactional
    public CorporationRelationship acceptTradeAgreement(Long corpId, Long proposalId) {
        CorporationInteractionProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != CorporationInteractionProposal.ProposalStatus.PENDING) {
            throw new IllegalArgumentException("Proposal is not pending: " + proposal.getStatus());
        }

        if (proposal.getTargetCorpId().equals(corpId)) {
            // Target is accepting
            proposal.accept();
            proposalRepository.save(proposal);

            Long proposerCorpId = proposal.getProposerCorpId();
            CorporationRelationship relationship = getOrCreateRelationship(proposerCorpId, corpId);
            relationship.setRelationshipType(CorporationRelationship.RelationshipType.TRADE_PARTNER);
            relationship.increaseTrust(TRUST_INCREASE_POSITIVE);
            relationshipRepository.save(relationship);

            log.info("Trade agreement accepted between corporations {} and {}", proposerCorpId, corpId);
            return relationship;
        } else {
            throw new IllegalArgumentException("Only the target corporation can accept this proposal");
        }
    }

    /**
     * Reject a proposal.
     *
     * @param corpId     the rejecting corporation ID
     * @param proposalId the proposal ID
     */
    @Transactional
    public void rejectProposal(Long corpId, Long proposalId) {
        CorporationInteractionProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != CorporationInteractionProposal.ProposalStatus.PENDING) {
            throw new IllegalArgumentException("Proposal is not pending: " + proposal.getStatus());
        }

        if (!proposal.getTargetCorpId().equals(corpId)) {
            throw new IllegalArgumentException("Only the target corporation can respond to this proposal");
        }

        proposal.reject();
        proposalRepository.save(proposal);

        // Slight trust decrease for rejected proposals
        CorporationRelationship relationship = getOrCreateRelationship(proposal.getProposerCorpId(), corpId);
        relationship.decreaseTrust(5);
        relationshipRepository.save(relationship);

        log.info("Proposal {} rejected by corporation {}", proposalId, corpId);
    }

    // ---- Joint Ventures ----

    /**
     * Propose a joint venture on a contract.
     *
     * @param corpId        the proposing corporation ID
     * @param contractId    the contract ID
     * @param partnerCorpId the partner corporation ID
     * @return the created proposal
     */
    @Transactional
    public CorporationInteractionProposal proposeJointVenture(Long corpId, Long contractId, Long partnerCorpId) {
        validateCorporationsExist(corpId, partnerCorpId);

        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.AVAILABLE) {
            throw new IllegalArgumentException("Contract is not available for joint venture");
        }

        CorporationRelationship relationship = getOrCreateRelationship(corpId, partnerCorpId);
        if (relationship.isHostile()) {
            throw new IllegalArgumentException("Cannot propose joint venture with a hostile corporation");
        }

        if (!relationship.canProposeJointVenture()) {
            throw new IllegalArgumentException("Relationship not strong enough for joint venture. Need TRADE_PARTNER or ALLY status");
        }

        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.JOINT_VENTURE, corpId, partnerCorpId);
        proposal.setContractId(contractId);
        proposal.setDescription("Joint venture proposal for contract: " + contract.getTitle());
        return proposalRepository.save(proposal);
    }

    /**
     * Accept a joint venture proposal.
     *
     * @param corpId     the accepting corporation ID
     * @param proposalId the proposal ID
     * @return the updated relationship
     */
    @Transactional
    public CorporationRelationship acceptJointVenture(Long corpId, Long proposalId) {
        CorporationInteractionProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != CorporationInteractionProposal.ProposalStatus.PENDING) {
            throw new IllegalArgumentException("Proposal is not pending: " + proposal.getStatus());
        }

        if (!proposal.getTargetCorpId().equals(corpId)) {
            throw new IllegalArgumentException("Only the target corporation can accept this proposal");
        }

        proposal.accept();
        proposalRepository.save(proposal);

        Long proposerCorpId = proposal.getProposerCorpId();
        CorporationRelationship relationship = getOrCreateRelationship(proposerCorpId, corpId);
        relationship.setRelationshipType(CorporationRelationship.RelationshipType.JOINT_VENTURE);
        relationship.increaseTrust(TRUST_INCREASE_POSITIVE);
        relationshipRepository.save(relationship);

        log.info("Joint venture accepted between corporations {} and {} for contract {}",
                proposerCorpId, corpId, proposal.getContractId());
        return relationship;
    }

    // ---- Hostile Takeovers ----

    /**
     * Attempt a hostile takeover of another corporation.
     *
     * @param attackerCorpId the attacking corporation ID
     * @param targetCorpId   the target corporation ID
     * @return map with result details
     * @throws IllegalArgumentException if takeover cannot be attempted
     */
    @Transactional
    public Map<String, Object> attemptHostileTakeover(Long attackerCorpId, Long targetCorpId) {
        validateCorporationsExist(attackerCorpId, targetCorpId);

        // Check for existing hostile relationship
        CorporationRelationship relationship = getOrCreateRelationship(attackerCorpId, targetCorpId);
        if (relationship.isHostile()) {
            // Check cooldown
            if (relationship.getLastInteractionAt() != null) {
                LocalDateTime lastInteraction = relationship.getLastInteractionAt();
                if (LocalDateTime.now().isBefore(lastInteraction.plusHours(TAKEOVER_COOLDOWN_HOURS))) {
                    throw new IllegalArgumentException("Takeover cooldown active. Try again later.");
                }
            }
        }

        // Calculate power scores
        EveCorporation attacker = corporationRepository.findById(attackerCorpId)
                .orElseThrow(() -> new IllegalArgumentException("Attacker corporation not found"));
        EveCorporation target = corporationRepository.findById(targetCorpId)
                .orElseThrow(() -> new IllegalArgumentException("Target corporation not found"));

        double attackerPower = calculateCorporationPower(attacker);
        double targetPower = calculateCorporationPower(target) * DEFENDER_ADVANTAGE;

        // Add alliance bonuses to target
        List<CorporationRelationship> targetAlliances = relationshipRepository
                .findByCorporationIdAndType(targetCorpId, CorporationRelationship.RelationshipType.ALLY);
        targetPower += targetAlliances.size() * 500;

        boolean success = attackerPower > targetPower;

        // Create proposal record
        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.HOSTILE_TAKEOVER, attackerCorpId, targetCorpId);
        proposal.setDescription(String.format("Hostile takeover attempt: attacker power=%.0f vs target power=%.0f",
                attackerPower, targetPower));

        if (success) {
            proposal.accept(); // "Accepted" means the takeover succeeded
            relationship.setRelationshipType(CorporationRelationship.RelationshipType.HOSTILE);
            relationship.setTrustLevel(0);

            // Transfer some resources
            long transferAmount = Math.min(target.getCreditBalance(), target.getCreditBalance() / 2);
            if (transferAmount > 0) {
                target.setCreditBalance(target.getCreditBalance() - transferAmount);
                attacker.setCreditBalance(attacker.getCreditBalance() + transferAmount);
                corporationRepository.save(target);
                corporationRepository.save(attacker);
            }
        } else {
            proposal.reject(); // "Rejected" means the takeover failed
            relationship.setRelationshipType(CorporationRelationship.RelationshipType.HOSTILE);
            relationship.decreaseTrust(TRUST_DECREASE_NEGATIVE);

            // Attacker loses credits
            long lossAmount = Math.min(attacker.getCreditBalance(), 1000);
            if (lossAmount > 0) {
                attacker.setCreditBalance(attacker.getCreditBalance() - lossAmount);
                corporationRepository.save(attacker);
            }
        }

        proposalRepository.save(proposal);
        relationshipRepository.save(relationship);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("attackerPower", attackerPower);
        result.put("targetPower", targetPower);
        result.put("attackerCorpId", attackerCorpId);
        result.put("targetCorpId", targetCorpId);
        result.put("message", success ?
                "Hostile takeover successful! Resources transferred." :
                "Hostile takeover failed! Relationship is now hostile.");
        return result;
    }

    /**
     * Calculate the power score of a corporation for hostile takeover mechanics.
     * Power = credit balance + crew count Ã— average performance + security contracts
     *
     * @param corporation the corporation
     * @return power score
     */
    public double calculateCorporationPower(EveCorporation corporation) {
        double power = corporation.getCreditBalance();

        // Crew contribution
        List<CorporateHiring> hires = hiringRepository.findByCorporationIdAndStatus(
                corporation.getId(), CorporateHiring.Status.ACTIVE);
        if (!hires.isEmpty()) {
            double avgPerformance = hires.stream()
                    .mapToDouble(CorporateHiring::getPerformanceRating)
                    .average()
                    .orElse(5.0);
            power += hires.size() * avgPerformance * 100;
        }

        // Active contracts contribution
        List<CorporateContract> activeContracts = contractRepository
                .findByCorporationIdAndStatus(corporation.getId(), CorporateContract.Status.ACTIVE);
        power += activeContracts.size() * 500;

        return power;
    }

    // ---- Trade Agreement Benefits ----

    /**
     * Apply trade agreement bonuses to a base cost.
     * Each trade partner reduces costs by 10%.
     *
     * @param corporationId the corporation ID
     * @param baseCost      the base cost
     * @return adjusted cost
     */
    @Transactional(readOnly = true)
    public long applyTradeAgreementBonuses(Long corporationId, long baseCost) {
        List<CorporationRelationship> tradePartners = relationshipRepository
                .findByCorporationIdAndType(corporationId, CorporationRelationship.RelationshipType.TRADE_PARTNER);

        double discountMultiplier = 1.0 - (tradePartners.size() * 0.1);
        discountMultiplier = Math.max(0.5, discountMultiplier); // Cap at 50% discount

        return (long) (baseCost * discountMultiplier);
    }

    /**
     * Get the number of trade partners for a corporation.
     *
     * @param corporationId the corporation ID
     * @return trade partner count
     */
    @Transactional(readOnly = true)
    public int getTradePartnerCount(Long corporationId) {
        return relationshipRepository
                .findByCorporationIdAndType(corporationId, CorporationRelationship.RelationshipType.TRADE_PARTNER)
                .size();
    }

    // ---- Utility Methods ----

    private void validateCorporationsExist(Long corp1Id, Long corp2Id) {
        if (!corporationRepository.existsById(corp1Id)) {
            throw new IllegalArgumentException("Corporation not found: " + corp1Id);
        }
        if (!corporationRepository.existsById(corp2Id)) {
            throw new IllegalArgumentException("Corporation not found: " + corp2Id);
        }
    }

    /**
     * Expire any expired proposals.
     * Call this periodically to clean up old proposals.
     */
    @Transactional
    public void expireOldProposals() {
        List<CorporationInteractionProposal> expired = proposalRepository.findExpiredProposals();
        for (CorporationInteractionProposal proposal : expired) {
            proposal.expireIfExpired();
            proposalRepository.save(proposal);
            log.info("Expired proposal {}", proposal.getId());
        }
    }
}

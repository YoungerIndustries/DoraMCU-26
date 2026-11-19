package com.yisy.modules.doramcu.corporation.controller;

import com.yisy.modules.doramcu.corporation.model.CorporationInteractionProposal;
import com.yisy.modules.doramcu.corporation.model.CorporationRelationship;
import com.yisy.modules.doramcu.corporation.service.CorporationInteractionService;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for corporation interactions:
 * trade agreements, joint ventures, and hostile takeovers.
 * <p>
 * All endpoints are under /api/v1/corporate/interactions.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/corporate/interactions")
public class CorporationInteractionController {

    private static final Logger log = LoggerFactory.getLogger(CorporationInteractionController.class);

    private final CorporationInteractionService interactionService;
    private final EveCorporationRepository corporationRepository;

    public CorporationInteractionController(CorporationInteractionService interactionService,
                                           EveCorporationRepository corporationRepository) {
        this.interactionService = interactionService;
        this.corporationRepository = corporationRepository;
    }

    // ---- Relationships ----

    /**
     * Get all relationships for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of relationships
     */
    @GetMapping("/relationships")
    public ResponseEntity<List<CorporationRelationship>> getRelationships(
            @RequestParam Long corporationId) {
        log.debug("GET /api/v1/corporate/interactions/relationships?corporationId={}", corporationId);
        List<CorporationRelationship> relationships = interactionService.getRelationships(corporationId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * Get pending proposals for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of pending proposals
     */
    @GetMapping("/proposals/pending")
    public ResponseEntity<List<CorporationInteractionProposal>> getPendingProposals(
            @RequestParam Long corporationId) {
        log.debug("GET /api/v1/corporate/interactions/proposals/pending?corporationId={}", corporationId);
        List<CorporationInteractionProposal> proposals = interactionService.getPendingProposals(corporationId);
        return ResponseEntity.ok(proposals);
    }

    /**
     * Get interaction history for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of proposals
     */
    @GetMapping("/history")
    public ResponseEntity<List<CorporationInteractionProposal>> getInteractionHistory(
            @RequestParam Long corporationId) {
        log.debug("GET /api/v1/corporate/interactions/history?corporationId={}", corporationId);
        List<CorporationInteractionProposal> history = interactionService.getInteractionHistory(corporationId);
        return ResponseEntity.ok(history);
    }

    // ---- Trade Agreements ----

    /**
     * Propose a trade agreement with another corporation.
     *
     * @param corporationId the proposing corporation ID
     * @param targetCorpId  the target corporation ID
     * @return the created proposal
     */
    @PostMapping("/trade/propose")
    public ResponseEntity<Map<String, Object>> proposeTradeAgreement(
            @RequestParam Long corporationId,
            @RequestParam Long targetCorpId) {
        log.info("POST /api/v1/corporate/interactions/trade/propose?corporationId={},targetCorpId={}",
                corporationId, targetCorpId);

        try {
            CorporationInteractionProposal proposal = interactionService.proposeTradeAgreement(corporationId, targetCorpId);
            Map<String, Object> result = new HashMap<>();
            result.put("proposalId", proposal.getId());
            result.put("proposalType", proposal.getProposalType().name());
            result.put("status", proposal.getStatus().name());
            result.put("expiresAt", proposal.getExpiresAt().toString());
            result.put("message", "Trade agreement proposal sent");
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Accept a trade agreement proposal.
     *
     * @param corporationId the accepting corporation ID
     * @param proposalId    the proposal ID
     * @return the updated relationship
     */
    @PostMapping("/trade/accept")
    public ResponseEntity<Map<String, Object>> acceptTradeAgreement(
            @RequestParam Long corporationId,
            @RequestParam Long proposalId) {
        log.info("POST /api/v1/corporate/interactions/trade/accept?corporationId={},proposalId={}",
                corporationId, proposalId);

        try {
            CorporationRelationship relationship = interactionService.acceptTradeAgreement(corporationId, proposalId);
            Map<String, Object> result = new HashMap<>();
            result.put("relationshipId", relationship.getId());
            result.put("relationshipType", relationship.getRelationshipType().name());
            result.put("trustLevel", relationship.getTrustLevel());
            result.put("message", "Trade agreement accepted");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Joint Ventures ----

    /**
     * Propose a joint venture on a contract.
     *
     * @param corporationId the proposing corporation ID
     * @param contractId    the contract ID
     * @param partnerCorpId the partner corporation ID
     * @return the created proposal
     */
    @PostMapping("/joint-venture/propose")
    public ResponseEntity<Map<String, Object>> proposeJointVenture(
            @RequestParam Long corporationId,
            @RequestParam Long contractId,
            @RequestParam Long partnerCorpId) {
        log.info("POST /api/v1/corporate/interactions/joint-venture/propose?corporationId={},contractId={},partnerCorpId={}",
                corporationId, contractId, partnerCorpId);

        try {
            CorporationInteractionProposal proposal = interactionService.proposeJointVenture(corporationId, contractId, partnerCorpId);
            Map<String, Object> result = new HashMap<>();
            result.put("proposalId", proposal.getId());
            result.put("proposalType", proposal.getProposalType().name());
            result.put("contractId", proposal.getContractId());
            result.put("status", proposal.getStatus().name());
            result.put("message", "Joint venture proposal sent");
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Accept a joint venture proposal.
     *
     * @param corporationId the accepting corporation ID
     * @param proposalId    the proposal ID
     * @return the updated relationship
     */
    @PostMapping("/joint-venture/accept")
    public ResponseEntity<Map<String, Object>> acceptJointVenture(
            @RequestParam Long corporationId,
            @RequestParam Long proposalId) {
        log.info("POST /api/v1/corporate/interactions/joint-venture/accept?corporationId={},proposalId={}",
                corporationId, proposalId);

        try {
            CorporationRelationship relationship = interactionService.acceptJointVenture(corporationId, proposalId);
            Map<String, Object> result = new HashMap<>();
            result.put("relationshipId", relationship.getId());
            result.put("relationshipType", relationship.getRelationshipType().name());
            result.put("trustLevel", relationship.getTrustLevel());
            result.put("message", "Joint venture accepted");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Hostile Takeovers ----

    /**
     * Attempt a hostile takeover of another corporation.
     *
     * @param corporationId the attacking corporation ID
     * @param targetCorpId  the target corporation ID
     * @return the result of the takeover attempt
     */
    @PostMapping("/takeover/attempt")
    public ResponseEntity<Map<String, Object>> attemptHostileTakeover(
            @RequestParam Long corporationId,
            @RequestParam Long targetCorpId) {
        log.info("POST /api/v1/corporate/interactions/takeover/attempt?corporationId={},targetCorpId={}",
                corporationId, targetCorpId);

        try {
            Map<String, Object> result = interactionService.attemptHostileTakeover(corporationId, targetCorpId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Proposal Management ----

    /**
     * Reject a proposal.
     *
     * @param corporationId the rejecting corporation ID
     * @param proposalId    the proposal ID
     * @return confirmation
     */
    @PostMapping("/proposal/reject")
    public ResponseEntity<Map<String, Object>> rejectProposal(
            @RequestParam Long corporationId,
            @RequestParam Long proposalId) {
        log.info("POST /api/v1/corporate/interactions/proposal/reject?corporationId={},proposalId={}",
                corporationId, proposalId);

        try {
            interactionService.rejectProposal(corporationId, proposalId);
            return ResponseEntity.ok(Map.of("message", "Proposal rejected"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Corporation Power ----

    /**
     * Get the power score of a corporation (for takeover mechanics).
     *
     * @param corporationId the corporation ID
     * @return power score
     */
    @GetMapping("/power")
    public ResponseEntity<Map<String, Object>> getCorporationPower(
            @RequestParam Long corporationId) {
        log.debug("GET /api/v1/corporate/interactions/power?corporationId={}", corporationId);

        try {
            Optional<EveCorporation> corp = corporationRepository.findById(corporationId);
            if (corp.isEmpty()) {
                return ResponseEntity.ok(Map.of("corporationId", corporationId, "power", 0.0));
            }
            double power = interactionService.calculateCorporationPower(corp.get());
            return ResponseEntity.ok(Map.of("corporationId", corporationId, "power", power));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("corporationId", corporationId, "power", 0.0));
        }
    }
}

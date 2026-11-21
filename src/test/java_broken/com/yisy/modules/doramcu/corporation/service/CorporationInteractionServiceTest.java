package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporationInteractionProposal;
import com.yisy.modules.doramcu.corporation.model.CorporationRelationship;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationInteractionProposalRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationRelationshipRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CorporationInteractionService.
 */
class CorporationInteractionServiceTest {

    @Mock
    private CorporationRelationshipRepository relationshipRepository;

    @Mock
    private CorporationInteractionProposalRepository proposalRepository;

    @Mock
    private EveCorporationRepository corporationRepository;

    @Mock
    private CorporateContractRepository contractRepository;

    @Mock
    private CorporateHiringRepository hiringRepository;

    @InjectMocks
    private CorporationInteractionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---- Relationship Management ----

    @Test
    void get_relationships() {
        when(relationshipRepository.findByCorporationId(1L))
                .thenReturn(Collections.singletonList(new CorporationRelationship(1L, 2L)));
        List<CorporationRelationship> result = service.getRelationships(1L);
        assertEquals(1, result.size());
    }

    @Test
    void get_or_create_relationship_creates_new() {
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        CorporationRelationship saved = new CorporationRelationship(1L, 2L);
        saved.setId(1L);
        when(relationshipRepository.save(any())).thenReturn(saved);

        CorporationRelationship result = service.getOrCreateRelationship(1L, 2L);
        assertEquals(1L, result.getId());
        verify(relationshipRepository).save(any());
    }

    @Test
    void get_or_create_relationship_returns_existing() {
        CorporationRelationship existing = new CorporationRelationship(1L, 2L);
        existing.setId(1L);
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.of(existing));

        CorporationRelationship result = service.getOrCreateRelationship(1L, 2L);
        assertEquals(1L, result.getId());
        verify(relationshipRepository, never()).save(any());
    }

    // ---- Trade Agreements ----

    @Test
    void propose_trade_agreement_success() {
        when(corporationRepository.existsById(1L)).thenReturn(true);
        when(corporationRepository.existsById(2L)).thenReturn(true);
        when(proposalRepository.existsByProposerCorpIdAndTargetCorpIdAndProposalTypeAndStatus(
                1L, 2L, CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT,
                CorporationInteractionProposal.ProposalStatus.PENDING)).thenReturn(false);
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        CorporationRelationship rel = new CorporationRelationship(1L, 2L);
        when(relationshipRepository.save(any(CorporationRelationship.class))).thenReturn(rel);

        CorporationInteractionProposal saved = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, 1L, 2L);
        saved.setId(1L);
        when(proposalRepository.save(any())).thenReturn(saved);

        CorporationInteractionProposal result = service.proposeTradeAgreement(1L, 2L);
        assertEquals(1L, result.getId());
        assertEquals(CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, result.getProposalType());
    }

    @Test
    void propose_trade_agreement_duplicate() {
        when(corporationRepository.existsById(1L)).thenReturn(true);
        when(corporationRepository.existsById(2L)).thenReturn(true);
        when(proposalRepository.existsByProposerCorpIdAndTargetCorpIdAndProposalTypeAndStatus(
                1L, 2L, CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT,
                CorporationInteractionProposal.ProposalStatus.PENDING)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.proposeTradeAgreement(1L, 2L));
    }

    @Test
    void propose_trade_agreement_hostile() {
        when(corporationRepository.existsById(1L)).thenReturn(true);
        when(corporationRepository.existsById(2L)).thenReturn(true);
        when(proposalRepository.existsByProposerCorpIdAndTargetCorpIdAndProposalTypeAndStatus(
                1L, 2L, CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT,
                CorporationInteractionProposal.ProposalStatus.PENDING)).thenReturn(false);
        CorporationRelationship rel = new CorporationRelationship(1L, 2L);
        rel.setRelationshipType(CorporationRelationship.RelationshipType.HOSTILE);
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.of(rel));

        assertThrows(IllegalArgumentException.class, () -> service.proposeTradeAgreement(1L, 2L));
    }

    @Test
    void accept_trade_agreement_success() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, 1L, 2L);
        proposal.setId(1L);
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        CorporationRelationship rel = new CorporationRelationship(1L, 2L);
        when(relationshipRepository.save(any(CorporationRelationship.class))).thenReturn(rel);

        CorporationRelationship result = service.acceptTradeAgreement(2L, 1L);
        assertEquals(CorporationRelationship.RelationshipType.TRADE_PARTNER, result.getRelationshipType());
        assertEquals(CorporationInteractionProposal.ProposalStatus.ACCEPTED, proposal.getStatus());
    }

    @Test
    void reject_proposal() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, 1L, 2L);
        proposal.setId(1L);
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        when(relationshipRepository.save(any(CorporationRelationship.class))).thenReturn(new CorporationRelationship(1L, 2L));

        service.rejectProposal(2L, 1L);
        assertEquals(CorporationInteractionProposal.ProposalStatus.REJECTED, proposal.getStatus());
    }

    // ---- Joint Ventures ----

    @Test
    void propose_joint_venture_requires_trade_partner() {
        when(corporationRepository.existsById(1L)).thenReturn(true);
        when(corporationRepository.existsById(2L)).thenReturn(true);
        CorporateContract contract = new CorporateContract();
        contract.setId(1L);
        contract.setStatus(CorporateContract.Status.AVAILABLE);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        when(relationshipRepository.save(any(CorporationRelationship.class))).thenReturn(new CorporationRelationship(1L, 2L));

        assertThrows(IllegalArgumentException.class, () -> service.proposeJointVenture(1L, 1L, 2L));
    }

    // ---- Hostile Takeovers ----

    @Test
    void attempt_hostile_takeover_success() {
        when(corporationRepository.existsById(1L)).thenReturn(true);
        when(corporationRepository.existsById(2L)).thenReturn(true);
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        when(relationshipRepository.save(any(CorporationRelationship.class))).thenReturn(new CorporationRelationship(1L, 2L));

        EveCorporation attacker = new EveCorporation();
        attacker.setId(1L);
        attacker.setCreditBalance(100000L);
        EveCorporation target = new EveCorporation();
        target.setId(2L);
        target.setCreditBalance(10000L);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(attacker));
        when(corporationRepository.findById(2L)).thenReturn(Optional.of(target));
        when(hiringRepository.findByCorporationIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(contractRepository.findByCorporationIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(relationshipRepository.findByCorporationIdAndType(anyLong(), any())).thenReturn(Collections.emptyList());
        when(proposalRepository.save(any())).thenReturn(new CorporationInteractionProposal());

        Map<String, Object> result = service.attemptHostileTakeover(1L, 2L);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void attempt_hostile_takeover_failure() {
        when(corporationRepository.existsById(1L)).thenReturn(true);
        when(corporationRepository.existsById(2L)).thenReturn(true);
        when(relationshipRepository.findByBothCorporations(1L, 2L)).thenReturn(Optional.empty());
        when(relationshipRepository.save(any(CorporationRelationship.class))).thenReturn(new CorporationRelationship(1L, 2L));

        EveCorporation attacker = new EveCorporation();
        attacker.setId(1L);
        attacker.setCreditBalance(1000L);
        EveCorporation target = new EveCorporation();
        target.setId(2L);
        target.setCreditBalance(100000L);
        when(corporationRepository.findById(1L)).thenReturn(Optional.of(attacker));
        when(corporationRepository.findById(2L)).thenReturn(Optional.of(target));
        when(hiringRepository.findByCorporationIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(contractRepository.findByCorporationIdAndStatus(anyLong(), any())).thenReturn(Collections.emptyList());
        when(relationshipRepository.findByCorporationIdAndType(anyLong(), any())).thenReturn(Collections.emptyList());
        when(proposalRepository.save(any())).thenReturn(new CorporationInteractionProposal());

        Map<String, Object> result = service.attemptHostileTakeover(1L, 2L);
        assertFalse((Boolean) result.get("success"));
    }

    // ---- Trade Agreement Benefits ----

    @Test
    void apply_trade_agreement_bonuses_no_partners() {
        when(relationshipRepository.findByCorporationIdAndType(1L, CorporationRelationship.RelationshipType.TRADE_PARTNER))
                .thenReturn(Collections.emptyList());
        long result = service.applyTradeAgreementBonuses(1L, 1000L);
        assertEquals(1000L, result);
    }

    @Test
    void apply_trade_agreement_bonuses_with_partners() {
        when(relationshipRepository.findByCorporationIdAndType(1L, CorporationRelationship.RelationshipType.TRADE_PARTNER))
                .thenReturn(Collections.singletonList(new CorporationRelationship()));
        long result = service.applyTradeAgreementBonuses(1L, 1000L);
        assertEquals(900L, result);
    }

    @Test
    void apply_trade_agreement_bonuses_capped() {
        when(relationshipRepository.findByCorporationIdAndType(1L, CorporationRelationship.RelationshipType.TRADE_PARTNER))
                .thenReturn(List.of(new CorporationRelationship(), new CorporationRelationship(),
                        new CorporationRelationship(), new CorporationRelationship(), new CorporationRelationship(),
                        new CorporationRelationship()));
        long result = service.applyTradeAgreementBonuses(1L, 1000L);
        assertEquals(500L, result); // capped at 50%
    }

    // ---- Corporation Power ----

    @Test
    void calculate_corporation_power_basic() {
        EveCorporation corp = new EveCorporation();
        corp.setId(1L);
        corp.setCreditBalance(10000L);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(Collections.emptyList());

        double power = service.calculateCorporationPower(corp);
        assertEquals(10000.0, power);
    }

    @Test
    void calculate_corporation_power_with_hires() {
        EveCorporation corp = new EveCorporation();
        corp.setId(1L);
        corp.setCreditBalance(10000L);
        CorporateHiring hire = new CorporateHiring();
        hire.setPerformanceRating(8.0);
        when(hiringRepository.findByCorporationIdAndStatus(1L, CorporateHiring.Status.ACTIVE))
                .thenReturn(Collections.singletonList(hire));
        when(contractRepository.findByCorporationIdAndStatus(1L, CorporateContract.Status.ACTIVE))
                .thenReturn(Collections.emptyList());

        double power = service.calculateCorporationPower(corp);
        assertEquals(10800.0, power); // 10000 + 1 * 8.0 * 100
    }
}

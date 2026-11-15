package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorporationInteractionProposal entity.
 */
class CorporationInteractionProposalTest {

    @Test
    void constructor_defaults() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        assertNull(proposal.getId());
        assertEquals(CorporationInteractionProposal.ProposalStatus.PENDING, proposal.getStatus());
        assertNotNull(proposal.getProposedAt());
        assertNotNull(proposal.getExpiresAt());
    }

    @Test
    void constructor_with_params() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, 1L, 2L);
        assertEquals(CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, proposal.getProposalType());
        assertEquals(1L, proposal.getProposerCorpId());
        assertEquals(2L, proposal.getTargetCorpId());
    }

    @Test
    void is_active() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.setExpiresAt(LocalDateTime.now().plusDays(1));
        assertTrue(proposal.isActive());
    }

    @Test
    void is_expired() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertTrue(proposal.isExpired());
    }

    @Test
    void not_active_when_status_not_pending() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.setStatus(CorporationInteractionProposal.ProposalStatus.ACCEPTED);
        proposal.setExpiresAt(LocalDateTime.now().plusDays(1));
        assertFalse(proposal.isActive());
    }

    @Test
    void accept_sets_status() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.accept();
        assertEquals(CorporationInteractionProposal.ProposalStatus.ACCEPTED, proposal.getStatus());
        assertNotNull(proposal.getRespondedAt());
    }

    @Test
    void reject_sets_status() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.reject();
        assertEquals(CorporationInteractionProposal.ProposalStatus.REJECTED, proposal.getStatus());
        assertNotNull(proposal.getRespondedAt());
    }

    @Test
    void expire_if_expired() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.setExpiresAt(LocalDateTime.now().minusDays(1));
        proposal.expireIfExpired();
        assertEquals(CorporationInteractionProposal.ProposalStatus.EXPIRED, proposal.getStatus());
    }

    @Test
    void expire_if_not_expired() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal();
        proposal.setExpiresAt(LocalDateTime.now().plusDays(1));
        proposal.expireIfExpired();
        assertEquals(CorporationInteractionProposal.ProposalStatus.PENDING, proposal.getStatus());
    }

    @Test
    void enum_display_names() {
        assertEquals("Trade Agreement", CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT.getDisplayName());
        assertEquals("Joint Venture", CorporationInteractionProposal.ProposalType.JOINT_VENTURE.getDisplayName());
        assertEquals("Hostile Takeover", CorporationInteractionProposal.ProposalType.HOSTILE_TAKEOVER.getDisplayName());
    }

    @Test
    void to_string() {
        CorporationInteractionProposal proposal = new CorporationInteractionProposal(
                CorporationInteractionProposal.ProposalType.TRADE_AGREEMENT, 1L, 2L);
        String str = proposal.toString();
        assertTrue(str.contains("TRADE_AGREEMENT"));
        assertTrue(str.contains("proposerCorpId=1"));
        assertTrue(str.contains("targetCorpId=2"));
    }
}

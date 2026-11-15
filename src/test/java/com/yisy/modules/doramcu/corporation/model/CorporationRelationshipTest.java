package com.yisy.modules.doramcu.corporation.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorporationRelationship entity.
 */
class CorporationRelationshipTest {

    @Test
    void constructor_defaults() {
        CorporationRelationship rel = new CorporationRelationship();
        assertNull(rel.getId());
        assertEquals(CorporationRelationship.RelationshipType.NEUTRAL, rel.getRelationshipType());
        assertEquals(0, rel.getTrustLevel());
        assertNotNull(rel.getEstablishedAt());
    }

    @Test
    void constructor_with_params() {
        CorporationRelationship rel = new CorporationRelationship(1L, 2L);
        assertEquals(1L, rel.getCorporation1Id());
        assertEquals(2L, rel.getCorporation2Id());
    }

    @Test
    void trust_level_clamped() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setTrustLevel(-10);
        assertEquals(0, rel.getTrustLevel());
        rel.setTrustLevel(150);
        assertEquals(100, rel.getTrustLevel());
    }

    @Test
    void increase_trust() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setTrustLevel(50);
        rel.increaseTrust(20);
        assertEquals(70, rel.getTrustLevel());
    }

    @Test
    void increase_trust_capped() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setTrustLevel(90);
        rel.increaseTrust(20);
        assertEquals(100, rel.getTrustLevel());
    }

    @Test
    void decrease_trust() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setTrustLevel(50);
        rel.decreaseTrust(20);
        assertEquals(30, rel.getTrustLevel());
    }

    @Test
    void decrease_trust_floored() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setTrustLevel(10);
        rel.decreaseTrust(20);
        assertEquals(0, rel.getTrustLevel());
    }

    @Test
    void is_friendly() {
        CorporationRelationship rel = new CorporationRelationship();
        assertFalse(rel.isFriendly());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.TRADE_PARTNER);
        assertTrue(rel.isFriendly());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.JOINT_VENTURE);
        assertTrue(rel.isFriendly());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.ALLY);
        assertTrue(rel.isFriendly());
    }

    @Test
    void is_hostile() {
        CorporationRelationship rel = new CorporationRelationship();
        assertFalse(rel.isHostile());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.HOSTILE);
        assertTrue(rel.isHostile());
    }

    @Test
    void can_propose_trade() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setRelationshipType(CorporationRelationship.RelationshipType.NEUTRAL);
        assertTrue(rel.canProposeTrade());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.TRADE_PARTNER);
        assertTrue(rel.canProposeTrade());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.HOSTILE);
        assertFalse(rel.canProposeTrade());
    }

    @Test
    void can_propose_joint_venture() {
        CorporationRelationship rel = new CorporationRelationship();
        rel.setRelationshipType(CorporationRelationship.RelationshipType.NEUTRAL);
        assertFalse(rel.canProposeJointVenture());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.TRADE_PARTNER);
        assertTrue(rel.canProposeJointVenture());

        rel.setRelationshipType(CorporationRelationship.RelationshipType.ALLY);
        assertTrue(rel.canProposeJointVenture());
    }

    @Test
    void enum_display_names() {
        assertEquals("Neutral", CorporationRelationship.RelationshipType.NEUTRAL.getDisplayName());
        assertEquals("Trade Partner", CorporationRelationship.RelationshipType.TRADE_PARTNER.getDisplayName());
        assertEquals("Joint Venture", CorporationRelationship.RelationshipType.JOINT_VENTURE.getDisplayName());
        assertEquals("Hostile", CorporationRelationship.RelationshipType.HOSTILE.getDisplayName());
        assertEquals("Ally", CorporationRelationship.RelationshipType.ALLY.getDisplayName());
    }

    @Test
    void to_string() {
        CorporationRelationship rel = new CorporationRelationship(1L, 2L);
        rel.setRelationshipType(CorporationRelationship.RelationshipType.TRADE_PARTNER);
        rel.setTrustLevel(50);
        String str = rel.toString();
        assertTrue(str.contains("corporation1Id=1"));
        assertTrue(str.contains("corporation2Id=2"));
        assertTrue(str.contains("TRADE_PARTNER"));
        assertTrue(str.contains("trustLevel=50"));
    }
}

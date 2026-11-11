package com.yisy.modules.doramcu.star_trek_4x.diplomacy;

import com.yisy.modules.doramcu.star_trek_4x.model.DiplomaticRelation;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.DiplomaticRelationRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Service for managing relations between factions.
 * <p>
 * Handles diplomatic relations, reputation scores, and war status.
 * </p>
 */
@Service
public class DiplomacyService {

    private static final Logger log = LoggerFactory.getLogger(DiplomacyService.class);
    private static final Random random = new Random();

    private final DiplomaticRelationRepository relationRepository;
    private final StarTrekFactionRepository factionRepository;

    public DiplomacyService(DiplomaticRelationRepository relationRepository,
                             StarTrekFactionRepository factionRepository) {
        this.relationRepository = relationRepository;
        this.factionRepository = factionRepository;
    }

    /**
     * Get or create a diplomatic relation between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return the diplomatic relation
     */
    public DiplomaticRelation getOrCreateRelation(Long faction1Id, Long faction2Id) {
        StarTrekFaction faction1 = factionRepository.findById(faction1Id)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + faction1Id));
        StarTrekFaction faction2 = factionRepository.findById(faction2Id)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + faction2Id));

        Optional<DiplomaticRelation> existing = relationRepository.findByFaction1NameAndFaction2Name(
                faction1.getName(), faction2.getName());
        if (existing.isPresent()) {
            return existing.get();
        }

        DiplomaticRelation relation = new DiplomaticRelation(faction1.getName(), faction2.getName(), faction1.getGameSession());
        relation.setRelationScore(0);
        relation.setAtWar(false);

        return relationRepository.save(relation);
    }

    /**
     * Modify the relation score between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @param delta      the change in relation score
     * @return updated relation
     */
    public DiplomaticRelation modifyRelation(Long faction1Id, Long faction2Id, int delta) {
        DiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        int newScore = Math.max(-100, Math.min(100, relation.getRelationScore() + delta));
        relation.setRelationScore(newScore);

        // Update war status
        relation.setAtWar(newScore < -50);

        // Update faction reputation
        factionRepository.findById(faction1Id).ifPresent(f1 -> {
            f1.modifyReputation(factionRepository.findById(faction2Id).map(StarTrekFaction::getName).orElse("Unknown"), delta);
            factionRepository.save(f1);
        });

        return relationRepository.save(relation);
    }

    /**
     * Declare war on another faction.
     *
     * @param attackerId the attacker faction ID
     * @param defenderId the defender faction ID
     * @return updated relation
     */
    public DiplomaticRelation declareWar(Long attackerId, Long defenderId) {
        log.info("Faction {} declares war on faction {}", attackerId, defenderId);
        DiplomaticRelation relation = getOrCreateRelation(attackerId, defenderId);
        relation.setAtWar(true);
        relation.setRelationScore(Math.min(relation.getRelationScore(), -60));
        return relationRepository.save(relation);
    }

    /**
     * Establish peace with another faction.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return updated relation
     */
    public DiplomaticRelation establishPeace(Long faction1Id, Long faction2Id) {
        log.info("Faction {} establishes peace with faction {}", faction1Id, faction2Id);
        DiplomaticRelation relation = getOrCreateRelation(faction1Id, faction2Id);
        relation.setAtWar(false);
        relation.setRelationScore(Math.max(relation.getRelationScore(), -30));
        return relationRepository.save(relation);
    }

    /**
     * Get all relations for a faction.
     *
     * @param factionId the faction ID
     * @return list of relations
     */
    public List<DiplomaticRelation> getRelations(Long factionId) {
        StarTrekFaction faction = factionRepository.findById(factionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + factionId));
        return relationRepository.findByFaction1NameOrFaction2Name(faction.getName(), faction.getName());
    }

    /**
     * Check if two factions are at war.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return true if at war
     */
    public boolean isAtWar(Long faction1Id, Long faction2Id) {
        StarTrekFaction f1 = factionRepository.findById(faction1Id).orElse(null);
        StarTrekFaction f2 = factionRepository.findById(faction2Id).orElse(null);
        if (f1 == null || f2 == null) return false;
        return relationRepository.findByFaction1NameAndFaction2Name(f1.getName(), f2.getName())
                .map(DiplomaticRelation::isAtWar)
                .orElse(false);
    }

    /**
     * Generate a random diplomatic event.
     *
     * @param factionId the faction ID
     * @return diplomatic event
     */
    public Map<String, Object> generateDiplomaticEvent(Long factionId) {
        int roll = random.nextInt(100);

        String eventType;
        String description;
        int relationChange;

        if (roll < 20) {
            eventType = "DIPLOMATIC_MEETING";
            description = "A diplomatic meeting is proposed with a neighboring faction.";
            relationChange = 10;
        } else if (roll < 40) {
            eventType = "TRADE_OFFER";
            description = "A trade offer is extended by another faction.";
            relationChange = 5;
        } else if (roll < 60) {
            eventType = "BORDER_DISPUTE";
            description = "A border dispute arises with a neighboring faction.";
            relationChange = -10;
        } else if (roll < 80) {
            eventType = "ALLIANCE_PROPOSAL";
            description = "An alliance proposal is received from a friendly faction.";
            relationChange = 15;
        } else {
            eventType = "SPIONAGE_DETECTED";
            description = "Espionage activities are detected from a hostile faction.";
            relationChange = -15;
        }

        return Map.of(
                "eventType", eventType,
                "description", description,
                "relationChange", relationChange,
                "factionId", factionId
        );
    }
}

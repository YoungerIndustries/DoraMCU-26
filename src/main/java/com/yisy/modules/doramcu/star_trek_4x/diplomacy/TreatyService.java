package com.yisy.modules.doramcu.star_trek_4x.diplomacy;

import com.yisy.modules.doramcu.star_trek_4x.model.DiplomaticRelation;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.DiplomaticRelationRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for creating and managing treaties.
 * <p>
 * Handles alliances, non-aggression pacts, and trade agreements.
 * </p>
 */
@Service
public class TreatyService {

    private static final Logger log = LoggerFactory.getLogger(TreatyService.class);

    private final DiplomaticRelationRepository relationRepository;
    private final StarTrekFactionRepository factionRepository;

    public TreatyService(DiplomaticRelationRepository relationRepository,
                          StarTrekFactionRepository factionRepository) {
        this.relationRepository = relationRepository;
        this.factionRepository = factionRepository;
    }

    /**
     * Create a treaty between two factions.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @param treatyType the treaty type
     * @return treaty result
     */
    public Map<String, Object> createTreaty(Long faction1Id, Long faction2Id, String treatyType) {
        log.info("Creating {} treaty between {} and {}", treatyType, faction1Id, faction2Id);

        StarTrekFaction faction1 = factionRepository.findById(faction1Id)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + faction1Id));
        StarTrekFaction faction2 = factionRepository.findById(faction2Id)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + faction2Id));

        DiplomaticRelation relation = relationRepository.findByFaction1NameAndFaction2Name(
                faction1.getName(), faction2.getName())
                .orElseThrow(() -> new IllegalArgumentException("No diplomatic relation found"));

        relation.addTreaty(treatyType);

        // Apply treaty effects
        int relationBonus = switch (treatyType.toUpperCase()) {
            case "ALLIANCE" -> 30;
            case "NON_AGGRESSION" -> 15;
            case "TRADE_AGREEMENT" -> 10;
            default -> 5;
        };

        relation.setRelationScore(Math.min(100, relation.getRelationScore() + relationBonus));
        relationRepository.save(relation);

        return Map.of(
                "success", true,
                "treatyType", treatyType,
                "faction1Id", faction1Id,
                "faction2Id", faction2Id,
                "relationBonus", relationBonus,
                "message", treatyType + " treaty established"
        );
    }

    /**
     * Break a treaty.
     *
     * @param faction1Id the first faction ID
     * @param faction2Id the second faction ID
     * @return result
     */
    public Map<String, Object> breakTreaty(Long faction1Id, Long faction2Id) {
        log.info("Breaking treaty between {} and {}", faction1Id, faction2Id);

        StarTrekFaction faction1 = factionRepository.findById(faction1Id)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + faction1Id));
        StarTrekFaction faction2 = factionRepository.findById(faction2Id)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + faction2Id));

        DiplomaticRelation relation = relationRepository.findByFaction1NameAndFaction2Name(
                faction1.getName(), faction2.getName())
                .orElseThrow(() -> new IllegalArgumentException("No diplomatic relation found"));

        relation.removeTreaty("ALLIANCE");
        relation.removeTreaty("NON_AGGRESSION");
        relation.removeTreaty("TRADE_AGREEMENT");
        relation.setRelationScore(Math.max(-100, relation.getRelationScore() - 20));
        relationRepository.save(relation);

        return Map.of(
                "success", true,
                "faction1Id", faction1Id,
                "faction2Id", faction2Id,
                "message", "Treaty broken"
        );
    }
}

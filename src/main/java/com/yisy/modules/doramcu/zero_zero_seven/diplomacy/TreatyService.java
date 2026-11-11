package com.yisy.modules.doramcu.zero_zero_seven.diplomacy;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondDiplomaticRelation;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondDiplomaticRelationRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for creating and managing treaties.
 * <p>
 * Handles alliance treaties, trade agreements, and non-aggression pacts.
 * </p>
 */
@Service
public class TreatyService {

    private final BondDiplomaticRelationRepository relationRepository;
    private final BondGameSessionRepository gameSessionRepository;

    public TreatyService(BondDiplomaticRelationRepository relationRepository,
                         BondGameSessionRepository gameSessionRepository) {
        this.relationRepository = relationRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Create an alliance treaty between two empires.
     *
     * @param empire1Name first empire
     * @param empire2Name second empire
     * @param gameSessionId game session
     */
    @Transactional
    public void createAlliance(String empire1Name, String empire2Name, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.addTreaty("ALLIANCE");
        relation.modifyRelation(25);
        relationRepository.save(relation);
    }

    /**
     * Create a trade agreement between two empires.
     *
     * @param empire1Name first empire
     * @param empire2Name second empire
     * @param gameSessionId game session
     */
    @Transactional
    public void createTradeAgreement(String empire1Name, String empire2Name, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.addTreaty("TRADE_AGREEMENT");
        relation.modifyRelation(15);
        relationRepository.save(relation);
    }

    /**
     * Create a non-aggression pact between two empires.
     *
     * @param empire1Name first empire
     * @param empire2Name second empire
     * @param gameSessionId game session
     */
    @Transactional
    public void createNonAggressionPact(String empire1Name, String empire2Name, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.addTreaty("NON_AGGRESSION_PACT");
        relation.modifyRelation(10);
        relationRepository.save(relation);
    }

    /**
     * Break a treaty between two empires.
     *
     * @param empire1Name first empire
     * @param empire2Name second empire
     * @param treatyType treaty type to break
     * @param gameSessionId game session
     */
    @Transactional
    public void breakTreaty(String empire1Name, String empire2Name, String treatyType, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.removeTreaty(treatyType);
        relation.modifyRelation(-20);
        relationRepository.save(relation);
    }

    private BondDiplomaticRelation getOrCreateRelation(String empire1Name, String empire2Name, Long gameSessionId) {
        return relationRepository.findByGameSessionIdAndEmpire1NameAndEmpire2Name(
                gameSessionId, empire1Name, empire2Name)
                .orElseGet(() -> {
                    BondGameSession session = gameSessionRepository.findById(gameSessionId)
                            .orElseThrow(() -> new IllegalArgumentException("Game session not found"));
                    BondDiplomaticRelation relation = new BondDiplomaticRelation(empire1Name, empire2Name, session);
                    return relationRepository.save(relation);
                });
    }
}

package com.yisy.modules.doramcu.zero_zero_seven.diplomacy;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondDiplomaticRelation;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondDiplomaticRelationRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing diplomatic relations between empires.
 * <p>
 * Handles war declarations, peace treaties, and relation score modifications.
 * </p>
 */
@Service
public class DiplomacyService {

    private final BondDiplomaticRelationRepository relationRepository;
    private final BondGameSessionRepository gameSessionRepository;

    public DiplomacyService(BondDiplomaticRelationRepository relationRepository,
                            BondGameSessionRepository gameSessionRepository) {
        this.relationRepository = relationRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Get or create a diplomatic relation between two empires.
     *
     * @param empire1Name first empire name
     * @param empire2Name second empire name
     * @param gameSessionId game session
     * @return the diplomatic relation
     */
    @Transactional
    public BondDiplomaticRelation getOrCreateRelation(String empire1Name, String empire2Name,
                                                       Long gameSessionId) {
        Optional<BondDiplomaticRelation> existing = relationRepository
                .findByGameSessionIdAndEmpire1NameAndEmpire2Name(gameSessionId, empire1Name, empire2Name);
        if (existing.isPresent()) return existing.get();

        // Check reverse order
        Optional<BondDiplomaticRelation> reverse = relationRepository
                .findByGameSessionIdAndEmpire1NameAndEmpire2Name(gameSessionId, empire2Name, empire1Name);
        if (reverse.isPresent()) return reverse.get();

        BondGameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        BondDiplomaticRelation relation = new BondDiplomaticRelation(empire1Name, empire2Name, session);
        return relationRepository.save(relation);
    }

    /**
     * Declare war on another empire.
     *
     * @param empire1Name the declaring empire
     * @param empire2Name the target empire
     * @param gameSessionId game session
     */
    @Transactional
    public void declareWar(String empire1Name, String empire2Name, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.setAtWar(true);
        relation.modifyRelation(-30);
        relationRepository.save(relation);
    }

    /**
     * Establish peace between two empires.
     *
     * @param empire1Name first empire
     * @param empire2Name second empire
     * @param gameSessionId game session
     */
    @Transactional
    public void establishPeace(String empire1Name, String empire2Name, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.setAtWar(false);
        relation.modifyRelation(20);
        relation.addTreaty("PEACE_TREATY");
        relationRepository.save(relation);
    }

    /**
     * Modify diplomatic relation.
     *
     * @param empire1Name first empire
     * @param empire2Name second empire
     * @param delta relation change
     * @param gameSessionId game session
     */
    @Transactional
    public void modifyRelation(String empire1Name, String empire2Name, int delta, Long gameSessionId) {
        BondDiplomaticRelation relation = getOrCreateRelation(empire1Name, empire2Name, gameSessionId);
        relation.modifyRelation(delta);
        relationRepository.save(relation);
    }

    /**
     * Get all relations for a game session.
     *
     * @param gameSessionId game session
     * @return list of relations
     */
    public List<BondDiplomaticRelation> getAllRelations(Long gameSessionId) {
        return relationRepository.findByGameSessionId(gameSessionId);
    }

    /**
     * Get relations for an empire.
     *
     * @param empireName the empire
     * @return list of relations
     */
    public List<BondDiplomaticRelation> getRelationsForEmpire(String empireName) {
        List<BondDiplomaticRelation> relations1 = relationRepository.findByEmpire1Name(empireName);
        List<BondDiplomaticRelation> relations2 = relationRepository.findByEmpire2Name(empireName);
        relations1.addAll(relations2);
        return relations1;
    }
}

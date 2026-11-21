package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Interface for 4X diplomacy mechanics.
 * Implemented by game-specific diplomacy services (B5, CaptainsLog, etc.).
 */
public interface FourxDiplomacyService {

    FourxDiplomaticRelation getOrCreateRelation(String faction1, String faction2);

    FourxDiplomaticRelation modifyRelation(String faction1, String faction2, int delta);

    FourxDiplomaticRelation declareWar(String faction1, String faction2);

    FourxDiplomaticRelation establishPeace(String faction1, String faction2);

    FourxDiplomaticRelation addTreaty(String faction1, String faction2, String treatyType);

    FourxDiplomaticRelation removeTreaty(String faction1, String faction2, String treatyType);

    List<FourxDiplomaticRelation> getRelations(String factionId);

    boolean isAtWar(String faction1, String faction2);

    List<FourxFaction> getAllFactions();

    FourxFaction getFaction(String factionId);

    Map<String, Object> generateDiplomaticEvent(String factionId);
}

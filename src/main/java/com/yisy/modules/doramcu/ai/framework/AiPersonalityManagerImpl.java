package com.yisy.modules.doramcu.ai.framework;
import com.yisy.modules.doramcu.ai.AiPersonality;
import com.yisy.modules.doramcu.ai.DynamicAiPersonality;
import com.yisy.modules.doramcu.ai.PlayerActionAnalyzer;
import com.yisy.modules.doramcu.ai.PlayerActionCategory;
import com.yisy.modules.doramcu.hex.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the unified personality manager.
 */
@Service
public class AiPersonalityManagerImpl implements AiPersonalityManager {

    private static final Logger log = LoggerFactory.getLogger(AiPersonalityManagerImpl.class);

    private final PlayerActionAnalyzer playerActionAnalyzer;
    private final Map<Integer, DynamicAiPersonality> dynamicPersonalities = new ConcurrentHashMap<>();
    private final Map<Integer, AiPersonality> staticPersonalities = new ConcurrentHashMap<>();

    public AiPersonalityManagerImpl(PlayerActionAnalyzer playerActionAnalyzer) {
        this.playerActionAnalyzer = playerActionAnalyzer;
    }

    @Override
    public PersonalityProfile getOrCreatePersonality(Player player) {
        int playerId = player.getId();
        DynamicAiPersonality dynamic = dynamicPersonalities.get(playerId);
        if (dynamic != null) return dynamic;
        AiPersonality staticPersonality = staticPersonalities.get(playerId);
        if (staticPersonality != null) return staticPersonality;
        AiPersonality newPersonality = AiPersonality.fromPlayer(player);
        staticPersonalities.put(playerId, newPersonality);
        return newPersonality;
    }

    @Override
    public Optional<PersonalityProfile> getPersonality(int playerId) {
        DynamicAiPersonality dynamic = dynamicPersonalities.get(playerId);
        if (dynamic != null) return Optional.of(dynamic);
        return Optional.ofNullable(staticPersonalities.get(playerId));
    }

    @Override
    public AiPersonality createStaticPersonality(Player player) {
        AiPersonality personality = AiPersonality.fromPlayer(player);
        staticPersonalities.put(player.getId(), personality);
        return personality;
    }

    @Override
    public DynamicAiPersonality createDynamicPersonality(Player player) {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);
        dynamicPersonalities.put(player.getId(), dynamic);
        return dynamic;
    }

    @Override
    public void updateFromAction(int playerId, String actionType, int turnNumber) {
        // Map string action type to PlayerActionCategory
        PlayerActionCategory category = mapToCategory(actionType);
        playerActionAnalyzer.recordAction(playerId, category, 1.0, turnNumber);
        DynamicAiPersonality dynamic = dynamicPersonalities.get(playerId);
        if (dynamic != null) dynamic.updateFromAnalyzer(playerActionAnalyzer, playerId);
    }

    private PlayerActionCategory mapToCategory(String actionType) {
        if (actionType == null) return PlayerActionCategory.COMBAT;
        try {
            return PlayerActionCategory.valueOf(actionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PlayerActionCategory.COMBAT;
        }
    }

    @Override
    public void updateFromAnalyzer(int playerId, PlayerActionAnalyzer analyzer) {
        DynamicAiPersonality dynamic = dynamicPersonalities.get(playerId);
        if (dynamic != null) dynamic.updateFromAnalyzer(analyzer, playerId);
    }

    @Override
    public PersonalityProfile getBlendedPersonality(int playerId) {
        DynamicAiPersonality dynamic = dynamicPersonalities.get(playerId);
        if (dynamic != null) return dynamic;
        return staticPersonalities.get(playerId);
    }

    @Override
    public double getConfidenceScore(int playerId) {
        DynamicAiPersonality dynamic = dynamicPersonalities.get(playerId);
        return dynamic != null ? dynamic.getConfidenceScore() : 1.0;
    }

    @Override
    public Map<Integer, PersonalityProfile> getAllPersonalities(String sessionId) {
        Map<Integer, PersonalityProfile> all = new HashMap<>();
        dynamicPersonalities.forEach((id, p) -> all.put(id, p));
        staticPersonalities.forEach((id, p) -> all.putIfAbsent(id, p));
        return all;
    }

    @Override
    public void clearCache(int playerId) {
        dynamicPersonalities.remove(playerId);
        staticPersonalities.remove(playerId);
    }

    @Override
    public void clearAllCaches() {
        dynamicPersonalities.clear();
        staticPersonalities.clear();
    }

    @Override
    public int getCacheSize() {
        return dynamicPersonalities.size() + staticPersonalities.size();
    }
}

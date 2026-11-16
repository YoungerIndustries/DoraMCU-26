package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.AiDecision;
import com.yisy.modules.doramcu.ai.AiDecisionContext;
import com.yisy.modules.doramcu.ai.EmpireAiCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of the unified AI decision engine.
 */
@Service
public class AiDecisionEngineImpl implements AiDecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(AiDecisionEngineImpl.class);

    private final Map<Integer, EmpireAiCore> aiCoreMap = new HashMap<>();

    public void registerAiCore(int playerId, EmpireAiCore aiCore) {
        aiCoreMap.put(playerId, aiCore);
    }

    public void removeAiCore(int playerId) {
        aiCoreMap.remove(playerId);
    }

    @Override
    public AiDecision decide(AiDecisionContext context) {
        if (context == null || context.getPlayer() == null) {
            return AiDecision.wait("No player context");
        }
        EmpireAiCore aiCore = aiCoreMap.get(context.getPlayer().getId());
        if (aiCore == null) {
            return AiDecision.wait("No AI core registered");
        }
        return aiCore.decide(context);
    }

    @Override
    public double evaluateAction(AiDecision.ActionType actionType, AiDecisionContext context) {
        if (context == null || context.getPlayer() == null) return 0.0;
        EmpireAiCore aiCore = aiCoreMap.get(context.getPlayer().getId());
        return aiCore != null ? aiCore.evaluateAction(actionType, context) : 0.0;
    }

    @Override
    public List<AiDecision.ActionType> getAvailableActions(AiDecisionContext context) {
        return context != null ? Arrays.asList(AiDecision.ActionType.values()) : Collections.emptyList();
    }

    @Override
    public Map<AiDecision.ActionType, Double> getAllActionScores(AiDecisionContext context) {
        Map<AiDecision.ActionType, Double> scores = new HashMap<>();
        for (AiDecision.ActionType actionType : getAvailableActions(context)) {
            scores.put(actionType, evaluateAction(actionType, context));
        }
        return scores;
    }

    @Override
    public DecisionDomain getDomain() { return DecisionDomain.STRATEGY; }

    public int getAiCoreCount() { return aiCoreMap.size(); }
    public Optional<EmpireAiCore> getAiCore(int playerId) { return Optional.ofNullable(aiCoreMap.get(playerId)); }
}

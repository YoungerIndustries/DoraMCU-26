package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.ai.AiDecision;
import com.yisy.modules.doramcu.ai.AiDecisionContext;

import java.util.List;
import java.util.Map;

/**
 * Unified AI decision engine that provides a common decision-making pipeline
 * for all AI domains (strategy, diplomacy, combat).
 * <p>
 * The decision engine evaluates the current game state and returns an AI decision
 * based on personality, context, and available options. It supports both rule-based
 * and neural network-based decision making.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * // Evaluate a single action
 * double score = engine.evaluateAction(actionType, context);
 *
 * // Make a decision
 * AiDecision decision = engine.decide(context);
 *
 * // Get available actions
 * List&lt;AiDecision.ActionType&gt; actions = engine.getAvailableActions(context);
 * </pre>
 * </p>
 */
public interface AiDecisionEngine {

    /**
     * Makes a decision based on the current game state.
     * <p>
     * This is the main entry point for AI decision making. It evaluates all
     * available actions and returns the best one based on personality and context.
     * </p>
     *
     * @param context the current decision context
     * @return the AI's chosen decision
     */
    AiDecision decide(AiDecisionContext context);

    /**
     * Evaluates how desirable a given action is for this AI's personality.
     *
     * @param actionType the action to evaluate
     * @param context the current context
     * @return a score from 0.0 (undesirable) to 1.0 (highly desirable)
     */
    double evaluateAction(AiDecision.ActionType actionType, AiDecisionContext context);

    /**
     * Returns all available actions for the current context.
     *
     * @param context the current decision context
     * @return list of available action types
     */
    List<AiDecision.ActionType> getAvailableActions(AiDecisionContext context);

    /**
     * Returns the scores for all available actions.
     *
     * @param context the current decision context
     * @return map of action type to score
     */
    Map<AiDecision.ActionType, Double> getAllActionScores(AiDecisionContext context);

    /**
     * Returns the decision domain (strategy, diplomacy, combat).
     *
     * @return the decision domain
     */
    DecisionDomain getDomain();

    /**
     * The domain of AI decisions.
     */
    enum DecisionDomain {
        /** Empire-level strategic decisions */
        STRATEGY,
        /** Diplomatic decisions between empires */
        DIPLOMACY,
        /** Combat and tactical decisions */
        COMBAT,
        /** Economic and resource management decisions */
        ECONOMY,
        /** Research and technology decisions */
        RESEARCH,
        /** NPC and character decisions */
        NPC
    }
}

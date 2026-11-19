package com.yisy.modules.doramcu.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.model.AiAdminTrainingScenario;
import com.yisy.modules.doramcu.admin.model.AiAdminTrainingSession;
import com.yisy.modules.doramcu.admin.repository.AiAdminTrainingScenarioRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminTrainingSessionRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.ai.client.AiRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing AI admin training scenarios and sessions.
 * Enables new AI admins to practice handling incidents in a sandbox environment.
 */
@Service
public class AiAdminTrainingService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminTrainingService.class);

    private final AiAdminTrainingScenarioRepository scenarioRepository;
    private final AiAdminTrainingSessionRepository sessionRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiRestClient aiRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiAdminTrainingService(
            AiAdminTrainingScenarioRepository scenarioRepository,
            AiAdminTrainingSessionRepository sessionRepository,
            AiCharacterRepository aiCharacterRepository,
            AiRestClient aiRestClient) {
        this.scenarioRepository = scenarioRepository;
        this.sessionRepository = sessionRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.aiRestClient = aiRestClient;
    }

    /**
     * Enroll an AI admin in a training scenario.
     */
    @Transactional
    public Optional<AiAdminTrainingSession> enrollAdmin(Long adminId, Long scenarioId) {
        // Validate admin exists and is an AI admin
        Optional<AiCharacter> adminOpt = aiCharacterRepository.findById(adminId);
        if (adminOpt.isEmpty() || !Boolean.TRUE.equals(adminOpt.get().isAiAdmin())) {
            log.warn("Cannot enroll non-admin character: {}", adminId);
            return Optional.empty();
        }

        // Validate scenario exists and is active
        Optional<AiAdminTrainingScenario> scenarioOpt = scenarioRepository.findById(scenarioId);
        if (scenarioOpt.isEmpty() || !Boolean.TRUE.equals(scenarioOpt.get().getIsActive())) {
            log.warn("Scenario not found or inactive: {}", scenarioId);
            return Optional.empty();
        }

        // Check if already enrolled in an active session
        List<AiAdminTrainingSession> activeSessions = sessionRepository.findByAiAdminIdAndStatus(
                adminId, AiAdminTrainingSession.Status.IN_PROGRESS);
        if (!activeSessions.isEmpty()) {
            log.warn("Admin {} already has an active training session", adminId);
            return Optional.empty();
        }

        // Create new session
        AiAdminTrainingSession session = new AiAdminTrainingSession();
        session.setAiAdminId(adminId);
        session.setScenarioId(scenarioId);
        session.setStatus(AiAdminTrainingSession.Status.NOT_STARTED);
        session.setScore(0);
        session.setMaxScore(scenarioOpt.get().getXpReward() * 2);
        session.setActionsTaken("[]");
        return Optional.of(sessionRepository.save(session));
    }

    /**
     * Start a training session — generates the simulated incident.
     */
    @Transactional
    public Optional<AiAdminTrainingSession> startSession(Long sessionId) {
        Optional<AiAdminTrainingSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        AiAdminTrainingSession session = sessionOpt.get();
        if (session.getStatus() != AiAdminTrainingSession.Status.NOT_STARTED) {
            log.warn("Session {} is not in NOT_STARTED state", sessionId);
            return Optional.empty();
        }

        session.setStatus(AiAdminTrainingSession.Status.IN_PROGRESS);
        session.setStartTime(LocalDateTime.now());
        return Optional.of(sessionRepository.save(session));
    }

    /**
     * Submit an action during training. The action is scored by the LLM.
     */
    @Transactional
    public Optional<AiAdminTrainingSession> submitAction(Long sessionId, String action, String reasoning) {
        Optional<AiAdminTrainingSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        AiAdminTrainingSession session = sessionOpt.get();
        if (session.getStatus() != AiAdminTrainingSession.Status.IN_PROGRESS) {
            log.warn("Session {} is not in progress", sessionId);
            return Optional.empty();
        }

        // Score the action using LLM
        int actionScore = scoreActionWithLlm(session, action, reasoning);

        // Update session score
        session.setScore(Math.min(session.getMaxScore(), session.getScore() + actionScore));

        // Log the action
        try {
            ArrayNode actions = (ArrayNode) objectMapper.readTree(session.getActionsTaken() != null ? session.getActionsTaken() : "[]");
            ObjectNode actionNode = objectMapper.createObjectNode();
            actionNode.put("action", action);
            actionNode.put("reasoning", reasoning);
            actionNode.put("score", actionScore);
            actionNode.put("timestamp", LocalDateTime.now().toString());
            actions.add(actionNode);
            session.setActionsTaken(objectMapper.writeValueAsString(actions));
        } catch (Exception e) {
            log.error("Failed to log action: {}", e.getMessage());
        }

        return Optional.of(sessionRepository.save(session));
    }

    /**
     * Complete a training session — calculates final score and generates LLM feedback.
     */
    @Transactional
    public Optional<AiAdminTrainingSession> completeSession(Long sessionId) {
        Optional<AiAdminTrainingSession> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        AiAdminTrainingSession session = sessionOpt.get();
        if (session.getStatus() != AiAdminTrainingSession.Status.IN_PROGRESS) {
            log.warn("Session {} is not in progress", sessionId);
            return Optional.empty();
        }

        session.setStatus(AiAdminTrainingSession.Status.COMPLETED);
        session.setEndTime(LocalDateTime.now());

        // Generate LLM feedback
        String feedback = generateFeedback(session);
        session.setFeedback(feedback);

        session = sessionRepository.save(session);

        // Award XP to the admin
        awardTrainingXp(session);

        return Optional.of(session);
    }

    /**
     * Get training progress for an admin.
     */
    @Transactional(readOnly = true)
    public List<AiAdminTrainingSession> getTrainingProgress(Long adminId) {
        return sessionRepository.findByAiAdminIdOrderByCreatedAtDesc(adminId);
    }

    /**
     * Get available training scenarios.
     */
    @Transactional(readOnly = true)
    public List<AiAdminTrainingScenario> getAvailableScenarios() {
        return scenarioRepository.findByIsActiveTrue();
    }

    /**
     * Get available scenarios by difficulty.
     */
    @Transactional(readOnly = true)
    public List<AiAdminTrainingScenario> getScenariosByDifficulty(AiAdminTrainingScenario.Difficulty difficulty) {
        return scenarioRepository.findByIsActiveTrueAndDifficulty(difficulty);
    }

    /**
     * Get a specific training session.
     */
    @Transactional(readOnly = true)
    public Optional<AiAdminTrainingSession> getSession(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }

    /**
     * Get LLM feedback for a completed session.
     */
    @Transactional(readOnly = true)
    public Optional<String> getFeedback(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> {
                    if (session.getStatus() == AiAdminTrainingSession.Status.COMPLETED && session.getFeedback() != null) {
                        return session.getFeedback();
                    }
                    return null;
                });
    }

    /**
     * Score an action using the LLM.
     */
    private int scoreActionWithLlm(AiAdminTrainingSession session, String action, String reasoning) {
        try {
            Optional<AiAdminTrainingScenario> scenarioOpt = scenarioRepository.findById(session.getScenarioId());
            if (scenarioOpt.isEmpty()) {
                return 0;
            }

            AiAdminTrainingScenario scenario = scenarioOpt.get();
            String systemPrompt = buildScoringSystemPrompt(scenario);
            String userMessage = buildScoringUserMessage(action, reasoning, scenario.getScenarioData());

            String response = aiRestClient.chat(userMessage, systemPrompt, null);
            if (response != null) {
                // Parse score from LLM response (expect JSON with "score" field)
                try {
                    JsonNode json = objectMapper.readTree(response);
                    if (json.has("score")) {
                        return json.get("score").asInt(0);
                    }
                } catch (Exception e) {
                    // Fallback: return a default score
                    log.debug("Failed to parse LLM score response: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("LLM scoring failed, using default score: {}", e.getMessage());
        }
        // Default score when LLM is unavailable
        return 5;
    }

    /**
     * Generate feedback for a completed training session.
     */
    private String generateFeedback(AiAdminTrainingSession session) {
        try {
            Optional<AiAdminTrainingScenario> scenarioOpt = scenarioRepository.findById(session.getScenarioId());
            if (scenarioOpt.isEmpty()) {
                return "Training scenario not found.";
            }

            AiAdminTrainingScenario scenario = scenarioOpt.get();
            String systemPrompt = "You are an AI admin training evaluator. Provide constructive feedback on the trainee's performance.";
            String userMessage = String.format(
                    "Scenario: %s (%s - %s)\n" +
                    "Trainee score: %d/%d\n" +
                    "Actions taken: %s\n" +
                    "Provide feedback on what went well and what could be improved.",
                    scenario.getName(),
                    scenario.getDifficulty(),
                    scenario.getDescription(),
                    session.getScore(),
                    session.getMaxScore(),
                    session.getActionsTaken()
            );

            String response = aiRestClient.chat(userMessage, systemPrompt, null);
            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("LLM feedback generation failed: {}", e.getMessage());
        }
        return "Training completed. Score: " + session.getScore() + "/" + session.getMaxScore() +
                ". Review your actions to improve.";
    }

    /**
     * Build system prompt for action scoring.
     */
    private String buildScoringSystemPrompt(AiAdminTrainingScenario scenario) {
        return String.format(
                "You are an AI admin training evaluator. " +
                "The trainee is practicing a %s %s incident scenario (%s difficulty). " +
                "Score their action from 0-20 based on correctness, reasoning, and timeliness. " +
                "Return JSON: {\"score\": <0-20>, \"reasoning\": \"<brief explanation>\"}",
                scenario.getIncidentType(),
                scenario.getSeverity(),
                scenario.getDifficulty()
        );
    }

    /**
     * Build user message for action scoring.
     */
    private String buildScoringUserMessage(String action, String reasoning, String scenarioData) {
        return String.format(
                "Scenario context: %s\n\n" +
                "Trainee action: %s\n" +
                "Trainee reasoning: %s\n\n" +
                "Score this action (0-20) and provide brief reasoning.",
                scenarioData != null ? scenarioData.substring(0, Math.min(500, scenarioData.length())) : "N/A",
                action,
                reasoning
        );
    }

    /**
     * Award XP to the admin for completing training.
     */
    private void awardTrainingXp(AiAdminTrainingSession session) {
        try {
            Optional<AiCharacter> adminOpt = aiCharacterRepository.findById(session.getAiAdminId());
            Optional<AiAdminTrainingScenario> scenarioOpt = scenarioRepository.findById(session.getScenarioId());

            if (adminOpt.isPresent() && scenarioOpt.isPresent()) {
                int xpEarned = (int) ((session.getScore() * 1.0 / session.getMaxScore()) * scenarioOpt.get().getXpReward());
                adminOpt.get().addExperience(xpEarned);
                aiCharacterRepository.save(adminOpt.get());
                log.info("Awarded {} XP to admin {} for completing training session {}",
                        xpEarned, session.getAiAdminId(), session.getId());
            }
        } catch (Exception e) {
            log.error("Failed to award training XP: {}", e.getMessage());
        }
    }
}

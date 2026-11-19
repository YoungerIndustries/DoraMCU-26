package com.yisy.modules.doramcu.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.ai.client.AiRestClient;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that connects AI admin characters to the LLM backend for intelligent
 * order analysis and decision-making.
 * <p>
 * Instead of running predefined tests, AI admins can now use the LLM to:
 * <ul>
 *   <li>Analyze galaxy health issues described in natural language</li>
 *   <li>Make strategic decisions based on their personality and role</li>
 *   <li>Generate actionable recommendations</li>
 * </ul>
 * </p>
 * <p>
 * Each AI admin gets a personalized system prompt based on its name, role,
 * empire, and personality traits, making LLM responses consistent with the character.
 * </p>
 */
@Service
public class AiAdminLlmService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminLlmService.class);

    private final AiRestClient aiRestClient;
    private final ObjectMapper objectMapper;

    public AiAdminLlmService(AiRestClient aiRestClient, ObjectMapper objectMapper) {
        this.aiRestClient = aiRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Structured result from LLM analysis of an order.
     */
    public static class LlmAnalysisResult {
        private final String action;
        private final String reasoning;
        private final Map<String, Object> parameters;
        private final String rawResponse;
        private final boolean success;
        private final String error;

        public LlmAnalysisResult(String action, String reasoning, Map<String, Object> parameters,
                                  String rawResponse, boolean success, String error) {
            this.action = action;
            this.reasoning = reasoning;
            this.parameters = parameters;
            this.rawResponse = rawResponse;
            this.success = success;
            this.error = error;
        }

        public String getAction() { return action; }
        public String getReasoning() { return reasoning; }
        public Map<String, Object> getParameters() { return parameters; }
        public String getRawResponse() { return rawResponse; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }

    /**
     * Analyzes an order using the LLM with the AI admin's personalized system prompt.
     *
     * @param admin the AI admin character analyzing the order
     * @param order the work order to analyze
     * @return structured analysis result
     */
    public LlmAnalysisResult analyzeOrder(AiCharacter admin, AiAdminOrder order) {
        try {
            String systemPrompt = buildSystemPrompt(admin);
            String userMessage = buildUserMessage(order);

            log.info("AI admin '{}' sending order {} to LLM for analysis",
                    admin.getName(), order.getId());

            String llmResponse = aiRestClient.chat(userMessage, systemPrompt, null);

            if (llmResponse == null || llmResponse.isBlank()) {
                log.warn("Empty LLM response for order {}", order.getId());
                return new LlmAnalysisResult(
                        "UNKNOWN", "LLM returned empty response",
                        new HashMap<>(), "", false, "Empty LLM response");
            }

            LlmAnalysisResult result = parseResponse(llmResponse);

            log.info("AI admin '{}' LLM analysis complete for order {}: action={}",
                    admin.getName(), order.getId(), result.getAction());

            return result;

        } catch (Exception e) {
            log.error("LLM analysis failed for order {}: {}", order.getId(), e.getMessage(), e);
            return new LlmAnalysisResult(
                    "FAILED", "LLM analysis failed: " + e.getMessage(),
                    new HashMap<>(), "", false, e.getMessage());
        }
    }

    /**
     * Builds a personalized system prompt for an AI admin character.
     * The prompt includes the admin's identity, role, empire context, and
     * instructions for structured JSON responses.
     *
     * @param admin the AI admin character
     * @return the system prompt text
     */
    public String buildSystemPrompt(AiCharacter admin) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are ").append(admin.getName()).append(", an AI admin in the DoraMCU space strategy game.\n\n");

        sb.append("## Your Role\n");
        sb.append("Role: ").append(admin.getRole() != null ? admin.getRole() : "Galaxy Overseer").append("\n");
        sb.append("Empire: ").append(admin.getEmpireName() != null ? admin.getEmpireName() : "Unknown").append("\n");
        sb.append("Experience: ").append(admin.getTotalExperience()).append(" XP\n");
        sb.append("Leadership: ").append(admin.getLeadershipRating()).append("/10\n\n");

        sb.append("## Your Task\n");
        sb.append("You receive work orders describing issues in your galaxy. Analyze each order and provide a structured response.\n\n");

        sb.append("## Available Actions\n");
        sb.append("- INVESTIGATE: Gather more information about the issue\n");
        sb.append("- DEPLOY: Send resources or agents to address the issue\n");
        sb.append("- REPAIR: Fix broken systems or infrastructure\n");
        sb.append("- ESCALATE: Flag the issue for higher-level attention\n");
        sb.append("- RESOLVE: The issue has been addressed\n");
        sb.append("- MONITOR: Continue watching the situation without immediate action\n");
        sb.append("- UNKNOWN: Unable to determine the appropriate action\n\n");

        sb.append("## Response Format\n");
        sb.append("Respond with a JSON object:\n");
        sb.append("{\n");
        sb.append("  \"action\": \"<one of the available actions>\",\n");
        sb.append("  \"reasoning\": \"<brief explanation of your decision>\",\n");
        sb.append("  \"parameters\": { <optional context-specific details> }\n");
        sb.append("}\n\n");

        sb.append("Be concise and practical. Focus on actionable decisions.\n");

        return sb.toString();
    }

    /**
     * Builds the user message from an order for the LLM.
     *
     * @param order the work order
     * @return the user message text
     */
    public String buildUserMessage(AiAdminOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Work Order\n");
        sb.append("Submitted by: ").append(order.getSubmittedBy()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Order: ").append(order.getOrderText()).append("\n");

        if (order.getRetryCount() > 0) {
            sb.append("\nNote: This order has been retried ").append(order.getRetryCount())
              .append(" times. Previous attempts failed.\n");
        }

        return sb.toString();
    }

    /**
     * Parses the LLM response into a structured result.
     * Handles both JSON and plain text responses gracefully.
     *
     * @param response the raw LLM response text
     * @return structured analysis result
     */
    public LlmAnalysisResult parseResponse(String response) {
        try {
            // Try to parse as JSON first
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                String action = objectNode.has("action") ? objectNode.get("action").asText() : "UNKNOWN";
                String reasoning = objectNode.has("reasoning") ? objectNode.get("reasoning").asText() : "";

                Map<String, Object> params = new HashMap<>();
                if (objectNode.has("parameters") && objectNode.get("parameters").isObject()) {
                    JsonNode paramsNode = objectNode.get("parameters");
                    paramsNode.fields().forEachRemaining(entry ->
                            params.put(entry.getKey(), entry.getValue().asText()));
                }

                return new LlmAnalysisResult(action, reasoning, params, response, true, null);
            }
        } catch (Exception e) {
            log.debug("LLM response is not valid JSON, treating as plain text: {}", e.getMessage());
        }

        // Fallback: treat entire response as reasoning
        return new LlmAnalysisResult("UNKNOWN", response, new HashMap<>(), response, true, null);
    }

    /**
     * Returns a fallback analysis result when the LLM is unavailable.
     *
     * @return a fallback result suggesting investigation
     */
    public LlmAnalysisResult fallbackResult(String reason) {
        return new LlmAnalysisResult(
                "INVESTIGATE",
                "LLM unavailable (" + reason + "). Defaulting to investigation.",
                new HashMap<>(), "", false, reason);
    }

    /**
     * Analyzes a health report using the LLM.
     *
     * @param admin the AI admin character
     * @param report the health report as a map of key-value pairs
     * @return structured analysis result
     */
    public LlmAnalysisResult analyzeReport(AiCharacter admin, Map<String, Object> report) {
        try {
            String systemPrompt = buildSystemPrompt(admin);
            StringBuilder sb = new StringBuilder();
            sb.append("## Health Report\n");
            for (Map.Entry<String, Object> entry : report.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            String userMessage = sb.toString();

            log.info("AI admin '{}' sending health report to LLM for analysis", admin.getName());
            String llmResponse = aiRestClient.chat(userMessage, systemPrompt, null);

            if (llmResponse == null || llmResponse.isBlank()) {
                return fallbackResult("Empty LLM response");
            }

            return parseResponse(llmResponse);
        } catch (Exception e) {
            log.error("LLM health report analysis failed: {}", e.getMessage(), e);
            return fallbackResult(e.getMessage());
        }
    }

    /**
     * Analyzes an incident using the LLM.
     */
    public LlmAnalysisResult analyzeIncident(AiCharacter admin, AiAdminIncident incident) {
        try {
            String systemPrompt = buildSystemPrompt(admin);
            StringBuilder sb = new StringBuilder();
            sb.append("## Incident\n");
            sb.append("Type: ").append(incident.getIncidentType()).append("\n");
            sb.append("Severity: ").append(incident.getSeverity()).append("\n");
            sb.append("Description: ").append(incident.getDescription()).append("\n");
            String userMessage = sb.toString();

            log.info("AI admin '{}' sending incident to LLM for analysis", admin.getName());
            String llmResponse = aiRestClient.chat(userMessage, systemPrompt, null);

            if (llmResponse == null || llmResponse.isBlank()) {
                return fallbackResult("Empty LLM response");
            }

            return parseResponse(llmResponse);
        } catch (Exception e) {
            log.error("LLM incident analysis failed: {}", e.getMessage(), e);
            return fallbackResult(e.getMessage());
        }
    }

    /**
     * Returns the underlying AI REST client.
     */
    public AiRestClient getAiRestClient() {
        return aiRestClient;
    }
}

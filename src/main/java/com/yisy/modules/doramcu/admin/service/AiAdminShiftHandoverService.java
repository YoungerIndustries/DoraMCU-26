package com.yisy.modules.doramcu.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.model.AiAdminShiftHandover;
import com.yisy.modules.doramcu.admin.repository.AiAdminIncidentRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.admin.repository.AiAdminShiftHandoverRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing shift handovers between AI admins.
 * When an outgoing admin's shift ends, a handover record is created
 * briefing the incoming admin on pending work (incidents, orders, notes).
 * The LLM generates a concise summary of the pending work.
 */
@Service
public class AiAdminShiftHandoverService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminShiftHandoverService.class);

    private final AiAdminShiftHandoverRepository handoverRepository;
    private final AiAdminIncidentRepository incidentRepository;
    private final AiAdminOrderRepository orderRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiAdminLlmService llmService;
    private final ObjectMapper objectMapper;

    public AiAdminShiftHandoverService(AiAdminShiftHandoverRepository handoverRepository,
                                        AiAdminIncidentRepository incidentRepository,
                                        AiAdminOrderRepository orderRepository,
                                        AiCharacterRepository aiCharacterRepository,
                                        AiAdminLlmService llmService,
                                        ObjectMapper objectMapper) {
        this.handoverRepository = handoverRepository;
        this.incidentRepository = incidentRepository;
        this.orderRepository = orderRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a handover between outgoing and incoming admins.
     * Gathers pending work and generates an LLM summary.
     *
     * @param outgoingAdminId the admin whose shift is ending
     * @param incomingAdminId the admin whose shift is beginning
     * @param galaxyName the galaxy where the handover takes place
     * @param handoverType AUTO or MANUAL
     * @return the created handover record
     */
    public AiAdminShiftHandover createHandover(Long outgoingAdminId, Long incomingAdminId,
                                                String galaxyName, AiAdminShiftHandover.HandoverType handoverType) {
        AiCharacter outgoingAdmin = aiCharacterRepository.findById(outgoingAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Outgoing admin not found: " + outgoingAdminId));
        AiCharacter incomingAdmin = aiCharacterRepository.findById(incomingAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Incoming admin not found: " + incomingAdminId));

        // Gather pending work
        List<AiAdminIncident> openIncidents = incidentRepository.findByStatusAndGalaxyName(
                AiAdminIncident.Status.OPEN, galaxyName);
        List<AiAdminIncident> investigatingIncidents = incidentRepository.findByStatusAndGalaxyName(
                AiAdminIncident.Status.INVESTIGATING, galaxyName);
        List<AiAdminOrder> runningOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.RUNNING);
        List<AiAdminOrder> pendingOrders = orderRepository.findByStatus(AiAdminOrder.OrderStatus.PENDING);

        // Build JSON lists of pending items
        List<Map<String, Object>> incidentList = new ArrayList<>();
        for (AiAdminIncident inc : openIncidents) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inc.getId());
            item.put("title", inc.getTitle());
            item.put("severity", inc.getSeverity().name());
            item.put("status", "OPEN");
            incidentList.add(item);
        }
        for (AiAdminIncident inc : investigatingIncidents) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inc.getId());
            item.put("title", inc.getTitle());
            item.put("severity", inc.getSeverity().name());
            item.put("status", "INVESTIGATING");
            incidentList.add(item);
        }

        List<Map<String, Object>> orderList = new ArrayList<>();
        for (AiAdminOrder order : runningOrders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("text", order.getOrderText());
            item.put("status", "RUNNING");
            orderList.add(item);
        }
        for (AiAdminOrder order : pendingOrders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("text", order.getOrderText());
            item.put("status", "PENDING");
            orderList.add(item);
        }

        String pendingIncidentsJson = toJson(incidentList);
        String pendingOrdersJson = toJson(orderList);

        // Generate LLM summary
        String summary = generateHandoverSummary(outgoingAdmin, incomingAdmin, galaxyName,
                incidentList, orderList);

        // Create handover record
        AiAdminShiftHandover handover = new AiAdminShiftHandover(
                outgoingAdminId, incomingAdminId, galaxyName, handoverType);
        handover.setPendingIncidents(pendingIncidentsJson);
        handover.setPendingOrders(pendingOrdersJson);
        handover.setSummary(summary);

        handover = handoverRepository.save(handover);

        log.info("Handover created: outgoing={}, incoming={}, galaxy={}, incidents={}, orders={}",
                outgoingAdmin.getName(), incomingAdmin.getName(), galaxyName,
                incidentList.size(), orderList.size());

        return handover;
    }

    /**
     * Generate an LLM summary of pending work for the handover.
     */
    private String generateHandoverSummary(AiCharacter outgoingAdmin, AiCharacter incomingAdmin,
                                            String galaxyName,
                                            List<Map<String, Object>> incidents,
                                            List<Map<String, Object>> orders) {
        try {
            String systemPrompt = buildHandoverSystemPrompt(outgoingAdmin, incomingAdmin);
            String userMessage = buildHandoverUserMessage(galaxyName, incidents, orders);

            log.info("Generating LLM handover summary for galaxy {}", galaxyName);

            String llmResponse = llmService.getAiRestClient().chat(userMessage, systemPrompt, null);

            if (llmResponse != null && !llmResponse.isBlank()) {
                var result = llmService.parseResponse(llmResponse);
                return result.getReasoning().isEmpty() ? llmResponse : result.getReasoning();
            }

            return buildFallbackSummary(outgoingAdmin, incomingAdmin, galaxyName, incidents, orders);

        } catch (Exception e) {
            log.warn("LLM handover summary failed: {}", e.getMessage());
            return buildFallbackSummary(outgoingAdmin, incomingAdmin, galaxyName, incidents, orders);
        }
    }

    /**
     * Builds a system prompt for handover summary generation.
     */
    public String buildHandoverSystemPrompt(AiCharacter outgoingAdmin, AiCharacter incomingAdmin) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are generating a shift handover briefing in the DoraMCU space strategy game.\n\n");

        sb.append("## Context\n");
        sb.append("Outgoing admin: ").append(outgoingAdmin.getName())
          .append(" (").append(outgoingAdmin.getRole() != null ? outgoingAdmin.getRole() : "Galaxy Overseer").append(")\n");
        sb.append("Incoming admin: ").append(incomingAdmin.getName())
          .append(" (").append(incomingAdmin.getRole() != null ? incomingAdmin.getRole() : "Galaxy Overseer").append(")\n\n");

        sb.append("## Your Task\n");
        sb.append("Summarize the pending work for the incoming admin. Be concise and highlight critical items first.\n\n");

        sb.append("## Response Format\n");
        sb.append("Respond with a JSON object:\n");
        sb.append("{\n");
        sb.append("  \"action\": \"BRIEFING\",\n");
        sb.append("  \"reasoning\": \"<your concise handover summary>\",\n");
        sb.append("  \"parameters\": { <optional details> }\n");
        sb.append("}\n\n");

        sb.append("Focus on: critical incidents, running orders, and any urgent actions needed.\n");

        return sb.toString();
    }

    /**
     * Builds the user message for handover summary generation.
     */
    public String buildHandoverUserMessage(String galaxyName,
                                            List<Map<String, Object>> incidents,
                                            List<Map<String, Object>> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Galaxy: ").append(galaxyName).append("\n\n");

        sb.append("## Open Incidents (").append(incidents.size()).append(")\n");
        if (incidents.isEmpty()) {
            sb.append("No open incidents.\n");
        } else {
            for (Map<String, Object> inc : incidents) {
                sb.append("- [").append(inc.get("severity"))
                  .append("] ").append(inc.get("title"))
                  .append(" (Status: ").append(inc.get("status")).append(")\n");
            }
        }

        sb.append("\n## Pending/Running Orders (").append(orders.size()).append(")\n");
        if (orders.isEmpty()) {
            sb.append("No pending or running orders.\n");
        } else {
            for (Map<String, Object> order : orders) {
                sb.append("- [").append(order.get("status"))
                  .append("] ").append(order.get("text")).append("\n");
            }
        }

        sb.append("\n## Task\n");
        sb.append("Generate a concise handover briefing for the incoming admin.\n");

        return sb.toString();
    }

    /**
     * Build a fallback summary when LLM is unavailable.
     */
    private String buildFallbackSummary(AiCharacter outgoingAdmin, AiCharacter incomingAdmin,
                                         String galaxyName,
                                         List<Map<String, Object>> incidents,
                                         List<Map<String, Object>> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("Shift handover from ").append(outgoingAdmin.getName())
          .append(" to ").append(incomingAdmin.getName())
          .append(" for galaxy ").append(galaxyName).append(".\n");
        sb.append("Open incidents: ").append(incidents.size()).append(".\n");
        sb.append("Pending/running orders: ").append(orders.size()).append(".\n");
        if (!incidents.isEmpty()) {
            sb.append("Review incident details for critical items requiring immediate attention.\n");
        }
        return sb.toString();
    }

    /**
     * Acknowledge a handover by the incoming admin.
     *
     * @param handoverId the handover ID
     * @param incomingAdminId the incoming admin ID
     * @param notes optional acknowledgment notes
     * @return the updated handover
     */
    public AiAdminShiftHandover acknowledgeHandover(Long handoverId, Long incomingAdminId, String notes) {
        AiAdminShiftHandover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new IllegalArgumentException("Handover not found: " + handoverId));

        if (!handover.getIncomingAdminId().equals(incomingAdminId)) {
            throw new IllegalArgumentException("Only the incoming admin can acknowledge this handover");
        }

        if (handover.getStatus() != AiAdminShiftHandover.Status.PENDING) {
            throw new IllegalStateException("Handover is already " + handover.getStatus());
        }

        handover.setStatus(AiAdminShiftHandover.Status.ACKNOWLEDGED);
        handover.setAcknowledgedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            handover.setNotes(notes);
        }

        handover = handoverRepository.save(handover);

        log.info("Handover {} acknowledged by admin {}", handoverId, incomingAdminId);

        return handover;
    }

    /**
     * Complete a handover.
     *
     * @param handoverId the handover ID
     * @return the updated handover
     */
    public AiAdminShiftHandover completeHandover(Long handoverId) {
        AiAdminShiftHandover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new IllegalArgumentException("Handover not found: " + handoverId));

        if (handover.getStatus() == AiAdminShiftHandover.Status.COMPLETED) {
            throw new IllegalStateException("Handover is already completed");
        }

        handover.setStatus(AiAdminShiftHandover.Status.COMPLETED);
        handover.setCompletedAt(LocalDateTime.now());

        handover = handoverRepository.save(handover);

        log.info("Handover {} completed", handoverId);

        return handover;
    }

    /**
     * Get all handovers for an admin (either outgoing or incoming).
     *
     * @param adminId the admin ID
     * @return list of handovers
     */
    public List<AiAdminShiftHandover> getHandoversForAdmin(Long adminId) {
        return handoverRepository.findByOutgoingAdminIdOrIncomingAdminIdOrderByCreatedAtDesc(
                adminId, adminId);
    }

    /**
     * Get all pending handovers.
     *
     * @return list of pending handovers
     */
    public List<AiAdminShiftHandover> getPendingHandovers() {
        return handoverRepository.findByStatusOrderByCreatedAtDesc(AiAdminShiftHandover.Status.PENDING);
    }

    /**
     * Get handovers for a specific galaxy.
     *
     * @param galaxyName the galaxy name
     * @return list of handovers
     */
    public List<AiAdminShiftHandover> getHandoversForGalaxy(String galaxyName) {
        return handoverRepository.findByGalaxyNameOrderByCreatedAtDesc(galaxyName);
    }

    /**
     * Get a handover by ID.
     *
     * @param handoverId the handover ID
     * @return the handover
     */
    public AiAdminShiftHandover getHandover(Long handoverId) {
        return handoverRepository.findById(handoverId)
                .orElseThrow(() -> new IllegalArgumentException("Handover not found: " + handoverId));
    }

    /**
     * Scheduled task to detect shift transitions and create handovers.
     * Runs every 5 minutes to check for ended shifts.
     */
    @Scheduled(fixedRate = 300000)
    public void detectShiftTransitions() {
        try {
            log.debug("Checking for shift transitions...");
            // Handover creation is triggered by AiAdminScheduleService when shifts end
            // This method is a placeholder for future auto-detection logic
        } catch (Exception e) {
            log.error("Error detecting shift transitions: {}", e.getMessage(), e);
        }
    }

    /**
     * Convert a list to JSON string.
     */
    private String toJson(List<?> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("Failed to serialize to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}

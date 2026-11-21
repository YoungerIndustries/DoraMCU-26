package com.yisy.modules.doramcu.earth2026.service;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType;
import com.yisy.modules.doramcu.ai.client.AiRestClient.SynthesizeResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.earth2026.model.HqEvent;
import com.yisy.modules.doramcu.earth2026.model.HqEventType;
import com.yisy.modules.doramcu.earth2026.model.HqNotification;
import com.yisy.modules.doramcu.earth2026.model.HqNotificationType;
import com.yisy.modules.doramcu.earth2026.repository.HqEventRepository;
import com.yisy.modules.doramcu.earth2026.repository.HqNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service that manages events in the Younger Industries HQ.
 * <p>
 * Events are time-limited occurrences that affect corporations in the HQ,
 * providing rewards, challenges, and emergent gameplay.
 * </p>
 */
@Service
public class HqEventService {

    private static final Logger log = LoggerFactory.getLogger(HqEventService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HqEventRepository hqEventRepository;
    private final HqNotificationRepository notificationRepository;
    private final CorporationEconomyService economyService;
    private final Random random = new Random();

    public HqEventService(
            HqEventRepository hqEventRepository,
            HqNotificationRepository notificationRepository,
            CorporationEconomyService economyService) {
        this.hqEventRepository = hqEventRepository;
        this.notificationRepository = notificationRepository;
        this.economyService = economyService;
    }

    // ---- Event Queries ----

    /**
     * Get all active events for an HQ.
     */
    public List<Map<String, Object>> getActiveEvents(Long hqId) {
        List<HqEvent> events = hqEventRepository.findActiveEvents(hqId, LocalDateTime.now());
        return events.stream().map(this::createEventSummary).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get completed event history for an HQ.
     */
    public List<Map<String, Object>> getEventHistory(Long hqId) {
        List<HqEvent> events = hqEventRepository.findCompletedEventsDesc(hqId);
        return events.stream().map(this::createEventSummary).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get a specific event by ID.
     */
    public Map<String, Object> getEvent(Long eventId) {
        return hqEventRepository.findById(eventId)
                .map(this::createEventSummary)
                .orElse(null);
    }

    // ---- Event Participation ----

    /**
     * Participate in an active event.
     */
    @Transactional
    public Map<String, Object> participateInEvent(Long eventId, Long corporationId) {
        HqEvent event = hqEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        if (!"ACTIVE".equals(event.getStatus())) {
            throw new IllegalStateException("Event is not active: " + event.getStatus());
        }

        if (event.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Event has expired");
        }

        // Add corporation to participants
        List<Long> participants = parseParticipants(event.getParticipatingCorps());
        if (!participants.contains(corporationId)) {
            participants.add(corporationId);
            try {
                event.setParticipatingCorps(objectMapper.writeValueAsString(participants));
            } catch (Exception e) {
                log.warn("Failed to serialize participants: {}", e.getMessage());
            }
        }

        // Apply event-specific participation effects
        Map<String, Object> result = applyEventParticipation(event, corporationId);
        event.setUpdatedAt(LocalDateTime.now());
        hqEventRepository.save(event);

        // Create notification
        createNotification(corporationId, HqNotificationType.HQ_EVENT,
                "Event Participation", "You participated in " + event.getTitle(),
                event.getEndTime(), null);

        log.info("Corporation {} participated in event {} ({})", corporationId, eventId, event.getEventType().getDisplayName());
        return result;
    }

    // ---- Event Generation ----

    /**
     * Try to generate a new random event for the HQ.
     * Called by the game tick system periodically.
     */
    @Transactional
    public HqEvent tryGenerateEvent(Long hqId) {
        LocalDateTime now = LocalDateTime.now();

        // Check if there are already too many active events
        List<HqEvent> activeEvents = hqEventRepository.findActiveEvents(hqId, now);
        if (activeEvents.size() >= 3) {
            return null;
        }

        // Pick a random event type that's not on cooldown
        HqEventType[] types = HqEventType.values();
        HqEventType chosenType = null;

        for (int i = 0; i < types.length; i++) {
            HqEventType type = types[random.nextInt(types.length)];
            List<HqEvent> recent = hqEventRepository.findActiveOrScheduledByType(hqId, type);
            if (recent.isEmpty()) {
                chosenType = type;
                break;
            }
        }

        if (chosenType == null) {
            return null;
        }

        // Create the event
        HqEvent event = new HqEvent();
        event.setHqId(hqId);
        event.setEventType(chosenType);
        event.setStatus("ACTIVE");
        event.setTitle(chosenType.getDisplayName());
        event.setDescription(chosenType.getDescription());
        event.setStartTime(now);
        event.setEndTime(now.plusHours(chosenType.getDurationHours()));
        event.setLocation(randomLocation());
        event.setParticipatingCorps("[]");
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        hqEventRepository.save(event);

        // Broadcast notification to all corporations with offices in this HQ
        notifyAllCorporations(hqId, event);

        log.info("Generated HQ event: {} at HQ {}", chosenType.getDisplayName(), hqId);
        return event;
    }

    /**
     * Activate scheduled events that have started.
     */
    @Transactional
    public int activateScheduledEvents(Long hqId) {
        LocalDateTime now = LocalDateTime.now();
        List<HqEvent> toActivate = hqEventRepository.findEventsToActivate(hqId, now);
        int count = 0;
        for (HqEvent event : toActivate) {
            event.setStatus("ACTIVE");
            event.setUpdatedAt(now);
            hqEventRepository.save(event);
            count++;
        }
        if (count > 0) {
            log.info("Activated {} scheduled events for HQ {}", count, hqId);
        }
        return count;
    }

    /**
     * Complete expired events and apply rewards.
     */
    @Transactional
    public int completeExpiredEvents(Long hqId) {
        LocalDateTime now = LocalDateTime.now();
        List<HqEvent> expired = hqEventRepository.findExpiredEvents(hqId, now);
        int count = 0;
        for (HqEvent event : expired) {
            event.setStatus("COMPLETED");
            event.setUpdatedAt(now);

            // Apply completion rewards to participants
            List<Long> participants = parseParticipants(event.getParticipatingCorps());
            for (Long corpId : participants) {
                applyCompletionReward(event, corpId);
            }

            hqEventRepository.save(event);
            count++;
        }
        if (count > 0) {
            log.info("Completed {} expired events for HQ {}", count, hqId);
        }
        return count;
    }

    /**
     * Clean up old completed events.
     */
    @Transactional
    public int cleanupOldEvents(Long hqId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<HqEvent> oldEvents = hqEventRepository.findOldCompletedEvents(hqId, cutoff);
        int count = oldEvents.size();
        hqEventRepository.deleteAll(oldEvents);
        if (count > 0) {
            log.info("Cleaned up {} old events for HQ {}", count, hqId);
        }
        return count;
    }

    // ---- Event Processing (called from game tick) ----

    /**
     * Process all event-related tasks for the HQ.
     * Called by the game tick system.
     */
    @Transactional
    public void processEvents(Long hqId) {
        activateScheduledEvents(hqId);
        completeExpiredEvents(hqId);

        // Try to generate a new event (30% chance per tick)
        if (random.nextInt(100) < 30) {
            tryGenerateEvent(hqId);
        }

        // Clean up old events occasionally
        if (random.nextInt(100) < 10) {
            cleanupOldEvents(hqId);
        }
    }

    // ---- Private Helpers ----

    private Map<String, Object> createEventSummary(HqEvent event) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", event.getId());
        summary.put("eventType", event.getEventType().name());
        summary.put("eventDisplayName", event.getEventType().getDisplayName());
        summary.put("severity", event.getEventType().getSeverity());
        summary.put("status", event.getStatus());
        summary.put("title", event.getTitle());
        summary.put("description", event.getDescription());
        summary.put("startTime", event.getStartTime());
        summary.put("endTime", event.getEndTime());
        summary.put("location", event.getLocation());
        summary.put("reward", event.getEventType().getReward());
        summary.put("penalty", event.getEventType().getPenalty());
        summary.put("durationHours", event.getEventType().getDurationHours());

        // Parse participants
        List<Long> participants = parseParticipants(event.getParticipatingCorps());
        summary.put("participantCount", participants.size());
        summary.put("participants", participants);

        return summary;
    }

    private Map<String, Object> applyEventParticipation(HqEvent event, Long corporationId) {
        Map<String, Object> result = new HashMap<>();
        HqEventType type = event.getEventType();

        switch (type) {
            case TRADE_SHOW:
                result.put("credits", 500);
                result.put("influence", 20);
                result.put("message", "You set up a booth and made several sales. +500 credits, +20 influence.");
                economyService.creditCorporation(corporationId, 500L, "Trade show earnings", null);
                break;

            case SECURITY_BREACH:
                result.put("reputation", 5);
                result.put("credits", 1000);
                result.put("message", "You helped security investigate the breach. +5 reputation, +1000 credits.");
                economyService.creditCorporation(corporationId, 1000L, "Security breach reward", null);
                break;

            case POWER_OUTAGE:
                result.put("credits", 300);
                result.put("message", "You provided backup power to neighboring offices. +300 credits.");
                economyService.creditCorporation(corporationId, 300L, "Power outage assistance", null);
                break;

            case FIRE_DRILL:
                result.put("reputation", 2);
                result.put("message", "Your corporation evacuated quickly and safely. +2 reputation.");
                break;

            case VIP_VISIT:
                result.put("reputation", 10);
                result.put("influence", 50);
                result.put("message", "The VIP was impressed by your corporation. +10 reputation, +50 influence.");
                break;

            case CORPORATION_PARTY:
                result.put("morale", 15);
                result.put("message", "Your employees enjoyed the party. Morale +15%.");
                break;

            case SYSTEM_HACK:
                result.put("tech", 50);
                result.put("reputation", 5);
                result.put("message", "You helped defend the network. +50 tech, +5 reputation.");
                break;

            case RIVAL_SABOTAGE:
                result.put("reputation", 8);
                result.put("credits", 1500);
                result.put("message", "You thwarted the sabotage attempt. +8 reputation, +1500 credits.");
                economyService.creditCorporation(corporationId, 1500L, "Sabotage prevention reward", null);
                break;

            case RECRUITMENT_FAIR:
                result.put("hiringDiscount", 30);
                result.put("message", "You found talented AI characters at reduced salaries. Hiring discount 30%.");
                break;

            case TECHNOLOGY_DEMO:
                result.put("tech", 100);
                result.put("message", "You gained access to cutting-edge technology. +100 tech.");
                break;

            case INSPECTION:
                result.put("reputation", 3);
                result.put("credits", 200);
                result.put("message", "Your corporation passed inspection. +3 reputation, +200 credits (tax reduction).");
                economyService.creditCorporation(corporationId, 200L, "Inspection tax reduction", null);
                break;

            case BLACK_MARKET:
                if (random.nextInt(100) > 30) {
                    result.put("tech", 80);
                    result.put("credits", 2000);
                    result.put("message", "You acquired rare items on the black market. +80 tech, +2000 credits.");
                    economyService.creditCorporation(corporationId, 2000L, "Black market trade", null);
                } else {
                    result.put("reputationPenalty", -3);
                    result.put("message", "You were caught trading on the black market. -3 reputation.");
                }
                break;

            case PORTAL_ANOMALY:
                result.put("tech", 100);
                result.put("message", "You discovered unusual energy patterns. +100 tech.");
                break;

            case LOBBY_AUCTION:
                result.put("creditsCost", 1000);
                result.put("tech", 60);
                result.put("message", "You won an auction item. -1000 credits, +60 tech.");
                economyService.debitCorporation(corporationId, 1000L, TransactionType.EXPENSE, "Auction bid", null);
                break;

            case EMERGENCY_DRILL:
                result.put("reputation", 1);
                result.put("message", "Your corporation responded well to the emergency drill. +1 reputation.");
                break;

            default:
                result.put("message", "You participated in " + type.getDisplayName() + ".");
                break;
        }

        return result;
    }

    private void applyCompletionReward(HqEvent event, Long corporationId) {
        HqEventType type = event.getEventType();
        switch (type) {
            case TRADE_SHOW:
                economyService.creditCorporation(corporationId, 200L, "Trade show completion bonus", null);
                break;
            case SECURITY_BREACH:
                economyService.creditCorporation(corporationId, 500L, "Security breach completion bonus", null);
                break;
            case VIP_VISIT:
                economyService.creditCorporation(corporationId, 1000L, "VIP visit completion bonus", null);
                break;
            default:
                break;
        }

        createNotification(corporationId, HqNotificationType.HQ_EVENT,
                "Event Completed", event.getTitle() + " has concluded.",
                LocalDateTime.now().plusDays(7), null);
    }

    /**
     * Manually complete an active event, applying completion bonus to all participants.
     *
     * @param eventId the event ID
     * @param corporationId the requesting corporation ID
     * @return result map with completion details
     * @throws IllegalArgumentException if event not found or not active
     */
    @Transactional
    public Map<String, Object> completeEvent(Long eventId, Long corporationId) {
        Optional<HqEvent> eventOpt = hqEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        HqEvent event = eventOpt.get();
        if (!"ACTIVE".equals(event.getStatus())) {
            throw new IllegalArgumentException("Event is not active: " + event.getStatus());
        }

        // Check if the corporation is a participant
        List<Long> participants = parseParticipants(event.getParticipatingCorps());
        if (!participants.contains(corporationId)) {
            throw new IllegalArgumentException("Your corporation is not a participant in this event");
        }

        // Complete the event
        event.setStatus("COMPLETED");
        event.setCompletedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        hqEventRepository.save(event);

        // Apply completion bonus to all participants
        Map<String, Object> result = new HashMap<>();
        int bonusCount = 0;
        for (Long corpId : participants) {
            applyCompletionReward(event, corpId);
            bonusCount++;
        }

        result.put("eventId", eventId);
        result.put("eventType", event.getEventType().getDisplayName());
        result.put("title", event.getTitle());
        result.put("participantsRewarded", bonusCount);
        result.put("message", "Event completed! Completion bonus applied to " + bonusCount + " participant(s).");

        // Notify all participants
        for (Long corpId : participants) {
            createNotification(corpId, HqNotificationType.HQ_EVENT,
                    "Event Completed", event.getTitle() + " has been completed. Bonus rewards applied.",
                    LocalDateTime.now().plusDays(7), null);
        }

        log.info("Event {} completed manually by corp {}", eventId, corporationId);
        return result;
    }

    private void notifyAllCorporations(Long hqId, HqEvent event) {
        // Get all corporations with offices in this HQ
        // For now, notify via the notification system
        log.info("New HQ event: {} - {}", event.getEventType().getDisplayName(), event.getTitle());
    }

    private void createNotification(Long corporationId, HqNotificationType type, String title, String message,
                                     LocalDateTime expiresAt, String payload) {
        try {
            HqNotification notification = new HqNotification();
            notification.setCorporationId(corporationId);
            notification.setNotificationType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(7));
            notification.setPayload(payload);
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.warn("Failed to create notification for corp {}: {}", corporationId, e.getMessage());
        }
    }

    private List<Long> parseParticipants(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (Exception e) {
            log.warn("Failed to parse participants: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String randomLocation() {
        String[] locations = {"Lobby", "Floor 1", "Floor 2", "Floor 3", "Basement B1", "Basement B2", "Basement B3"};
        return locations[random.nextInt(locations.length)];
    }
}

package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OnlineStatus;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OnlineStatusRepository;
import com.yisy.modules.doramcu.earth2026.service.Earth2026ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

/**
 * WebSocket/SSE controller for Earth 2026 MMOG real-time events.
 * <p>
 * Provides Server-Sent Events for real-time updates including:
 * <ul>
 *   <li>Chat messages (public, private, party)</li>
 *   <li>Player presence (online/offline, location changes)</li>
 *   <li>Party events (created, disbanded, member changes)</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/ws/v1/earth2026")
public class Earth2026WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(Earth2026WebSocketController.class);
    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final long HEARTBEAT_INTERVAL = 15000L; // 15 seconds

    private final Earth2026ChatService chatService;
    private final Earth2026OnlineStatusRepository onlineStatusRepository;

    /**
     * Registry of active SSE emitters keyed by character ID.
     */
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(2);

    public Earth2026WebSocketController(Earth2026ChatService chatService,
                                         Earth2026OnlineStatusRepository onlineStatusRepository) {
        this.chatService = chatService;
        this.onlineStatusRepository = onlineStatusRepository;
    }

    /**
     * Connect to the Earth 2026 real-time event stream.
     *
     * @param characterId the character ID
     * @return SSE emitter for real-time events
     */
    @GetMapping(value = "/{characterId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable Long characterId) {
        log.debug("Character {} connecting to Earth 2026 SSE stream", characterId);

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        emitters.put(characterId, emitter);

        // Update online status
        onlineStatusRepository.findByCharacterId(characterId).ifPresentOrElse(
                status -> {
                    status.setOnline(true);
                    status.setLastSeenAt(LocalDateTime.now());
                    status.setConnectedAt(LocalDateTime.now());
                    status.setUpdatedAt(LocalDateTime.now());
                    onlineStatusRepository.save(status);
                },
                () -> {
                    Earth2026OnlineStatus status = new Earth2026OnlineStatus(characterId);
                    status.setOnline(true);
                    status.setLastSeenAt(LocalDateTime.now());
                    status.setConnectedAt(LocalDateTime.now());
                    onlineStatusRepository.save(status);
                }
        );

        emitter.onCompletion(() -> {
            log.debug("SSE stream completed for character {}", characterId);
            emitters.remove(characterId);
            setOffline(characterId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE stream timed out for character {}", characterId);
            emitters.remove(characterId);
            setOffline(characterId);
        });
        emitter.onError((e) -> {
            log.warn("SSE stream error for character {}", characterId, e);
            emitters.remove(characterId);
            setOffline(characterId);
        });

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("characterId", characterId, "message", "Connected to Earth 2026")));
        } catch (IOException e) {
            log.warn("Failed to send initial connection event for character {}", characterId, e);
        }

        // Start heartbeat
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            SseEmitter current = emitters.get(characterId);
            if (current != null) {
                try {
                    current.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(System.currentTimeMillis()));
                    // Update last seen
                    onlineStatusRepository.findByCharacterId(characterId).ifPresent(status -> {
                        status.setLastSeenAt(LocalDateTime.now());
                        onlineStatusRepository.save(status);
                    });
                } catch (IOException e) {
                    log.debug("Heartbeat failed for character {}: {}", characterId, e.getMessage());
                    emitters.remove(characterId);
                    setOffline(characterId);
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

        return emitter;
    }

    /**
     * Send a chat message and broadcast it to connected clients.
     *
     * @param characterId the sender character ID
     * @param request the message request
     * @return the saved message
     */
    @PostMapping("/chat/{characterId}")
    public Earth2026Chat sendMessage(@PathVariable Long characterId, @RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        String type = (String) request.getOrDefault("type", "public");

        Earth2026Chat chat;
        switch (type) {
            case "private":
                Long recipientId = Long.valueOf(request.get("recipientId").toString());
                chat = chatService.sendPrivateMessage(characterId, recipientId, content);
                break;
            case "party":
                Long partyId = Long.valueOf(request.get("partyId").toString());
                chat = chatService.sendPartyMessage(characterId, partyId, content);
                break;
            default:
                Long locationId = Long.valueOf(request.get("locationId").toString());
                chat = chatService.sendPublicMessage(characterId, locationId, content);
                break;
        }

        // Broadcast to connected clients
        broadcastEvent("chat", chat);
        return chat;
    }

    /**
     * Get the number of connected clients.
     *
     * @return number of connected clients
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
                "connectedClients", emitters.size(),
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * Broadcast an event to all connected clients.
     */
    private void broadcastEvent(String eventName, Object data) {
        emitters.forEach((characterId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.debug("Failed to broadcast to character {}: {}", characterId, e.getMessage());
                emitters.remove(characterId);
            }
        });
    }

    /**
     * Set a character's online status to offline.
     */
    private void setOffline(Long characterId) {
        onlineStatusRepository.findByCharacterId(characterId).ifPresent(status -> {
            status.setOnline(false);
            status.setLastSeenAt(LocalDateTime.now());
            status.setUpdatedAt(LocalDateTime.now());
            onlineStatusRepository.save(status);
            log.debug("Character {} set offline", characterId);
        });
    }
}

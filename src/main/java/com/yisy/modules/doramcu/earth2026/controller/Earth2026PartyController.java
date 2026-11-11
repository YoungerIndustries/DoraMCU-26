package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OnlineStatus;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Party;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OnlineStatusRepository;
import com.yisy.modules.doramcu.earth2026.service.Earth2026ChatService;
import com.yisy.modules.doramcu.earth2026.service.Earth2026PartyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Earth 2026 MMOG party and chat operations.
 * <p>
 * Provides endpoints for party management, chat messaging, and online status.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/mmog")
public class Earth2026PartyController {

    private final Earth2026PartyService partyService;
    private final Earth2026ChatService chatService;
    private final Earth2026OnlineStatusRepository onlineStatusRepository;

    public Earth2026PartyController(Earth2026PartyService partyService,
                                     Earth2026ChatService chatService,
                                     Earth2026OnlineStatusRepository onlineStatusRepository) {
        this.partyService = partyService;
        this.chatService = chatService;
        this.onlineStatusRepository = onlineStatusRepository;
    }

    // ------------------------------------------------------------------
    // Party endpoints
    // ------------------------------------------------------------------

    @GetMapping("/parties")
    public List<Earth2026Party> getAllParties() {
        return partyService.getActiveParties();
    }

    @GetMapping("/parties/recruiting")
    public List<Earth2026Party> getRecruitingParties() {
        return partyService.getRecruitingParties();
    }

    @GetMapping("/parties/{id}")
    public ResponseEntity<Earth2026Party> getParty(@PathVariable Long id) {
        return partyService.getParty(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/parties/location/{locationId}")
    public List<Earth2026Party> getPartiesAtLocation(@PathVariable Long locationId) {
        return partyService.getPartiesAtLocation(locationId);
    }

    @PostMapping("/parties")
    public ResponseEntity<?> createParty(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Party name is required"));
            }
            Long locationId = Long.valueOf(request.get("locationId").toString());
            Long leaderId = Long.valueOf(request.get("leaderId").toString());

            Earth2026Party party = partyService.createParty(name.trim(), locationId, leaderId);
            return ResponseEntity.ok(party);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/parties/{id}")
    public ResponseEntity<?> disbandParty(@PathVariable Long id) {
        return partyService.disbandParty(id)
                .map(party -> ResponseEntity.ok(Map.of("message", "Party disbanded", "party", party)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/parties/{id}/recruiting")
    public ResponseEntity<?> setRecruiting(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Boolean recruiting = (Boolean) request.get("recruiting");
        if (recruiting == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "recruiting field is required"));
        }
        return partyService.setRecruiting(id, recruiting)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/parties/{id}/scenario")
    public ResponseEntity<?> setScenario(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long scenarioId = Long.valueOf(request.get("scenarioId").toString());
        return partyService.setScenario(id, scenarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/parties/{id}/move")
    public ResponseEntity<?> moveParty(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long locationId = Long.valueOf(request.get("locationId").toString());
        return partyService.moveParty(id, locationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------
    // Chat endpoints
    // ------------------------------------------------------------------

    @GetMapping("/chat/location/{locationId}")
    public List<Earth2026Chat> getLocationChat(@PathVariable Long locationId) {
        return chatService.getLocationMessages(locationId);
    }

    @GetMapping("/chat/private/{characterId1}/{characterId2}")
    public List<Earth2026Chat> getPrivateChat(@PathVariable Long characterId1,
                                               @PathVariable Long characterId2) {
        return chatService.getPrivateMessages(characterId1, characterId2);
    }

    @GetMapping("/chat/party/{partyId}")
    public List<Earth2026Chat> getPartyChat(@PathVariable Long partyId) {
        return chatService.getPartyMessages(partyId);
    }

    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            Long senderId = Long.valueOf(request.get("senderId").toString());
            String content = (String) request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message content is required"));
            }
            String type = (String) request.getOrDefault("type", "public");

            Earth2026Chat chat;
            switch (type) {
                case "private":
                    Long recipientId = Long.valueOf(request.get("recipientId").toString());
                    chat = chatService.sendPrivateMessage(senderId, recipientId, content.trim());
                    break;
                case "party":
                    Long partyId = Long.valueOf(request.get("partyId").toString());
                    chat = chatService.sendPartyMessage(senderId, partyId, content.trim());
                    break;
                default:
                    Long locationId = Long.valueOf(request.get("locationId").toString());
                    chat = chatService.sendPublicMessage(senderId, locationId, content.trim());
                    break;
            }
            return ResponseEntity.ok(chat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------
    // Online status endpoints
    // ------------------------------------------------------------------

    @GetMapping("/online")
    public List<Earth2026OnlineStatus> getOnlineCharacters() {
        return onlineStatusRepository.findByIsOnlineTrue();
    }

    @GetMapping("/online/location/{locationId}")
    public List<Earth2026OnlineStatus> getOnlineAtLocation(@PathVariable Long locationId) {
        return onlineStatusRepository.findByLocationId(locationId);
    }

    @GetMapping("/online/{characterId}")
    public ResponseEntity<Earth2026OnlineStatus> getOnlineStatus(@PathVariable Long characterId) {
        return onlineStatusRepository.findByCharacterId(characterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/online/{characterId}/activity")
    public ResponseEntity<?> setActivity(@PathVariable Long characterId,
                                          @RequestBody Map<String, Object> request) {
        String activity = (String) request.get("activity");
        if (activity == null || activity.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Activity is required"));
        }
        Optional<Earth2026OnlineStatus> status = onlineStatusRepository.findByCharacterId(characterId);
        if (status.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Earth2026OnlineStatus s = status.get();
        s.setActivity(activity.trim());
        s.setLastSeenAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        onlineStatusRepository.save(s);
        return ResponseEntity.ok(s);
    }
}

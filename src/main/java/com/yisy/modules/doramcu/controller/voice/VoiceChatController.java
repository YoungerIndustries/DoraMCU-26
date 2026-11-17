package com.yisy.modules.doramcu.controller.voice;

import com.yisy.modules.doramcu.service.voice.VoiceChatService;
import com.yisy.modules.doramcu.service.voice.VoiceChatService.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for voice chat — WebRTC signaling for multiplayer voice communication.
 * <p>
 * Provides endpoints for creating voice rooms, joining/leaving,
 * WebRTC SDP/ICE signaling, mute control, and statistics.
 * The actual audio streams flow peer-to-peer between browsers via WebRTC.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/voice-chat}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/voice-chat")
public class VoiceChatController {

    private final VoiceChatService voiceChatService;

    public VoiceChatController(VoiceChatService voiceChatService) {
        this.voiceChatService = voiceChatService;
    }

    /**
     * Create a voice room for a game session.
     */
    @PostMapping("/rooms/{roomId}/{sessionId}")
    public ResponseEntity<?> createRoom(@PathVariable String roomId,
                                        @PathVariable String sessionId) {
        return voiceChatService.createRoom(roomId, sessionId)
                .map(room -> ResponseEntity.ok(buildRoomInfoResponse(room)))
                .orElse(ResponseEntity.badRequest().body(Map.of("error", "Room already exists: " + roomId)));
    }

    /**
     * Get or create a voice room for a session.
     */
    @GetMapping("/rooms/session/{sessionId}")
    public ResponseEntity<?> getOrCreateRoom(@PathVariable String sessionId) {
        VoiceRoom room = voiceChatService.getOrCreateRoom(sessionId);
        return ResponseEntity.ok(buildRoomInfoResponse(room));
    }

    /**
     * Get a voice room by ID.
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        return voiceChatService.getRoom(roomId)
                .map(room -> ResponseEntity.ok(buildRoomInfoResponse(room)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Join a voice room.
     */
    @PostMapping("/rooms/{roomId}/join/{participantId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId,
                                      @PathVariable String participantId) {
        if (voiceChatService.joinRoom(roomId, participantId)) {
            return ResponseEntity.ok(Map.of("message", "Joined room: " + roomId));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Room not found or full: " + roomId));
    }

    /**
     * Leave a voice room.
     */
    @PostMapping("/rooms/{roomId}/leave/{participantId}")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId,
                                       @PathVariable String participantId) {
        voiceChatService.leaveRoom(roomId, participantId);
        return ResponseEntity.ok(Map.of("message", "Left room: " + roomId));
    }

    /**
     * Get the room a participant is in.
     */
    @GetMapping("/participant/{participantId}/room")
    public ResponseEntity<?> getParticipantRoom(@PathVariable String participantId) {
        return voiceChatService.getParticipantRoom(participantId)
                .map(roomId -> ResponseEntity.ok(Map.of("participantId", participantId, "roomId", roomId)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Send an SDP offer to another participant.
     */
    @PostMapping("/rooms/{roomId}/sdp/offer")
    public ResponseEntity<?> sendSdpOffer(@PathVariable String roomId,
                                          @RequestBody Map<String, String> body) {
        String sdp = body.get("sdp");
        String from = body.get("from");
        String to = body.get("to");
        if (sdp == null || from == null || to == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing sdp, from, or to"));
        }
        voiceChatService.relaySdpOffer(roomId, SdpMessage.offer(sdp, from, to));
        return ResponseEntity.ok(Map.of("message", "SDP offer relayed"));
    }

    /**
     * Send an SDP answer to another participant.
     */
    @PostMapping("/rooms/{roomId}/sdp/answer")
    public ResponseEntity<?> sendSdpAnswer(@PathVariable String roomId,
                                           @RequestBody Map<String, String> body) {
        String sdp = body.get("sdp");
        String from = body.get("from");
        String to = body.get("to");
        if (sdp == null || from == null || to == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing sdp, from, or to"));
        }
        voiceChatService.relaySdpAnswer(roomId, SdpMessage.answer(sdp, from, to));
        return ResponseEntity.ok(Map.of("message", "SDP answer relayed"));
    }

    /**
     * Get pending SDP messages for a participant.
     */
    @GetMapping("/rooms/{roomId}/sdp/{participantId}")
    public ResponseEntity<?> getPendingSdpMessages(@PathVariable String roomId,
                                                    @PathVariable String participantId) {
        List<SdpMessage> messages = voiceChatService.getPendingSdpMessages(roomId, participantId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SdpMessage msg : messages) {
            result.add(Map.of(
                    "type", msg.getType(),
                    "sdp", msg.getSdp(),
                    "from", msg.getFromParticipant(),
                    "to", msg.getToParticipant(),
                    "timestamp", msg.getTimestamp().toString()
            ));
        }
        return ResponseEntity.ok(Map.of("messages", result, "count", result.size()));
    }

    /**
     * Send an ICE candidate to another participant.
     */
    @PostMapping("/rooms/{roomId}/ice")
    public ResponseEntity<?> sendIceCandidate(@PathVariable String roomId,
                                              @RequestBody Map<String, Object> body) {
        String candidate = (String) body.get("candidate");
        String sdpMid = (String) body.get("sdpMid");
        Integer sdpMLineIndex = (Integer) body.get("sdpMLineIndex");
        String from = (String) body.get("from");
        String to = (String) body.get("to");
        if (candidate == null || from == null || to == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing candidate, from, or to"));
        }
        voiceChatService.relayIceCandidate(roomId,
                new IceCandidateMessage(candidate, sdpMid, sdpMLineIndex != null ? sdpMLineIndex : 0, from, to));
        return ResponseEntity.ok(Map.of("message", "ICE candidate relayed"));
    }

    /**
     * Get pending ICE candidates for a participant.
     */
    @GetMapping("/rooms/{roomId}/ice/{participantId}")
    public ResponseEntity<?> getPendingIceCandidates(@PathVariable String roomId,
                                                      @PathVariable String participantId) {
        List<IceCandidateMessage> candidates = voiceChatService.getPendingIceCandidates(roomId, participantId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (IceCandidateMessage msg : candidates) {
            result.add(Map.of(
                    "candidate", msg.getCandidate(),
                    "sdpMid", msg.getSdpMid(),
                    "sdpMLineIndex", msg.getSdpMLineIndex(),
                    "from", msg.getFromParticipant(),
                    "to", msg.getToParticipant(),
                    "timestamp", msg.getTimestamp().toString()
            ));
        }
        return ResponseEntity.ok(Map.of("candidates", result, "count", result.size()));
    }

    /**
     * Set a participant as speaking.
     */
    @PostMapping("/rooms/{roomId}/speaking/{participantId}")
    public ResponseEntity<?> setSpeaking(@PathVariable String roomId,
                                         @PathVariable String participantId,
                                         @RequestParam(defaultValue = "true") boolean speaking) {
        voiceChatService.setSpeaking(roomId, participantId, speaking);
        return ResponseEntity.ok(Map.of("message", "Speaking state updated", "speaking", speaking));
    }

    /**
     * Set a participant as muted.
     */
    @PostMapping("/rooms/{roomId}/mute/{participantId}")
    public ResponseEntity<?> setMuted(@PathVariable String roomId,
                                      @PathVariable String participantId,
                                      @RequestParam(defaultValue = "true") boolean muted) {
        voiceChatService.setMuted(roomId, participantId, muted);
        return ResponseEntity.ok(Map.of("message", "Mute state updated", "muted", muted));
    }

    /**
     * Get active speakers in a room.
     */
    @GetMapping("/rooms/{roomId}/speakers")
    public ResponseEntity<?> getActiveSpeakers(@PathVariable String roomId) {
        Set<String> speakers = voiceChatService.getActiveSpeakers(roomId);
        return ResponseEntity.ok(Map.of("speakers", new ArrayList<>(speakers), "count", speakers.size()));
    }

    /**
     * Get all voice rooms.
     */
    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        List<RoomInfo> rooms = voiceChatService.getAllRooms();
        List<Map<String, Object>> result = new ArrayList<>();
        for (RoomInfo room : rooms) {
            result.add(Map.of(
                    "roomId", room.getRoomId(),
                    "sessionId", room.getSessionId(),
                    "participantCount", room.getParticipantCount(),
                    "speakerCount", room.getSpeakerCount()
            ));
        }
        return ResponseEntity.ok(Map.of("rooms", result, "count", result.size()));
    }

    /**
     * Get room statistics.
     */
    @GetMapping("/rooms/{roomId}/stats")
    public ResponseEntity<?> getRoomStatistics(@PathVariable String roomId) {
        return ResponseEntity.ok(voiceChatService.getRoomStatistics(roomId));
    }

    /**
     * Get global voice chat statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(voiceChatService.getStatistics());
    }

    /**
     * Remove stale rooms.
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> removeStaleRooms() {
        int removed = voiceChatService.removeStaleRooms();
        return ResponseEntity.ok(Map.of("removed", removed, "message", removed + " stale rooms removed"));
    }

    /**
     * Delete a room.
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        if (voiceChatService.deleteRoom(roomId)) {
            return ResponseEntity.ok(Map.of("message", "Room deleted: " + roomId));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Build a response map from VoiceRoom.
     */
    private Map<String, Object> buildRoomInfoResponse(VoiceRoom room) {
        return Map.of(
                "roomId", room.getRoomId(),
                "sessionId", room.getSessionId(),
                "participantCount", room.getParticipantCount(),
                "participants", new ArrayList<>(room.getParticipants()),
                "speakers", new ArrayList<>(room.getSpeakers()),
                "muted", new ArrayList<>(room.getMuted()),
                "createdAt", room.getCreatedAt().toString(),
                "lastActivity", room.getLastActivity().toString()
        );
    }
}

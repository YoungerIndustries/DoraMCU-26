package com.yisy.modules.doramcu.service.voice;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Voice chat service for WebRTC-based voice communication in multiplayer sessions.
 * <p>
 * Provides server-side signaling for WebRTC peer-to-peer voice channels.
 * The server manages voice rooms, tracks participants, and relays SDP offers/answers
 * and ICE candidates. Actual audio streams flow peer-to-peer between browsers.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Voice room creation per game session</li>
 *   <li>Participant join/leave with speaker tracking</li>
 *   <li>WebRTC signaling: SDP offer/answer exchange</li>
 *   <li>ICE candidate relay between peers</li>
 *   <li>Mute/unmute state management</li>
 *   <li>Room statistics and active speaker detection</li>
 *   <li>Auto-cleanup of empty rooms</li>
 * </ul>
 * </p>
 */

@Service
public class VoiceChatService {

    private static final Logger log = LoggerFactory.getLogger(VoiceChatService.class);
    private static final int MAX_ROOM_PARTICIPANTS = 50;
    private static final int ROOM_TIMEOUT_MINUTES = 60;
    private static final int MAX_ICE_CANDIDATES = 100;

    /** Room ID ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ VoiceRoom mapping. */
    private final ConcurrentHashMap<String, VoiceRoom> rooms = new ConcurrentHashMap<>();

    /** Participant ID ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ Room ID mapping for quick lookup. */
    private final ConcurrentHashMap<String, String> participantRooms = new ConcurrentHashMap<>();

    /** Room ID ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ ICE candidates queue. */
    private final ConcurrentHashMap<String, Deque<IceCandidateMessage>> iceCandidates = new ConcurrentHashMap<>();

    /** Room ID ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ SDP messages queue. */
    private final ConcurrentHashMap<String, Deque<SdpMessage>> sdpMessages = new ConcurrentHashMap<>();

    /**
     * A voice chat room tied to a game session.
     */
    public static class VoiceRoom {
        private final String roomId;
        private final String sessionId;
        private final Instant createdAt;
        private final Set<String> participants;
        private final Set<String> speakers;
        private final Set<String> muted;
        private Instant lastActivity;

        private VoiceRoom(Builder builder) {
            this.roomId = builder.roomId;
            this.sessionId = builder.sessionId;
            this.createdAt = builder.createdAt;
            this.participants = new HashSet<>(builder.participants);
            this.speakers = new HashSet<>();
            this.muted = new HashSet<>();
            this.lastActivity = Instant.now();
        }

        public String getRoomId() { return roomId; }
        public String getSessionId() { return sessionId; }
        public Instant getCreatedAt() { return createdAt; }
        public Set<String> getParticipants() { return Collections.unmodifiableSet(participants); }
        public Set<String> getSpeakers() { return Collections.unmodifiableSet(speakers); }
        public Set<String> getMuted() { return Collections.unmodifiableSet(muted); }
        public Instant getLastActivity() { return lastActivity; }
        public int getParticipantCount() { return participants.size(); }

        public boolean addParticipant(String participantId) {
            if (participants.size() >= MAX_ROOM_PARTICIPANTS) {
                return false;
            }
            participants.add(participantId);
            lastActivity = Instant.now();
            return true;
        }

        public boolean removeParticipant(String participantId) {
            boolean removed = participants.remove(participantId);
            if (removed) {
                speakers.remove(participantId);
                muted.remove(participantId);
                lastActivity = Instant.now();
            }
            return removed;
        }

        public boolean hasParticipant(String participantId) {
            return participants.contains(participantId);
        }

        public void setSpeaking(String participantId, boolean speaking) {
            if (speaking) {
                speakers.add(participantId);
            } else {
                speakers.remove(participantId);
            }
            lastActivity = Instant.now();
        }

        public void setMuted(String participantId, boolean mutedState) {
            if (mutedState) {
                muted.add(participantId);
                speakers.remove(participantId);
            } else {
                muted.remove(participantId);
            }
            lastActivity = Instant.now();
        }

        public boolean isMuted(String participantId) {
            return muted.contains(participantId);
        }

        public boolean isStale() {
            return lastActivity.isBefore(Instant.now().minusSeconds(ROOM_TIMEOUT_MINUTES * 60));
        }

        public static Builder builder(String roomId, String sessionId) {
            return new Builder(roomId, sessionId);
        }

        public static class Builder {
            private final String roomId;
            private final String sessionId;
            private Instant createdAt = Instant.now();
            private final Set<String> participants = new HashSet<>();

            public Builder(String roomId, String sessionId) {
                this.roomId = roomId;
                this.sessionId = sessionId;
            }

            public Builder createdAt(Instant createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder participants(Set<String> participants) {
                this.participants.addAll(participants);
                return this;
            }

            public VoiceRoom build() {
                return new VoiceRoom(this);
            }
        }
    }

    /**
     * WebRTC SDP (Session Description Protocol) message.
     */
    public static class SdpMessage {
        private final String type;
        private final String sdp;
        private final String fromParticipant;
        private final String toParticipant;
        private final Instant timestamp;

        public SdpMessage(String type, String sdp, String fromParticipant, String toParticipant) {
            this.type = type;
            this.sdp = sdp;
            this.fromParticipant = fromParticipant;
            this.toParticipant = toParticipant;
            this.timestamp = Instant.now();
        }

        public String getType() { return type; }
        public String getSdp() { return sdp; }
        public String getFromParticipant() { return fromParticipant; }
        public String getToParticipant() { return toParticipant; }
        public Instant getTimestamp() { return timestamp; }

        public static SdpMessage offer(String sdp, String from, String to) {
            return new SdpMessage("offer", sdp, from, to);
        }

        public static SdpMessage answer(String sdp, String from, String to) {
            return new SdpMessage("answer", sdp, from, to);
        }

        public static SdpMessage pranswer(String sdp, String from, String to) {
            return new SdpMessage("pranswer", sdp, from, to);
        }
    }

    /**
     * WebRTC ICE (Interactive Connectivity Establishment) candidate message.
     */
    public static class IceCandidateMessage {
        private final String candidate;
        private final String sdpMid;
        private final int sdpMLineIndex;
        private final String fromParticipant;
        private final String toParticipant;
        private final Instant timestamp;

        public IceCandidateMessage(String candidate, String sdpMid, int sdpMLineIndex,
                                    String fromParticipant, String toParticipant) {
            this.candidate = candidate;
            this.sdpMid = sdpMid;
            this.sdpMLineIndex = sdpMLineIndex;
            this.fromParticipant = fromParticipant;
            this.toParticipant = toParticipant;
            this.timestamp = Instant.now();
        }

        public String getCandidate() { return candidate; }
        public String getSdpMid() { return sdpMid; }
        public int getSdpMLineIndex() { return sdpMLineIndex; }
        public String getFromParticipant() { return fromParticipant; }
        public String getToParticipant() { return toParticipant; }
        public Instant getTimestamp() { return timestamp; }
    }

    /**
     * Room information for API responses.
     */
    public static class RoomInfo {
        private final String roomId;
        private final String sessionId;
        private final int participantCount;
        private final int speakerCount;
        private final Instant createdAt;
        private final Instant lastActivity;

        public RoomInfo(VoiceRoom room) {
            this.roomId = room.getRoomId();
            this.sessionId = room.getSessionId();
            this.participantCount = room.getParticipantCount();
            this.speakerCount = room.getSpeakers().size();
            this.createdAt = room.getCreatedAt();
            this.lastActivity = room.getLastActivity();
        }

        public String getRoomId() { return roomId; }
        public String getSessionId() { return sessionId; }
        public int getParticipantCount() { return participantCount; }
        public int getSpeakerCount() { return speakerCount; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getLastActivity() { return lastActivity; }
    }

    /**
     * Create a voice room for a game session.
     */
    public Optional<VoiceRoom> createRoom(String roomId, String sessionId) {
        if (rooms.containsKey(roomId)) {
            log.warn("Voice room already exists: {}", roomId);
            return Optional.of(rooms.get(roomId));
        }
        VoiceRoom room = VoiceRoom.builder(roomId, sessionId).build();
        rooms.put(roomId, room);
        log.info("Voice room created: {} for session {}", roomId, sessionId);
        return Optional.of(room);
    }

    /**
     * Create a voice room automatically for a session if it doesn't exist.
     */
    public VoiceRoom getOrCreateRoom(String sessionId) {
        String roomId = "voice-" + sessionId;
        return rooms.computeIfAbsent(roomId, id -> {
            VoiceRoom room = VoiceRoom.builder(roomId, sessionId).build();
            log.info("Auto-created voice room: {} for session {}", roomId, sessionId);
            return room;
        });
    }

    /**
     * Get a voice room by ID.
     */
    public Optional<VoiceRoom> getRoom(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    /**
     * Get the room for a session.
     */
    public Optional<VoiceRoom> getRoomBySession(String sessionId) {
        String roomId = "voice-" + sessionId;
        return Optional.ofNullable(rooms.get(roomId));
    }

    /**
     * A participant joins a voice room.
     */
    public boolean joinRoom(String roomId, String participantId) {
        VoiceRoom room = rooms.get(roomId);
        if (room == null) {
            log.warn("Participant {} attempted to join non-existent room: {}", participantId, roomId);
            return false;
        }
        if (!room.addParticipant(participantId)) {
            log.warn("Room {} is full (max {})", roomId, MAX_ROOM_PARTICIPANTS);
            return false;
        }
        participantRooms.put(participantId, roomId);
        log.info("Participant {} joined voice room {}", participantId, roomId);
        return true;
    }

    /**
     * A participant leaves a voice room.
     */
    public void leaveRoom(String roomId, String participantId) {
        VoiceRoom room = rooms.get(roomId);
        if (room != null) {
            room.removeParticipant(participantId);
            log.info("Participant {} left voice room {}", participantId, roomId);
        }
        participantRooms.remove(participantId);

        // Auto-remove empty rooms
        if (room != null && room.getParticipantCount() == 0) {
            rooms.remove(roomId);
            iceCandidates.remove(roomId);
            sdpMessages.remove(roomId);
            log.info("Empty voice room {} removed", roomId);
        }
    }

    /**
     * Get the room a participant is in.
     */
    public Optional<String> getParticipantRoom(String participantId) {
        return Optional.ofNullable(participantRooms.get(participantId));
    }

    /**
     * Relay an SDP offer from one participant to another.
     */
    public void relaySdpOffer(String roomId, SdpMessage offer) {
        Deque<SdpMessage> queue = sdpMessages.computeIfAbsent(roomId,
                k -> new LinkedList<>());
        queue.addLast(offer);
        while (queue.size() > MAX_ICE_CANDIDATES) {
            queue.removeFirst();
        }
        log.debug("SDP offer relayed in room {} from {} to {}", roomId,
                offer.getFromParticipant(), offer.getToParticipant());
    }

    /**
     * Relay an SDP answer from one participant to another.
     */
    public void relaySdpAnswer(String roomId, SdpMessage answer) {
        Deque<SdpMessage> queue = sdpMessages.computeIfAbsent(roomId,
                k -> new LinkedList<>());
        queue.addLast(answer);
        while (queue.size() > MAX_ICE_CANDIDATES) {
            queue.removeFirst();
        }
        log.debug("SDP answer relayed in room {} from {} to {}", roomId,
                answer.getFromParticipant(), answer.getToParticipant());
    }

    /**
     * Relay an ICE candidate from one participant to another.
     */
    public void relayIceCandidate(String roomId, IceCandidateMessage candidate) {
        Deque<IceCandidateMessage> queue = iceCandidates.computeIfAbsent(roomId,
                k -> new LinkedList<>());
        queue.addLast(candidate);
        while (queue.size() > MAX_ICE_CANDIDATES) {
            queue.removeFirst();
        }
        log.debug("ICE candidate relayed in room {} from {} to {}", roomId,
                candidate.getFromParticipant(), candidate.getToParticipant());
    }

    /**
     * Get pending SDP messages for a participant.
     */
    public List<SdpMessage> getPendingSdpMessages(String roomId, String participantId) {
        Deque<SdpMessage> queue = sdpMessages.get(roomId);
        if (queue == null) {
            return Collections.emptyList();
        }
        return queue.stream()
                .filter(msg -> msg.getToParticipant().equals(participantId))
                .collect(Collectors.toList());
    }

    /**
     * Get pending ICE candidates for a participant.
     */
    public List<IceCandidateMessage> getPendingIceCandidates(String roomId, String participantId) {
        Deque<IceCandidateMessage> queue = iceCandidates.get(roomId);
        if (queue == null) {
            return Collections.emptyList();
        }
        return queue.stream()
                .filter(msg -> msg.getToParticipant().equals(participantId))
                .collect(Collectors.toList());
    }

    /**
     * Set a participant as speaking.
     */
    public void setSpeaking(String roomId, String participantId, boolean speaking) {
        VoiceRoom room = rooms.get(roomId);
        if (room != null) {
            room.setSpeaking(participantId, speaking);
        }
    }

    /**
     * Set a participant as muted.
     */
    public void setMuted(String roomId, String participantId, boolean muted) {
        VoiceRoom room = rooms.get(roomId);
        if (room != null) {
            room.setMuted(participantId, muted);
        }
    }

    /**
     * Get active speakers in a room.
     */
    public Set<String> getActiveSpeakers(String roomId) {
        VoiceRoom room = rooms.get(roomId);
        if (room == null) {
            return Collections.emptySet();
        }
        return room.getSpeakers();
    }

    /**
     * Get all rooms.
     */
    public List<RoomInfo> getAllRooms() {
        return rooms.values().stream()
                .map(RoomInfo::new)
                .collect(Collectors.toList());
    }

    /**
     * Get room statistics.
     */
    public Map<String, Object> getRoomStatistics(String roomId) {
        VoiceRoom room = rooms.get(roomId);
        if (room == null) {
            return Map.of("roomId", roomId, "exists", false);
        }
        return Map.of(
                "roomId", roomId,
                "sessionId", room.getSessionId(),
                "exists", true,
                "participantCount", room.getParticipantCount(),
                "speakerCount", room.getSpeakers().size(),
                "mutedCount", room.getMuted().size(),
                "createdAt", room.getCreatedAt().toString(),
                "lastActivity", room.getLastActivity().toString()
        );
    }

    /**
     * Get global voice chat statistics.
     */
    public Map<String, Object> getStatistics() {
        int totalRooms = rooms.size();
        int totalParticipants = rooms.values().stream()
                .mapToInt(VoiceRoom::getParticipantCount)
                .sum();
        int totalSpeakers = rooms.values().stream()
                .mapToInt(r -> r.getSpeakers().size())
                .sum();

        return Map.of(
                "totalRooms", totalRooms,
                "totalParticipants", totalParticipants,
                "totalSpeakers", totalSpeakers,
                "maxParticipantsPerRoom", MAX_ROOM_PARTICIPANTS
        );
    }

    /**
     * Remove stale rooms.
     */
    public int removeStaleRooms() {
        int removed = 0;
        Iterator<Map.Entry<String, VoiceRoom>> it = rooms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, VoiceRoom> entry = it.next();
            if (entry.getValue().isStale()) {
                // Remove participants from participantRooms map
                for (String participantId : entry.getValue().getParticipants()) {
                    participantRooms.remove(participantId);
                }
                it.remove();
                iceCandidates.remove(entry.getKey());
                sdpMessages.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.info("Removed {} stale voice rooms", removed);
        }
        return removed;
    }

    /**
     * Clear all voice chat data.
     */
    public void clearAll() {
        rooms.clear();
        participantRooms.clear();
        iceCandidates.clear();
        sdpMessages.clear();
    }

    /**
     * Delete a room and remove all participants.
     */
    public boolean deleteRoom(String roomId) {
        VoiceRoom room = rooms.remove(roomId);
        if (room != null) {
            for (String participantId : room.getParticipants()) {
                participantRooms.remove(participantId);
            }
            iceCandidates.remove(roomId);
            sdpMessages.remove(roomId);
            log.info("Voice room {} deleted", roomId);
            return true;
        }
        return false;
    }
}

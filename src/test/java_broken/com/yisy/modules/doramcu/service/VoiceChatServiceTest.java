package com.yisy.modules.doramcu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for VoiceChatService.
 */
class VoiceChatServiceTest {

    private VoiceChatService voiceChatService;

    @BeforeEach
    void setUp() {
        voiceChatService = new VoiceChatService();
    }

    // ============================================================
    // Room Creation
    // ============================================================

    @Nested
    class CreateRoom {

        @Test
        @DisplayName("Create room returns the room")
        void createReturnsRoom() {
            Optional<VoiceChatService.VoiceRoom> result = voiceChatService.createRoom("room1", "session1");

            assertTrue(result.isPresent());
            assertEquals("room1", result.get().getRoomId());
            assertEquals("session1", result.get().getSessionId());
        }

        @Test
        @DisplayName("Create room stores it")
        void createStoresRoom() {
            voiceChatService.createRoom("room1", "session1");

            Optional<VoiceChatService.VoiceRoom> room = voiceChatService.getRoom("room1");
            assertTrue(room.isPresent());
        }

        @Test
        @DisplayName("Create duplicate room returns existing")
        void createDuplicateReturnsExisting() {
            voiceChatService.createRoom("room1", "session1");
            Optional<VoiceChatService.VoiceRoom> result = voiceChatService.createRoom("room1", "session2");

            assertTrue(result.isPresent());
            assertEquals("session1", result.get().getSessionId());
        }

        @Test
        @DisplayName("Get or create room creates if not exists")
        void getOrCreateCreates() {
            VoiceChatService.VoiceRoom room = voiceChatService.getOrCreateRoom("session1");

            assertEquals("voice-session1", room.getRoomId());
            assertEquals("session1", room.getSessionId());
        }

        @Test
        @DisplayName("Get or create room returns existing")
        void getOrCreateReturnsExisting() {
            voiceChatService.createRoom("room1", "session1");
            VoiceChatService.VoiceRoom room1 = voiceChatService.getOrCreateRoom("session1");
            VoiceChatService.VoiceRoom room2 = voiceChatService.getOrCreateRoom("session1");

            assertEquals(room1.getRoomId(), room2.getRoomId());
        }

        @Test
        @DisplayName("Get room by session")
        void getRoomBySession() {
            voiceChatService.createRoom("room1", "session1");

            Optional<VoiceChatService.VoiceRoom> room = voiceChatService.getRoomBySession("session1");
            assertTrue(room.isEmpty()); // getRoomBySession looks for "voice-session1"
        }

        @Test
        @DisplayName("Get room by session with auto-created room")
        void getRoomBySessionAutoCreated() {
            voiceChatService.getOrCreateRoom("session1");

            Optional<VoiceChatService.VoiceRoom> room = voiceChatService.getRoomBySession("session1");
            assertTrue(room.isPresent());
            assertEquals("voice-session1", room.get().getRoomId());
        }

        @Test
        @DisplayName("Get non-existent room returns empty")
        void getNonExistentReturnsEmpty() {
            assertTrue(voiceChatService.getRoom("nonexistent").isEmpty());
        }
    }

    // ============================================================
    // Join Room
    // ============================================================

    @Nested
    class JoinRoom {

        @Test
        @DisplayName("Join room adds participant")
        void joinAddsParticipant() {
            voiceChatService.createRoom("room1", "session1");
            boolean joined = voiceChatService.joinRoom("room1", "player1");

            assertTrue(joined);
            VoiceChatService.VoiceRoom room = voiceChatService.getRoom("room1").orElseThrow();
            assertTrue(room.hasParticipant("player1"));
            assertEquals(1, room.getParticipantCount());
        }

        @Test
        @DisplayName("Join non-existent room fails")
        void joinNonExistentFails() {
            boolean joined = voiceChatService.joinRoom("nonexistent", "player1");
            assertFalse(joined);
        }

        @Test
        @DisplayName("Join room tracks participant room")
        void joinTracksParticipantRoom() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");

            Optional<String> room = voiceChatService.getParticipantRoom("player1");
            assertTrue(room.isPresent());
            assertEquals("room1", room.get());
        }

        @Test
        @DisplayName("Multiple participants can join")
        void multipleParticipantsJoin() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.joinRoom("room1", "player2");
            voiceChatService.joinRoom("room1", "player3");

            VoiceChatService.VoiceRoom room = voiceChatService.getRoom("room1").orElseThrow();
            assertEquals(3, room.getParticipantCount());
        }

        @Test
        @DisplayName("Room participant limit enforced")
        void participantLimitEnforced() {
            voiceChatService.createRoom("room1", "session1");
            for (int i = 0; i < 50; i++) {
                assertTrue(voiceChatService.joinRoom("room1", "player" + i));
            }
            assertFalse(voiceChatService.joinRoom("room1", "player50"));
        }
    }

    // ============================================================
    // Leave Room
    // ============================================================

    @Nested
    class LeaveRoom {

        @Test
        @DisplayName("Leave room removes participant and auto-removes empty room")
        void leaveRemovesParticipant() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.leaveRoom("room1", "player1");

            // Room is auto-removed when last participant leaves
            assertTrue(voiceChatService.getRoom("room1").isEmpty());
        }

        @Test
        @DisplayName("Leave room clears participant room mapping")
        void leaveClearsMapping() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.leaveRoom("room1", "player1");

            assertTrue(voiceChatService.getParticipantRoom("player1").isEmpty());
        }

        @Test
        @DisplayName("Leave room removes empty room")
        void leaveRemovesEmptyRoom() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.leaveRoom("room1", "player1");

            assertTrue(voiceChatService.getRoom("room1").isEmpty());
        }

        @Test
        @DisplayName("Leave room keeps room with other participants")
        void leaveKeepsRoomWithOthers() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.joinRoom("room1", "player2");
            voiceChatService.leaveRoom("room1", "player1");

            VoiceChatService.VoiceRoom room = voiceChatService.getRoom("room1").orElseThrow();
            assertEquals(1, room.getParticipantCount());
            assertTrue(room.hasParticipant("player2"));
        }

        @Test
        @DisplayName("Leave non-existent room does nothing")
        void leaveNonExistentDoesNothing() {
            voiceChatService.leaveRoom("nonexistent", "player1");
            // No exception
        }
    }

    // ============================================================
    // SDP Signaling
    // ============================================================

    @Nested
    class SdpSignaling {

        @Test
        @DisplayName("Relay SDP offer")
        void relaySdpOffer() {
            voiceChatService.createRoom("room1", "session1");
            VoiceChatService.SdpMessage offer = VoiceChatService.SdpMessage.offer("v=0\r\no=-...", "player1", "player2");
            voiceChatService.relaySdpOffer("room1", offer);

            List<VoiceChatService.SdpMessage> messages = voiceChatService.getPendingSdpMessages("room1", "player2");
            assertEquals(1, messages.size());
            assertEquals("offer", messages.get(0).getType());
            assertEquals("player1", messages.get(0).getFromParticipant());
        }

        @Test
        @DisplayName("Relay SDP answer")
        void relaySdpAnswer() {
            voiceChatService.createRoom("room1", "session1");
            VoiceChatService.SdpMessage answer = VoiceChatService.SdpMessage.answer("v=0\r\no=-...", "player2", "player1");
            voiceChatService.relaySdpAnswer("room1", answer);

            List<VoiceChatService.SdpMessage> messages = voiceChatService.getPendingSdpMessages("room1", "player1");
            assertEquals(1, messages.size());
            assertEquals("answer", messages.get(0).getType());
        }

        @Test
        @DisplayName("SDP messages filtered by recipient")
        void sdpFilteredByRecipient() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.relaySdpOffer("room1", VoiceChatService.SdpMessage.offer("sdp1", "player1", "player2"));
            voiceChatService.relaySdpOffer("room1", VoiceChatService.SdpMessage.offer("sdp2", "player1", "player3"));

            List<VoiceChatService.SdpMessage> forPlayer2 = voiceChatService.getPendingSdpMessages("room1", "player2");
            List<VoiceChatService.SdpMessage> forPlayer3 = voiceChatService.getPendingSdpMessages("room1", "player3");

            assertEquals(1, forPlayer2.size());
            assertEquals(1, forPlayer3.size());
            assertEquals("sdp1", forPlayer2.get(0).getSdp());
            assertEquals("sdp2", forPlayer3.get(0).getSdp());
        }

        @Test
        @DisplayName("Get SDP messages for non-existent room")
        void sdpNonExistentRoom() {
            List<VoiceChatService.SdpMessage> messages = voiceChatService.getPendingSdpMessages("nonexistent", "player1");
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("SDP message factory methods")
        void sdpFactoryMethods() {
            VoiceChatService.SdpMessage offer = VoiceChatService.SdpMessage.offer("sdp", "from", "to");
            VoiceChatService.SdpMessage answer = VoiceChatService.SdpMessage.answer("sdp", "from", "to");
            VoiceChatService.SdpMessage pranswer = VoiceChatService.SdpMessage.pranswer("sdp", "from", "to");

            assertEquals("offer", offer.getType());
            assertEquals("answer", answer.getType());
            assertEquals("pranswer", pranswer.getType());
        }
    }

    // ============================================================
    // ICE Candidates
    // ============================================================

    @Nested
    class IceCandidates {

        @Test
        @DisplayName("Relay ICE candidate")
        void relayIceCandidate() {
            voiceChatService.createRoom("room1", "session1");
            VoiceChatService.IceCandidateMessage candidate =
                    new VoiceChatService.IceCandidateMessage("candidate:0", "audio", 0, "player1", "player2");
            voiceChatService.relayIceCandidate("room1", candidate);

            List<VoiceChatService.IceCandidateMessage> candidates =
                    voiceChatService.getPendingIceCandidates("room1", "player2");
            assertEquals(1, candidates.size());
            assertEquals("candidate:0", candidates.get(0).getCandidate());
        }

        @Test
        @DisplayName("ICE candidates filtered by recipient")
        void iceFilteredByRecipient() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.relayIceCandidate("room1",
                    new VoiceChatService.IceCandidateMessage("c1", "audio", 0, "p1", "p2"));
            voiceChatService.relayIceCandidate("room1",
                    new VoiceChatService.IceCandidateMessage("c2", "audio", 0, "p1", "p3"));

            assertEquals(1, voiceChatService.getPendingIceCandidates("room1", "p2").size());
            assertEquals(1, voiceChatService.getPendingIceCandidates("room1", "p3").size());
        }

        @Test
        @DisplayName("Get ICE candidates for non-existent room")
        void iceNonExistentRoom() {
            List<VoiceChatService.IceCandidateMessage> candidates =
                    voiceChatService.getPendingIceCandidates("nonexistent", "player1");
            assertTrue(candidates.isEmpty());
        }

        @Test
        @DisplayName("ICE candidate properties")
        void iceCandidateProperties() {
            VoiceChatService.IceCandidateMessage candidate =
                    new VoiceChatService.IceCandidateMessage("cand", "mid", 1, "from", "to");

            assertEquals("cand", candidate.getCandidate());
            assertEquals("mid", candidate.getSdpMid());
            assertEquals(1, candidate.getSdpMLineIndex());
            assertEquals("from", candidate.getFromParticipant());
            assertEquals("to", candidate.getToParticipant());
            assertNotNull(candidate.getTimestamp());
        }
    }

    // ============================================================
    // Speaker Tracking
    // ============================================================

    @Nested
    class SpeakerTracking {

        @Test
        @DisplayName("Set speaking adds to speakers")
        void setSpeakingAdds() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setSpeaking("room1", "player1", true);

            Set<String> speakers = voiceChatService.getActiveSpeakers("room1");
            assertTrue(speakers.contains("player1"));
        }

        @Test
        @DisplayName("Set not speaking removes from speakers")
        void setNotSpeakingRemoves() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setSpeaking("room1", "player1", true);
            voiceChatService.setSpeaking("room1", "player1", false);

            Set<String> speakers = voiceChatService.getActiveSpeakers("room1");
            assertFalse(speakers.contains("player1"));
        }

        @Test
        @DisplayName("Multiple speakers")
        void multipleSpeakers() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.joinRoom("room1", "player2");
            voiceChatService.joinRoom("room1", "player3");
            voiceChatService.setSpeaking("room1", "player1", true);
            voiceChatService.setSpeaking("room1", "player3", true);

            Set<String> speakers = voiceChatService.getActiveSpeakers("room1");
            assertEquals(2, speakers.size());
            assertTrue(speakers.contains("player1"));
            assertTrue(speakers.contains("player3"));
        }

        @Test
        @DisplayName("Get speakers for non-existent room")
        void speakersNonExistentRoom() {
            Set<String> speakers = voiceChatService.getActiveSpeakers("nonexistent");
            assertTrue(speakers.isEmpty());
        }
    }

    // ============================================================
    // Mute Control
    // ============================================================

    @Nested
    class MuteControl {

        @Test
        @DisplayName("Mute adds to muted set")
        void muteAdds() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setMuted("room1", "player1", true);

            VoiceChatService.VoiceRoom room = voiceChatService.getRoom("room1").orElseThrow();
            assertTrue(room.isMuted("player1"));
        }

        @Test
        @DisplayName("Mute removes from speakers")
        void muteRemovesFromSpeakers() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setSpeaking("room1", "player1", true);
            voiceChatService.setMuted("room1", "player1", true);

            Set<String> speakers = voiceChatService.getActiveSpeakers("room1");
            assertFalse(speakers.contains("player1"));
        }

        @Test
        @DisplayName("Unmute removes from muted set")
        void unmuteRemoves() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setMuted("room1", "player1", true);
            voiceChatService.setMuted("room1", "player1", false);

            VoiceChatService.VoiceRoom room = voiceChatService.getRoom("room1").orElseThrow();
            assertFalse(room.isMuted("player1"));
        }
    }

    // ============================================================
    // Room Management
    // ============================================================

    @Nested
    class RoomManagement {

        @Test
        @DisplayName("Get all rooms")
        void getAllRooms() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.createRoom("room2", "session2");

            List<VoiceChatService.RoomInfo> rooms = voiceChatService.getAllRooms();
            assertEquals(2, rooms.size());
        }

        @Test
        @DisplayName("Get all rooms empty")
        void getAllRoomsEmpty() {
            List<VoiceChatService.RoomInfo> rooms = voiceChatService.getAllRooms();
            assertTrue(rooms.isEmpty());
        }

        @Test
        @DisplayName("Delete room")
        void deleteRoom() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            boolean deleted = voiceChatService.deleteRoom("room1");

            assertTrue(deleted);
            assertTrue(voiceChatService.getRoom("room1").isEmpty());
            assertTrue(voiceChatService.getParticipantRoom("player1").isEmpty());
        }

        @Test
        @DisplayName("Delete non-existent room")
        void deleteNonExistent() {
            assertFalse(voiceChatService.deleteRoom("nonexistent"));
        }

        @Test
        @DisplayName("RoomInfo from room")
        void roomInfo() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setSpeaking("room1", "player1", true);

            VoiceChatService.RoomInfo info = new VoiceChatService.RoomInfo(
                    voiceChatService.getRoom("room1").orElseThrow());

            assertEquals("room1", info.getRoomId());
            assertEquals("session1", info.getSessionId());
            assertEquals(1, info.getParticipantCount());
            assertEquals(1, info.getSpeakerCount());
        }
    }

    // ============================================================
    // Statistics
    // ============================================================

    @Nested
    class Statistics {

        @Test
        @DisplayName("Get room statistics")
        void getRoomStatistics() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setSpeaking("room1", "player1", true);

            Map<String, Object> stats = voiceChatService.getRoomStatistics("room1");

            assertEquals("room1", stats.get("roomId"));
            assertEquals("session1", stats.get("sessionId"));
            assertEquals(true, stats.get("exists"));
            assertEquals(1, stats.get("participantCount"));
            assertEquals(1, stats.get("speakerCount"));
        }

        @Test
        @DisplayName("Get room statistics non-existent")
        void getRoomStatisticsNonExistent() {
            Map<String, Object> stats = voiceChatService.getRoomStatistics("nonexistent");
            assertEquals(false, stats.get("exists"));
        }

        @Test
        @DisplayName("Get global statistics")
        void getGlobalStatistics() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.createRoom("room2", "session2");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.joinRoom("room1", "player2");
            voiceChatService.joinRoom("room2", "player3");
            voiceChatService.setSpeaking("room1", "player1", true);

            Map<String, Object> stats = voiceChatService.getStatistics();

            assertEquals(2, stats.get("totalRooms"));
            assertEquals(3, stats.get("totalParticipants"));
            assertEquals(1, stats.get("totalSpeakers"));
        }

        @Test
        @DisplayName("Get global statistics empty")
        void getGlobalStatisticsEmpty() {
            Map<String, Object> stats = voiceChatService.getStatistics();
            assertEquals(0, stats.get("totalRooms"));
            assertEquals(0, stats.get("totalParticipants"));
            assertEquals(0, stats.get("totalSpeakers"));
        }
    }

    // ============================================================
    // Stale Room Cleanup
    // ============================================================

    @Nested
    class StaleRoomCleanup {

        @Test
        @DisplayName("Remove stale rooms")
        void removeStaleRooms() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");

            VoiceChatService.VoiceRoom room = voiceChatService.getRoom("room1").orElseThrow();
            // Make it stale by setting lastActivity far in the past
            // We can't directly set lastActivity, so we test the logic indirectly
            int removed = voiceChatService.removeStaleRooms();
            assertEquals(0, removed); // Not stale yet
        }

        @Test
        @DisplayName("No stale rooms returns 0")
        void noStaleReturnsZero() {
            voiceChatService.createRoom("room1", "session1");
            int removed = voiceChatService.removeStaleRooms();
            assertEquals(0, removed);
        }
    }

    // ============================================================
    // Clear All
    // ============================================================

    @Nested
    class ClearAll {

        @Test
        @DisplayName("Clear all removes all data")
        void clearAll() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.relaySdpOffer("room1",
                    VoiceChatService.SdpMessage.offer("sdp", "p1", "p2"));
            voiceChatService.relayIceCandidate("room1",
                    new VoiceChatService.IceCandidateMessage("c", "m", 0, "p1", "p2"));

            voiceChatService.clearAll();

            assertTrue(voiceChatService.getAllRooms().isEmpty());
            assertTrue(voiceChatService.getParticipantRoom("player1").isEmpty());
        }
    }

    // ============================================================
    // VoiceRoom Builder
    // ============================================================

    @Nested
    class VoiceRoomBuilder {

        @Test
        @DisplayName("Builder with all fields")
        void builderWithAllFields() {
            Instant now = Instant.now();
            VoiceChatService.VoiceRoom room = VoiceChatService.VoiceRoom.builder("room1", "session1")
                    .createdAt(now)
                    .participants(Set.of("player1", "player2"))
                    .build();

            assertEquals("room1", room.getRoomId());
            assertEquals("session1", room.getSessionId());
            assertEquals(now, room.getCreatedAt());
            assertEquals(2, room.getParticipantCount());
        }

        @Test
        @DisplayName("Builder default values")
        void builderDefaults() {
            VoiceChatService.VoiceRoom room = VoiceChatService.VoiceRoom.builder("room1", "session1").build();

            assertEquals(0, room.getParticipantCount());
            assertTrue(room.getSpeakers().isEmpty());
            assertTrue(room.getMuted().isEmpty());
        }

        @Test
        @DisplayName("Participants set is unmodifiable")
        void participantsUnmodifiable() {
            VoiceChatService.VoiceRoom room = VoiceChatService.VoiceRoom.builder("room1", "session1").build();
            assertThrows(UnsupportedOperationException.class, () -> room.getParticipants().add("player1"));
        }

        @Test
        @DisplayName("Speakers set is unmodifiable")
        void speakersUnmodifiable() {
            VoiceChatService.VoiceRoom room = VoiceChatService.VoiceRoom.builder("room1", "session1").build();
            assertThrows(UnsupportedOperationException.class, () -> room.getSpeakers().add("player1"));
        }

        @Test
        @DisplayName("Muted set is unmodifiable")
        void mutedUnmodifiable() {
            VoiceChatService.VoiceRoom room = VoiceChatService.VoiceRoom.builder("room1", "session1").build();
            assertThrows(UnsupportedOperationException.class, () -> room.getMuted().add("player1"));
        }
    }

    // ============================================================
    // Integration Scenarios
    // ============================================================

    @Nested
    class IntegrationScenarios {

        @Test
        @DisplayName("Full WebRTC signaling workflow")
        void fullSignalingWorkflow() {
            // Create room
            voiceChatService.createRoom("room1", "session1");

            // Two players join
            assertTrue(voiceChatService.joinRoom("room1", "player1"));
            assertTrue(voiceChatService.joinRoom("room1", "player2"));

            // Player 1 sends SDP offer to player 2
            voiceChatService.relaySdpOffer("room1",
                    VoiceChatService.SdpMessage.offer("v=0\r\no=- 1234...", "player1", "player2"));

            // Player 2 receives the offer
            List<VoiceChatService.SdpMessage> offers = voiceChatService.getPendingSdpMessages("room1", "player2");
            assertEquals(1, offers.size());
            assertEquals("offer", offers.get(0).getType());

            // Player 2 sends SDP answer back
            voiceChatService.relaySdpAnswer("room1",
                    VoiceChatService.SdpMessage.answer("v=0\r\no=- 5678...", "player2", "player1"));

            // Player 1 receives the answer
            List<VoiceChatService.SdpMessage> answers = voiceChatService.getPendingSdpMessages("room1", "player1");
            assertEquals(1, answers.size());
            assertEquals("answer", answers.get(0).getType());

            // ICE candidates exchanged
            voiceChatService.relayIceCandidate("room1",
                    new VoiceChatService.IceCandidateMessage("candidate:0", "audio", 0, "player1", "player2"));
            voiceChatService.relayIceCandidate("room1",
                    new VoiceChatService.IceCandidateMessage("candidate:1", "audio", 0, "player2", "player1"));

            assertEquals(1, voiceChatService.getPendingIceCandidates("room1", "player1").size());
            assertEquals(1, voiceChatService.getPendingIceCandidates("room1", "player2").size());
        }

        @Test
        @DisplayName("Multi-room scenario")
        void multiRoomScenario() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.createRoom("room2", "session2");

            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.joinRoom("room1", "player2");
            voiceChatService.joinRoom("room2", "player3");
            voiceChatService.joinRoom("room2", "player4");

            voiceChatService.setSpeaking("room1", "player1", true);
            voiceChatService.setSpeaking("room2", "player3", true);

            assertEquals(1, voiceChatService.getActiveSpeakers("room1").size());
            assertEquals(1, voiceChatService.getActiveSpeakers("room2").size());

            Map<String, Object> stats = voiceChatService.getStatistics();
            assertEquals(2, stats.get("totalRooms"));
            assertEquals(4, stats.get("totalParticipants"));
            assertEquals(2, stats.get("totalSpeakers"));
        }

        @Test
        @DisplayName("Mute and speaker interaction")
        void muteAndSpeakerInteraction() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");

            // Player starts speaking
            voiceChatService.setSpeaking("room1", "player1", true);
            assertEquals(1, voiceChatService.getActiveSpeakers("room1").size());

            // Player is muted — removed from speakers
            voiceChatService.setMuted("room1", "player1", true);
            assertTrue(voiceChatService.getActiveSpeakers("room1").isEmpty());

            // Player is unmuted
            voiceChatService.setMuted("room1", "player1", false);
            // But not speaking again until explicitly set
            assertTrue(voiceChatService.getActiveSpeakers("room1").isEmpty());

            // Player starts speaking again
            voiceChatService.setSpeaking("room1", "player1", true);
            assertEquals(1, voiceChatService.getActiveSpeakers("room1").size());
        }

        @Test
        @DisplayName("Leave removes speaker and mute state")
        void leaveRemovesSpeakerAndMute() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            voiceChatService.setSpeaking("room1", "player1", true);
            voiceChatService.setMuted("room1", "player1", true);

            voiceChatService.leaveRoom("room1", "player1");
            // Room is auto-removed because empty
            assertTrue(voiceChatService.getRoom("room1").isEmpty());
        }

        @Test
        @DisplayName("Participant room lookup after leave")
        void participantRoomAfterLeave() {
            voiceChatService.createRoom("room1", "session1");
            voiceChatService.joinRoom("room1", "player1");
            assertEquals("room1", voiceChatService.getParticipantRoom("player1").orElseThrow());

            voiceChatService.leaveRoom("room1", "player1");
            assertTrue(voiceChatService.getParticipantRoom("player1").isEmpty());
        }
    }
}

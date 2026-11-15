package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.hex.chat.ChatMessage;
import com.yisy.modules.doramcu.hex.chat.ChatMessage.Channel;
import com.yisy.modules.doramcu.hex.chat.ChatMessage.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
    }

    @Nested
    class SendMessageTests {

        @Test
        void sendMessageCreatesMessage() {
            ChatMessage msg = chatService.sendMessage("session1", 1, "Captain", "Hello!", Channel.GENERAL);

            assertNotNull(msg.getId());
            assertEquals("session1", msg.getSessionId());
            assertEquals(1, msg.getSenderId());
            assertEquals("Captain", msg.getSenderName());
            assertEquals("Hello!", msg.getContent());
            assertEquals(Channel.GENERAL, msg.getChannel());
            assertEquals(MessageType.TEXT, msg.getType());
        }

        @Test
        void sendMessageDefaultsToGeneralChannel() {
            ChatMessage msg = chatService.sendMessage("session1", 1, "Captain", "Hello!", null);
            assertEquals(Channel.GENERAL, msg.getChannel());
        }

        @Test
        void sendMessageTrimsContent() {
            ChatMessage msg = chatService.sendMessage("session1", 1, null, "  Hello!  ", Channel.GENERAL);
            assertEquals("Hello!", msg.getContent());
        }

        @Test
        void sendMessageDefaultsSenderName() {
            ChatMessage msg = chatService.sendMessage("session1", 42, null, "Hello!", Channel.GENERAL);
            assertEquals("Player42", msg.getSenderName());
        }

        @Test
        void sendMessageRejectsNullSession() {
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendMessage(null, 1, "Cap", "Hello", Channel.GENERAL));
        }

        @Test
        void sendMessageRejectsEmptySession() {
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendMessage("", 1, "Cap", "Hello", Channel.GENERAL));
        }

        @Test
        void sendMessageRejectsEmptyContent() {
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendMessage("s1", 1, "Cap", "", Channel.GENERAL));
        }

        @Test
        void sendMessageRejectsNullContent() {
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendMessage("s1", 1, "Cap", null, Channel.GENERAL));
        }

        @Test
        void sendMessageRejectsWhitespaceOnlyContent() {
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendMessage("s1", 1, "Cap", "   ", Channel.GENERAL));
        }

        @Test
        void sendMessageRejectsTooLongContent() {
            String longContent = "x".repeat(1001);
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendMessage("s1", 1, "Cap", longContent, Channel.GENERAL));
        }

        @Test
        void sendMessageAcceptsMaxLengthContent() {
            String maxContent = "x".repeat(1000);
            ChatMessage msg = chatService.sendMessage("s1", 1, "Cap", maxContent, Channel.GENERAL);
            assertEquals(1000, msg.getContent().length());
        }

        @Test
        void sendMessageOnAllChannels() {
            ChatMessage general = chatService.sendMessage("s1", 1, "Cap", "General", Channel.GENERAL);
            ChatMessage tactics = chatService.sendMessage("s1", 1, "Cap", "Tactics", Channel.TACTICS);
            ChatMessage ooc = chatService.sendMessage("s1", 1, "Cap", "OOC", Channel.OOC);

            assertEquals(Channel.GENERAL, general.getChannel());
            assertEquals(Channel.TACTICS, tactics.getChannel());
            assertEquals(Channel.OOC, ooc.getChannel());
        }
    }

    @Nested
    class SendWhisperTests {

        @Test
        void sendWhisperCreatesWhisperMessage() {
            ChatMessage msg = chatService.sendWhisper("s1", 1, "Cap", 2, "Secret");

            assertEquals(MessageType.WHISPER, msg.getType());
            assertEquals(2L, msg.getTargetPlayerId());
            assertEquals("Secret", msg.getContent());
        }

        @Test
        void sendWhisperRejectsSelfWhisper() {
            assertThrows(IllegalArgumentException.class, () ->
                    chatService.sendWhisper("s1", 1, "Cap", 1, "Self"));
        }

        @Test
        void sendWhisperTrimsContent() {
            ChatMessage msg = chatService.sendWhisper("s1", 1, "Cap", 2, "  Secret  ");
            assertEquals("Secret", msg.getContent());
        }
    }

    @Nested
    class PostSystemMessageTests {

        @Test
        void postSystemMessageCreatesSystemMessage() {
            ChatMessage msg = chatService.postSystemMessage("s1", "Player joined", Channel.GENERAL);

            assertEquals(MessageType.SYSTEM, msg.getType());
            assertEquals(-1, msg.getSenderId());
            assertEquals("System", msg.getSenderName());
            assertEquals("Player joined", msg.getContent());
        }

        @Test
        void postSystemMessageDefaultsChannel() {
            ChatMessage msg = chatService.postSystemMessage("s1", "Player joined", null);
            assertEquals(Channel.GENERAL, msg.getChannel());
        }
    }

    @Nested
    class GetHistoryTests {

        @Test
        void getHistoryReturnsEmptyForUnknownSession() {
            List<ChatMessage> history = chatService.getHistory("unknown", null, 50);
            assertTrue(history.isEmpty());
        }

        @Test
        void getHistoryReturnsAllMessages() {
            chatService.sendMessage("s1", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("s1", 2, "Vice", "Msg2", Channel.GENERAL);

            List<ChatMessage> history = chatService.getHistory("s1", null, 0);
            assertEquals(2, history.size());
        }

        @Test
        void getHistoryFiltersByChannel() {
            chatService.sendMessage("s1", 1, "Cap", "General", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Tactics", Channel.TACTICS);
            chatService.sendMessage("s1", 1, "Cap", "OOC", Channel.OOC);

            List<ChatMessage> tactics = chatService.getHistory("s1", Channel.TACTICS, 0);
            assertEquals(1, tactics.size());
            assertEquals("Tactics", tactics.get(0).getContent());
        }

        @Test
        void getHistoryLimitsResults() {
            chatService.sendMessage("s1", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Msg2", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Msg3", Channel.GENERAL);

            List<ChatMessage> history = chatService.getHistory("s1", null, 2);
            assertEquals(2, history.size());
            assertEquals("Msg2", history.get(0).getContent());
            assertEquals("Msg3", history.get(1).getContent());
        }

        @Test
        void getHistoryReturnsLastNMessages() {
            chatService.sendMessage("s1", 1, "Cap", "Old1", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Old2", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "New", Channel.GENERAL);

            List<ChatMessage> history = chatService.getHistory("s1", null, 2);
            assertEquals("Old2", history.get(0).getContent());
            assertEquals("New", history.get(1).getContent());
        }
    }

    @Nested
    class GetHistorySinceTests {

        @Test
        void getHistorySinceReturnsEmptyForUnknownSession() {
            List<ChatMessage> history = chatService.getHistorySince("unknown", Instant.now());
            assertTrue(history.isEmpty());
        }

        @Test
        void getHistorySinceFiltersByTime() {
            // Use a past timestamp as the cutoff so both messages are after it
            Instant past = Instant.now().minusSeconds(60);
            chatService.sendMessage("s1", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Msg2", Channel.GENERAL);

            List<ChatMessage> history = chatService.getHistorySince("s1", past);
            assertEquals(2, history.size());
            assertEquals("Msg1", history.get(0).getContent());
            assertEquals("Msg2", history.get(1).getContent());

            // Use a future timestamp - no messages should be returned
            Instant future = Instant.now().plusSeconds(60);
            List<ChatMessage> empty = chatService.getHistorySince("s1", future);
            assertTrue(empty.isEmpty());
        }
    }

    @Nested
    class GetPlayerMessagesTests {

        @Test
        void getPlayerMessagesReturnsSentAndReceived() {
            chatService.sendMessage("s1", 1, "Cap", "FromCap", Channel.GENERAL);
            chatService.sendMessage("s1", 2, "Vice", "FromVice", Channel.GENERAL);
            chatService.sendWhisper("s1", 2, "Vice", 1, "Secret to Cap");

            List<ChatMessage> messages = chatService.getPlayerMessages("s1", 1);
            assertEquals(2, messages.size());
        }

        @Test
        void getPlayerMessagesReturnsEmptyForUnknownSession() {
            List<ChatMessage> messages = chatService.getPlayerMessages("unknown", 1);
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    class GetChannelsTests {

        @Test
        void getChannelsReturnsAllChannels() {
            var channels = chatService.getChannels();
            assertEquals(3, channels.size());
            assertTrue(channels.contains("general"));
            assertTrue(channels.contains("tactics"));
            assertTrue(channels.contains("ooc"));
        }
    }

    @Nested
    class GetMessageCountTests {

        @Test
        void getMessageCountReturnsZeroForUnknownSession() {
            assertEquals(0, chatService.getMessageCount("unknown"));
        }

        @Test
        void getMessageCountReturnsCorrectCount() {
            chatService.sendMessage("s1", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Msg2", Channel.GENERAL);
            assertEquals(2, chatService.getMessageCount("s1"));
        }
    }

    @Nested
    class ClearSessionTests {

        @Test
        void clearSessionRemovesAllMessages() {
            chatService.sendMessage("s1", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("s1", 1, "Cap", "Msg2", Channel.GENERAL);
            chatService.clearSession("s1");
            assertEquals(0, chatService.getMessageCount("s1"));
        }

        @Test
        void clearSessionDoesNotAffectOtherSessions() {
            chatService.sendMessage("s1", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("s2", 1, "Cap", "Msg2", Channel.GENERAL);
            chatService.clearSession("s1");
            assertEquals(0, chatService.getMessageCount("s1"));
            assertEquals(1, chatService.getMessageCount("s2"));
        }
    }

    @Nested
    class GetStatsTests {

        @Test
        void getStatsReturnsEmptyForUnknownSession() {
            Map<String, Object> stats = chatService.getStats("unknown");
            assertEquals(0, stats.get("totalMessages"));
            assertEquals(0, stats.get("uniqueSenders"));
        }

        @Test
        void getStatsReturnsCorrectCounts() {
            chatService.sendMessage("s1", 1, "Cap", "General", Channel.GENERAL);
            chatService.sendMessage("s1", 2, "Vice", "Tactics", Channel.TACTICS);
            chatService.postSystemMessage("s1", "System msg", Channel.GENERAL);

            Map<String, Object> stats = chatService.getStats("s1");
            assertEquals(3, stats.get("totalMessages"));
            // System sender is -1, so unique senders are: 1, 2, -1 = 3
            assertEquals(Long.valueOf(3), stats.get("uniqueSenders"));

            @SuppressWarnings("unchecked")
            Map<String, Integer> byChannel = (Map<String, Integer>) stats.get("messagesByChannel");
            assertEquals(2, byChannel.get("general").intValue());
            assertEquals(1, byChannel.get("tactics").intValue());

            @SuppressWarnings("unchecked")
            Map<String, Integer> byType = (Map<String, Integer>) stats.get("messagesByType");
            assertEquals(2, byType.get("TEXT").intValue());
            assertEquals(1, byType.get("SYSTEM").intValue());
        }
    }

    @Nested
    class MessageTrimmingTests {

        @Test
        void trimOldMessagesRemovesExcess() {
            // Add more than MAX_MESSAGES_PER_SESSION messages
            for (int i = 0; i < ChatService.MAX_MESSAGES_PER_SESSION + 10; i++) {
                chatService.sendMessage("s1", 1, "Cap", "Msg" + i, Channel.GENERAL);
            }
            assertTrue(chatService.getMessageCount("s1") <= ChatService.MAX_MESSAGES_PER_SESSION);
        }

        @Test
        void multipleSessionsAreIndependent() {
            chatService.sendMessage("s1", 1, "Cap", "S1Msg", Channel.GENERAL);
            chatService.sendMessage("s2", 2, "Vice", "S2Msg", Channel.GENERAL);

            assertEquals(1, chatService.getMessageCount("s1"));
            assertEquals(1, chatService.getMessageCount("s2"));

            List<ChatMessage> s1History = chatService.getHistory("s1", null, 0);
            assertEquals("S1Msg", s1History.get(0).getContent());
        }
    }

    @Nested
    class IntegrationScenarios {

        @Test
        void fullChatWorkflow() {
            // Players send messages
            ChatMessage m1 = chatService.sendMessage("game1", 1, "Admiral", "Fleet ready", Channel.TACTICS);
            ChatMessage m2 = chatService.sendMessage("game1", 2, "Captain", "Acknowledged", Channel.TACTICS);
            ChatMessage m3 = chatService.sendMessage("game1", 1, "Admiral", "GG", Channel.OOC);

            // System events
            chatService.postSystemMessage("game1", "Player 1 joined the game", null);
            chatService.postSystemMessage("game1", "Player 3 joined the game", null);

            // Whisper
            ChatMessage whisper = chatService.sendWhisper("game1", 1, "Admiral", 2, "Secret plan");

            // Verify history
            List<ChatMessage> allHistory = chatService.getHistory("game1", null, 0);
            assertEquals(6, allHistory.size());

            // Verify channel filter
            List<ChatMessage> tactics = chatService.getHistory("game1", Channel.TACTICS, 0);
            assertEquals(2, tactics.size());

            // Verify stats
            Map<String, Object> stats = chatService.getStats("game1");
            assertEquals(6, stats.get("totalMessages"));
            // Unique senders: 1 (Admiral), 2 (Captain), -1 (System) = 3 distinct
            assertEquals(Long.valueOf(3), stats.get("uniqueSenders"));

            // Verify player messages - player 1 sent 2 text + 1 whisper = 3 messages
            List<ChatMessage> player1Msgs = chatService.getPlayerMessages("game1", 1);
            assertEquals(3, player1Msgs.size());

            // Verify whisper
            assertEquals(MessageType.WHISPER, whisper.getType());
            assertEquals(2L, whisper.getTargetPlayerId());
        }

        @Test
        void sessionCleanupWorkflow() {
            chatService.sendMessage("game1", 1, "Cap", "Hello", Channel.GENERAL);
            chatService.sendMessage("game1", 2, "Vice", "Hi", Channel.GENERAL);

            assertEquals(2, chatService.getMessageCount("game1"));

            // Session ends
            chatService.clearSession("game1");
            assertEquals(0, chatService.getMessageCount("game1"));
            assertTrue(chatService.getHistory("game1", null, 0).isEmpty());
        }
    }
}

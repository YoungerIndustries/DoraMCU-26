package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.service.ChatService;
import com.yisy.modules.doramcu.hex.chat.ChatMessage.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ChatControllerTest {

    ChatService chatService = new ChatService();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
        mockMvc = standaloneSetup(new ChatController(chatService)).build();
    }

    @Nested
    class ChannelsEndpoint {

        @Test
        void getChannelsReturnsAllChannels() throws Exception {
            mockMvc.perform(get("/api/v1/chat/channels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0]").value("general"))
                    .andExpect(jsonPath("$[1]").value("tactics"))
                    .andExpect(jsonPath("$[2]").value("ooc"));
        }
    }

    @Nested
    class SendMessageEndpoint {

        @Test
        void sendMessageCreatesMessage() throws Exception {
            String body = """
                    {"senderId": 1, "senderName": "Captain", "content": "Hello everyone!", "channel": "general"}
                    """;

            mockMvc.perform(post("/api/v1/chat/sessions/test-session/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Hello everyone!"))
                    .andExpect(jsonPath("$.senderName").value("Captain"))
                    .andExpect(jsonPath("$.channel").value("general"));
        }

        @Test
        void sendMessageDefaultsChannel() throws Exception {
            String body = """
                    {"senderId": 1, "content": "No channel"}
                    """;

            mockMvc.perform(post("/api/v1/chat/sessions/test-session/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.channel").value("general"));
        }

        @Test
        void sendMessageOnTacticsChannel() throws Exception {
            String body = """
                    {"senderId": 2, "senderName": "Admiral", "content": "Attack!", "channel": "tactics"}
                    """;

            mockMvc.perform(post("/api/v1/chat/sessions/test-session/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.channel").value("tactics"));
        }
    }

    @Nested
    class WhisperEndpoint {

        @Test
        void sendWhisperCreatesWhisper() throws Exception {
            String body = """
                    {"senderId": 1, "senderName": "Captain", "targetPlayerId": 2, "content": "Secret plan"}
                    """;

            mockMvc.perform(post("/api/v1/chat/sessions/test-session/whisper")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("WHISPER"))
                    .andExpect(jsonPath("$.targetPlayerId").value(2));
        }
    }

    @Nested
    class HistoryEndpoint {

        @Test
        void getHistoryReturnsMessages() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("test-session", 2, "Vice", "Msg2", Channel.GENERAL);

            mockMvc.perform(get("/api/v1/chat/sessions/test-session/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getHistoryWithChannelFilter() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "General", Channel.GENERAL);
            chatService.sendMessage("test-session", 1, "Cap", "Tactics", Channel.TACTICS);

            mockMvc.perform(get("/api/v1/chat/sessions/test-session/history")
                            .param("channel", "tactics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].content").value("Tactics"));
        }

        @Test
        void getHistoryWithLimit() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("test-session", 1, "Cap", "Msg2", Channel.GENERAL);
            chatService.sendMessage("test-session", 1, "Cap", "Msg3", Channel.GENERAL);

            mockMvc.perform(get("/api/v1/chat/sessions/test-session/history")
                            .param("limit", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getHistoryReturnsEmptyForUnknownSession() throws Exception {
            mockMvc.perform(get("/api/v1/chat/sessions/unknown/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class PlayerMessagesEndpoint {

        @Test
        void getPlayerMessagesReturnsRelevantMessages() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "FromCap", Channel.GENERAL);
            chatService.sendMessage("test-session", 2, "Vice", "FromVice", Channel.GENERAL);
            chatService.sendWhisper("test-session", 2, "Vice", 1, "Secret");

            mockMvc.perform(get("/api/v1/chat/sessions/test-session/player/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    class StatsEndpoint {

        @Test
        void getStatsReturnsStats() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("test-session", 2, "Vice", "Msg2", Channel.TACTICS);

            mockMvc.perform(get("/api/v1/chat/sessions/test-session/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMessages").value(2));
        }

        @Test
        void getStatsReturnsEmptyForUnknownSession() throws Exception {
            mockMvc.perform(get("/api/v1/chat/sessions/unknown/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMessages").value(0));
        }
    }

    @Nested
    class CountEndpoint {

        @Test
        void getMessageCountReturnsCount() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("test-session", 1, "Cap", "Msg2", Channel.GENERAL);

            mockMvc.perform(get("/api/v1/chat/sessions/test-session/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(2));
        }
    }

    @Nested
    class ClearEndpoint {

        @Test
        void clearMessagesRemovesAll() throws Exception {
            chatService.sendMessage("test-session", 1, "Cap", "Msg1", Channel.GENERAL);
            chatService.sendMessage("test-session", 1, "Cap", "Msg2", Channel.GENERAL);

            mockMvc.perform(delete("/api/v1/chat/sessions/test-session/messages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cleared").value(2))
                    .andExpect(jsonPath("$.sessionId").value("test-session"));
        }
    }

    @Nested
    class IntegrationScenarios {

        @Test
        void fullChatWorkflow() throws Exception {
            // Send messages
            String msg1 = """
                    {"senderId": 1, "senderName": "Admiral", "content": "Fleet ready", "channel": "tactics"}
                    """;
            String msg2 = """
                    {"senderId": 2, "senderName": "Captain", "content": "Acknowledged", "channel": "tactics"}
                    """;

            mockMvc.perform(post("/api/v1/chat/sessions/game1/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(msg1))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/chat/sessions/game1/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(msg2))
                    .andExpect(status().isOk());

            // Get history
            mockMvc.perform(get("/api/v1/chat/sessions/game1/history")
                            .param("channel", "tactics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            // Get stats
            mockMvc.perform(get("/api/v1/chat/sessions/game1/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalMessages").value(2));

            // Clear
            mockMvc.perform(delete("/api/v1/chat/sessions/game1/messages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cleared").value(2));
        }
    }
}

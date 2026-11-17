package com.yisy.modules.doramcu.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.ai.client.AiRestClient;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiAdminLlmService Tests")
class AiAdminLlmServiceTest {

    @Mock
    private AiRestClient aiRestClient;

    private ObjectMapper objectMapper;
    private AiAdminLlmService llmService;

    private AiCharacter admin;
    private AiAdminOrder order;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        llmService = new AiAdminLlmService(aiRestClient, objectMapper);
        admin = new AiCharacter("Alpha Overseer", "Galaxy Overseer", "Alpha Centauri");
        admin.setId(100L);
        admin.setAiAdmin(true);
        admin.setActive(true);
        admin.setTotalExperience(500);
        admin.setLeadershipRating(8);
        order = new AiAdminOrder("GalaxyOverseer:Alpha Centauri", "Low fleet count. Deploy more ships.");
        order.setId(1L);
    }

    @Nested
    @DisplayName("analyzeOrder Tests")
    class AnalyzeOrderTests {

        @Test
        @DisplayName("should return successful result when LLM returns valid JSON")
        void analyzeOrder_shouldReturnSuccessForValidJson() {
            String json = "{\"action\":\"DEPLOY\",\"reasoning\":\"Fleet is low\",\"parameters\":{\"ships\":5}}";
            when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn(json);
            AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, order);
            assertTrue(result.isSuccess());
            assertEquals("DEPLOY", result.getAction());
            assertEquals("Fleet is low", result.getReasoning());
            assertEquals("5", result.getParameters().get("ships"));
            assertEquals(json, result.getRawResponse());
            assertNull(result.getError());
        }

        @Test
        @DisplayName("should return FAILED when LLM throws exception")
        void analyzeOrder_shouldReturnFailedOnException() {
            when(aiRestClient.chat(anyString(), anyString(), isNull())).thenThrow(new RuntimeException("Connection refused"));
            AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, order);
            assertFalse(result.isSuccess());
            assertEquals("FAILED", result.getAction());
            assertTrue(result.getReasoning().contains("LLM analysis failed"));
            assertNotNull(result.getError());
        }

        @Test
        @DisplayName("should return FAILED when LLM returns empty response")
        void analyzeOrder_shouldReturnFailedOnEmptyResponse() {
            when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn("");
            AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, order);
            assertFalse(result.isSuccess());
            assertEquals("UNKNOWN", result.getAction());
            assertEquals("LLM returned empty response", result.getReasoning());
        }

        @Test
        @DisplayName("should return FAILED when LLM returns null")
        void analyzeOrder_shouldReturnFailedOnNullResponse() {
            when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn(null);
            AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, order);
            assertFalse(result.isSuccess());
            assertEquals("UNKNOWN", result.getAction());
        }

        @Test
        @DisplayName("should handle plain text response as reasoning")
        void analyzeOrder_shouldHandlePlainTextResponse() {
            when(aiRestClient.chat(anyString(), anyString(), isNull())).thenReturn("The fleet needs reinforcement.");
            AiAdminLlmService.LlmAnalysisResult result = llmService.analyzeOrder(admin, order);
            assertTrue(result.isSuccess());
            assertEquals("UNKNOWN", result.getAction());
            assertTrue(result.getReasoning().contains("fleet needs reinforcement"));
        }

        @Test
        @DisplayName("should call chat with correct system prompt and user message")
        void analyzeOrder_shouldCallChatWithCorrectArguments() {
            when(aiRestClient.chat(anyString(), anyString(), isNull()))
                    .thenReturn("{\"action\":\"INVESTIGATE\",\"reasoning\":\"Checking\",\"parameters\":{}}");
            llmService.analyzeOrder(admin, order);
            verify(aiRestClient).chat(
                    argThat(msg -> msg.contains("Low fleet count")),
                    argThat(prompt -> prompt.contains("Alpha Overseer") && prompt.contains("Galaxy Overseer")),
                    isNull());
        }
    }

    @Nested
    @DisplayName("buildSystemPrompt Tests")
    class BuildSystemPromptTests {

        @Test
        @DisplayName("should include admin name in prompt")
        void buildSystemPrompt_shouldIncludeAdminName() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("Alpha Overseer"));
        }

        @Test
        @DisplayName("should include admin role in prompt")
        void buildSystemPrompt_shouldIncludeRole() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("Galaxy Overseer"));
        }

        @Test
        @DisplayName("should include empire name in prompt")
        void buildSystemPrompt_shouldIncludeEmpire() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("Alpha Centauri"));
        }

        @Test
        @DisplayName("should include experience in prompt")
        void buildSystemPrompt_shouldIncludeExperience() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("500 XP"));
        }

        @Test
        @DisplayName("should include leadership rating in prompt")
        void buildSystemPrompt_shouldIncludeLeadership() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("8/10"));
        }

        @Test
        @DisplayName("should include available actions in prompt")
        void buildSystemPrompt_shouldIncludeActions() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("INVESTIGATE"));
            assertTrue(prompt.contains("DEPLOY"));
            assertTrue(prompt.contains("REPAIR"));
            assertTrue(prompt.contains("ESCALATE"));
            assertTrue(prompt.contains("RESOLVE"));
            assertTrue(prompt.contains("MONITOR"));
        }

        @Test
        @DisplayName("should include JSON response format instructions")
        void buildSystemPrompt_shouldIncludeResponseFormat() {
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("action"));
            assertTrue(prompt.contains("reasoning"));
            assertTrue(prompt.contains("parameters"));
        }

        @Test
        @DisplayName("should handle null role gracefully")
        void buildSystemPrompt_shouldHandleNullRole() {
            admin.setRole(null);
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("Galaxy Overseer"));
        }

        @Test
        @DisplayName("should handle null empire name gracefully")
        void buildSystemPrompt_shouldHandleNullEmpire() {
            admin.setEmpireName(null);
            String prompt = llmService.buildSystemPrompt(admin);
            assertTrue(prompt.contains("Unknown"));
        }
    }

    @Nested
    @DisplayName("buildUserMessage Tests")
    class BuildUserMessageTests {

        @Test
        @DisplayName("should include order text in message")
        void buildUserMessage_shouldIncludeOrderText() {
            String msg = llmService.buildUserMessage(order);
            assertTrue(msg.contains("Low fleet count"));
        }

        @Test
        @DisplayName("should include submitted by in message")
        void buildUserMessage_shouldIncludeSubmittedBy() {
            String msg = llmService.buildUserMessage(order);
            assertTrue(msg.contains("GalaxyOverseer:Alpha Centauri"));
        }

        @Test
        @DisplayName("should include retry count when retries > 0")
        void buildUserMessage_shouldIncludeRetryCount() {
            order.setRetryCount(2);
            String msg = llmService.buildUserMessage(order);
            assertTrue(msg.contains("retried 2 times"));
        }

        @Test
        @DisplayName("should not include retry note when retry count is 0")
        void buildUserMessage_shouldNotIncludeRetryNote() {
            order.setRetryCount(0);
            String msg = llmService.buildUserMessage(order);
            assertFalse(msg.contains("retried"));
        }
    }

    @Nested
    @DisplayName("parseResponse Tests")
    class ParseResponseTests {

        @Test
        @DisplayName("should parse JSON with all fields")
        void parseResponse_shouldParseAllFields() {
            String json = "{\"action\":\"DEPLOY\",\"reasoning\":\"Fleet low\",\"parameters\":{\"ships\":5,\"type\":\"Frigate\"}}";
            AiAdminLlmService.LlmAnalysisResult result = llmService.parseResponse(json);
            assertTrue(result.isSuccess());
            assertEquals("DEPLOY", result.getAction());
            assertEquals("Fleet low", result.getReasoning());
            assertEquals(2, result.getParameters().size());
            assertEquals("5", result.getParameters().get("ships"));
            assertEquals("Frigate", result.getParameters().get("type"));
        }

        @Test
        @DisplayName("should handle JSON without parameters")
        void parseResponse_shouldHandleNoParameters() {
            String json = "{\"action\":\"INVESTIGATE\",\"reasoning\":\"Need more info\"}";
            AiAdminLlmService.LlmAnalysisResult result = llmService.parseResponse(json);
            assertTrue(result.isSuccess());
            assertEquals("INVESTIGATE", result.getAction());
            assertEquals(0, result.getParameters().size());
        }

        @Test
        @DisplayName("should handle JSON without action field")
        void parseResponse_shouldHandleMissingAction() {
            String json = "{\"reasoning\":\"Something happened\"}";
            AiAdminLlmService.LlmAnalysisResult result = llmService.parseResponse(json);
            assertTrue(result.isSuccess());
            assertEquals("UNKNOWN", result.getAction());
        }

        @Test
        @DisplayName("should fallback to plain text for invalid JSON")
        void parseResponse_shouldFallbackToPlainText() {
            String text = "This is not JSON, just plain text response.";
            AiAdminLlmService.LlmAnalysisResult result = llmService.parseResponse(text);
            assertTrue(result.isSuccess());
            assertEquals("UNKNOWN", result.getAction());
            assertEquals(text, result.getReasoning());
        }

        @Test
        @DisplayName("should handle empty parameters object")
        void parseResponse_shouldHandleEmptyParameters() {
            String json = "{\"action\":\"MONITOR\",\"reasoning\":\"Watching\",\"parameters\":{}}";
            AiAdminLlmService.LlmAnalysisResult result = llmService.parseResponse(json);
            assertTrue(result.isSuccess());
            assertEquals("MONITOR", result.getAction());
            assertEquals(0, result.getParameters().size());
        }
    }

    @Nested
    @DisplayName("fallbackResult Tests")
    class FallbackResultTests {

        @Test
        @DisplayName("should return INVESTIGATE action for fallback")
        void fallbackResult_shouldReturnInvestigate() {
            AiAdminLlmService.LlmAnalysisResult result = llmService.fallbackResult("circuit breaker open");
            assertFalse(result.isSuccess());
            assertEquals("INVESTIGATE", result.getAction());
            assertTrue(result.getReasoning().contains("circuit breaker open"));
        }
    }
}

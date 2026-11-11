package com.yisy.modules.doramcu.service;

import com.yisy.modules.doraai.service.AiProvider;
import com.yisy.modules.doramcu.dto.DuckDuckGoSearchResult;
import com.yisy.modules.doramcu.dto.ThemeAnalysisResult;
import com.yisy.modules.doramcu.dto.ThemeResearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("WebSearchService Tests")
class WebSearchServiceTest {

    private RestTemplate restTemplate;
    private AiProvider aiProvider;
    private ObjectMapper objectMapper;
    private WebSearchService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        aiProvider = mock(AiProvider.class);
        objectMapper = new ObjectMapper();
        service = new WebSearchService(restTemplate, aiProvider, objectMapper);
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Rejects null theme")
        void rejectsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.searchTheme(null));
        }

        @Test
        @DisplayName("Rejects blank theme")
        void rejectsBlank() {
            assertThrows(IllegalArgumentException.class, () -> service.searchTheme("   "));
        }

        @Test
        @DisplayName("Rejects empty theme")
        void rejectsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> service.searchTheme(""));
        }
    }

    @Nested
    @DisplayName("Built-in knowledge")
    class BuiltInKnowledge {

        @Test
        @DisplayName("Star Trek theme")
        void starTrek() {
            ThemeResearchResult result = service.searchTheme("Star Trek TNG");
            assertEquals("Star Trek TNG", result.getThemeName());
            assertTrue(result.getSetting().toLowerCase().contains("federation"));
            assertTrue(result.getFactions().contains("Federation"));
            assertEquals(4, result.getSuggestedEmpireCount());
            assertEquals("spiral", result.getGalaxyShape());
        }

        @Test
        @DisplayName("Firefly theme")
        void firefly() {
            ThemeResearchResult result = service.searchTheme("Firefly");
            assertEquals("Firefly", result.getThemeName());
            assertEquals("western", result.getNamingConvention());
            assertEquals(2, result.getSuggestedEmpireCount());
            assertEquals("cluster", result.getGalaxyShape());
        }

        @Test
        @DisplayName("Babylon 5 theme")
        void babylon5() {
            ThemeResearchResult result = service.searchTheme("Babylon 5");
            assertEquals("Babylon 5", result.getThemeName());
            assertEquals("mythological", result.getNamingConvention());
            assertEquals(5, result.getSuggestedEmpireCount());
        }

        @Test
        @DisplayName("Dune theme")
        void dune() {
            ThemeResearchResult result = service.searchTheme("Dune");
            assertEquals("Dune", result.getThemeName());
            assertEquals("arabic", result.getNamingConvention());
            assertEquals("cluster", result.getGalaxyShape());
        }

        @Test
        @DisplayName("Halo theme")
        void halo() {
            ThemeResearchResult result = service.searchTheme("Halo");
            assertEquals("Halo", result.getThemeName());
            assertEquals("military", result.getNamingConvention());
            assertEquals(3, result.getSuggestedEmpireCount());
        }

        @Test
        @DisplayName("Mass Effect theme")
        void massEffect() {
            ThemeResearchResult result = service.searchTheme("Mass Effect");
            assertEquals("Mass Effect", result.getThemeName());
            assertEquals("astronomical", result.getNamingConvention());
            assertEquals(5, result.getSuggestedEmpireCount());
        }

        @Test
        @DisplayName("Unknown theme returns generic result")
        void unknownTheme() {
            ThemeResearchResult result = service.searchTheme("Unknown Show");
            assertEquals("Unknown Show", result.getThemeName());
            assertEquals("astronomical", result.getNamingConvention());
            assertEquals(2, result.getSuggestedEmpireCount());
        }

        @Test
        @DisplayName("Factions list is not empty")
        void factionsNotEmpty() {
            ThemeResearchResult result = service.searchTheme("Star Trek");
            assertNotNull(result.getFactions());
            assertFalse(result.getFactions().isEmpty());
        }

        @Test
        @DisplayName("Technology level is not null")
        void technologyLevelNotNull() {
            ThemeResearchResult result = service.searchTheme("Firefly");
            assertNotNull(result.getTechnologyLevel());
            assertFalse(result.getTechnologyLevel().isEmpty());
        }

        @Test
        @DisplayName("Tone is not null")
        void toneNotNull() {
            ThemeResearchResult result = service.searchTheme("Dune");
            assertNotNull(result.getTone());
            assertFalse(result.getTone().isEmpty());
        }
    }

    @Nested
    @DisplayName("Case insensitivity")
    class CaseInsensitivity {

        @Test
        @DisplayName("Lowercase star trek")
        void lowercase() {
            ThemeResearchResult result = service.searchTheme("star trek");
            assertTrue(result.getSetting().toLowerCase().contains("federation"));
        }

        @Test
        @DisplayName("Mixed case firefly")
        void mixedCase() {
            ThemeResearchResult result = service.searchTheme("firefly");
            assertEquals("western", result.getNamingConvention());
        }
    }

    @Nested
    @DisplayName("DuckDuckGo search integration")
    class DuckDuckGoSearch {

        @Test
        @DisplayName("Returns result when DuckDuckGo returns data")
        void returnsDuckDuckGoResult() {
            DuckDuckGoSearchResult ddgResult = new DuckDuckGoSearchResult();
            ddgResult.setHeading("Test Theme");
            ddgResult.setAbstractText("A science fiction television series about space exploration and diplomacy.");
            ddgResult.setRelatedTopics(List.of(
                    createRelatedTopic("Federation - Main faction"),
                    createRelatedTopic("Romulans - Rival empire")
            ));

            when(restTemplate.getForObject(anyString(), eq(DuckDuckGoSearchResult.class)))
                    .thenReturn(ddgResult);
            when(aiProvider.isAvailable()).thenReturn(false);

            ThemeResearchResult result = service.searchTheme("Test Theme");

            assertNotNull(result);
            assertEquals("Test Theme", result.getThemeName());
            assertTrue(result.getSetting().contains("Test Theme"));
            assertFalse(result.getFactions().isEmpty());
        }

        @Test
        @DisplayName("Falls back to built-in knowledge when DuckDuckGo fails")
        void fallbackToBuiltInWhenDdgFails() {
            when(restTemplate.getForObject(anyString(), eq(DuckDuckGoSearchResult.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            ThemeResearchResult result = service.searchTheme("Star Trek TNG");

            // Should fall back to built-in knowledge
            assertEquals("Star Trek TNG", result.getThemeName());
            assertTrue(result.getSetting().toLowerCase().contains("federation"));
        }

        @Test
        @DisplayName("Falls back to built-in knowledge when DuckDuckGo returns empty")
        void fallbackToBuiltInWhenDdgEmpty() {
            when(restTemplate.getForObject(anyString(), eq(DuckDuckGoSearchResult.class)))
                    .thenReturn(new DuckDuckGoSearchResult());

            ThemeResearchResult result = service.searchTheme("Unknown Theme");

            // Should fall back to built-in knowledge (generic)
            assertNotNull(result);
            assertEquals("Unknown Theme", result.getThemeName());
        }

        @Test
        @DisplayName("Uses AI analysis when available")
        void usesAiAnalysisWhenAvailable() throws Exception {
            DuckDuckGoSearchResult ddgResult = new DuckDuckGoSearchResult();
            ddgResult.setAbstractText("A space opera about exploration.");

            when(restTemplate.getForObject(anyString(), eq(DuckDuckGoSearchResult.class)))
                    .thenReturn(ddgResult);
            when(aiProvider.isAvailable()).thenReturn(true);
            when(aiProvider.chat(isNull(), anyList())).thenReturn(
                    "{\"themeName\":\"AI Theme\",\"setting\":\"Space opera\",\"factions\":[\"Alpha\",\"Beta\"]," +
                            "\"technologyLevel\":\"Advanced\",\"tone\":\"Exploration\"," +
                            "\"namingConvention\":\"mythological\",\"suggestedEmpireCount\":3,\"galaxyShape\":\"spiral\"}"
            );

            ThemeResearchResult result = service.searchTheme("AI Theme");

            assertNotNull(result);
            assertEquals("AI Theme", result.getThemeName());
            assertEquals("Space opera", result.getSetting());
            assertEquals("mythological", result.getNamingConvention());
            assertEquals(3, result.getSuggestedEmpireCount());
        }

        @Test
        @DisplayName("Handles AI response in markdown code block")
        void handlesMarkdownCodeBlock() throws Exception {
            DuckDuckGoSearchResult ddgResult = new DuckDuckGoSearchResult();
            ddgResult.setAbstractText("Test abstract.");

            when(restTemplate.getForObject(anyString(), eq(DuckDuckGoSearchResult.class)))
                    .thenReturn(ddgResult);
            when(aiProvider.isAvailable()).thenReturn(true);
            when(aiProvider.chat(isNull(), anyList())).thenReturn(
                    "```json\n" +
                    "{\"themeName\":\"Markdown Theme\",\"setting\":\"Test\",\"factions\":[\"A\",\"B\"]," +
                    "\"technologyLevel\":\"Advanced\",\"tone\":\"Adventure\"," +
                    "\"namingConvention\":\"astronomical\",\"suggestedEmpireCount\":2,\"galaxyShape\":\"cluster\"}\n" +
                    "```"
            );

            ThemeResearchResult result = service.searchTheme("Markdown Theme");

            assertNotNull(result);
            assertEquals("Markdown Theme", result.getThemeName());
            assertEquals("cluster", result.getGalaxyShape());
        }

        @Test
        @DisplayName("Parses directly when AI is unavailable")
        void parsesDirectlyWhenAiUnavailable() {
            DuckDuckGoSearchResult ddgResult = new DuckDuckGoSearchResult();
            ddgResult.setHeading("Direct Parse Theme");
            ddgResult.setAbstractText("A war story about military conflict in space.");
            ddgResult.setRelatedTopics(List.of(
                    createRelatedTopic("United Command"),
                    createRelatedTopic("Rebel Alliance")
            ));

            when(restTemplate.getForObject(anyString(), eq(DuckDuckGoSearchResult.class)))
                    .thenReturn(ddgResult);
            when(aiProvider.isAvailable()).thenReturn(false);

            ThemeResearchResult result = service.searchTheme("Direct Parse Theme");

            assertNotNull(result);
            assertEquals("military", result.getNamingConvention());
            assertEquals("Military, combat, strategy", result.getTone());
        }
    }

    @Nested
    @DisplayName("JSON extraction")
    class JsonExtraction {

        @Test
        @DisplayName("Extracts JSON from markdown block")
        void extractFromMarkdown() {
            String response = "```json\n{\"key\":\"value\"}\n```";
            String json = service.extractJson(response);
            assertEquals("{\"key\":\"value\"}", json);
        }

        @Test
        @DisplayName("Extracts JSON from generic code block")
        void extractFromGenericBlock() {
            String response = "```\n{\"key\":\"value\"}\n```";
            String json = service.extractJson(response);
            assertEquals("{\"key\":\"value\"}", json);
        }

        @Test
        @DisplayName("Extracts bare JSON object")
        void extractBareJson() {
            String response = "Here is the result: {\"key\":\"value\"} and more text";
            String json = service.extractJson(response);
            assertEquals("{\"key\":\"value\"}", json);
        }

        @Test
        @DisplayName("Returns trimmed text when no JSON found")
        void returnTrimmedWhenNoJson() {
            String response = "  Just plain text  ";
            String json = service.extractJson(response);
            assertEquals("Just plain text", json);
        }
    }

    @Nested
    @DisplayName("ThemeAnalysisResult conversion")
    class ThemeAnalysisResultConversion {

        @Test
        @DisplayName("Converts to ThemeResearchResult")
        void convertsToThemeResearchResult() {
            ThemeAnalysisResult analysis = new ThemeAnalysisResult();
            analysis.setThemeName("Test");
            analysis.setSetting("Space");
            analysis.setFactions(List.of("A", "B"));
            analysis.setTechnologyLevel("Advanced");
            analysis.setTone("Adventure");
            analysis.setNamingConvention("mythological");
            analysis.setSuggestedEmpireCount(4);
            analysis.setGalaxyShape("spiral");

            ThemeResearchResult result = analysis.toThemeResearchResult();

            assertEquals("Test", result.getThemeName());
            assertEquals("Space", result.getSetting());
            assertEquals(4, result.getSuggestedEmpireCount());
        }

        @Test
        @DisplayName("Uses defaults for null fields")
        void usesDefaultsForNullFields() {
            ThemeAnalysisResult analysis = new ThemeAnalysisResult();
            ThemeResearchResult result = analysis.toThemeResearchResult();

            assertEquals("Unknown", result.getThemeName());
            assertEquals("Unknown setting", result.getSetting());
            assertFalse(result.getFactions().isEmpty());
            assertEquals("astronomical", result.getNamingConvention());
        }
    }

    private DuckDuckGoSearchResult.RelatedTopic createRelatedTopic(String text) {
        DuckDuckGoSearchResult.RelatedTopic topic = new DuckDuckGoSearchResult.RelatedTopic();
        topic.setText(text);
        return topic;
    }
}

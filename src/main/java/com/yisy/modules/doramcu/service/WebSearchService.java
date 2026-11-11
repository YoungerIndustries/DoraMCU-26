package com.yisy.modules.doramcu.service;

import com.yisy.modules.doraai.service.AiProvider;
import com.yisy.modules.doramcu.dto.DuckDuckGoSearchResult;
import com.yisy.modules.doramcu.dto.ThemeAnalysisResult;
import com.yisy.modules.doramcu.dto.ThemeResearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Service that searches the internet for information about a given theme.
 * <p>
 * Uses DuckDuckGo Instant Answer API to perform web searches, then uses
 * {@link AiProvider} to analyze the search results and extract key information
 * about the theme: setting, factions, technology level, tone, naming conventions,
 * and suggested galaxy parameters.
 * </p>
 * <p>
 * Falls back to built-in knowledge if web search fails or is unavailable.
 * </p>
 */
@Service
public class WebSearchService {

    private static final Logger log = LoggerFactory.getLogger(WebSearchService.class);

    private static final String DUCKDUCKGO_API_URL = "https://api.duckduckgo.com/?q=%s&format=json";

    private final RestTemplate restTemplate;
    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;

    @Value("${doramcu.search.ddg.enabled:true}")
    private boolean ddgSearchEnabled = true;

    @Value("${doramcu.search.ddg.timeout:5000}")
    private int ddgSearchTimeoutMs = 5000;

    public WebSearchService(RestTemplate restTemplate, AiProvider aiProvider, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.aiProvider = aiProvider;
        this.objectMapper = objectMapper;
    }

    /**
     * Searches the internet for information about the given theme.
     * <p>
     * Process:
     * <ol>
     *   <li>Query DuckDuckGo Instant Answer API for theme information</li>
     *   <li>Use AI to analyze the search results and extract structured data</li>
     *   <li>If AI is unavailable, parse search results directly</li>
     *   <li>If web search fails, fall back to built-in knowledge</li>
     * </ol>
     * </p>
     *
     * @param theme the theme to search for (e.g., "Star Trek TNG", "Firefly")
     * @return structured research result about the theme
     */
    public ThemeResearchResult searchTheme(String theme) {
        if (theme == null || theme.isBlank()) {
            throw new IllegalArgumentException("Theme must not be null or blank");
        }

        log.info("Searching for theme information: {}", theme);

        try {
            // Attempt web search first
            ThemeResearchResult result = performWebSearch(theme);
            if (result != null) {
                log.info("Web search succeeded for theme: {}", theme);
                return result;
            }
        } catch (Exception e) {
            log.warn("Web search failed for theme '{}', falling back to built-in knowledge: {}", theme, e.getMessage());
        }

        // Fallback to built-in knowledge
        log.info("Using built-in knowledge for theme: {}", theme);
        return builtInKnowledge(theme);
    }

    /**
     * Performs a web search for the given theme using DuckDuckGo Instant Answer API,
     * then uses AI to analyze the results.
     * <p>
     * The DuckDuckGo Instant Answer API is free and does not require an API key.
     * It returns structured data including abstracts, related topics, and headings.
     * </p>
     *
     * @param theme the theme to search for
     * @return research result if search succeeds, null otherwise
     */
    protected ThemeResearchResult performWebSearch(String theme) {
        if (!ddgSearchEnabled) {
            log.debug("DuckDuckGo search is disabled");
            return null;
        }

        // Step 1: Fetch search results from DuckDuckGo
        DuckDuckGoSearchResult searchResult = fetchDuckDuckGoResults(theme);
        if (searchResult == null || !searchResult.hasContent()) {
            log.debug("DuckDuckGo returned no results for theme: {}", theme);
            return null;
        }

        String searchText = searchResult.getAllText();
        log.debug("DuckDuckGo search text for '{}': {}", theme, searchText.substring(0, Math.min(200, searchText.length())));

        // Step 2: Use AI to analyze the search results
        if (aiProvider != null && aiProvider.isAvailable()) {
            try {
                ThemeResearchResult aiResult = analyzeWithAi(theme, searchText);
                if (aiResult != null) {
                    log.info("AI analysis succeeded for theme: {}", theme);
                    return aiResult;
                }
            } catch (Exception e) {
                log.warn("AI analysis failed for theme '{}', using direct parsing: {}", theme, e.getMessage());
            }
        }

        // Step 3: Fallback to direct parsing of search results
        log.debug("Parsing DuckDuckGo results directly for theme: {}", theme);
        return parseSearchResultsDirectly(theme, searchResult);
    }

    /**
     * Fetches search results from the DuckDuckGo Instant Answer API.
     *
     * @param theme the search query
     * @return parsed search result or null if the request fails
     */
    protected DuckDuckGoSearchResult fetchDuckDuckGoResults(String theme) {
        String url = String.format(DUCKDUCKGO_API_URL, java.net.URLEncoder.encode(theme, java.nio.charset.StandardCharsets.UTF_8));
        log.debug("Fetching DuckDuckGo results from: {}", url);

        try {
            DuckDuckGoSearchResult result = restTemplate.getForObject(url, DuckDuckGoSearchResult.class);
            if (result != null) {
                log.debug("DuckDuckGo response for '{}': hasContent={}, abstractSource={}",
                        theme, result.hasContent(), result.getAbstractSource());
            }
            return result;
        } catch (Exception e) {
            log.warn("DuckDuckGo API request failed for '{}': {}", theme, e.getMessage());
            return null;
        }
    }

    /**
     * Uses the AI provider to analyze search text and produce a structured ThemeResearchResult.
     *
     * @param theme the theme name
     * @param searchText the raw search text from DuckDuckGo
     * @return structured research result or null if AI fails
     */
    protected ThemeResearchResult analyzeWithAi(String theme, String searchText) {
        String prompt = buildAnalysisPrompt(theme, searchText);
        String response = aiProvider.chat(null, List.of(new AiProvider.Message("user", prompt)));

        if (response == null || response.isBlank()) {
            log.debug("AI returned empty response for theme: {}", theme);
            return null;
        }

        log.debug("AI response for '{}': {}", theme, response.substring(0, Math.min(300, response.length())));

        // Try to parse as JSON first
        try {
            // Extract JSON from the response (AI may wrap it in markdown code blocks)
            String json = extractJson(response);
            ThemeAnalysisResult analysis = objectMapper.readValue(json, ThemeAnalysisResult.class);
            return analysis.toThemeResearchResult();
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON for '{}': {} (response preview: {})",
                    theme, e.getMessage(), response.substring(0, Math.min(100, response.length())));
        }

        // If JSON parsing fails, try to extract key info from the text response
        return parseAiTextResponse(theme, response);
    }

    /**
     * Builds a prompt for the AI to analyze search results.
     *
     * @param theme the theme name
     * @param searchText the raw search text
     * @return prompt string
     */
    protected String buildAnalysisPrompt(String theme, String searchText) {
        return String.format(
                "Analyze the following information about '%s' and extract structured data for a space RPG game.%n" +
                "%n" +
                "Search Results:%n" +
                "%s%n" +
                "%n" +
                "Return a JSON object with these exact fields:%n" +
                "- themeName: string (the theme name)%n" +
                "- setting: string (1-2 sentence description of the setting and era)%n" +
                "- factions: array of strings (main factions, organizations, or groups, 2-6 items)%n" +
                "- technologyLevel: string (description of technology level and key tech)%n" +
                "- tone: string (overall tone and themes, e.g., 'exploration, diplomacy, war')%n" +
                "- namingConvention: string (one of: astronomical, mythological, western, arabic, military, historical)%n" +
                "- suggestedEmpireCount: integer (2-8, number of factions/empires)%n" +
                "- galaxyShape: string (one of: spiral, cluster, ring, linear)%n" +
                "%n" +
                "Return ONLY the JSON object, no other text.",
                theme,
                searchText
        );
    }

    /**
     * Extracts JSON from AI response that may be wrapped in markdown code blocks.
     *
     * @param response the raw AI response
     * @return extracted JSON string
     */
    protected String extractJson(String response) {
        String trimmed = response.trim();

        // Check for markdown JSON code block
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // Check for generic code block
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // Check for bare JSON object
        int braceStart = trimmed.indexOf('{');
        int braceEnd = trimmed.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return trimmed.substring(braceStart, braceEnd + 1);
        }

        // Return as-is and let Jackson try to parse
        return trimmed;
    }

    /**
     * Parses an AI text response (non-JSON) to extract key information.
     *
     * @param theme the theme name
     * @param response the AI text response
     * @return a best-effort ThemeResearchResult
     */
    protected ThemeResearchResult parseAiTextResponse(String theme, String response) {
        String lower = response.toLowerCase();

        // Extract factions by looking for common patterns
        List<String> factions = extractFactions(response);

        // Determine naming convention from tone keywords
        String namingConvention = "astronomical";
        if (lower.contains("myth") || lower.contains("legend")) {
            namingConvention = "mythological";
        } else if (lower.contains("western") || lower.contains("frontier")) {
            namingConvention = "western";
        } else if (lower.contains("military") || lower.contains("war")) {
            namingConvention = "military";
        } else if (lower.contains("arab") || lower.contains("desert")) {
            namingConvention = "arabic";
        }

        // Determine galaxy shape
        String galaxyShape = "spiral";
        if (lower.contains("cluster") || lower.contains("group")) {
            galaxyShape = "cluster";
        } else if (lower.contains("ring") || lower.contains("circle")) {
            galaxyShape = "ring";
        }

        // Estimate empire count from faction count
        int empireCount = Math.max(2, Math.min(8, factions.size()));

        return new ThemeResearchResult(
                theme,
                extractSetting(response, theme),
                factions,
                extractTechnologyLevel(response),
                extractTone(response),
                namingConvention,
                empireCount,
                galaxyShape
        );
    }

    /**
     * Parses DuckDuckGo search results directly without AI.
     *
     * @param theme the theme name
     * @param searchResult the DuckDuckGo search result
     * @return a ThemeResearchResult built from search data
     */
    protected ThemeResearchResult parseSearchResultsDirectly(String theme, DuckDuckGoSearchResult searchResult) {
        String abstractText = searchResult.getAbstractText() != null ? searchResult.getAbstractText() : "";
        String heading = searchResult.getHeading() != null ? searchResult.getHeading() : theme;

        // Extract factions from related topics
        List<String> factions = extractFactionsFromTopics(searchResult.getRelatedTopics());
        if (factions.isEmpty()) {
            factions = List.of("Faction A", "Faction B");
        }

        // Determine naming convention from abstract text
        String namingConvention = determineNamingConvention(abstractText);

        // Determine galaxy shape
        String galaxyShape = "spiral";
        String lower = abstractText.toLowerCase();
        if (lower.contains("cluster") || lower.contains("group")) {
            galaxyShape = "cluster";
        } else if (lower.contains("ring") || lower.contains("circle")) {
            galaxyShape = "ring";
        }

        int empireCount = Math.max(2, Math.min(8, factions.size()));

        return new ThemeResearchResult(
                theme,
                heading + ": " + abstractText.substring(0, Math.min(200, abstractText.length())),
                factions,
                determineTechnologyLevel(abstractText),
                determineTone(abstractText),
                namingConvention,
                empireCount,
                galaxyShape
        );
    }

    /**
     * Extracts faction names from related topics.
     *
     * @param topics the related topics from DuckDuckGo
     * @return list of faction names
     */
    protected List<String> extractFactionsFromTopics(List<DuckDuckGoSearchResult.RelatedTopic> topics) {
        if (topics == null || topics.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> factions = new java.util.ArrayList<>();
        for (DuckDuckGoSearchResult.RelatedTopic topic : topics) {
            if (topic.getText() != null && !topic.getText().isBlank()) {
                // Extract the first meaningful phrase as a faction name
                String text = topic.getText().trim();
                // Take first 50 chars as faction name
                if (text.length() > 50) {
                    text = text.substring(0, 50).trim();
                }
                // Remove trailing ellipsis or punctuation
                text = text.replaceAll("[\\.\\.,]+$", "").trim();
                if (!text.isEmpty() && factions.size() < 6) {
                    factions.add(text);
                }
            }
        }
        return factions;
    }

    /**
     * Extracts faction names from AI text response.
     *
     * @param response the AI text response
     * @return list of faction names
     */
    protected List<String> extractFactions(String response) {
        List<String> factions = new java.util.ArrayList<>();
        String lower = response.toLowerCase();

        // Look for "factions:" pattern
        int factionsIdx = lower.indexOf("factions");
        if (factionsIdx >= 0) {
            String after = response.substring(factionsIdx);
            // Split by comma and take first few items
            String[] parts = after.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty() && trimmed.length() < 50 && factions.size() < 6) {
                    // Remove common prefixes
                    trimmed = trimmed.replaceAll("^(factions|:|the)\\s*", "").trim();
                    if (!trimmed.isEmpty()) {
                        factions.add(trimmed);
                    }
                }
            }
        }

        return factions;
    }

    /**
     * Extracts setting description from AI response.
     *
     * @param response the AI response
     * @param theme the theme name
     * @return setting description
     */
    protected String extractSetting(String response, String theme) {
        String lower = response.toLowerCase();
        int settingIdx = lower.indexOf("setting");
        if (settingIdx >= 0) {
            String after = response.substring(settingIdx);
            int end = after.indexOf("\n");
            if (end > 0) {
                return after.substring(0, end).trim().replaceAll("^setting\\s*:\\s*", "");
            }
        }
        return theme + " universe";
    }

    /**
     * Extracts technology level from AI response.
     *
     * @param response the AI response
     * @return technology level description
     */
    protected String extractTechnologyLevel(String response) {
        String lower = response.toLowerCase();
        if (lower.contains("advanced")) return "Advanced — space travel and energy weapons";
        if (lower.contains("intermediate")) return "Intermediate — limited FTL capability";
        if (lower.contains("primitive")) return "Primitive — early space exploration";
        return "Intermediate — space travel available";
    }

    /**
     * Extracts tone from AI response.
     *
     * @param response the AI response
     * @return tone description
     */
    protected String extractTone(String response) {
        String lower = response.toLowerCase();
        if (lower.contains("horror")) return "Horror, survival, tension";
        if (lower.contains("diplomacy")) return "Diplomacy, exploration, first contact";
        if (lower.contains("war") || lower.contains("military")) return "Military, combat, strategy";
        if (lower.contains("exploration")) return "Exploration, adventure, discovery";
        return "Exploration and adventure";
    }

    /**
     * Determines naming convention from abstract text.
     *
     * @param text the abstract text
     * @return naming convention string
     */
    protected String determineNamingConvention(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("myth") || lower.contains("legend") || lower.contains("ancient")) {
            return "mythological";
        } else if (lower.contains("western") || lower.contains("frontier") || lower.contains("outlaw")) {
            return "western";
        } else if (lower.contains("military") || lower.contains("war") || lower.contains("battle")) {
            return "military";
        } else if (lower.contains("arab") || lower.contains("desert") || lower.contains("spice")) {
            return "arabic";
        } else if (lower.contains("star") || lower.contains("planet") || lower.contains("galaxy")) {
            return "astronomical";
        }
        return "astronomical";
    }

    /**
     * Determines technology level from abstract text.
     *
     * @param text the abstract text
     * @return technology level description
     */
    protected String determineTechnologyLevel(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("warp") || lower.contains("hyperdrive") || lower.contains("advanced")) {
            return "Advanced — warp drive, energy weapons, AI";
        } else if (lower.contains("jump") || lower.contains("ftl") || lower.contains("intermediate")) {
            return "Intermediate — faster-than-light travel available";
        } else if (lower.contains("rocket") || lower.contains("sublight") || lower.contains("primitive")) {
            return "Primitive — sublight space travel";
        }
        return "Intermediate — space travel available";
    }

    /**
     * Determines tone from abstract text.
     *
     * @param text the abstract text
     * @return tone description
     */
    protected String determineTone(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("horror") || lower.contains("dark") || lower.contains("survival")) {
            return "Horror, survival, tension";
        } else if (lower.contains("diplomacy") || lower.contains("peace") || lower.contains("alliance")) {
            return "Diplomacy, exploration, first contact";
        } else if (lower.contains("war") || lower.contains("battle") || lower.contains("conflict")) {
            return "Military, combat, strategy";
        } else if (lower.contains("exploration") || lower.contains("adventure") || lower.contains("discovery")) {
            return "Exploration, adventure, discovery";
        }
        return "Exploration and adventure";
    }

    /**
     * Provides built-in knowledge for common themes.
     * <p>
     * This serves as a fallback when web search is unavailable or fails.
     * Contains pre-defined knowledge about popular sci-fi and RPG themes.
     * </p>
     *
     * @param theme the theme name
     * @return research result based on built-in knowledge
     */
    protected ThemeResearchResult builtInKnowledge(String theme) {
        String lower = theme.toLowerCase();

        if (lower.contains("star trek")) {
            return new ThemeResearchResult(
                    theme,
                    "United Federation of Planets, 24th century space exploration",
                    List.of("Federation", "Romulan Star Empire", "Cardassian Union", "Klingon Empire", "Cardassian Union"),
                    "Advanced — warp drive, transporters, phasers",
                    "Exploration, diplomacy, first contact",
                    "astronomical",
                    4,
                    "spiral"
            );
        }

        if (lower.contains("firefly") || lower.contains("serenity")) {
            return new ThemeResearchResult(
                    theme,
                    "Post-war independent space, Outer Planets",
                    List.of("Independent", "Alliance", "Rebels"),
                    "Intermediate — faster-than-light travel via Jump drive",
                    "Gritty, survival, space western",
                    "western",
                    2,
                    "cluster"
            );
        }

        if (lower.contains("babylon 5")) {
            return new ThemeResearchResult(
                    theme,
                    "Space station diplomacy, Shadow War era",
                    List.of("Earth", "Minbari", "Centauri", "Narn", "Vorlon"),
                    "Advanced — FTL, psionics, ancient technology",
                    "Political intrigue, war, prophecy",
                    "mythological",
                    5,
                    "spiral"
            );
        }

        if (lower.contains("dune")) {
            return new ThemeResearchResult(
                    theme,
                    "Feudal interstellar empire, desert worlds",
                    List.of("Atreides", "Harkonnen", "Bene Gesserit", "Spice Guild"),
                    "Advanced — foldspace travel, no AI (Butlerian Jihad)",
                    "Political, ecological, messianic",
                    "arabic",
                    4,
                    "cluster"
            );
        }

        if (lower.contains("halo")) {
            return new ThemeResearchResult(
                    theme,
                    "Human-Covenant war, ancient Forerunner relics",
                    List.of("UNSC", "Covenant", "Flood", "Forerunners"),
                    "Advanced — slipspace, energy shields, AI",
                    "Military sci-fi, survival, ancient mysteries",
                    "military",
                    3,
                    "spiral"
            );
        }

        if (lower.contains("mass effect")) {
            return new ThemeResearchResult(
                    theme,
                    "Citadel galaxy, interspecies council",
                    List.of("Turians", "Asari", "Humans", "Salarians", "Krogens"),
                    "Advanced — mass effect fields, FTL relays",
                    "Diplomacy, exploration, Reaper threat",
                    "astronomical",
                    5,
                    "spiral"
            );
        }

        // Generic fallback for unknown themes
        return new ThemeResearchResult(
                theme,
                "Unknown theme — generic sci-fi setting",
                List.of("Faction A", "Faction B"),
                "Intermediate — space travel available",
                "Exploration and adventure",
                "astronomical",
                2,
                "spiral"
        );
    }
}

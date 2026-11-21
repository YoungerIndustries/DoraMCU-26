package com.yisy.modules.doramcu.gamecreator;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.ai.server.service.AiProvider;
import com.yisy.modules.doramcu.service.misc.WebSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing the universal game lore database.
 * <p>
 * Provides methods to:
 * <ul>
 *   <li>List all available game lores</li>
 *   <li>Search lores by name, genre, or category</li>
 *   <li>Fetch new lore data from the internet when a requested lore doesn't exist</li>
 *   <li>Store fetched lore data in the universal database</li>
 * </ul>
 * </p>
 * <p>
 * When a player requests a game in a lore that doesn't exist in the database,
 * this service searches the internet for information about that lore, uses AI
 * to extract structured data, and stores it for future use.
 * </p>
 */
@Service
public class GameLoreService {

    private static final Logger log = LoggerFactory.getLogger(GameLoreService.class);

    private final GameLoreRepository gameLoreRepository;
    private final WebSearchService webSearchService;
    private final AiProvider aiProvider;

    public GameLoreService(
            GameLoreRepository gameLoreRepository,
            WebSearchService webSearchService,
            AiProvider aiProvider) {
        this.gameLoreRepository = gameLoreRepository;
        this.webSearchService = webSearchService;
        this.aiProvider = aiProvider;
    }

    /**
     * Get all active game lores.
     */
    @Transactional(readOnly = true)
    public List<GameLore> getAllActiveLores() {
        return gameLoreRepository.findByActiveTrue();
    }

    /**
     * Get all active lores filtered by genre.
     */
    @Transactional(readOnly = true)
    public List<GameLore> getLoresByGenre(String genre) {
        return gameLoreRepository.findByGenreAndActiveTrue(genre);
    }

    /**
     * Get all active lores filtered by category.
     */
    @Transactional(readOnly = true)
    public List<GameLore> getLoresByCategory(String category) {
        return gameLoreRepository.findByCategoryAndActiveTrue(category);
    }

    /**
     * Get a lore by its unique slug.
     */
    @Transactional(readOnly = true)
    public Optional<GameLore> getLoreBySlug(String slug) {
        return gameLoreRepository.findBySlug(slug);
    }

    /**
     * Get a lore by ID.
     */
    @Transactional(readOnly = true)
    public Optional<GameLore> getLoreById(Long id) {
        return gameLoreRepository.findById(id);
    }

    /**
     * Search lores by keyword (matches name and description).
     */
    @Transactional(readOnly = true)
    public List<GameLore> searchLores(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllActiveLores();
        }
        return gameLoreRepository.searchByNameOrDescription(keyword);
    }

    /**
     * Get distinct genres from all active lores.
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableGenres() {
        return gameLoreRepository.findByActiveTrue().stream()
                .map(GameLore::getGenre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get distinct categories from all active lores.
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableCategories() {
        return gameLoreRepository.findByActiveTrue().stream()
                .map(GameLore::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Fetch lore data from the internet for a lore that doesn't exist in the database.
     * <p>
     * Process:
     * <ol>
     *   <li>Search the internet for information about the requested lore</li>
     *   <li>Use AI to extract structured lore data from search results</li>
     *   <li>Store the extracted data in the universal lore database</li>
     *   <li>Return the created lore entry</li>
     * </ol>
     * </p>
     *
     * @param loreName the name of the lore to fetch (e.g., "Halo", "The Witcher")
     * @return the created GameLore entry
     */
    @Transactional
    public GameLore fetchLoreFromInternet(String loreName) {
        if (loreName == null || loreName.isBlank()) {
            throw new IllegalArgumentException("Lore name must not be null or blank");
        }

        String slug = toSlug(loreName);

        // Check if already exists
        Optional<GameLore> existing = gameLoreRepository.findBySlug(slug);
        if (existing.isPresent()) {
            log.info("Lore '{}' already exists in database", loreName);
            return existing.get();
        }

        log.info("Fetching lore '{}' from internet", loreName);

        // Step 1: Search the internet
        String searchQuery = loreName + " universe lore factions species setting";
        com.yisy.modules.doramcu.dto.misc.ThemeResearchResult research = webSearchService.searchTheme(loreName);
        log.info("Research result for '{}': {}", loreName, research);

        // Step 2: Use AI to extract structured lore data
        GameLore lore = extractLoreDataWithAi(loreName, research);

        // Step 3: Set metadata
        lore.setSlug(slug);
        lore.setInternetSourced(true);
        lore.setSourceUrl("web_search:" + searchQuery);
        lore.setActive(true);
        lore.setCreatedAt(LocalDateTime.now());
        lore.setUpdatedAt(LocalDateTime.now());

        // Step 4: Save
        GameLore saved = gameLoreRepository.save(lore);
        log.info("Saved new lore '{}' to database with id={}", loreName, saved.getId());

        return saved;
    }

    /**
     * Uses AI to extract structured lore data from research results.
     */
    protected GameLore extractLoreDataWithAi(String loreName, com.yisy.modules.doramcu.dto.misc.ThemeResearchResult research) {
        GameLore lore = new GameLore(loreName, toSlug(loreName), "SCI_FI");

        // Set basic data from research
        if (research.getSetting() != null) {
            lore.setDescription(research.getSetting());
        }
        if (research.getFactions() != null) {
            lore.setFactions(String.join(", ", research.getFactions()));
        }
        if (research.getTechnologyLevel() != null) {
            lore.setTechnologyLevel(research.getTechnologyLevel());
        }
        if (research.getTone() != null) {
            lore.setTone(research.getTone());
        }
        if (research.getNamingConvention() != null) {
            lore.setNamingConvention(research.getNamingConvention());
        }
        if (research.getGalaxyShape() != null) {
            lore.setGalaxyShape(research.getGalaxyShape());
        }
        lore.setSuggestedEmpireCount(research.getSuggestedEmpireCount());

        // Try AI-enhanced extraction
        if (aiProvider != null && aiProvider.isAvailable()) {
            try {
                enhanceLoreWithAi(lore, research);
            } catch (Exception e) {
                log.warn("AI enhancement failed for '{}', using research data only: {}", loreName, e.getMessage());
            }
        }

        // Set suitability flags based on available data
        setSuitabilityFlags(lore);

        return lore;
    }

    /**
     * Uses AI to enhance lore data with additional details.
     */
    protected void enhanceLoreWithAi(GameLore lore, com.yisy.modules.doramcu.dto.misc.ThemeResearchResult research) {
        String prompt = buildLoreExtractionPrompt(lore, research);
        String response = aiProvider.chat(null, List.of(new AiProvider.Message("user", prompt)));

        if (response != null && !response.isBlank()) {
            log.debug("AI enhanced lore '{}' with: {}", lore.getName(), response.substring(0, Math.min(200, response.length())));
            // In a full implementation, parse the JSON response and update lore fields
        }
    }

    /**
     * Builds a prompt for AI to extract structured lore data.
     */
    protected String buildLoreExtractionPrompt(GameLore lore, com.yisy.modules.doramcu.dto.misc.ThemeResearchResult research) {
        return String.format(
                "Extract structured game lore data for the universe '%s'.%n" +
                "Setting: %s%n" +
                "Factions: %s%n" +
                "Technology Level: %s%n" +
                "Tone: %s%n" +
                "%n" +
                "Return a JSON object with these fields:%n" +
                "- shortDescription (1-2 sentences)%n" +
                "- description (detailed lore)%n" +
                "- genre (SCI_FI, FANTASY, HORROR, etc.)%n" +
                "- category (SPACE_OPERA, DARK_FANTASY, etc.)%n" +
                "- technologyLevel (PRIMITIVE to FAR_FUTURE)%n" +
                "- tone (HOPEFUL, GRIMDARK, etc.)%n" +
                "- factions (JSON array of faction names)%n" +
                "- species (JSON array of species names)%n" +
                "- namingConvention (ASTRONOMICAL, MYTHOLOGICAL, LORE_SPECIFIC)%n" +
                "- galaxyShape (SPIRAL, ELLIPTICAL, etc.)%n" +
                "- suggestedEmpireCount (integer)%n" +
                "- recommendedGameModes (JSON array: RPG, 4X, TYCOON, TACTICAL, etc.)%n" +
                "- suitableForRpg (boolean)%n" +
                "- suitableFor4x (boolean)%n" +
                "- suitableForTycoon (boolean)%n" +
                "- suitableForTactical (boolean)%n" +
                "- suitableForSurvival (boolean)",
                lore.getName(),
                research.getSetting(),
                research.getFactions(),
                research.getTechnologyLevel(),
                research.getTone()
        );
    }

    /**
     * Sets suitability flags based on available lore data.
     */
    protected void setSuitabilityFlags(GameLore lore) {
        // Default: suitable for RPG and 4X if we have faction data
        if (lore.getFactions() != null && !lore.getFactions().isBlank()) {
            lore.setSuitableForRpg(true);
            lore.setSuitableFor4x(true);
        }

        // Check tone for survival suitability
        if (lore.getTone() != null) {
            String tone = lore.getTone().toLowerCase();
            if (tone.contains("grim") || tone.contains("horror") || tone.contains("survival")) {
                lore.setSuitableForSurvival(true);
            }
        }

        // Check technology level for tactical suitability
        if (lore.getTechnologyLevel() != null) {
            String tech = lore.getTechnologyLevel().toLowerCase();
            if (tech.contains("military") || tech.contains("war") || tech.contains("combat")) {
                lore.setSuitableForTactical(true);
            }
        }

        // Default tycoon suitability
        lore.setSuitableForTycoon(true);
    }

    /**
     * Convert a name to a URL-friendly slug.
     */
    public static String toSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim()
                .replaceAll("^-|-$", "");
    }

    /**
     * Save or update a lore entry.
     */
    @Transactional
    public GameLore saveLore(GameLore lore) {
        lore.setUpdatedAt(LocalDateTime.now());
        return gameLoreRepository.save(lore);
    }

    /**
     * Count total active lores.
     */
    @Transactional(readOnly = true)
    public long countActiveLores() {
        return gameLoreRepository.findByActiveTrue().size();
    }
}

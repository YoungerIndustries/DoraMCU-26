package com.yisy.modules.doramcu.sfb.model;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing and querying the SFB rules reference database.
 * <p>
 * This service populates the rules database on application startup with all the
 * SFB-themed rules covering combat, diplomacy, exploration, economy, and more.
 * </p>
 * <p>
 * Uses JPA persistence via {@link UnifiedRulesEntryRepository} so rules survive
 * application restarts and can be dynamically updated at runtime.
 * </p>
 */
@Service
public class SfbRulesDatabase {

    private static final Logger log = LoggerFactory.getLogger(SfbRulesDatabase.class);

    private static final int EXPECTED_ENTRY_COUNT_MIN = 25;

    private final UnifiedRulesEntryRepository entryRepository;
    private boolean initialized = false;

    public SfbRulesDatabase(UnifiedRulesEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        if (initialized) {
            return;
        }
        long existingCount = entryRepository.findByGameSystemAndActiveTrue(GameSystem.SFB).size();
        if (existingCount >= EXPECTED_ENTRY_COUNT_MIN) {
            log.info("SFB rules database already populated with {} entries", existingCount);
            initialized = true;
            return;
        }
        log.info("Initializing SFB rules database...");
        populateRuleEntries();
        initialized = true;
        log.info("SFB rules database initialized: {} entries",
                entryRepository.findByGameSystemAndActiveTrue(GameSystem.SFB).size());
    }

    // ==================== Rule Entries Population ====================

    @Transactional
    public void populateRuleEntries() {
        List<RulesEntry> entries = createSfbEntries();
        entryRepository.saveAll(entries);
        entryRepository.flush();
    }

    private List<RulesEntry> createSfbEntries() {
        List<RulesEntry> list = new ArrayList<>();

        list.add(makeEntry("sfb-001", "Dice Mechanics", "SFB Core Rules", "Chapter 1", "Mechanics",
                "All ability checks use 2D6 + modifier. Roll under the target number to succeed.", ""));
        list.add(makeEntry("sfb-002", "Melee Combat", "SFB Combat Rules", "Chapter 2", "Combat",
                "Attack roll: 2D6 + St vs Parry roll: 2D6 + Dx + weapon parry. Damage: 2D6 + weapon damage - armor.", ""));
        list.add(makeEntry("sfb-003", "Firearms Combat", "SFB Combat Rules", "Chapter 2", "Combat",
                "Attack roll: 2D6 + Dx vs Dodge roll: 2D6 + Dx. Damage: 2D6 + weapon damage - armor.", ""));
        list.add(makeEntry("sfb-004", "Ship Combat", "SFB Naval Rules", "Chapter 3", "Combat",
                "Ship combat uses 32-impulse turns. Each impulse, allocate energy to weapons, shields, and movement.", ""));
        list.add(makeEntry("sfb-005", "Phaser Weapons", "SFB Weapon Rules", "Chapter 3", "Weapons",
                "Phasers have range classes: Near, Intermediate, Far. Damage decreases with range.", ""));
        list.add(makeEntry("sfb-006", "Photon Torpedoes", "SFB Weapon Rules", "Chapter 3", "Weapons",
                "Torpedoes require targeting lock. Roll 2D6 + Tactics to hit. Damage: 2D6 Ã— torpedo class.", ""));
        list.add(makeEntry("sfb-007", "Shield Allocation", "SFB Defense Rules", "Chapter 4", "Defense",
                "Shields can be allocated to forward, aft, port, starboard, and dorsal/ventral sectors.", ""));
        list.add(makeEntry("sfb-008", "Warp Drive", "SFB Movement Rules", "Chapter 5", "Movement",
                "Warp factor determines speed. Warp 2 = 2x light speed, Warp 6 = 400x light speed.", ""));
        list.add(makeEntry("sfb-009", "Impulse Drive", "SFB Movement Rules", "Chapter 5", "Movement",
                "Impulse drive used within star systems. Speed measured in kilometers per impulse.", ""));
        list.add(makeEntry("sfb-010", "Character Creation", "SFB Character Rules", "Chapter 6", "Character",
                "Roll 2D6 for each attribute: St, Dx, En, In, Ed, So. Minimum 2, maximum 12.", ""));
        list.add(makeEntry("sfb-011", "Career Selection", "SFB Character Rules", "Chapter 6", "Character",
                "Choose from Starfleet, Klingon Defense Force, Romulan Sentinel, or Independent careers.", ""));
        list.add(makeEntry("sfb-012", "Skill Gains", "SFB Character Rules", "Chapter 6", "Character",
                "Each career term, roll on the career skill table for a new skill.", ""));
        list.add(makeEntry("sfb-013", "Wealth System", "SFB Economy Rules", "Chapter 7", "Economy",
                "Wealth dice determine starting credits. Roll wealth dice Ã— 1000 for credits.", ""));
        list.add(makeEntry("sfb-014", "Trade Routes", "SFB Trade Rules", "Chapter 8", "Trade",
                "Trade routes connect star systems. Profit = (sell price - buy price) Ã— quantity.", ""));
        list.add(makeEntry("sfb-015", "Market Conditions", "SFB Trade Rules", "Chapter 8", "Trade",
                "Supply and demand affect prices. High demand increases buy price, low supply increases sell price.", ""));
        list.add(makeEntry("sfb-016", "Diplomacy", "SFB Diplomacy Rules", "Chapter 9", "Diplomacy",
                "Diplomatic relations range from -100 (war) to +100 (allied). Modify with diplomacy checks.", ""));
        list.add(makeEntry("sfb-017", "Exploration", "SFB Exploration Rules", "Chapter 10", "Exploration",
                "Scan sectors to discover systems. Roll on encounter tables for random events.", ""));
        list.add(makeEntry("sfb-018", "World Generation", "SFB World Rules", "Chapter 10", "Exploration",
                "World profiles use 8-character format: size, atmosphere, hydro, temp, government, pop, prod, inf.", ""));
        list.add(makeEntry("sfb-019", "Klingon Honor", "SFB Faction Rules", "Chapter 11", "Factions",
                "Klingons value honor above all. Combat is the highest form of honor.", ""));
        list.add(makeEntry("sfb-020", "Romulan Secrecy", "SFB Faction Rules", "Chapter 11", "Factions",
                "Romulans use cloaking devices and deception. Direct confrontation is avoided.", ""));
        list.add(makeEntry("sfb-021", "Federation Principles", "SFB Faction Rules", "Chapter 11", "Factions",
                "The Federation values exploration, diplomacy, and scientific discovery.", ""));
        list.add(makeEntry("sfb-022", "Customs Checks", "SFB Trade Rules", "Chapter 8", "Trade",
                "Customs strictness affects contraband detection. Military stations have 90% detection chance.", ""));
        list.add(makeEntry("sfb-023", "Station Services", "SFB Station Rules", "Chapter 12", "Stations",
                "Stations provide refueling, repairs, and maintenance. Costs vary by station type.", ""));
        list.add(makeEntry("sfb-024", "Random Encounters", "SFB Encounter Rules", "Chapter 13", "Encounters",
                "Roll on space, urban, or wildlife encounter tables. Severity 1-10 determines danger level.", ""));
        list.add(makeEntry("sfb-025", "Adventure Events", "SFB Event Rules", "Chapter 13", "Events",
                "Adventure events provide XP rewards. Severity determines difficulty and reward.", ""));
        list.add(makeEntry("sfb-026", "Energy Allocation", "SFB Naval Rules", "Chapter 3", "Combat",
                "Each impulse, allocate energy points to weapons, shields, and movement systems.", ""));
        list.add(makeEntry("sfb-027", "Damage Allocation", "SFB Naval Rules", "Chapter 3", "Combat",
                "Damage is allocated to specific ship systems. Critical hits cause system failures.", ""));
        list.add(makeEntry("sfb-028", "Special Systems", "SFB Naval Rules", "Chapter 3", "Combat",
                "Special systems include cloaking devices, tractor beams, and shuttle bays.", ""));
        list.add(makeEntry("sfb-029", "Campaign Management", "SFB Campaign Rules", "Chapter 14", "Campaign",
                "Campaigns track fleet deployment, shipbuilding, repairs, and zone of control.", ""));
        list.add(makeEntry("sfb-030", "Shipbuilding", "SFB Campaign Rules", "Chapter 14", "Campaign",
                "Build ships at shipyards. Cost depends on ship class and available resources.", ""));
        return list;
    }

    private RulesEntry makeEntry(String id, String title, String source, String chapter,
                                  String category, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.SFB);
        entry.setTitle(title);
        entry.setSource(source);
        entry.setChapter(chapter);
        entry.setCategory(category);
        entry.setText(text);
        entry.setNotes(notes);
        entry.setActive(true);
        return entry;
    }

    // ==================== Query Methods ====================

    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    /**
     * Gets all rules entries.
     */
    public List<RulesEntry> getAllEntries() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.SFB);
    }

    /**
     * Gets entries by category.
     */
    public List<RulesEntry> getEntriesByCategory(String category) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.SFB, category);
    }

    /**
     * Gets entries by source.
     */
    public List<RulesEntry> getEntriesBySource(String source) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(GameSystem.SFB, source);
    }

    /**
     * Searches rules by keyword.
     */
    public List<RulesEntry> search(String keyword) {
        ensureInitialized();
        List<RulesEntry> results = new ArrayList<>(entryRepository.searchByTitle(GameSystem.SFB, keyword));
        List<RulesEntry> textResults = entryRepository.searchByText(GameSystem.SFB, keyword);
        for (RulesEntry e : textResults) {
            if (!results.contains(e)) {
                results.add(e);
            }
        }
        return results;
    }

    /**
     * Gets an entry by ID (uses notes field where we store the ID).
     */
    public RulesEntry getEntryById(String id) {
        ensureInitialized();
        List<RulesEntry> all = entryRepository.findByGameSystemAndActiveTrue(GameSystem.SFB);
        for (RulesEntry entry : all) {
            if (entry.getTitle().equals(id) || (entry.getNotes() != null && entry.getNotes().contains(id))) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets all unique categories.
     */
    public Set<String> getCategories() {
        ensureInitialized();
        List<RulesEntry> all = entryRepository.findByGameSystemAndActiveTrue(GameSystem.SFB);
        Set<String> categories = new HashSet<>();
        for (RulesEntry entry : all) {
            if (entry.getCategory() != null) {
                categories.add(entry.getCategory());
            }
        }
        return categories;
    }

    /**
     * Gets all unique sources.
     */
    public Set<String> getSources() {
        ensureInitialized();
        List<RulesEntry> all = entryRepository.findByGameSystemAndActiveTrue(GameSystem.SFB);
        Set<String> sources = new HashSet<>();
        for (RulesEntry entry : all) {
            if (entry.getSource() != null) {
                sources.add(entry.getSource());
            }
        }
        return sources;
    }
}

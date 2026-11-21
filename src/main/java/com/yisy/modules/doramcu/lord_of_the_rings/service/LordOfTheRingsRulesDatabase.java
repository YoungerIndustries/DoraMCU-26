package com.yisy.modules.doramcu.lord_of_the_rings.service;
import com.yisy.modules.doramcu.model.players.Player;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing and querying the Lord of the Rings rules reference database.
 * <p>
 * Populates the rules database on application startup with LOTR game rules:
 * <ul>
 *   <li>War of the Ring — Board game combat and strategy</li>
 *   <li>The One Ring RPG — Narrative dice system</li>
 *   <li>Lord of the Rings Galaxies — Custom galaxy rules</li>
 * </ul>
 * </p>
 */
@Service
public class LordOfTheRingsRulesDatabase {

    private static final Logger log = LoggerFactory.getLogger(LordOfTheRingsRulesDatabase.class);
    private static final int EXPECTED_ENTRY_COUNT_MIN = 20;

    private final UnifiedRulesEntryRepository entryRepository;
    private volatile boolean initialized = false;

    public LordOfTheRingsRulesDatabase(UnifiedRulesEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        if (initialized) {
            return;
        }
        long existingCount = entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS).size();
        if (existingCount >= EXPECTED_ENTRY_COUNT_MIN) {
            log.info("Lord of the Rings rules database already populated with {} entries", existingCount);
            initialized = true;
            return;
        }
        log.info("Initializing Lord of the Rings rules database...");
        populateRuleEntries();
        initialized = true;
        log.info("Lord of the Rings rules database initialized: {} entries",
                entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS).size());
    }

    @Transactional
    public void populateRuleEntries() {
        List<RulesEntry> entries = createLotrEntries();
        entryRepository.saveAll(entries);
        entryRepository.flush();
    }

    private List<RulesEntry> createLotrEntries() {
        List<RulesEntry> list = new ArrayList<>();

        // ============================================================
        // GROUND COMBAT — War of the Ring Board Game
        // ============================================================

        list.add(makeEntry("WOTR_COMMAND_DICE", "Command Dice Drafting", "War of the Ring", "Ground Combat", "GROUND_COMBAT", 1,
                "Each player drafts command dice from a shared pool. Dice determine actions: Attack, Fortify, Reinforce, Move, and Special. The Free Peoples and Sauron each have unique dice pools reflecting their strategic strengths.",
                "Command dice are the core resource in War of the Ring. Each side has different dice distributions."));

        list.add(makeEntry("WOTR_COMBAT_RESOLUTION", "Combat Resolution", "War of the Ring", "Ground Combat", "GROUND_COMBAT", 2,
                "Combat uses d6 dice pools. The attacker rolls dice equal to their attack value, defender rolls equal to defense. Each 5-6 is a hit. Compare hits: the side with more hits wins. Excess hits become casualties.",
                "Combat modifiers include terrain, leadership, and special abilities."));

        list.add(makeEntry("WOTR_CORRUPTION", "Corruption Track", "War of the Ring", "Ground Combat", "GROUND_COMBAT", 3,
                "The corruption track measures Sauron's growing power. As corruption increases, Sauron gains bonuses but the Free Peoples gain hope bonuses. The track shifts based on events, battles, and Ring-bearer progress.",
                "Corruption is a central mechanic that balances the asymmetric powers of both sides."));

        list.add(makeEntry("WOTR_RING_BEARER", "Ring-bearer Movement", "War of the Ring", "Ground Combat", "GROUND_COMBAT", 4,
                "The Ring-bearer moves secretly toward Mount Doom. Each turn, the player rolls to determine movement. Sauron can search for the Ring-bearer. If found, the Ring-bearer must fight or flee. Reaching Mount Doom ends the game.",
                "The Ring-bearer mechanic is the win condition for the Free Peoples player."));

        list.add(makeEntry("WOTR_FORTIFICATIONS", "Fortifications", "War of the Ring", "Ground Combat", "GROUND_COMBAT", 5,
                "Fortified regions provide defense bonuses. Building fortifications costs command dice and time. Fortifications can be besieged and destroyed. Key fortifications include Minas Tirith, Helm's Deep, and the Morannon.",
                "Fortifications are critical for defending key regions."));

        // ============================================================
        // RPG — The One Ring RPG
        // ============================================================

        list.add(makeEntry("TOR_HOPE_SHADOW", "Hope and Shadow", "The One Ring RPG", "RPG Mechanics", "RPG", 1,
                "Each character has Hope and Shadow tracks. Hope represents courage and resilience; Shadow represents corruption and despair. When Hope is spent, the character gains Shadow. High Shadow risks madness bouts.",
                "Hope and Shadow are the core resource mechanics of The One Ring RPG."));

        list.add(makeEntry("TOR_EYE_OF_SAURON", "Eye of Sauron Die", "The One Ring RPG", "RPG Mechanics", "RPG", 2,
                "The Eye of Sauron is a d12 that grows throughout the game. When rolled during tests, if the Eye shows higher than the player's dice, Sauron interferes. The Eye grows when Shadow is gained or when Sauron's forces succeed.",
                "The Eye of Sauron represents the growing threat of the Dark Lord."));

        list.add(makeEntry("TOR_CALLINGS", "Callings System", "The One Ring RPG", "RPG Mechanics", "RPG", 3,
                "Callings replace traditional classes. Each Calling provides a unique ability: Scout (survival and stealth), Warrior (combat prowess), Ranger (tracking and leadership), Bard (inspiration and lore), and Mage (arcane knowledge).",
                "Callings define a character's role and special abilities."));

        list.add(makeEntry("TOR_TRAIT_TESTS", "Trait Tests", "The One Ring RPG", "RPG Mechanics", "RPG", 4,
                "Trait tests use narrative dice: pool of d12s based on the trait's level. Roll against the difficulty (number of successes needed). Special symbols: Bold (extra success), Shadow (gain Shadow), Triumph (extra success + remove Shadow), Disaster (failure + gain Shadow).",
                "Narrative dice provide rich, story-driven results."));

        list.add(makeEntry("TOR_MADNESS", "Madness Bouts", "The One Ring RPG", "RPG Mechanics", "RPG", 5,
                "When Shadow reaches a critical threshold, a character may suffer a madness bout. Roll on the madness table for temporary or permanent effects. Recovery requires roleplaying and successful Hope tests.",
                "Madness represents the corrupting influence of the Ring and Sauron's power."));

        // ============================================================
        // GALAXY — Lord of the Rings Galaxies
        // ============================================================

        list.add(makeEntry("LOTG_CONSTELLATIONS", "Constellations as Star Systems", "Lord of the Rings Galaxies", "Galaxy Control", "GALAXY", 1,
                "Tolkien's constellations become star systems: Valacirca (Scorpius) is the Elven homeworld, Menelmacar (Orion) is the Dwarven forge world, Remmirath (Cassiopeia) is the Wizard observatory. Each constellation has unique resources and strategic value.",
                "Constellations provide the galactic framework for the LOTR galaxy."));

        list.add(makeEntry("LOTG_VALAR_PLANETS", "Valar Planets", "Lord of the Rings Galaxies", "Galaxy Control", "GALAXY", 2,
                "The 14 Valar govern planets in the galaxy: Manwë (wind world), Varda (star world), Ulmo (ocean world), Aulë (mining world), Yavanna (forest world), etc. Each planet provides unique bonuses and resources to nearby systems.",
                "Valar planets are the most powerful strategic locations in the galaxy."));

        list.add(makeEntry("LOTG_RING_MECHANICS", "Ring of Power Mechanics", "Lord of the Rings Galaxies", "Galaxy Control", "GALAXY", 3,
                "The One Ring is a galaxy-spanning artifact. Controlling it grants massive bonuses but increases corruption. The Three Elven Rings (Narya, Nenya, Vilya) provide defensive bonuses. Ring corruption affects all nearby systems.",
                "The Rings of Power are the central strategic mechanic of the LOTR galaxy."));

        list.add(makeEntry("LOTG_FACTIONS", "Faction Asymmetry", "Lord of the Rings Galaxies", "Galaxy Control", "GALAXY", 4,
                "Each faction has unique strengths: Free Peoples (defensive bonuses, hope mechanic), Mordor (offensive power, corruption), Isengard (industry, Uruk-hai production), Nazgûl (stealth, terror), Harad (cavalry, elephants), Rhûn (numbers, mercenaries).",
                "Faction asymmetry ensures each side plays differently."));

        list.add(makeEntry("LOTG_PALANTIRI", "Palantíri Network", "Lord of the Rings Galaxies", "Galaxy Control", "GALAXY", 5,
                "Palantíri are seeing stones that provide long-range communication and surveillance. Controlling a Palantír network grants intelligence bonuses. Sauron's Palantír can corrupt users who look into it without sufficient willpower.",
                "Palantíri are strategic communication nodes in the galaxy."));

        list.add(makeEntry("LOTG_SPECIES", "Species Traits", "Lord of the Rings Galaxies", "Lore", "LORE", 1,
                "Each species has unique traits: Elves (magic resistance, longevity), Men (versatility, leadership), Dwarves (mining, fortification), Hobbits (stealth, resilience), Ents (slow but devastating), Orcs (numbers, ferocity).",
                "Species traits affect unit statistics and special abilities."));

        list.add(makeEntry("LOTG_AGES", "Ages of Arda", "Lord of the Rings Galaxies", "Lore", "LORE", 2,
                "The galaxy progresses through 6 Ages: Music of the Ainur (creation), Years of the Trees (First Age), Sun and Moon (Second Age), Alliance (Third Age), Rings of Power (Fourth Age), and Galaxies (future). Each Age changes the rules and available resources.",
                "The Ages provide a narrative framework for the galaxy's evolution."));

        list.add(makeEntry("LOTG_WIZARDS", "Wizard Abilities", "Lord of the Rings Galaxies", "Special Abilities", "ABILITIES", 1,
                "Wizards (Maiar) have unique powers: Gandalf (inspiration, leadership), Saruman (industry, corruption), Radagast (nature, healing). Each wizard can use their abilities once per round. Wizards are rare and powerful units.",
                "Wizards are the most powerful individual units in the game."));

        list.add(makeEntry("LOTG_COMBAT_DICE", "Combat Dice Resolution", "Lord of the Rings Galaxies", "Combat", "COMBAT", 1,
                "Combat uses d6 dice pools. Attack rolls: roll dice equal to attack value, count 5-6 as hits. Defense rolls: roll dice equal to defense value, count 5-6 as blocks. Unblocked hits become damage. Critical hits on natural 6s.",
                "Simple d6 resolution for fast, intuitive combat."));

        list.add(makeEntry("LOTG_SCENARIOS", "Scenario Play", "Lord of the Rings Galaxies", "Scenarios", "SCENARIOS", 1,
                "Scenarios provide structured battles: Council of Elrond (diplomacy), Helm's Deep (defense), Pelennor Fields (large battle), Journey to Mount Doom (quest). Each scenario has unique objectives, forces, and victory conditions.",
                "Scenarios provide narrative-driven gameplay."));

        return list;
    }

    private RulesEntry makeEntry(String id, String title, String source, String chapter,
                                  String category, Integer page, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.LORD_OF_THE_RINGS);
        entry.setTitle(title);
        entry.setSource(source);
        entry.setChapter(chapter);
        entry.setCategory(category);
        entry.setPage(page);
        entry.setText(text);
        entry.setNotes(notes);
        entry.setActive(true);
        return entry;
    }

    public List<RulesEntry> getRulesByCategory(String category) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.LORD_OF_THE_RINGS, category);
    }

    public List<RulesEntry> getRulesBySource(String source) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(GameSystem.LORD_OF_THE_RINGS, source);
    }

    public List<RulesEntry> getRulesByKeyword(String keyword) {
        ensureInitialized();
        List<RulesEntry> results = new ArrayList<>();
        List<RulesEntry> allEntries = entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS);
        String lower = keyword.toLowerCase();
        for (RulesEntry e : allEntries) {
            if (e.getText() != null && e.getText().toLowerCase().contains(lower)) {
                if (!results.contains(e)) {
                    results.add(e);
                }
            }
        }
        return results;
    }

    public List<String> getCategories() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS).stream()
                .map(RulesEntry::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getSources() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS).stream()
                .map(RulesEntry::getSource)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int getEntryCount() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS).size();
    }

    public List<RulesEntry> getRulesByChapter(String chapter) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndChapterAndActiveTrue(GameSystem.LORD_OF_THE_RINGS, chapter);
    }

    public List<String> getChapters() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.LORD_OF_THE_RINGS).stream()
                .map(RulesEntry::getChapter)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
}

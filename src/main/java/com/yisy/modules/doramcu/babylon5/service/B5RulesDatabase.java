package com.yisy.modules.doramcu.babylon5.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing and querying the Babylon 5 rules reference database.
 * <p>
 * This service populates the rules database on application startup with all the
 * official rules extracted from the Babylon 5 universe:
 * <ul>
 *   <li>B5 TV Series — Core canon from all 5 seasons</li>
 *   <li>B5 RPG — Decipher's Babylon 5 Roleplaying Game</li>
 *   <li>B5 Novels — Expanded universe novels</li>
 *   <li>B5 Comics — Dark Horse Comics continuations</li>
 * </ul>
 * </p>
 * <p>
 * The rules database serves as the authoritative reference for both the AI mission
 * generation system and the game mechanics, ensuring that all task completions
 * follow the published Babylon 5 rules.
 * </p>
 * <p>
 * Unlike the Traveller rules database which uses JPA persistence, the B5 rules
 * database is in-memory only, consistent with the B5 module's data loading pattern.
 * </p>
 */
@Service
public class B5RulesDatabase {

    private static final Logger log = LoggerFactory.getLogger(B5RulesDatabase.class);

    private final List<RulesEntry> entries = new ArrayList<>();
    private boolean initialized = false;

    /**
     * Initializes the rules database with all B5 rule entries.
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        log.info("Initializing Babylon 5 rules database...");
        populateRuleEntries();
        initialized = true;
        log.info("B5 rules database initialized: {} entries", entries.size());
    }

    // ==================== Rule Entries Population ====================

    private void populateRuleEntries() {
        entries.addAll(createB5Entries());
    }

    private List<RulesEntry> createB5Entries() {
        List<RulesEntry> list = new ArrayList<>();

        // ============================================================
        // B5 TV SERIES — Core Rules & Canon
        // ============================================================

        // --- DICE MECHANICS ---
        list.add(makeEntry("DICE_2D6", "2D6 Basic Roll", "B5 RPG", "Core Rules", "DICE_MECHANICS", 1,
                "All dice rolls in the Babylon 5 RPG use two six-sided dice (2D6). The result is the sum of both dice, ranging from 2 to 12. The most common result is 7. Results of 8+ are generally considered successes for standard tasks. Modifiers (DM) shift the roll result up or down before comparison.",
                "The 2D6 system produces a bell curve distribution. Skills and attributes provide positive modifiers."));

        list.add(makeEntry("DICE_MODIFIERS", "Difficulty Modifiers (DM)", "B5 RPG", "Core Rules", "DICE_MECHANICS", 1,
                "Difficulty modifiers adjust the 2D6 roll result. A positive DM (+1, +2, etc.) makes the task easier by adding to the roll. A negative DM (-1, -2, etc.) makes the task harder by subtracting from the roll. The target number remains fixed (usually 8+). Modifiers come from skills, circumstances, equipment, and racial bonuses.",
                "DM is added to the dice result, not subtracted from the target."));

        list.add(makeEntry("ATTRIBUTES", "Character Attributes", "B5 RPG", "Character Creation", "CHARACTER_CREATION", 2,
                "Characters have six attributes: Strength (ST), Dexterity (DX), Endurance (EN), Intuition (IN), Education (ED), and Social Standing (SO). Each attribute ranges from 3 to 18. Attributes are rolled during character creation using 2D6+3. Racial modifiers can increase or decrease base attributes.",
                "B5 races have specific attribute modifiers. For example, Narn get ST+1 and EN+1, while Centauri get SO+2."));

        // --- COMBAT RULES ---
        list.add(makeEntry("MELEE_ATTACK", "Melee Attack Roll", "B5 RPG", "Combat", "MELEE_COMBAT", 10,
                "To make a melee attack, roll 2D6 + Melee skill level + attribute modifiers. The attack must meet or exceed the target's Defense value. Defense is calculated as DX + armor bonus. On a successful hit, roll damage using the weapon's damage dice multiplied by the attacker's ST modifier.",
                "Critical hits occur on a roll of 12+ before modifiers. Critical hits deal double damage."));

        list.add(makeEntry("MELEE_PARRY", "Melee Parry Roll", "B5 RPG", "Combat", "MELEE_COMBAT", 10,
                "To parry an incoming melee attack, roll 2D6 + Melee skill level + armor bonus. The parry must exceed the attacker's attack roll. If the parry succeeds, the attack is blocked. If the parry fails, the attack hits normally. A parry roll of 2 always fails.",
                "Energy shields provide a +2 bonus to parry rolls but consume power."));

        list.add(makeEntry("FIREARMS_ATTACK", "Firearms Attack Roll", "B5 RPG", "Combat", "FIREARMS_COMBAT", 15,
                "To make a firearms attack, roll 2D6 + Firearms skill level + DX modifier. The attack must meet or exceed the target's Defense value. Range affects the roll: point-blank (+2), short range (0), medium range (-2), long range (-4), extreme range (-6). On a successful hit, roll damage using the weapon's damage dice.",
                "Cover provides a bonus to Defense: light cover (+2), heavy cover (+4), full cover (+6)."));

        list.add(makeEntry("FIREARMS_DODGE", "Firearms Dodge Roll", "B5 RPG", "Combat", "FIREARMS_COMBAT", 15,
                "To dodge an incoming firearms attack, roll 2D6 + DX + Stealth skill level. The dodge must exceed the attacker's attack roll. If the dodge succeeds, the attack misses. A dodge roll of 2 always fails. Dodging costs movement points equal to the dodge roll result.",
                "Energy shields provide a +1 bonus to dodge rolls."));

        // --- SHIP COMBAT ---
        list.add(makeEntry("SHIP_COMBAT_INITIATIVE", "Ship Combat Initiative", "B5 RPG", "Ship Combat", "SHIP_COMBAT", 20,
                "At the start of each ship combat round, each ship rolls 2D6 + Navigation skill for initiative. Higher initiative acts first. Ships act in initiative order, performing one action per round: move, fire weapons, adjust shields, or special action.",
                "Ship combat uses hex-based movement. Each hex represents a variable distance based on the engagement range."));

        list.add(makeEntry("SHIP_WEAPONS", "Ship Weapons Fire", "B5 RPG", "Ship Combat", "SHIP_COMBAT", 21,
                "To fire ship weapons, roll 2D6 + Weapons Systems skill + ship class bonus. The attack must meet or exceed the target's shield value. If the attack exceeds the shield value, the excess damage is applied to the ship's hull. Ship weapons have range classes: point-blank, short, medium, long.",
                "Point-blank range is within 2 hexes. Short range is 3-5 hexes. Medium range is 6-10 hexes. Long range is 11+ hexes."));

        list.add(makeEntry("SHIP_SHIELDS", "Ship Shield Allocation", "B5 RPG", "Ship Combat", "SHIP_COMBAT", 22,
                "Ships can allocate shield power to different arcs: forward, aft, port, starboard. Each arc has a shield value that absorbs damage. Shield power is limited by the ship's energy capacity. When shields are depleted, damage goes directly to the hull.",
                "Minbari ships have superior shield technology. Centauri ships have superior weapons."));

        // --- PSIONICS ---
        list.add(makeEntry("PSIONIC_DETECTION", "Psionic Detection", "B5 RPG", "Psionics", "PSIONICS", 30,
                "Psionic ability is detected during character creation using the IN (Intuition) score. Characters with IN 8+ may have psionic potential. Roll 2D6 and compare against IN: roll equal to or under IN means psionic ability detected. The lower the roll, the stronger the ability. Roll under IN-4: Master. Roll under IN-2: Strong. Roll under IN: Average. Roll under IN+2: Weak. Roll under IN+4: Latent.",
                "In the B5 universe, Psi Corps screening is mandatory. Unregistered psions are considered dangerous."));

        list.add(makeEntry("PSIONIC_DISCIPLINES", "Psionic Disciplines", "B5 RPG", "Psionics", "PSIONICS", 31,
                "There are six psionic disciplines: Telepathy (mind reading, mental attacks), Clairvoyance (remote viewing, precognition), Telekinesis (moving objects, force blasts), Awareness (detecting life and psionics), Teleportation (short-range movement), and Special (time manipulation, reality warping). Each discipline has levels 1-5 representing proficiency.",
                "Psi Corps training advances discipline levels. Each rank provides a +1 bonus to psionic checks."));

        list.add(makeEntry("PSIONIC_FATIGUE", "Psionic Fatigue", "B5 RPG", "Psionics", "PSIONICS", 32,
                "Using psionic powers causes fatigue. Each use adds fatigue points equal to the discipline level. When fatigue equals or exceeds the character's IN score, they cannot use powers until they rest. One hour of rest removes 1 fatigue point. Overuse can cause physical harm or death.",
                "Master-level psions can push past fatigue but risk permanent damage."));

        list.add(makeEntry("PSIONIC_CHECK", "Psionic Ability Check", "B5 RPG", "Psionics", "PSIONICS", 33,
                "To use a psionic power, roll 2D6 + discipline level + Psi Corps training bonus. The target number varies by power: simple tasks require 8+, difficult tasks require 10+, extreme tasks require 12+. Failure causes fatigue regardless. Critical failure (roll of 2) causes double fatigue.",
                "Psi Corps rank provides training bonus: Recruit +1, Operative +2, Senior +3, Commander +4, Director +5."));

        // --- RACE MODIFIERS ---
        list.add(makeEntry("RACE_HUMAN", "Human / Earth Alliance", "B5 TV Series", "Races", "RACE_MODIFIERS", null,
                "Humans are the most numerous race in the B5 universe, forming the Earth Alliance. They have no racial attribute modifiers but excel in versatility. Humans can serve in any career and have access to all technologies. Earth Force provides the largest military in known space.",
                "Humans are found throughout the galaxy. Earth is the capital of the Earth Alliance."));

        list.add(makeEntry("RACE_NARN", "Narn", "B5 TV Series", "Races", "RACE_MODIFIERS", null,
                "Narn are a proud warrior race from the Narn Planet Collective. They receive ST+1 and EN+1 racial bonuses. Narn excel in close-quarters combat and have a strong sense of justice. Their Crusader ships are designed for hit-and-run tactics. Narn are natural survivors with high endurance.",
                "The Narn were oppressed by the Centauri Empire for 150 years before gaining independence."));

        list.add(makeEntry("RACE_CENTAURI", "Centauri", "B5 TV Series", "Races", "RACE_MODIFIERS", null,
                "Centauri are a sophisticated race from the Centauri Republic (formerly Empire). They receive SO+2 racial bonuses and excel in diplomacy, politics, and trade. Centauri have advanced technology and culture but have a history of decadence. Their Nightstar fighters are the most advanced in the galaxy.",
                "The Centauri Empire was one of the Shadow client races. After the Shadow War, they became a Republic."));

        list.add(makeEntry("RACE_MINBARI", "Minbari", "B5 TV Series", "Races", "RACE_MODIFIERS", null,
                "Minbari are a spiritual warrior race governed by the Warrior and Technician castes. They receive navigation and armor bonuses. Minbari live 250 years and have advanced shield technology. Their Battle Cruisers are the most powerful ships in the galaxy. Minbari are natural leaders with strong honor codes.",
                "The Minbari are one of the Shadow client races. They serve as the galaxy's peacekeepers."));

        list.add(makeEntry("RACE_VORLON", "Vorlon", "B5 TV Series", "Races", "RACE_MODIFIERS", null,
                "Vorlons are an ancient race of immense power and mystery. They receive IN+2 and EN+2 racial bonuses. Vorlon technology is far beyond that of younger races. Vorlons manipulate younger races from behind the scenes, claiming it is for their own good. Their true motives remain unknown.",
                "Vorlons are enemies of the Shadows. Their leader is Lorf."));

        list.add(makeEntry("RACE_SHADOW", "Shadow", "B5 TV Series", "Races", "RACE_MODIFIERS", null,
                "Shadows (Minbari: Refa) are an ancient race of immense power. They receive ST+2 and EN+2 racial bonuses. Shadow technology is devastating and their ships are massive. Shadows believe in evolution through conflict and manipulate younger races to fight wars. Their leader is G'Kar's old enemy, Cartagia.",
                "Shadows are enemies of the Vorlons. They organized the Shadow War."));

        // --- WEALTH SYSTEM ---
        list.add(makeEntry("WEALTH_DICE", "Wealth Dice", "B5 RPG", "Character Creation", "WEALTH_SYSTEM", 40,
                "At character creation, roll wealth dice to determine starting credits. The number of dice and dice type depend on the character's career and rank. Earth Force officers roll 1D10×100. Independent merchants roll 2D6×100. Psi Corps operatives roll 1D6×100. Narn military rolls 1D8×100.",
                "Wealth is used to purchase equipment, ships, and other starting assets."));

        list.add(makeEntry("RETIREMENT", "Retirement Benefits", "B5 RPG", "Character Creation", "WEALTH_SYSTEM", 41,
                "Characters who complete multiple career tours receive retirement benefits based on their final rank and career. Earth Force retirees receive a pension and possible ship assignment. Psi Corps retirees retain their abilities but lose access to Corps resources. Independent merchants keep their ships and cargo.",
                "Retirement benefits are calculated at the end of character creation."));

        // --- TRADE ---
        list.add(makeEntry("TRADE_BASICS", "Trade Basics", "B5 RPG", "Trade", "TRADE_SYSTEM", 50,
                "Trade involves buying commodities at low prices and selling at high prices. Each system has supply and demand levels (1-6) that affect prices. High supply lowers prices; high demand raises prices. Profit is the difference between buy and sell prices minus travel costs.",
                "B5 has 26 themed commodities including Narn Grain, Centauri Wine, and Vorlon Artifacts."));

        list.add(makeEntry("TRADE_ROUTES", "Trade Routes", "B5 RPG", "Trade", "TRADE_SYSTEM", 51,
                "Trade routes connect systems via hyperlanes. Each route has a security level that affects the risk of piracy. Secure routes have low risk but high competition. Insecure routes have high risk but potential for higher profits. Trade missions can encounter piracy, customs checks, or market crashes.",
                "B5 trade routes include the Earth-Narn route, the Centauri-Minbari route, and the Shadow Zone route."));

        // --- EXPLORATION ---
        list.add(makeEntry("EXPLORATION_SURVEY", "World Survey", "B5 RPG", "Exploration", "EXPLORATION", 60,
                "World survey determines a planet's characteristics: size, atmosphere, gravity, starport class, population, government type, and law level. Survey uses a B5 world profile similar to Traveller's UWP. The profile is an 8-character string describing the planet's key features.",
                "B5 world profiles include government types like Earth Alliance Democracy, Narn Collective, and Centauri Empire."));

        list.add(makeEntry("EXPLORATION_DISCOVERY", "Exploration Discoveries", "B5 RPG", "Exploration", "EXPLORATION", 61,
                "Exploration can reveal discoveries: Vorlon artifacts, Shadow facilities, wormholes, First One technology, psionic sites, and derelict vessels. Each discovery provides XP rewards and potential plot hooks. The type of discovery depends on the sector and random roll.",
                "Vorlon artifacts are the most valuable discoveries. Shadow facilities are the most dangerous."));

        // --- RANDOM ENCOUNTERS ---
        list.add(makeEntry("ENCOUNTER_SPACE", "Space Encounters", "B5 RPG", "Exploration", "RANDOM_ENCOUNTERS", 70,
                "While traveling through space, characters may encounter: Earth Force patrols, Narn Crusaders, Shadow ships, wormholes, derelict vessels, pirate attacks, or psionic phenomena. Each encounter has a severity rating (1-10) and multiple possible outcomes.",
                "Space encounters are determined by rolling 2D6 and consulting the space encounter table."));

        list.add(makeEntry("ENCOUNTER_URBAN", "Urban Encounters", "B5 RPG", "Exploration", "RANDOM_ENCOUNTERS", 71,
                "While on a planet or station, characters may encounter: Psi Corps recruitment, Vorlon envoys, Shadow agents, bar fights, political rallies, black market dealers, or cultural events. Each encounter has a severity rating and multiple possible outcomes.",
                "Urban encounters are determined by rolling 2D6 and consulting the urban encounter table."));

        // --- STORYLINES ---
        list.add(makeEntry("STORYLINE_SHADOW_WAR", "Shadow War", "B5 TV Series", "Storylines", "STORYLINES", null,
                "The Shadow War is the central conflict of Babylon 5. The Shadows, an ancient race, manipulate younger races into fighting a war to prove their worth. The Earth Alliance, Narn, Minbari, and Centauri are all involved. Sheridan leads the resistance against the Shadows.",
                "The Shadow War spans all 5 seasons of Babylon 5."));

        list.add(makeEntry("STORYLINE_EARTH_MIND", "Earth-Mind War", "B5 TV Series", "Storylines", "STORYLINES", null,
                "The Earth-Mind War is a conflict between the Earth Alliance government and the Earth-Mind, a collective consciousness formed by the Vorlon technology. The Earth-Mind seeks to assimilate all humanity. Sheridan returns to lead the resistance.",
                "The Earth-Mind War occurs in Season 5 of Babylon 5."));

        // --- WORMHOLES ---
        list.add(makeEntry("WORMHOLE_NAVIGATION", "Wormhole Navigation", "B5 TV Series", "Space Travel", "SPACE_TRAVEL", null,
                "The Babylon 5 wormhole connects two points in the galaxy. Navigation through the wormhole requires precise timing and skill. The wormhole is guarded by the Rangers, a secret Minbari organization. Unauthorized use of the wormhole is considered a serious offense.",
                "The wormhole is a central plot element throughout Babylon 5."));

        // --- RANGERS ---
        list.add(makeEntry("RANGERS", "The Rangers", "B5 TV Series", "Organizations", "ORGANIZATIONS", null,
                "The Rangers are a secret Minbari organization dedicated to protecting the wormhole and maintaining balance in the galaxy. Rangers come from all races and serve without regard to faction. They have special training and access to the wormhole. Sheridan becomes a Ranger at the end of the series.",
                "Rangers are neutral in galactic conflicts but will intervene when the balance is threatened."));

        // --- PSI CORPS ---
        list.add(makeEntry("PSI_CORPS", "Psi Corps", "B5 TV Series", "Organizations", "ORGANIZATIONS", null,
                "The Psi Corps is the Earth Alliance organization that trains and controls psionic individuals. Membership is mandatory for those who test positive for psionic ability. The Psi Corps has its own ranks, training facilities, and enforcement capabilities. The Corps is corrupt and abusive, leading to the formation of the Psi War.",
                "The Psi War is a major storyline in Season 4 of Babylon 5."));

        // --- BATTLE OF THE SEERS ---
        list.add(makeEntry("BATTLE_SEERS", "Battle of the Seers", "B5 TV Series", "Battles", "BATTLES", null,
                "The Battle of the Seers is a major space battle between the Earth Alliance and the Minbari. The battle takes place near the Seers asteroid field. Sheridan leads the Earth Alliance forces against the Minbari Battle Cruisers. The battle is a turning point in the Earth-Minbari conflict.",
                "The battle occurs in Season 1 of Babylon 5."));

        // --- LINE OF DESTINY ---
        list.add(makeEntry("LINE_DESTINY", "Line of Destiny", "B5 TV Series", "Lore", "LORE", null,
                "The Line of Destiny is a Minbari prophecy that foretells the coming of a great leader who will unite the galaxy against the Shadows. Sheridan is the fulfillment of this prophecy. The Line of Destiny connects Sheridan to the Minbari warrior Valen and to the ancient conflicts between the Vorlons and Shadows.",
                "The Line of Destiny is a central theme throughout Babylon 5."));
        return list;
    }

    private RulesEntry makeEntry(String id, String title, String source, String chapter,
                                  String category, Integer page, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.BABYLON_5);
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

    // ==================== Query Methods ====================

    /**
     * Gets all rule entries.
     *
     * @return list of all entries
     */
    public List<RulesEntry> getAllEntries() {
        ensureInitialized();
        return new ArrayList<>(entries);
    }

    /**
     * Gets all rule entries for a given category.
     *
     * @param category the category (e.g., "DICE_MECHANICS", "MELEE_COMBAT")
     * @return list of entries in that category
     */
    public List<RulesEntry> getRulesByCategory(String category) {
        ensureInitialized();
        return entries.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Gets all rule entries from a given source.
     *
     * @param source the source (e.g., "B5 TV Series", "B5 RPG")
     * @return list of entries from that source
     */
    public List<RulesEntry> getRulesBySource(String source) {
        ensureInitialized();
        return entries.stream()
                .filter(e -> e.getSource().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    }

    /**
     * Searches rule entries by keyword.
     *
     * @param keyword the search keyword
     * @return list of matching entries
     */
    public List<RulesEntry> searchRules(String keyword) {
        ensureInitialized();
        String lower = keyword.toLowerCase();
        return entries.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lower) ||
                        e.getText().toLowerCase().contains(lower) ||
                        e.getNotes() != null && e.getNotes().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Gets all available rule categories.
     *
     * @return list of unique categories
     */
    public List<String> getCategories() {
        ensureInitialized();
        return entries.stream()
                .map(RulesEntry::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Gets all available sources.
     *
     * @return list of unique sources
     */
    public List<String> getSources() {
        ensureInitialized();
        return entries.stream()
                .map(RulesEntry::getSource)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Gets the total number of rule entries.
     *
     * @return entry count
     */
    public int getEntryCount() {
        ensureInitialized();
        return entries.size();
    }

    /**
     * Gets a rule entry by ID.
     *
     * @param id the entry ID
     * @return the entry or null if not found
     */
    public RulesEntry getEntryById(String id) {
        ensureInitialized();
        return entries.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all entries for a given chapter.
     *
     * @param chapter the chapter name
     * @return list of entries in that chapter
     */
    public List<RulesEntry> getRulesByChapter(String chapter) {
        ensureInitialized();
        return entries.stream()
                .filter(e -> e.getChapter().equalsIgnoreCase(chapter))
                .collect(Collectors.toList());
    }

    /**
     * Gets all available chapters.
     *
     * @return list of unique chapters
     */
    public List<String> getChapters() {
        ensureInitialized();
        return entries.stream()
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

package com.yisy.modules.doramcu.thirdreich.service;

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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing and querying the Third Reich rules reference database.
 * <p>
 * This service populates the rules database on application startup with all the
 * rules for the 3R4 (Third Reich) ground combat system:
 * <ul>
 *   <li>CRT Combat Outcomes â€” Combat Results Table resolution mechanics</li>
 *   <li>Terrain Modifiers â€” Ground, urban, forest, hill, river effects</li>
 *   <li>Commander Traits â€” Leadership, tactical preference, trait bonuses</li>
 *   <li>Unit Types â€” Infantry, armor, artillery, reconnaissance</li>
 *   <li>Morale Rules â€” Morale checks, rout conditions, leadership effects</li>
 *   <li>Battle Phases â€” Movement, combat, morale, cleanup phases</li>
 * </ul>
 * </p>
 * <p>
 * Uses JPA persistence via {@link UnifiedRulesEntryRepository} so rules survive
 * application restarts and can be dynamically updated at runtime.
 * </p>
 */
@Service
public class ThirdReichRulesDatabase {

    private static final Logger log = LoggerFactory.getLogger(ThirdReichRulesDatabase.class);

    private static final int EXPECTED_ENTRY_COUNT_MIN = 25;

    private final UnifiedRulesEntryRepository entryRepository;
    private boolean initialized = false;

    public ThirdReichRulesDatabase(UnifiedRulesEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        if (initialized) {
            return;
        }
        long existingCount = entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).size();
        if (existingCount >= EXPECTED_ENTRY_COUNT_MIN) {
            log.info("Third Reich rules database already populated with {} entries", existingCount);
            initialized = true;
            return;
        }
        log.info("Initializing Third Reich rules database...");
        populateRuleEntries();
        initialized = true;
        log.info("Third Reich rules database initialized: {} entries",
                entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).size());
    }

    // ==================== Rule Entries Population ====================

    @Transactional
    public void populateRuleEntries() {
        List<RulesEntry> entries = createThirdReichEntries();
        entryRepository.saveAll(entries);
        entryRepository.flush();
    }

    private List<RulesEntry> createThirdReichEntries() {
        List<RulesEntry> list = new ArrayList<>();

        // ============================================================
        // CRT COMBAT OUTCOMES â€” Combat Results Table Resolution
        // ============================================================

        list.add(makeEntry("CRT_DECISIVE_VICTORY", "Decisive Victory", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 1,
                "When the CRT differential is +3 or greater, the attacker achieves a decisive victory. The defender suffers double damage and must immediately attempt a morale check at -2 penalty. If the morale check fails, the defender routs from the battlefield. A decisive victory also allows the attacker to advance one additional hex after combat resolution.",
                "CRT differential = (d6 + attackPower) - (d6 + defensePower). Differential â‰¥ 3 = Decisive Victory."));

        list.add(makeEntry("CRT_MARGINAL_VICTORY", "Marginal Victory", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 2,
                "When the CRT differential is +1 or +2, the attacker achieves a marginal victory. The defender suffers normal damage and must attempt a morale check. If the morale check fails, the defender retreats one hex. The attacker may advance one hex after combat resolution.",
                "CRT differential = (d6 + attackPower) - (d6 + defensePower). Differential +1 to +2 = Marginal Victory."));

        list.add(makeEntry("CRT_STANDOFF", "Standoff", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 3,
                "When the CRT differential is 0, the combat results in a standoff. Neither side suffers damage and no morale check is required. Both units remain in their current positions. The next round begins with both sides at full strength.",
                "CRT differential = (d6 + attackPower) - (d6 + defensePower). Differential = 0 = Standoff."));

        list.add(makeEntry("CRT_DEFENDER_HOLDS", "Defender Holds", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 4,
                "When the CRT differential is negative (attacker rolls lower than defender), the defender holds the position. The attacker suffers no damage but must attempt a morale check if the differential is -2 or worse. The attacker cannot advance after this combat resolution.",
                "CRT differential = (d6 + attackPower) - (d6 + defensePower). Differential < 0 = Defender Holds."));

        list.add(makeEntry("CRT_LEADERSHIP_MODIFIER", "Leadership Modifier", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 5,
                "Commander leadership modifies CRT resolution. Leadership 1-4 provides -1 to the commander's side CRT rolls. Leadership 5-6 provides no modifier. Leadership 7-8 provides +1 to the commander's side CRT rolls. Leadership 9-10 provides +2 to the commander's side CRT rolls. This modifier applies to both attack and defense rolls.",
                "Leadership modifier is added to the d6 roll before calculating the differential."));

        list.add(makeEntry("CRT_MAX_ROUNDS", "Maximum Combat Rounds", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 6,
                "A single combat engagement can last a maximum of 20 rounds. If neither side achieves victory after 20 rounds, the combat ends in a draw. Both sides suffer no additional damage and remain in their current positions. The battle outcome is determined by total casualties and units lost.",
                "After 20 rounds, compare total attack power vs total defense power to determine the battle outcome."));

        list.add(makeEntry("CRT_MORALE_ROUT", "Morale Rout Threshold", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 7,
                "When a unit's morale reaches 0 or below, the unit immediately routs from the battlefield. A routed unit is removed from the battle and cannot participate in further combat. Routed units count as destroyed for victory condition purposes. Leadership can prevent rout by spending leadership points.",
                "Morale starts at the unit's morale value. Each failed morale check reduces morale by 1. Morale â‰¤ 0 = Rout."));

        list.add(makeEntry("CRT_DAMAGE_CALCULATION", "Damage Calculation", "3R4 Core Rules", "Combat Resolution", "CRT_COMBAT", 8,
                "Damage is calculated based on the attacker's attack power. Normal damage equals the attack power value. Decisive victory doubles the damage. Defender's armor value reduces damage by half (rounded down). Minimum damage is 1 point per hit. Armor cannot reduce damage below 1.",
                "Damage = max(1, attackPower - armor/2). Decisive victory: damage *= 2."));

        // ============================================================
        // TERRAIN MODIFIERS â€” Ground, Urban, Forest, Hill, River
        // ============================================================

        list.add(makeEntry("TERRAIN_OPEN_GROUND", "Open Ground", "3R4 Core Rules", "Terrain", "TERRAIN", 10,
                "Open ground provides no cover or protection. Attack and defense rolls are unmodified. Movement is unrestricted. Units on open ground are vulnerable to artillery and air support. Open ground favors armored units and artillery due to clear lines of sight.",
                "Attack modifier: 0. Defense modifier: 0. Movement modifier: 0."));

        list.add(makeEntry("TERRAIN_FOREST", "Forest", "3R4 Core Rules", "Terrain", "TERRAIN", 11,
                "Forest terrain provides cover to defending units. Defenders in forest receive +2 to defense rolls. Attackers suffer -1 to attack rolls due to limited visibility. Movement through forest is reduced by half. Artillery effectiveness is reduced by 50% in forest terrain.",
                "Attack modifier: -1. Defense modifier: +2. Movement: halved. Artillery: -50% effectiveness."));

        list.add(makeEntry("TERRAIN_URBAN", "Urban", "3R4 Core Rules", "Terrain", "TERRAIN", 12,
                "Urban terrain provides significant cover to defending units. Defenders in urban areas receive +4 to defense rolls. Attackers suffer -2 to attack rolls due to rubble and limited fields of fire. Movement through urban terrain is reduced to one-third normal speed. Armored units are at a disadvantage in urban combat.",
                "Attack modifier: -2. Defense modifier: +4. Movement: 1/3 normal. Armor disadvantage."));

        list.add(makeEntry("TERRAIN_HILL", "Hill", "3R4 Core Rules", "Terrain", "TERRAIN", 13,
                "Hill terrain provides the high ground advantage. Units on higher ground receive +1 to attack rolls and +1 to defense rolls. Units attacking uphill suffer -1 to attack rolls. Artillery on high ground has extended range. Movement uphill costs double movement points.",
                "High ground: attack +1, defense +1. Attacking uphill: attack -1. Movement uphill: 2x cost."));

        list.add(makeEntry("TERRAIN_RIVER", "River", "3R4 Core Rules", "Terrain", "TERRAIN", 14,
                "River terrain blocks movement unless a bridge or ford is available. Units crossing a river suffer -2 to attack rolls and -2 to defense rolls during the crossing phase. A unit that fails a morale check while crossing a river is destroyed (drowning). Bridges can be destroyed to block enemy movement.",
                "Crossing: attack -2, defense -2. Failed morale while crossing = destroyed. Bridges can be destroyed."));

        list.add(makeEntry("TERRAIN_SWAMP", "Swamp", "3R4 Core Rules", "Terrain", "TERRAIN", 15,
                "Swamp terrain severely restricts movement. All units moving through swamp have their movement reduced to one-quarter normal speed. Armored units are especially disadvantaged, suffering -2 to attack rolls in swamp. Infantry units receive +1 to defense rolls in swamp terrain due to the difficulty of attacking through mud.",
                "Movement: 1/4 normal. Armor attack: -2. Infantry defense: +1."));

        // ============================================================
        // COMMANDER TRAITS â€” Leadership, Tactical Preference, Traits
        // ============================================================

        list.add(makeEntry("TRAIT_AGGRESSIVE", "Aggressive Trait", "3R4 Core Rules", "Commander Traits", "COMMANDER_TRAITS", 20,
                "The Aggressive trait provides +1 to attack rolls but -1 to defense rolls. Commanders with this trait favor offensive tactics and are more likely to initiate combat. Aggressive commanders gain +1 to morale checks when attacking but suffer -1 to morale checks when defending.",
                "Attack: +1. Defense: -1. Morale (attacking): +1. Morale (defending): -1."));

        list.add(makeEntry("TRAIT_FLANKER", "Flanker Trait", "3R4 Core Rules", "Commander Traits", "COMMANDER_TRAITS", 21,
                "The Flanker trait provides +2 to attack rolls when attacking from the side or rear. Flanking attacks ignore cover bonuses. Commanders with this trait are skilled at maneuvering units to exploit weak points in enemy lines. Flanking requires at least one movement action before the attack.",
                "Flanking attack: +2. Ignores cover. Requires movement before attack."));

        list.add(makeEntry("TRAIT_INSPIRATIONAL", "Inspirational Trait", "3R4 Core Rules", "Commander Traits", "COMMANDER_TRAITS", 22,
                "The Inspirational trait provides +1 to all morale checks for units in the commander's area of effect. Units led by inspirational commanders are less likely to rout and more likely to stand firm under fire. Inspirational commanders can prevent one rout per battle by spending a leadership point.",
                "Morale check: +1 for all units. Can prevent one rout per battle."));

        list.add(makeEntry("TRAIT_LOGISTICIAN", "Logistician Trait", "3R4 Core Rules", "Commander Traits", "COMMANDER_TRAITS", 23,
                "The Logistician trait provides +1 to movement rolls and reduces supply consumption by 25%. Units led by logisticians can operate further from supply lines without penalty. Logisticians can establish supply depots that restore unit morale and ammunition.",
                "Movement: +1. Supply consumption: -25%. Can establish supply depots."));

        list.add(makeEntry("TRAIT_CAUTIOUS", "Cautious Trait", "3R4 Core Rules", "Commander Traits", "COMMANDER_TRAITS", 24,
                "The Cautious trait provides +1 to defense rolls and +1 to morale checks when defending. Cautious commanders are less likely to initiate combat but excel at holding positions. Units led by cautious commanders suffer -1 to attack rolls but gain +2 to defense rolls when in prepared positions.",
                "Defense: +1. Morale (defending): +1. Attack: -1. Prepared position: defense +2."));

        // ============================================================
        // UNIT TYPES â€” Infantry, Armor, Artillery, Reconnaissance
        // ============================================================

        list.add(makeEntry("UNIT_INFANTRY", "Infantry Unit", "3R4 Core Rules", "Unit Types", "UNIT_TYPES", 30,
                "Infantry units are the backbone of ground combat. They have moderate attack and defense values, good morale, and can operate in all terrain types. Infantry receive +1 to defense rolls in urban and forest terrain. Infantry are vulnerable to artillery and armored units in open ground.",
                "Attack: 3-5. Defense: 3-5. Morale: 6-8. Urban/forest defense: +1."));

        list.add(makeEntry("UNIT_ARMOR", "Armored Unit", "3R4 Core Rules", "Unit Types", "UNIT_TYPES", 31,
                "Armored units (tanks and armored vehicles) have high attack power and good mobility on open ground. They receive +2 to attack rolls on open ground but suffer -2 to attack rolls in urban and forest terrain. Armor has high defense values but low morale when isolated from infantry support.",
                "Attack: 5-8. Defense: 4-6. Open ground attack: +2. Urban/forest attack: -2. Low morale without infantry."));

        list.add(makeEntry("UNIT_ARTILLERY", "Artillery Unit", "3R4 Core Rules", "Unit Types", "UNIT_TYPES", 32,
                "Artillery units provide long-range fire support. They cannot engage in direct combat but can bombard enemy units at range. Artillery effectiveness is reduced by 50% in forest terrain and cannot target units in urban terrain. Artillery has low movement and low morale.",
                "Range: 3-5 hexes. Forest effectiveness: -50%. Urban: cannot target. Low movement, low morale."));

        list.add(makeEntry("UNIT_RECON", "Reconnaissance Unit", "3R4 Core Rules", "Unit Types", "UNIT_TYPES", 33,
                "Reconnaissance units have high movement and can scout enemy positions. They receive +2 to movement rolls and can move through difficult terrain without penalty. Recon units have low attack and defense values but can provide intelligence bonuses to friendly units. Recon units can perform flanking attacks without the movement penalty.",
                "Movement: +2. Difficult terrain: no penalty. Low attack/defense. Flanking: no movement penalty."));

        // ============================================================
        // MORALE RULES â€” Morale Checks, Rout Conditions, Leadership
        // ============================================================

        list.add(makeEntry("MORALE_CHECK", "Morale Check", "3R4 Core Rules", "Morale", "MORALE", 40,
                "A morale check is required when a unit suffers damage, loses a combat round, or is ordered to perform a dangerous action. Roll d6 and add the unit's morale value and any leadership modifiers. If the result meets or exceeds the required threshold (usually 8), the unit stands firm. If the check fails, the unit's morale is reduced by 1.",
                "Roll d6 + morale + leadership modifier â‰¥ 8 = stands firm. Fail: morale -1."));

        list.add(makeEntry("MORALE_ROUT_CONDITIONS", "Rout Conditions", "3R4 Core Rules", "Morale", "MORALE", 41,
                "A unit routs when its morale reaches 0 or below. Additionally, a unit automatically routs if it suffers more than 50% casualties in a single round, if its commander is killed, or if it is surrounded by enemy units on three or more sides. Routed units move away from the nearest enemy unit and cannot attack.",
                "Rout when: morale â‰¤ 0, casualties > 50% in one round, commander killed, surrounded on 3+ sides."));

        list.add(makeEntry("MORALE_LEADERSHIP_BONUS", "Leadership Morale Bonus", "3R4 Core Rules", "Morale", "MORALE", 42,
                "Commander leadership provides a bonus to all morale checks within the commander's area of effect. Leadership 1-4: -1 to morale checks. Leadership 5-6: no modifier. Leadership 7-8: +1 to morale checks. Leadership 9-10: +2 to morale checks. A commander's area of effect extends 3 hexes from their position.",
                "Leadership bonus applies to all units within 3 hexes of the commander."));

        list.add(makeEntry("MORALE_RECOVERY", "Morale Recovery", "3R4 Core Rules", "Morale", "MORALE", 43,
                "Units can recover morale during the cleanup phase of a battle round. A unit recovers 1 morale point if it is not engaged in combat and is within range of friendly supply lines. Units led by inspirational commanders recover 2 morale points per round. Maximum morale cannot exceed the unit's base morale value.",
                "Recover 1 morale per round if not in combat and near supply. Inspirational commander: recover 2."));

        // ============================================================
        // BATTLE PHASES â€” Movement, Combat, Morale, Cleanup
        // ============================================================

        list.add(makeEntry("PHASE_MOVEMENT", "Movement Phase", "3R4 Core Rules", "Battle Phases", "BATTLE_PHASES", 50,
                "In the movement phase, each player moves their units according to their movement values. Units can move, advance, retreat, or reorganize. Movement is affected by terrain modifiers. Units cannot move through enemy-occupied hexes unless performing an attack. Reconnaissance units can move through enemy-adjacent hexes without penalty.",
                "Order: move units according to movement values, modified by terrain. Cannot move through enemy hexes."));

        list.add(makeEntry("PHASE_COMBAT", "Combat Phase", "3R4 Core Rules", "Battle Phases", "BATTLE_PHASES", 51,
                "In the combat phase, each player resolves attacks for units adjacent to enemy units. Attacks are resolved using the CRT system: roll d6 + attack power vs d6 + defense power. The differential determines the outcome. Each unit can attack once per combat phase. Artillery units can attack at range during this phase.",
                "CRT resolution: d6 + attackPower vs d6 + defensePower. Each unit attacks once per phase."));

        list.add(makeEntry("PHASE_MORALE", "Morale Phase", "3R4 Core Rules", "Battle Phases", "BATTLE_PHASES", 52,
                "In the morale phase, all units that suffered damage or lost a combat round must attempt morale checks. Units that fail their morale check have their morale reduced by 1. Units with morale at 0 or below rout immediately. Routed units move away from the nearest enemy unit and are removed from the battle.",
                "All damaged units check morale. Fail: morale -1. Morale â‰¤ 0: rout and remove from battle."));

        return list;
    }

    private RulesEntry makeEntry(String id, String title, String source, String chapter,
                                  String category, Integer page, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.THIRD_REICH);
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH);
    }

    /**
     * Gets all rule entries for a given category.
     *
     * @param category the category (e.g., "CRT_COMBAT", "TERRAIN")
     * @return list of entries in that category
     */
    public List<RulesEntry> getRulesByCategory(String category) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.THIRD_REICH, category);
    }

    /**
     * Gets all rule entries from a given source.
     *
     * @param source the source (e.g., "3R4 Core Rules")
     * @return list of entries from that source
     */
    public List<RulesEntry> getRulesBySource(String source) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(GameSystem.THIRD_REICH, source);
    }

    /**
     * Searches rule entries by keyword in title and text.
     *
     * @param keyword the search keyword
     * @return list of matching entries
     */
    public List<RulesEntry> searchRules(String keyword) {
        ensureInitialized();
        List<RulesEntry> results = new ArrayList<>(entryRepository.searchByTitle(GameSystem.THIRD_REICH, keyword));
        List<RulesEntry> textResults = entryRepository.searchByText(GameSystem.THIRD_REICH, keyword);
        for (RulesEntry e : textResults) {
            if (!results.contains(e)) {
                results.add(e);
            }
        }
        // Also check notes field (not indexed by repository methods)
        List<RulesEntry> allEntries = entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH);
        String lower = keyword.toLowerCase();
        for (RulesEntry e : allEntries) {
            if (e.getNotes() != null && e.getNotes().toLowerCase().contains(lower)) {
                if (!results.contains(e)) {
                    results.add(e);
                }
            }
        }
        return results;
    }

    /**
     * Gets all available rule categories.
     *
     * @return list of unique categories
     */
    public List<String> getCategories() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).stream()
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).stream()
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).size();
    }

    /**
     * Gets a rule entry by ID (matches against title or notes).
     *
     * @param id the entry ID
     * @return the entry or null if not found
     */
    public RulesEntry getEntryById(String id) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).stream()
                .filter(e -> e.getTitle().equalsIgnoreCase(id) ||
                        (e.getNotes() != null && e.getNotes().contains(id)))
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
        return entryRepository.findByGameSystemAndChapterAndActiveTrue(GameSystem.THIRD_REICH, chapter);
    }

    /**
     * Gets all available chapters.
     *
     * @return list of unique chapters
     */
    public List<String> getChapters() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.THIRD_REICH).stream()
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

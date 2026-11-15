package com.yisy.modules.doramcu.starwars.service;
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
 * Service for managing and querying the Star Wars rules reference database.
 * <p>
 * This service populates the rules database on application startup with all the
 * official rules extracted from the Star Wars universe:
 * <ul>
 *   <li>Star Wars: Legion â€” Ground miniatures combat</li>
 *   <li>Star Wars: X-Wing â€” Space dogfighting</li>
 *   <li>Star Wars: Rebellion â€” Galaxy-scale strategy</li>
 *   <li>Star Wars: Force and Destiny â€” Narrative RPG</li>
 *   <li>Star Wars: Unlimited â€” Trading card game</li>
 * </ul>
 * </p>
 * <p>
 * The rules database serves as the authoritative reference for both the AI mission
 * generation system and the game mechanics, ensuring that all task completions
 * follow the published Star Wars rules.
 * </p>
 * <p>
 * Uses JPA persistence via {@link UnifiedRulesEntryRepository} so rules survive
 * application restarts and can be dynamically updated at runtime.
 * </p>
 */
@Service
public class StarWarsRulesDatabase {

    private static final Logger log = LoggerFactory.getLogger(StarWarsRulesDatabase.class);

    private static final int EXPECTED_ENTRY_COUNT_MIN = 45;

    private final UnifiedRulesEntryRepository entryRepository;
    private boolean initialized = false;

    public StarWarsRulesDatabase(UnifiedRulesEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        if (initialized) {
            return;
        }
        long existingCount = entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).size();
        if (existingCount >= EXPECTED_ENTRY_COUNT_MIN) {
            log.info("Star Wars rules database already populated with {} entries", existingCount);
            initialized = true;
            return;
        }
        log.info("Initializing Star Wars rules database...");
        populateRuleEntries();
        initialized = true;
        log.info("Star Wars rules database initialized: {} entries",
                entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).size());
    }

    // ==================== Rule Entries Population ====================

    @Transactional
    public void populateRuleEntries() {
        List<RulesEntry> entries = createStarWarsEntries();
        entryRepository.saveAll(entries);
        entryRepository.flush();
    }

    private List<RulesEntry> createStarWarsEntries() {
        List<RulesEntry> list = new ArrayList<>();

        // ============================================================
        // GROUND COMBAT â€” Star Wars: Legion
        // ============================================================

        list.add(makeEntry("LEGION_COMMAND_PHASE", "Command Phase", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 1,
                "Both players secretly select a command card, then reveal and resolve effects. Priority is determined by comparing pip values. The priority player issues orders first. Command cards provide orders, buffs, and special abilities that shape the flow of battle.",
                "Command cards are the core resource in Legion. Each faction has a unique deck of 20 cards."));

        list.add(makeEntry("LEGION_ACTIVATION", "Activation Phase", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 2,
                "Units are activated in order determined by command cards and order pools. Each activation includes movement and attacks. Draw order tokens from order pools. Activate units based on order tokens: Move, then Attack. Units with Standby tokens wait for later activation. Continue until all order pools are depleted and all units have face-down order tokens.",
                "Order pools use red and blue tokens. Red tokens activate first, then blue. Standby tokens defer activation."));

        list.add(makeEntry("LEGION_ATTACK", "Attack Resolution", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 3,
                "Attacks use dice pools modified by range, cover, and unit abilities. Attacker declares target unit within range and line of sight. Roll attack dice based on weapon profile and range band. Defender rolls defense dice based on Agility stat. Resolve results: Hits and Glances cause damage, Crits cause extra effects. Apply damage to shields first, then hull.",
                "Red attack dice produce Hits, Glances, Crits, and Blanks. Yellow defense dice produce Evades, Shields, and Blanks."));

        list.add(makeEntry("LEGION_DICE", "Dice Types", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 4,
                "Legion uses three types of custom dice: Red (attack) dice produce Hits, Glances, Crits, and Blanks. Yellow (defense) dice produce Evades, Shields, and Blanks. Green (special) dice are used for certain abilities. Each symbol resolves in a specific order: Crits first, then Hits, then Glances, then Shields, then Evades.",
                "A Hit or Glance that is not resolved by an Evade or Shield causes 1 damage."));

        list.add(makeEntry("LEGION_MOVEMENT", "Movement Rules", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 5,
                "Units move up to their Speed stat in inches per activation. During movement, units may rotate and translate. Light cover grants +1 defense dice. Heavy cover grants +2 defense dice and blocks line of sight. Difficult terrain hinders movement; units must stop if they end in difficult terrain.",
                "Units cannot move through other units or terrain obstacles."));

        list.add(makeEntry("LEGION_SUPPRESSION", "Suppression", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 6,
                "Units can be suppressed, reducing their effectiveness. Suppression tokens are placed on units when certain attacks or abilities resolve. Suppressed units may have reduced attack or defense capabilities. One suppression token is removed from each unit during the end phase.",
                "Suppression is a key mechanic for area denial and controlling enemy activations."));

        list.add(makeEntry("LEGION_UNIT_RANKS", "Unit Ranks", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 7,
                "Units are categorized by rank: Commander (elite leaders with unique command cards), Corps (standard line units forming the backbone of armies), Special Forces (specialized units with unique abilities), Support (support units providing buffs and utility), and Heavy (heavy weapons and vehicles with high firepower).",
                "Each unit has a cost that determines the army point limit."));

        list.add(makeEntry("LEGION_END_PHASE", "End Phase", "Star Wars: Legion", "Ground Combat", "GROUND_COMBAT", 8,
                "Cleanup and victory point scoring. Resolve start of end phase effects. Score victory points from objective cards. Discard revealed command cards. Remove green tokens (Aim, Dodge, Observation, Surge, Standby). Remove one suppression token from each unit. Advance round counter.",
                "A standard Legion game lasts 6 rounds."));

        // ============================================================
        // SPACE COMBAT â€” Star Wars: X-Wing
        // ============================================================

        list.add(makeEntry("XWING_PLANNING", "Planning Phase", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 10,
                "Each player secretly chooses one maneuver for each ship using its maneuver dial, placing dials face-down. For each ship, select a maneuver from the ship's dial. Place the dial face-down next to the ship. This secret planning creates the bluffing element central to X-Wing.",
                "Maneuver dials are unique to each ship and define its movement capabilities."));

        list.add(makeEntry("XWING_ACTIVATION", "Activation Phase", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 11,
                "Ships move and perform one action, in ascending order of pilot skill. Reveal each ship's maneuver dial in ascending pilot skill order. Execute the chosen maneuver. Ship may perform one action (barrel roll, boost, evade, focus, lock, reload, etc.).",
                "Pilot skill is a numeric value (1-210+) that determines turn order."));

        list.add(makeEntry("XWING_COMBAT", "Combat Phase", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 12,
                "Each ship may perform one attack, in descending order of pilot skill. Attacker declares target within firing arc and range. Roll attack dice modified by range and abilities. Defender rolls defense dice modified by Agility and actions. Resolve hits: assign damage cards to defending ship. Critical hits reveal damage cards face-up for ongoing effects.",
                "Front arc ships fire forward in a 120-degree arc. Full arc ships can fire in any direction."));

        list.add(makeEntry("XWING_SHIP_STATS", "Ship Statistics", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 13,
                "Each ship has: Pilot Skill (determines activation and combat order), Attack (number of attack dice rolled), Agility (number of defense dice the target rolls), Hull (structural integrity; absorbs damage after shields are depleted), Shields (energy barrier; absorbs damage before hull), and Firing Arc (front arc for most ships, full arc for some).",
                "Shields regenerate at the start of each round up to the ship's shield value."));

        list.add(makeEntry("XWING_RANGES", "Ranges", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 14,
                "X-Wing uses three range bands: Range 1 (0-3 inches, close range), Range 2 (3-9 inches, medium range), Range 3 (9-18 inches, long range). Range affects attack dice: some weapons gain dice at closer range, while others lose dice at longer range.",
                "The range finder tool is essential for accurate measurement."));

        list.add(makeEntry("XWING_DAMAGE", "Damage System", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 15,
                "Ships receive damage cards when hit. Face-down cards are stress or hidden damage. Face-up cards are critical hits with ongoing effects. Critical hits reveal a damage card face-up, imposing a lasting penalty until repaired. When a ship's hull reaches 0, it is destroyed.",
                "Stress tokens prevent performing actions until removed."));

        list.add(makeEntry("XWING_ACTIONS", "Actions", "Star Wars: X-Wing", "Space Combat", "SPACE_COMBAT", 16,
                "During activation, a ship may perform one action: Barrel Roll (rotate and move), Boost (move forward), Evade (add evade token for defense), Focus (add focus token for attack), Lock (gain target lock on enemy), Reload (reload weapons), or Repair (restore shields).",
                "Focus and Evade tokens modify the attack and defense dice pools respectively."));

        // ============================================================
        // GALAXY CONTROL â€” Star Wars: Rebellion
        // ============================================================

        list.add(makeEntry("REBELLION_ASSIGNMENT", "Assignment Phase", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 20,
                "Players assign leaders to missions secretly. Each player assigns leaders to mission cards. Leaders are placed face-down on missions. This creates a bidding war for mission completion, as players try to outbid their opponent's leader assignments.",
                "Missions provide resources, units, and special effects."));

        list.add(makeEntry("REBELLION_COMMAND", "Command Phase", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 21,
                "Players take turns resolving missions or placing leaders on the board to move units and start combats. Players alternate taking actions. Actions include: resolve missions, place leaders, move ships, move ground units, initiate combat. Phase ends when both players pass consecutively.",
                "Leaders provide combat bonuses and special abilities when placed on the board."));

        list.add(makeEntry("REBELLION_REFRESH", "Refresh Phase", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 22,
                "Cleanup steps: retrieve leaders, draw missions, launch probe droids, draw objectives, recruit leaders, build units. Retrieve leaders from the board and missions. Draw mission cards (2 per player). Imperial player launches probe droids (2 probe cards). Rebel player draws 1 objective card. Recruit new leaders if time track allows. Build new units if time track allows.",
                "The refresh phase is where both players rebuild their resources."));

        list.add(makeEntry("REBELLION_VICTORY", "Victory Conditions", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 23,
                "Imperial wins: Conquer the Rebel base system (Imperial units present, no Rebel units) or Death Star destroys the system containing the Rebel base. Rebel wins: Reputation marker meets or passes the time marker on the time track, or time track reaches its end without the Empire finding the base.",
                "The game is highly asymmetric: Empire seeks out the hidden Rebel base, while Rebels build reputation through objectives."));

        list.add(makeEntry("REBELLION_TIME_TRACK", "Time Track", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 24,
                "16 spaces maximum. Time marker starts at 1 and advances each round. Reputation marker starts at 14 and moves toward time as Rebels complete objectives. Rebels gain reputation by fulfilling objective card requirements. Empire advances time by completing projects and building superweapons.",
                "The time track creates a race: Rebels must build reputation before the Empire finds their base."));

        list.add(makeEntry("REBELLION_PROBE_DROIDS", "Probe Droids", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 25,
                "Imperial player draws probe cards each round to eliminate possible Rebel base locations. Each probe reveals 1 system where the base is NOT located. The purpose is to gradually narrow down the hidden Rebel base location. As the game progresses, the Empire's search becomes more focused.",
                "The Rebel base is hidden in a face-down system token."));

        list.add(makeEntry("REBELLION_SPACE_COMBAT", "Space Combat", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 26,
                "Dice-based combat using tactic cards. Attacker declares combat. Both players draw or play tactic cards. Roll dice based on ship attack values. Apply hits to ship shields and hull. Tactic cards modify dice rolls and provide special effects.",
                "Each ship has attack and defense values that determine dice pools."));

        list.add(makeEntry("REBELLION_GROUND_COMBAT", "Ground Combat", "Star Wars: Rebellion", "Galaxy Control", "GALAXY_CONTROL", 27,
                "Dice-based combat using ground tactic cards to determine system loyalty. Attacker declares ground combat. Both players draw or play ground tactic cards. Roll dice based on ground unit values. Winner gains loyalty in the system. Ground combat is essential for controlling systems and completing objectives.",
                "System loyalty determines which player controls the system's resources."));

        // ============================================================
        // RPG MECHANICS â€” Force and Destiny
        // ============================================================

        list.add(makeEntry("FANDD_CORE_MECHANIC", "Core Mechanic", "Star Wars: Force and Destiny", "RPG Mechanics", "RPG_MECHANICS", 30,
                "Build and roll a dice pool. Results are interpreted using four symbol pairs: Success/Failure (determines whether the action succeeds or fails), Advantage/Threat (side effects â€” Advantage helps the actor, Threat helps the opponent), Triumph/Despair (major narrative effects â€” Triumph is a huge bonus, Despair is a severe setback).",
                "The narrative dice system produces rich, story-driven results."));

        list.add(makeEntry("FANDD_DICE_TYPES", "Dice Types", "Star Wars: Force and Destiny", "RPG Mechanics", "RPG_MECHANICS", 31,
                "Ability (blue) dice represent character skill. Difficulty (red) dice represent task difficulty. Proficiency (light blue) dice are upgrade dice. Challenge (dark red) dice are upgrade dice. Boost (white) dice represent helpful circumstances. Advantage (green) dice represent favorable conditions. Despair (black) dice represent dangerous situations.",
                "The GM builds the difficulty pool; the player builds the ability pool."));

        list.add(makeEntry("FANDD_FORCE_POWERS", "Force Powers", "Star Wars: Force and Destiny", "RPG Mechanics", "RPG_MECHANICS", 32,
                "Force-sensitive characters use the Force through ranked powers with light and dark side options. Light Side powers include healing, protection, enhancement, and telekinesis for defense. Dark Side powers include mind control, force lightning, choke, fear, and domination. Each power has ranks that increase its potency.",
                "Force powers are a core mechanic for Force-sensitive characters."));

        list.add(makeEntry("FANDD_DESTINY_TOKENS", "Destiny Tokens", "Star Wars: Force and Destiny", "RPG Mechanics", "RPG_MECHANICS", 33,
                "A shared pool of light and dark side tokens that players can spend to modify dice pools, driving the narrative. Light side tokens are spent to add success, advantage, or boost dice. Dark side tokens are spent to add threat, challenge, or despair dice. Players control the story by spending tokens.",
                "The GM and players share control of the destiny pool."));

        list.add(makeEntry("FANDD_SKILL_CHECK", "Skill Checks", "Star Wars: Force and Destiny", "RPG Mechanics", "RPG_MECHANICS", 34,
                "To perform a skill check, the player rolls their ability pool against the GM's difficulty pool. The number of Successes determines the outcome. Advantage provides benefits like extra damage or reduced cost. Threat provides benefits to the opponent. Triumph and Despair create major narrative shifts.",
                "Skill checks use the character's relevant skill rank to determine the number of ability dice."));

        list.add(makeEntry("FANDD_COMBAT", "Combat", "Star Wars: Force and Destiny", "RPG Mechanics", "RPG_MECHANICS", 35,
                "Combat uses the narrative dice system. Attackers roll against the defender's Agility or Cool. Damage is tracked as wounds. Strain represents stress and fatigue. When strain equals the character's Resilience x 4, they are stunned. When wounds equal the character's Resilience, they are incapacitated.",
                "Combat is cinematic and narrative-driven, not simulationist."));

        // ============================================================
        // CARD GAME â€” Star Wars: Unlimited
        // ============================================================

        list.add(makeEntry("UNLIMITED_DECK_BUILDING", "Deck Building", "Star Wars: Unlimited", "Card Game", "CARD_GAME", 40,
                "Decks must contain a minimum of 50 cards, including 1 leader and 1 base. Maximum 3 copies of any card. The leader is a double-sided key figure that starts as a leader and can deploy as a unit. The base is a starting structure with ~30 HP; if reduced to 0 HP, the player loses.",
                "Deck building strategy revolves around aspect synergy."));

        list.add(makeEntry("UNLIMITED_ASPECTS", "Aspects", "Star Wars: Unlimited", "Card Game", "CARD_GAME", 41,
                "Six aspects define card colors: Vigilance (blue) â€” defense, control, and manipulation. Command (green) â€” strategy, resource management, and support. Aggression (red) â€” direct damage and offensive power. Cunning (yellow) â€” deception, tricks, and resource denial. Heroism (white) â€” altruism, healing, and protection of allies. Villainy (black) â€” cruelty, domination, and fear.",
                "Each player chooses two aspects that define their strategy."));

        list.add(makeEntry("UNLIMITED_ACTION_PHASE", "Action Phase", "Star Wars: Unlimited", "Card Game", "CARD_GAME", 42,
                "Players alternate taking actions: play a unit from hand, play an event from hand, play an upgrade on a unit, attack with a ready unit, use an ability, or pass. Phase ends when both players pass consecutively. Units have initiative values that determine attack order.",
                "Resources are generated by face-down cards in the resource area."));

        list.add(makeEntry("UNLIMITED_REGROUP", "Regroup Phase", "Star Wars: Unlimited", "Card Game", "CARD_GAME", 43,
                "Draw cards to return hand to starting size. Place one card from hand face-down as a resource. Ready all exhausted cards. This phase resets the board for the next round of actions.",
                "The regroup phase ensures both players have resources and cards to play."));

        list.add(makeEntry("UNLIMITED_CARD_TYPES", "Card Types", "Star Wars: Unlimited", "Card Game", "CARD_GAME", 44,
                "Leader: double-sided key figure; starts as leader, can deploy as unit. Base: starting structure with ~30 HP; if reduced to 0 HP, player loses. Unit: characters and vehicles that can attack and defend. Event: one-time effects that resolve when played. Upgrade: cards attached to units to enhance their abilities.",
                "Card types determine how cards interact with the game."));

        list.add(makeEntry("UNLIMITED_ASPECT_PENALTY", "Aspect Penalty", "Star Wars: Unlimited", "Card Game", "CARD_GAME", 45,
                "When playing a card, for each aspect icon on the card not provided by your leader or base, pay 2 extra resources. This encourages deck building around your chosen aspects and punishes off-aspect plays.",
                "Aspect penalty is a core balancing mechanic."));

        // ============================================================
        // THE FORCE â€” Lore and Mechanics
        // ============================================================

        list.add(makeEntry("FORCE_LIGHT_SIDE", "Light Side of the Force", "Star Wars Lore", "The Force", "THE_FORCE", 50,
                "The Light Side of the Force represents peace, knowledge, serenity, defense, and selflessness. Light Side users channel the Force through compassion, healing, and protection. Powers include Force Heal, Force Speed, Force Barrier, Force Push (defensive), and Precognition. Jedi are the primary Light Side order.",
                "The Light Side is the living Force, flowing through all living things."));

        list.add(makeEntry("FORCE_DARK_SIDE", "Dark Side of the Force", "Star Wars Lore", "The Force", "THE_FORCE", 51,
                "The Dark Side of the Force represents passion, aggression, domination, and fear. Dark Side users channel the Force through anger, hatred, and the desire for power. Powers include Force Lightning, Force Choke, Mind Trick, Force Drain, and Force Storm. Sith are the primary Dark Side order.",
                "The Dark Side is a corruption of the Force, amplifying power at the cost of morality."));

        list.add(makeEntry("FORCE_SENSITIVITY", "Force Sensitivity", "Star Wars Lore", "The Force", "THE_FORCE", 52,
                "Force sensitivity is determined by midi-chlorian count in a being's cells. Higher midi-chlorian counts indicate greater potential. Force-sensitive beings can sense and manipulate the Force through training. Notable Force-sensitive beings include Jedi, Sith, and Force Adepts. Children with high midi-chlorian counts are often recruited by the Jedi Order.",
                "Anakin Skywalker had the highest midi-chlorian count ever recorded."));

        list.add(makeEntry("LIGHTSABER", "Lightsaber Combat", "Star Wars Lore", "The Force", "THE_FORCE", 53,
                "Lightsabers are the traditional weapon of the Jedi Order. They consist of a kyber crystal focusing lens enclosed in a metal hilt. The blade is pure energy, capable of cutting through most materials. There are seven forms of lightsaber combat: Shii-Cho (Form I), Makashi (Form II), Soresu (Form III), Jar'Kai (Form IV), Juyo (Form V), Vaapad (Form VI), and Jett (Form VII).",
                "Kyber crystals are naturally clear and bond with their wielder, taking on a color."));

        list.add(makeEntry("KYBER_CRYSTALS", "Kyber Crystals", "Star Wars Lore", "The Force", "THE_FORCE", 54,
                "Kyber crystals are Force-sensitive crystals that focus the energy of a lightsaber. They are found on various planets, including Ilum and Ossus. Crystals naturally bond with a Jedi, taking on a color: blue (Guardian), green (Scholar), purple (balance), yellow (Templar), red (Sith, forced bond), white (redeemed).",
                "Sith crystals are 'bled' through the Dark Side, turning them red."));

        // ============================================================
        // FACTIONS
        // ============================================================

        list.add(makeEntry("FACTION_REPUBLIC", "Galactic Republic", "Star Wars Lore", "Factions", "FACTIONS", 60,
                "The Galactic Republic governed the galaxy for 10,000 years before its fall. Led by the Senate and protected by the Jedi Order and Clone Army. The Republic fell when Palpatine transformed it into the Galactic Empire. Key leaders: Palpatine, Yoda, Obi-Wan Kenobi, Mace Windu, Anakin Skywalker.",
                "The Republic's fall marked the end of the Clone Wars and the rise of the Empire."));

        list.add(makeEntry("FACTION_EMPIRE", "Galactic Empire", "Star Wars Lore", "Factions", "FACTIONS", 61,
                "The Galactic Empire ruled the galaxy through fear and military might. Led by Emperor Palpatine and Darth Vader. The Empire maintained control through the Imperial Military, including Star Destroyers, TIE fighters, and Stormtroopers. Key projects: Death Star superweapon, Inquisitorius for hunting Jedi.",
                "The Empire fell at the Battle of Endor when the Death Star II was destroyed."));

        list.add(makeEntry("FACTION_REBELS", "Rebel Alliance", "Star Wars Lore", "Factions", "FACTIONS", 62,
                "The Rebel Alliance (Alliance to Restore the Republic) fought to overthrow the Galactic Empire. Led by Mon Mothma, Leia Organa, and supported by Luke Skywalker and Han Solo. The Rebels operated from hidden bases and used guerrilla tactics. Key victories: destruction of the Death Star, Battle of Endor.",
                "The Rebels became the New Republic after the fall of the Empire."));

        list.add(makeEntry("FACTION_SEPARATISTS", "Separatist Alliance", "Star Wars Lore", "Factions", "FACTIONS", 63,
                "The Separatist Alliance (Confederacy of Independent Systems) broke from the Republic during the Clone Wars. Led by Count Dooku and General Grievous, with financial support from the Trade Federation. The Separatists used droid armies. The conflict was secretly orchestrated by Palpatine as both Sidious and Tyranus.",
                "The Separatists were defeated when their droid army was shut down."));

        list.add(makeEntry("FACTION_SITH", "Sith Order", "Star Wars Lore", "Factions", "FACTIONS", 64,
                "The Sith are Dark Side users who follow the Rule of Two: one Master to embody power, one apprentice to crave it. The Sith seek to overthrow the Jedi and rule the galaxy. Key Sith: Palpatine (Sidious), Dooku (Tyranus), Vader (Darth Vader), Plagueis the Wise. The Sith operate in secrecy, manipulating events from the shadows.",
                "The Rule of Two was established by Darth Bane 1,000 years before the Clone Wars."));

        list.add(makeEntry("FACTION_FIRST_ORDER", "First Order", "Star Wars Lore", "Factions", "FACTIONS", 65,
                "The First Order is a militaristic remnant of the Galactic Empire that rose after the New Republic. Led by Supreme Leader Snoke and Kylo Ren. The First Order possesses advanced technology including Starkiller Base and the Supremae-class Star Destroyer. They seek to destroy the Resistance and remake the galaxy in their image.",
                "The First Order was defeated at the Battle of Exegol."));

        list.add(makeEntry("FACTION_RESISTANCE", "Resistance", "Star Wars Lore", "Factions", "FACTIONS", 66,
                "The Resistance is a military force formed by Leia Organa to counter the First Order threat. Led by Poe Dameron and supported by Luke Skywalker (briefly) and Rey. The Resistance operates from hidden bases and uses guerrilla tactics. Key members: Finn, Rose Tico, BB-8, C-3PO, R2-D2.",
                "The Resistance became the foundation of the New Republic's rebuilt military."));

        list.add(makeEntry("FACTION_JEDI", "Jedi Order", "Star Wars Lore", "Factions", "FACTIONS", 67,
                "The Jedi Order is an ancient organization of Force-sensitive peacekeepers. The Jedi follow the Light Side and maintain peace through meditation, training, and combat. The Order was nearly destroyed by Order 66, which turned the Clone Army against the Jedi. Survivors included Yoda, Obi-Wan, Luke, and Ahsoka Tano.",
                "The Jedi Order was rebuilt by Luke Skywalker and later by Rey."));

        // ============================================================
        // GALACTIC ERAS
        // ============================================================

        list.add(makeEntry("ERA_OLD_REPUBLIC", "Old Republic Era", "Star Wars Lore", "Galactic Eras", "GALACTIC_ERAS", 70,
                "The Old Republic spanned thousands of years before the fall of the Republic. During this era, the Jedi and Sith engaged in ancient conflicts. The Mandalorian Wars, the Jedi Civil War, and the rise of the Sith Empire defined this period. Key events: the fall of the Sith Empire, the rise of the Republic.",
                "The Old Republic era is the setting for Star Wars: Knights of the Old Republic."));

        list.add(makeEntry("ERA_PREQUEL", "Prequel Era", "Star Wars Lore", "Galactic Eras", "GALACTIC_ERAS", 71,
                "The Prequel Era covers the decline of the Republic and the rise of the Empire. Key events: the Trade Federation invasion of Naboo, the discovery of Anakin Skywalker, the Clone Wars, and the fall of the Republic. This era spans Episodes I-III and the Clone Wars series.",
                "The Prequel Era ends with Order 66 and the establishment of the Empire."));

        list.add(makeEntry("ERA_ORIGINAL", "Original Trilogy Era", "Star Wars Lore", "Galactic Eras", "GALACTIC_ERAS", 72,
                "The Original Trilogy Era covers the Rebel Alliance's fight against the Empire. Key events: the destruction of the Death Star, the rescue of Princess Leia, the training of Luke Skywalker, and the Battle of Endor. This era spans Episodes IV-VI.",
                "The Original Trilogy era ends with the destruction of the Empire."));

        list.add(makeEntry("ERA_SEQUEL", "Sequel Era", "Star Wars Lore", "Galactic Eras", "GALACTIC_ERAS", 73,
                "The Sequel Era covers the rise of the First Order and the search for Luke Skywalker. Key events: the destruction of Jakku, the hunt for Luke, the Battle of Crait, and the defeat of the First Order. This era spans Episodes VII-IX.",
                "The Sequel Era ends with the defeat of Palpatine and the rebuilding of the Jedi."));

        // ============================================================
        // ICONIC BATTLES
        // ============================================================

        list.add(makeEntry("BATTLE_YAVIN", "Battle of Yavin", "Star Wars Lore", "Iconic Battles", "ICONIC_BATTLES", 80,
                "The Battle of Yavin was the first major Rebel victory against the Empire. Rebel X-Wings and Y-Wings attacked the Death Star over Yavin 4. Luke Skywalker, guided by the Force, fired the proton torpedoes that destroyed the Death Star. Key participants: Luke Skywalker, Han Solo, Obi-Wan Kenobi, Darth Vader.",
                "The battle proved the Empire was vulnerable and inspired the Rebel cause."));

        list.add(makeEntry("BATTLE_HOTH", "Battle of Hoth", "Star Wars Lore", "Iconic Battles", "ICONIC_BATTLES", 81,
                "The Battle of Hoth was a major Imperial victory against the Rebel Alliance. Imperial AT-AT walkers attacked Echo Base on the ice planet Hoth. The Rebels used snowspeeders to disable the walkers but were ultimately forced to evacuate. Key participants: Luke Skywalker, Han Solo, Leia Organa, Darth Vader.",
                "The battle forced the Rebels to abandon their primary base."));

        list.add(makeEntry("BATTLE_ENDOR", "Battle of Endor", "Star Wars Lore", "Iconic Battles", "ICONIC_BATTLES", 82,
                "The Battle of Endor was the decisive Rebel victory that ended the Galactic Civil War. Rebel forces attacked the second Death Star while a ground team disabled its shield generator. Luke Skywalker confronted his father, Han Solo led the ground assault, and the Rebel fleet engaged the Imperial fleet. Key participants: Luke Skywalker, Han Solo, Leia Organa, Darth Vader, Emperor Palpatine.",
                "The battle resulted in the death of Palpatine and Vader, and the fall of the Empire."));

        list.add(makeEntry("BATTLE_GEONOSIS", "Battle of Geonosis", "Star Wars Lore", "Iconic Battles", "ICONIC_BATTLES", 83,
                "The Battle of Geonosis marked the start of the Clone Wars. Jedi and Clone Troopers fought Separatist droids in the Petranaki Arena and across Geonosis. Key participants: Obi-Wan Kenobi, Anakin Skywalker, Padme Amidala, Mace Windu, Jango Fett, Count Dooku, General Grievous.",
                "The battle established the Clone Army as the Republic's military force."));

        list.add(makeEntry("BATTLE_JAKKU", "Battle of Jakku", "Star Wars Lore", "Iconic Battles", "ICONIC_BATTLES", 84,
                "The Battle of Jakku was the final major battle of the Galactic Civil War. Rebel forces defeated the remaining Imperial fleet, effectively ending the Empire's rule. The battle led to the Galactic Concordance, which limited military power across the galaxy. Remnants of the Empire would later form the First Order.",
                "The battle is considered the end of the Galactic Civil War."));

        // ============================================================
        // SPECIES
        // ============================================================

        list.add(makeEntry("SPECIES_HUMAN", "Humans", "Star Wars Lore", "Species", "SPECIES", 90,
                "Humans are the most common species in the Star Wars galaxy. They are found on Coruscant, Tatooine, Naboo, Alderaan, and countless other worlds. Humans include Jedi, Sith, politicians, soldiers, and civilians. Notable humans: Anakin Skywalker, Luke Skywalker, Leia Organa, Han Solo, Palpatine, Obi-Wan Kenobi.",
                "Humans dominate galactic politics and culture."));

        list.add(makeEntry("SPECIES_TWI_LEK", "Twi'lek", "Star Wars Lore", "Species", "SPECIES", 91,
                "Twi'lek are a humanoid species known for their head-tails (lekku). They are native to Ryloth and are found throughout the galaxy. Twi'lek are known for their diplomacy and entertainment skills. Notable Twi'lek: Bossk, Oola, Cham Syndulla.",
                "Twi'lek have been enslaved by the Empire and other factions."));

        list.add(makeEntry("SPECIES_WOOKIEE", "Wookiees", "Star Wars Lore", "Species", "SPECIES", 92,
                "Wookiees are a tall, hairy species native to Kashyyyk. They are known for their strength, loyalty, and craftsmanship. Wookiees speak Shyriiwook, which is translated by droids. Notable Wookiees: Chewbacca, Tarfful, Logray.",
                "Chewbacca is one of the most famous Wookiees, serving as Han Solo's co-pilot."));

        list.add(makeEntry("SPECIES_DROID", "Droids", "Star Wars Lore", "Species", "SPECIES", 93,
                "Droids are robotic beings that serve various functions in the Star Wars galaxy. They include astromech droids (R2-D2, BB-8), protocol droids (C-3PO), battle droids, and assassin droids (IG-88). Droids can gain sentience and develop personality. Notable droids: R2-D2, C-3PO, BB-8, IG-88, K-2SO.",
                "Droids are essential to galactic infrastructure and daily life."));

        return list;
    }

    private RulesEntry makeEntry(String id, String title, String source, String chapter,
                                  String category, Integer page, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.STAR_WARS);
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS);
    }

    /**
     * Gets all rule entries for a given category.
     *
     * @param category the category (e.g., "GROUND_COMBAT", "SPACE_COMBAT")
     * @return list of entries in that category
     */
    public List<RulesEntry> getRulesByCategory(String category) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.STAR_WARS, category);
    }

    /**
     * Gets all rule entries from a given source.
     *
     * @param source the source (e.g., "Star Wars: Legion", "Star Wars: X-Wing")
     * @return list of entries from that source
     */
    public List<RulesEntry> getRulesBySource(String source) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(GameSystem.STAR_WARS, source);
    }

    /**
     * Searches rule entries by keyword in title and text.
     *
     * @param keyword the search keyword
     * @return list of matching entries
     */
    public List<RulesEntry> searchRules(String keyword) {
        ensureInitialized();
        List<RulesEntry> results = new ArrayList<>(entryRepository.searchByTitle(GameSystem.STAR_WARS, keyword));
        List<RulesEntry> textResults = entryRepository.searchByText(GameSystem.STAR_WARS, keyword);
        for (RulesEntry e : textResults) {
            if (!results.contains(e)) {
                results.add(e);
            }
        }
        // Also check notes field (not indexed by repository methods)
        List<RulesEntry> allEntries = entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS);
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).stream()
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).stream()
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
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).size();
    }

    /**
     * Gets a rule entry by ID (matches against title or notes).
     *
     * @param id the entry ID
     * @return the entry or null if not found
     */
    public RulesEntry getEntryById(String id) {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).stream()
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
        return entryRepository.findByGameSystemAndChapterAndActiveTrue(GameSystem.STAR_WARS, chapter);
    }

    /**
     * Gets all available chapters.
     *
     * @return list of unique chapters
     */
    public List<String> getChapters() {
        ensureInitialized();
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.STAR_WARS).stream()
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

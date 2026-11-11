package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesEntryRepository;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing and querying the D&D 5e rulebook reference database.
 * <p>
 * Uses the unified {@link RulesEntry} and {@link RulesTable} models with
 * {@link GameSystem#DND_5E} to store and query D&D 5e core rules.
 * </p>
 */
@Service
public class Dnd5eRulesService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eRulesService.class);

    private static final int EXPECTED_ENTRY_COUNT_MIN = 60;

    private final UnifiedRulesEntryRepository entryRepository;
    private final UnifiedRulesTableRepository tableRepository;
    private boolean initialized = false;

    public Dnd5eRulesService(UnifiedRulesEntryRepository entryRepository,
                             UnifiedRulesTableRepository tableRepository) {
        this.entryRepository = entryRepository;
        this.tableRepository = tableRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRulesDatabase() {
        if (initialized) {
            return;
        }
        long existingCount = entryRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E).size();
        if (existingCount >= EXPECTED_ENTRY_COUNT_MIN) {
            log.info("D&D 5e rules database already populated with {} entries", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e rules database...");
        populateRuleEntries();
        populateReferenceTables();
        initialized = true;
        log.info("D&D 5e rules database initialized: {} entries, {} table entries",
                entryRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E).size(),
                tableRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E).size());
    }

    // ==================== Rule Entries Population ====================

    @Transactional
    public void populateRuleEntries() {
        List<RulesEntry> entries = new ArrayList<>();

        // ============================================================
        // PHB — Core Rules
        // ============================================================

        // --- ABILITY CHECKS ---
        entries.add(createEntry("Ability Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "When you attempt something uncertain, the DM calls for an ability check. Roll a d20 and add the relevant ability modifier. If the total meets or exceeds a Difficulty Class (DC), the check succeeds. DCs range from 5 (very easy) to 25 (near impossible). The six abilities are Strength, Dexterity, Constitution, Intelligence, Wisdom, and Charisma.",
                "Each ability has associated skills. Proficiency in a skill adds your proficiency bonus to that skill's ability check."));

        entries.add(createEntry("Strength Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "Strength measures bodily power, athletic training, and the extent to which you can apply raw force to a situation. Use Strength checks for actions like breaking objects, forcing open doors, leaping great distances, or overpowering opponents in combat. Common DCs: 10 for average tasks, 15 for difficult tasks, 20 for very difficult tasks.",
                "Athletics is the Strength-based skill for climbing, jumping, and swimming."));

        entries.add(createEntry("Dexterity Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "Dexterity covers agility, reflexes, and balance. Use Dexterity checks for acrobatics, stealth, sleight of hand, and reacting quickly to danger. Your Dexterity modifier also affects your Armor Class (AC) and initiative rolls.",
                "Acrobatics, Sleight of Hand, and Stealth are Dexterity-based skills."));

        entries.add(createEntry("Constitution Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "Constitution measures stamina and vitality. Constitution checks are rare but used for holding breath, resisting poison or disease, and maintaining concentration on a spell while taking damage. Your Constitution modifier also affects your hit points.",
                "Concentration saves use the damage taken or the DC of the effect, whichever is higher."));

        entries.add(createEntry("Intelligence Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "Intelligence reflects reasoning and memory. Use Intelligence checks for recalling knowledge, analyzing data, or making educated guesses. Arcana, History, Investigation, and Nature are Intelligence-based skills.",
                "An Intelligence check without a specific skill is a raw knowledge check."));

        entries.add(createEntry("Wisdom Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "Wisdom represents intuition, self-awareness, and insight. Use Wisdom checks for perceiving details, sensing motives, or resisting mental influence. Animal Handling, Insight, Medicine, Perception, and Religion are Wisdom-based skills.",
                "Perception is the most commonly used Wisdom skill for detecting hidden things."));

        entries.add(createEntry("Charisma Checks", "PHB", "Core Rules", "ABILITY_CHECKS", 230,
                "Charisma measures force of personality, persuasiveness, and social grace. Use Charisma checks for influencing others, performing, or bluffing. Deception, Intimidation, Performance, and Persuasion are Charisma-based skills.",
                "Charisma is the primary ability for Bards, Sorcerers, and Warlocks."));

        // --- SAVING THROWS ---
        entries.add(createEntry("Saving Throws", "PHB", "Core Rules", "SAVING_THROWS", 232,
                "A saving throw represents an attempt to resist a spell, trap, or other harmful effect. Roll a d20 and add the relevant ability modifier. If the total meets or exceeds the DC, the saving throw succeeds and you avoid or reduce the effect. You can be proficient in certain saving throws, adding your proficiency bonus.",
                "Each class grants proficiency in two saving throws. Clerics get STR and WIS, Wizards get INT and WIS, etc."));

        entries.add(createEntry("Death Saving Throws", "PHB", "Core Rules", "SAVING_THROWS", 232,
                "When you drop to 0 hit points, you fall unconscious and must make death saving throws. At the start of each of your turns, roll a d20. On a 10 or higher, you gain a success. On a 9 or lower, you gain a failure. A natural 20 counts as two successes and you regain 1 hit point. A natural 20 counts as two failures. Three successes stabilize you. Three failures means you die.",
                "Critical hits deal damage to a creature at 0 HP count as one death save failure."));

        // --- ATTACK ROLLS ---
        entries.add(createEntry("Attack Rolls", "PHB", "Combat", "ATTACK_ROLLS", 194,
                "When you make an attack, roll a d20 and add an attack bonus. The attack bonus equals your proficiency bonus (if proficient with the weapon) plus the relevant ability modifier (STR for melee, DEX for ranged, unless the weapon has the Finesse or Thrown property). If the total equals or exceeds the target's AC, the attack hits.",
                "Melee weapon attacks use STR by default. Ranged weapon attacks use DEX. Finesse weapons let you choose STR or DEX."));

        entries.add(createEntry("Armor Class (AC)", "PHB", "Combat", "ATTACK_ROLLS", 144,
                "Armor Class measures how hard it is to land a blow on a creature. Base AC is 10 + Dexterity modifier. Armor adds a fixed value or uses a formula: Light armor = armor value + DEX modifier; Medium armor = armor value + DEX modifier (max listed); Heavy armor = armor value (no DEX). Shields add +2 to AC.",
                "Unarmored Defense class features (Monk, Barbarian, Druid) replace the base AC calculation."));

        entries.add(createEntry("Melee Attacks", "PHB", "Combat", "ATTACK_ROLLS", 195,
                "A melee attack targets a creature within your reach (usually 5 feet). Roll d20 + attack bonus vs. target's AC. On a hit, roll the weapon's damage dice and add the ability modifier. Opportunity attacks are melee attacks made when an enemy leaves your reach.",
                "Reach weapons (glaive, whip) have 10-foot reach."));

        entries.add(createEntry("Ranged Attacks", "PHB", "Combat", "ATTACK_ROLLS", 195,
                "A ranged attack targets a creature within the weapon's range. Roll d20 + attack bonus vs. AC. Weapons have a normal range and a long range. Attacks beyond normal range have disadvantage. Beyond long range is impossible. If an enemy is within 5 feet of you, you have disadvantage on ranged attacks.",
                "Ammunition is required for ranged weapons and is consumed on a hit."));

        // --- DAMAGE ---
        entries.add(createEntry("Damage and Healing", "PHB", "Combat", "DAMAGE", 196,
                "When an attack hits, roll the weapon's damage dice and add the ability modifier. Damage types include bludgeoning, piercing, slashing (melee), and piercing, slashing (ranged). Some attacks deal non-weapon damage types: acid, cold, fire, force, lightning, necrotic, poison, psychic, radiant, thunder. Subtract damage from current HP. At 0 HP, you fall unconscious and start making death saves.",
                "Healing restores hit points. A creature's HP can't exceed its maximum."));

        entries.add(createEntry("Critical Hits", "PHB", "Combat", "DAMAGE", 196,
                "When you roll a 20 on the attack die, you score a critical hit. You double the number of damage dice for the attack (not the flat modifier). Roll all the dice and add the modifier once. For example, a longsword normally deals 1d8+3 damage; on a critical hit, it deals 2d8+3.",
                "Spells that require attack rolls also critically hit on a natural 20."));

        entries.add(createEntry("Critical Fails", "PHB", "Combat", "DAMAGE", 196,
                "Rolling a 1 on an attack roll is an automatic miss, regardless of modifiers or the target's AC. This is commonly called a 'fumble' but the rules simply state it is an automatic miss.",
                "Some DMs add additional fumble effects, but this is not in the core rules."));

        // --- DAMAGE TYPES ---
        entries.add(createEntry("Damage Types", "PHB", "Combat", "DAMAGE_TYPES", 196,
                "There are 12 damage types in D&D 5e: Acid, Bludgeoning, Cold, Fire, Force, Lightning, Necrotic, Piercing, Poison, Psychic, Radiant, Slashing, Thunder. Creatures can have resistance (half damage), vulnerability (double damage), or immunity (no damage) to specific damage types. Bludgeoning, piercing, and slashing are physical damage types.",
                "Magic weapons overcome non-magical resistance to physical damage types."));

        // --- COMBAT PHASES ---
        entries.add(createEntry("Combat Overview", "PHB", "Combat", "COMBAT_PHASES", 190,
                "Combat follows a structured sequence: 1) Determine surprise. 2) Roll initiative (DEX check for each creature). 3) Act in initiative order. 4) On your turn, you can move up to your speed and take an action. 5) Combat ends when one side is defeated or surrenders. Each round represents 6 seconds of game time.",
                "A round consists of each participant taking one turn in initiative order."));

        entries.add(createEntry("Initiative", "PHB", "Combat", "COMBAT_PHASES", 190,
                "At the start of combat, every participant rolls initiative: d20 + Dexterity modifier. The DM orders the combatants from highest to lowest. This order determines turn sequence for the entire combat. Ties are broken by the DM or by rerolling.",
                "The Ready action lets you act on someone else's turn, but you still use your original initiative count."));

        entries.add(createEntry("Your Turn in Combat", "PHB", "Combat", "COMBAT_PHASES", 190,
                "On your turn, you can: 1) Move up to your speed. 2) Take one action (Attack, Cast a Spell, Dash, Disengage, Dodge, Help, Hide, Ready, Use an Object). 3) Take one bonus action (if you have a feature that grants one). 4) Use one reaction (if you have one available). You can also interact with one object for free.",
                "You can move between actions and bonus actions."));

        entries.add(createEntry("Actions in Combat", "PHB", "Combat", "COMBAT_ACTIONS", 192,
                "Main actions: Attack (one melee or ranged attack), Cast a Spell (if casting time is 1 action), Dash (double your movement), Disengage (end your movement without provoking opportunity attacks), Dodge (attacks against you have advantage if you move), Help (give advantage to an ally's check or attack), Hide (make a stealth check), Ready (prepare an action for a trigger), Use an Object (interact with an item).",
                "Some class features grant additional actions."));

        entries.add(createEntry("Movement in Combat", "PHB", "Combat", "MOVEMENT", 191,
                "Your speed is the distance you can move in a turn. Most humanoids have 30 feet. You can move before, after, or between your action and bonus action. Difficult terrain costs extra movement (each foot costs 2 feet). You can climb, swim, or fall during movement. Entering an enemy's space is not allowed unless the enemy is at least two sizes larger or smaller.",
                "The Dash action gives you extra movement equal to your speed."));

        // --- CONDITIONS ---
        entries.add(createEntry("Conditions in Combat", "PHB", "Combat", "CONDITIONS", 290,
                "Conditions modify a creature's capabilities. The main conditions are: Blinded (can't see, attacks have advantage, attacks by it have disadvantage, DEX saves have disadvantage), Charmed (can't be hostile, DM's choice for influence), Deafened (can't hear, audio-based checks fail), Frightened (disadvantage on checks and attacks while source is in sight, can't move toward source), Grappled (speed is 0, ends on escape or if grappler is incapacitated), Incapacitated (can't take actions or reactions), Invisible (attacks have advantage, attacks by it have disadvantage), Paralyzed (incapacitated, can't move or speak, attacks within 5 feet are critical hits, saves against poison/auto-fail, melee attacks hit automatically), Petrified (transformed to stone, poisoned and blinded, weight doubles, falls prone), Poisoned (disadvantage on attack rolls and ability checks), Prone (hard to stand up, attacks within 5 feet have advantage, attacks beyond 5 feet have disadvantage, can dash to stand up), Restrained (speed is 0, attacks have advantage, attacks by it have disadvantage, DEX saves have disadvantage), Stunned (incapacitated, attacks within 5 feet hit automatically), Unconscious (incapacitated, drops what it holds, falls prone, can't move or sense, attacks within 5 feet are critical hits, attacks beyond 5 feet have advantage).",
                "A condition lasts until it is removed by an effect or the end of a duration."));

        // --- SPELLCASTING ---
        entries.add(createEntry("Spellcasting Overview", "PHB", "Spellcasting", "SPELLCASTING", 200,
                "Spellcasting is the imposition of your will upon reality to change it. Each spell has a level (0-9), a school, a casting time, a range, components, a duration, and an effect. To cast a spell, you must have a spell slot of the spell's level or higher. You spend the slot to cast the spell. Spell slots are recovered after a long rest.",
                "Cantrips are level 0 spells that don't use spell slots and can be cast at will."));

        entries.add(createEntry("Spell Slots", "PHB", "Spellcasting", "SPELLCASTING", 200,
                "Spell slots are the energy used to cast spells. Each class has a spell slot table showing how many slots of each level you have at each character level. When you cast a spell, you spend a slot of the spell's level or higher. You can upcast a spell using a higher-level slot for enhanced effects. All expended spell slots are recovered after a long rest.",
                "Short rests do not recover spell slots, except for specific features like Warlock's Pact Magic."));

        entries.add(createEntry("Spell Components", "PHB", "Spellcasting", "SPELLCASTING", 201,
                "Spells have three types of components: Verbal (V) — spoken words; Somatic (S) — hand gestures; Material (M) — physical objects. Material components are specified in the spell description. A component pouch or spellcasting focus can substitute for material components that lack a gold cost and aren't consumed.",
                "A spell with a Somatic component requires a free hand unless you have a focus or material component."));

        entries.add(createEntry("Spell Schools", "PHB", "Spellcasting", "SPELL_SCHOOLS", 201,
                "There are 8 schools of magic: Abjuration (barriers and protections), Conjuration (teleportation and summoning), Divination (gaining information), Enchantment (influencing minds), Evocation (manipulating energy), Necromancy (life force manipulation), Transmutation (altering properties), Illusion (creating false senses).",
                "Arcane Tricksters and Warlocks have restricted spell lists based on school."));

        // --- RESTING ---
        entries.add(createEntry("Short Rest", "PHB", "Resting", "RESTING", 186,
                "A short rest is a period of downtime, at least 1 hour long, during which a character does nothing more strenuous than eating, drinking, reading, and tending to wounds. A character can spend one or more Hit Dice at the end of a short rest, rolling each die and regaining hit points equal to the roll + Constitution modifier (minimum 1). The character can spend as many Hit Dice as they want, up to their maximum number.",
                "Some class features (Warlock spell slots, Barbarian rage, Monk ki points) are recovered on a short rest."));

        entries.add(createEntry("Long Rest", "PHB", "Resting", "RESTING", 186,
                "A long rest is a period of extended downtime, at least 8 hours long, during which a character sleeps for at least 6 hours and performs no more than 2 hours of light activity. At the end of a long rest, a character regains all hit points and all expended spell slots. The character regains half of their maximum Hit Dice. A character can benefit from no more than one long rest in a 24-hour period.",
                "Exhaustion levels are reduced by 1 after a long rest."));

        // --- PROFICIENCY BONUS ---
        entries.add(createEntry("Proficiency Bonus", "PHB", "Core Rules", "PROFICIENCY", 230,
                "Your proficiency bonus represents your training and expertise. It starts at +2 at level 1 and increases to +5 at level 17. You add your proficiency bonus to d20 rolls for attacks with weapons you are proficient with, saving throws you are proficient in, skill checks for skills you are proficient in, and the DC of spells and features you create. You never add the bonus more than once to a given roll.",
                "Proficiency bonus by level: 1-4: +2, 5-8: +3, 9-12: +4, 13-16: +5, 17-20: +6."));

        // --- SKILLS ---
        entries.add(createEntry("Skills List", "PHB", "Skills", "SKILLS", 230,
                "There are 18 skills in D&D 5e: Acrobatics (DEX), Animal Handling (WIS), Arcana (INT), Athletics (STR), Deception (CHA), History (INT), Insight (WIS), Intimidation (CHA), Investigation (INT), Medicine (WIS), Nature (INT), Perception (WIS), Performance (CHA), Persuasion (CHA), Religion (INT), Sleight of Hand (DEX), Stealth (DEX), Survival (WIS). Each skill is tied to one ability score.",
                "Proficiency in a skill adds your proficiency bonus to checks using that skill."));

        // --- ABILITY SCORE GENERATION ---
        entries.add(createEntry("Ability Score Generation", "PHB", "Character Creation", "CHARACTER_CREATION", 23,
                "There are several methods for generating ability scores: Standard Array — use the scores 15, 14, 13, 12, 10, 8 and assign them to abilities. Point Buy — spend 27 points to buy scores (cost: 8=0, 9=1, 10=2, 11=3, 12=4, 13=5, 14=7, 15=9). Rolling — roll 4d6 and drop the lowest die for each of the six scores. Racial traits may add bonuses to ability scores.",
                "The DM chooses which method the party uses."));

        // --- HIT DICE ---
        entries.add(createEntry("Hit Dice by Class", "PHB", "Character Creation", "CHARACTER_CREATION", 23,
                "Each class has a hit die that determines your hit points: Barbarian and Paladin use d12, Fighter and Ranger use d10, Cleric, Druid, Monk, and Rogue use d8, Bard and Sorcerer use d6, Wizard uses d4. At 1st level, you gain your hit die + Constitution modifier hit points. At higher levels, you gain the average (rounded up) or roll the die + CON mod.",
                "Hit Dice are also used for healing during short rests."));

        // --- CRAFTING (2024 PHB p.233) ---
        entries.add(createEntry("Crafting Overview", "PHB", "Crafting", "CRAFTING", 233,
                "Characters can craft non-magical items during downtime. Crafting requires a relevant tool proficiency, raw materials costing half the item's price, and time. For every day you work, you make a check using the tool's associated ability. The DC depends on the item's complexity. On a success, you complete work equal to the item's price divided by the DC. Group crafting divides the time by the number of characters working.",
                "Magic item crafting requires special prerequisites and is at the DM's discretion."));

        entries.add(createEntry("Non-Magical Item Crafting", "PHB", "Crafting", "CRAFTING", 233,
                "To craft a non-magical item, you need: 1) Proficiency with the relevant tool. 2) Raw materials costing half the item's gold value. 3) Time — typically 1 day per 25 gp of the item's price. Each day, make an ability check with the tool's associated ability. The DC is set by the DM based on complexity (typically 10-15). On a success, you complete progress equal to 25 gp worth of work.",
                "Common items use DC 10-12. Uncommon items use DC 15. Rare items use DC 17+."));

        entries.add(createEntry("Potion of Healing Crafting", "PHB", "Crafting", "CRAFTING", 233,
                "A character proficient with an herbalism kit can craft a potion of healing. The process takes 8 hours and requires 25 gp worth of herbal reagents and a blank potion vial. Make a WIS check (DC 15) using the herbalism kit. On a success, the potion is complete. On a failure, the materials are wasted.",
                "Potions of healing are the most commonly crafted magical items."));

        entries.add(createEntry("Spell Scroll Crafting", "PHB", "Crafting", "CRAFTING", 233,
                "A character who can cast a spell can create a spell scroll of that spell. The process requires calligraphy supplies and a blank scroll. The cost equals 50 gp × spell level (minimum 50 gp for cantrips). The time required is 8 hours per spell level. Make an INT check using a calligraphy set. The DC equals 15 + spell level.",
                "The spell must be on your class's spell list and of a level you can cast."));

        entries.add(createEntry("Group Crafting", "PHB", "Crafting", "CRAFTING", 233,
                "Multiple characters can work together on a crafting project. Divide the required time by the number of characters working. Each character must have the relevant tool proficiency and contribute materials. Each character makes their own check each day. If any character succeeds, that character's worth of progress is made.",
                "Group crafting is efficient but requires all participants to have downtime."));

        entries.add(createEntry("Tool Proficiencies", "PHB", "Crafting", "CRAFTING", 233,
                "Every tool has an associated ability score for crafting checks. Common crafting tools include: Alchemist's Supplies (INT), Calligraphy Set (DEX), Carpenter's Tools (STR), Cobbler's Tools (DEX), Cook's Utensils (CHA), Glassblower's Tools (DEX), Herbalism Kit (WIS), Jeweler's Tools (DEX), Leatherworker's Tools (DEX), Smith's Hammer (STR), Tailor's Tools (DEX), Woodcarver's Tools (DEX). Proficiency adds your proficiency bonus to crafting checks.",
                "Tools also have combat and exploration uses beyond crafting."));

        // --- HEROIC INSPIRATION (2024 PHB) ---
        entries.add(createEntry("Heroic Inspiration Overview", "PHB 2024", "Core Rules", "HEROIC_INSPIRATION", 254,
                "Heroic Inspiration replaces the old Inspiration mechanic. It lets you reroll any die after rolling it — including attack rolls, saving throws, ability checks, damage rolls, hit points gained on level-up, and wild magic surges. You keep the higher of the two rolls. This is the most versatile resource in the game, as it can improve any outcome.",
                "Heroic Inspiration is not limited to d20 tests — it works on any die roll in the game."));

        entries.add(createEntry("Earning Heroic Inspiration", "PHB 2024", "Core Rules", "HEROIC_INSPIRATION", 254,
                "Humans get one Heroic Inspiration after a long rest. The Musician origin feat awards one Heroic Inspiration after any rest (short or long). The DM can also award Heroic Inspiration for exceptional roleplaying or creative problem-solving, at their discretion.",
                "A character can hold only one Heroic Inspiration at a time, unless a feature explicitly allows more."));

        entries.add(createEntry("Using Heroic Inspiration", "PHB 2024", "Core Rules", "HEROIC_INSPIRATION", 254,
                "When you roll any die, you can spend a Heroic Inspiration to reroll it. You must decide to use it after seeing the first roll but before the outcome is determined. Roll the same die again and keep the higher of the two results. The Heroic Inspiration is consumed regardless of whether the reroll improves the result.",
                "You can use Heroic Inspiration on damage rolls to deal more damage, on HP rolls to gain more hit points, or on any d20 test to improve your chance of success."));

        entries.add(createEntry("Heroic Inspiration Limitations", "PHB 2024", "Core Rules", "HEROIC_INSPIRATION", 254,
                "You can spend Heroic Inspiration only once per die roll. You cannot chain rerolls. Some effects that prevent rerolling also block Heroic Inspiration. The DM has final say on whether a particular die roll can be rerolled with Heroic Inspiration.",
                "Heroic Inspiration is consumed when spent, even if the reroll does not improve the result."));

        // --- REVISED EXHAUSTION (2024 PHB) ---
        entries.add(createEntry("Revised Exhaustion Overview", "PHB 2024", "Core Rules", "EXHAUSTION", 264,
                "Exhaustion is tracked as a level from 0 to 6. Each level imposes a penalty of 2 × exhaustion level on all d20 tests (ability checks, saving throws, and attack rolls). Additionally, your speed is reduced by 5 × exhaustion level feet, to a minimum of 0. At level 6, you suffer a -12 penalty on d20 tests and your speed is reduced by 30 feet.",
                "The revised exhaustion system replaces the old 6-level table with a simple formula, making it easier to track and more impactful at higher levels."));

        entries.add(createEntry("Gaining Exhaustion", "PHB 2024", "Core Rules", "EXHAUSTION", 264,
                "Exhaustion is gained from harsh environments, starvation, dehydration, and other severe stresses. A character who goes a day without food gains 1 level of exhaustion. A character who goes a day without water gains 1 level of exhaustion. Extreme temperatures, forced marches, and certain magical effects can also impose exhaustion levels.",
                "A character can have multiple levels of exhaustion simultaneously. Each level stacks with the others."));

        entries.add(createEntry("Removing Exhaustion", "PHB 2024", "Core Rules", "EXHAUSTION", 264,
                "A long rest removes one level of exhaustion. Finishing a long rest requires at least 8 hours of rest, during which no more than 2 hours are spent on light activities. A character must be at 1 hit point or higher to begin a long rest. Magical effects like Greater Restoration can also remove exhaustion.",
                "Multiple long rests are required to remove multiple levels of exhaustion. Each long rest removes only one level."));

        entries.add(createEntry("Exhaustion Effects by Level", "PHB 2024", "Core Rules", "EXHAUSTION", 264,
                "Level 0: No penalty. Level 1: -2 on d20 tests, speed -5 ft. Level 2: -4 on d20 tests, speed -10 ft. Level 3: -6 on d20 tests, speed -15 ft. Level 4: -8 on d20 tests, speed -20 ft. Level 5: -10 on d20 tests, speed -25 ft. Level 6: -12 on d20 tests, speed -30 ft. Speed cannot be reduced below 0.",
                "At level 6, a character's speed is reduced to 0 if their base speed is 30 feet or less, effectively immobilizing them."));

        // --- FEATS (2024 PHB) ---
        entries.add(createEntry("Feats Overview", "PHB 2024", "Character Options", "FEATS", 237,
                "Feats are optional benefits that characters can select instead of taking an ability score improvement. " +
                "At levels 4, 6, 8, 12, 14, 16, and 19, a character can choose a feat instead of increasing an ability score by 1 (or by 2 if the class allows it). " +
                "Some feats require a minimum ability score. The 2024 PHB revises several feats from the 2014 edition.",
                "Feats are organized into categories: General, Combat, Magic, Skill, and Defense."));

        entries.add(createEntry("2024 Feat Revisions", "PHB 2024", "Character Options", "FEATS", 237,
                "Major changes in the 2024 PHB: Lucky grants advantage instead of rerolls. " +
                "Sharpshooter and Great Weapon Master no longer have the -5/+10 penalty/bonus. " +
                "Sentinel and Polearm Master combo is addressed. " +
                "Magic Initiate's learned spells can use other spell slots of the appropriate level. " +
                "Musician feat awards Heroic Inspiration after any rest.",
                "These changes make feats more balanced and reduce the dominance of certain feat combinations."));

        entries.add(createEntry("Combat Feats", "PHB 2024", "Character Options", "FEATS", 237,
                "Combat feats enhance your fighting abilities. Great Weapon Master adds extra damage dice on critical hits with heavy weapons. " +
                "Sharpshooter lets you ignore cover and distance penalties with ranged weapons. " +
                "Sentinel prevents enemies from targeting your allies. Polearm Master gives you a bonus action attack with polearms. " +
                "Duelist gives +2 damage with one-handed weapons. Moderately Armored and Heavily Armored grant armor proficiencies.",
                "Most combat feats require a minimum Strength or Dexterity score of 13."));

        entries.add(createEntry("Magic Feats", "PHB 2024", "Character Options", "FEATS", 237,
                "Magic feats expand your spellcasting options. Magic Initiate lets you learn three cantrips and one 1st-level spell from any class. " +
                "Ritual Caster gives you a ritual book and the ability to cast spells as rituals. " +
                "War Caster grants advantage on concentration saves and lets you cast spells as reactions.",
                "Magic Initiate can be taken multiple times, each time learning new spells."));

        entries.add(createEntry("General and Skill Feats", "PHB 2024", "Character Options", "FEATS", 237,
                "General feats provide versatile benefits. Tough increases your hit point maximum. " +
                "Observant improves your passive Perception. Lucky grants advantage on rolls. " +
                "Skill Expert increases your proficiency bonus with chosen skills. " +
                "Musician grants Heroic Inspiration after any rest. Actor, Athlete, Crusader, and Navigator provide specialized benefits.",
                "General feats typically have no prerequisites and are available to any character."));

        entries.add(createEntry("Defense Feats", "PHB 2024", "Character Options", "FEATS", 237,
                "Defense feats improve your survivability. Resilient grants resistance to a damage type and saving throw proficiency. " +
                "Durable lets you survive lethal damage once per long rest. " +
                "These feats are essential for characters who want to focus on durability.",
                "Resilient can be taken multiple times for different damage types."));

        // --- TOOL PROFICIENCIES (2024 PHB) ---
        entries.add(createEntry("Tool Proficiencies Overview", "PHB 2024", "Equipment", "TOOLS", 233,
                "In the 2024 PHB, tool proficiencies grant concrete mechanical effects beyond just a bonus to checks. " +
                "Each tool has an associated ability score, crafting uses, combat uses, exploration uses, and a 'Use' action. " +
                "Proficiency with a tool adds your proficiency bonus to ability checks using that tool.",
                "Tool proficiencies are gained through class features, backgrounds, feats, and multiclassing."));

        entries.add(createEntry("Tool Combat Uses", "PHB 2024", "Equipment", "TOOLS", 233,
                "Tools can be used in combat for specialized effects. Alchemist's Supplies can create alchemist's fire as a bonus action. " +
                "Smith's Hammer can repair weapons or armor in the field. Leatherworker's Tools can create armor patches for a temporary AC bonus. " +
                "Tailor's Tools can create disguises or camouflage. Carpenter's Tools can jam doors or break down barriers.",
                "Combat uses typically require an action or bonus action and a successful tool check."));

        entries.add(createEntry("Tool Exploration Uses", "PHB 2024", "Equipment", "TOOLS", 233,
                "Tools aid exploration in diverse ways. Herbalism Kit identifies and prepares medicinal plants. " +
                "Calligraphy Set creates detailed maps during travel. Carpenter's Tools builds shelters or bridges. " +
                "Cook's Utensils preserves food for long journeys. Glassblower's Tools crafts potion vials or lenses.",
                "Exploration uses often involve ability checks with the tool's associated ability score."));

        entries.add(createEntry("Tool Expertise and Use Action", "PHB 2024", "Equipment", "TOOLS", 233,
                "Expertise with a tool doubles your proficiency bonus for checks using that tool. " +
                "Certain class features (e.g., Rogue's Skill Expert) grant expertise. " +
                "The 'Use' action allows a proficient character to actively employ a tool in any situation, " +
                "making tools perpetually relevant rather than passive bonuses.",
                "Expertise is typically limited to a small number of tools per character."));

        // --- PSION CLASS (UA 2025) ---
        entries.add(createEntry("Psion Class Overview", "UA 2025", "Classes", "PSION", 1,
                "The Psion is a new class in D&D 2024 that uses psionic power points instead of spell slots. " +
                "Psions harness the power of their minds to manipulate reality, control thoughts, and reshape matter. " +
                "They use Intelligence or Wisdom as their primary ability score and have four subclasses: " +
                "Psi Warper, Metamorph, Psykinetic, and Telepath.",
                "Psions use power points instead of spell slots, allowing more flexible resource management."));

        entries.add(createEntry("Psionic Power Points", "UA 2025", "Classes", "PSION", 1,
                "Power points fuel a psion's abilities. The number of power points equals the psion's level + 1. " +
                "Cantrips cost no power points. Higher-level powers cost 2-6 power points depending on level. " +
                "Power points are restored after a long rest. Unlike spell slots, power points allow partial spending.",
                "Power point progression: Level 1 = 2 points, Level 10 = 11 points, Level 20 = 21 points."));

        entries.add(createEntry("Psionic Surge", "UA 2025", "Classes", "PSION", 1,
                "Psionic Surge is a class feature that enhances the next psionic power used. " +
                "Surge points increase with level: 0 at level 1-3, 1 at level 4-7, 2 at level 8-11, " +
                "3 at level 12-15, 4 at level 16-19, 5 at level 20. Activating surge costs 1 surge point. " +
                "Surge points are restored after a long rest.",
                "When a power is used with surge active, the effect is enhanced with additional damage or duration."));

        entries.add(createEntry("Psi Warper Subclass", "UA 2025", "Classes", "PSION", 1,
                "Psi Warpers manipulate the fabric of reality itself. They can alter probability, " +
                "create illusions, and warp space. Key powers include Psychic Scream, Psychic Enervation, " +
                "Psychic Reanimation, and Hallucinatory Terrain. Psi Warpers excel at control and deception.",
                "Psi Warpers are the illusionists and reality-benders of the psion subclasses."));

        entries.add(createEntry("Metamorph Subclass", "UA 2025", "Classes", "PSION", 1,
                "Metamorphs reshape their own physical form using psionic energy. They can alter their appearance, " +
                "grow limbs, and harden their skin. Key powers include Psychic Transformation, Psychic Resilience, " +
                "Mirror Image, and Greater Invisibility. Metamorphs excel at survivability and adaptability.",
                "Metamorphs are the shapeshifters and tanks of the psion subclasses."));

        entries.add(createEntry("Psykinetic Subclass", "UA 2025", "Classes", "PSION", 1,
                "Psykinetics manipulate physical objects and creatures with their minds. They can crush, lift, " +
                "and throw objects with telekinetic force. Key powers include Psychic Crush, Psychic Bulwark, " +
                "and Telekinesis. Psykinetics excel at battlefield control and direct damage.",
                "Psykinetics are the telekinetic warriors of the psion subclasses."));

        entries.add(createEntry("Telepath Subclass", "UA 2025", "Classes", "PSION", 1,
                "Telepaths probe and manipulate the minds of others. They can read thoughts, alter memories, " +
                "and communicate across vast distances. Key powers include Detect Thoughts, Modify Memory, " +
                "Psychic Link, Psychic Probe, Clairvoyance, and Contact Other Plane. Telepaths excel at information gathering.",
                "Telepaths are the mind-readers and information specialists of the psion subclasses."));

        entries.add(createEntry("Psionic Powers", "UA 2025", "Classes", "PSION", 1,
                "Psionic powers are the psion's version of spells. They range from cantrips (level 0) to " +
                "9th-level powers. Each power has a power point cost, casting time, duration, and range. " +
                "Powers can require concentration and some can be cast as rituals. Psions know a limited " +
                "number of powers that increase with level: 4 at level 1, up to 15 at level 20.",
                "Psionic powers use the same schools as spells: Abjuration, Conjuration, Divination, Enchantment, Evocation, Illusion, Necromancy, and Transmutation."));

        entryRepository.saveAll(entries);
        log.info("Populated {} D&D 5e rule entries", entries.size());
    }

    // ==================== Reference Tables Population ====================

    @Transactional
    public void populateReferenceTables() {
        List<RulesTable> tables = new ArrayList<>();

        // --- Ability Score Modifiers ---
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                3, "-4", "Extremely low ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                4, "-3", "Very low ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                5, "-3", "Very low ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                6, "-2", "Below average ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                7, "-2", "Below average ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                8, "-1", "Slightly below average"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                9, "-1", "Slightly below average"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                10, "+0", "Average ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                11, "+0", "Average ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                12, "+1", "Slightly above average"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                13, "+1", "Slightly above average"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                14, "+2", "Above average ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                15, "+2", "Above average ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                16, "+3", "Very high ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                17, "+3", "Very high ability"));
        tables.add(createTable("Ability Score Modifiers", "PHB", "TABLE_ABILITY_MOD", "Core Rules", 230,
                18, "+4", "Extremely high ability"));

        // --- Difficulty Classes ---
        tables.add(createTable("Difficulty Classes", "PHB", "TABLE_DC", "Core Rules", 230,
                5, "Very Easy", "Trivial task"));
        tables.add(createTable("Difficulty Classes", "PHB", "TABLE_DC", "Core Rules", 230,
                10, "Easy", "Routine task"));
        tables.add(createTable("Difficulty Classes", "PHB", "TABLE_DC", "Core Rules", 230,
                15, "Medium", "Challenging task"));
        tables.add(createTable("Difficulty Classes", "PHB", "TABLE_DC", "Core Rules", 230,
                20, "Hard", "Very difficult task"));
        tables.add(createTable("Difficulty Classes", "PHB", "TABLE_DC", "Core Rules", 230,
                25, "Very Hard", "Nearly impossible task"));

        // --- Saving Throw DCs ---
        tables.add(createTable("Saving Throw DCs", "PHB", "TABLE_SAVE_DC", "Core Rules", 232,
                8, "Very Easy", "Weak effect"));
        tables.add(createTable("Saving Throw DCs", "PHB", "TABLE_SAVE_DC", "Core Rules", 232,
                10, "Easy", "Minor effect"));
        tables.add(createTable("Saving Throw DCs", "PHB", "TABLE_SAVE_DC", "Core Rules", 232,
                12, "Medium", "Standard effect"));
        tables.add(createTable("Saving Throw DCs", "PHB", "TABLE_SAVE_DC", "Core Rules", 232,
                15, "Hard", "Strong effect"));
        tables.add(createTable("Saving Throw DCs", "PHB", "TABLE_SAVE_DC", "Core Rules", 232,
                17, "Very Hard", "Powerful effect"));
        tables.add(createTable("Saving Throw DCs", "PHB", "TABLE_SAVE_DC", "Core Rules", 232,
                20, "Extreme", "Devastating effect"));

        // --- Armor Class Values ---
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                10, "Unarmored", "10 + DEX modifier"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                11, "Padded Armor", "Light armor + DEX"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                12, "Leather Armor / Shield", "Light armor + DEX or base + shield"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                13, "Studded Leather", "Light armor + DEX"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                14, "Hide Armor / Chain Shirt", "Medium armor + DEX (max 2)"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                15, "Scale Mail / Breastplate", "Medium armor + DEX (max 2)"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                16, "Half Plate / Plate Mail", "Heavy armor or medium + shield"));
        tables.add(createTable("Armor Class Values", "PHB", "TABLE_AC", "Combat", 144,
                18, "Plate + Shield", "Best non-magical armor"));

        // --- Damage Types ---
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                1, "Acid", "Corrosive damage from acids"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                2, "Bludgeoning", "Crushing, bashing damage"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                3, "Cold", "Freezing damage from ice"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                4, "Fire", "Burning damage from flames"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                5, "Force", "Pure magical force damage"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                6, "Lightning", "Electrical damage from storms"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                7, "Necrotic", "Death energy damage"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                8, "Piercing", "Penetrating damage from sharp objects"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                9, "Poison", "Toxic damage from venom"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                10, "Psychic", "Mental damage from mind effects"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                11, "Radiant", "Holy light damage"));
        tables.add(createTable("Damage Types", "PHB", "TABLE_DAMAGE_TYPE", "Combat", 196,
                12, "Slashing", "Cutting damage from blades"));

        // --- Spell Schools ---
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                1, "Abjuration", "Barriers, wards, and protections"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                2, "Conjuration", "Teleportation and summoning"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                3, "Divination", "Gaining information"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                4, "Enchantment", "Influencing minds"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                5, "Evocation", "Manipulating energy"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                6, "Illusion", "Creating false senses"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                7, "Necromancy", "Life force manipulation"));
        tables.add(createTable("Spell Schools", "PHB", "TABLE_SPELL_SCHOOL", "Spellcasting", 201,
                8, "Transmutation", "Altering properties"));

        // --- Spell Levels ---
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                0, "Cantrip", "At-will, no spell slot"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                1, "1st Level", "Basic spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                2, "2nd Level", "Intermediate spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                3, "3rd Level", "Advanced spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                4, "4th Level", "Powerful spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                5, "5th Level", "Major spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                6, "6th Level", "Rare spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                7, "7th Level", "Very rare spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                8, "8th Level", "Legendary spells"));
        tables.add(createTable("Spell Levels", "PHB", "TABLE_SPELL_LEVEL", "Spellcasting", 200,
                9, "9th Level", "Mythic spells"));

        // --- Initiative Results ---
        tables.add(createTable("Initiative Results", "PHB", "TABLE_INITIATIVE", "Combat", 190,
                2, "Last", "Very slow, acts last"));
        tables.add(createTable("Initiative Results", "PHB", "TABLE_INITIATIVE", "Combat", 190,
                5, "Late", "Acts near the end"));
        tables.add(createTable("Initiative Results", "PHB", "TABLE_INITIATIVE", "Combat", 190,
                10, "Middle", "Average initiative"));
        tables.add(createTable("Initiative Results", "PHB", "TABLE_INITIATIVE", "Combat", 190,
                15, "Early", "Acts near the start"));
        tables.add(createTable("Initiative Results", "PHB", "TABLE_INITIATIVE", "Combat", 190,
                20, "First", "Very fast, acts first"));

        // --- Critical Hit Table ---
        tables.add(createTable("Attack Roll Results", "PHB", "TABLE_ATTACK_RESULT", "Combat", 194,
                1, "Critical Miss", "Automatic miss, possible fumble"));
        tables.add(createTable("Attack Roll Results", "PHB", "TABLE_ATTACK_RESULT", "Combat", 194,
                5, "Miss", "Attack misses the target"));
        tables.add(createTable("Attack Roll Results", "PHB", "TABLE_ATTACK_RESULT", "Combat", 194,
                10, "Hit", "Attack hits the target"));
        tables.add(createTable("Attack Roll Results", "PHB", "TABLE_ATTACK_RESULT", "Combat", 194,
                15, "Hit", "Attack hits the target"));
        tables.add(createTable("Attack Roll Results", "PHB", "TABLE_ATTACK_RESULT", "Combat", 194,
                19, "Hit", "Attack hits the target"));
        tables.add(createTable("Attack Roll Results", "PHB", "TABLE_ATTACK_RESULT", "Combat", 194,
                20, "Critical Hit", "Double damage dice"));

        // --- Proficiency Bonus by Level ---
        tables.add(createTable("Proficiency Bonus", "PHB", "TABLE_PROFICIENCY", "Core Rules", 230,
                1, "Levels 1-4", "+2"));
        tables.add(createTable("Proficiency Bonus", "PHB", "TABLE_PROFICIENCY", "Core Rules", 230,
                5, "Levels 5-8", "+3"));
        tables.add(createTable("Proficiency Bonus", "PHB", "TABLE_PROFICIENCY", "Core Rules", 230,
                9, "Levels 9-12", "+4"));
        tables.add(createTable("Proficiency Bonus", "PHB", "TABLE_PROFICIENCY", "Core Rules", 230,
                13, "Levels 13-16", "+5"));
        tables.add(createTable("Proficiency Bonus", "PHB", "TABLE_PROFICIENCY", "Core Rules", 230,
                17, "Levels 17-20", "+6"));

        // --- D&D 5e Conditions ---
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                1, "Blinded", "Can't see, attacks have advantage/disadvantage"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                2, "Charmed", "Can't be hostile to source"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                3, "Deafened", "Can't hear"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                4, "Frightened", "Disadvantage near source, can't approach"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                5, "Grappled", "Speed is 0"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                6, "Incapacitated", "Can't take actions or reactions"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                7, "Invisible", "Attacks have advantage/disadvantage"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                8, "Paralyzed", "Incapacitated, can't move or speak"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                9, "Petrified", "Transformed to stone, poisoned and blinded"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                10, "Poisoned", "Disadvantage on attacks and checks"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                11, "Prone", "Hard to stand, attacks have advantage/disadvantage"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                12, "Restrained", "Speed is 0, attacks have advantage/disadvantage"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                13, "Stunned", "Incapacitated, attacks hit automatically"));
        tables.add(createTable("Conditions", "PHB", "TABLE_CONDITIONS", "Combat", 290,
                14, "Unconscious", "Incapacitated, drops items, falls prone"));

        // --- Crafting DCs ---
        tables.add(createTable("Crafting DCs", "PHB", "TABLE_CRAFTING_DC", "Crafting", 233,
                10, "Simple", "Basic items like arrows and rope"));
        tables.add(createTable("Crafting DCs", "PHB", "TABLE_CRAFTING_DC", "Crafting", 233,
                12, "Moderate", "Weapons, armor, and tools"));
        tables.add(createTable("Crafting DCs", "PHB", "TABLE_CRAFTING_DC", "Crafting", 233,
                15, "Complex", "Potions, spell scrolls, and fine items"));
        tables.add(createTable("Crafting DCs", "PHB", "TABLE_CRAFTING_DC", "Crafting", 233,
                17, "Very Complex", "Rare magical items"));
        tables.add(createTable("Crafting DCs", "PHB", "TABLE_CRAFTING_DC", "Crafting", 233,
                20, "Legendary", "Unique artifacts and relics"));

        // --- Crafting Time by Item Value ---
        tables.add(createTable("Crafting Time", "PHB", "TABLE_CRAFTING_TIME", "Crafting", 233,
                1, "1-25 gp", "1 day"));
        tables.add(createTable("Crafting Time", "PHB", "TABLE_CRAFTING_TIME", "Crafting", 233,
                2, "26-50 gp", "2 days"));
        tables.add(createTable("Crafting Time", "PHB", "TABLE_CRAFTING_TIME", "Crafting", 233,
                3, "51-100 gp", "4 days"));
        tables.add(createTable("Crafting Time", "PHB", "TABLE_CRAFTING_TIME", "Crafting", 233,
                4, "101-250 gp", "8 days"));
        tables.add(createTable("Crafting Time", "PHB", "TABLE_CRAFTING_TIME", "Crafting", 233,
                5, "251-500 gp", "16 days"));
        tables.add(createTable("Crafting Time", "PHB", "TABLE_CRAFTING_TIME", "Crafting", 233,
                6, "500+ gp", "1 day per 25 gp"));

        tableRepository.saveAll(tables);
        log.info("Populated {} D&D 5e reference table entries", tables.size());
    }

    // ==================== Helper Methods ====================

    private RulesEntry createEntry(String title, String source, String chapter, String category,
                                   Integer page, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.DND_5E);
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

    private RulesTable createTable(String tableName, String source, String category, String chapter,
                                   Integer page, int rollValue, String resultLabel, String description) {
        RulesTable table = new RulesTable();
        table.setGameSystem(GameSystem.DND_5E);
        table.setTableName(tableName);
        table.setSource(source);
        table.setCategory(category);
        table.setChapter(chapter);
        table.setPage(page);
        table.setRollValue(rollValue);
        table.setResultLabel(resultLabel);
        table.setDescription(description);
        table.setActive(true);
        return table;
    }

    // ==================== Query Methods ====================

    /**
     * Get all D&D 5e rules.
     */
    public List<RulesEntry> getAllRules() {
        return entryRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E);
    }

    /**
     * Get all D&D 5e rules for a given category.
     */
    public List<RulesEntry> getRulesByCategory(String category) {
        return entryRepository.findByGameSystemAndCategoryAndActiveTrue(GameSystem.DND_5E, category);
    }

    /**
     * Get all D&D 5e rules from a given source.
     */
    public List<RulesEntry> getRulesBySource(String source) {
        return entryRepository.findByGameSystemAndSourceAndActiveTrue(GameSystem.DND_5E, source);
    }

    /**
     * Search D&D 5e rules by keyword in title.
     */
    public List<RulesEntry> searchRules(String keyword) {
        return entryRepository.searchByTitle(GameSystem.DND_5E, keyword);
    }

    /**
     * Get all distinct categories for D&D 5e rules.
     */
    public List<String> getCategories() {
        List<RulesEntry> entries = entryRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E);
        return entries.stream().map(RulesEntry::getCategory).distinct().sorted().toList();
    }

    /**
     * Get all distinct sources for D&D 5e rules.
     */
    public List<String> getSources() {
        List<RulesEntry> entries = entryRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E);
        return entries.stream().map(RulesEntry::getSource).distinct().sorted().toList();
    }

    /**
     * Get all table entries for a given table name.
     */
    public List<RulesTable> getTableEntries(String tableName) {
        return tableRepository.findByGameSystemAndTableNameAndActiveTrue(GameSystem.DND_5E, tableName);
    }

    /**
     * Get all D&D 5e table entries.
     */
    public List<RulesTable> getAllTableEntries() {
        return tableRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E);
    }

    /**
     * Get all distinct table categories for D&D 5e.
     */
    public List<String> getAllTableCategories() {
        List<RulesTable> tables = tableRepository.findByGameSystemAndActiveTrue(GameSystem.DND_5E);
        return tables.stream().map(RulesTable::getCategory).distinct().sorted().toList();
    }
}

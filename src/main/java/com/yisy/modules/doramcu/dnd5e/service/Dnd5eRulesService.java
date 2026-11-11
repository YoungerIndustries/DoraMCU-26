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

    private static final int EXPECTED_ENTRY_COUNT_MIN = 159;

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

        // --- HORROR MECHANICS (Ravenloft) ---
        entries.add(createEntry("Dark Gifts Overview", "Ravenloft", "Horror", "DARK_GIFT", 1,
                "Dark Gifts are horror-themed abilities that characters in the Domains of Dread can acquire. " +
                "Each Dark Gift provides a powerful benefit but comes with a curse or drawback. " +
                "Dark Gifts are categorized by type: Undead, Fiend, Aberration, Fey, and Beast. " +
                "Acquiring a Dark Gift increases the character's dread level by 10.",
                "Dark Gifts represent the corrupting influence of the Domains of Dread on those who dwell within them."));

        entries.add(createEntry("Fear Level Mechanics", "Ravenloft", "Horror", "FEAR", 1,
                "Fear Level ranges from 0 (no fear) to 6 (paralyzed with fear). " +
                "Fear Level 0: No effect. Fear Level 1-2: Disadvantage on d20 tests. " +
                "Fear Level 3-4: Disadvantage on d20 tests, cannot take reactions. " +
                "Fear Level 5-6: Paralyzed with fear, cannot act, move, or speak. " +
                "Fear checks use WIS saving throws vs DC 15. Success reduces fear by 1; failure increases fear by 1.",
                "Fear is a core mechanic in the Domains of Dread, representing the psychological toll of horror."));

        entries.add(createEntry("Dread Level Mechanics", "Ravenloft", "Horror", "DREAD", 1,
                "Dread Level ranges from 0 to 100, representing a character's corruption by the Domain of Dread. " +
                "Dread increases when encountering horror, failing fear checks, or acquiring Dark Gifts. " +
                "Dread decreases when succeeding on fear checks, defeating horrors, or leaving the Domain. " +
                "At certain thresholds, dread causes physical and mental changes to the character.",
                "Dread is the long-term corruption mechanic that drives the horror narrative in Ravenloft."));

        entries.add(createEntry("Dread Threshold Effects", "Ravenloft", "Horror", "DREAD", 1,
                "Dread 10+: Nightmares and disturbing visions. " +
                "Dread 25+: Physical changes: pale skin, dark circles. " +
                "Dread 50+: Mental changes: paranoia, distrust. " +
                "Dread 75+: Supernatural changes: shadowy aura, cold touch. " +
                "Dread 90+: Transformation: becoming part of the Domain.",
                "Dread threshold effects mark the progression of a character's corruption by the Domains of Dread."));

        entries.add(createEntry("Darklords and Domains", "Ravenloft", "Horror", "DARKLORD", 1,
                "Each Domain of Dread is ruled by a Darklord, a powerful villain anchored to their domain " +
                "by a phylactery. Notable Darklords include Strahd (Barovia), Azalin (Ravenloft), " +
                "Vecna (Tharizdun), and Lamordia (Lamordia). " +
                "Darklords possess unique powers tied to their domain and can only be defeated " +
                "by destroying their phylactery.",
                "Darklords are the eternal rulers of the Domains of Dread, each bound to their domain by dark magic."));

        entries.add(createEntry("Domains of Dread", "Ravenloft", "Horror", "DOMAIN", 1,
                "The Domains of Dread are isolated worlds connected by the Mist. " +
                "Known domains include Barovia, Ravenloft, Ettin, Tharizdun, Harmach, " +
                "Dementlieu, Isle of Dread, Fen'Harel, Lamordia, and Menagerie. " +
                "Characters trapped in a Domain must find a way to escape through the Mist " +
                "or risk being consumed by the Domain's darkness.",
                "The Domains of Dread form the setting for Ravenloft horror campaigns."));

        // ==================== Growing Magic Items (Arcana Unleashed) ====================

        entries.add(createEntry("Growing Magic Items Overview", "Arcana Unleashed", "Growing Items", "GROWING", 1,
                "Growing magic items are powerful artifacts that scale with the character's level. " +
                "Unlike static magic items, these items grow stronger as the character advances, " +
                "unlocking new abilities at levels 1, 5, 10, 15, and 20. " +
                "They require attunement and are bound to a single character. " +
                "When a character levels up, their growing items level up automatically.",
                "Growing magic items provide balanced power progression throughout a character's entire career."));

        entries.add(createEntry("Growing Item Level Thresholds", "Arcana Unleashed", "Growing Items", "THRESHOLD", 1,
                "Level 1: Base ability and stat bonus unlocked. " +
                "Level 5: Second ability tier unlocked; stat bonus increases. " +
                "Level 10: Third ability tier unlocked; significant power increase. " +
                "Level 15: Fourth ability tier unlocked; item approaches legendary power. " +
                "Level 20: Ultimate ability unlocked; item reaches its full potential. " +
                "Each tier builds upon the previous, adding new capabilities rather than replacing them.",
                "The five-tier progression ensures growing items remain relevant from level 1 to 20."));

        entries.add(createEntry("Growing Item Attunement", "Arcana Unleashed", "Growing Items", "ATTUNEMENT", 1,
                "Growing magic items require attunement. A character can attune to up to 3 magic items. " +
                "To attune, a character must spend 1 hour in contact with the item during a short or long rest. " +
                "To end attunement, a character must spend another short or long rest. " +
                "If a character's attuned items exceed 3, they lose attunement to the excess items. " +
                "Growing items remain bound to their character even if attunement is broken.",
                "Attunement limits ensure growing items don't overwhelm the magic item economy."));

        entries.add(createEntry("Growing Item Categories", "Arcana Unleashed", "Growing Items", "CATEGORY", 1,
                "Growing items come in various categories: Weapon, Armor, Wondrous Item, Ring, Amulet, Rod, Staff, and Wand. " +
                "Each category has different mechanical effects. Weapons enhance combat, armor provides defense, " +
                "wondrous items grant unique abilities, and focus items (rods, staves, wands) enhance spellcasting. " +
                "Rings and amulets provide passive benefits. " +
                "A character can have multiple growing items of different categories.",
                "Category diversity allows characters to customize their growing item loadout."));

        entries.add(createEntry("Growing Item Rarity", "Arcana Unleashed", "Growing Items", "RARITY", 1,
                "Growing items range from Uncommon to Artifact rarity. " +
                "Uncommon: Minor growing items with modest scaling. " +
                "Rare: Standard growing items with meaningful progression. " +
                "Very Rare: Powerful growing items with significant abilities. " +
                "Legendary: Exceptional growing items with game-changing powers. " +
                "Artifact: Unique growing items with narrative significance and ultimate power. " +
                "Rarity determines the item's starting power and growth rate.",
                "Rarity provides DMs with tools to balance growing items for their campaign."));

        entries.add(createEntry("Creating Growing Items", "Arcana Unleashed", "Growing Items", "CREATION", 1,
                "When creating a growing item, define abilities for each of the 5 tiers. " +
                "Each tier should add new functionality rather than simply increasing numbers. " +
                "Consider the character's class and playstyle when designing abilities. " +
                "Balance growing items against standard magic items of equivalent rarity. " +
                "A growing item's total power at level 20 should roughly equal a legendary item of the same category. " +
                "Use stat bonuses to provide consistent value between ability unlocks.",
                "DMs can create custom growing items to reward specific characters or advance the campaign narrative."));

        entries.add(createEntry("Growing Items in Campaigns", "Arcana Unleashed", "Growing Items", "CAMPAIGN", 1,
                "Growing items work best in long campaigns where characters progress through multiple levels. " +
                "Award a growing item at level 1 or as a significant reward early in the campaign. " +
                "Tie the item's lore to the campaign narrative for maximum impact. " +
                "Use ability unlocks at levels 5, 10, 15, and 20 as milestone rewards. " +
                "Consider making the growing item a plot device that grows with the story. " +
                "Limit the number of growing items per character to maintain balance.",
                "Growing items enhance campaign engagement by providing continuous character progression."));

        // ==================== Villainous Options (UA 2026) ====================

        entries.add(createEntry("Villainous Options Overview", "Villainous Options UA", "Villains", "OVERVIEW", 1,
                "Villainous Options provides mechanics for creating and running powerful antagonists. " +
                "Villains include Liches, Death Knights, Archfey, Dragons, Demon Lords, and more. " +
                "Each villain has unique mechanics: lair actions, legendary actions, regional effects, " +
                "and minion spawning. Villains drive campaign narratives and provide epic encounters.",
                "Villainous Options gives DMs tools to create memorable and challenging antagonists."));

        entries.add(createEntry("Lair Actions", "Villainous Options UA", "Villains", "LAIR", 1,
                "Lair actions represent the supernatural powers available to a villain in its lair. " +
                "At initiative count 20 (losing initiative ties), the villain takes a lair action. " +
                "A villain can take only one lair action at a time and must wait for its next opportunity. " +
                "Lair actions change the lair's terrain, summon minions, or directly affect creatures. " +
                "Common lair actions include environmental hazards, teleportation, and minion spawning.",
                "Lair actions make a villain's territory a dangerous and dynamic battlefield."));

        entries.add(createEntry("Legendary Actions", "Villainous Options UA", "Villains", "LEGENDARY", 1,
                "A villain has a number of legendary actions it can use, as noted in its stat block. " +
                "Only one legendary action can be used at a time and must be used at the end of another creature's turn. " +
                "The villain regains its spent legendary actions at the start of its turn. " +
                "Legendary actions have costs (1-3), and the villain has 3 points per round to spend. " +
                "Common legendary actions include attacks, movement, spellcasting, and special abilities.",
                "Legendary actions keep villains threatening even when it's not their turn."));

        entries.add(createEntry("Regional Effects", "Villainous Options UA", "Villains", "REGIONAL", 1,
                "A powerful villain's presence affects its territory. These regional effects manifest " +
                "within a certain radius of the villain's lair. Effects can include weather changes, " +
                "undead rising, magical surges, or psychological effects on inhabitants. " +
                "Regional effects persist until the villain is defeated or leaves the area. " +
                "DMs can use regional effects to foreshadow a villain's approach or show the impact of defeating them.",
                "Regional effects make the villain's influence felt throughout the campaign world."));

        entries.add(createEntry("Lich Mechanics", "Villainous Options UA", "Villains", "LICH", 1,
                "Liches are undead spellcasters who have achieved immortality through dark rituals. " +
                "A lich's soul is stored in a phylactery, making it nearly impossible to permanently destroy. " +
                "When reduced to 0 HP, a lich retreats to its phylactery and regenerates over time. " +
                "To permanently destroy a lich, its phylactery must be found and destroyed. " +
                "Liches typically have legendary resistances, lair actions, and powerful spellcasting abilities. " +
                "The Lich progression feat allows characters to gain lich-like abilities.",
                "Liches represent the ultimate undead threat, requiring both combat and investigation to defeat."));

        entries.add(createEntry("Death Knight Mechanics", "Villainous Options UA", "Villains", "DEATH_KNIGHT", 1,
                "Death Knights are fallen warriors raised as undead champions of dark lords. " +
                "They retain their martial prowess and gain undead abilities like Death Throes, " +
                "Death's Chill, Death Save Points, and Reincarnate. " +
                "Death Knights typically serve a dark lord and command armies of undead. " +
                "The Death Knight progression feat allows characters to gain death knight abilities. " +
                "Death Knights have Action Surge and legendary resistances.",
                "Death Knights combine martial prowess with undead resilience for terrifying encounters."));

        entries.add(createEntry("Villain Design Principles", "Villainous Options UA", "Villains", "DESIGN", 1,
                "When designing a villain, consider their motive, resources, and lair. " +
                "Give villains clear goals that conflict with the players' objectives. " +
                "Provide villains with minions, allies, and resources beyond just combat stats. " +
                "Make villains adaptable: they should react to player actions and change tactics. " +
                "Use lair actions and regional effects to make encounters unique. " +
                "Allow any villain to be defeated at any time if players are clever enough.",
                "Good villain design creates memorable stories, not just difficult encounters."));

        // ==================== Bastions (DMG) ====================

        entries.add(createEntry("Bastions Overview", "DMG", "Bastions", "OVERVIEW", 1,
                "Bastions are party bases that can be created and customized for perks. " +
                "They provide safe havens, crafting facilities, training grounds, and healing sanctuaries. " +
                "A bastion's level (1-20) determines available perks, with major unlocks at levels 1, 5, 10, 15, and 20. " +
                "Themes include Fortress, Tower, Ship, Camp, Temple, Manor, Underground Hideout, Treehouse, " +
                "Floating Platform, and Cave System. Each theme offers different roleplay opportunities.",
                "Bastions give parties a home base that grows with them, adding narrative depth to campaigns."));

        entries.add(createEntry("Bastion Level Progression", "DMG", "Bastions", "LEVEL", 1,
                "Bastions level up from 1 to 20, unlocking perks at key thresholds. " +
                "Level 1: Basic shelter and safe resting. Level 5: Reinforced walls, guard posts, extra rooms. " +
                "Level 10: Magical wards, teleportation circle, expanded storage. " +
                "Level 15: Planar protection, construct sentries, healing sanctuary bonus. " +
                "Level 20: Legendary status, extraplanar allies, siege immunity. " +
                "Each level-up increases max HP by 20, and every 5 levels adds +1 AC and an extra room.",
                "Bastion progression mirrors character progression, providing a sense of growth."));

        entries.add(createEntry("Bastion Facilities", "DMG", "Bastions", "FACILITIES", 1,
                "Bastions can be equipped with specialized facilities for 500 gp each. " +
                "Crafting facility: Reduces crafting time by 25%. Training facility: Advantage on skill checks " +
                "related to training. Library: Advantage on Arcana, History, and Religion checks. " +
                "Healing sanctuary: +1d4 HP per long rest for all party members. " +
                "Facilities are permanent upgrades that enhance the party's capabilities between adventures.",
                "Facilities make bastions strategically valuable, not just narrative flavor."));

        entries.add(createEntry("Bastion Defense and Siege", "DMG", "Bastions", "DEFENSE", 1,
                "Bastions have HP and AC for siege combat. When a bastion reaches 0 HP, it is destroyed " +
                "and becomes inactive until repaired. Bastions can be repaired to restore HP. " +
                "Gold invested in a bastion can be used for upgrades and facilities. " +
                "The party size determines how many characters can benefit from the bastion's perks. " +
                "Room count determines the number of available spaces for activities.",
                "Siege mechanics add tension — a bastion can be a target for enemies."));

        entries.add(createEntry("Bastion Resource Management", "DMG", "Bastions", "RESOURCES", 1,
                "Gold can be invested in or withdrawn from a bastion. Investing gold enables upgrades, " +
                "facility purchases, and improvements. Withdrawing gold provides funds for adventures " +
                "but limits upgrade options. Managing bastion resources is a strategic decision: " +
                "more gold means better facilities but less spending power for the party.",
                "Resource management adds a strategic layer to bastion ownership."));

        entries.add(createEntry("Bastion Themes and Roleplay", "DMG", "Bastions", "THEMES", 1,
                "Each bastion theme offers unique roleplay opportunities. A Fortress provides military " +
                "flavor and defensive capabilities. A Tower suits arcane themes and scholarly pursuits. " +
                "A Ship enables nautical adventures and mobility. A Camp is portable and flexible. " +
                "A Temple provides divine connections and healing. An Underground Hideout supports " +
                "covert operations. The theme should reflect the party's identity and goals.",
                "Bastion themes enhance roleplay and give the party a unique identity."));

        // ==================== Adventure Hooks (Module Builder) ====================

        entries.add(createEntry("Adventure Hooks Overview", "DMG", "Adventure Hooks", "OVERVIEW", 1,
                "Adventure hooks are personalized story starters that tie into a character's background, race, " +
                "class, or motivations. They provide the DM with compelling reasons for a specific character " +
                "to become involved in an adventure. Hooks have types (Personal, Political, Mystery, Combat, " +
                "Exploration, Social, Supernatural), themes (Horror, High Magic, Sandbox, Dungeon Crawl, " +
                "Feywild, Steampunk), and urgency levels (Immediate, Soon, Eventual). Good hooks make the " +
                "adventure feel personal and urgent to each character.",
                "Personalized hooks increase player engagement by making the adventure relevant to their character."));

        entries.add(createEntry("Hook Types and Design", "DMG", "Adventure Hooks", "TYPES", 1,
                "Personal hooks tie directly to a character's backstory, relationships, or secrets. " +
                "Political hooks involve court intrigue, faction conflicts, and power struggles. " +
                "Mystery hooks present puzzles, crimes, or unexplained phenomena to investigate. " +
                "Combat hooks create immediate threats that require martial action. " +
                "Exploration hooks send characters into unknown territories or dungeons. " +
                "Social hooks involve negotiation, deception, and interpersonal drama. " +
                "Supernatural hooks introduce otherworldly threats, curses, or divine interventions.",
                "Mixing hook types creates a rich adventure that appeals to different character roles."));

        entries.add(createEntry("Hook Urgency and Pacing", "DMG", "Adventure Hooks", "URGENCY", 1,
                "Immediate hooks demand action right now — a village is burning, a killer is hunting, " +
                "a rift is growing. Soon hooks create pressure without panic — a curse is spreading, " +
                "a rival is gathering power, a deadline approaches. Eventual hooks provide long-term " +
                "motivation — a prophecy foretells doom, a treasure awaits discovery, a god's quest " +
                "calls. A good adventure mixes urgency levels: immediate hooks drive the opening, " +
                "soon hooks sustain momentum, and eventual hooks provide overarching goals.",
                "Urgency levels help the DM pace the adventure and create tension."));

        entries.add(createEntry("Character Profile Matching", "DMG", "Adventure Hooks", "MATCHING", 1,
                "Hooks can be targeted to specific character profiles for maximum impact. A hook targeted " +
                "to a Soldier background might involve a former comrade or a military secret. A hook for " +
                "an Elf might involve fey politics or ancient elven lore. A hook for a Wizard might involve " +
                "a magical artifact or academic mystery. Hooks also match by level: low-level hooks focus " +
                "on local threats, while high-level hooks involve world-ending stakes. The best hooks match " +
                "multiple aspects of a character's profile simultaneously.",
                "Profile-matched hooks make characters feel like the adventure was designed for them."));

        entries.add(createEntry("Hook Themes and Campaign Tone", "DMG", "Adventure Hooks", "THEMES", 1,
                "The theme of a hook sets the tone for the adventure. Horror hooks create dread and tension " +
                "through supernatural threats and body horror. High Magic hooks emphasize wonder, arcane mystery, " +
                "and powerful magical artifacts. Sandbox hooks provide open-ended exploration and player freedom. " +
                "Dungeon Crawl hooks focus on traps, treasure, and deadly challenges. Feywild hooks blend whimsy " +
                "with danger through fey trickery and otherworldly beauty. Steampunk hooks combine magic and " +
                "technology in a world of airships, automatons, and industrial intrigue.",
                "Consistent themes create a cohesive campaign identity."));

        entries.add(createEntry("Killer Hooks and Personal Stakes", "DMG", "Adventure Hooks", "STAKES", 1,
                "The most effective hooks create personal stakes that demand urgent action. Make hooks personal: " +
                "curiosity (a mystery only the character can solve), survival (a threat to the character or loved ones), " +
                "greed (a treasure the character desperately needs), heroism (innocents who need the character's help), " +
                "loyalty (a duty to a person or cause), honor (a code that demands action), or revenge (a wrong that " +
                "must be righted). The best hooks combine multiple motivations and make the character feel that " +
                "only they can act.",
                "Personal stakes transform a generic adventure into a character-defining quest."));

        // ==================== Dungeon Rooms (Module Builder) ====================

        entries.add(createEntry("Dungeon Room Overview", "DMG", "Dungeon Rooms", "OVERVIEW", 1,
                "Dungeon rooms are the building blocks of dungeon crawls and adventure locations. " +
                "Each room has a theme (Combat, Trap, Treasure, Puzzle, Social, Lore, Rest, Boss), " +
                "connections to other rooms, and optional traps, treasures, and encounters. " +
                "Circular connections ensure non-linear exploration, giving players meaningful choices.",
                "Thematic rooms with circular connections create engaging, non-linear dungeons."));

        entries.add(createEntry("Room Themes and Design", "DMG", "Dungeon Rooms", "THEMES", 1,
                "Entrance rooms set the tone and often contain the first encounter. Trap rooms test " +
                "player caution and skill. Treasure rooms reward exploration. Puzzle rooms challenge " +
                "the mind. Social rooms provide NPC interactions. Lore rooms reveal backstory. " +
                "Rest rooms offer safe havens. Boss rooms are climactic encounters. " +
                "Mixing themes creates variety and keeps players engaged.",
                "Each theme should have a distinctive atmosphere and mechanical focus."));

        entries.add(createEntry("Circular Connections", "DMG", "Dungeon Rooms", "CONNECTIONS", 1,
                "Circular connections ensure that rooms form a loop, so players can always find " +
                "a path forward. Additionally, cross-connections (shortcuts) every few rooms " +
                "create non-linear exploration. This avoids linear, corridor-style dungeons " +
                "and gives players meaningful navigation choices.",
                "Avoid linear dungeons; provide multiple paths and shortcuts."));

        entries.add(createEntry("Trap Mechanics", "DMG", "Dungeon Rooms", "TRAPS", 1,
                "Traps use DEX saving throws vs the trap DC. A natural 1 (critical failure) " +
                "deals double damage. A natural 20 (critical success) avoids all damage regardless " +
                "of the DC. Investigation checks detect traps; Thieves' Tools disable them. " +
                "Trap damage types include poison, bludgeoning, piercing, slashing, fire, cold, " +
                "and acid.",
                "Traps should be detectable and avoidable with skill."));

        entries.add(createEntry("Treasure Discovery", "DMG", "Dungeon Rooms", "TREASURE", 1,
                "Treasure rooms contain gold pieces, magic items, and artifacts. Magic item " +
                "rarity scales with dungeon difficulty: Common for low-level dungeons, " +
                "Uncommon for medium, Rare for high, Very Rare and Legendary for epic dungeons. " +
                "Treasure should reward risk and exploration.",
                "Scale treasure to dungeon difficulty and player level."));

        entries.add(createEntry("Encounter Difficulty", "DMG", "Dungeon Rooms", "ENCOUNTERS", 1,
                "Room encounters range from Trivial to Deadly difficulty. XP rewards scale " +
                "with encounter difficulty and character level. Boss rooms contain the hardest " +
                "encounters with legendary actions and lair effects. Social encounters use " +
                "skill checks instead of combat. Mix encounter types for variety.",
                "Balance encounters using XP thresholds and tension pools."));

        entries.add(createEntry("DM Secrets and Hidden Details", "DMG", "Dungeon Rooms", "SECRETS", 1,
                "DM secrets are hidden details only the DM knows: secret passages, hidden motives, " +
                "undiscovered treasures, and plot twists. These add depth and surprise to dungeons. " +
                "Use secrets to reward thorough exploration and creative problem-solving.",
                "Secrets make dungeons feel alive and reward curiosity."));

        // ==================== Tension Pools (Module Builder) ====================

        entries.add(createEntry("Tension Pool Overview", "DMG", "Tension Pools", "OVERVIEW", 1,
                "Tension pools are a mechanical system for tracking the consequences of player actions. " +
                "As tension increases, wandering encounters, alarms, and other complications become more likely. " +
                "This creates a dynamic, reactive game world where player choices matter. " +
                "Each area or situation has its own tension pool with thresholds and consequences.",
                "Tension pools make the world feel alive and responsive to player behavior."));

        entries.add(createEntry("Tension Thresholds", "DMG", "Tension Pools", "THRESHOLDS", 1,
                "Each tension pool has thresholds — specific tension levels that trigger consequences. " +
                "For example, a Guard Alertness pool might have thresholds at 3, 5, 7, and 9. " +
                "When tension crosses a threshold, the corresponding consequence fires: guards become watchful, " +
                "a captain is alerted, reinforcements arrive, or a full alarm sounds. " +
                "Once a threshold is triggered, it does not fire again until the pool is reset.",
                "Design thresholds to escalate consequences gradually, giving players warning before the ultimate outcome."));

        entries.add(createEntry("Increasing and Decreasing Tension", "DMG", "Tension Pools", "ACTIONS", 1,
                "Tension increases when players make noise, fail stealth checks, trigger alarms, " +
                "kill creatures, or otherwise draw attention. Typical increases are 1-2 points per action. " +
                "Tension decreases when players are stealthy, rest quietly, use magic to calm guards, " +
                "or leave the area. A long rest or leaving an area typically resets tension to 0.",
                "Balance tension increases and decreases so the pool feels meaningful but not overwhelming."));

        entries.add(createEntry("Wandering Encounters", "DMG", "Tension Pools", "ENCOUNTERS", 1,
                "Wandering encounters are triggered by rolling a d20 against a DC that decreases as tension increases. " +
                "At tension 0, the DC is 20 (very unlikely). At maximum tension, the DC drops to 5 (very likely). " +
                "Formula: DC = max(5, 20 - currentTension). Roll >= DC means an encounter occurs. " +
                "This mechanic ties player behavior directly to the risk of random encounters.",
                "Use wandering encounters to reward stealth and punish recklessness."));

        entries.add(createEntry("Ultimate Consequences", "DMG", "Tension Pools", "ULTIMATE", 1,
                "When a tension pool reaches its maximum tension, the ultimate consequence fires. " +
                "This is the worst-case scenario: full alarm, all enemies attack, the area turns hostile. " +
                "Ultimate consequences should be dramatic and force the party to either fight, flee, " +
                "or find a creative solution. They represent the cost of pushing tension too far.",
                "Make ultimate consequences memorable and consequential — they should change the situation dramatically."));

        entries.add(createEntry("Tension Pool Design Tips", "DMG", "Tension Pools", "DESIGN", 1,
                "Create tension pools for areas where player actions have cumulative consequences: " +
                "infiltrating a castle, exploring a dangerous forest, navigating fey politics. " +
                "Use 4-6 thresholds with escalating consequences. Set max tension between 6-10. " +
                "Reset tension at rest stops or when leaving an area. " +
                "Multiple tension pools can operate simultaneously for complex scenarios.",
                "Tension pools work best when players can see the tension level and make informed choices about risk."));

        // ==================== NPC Roster (Module Builder) ====================

        entries.add(createEntry("NPC Roster Overview", "DMG", "NPC Roster", "OVERVIEW", 1,
                "The NPC roster tracks all key non-player characters in the campaign. " +
                "Each NPC has a name, type, race, class, level, and challenge rating. " +
                "NPCs have motives that drive their behavior, resources they control, " +
                "and relationships with other NPCs. The roster helps the DM manage a living world.",
                "Build a roster of key NPCs before the campaign starts, with clear motives and connections."));

        entries.add(createEntry("NPC Disposition System", "DMG", "NPC Roster", "DISPOSITION", 1,
                "Each NPC has a disposition score from -10 to +10 that tracks their attitude toward the player characters. " +
                "Scores map to disposition categories: Hostile (-10 to -6), Unfriendly (-5 to -2), " +
                "Indifferent (-1 to +1), Friendly (+2 to +5), Helpful (+6 to +10). " +
                "Player actions can change disposition: helping an NPC improves it, harming them worsens it. " +
                "Disposition affects how NPCs interact with the party: hostile NPCs attack, helpful NPCs aid freely.",
                "Use disposition to make social encounters mechanical and trackable."));

        entries.add(createEntry("NPC Relationships", "DMG", "NPC Roster", "RELATIONSHIPS", 1,
                "NPCs have relationships with each other: allies, enemies, and family. " +
                "Relationships are bidirectional — if A is B's ally, B is also A's ally. " +
                "These relationships drive NPC behavior: allies help each other, enemies oppose each other. " +
                "Family ties create personal stakes and emotional hooks for both NPCs and players.",
                "Web of relationships makes the world feel interconnected and alive."));

        entries.add(createEntry("NPC Resources", "DMG", "NPC Roster", "RESOURCES", 1,
                "Each NPC tracks their resources: gold pieces, items, hit points, and lair location. " +
                "Gold can be added or removed through trade, theft, or bribes. " +
                "Hit points track combat damage to NPCs. " +
                "Lair locations indicate where NPCs can be found and provide encounter sites.",
                "Resource tracking makes NPCs feel like real entities with assets and vulnerabilities."));

        entries.add(createEntry("NPC Motives and Secrets", "DMG", "NPC Roster", "MOTIVES", 1,
                "Every NPC should have a primary motive — what they want most. " +
                "Secondary motives and secrets add depth: hidden agendas, past traumas, secret alliances. " +
                "Motives drive NPC behavior and create plot hooks. When players understand an NPC's motive, " +
                "they can predict and influence that NPC's actions. " +
                "Villains and allies flags help quickly identify key antagonists and mentors.",
                "Motives are the engine of NPC-driven plots — give every important NPC a clear goal."));

        entries.add(createEntry("NPC Design Principles", "DMG", "NPC Roster", "DESIGN", 1,
                "Build a diverse roster: mix races, classes, types, and roles. " +
                "Include villains with clear threats, allies who provide aid, and neutral NPCs who can sway either way. " +
                "Make NPCs memorable with unique names, appearances, and quirks. " +
                "Link NPCs through relationships so the world feels interconnected. " +
                "Use disposition to track how player actions affect the world's attitude toward them.",
                "A well-designed NPC roster makes the campaign world feel alive and reactive."));

        // ==================== Encounter Balancer ====================

        entries.add(createEntry("Encounter Balancing Overview", "DMG", "Encounter Balancer", "OVERVIEW", 1,
                "Encounter balancing uses XP thresholds to determine the difficulty of combat encounters. " +
                "Each monster has an XP value based on its Challenge Rating. The total XP of all monsters " +
                "in an encounter is compared against the party's XP thresholds for their level and size. " +
                "Difficulty categories: Trivial, Easy, Medium, Hard, Deadly.",
                "Use XP thresholds as a guide, not a rigid rule — adjust for the party's actual capabilities."));

        entries.add(createEntry("XP Thresholds by Party Size", "DMG", "Encounter Balancer", "THRESHOLDS", 1,
                "XP thresholds scale with party size. For a party of 4, multiply the per-character XP by 4. " +
                "For parties smaller than 4, multiply by party size / 4. " +
                "For parties larger than 4, multiply by (party size - 3) to account for diminishing returns. " +
                "The thresholds table provides values for all party sizes (1-8) and levels (1-20).",
                "A solo character faces much lower thresholds — a CR 1 monster is Deadly for a level 1 solo character."));

        entries.add(createEntry("Multiple Encounter Adjustment", "DMG", "Encounter Balancer", "MULTIPLIER", 1,
                "When a party faces multiple encounters in quick succession, their effective XP budget decreases. " +
                "Apply a multiplier to the XP thresholds: 0 recent encounters = 1.0x, " +
                "1-2 recent encounters = 1.5x, 3+ recent encounters = 2.0x. " +
                "This means the same encounter becomes harder when the party is already fatigued.",
                "Track recent encounters to maintain appropriate tension throughout an adventure."));

        entries.add(createEntry("Challenge Rating to XP", "DMG", "Encounter Balancer", "CR_XP", 1,
                "Each monster's Challenge Rating maps to an XP value. " +
                "CR 1/8 = 25 XP, CR 1/4 = 50 XP, CR 1/2 = 100 XP, CR 1 = 200 XP, " +
                "CR 5 = 1,300 XP, CR 10 = 2,800 XP, CR 20 = 15,000 XP, CR 30 = 50,000 XP. " +
                "Sum the XP values of all monsters in an encounter for the total monster XP.",
                "The CR table is the foundation of encounter balancing — know it well."));

        entries.add(createEntry("Tension-Based Encounters", "DMG", "Encounter Balancer", "TENSION", 1,
                "Tension pools can modify encounter difficulty. Each tension level adds 10% of the base " +
                "monster XP as a bonus, making encounters harder as tension rises. " +
                "This creates a dynamic system where player actions that increase tension " +
                "also make subsequent encounters more dangerous.",
                "Combine tension pools with encounter balancing for reactive, player-driven difficulty."));

        entries.add(createEntry("Encounter Design Tips", "DMG", "Encounter Balancer", "DESIGN", 1,
                "Vary difficulty: mix Easy, Medium, Hard, and Deadly encounters in an adventure. " +
                "Use Medium as your baseline, Easy for breather encounters, and Deadly for climaxes. " +
                "Consider the party's resources: spell slots, hit points, and abilities when designing. " +
                "Environment matters: terrain, cover, and lighting can shift effective difficulty. " +
                "Social and exploration challenges should also be balanced against combat encounters.",
                "Good encounter design creates a rhythm of tension and release throughout the adventure."));

        // ==================== Module Builder ====================

        entries.add(createEntry("Module Builder Overview", "DMG", "Module Builder", "OVERVIEW", 1,
                "The Module Builder ties together all D&D 5e subsystems into a complete, self-contained adventure. " +
                "A module references adventure hooks, dungeon rooms, NPCs, encounters, villains, bastions, " +
                "tension pools, growing magic items, dark gifts, feats, crafting recipes, and psionic powers. " +
                "Each module has a theme (Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, Steampunk), " +
                "a level range, and an estimated number of sessions.",
                "The Module Builder is the master tool for creating complete adventures from building blocks."));

        entries.add(createEntry("Module Themes", "DMG", "Module Builder", "THEMES", 1,
                "Each module theme suggests different component types: " +
                "Horror modules use Dark Gifts, tension pools, and villains. " +
                "High Magic modules use growing items, psionic powers, and crafting recipes. " +
                "Sandbox modules use tension pools, bastions, and NPCs. " +
                "Dungeon Crawl modules use rooms, encounters, and growing items. " +
                "Feywild modules use NPCs, rooms, and Dark Gifts. " +
                "Steampunk modules use crafting recipes, bastions, and feats.",
                "Choose a theme that matches the tone and mechanics you want for your adventure."));

        entries.add(createEntry("Module Components", "DMG", "Module Builder", "COMPONENTS", 1,
                "A complete module includes components from multiple subsystems: " +
                "Adventure Hooks provide player motivation. Dungeon Rooms create exploration spaces. " +
                "NPCs populate the world. Encounters provide challenges. Villains drive the plot. " +
                "Bastions serve as party bases. Tension Pools create reactive worlds. " +
                "Growing Magic Items reward progression. Dark Gifts add horror elements. " +
                "Feats and Crafting Recipes provide character options. Psionic Powers add psionic mechanics.",
                "Aim for at least one of each component type for a well-rounded module."));

        entries.add(createEntry("Module Progression", "DMG", "Module Builder", "PROGRESSION", 1,
                "Modules track status through four stages: Draft, In Progress, Complete, and Archived. " +
                "Draft modules are being planned. In Progress modules are actively being played. " +
                "Complete modules have finished their story. Archived modules are stored for reference. " +
                "The generate-summary endpoint creates a component count and completion percentage " +
                "to help DMs track their module's readiness.",
                "Use the validation endpoint to check for missing components before starting play."));

        entries.add(createEntry("Module Statistics", "DMG", "Module Builder", "STATISTICS", 1,
                "Each module tracks play count and rating. Increment the play count when starting a module. " +
                "Set a rating (1-5 stars) after completing a module to help DMs find quality content. " +
                "The most-played and top-rated endpoints help discover popular modules. " +
                "Module statistics include component counts for each subsystem, " +
                "providing a quick overview of the module's scope and complexity.",
                "Statistics help DMs choose modules appropriate for their campaign's needs."));

        entries.add(createEntry("Theme Suggestions", "DMG", "Module Builder", "SUGGESTIONS", 1,
                "The theme suggestions endpoint recommends component types for each theme. " +
                "For Horror: Undead/Fiend/Aberration Dark Gifts, Castle Ward tension pools, Lich villains. " +
                "For High Magic: Staff of the Arcane, Telepath powers, Magic Initiate feats. " +
                "For Sandbox: Open world tension pools, Party base bastions, Faction leader NPCs. " +
                "For Dungeon Crawl: Trap rooms, Combat encounters, Guardian NPCs. " +
                "For Feywild: Archfey NPCs, Fey grove rooms, Fey Dark Gifts. " +
                "For Steampunk: Magitech crafting, Workshop bastions, Artificer feats.",
                "Use theme suggestions as a starting point, then customize for your campaign."));

        // ==================== Species & Name Database ====================

        entries.add(createEntry("Species Overview", "PHB", "Species", "OVERVIEW", 1,
                "The Species database defines all D&D 5e races with their racial traits, ability score increases, " +
                "size, speed, lifespan, darkvision, and name databases. Each species follows the Earth 2026 pattern " +
                "with male/female first names, last names, title prefixes/suffixes, and AI leader/governor/captain titles. " +
                "Core races include Human, Elf, Dwarf, Halfling, Dragonborn, Gnome, Half-Elf, Half-Orc, Tiefling, and Aasimar. " +
                "TCE races include Tabaxi, Tortle, Triton, Kenku, Bugbear, Goliath, Hobgoblin, Lizardfolk, Satyr, and Firbolg.",
                "The Species database is the foundation for character creation and NPC name generation."));

        entries.add(createEntry("Ability Score Increases", "PHB", "Species", "ABILITY_SCORES", 1,
                "Each species grants fixed ability score increases. Human: +1 to all six abilities. " +
                "Elf: +2 DEX. Dwarf: +2 CON. Halfling: +2 DEX. Dragonborn: +2 STR, +1 CHA. " +
                "Gnome: +2 INT. Half-Elf: +2 CHA. Half-Orc: +2 STR, +1 CON. Tiefling: +2 CHA, +1 INT. " +
                "Aasimar: +2 CHA, +1 WIS. Genasi: +2 CON, +1 CHA. " +
                "TCE races vary: Tabaxi (+2 DEX, +1 CHA), Goliath (+2 STR, +1 CON), Hobgoblin (+2 CON, +1 STR), etc.",
                "Ability score increases are applied automatically when creating a character of a given species."));

        entries.add(createEntry("Racial Traits", "PHB", "Species", "RACIAL_TRAITS", 1,
                "Each species has unique racial traits that define its mechanical abilities. " +
                "Common traits include Darkvision (60 ft range), Fey Ancestry (advantage vs charm), " +
                "and Trance (elves rest in 4 hours). " +
                "Human: Customizable Origin, Extra Language. Elf: Keen Senses, Elf Weapon Training. " +
                "Dwarf: Dwarven Resilience, Stonecunning, Dwarven Combat Training, Tool Proficiency. " +
                "Halfling: Lucky, Brave, Halfling Nimbleness. Dragonborn: Draconic Ancestry, Breath Weapon, Damage Resistance.",
                "Racial traits are stored as JSON arrays and can be queried by trait name."));

        entries.add(createEntry("Name Generation", "PHB", "Species", "NAME_GENERATION", 1,
                "Each species has a complete name database for random character and NPC name generation. " +
                "The generate-name endpoint returns a random first name, last name, and full name for a given species and gender. " +
                "The generate-title endpoint returns a random title for a given species and role (leader, governor, captain, prefix, suffix). " +
                "Names are culturally appropriate for each species: Elvish names for elves, Dwarven names for dwarves, etc.",
                "Name generation is essential for creating NPCs and populating adventure modules with diverse characters."));

        entries.add(createEntry("Species Sizes", "PHB", "Species", "SIZES", 1,
                "Species come in different sizes that affect equipment, space, and combat. " +
                "Tiny: 1.5 ft tall (no core species). Small: 3 ft tall (Halfling, Gnome). " +
                "Medium: 5-6 ft tall (Human, Elf, Dwarf, Dragonborn, Half-Elf, Half-Orc, Tiefling, Aasimar, Genasi, most TCE). " +
                "Large: 8-10 ft tall (Goliath, Firbolg). " +
                "Size affects the space a creature controls in combat and the size of equipment it can use.",
                "Size is a fundamental property that affects many game mechanics."));

        entries.add(createEntry("Species Lifespans", "PHB", "Species", "LIFESPANS", 1,
                "Species lifespans vary dramatically, affecting roleplay and campaign design. " +
                "Short-lived: Bugbear (70), Halfling (150), Tabaxi (70), Kenku (70), Lizardfolk (70), Satyr (70). " +
                "Medium-lived: Human (100), Dragonborn (80), Half-Orc (75), Tiefling (100), Hobgoblin (90). " +
                "Long-lived: Aasimar (160), Half-Elf (180), Tortle (100), Triton (150). " +
                "Very long-lived: Dwarf (350), Gnome (425), Firbolg (500), Elf (750). " +
                "Lifespan affects how characters view time, relationships, and adventure goals.",
                "Use lifespan to create interesting roleplay opportunities and cultural differences between species."));

        // ==================== Campaign Theme Templates ====================

        entries.add(createEntry("Campaign Templates Overview", "DMG", "Campaign Templates", "OVERVIEW", 1,
                "Campaign templates provide pre-built frameworks for DMs to quickly start themed campaigns. " +
                "Each template includes recommended subsystems, setup instructions, pacing guides, " +
                "example hooks, villains, NPCs, and encounters, plus difficulty scaling advice and DM tips. " +
                "Templates cover 6 major themes: Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, and Steampunk.",
                "Templates are the fastest way to start a campaign — just follow the setup instructions and customize as needed."));

        entries.add(createEntry("Campaign Template Themes", "DMG", "Campaign Templates", "THEMES", 1,
                "Horror: Ravenloft-style dread with fear checks, Dark Gifts, and tension pools. " +
                "High Magic: Arcane academy with growing magic items, psionics, and spellcasting focus. " +
                "Sandbox: Open-world exploration with bastions, NPC relationships, and political intrigue. " +
                "Dungeon Crawl: Multi-level dungeons with traps, treasure, and circular connections. " +
                "Feywild: Whimsical realm with bargains, trickery, and non-combat resolution. " +
                "Steampunk: Magic-technology fusion with crafting, tool proficiencies, and invention.",
                "Choose a theme that matches your group's preferences; each theme emphasizes different subsystems."));

        entries.add(createEntry("Campaign Template Structure", "DMG", "Campaign Templates", "STRUCTURE", 1,
                "Each template includes: recommended subsystems (which modules to use), " +
                "setup instructions (step-by-step campaign creation), pacing guide (session-by-session structure), " +
                "example hook (adventure starter), example villain (main antagonist), " +
                "example NPCs (key characters), example encounters (sample challenges), " +
                "difficulty scaling (adjusting for party size/level), and DM tips (best practices).",
                "Use the template as a starting point — customize every element to fit your campaign."));

        entries.add(createEntry("Campaign Template Pacing", "DMG", "Campaign Templates", "PACING", 1,
                "Templates provide session-by-session pacing guides that structure the campaign arc. " +
                "Typical structure: Introduction (sessions 1-3) establishes the world and hooks. " +
                "Rising Action (sessions 4-6) deepens the conflict and introduces complications. " +
                "Midpoint (sessions 7-9) reveals the true threat and raises stakes. " +
                "Climax (final sessions) resolves the main conflict. " +
                "Adjust pacing based on player choices and emergent story.",
                "Pacing guides are suggestions — let player agency drive the actual timeline."));

        entries.add(createEntry("Campaign Template Difficulty Scaling", "DMG", "Campaign Templates", "SCALING", 1,
                "Each template includes difficulty scaling advice for different party sizes and levels. " +
                "Smaller parties (2-3): Reduce monster HP, lower DCs, provide more resources. " +
                "Larger parties (6-8): Increase monster numbers, add more complex challenges. " +
                "Higher levels: Introduce legendary monsters, planar threats, and epic stakes. " +
                "Use the encounter balancer to fine-tune difficulty for your specific party.",
                "Always adjust encounters based on your party's actual composition and skill."));

        entries.add(createEntry("Campaign Template Customization", "DMG", "Campaign Templates", "CUSTOMIZATION", 1,
                "Templates are designed to be customized. Swap example NPCs for your own creations. " +
                "Replace example encounters with ones that fit your campaign's tone. " +
                "Mix and match subsystems from different templates for hybrid campaigns. " +
                "Use the module builder to combine template components into cohesive adventures. " +
                "The species database provides culturally appropriate names for NPCs in any theme.",
                "The best campaigns blend template structure with DM creativity."));

        // ==================== Magic Items ====================

        entries.add(createEntry("Magic Items Overview", "DMG", "Magic Items", "OVERVIEW", 1,
                "Magic items are objects that have magical properties. They come in various rarities " +
                "(Common, Uncommon, Rare, Very Rare, Legendary, Artifact) and categories " +
                "(Weapon, Armor, Wondrous Item, Ring, Rod, Staff, Potion, Scroll, etc.). " +
                "Some items require attunement; a character can attune to at most 3 items. " +
                "Items with charges track remaining charges and may recharge after short rest, long rest, or dawn.",
                "Magic items are a core part of D&D 5e progression — use them to reward players and enable creative solutions."));

        entries.add(createEntry("Magic Item Rarities", "DMG", "Magic Items", "RARITY", 1,
                "Common: Basic magical effect, no attunement required. " +
                "Uncommon: Moderate effect, may require attunement. " +
                "Rare: Strong effect, usually requires attunement. " +
                "Very Rare: Powerful effect, always requires attunement. " +
                "Legendary: Game-changing effect, unique properties. " +
                "Artifact: World-altering power, often tied to campaign plot.",
                "Rarity determines how frequently an item should appear and how powerful its effects are."));

        entries.add(createEntry("Magic Item Attunement", "DMG", "Magic Items", "ATTUNEMENT", 1,
                "A character can attune to at most 3 magic items that require attunement. " +
                "Attuning takes a short rest (1 hour) and requires the character to be in physical contact with the item. " +
                "De-attuning also takes a short rest. Some items require attunement by a specific class. " +
                "If a character dies, they must attune again to their items after reviving.",
                "Attunement limits prevent characters from stacking too many powerful items."));

        entries.add(createEntry("Magic Item Charges", "DMG", "Magic Items", "CHARGES", 1,
                "Some items have a number of charges. Using a property may expend 1 or more charges. " +
                "When an item runs out of charges, it can't be used until it recharges. " +
                "Recharge rules: Dawn (roll d20 vs DC), Short Rest (full recharge), Long Rest (full recharge), None (no recharge). " +
                "Items that recharge at dawn typically require a d20 roll meeting or exceeding the DC.",
                "Charges add resource management to magic items — players must decide when to use them."));

        entries.add(createEntry("Cursed Magic Items", "DMG", "Magic Items", "CURSE", 1,
                "Cursed items have detrimental effects that may not be immediately apparent. " +
                "A cursed item's effects can't be removed until the curse is lifted, typically by a Remove Curse spell. " +
                "While cursed, the character can't de-attune from the item. " +
                "DMs should decide whether characters know an item is cursed; revealing the curse allows them to seek help.",
                "Cursed items add drama and roleplay opportunities — use them sparingly for maximum impact."));

        entries.add(createEntry("Magic Item Categories", "DMG", "Magic Items", "CATEGORIES", 1,
                "Weapon: Magical weapons that enhance attack and damage. " +
                "Armor: Magical armor that improves AC or grants special abilities. " +
                "Wondrous Item: Miscellaneous magical objects with varied effects. " +
                "Ring: Wearable items that grant passive or active abilities. " +
                "Rod: Held items that channel magical power. " +
                "Staff: Held items that grant spell-like abilities. " +
                "Potion: Consumable liquid that grants a one-time effect. " +
                "Scroll: Contains a spell that can be cast by reading the scroll. " +
                "Wand: Held items that cast spells when activated.",
                "Categories help organize items and determine how they interact with character equipment."));

        // ==================== Backgrounds (2024 PHB) ====================

        entries.add(createEntry("Backgrounds Overview", "PHB", "Backgrounds", "BACKGROUND", 120,
                "A background represents where you came from and how you became an adventurer. " +
                "Each background grants skill proficiencies, tool or language proficiencies, a feature, " +
                "and starting equipment. The 2024 PHB revised backgrounds to include feature slots, " +
                "allowing more customization within each background's theme.",
                "Choose a background that complements your character's class and roleplaying concept."));

        entries.add(createEntry("Background Skill Proficiencies", "PHB", "Backgrounds", "SKILLS", 120,
                "Each background grants proficiency in two skills. These proficiencies represent " +
                "the knowledge and training you gained during your time in your background's environment. " +
                "For example, an Acolyte gains Insight and Religion, while a Criminal gains Deception and Stealth.",
                "Background skills are in addition to your class's skill proficiencies."));

        entries.add(createEntry("Background Tool Proficiencies", "PHB", "Backgrounds", "TOOLS", 120,
                "Some backgrounds grant proficiency with tools or languages. Tool proficiencies " +
                "include artisan's tools, gaming sets, musical instruments, and thieves' tools. " +
                "Language proficiencies grant the ability to speak, read, and write an extra language.",
                "Tool proficiencies are useful for crafting, performance, and stealth checks."));

        entries.add(createEntry("Background Feature Slots", "PHB", "Backgrounds", "FEATURE_SLOTS", 120,
                "2024 PHB backgrounds include feature slots that allow customization. Each background " +
                "has one or more feature slots, and each slot offers several options. " +
                "For example, the Acolyte background has a Sheltered option (temple network access) " +
                "and a Missionary option (finding work in temples). Choose one option per slot.",
                "Feature slots make backgrounds more flexible and allow characters to tailor their origins."));

        entries.add(createEntry("Background Categories", "PHB", "Backgrounds", "CATEGORIES", 120,
                "Backgrounds are categorized by rarity: Common backgrounds are available to most characters " +
                "(Acolyte, Charlatan, Criminal, Entertainer, Folk Hero, Guild Artisan, Hermit, Noble, " +
                "Outlander, Sage). Rare backgrounds require DM approval (Soldier, Urchin). " +
                "Exotic backgrounds are for special campaigns (Agent of the Guild, Ship's Captain, Spy, " +
                "Urban Bounty Hunter, Face, Haunted One).",
                "Use categories to control the tone and scope of your campaign's character options."));

        entries.add(createEntry("Background Equipment", "PHB", "Backgrounds", "EQUIPMENT", 120,
                "Each background provides starting equipment that reflects your origins. " +
                "This equipment is in addition to your class's starting equipment. " +
                "For example, a Sage starts with a bottle of ink, quill, and a letter from a dead colleague, " +
                "while a Noble starts with fine clothes, a signet ring, and a scroll of pedigree.",
                "Background equipment helps establish your character's identity and provides useful items."));

        // ==================== Classes (2024 PHB) ====================

        entries.add(createEntry("Classes Overview", "PHB", "Classes", "OVERVIEW", 130,
                "A class defines a character's abilities and role in the game. The PHB includes 13 classes: " +
                "Barbarian, Bard, Cleric, Druid, Fighter, Monk, Paladin, Ranger, Rogue, Sorcerer, " +
                "Warlock, Wizard, plus Artificer (TCE) and Blood Hunter (GGR). Each class has a hit die, " +
                "primary ability, proficiencies, and a progression of features from level 1 to 20.",
                "Choose a class that matches your character concept and playstyle."));

        entries.add(createEntry("Class Hit Dice", "PHB", "Classes", "HIT_DICE", 130,
                "Hit dice determine how many hit points you gain when you level up and when you heal " +
                "with a short rest. d12 (Barbarian), d10 (Fighter, Paladin, Ranger, Blood Hunter), " +
                "d8 (Bard, Cleric, Druid, Monk, Rogue, Warlock, Artificer), d6 (Sorcerer, Wizard). " +
                "When you gain a level, you can add your hit die + CON modifier to your HP, or take 2x hit die.",
                "Larger hit dice mean more hit points per level."));

        entries.add(createEntry("Class Spellcasting Types", "PHB", "Classes", "SPELLCASTING", 130,
                "Full casters (Bard, Cleric, Druid, Sorcerer, Wizard) gain all spell levels up to 9th. " +
                "Half casters (Paladin, Ranger) gain spell levels up to 5th, gaining levels every 2 class levels. " +
                "Pact casters (Warlock) have pact magic with invocations and mystic arcanum. " +
                "Eldritch Burst (Artificer) gains spell slots at specific levels. " +
                "Non-casters (Barbarian, Fighter, Monk, Rogue) have no spellcasting.",
                "Spellcasting type determines your spell slot progression and maximum spell level."));

        entries.add(createEntry("Class Subclasses", "PHB", "Classes", "SUBCLASSES", 130,
                "At level 2 (or later for some classes), you choose a subclass that defines your specialization. " +
                "For example, a Barbarian chooses a Path (Berserker, Totem Warrior, Ancestral Guardian, Beast), " +
                "a Wizard chooses a School (Evocation, Abjuration, Conjuration, etc.), " +
                "a Cleric chooses a Domain (Life, Light, Tempest, etc.). " +
                "Subclasses grant additional features at levels 3, 6, 10, 14, and sometimes 18.",
                "Subclasses dramatically change how a class plays."));

        entries.add(createEntry("Class Features", "PHB", "Classes", "FEATURES", 130,
                "Each class gains features at specific levels. Base class features are available to all members " +
                "of the class, while subclass features are unique to your chosen specialization. " +
                "Key features include Extra Attack (Fighter, Barbarian, Paladin, Ranger, Blood Hunter), " +
                "Action Surge (Fighter), Sneak Attack (Rogue), Rage (Barbarian), Wild Shape (Druid), " +
                "Divine Smite (Paladin), and Bardic Inspiration (Bard).",
                "Features define what makes each class unique."));

        entries.add(createEntry("Multiclassing", "PHB", "Classes", "MULTICLASSING", 130,
                "Characters can take levels in multiple classes. To multiclass, you need minimum ability scores: " +
                "STR 13 (Barbarian, Paladin), DEX 13 (Monk, Rogue), INT 13 (Artificer, Wizard), " +
                "WIS 13 (Cleric, Druid), CHA 13 (Bard, Sorcerer, Warlock). " +
                "When multiclassing, spellcaster levels are calculated: full caster levels count fully, " +
                "half caster levels count as half, and Artificer levels count as one-third. " +
                "Warlock pact magic is separate from multiclass spellcasting.",
                "Multiclassing allows powerful combinations but requires careful planning."));

        // ============================================================
        // MONSTERS
        // ============================================================
        entries.add(createEntry("Monsters Overview", "MM", "Monsters", "Monsters", null,
                "Monsters are creatures that DMs use for encounters, from common vermin to ancient dragons. Each monster has a Challenge Rating (CR) that determines its difficulty and XP value.",
                "Monsters span all types: Aberration, Beast, Celestial, Construct, Dragon, Elemental, Fey, Giant, Humanoid, Monstrosity, Plant, Undead."));

        entries.add(createEntry("Challenge Rating", "MM", "Monsters", "Monsters", null,
                "Challenge Rating (CR) measures a monster's threat level. CR 0 is trivial, CR 1-4 is low level, CR 5-10 is mid level, CR 11-16 is high level, CR 17+ is very high level. XP awarded for defeating a monster is based on its CR.",
                "Use the DMG XP threshold table to balance encounters based on party size and monster CR."));

        entries.add(createEntry("Monster Types", "MM", "Monsters", "Monsters", null,
                "Aberration: Alien creatures from beyond. Beast: Animals and monsters. Celestial: Divine beings. Construct: Artificial creations. Dragon: Winged reptiles. Elemental: Beings of pure element. Fey: Magical beings of the Feywild. Giant: Huge humanoids. Humanoid: People and near-people. Monstrosity: Fearsome creatures. Plant: Sentient flora. Undead: Reanimated dead.",
                "Monster type determines vulnerabilities, resistances, and roleplaying guidance."));

        entries.add(createEntry("Monster Sizes", "MM", "Monsters", "Monsters", null,
                "Tiny (1-2.5ft), Small (2.5-5ft), Medium (5-10ft), Large (10-15ft), Huge (15-30ft), Gargantuan (30ft+). Size affects space occupied, reach, and certain spell effects.",
                "A creature's size category determines the space it controls in combat."));

        entries.add(createEntry("Legendary Actions", "MM", "Monsters", "Monsters", null,
                "Legendary actions allow powerful monsters to act between turns. A monster has a pool of legendary action points (usually 3) that refresh at the start of its turn. Each legendary action costs 1 point.",
                "Only monsters with CR 10+ typically have legendary actions."));

        entries.add(createEntry("Lair Actions", "MM", "Monsters", "Monsters", null,
                "Lair actions allow powerful monsters to affect their environment. On initiative count 20 (losing initiative ties), the monster takes a lair action. A monster can't use lair actions while it has fewer than 50 hit points.",
                "Lair actions create dynamic and dangerous environments for high-level encounters."));

        entries.add(createEntry("Regional Effects", "MM", "Monsters", "Monsters", null,
                "Regional effects are the influence a powerful monster has on its territory. Only monsters with CR 15+ have regional effects. These effects can include weather changes, magical phenomena, and influenced creatures.",
                "Regional effects make the world feel alive and connected to powerful monsters."));

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

        // --- Monster XP by CR ---
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                0, "CR 0", "50 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                1, "CR 1", "200 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                5, "CR 5", "1800 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                10, "CR 10", "5900 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                15, "CR 15", "16000 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                20, "CR 20", "41000 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                25, "CR 25", "105000 XP"));
        tables.add(createTable("Monster XP", "MM", "TABLE_MONSTER_XP", "Monsters", 0,
                30, "CR 30", "230000 XP"));

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

package com.yisy.modules.doramcu.captainslog.service;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Implements the Probability Matrixes from the Captain's Log Solo RPG rulebook.
 * <p>
 * The rulebook contains over 100 random tables organized into categories:
 * <ul>
 *   <li>Yes/No Probability Matrix ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â core oracle with probability levels</li>
 *   <li>Mission Type tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â what kind of mission is this?</li>
 *   <li>Theme tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the underlying narrative theme</li>
 *   <li>Advantage/Complication tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â narrative twists</li>
 *   <li>Encounter tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â who or what appears</li>
 *   <li>NPC tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â allies and adversaries (Appendix II)</li>
 *   <li>Location tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â where events take place (Appendix III)</li>
 *   <li>Alien Civilization tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â species and polities</li>
 *   <li>Career Event tables ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â what happens during downtime</li>
 * </ul>
 * </p>
 * <p>
 * Each table uses a d20 roll (1-20) to select from 20 entries.
 * Some tables use d6 or d10 and are mapped accordingly.
 * </p>
 */
@Component
public class ProbabilityMatrixService {

    private static final Logger log = LoggerFactory.getLogger(ProbabilityMatrixService.class);
    private final Random random = new Random();

    // ==================== Yes/No Probability Matrix ====================

    /**
     * Probability levels for Yes/No oracle queries.
     * The player sets the probability based on narrative context.
     */
    public enum ProbabilityLevel {
        VERY_UNLIKELY,   // 10% Yes
        UNLIKELY,        // 30% Yes
        FIFTY_FIFTY,     // 50% Yes
        LIKELY,          // 70% Yes
        VERY_LIKELY,     // 90% Yes
        CERTAIN          // 100% Yes (but may have complications)
    }

    /**
     * The Yes/No oracle result.
     */
    public enum YesNoResult {
        YES("Yes ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the answer is affirmative."),
        YES_PLUS("Yes, and ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the answer is affirmative with a bonus or advantage."),
        YES_MINUS("Yes, but ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the answer is affirmative with a complication or cost."),
        NO("No ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the answer is negative."),
        NO_PLUS("No, but ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the answer is negative with a silver lining."),
        NO_MINUS("No, and ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the answer is negative with additional bad news."),
        BOTH("Both/Neither ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the situation is more complex than a simple yes or no."),
        CRITICAL_YES("Critical Yes ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â an extraordinary and highly favorable outcome."),
        CRITICAL_NO("Critical No ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â an extraordinary and highly unfavorable outcome.");

        private final String description;

        YesNoResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Queries the Yes/No oracle with a probability level.
     * <p>
     * The probability level determines the likelihood of a Yes result.
     * A d20 is rolled, and the result is compared to the probability threshold.
     * </p>
     *
     * @param probability the estimated probability level
     * @return the oracle result
     */
    public YesNoResult queryYesNo(ProbabilityLevel probability) {
        int roll = random.nextInt(20) + 1;
        log.debug("Yes/No oracle: probability={}, roll={}", probability, roll);

        return switch (probability) {
            case VERY_UNLIKELY -> queryVeryUnlikely(roll);
            case UNLIKELY -> queryUnlikely(roll);
            case FIFTY_FIFTY -> queryFiftyFifty(roll);
            case LIKELY -> queryLikely(roll);
            case VERY_LIKELY -> queryVeryLikely(roll);
            case CERTAIN -> queryCertain(roll);
        };
    }

    private YesNoResult queryVeryUnlikely(int roll) {
        // 10% Yes: only 20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 19) return YesNoResult.YES_MINUS;
        if (roll >= 17) return YesNoResult.NO_PLUS;
        if (roll >= 15) return YesNoResult.NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryUnlikely(int roll) {
        // 30% Yes: 16-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_MINUS;
        if (roll >= 14) return YesNoResult.YES;
        if (roll >= 11) return YesNoResult.BOTH;
        if (roll >= 6) return YesNoResult.NO;
        if (roll <= 2) return YesNoResult.CRITICAL_NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryFiftyFifty(int roll) {
        // 50% Yes: 11-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 14) return YesNoResult.YES_PLUS;
        if (roll >= 11) return YesNoResult.YES;
        if (roll >= 10) return YesNoResult.BOTH;
        if (roll >= 7) return YesNoResult.NO;
        if (roll <= 2) return YesNoResult.CRITICAL_NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryLikely(int roll) {
        // 70% Yes: 4-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_PLUS;
        if (roll >= 4) return YesNoResult.YES;
        if (roll >= 2) return YesNoResult.YES_MINUS;
        return YesNoResult.CRITICAL_NO;
    }

    private YesNoResult queryVeryLikely(int roll) {
        // 90% Yes: 2-20 = Yes
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_PLUS;
        if (roll >= 2) return YesNoResult.YES;
        if (roll == 1) return YesNoResult.CRITICAL_NO;
        return YesNoResult.NO_MINUS;
    }

    private YesNoResult queryCertain(int roll) {
        // 100% Yes, but may have complications
        if (roll == 20) return YesNoResult.CRITICAL_YES;
        if (roll >= 16) return YesNoResult.YES_PLUS;
        if (roll >= 6) return YesNoResult.YES;
        return YesNoResult.YES_MINUS;
    }

    // ==================== Mission Type Table ====================

    /**
     * Mission types from the Captain's Log rules.
     */
    public enum MissionType {
        DIPLOMACY("Diplomacy ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Negotiations, treaties, and political maneuvering."),
        EXPLORATION("Exploration ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Charting unknown space and discovering new worlds."),
        COMBAT("Combat ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Engaging hostile forces in battle."),
        SCIENCE("Science ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Research, analysis, and scientific discovery."),
        RESCUE("Rescue ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Saving those in distress."),
        INVESTIGATION("Investigation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Uncovering secrets and solving mysteries."),
        TRADE("Trade ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Commerce, resource acquisition, and economic dealings."),
        SURVEILLANCE("Surveillance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Espionage, reconnaissance, and covert operations."),
        CRISIS("Crisis ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Responding to emergencies and disasters."),
        FIRST_CONTACT("First Contact ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Encountering unknown species or civilizations."),
        MAINTENANCE("Maintenance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ship repairs, upgrades, and downtime activities."),
        PATROL("Patrol ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Monitoring borders and protecting territory."),
        TRANSPORT("Transport ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Delivering personnel, cargo, or messages."),
        MEDICAL("Medical ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Treating the sick and injured, epidemiological research."),
        SEARCH("Search ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Finding lost ships, colonies, or artifacts."),
        ESCORT("Escort ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Protecting another vessel or convoy."),
        RECONNAISSANCE("Reconnaissance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Gathering intelligence on a target."),
        NEGOTIATION("Negotiation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Brokerage between conflicting parties."),
        SURVIVAL("Survival ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Enduring hostile environments or situations."),
        CONSPIRACY("Conspiracy ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Uncovering plots and hidden agendas.");

        private final String description;

        MissionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random mission type.
     * @return a randomly selected mission type
     */
    public MissionType rollMissionType() {
        MissionType[] types = MissionType.values();
        int index = random.nextInt(types.length);
        log.debug("Rolled mission type: {}", types[index]);
        return types[index];
    }

    // ==================== Mission Theme Table ====================

    /**
     * Narrative themes that color a mission.
     */
    public enum MissionTheme {
        BETRAYAL("Betrayal ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Someone close cannot be trusted."),
        SACRIFICE("Sacrifice ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Something valuable must be given up."),
        REDEMPTION("Redemption ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A chance to make amends for past failures."),
        DISCOVERY("Discovery ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Uncovering something that changes everything."),
        SURVIVAL("Survival ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Enduring against overwhelming odds."),
        HONOR("Honor ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Upholding principles in the face of temptation."),
        REVENGE("Revenge ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Settling old scores."),
        HOPE("Hope ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Finding light in the darkest circumstances."),
        FEAR("Fear ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Confronting what lies in the shadows."),
        LOYALTY("Loyalty ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Standing by allies when it matters most."),
        DECEPTION("Deception ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Nothing is as it seems."),
        LEGACY("Legacy ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The weight of the past shapes the future."),
        ISOLATION("Isolation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Cut off from help, alone with the problem."),
        ALLIANCE("Alliance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Forming unexpected partnerships."),
        CORRUPTION("Corruption ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Power's corrupting influence."),
        INNOVATION("Innovation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Breaking new ground with bold ideas."),
        JUSTICE("Justice ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Righting wrongs and holding the guilty accountable."),
        MYSTERY("Mystery ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Solving an enigma with far-reaching consequences."),
        WAR("War ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The brutal reality of conflict."),
        PEACE("Peace ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Building bridges where walls once stood.");

        private final String description;

        MissionTheme(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random mission theme.
     * @return a randomly selected mission theme
     */
    public MissionTheme rollMissionTheme() {
        MissionTheme[] themes = MissionTheme.values();
        int index = random.nextInt(themes.length);
        log.debug("Rolled mission theme: {}", themes[index]);
        return themes[index];
    }

    // ==================== Complication Table ====================

    /**
     * Complications that arise during a mission.
     * These are triggered by Threat, failed checks, or narrative choices.
     */
    public enum Complication {
        REINFORCEMENTS("Reinforcements ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The enemy gets backup."),
        SYSTEM_FAILURE("System Failure ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A critical system goes offline."),
        BETRAYAL("Betrayal ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An ally reveals hidden motives."),
        TIME_PRESSURE("Time Pressure ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The clock starts ticking faster."),
        CASUALTIES("Casualties ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Someone is hurt or lost."),
        ENVIRONMENTAL_HAZARD("Environmental Hazard ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The environment turns hostile."),
        COMMUNICATIONS_BLACKOUT("Communications Blackout ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Cut off from support."),
        UNEXPECTED_ALLY("Unexpected Ally ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A stranger offers help, but at what cost?"),
        MORAL_DILEMMA("Moral Dilemma ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A choice with no good options."),
        EQUIPMENT_LOSS("Equipment Loss ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Vital gear is destroyed or stolen."),
        AMBUSH("Ambush ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Attacked from an unexpected direction."),
        POLITICAL_INTERFERENCE("Political Interference ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Orders from above complicate things."),
        TREACHEROUS_TERRAIN("Treachorous Terrain ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The environment works against you."),
        DOUBLE_AGENT("Double Agent ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Someone is playing both sides."),
        POWER_FAILURE("Power Failure ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Lights out, systems down."),
        HOSTAGE_SITUATION("Hostage Situation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Innocents are held at gunpoint."),
        WEAPON_OF_MASS_DESTRUCTION("Weapon of Mass Destruction ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A doomsday device is activated."),
        MUTINY("Mutiny ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Crew members turn against the captain."),
        TEMPORAL_ANOMALY("Temporal Anomaly ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Time behaves strangely."),
        COSMIC_HORROR("Cosmic Horror ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Something beyond comprehension appears.");

        private final String description;

        Complication(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random complication.
     * @return a randomly selected complication
     */
    public Complication rollComplication() {
        Complication[] complications = Complication.values();
        int index = random.nextInt(complications.length);
        log.debug("Rolled complication: {}", complications[index]);
        return complications[index];
    }

    // ==================== Advantage Table ====================

    /**
     * Advantages that help during a mission.
     * These are triggered by Momentum, successful checks, or narrative choices.
     */
    public enum Advantage {
        INTEL("Intel ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You learn something the enemy doesn't expect you to know."),
        REINFORCEMENTS("Reinforcements ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Friendly forces arrive to help."),
        EQUIPMENT_BOOST("Equipment Boost ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A piece of gear performs better than expected."),
        ALLY_ASSISTANCE("Ally Assistance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An ally steps in at the crucial moment."),
        ENVIRONMENTAL_EDGE("Environmental Edge ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The environment works in your favor."),
        SURPRISE_ATTACK("Surprise Attack ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You catch the enemy off guard."),
        MORALE_BOOST("Morale Boost ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The crew's spirits are lifted."),
        TECHNOLOGY_EDGE("Technology Edge ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Your tech outclasses the opposition."),
        INSIDER_INFORMATION("Insider Information ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A mole provides critical details."),
        SAFE_HAVEN("Safe Haven ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A place to rest and regroup."),
        WEAPON_UPGRADE("Weapon Upgrade ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Armaments are more effective than planned."),
        DIPLOMATIC_OPENING("Diplomatic Opening ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A path to negotiation appears."),
        MEDICAL_SUPPLY("Medical Supply ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Healing resources are found."),
        TRANSPORT_ASSISTANCE("Transport Assistance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A ride or shuttle becomes available."),
        SENSOR_ADVANTAGE("Sensor Advantage ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You see what others miss."),
        SHIELD_BOOST("Shield Boost ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Defenses hold stronger than expected."),
        CREW_EXPERTISE("Crew Expertise ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A crew member's skill shines."),
        LUCKY_BREAK("Lucky Break ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Fortune smiles upon you."),
        ELEMENT_OF_SURPRISE("Element of Surprise ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The enemy isn't ready."),
        COMMAND_PRESENCE("Command Presence ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Your leadership inspires excellence.");

        private final String description;

        Advantage(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random advantage.
     * @return a randomly selected advantage
     */
    public Advantage rollAdvantage() {
        Advantage[] advantages = Advantage.values();
        int index = random.nextInt(advantages.length);
        log.debug("Rolled advantage: {}", advantages[index]);
        return advantages[index];
    }

    // ==================== Encounter Table ====================

    /**
     * Types of encounters that can occur during a mission.
     */
    public enum EncounterType {
        HOSTILE_SHIP("Hostile Ship ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An enemy vessel hails or attacks."),
        DISTRESS_SIGNAL("Distress Signal ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Someone is calling for help."),
        ANOMALOUS_READING("Anomalous Reading ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Sensors detect something unusual."),
        NEUTRAL_PARTY("Neutral Party ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A third faction enters the scene."),
        CIVILIAN_VESSEL("Civilian Vessel ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Non-combatants are in the area."),
        DERELICT("Derelict ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An abandoned ship or station drifts nearby."),
        ASTEROID_FIELD("Asteroid Field ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Navigational hazard ahead."),
        PLANETARY_DEFENSE("Planetary Defense ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Automated defenses activate."),
        PIRATE_ATTACK("Pirate Attack ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Raiders strike without warning."),
        DIPLOMATIC_MEETING("Diplomatic Meeting ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A formal encounter with another faction."),
        SCIENTIFIC_PHENOMENON("Scientific Phenomenon ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A natural event demands study."),
        BLACK_MARKET("Black Market ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An opportunity for illicit trade."),
        SEARCH_AND_RESCUE("Search and Rescue ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Survivors need extraction."),
        TERRITORIAL_DISPUTE("Territorial Dispute ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Two factions clash over space."),
        MYSTERIOUS_SIGNAL("Mysterious Signal ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An encoded transmission is detected."),
        WEATHER_STORM("Weather Storm ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A plasma storm or radiation belt."),
        GHOST_SHIP("Ghost Ship ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A vessel with no life signs approaches."),
        REFUGEE_CRISIS("Refugee Crisis ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Displaced civilians need aid."),
        MILITARY_BLOCKADE("Military Blockade ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Passage is denied."),
        ARTIFACT_DISCOVERY("Artifact Discovery ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An ancient object is found.");

        private final String description;

        EncounterType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random encounter.
     * @return a randomly selected encounter type
     */
    public EncounterType rollEncounter() {
        EncounterType[] encounters = EncounterType.values();
        int index = random.nextInt(encounters.length);
        log.debug("Rolled encounter: {}", encounters[index]);
        return encounters[index];
    }

    // ==================== NPC Generation Tables (Appendix II) ====================

    /**
     * NPC roles from the Captain's Log rulebook.
     */
    public enum NpcRole {
        COMMANDING_OFFICER("Commanding Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A leader making tough decisions."),
        FIRST_OFFICER("First Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The right hand, executing orders."),
        SCIENCE_OFFICER("Science Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Analyzing data and making discoveries."),
        SECURITY_OFFICER("Security Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Enforcing order and handling threats."),
        ENGINEER("Engineer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Keeping the ship running and fixing problems."),
        MEDICAL_OFFICER("Medical Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Healing the wounded and treating disease."),
        PILOT("Pilot ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Flying the ship through danger."),
        TACTICAL_OFFICER("Tactical Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Directing weapons and shields."),
        COMMUNICATIONS_OFFICER("Communications Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Managing all transmissions."),
        NAVIGATOR("Navigator ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Plotting courses through space."),
        DIPLOMAT("Diplomat ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Negotiating with other factions."),
        SCOUT("Scout ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Reconnaissance and first contact."),
        MERCHANT("Merchant ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Trading goods and services."),
        SMUGGLER("Smuggler ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Operating outside the law."),
        SPY("Spy ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Gathering intelligence covertly."),
        MERCENARY("Mercenary ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Fighting for profit."),
        ACADEMIC("Academic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Researching and teaching."),
        RELIGIOUS_LEADER("Religious Leader ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Guiding the faithful."),
        CRIMINAL("Criminal ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Operating in the shadows."),
        CIVILIAN("Civilian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An ordinary person caught in extraordinary events.");

        private final String description;

        NpcRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * NPC alignment ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â how they relate to the player.
     */
    public enum NpcAlignment {
        ALLY("Ally ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Works with the player character."),
        NEUTRAL("Neutral ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Has their own agenda."),
        RIVAL("Rival ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Competes with the player."),
        ENEMY("Enemy ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Actively opposes the player."),
        WILD_CARD("Wild Card ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Unpredictable, could go either way.");

        private final String description;

        NpcAlignment(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Alien species from the Star Trek universe and beyond.
     */
    public enum AlienSpecies {
        HUMAN("Human ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Versatile and adaptable."),
        ANDORIAN("Andorian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Honor-bound warriors from a cold world."),
        BAJORAN("Bajoran ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Spiritual and resilient."),
        BETAZOID("Betazoid ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Empathic communicators."),
        BOLIAN("Bolian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Artistic and philosophical."),
        CARDASSIAN("Cardassian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ambitious and militaristic."),
        DENOBULAN("Denobulan ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Master healers and scientists."),
        FERENGI("Ferengi ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Profit-driven traders."),
        HYLEAN("Hylean ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Proud and traditional."),
        KLINGON("Klingon ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Fierce warriors who value honor."),
        ROMULAN("Romulan ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Cunning and secretive."),
        VULCAN("Vulcan ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Logical and disciplined."),
        TRILL("Trill ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Symbiotic beings with combined wisdom."),
        ORION("Orion ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Raiders and traders."),
        THOLIAN("Tholian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Isolationist and technologically advanced."),
        ROMULAN_ELITE("Romulan Elite ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Nobility of the Romulan Star Empire."),
        FOUNDLING("Foundling ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A mysterious and ancient species."),
        XINDI("Xindi ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A diverse species with multiple primate forms."),
        DELTA_RACE("Delta Race ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Advanced beings from the Delta Quadrant."),
        UNKNOWN("Unknown ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An unclassified species.");

        private final String description;

        AlienSpecies(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * NPC name prefixes for generating character names.
     */
    private static final String[] NPC_FIRST_NAMES = {
        "James", "Kathryn", "Jean-Luc", "Benjamin", "Jonathan", "Chakotay",
        "Worf", "Deanna", "Data", "Geordi", "Picard", "Sisko",
        "Janeway", "Archer", "Kirk", "Spock", "McCoy", "Scott",
        "Uhura", "Chekov", "Sulu", "Riker", "Troi", "La Forge",
        "Tuvok", "Harry", "Paris", "Tom", "Voyager", "B'Elanna",
        "Torres", "Neelix", "Seven", "Kes", "Tuvok", "Guinan",
        "Q", "Zefram", "Zhang", "Abe", "Pike", "Number One",
        "Philippa", "George", "Muiri", "Raana", "Hikaru", "Carol",
        "Saru", "Tilly", "Stamets", "Burnham", "Georgiou", "Vash"
    };

    /**
     * NPC name suffixes for generating character names.
     */
    private static final String[] NPC_LAST_NAMES = {
        "Kirk", "Picard", "Sisko", "Janeway", "Archer", "Scott",
        "McCoy", "Spock", "Chekov", "Sulu", "Riker", "Troi",
        "La Forge", "Tuvok", "Paris", "Tom", "Torres", "Neelix",
        "Guinan", "Zefram", "Zhang", "Pike", "Saru", "Tilly",
        "Stamets", "Burnham", "Georgiou", "Vash", "Crusher", "Worf",
        "Dax", "Ezri", "Weyoun", "Dukat", "O'Brien", "Kira",
        "Sisko", "Quark", "Garak", "Odo", "Dax", "Rom",
        "Leeta", " Nog", "Jadzia", "Curzon", "Emony", "Gowron"
    };

    /**
     * Generates a random NPC name.
     * @return a randomly generated NPC name
     */
    public String generateNpcName() {
        String first = NPC_FIRST_NAMES[random.nextInt(NPC_FIRST_NAMES.length)];
        String last = NPC_LAST_NAMES[random.nextInt(NPC_LAST_NAMES.length)];
        return first + " " + last;
    }

    /**
     * Rolls for a random NPC role.
     * @return a randomly selected NPC role
     */
    public NpcRole rollNpcRole() {
        NpcRole[] roles = NpcRole.values();
        int index = random.nextInt(roles.length);
        log.debug("Rolled NPC role: {}", roles[index]);
        return roles[index];
    }

    /**
     * Rolls for a random NPC alignment.
     * @return a randomly selected NPC alignment
     */
    public NpcAlignment rollNpcAlignment() {
        NpcAlignment[] alignments = NpcAlignment.values();
        int index = random.nextInt(alignments.length);
        log.debug("Rolled NPC alignment: {}", alignments[index]);
        return alignments[index];
    }

    /**
     * Rolls for a random alien species.
     * @return a randomly selected alien species
     */
    public AlienSpecies rollAlienSpecies() {
        AlienSpecies[] species = AlienSpecies.values();
        int index = random.nextInt(species.length);
        log.debug("Rolled alien species: {}", species[index]);
        return species[index];
    }

    /**
     * Generates a complete NPC profile.
     * @return a map containing the NPC's name, role, alignment, and species
     */
    public Map<String, String> generateNpc() {
        String name = generateNpcName();
        NpcRole role = rollNpcRole();
        NpcAlignment alignment = rollNpcAlignment();
        AlienSpecies species = rollAlienSpecies();

        log.info("Generated NPC: {} ({}) - {} - {} - {}", name, species, role, alignment, role.getDescription());

        Map<String, String> npc = new LinkedHashMap<>();
        npc.put("name", name);
        npc.put("species", species.name());
        npc.put("speciesDescription", species.getDescription());
        npc.put("role", role.name());
        npc.put("roleDescription", role.getDescription());
        npc.put("alignment", alignment.name());
        return npc;
    }

    // ==================== Location Tables (Appendix III) ====================

    /**
     * Location types where events can take place.
     */
    public enum LocationType {
        STARBASE("Starbase ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A large military or civilian space station."),
        COLONY("Colony ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A settled world with infrastructure."),
        SPACE_STATION("Space Station ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A smaller orbital facility."),
        PLANET_SURFACE("Planet Surface ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â On the ground of a habitable world."),
        DEEP_SPACE("Deep Space ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Far from any star or colony."),
        WORMHOLE("Wormhole ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A spatial anomaly connecting distant points."),
        NEBULA("Nebula ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A cloud of gas and dust."),
        ASTEROID_BELT("Asteroid Belt ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A field of rocky bodies."),
        STARSHIP("Starship ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Aboard a vessel in transit."),
        MOON("Moon ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A natural satellite with facilities."),
        OUTPOST("Outpost ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A remote forward base."),
        MARKET("Market ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A trading hub or bazaar."),
        DIPLOMATIC_ENCLAVE("Diplomatic Enclave ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Neutral ground for negotiations."),
        RESEARCH_FACILITY("Research Facility ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A laboratory or observatory."),
        MILITARY_FORTRESS("Military Fortress ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A heavily defended installation."),
        PRISON("Prison ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A detention facility."),
        HOSPITAL("Hospital ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A medical center."),
        TEMPLE("Temple ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A place of worship or meditation."),
        BLACK_SITE("Black Site ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A covert, unacknowledged facility."),
        ANOMALOUS_ZONE("Anomalous Zone ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An area where physics behaves strangely.");

        private final String description;

        LocationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Planet types for world generation.
     */
    public enum PlanetType {
        TERRAN("Terran ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Earth-like, habitable world."),
        DESERT("Desert ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Arid world with vast sand seas."),
        OCEAN("Ocean ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Water world with minimal landmass."),
        ICE("Ice ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Frozen world with glacial landscapes."),
        VOLCANIC("Volcanic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Active tectonics and lava flows."),
        JUNGLE("Jungle ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Dense vegetation and high humidity."),
        GAS_GIANT("Gas Giant ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â No solid surface, atmospheric platforms."),
        BARREN("Barren ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Lifeless rock with minimal atmosphere."),
        TOXIC("Toxic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Hazardous atmosphere requires protection."),
        URBAN("Urban ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Heavily developed, city-covered world."),
        ARCTIC("Arctic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Polar world with ice caps."),
        SWAMP("Swamp ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Wetlands and marshes."),
        MOUNTAIN("Mountain ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rugged terrain with high peaks."),
        SUBTERRANEAN("Subterranean ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Underground civilization."),
        MOON_HABITAT("Moon Habitat ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Artificial habitat on a moon."),
        DYSON_STRUCTURE("Dyson Structure ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Artificial megastructure."),
        WARP_STORM("Warp Storm ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Perpetual energy storm."),
        CRYSTAL("Crystal ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â World covered in crystalline formations."),
        MAGNETIC("Magnetic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Extreme magnetic fields affect technology."),
        UNKNOWN("Unknown ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Classification pending further study.");

        private final String description;

        PlanetType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Location name prefixes for generating place names.
     */
    private static final String[] LOCATION_PREFIXES = {
        "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta",
        "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu",
        "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau",
        "Upsilon", "Phi", "Chi", "Psi", "Omega"
    };

    /**
     * Location name roots for generating place names.
     */
    private static final String[] LOCATION_ROOTS = {
        "Prime", "Major", "Minor", "Nova", "Aeterna", "Solis",
        "Luna", "Stellar", "Nebula", "Vega", "Rigel", "Altair",
        "Deneb", "Sirius", "Polaris", "Andromeda", "Centauri", "Pegasus",
        "Orion", "Lyra", "Cassiopeia", "Draco", "Hydra", "Phoenix",
        "Andoria", "Vulcan", "Qo'noS", "Romulus", "Cardasia", "Ferenginar",
        "Bajor", "Denobula", "Trill", "Betazed", "Risa", "Cestus"
    };

    /**
     * Generates a random location name.
     * @return a randomly generated location name
     */
    public String generateLocationName() {
        String prefix = LOCATION_PREFIXES[random.nextInt(LOCATION_PREFIXES.length)];
        String root = LOCATION_ROOTS[random.nextInt(LOCATION_ROOTS.length)];
        return prefix + " " + root;
    }

    /**
     * Rolls for a random location type.
     * @return a randomly selected location type
     */
    public LocationType rollLocationType() {
        LocationType[] types = LocationType.values();
        int index = random.nextInt(types.length);
        log.debug("Rolled location type: {}", types[index]);
        return types[index];
    }

    /**
     * Rolls for a random planet type.
     * @return a randomly selected planet type
     */
    public PlanetType rollPlanetType() {
        PlanetType[] types = PlanetType.values();
        int index = random.nextInt(types.length);
        log.debug("Rolled planet type: {}", types[index]);
        return types[index];
    }

    /**
     * Generates a complete location profile.
     * @return a map containing the location's name, type, and planet type
     */
    public Map<String, String> generateLocation() {
        String name = generateLocationName();
        LocationType locationType = rollLocationType();
        PlanetType planetType = rollPlanetType();

        log.info("Generated location: {} - {} - {}", name, locationType, planetType);

        Map<String, String> location = new LinkedHashMap<>();
        location.put("name", name);
        location.put("locationType", locationType.name());
        location.put("locationTypeDescription", locationType.getDescription());
        location.put("planetType", planetType.name());
        location.put("planetTypeDescription", planetType.getDescription());
        return location;
    }

    // ==================== Career Event Table ====================

    /**
     * Career events that occur during downtime between missions.
     */
    public enum CareerEvent {
        PROMOTION("Promotion ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Your rank and responsibilities increase."),
        NEW_ASSIGNMENT("New Assignment ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Transferred to a different post."),
        TRAINING("Training ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Attended a course that improves your skills."),
        LEAVE("Leave ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Time off to rest and recharge."),
        PERSONAL_CRISIS("Personal Crisis ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A personal matter demands attention."),
        NEW_ALLY("New Ally ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You gain a valuable contact or friend."),
        NEW_ENEMY("New Enemy ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Someone holds a grudge against you."),
        EQUIPMENT_UPGRADE("Equipment Upgrade ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Your gear is improved."),
        SHIP_TRANSFER("Ship Transfer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Reassigned to a different vessel."),
        MEDICAL_TREATMENT("Medical Treatment ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Health issues are addressed."),
        POLITICAL_INTRIGUE("Political Intrigue ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Office politics affect your career."),
        SCIENTIFIC_BREAKTHROUGH("Scientific Breakthrough ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Your research yields results."),
        COURT_MARTIAL("Court Martial ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You face disciplinary action."),
        AWARDS_CEREMONY("Awards Ceremony ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Your achievements are recognized."),
        MENTORSHIP("Mentorship ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You guide a junior officer."),
        INVESTIGATION("Investigation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â You're questioned about a past mission."),
        BLACK_OPS("Black Ops ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A covert assignment is offered."),
        RETIREMENT_CONSIDERATION("Retirement Consideration ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Is it time to step down?"),
        FAMILY_MATTER("Family Matter ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Relatives need your help."),
        RETIREMENT("Retirement ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A chapter closes, a new one begins.");

        private final String description;

        CareerEvent(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random career event.
     * @return a randomly selected career event
     */
    public CareerEvent rollCareerEvent() {
        CareerEvent[] events = CareerEvent.values();
        int index = random.nextInt(events.length);
        log.debug("Rolled career event: {}", events[index]);
        return events[index];
    }

    // ==================== Expanded Prompt Cards ====================

    /**
     * Generates a random prompt card for mission inspiration.
     * This is an expanded version of the original 20 prompts, now with 60+ prompts
     * organized by category.
     */
    public String generatePromptCard() {
        String[] prompts = {
            // Combat prompts
            "A distress signal leads to an unexpected discovery.",
            "First contact with a species that defies classification.",
            "A political crisis threatens to escalate into war.",
            "An ancient artifact holds the key to a long-lost technology.",
            "A crew member's secret is revealed at the worst moment.",
            "A spatial anomaly creates a time dilation effect.",
            "A rival faction competes for the same objective.",
            "A moral dilemma challenges the crew's values.",
            "A seemingly routine mission hides a deeper conspiracy.",
            "A natural disaster threatens a nearby colony.",
            // Exploration prompts
            "An old enemy resurfaces with a new threat.",
            "A diplomatic mission requires unusual tactics.",
            "A scientific discovery has dangerous implications.",
            "A lost starship is found with its crew intact.",
            "A planet's ecosystem is collapsing ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â can it be saved?",
            "A cyber attack targets the ship's computer systems.",
            "A refugee fleet seeks asylum in Federation space.",
            "A wormhole opens to an uncharted region.",
            "A saboteur is among the crew.",
            "A race against time to prevent a catastrophe.",
            // Personal prompts
            "A former colleague asks for your help in secret.",
            "You receive a message from someone you thought was dead.",
            "A vision or dream hints at a future threat.",
            "Your ship's AI begins behaving strangely.",
            "A crew member goes missing during a routine EVA.",
            "An old wound reopens ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â literally or figuratively.",
            "You must choose between duty and personal loyalty.",
            "A child's life depends on your decision.",
            "Your past catches up with you in an unexpected way.",
            "A gift from an unknown sender arrives aboard.",
            // Crisis prompts
            "A supernova threatens nearby colonies.",
            "The ship's warp core goes critical.",
            "A plague spreads through the crew.",
            "Hostile forces blockade a vital trade route.",
            "A diplomatic incident could spark a war.",
            "An ancient weapon is discovered and activated.",
            "The crew mutinies over a controversial order.",
            "A black hole destabilizes the sector.",
            "A temporal rift threatens to erase the timeline.",
            "An AI achieves sentience and demands rights.",
            // Diplomacy prompts
            "Two factions negotiate a fragile peace.",
            "A trade agreement hides a hidden clause.",
            "An ambassador is assassinated on your watch.",
            "A cultural misunderstanding nearly causes conflict.",
            "A neutral zone is violated by an unknown party.",
            "A hostage exchange goes wrong.",
            "A secret treaty is exposed to the public.",
            "An alien delegation requests asylum.",
            "A border dispute escalates into skirmishes.",
            "A peace conference is sabotaged.",
            // Science prompts
            "A new form of matter is discovered.",
            "An experiment yields unexpected results.",
            "A disease with no known cure spreads.",
            "A signal from deep space contains a message.",
            "A planet's core is artificially generated.",
            "A biological weapon is unleashed.",
            "A star's behavior defies known physics.",
            "An archaeological dig uncovers a pre-warp civilization.",
            "A genetic anomaly is found in the crew.",
            "A parallel universe is detected.",
            // Mystery prompts
            "A series of murders points to an inside job.",
            "Stolen data could compromise national security.",
            "A ghost ship drifts through space, crew unaccounted for.",
            "A coded message leads to a hidden base.",
            "A witness disappears before testifying.",
            "A frame-up targets an innocent officer.",
            "A conspiracy reaches the highest levels of command.",
            "A missing person's trail leads to a black site.",
            "A forged document alters the course of events.",
            "A mysterious illness affects only certain crew members."
        };
        return prompts[random.nextInt(prompts.length)];
    }

    // ==================== Maintenance / Downtime Tables (Appendix IV) ====================

    /**
     * Maintenance events that occur during downtime between missions.
     */
    public enum MaintenanceEvent {
        SYSTEM_REPAIR("System Repair ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Fixing damage to ship systems."),
        WEAPON_CALIBRATION("Weapon Calibration ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Tuning weapons for optimal performance."),
        SHIELD_UPGRADE("Shield Upgrade ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Improving defensive capabilities."),
        ENGINE_OVERHAUL("Engine Overhaul ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Boosting warp and impulse performance."),
        SENSOR_ENHANCEMENT("Sensor Enhancement ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Extending detection range."),
        COMPUTER_UPDATE("Computer Update ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Installing new software and patches."),
        CREW_TRAINING("Crew Training ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Improving crew skills and coordination."),
        MEDICAL_SUPPLY_RESTOCK("Medical Supply Restock ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Replenishing healing resources."),
        HULL_PATCHING("Hull Patching ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Repairing structural damage."),
        COMMS_REPAIR("Comms Repair ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Restoring communication systems."),
        LIFE_SUPPORT_CHECK("Life Support Check ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ensuring environmental systems work."),
        CARGO_ORGANIZATION("Cargo Organization ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Sorting and securing supplies."),
        CREW_LEAVE("Crew Leave ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Allowing crew members time off."),
        DIPLOMATIC_VISIT("Diplomatic Visit ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Hosting or attending a diplomatic event."),
        SCIENTIFIC_RESEARCH("Scientific Research ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Conducting experiments and studies."),
        PERSONAL_TIME("Personal Time ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Reflecting on values and goals."),
        INTEL_ANALYSIS("Intel Analysis ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Reviewing intelligence reports."),
        CREW_EVALUATION("Crew Evaluation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Assessing crew performance."),
        EMERGENCY_DRILL("Emergency Drill ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Practicing for worst-case scenarios."),
        SHIP_CUSTOMIZATION("Ship Customization ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Personalizing the vessel.");

        private final String description;

        MaintenanceEvent(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random maintenance event.
     * @return a randomly selected maintenance event
     */
    public MaintenanceEvent rollMaintenanceEvent() {
        MaintenanceEvent[] events = MaintenanceEvent.values();
        int index = random.nextInt(events.length);
        log.debug("Rolled maintenance event: {}", events[index]);
        return events[index];
    }

    /**
     * Alien polities and factions from the Star Trek universe.
     */
    public enum AlienPolity {
        UNITED_FEDERATION("United Federation of Planets ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Democratic alliance of worlds."),
        KLINGON_EMPIRE("Klingon Empire ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Warrior culture valoring honor and strength."),
        ROMULAN_STAR_EMPIRE("Romulan Star Empire ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Secretive and cunning rivals."),
        FERENGI_ALLIANCE("Ferengi Alliance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Profit-driven merchant society."),
        CARDASSIAN_UNION("Cardassian Union ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Authoritarian militaristic state."),
        BAJORAN_RESISTANCE("Bajoran Resistance ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Freedom fighters seeking independence."),
        ORION_SYNDICATE("Orion Syndicate ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Interstellar criminal organization."),
        THOLIAN_COLLECTIVE("Tholian Collective ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Isolationist defensive alliance."),
        ANDORIAN_EMPIRE("Andorian Empire ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Honor-bound warrior culture."),
        BETAZOI_COLLECTIVE("Betazoi Collective ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Empathic telepathic society."),
        DENOBULAN_FEDERATION("Denobulan Federation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Healing-focused civilization."),
        TRILL_UNITY("Trill Unity ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Symbiotic beings with combined wisdom."),
        ROMULAN_SENSITATIVE_OFFICE("Romulan Sensitive Office ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Secret intelligence agency."),
        FERENGI_COMMERCE_AUTHORITY("Ferengi Commerce Authority ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Corporate overlords."),
        KLINGON_HIGH_COUNCIL("Klingon High Council ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Governing body of the Empire."),
        FEDERATION_COUNCIL("Federation Council ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Legislative body of the Federation."),
        BAJORAN_RELICTION_AUTHORITY("Bajoran Religion Authority ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Spiritual leaders."),
        CARDASSIA_CENTRAL("Cardassia Central ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ruling council of Cardassia."),
        MAUDITH("Mau'Dor ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ancient and mysterious beings."),
        UNKNOWN_FACTION("Unknown Faction ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â An unclassified political entity.");

        private final String description;

        AlienPolity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random alien polity.
     * @return a randomly selected alien polity
     */
    public AlienPolity rollAlienPolity() {
        AlienPolity[] polities = AlienPolity.values();
        int index = random.nextInt(polities.length);
        log.debug("Rolled alien polity: {}", polities[index]);
        return polities[index];
    }

    /**
     * Generates a complete downtime profile.
     * @return a map containing the maintenance event and any related polity
     */
    public Map<String, String> generateDowntimeProfile() {
        MaintenanceEvent event = rollMaintenanceEvent();
        AlienPolity polity = rollAlienPolity();

        log.info("Generated downtime profile: {} ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â {}", event, polity);

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("maintenanceEvent", event.name());
        profile.put("maintenanceDescription", event.getDescription());
        profile.put("relatedPolity", polity.name());
        profile.put("polityDescription", polity.getDescription());
        return profile;
    }

    // ==================== Appendix II: Allies and Adversaries ====================

    /**
     * Pre-built adversary types from the Captain's Log rulebook (Appendix II).
     * These represent recurring threats and enemies.
     */
    public enum Adversary {
        KLINGON_WARRIOR("Klingon Warrior ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Honorable but deadly opponent."),
        ROMULAN_OPERATIVE("Romulan Operative ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Cunning agent of the Tal Shiar."),
        FERENGI_RAIDER("Ferengi Raider ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Profit-driven pirate."),
        CARDASSIAN_OFFICER("Cardassian Officer ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ruthless enforcer of the Union."),
        ORION_SLAVE_TRADER("Orion Slave Trader ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Criminal dealing in lives."),
        BORG_DRONE("Borg Drone ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Relentless assimilator."),
        ROMULAN_SNIPER("Romulan Sniper ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Patient and precise assassin."),
        KLINGON_BOARDING_PARTY("Klingon Boarding Party ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Aggressive assault team."),
        FERENGI_MERCENARY("Ferengi Mercenary ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Will fight for the right price."),
        CARDASSIAN_JAGHRAAF("Cardassian Jaghraf ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Elite special forces operative."),
        THOLIAN_GUARDIAN("Tholian Guardian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Defensive but dangerous."),
        BAJORAN_INSURGENT("Bajoran Insurgent ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Desperate freedom fighter."),
        ORION_WARRIOR("Orion Warrior ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Brutal and strong combatant."),
        ROMULAN_TACTICIAN("Romulan Tactician ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Master of deception and strategy."),
        KLINGON_CHAMPION("Klingon Champion ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â The best warrior the Empire has to offer."),
        UNKNOWN_ENTITIES("Unknown Entities ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Mysterious beings with unknown motives."),
        CYBORG_ASSASSIN("Cyborg Assassin ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Enhanced killer with no mercy."),
        PIRATE_CAPTAIN("Pirate Captain ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Leader of a rogue crew."),
        CORRUPT_OFFICIAL("Corrupt Official ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Abuses power for personal gain."),
        RIVAL_CAPTAIN("Rival Captain ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Competing officer with conflicting goals.");

        private final String description;

        Adversary(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random adversary.
     * @return a randomly selected adversary
     */
    public Adversary rollAdversary() {
        Adversary[] adversaries = Adversary.values();
        int index = random.nextInt(adversaries.length);
        log.debug("Rolled adversary: {}", adversaries[index]);
        return adversaries[index];
    }

    /**
     * Pre-built ally types from the Captain's Log rulebook (Appendix II).
     * These represent helpful contacts and allies.
     */
    public enum Ally {
        FEDERATION_LIAISON("Federation Liaison ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Official contact within Starfleet."),
        INFORMANT("Informant ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Source of intelligence from within enemy ranks."),
        LOCAL_GUIDE("Local Guide ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Knows the area and its secrets."),
        ALLIED_CAPTAIN("Allied Captain ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Friendly officer who can provide assistance."),
        SCIENTIFIC_CONTACT("Scientific Contact ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Expert who can analyze anomalies."),
        DIPLOMATIC_CONTACT("Diplomatic Contact ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can open doors through protocol."),
        RESISTANCE_LEADER("Resistance Leader ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Commands local freedom fighters."),
        MEDICAL_EXPERT("Medical Expert ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can heal injuries and treat diseases."),
        TECH_SPECIALIST("Tech Specialist ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can repair or hack systems."),
        NAVIGATOR("Navigator ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Knows the routes through dangerous space."),
        TRANSLATOR("Translator ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can communicate with alien species."),
        PILOT("Pilot ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Expert ship or shuttle operator."),
        SECURITY_CONSULTANT("Security Consultant ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can assess and improve defenses."),
        SUPPLIER("Supplier ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Provides equipment and resources."),
        LEGAL_ADVISOR("Legal Advisor ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Knows the regulations and loopholes."),
        HISTORIAN("Historian ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Knows the ancient secrets and prophecies."),
        PSYCHIC_ALLY("Psychic Ally ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can sense danger and read minds."),
        ENGINEER_CONTACT("Engineer Contact ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can modify or build equipment."),
        DIPLOMAT("Diplomat ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Can negotiate peace or alliances."),
        MENTOR("Mentor ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Wise advisor who guides your character.");

        private final String description;

        Ally(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random ally.
     * @return a randomly selected ally
     */
    public Ally rollAlly() {
        Ally[] allies = Ally.values();
        int index = random.nextInt(allies.length);
        log.debug("Rolled ally: {}", allies[index]);
        return allies[index];
    }

    /**
     * Generates an adversary profile with name, type, and motivation.
     * @return a map containing adversary details
     */
    public Map<String, String> generateAdversaryProfile() {
        Adversary adversary = rollAdversary();
        AlienSpecies species = rollAlienSpecies();
        AlienPolity polity = rollAlienPolity();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("adversary", adversary.name());
        profile.put("adversaryDescription", adversary.getDescription());
        profile.put("species", species.name());
        profile.put("speciesDescription", species.getDescription());
        profile.put("affiliation", polity.name());
        profile.put("affiliationDescription", polity.getDescription());
        return profile;
    }

    /**
     * Generates an ally profile with name, type, and specialty.
     * @return a map containing ally details
     */
    public Map<String, String> generateAllyProfile() {
        Ally ally = rollAlly();
        AlienSpecies species = rollAlienSpecies();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("ally", ally.name());
        profile.put("allyDescription", ally.getDescription());
        profile.put("species", species.name());
        profile.put("speciesDescription", species.getDescription());
        return profile;
    }

    // ==================== Appendix III: Locations and Starships ====================

    /**
     * Starship classes and types from the Star Trek universe (Appendix III).
     */
    public enum StarshipClass {
        CONSTITUTION("Constitution-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Versatile exploration vessel (2 ships)."),
        GALAXY("Galaxy-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Long-range exploration flagship (5 ships)."),
        SOVEREIGN("Sovereign-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Diplomatic and combat vessel (3 ships)."),
        VOYAGER("Voyager-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Deep space exploration (2 ships)."),
        DEFiant("Defiant-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Heavy combat vessel (1 ship)."),
        AKIRA("Akira-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Heavy exploration and combat (3 ships)."),
        NEBUULA("Nebula-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Science and exploration (2 ships)."),
        PROMETHEUS("Prometheus-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Stealth and reconnaissance (1 ship)."),
        D7("D7-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Klingon battle cruiser (1 ship)."),
        DREADNOUGHT("Dreadnought-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Klingon heavy warship (2 ships)."),
        WARSHIP("Warship-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Romulan heavy combat vessel (1 ship)."),
        CRUISER("Cruiser-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Romulan long-range vessel (2 ships)."),
        RAIDER("Raider-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ferengi fast attack ship (1 ship)."),
        MERCHANT("Merchant-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Ferengi cargo and trade vessel (2 ships)."),
        ORBITAL("Orbital Station ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Large space station (6 ships)."),
        SHUTTLE("Shuttlecraft ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Small transport vessel (0 ships)."),
        RUNABOUT("Runabout ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Small multi-role vessel (0 ships)."),
        EXPLORER("Explorer-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Deep space probe ship (1 ship)."),
        CUSTOM("Custom-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Unique vessel with custom capabilities."),
        UNKNOWN("Unknown-Class ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Unidentified vessel type.");

        private final String description;

        StarshipClass(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random starship class.
     * @return a randomly selected starship class
     */
    public StarshipClass rollStarshipClass() {
        StarshipClass[] classes = StarshipClass.values();
        int index = random.nextInt(classes.length);
        log.debug("Rolled starship class: {}", classes[index]);
        return classes[index];
    }

    /**
     * Terrain types for planetary surfaces (Appendix III).
     */
    public enum TerrainType {
        MOUNTAINS("Mountains ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Rugged peaks and valleys."),
        PLAINS("Plains ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Flat, open grasslands."),
        FOREST("Forest ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Dense woodland with diverse flora."),
        JUNGLE("Jungle ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Thick, humid tropical growth."),
        DESERT("Desert ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Arid sands and rock formations."),
        TUNDRA("Tundra ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Frozen, barren landscape."),
        SWAMP("Swamp ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Wet, marshy terrain with standing water."),
        VOLCANIC("Volcanic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Active lava flows and ash fields."),
        OCEANIC("Oceanic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Water-covered world with islands."),
        URBAN("Urban ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Heavily developed cityscape."),
        CANYON("Canyon ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Deep gorges and cliffs."),
        CAVE("Cave ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Underground network of tunnels."),
        GLACIER("Glacier ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Massive ice formations."),
        RAIN_FOREST("Rain Forest ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Lush, biodiverse ecosystem."),
        STEPPE("Steppe ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Semi-arid grassland."),
        BADLANDS("Badlands ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Eroded, rocky terrain."),
        CRATER("Crater ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Impact site with unique geology."),
        RIFT("Rift ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Tectonic fissure with unusual activity."),
        ARTIFICIAL("Artificial ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Constructed or modified terrain."),
        UNKNOWN("Unknown ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Terrain type not yet identified.");

        private final String description;

        TerrainType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random terrain type.
     * @return a randomly selected terrain type
     */
    public TerrainType rollTerrainType() {
        TerrainType[] terrains = TerrainType.values();
        int index = random.nextInt(terrains.length);
        log.debug("Rolled terrain type: {}", terrains[index]);
        return terrains[index];
    }

    /**
     * Atmosphere types for planetary classification (Appendix III).
     */
    public enum AtmosphereType {
        BREATHABLE("Breathable ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Humans can breathe without assistance."),
        TOXIC("Toxic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Requires environmental suit."),
        THIN("Thin ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Low oxygen, requires breathing apparatus."),
        DENSE("Dense ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â High pressure, requires pressure suit."),
        NONE("None ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Vacuum or near-vacuum environment."),
        WATER_VAPOR("Water Vapor ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Steam-filled atmosphere."),
        METHANE("Methane ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Natural gas atmosphere."),
        AMMONIA("Ammonia ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Toxic ammonia-based atmosphere."),
        CARBON_DIOXIDE("Carbon Dioxide ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â CO2-rich atmosphere."),
        NITROGEN("Nitrogen ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Nitrogen-dominant atmosphere."),
        OXYGEN_RICH("Oxygen Rich ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Highly flammable, oxygen-saturated."),
        SULFURIC("Sulfuric ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Acidic atmosphere with sulfur compounds."),
        RADIATION("Radiation ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â High radiation levels, dangerous to life."),
        PLASMA("Plasma ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Superheated ionized gas."),
        CRYOGENIC("Cryogenic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Extremely cold, frozen gases."),
        STORM("Storm ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Perpetual storms and lightning."),
        MAGNETIC("Magnetic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Strong electromagnetic field in atmosphere."),
        BIOLOGICAL("Biological ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Living, organic atmosphere."),
        ARTIFICIAL("Artificial ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Maintained by technology."),
        UNKNOWN("Unknown ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Atmosphere composition not yet determined.");

        private final String description;

        AtmosphereType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random atmosphere type.
     * @return a randomly selected atmosphere type
     */
    public AtmosphereType rollAtmosphereType() {
        AtmosphereType[] atmospheres = AtmosphereType.values();
        int index = random.nextInt(atmospheres.length);
        log.debug("Rolled atmosphere type: {}", atmospheres[index]);
        return atmospheres[index];
    }

    /**
     * Population levels for world generation (Appendix III).
     */
    public enum PopulationLevel {
        UNINHABITED("Uninhabited ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â No sentient life."),
        WILDLIFE_ONLY("Wildlife Only ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Non-sentient animal life."),
        PRIMITIVE("Primitive ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Pre-industrial civilization."),
        MEDIEVAL("Medieval ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Early industrial technology."),
        INDUSTRIAL("Industrial ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Steam and early electricity."),
        ATOMIC("Atomic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Nuclear age technology."),
        INTERPLANETARY("Interplanetary ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Colonizing nearby planets."),
        PRE_WARP("Pre-Warp ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Approaching faster-than-light travel."),
        WARP_CAPABLE("Warp Capable ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Has achieved FTL travel."),
        FEDERATION_LEVEL("Federation Level ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Advanced warp civilization."),
        POST_WARP("Post-Warp ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Mature warp civilization."),
        TYPE_II("Type II ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Harnesses stellar energy (Kardashev scale)."),
        TYPE_III("Type III ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Controls galactic energy."),
        ANCIENT("Ancient ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Vastly advanced, possibly precursors."),
        DYING("Dying ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Civilization in decline or collapse."),
        REBUILT("Rebuilt ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Recovering from past destruction."),
        HIDDEN("Hidden ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Deliberately concealing their presence."),
        NOMADIC("Nomadic ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Moving civilization without fixed homes."),
        SUBTERRANEAN("Subterranean ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Lives underground or beneath surface."),
        UNKNOWN("Unknown ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Population level not yet determined.");

        private final String description;

        PopulationLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Rolls for a random population level.
     * @return a randomly selected population level
     */
    public PopulationLevel rollPopulationLevel() {
        PopulationLevel[] levels = PopulationLevel.values();
        int index = random.nextInt(levels.length);
        log.debug("Rolled population level: {}", levels[index]);
        return levels[index];
    }

    /**
     * Generates a complete world profile combining planet type, terrain, atmosphere, and population.
     * @return a map containing all world details
     */
    public Map<String, String> generateWorldProfile() {
        PlanetType planetType = rollPlanetType();
        TerrainType terrain = rollTerrainType();
        AtmosphereType atmosphere = rollAtmosphereType();
        PopulationLevel population = rollPopulationLevel();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("planetType", planetType.name());
        profile.put("planetTypeDescription", planetType.getDescription());
        profile.put("terrain", terrain.name());
        profile.put("terrainDescription", terrain.getDescription());
        profile.put("atmosphere", atmosphere.name());
        profile.put("atmosphereDescription", atmosphere.getDescription());
        profile.put("population", population.name());
        profile.put("populationDescription", population.getDescription());
        return profile;
    }

    /**
     * Generates a complete starship encounter profile.
     * @return a map containing starship class and polity affiliation
     */
    public Map<String, String> generateStarshipEncounter() {
        StarshipClass shipClass = rollStarshipClass();
        AlienPolity polity = rollAlienPolity();

        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("starshipClass", shipClass.name());
        profile.put("starshipDescription", shipClass.getDescription());
        profile.put("affiliation", polity.name());
        profile.put("affiliationDescription", polity.getDescription());
        return profile;
    }

    // ==================== Combined Mission Generation ====================

    /**
     * Generates a complete mission profile by rolling on multiple tables.
     * @return a map containing the mission type, theme, complication, encounter, NPC, and location
     */
    public Map<String, Object> generateMissionProfile() {
        MissionType missionType = rollMissionType();
        MissionTheme theme = rollMissionTheme();
        EncounterType encounter = rollEncounter();
        Map<String, String> npc = generateNpc();
        Map<String, String> location = generateLocation();
        String prompt = generatePromptCard();

        log.info("Generated mission profile: {} / {} at {}", missionType, theme, location.get("name"));

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("missionType", missionType.name());
        profile.put("missionTypeDescription", missionType.getDescription());
        profile.put("theme", theme.name());
        profile.put("themeDescription", theme.getDescription());
        profile.put("encounter", encounter.name());
        profile.put("encounterDescription", encounter.getDescription());
        profile.put("npc", npc);
        profile.put("location", location);
        profile.put("prompt", prompt);
        return profile;
    }
}

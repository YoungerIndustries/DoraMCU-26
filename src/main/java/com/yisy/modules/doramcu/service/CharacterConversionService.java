package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.traveller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service that converts characters between DoraT (Classic Traveller) and Captain's Log formats.
 * <p>
 * Enables cross-galaxy travel by allowing a character created in one game system
 * to be converted for use in another. The conversion maps attributes, skills,
 * and career information between the two systems using reasonable equivalences.
 * </p>
 * <p>
 * Attribute mapping (Traveller 2D6 range 2-12 to Captain's Log 0-5 scale):
 * </p>
 * <ul>
 *   <li>FIT (Fitness) = (END + ST) / 2, normalized to 0-5</li>
 *   <li>TEC (Technical) = INT, normalized to 0-5</li>
 *   <li>STR (Strength) = END, normalized to 0-5</li>
 *   <li>REA (Reason) = INT, normalized to 0-5</li>
 *   <li>INS (Insight) = ST, normalized to 0-5</li>
 *   <li>PRE (Presence) = APP, normalized to 0-5</li>
 * </ul>
 * <p>
 * Skill-to-Discipline mapping:
 * </p>
 * <ul>
 *   <li>COMBAT, STEALTH, SURVIVAL skills increase Security</li>
 *   <li>LEADERSHIP skills increase Command</li>
 *   <li>TECHNICAL, NAVIGATION, SCIENTIFIC skills increase Science</li>
 *   <li>SOCIAL skills increase Leadership</li>
 * </ul>
 */
@Service
public class CharacterConversionService {

    private static final Logger log = LoggerFactory.getLogger(CharacterConversionService.class);

    /**
     * Converts a Traveller ability score (2-12) to a Captain's Log attribute (0-5).
     * <p>
     * Uses linear normalization: (value - 2) / 10 * 5, rounded to nearest integer.
     * </p>
     *
     * @param travellerScore the Traveller ability score (2-12)
     * @return the Captain's Log attribute value (0-5)
     */
    public static int normalizeAttribute(int travellerScore) {
        if (travellerScore < 2) travellerScore = 2;
        if (travellerScore > 12) travellerScore = 12;
        return (int) Math.round((travellerScore - 2.0) / 10.0 * 5.0);
    }

    /**
     * Converts a Captain's Log attribute (0-5) back to a Traveller ability score (2-12).
     * Inverse of normalizeAttribute.
     *
     * @param clAttribute the Captain's Log attribute (0-5)
     * @return the Traveller ability score (2-12)
     */
    public static int denormalizeAttribute(int clAttribute) {
        if (clAttribute < 0) clAttribute = 0;
        if (clAttribute > 5) clAttribute = 5;
        return (int) Math.round(clAttribute / 5.0 * 10.0 + 2.0);
    }

    /**
     * Converts a Traveller character to a Captain's Log character.
     * <p>
     * Maps all abilities, skills, and career information. The returned character
     * is a new instance that is not persisted.
     * </p>
     *
     * @param traveller the Traveller character to convert
     * @return a new Captain's Log character with converted values
     * @throws IllegalArgumentException if the traveller character is null
     */
    public CaptainLogCharacter toCaptainsLog(TravellerCharacter traveller) {
        if (traveller == null) {
            throw new IllegalArgumentException("Traveller character must not be null");
        }

        log.info("Converting Traveller character '{}' to Captain's Log format", traveller.getName());

        CaptainLogCharacter cl = new CaptainLogCharacter();
        cl.setName(traveller.getName());

        // Map abilities to attributes
        int end = traveller.getAbility(AbilityScore.END);
        int st = traveller.getAbility(AbilityScore.ST);
        int intVal = traveller.getAbility(AbilityScore.INT);
        int app = traveller.getAbility(AbilityScore.APP);
        int edu = traveller.getAbility(AbilityScore.EDU);
        int soc = traveller.getAbility(AbilityScore.SOC);

        // FIT = average of END and ST
        cl.setFitness(normalizeAttribute((end + st) / 2));
        // TEC = INT (technical aptitude)
        cl.setTechnical(normalizeAttribute(intVal));
        // STR = END (physical strength)
        cl.setStrength(normalizeAttribute(end));
        // REA = INT (reasoning)
        cl.setReason(normalizeAttribute(intVal));
        // INS = ST (willpower/insight)
        cl.setInsight(normalizeAttribute(st));
        // PRE = APP (presence/charisma)
        cl.setPresence(normalizeAttribute(app));

        // Constitution based on END
        cl.setConstitution(Math.max(1, normalizeAttribute(end)));

        // Map skills to disciplines
        int command = 0;
        int leadership = 0;
        int science = 0;
        int security = 0;

        Set<Skill> skills = traveller.getSkills();
        for (Skill skill : skills) {
            switch (skill.getCategory()) {
                case COMBAT:
                case STEALTH:
                case SURVIVAL:
                    security++;
                    break;
                case LEADERSHIP:
                    command++;
                    break;
                case TECHNICAL:
                case NAVIGATION:
                case SCIENTIFIC:
                    science++;
                    break;
                case SOCIAL:
                    leadership++;
                    break;
                default:
                    science++;
                    break;
            }
        }

        // Set discipline values (capped at 5)
        cl.setCommand(Math.min(5, command));
        cl.setLeadership(Math.min(5, leadership));
        cl.setScience(Math.min(5, science));
        cl.setSecurity(Math.min(5, security));

        // Map career to assignment
        CaptainLogCharacter.Assignment assignment = mapCareerToAssignment(traveller);
        cl.setAssignment(assignment);

        // Map career tours to rank
        CaptainLogCharacter.Rank rank = mapCareerToRank(traveller);
        cl.setRank(rank);

        // Map education
        CaptainLogCharacter.Education education = mapEducation(edu, soc);
        cl.setEducation(education);

        // Map environment based on SOC
        CaptainLogCharacter.Environment environment = mapEnvironment(soc);
        cl.setEnvironment(environment);

        // Map outlook based on ST
        CaptainLogCharacter.Outlook outlook = mapOutlook(st);
        cl.setOutlook(outlook);

        // Set traits from career
        List<String> traits = extractTraits(traveller);
        if (!traits.isEmpty()) cl.setTrait1(traits.get(0));
        if (traits.size() > 1) cl.setTrait2(traits.get(1));
        if (traits.size() > 2) cl.setTrait3(traits.get(2));

        log.info("Conversion complete: '{}' to Captain's Log (rank={}, assignment={})",
                traveller.getName(), rank, assignment);

        return cl;
    }

    /**
     * Converts a Captain's Log character to a Traveller character.
     * <p>
     * The reverse conversion maps attributes back to abilities using the inverse
     * of the normalization formula. Disciplines are mapped back to skills.
     * Note: the returned Traveller character will have abilities set but no
     * career tours, since career tour information is lost in the conversion.
     * </p>
     *
     * @param captainsLog the Captain's Log character to convert
     * @return a new Traveller character with converted values
     * @throws IllegalArgumentException if the captains log character is null
     */
    public TravellerCharacter toTraveller(CaptainLogCharacter captainsLog) {
        if (captainsLog == null) {
            throw new IllegalArgumentException("Captain's Log character must not be null");
        }

        log.info("Converting Captain's Log character '{}' to Traveller format", captainsLog.getName());

        // Create Traveller character with a generated ID
        int id = captainsLog.getId() != null ? captainsLog.getId().intValue() : 1;
        TravellerCharacter traveller = new TravellerCharacter(id, captainsLog.getName());

        // Reverse-map attributes to abilities
        int end = denormalizeAttribute(captainsLog.getStrength());
        int st = denormalizeAttribute(captainsLog.getInsight());
        int intVal = denormalizeAttribute((captainsLog.getReason() + captainsLog.getTechnical()) / 2);
        int app = denormalizeAttribute(captainsLog.getPresence());
        int dex = denormalizeAttribute(captainsLog.getFitness());
        int edu = mapEducationToScore(captainsLog.getEducation());
        int soc = mapEnvironmentToScore(captainsLog.getEnvironment());

        traveller.setAbility(AbilityScore.END, end);
        traveller.setAbility(AbilityScore.ST, st);
        traveller.setAbility(AbilityScore.INT, intVal);
        traveller.setAbility(AbilityScore.APP, app);
        traveller.setAbility(AbilityScore.DEX, dex);
        traveller.setAbility(AbilityScore.EDU, edu);
        traveller.setAbility(AbilityScore.SOC, soc);

        // Map disciplines back to skills
        Set<Skill> skills = mapDisciplinesToSkills(captainsLog);
        for (Skill skill : skills) {
            traveller.addSkill(skill);
        }

        log.info("Conversion complete: '{}' to Traveller (abilities set, skills: {})",
                captainsLog.getName(), skills.size());

        return traveller;
    }

    /**
     * Maps a Traveller skill to a discipline name for logging purposes.
     *
     * @param skill the Traveller skill
     * @return the discipline name string
     */
    public static String mapSkillToDisciplineName(Skill skill) {
        switch (skill.getCategory()) {
            case COMBAT:
            case STEALTH:
            case SURVIVAL:
                return "Security";
            case LEADERSHIP:
                return "Command";
            case TECHNICAL:
            case NAVIGATION:
            case SCIENTIFIC:
                return "Science";
            case SOCIAL:
                return "Leadership";
            default:
                return "Science";
        }
    }

    /**
     * Maps Captain's Log disciplines back to Traveller skills.
     *
     * @param cl the Captain's Log character
     * @return a set of Traveller skills derived from the disciplines
     */
    public static Set<Skill> mapDisciplinesToSkills(CaptainLogCharacter cl) {
        Set<Skill> skills = new HashSet<>();

        // Security to combat and survival skills
        if (cl.getSecurity() > 0) {
            skills.add(Skill.MELEE);
            if (cl.getSecurity() >= 2) skills.add(Skill.FIREARMS);
            if (cl.getSecurity() >= 3) skills.add(Skill.SURVIVAL);
        }

        // Command to leadership skills
        if (cl.getCommand() > 0) {
            skills.add(Skill.LEADERSHIP);
            if (cl.getCommand() >= 2) skills.add(Skill.TACTICS);
        }

        // Science to technical and scientific skills
        if (cl.getScience() > 0) {
            skills.add(Skill.COMPUTERS);
            if (cl.getScience() >= 2) skills.add(Skill.ELECTRONICS);
            if (cl.getScience() >= 3) skills.add(Skill.ASTRONOMY);
            if (cl.getScience() >= 4) skills.add(Skill.NAVIGATION);
        }

        // Leadership to social skills
        if (cl.getLeadership() > 0) {
            skills.add(Skill.LANGUAGE);
            if (cl.getLeadership() >= 2) skills.add(Skill.PSYCHOLOGY);
        }

        return skills;
    }

    // ---- Private mapping helpers ----

    private CaptainLogCharacter.Assignment mapCareerToAssignment(TravellerCharacter traveller) {
        // Count skills by category to determine primary assignment
        Map<Skill.SkillCategory, Long> categoryCount = new EnumMap<>(Skill.SkillCategory.class);
        for (Skill.SkillCategory cat : Skill.SkillCategory.values()) {
            categoryCount.put(cat, 0L);
        }

        for (Skill skill : traveller.getSkills()) {
            categoryCount.merge(skill.getCategory(), 1L, Long::sum);
        }

        long combat = categoryCount.getOrDefault(Skill.SkillCategory.COMBAT, 0L);
        long leadership = categoryCount.getOrDefault(Skill.SkillCategory.LEADERSHIP, 0L);
        long technical = categoryCount.getOrDefault(Skill.SkillCategory.TECHNICAL, 0L);
        long scientific = categoryCount.getOrDefault(Skill.SkillCategory.SCIENTIFIC, 0L);
        long survival = categoryCount.getOrDefault(Skill.SkillCategory.SURVIVAL, 0L);

        long max = Math.max(Math.max(Math.max(combat, leadership), Math.max(technical, scientific)), survival);

        if (combat == max && max > 0) return CaptainLogCharacter.Assignment.SECURITY;
        if (leadership == max && max > 0) return CaptainLogCharacter.Assignment.COMMAND;
        if (technical == max && max > 0) return CaptainLogCharacter.Assignment.ENGINEERING;
        if (scientific == max && max > 0) return CaptainLogCharacter.Assignment.SCIENCE;
        if (survival == max && max > 0) return CaptainLogCharacter.Assignment.MEDICAL;

        // Default based on career tours
        List<TravellerCharacter.CareerTour> tours = traveller.getCareerTours();
        if (!tours.isEmpty()) {
            TravellerCharacter.CareerTour lastTour = tours.get(tours.size() - 1);
            return mapCareerToAssignment(lastTour.getCareer());
        }

        return CaptainLogCharacter.Assignment.OPERATIONS;
    }

    private CaptainLogCharacter.Assignment mapCareerToAssignment(Career career) {
        switch (career) {
            case SOLDIER:
                return CaptainLogCharacter.Assignment.SECURITY;
            case GUNNER:
                return CaptainLogCharacter.Assignment.SECURITY;
            case PILOT:
                return CaptainLogCharacter.Assignment.OPERATIONS;
            case EXPLORER:
                return CaptainLogCharacter.Assignment.SCIENCE;
            case MERCHANT:
                return CaptainLogCharacter.Assignment.OPERATIONS;
            case SCOUT:
                return CaptainLogCharacter.Assignment.SCIENCE;
            case SPY:
                return CaptainLogCharacter.Assignment.SECURITY;
            case NOBLE:
                return CaptainLogCharacter.Assignment.COMMAND;
            default:
                return CaptainLogCharacter.Assignment.OPERATIONS;
        }
    }

    private CaptainLogCharacter.Rank mapCareerToRank(TravellerCharacter traveller) {
        List<TravellerCharacter.CareerTour> tours = traveller.getCareerTours();
        if (tours.isEmpty()) {
            return CaptainLogCharacter.Rank.ENSIGN;
        }

        // Map based on number of tours and highest rank achieved
        int tourCount = tours.size();
        String highestRank = "";
        for (TravellerCharacter.CareerTour tour : tours) {
            String rank = tour.getRank();
            if (rank.compareTo(highestRank) > 0) {
                highestRank = rank;
            }
        }

        // Simple rank mapping based on tour count
        if (tourCount >= 4) return CaptainLogCharacter.Rank.CAPTAIN;
        if (tourCount >= 3) return CaptainLogCharacter.Rank.COMMANDER;
        if (tourCount >= 2) return CaptainLogCharacter.Rank.LIEUTENANT_COMMANDER;
        if (tourCount >= 1) return CaptainLogCharacter.Rank.LIEUTENANT;
        return CaptainLogCharacter.Rank.ENSIGN;
    }

    private CaptainLogCharacter.Education mapEducation(int edu, int soc) {
        int combined = edu + soc;
        if (combined >= 20) return CaptainLogCharacter.Education.ACADEMY;
        if (combined >= 16) return CaptainLogCharacter.Education.ACADEMIC;
        if (combined >= 12) return CaptainLogCharacter.Education.FIELD_TRAINING;
        if (combined >= 8) return CaptainLogCharacter.Education.APPRENTICE;
        return CaptainLogCharacter.Education.SELF_TAUGHT;
    }

    private CaptainLogCharacter.Environment mapEnvironment(int soc) {
        if (soc >= 10) return CaptainLogCharacter.Environment.CITY;
        if (soc >= 7) return CaptainLogCharacter.Environment.COLONY;
        if (soc >= 4) return CaptainLogCharacter.Environment.RURAL;
        return CaptainLogCharacter.Environment.WILD;
    }

    private CaptainLogCharacter.Outlook mapOutlook(int st) {
        if (st >= 10) return CaptainLogCharacter.Outlook.OPTIMISTIC;
        if (st >= 7) return CaptainLogCharacter.Outlook.IDEALISTIC;
        if (st >= 4) return CaptainLogCharacter.Outlook.PRAGMATIC;
        return CaptainLogCharacter.Outlook.PESSIMISTIC;
    }

    private List<String> extractTraits(TravellerCharacter traveller) {
        List<String> traits = new ArrayList<>();

        // Add race as a trait
        if (traveller.getRace() != null && traveller.getRace() != AlienRace.HUMAN) {
            traits.add(traveller.getRace().getDisplayName() + " Origin");
        }

        // Add psionic trait if applicable
        if (traveller.getPsionicAbility() != null) {
            traits.add("Psionic Potential");
        }

        // Add career-based traits
        List<TravellerCharacter.CareerTour> tours = traveller.getCareerTours();
        if (!tours.isEmpty()) {
            TravellerCharacter.CareerTour lastTour = tours.get(tours.size() - 1);
            if (lastTour.isRetired()) {
                traits.add("Retired " + lastTour.getCareer().getDisplayName());
            }
        }

        return traits;
    }

    private int mapEducationToScore(CaptainLogCharacter.Education education) {
        if (education == null) return 7;
        switch (education) {
            case ACADEMY:
                return 10;
            case ACADEMIC:
                return 9;
            case FIELD_TRAINING:
                return 7;
            case MILITARY:
                return 7;
            case APPRENTICE:
                return 5;
            case SELF_TAUGHT:
                return 4;
            default:
                return 7;
        }
    }

    private int mapEnvironmentToScore(CaptainLogCharacter.Environment environment) {
        if (environment == null) return 7;
        switch (environment) {
            case CITY:
                return 10;
            case FEDERATION_WORLD:
                return 9;
            case COLONY:
                return 7;
            case SPACE:
                return 7;
            case NEUTRAL_ZONE:
                return 5;
            case RURAL:
                return 4;
            case WILD:
                return 3;
            default:
                return 7;
        }
    }
}

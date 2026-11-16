package com.yisy.modules.doramcu.corporation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI-driven simulation of character training tours.
 * <p>
 * Generates comprehensive narratives for training tours based on the skill
 * being trained, the profession, and the duration. Uses patterns from
 * MilitaryTourService for battle/conflict events and NarrativeBeatService
 * for 3-act narrative structure (Enlistment, Service, Completion).
 * </p>
 * <p>
 * The simulation is instant — it compresses an entire 1–4 year tour into
 * a single call, generating events, XP awards, and skill improvements.
 * </p>
 */
@Service
public class TrainingSimulationService {

    private static final Logger log = LoggerFactory.getLogger(TrainingSimulationService.class);
    private final Random random = new Random();

    /**
     * Result of a training tour simulation.
     */
    public static class SimulationResult {
        private final String narrative;
        private final List<String> events;
        private final int xpAwarded;
        private final int skillImprovement;
        private final long wealthEarned;

        public SimulationResult(String narrative, List<String> events, int xpAwarded,
                                int skillImprovement, long wealthEarned) {
            this.narrative = narrative;
            this.events = events;
            this.xpAwarded = xpAwarded;
            this.skillImprovement = skillImprovement;
            this.wealthEarned = wealthEarned;
        }

        public String getNarrative() {
            return narrative;
        }

        public List<String> getEvents() {
            return events;
        }

        public int getXpAwarded() {
            return xpAwarded;
        }

        public int getSkillImprovement() {
            return skillImprovement;
        }

        public long getWealthEarned() {
            return wealthEarned;
        }
    }

    /**
     * Simulates an entire training tour immediately.
     * <p>
     * Generates events based on the skill and profession, uses patterns from
     * MilitaryTourService for battle/conflict events, and NarrativeBeatService
     * for narrative structure (Act 1: Enlistment, Act 2: Service, Act 3: Completion).
     * </p>
     *
     * @param skill the skill being trained (e.g., "COMBAT", "NEGOTIATION", etc.)
     * @param profession the profession/career being trained (e.g., "Army", "Navy", "Merchant Service")
     * @param durationYears the number of years in the tour (1–4)
     * @return the simulation result with narrative, events, XP, and skill improvements
     */
    public SimulationResult simulateTour(String skill, String profession, int durationYears) {
        log.info("Simulating training tour: skill={}, profession={}, durationYears={}", skill, profession, durationYears);

        List<String> events = new ArrayList<>();
        StringBuilder narrative = new StringBuilder();

        // Act 1: Enlistment
        narrative.append(generateEnlistmentAct(skill, profession));
        events.add("Enlisted in " + profession + " for " + skill + " training");

        // Act 2: Service — generate events based on skill type
        String serviceNarrative;
        switch (skill.toUpperCase()) {
            case "COMBAT":
                serviceNarrative = generateCombatNarrative(profession, durationYears, events);
                break;
            case "NEGOTIATION":
                serviceNarrative = generateDiplomaticNarrative(profession, durationYears, events);
                break;
            case "RESEARCH":
                serviceNarrative = generateResearchNarrative(profession, durationYears, events);
                break;
            case "LOGISTICS":
                serviceNarrative = generateLogisticsNarrative(profession, durationYears, events);
                break;
            case "GOVERNANCE":
                serviceNarrative = generateGovernanceNarrative(profession, durationYears, events);
                break;
            default:
                serviceNarrative = generateGenericNarrative(skill, profession, durationYears, events);
                break;
        }
        narrative.append(serviceNarrative);

        // Act 3: Completion
        narrative.append(generateCompletionAct(skill, profession, durationYears, events));

        // Calculate rewards
        int xpAwarded = calculateXp(skill, durationYears);
        int skillImprovement = calculateSkillImprovement(durationYears);
        long wealthEarned = calculateWealth(skill, durationYears);

        return new SimulationResult(narrative.toString(), events, xpAwarded, skillImprovement, wealthEarned);
    }

    /**
     * Act 1: Enlistment narrative.
     */
    private String generateEnlistmentAct(String skill, String profession) {
        String[] openings = {
            "The character reported for duty with the %s, beginning a rigorous training program focused on %s.",
            "Assigned to the %s, the character embarked on an intensive course of %s instruction.",
            "The character joined the ranks of the %s, where mastery of %s would be the defining challenge.",
            "With high expectations, the character entered the %s to hone their %s abilities."
        };
        return String.format(openings[random.nextInt(openings.length)], profession, skill.toLowerCase()) + " ";
    }

    /**
     * Act 2: Combat-focused service narrative.
     */
    private String generateCombatNarrative(String profession, int durationYears, List<String> events) {
        StringBuilder sb = new StringBuilder();
        String[] battles = {
            "the Battle of the Outer Rim", "the Siege of Kharad", "the Skirmish at Veltor Station",
            "the Defense of Core World Prime", "the Raid on the Pirate Haven", "the Assault on the Rebel Stronghold",
            "the Engagement at the Nebula Frontier", "the Counterattack at Sector 7",
            "the Ambush in the Asteroid Belt", "the Final Stand at the Gateway"
        };
        String[] outcomes = {
            "emerged victorious despite heavy losses", "held the line against overwhelming odds",
            "turned the tide through brilliant tactical maneuvering", "sustained casualties but achieved the objective",
            "demonstrated exceptional courage under fire", "outmaneuvered the enemy with precision strikes"
        };
        String[] injuries = {
            "was wounded but recovered", "sustained minor injuries", "showed remarkable resilience",
            "earned a commendation for bravery", "was decorated for valor"
        };

        int numEvents = durationYears * 2 + random.nextInt(2);
        for (int i = 0; i < numEvents; i++) {
            String battle = battles[random.nextInt(battles.length)];
            String outcome = outcomes[random.nextInt(outcomes.length)];
            sb.append("During ").append(battle).append(", the character ").append(outcome).append(". ");
            events.add("Participated in " + battle + ": " + outcome);
        }

        if (random.nextDouble() < 0.5) {
            String injury = injuries[random.nextInt(injuries.length)];
            sb.append("At one point, the character ").append(injury).append(". ");
            events.add("Combat injury: " + injury);
        }

        return sb.toString();
    }

    /**
     * Act 2: Diplomacy-focused service narrative.
     */
    private String generateDiplomaticNarrative(String profession, int durationYears, List<String> events) {
        StringBuilder sb = new StringBuilder();
        String[] missions = {
            "a peace negotiation with the Kharad Confederacy", "trade talks with the Outer Rim Alliance",
            "a diplomatic summit at Veltor Station", "crisis mediation between rival factions",
            "an envoy mission to the Core World Council", "a first contact protocol exercise",
            "a hostage negotiation in the Asteroid Belt", "a treaty ratification ceremony",
            "an intelligence gathering operation in hostile territory", "a cultural exchange program"
        };
        String[] outcomes = {
            "successfully brokered an agreement", "navigated complex political tensions with grace",
            "prevented an escalating conflict through careful diplomacy", "secured favorable terms for the corporation",
            "built lasting relationships with key stakeholders", "uncovered critical intelligence through dialogue"
        };

        int numEvents = durationYears * 2 + random.nextInt(2);
        for (int i = 0; i < numEvents; i++) {
            String mission = missions[random.nextInt(missions.length)];
            String outcome = outcomes[random.nextInt(outcomes.length)];
            sb.append("During ").append(mission).append(", the character ").append(outcome).append(". ");
            events.add("Diplomatic mission: " + mission + " — " + outcome);
        }

        return sb.toString();
    }

    /**
     * Act 2: Research-focused service narrative.
     */
    private String generateResearchNarrative(String profession, int durationYears, List<String> events) {
        StringBuilder sb = new StringBuilder();
        String[] discoveries = {
            "a new propulsion technique", "an anomaly in the Nebula Frontier",
            "ancient alien technology on a derelict world", "a breakthrough in shield harmonics",
            "a previously unknown mineral deposit", "a medical treatment for radiation exposure",
            "a communication protocol for deep-space relay", "a navigation shortcut through the asteroid belt",
            "a weapon system vulnerability", "a sustainable life-support method"
        };
        String[] outcomes = {
            "published findings that advanced the field", "filed a patent for the corporation",
            "shared results with allied research teams", "demonstrated the discovery to senior leadership",
            "authored a comprehensive report on the findings", "presented at an interstellar science symposium"
        };

        int numEvents = durationYears * 2 + random.nextInt(2);
        for (int i = 0; i < numEvents; i++) {
            String discovery = discoveries[random.nextInt(discoveries.length)];
            String outcome = outcomes[random.nextInt(outcomes.length)];
            sb.append("The character discovered ").append(discovery).append(" and ").append(outcome).append(". ");
            events.add("Research discovery: " + discovery + " — " + outcome);
        }

        return sb.toString();
    }

    /**
     * Act 2: Logistics-focused service narrative.
     */
    private String generateLogisticsNarrative(String profession, int durationYears, List<String> events) {
        StringBuilder sb = new StringBuilder();
        String[] operations = {
            "a supply convoy through hostile territory", "a resource extraction operation on a mining world",
            "a fleet resupply mission at Veltor Station", "an emergency evacuation from a war zone",
            "a trade route establishment in the Outer Rim", "a warehouse management overhaul",
            "a cross-sector transport coordination", "a ration distribution during a blockade",
            "a vehicle fleet maintenance program", "a cargo security audit"
        };
        String[] outcomes = {
            "ensured timely delivery despite challenges", "optimized the supply chain for maximum efficiency",
            "prevented a critical shortage through quick thinking", "reduced costs while improving reliability",
            "coordinated multiple teams across sectors", "maintained operations under extreme pressure"
        };

        int numEvents = durationYears * 2 + random.nextInt(2);
        for (int i = 0; i < numEvents; i++) {
            String operation = operations[random.nextInt(operations.length)];
            String outcome = outcomes[random.nextInt(outcomes.length)];
            sb.append("During ").append(operation).append(", the character ").append(outcome).append(". ");
            events.add("Logistics operation: " + operation + " — " + outcome);
        }

        return sb.toString();
    }

    /**
     * Act 2: Governance-focused service narrative.
     */
    private String generateGovernanceNarrative(String profession, int durationYears, List<String> events) {
        StringBuilder sb = new StringBuilder();
        String[] challenges = {
            "a sector-wide budget crisis", "a labor dispute at a major facility",
            "a regulatory compliance audit", "a public relations emergency",
            "a strategic planning session for expansion", "a conflict resolution between departments",
            "a policy reform initiative", "a stakeholder engagement program",
            "a crisis management drill", "a corporate governance review"
        };
        String[] outcomes = {
            "resolved the issue through decisive leadership", "implemented reforms that improved morale",
            "balanced competing interests with fairness", "delivered results ahead of schedule",
            "earned the trust of senior management", "set a precedent for future operations"
        };

        int numEvents = durationYears * 2 + random.nextInt(2);
        for (int i = 0; i < numEvents; i++) {
            String challenge = challenges[random.nextInt(challenges.length)];
            String outcome = outcomes[random.nextInt(outcomes.length)];
            sb.append("Faced with ").append(challenge).append(", the character ").append(outcome).append(". ");
            events.add("Governance challenge: " + challenge + " — " + outcome);
        }

        return sb.toString();
    }

    /**
     * Act 2: Generic service narrative for skills not in the main catalog.
     */
    private String generateGenericNarrative(String skill, String profession, int durationYears, List<String> events) {
        StringBuilder sb = new StringBuilder();
        String[] activities = {
            "intensive training exercises", "field operations", "specialized assignments",
            "cross-functional projects", "mentorship programs", "performance evaluations",
            "advanced certification courses", "practical application scenarios"
        };
        String[] outcomes = {
            "demonstrated exceptional proficiency", "exceeded all performance benchmarks",
            "received commendations from superiors", "showed rapid skill development",
            "contributed to team success", "earned recognition for outstanding work"
        };

        int numEvents = durationYears * 2 + random.nextInt(2);
        for (int i = 0; i < numEvents; i++) {
            String activity = activities[random.nextInt(activities.length)];
            String outcome = outcomes[random.nextInt(outcomes.length)];
            sb.append("During ").append(activity).append(", the character ").append(outcome).append(" in ").append(skill.toLowerCase()).append(". ");
            events.add("Training: " + activity + " — " + outcome);
        }

        return sb.toString();
    }

    /**
     * Act 3: Completion narrative.
     */
    private String generateCompletionAct(String skill, String profession, int durationYears, List<String> events) {
        String[] closings = {
            "After %d years of service with the %s, the character completed their training in %s with distinction, having grown significantly in skill and maturity.",
            "The character's %d-year tour with the %s concluded successfully. Their mastery of %s was evident in every assessment.",
            "Having served %d years in the %s, the character emerged as a seasoned professional in %s, ready for greater challenges.",
            "The %d-year training tour in the %s shaped the character into a capable %s specialist, prepared for the demands of future assignments."
        };
        String closing = String.format(closings[random.nextInt(closings.length)],
                durationYears, profession, skill.toLowerCase());
        events.add("Tour completed: " + durationYears + " years in " + profession);
        return closing;
    }

    /**
     * Calculate XP awarded based on skill and duration.
     */
    int calculateXp(String skill, int durationYears) {
        int baseXp = 50;
        int skillMultiplier = switch (skill.toUpperCase()) {
            case "COMBAT" -> 120;
            case "NEGOTIATION" -> 100;
            case "RESEARCH" -> 110;
            case "LOGISTICS" -> 90;
            case "GOVERNANCE" -> 100;
            default -> 80;
        };
        return baseXp + (skillMultiplier * durationYears) + random.nextInt(30 * durationYears);
    }

    /**
     * Calculate skill improvement based on duration.
     */
    int calculateSkillImprovement(int durationYears) {
        return Math.min(durationYears, 3) + (random.nextDouble() < 0.3 ? 1 : 0);
    }

    /**
     * Calculate wealth earned during the tour.
     */
    long calculateWealth(String skill, int durationYears) {
        long baseSalary = switch (skill.toUpperCase()) {
            case "COMBAT" -> 5000L;
            case "NEGOTIATION" -> 4000L;
            case "RESEARCH" -> 4500L;
            case "LOGISTICS" -> 3500L;
            case "GOVERNANCE" -> 6000L;
            default -> 3000L;
        };
        return baseSalary * durationYears + random.nextInt(2000 * durationYears);
    }
}

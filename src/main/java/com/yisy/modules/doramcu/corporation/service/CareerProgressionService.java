package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.model.misc.CareerLevel;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.model.HireSkill;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service that manages AI hire career progression.
 * <p>
 * Handles XP awarding from contract work, automatic level-ups, skill development,
 * and manual promotion/demotion. Each position has a primary skill that develops
 * through contract work, with matching contract types giving 2x XP to that skill.
 * </p>
 */
@Service("careerProgressionService_corporation_service_CareerProgressionService")
    @Service
public class CareerProgressionService {

    private static final Logger log = LoggerFactory.getLogger(CareerProgressionService.class);

    /** XP needed per skill level increase */
    static final int XP_PER_SKILL_LEVEL = 50;

    /** Maximum skill level */
    static final int MAX_SKILL_LEVEL = 10;

    /** Minimum skill level */
    static final int MIN_SKILL_LEVEL = 1;

    /** Performance bonus per skill level above 5 */
    static final double PERFORMANCE_BONUS_PER_SKILL_ABOVE_5 = 0.5;

    private final CorporateHiringRepository hiringRepository;
    private final AiCharacterRepository aiCharacterRepository;

    public CareerProgressionService(CorporateHiringRepository hiringRepository, AiCharacterRepository aiCharacterRepository) {
        this.hiringRepository = hiringRepository;
        this.aiCharacterRepository = aiCharacterRepository;
    }

    /**
     * Awards XP to a hire for completing a contract.
     * <p>
     * Updates the hire's total XP, increases the primary skill level if the contract
     * type matches the position's primary skill, and checks for automatic level-up.
     * </p>
     *
     * @param hiringId    the hire to award XP to
     * @param xp          the base XP amount
     * @param contractType the contract type for skill matching
     * @return the updated hire, or empty if not found
     */
    @Transactional
    public Optional<CorporateHiring> awardXp(Long hiringId, int xp, CorporateContract.ContractType contractType) {
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        if (hireOpt.isEmpty()) {
            return Optional.empty();
        }

        CorporateHiring hire = hireOpt.get();
        if (!hire.isActive()) {
            log.warn("Cannot award XP to inactive hire {}", hiringId);
            return Optional.empty();
        }

        // Award total XP
        int currentXp = hire.getXpEarned() != null ? hire.getXpEarned() : 0;
        hire.setXpEarned(currentXp + xp);

        // Determine primary skill for this position
        CorporatePosition position = hire.getPosition();
        HireSkill primarySkill = position.getPrimarySkill();
        String skillName = primarySkill.name();

        // Check if contract type matches the primary skill
        boolean isMatchingSkill = primarySkill.getPreferredContractTypes().contains(contractType);
        int skillXp = isMatchingSkill ? xp * 2 : xp; // 2x XP for matching skills

        // Update skill level
        int currentSkillLevel = hire.getSkillLevel(skillName);
        int skillLevelIncrease = skillXp / XP_PER_SKILL_LEVEL;
        if (skillLevelIncrease > 0) {
            int newSkillLevel = Math.min(MAX_SKILL_LEVEL, currentSkillLevel + skillLevelIncrease);
            hire.setSkillLevel(skillName, newSkillLevel);
            log.debug("Hire {} skill {} increased from {} to {} (contract type {})",
                    hiringId, skillName, currentSkillLevel, newSkillLevel, contractType);
        }

        // Check for automatic level-up
        checkAndLevelUp(hiringId);

        log.info("Awarded {} XP to hire {} (total XP: {}, skill {}: {})",
                xp, hiringId, hire.getXpEarned(), skillName, hire.getSkillLevel(skillName));

        return Optional.of(hire);
    }

    /**
     * Checks if a hire meets the threshold for the next level and promotes automatically.
     *
     * @param hiringId the hire to check
     * @return the updated hire, or empty if not found
     */
    @Transactional
    public Optional<CorporateHiring> checkAndLevelUp(Long hiringId) {
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        if (hireOpt.isEmpty()) {
            return Optional.empty();
        }

        CorporateHiring hire = hireOpt.get();
        if (hire.isEligibleForPromotion()) {
            boolean promoted = hire.promote();
            if (promoted) {
                hiringRepository.save(hire);
                log.info("Hire {} auto-promoted to {} (XP: {})",
                        hiringId, hire.getCareerLevel(), hire.getXpEarned());
            }
        }

        return Optional.of(hire);
    }

    /**
     * Manually promotes a hire to the next career level.
     * Requires meeting the XP threshold.
     *
     * @param hiringId the hire to promote
     * @return the updated hire, or empty if not found or not eligible
     */
    @Transactional
    public Optional<CorporateHiring> promote(Long hiringId) {
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        if (hireOpt.isEmpty()) {
            return Optional.empty();
        }

        CorporateHiring hire = hireOpt.get();
        if (!hire.isEligibleForPromotion()) {
            log.warn("Hire {} is not eligible for promotion (level: {}, XP: {}, threshold: {})",
                    hiringId, hire.getCareerLevel(), hire.getXpEarned(), hire.getLevelThreshold());
            return Optional.empty();
        }

        hire.promote();
        hiringRepository.save(hire);
        log.info("Hire {} manually promoted to {} (salary: {} -> {})",
                hiringId, hire.getCareerLevel(), hire.getDailySalary(), hire.getEffectiveSalary());
        return Optional.of(hire);
    }

    /**
     * Manually demotes a hire to the previous career level.
     * Reduces salary but has no XP requirements.
     *
     * @param hiringId the hire to demote
     * @return the updated hire, or empty if not found or already at minimum
     */
    @Transactional
    public Optional<CorporateHiring> demote(Long hiringId) {
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        if (hireOpt.isEmpty()) {
            return Optional.empty();
        }

        CorporateHiring hire = hireOpt.get();
        if (hire.getCareerLevel() == CareerLevel.JUNIOR) {
            log.warn("Hire {} is already at minimum level (JUNIOR)", hiringId);
            return Optional.empty();
        }

        hire.demote();
        hiringRepository.save(hire);
        log.info("Hire {} manually demoted to {} (salary: {} -> {})",
                hiringId, hire.getCareerLevel(), hire.getDailySalary(), hire.getEffectiveSalary());
        return Optional.of(hire);
    }

    /**
     * Returns detailed career information for a hire.
     *
     * @param hiringId the hire to query
     * @return a map with career details, or empty if not found
     */
    public Optional<Map<String, Object>> getCareerDetails(Long hiringId) {
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        if (hireOpt.isEmpty()) {
            return Optional.empty();
        }

        CorporateHiring hire = hireOpt.get();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("hiringId", hire.getId());
        details.put("position", hire.getPosition().name());
        details.put("positionDisplayName", hire.getPosition().getDisplayName());
        details.put("careerLevel", hire.getCareerLevel().name());
        details.put("careerLevelDisplayName", hire.getCareerLevel().name());
        details.put("xpEarned", hire.getXpEarned());
        details.put("nextLevelThreshold", hire.getLevelThreshold());
        details.put("eligibleForPromotion", hire.isEligibleForPromotion());
        details.put("skills", new HashMap<>(hire.getSkills()));
        details.put("primarySkill", hire.getPosition().getPrimarySkill().name());
        details.put("primarySkillLevel", hire.getSkillLevel(hire.getPosition().getPrimarySkill().name()));
        details.put("salaryMultiplier", hire.getCareerLevel().getSalaryMultiplier());
        details.put("baseSalary", hire.getDailySalary());
        details.put("effectiveSalary", hire.getEffectiveSalary());
        details.put("performanceRating", hire.getPerformanceRating());

        // Look up the AI character name
        Optional<AiCharacter> aiCharacter = aiCharacterRepository.findById(hire.getAiCharacterId());
        details.put("aiCharacterName", aiCharacter.map(AiCharacter::getName).orElse("Unknown"));

        return Optional.of(details);
    }

    /**
     * Calculates the effective performance rating for a hire on a specific contract type.
     * <p>
     * Formula: basePerformance + (skillLevel - 5) * 0.5 for matching skills.
     * Skills above level 5 provide a bonus; skills below 5 provide no penalty.
     * </p>
     *
     * @param hire         the hire
     * @param contractType the contract type
     * @return the effective performance rating
     */
    public double calculateEffectivePerformance(CorporateHiring hire, CorporateContract.ContractType contractType) {
        double basePerformance = hire.getPerformanceRating() != null ? hire.getPerformanceRating() : 5.0;

        // Get primary skill for the position
        HireSkill primarySkill = hire.getPosition().getPrimarySkill();
        String skillName = primarySkill.name();
        int skillLevel = hire.getSkillLevel(skillName);

        // Check if the contract type matches the primary skill
        boolean isMatchingSkill = primarySkill.getPreferredContractTypes().contains(contractType);

        double effectivePerformance = basePerformance;
        if (isMatchingSkill && skillLevel > 5) {
            effectivePerformance += (skillLevel - 5) * PERFORMANCE_BONUS_PER_SKILL_ABOVE_5;
        }

        return effectivePerformance;
    }
}

package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.eve.model.AiCommanderHiring;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.AiCommanderExperienceRepository;
import com.yisy.modules.doramcu.repository.AiCommanderHiringRepository;
import com.yisy.modules.doramcu.sfb.model.SfbScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing AI commander experience tracking and hiring.
 * <p>
 * This service provides methods to record battle experience, calculate
 * experience-based improvements, and handle the hiring of AI commanders
 * by players. It ensures that AI commanders accumulate experience across
 * battles and game modules, making them more valuable over time.
 * </p>
 */
@Service
public class AiCommanderExperienceService {

    private static final Logger log = LoggerFactory.getLogger(AiCommanderExperienceService.class);

    private final AiCharacterRepository aiCharacterRepository;
    private final AiCommanderExperienceRepository experienceRepository;
    private final AiCommanderHiringRepository hiringRepository;

    public AiCommanderExperienceService(
            AiCharacterRepository aiCharacterRepository,
            AiCommanderExperienceRepository experienceRepository,
            AiCommanderHiringRepository hiringRepository) {
        this.aiCharacterRepository = aiCharacterRepository;
        this.experienceRepository = experienceRepository;
        this.hiringRepository = hiringRepository;
    }

    // ---- Experience Recording ----

    /**
     * Record experience from an SFB battle scenario for an AI commander.
     *
     * @param aiCharacterId the AI character ID
     * @param scenario the completed SFB battle scenario
     */
    @Transactional
    public void recordExperience(Long aiCharacterId, SfbScenario scenario) {
        if (scenario == null) {
            return;
        }
        // Determine outcome based on scenario state
        AiCommanderExperience.Outcome outcome;
        if (scenario.isBattleOver()) {
            // Simple heuristic: check which side has more ships remaining
            int side0Ships = (int) scenario.getShipsBySide(0).stream().filter(s -> !s.isDestroyed()).count();
            int side1Ships = (int) scenario.getShipsBySide(1).stream().filter(s -> !s.isDestroyed()).count();
            if (side0Ships > side1Ships) {
                outcome = AiCommanderExperience.Outcome.VICTORY;
            } else if (side1Ships > side0Ships) {
                outcome = AiCommanderExperience.Outcome.DEFEAT;
            } else {
                outcome = AiCommanderExperience.Outcome.STALEMATE;
            }
        } else {
            outcome = AiCommanderExperience.Outcome.STALEMATE;
        }
        recordBattleExperience(aiCharacterId, "SFB", "Starship Battle", outcome,
                "SFB Battle: " + scenario.getName(), scenario.getName());
    }

    /**
     * Record a battle experience for an AI commander.
     *
     * @param aiCharacterId the AI character ID
     * @param gameModuleName the game module (e.g., "THIRD_REICH", "TRAVELLER")
     * @param battleType the type of battle
     * @param outcome the battle outcome
     * @return the saved experience record
     */
    @Transactional
    public AiCommanderExperience recordBattleExperience(
            Long aiCharacterId, String gameModuleName, String battleType,
            AiCommanderExperience.Outcome outcome) {
        return recordBattleExperience(aiCharacterId, gameModuleName, battleType, outcome, null, null);
    }

    /**
     * Record a detailed battle experience for an AI commander.
     *
     * @param aiCharacterId the AI character ID
     * @param gameModuleName the game module
     * @param battleType the type of battle
     * @param outcome the battle outcome
     * @param description battle description
     * @param location battle location
     * @return the saved experience record
     */
    @Transactional
    public AiCommanderExperience recordBattleExperience(
            Long aiCharacterId, String gameModuleName, String battleType,
            AiCommanderExperience.Outcome outcome, String description, String location) {
        
        AiCharacter character = aiCharacterRepository.findById(aiCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("AI character not found: " + aiCharacterId));

        AiCommanderExperience exp = new AiCommanderExperience(aiCharacterId, gameModuleName, battleType, outcome);
        exp.setBattleDescription(description);
        exp.setBattleLocation(location);

        // Calculate experience points based on outcome
        int xp = calculateExperiencePoints(outcome);
        exp.setExperiencePoints(xp);
        exp.setLeadershipBonus(calculateLeadershipBonus(outcome));

        // Update character's accumulated experience
        character.addExperience(xp);
        if (outcome == AiCommanderExperience.Outcome.VICTORY || 
            outcome == AiCommanderExperience.Outcome.PYRRHIC_VICTORY) {
            character.recordVictory();
        } else {
            character.recordDefeat();
        }
        aiCharacterRepository.save(character);

        AiCommanderExperience saved = experienceRepository.save(exp);
        log.info("Recorded battle experience for {}: {} ({} XP)", character.getName(), outcome, xp);
        return saved;
    }

    /**
     * Record a battle experience with detailed stats.
     *
     * @param aiCharacterId the AI character ID
     * @param gameModuleName the game module
     * @param battleType the type of battle
     * @param outcome the battle outcome
     * @param description battle description
     * @param location battle location
     * @param enemiesDefeated number of enemies defeated
     * @param friendlyLosses number of friendly losses
     * @param skillImproved skill that improved
     * @param exceptional whether performance was exceptional
     * @return the saved experience record
     */
    @Transactional
    public AiCommanderExperience recordDetailedBattleExperience(
            Long aiCharacterId, String gameModuleName, String battleType,
            AiCommanderExperience.Outcome outcome, String description, String location,
            int enemiesDefeated, int friendlyLosses, String skillImproved, boolean exceptional) {
        
        AiCommanderExperience exp = recordBattleExperience(aiCharacterId, gameModuleName, battleType, outcome, description, location);
        exp.setEnemiesDefeated(enemiesDefeated);
        exp.setFriendlyLosses(friendlyLosses);
        exp.setSkillImproved(skillImproved);
        exp.setExceptionalPerformance(exceptional);

        // Bonus XP for exceptional performance
        if (exceptional) {
            int bonusXp = exp.getExperiencePoints() / 2;
            exp.setExperiencePoints(exp.getExperiencePoints() + bonusXp);
        }

        return experienceRepository.save(exp);
    }

    // ---- Experience Queries ----

    /**
     * Get all battle experiences for an AI commander.
     */
    @Transactional(readOnly = true)
    public List<AiCommanderExperience> getExperiences(Long aiCharacterId) {
        return experienceRepository.findByAiCharacterIdOrderByBattleDateDesc(aiCharacterId);
    }

    /**
     * Get total experience points for an AI commander.
     */
    @Transactional(readOnly = true)
    public int getTotalExperience(Long aiCharacterId) {
        return experienceRepository.sumExperiencePointsByAiCharacterId(aiCharacterId);
    }

    /**
     * Get battle count for an AI commander.
     */
    @Transactional(readOnly = true)
    public long getBattleCount(Long aiCharacterId) {
        return experienceRepository.countByAiCharacterId(aiCharacterId);
    }

    // ---- Hiring ----

    /**
     * Get all available (hirable) AI commanders.
     */
    @Transactional(readOnly = true)
    public List<AiCharacter> getAvailableCommanders() {
        return aiCharacterRepository.findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
    }

    /**
     * Get available commanders with minimum experience.
     */
    @Transactional(readOnly = true)
    public List<AiCharacter> getAvailableCommanders(int minExperience) {
        return aiCharacterRepository.findByTotalExperienceGreaterThanEqualAndHirableTrue(minExperience);
    }

    /**
     * Hire an AI commander for a player.
     *
     * @param aiCharacterId the AI character to hire
     * @param playerId the player hiring the commander
     * @param contractDurationDays contract duration in days (0 for indefinite)
     * @return the hiring contract
     */
    @Transactional
    public AiCommanderHiring hireCommander(Long aiCharacterId, String playerId, int contractDurationDays) {
        AiCharacter character = aiCharacterRepository.findById(aiCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("AI character not found: " + aiCharacterId));

        if (!character.isHirable()) {
            throw new IllegalStateException("AI character is not hirable: " + character.getName());
        }

        if (character.getHiringStatus() != AiCharacter.HiringStatus.AVAILABLE) {
            throw new IllegalStateException("AI character is not available for hiring: " + character.getName());
        }

        // Check if already hired
        if (hiringRepository.existsByAiCharacterIdAndStatus(aiCharacterId, AiCommanderHiring.Status.ACTIVE)) {
            throw new IllegalStateException("AI character is already hired: " + character.getName());
        }

        // Create hiring contract
        AiCommanderHiring hiring = new AiCommanderHiring(aiCharacterId, playerId, character.getDailySalary());
        hiring.setContractDurationDays(contractDurationDays);
        if (contractDurationDays > 0) {
            hiring.setExpiryDate(LocalDateTime.now().plusDays(contractDurationDays));
        }

        // Update character status
        character.setHiringStatus(AiCharacter.HiringStatus.HIRED);
        aiCharacterRepository.save(character);

        AiCommanderHiring saved = hiringRepository.save(hiring);
        log.info("Player {} hired AI commander {} (salary: {})", playerId, character.getName(), character.getDailySalary());
        return saved;
    }

    /**
     * Release (fire) an AI commander.
     *
     * @param aiCharacterId the AI character to release
     * @return the updated hiring contract
     */
    @Transactional
    public AiCommanderHiring releaseCommander(Long aiCharacterId) {
        Optional<AiCommanderHiring> hiringOpt = hiringRepository
                .findByAiCharacterIdAndStatus(aiCharacterId, AiCommanderHiring.Status.ACTIVE);
        
        if (hiringOpt.isEmpty()) {
            throw new IllegalStateException("AI commander is not currently hired: " + aiCharacterId);
        }

        AiCommanderHiring hiring = hiringOpt.get();
        hiring.setStatus(AiCommanderHiring.Status.EXPIRED);
        hiringRepository.save(hiring);

        // Update character status
        AiCharacter character = aiCharacterRepository.findById(aiCharacterId)
                .orElseThrow(() -> new IllegalArgumentException("AI character not found: " + aiCharacterId));
        character.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
        aiCharacterRepository.save(character);

        log.info("Released AI commander {} from player {}", character.getName(), hiring.getPlayerId());
        return hiring;
    }

    /**
     * Get active hiring contract for an AI character.
     */
    @Transactional(readOnly = true)
    public Optional<AiCommanderHiring> getActiveHiring(Long aiCharacterId) {
        return hiringRepository.findByAiCharacterIdAndStatus(aiCharacterId, AiCommanderHiring.Status.ACTIVE);
    }

    /**
     * Get all active hiring contracts for a player.
     */
    @Transactional(readOnly = true)
    public List<AiCommanderHiring> getPlayerHiredCommanders(String playerId) {
        return hiringRepository.findByPlayerIdAndStatus(playerId, AiCommanderHiring.Status.ACTIVE);
    }

    /**
     * Record a battle result for a hired commander.
     */
    @Transactional
    public void recordHiredCommanderBattle(Long aiCharacterId, boolean victory) {
        Optional<AiCommanderHiring> hiringOpt = hiringRepository
                .findByAiCharacterIdAndStatus(aiCharacterId, AiCommanderHiring.Status.ACTIVE);
        
        if (hiringOpt.isPresent()) {
            AiCommanderHiring hiring = hiringOpt.get();
            hiring.setBattlesFought(hiring.getBattlesFought() + 1);
            if (victory) {
                hiring.setVictories(hiring.getVictories() + 1);
            }
            // Update performance rating
            int battles = hiring.getBattlesFought();
            int victories = hiring.getVictories();
            double rating = battles > 0 ? (victories / (double) battles) * 10.0 : 5.0;
            hiring.setPerformanceRating(rating);
            hiringRepository.save(hiring);
        }
    }

    // ---- Experience Calculation ----

    /**
     * Calculate experience points based on battle outcome.
     */
    int calculateExperiencePoints(AiCommanderExperience.Outcome outcome) {
        switch (outcome) {
            case VICTORY: return 50;
            case PYRRHIC_VICTORY: return 30;
            case STALEMATE: return 15;
            case DEFEAT: return 10;
            case ROUTE: return 5;
            default: return 0;
        }
    }

    /**
     * Calculate leadership bonus based on battle outcome.
     */
    int calculateLeadershipBonus(AiCommanderExperience.Outcome outcome) {
        switch (outcome) {
            case VICTORY: return 1;
            case PYRRHIC_VICTORY: return 0;
            case STALEMATE: return 0;
            case DEFEAT: return -1;
            case ROUTE: return -2;
            default: return 0;
        }
    }
}

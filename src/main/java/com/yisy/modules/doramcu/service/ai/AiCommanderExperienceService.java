package com.yisy.modules.doramcu.service.ai;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.hex.PlanetFacility;
import com.yisy.modules.doramcu.model.misc.CharacterType;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.eve.model.AiCommanderHiring;
import com.yisy.modules.doramcu.hex.AiCoreComponent;
import com.yisy.modules.doramcu.hex.ArmyUnit;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.manager.GameContext;
import com.yisy.modules.doramcu.hex.spi.AiCommanderExperienceSpi;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.ai.AiCommanderExperienceRepository;
import com.yisy.modules.doramcu.repository.ai.AiCommanderHiringRepository;
import com.yisy.modules.doramcu.sfb.model.SfbScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

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
public class AiCommanderExperienceService implements AiCommanderExperienceSpi {

    private static final Logger log = LoggerFactory.getLogger(AiCommanderExperienceService.class);

    private final AiCharacterRepository aiCharacterRepository;
    private final AiCommanderExperienceRepository experienceRepository;
    private final AiCommanderHiringRepository hiringRepository;

    /** Optional: GameContext for looking up entities by ID to sync AI Core tier. */
    @Autowired(required = false)
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    private GameContext gameContext;

    public AiCommanderExperienceService(
            AiCharacterRepository aiCharacterRepository,
            AiCommanderExperienceRepository experienceRepository,
            AiCommanderHiringRepository hiringRepository) {
        this.aiCharacterRepository = aiCharacterRepository;
        this.experienceRepository = experienceRepository;
        this.hiringRepository = hiringRepository;
    }

    /**
     * Sets the GameContext for entity lookups (optional, for AI Core tier sync).
     *
     * @param gameContext the game context
     */
    public void setGameContext(GameContext gameContext) {
        this.gameContext = gameContext;
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
    public int calculateExperiencePoints(AiCommanderExperience.Outcome outcome) {
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
    public int calculateLeadershipBonus(AiCommanderExperience.Outcome outcome) {
        switch (outcome) {
            case VICTORY: return 1;
            case PYRRHIC_VICTORY: return 0;
            case STALEMATE: return 0;
            case DEFEAT: return -1;
            case ROUTE: return -2;
            default: return 0;
        }
    }

    // ---- Training Level Progression ----

    /** XP threshold for UNTRAINED â†’ BASIC */
    private static final int XP_BASIC = 100;
    /** XP threshold for BASIC â†’ ADVANCED */
    private static final int XP_ADVANCED = 500;
    /** XP threshold for ADVANCED â†’ MASTER */
    private static final int XP_MASTER = 1500;

    /**
     * Advances the training level of an AI character based on accumulated XP.
     * <p>
     * XP thresholds: UNTRAINED â†’ BASIC at 100 XP, BASIC â†’ ADVANCED at 500 XP,
     * ADVANCED â†’ MASTER at 1500 XP.
     * </p>
     *
     * @param aiCharacterId the AI character ID
     * @return true if the training level was advanced
     */
    @Transactional
    public boolean advanceTrainingLevel(Long aiCharacterId) {
        AiCharacter character = aiCharacterRepository.findById(aiCharacterId)
                .orElse(null);
        if (character == null) {
            return false;
        }

        AiCharacter.TrainingLevel current = character.getTrainingLevel();
        if (current == AiCharacter.TrainingLevel.MASTER) {
            return false;
        }

        AiCharacter.TrainingLevel next;
        int threshold;
        switch (current) {
            case UNTRAINED:
                next = AiCharacter.TrainingLevel.BASIC;
                threshold = XP_BASIC;
                break;
            case BASIC:
                next = AiCharacter.TrainingLevel.ADVANCED;
                threshold = XP_ADVANCED;
                break;
            case ADVANCED:
                next = AiCharacter.TrainingLevel.MASTER;
                threshold = XP_MASTER;
                break;
            default:
                return false;
        }

        if (character.getTotalExperience() >= threshold) {
            character.setTrainingLevel(next);
            aiCharacterRepository.save(character);
            log.info("AI character {} training advanced: {} -> {} (XP: {})",
                    character.getName(), current, next, character.getTotalExperience());
            // Sync the physical AI Core tier on the linked entity
            syncAiCoreTier(character);
            return true;
        }
        return false;
    }

    /**
     * Syncs the physical AI Core tier on the linked entity when training level advances.
     * <p>
     * Maps TrainingLevel â†’ AiCoreComponent.Tier:
     * UNTRAINEDâ†’no change, BASICâ†’BASIC, ADVANCEDâ†’ADVANCED, MASTERâ†’ELITE.
     * </p>
     *
     * @param character the AI character whose entity's AI Core should be upgraded
     */
    public void syncAiCoreTier(AiCharacter character) {
        if (character == null || gameContext == null) {
            return;
        }

        AiCharacter.TrainingLevel trainingLevel = character.getTrainingLevel();
        if (trainingLevel == AiCharacter.TrainingLevel.UNTRAINED ||
            trainingLevel == AiCharacter.TrainingLevel.MASTER) {
            return;
        }

        AiCoreComponent.Tier tier;
        switch (trainingLevel) {
            case BASIC:
                tier = AiCoreComponent.Tier.BASIC;
                break;
            case ADVANCED:
                tier = AiCoreComponent.Tier.ADVANCED;
                break;
            case MASTER:
                tier = AiCoreComponent.Tier.ELITE;
                break;
            default:
                return;
        }

        AiCharacter.CharacterType charType = character.getCharacterType();
        if (charType == null) {
            return;
        }

        try {
            switch (charType) {
                case SHIP:
                    if (character.getLinkedShipId() != null) {
                        Ship ship = gameContext.getShip(character.getLinkedShipId().intValue());
                        if (ship != null && ship.hasAiCore()) {
                            AiCoreComponent currentCore = ship.getAiCore();
                            if (currentCore.getTier().ordinal() < tier.ordinal()) {
                                ship.setAiCore(new AiCoreComponent(tier));
                                log.info("Upgraded AI Core for ship {}: {} -> {}",
                                        ship.getName(), currentCore.getTier(), tier);
                            }
                        }
                    }
                    break;

                case VEHICLE:
                    // ArmyUnit uses entity ID string "army_unit_{unitId}"
                    if (character.getLinkedEntityId() != null && character.getLinkedEntityId().startsWith("army_unit_")) {
                        String entityId = character.getLinkedEntityId();
                        int unitId = Integer.parseInt(entityId.replace("army_unit_", ""));
                        // Look up ArmyUnit across all empires
                        for (Empire empire : gameContext.getEmpires().values()) {
                            for (ArmyUnit unit : empire.getArmy().getUnits()) {
                                if (unit.getId() == unitId && unit.hasAiCore()) {
                                    AiCoreComponent currentCore = unit.getAiCore();
                                    if (currentCore.getTier().ordinal() < tier.ordinal()) {
                                        unit.setAiCore(new AiCoreComponent(tier));
                                        log.info("Upgraded AI Core for army unit {}: {} -> {}",
                                                unit.getName(), currentCore.getTier(), tier);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    break;

                case STATION:
                    if (character.getLinkedStationId() != null) {
                        int stationId = character.getLinkedStationId().intValue();
                        for (SpaceStation station : gameContext.getSpaceStations()) {
                            if (station.getId() == stationId && station.hasAiCore()) {
                                com.yisy.modules.doramcu.hex.spi.StationAiCoreSpi.Level stationLevel;
                                switch (tier) {
                                    case ADVANCED:
                                        stationLevel = com.yisy.modules.doramcu.hex.spi.StationAiCoreSpi.Level.ADVANCED;
                                        break;
                                    case ELITE:
                                        stationLevel = com.yisy.modules.doramcu.hex.spi.StationAiCoreSpi.Level.ELITE;
                                        break;
                                    default:
                                        stationLevel = com.yisy.modules.doramcu.hex.spi.StationAiCoreSpi.Level.BASIC;
                                        break;
                                }
                                com.yisy.modules.doramcu.hex.spi.StationAiCoreSpi.Level currentLevel = station.getAiCore().getAiCoreLevel();
                                if (currentLevel.getValue() < stationLevel.getValue()) {
                                    station.getAiCore().setAiCoreLevel(stationLevel);
                                    log.info("Upgraded AI Core for station {}: {} -> {}",
                                            station.getName(), currentLevel, stationLevel);
                                }
                            }
                        }
                    }
                    break;

                case FACILITY:
                    // PlanetFacility has no central registry in GameContext; skip for now
                    log.debug("Skipping AI Core tier sync for facility {}: no central registry available", character.getName());
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Failed to sync AI Core tier for character {}: {}", character.getName(), e.getMessage());
        }
    }
}

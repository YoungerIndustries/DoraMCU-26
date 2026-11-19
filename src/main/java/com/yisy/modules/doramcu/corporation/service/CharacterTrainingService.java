package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.CharacterTrainingTour;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.repository.CharacterTrainingTourRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.service.character.CharacterService;
import com.yisy.modules.doramcu.traveller.Career;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that manages character training tours within the corporation.
 * <p>
 * Handles enrollment, instant training simulation, completion, and dismissal.
 * Integrates with TrainingSimulationService for AI-driven narrative generation,
 * CareerProgressionService for XP awards, and CorporationEconomyService for
 * credit deductions.
 * </p>
 */
@Service
public class CharacterTrainingService {

    private static final Logger log = LoggerFactory.getLogger(CharacterTrainingService.class);

    /** Base cost per year of training in credits */
    private static final long BASE_COST_PER_YEAR = 5000L;

    /** Skill difficulty multipliers for training cost */
    private static final Map<String, Double> SKILL_COST_MULTIPLIER = Map.of(
            "COMBAT", 1.5,
            "NEGOTIATION", 1.2,
            "RESEARCH", 1.3,
            "LOGISTICS", 1.0,
            "GOVERNANCE", 1.4
    );

    private final CharacterTrainingTourRepository tourRepository;
    private final TrainingSimulationService simulationService;
    private final CareerProgressionService careerProgressionService;
    private final CorporationEconomyService economyService;
    private final CharacterService travellerCharacterService;
    private final AiCharacterRepository aiCharacterRepository;
    private final CorporateHiringRepository hiringRepository;

    public CharacterTrainingService(
            CharacterTrainingTourRepository tourRepository,
            TrainingSimulationService simulationService,
            CareerProgressionService careerProgressionService,
            CorporationEconomyService economyService,
            CharacterService travellerCharacterService,
            AiCharacterRepository aiCharacterRepository,
            CorporateHiringRepository hiringRepository) {
        this.tourRepository = tourRepository;
        this.simulationService = simulationService;
        this.careerProgressionService = careerProgressionService;
        this.economyService = economyService;
        this.travellerCharacterService = travellerCharacterService;
        this.aiCharacterRepository = aiCharacterRepository;
        this.hiringRepository = hiringRepository;
    }

    /**
     * Enrolls a character in a training tour.
     * <p>
     * Validates the character exists, deducts credits from the corporation treasury,
     * and creates a tour record with ENROLLED status.
     * </p>
     *
     * @param corporationId the corporation ID
     * @param characterType PLAYER_CHARACTER or AI_CHARACTER
     * @param characterId the character ID (TravellerCharacter id or AiCharacter id)
     * @param hiringId the CorporateHiring ID (required for AI characters)
     * @param skill the skill being trained
     * @param profession the profession/career being trained
     * @param durationYears the duration of the tour (1–4 years)
     * @return the created training tour
     */
    @Transactional
    public CharacterTrainingTour enrollInTraining(Long corporationId, CharacterTrainingTour.CharacterType characterType,
                                                   Integer characterId, Long hiringId, String skill,
                                                   String profession, int durationYears) {
        if (durationYears < 1 || durationYears > 4) {
            throw new IllegalArgumentException("Duration must be 1-4 years, got: " + durationYears);
        }

        // Calculate training cost
        long cost = calculateTrainingCost(skill, durationYears);

        // Deduct credits from treasury
        economyService.debitCorporation(corporationId, cost,
                CorporateFinanceTransaction.TransactionType.EXPENSE,
                "Training enrollment: " + skill + " (" + durationYears + " years)", null);

        // Create tour record
        CharacterTrainingTour tour = new CharacterTrainingTour(
                corporationId, characterType, skill, profession, durationYears, cost);

        if (characterType == CharacterTrainingTour.CharacterType.PLAYER_CHARACTER) {
            tour.setTravellerCharacterId(characterId);
            TravellerCharacter tc = travellerCharacterService.getCharacter(characterId);
            if (tc != null) {
                tour.setAgeAtStart(tc.getAge());
            }
        } else {
            tour.setAiCharacterId(Long.valueOf(characterId));
            tour.setHiringId(hiringId);
            if (hiringId != null) {
                Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
                tour.setSkillLevelBefore(hireOpt.map(h -> h.getSkillLevel(skill)).orElse(1));
            }
        }

        return tourRepository.save(tour);
    }

    /**
     * Instantly simulates an entire training tour.
     * <p>
     * This is the "instant train" button: calls TrainingSimulationService to generate
     * the full tour narrative, then completes the tour immediately — awarding XP,
     * improving skills, aging the character, and recording the narrative.
     * </p>
     *
     * @param tourId the training tour ID
     * @return the completed training tour
     */
    @Transactional
    public CharacterTrainingTour simulateInstantTraining(Long tourId) {
        CharacterTrainingTour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Training tour not found: " + tourId));

        if (tour.getStatus() != CharacterTrainingTour.Status.ENROLLED) {
            throw new IllegalStateException("Tour is not in ENROLLED status: " + tour.getStatus());
        }

        // Mark as simulating
        tour.setStatus(CharacterTrainingTour.Status.SIMULATING);
        tourRepository.save(tour);

        // Run the simulation
        TrainingSimulationService.SimulationResult result = simulationService.simulateTour(
                tour.getSkill(), tour.getProfession(), tour.getDurationYears());

        // Complete the tour with simulation results
        completeTrainingTour(tour, result);

        return tour;
    }

    /**
     * Completes a training tour, applying simulation results.
     *
     * @param tour the tour to complete
     * @param result the simulation result
     */
    private void completeTrainingTour(CharacterTrainingTour tour,
                                      TrainingSimulationService.SimulationResult result) {
        tour.setStatus(CharacterTrainingTour.Status.COMPLETED);
        tour.setCompletedAt(LocalDateTime.now());
        tour.setNarrative(result.getNarrative());
        tour.setEventsSummary(String.join("; ", result.getEvents()));
        tour.setXpAwarded(result.getXpAwarded());
        tour.setSkillLevelAfter(
                Math.min(10, tour.getSkillLevelBefore() + result.getSkillImprovement()));

        int ageAtEnd = (tour.getAgeAtStart() != null ? tour.getAgeAtStart() : 18) + tour.getDurationYears();
        tour.setAgeAtEnd(ageAtEnd);

        // Apply character-specific effects
        if (tour.getCharacterType() == CharacterTrainingTour.CharacterType.PLAYER_CHARACTER) {
            applyPlayerCharacterCompletion(tour, result);
        } else {
            applyAiCharacterCompletion(tour, result);
        }

        tourRepository.save(tour);
        log.info("Completed training tour {}: {} in {} for {} years",
                tour.getId(), tour.getSkill(), tour.getProfession(), tour.getDurationYears());
    }

    /**
     * Apply training completion effects to a player character.
     */
    private void applyPlayerCharacterCompletion(CharacterTrainingTour tour,
                                                 TrainingSimulationService.SimulationResult result) {
        TravellerCharacter tc = travellerCharacterService.getCharacter(tour.getTravellerCharacterId());
        if (tc == null) {
            log.warn("TravellerCharacter not found: {}", tour.getTravellerCharacterId());
            return;
        }

        // Age the character
        tc.setAge(tc.getAge() + tour.getDurationYears());

        // Add wealth earned
        tc.setWealth(tc.getWealth() + result.getWealthEarned());

        // Map profession string to Career enum
        Career careerEnum = Career.SOLDIER; // default
        try {
            careerEnum = Career.valueOf(tour.getProfession().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown profession '{}', defaulting to SOLDIER", tour.getProfession());
        }

        // Record as a CareerTour in the character's book
        List<Skill> skillsGained = new ArrayList<>();
        try {
            skillsGained.add(Skill.valueOf(tour.getSkill().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown skill '{}', no skills recorded", tour.getSkill());
        }

        TravellerCharacter.CareerTour careerTour = new TravellerCharacter.CareerTour(
                careerEnum, tour.getDurationYears(),
                skillsGained,
                "Trained", result.getWealthEarned(),
                false, TravellerCharacter.RetirementType.NORMAL);
        tc.addCareerTour(careerTour);

        log.info("Player character {} aged from {} to {} after training tour",
                tc.getName(), tour.getAgeAtStart(), tc.getAge());
    }

    /**
     * Apply training completion effects to an AI character.
     */
    private void applyAiCharacterCompletion(CharacterTrainingTour tour,
                                             TrainingSimulationService.SimulationResult result) {
        if (tour.getHiringId() == null) {
            log.warn("No hiring ID for AI character training tour: {}", tour.getId());
            return;
        }

        // Award XP to the hire
        try {
            careerProgressionService.awardXp(tour.getHiringId(), result.getXpAwarded(),
                    CorporateContract.ContractType.TRADE);
        } catch (Exception e) {
            log.warn("Failed to award XP to hire {}: {}", tour.getHiringId(), e.getMessage());
        }

        // Update skill level on the hire
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(tour.getHiringId());
        hireOpt.ifPresent(h -> {
            int newLevel = Math.min(10, h.getSkillLevel(tour.getSkill()) + result.getSkillImprovement());
            h.setSkillLevel(tour.getSkill(), newLevel);
            hiringRepository.save(h);
        });

        log.info("AI character {} completed training tour with {} XP awarded",
                tour.getAiCharacterId(), result.getXpAwarded());
    }

    /**
     * Dismisses a character from training early.
     *
     * @param tourId the training tour ID
     * @return the dismissed tour
     */
    @Transactional
    public CharacterTrainingTour dismissFromTraining(Long tourId) {
        CharacterTrainingTour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Training tour not found: " + tourId));

        if (tour.getStatus() != CharacterTrainingTour.Status.ENROLLED
                && tour.getStatus() != CharacterTrainingTour.Status.SIMULATING) {
            throw new IllegalStateException("Cannot dismiss tour with status: " + tour.getStatus());
        }

        tour.setStatus(CharacterTrainingTour.Status.DISMISSED);
        tour.setCompletedAt(LocalDateTime.now());
        tour.setNarrative("The character was dismissed from training before completion.");

        return tourRepository.save(tour);
    }

    /**
     * Gets all active training tours for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of active tours
     */
    public List<CharacterTrainingTour> getActiveTrainingTours(Long corporationId) {
        return tourRepository.findByCorporationIdAndStatus(corporationId, CharacterTrainingTour.Status.ENROLLED);
    }

    /**
     * Gets the training history for a character.
     *
     * @param corporationId the corporation ID
     * @param characterType the character type
     * @param characterId the character ID
     * @return list of completed tours
     */
    public List<CharacterTrainingTour> getTrainingHistory(Long corporationId,
                                                           CharacterTrainingTour.CharacterType characterType,
                                                           Integer characterId) {
        List<CharacterTrainingTour> allTours;
        if (characterType == CharacterTrainingTour.CharacterType.PLAYER_CHARACTER) {
            allTours = tourRepository.findByTravellerCharacterId(characterId);
        } else {
            allTours = tourRepository.findByAiCharacterId(Long.valueOf(characterId));
        }
        return allTours.stream()
                .filter(t -> t.getStatus() == CharacterTrainingTour.Status.COMPLETED
                        || t.getStatus() == CharacterTrainingTour.Status.DISMISSED)
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of available training skills.
     *
     * @return list of skill names
     */
    public List<String> getAvailableTrainingSkills() {
        return new ArrayList<>(SKILL_COST_MULTIPLIER.keySet());
    }

    /**
     * Calculates the training cost based on skill difficulty and duration.
     *
     * @param skill the skill being trained
     * @param durationYears the duration in years
     * @return the cost in credits
     */
    public long calculateTrainingCost(String skill, int durationYears) {
        double multiplier = SKILL_COST_MULTIPLIER.getOrDefault(skill.toUpperCase(), 1.0);
        return Math.round(BASE_COST_PER_YEAR * durationYears * multiplier);
    }

    /**
     * Gets all tours for a corporation (for the HUD panel).
     *
     * @param corporationId the corporation ID
     * @return all tours
     */
    public List<CharacterTrainingTour> getAllTours(Long corporationId) {
        return tourRepository.findByCorporationId(corporationId);
    }
}

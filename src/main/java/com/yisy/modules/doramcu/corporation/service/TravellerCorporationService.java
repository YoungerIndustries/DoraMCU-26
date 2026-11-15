package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.model.TravellerContractAssignment;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.repository.TravellerContractAssignmentRepository;
import com.yisy.modules.doramcu.service.CharacterService;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.Skill;
import com.yisy.modules.doramcu.traveller.Skill.SkillCategory;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that integrates Traveller characters with the corporation system.
 * <p>
 * Maps Traveller abilities to contract types for performance bonuses,
 * uses Traveller skills to unlock special contracts, and assigns
 * characters as crew members on contracts.
 * </p>
 */
@Service
public class TravellerCorporationService {

    private static final Logger log = LoggerFactory.getLogger(TravellerCorporationService.class);

    /**
     * Maps each ability to preferred contract types.
     */
    private static final Map<AbilityScore, List<CorporateContract.ContractType>> ABILITY_CONTRACT_MAP = Map.of(
            AbilityScore.INT, List.of(CorporateContract.ContractType.MISSION),
            AbilityScore.EDU, List.of(CorporateContract.ContractType.TRADE),
            AbilityScore.SOC, List.of(CorporateContract.ContractType.MISSION),
            AbilityScore.END, List.of(CorporateContract.ContractType.SECURITY),
            AbilityScore.DEX, List.of(CorporateContract.ContractType.LOGISTICS),
            AbilityScore.ST, List.of(
                    CorporateContract.ContractType.TRADE,
                    CorporateContract.ContractType.SECURITY,
                    CorporateContract.ContractType.LOGISTICS,
                    CorporateContract.ContractType.MISSION
            ),
            AbilityScore.APP, List.of(CorporateContract.ContractType.TRADE)
    );

    /**
     * Maps skill categories to contract types.
     */
    private static final Map<SkillCategory, List<CorporateContract.ContractType>> SKILL_CATEGORY_CONTRACT_MAP = Map.of(
            SkillCategory.SOCIAL, List.of(CorporateContract.ContractType.TRADE, CorporateContract.ContractType.MISSION),
            SkillCategory.COMBAT, List.of(CorporateContract.ContractType.SECURITY),
            SkillCategory.TECHNICAL, List.of(CorporateContract.ContractType.LOGISTICS),
            SkillCategory.SCIENTIFIC, List.of(CorporateContract.ContractType.MISSION),
            SkillCategory.NAVIGATION, List.of(CorporateContract.ContractType.LOGISTICS, CorporateContract.ContractType.MISSION),
            SkillCategory.LEADERSHIP, List.of(CorporateContract.ContractType.MISSION, CorporateContract.ContractType.SECURITY),
            SkillCategory.SURVIVAL, List.of(CorporateContract.ContractType.LOGISTICS),
            SkillCategory.STEALTH, List.of(CorporateContract.ContractType.SECURITY, CorporateContract.ContractType.MISSION)
    );

    /**
     * Skills that unlock special high-value contracts.
     */
    private static final Set<Skill> SPECIAL_CONTRACT_SKILLS = Set.of(
            Skill.ENTREPRENEUR,  // High-value TRADE contracts
            Skill.DIPLOMACY,     // Special MISSION contracts
            Skill.ADMINISTRATION // Large LOGISTICS contracts
    );

    private final CharacterService characterService;
    private final CorporateContractRepository contractRepository;
    private final TravellerContractAssignmentRepository assignmentRepository;

    public TravellerCorporationService(
            CharacterService characterService,
            CorporateContractRepository contractRepository,
            TravellerContractAssignmentRepository assignmentRepository) {
        this.characterService = characterService;
        this.contractRepository = contractRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Get the CharacterService for accessing Traveller characters.
     */
    public CharacterService getCharacterService() {
        return characterService;
    }

    /**
     * Get the assignment repository for saving assignment changes.
     */
    public TravellerContractAssignmentRepository getAssignmentRepository() {
        return assignmentRepository;
    }

    // ---- Ability Bonus Calculation ----

    /**
     * Calculate the performance bonus from a character's abilities for a given contract type.
     * <p>
     * Formula: (abilityScore - 7) * 0.2, capped at [-1.0, +1.0].
     * The best matching ability is used.
     * </p>
     *
     * @param character    the Traveller character
     * @param contractType the contract type
     * @return the ability bonus (can be negative)
     */
    public double getAbilityBonus(TravellerCharacter character, CorporateContract.ContractType contractType) {
        double bestBonus = 0.0;

        for (Map.Entry<AbilityScore, List<CorporateContract.ContractType>> entry : ABILITY_CONTRACT_MAP.entrySet()) {
            AbilityScore ability = entry.getKey();
            List<CorporateContract.ContractType> types = entry.getValue();

            if (types.contains(contractType)) {
                int score = character.getAbility(ability);
                double bonus = (score - 7) * 0.2;
                bonus = Math.max(-1.0, Math.min(1.0, bonus));
                bestBonus = Math.max(bestBonus, bonus);
            }
        }

        return bestBonus;
    }

    // ---- Skill Bonus Calculation ----

    /**
     * Calculate the performance bonus from a character's skills for a given contract type.
     * <p>
     * Each matching skill category gives +0.5 per skill level above 5.
     * Skills are counted at level 1 (presence), so matching skills give +0.5 each.
     * </p>
     *
     * @param character    the Traveller character
     * @param contractType the contract type
     * @return the skill bonus
     */
    public double getSkillBonus(TravellerCharacter character, CorporateContract.ContractType contractType) {
        double bonus = 0.0;
        Set<Skill> characterSkills = character.getSkills();

        for (Map.Entry<SkillCategory, List<CorporateContract.ContractType>> entry : SKILL_CATEGORY_CONTRACT_MAP.entrySet()) {
            SkillCategory category = entry.getKey();
            List<CorporateContract.ContractType> types = entry.getValue();

            if (types.contains(contractType)) {
                // Count skills in this category
                long matchingSkills = characterSkills.stream()
                        .filter(skill -> skill.getCategory() == category)
                        .count();
                // Each matching skill gives +0.5 bonus
                bonus += matchingSkills * 0.5;
            }
        }

        // Cap skill bonus at +3.0
        return Math.min(3.0, bonus);
    }

    // ---- Character Performance ----

    /**
     * Calculate the total character performance for a contract type.
     * Combines ability bonus and skill bonus.
     *
     * @param character    the Traveller character
     * @param contractType the contract type
     * @return total performance bonus
     */
    public double calculateCharacterPerformance(TravellerCharacter character, CorporateContract.ContractType contractType) {
        double abilityBonus = getAbilityBonus(character, contractType);
        double skillBonus = getSkillBonus(character, contractType);
        return abilityBonus + skillBonus;
    }

    // ---- Qualified Contracts ----

    /**
     * Get contracts that the character qualifies for based on their skills.
     * A character qualifies if they have at least one skill in a matching category
     * for the contract type, or if they have a special contract-unlocking skill.
     *
     * @param character the Traveller character
     * @return list of qualified contracts
     */
    @Transactional(readOnly = true)
    public List<CorporateContract> getQualifiedContracts(TravellerCharacter character) {
        List<CorporateContract> available = contractRepository.findByStatus(CorporateContract.Status.AVAILABLE);
        Set<Skill> characterSkills = character.getSkills();

        return available.stream()
                .filter(contract -> isQualified(character, characterSkills, contract))
                .collect(Collectors.toList());
    }

    /**
     * Check if a character qualifies for a specific contract.
     */
    public boolean isQualified(TravellerCharacter character, Set<Skill> characterSkills, CorporateContract contract) {
        CorporateContract.ContractType type = contract.getType();

        // Check for special contract-unlocking skills
        for (Skill skill : SPECIAL_CONTRACT_SKILLS) {
            if (characterSkills.contains(skill)) {
                // ENTREPRENEUR unlocks TRADE, DIPLOMACY unlocks MISSION, ADMINISTRATION unlocks LOGISTICS
                if (skill == Skill.ENTREPRENEUR && type == CorporateContract.ContractType.TRADE) return true;
                if (skill == Skill.DIPLOMACY && type == CorporateContract.ContractType.MISSION) return true;
                if (skill == Skill.ADMINISTRATION && type == CorporateContract.ContractType.LOGISTICS) return true;
            }
        }

        // Check skill category matching
        for (Map.Entry<SkillCategory, List<CorporateContract.ContractType>> entry : SKILL_CATEGORY_CONTRACT_MAP.entrySet()) {
            SkillCategory category = entry.getKey();
            List<CorporateContract.ContractType> types = entry.getValue();

            if (types.contains(type)) {
                boolean hasSkillInCategory = characterSkills.stream()
                        .anyMatch(skill -> skill.getCategory() == category);
                if (hasSkillInCategory) return true;
            }
        }

        // Default: character qualifies for any contract if they have any skills
        return !characterSkills.isEmpty();
    }

    // ---- Character Assignment ----

    /**
     * Assign a Traveller character to work on a contract.
     *
     * @param characterId the character ID
     * @param contractId  the contract ID
     * @return the created assignment
     * @throws IllegalArgumentException if character or contract not found
     * @throws IllegalStateException    if character is already assigned or contract is not active
     */
    @Transactional
    public TravellerContractAssignment assignCharacterToContract(Integer characterId, Long contractId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Traveller character not found: " + characterId);
        }

        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (contract.getStatus() != CorporateContract.Status.ACTIVE) {
            throw new IllegalStateException("Contract is not active: " + contract.getStatus());
        }

        // Check if already assigned
        if (assignmentRepository.existsByContractIdAndTravellerCharacterId(contractId, characterId)) {
            throw new IllegalStateException("Character already assigned to this contract");
        }

        // Check if character is busy on another active contract
        List<TravellerContractAssignment> existingAssignments = assignmentRepository.findByTravellerCharacterId(characterId);
        for (TravellerContractAssignment assignment : existingAssignments) {
            Optional<CorporateContract> existingContract = contractRepository.findById(assignment.getContractId());
            if (existingContract.isPresent() && existingContract.get().getStatus() == CorporateContract.Status.ACTIVE) {
                throw new IllegalStateException("Character is already working on an active contract");
            }
        }

        TravellerContractAssignment assignment = new TravellerContractAssignment(contractId, characterId);
        assignment = assignmentRepository.save(assignment);

        log.info("Assigned Traveller character {} ({}) to contract {}", characterId, character.getName(), contractId);
        return assignment;
    }

    /**
     * Remove a character from a contract.
     *
     * @param assignmentId the assignment ID
     */
    @Transactional
    public void removeCharacterFromContract(Long assignmentId) {
        TravellerContractAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        assignmentRepository.delete(assignment);
        log.info("Removed Traveller character {} from contract {}", assignment.getTravellerCharacterId(), assignment.getContractId());
    }

    // ---- Character Stats ----

    /**
     * Get corporation stats for a Traveller character.
     *
     * @param characterId the character ID
     * @return map with character corporation stats
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCharacterStats(Integer characterId) {
        TravellerCharacter character = characterService.getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Traveller character not found: " + characterId);
        }

        List<TravellerContractAssignment> assignments = assignmentRepository.findByTravellerCharacterId(characterId);

        int totalContracts = assignments.size();
        int[] counters = new int[2]; // [0] = active, [1] = completed
        int totalXpEarned = 0;

        for (TravellerContractAssignment assignment : assignments) {
            Optional<CorporateContract> contract = contractRepository.findById(assignment.getContractId());
            contract.ifPresent(c -> {
                if (c.getStatus() == CorporateContract.Status.ACTIVE) counters[0]++;
                if (c.getStatus() == CorporateContract.Status.COMPLETED) counters[1]++;
            });
            totalXpEarned += assignment.getXpEarned() != null ? assignment.getXpEarned() : 0;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("characterId", characterId);
        stats.put("characterName", character.getName());
        stats.put("age", character.getAge());
        stats.put("wealth", character.getWealth());
        stats.put("totalContracts", totalContracts);
        stats.put("contractsActive", counters[0]);
        stats.put("contractsCompleted", counters[1]);
        stats.put("totalXpEarned", totalXpEarned);
        
        // Calculate level up threshold based on XP thresholds
        int levelUpThreshold;
        int[] xpThresholds = {0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500};
        int currentLevel = 1;
        for (int i = 0; i < xpThresholds.length; i++) {
            if (totalXpEarned >= xpThresholds[i]) {
                currentLevel = i + 1;
            } else {
                break;
            }
        }
        if (currentLevel < xpThresholds.length) {
            levelUpThreshold = xpThresholds[currentLevel] - totalXpEarned;
        } else {
            levelUpThreshold = 0;
        }
        stats.put("levelUpThreshold", levelUpThreshold);
        stats.put("isLevelUpReady", totalXpEarned >= (currentLevel < xpThresholds.length ? xpThresholds[currentLevel] : Integer.MAX_VALUE));
        
        // Add assignments list
        List<Map<String, Object>> assignmentsList = new java.util.ArrayList<>();
        for (TravellerContractAssignment assignment : assignments) {
            Optional<CorporateContract> contract = contractRepository.findById(assignment.getContractId());
            if (contract.isPresent()) {
                Map<String, Object> assignmentMap = new java.util.HashMap<>();
                assignmentMap.put("id", assignment.getId());
                assignmentMap.put("contractId", assignment.getContractId());
                assignmentMap.put("contractTitle", contract.get().getTitle());
                assignmentMap.put("assignedAt", assignment.getAssignedAt() != null ? assignment.getAssignedAt().toString() : "");
                assignmentMap.put("xpEarned", assignment.getXpEarned() != null ? assignment.getXpEarned() : 0);
                assignmentMap.put("status", contract.get().getStatus().name());
                assignmentsList.add(assignmentMap);
            }
        }
        stats.put("assignments", assignmentsList);

        return stats;
    }

    /**
     * Get all assignments for a contract (for use in success calculation).
     *
     * @param contractId the contract ID
     * @return list of assignments
     */
    @Transactional(readOnly = true)
    public List<TravellerContractAssignment> getAssignmentsForContract(Long contractId) {
        return assignmentRepository.findByContractId(contractId);
    }

    /**
     * Get all assignments for a character.
     *
     * @param characterId the character ID
     * @return list of assignments
     */
    @Transactional(readOnly = true)
    public List<TravellerContractAssignment> getAssignmentsForCharacter(Integer characterId) {
        return assignmentRepository.findByTravellerCharacterId(characterId);
    }

    /**
     * Get the contract repository for accessing contracts.
     *
     * @return the contract repository
     */
    public CorporateContractRepository getContractRepository() {
        return contractRepository;
    }

    /**
     * Get a human-readable reason why a character qualifies for a contract.
     *
     * @param character the Traveller character
     * @param contract the contract to check
     * @return description of why the character qualifies
     */
    public String getQualificationReason(TravellerCharacter character, CorporateContract contract) {
        Set<Skill> characterSkills = character.getSkills();
        CorporateContract.ContractType type = contract.getType();

        // Check for special contract-unlocking skills
        for (Skill skill : SPECIAL_CONTRACT_SKILLS) {
            if (characterSkills.contains(skill)) {
                if (skill == Skill.ENTREPRENEUR && type == CorporateContract.ContractType.TRADE) {
                    return "ENTREPRENEUR skill unlocks TRADE contracts";
                }
                if (skill == Skill.DIPLOMACY && type == CorporateContract.ContractType.MISSION) {
                    return "DIPLOMACY skill unlocks MISSION contracts";
                }
                if (skill == Skill.ADMINISTRATION && type == CorporateContract.ContractType.LOGISTICS) {
                    return "ADMINISTRATION skill unlocks LOGISTICS contracts";
                }
            }
        }

        // Check skill category matching
        for (Map.Entry<SkillCategory, List<CorporateContract.ContractType>> entry : SKILL_CATEGORY_CONTRACT_MAP.entrySet()) {
            SkillCategory category = entry.getKey();
            List<CorporateContract.ContractType> types = entry.getValue();

            if (types.contains(type)) {
                boolean hasSkillInCategory = characterSkills.stream()
                        .anyMatch(skill -> skill.getCategory() == category);
                if (hasSkillInCategory) {
                    return category.name() + " skills match " + type.name() + " contract type";
                }
            }
        }

        // Default: character has skills
        return "Character has relevant skills for this contract type";
    }
}

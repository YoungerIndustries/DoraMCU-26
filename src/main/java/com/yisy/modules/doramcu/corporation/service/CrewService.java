package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.model.Crew;
import com.yisy.modules.doramcu.corporation.model.CrewMember;
import com.yisy.modules.doramcu.corporation.model.HireSkill;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CrewMemberRepository;
import com.yisy.modules.doramcu.corporation.repository.CrewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that manages crew groups within the corporation.
 * <p>
 * Handles crew creation, member management, one-button crew assembly,
 * and contract assignment. Crews are assembled from the employee pool
 * (hires not in a crew) and execute contracts as a unit.
 * </p>
 */
@Service
public class CrewService {

    private static final Logger log = LoggerFactory.getLogger(CrewService.class);

    /**
     * Maps contract types to preferred positions for crew assembly.
     * Mirrors the POSITION_CONTRACT_MAP in ContractExecutionService.
     */
    private static final Map<CorporateContract.ContractType, List<CorporatePosition>> CONTRACT_POSITION_MAP = Map.of(
            CorporateContract.ContractType.TRADE, List.of(
                    CorporatePosition.SHIP_CAPTAIN, CorporatePosition.ENGINEER, CorporatePosition.SECURITY_CHIEF),
            CorporateContract.ContractType.SECURITY, List.of(
                    CorporatePosition.SECURITY_CHIEF, CorporatePosition.SHIP_CAPTAIN, CorporatePosition.ENGINEER),
            CorporateContract.ContractType.LOGISTICS, List.of(
                    CorporatePosition.ENGINEER, CorporatePosition.SHIP_CAPTAIN, CorporatePosition.SECURITY_CHIEF),
            CorporateContract.ContractType.MISSION, List.of(
                    CorporatePosition.SHIP_CAPTAIN, CorporatePosition.SECURITY_CHIEF, CorporatePosition.ENGINEER)
    );

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CorporateHiringRepository hiringRepository;
    private final CorporateContractRepository contractRepository;

    public CrewService(CrewRepository crewRepository, CrewMemberRepository crewMemberRepository,
                       CorporateHiringRepository hiringRepository, CorporateContractRepository contractRepository) {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.hiringRepository = hiringRepository;
        this.contractRepository = contractRepository;
    }

    /**
     * Creates a new crew with a leader.
     *
     * @param corporationId the corporation ID
     * @param name the crew name
     * @param leaderHiringId the hiring ID of the leader AI
     * @param description the crew description
     * @return the created crew
     */
    @Transactional
    public Crew createCrew(Long corporationId, String name, Long leaderHiringId, String description) {
        // Validate leader exists and is active
        Optional<CorporateHiring> leaderOpt = hiringRepository.findById(leaderHiringId);
        if (leaderOpt.isEmpty() || !leaderOpt.get().isActive()) {
            throw new IllegalArgumentException("Leader hire not found or inactive: " + leaderHiringId);
        }
        if (leaderOpt.get().getCrewId() != null) {
            throw new IllegalArgumentException("Leader is already in a crew: " + leaderHiringId);
        }

        // Create the crew
        Crew crew = new Crew(corporationId, name, leaderHiringId, description);
        crew = crewRepository.save(crew);

        // Assign leader to crew
        CorporateHiring leader = leaderOpt.get();
        leader.setCrewId(crew.getId());
        hiringRepository.save(leader);

        // Create crew member record for leader
        CrewMember leaderMember = new CrewMember(crew, leaderHiringId, true);
        crewMemberRepository.save(leaderMember);

        log.info("Created crew {} with leader {} for corporation {}", crew.getId(), leaderHiringId, corporationId);
        return crew;
    }

    /**
     * Adds a member to an existing crew.
     *
     * @param crewId the crew ID
     * @param hiringId the hiring ID to add
     * @return the created crew member
     */
    @Transactional
    public CrewMember addMemberToCrew(Long crewId, Long hiringId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new IllegalArgumentException("Crew not found: " + crewId));

        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        if (hireOpt.isEmpty() || !hireOpt.get().isActive()) {
            throw new IllegalArgumentException("Hire not found or inactive: " + hiringId);
        }
        if (hireOpt.get().getCrewId() != null) {
            throw new IllegalArgumentException("Hire is already in a crew: " + hiringId);
        }

        // Assign hire to crew
        CorporateHiring hire = hireOpt.get();
        hire.setCrewId(crewId);
        hiringRepository.save(hire);

        // Create crew member record
        CrewMember member = new CrewMember(crew, hiringId, false);
        return crewMemberRepository.save(member);
    }

    /**
     * Removes a member from a crew.
     *
     * @param crewId the crew ID
     * @param hiringId the hiring ID to remove
     */
    @Transactional
    public void removeMemberFromCrew(Long crewId, Long hiringId) {
        Optional<CrewMember> memberOpt = crewMemberRepository.findByHiringId(hiringId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("Hire is not a member of any crew: " + hiringId);
        }

        CrewMember member = memberOpt.get();
        if (!member.getCrew().getId().equals(crewId)) {
            throw new IllegalArgumentException("Hire is not a member of this crew: " + hiringId);
        }

        // Cannot remove the leader directly — disband the crew or reassign leadership
        if (Boolean.TRUE.equals(member.getIsLeader())) {
            throw new IllegalStateException("Cannot remove crew leader. Disband the crew or reassign leadership.");
        }

        // Clear crew assignment
        Optional<CorporateHiring> hireOpt = hiringRepository.findById(hiringId);
        hireOpt.ifPresent(h -> {
            h.setCrewId(null);
            hiringRepository.save(h);
        });

        crewMemberRepository.delete(member);
        log.info("Removed member {} from crew {}", hiringId, crewId);
    }

    /**
     * Disbands a crew, returning members to the employee pool.
     *
     * @param crewId the crew ID
     */
    @Transactional
    public void disbandCrew(Long crewId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new IllegalArgumentException("Crew not found: " + crewId));

        // Clear crew assignment for all members
        List<CrewMember> members = crewMemberRepository.findByCrewId(crewId);
        for (CrewMember member : members) {
            hiringRepository.findById(member.getHiringId()).ifPresent(h -> {
                h.setCrewId(null);
                hiringRepository.save(h);
            });
        }

        crewMemberRepository.deleteByCrewId(crewId);
        crew.setStatus(Crew.Status.DISBANDED);
        crew.setContractId(null);
        crewRepository.save(crew);

        log.info("Disbanded crew {}. {} members returned to employee pool.", crewId, members.size());
    }

    /**
     * Gets all active crews for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of active crews
     */
    public List<Crew> getActiveCrews(Long corporationId) {
        return crewRepository.findByCorporationIdAndStatus(corporationId, Crew.Status.ACTIVE);
    }

    /**
     * Gets a crew by ID with its members.
     *
     * @param crewId the crew ID
     * @return the crew
     */
    public Crew getCrewById(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new IllegalArgumentException("Crew not found: " + crewId));
    }

    /**
     * Gets all active hires not in a crew (the employee pool).
     *
     * @param corporationId the corporation ID
     * @return list of available hires
     */
    public List<CorporateHiring> getEmployeePool(Long corporationId) {
        List<CorporateHiring> allActive = hiringRepository.findByCorporationIdAndStatus(
                corporationId, CorporateHiring.Status.ACTIVE);
        return allActive.stream()
                .filter(h -> h.getCrewId() == null)
                .collect(Collectors.toList());
    }

    /**
     * One-button crew assembly: auto-assembles a crew from available employees
     * based on the contract type, selects a leader, and assigns the crew to the contract.
     *
     * @param corporationId the corporation ID
     * @param contractId the contract ID
     * @return the created crew
     */
    @Transactional
    public Crew autoAssembleCrew(Long corporationId, Long contractId) {
        // Look up the contract
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        // Get available employees
        List<CorporateHiring> employeePool = getEmployeePool(corporationId);
        if (employeePool.isEmpty()) {
            throw new IllegalStateException("No available employees for crew assembly");
        }

        // Determine preferred positions for this contract type
        List<CorporatePosition> preferredPositions = CONTRACT_POSITION_MAP.getOrDefault(
                contract.getType(), List.of(
                        CorporatePosition.SHIP_CAPTAIN,
                        CorporatePosition.SECURITY_CHIEF,
                        CorporatePosition.ENGINEER
                ));

        // Score and sort employees by position match and performance
        employeePool.sort((a, b) -> {
            double scoreA = calculateHireScore(a, preferredPositions);
            double scoreB = calculateHireScore(b, preferredPositions);
            return Double.compare(scoreB, scoreA);
        });

        // Select up to 3 members
        int crewSize = Math.min(3, employeePool.size());
        List<CorporateHiring> selected = employeePool.subList(0, crewSize);

        // The best hire becomes the leader
        CorporateHiring leader = selected.get(0);

        // Generate crew name
        String crewName = generateCrewName(contract.getType());

        // Create the crew
        Crew crew = createCrew(corporationId, crewName, leader.getId(),
                "Auto-assembled crew for " + contract.getTitle());

        // Add remaining members
        for (int i = 1; i < selected.size(); i++) {
            addMemberToCrew(crew.getId(), selected.get(i).getId());
        }

        // Assign crew to contract
        assignCrewToContract(crew.getId(), contractId);

        log.info("Auto-assembled crew {} with {} members for contract {}",
                crew.getId(), selected.size(), contractId);
        return crew;
    }

    /**
     * Assigns a crew to execute a contract.
     *
     * @param crewId the crew ID
     * @param contractId the contract ID
     */
    @Transactional
    public void assignCrewToContract(Long crewId, Long contractId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new IllegalArgumentException("Crew not found: " + crewId));
        CorporateContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        crew.setContractId(contractId);
        crew.setStatus(Crew.Status.ON_CONTRACT);
        crewRepository.save(crew);

        contract.setCrewId(crewId);
        contractRepository.save(contract);

        log.info("Assigned crew {} to contract {}", crewId, contractId);
    }

    /**
     * Calculates a score for a hire based on position match and performance.
     */
    private double calculateHireScore(CorporateHiring hire, List<CorporatePosition> preferredPositions) {
        double score = 0;

        // Position match bonus
        if (preferredPositions.contains(hire.getPosition())) {
            score += 10;
        }

        // Performance rating
        if (hire.getPerformanceRating() != null) {
            score += hire.getPerformanceRating();
        }

        // Skill level bonus for primary skill
        HireSkill primarySkill = hire.getPosition().getPrimarySkill();
        score += hire.getSkillLevel(primarySkill.name()) * 0.5;

        return score;
    }

    /**
     * Generates a crew name based on contract type.
     */
    private String generateCrewName(CorporateContract.ContractType contractType) {
        String[] prefixes = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot"};
        String suffix = switch (contractType) {
            case TRADE -> "Trade Crew";
            case SECURITY -> "Security Crew";
            case LOGISTICS -> "Logistics Crew";
            case MISSION -> "Mission Crew";
        };
        String prefix = prefixes[new Random().nextInt(prefixes.length)];
        return prefix + " " + suffix;
    }
}

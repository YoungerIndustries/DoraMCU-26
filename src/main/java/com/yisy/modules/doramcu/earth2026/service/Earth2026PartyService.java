package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Party;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026PartyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Earth 2026 parties.
 * <p>
 * Parties allow multiple characters at the same location to group together
 * for scenarios and adventures.
 * </p>
 */
@Service
public class Earth2026PartyService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026PartyService.class);

    private final Earth2026PartyRepository partyRepository;
    private final Earth2026CharacterRepository characterRepository;

    public Earth2026PartyService(Earth2026PartyRepository partyRepository,
                                  Earth2026CharacterRepository characterRepository) {
        this.partyRepository = partyRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * Create a new party at a location.
     *
     * @param name the party name
     * @param locationId the location where the party is formed
     * @param leaderId the leader character ID
     * @return the created party
     */
    @Transactional
    public Earth2026Party createParty(String name, Long locationId, Long leaderId) {
        Earth2026Character leader = characterRepository.findById(leaderId)
                .orElseThrow(() -> new IllegalArgumentException("Leader character not found: " + leaderId));

        Earth2026Party party = new Earth2026Party(name, locationId, leaderId);
        party.setUpdatedAt(LocalDateTime.now());
        party = partyRepository.save(party);
        log.info("Created party: {} at location {} led by {}", name, locationId, leader.getName());
        return party;
    }

    /**
     * Get a party by ID.
     *
     * @param id the party ID
     * @return the party if found
     */
    public Optional<Earth2026Party> getParty(Long id) {
        return partyRepository.findById(id);
    }

    /**
     * List all parties at a specific location.
     *
     * @param locationId the location ID
     * @return list of parties at that location
     */
    public List<Earth2026Party> getPartiesAtLocation(Long locationId) {
        return partyRepository.findByLocationId(locationId);
    }

    /**
     * List all recruiting parties.
     *
     * @return list of parties open to new members
     */
    public List<Earth2026Party> getRecruitingParties() {
        return partyRepository.findByIsRecruitingTrue();
    }

    /**
     * List all active parties.
     *
     * @return list of active parties
     */
    public List<Earth2026Party> getActiveParties() {
        return partyRepository.findByStatus("active");
    }

    /**
     * Disband a party.
     *
     * @param partyId the party ID
     * @return the disbanded party
     */
    @Transactional
    public Optional<Earth2026Party> disbandParty(Long partyId) {
        return partyRepository.findById(partyId)
                .map(party -> {
                    party.setStatus("disbanded");
                    party.setUpdatedAt(LocalDateTime.now());
                    partyRepository.save(party);
                    log.info("Disbanded party: {}", party.getName());
                    return party;
                });
    }

    /**
     * Set a party as recruiting or not.
     *
     * @param partyId the party ID
     * @param recruiting whether the party is open to new members
     * @return the updated party
     */
    @Transactional
    public Optional<Earth2026Party> setRecruiting(Long partyId, boolean recruiting) {
        return partyRepository.findById(partyId)
                .map(party -> {
                    party.setRecruiting(recruiting);
                    party.setUpdatedAt(LocalDateTime.now());
                    partyRepository.save(party);
                    log.info("Party {} recruiting set to {}", party.getName(), recruiting);
                    return party;
                });
    }

    /**
     * Set the current scenario for a party.
     *
     * @param partyId the party ID
     * @param scenarioId the scenario ID
     * @return the updated party
     */
    @Transactional
    public Optional<Earth2026Party> setScenario(Long partyId, Long scenarioId) {
        return partyRepository.findById(partyId)
                .map(party -> {
                    party.setScenarioId(scenarioId);
                    party.setStatus("on_scenario");
                    party.setUpdatedAt(LocalDateTime.now());
                    partyRepository.save(party);
                    log.info("Party {} started scenario {}", party.getName(), scenarioId);
                    return party;
                });
    }

    /**
     * Move a party to a new location.
     *
     * @param partyId the party ID
     * @param locationId the new location ID
     * @return the updated party
     */
    @Transactional
    public Optional<Earth2026Party> moveParty(Long partyId, Long locationId) {
        return partyRepository.findById(partyId)
                .map(party -> {
                    party.setLocationId(locationId);
                    party.setUpdatedAt(LocalDateTime.now());
                    partyRepository.save(party);
                    log.info("Party {} moved to location {}", party.getName(), locationId);
                    return party;
                });
    }
}

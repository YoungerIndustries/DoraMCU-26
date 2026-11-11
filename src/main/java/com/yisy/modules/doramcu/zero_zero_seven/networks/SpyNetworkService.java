package com.yisy.modules.doramcu.zero_zero_seven.networks;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.SpyNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing spy networks.
 * <p>
 * Handles establishing, upgrading, and maintaining spy networks in foreign territories.
 * Networks are the backbone of intelligence operations.
 * </p>
 */
@Service
public class SpyNetworkService {

    private final SpyNetworkRepository spyNetworkRepository;
    private final BondGameSessionRepository gameSessionRepository;

    public SpyNetworkService(SpyNetworkRepository spyNetworkRepository,
                             BondGameSessionRepository gameSessionRepository) {
        this.spyNetworkRepository = spyNetworkRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    /**
     * Establish a new spy network in a foreign territory.
     *
     * @param name network name
     * @param locationId territory where the network is established
     * @param ownerEmpireId empire establishing the network
     * @param gameSessionId game session
     * @return the created spy network
     */
    @Transactional
    public SpyNetwork establishNetwork(String name, Long locationId, Long ownerEmpireId,
                                       Long gameSessionId) {
        BondGameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        SpyNetwork network = new SpyNetwork(name, locationId, ownerEmpireId, session);
        network.setIntelligenceGathered(5);
        network.setDetectionRisk(10);
        network.setSafehouseCount(1);

        return spyNetworkRepository.save(network);
    }

    /**
     * Upgrade an existing spy network.
     *
     * @param networkId the network to upgrade
     * @return the upgraded network
     */
    @Transactional
    public SpyNetwork upgradeNetwork(Long networkId) {
        SpyNetwork network = spyNetworkRepository.findById(networkId)
                .orElseThrow(() -> new IllegalArgumentException("Spy network not found: " + networkId));

        network.upgrade();
        return spyNetworkRepository.save(network);
    }

    /**
     * Assign an agent to a spy network.
     *
     * @param networkId the network
     * @param agentId the agent
     */
    @Transactional
    public void assignAgent(Long networkId, Long agentId) {
        SpyNetwork network = spyNetworkRepository.findById(networkId)
                .orElseThrow(() -> new IllegalArgumentException("Spy network not found: " + networkId));

        network.assignAgent(agentId);
        spyNetworkRepository.save(network);
    }

    /**
     * Get all active networks for an empire.
     *
     * @param ownerEmpireId the empire
     * @return list of active networks
     */
    public List<SpyNetwork> getActiveNetworks(Long ownerEmpireId) {
        return spyNetworkRepository.findByOwnerEmpireIdAndActive(ownerEmpireId, true);
    }

    /**
     * Get all networks for an empire.
     *
     * @param ownerEmpireId the empire
     * @return list of all networks
     */
    public List<SpyNetwork> getAllNetworks(Long ownerEmpireId) {
        return spyNetworkRepository.findByOwnerEmpireId(ownerEmpireId);
    }

    /**
     * Get all networks in a territory.
     *
     * @param locationId the territory
     * @return list of networks
     */
    public List<SpyNetwork> getNetworksInTerritory(Long locationId) {
        return spyNetworkRepository.findByLocationId(locationId);
    }
}

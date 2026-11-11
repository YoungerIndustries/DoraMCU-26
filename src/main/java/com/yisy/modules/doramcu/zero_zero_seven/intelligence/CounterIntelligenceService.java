package com.yisy.modules.doramcu.zero_zero_seven.intelligence;

import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.repository.SpyNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Service for detecting and neutralizing enemy spies.
 * <p>
 * Counter-intelligence operations protect an empire's territories from
 * foreign spy networks and agents, reducing their effectiveness or eliminating them.
 * </p>
 */
@Service
public class CounterIntelligenceService {

    private final SpyNetworkRepository spyNetworkRepository;
    private final Random random;

    public CounterIntelligenceService(SpyNetworkRepository spyNetworkRepository) {
        this.spyNetworkRepository = spyNetworkRepository;
        this.random = new Random();
    }

    /**
     * Detect enemy spy networks in a territory.
     *
     * @param locationId the territory to check
     * @param ownerEmpireId the empire conducting counter-intelligence
     * @return list of detected enemy networks
     */
    public List<SpyNetwork> detectEnemyNetworks(Long locationId, Long ownerEmpireId) {
        List<SpyNetwork> networks = spyNetworkRepository.findByLocationId(locationId);
        return networks.stream()
                .filter(n -> !n.getOwnerEmpireId().equals(ownerEmpireId) && n.isActive())
                .toList();
    }

    /**
     * Attempt to neutralize an enemy spy network.
     *
     * @param networkId the spy network to neutralize
     * @param counterIntelligenceStrength strength of the counter-intelligence effort (1-10)
     * @return true if the network was neutralized
     */
    @Transactional
    public boolean neutralizeNetwork(Long networkId, int counterIntelligenceStrength) {
        SpyNetwork network = spyNetworkRepository.findById(networkId)
                .orElseThrow(() -> new IllegalArgumentException("Spy network not found: " + networkId));

        // Success chance based on CI strength vs network level
        int successChance = counterIntelligenceStrength * 10 - network.getLevel() * 5;
        int roll = random.nextInt(100);

        if (roll < successChance) {
            network.setActive(false);
            spyNetworkRepository.save(network);
            return true;
        }

        // Failed attempt increases detection risk
        network.setDetectionRisk(Math.min(100, network.getDetectionRisk() + 15));
        spyNetworkRepository.save(network);
        return false;
    }

    /**
     * Check detection risk for all networks in a territory.
     *
     * @param locationId the territory
     * @return average detection risk
     */
    public int getAverageDetectionRisk(Long locationId) {
        List<SpyNetwork> networks = spyNetworkRepository.findByLocationId(locationId);
        if (networks.isEmpty()) return 0;

        return (int) networks.stream()
                .filter(SpyNetwork::isActive)
                .mapToInt(SpyNetwork::getDetectionRisk)
                .average()
                .orElse(0);
    }

    /**
     * Run counter-intelligence sweep across all territories owned by an empire.
     *
     * @param ownerEmpireId the empire conducting the sweep
     * @param counterIntelligenceStrength strength of the sweep
     * @return number of networks neutralized
     */
    @Transactional
    public int conductCISweep(Long ownerEmpireId, int counterIntelligenceStrength) {
        List<SpyNetwork> allNetworks = spyNetworkRepository.findAll();
        int neutralized = 0;

        for (SpyNetwork network : allNetworks) {
            if (!network.getOwnerEmpireId().equals(ownerEmpireId) && network.isActive()) {
                if (neutralizeNetwork(network.getId(), counterIntelligenceStrength)) {
                    neutralized++;
                }
            }
        }

        return neutralized;
    }
}

package com.yisy.modules.doramcu.zero_zero_seven.networks;

import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.repository.SpyNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing safehouses within spy networks.
 * <p>
 * Safehouses provide cover for agents and reduce detection risk.
 * More safehouses mean better network resilience.
 * </p>
 */
@Service
public class SafehouseService {

    private final SpyNetworkRepository spyNetworkRepository;

    public SafehouseService(SpyNetworkRepository spyNetworkRepository) {
        this.spyNetworkRepository = spyNetworkRepository;
    }

    /**
     * Build a new safehouse for a spy network.
     *
     * @param networkId the spy network
     * @return the updated network
     */
    @Transactional
    public SpyNetwork buildSafehouse(Long networkId) {
        SpyNetwork network = spyNetworkRepository.findById(networkId)
                .orElseThrow(() -> new IllegalArgumentException("Spy network not found: " + networkId));

        network.setSafehouseCount(network.getSafehouseCount() + 1);
        // Each safehouse reduces detection risk by 2%
        network.setDetectionRisk(Math.max(0, network.getDetectionRisk() - 2));

        return spyNetworkRepository.save(network);
    }

    /**
     * Get total safehouses for an empire.
     *
     * @param ownerEmpireId the empire
     * @return total safehouse count
     */
    public int getTotalSafehouses(Long ownerEmpireId) {
        return spyNetworkRepository.findByOwnerEmpireIdAndActive(ownerEmpireId, true)
                .stream()
                .mapToInt(SpyNetwork::getSafehouseCount)
                .sum();
    }

    /**
     * Calculate detection risk reduction from safehouses.
     *
     * @param safehouseCount number of safehouses
     * @return detection risk reduction percentage
     */
    public int getDetectionRiskReduction(int safehouseCount) {
        return safehouseCount * 2; // 2% per safehouse
    }
}

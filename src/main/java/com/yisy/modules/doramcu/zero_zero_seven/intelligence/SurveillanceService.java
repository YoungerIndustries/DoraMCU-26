package com.yisy.modules.doramcu.zero_zero_seven.intelligence;

import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.repository.SpyNetworkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for monitoring enemy activities.
 * <p>
 * Surveillance operations provide ongoing intelligence on enemy movements,
 * troop deployments, and strategic plans.
 * </p>
 */
@Service
public class SurveillanceService {

    private final SpyNetworkRepository spyNetworkRepository;

    public SurveillanceService(SpyNetworkRepository spyNetworkRepository) {
        this.spyNetworkRepository = spyNetworkRepository;
    }

    /**
     * Monitor a target territory for enemy activity.
     *
     * @param locationId the territory to monitor
     * @return list of spy networks in the location
     */
    public List<SpyNetwork> monitorTerritory(Long locationId) {
        return spyNetworkRepository.findByLocationId(locationId);
    }

    /**
     * Calculate surveillance coverage for an empire.
     *
     * @param ownerEmpireId the empire
     * @return number of territories under surveillance
     */
    public int getSurveillanceCoverage(Long ownerEmpireId) {
        return spyNetworkRepository.findByOwnerEmpireIdAndActive(ownerEmpireId, true).size();
    }

    /**
     * Check if a territory is under surveillance by an empire.
     *
     * @param locationId the territory
     * @param ownerEmpireId the empire
     * @return true if under surveillance
     */
    public boolean isUnderSurveillance(Long locationId, Long ownerEmpireId) {
        List<SpyNetwork> networks = spyNetworkRepository.findByLocationId(locationId);
        return networks.stream()
                .anyMatch(n -> n.getOwnerEmpireId().equals(ownerEmpireId) && n.isActive());
    }
}

package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpyNetworkRepository extends JpaRepository<SpyNetwork, Long> {
    List<SpyNetwork> findByGameSessionId(Long gameSessionId);
    List<SpyNetwork> findByOwnerEmpireId(Long ownerEmpireId);
    List<SpyNetwork> findByLocationId(Long locationId);
    List<SpyNetwork> findByOwnerEmpireIdAndActive(Long ownerEmpireId, boolean active);
}

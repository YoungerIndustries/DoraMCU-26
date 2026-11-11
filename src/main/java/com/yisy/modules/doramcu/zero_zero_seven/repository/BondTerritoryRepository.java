package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondTerritory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BondTerritoryRepository extends JpaRepository<BondTerritory, Long> {
    List<BondTerritory> findByGameSessionId(Long gameSessionId);
    List<BondTerritory> findByOwnerEmpireId(Long ownerEmpireId);
    Optional<BondTerritory> findByGameSessionIdAndHexXAndHexY(Long gameSessionId, int hexX, int hexY);
    List<BondTerritory> findByHexXAndHexY(int hexX, int hexY);
}

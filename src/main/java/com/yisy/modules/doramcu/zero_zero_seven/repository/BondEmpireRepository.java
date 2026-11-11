package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondEmpire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BondEmpireRepository extends JpaRepository<BondEmpire, Long> {
    List<BondEmpire> findByGameSessionId(Long gameSessionId);
    BondEmpire findByGameSessionIdAndName(Long gameSessionId, String name);
    List<BondEmpire> findByAlignment(BondEmpire.ColdWarAlignment alignment);
}

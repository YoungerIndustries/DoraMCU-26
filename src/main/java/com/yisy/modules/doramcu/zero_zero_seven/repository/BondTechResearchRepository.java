package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondTechResearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BondTechResearchRepository extends JpaRepository<BondTechResearch, Long> {
    List<BondTechResearch> findByGameSessionId(Long gameSessionId);
    List<BondTechResearch> findByOwnerEmpireId(Long ownerEmpireId);
    List<BondTechResearch> findByOwnerEmpireIdAndCompleted(Long ownerEmpireId, boolean completed);
    List<BondTechResearch> findByCategory(BondTechResearch.ResearchCategory category);
}

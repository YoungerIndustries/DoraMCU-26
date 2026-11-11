package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondDiplomaticRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BondDiplomaticRelationRepository extends JpaRepository<BondDiplomaticRelation, Long> {
    List<BondDiplomaticRelation> findByGameSessionId(Long gameSessionId);
    Optional<BondDiplomaticRelation> findByGameSessionIdAndEmpire1NameAndEmpire2Name(
            Long gameSessionId, String empire1Name, String empire2Name);
    List<BondDiplomaticRelation> findByEmpire1Name(String empire1Name);
    List<BondDiplomaticRelation> findByEmpire2Name(String empire2Name);
    List<BondDiplomaticRelation> findByAtWar(boolean atWar);
}

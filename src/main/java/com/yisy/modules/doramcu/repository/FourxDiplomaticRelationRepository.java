package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.fourx.FourxDiplomaticRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FourxDiplomaticRelationRepository extends JpaRepository<FourxDiplomaticRelationEntity, Long> {

    Optional<FourxDiplomaticRelationEntity> findByRelationId(String relationId);

    List<FourxDiplomaticRelationEntity> findByFaction1IdOrFaction2Id(String faction1Id, String faction2Id);

    List<FourxDiplomaticRelationEntity> findBySession_SessionId(String sessionId);
}

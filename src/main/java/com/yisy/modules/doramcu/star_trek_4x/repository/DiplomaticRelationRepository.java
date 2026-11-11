package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.DiplomaticRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiplomaticRelationRepository extends JpaRepository<DiplomaticRelation, Long> {
    List<DiplomaticRelation> findByGameSessionId(Long gameSessionId);
    Optional<DiplomaticRelation> findByFaction1NameAndFaction2Name(String faction1Name, String faction2Name);
    Optional<DiplomaticRelation> findByFaction2NameAndFaction1Name(String faction2Name, String faction1Name);
    List<DiplomaticRelation> findByFaction1NameOrFaction2Name(String faction1Name, String faction2Name);
}

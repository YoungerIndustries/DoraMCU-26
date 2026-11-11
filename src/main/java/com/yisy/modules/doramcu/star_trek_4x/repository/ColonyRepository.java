package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.Colony;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColonyRepository extends JpaRepository<Colony, Long> {
    List<Colony> findByGameSessionId(Long gameSessionId);
    List<Colony> findByOwnerFactionId(Long ownerFactionId);
    List<Colony> findByStarSystemId(Long starSystemId);
}

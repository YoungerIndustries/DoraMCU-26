package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StarTrekFactionRepository extends JpaRepository<StarTrekFaction, Long> {
    List<StarTrekFaction> findByGameSessionId(Long gameSessionId);
    StarTrekFaction findByGameSessionIdAndName(Long gameSessionId, String name);
}

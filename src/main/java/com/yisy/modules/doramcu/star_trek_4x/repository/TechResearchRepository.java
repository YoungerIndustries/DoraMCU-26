package com.yisy.modules.doramcu.star_trek_4x.repository;

import com.yisy.modules.doramcu.star_trek_4x.model.TechResearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechResearchRepository extends JpaRepository<TechResearch, Long> {
    List<TechResearch> findByOwnerFactionId(Long ownerFactionId);
    List<TechResearch> findByGameSessionId(Long gameSessionId);
}

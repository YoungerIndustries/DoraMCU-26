package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eSessionRepository extends JpaRepository<Dnd5eSession, Long> {

    List<Dnd5eSession> findByCampaignIdOrderBySessionNumberDesc(Long campaignId);

    List<Dnd5eSession> findByCampaignIdAndSessionNameContainingIgnoreCase(Long campaignId, String name);
}

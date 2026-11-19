package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrSessionRepository extends JpaRepository<LotrSession, Long> {

    List<LotrSession> findByCampaignId(Long campaignId);

    List<LotrSession> findByCampaignIdOrderBySessionNumberDesc(Long campaignId);
}

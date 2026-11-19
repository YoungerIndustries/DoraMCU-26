package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCampaignRepository extends JpaRepository<LotrCampaign, Long> {

    List<LotrCampaign> findByNameContainingIgnoreCase(String name);

    List<LotrCampaign> findByActiveTrue();

    List<LotrCampaign> findByQuestType(String questType);
}

package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eCampaignRepository extends JpaRepository<Dnd5eCampaign, Long> {

    List<Dnd5eCampaign> findByNameContainingIgnoreCase(String name);

    List<Dnd5eCampaign> findByActive(boolean active);
}

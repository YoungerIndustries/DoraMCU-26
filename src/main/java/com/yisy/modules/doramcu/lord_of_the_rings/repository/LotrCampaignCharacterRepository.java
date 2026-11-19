package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaignCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCampaignCharacterRepository extends JpaRepository<LotrCampaignCharacter, Long> {

    List<LotrCampaignCharacter> findByCampaignId(Long campaignId);

    List<LotrCampaignCharacter> findByCharacterId(Long characterId);
}

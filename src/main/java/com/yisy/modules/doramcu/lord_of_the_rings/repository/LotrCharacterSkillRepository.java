package com.yisy.modules.doramcu.lord_of_the_rings.repository;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacterSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotrCharacterSkillRepository extends JpaRepository<LotrCharacterSkill, Long> {

    List<LotrCharacterSkill> findByCharacterId(Long characterId);

    List<LotrCharacterSkill> findBySkill(String skill);
}

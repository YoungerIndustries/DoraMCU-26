package com.yisy.modules.doramcu.hex.repository;

import com.yisy.modules.doramcu.hex.model.CrewMemberSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewMemberSkillRepository extends JpaRepository<CrewMemberSkill, Long> {

    /**
     * Find all skills for a specific AI character.
     */
    List<CrewMemberSkill> findByAiCharacterId(Long aiCharacterId);

    /**
     * Find all skills in a given category (MINING, ENGINEERING, SCIENCE, COMBAT, TRADE).
     */
    List<CrewMemberSkill> findBySkillCategory(CrewMemberSkill.SkillCategory skillCategory);

    /**
     * Find a specific skill for a character in a given category.
     */
    Optional<CrewMemberSkill> findByAiCharacterIdAndSkillCategory(Long aiCharacterId, CrewMemberSkill.SkillCategory skillCategory);

    /**
     * Find all characters with a skill level greater than or equal to the threshold in a given category.
     */
    List<CrewMemberSkill> findBySkillCategoryAndSkillLevelGreaterThanEqual(CrewMemberSkill.SkillCategory skillCategory, int skillLevel);
}

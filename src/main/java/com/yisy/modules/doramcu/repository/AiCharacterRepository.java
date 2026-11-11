package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiCharacterRepository extends JpaRepository<AiCharacter, Long> {

    Optional<AiCharacter> findByName(String name);

    List<AiCharacter> findByEmpireName(String empireName);

    List<AiCharacter> findByActive(boolean active);

    /**
     * Find hirable AI characters.
     */
    List<AiCharacter> findByHirableTrueAndHiringStatus(AiCharacter.HiringStatus status);

    /**
     * Find AI characters by role.
     */
    List<AiCharacter> findByRole(String role);

    /**
     * Find AI characters with minimum experience.
     */
    List<AiCharacter> findByTotalExperienceGreaterThanEqualAndHirableTrue(int minExperience);

    /**
     * Count hirable AI characters.
     */
    long countByHirableTrue();

    /**
     * Find AI admin tester characters.
     */
    java.util.List<AiCharacter> findByAiAdminTrue();

    /**
     * Find AI admin characters that are active, filtered by empire name.
     */
    java.util.List<AiCharacter> findByAiAdminTrueAndActiveTrueAndEmpireName(String empireName);

    /**
     * Find AI characters by role that are active, ordered by total experience descending.
     */
    List<AiCharacter> findByRoleAndActiveTrueOrderByTotalExperienceDesc(String role);
}

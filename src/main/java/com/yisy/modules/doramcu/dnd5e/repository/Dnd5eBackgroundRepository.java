package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eBackgroundRepository extends JpaRepository<Dnd5eBackground, Long> {

    Optional<Dnd5eBackground> findByName(String name);

    List<Dnd5eBackground> findByCategoryAndActiveTrue(String category);

    List<Dnd5eBackground> findByActiveTrue();

    @Query("SELECT b FROM Dnd5eBackground b WHERE b.active = true AND LOWER(b.skillProficiencies) LIKE LOWER(CONCAT('%:', :skill, '%'))")
    List<Dnd5eBackground> findBySkillProficiency(@Param("skill") String skill);

    @Query("SELECT b FROM Dnd5eBackground b WHERE b.active = true AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eBackground> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByName(String name);
}

package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSubclass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eSubclassRepository extends JpaRepository<Dnd5eSubclass, Long> {

    Optional<Dnd5eSubclass> findByName(String name);

    List<Dnd5eSubclass> findByClassNameAndActiveTrue(String className);

    List<Dnd5eSubclass> findByActiveTrue();

    @Query("SELECT s FROM Dnd5eSubclass s WHERE s.active = true AND LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eSubclass> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByName(String name);
}

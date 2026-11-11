package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eClassFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Dnd5eClassFeatureRepository extends JpaRepository<Dnd5eClassFeature, Long> {

    List<Dnd5eClassFeature> findByClassNameAndActiveTrue(String className);

    List<Dnd5eClassFeature> findByClassNameAndLevelAndActiveTrue(String className, int level);

    List<Dnd5eClassFeature> findByClassNameAndSubclassAndActiveTrue(String className, String subclass);

    List<Dnd5eClassFeature> findByClassNameAndLevelAndSubclassAndActiveTrue(String className, int level, String subclass);

    List<Dnd5eClassFeature> findBySubclassAndActiveTrue(String subclass);

    @Query("SELECT f FROM Dnd5eClassFeature f WHERE f.active = true AND f.className = :className " +
           "AND f.level <= :level ORDER BY f.level ASC")
    List<Dnd5eClassFeature> findFeaturesUpToLevel(@Param("className") String className, @Param("level") int level);

    @Query("SELECT f FROM Dnd5eClassFeature f WHERE f.active = true AND f.className = :className " +
           "AND f.subclass = :subclass AND f.level <= :level ORDER BY f.level ASC")
    List<Dnd5eClassFeature> findSubclassFeaturesUpToLevel(@Param("className") String className,
                                                          @Param("subclass") String subclass,
                                                          @Param("level") int level);

    @Query("SELECT f FROM Dnd5eClassFeature f WHERE f.active = true AND LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dnd5eClassFeature> searchByName(@Param("keyword") String keyword);
}

package com.yisy.modules.doramcu.dnd5e.repository;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Dnd5eSpeciesRepository extends JpaRepository<Dnd5eSpecies, Long> {

    Optional<Dnd5eSpecies> findByName(String name);

    List<Dnd5eSpecies> findByCategory(String category);

    List<Dnd5eSpecies> findBySize(String size);

    List<Dnd5eSpecies> findBySubspecies(String subspecies);

    List<Dnd5eSpecies> findByCategoryAndSize(String category, String size);

    @Query("SELECT s FROM Dnd5eSpecies s WHERE s.darkvisionRange > 0")
    List<Dnd5eSpecies> findByDarkvision();

    @Query("SELECT s FROM Dnd5eSpecies s WHERE s.lifespan >= :minLifespan")
    List<Dnd5eSpecies> findByLifespanGte(@Param("minLifespan") int minLifespan);

    @Query("SELECT s FROM Dnd5eSpecies s WHERE s.name LIKE %:keyword% OR s.description LIKE %:keyword%")
    List<Dnd5eSpecies> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM Dnd5eSpecies s WHERE s.racialTraits LIKE %:trait%")
    List<Dnd5eSpecies> findByRacialTrait(@Param("trait") String trait);

    boolean existsByName(String name);
}

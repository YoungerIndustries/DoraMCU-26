package com.yisy.modules.doramcu.corporation.repository;

import com.yisy.modules.doramcu.corporation.model.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {

    /**
     * Find all members of a crew.
     */
    List<CrewMember> findByCrewId(Long crewId);

    /**
     * Find the crew membership for a specific hiring record.
     */
    Optional<CrewMember> findByHiringId(Long hiringId);

    /**
     * Delete all members of a crew.
     */
    void deleteByCrewId(Long crewId);

    /**
     * Check if a hiring record is already in a crew.
     */
    boolean existsByHiringId(Long hiringId);
}

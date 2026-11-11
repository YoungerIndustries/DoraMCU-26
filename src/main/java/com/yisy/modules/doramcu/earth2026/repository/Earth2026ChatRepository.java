package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Earth2026ChatRepository extends JpaRepository<Earth2026Chat, Long> {

    List<Earth2026Chat> findByLocationIdOrderByCreatedAtDesc(Long locationId);

    List<Earth2026Chat> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(Long senderId, Long recipientId);

    List<Earth2026Chat> findByPartyIdOrderByCreatedAtDesc(Long partyId);

    List<Earth2026Chat> findByMessageTypeOrderByCreatedAtDesc(String messageType);

    long countByLocationId(Long locationId);
}

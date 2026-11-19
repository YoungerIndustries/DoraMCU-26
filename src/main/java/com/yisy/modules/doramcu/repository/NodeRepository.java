package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findByNodeId(String nodeId);

    Optional<Node> findByDeviceId(String deviceId);

    List<Node> findByNodeType(String nodeType);

    List<Node> findByStatus(String status);

    @Query("SELECT n FROM Node n WHERE n.lastHeartbeat < :threshold")
    List<Node> findStaleNodes(@Param("threshold") LocalDateTime threshold);

    void deleteByNodeId(String nodeId);
}

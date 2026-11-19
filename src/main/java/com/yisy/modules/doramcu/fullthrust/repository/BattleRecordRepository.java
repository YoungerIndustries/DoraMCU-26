package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.BattleRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BattleRecordRepository extends JpaRepository<BattleRecordEntity, Long> {
    List<BattleRecordEntity> findByPlayerAccountIdOrderByCompletedAtDesc(Long playerAccountId);
    List<BattleRecordEntity> findByGameSessionId(String gameSessionId);
    List<BattleRecordEntity> findByPlayerNameOrderByCompletedAtDesc(String playerName);
}

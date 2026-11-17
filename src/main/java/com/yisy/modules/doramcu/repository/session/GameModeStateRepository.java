package com.yisy.modules.doramcu.repository.session;

import com.yisy.modules.doramcu.model.session.GameModeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameModeStateRepository extends JpaRepository<GameModeState, Long> {

    Optional<GameModeState> findByGalaxyNameAndGameMode(String galaxyName, String gameMode);

    List<GameModeState> findByGalaxyName(String galaxyName);

    void deleteByGalaxyNameAndGameMode(String galaxyName, String gameMode);
}

package com.yisy.modules.doramcu.repository.registry;

import com.yisy.modules.doramcu.model.registry.GameObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link GameObjectEntity}.
 * <p>
 * Provides query methods for filtering game objects by game module,
 * object type, and UUID — used by the FPS scene decoration system
 * to load data-driven props for 3D maps.
 * </p>
 */
@Repository
public interface GameObjectRepository extends JpaRepository<GameObjectEntity, Long> {

    /**
     * Find all objects registered for a specific game module (theme).
     */
    List<GameObjectEntity> findByGameModule(String gameModule);

    /**
     * Find objects for a specific game module and object type.
     */
    List<GameObjectEntity> findByGameModuleAndObjectType(String gameModule, String objectType);

    /**
     * Find a single object by its UUID.
     */
    Optional<GameObjectEntity> findByObjectUuid(String objectUuid);

    /**
     * Find all objects for a list of game modules.
     */
    List<GameObjectEntity> findByGameModuleIn(List<String> gameModules);

    /**
     * Find objects by object type across all game modules.
     */
    List<GameObjectEntity> findByObjectType(String objectType);

    /**
     * Find objects by game module and subtype.
     */
    List<GameObjectEntity> findByGameModuleAndObjectSubtype(String gameModule, String objectSubtype);

    /**
     * Count objects by game module.
     */
    long countByGameModule(String gameModule);

    /**
     * Get distinct object types for a game module.
     */
    @Query("SELECT DISTINCT go.objectType FROM GameObjectEntity go WHERE go.gameModule = :gameModule")
    List<String> findDistinctObjectTypesByGameModule(@Param("gameModule") String gameModule);
}

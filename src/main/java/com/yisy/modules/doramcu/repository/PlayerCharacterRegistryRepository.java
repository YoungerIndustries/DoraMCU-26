package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.model.CharacterType;
import com.yisy.modules.doramcu.model.PlayerCharacterRegistry;
import com.yisy.modules.doramcu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerCharacterRegistryRepository extends JpaRepository<PlayerCharacterRegistry, Long> {

    /** Get all registry entries for a player. */
    List<PlayerCharacterRegistry> findByPlayer(User player);

    /** Get registry entries filtered by player and role. */
    List<PlayerCharacterRegistry> findByPlayerAndCharacterRole(User player, CharacterRole characterRole);

    /** Get registry entries filtered by player and galaxy. */
    List<PlayerCharacterRegistry> findByPlayerAndGalaxyName(User player, String galaxyName);

    /** Get crew members in a specific galaxy for a player. */
    List<PlayerCharacterRegistry> findByPlayerAndCharacterRoleAndGalaxyName(User player, CharacterRole characterRole, String galaxyName);

    /** Find registry entry by character type and ID. */
    List<PlayerCharacterRegistry> findByCharacterTypeAndCharacterId(CharacterType characterType, Long characterId);

    /** Remove a character from registry. */
    @Modifying
    @Query("DELETE FROM PlayerCharacterRegistry p WHERE p.player.id = :playerId AND p.characterId = :characterId")
    void deleteByPlayerAndCharacterId(@Param("playerId") Long playerId, @Param("characterId") Long characterId);

    /** Check if a character is already registered for a player. */
    boolean existsByPlayerAndCharacterTypeAndCharacterId(User player, CharacterType characterType, Long characterId);
}

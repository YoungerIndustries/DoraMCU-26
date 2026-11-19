package com.yisy.modules.doramcu.fullthrust.service;

import com.yisy.modules.doramcu.fullthrust.entity.BattleRecordEntity;
import com.yisy.modules.doramcu.fullthrust.entity.PlayerAccountEntity;
import com.yisy.modules.doramcu.fullthrust.model.GameSession;
import com.yisy.modules.doramcu.fullthrust.model.Player;
import com.yisy.modules.doramcu.fullthrust.model.Ship;
import com.yisy.modules.doramcu.fullthrust.repository.BattleRecordRepository;
import com.yisy.modules.doramcu.fullthrust.repository.PlayerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing persistent player accounts and battle records.
 */
@Service
public class PlayerAccountService {

    private final PlayerAccountRepository playerAccountRepository;
    private final BattleRecordRepository battleRecordRepository;

    public PlayerAccountService(PlayerAccountRepository playerAccountRepository,
                                BattleRecordRepository battleRecordRepository) {
        this.playerAccountRepository = playerAccountRepository;
        this.battleRecordRepository = battleRecordRepository;
    }

    /**
     * Create default admin account on application startup if it doesn't already exist.
     */
    @PostConstruct
    @Transactional
    public void initDefaultAdmin() {
        if (!playerAccountRepository.existsByUsername("admin")) {
            PlayerAccountEntity admin = new PlayerAccountEntity();
            admin.setUsername("admin");
            admin.setPasswordHash(hashPassword("admin"));
            admin.setFaction("Federation");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setLastLoginAt(LocalDateTime.now());
            playerAccountRepository.save(admin);
            System.out.println("[PlayerAccountService] Default admin account created (username: admin, password: admin)");
        }

        // Create default account for kenbarksdale66@gmail.com if it doesn't exist
        if (!playerAccountRepository.existsByGoogleEmail("kenbarksdale66@gmail.com")) {
            PlayerAccountEntity kenAccount = new PlayerAccountEntity();
            kenAccount.setUsername("ken");
            kenAccount.setPasswordHash(hashPassword("Riggs2012"));
            kenAccount.setGoogleEmail("kenbarksdale66@gmail.com");
            kenAccount.setFaction("Federation");
            kenAccount.setCreatedAt(LocalDateTime.now());
            kenAccount.setLastLoginAt(LocalDateTime.now());
            playerAccountRepository.save(kenAccount);
            System.out.println("[PlayerAccountService] Default account created for kenbarksdale66@gmail.com (username: ken)");
        }
    }

    /**
     * Register a new player account.
     */
    @Transactional
    public PlayerAccountEntity register(String username, String password, String faction) {
        if (playerAccountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }

        PlayerAccountEntity account = new PlayerAccountEntity();
        account.setUsername(username);
        account.setPasswordHash(hashPassword(password));
        account.setFaction(faction != null && !faction.isEmpty() ? faction : "Independent");
        account.setCreatedAt(LocalDateTime.now());
        account.setLastLoginAt(LocalDateTime.now());
        return playerAccountRepository.save(account);
    }

    /**
     * Authenticate a player and return their account.
     */
    @Transactional
    public Optional<PlayerAccountEntity> login(String username, String password) {
        return playerAccountRepository.findByUsername(username)
                .filter(account -> account.getPasswordHash().equals(hashPassword(password)))
                .map(account -> {
                    account.setLastLoginAt(LocalDateTime.now());
                    return playerAccountRepository.save(account);
                });
    }

    /**
     * Find an account by username (without authentication).
     */
    @Transactional
    public Optional<PlayerAccountEntity> findByUsername(String username) {
        return playerAccountRepository.findByUsername(username);
    }

    /**
     * Get all accounts.
     */
    @Transactional
    public List<PlayerAccountEntity> getAllAccounts() {
        return playerAccountRepository.findAll();
    }

    /**
     * Get battle records for a player account.
     */
    @Transactional
    public List<BattleRecordEntity> getBattleRecords(Long playerAccountId) {
        return battleRecordRepository.findByPlayerAccountIdOrderByCompletedAtDesc(playerAccountId);
    }

    /**
     * Get battle records by player name.
     */
    @Transactional
    public List<BattleRecordEntity> getBattleRecordsByName(String playerName) {
        return battleRecordRepository.findByPlayerNameOrderByCompletedAtDesc(playerName);
    }

    /**
     * Get battle records for a specific game session.
     */
    @Transactional
    public List<BattleRecordEntity> getBattleRecordsForGame(String gameSessionId) {
        return battleRecordRepository.findByGameSessionId(gameSessionId);
    }

    /**
     * Record battle results when a game ends.
     * This method is called by GameService when a game session ends.
     */
    @Transactional
    public void recordBattleResults(GameSession session) {
        String sessionId = session.getId();
        String gameName = session.getName();
        String winnerId = session.getWinnerId();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, Player> entry : session.getPlayers().entrySet()) {
            String playerId = entry.getKey();
            Player player = entry.getValue();

            // Skip AI players for account updates, but still record their battles
            BattleRecordEntity record = new BattleRecordEntity();
            record.setGameSessionId(sessionId);
            record.setGameName(gameName);
            record.setPlayerId(playerId);
            record.setPlayerName(player.getName());
            record.setFaction(player.getFaction());
            record.setAI(player.isAI());
            record.setAiDifficulty(player.getAiDifficulty());
            record.setWon(playerId.equals(winnerId));
            record.setCompletedAt(now);

            // Calculate player's battle stats
            int shipsDeployed = 0;
            int shipsLost = 0;
            int shipsDestroyed = 0;
            int damageDealt = 0;
            int damageReceived = 0;
            int totalFinalHull = 0;
            int totalFinalShield = 0;
            int totalFinalFuel = 0;

            for (String shipId : player.getShipIds()) {
                Ship ship = session.getShips().get(shipId);
                if (ship != null) {
                    shipsDeployed++;
                    int maxHull = ship.getMaxHull();
                    int maxShield = ship.getMaxShield();
                    int maxFuel = ship.getMaxFuel();

                    int shipDamageReceived = (maxHull - ship.getCurrentHull()) + (maxShield - ship.getCurrentShield());
                    damageReceived += shipDamageReceived;

                    if (ship.getCurrentHull() <= 0) {
                        shipsLost++;
                    }

                    totalFinalHull += Math.max(0, ship.getCurrentHull());
                    totalFinalShield += Math.max(0, ship.getCurrentShield());
                    totalFinalFuel += Math.max(0, ship.getCurrentFuel());
                }
            }

            // Count ships destroyed by parsing event log
            String playerName = player.getName();
            for (String event : session.getEventLog()) {
                if (event.contains(playerName + " destroys")) {
                    shipsDestroyed++;
                }
                // Parse damage dealt: "PlayerName's Ship fires Weapon at EnemyShip for X damage"
                if (event.contains(playerName + "'s") && event.contains("damage")) {
                    int damage = parseDamageFromEvent(event);
                    damageDealt += damage;
                }
            }

            record.setShipsDeployed(shipsDeployed);
            record.setShipsLost(shipsLost);
            record.setShipsDestroyed(shipsDestroyed);
            record.setDamageDealt(damageDealt);
            record.setDamageReceived(damageReceived);
            record.setTurnsSurvived(session.getCurrentTurn());
            record.setFinalHull(totalFinalHull);
            record.setFinalShield(totalFinalShield);
            record.setFinalFuel(totalFinalFuel);

            battleRecordRepository.save(record);

            // Update player account stats if they have one
            Optional<PlayerAccountEntity> accountOpt = playerAccountRepository.findByUsername(player.getName());
            if (accountOpt.isPresent()) {
                PlayerAccountEntity account = accountOpt.get();
                record.setPlayerAccountId(account.getId());
                account.setGamesPlayed(account.getGamesPlayed() + 1);
                if (playerId.equals(winnerId)) {
                    account.setGamesWon(account.getGamesWon() + 1);
                } else {
                    account.setGamesLost(account.getGamesLost() + 1);
                }
                account.setShipsDestroyed(account.getShipsDestroyed() + shipsDestroyed);
                account.setTotalDamageDealt(account.getTotalDamageDealt() + damageDealt);
                playerAccountRepository.save(account);

                // Update the record with the account ID after saving
                battleRecordRepository.save(record);
            }
        }
    }

    /**
     * Parse damage amount from an event log string.
     */
    private int parseDamageFromEvent(String event) {
        // Look for "for X damage" pattern
        int forIndex = event.lastIndexOf("for ");
        int damageIndex = event.lastIndexOf(" damage");
        if (forIndex >= 0 && damageIndex > forIndex) {
            try {
                String numStr = event.substring(forIndex + 4, damageIndex).trim();
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Hash a password using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

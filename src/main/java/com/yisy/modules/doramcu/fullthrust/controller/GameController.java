package com.yisy.modules.doramcu.fullthrust.controller;
import com.yisy.modules.doramcu.fullthrust.entity.BattleRecordEntity;
import com.yisy.modules.doramcu.fullthrust.entity.PlayerAccountEntity;
import com.yisy.modules.doramcu.fullthrust.handler.GameWebSocketHandler;
import com.yisy.modules.doramcu.fullthrust.model.GameSession;
import com.yisy.modules.doramcu.fullthrust.service.GameService;
import com.yisy.modules.doramcu.fullthrust.service.PlayerAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("gameController_fullthrust_controller_GameController")
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final GameWebSocketHandler webSocketHandler;
    private final PlayerAccountService playerAccountService;

    public GameController(GameService gameService, GameWebSocketHandler webSocketHandler, PlayerAccountService playerAccountService) {
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
        this.playerAccountService = playerAccountService;
    }

    @PostMapping("/create")
    public GameSession createGame(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "New Game");
        return gameService.createGame(name);
    }

    @GetMapping
    public java.util.List<GameSession> listSessions() {
        return gameService.listSessions();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<GameSession> getSession(@PathVariable String sessionId) {
        return gameService.getSession(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{sessionId}/players")
    public GameSession addPlayer(@PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String faction = (String) body.getOrDefault("faction", "Independent");
        Boolean isAI = body.get("isAI") != null ? (Boolean) body.get("isAI") : false;
        String aiDifficulty = (String) body.getOrDefault("aiDifficulty", "medium");
        return gameService.addPlayer(sessionId, name, faction, isAI, aiDifficulty);
    }

    @PostMapping("/{sessionId}/ships")
    public GameSession deployShip(@PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {
        String playerId = (String) body.get("playerId");
        String shipClassId = (String) body.get("shipClassId");
        String shipName = (String) body.get("shipName");
        int startQ = ((Number) body.get("startQ")).intValue();
        int startR = ((Number) body.get("startR")).intValue();
        return gameService.deployShip(sessionId, playerId, shipClassId, shipName, startQ, startR);
    }

    @PostMapping("/{sessionId}/start")
    public GameSession startGame(@PathVariable String sessionId) {
        return gameService.startGame(sessionId);
    }

    @PostMapping("/{sessionId}/move")
    public GameSession moveShip(@PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {
        String shipId = (String) body.get("shipId");
        int headingChange = ((Number) body.get("headingChange")).intValue();
        int thrust = ((Number) body.get("thrust")).intValue();
        return gameService.moveShip(sessionId, shipId, headingChange, thrust);
    }

    @PostMapping("/{sessionId}/fire")
    public GameSession fireWeapon(@PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {
        String shipId = (String) body.get("shipId");
        String targetId = (String) body.get("targetId");
        int weaponIndex = ((Number) body.get("weaponIndex")).intValue();
        return gameService.fireWeapon(sessionId, shipId, targetId, weaponIndex);
    }

    @PostMapping("/{sessionId}/end-turn")
    public GameSession endTurn(@PathVariable String sessionId) {
        return gameService.endTurn(sessionId);
    }

    @PostMapping("/{sessionId}/ai-turn")
    public GameSession processAITurn(@PathVariable String sessionId) {
        return gameService.processAITurn(sessionId);
    }

    @PostMapping("/{sessionId}/ai-all-turns")
    public GameSession processAllAITurns(@PathVariable String sessionId) {
        return gameService.processAllAITurns(sessionId);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteGame(@PathVariable String sessionId) {
        gameService.deleteGame(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sessionId}/players/connected")
    public List<Map<String, String>> getConnectedPlayers(@PathVariable String sessionId) {
        return webSocketHandler.getConnectedPlayers(sessionId);
    }

    // ==================== Player Account Endpoints ====================

    @PostMapping("/account/register")
    public ResponseEntity<?> registerAccount(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String faction = body.getOrDefault("faction", "Independent");
        try {
            PlayerAccountEntity account = playerAccountService.register(username, password, faction);
            return ResponseEntity.ok(Map.of(
                    "id", account.getId(),
                    "username", account.getUsername(),
                    "faction", account.getFaction(),
                    "message", "Account created successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/account/login")
    public ResponseEntity<?> loginAccount(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return playerAccountService.login(username, password)
                .map(account -> {
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("id", account.getId());
                    result.put("username", account.getUsername());
                    result.put("faction", account.getFaction());
                    result.put("googleEmail", account.getGoogleEmail());
                    result.put("gamesPlayed", account.getGamesPlayed());
                    result.put("gamesWon", account.getGamesWon());
                    result.put("gamesLost", account.getGamesLost());
                    result.put("shipsDestroyed", account.getShipsDestroyed());
                    result.put("totalDamageDealt", account.getTotalDamageDealt());
                    result.put("winRate", Math.round(account.getWinRate() * 10.0) / 10.0);
                    result.put("message", "Login successful");
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid username or password")));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getAccount(@PathVariable Long accountId) {
        return playerAccountService.getAllAccounts().stream()
                .filter(a -> a.getId().equals(accountId))
                .map(account -> {
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("id", account.getId());
                    result.put("username", account.getUsername());
                    result.put("faction", account.getFaction());
                    result.put("googleEmail", account.getGoogleEmail());
                    result.put("gamesPlayed", account.getGamesPlayed());
                    result.put("gamesWon", account.getGamesWon());
                    result.put("gamesLost", account.getGamesLost());
                    result.put("shipsDestroyed", account.getShipsDestroyed());
                    result.put("totalDamageDealt", account.getTotalDamageDealt());
                    result.put("winRate", Math.round(account.getWinRate() * 10.0) / 10.0);
                    result.put("createdAt", account.getCreatedAt());
                    result.put("lastLoginAt", account.getLastLoginAt());
                    return ResponseEntity.ok(result);
                })
                .findFirst()
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}/battles")
    public ResponseEntity<List<BattleRecordEntity>> getBattleRecords(@PathVariable Long accountId) {
        List<BattleRecordEntity> records = playerAccountService.getBattleRecords(accountId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{sessionId}/battles")
    public ResponseEntity<List<BattleRecordEntity>> getGameBattles(@PathVariable String sessionId) {
        List<BattleRecordEntity> records = playerAccountService.getBattleRecordsForGame(sessionId);
        return ResponseEntity.ok(records);
    }

    // ==================== Ship Library Endpoints ====================

    @GetMapping("/ship-classes")
    public List<com.yisy.modules.doramcu.fullthrust.model.ShipClass> getShipClasses() {
        return gameService.getDefaultShipClasses();
    }

    // ==================== Player Account Endpoints ====================

    @GetMapping("/accounts")
    public ResponseEntity<List<Map<String, Object>>> getAllAccounts() {
        List<Map<String, Object>> accounts = new java.util.ArrayList<>();
        for (PlayerAccountEntity account : playerAccountService.getAllAccounts()) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", account.getId());
            map.put("username", account.getUsername());
            map.put("faction", account.getFaction());
            map.put("googleEmail", account.getGoogleEmail());
            map.put("gamesPlayed", account.getGamesPlayed());
            map.put("gamesWon", account.getGamesWon());
            map.put("winRate", Math.round(account.getWinRate() * 10.0) / 10.0);
            accounts.add(map);
        }
        return ResponseEntity.ok(accounts);
    }

}

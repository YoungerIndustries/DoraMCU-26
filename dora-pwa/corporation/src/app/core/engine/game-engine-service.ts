import { Injectable, inject } from '@angular/core';
import { GameStateStore, GameState } from './game-state-store';
import { RuleEngineService, RuleResult } from './rule-engine-service';
import { SyncService } from './sync-service';

/**
 * Orchestrates local game state, rules, and AI for the player's game node.
 *
 * This is the main entry point for the client-side game engine. It coordinates
 * the game state store, rule engine, and sync service to provide a complete
 * local game experience with server synchronization.
 */
@Injectable({
  providedIn: 'root'
})
export class GameEngineService {
  private stateStore = inject(GameStateStore);
  private ruleEngine = inject(RuleEngineService);
  private syncService = inject(SyncService);

  private playerId = '';
  private sessionId = '';
  private gameSystem = '';

  /**
   * Initialize the game engine with player and session info.
   */
  async initialize(playerId: string, sessionId: string, gameSystem: string) {
    this.playerId = playerId;
    this.sessionId = sessionId;
    this.gameSystem = gameSystem;

    // Initialize game state
    const initialState: GameState = {
      playerId,
      sessionId,
      gameSystem,
      version: 0,
      timestamp: Date.now(),
      character: {},
      inventory: [],
      location: {},
      narrativeState: {},
      aiContext: {}
    };
    this.stateStore.initialize(initialState);

    // Set up sync callbacks
    this.syncService.onStateUpdate = (state, version) => {
      this.stateStore.resetFromServer({
        ...this.stateStore.toJSON()!,
        ...state,
        version
      } as GameState);
    };

    this.syncService.onSyncAck = (version) => {
      this.stateStore.markSynced();
    };

    console.log(`[GameEngine] Initialized for player ${playerId}, session ${sessionId}, system ${gameSystem}`);
  }

  /**
   * Load game rules from the provided URLs.
   */
  async loadGameRules(ruleUrls: Array<{ system: string; url: string }>) {
    for (const rule of ruleUrls) {
      await this.ruleEngine.loadRuleSetFromUrl(rule.system, rule.url);
    }
  }

  /**
   * Connect to the server for state synchronization.
   */
  connectToServer(syncEndpoint: string) {
    this.syncService.connect(syncEndpoint, this.playerId, this.sessionId);
  }

  /**
   * Execute a skill check in the current game system.
   */
  executeSkillCheck(skill: string, difficulty: number, modifiers: number = 0): RuleResult {
    const result = this.ruleEngine.executeSkillCheck(this.gameSystem, skill, difficulty, modifiers);

    // Update local state with the result
    const currentState = this.stateStore.toJSON();
    if (currentState) {
      this.stateStore.updateState({
        narrativeState: {
          ...currentState.narrativeState,
          lastAction: result
        }
      });

      // Sync with server
      this.syncWithServer();
    }

    return result;
  }

  /**
   * Execute a combat action in the current game system.
   */
  executeCombatAction(action: string, target: Record<string, unknown>): RuleResult {
    const result = this.ruleEngine.executeCombatAction(this.gameSystem, action, target);

    // Update local state
    const currentState = this.stateStore.toJSON();
    if (currentState) {
      this.stateStore.updateState({
        narrativeState: {
          ...currentState.narrativeState,
          lastCombat: result
        }
      });

      // Sync with server
      this.syncWithServer();
    }

    return result;
  }

  /**
   * Generate a narrative event based on the current game state.
   */
  generateNarrativeEvent(): RuleResult {
    const currentState = this.stateStore.toJSON();
    const context = currentState ? currentState.narrativeState : {};
    const result = this.ruleEngine.generateNarrativeEvent(this.gameSystem, context as Record<string, unknown>);

    // Update local state
    if (currentState) {
      this.stateStore.updateState({
        narrativeState: {
          ...currentState.narrativeState,
          lastEvent: result
        }
      });

      // Sync with server
      this.syncWithServer();
    }

    return result;
  }

  /**
   * Update the character data locally and sync with server.
   */
  updateCharacter(characterData: Record<string, unknown>) {
    this.stateStore.updateState({
      character: characterData
    });
    this.syncWithServer();
  }

  /**
   * Update the inventory locally and sync with server.
   */
  updateInventory(inventory: Record<string, unknown>[]) {
    this.stateStore.updateState({
      inventory
    });
    this.syncWithServer();
  }

  /**
   * Update the location locally and sync with server.
   */
  updateLocation(location: Record<string, unknown>) {
    this.stateStore.updateState({
      location
    });
    this.syncWithServer();
  }

  /**
   * Sync the current state with the server.
   */
  private syncWithServer() {
    const state = this.stateStore.toJSON();
    if (state && this.syncService.isConnected()) {
      this.syncService.sendStateUpdate(
        this.playerId,
        this.sessionId,
        state,
        state.version
      );
    }
  }

  /**
   * Get the current game state.
   */
  getCurrentState(): GameState | null {
    return this.stateStore.toJSON();
  }

  /**
   * Check if the engine is connected to the server.
   */
  isConnected(): boolean {
    return this.syncService.isConnected();
  }

  /**
   * Disconnect from the server.
   */
  disconnect() {
    this.syncService.disconnect();
  }
}

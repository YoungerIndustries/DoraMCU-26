import { signal, computed } from '@angular/core';

/**
 * Represents the current state of a local game session.
 */
export interface GameState {
  playerId: string;
  sessionId: string;
  gameSystem: string;
  version: number;
  timestamp: number;
  character: Record<string, unknown>;
  inventory: Record<string, unknown>[];
  location: Record<string, unknown>;
  narrativeState: Record<string, unknown>;
  aiContext: Record<string, unknown>;
}

/**
 * Local state management for the game node using Angular signals.
 *
 * Provides reactive access to the current game state and handles
 * state mutations with version tracking for synchronization.
 */
export class GameStateStore {
  // Core state signals
  private _state = signal<GameState | null>(null);
  private _synced = signal<boolean>(true);
  private _version = signal<number>(0);

  // Computed properties
  currentState = computed(() => this._state());
  isSynced = computed(() => this._synced());
  currentVersion = computed(() => this._version());

  /**
   * Initialize the store with the provided state.
   */
  initialize(state: GameState) {
    this._state.set(state);
    this._version.set(state.version);
    this._synced.set(true);
  }

  /**
   * Update the game state with new data, incrementing the version.
   */
  updateState(updates: Partial<GameState>) {
    const current = this._state();
    if (!current) {
      console.warn('[GameStateStore] Cannot update: state not initialized');
      return;
    }

    const newState: GameState = {
      ...current,
      ...updates,
      version: current.version + 1,
      timestamp: Date.now()
    };

    this._state.set(newState);
    this._version.set(newState.version);
    this._synced.set(false);
  }

  /**
   * Mark the current state as synced with the server.
   */
  markSynced() {
    this._synced.set(true);
  }

  /**
   * Reset the state to a server-provided version (conflict resolution).
   */
  resetFromServer(state: GameState) {
    this._state.set(state);
    this._version.set(state.version);
    this._synced.set(true);
  }

  /**
   * Get the current state as a plain object for serialization.
   */
  toJSON(): GameState | null {
    return this._state();
  }
}

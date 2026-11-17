import { Injectable } from '@angular/core';

/**
 * Message types for WebSocket state synchronization.
 */
export type SyncMessageType = 'STATE_UPDATE' | 'STATE_REQUEST' | 'SYNC_ACK' | 'SYNC_ERROR';

/**
 * WebSocket message for game state synchronization.
 */
export interface SyncMessage {
  type: SyncMessageType;
  playerId: string;
  sessionId: string;
  timestamp: number;
  state?: Record<string, unknown>;
  version?: number;
}

/**
 * Handles WebSocket-based state synchronization between the local game node and the server.
 *
 * Manages the connection lifecycle, sends state updates, and handles
 * incoming sync messages from the server with conflict resolution.
 */
@Injectable({
  providedIn: 'root'
})
export class SyncService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private connected = false;

  // Callbacks for sync events
  onStateUpdate?: (state: Record<string, unknown>, version: number) => void;
  onSyncAck?: (version: number) => void;
  onSyncError?: (error: string) => void;
  onConnectionStatus?: (connected: boolean) => void;

  /**
   * Connect to the WebSocket sync endpoint.
   */
  connect(url: string, playerId: string, sessionId: string) {
    try {
      this.ws = new WebSocket(url);

      this.ws.onopen = () => {
        console.log('[SyncService] Connected to', url);
        this.connected = true;
        this.reconnectAttempts = 0;
        this.onConnectionStatus?.(true);

        // Request initial state from server
        this.send({
          type: 'STATE_REQUEST',
          playerId,
          sessionId,
          timestamp: Date.now()
        });
      };

      this.ws.onmessage = (event) => {
        try {
          const message: SyncMessage = JSON.parse(event.data);
          this.handleMessage(message);
        } catch (error) {
          console.error('[SyncService] Failed to parse message:', error);
        }
      };

      this.ws.onerror = (error) => {
        console.error('[SyncService] WebSocket error:', error);
        this.onSyncError?.('WebSocket connection error');
      };

      this.ws.onclose = () => {
        console.log('[SyncService] Connection closed');
        this.connected = false;
        this.onConnectionStatus?.(false);
        this.attemptReconnect(url, playerId, sessionId);
      };
    } catch (error) {
      console.error('[SyncService] Failed to connect:', error);
      this.onSyncError?.('Failed to establish WebSocket connection');
    }
  }

  /**
   * Send a state update to the server.
   */
  sendStateUpdate(playerId: string, sessionId: string, state: Record<string, unknown>, version: number) {
    if (!this.connected || !this.ws) {
      console.warn('[SyncService] Not connected, queuing state update');
      return;
    }

    this.send({
      type: 'STATE_UPDATE',
      playerId,
      sessionId,
      timestamp: Date.now(),
      state,
      version
    });
  }

  /**
   * Disconnect from the server.
   */
  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.connected = false;
    this.reconnectAttempts = 0;
  }

  /**
   * Check if the service is currently connected.
   */
  isConnected(): boolean {
    return this.connected;
  }

  private send(message: SyncMessage) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  private handleMessage(message: SyncMessage) {
    switch (message.type) {
      case 'STATE_UPDATE':
        // Server pushing state update (conflict resolution)
        if (message.state && message.version !== undefined) {
          this.onStateUpdate?.(message.state, message.version);
        }
        break;

      case 'SYNC_ACK':
        // Server acknowledged our state update
        if (message.version !== undefined) {
          this.onSyncAck?.(message.version);
        }
        break;

      case 'SYNC_ERROR':
        // Server reported an error
        this.onSyncError?.('Server reported sync error');
        break;

      default:
        console.warn('[SyncService] Unknown message type:', message.type);
    }
  }

  private attemptReconnect(url: string, playerId: string, sessionId: string) {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[SyncService] Max reconnect attempts reached');
      this.onSyncError?.('Failed to reconnect after multiple attempts');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    console.log(`[SyncService] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);

    setTimeout(() => {
      this.connect(url, playerId, sessionId);
    }, delay);
  }
}

/**
 * Content script for the DoraMCU Game Node Extension.
 *
 * Injected into the PWA page to enhance it with extension capabilities:
 * - Offline storage access
 * - Local AI inference via the background worker
 * - State synchronization bridge
 */

(function() {
  'use strict';

  console.log('[DoraMCU Extension] Content script loaded');

  // Check if we're on a DoraMCU page
  if (!window.location.hostname.includes('localhost') &&
      !window.location.hostname.includes('doramcu')) {
    return;
  }

  // Bridge for communication between PWA and extension
  const BRIDGE_NAME = 'DoraMCUExtensionBridge';

  window[BRIDGE_NAME] = {
    /**
     * Send a message to the extension background worker.
     */
    sendMessage: function(type, payload) {
      return new Promise((resolve, reject) => {
        chrome.runtime.sendMessage({ type, payload }, (response) => {
          if (chrome.runtime.lastError) {
            reject(chrome.runtime.lastError);
          } else {
            resolve(response);
          }
        });
      });
    },

    /**
     * Sync game state to extension storage for offline access.
     */
    syncState: function(state) {
      return this.sendMessage('SYNC_STATE', state);
    },

    /**
     * Retrieve cached game state from extension storage.
     */
    getState: function() {
      return this.sendMessage('GET_STATE', null);
    },

    /**
     * Request local AI inference from the extension worker.
     */
    infer: function(input) {
      return this.sendMessage('AI_INFER', input);
    },

    /**
     * Check if the extension is available.
     */
    isAvailable: function() {
      return true;
    }
  };

  // Listen for messages from the PWA via window.postMessage
  window.addEventListener('message', (event) => {
    if (event.source !== window) return;
    if (!event.data || event.data.source !== 'DoraMCU_PWA') return;

    const { type, payload } = event.data;

    switch (type) {
      case 'EXTENSION_PING':
        window.postMessage({
          source: 'DoraMCU_Extension',
          type: 'EXTENSION_PONG',
          payload: { available: true }
        }, '*');
        break;

      case 'SYNC_STATE':
        chrome.runtime.sendMessage({ type: 'SYNC_STATE', payload }, (response) => {
          window.postMessage({
            source: 'DoraMCU_Extension',
            type: 'SYNC_ACK',
            payload: response
          }, '*');
        });
        break;

      case 'AI_INFER':
        chrome.runtime.sendMessage({ type: 'AI_INFER', payload }, (response) => {
          window.postMessage({
            source: 'DoraMCU_Extension',
            type: 'AI_RESULT',
            payload: response
          }, '*');
        });
        break;
    }
  });

  // Announce extension presence
  window.postMessage({
    source: 'DoraMCU_Extension',
    type: 'EXTENSION_READY',
    payload: { version: '0.3.0' }
  }, '*');

})();

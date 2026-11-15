/**
 * Background service worker for the DoraMCU Game Node Extension.
 *
 * Handles offline AI inference, cached game state, and communication with the PWA.
 */

// Install event — cache all required assets
self.addEventListener('install', (event) => {
  console.log('[DoraMCU Extension] Service worker installed');
  event.waitUntil(
    caches.open('doramcu-game-node-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/manifest.json'
      ]);
    })
  );
  self.skipWaiting();
});

// Activate event — clean up old caches
self.addEventListener('activate', (event) => {
  console.log('[DoraMCU Extension] Service worker activated');
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.filter((name) => name.startsWith('doramcu-') && name !== 'doramcu-game-node-v1')
          .map((name) => caches.delete(name))
      );
    })
  );
  self.clients.claim();
});

// Fetch event — serve from cache, fallback to network
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);
  // Only intercept game-related requests
  if (url.pathname.startsWith('/assets/') || url.pathname.startsWith('/models/')) {
    event.respondWith(
      caches.match(event.request).then((response) => {
        if (response) {
          return response;
        }
        return fetch(event.request).then((networkResponse) => {
          // Cache the response for future offline use
          if (networkResponse.ok && networkResponse.type === 'basic') {
            const responseClone = networkResponse.clone();
            caches.open('doramcu-game-node-v1').then((cache) => {
              cache.put(event.request, responseClone);
            });
          }
          return networkResponse;
        });
      })
    );
  }
});

// Message handler — communicate with content script and PWA
self.addEventListener('message', (event) => {
  const { type, payload } = event.data;

  switch (type) {
    case 'SYNC_STATE':
      // Store game state in extension storage for offline access
      chrome.storage.local.set({ gameState: payload }, () => {
        console.log('[DoraMCU Extension] Game state synced to extension storage');
      });
      break;

    case 'GET_STATE':
      // Retrieve cached game state
      chrome.storage.local.get('gameState', (result) => {
        event.source.postMessage({
          type: 'STATE_RESPONSE',
          payload: result.gameState || null
        });
      });
      break;

    case 'AI_INFER':
      // Perform local AI inference (placeholder for ONNX Runtime in worker)
      console.log('[DoraMCU Extension] AI inference requested:', payload);
      // In production, this would load the ONNX model and run inference
      event.source.postMessage({
        type: 'AI_RESULT',
        payload: { prediction: 'placeholder', confidence: 0.0 }
      });
      break;

    default:
      console.warn('[DoraMCU Extension] Unknown message type:', type);
  }
});

/**
 * DoraMCU Service Worker
 * 
 * Provides offline-first caching for static assets and network-first for API calls.
 * Supports cache versioning and update detection.
 */

const CACHE_NAME = 'doramcu-v8';
const API_CACHE_NAME = 'doramcu-api-v8';

// Static assets to pre-cache (relative to scope)
const PRECACHE_ASSETS = [
    '/',
    '/index.html',
    '/config.json',
    '/clients/mobile/index.html',
    '/clients/tablet/index.html',
    '/clients/desktop/index.html',
    '/manifest.json',
    '/css/bootstrap.css',
    '/js/platform-detect.js',
    '/js/config.js',
    '/js/client-loader.js',
    '/js/api-client.js',
];

// ---- Installation: pre-cache static assets ----

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            return cache.addAll(PRECACHE_ASSETS.map(url => url + '?t=' + Date.now()));
        }).catch((error) => {
            console.warn('Service Worker pre-cache failed:', error);
        })
    );
    // Activate immediately instead of waiting for old tabs to close
    self.skipWaiting();
});

// ---- Activation: clean up old caches ----

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames
                    .filter((cacheName) => {
                        // Delete any cache that doesn't match our current version
                        return cacheName.startsWith('doramcu-') && 
                               cacheName !== CACHE_NAME && 
                               cacheName !== API_CACHE_NAME;
                    })
                    .map((cacheName) => {
                        console.log('Service Worker: deleting old cache', cacheName);
                        return caches.delete(cacheName);
                    })
            );
        })
    );
    // Take over of clients immediately
    self.clients.claim();
});

// ---- Fetch: cache-first for static, network-first for API ----

self.addEventListener('fetch', (event) => {
    const { request } = event;
    const url = new URL(request.url);

    // Skip non-HTTP(S) requests
    if (!url.protocol.startsWith('http')) {
        return;
    }

    // API requests: network-first with cache fallback
    if (url.pathname.startsWith('/api/')) {
        event.respondWith(handleApiRequest(request));
        return;
    }

    // SSE (Server-Sent Events): always from network, no caching
    if (request.headers.get('Accept') === 'text/event-stream') {
        return;
    }

    // WebSocket: always from network
    if (request.url.startsWith('ws://') || request.url.startsWith('wss://')) {
        return;
    }

    // JavaScript files: network-first to avoid stale code after deployments
    if (url.pathname.endsWith('.js')) {
        event.respondWith(handleNetworkFirstRequest(request));
        return;
    }

    // Static assets: cache-first with network fallback
    event.respondWith(handleStaticRequest(request));
});

// ---- Network-first handler for JS files: always fetch fresh code ----

async function handleNetworkFirstRequest(request) {
    // Try network first
    try {
        const response = await fetch(request);
        if (response && response.ok) {
            // Update cache with fresh version
            const responseToCache = response.clone();
            caches.open(CACHE_NAME).then((cache) => {
                cache.put(request, responseToCache);
            });
            return response;
        }
    } catch (error) {
        // Network failed — fall back to cache
        console.warn('Service Worker: network failed for JS, using cache for', request.url);
    }

    // Return cached version if available
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
        return cachedResponse;
    }

    // No network, no cache
    return new Response('Network error — no cached version available', {
        status: 503,
        statusText: 'Service Unavailable',
        headers: new Headers({ 'Content-Type': 'text/plain' })
    });
}

// ---- Static asset handler: cache-first strategy ----

async function handleStaticRequest(request) {
    const cachedResponse = await caches.match(request);
    
    if (cachedResponse) {
        // Return cached version immediately
        // In background, fetch the latest version and update cache
        fetch(request).then((response) => {
            if (response && response.ok) {
                caches.open(CACHE_NAME).then((cache) => {
                    cache.put(request, response);
                });
            }
        }).catch(() => {
            // Network fetch failed — silently use cached version
        });
        return cachedResponse;
    }
    
    // Not in cache — fetch from network
    try {
        const response = await fetch(request);
        if (response && response.ok) {
            const responseToCache = response.clone();
            caches.open(CACHE_NAME).then((cache) => {
                cache.put(request, responseToCache);
            });
        }
        return response;
    } catch (error) {
        // Network failed and nothing in cache
        console.warn('Service Worker: fetch failed for', request.url, error);
        return new Response('Offline — no cached version available', {
            status: 503,
            statusText: 'Service Unavailable',
            headers: new Headers({ 'Content-Type': 'text/plain' })
        });
    }
}

// ---- API request handler: network-first with cache fallback ----

async function handleApiRequest(request) {
    // Only cache GET requests; POST/PUT/DELETE go straight to network
    if (request.method !== 'GET') {
        return fetch(request).catch(() => {
            return new Response(JSON.stringify({ error: 'Network error' }), {
                status: 503,
                headers: { 'Content-Type': 'application/json' }
            });
        });
    }

    // Try network first
    try {
        const response = await fetch(request);
        
        // If the response is OK, cache it and return it
        if (response && response.ok) {
            const responseToCache = response.clone();
            caches.open(API_CACHE_NAME).then((cache) => {
                cache.put(request, responseToCache);
            });
            return response;
        }
        
        // For non-OK responses (4xx, 5xx), return the actual response
        // instead of falling through to cache — this preserves proper
        // error messages like 404 Not Found for missing resources
        return response;
    } catch (error) {
        // Network actually failed (DNS error, connection refused, etc.) — fall back to cache
        console.warn('Service Worker: API network failed, using cache for', request.url);
    }

    // Return cached version if available
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
        return cachedResponse;
    }

    // No network, no cache
    return new Response(JSON.stringify({ error: 'Network error — no cached data' }), {
        status: 503,
        headers: { 'Content-Type': 'application/json' }
    });
}

// ---- Message handler: cache update notifications ----

self.addEventListener('message', (event) => {
    if (event.data && event.data.type === 'SKIP_WAITING') {
        self.skipWaiting();
    }
    
    if (event.data && event.data.type === 'CACHE_STATUS') {
        caches.keys().then((names) => {
            event.source.postMessage({
                type: 'CACHE_STATUS_RESPONSE',
                caches: names
            });
        });
    }
});

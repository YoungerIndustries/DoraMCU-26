const CACHE_NAME = 'dora-neuron-v2';
const EXPERIENCE_CACHE = 'dora-experience-cache';
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/style.css'
];

// Install event - cache assets
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        return cache.addAll(urlsToCache);
      })
  );
  self.skipWaiting();
});

// Activate event - clean old caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME && cacheName !== EXPERIENCE_CACHE) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  self.clients.claim();
});

// Fetch event - cache-first strategy for static assets, network-first for API calls
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // Network-first for API calls
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(event.request)
        .then((response) => {
          const responseToCache = response.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseToCache);
          });
          return response;
        })
        .catch(() => {
          return caches.match(event.request);
        })
    );
    return;
  }

  // Cache-first for static assets
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        if (response) {
          return response;
        }
        return fetch(event.request)
          .then((response) => {
            if (!response || response.status !== 200 || response.type !== 'basic') {
              return response;
            }
            const responseToCache = response.clone();
            caches.open(CACHE_NAME)
              .then((cache) => {
                cache.put(event.request, responseToCache);
              });
            return response;
          });
      })
  );
});

// Background sync - queue experiences when offline
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-experiences') {
    event.waitUntil(syncExperiences());
  }
});

async function syncExperiences() {
  const cache = await caches.open(EXPERIENCE_CACHE);
  const requests = await cache.keys();
  for (const request of requests) {
    const response = await cache.match(request);
    if (response) {
      const experience = await response.json();
      try {
        await fetch('/api/v1/neuron/experience', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(experience)
        });
        await cache.delete(request);
      } catch (e) {
        console.error('Failed to sync experience:', e);
      }
    }
  }
}

// Push notifications
self.addEventListener('push', (event) => {
  const data = event.data ? event.data.json() : { title: 'DoraMCU', body: 'New notification' };
  event.waitUntil(
    self.registration.showNotification(data.title, {
      body: data.body,
      icon: 'icon-192.png',
      badge: 'icon-192.png',
      tag: data.tag || 'default'
    })
  );
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(
    clients.openWindow('/')
  );
});

// Handle messages from main thread
self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') {
    self.skipWaiting();
  }
  if (event.data === 'CACHE_EXPERIENCE') {
    caches.open(EXPERIENCE_CACHE).then((cache) => {
      const request = new Request(`experience-${Date.now()}`);
      const response = new Response(JSON.stringify(event.data.experience));
      cache.put(request, response);
    });
  }
});

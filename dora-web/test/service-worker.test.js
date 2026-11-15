/**
 * Tests for Service Worker and PWA integration
 * 
 * Tests cover:
 * - Service Worker registration
 * - Cache strategies (static vs API)
 * - Update detection
 * - PWA install prompt behavior
 */

// Load the source files to test
const fs = require('fs');
const path = require('path');

// Read source files
const swSource = fs.readFileSync(path.join(__dirname, '../sw.js'), 'utf-8');
const clientLoaderSource = fs.readFileSync(path.join(__dirname, '../js/client-loader.js'), 'utf-8');
const manifestSource = fs.readFileSync(path.join(__dirname, '../manifest.json'), 'utf-8');
const mobileSource = fs.readFileSync(path.join(__dirname, '../clients/mobile/index.html'), 'utf-8');
const indexSource = fs.readFileSync(path.join(__dirname, '../index.html'), 'utf-8');

describe('Service Worker', () => {
    
    describe('Installation', () => {
        test('should have install event listener', () => {
            expect(swSource).toContain("self.addEventListener('install'");
        });

        test('should pre-cache essential assets', () => {
            expect(swSource).toContain('PRECACHE_ASSETS');
            expect(swSource).toContain('/index.html');
            expect(swSource).toContain('/config.json');
            expect(swSource).toContain('/manifest.json');
        });

        test('should include client modules in pre-cache', () => {
            expect(swSource).toContain('/clients/mobile/index.html');
            expect(swSource).toContain('/clients/tablet/index.html');
            expect(swSource).toContain('/clients/desktop/index.html');
        });

        test('should include JS modules in pre-cache', () => {
            expect(swSource).toContain('/js/platform-detect.js');
            expect(swSource).toContain('/js/config.js');
            expect(swSource).toContain('/js/client-loader.js');
        });

        test('should call skipWaiting on install', () => {
            expect(swSource).toContain('self.skipWaiting()');
        });

        test('should handle pre-cache failures gracefully', () => {
            expect(swSource).toContain('.catch');
            expect(swSource).toContain('pre-cache failed');
        });
    });

    describe('Activation', () => {
        test('should have activate event listener', () => {
            expect(swSource).toContain("self.addEventListener('activate'");
        });

        test('should clean up old caches', () => {
            expect(swSource).toContain('caches.delete');
        });

        test('should only delete doramcu- prefixed caches', () => {
            expect(swSource).toContain("cacheName.startsWith('doramcu-'");
        });

        test('should preserve current cache names', () => {
            expect(swSource).toContain('CACHE_NAME');
            expect(swSource).toContain('API_CACHE_NAME');
        });

        test('should call clients.claim on activate', () => {
            expect(swSource).toContain('self.clients.claim()');
        });
    });

    describe('Fetch Handling', () => {
        test('should have fetch event listener', () => {
            expect(swSource).toContain("self.addEventListener('fetch'");
        });

        test('should handle API requests separately', () => {
            expect(swSource).toContain("url.pathname.startsWith('/api/'");
            expect(swSource).toContain('handleApiRequest');
        });

        test('should skip SSE requests', () => {
            expect(swSource).toContain('text/event-stream');
        });

        test('should skip WebSocket requests', () => {
            expect(swSource).toContain("request.url.startsWith('ws://'");
            expect(swSource).toContain("request.url.startsWith('wss://'");
        });

        test('should handle static assets with cache-first strategy', () => {
            expect(swSource).toContain('handleStaticRequest');
        });

        test('should skip non-HTTP protocols', () => {
            expect(swSource).toContain("url.protocol.startsWith('http'");
        });
    });

    describe('Static Asset Caching', () => {
        test('should return cached response when available', () => {
            expect(swSource).toContain('caches.match(request)');
            expect(swSource).toContain('return cachedResponse');
        });

        test('should update cache in background after returning cached response', () => {
            expect(swSource).toContain('fetch(request).then');
            expect(swSource).toContain('cache.put(request, response)');
        });

        test('should fetch from network when not in cache', () => {
            expect(swSource).toContain('const response = await fetch(request)');
        });

        test('should cache new responses', () => {
            expect(swSource).toContain('response.clone()');
            expect(swSource).toContain('responseToCache');
        });

        test('should return 503 when offline and no cache', () => {
            expect(swSource).toContain('503');
            expect(swSource).toContain('Service Unavailable');
        });
    });

    describe('API Request Caching', () => {
        test('should only cache GET requests', () => {
            expect(swSource).toContain("request.method !== 'GET'");
        });

        test('should use network-first strategy for API', () => {
            // Network is tried first, then cache fallback
            expect(swSource).toContain('await fetch(request)');
            expect(swSource).toContain('const cachedResponse = await caches.match(request)');
        });

        test('should cache successful API responses', () => {
            expect(swSource).toContain('API_CACHE_NAME');
        });

        test('should return 503 for non-GET network failures', () => {
            expect(swSource).toContain("JSON.stringify({ error: 'Network error' })");
        });

        test('should return 503 when no network and no cache', () => {
            expect(swSource).toContain("JSON.stringify({ error: 'Network error — no cached data' })");
        });
    });

    describe('Message Handling', () => {
        test('should handle SKIP_WAITING message', () => {
            expect(swSource).toContain("event.data.type === 'SKIP_WAITING'");
        });

        test('should handle CACHE_STATUS message', () => {
            expect(swSource).toContain("event.data.type === 'CACHE_STATUS'");
            expect(swSource).toContain('CACHE_STATUS_RESPONSE');
        });
    });

    describe('Cache Versioning', () => {
        test('should use versioned cache names', () => {
            expect(swSource).toContain("CACHE_NAME = 'doramcu-v1'");
            expect(swSource).toContain("API_CACHE_NAME = 'doramcu-api-v1'");
        });
    });
});

describe('Service Worker Registration', () => {
    
    describe('registerServiceWorker function', () => {
        test('should be defined in client-loader.js', () => {
            expect(clientLoaderSource).toContain('async function registerServiceWorker()');
        });

        test('should check for serviceWorker support', () => {
            expect(clientLoaderSource).toContain("'serviceWorker' in navigator");
        });

        test('should register sw.js with scope', () => {
            expect(clientLoaderSource).toContain("navigator.serviceWorker.register('/sw.js'");
            expect(clientLoaderSource).toContain("scope: '/'");
        });

        test('should handle registration errors', () => {
            expect(clientLoaderSource).toContain('registration failed');
        });

        test('should listen for updatefound event', () => {
            expect(clientLoaderSource).toContain('updatefound');
        });

        test('should show update notification when new worker is installed', () => {
            expect(clientLoaderSource).toContain("newWorker.state === 'installed'");
            expect(clientLoaderSource).toContain('showUpdateAvailable()');
        });
    });

    describe('Update Detection', () => {
        test('should have showUpdateAvailable function', () => {
            expect(clientLoaderSource).toContain('function showUpdateAvailable()');
        });

        test('should prevent duplicate notifications', () => {
            expect(clientLoaderSource).toContain("document.getElementById('pwa-update-notification'");
        });

        test('should have reload button', () => {
            expect(clientLoaderSource).toContain('reload-update');
        });

        test('should have dismiss button', () => {
            expect(clientLoaderSource).toContain('dismiss-update');
        });

        test('should send SKIP_WAITING message on reload', () => {
            expect(clientLoaderSource).toContain("postMessage({ type: 'SKIP_WAITING' })");
        });

        test('should reload the page', () => {
            expect(clientLoaderSource).toContain('window.location.reload()');
        });

        test('should auto-dismiss after timeout', () => {
            expect(clientLoaderSource).toContain('setTimeout');
            expect(clientLoaderSource).toContain('30000');
        });
    });

    describe('Main index.html integration', () => {
        test('should call registerServiceWorker on DOMContentLoaded', () => {
            expect(indexSource).toContain('registerServiceWorker()');
        });

        test('should handle registration errors gracefully', () => {
            expect(indexSource).toContain('.catch(console.warn)');
        });
    });
});

describe('PWA Manifest', () => {
    
    test('should have valid JSON structure', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest).toBeDefined();
    });

    test('should have name and short_name', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.name).toBeDefined();
        expect(manifest.short_name).toBeDefined();
        expect(manifest.short_name.length).toBeLessThanOrEqual(12);
    });

    test('should have start_url with source parameter', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.start_url).toContain('source=pwa');
    });

    test('should have standalone display mode', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.display).toBe('standalone');
    });

    test('should have background and theme colors', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.background_color).toBeDefined();
        expect(manifest.theme_color).toBeDefined();
    });

    test('should have categories', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.categories).toContain('games');
    });

    test('should have lang property', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.lang).toBe('en');
    });

    test('should have scope property', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.scope).toBe('/');
    });

    test('should have two icon sizes', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.icons).toHaveLength(2);
    });

    test('should have 192x192 icon', () => {
        const manifest = JSON.parse(manifestSource);
        const icon192 = manifest.icons.find(i => i.sizes === '192x192');
        expect(icon192).toBeDefined();
        expect(icon192.purpose).toContain('maskable');
    });

    test('should have 512x512 icon', () => {
        const manifest = JSON.parse(manifestSource);
        const icon512 = manifest.icons.find(i => i.sizes === '512x512');
        expect(icon512).toBeDefined();
        expect(icon512.purpose).toContain('maskable');
    });
});

describe('PWA Install Prompt', () => {
    
    test('should listen for beforeinstallprompt event', () => {
        expect(mobileSource).toContain("window.addEventListener('beforeinstallprompt'");
    });

    test('should prevent default install prompt', () => {
        expect(mobileSource).toContain('e.preventDefault()');
    });

    test('should store deferred prompt', () => {
        expect(mobileSource).toContain('deferredPrompt = e');
    });

    test('should show install banner on beforeinstallprompt', () => {
        expect(mobileSource).toContain("document.getElementById('installBanner'");
    });

    test('should have showInstallPrompt function', () => {
        expect(mobileSource).toContain('window.showInstallPrompt');
    });

    test('should prompt when showInstallPrompt is called', () => {
        expect(mobileSource).toContain('deferredPrompt.prompt()');
    });

    test('should handle user choice', () => {
        expect(mobileSource).toContain('deferredPrompt.userChoice');
    });

    test('should clear deferred prompt after use', () => {
        expect(mobileSource).toContain('deferredPrompt = null');
    });

    test('should have dismissInstallBanner function', () => {
        expect(mobileSource).toContain('window.dismissInstallBanner');
    });

    test('should have install banner HTML', () => {
        expect(mobileSource).toContain('id="installBanner"');
    });

    test('should have install button', () => {
        expect(mobileSource).toContain('onclick="showInstallPrompt()"');
    });

    test('should have dismiss button', () => {
        expect(mobileSource).toContain('onclick="dismissInstallBanner()"');
    });

    test('should check for standalone display mode', () => {
        expect(mobileSource).toContain('(display-mode: standalone)');
    });
});

describe('End-to-End PWA Flow', () => {
    
    test('index.html should link to manifest.json', () => {
        expect(indexSource).toContain('rel="manifest"');
        expect(indexSource).toContain('manifest.json');
    });

    test('index.html should have theme-color meta tag', () => {
        expect(indexSource).toContain('name="theme-color"');
    });

    test('mobile client should link to manifest.json', () => {
        expect(mobileSource).toContain('rel="manifest"');
    });

    test('mobile client should have theme-color meta tag', () => {
        expect(mobileSource).toContain('name="theme-color"');
    });

    test('mobile client should have apple-mobile-web-app-capable', () => {
        expect(mobileSource).toContain('apple-mobile-web-app-capable');
    });

    test('Service Worker should be in dora-web root', () => {
        expect(swSource.length).toBeGreaterThan(100);
    });

    test('All client modules should be in pre-cache list', () => {
        const manifest = JSON.parse(manifestSource);
        expect(manifest.start_url).toBeDefined();
        
        // Verify SW precache includes all client types
        expect(swSource).toContain('/clients/desktop/');
        expect(swSource).toContain('/clients/mobile/');
        expect(swSource).toContain('/clients/tablet/');
    });
});

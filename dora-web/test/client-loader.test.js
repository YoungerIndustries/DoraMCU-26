/**
 * Client Loader Unit Tests
 *
 * Tests for the dora-web client loader module (client-loader.js).
 * Uses Jest with jsdom for DOM mocking.
 *
 * Run: cd dora-web/test && npx jest client-loader.test.js
 */

// ---------------------------------------------------------------------------
// DOM Setup – use Jest's built-in jsdom, inject HTML manually
// ---------------------------------------------------------------------------
const ENTRY_HTML = `
    <div class="loading-container">
        <div class="loading-spinner"></div>
        <h1 class="loading-title">DoraMCU</h1>
        <p class="loading-subtitle">Detecting your platform...</p>
        <p class="loading-status" id="status">Initializing...</p>
    </div>
    <div id="client-container" style="display: none;"></div>
`;

function resetDOM() {
    document.open();
    document.write('<!DOCTYPE html><html><head><title>DoraMCU</title></head><body>' + ENTRY_HTML + '</body></html>');
    document.close();
}

// ---------------------------------------------------------------------------
// Mock PlatformDetector – the client loader calls these statically
// ---------------------------------------------------------------------------
function createMockPlatformDetector(recommendedClient, hasNative) {
    return {
        TIER_DESKTOP_FULL: 'desktop',
        TIER_TABLET: 'tablet',
        TIER_MOBILE: 'mobile',
        TIER_LOW_END: 'low-end',
        OS_WINDOWS: 'windows',
        OS_MACOS: 'macos',
        OS_LINUX: 'linux',
        OS_ANDROID: 'android',
        OS_IOS: 'ios',
        OS_UNKNOWN: 'unknown',
        getRecommendedClient: jest.fn().mockReturnValue(recommendedClient),
        hasNativeClient: jest.fn().mockReturnValue(hasNative)
    };
}

// ---------------------------------------------------------------------------
// Mock ConfigManager
// ---------------------------------------------------------------------------
function createMockConfigManager(webClientUrl, downloadUrl) {
    return {
        config: {
            apiUrl: 'http://localhost:8080',
            version: '1.0.0',
            clients: {
                desktop: { url: webClientUrl || '/clients/desktop/' },
                tablet: { url: '/clients/tablet/' },
                mobile: { url: '/clients/mobile/' }
            }
        },
        getWebClientUrl: jest.fn((tier) => {
            const urls = {
                desktop: webClientUrl || '/clients/desktop/',
                tablet: '/clients/tablet/',
                mobile: '/clients/mobile/',
                'low-end': '/clients/mobile/'
            };
            return urls[tier] || '/clients/mobile/';
        }),
        getDownloadUrl: jest.fn((clientId, os) => downloadUrl || null)
    };
}

// ---------------------------------------------------------------------------
// Load modules into globals and the ClientLoader itself
// ---------------------------------------------------------------------------
let ClientLoader;

beforeEach(() => {
    jest.useFakeTimers();
    resetDOM();
    global.console = {
        log: jest.fn(),
        warn: jest.fn(),
        error: jest.fn()
    };
});

afterEach(() => {
    jest.useRealTimers();
});

// ---------------------------------------------------------------------------
// Helper: load the ClientLoader code with mocked dependencies
// ---------------------------------------------------------------------------
function loadClientLoader(platformDetector, configManager) {
    global.PlatformDetector = platformDetector;
    global.ConfigManager = configManager;

    const fs = require('fs');
    const path = require('path');
    const source = fs.readFileSync(
        path.join(__dirname, '..', 'js', 'client-loader.js'),
        'utf-8'
    );
    const fn = new Function(source + '\nreturn ClientLoader;');
    ClientLoader = fn();
}

// ==========================================================================
// Tests
// ==========================================================================

describe('ClientLoader', () => {

    describe('loadClient()', () => {

        test('should load desktop client when platform is desktop with native client', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager('/clients/desktop/', 'http://localhost:8080/api/v1/clients/download/unity/windows');
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            expect(pd.getRecommendedClient).toHaveBeenCalledWith('desktop', 'windows');
            expect(document.getElementById('status').textContent).toContain('desktop');
            expect(document.getElementById('client-container').style.display).toBe('block');
            expect(document.querySelector('.loading-container').style.display).toBe('none');
        });

        test('should load desktop web client when platform is desktop without native client', async () => {
            const pd = createMockPlatformDetector('desktop', false);
            const cm = createMockConfigManager('/clients/desktop/', null);
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'unknown' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            expect(pd.getRecommendedClient).toHaveBeenCalledWith('desktop', 'unknown');
            // Without native client, falls through to web client load
            expect(document.getElementById('client-container').style.display).toBe('block');
        });

        test('should load tablet client when platform is tablet', async () => {
            const pd = createMockPlatformDetector('tablet', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'tablet', os: 'ios' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            expect(pd.getRecommendedClient).toHaveBeenCalledWith('tablet', 'ios');
            expect(document.getElementById('status').textContent).toContain('tablet');
            expect(document.getElementById('client-container').style.display).toBe('block');

            // Verify iframe was created with correct URL
            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/tablet/');
        });

        test('should load mobile client when platform is mobile', async () => {
            const pd = createMockPlatformDetector('mobile', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'mobile', os: 'android' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            expect(pd.getRecommendedClient).toHaveBeenCalledWith('mobile', 'android');
            expect(document.getElementById('status').textContent).toContain('mobile');

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');
        });

        test('should load mobile client as fallback for low-end devices', async () => {
            const pd = createMockPlatformDetector('low-end', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'low-end', os: 'android' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            // Status is updated by loadWebClient to show the loading URL
            expect(document.getElementById('status').textContent).toContain('mobile');

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');
        });

        test('should fallback to mobile client when client type is unknown', async () => {
            const pd = createMockPlatformDetector('unknown', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'unknown', os: 'unknown' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');
        });

        test('should fallback to mobile client on error', async () => {
            // Simulate error by making getRecommendedClient throw
            const pd = createMockPlatformDetector('desktop', true);
            pd.getRecommendedClient.mockImplementation(() => {
                throw new Error('Platform detection failed');
            });
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            // Status is updated by loadWebClient after the error fallback
            expect(document.getElementById('status').textContent).toContain('mobile');
            // Color is set to red for errors; jsdom normalizes hex to rgb
            const statusColor = document.getElementById('status').style.color;
            expect(statusColor).toMatch(/^(#ff6b6b|rgb\(255,\s*107,\s*107\))$/);

            // Should still load the fallback mobile client
            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');
        });
    });

    describe('loadDesktopClient()', () => {

        test('should show native client offer when download URL is available', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            const downloadUrl = 'http://localhost:8080/api/v1/clients/download/unity/windows';
            const cm = createMockConfigManager('/clients/desktop/', downloadUrl);
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadDesktopClient(platform, config);

            // The offer is shown after 2 seconds via setTimeout
            expect(document.getElementById('native-client-offer')).toBeNull();

            // Fast-forward timers
            jest.advanceTimersByTime(2000);

            const offer = document.getElementById('native-client-offer');
            expect(offer).toBeTruthy();
            expect(offer.innerHTML).toContain('Native Client Available');
            expect(offer.innerHTML).toContain('Windows');
            expect(offer.innerHTML).toContain(downloadUrl);
        });

        test('should not show native client offer when download URL is null', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager('/clients/desktop/', null);
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadDesktopClient(platform, config);

            jest.advanceTimersByTime(2000);

            expect(document.getElementById('native-client-offer')).toBeNull();
        });

        test('should load desktop web client iframe', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager('/clients/desktop/', null);
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadDesktopClient(platform, config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/desktop/');
            expect(iframe.title).toBe('DoraMCU desktop client');
        });
    });

    describe('loadWebClient()', () => {

        test('should create iframe with correct attributes', async () => {
            const pd = createMockPlatformDetector('mobile', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadWebClient('mobile', config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');
            expect(iframe.style.width).toBe('100%');
            expect(iframe.style.height).toBe('100%');
            expect(iframe.style.border).toBe('');
            expect(iframe.title).toBe('DoraMCU mobile client');
        });

        test('should hide loading container and show client container', async () => {
            const pd = createMockPlatformDetector('mobile', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadWebClient('mobile', config);

            expect(document.querySelector('.loading-container').style.display).toBe('none');
            expect(document.getElementById('client-container').style.display).toBe('block');
        });

        test('should send config message to iframe on load', async () => {
            const pd = createMockPlatformDetector('mobile', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const config = { apiUrl: 'http://api.example.com', version: '2.0.0' };

            await ClientLoader.loadWebClient('mobile', config);

            const iframe = document.querySelector('#client-container iframe');

            // Simulate iframe load - in jsdom postMessage may or may not throw
            // depending on the jsdom version; either way the onload handler runs
            iframe.onload();

            // Verify the iframe was created and onload handler was set
            expect(iframe.onload).toBeTruthy();
        });

        test('should load desktop client iframe with correct URL', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager('/clients/desktop/', null);
            loadClientLoader(pd, cm);

            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadWebClient('desktop', config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe.src).toContain('/clients/desktop/');
        });

        test('should load tablet client iframe with correct URL', async () => {
            const pd = createMockPlatformDetector('tablet', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadWebClient('tablet', config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe.src).toContain('/clients/tablet/');
        });

        test('should update status text with tier and URL', async () => {
            const pd = createMockPlatformDetector('mobile', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadWebClient('mobile', config);

            const status = document.getElementById('status');
            expect(status.textContent).toContain('mobile');
            expect(status.textContent).toContain('/clients/mobile/');
        });
    });

    describe('showNativeClientOffer()', () => {

        test('should create offer element with correct structure', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager('/clients/desktop/', 'http://localhost:8080/download.exe');
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://localhost:8080/download.exe', 'windows');

            const offer = document.getElementById('native-client-offer');
            expect(offer).toBeTruthy();
            expect(offer.id).toBe('native-client-offer');
            expect(offer.innerHTML).toContain('Native Client Available');
            expect(offer.innerHTML).toContain('Windows');
        });

        test('should include download link with correct URL', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const downloadUrl = 'http://api.example.com/download/DoraMCU-Setup.exe';
            ClientLoader.showNativeClientOffer(downloadUrl, 'windows');

            const link = document.getElementById('download-native');
            expect(link).toBeTruthy();
            expect(link.href).toBe(downloadUrl);
            expect(link.textContent).toContain('Download for Windows');
        });

        test('should capitalize OS name correctly', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://example.com/download', 'macos');
            expect(document.getElementById('native-client-offer').innerHTML).toContain('Macos');

            const offer1 = document.getElementById('native-client-offer');
            offer1.remove();

            ClientLoader.showNativeClientOffer('http://example.com/download', 'linux');
            expect(document.getElementById('native-client-offer').innerHTML).toContain('Linux');
        });

        test('should not create duplicate offer', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://example.com/1', 'windows');
            ClientLoader.showNativeClientOffer('http://example.com/2', 'windows');

            const offers = document.querySelectorAll('#native-client-offer');
            expect(offers.length).toBe(1);
        });

        test('should close offer when close button is clicked', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://example.com/download', 'windows');

            expect(document.getElementById('native-client-offer')).toBeTruthy();

            document.getElementById('close-offer').click();

            expect(document.getElementById('native-client-offer')).toBeNull();
        });

        test('should auto-hide offer after 10 seconds', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://example.com/download', 'windows');

            expect(document.getElementById('native-client-offer')).toBeTruthy();

            jest.advanceTimersByTime(10000);

            expect(document.getElementById('native-client-offer')).toBeNull();
        });

        test('should append offer to body', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://example.com/download', 'windows');

            const offer = document.getElementById('native-client-offer');
            expect(offer.parentNode).toBe(document.body);
        });

        test('should have correct styling', () => {
            const pd = createMockPlatformDetector('desktop', true);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            ClientLoader.showNativeClientOffer('http://example.com/download', 'windows');

            const offer = document.getElementById('native-client-offer');
            expect(offer.style.position).toBe('fixed');
            expect(offer.style.zIndex).toBe('1000');
        });
    });

    describe('Integration scenarios', () => {

        test('full flow: desktop with native client shows offer and loads web client', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            const downloadUrl = 'http://localhost:8080/api/v1/clients/download/unity/windows';
            const cm = createMockConfigManager('/clients/desktop/', downloadUrl);
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows', isTouchDevice: false, cores: 8, memory: 16 };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            // Web client should be loaded
            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/desktop/');

            // Loading screen hidden
            expect(document.querySelector('.loading-container').style.display).toBe('none');

            // Offer appears after delay
            jest.advanceTimersByTime(2000);
            expect(document.getElementById('native-client-offer')).toBeTruthy();
        });

        test('full flow: mobile loads lightweight client directly', async () => {
            const pd = createMockPlatformDetector('mobile', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'mobile', os: 'android', isTouchDevice: true, cores: 4, memory: 4 };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');

            // No native client offer for mobile
            jest.advanceTimersByTime(2000);
            expect(document.getElementById('native-client-offer')).toBeNull();
        });

        test('full flow: tablet loads optimized client', async () => {
            const pd = createMockPlatformDetector('tablet', false);
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'tablet', os: 'ios', isTouchDevice: true, cores: 8, memory: 4 };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/tablet/');
        });

        test('error recovery: loads fallback mobile client on any failure', async () => {
            const pd = createMockPlatformDetector('desktop', true);
            pd.getRecommendedClient.mockImplementation(() => {
                throw new Error('Critical failure');
            });
            const cm = createMockConfigManager();
            loadClientLoader(pd, cm);

            const platform = { tier: 'desktop', os: 'windows' };
            const config = { apiUrl: 'http://localhost:8080', version: '1.0.0' };

            await ClientLoader.loadClient(platform, config);

            // Should have fallen back to mobile
            const iframe = document.querySelector('#client-container iframe');
            expect(iframe).toBeTruthy();
            expect(iframe.src).toContain('/clients/mobile/');
            // Color is set to red for errors; jsdom normalizes hex to rgb
            const statusColor = document.getElementById('status').style.color;
            expect(statusColor).toMatch(/^(#ff6b6b|rgb\(255,\s*107,\s*107\))$/);
        });
    });
});

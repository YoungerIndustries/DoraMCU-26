/**
 * Platform Detector Unit Tests
 *
 * Tests for the dora-web platform detection module (platform-detect.js).
 * Uses Jest with jsdom for DOM mocking.
 *
 * Run: cd dora-web/test && npx jest platform-detect.test.js
 */

// ---------------------------------------------------------------------------
// Helpers – use Jest's built-in jsdom, mock navigator properties
// ---------------------------------------------------------------------------
const originalNavigator = global.navigator;
const originalWindow = global.window;

function setupNavigator(userAgent, platform, hardwareConcurrency, deviceMemory, maxTouchPoints, msMaxTouchPoints) {
    // Mock navigator properties
    Object.defineProperty(global, 'navigator', {
        value: {
            ...originalNavigator,
            userAgent: userAgent || '',
            platform: platform || '',
            hardwareConcurrency: hardwareConcurrency,
            deviceMemory: deviceMemory,
            maxTouchPoints: maxTouchPoints || 0,
            msMaxTouchPoints: msMaxTouchPoints || 0
        },
        writable: true,
        configurable: true,
        enumerable: true
    });

    // Mock touch support
    if (maxTouchPoints > 0 || msMaxTouchPoints > 0) {
        global.window.ontouchstart = function () { };
    } else {
        delete global.window.ontouchstart;
    }

    // Mock screen
    Object.defineProperty(global.window, 'screen', {
        value: { width: 1920, height: 1080 },
        writable: true,
        configurable: true,
        enumerable: true
    });

    Object.defineProperty(global.window, 'devicePixelRatio', {
        value: 1,
        writable: true,
        configurable: true,
        enumerable: true
    });
}

function resetNavigator() {
    Object.defineProperty(global, 'navigator', {
        value: originalNavigator,
        writable: true,
        configurable: true,
        enumerable: true
    });
    delete global.window.ontouchstart;
}

let PlatformDetector;

beforeEach(() => {
    setupNavigator();

    const fs = require('fs');
    const path = require('path');
    const source = fs.readFileSync(
        path.join(__dirname, '..', 'js', 'platform-detect.js'),
        'utf-8'
    );
    const fn = new Function(source + '\nreturn PlatformDetector;');
    PlatformDetector = fn();
});

afterEach(() => {
    resetNavigator();
});

// ==========================================================================
// Tests
// ==========================================================================

describe('PlatformDetector', () => {

    describe('Tier constants', () => {
        test('should have correct tier constants', () => {
            expect(PlatformDetector.TIER_DESKTOP_FULL).toBe('desktop');
            expect(PlatformDetector.TIER_TABLET).toBe('tablet');
            expect(PlatformDetector.TIER_MOBILE).toBe('mobile');
            expect(PlatformDetector.TIER_LOW_END).toBe('low-end');
        });

        test('should have correct OS constants', () => {
            expect(PlatformDetector.OS_WINDOWS).toBe('windows');
            expect(PlatformDetector.OS_MACOS).toBe('macos');
            expect(PlatformDetector.OS_LINUX).toBe('linux');
            expect(PlatformDetector.OS_ANDROID).toBe('android');
            expect(PlatformDetector.OS_IOS).toBe('ios');
            expect(PlatformDetector.OS_UNKNOWN).toBe('unknown');
        });
    });

    describe('detectOS()', () => {

        test('should detect iOS from iPhone user agent', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)', 'iPhone');
            expect(os).toBe(PlatformDetector.OS_IOS);
        });

        test('should detect iOS from iPad user agent', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X)', 'iPad');
            expect(os).toBe(PlatformDetector.OS_IOS);
        });

        test('should detect Android from user agent', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (Linux; Android 11; Pixel 5)', 'Linux');
            expect(os).toBe(PlatformDetector.OS_ANDROID);
        });

        test('should detect Windows from user agent', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'Win32');
            expect(os).toBe(PlatformDetector.OS_WINDOWS);
        });

        test('should detect Windows from platform', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0', 'Windows');
            expect(os).toBe(PlatformDetector.OS_WINDOWS);
        });

        test('should detect macOS from user agent', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', 'MacIntel');
            expect(os).toBe(PlatformDetector.OS_MACOS);
        });

        test('should detect macOS from platform', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0', 'MacOS');
            expect(os).toBe(PlatformDetector.OS_MACOS);
        });

        test('should detect Linux from user agent', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (X11; Linux x86_64)', 'Linux x86_64');
            expect(os).toBe(PlatformDetector.OS_LINUX);
        });

        test('should detect Linux from platform', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0', 'Linux');
            expect(os).toBe(PlatformDetector.OS_LINUX);
        });

        test('should return unknown for unrecognized platform', () => {
            const os = PlatformDetector.detectOS('Mozilla/5.0 (Unknown)', 'Unknown');
            expect(os).toBe(PlatformDetector.OS_UNKNOWN);
        });

        test('should be case insensitive', () => {
            const os = PlatformDetector.detectOS('mozilla/5.0 (windows nt 10.0)', 'win32');
            expect(os).toBe(PlatformDetector.OS_WINDOWS);
        });
    });

    describe('isTouchDevice()', () => {

        test('should return true when ontouchstart is present', () => {
            setupNavigator('', '', 4, 8, 5, 0);
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            expect(fn().isTouchDevice()).toBe(true);
        });

        test('should return true when maxTouchPoints > 0', () => {
            setupNavigator('', '', 4, 8, 5, 0);
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            expect(fn().isTouchDevice()).toBe(true);
        });

        test('should return true when msMaxTouchPoints > 0', () => {
            setupNavigator('', '', 4, 8, 0, 5);
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            expect(fn().isTouchDevice()).toBe(true);
        });

        test('should return false when no touch support', () => {
            setupNavigator('', '', 4, 8, 0, 0);
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            expect(fn().isTouchDevice()).toBe(false);
        });
    });

    describe('determineTier()', () => {

        test('should return low-end when cores < 2', () => {
            const tier = PlatformDetector.determineTier('windows', false, 1920, 1, 8);
            expect(tier).toBe(PlatformDetector.TIER_LOW_END);
        });

        test('should return low-end when memory < 2', () => {
            const tier = PlatformDetector.determineTier('windows', false, 1920, 4, 1);
            expect(tier).toBe(PlatformDetector.TIER_LOW_END);
        });

        test('should return mobile for touch device with small screen', () => {
            const tier = PlatformDetector.determineTier('android', true, 375, 4, 4);
            expect(tier).toBe(PlatformDetector.TIER_MOBILE);
        });

        test('should return tablet for touch device with medium screen', () => {
            const tier = PlatformDetector.determineTier('ios', true, 800, 4, 4);
            expect(tier).toBe(PlatformDetector.TIER_TABLET);
        });

        test('should return tablet for touch device at 1024px', () => {
            const tier = PlatformDetector.determineTier('ios', true, 1024, 4, 4);
            expect(tier).toBe(PlatformDetector.TIER_TABLET);
        });

        test('should return tablet for large touch device with 4+ cores', () => {
            const tier = PlatformDetector.determineTier('ios', true, 1366, 8, 4);
            expect(tier).toBe(PlatformDetector.TIER_TABLET);
        });

        test('should return desktop for non-touch device with 4+ cores and 4+ memory', () => {
            const tier = PlatformDetector.determineTier('windows', false, 1920, 8, 16);
            expect(tier).toBe(PlatformDetector.TIER_DESKTOP_FULL);
        });

        test('should return desktop for non-touch device with lower specs (fallback)', () => {
            const tier = PlatformDetector.determineTier('windows', false, 1920, 2, 2);
            expect(tier).toBe(PlatformDetector.TIER_DESKTOP_FULL);
        });

        test('should return mobile as default fallback for touch devices', () => {
            // Touch device with > 1024px but < 4 cores
            const tier = PlatformDetector.determineTier('android', true, 1366, 2, 4);
            expect(tier).toBe(PlatformDetector.TIER_MOBILE);
        });

        test('should prioritize low-end over other tiers', () => {
            // Even a desktop-like device with low memory should be low-end
            const tier = PlatformDetector.determineTier('windows', false, 1920, 8, 1);
            expect(tier).toBe(PlatformDetector.TIER_LOW_END);
        });

        test('should handle 768px boundary correctly as tablet', () => {
            const tier = PlatformDetector.determineTier('ios', true, 768, 4, 4);
            expect(tier).toBe(PlatformDetector.TIER_TABLET);
        });

        test('should handle 767px as mobile', () => {
            const tier = PlatformDetector.determineTier('android', true, 767, 4, 4);
            expect(tier).toBe(PlatformDetector.TIER_MOBILE);
        });
    });

    describe('hasNativeClient()', () => {

        test('should return true for Windows', () => {
            expect(PlatformDetector.hasNativeClient('windows')).toBe(true);
        });

        test('should return true for macOS', () => {
            expect(PlatformDetector.hasNativeClient('macos')).toBe(true);
        });

        test('should return true for Linux', () => {
            expect(PlatformDetector.hasNativeClient('linux')).toBe(true);
        });

        test('should return false for Android', () => {
            expect(PlatformDetector.hasNativeClient('android')).toBe(false);
        });

        test('should return false for iOS', () => {
            expect(PlatformDetector.hasNativeClient('ios')).toBe(false);
        });

        test('should return false for unknown', () => {
            expect(PlatformDetector.hasNativeClient('unknown')).toBe(false);
        });
    });

    describe('getRecommendedClient()', () => {

        test('should return desktop for desktop tier', () => {
            const client = PlatformDetector.getRecommendedClient('desktop', 'windows');
            expect(client).toBe('desktop');
        });

        test('should return tablet for tablet tier', () => {
            const client = PlatformDetector.getRecommendedClient('tablet', 'ios');
            expect(client).toBe('tablet');
        });

        test('should return mobile for mobile tier', () => {
            const client = PlatformDetector.getRecommendedClient('mobile', 'android');
            expect(client).toBe('mobile');
        });

        test('should return low-end for low-end tier', () => {
            const client = PlatformDetector.getRecommendedClient('low-end', 'android');
            expect(client).toBe('low-end');
        });

        test('should return mobile for unknown tier', () => {
            const client = PlatformDetector.getRecommendedClient('unknown', 'unknown');
            expect(client).toBe('mobile');
        });

        test('should ignore OS parameter and use tier only', () => {
            expect(PlatformDetector.getRecommendedClient('desktop', 'android')).toBe('desktop');
            expect(PlatformDetector.getRecommendedClient('mobile', 'windows')).toBe('mobile');
        });
    });

    describe('detect() - full detection', () => {

        test('should detect desktop Windows machine', () => {
            setupNavigator(
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                'Win32',
                8,
                16,
                0,
                0
            );
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result.tier).toBe('desktop');
            expect(result.os).toBe('windows');
            expect(result.isTouchDevice).toBe(false);
            expect(result.cores).toBe(8);
            expect(result.memory).toBe(16);
        });

        test('should detect Android mobile phone', () => {
            setupNavigator(
                'Mozilla/5.0 (Linux; Android 12; Pixel 6)',
                'Linux',
                8,
                8,
                5,
                0
            );
            global.window.screen = { width: 412, height: 892 };
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result.tier).toBe('mobile');
            expect(result.os).toBe('android');
            expect(result.isTouchDevice).toBe(true);
        });

        test('should detect iPad as tablet', () => {
            setupNavigator(
                'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X)',
                'iPad',
                8,
                4,
                5,
                0
            );
            global.window.screen = { width: 1024, height: 768 };
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result.tier).toBe('tablet');
            expect(result.os).toBe('ios');
            expect(result.isTouchDevice).toBe(true);
        });

        test('should detect low-end device', () => {
            setupNavigator(
                'Mozilla/5.0 (Linux; Android 8)',
                'Linux',
                1,
                1,
                5,
                0
            );
            global.window.screen = { width: 360, height: 640 };
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result.tier).toBe('low-end');
            expect(result.cores).toBe(1);
            expect(result.memory).toBe(1);
        });

        test('should include all platform info in result', () => {
            setupNavigator(
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
                'Win32',
                4,
                8,
                0,
                0
            );
            global.window.screen = { width: 1920, height: 1080 };
            global.window.devicePixelRatio = 2;
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result).toHaveProperty('tier');
            expect(result).toHaveProperty('os');
            expect(result).toHaveProperty('isTouchDevice');
            expect(result).toHaveProperty('screenWidth');
            expect(result).toHaveProperty('screenHeight');
            expect(result).toHaveProperty('hasHighDPI');
            expect(result).toHaveProperty('cores');
            expect(result).toHaveProperty('memory');
            expect(result).toHaveProperty('userAgent');
            expect(result).toHaveProperty('platform');
            expect(result.hasHighDPI).toBe(true);
        });

        test('should default cores to 2 when hardwareConcurrency is undefined', () => {
            setupNavigator('', '', undefined, 4, 0, 0);
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result.cores).toBe(2);
        });

        test('should default memory to 4 when deviceMemory is undefined', () => {
            setupNavigator('', '', 4, undefined, 0, 0);
            const fs = require('fs');
            const path = require('path');
            const source = fs.readFileSync(path.join(__dirname, '..', 'js', 'platform-detect.js'), 'utf-8');
            const fn = new Function(source + '\nreturn PlatformDetector;');
            const detector = fn();

            const result = detector.detect();
            expect(result.memory).toBe(4);
        });
    });
});

/**
 * Config Manager Unit Tests
 *
 * Tests for the dora-web configuration manager module (config.js).
 * Uses Jest with jsdom for DOM mocking.
 *
 * Run: cd dora-web/test && npx jest config.test.js
 */

const { JSDOM } = require('jsdom');

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
function setupDOM() {
    const dom = new JSDOM('<!DOCTYPE html><html><body></body></html>', {
        url: 'http://localhost:8080',
        runScripts: 'dangerously'
    });
    global.window = dom.window;
    global.document = dom.window.document;
    global.console = {
        log: jest.fn(),
        warn: jest.fn(),
        error: jest.fn()
    };
    return dom;
}

// Mock fetch globally
const mockFetch = jest.fn();

let ConfigManager;

beforeEach(() => {
    setupDOM();
    mockFetch.mockReset();
    global.fetch = mockFetch;

    const fs = require('fs');
    const path = require('path');
    const source = fs.readFileSync(
        path.join(__dirname, '..', 'js', 'config.js'),
        'utf-8'
    );
    const fn = new Function(source + '\nreturn ConfigManager;');
    ConfigManager = fn();
});

// ==========================================================================
// Tests
// ==========================================================================

describe('ConfigManager', () => {

    describe('loadConfig()', () => {

        test('should load config from config.json', async () => {
            const config = {
                apiUrl: 'http://api.example.com',
                version: '2.0.0',
                clients: {
                    desktop: { url: '/clients/desktop/' }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            const result = await ConfigManager.loadConfig();

            expect(mockFetch).toHaveBeenCalledWith('config.json');
            expect(result.apiUrl).toBe('http://api.example.com');
            expect(result.version).toBe('2.0.0');
        });

        test('should return cached config on second call', async () => {
            const config = {
                apiUrl: 'http://api.example.com',
                version: '2.0.0',
                clients: {}
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            await ConfigManager.loadConfig();

            expect(mockFetch).toHaveBeenCalledTimes(1);
        });

        test('should use default apiUrl when missing', async () => {
            const config = {
                version: '1.0.0',
                clients: {}
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            const result = await ConfigManager.loadConfig();

            expect(result.apiUrl).toBe('http://localhost:8080');
            expect(console.warn).toHaveBeenCalledWith('Config missing apiUrl, using default');
        });

        test('should use default clients when missing', async () => {
            const config = {
                apiUrl: 'http://api.example.com',
                version: '1.0.0'
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            const result = await ConfigManager.loadConfig();

            expect(result.clients).toBeDefined();
            expect(result.clients.desktop).toBeDefined();
            expect(result.clients.mobile).toBeDefined();
            expect(console.warn).toHaveBeenCalledWith('Config missing clients manifest, using defaults');
        });

        test('should use default version when missing', async () => {
            const config = {
                apiUrl: 'http://api.example.com',
                clients: {}
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            const result = await ConfigManager.loadConfig();

            expect(result.version).toBe('0.0.0');
        });

        test('should fallback to default config on fetch error', async () => {
            mockFetch.mockRejectedValue(new Error('Network error'));

            const result = await ConfigManager.loadConfig();

            expect(result.apiUrl).toBe('http://localhost:8080');
            expect(result.version).toBe('0.0.0');
            expect(result.clients).toBeDefined();
            expect(console.error).toHaveBeenCalled();
            expect(console.warn).toHaveBeenCalled();
        });

        test('should fallback to default config on HTTP error', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                status: 404,
                statusText: 'Not Found'
            });

            const result = await ConfigManager.loadConfig();

            expect(result.apiUrl).toBe('http://localhost:8080');
            expect(console.error).toHaveBeenCalled();
        });

        test('should throw error message with status code on failure', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                status: 500,
                statusText: 'Internal Server Error'
            });

            await ConfigManager.loadConfig();

            expect(console.error).toHaveBeenCalledWith(
                'Error loading config:',
                expect.objectContaining({ message: 'Failed to load config.json: 500 Internal Server Error' })
            );
        });
    });

    describe('getDefaultConfig()', () => {

        test('should return default config with correct structure', () => {
            const config = ConfigManager.getDefaultConfig();

            expect(config.apiUrl).toBe('http://localhost:8080');
            expect(config.version).toBe('0.0.0');
            expect(config.clients).toBeDefined();
        });

        test('should include all default client types', () => {
            const config = ConfigManager.getDefaultConfig();

            expect(config.clients.desktop).toBeDefined();
            expect(config.clients.electron).toBeDefined();
            expect(config.clients.web).toBeDefined();
            expect(config.clients.mobile).toBeDefined();
            expect(config.clients.tablet).toBeDefined();
        });
    });

    describe('getDefaultClients()', () => {

        test('should return desktop client with downloads', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.desktop.name).toBe('DoraMCU Desktop');
            expect(clients.desktop.platforms).toContain('windows');
            expect(clients.desktop.platforms).toContain('macos');
            expect(clients.desktop.platforms).toContain('linux');
            expect(clients.desktop.downloads.windows).toBeDefined();
            expect(clients.desktop.downloads.macos).toBeDefined();
            expect(clients.desktop.downloads.linux).toBeDefined();
        });

        test('should return electron client with downloads', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.electron.name).toBe('DoraMCU Electron');
            expect(clients.electron.downloads.windows).toBeDefined();
        });

        test('should return web client with URL', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.web.url).toBe('/clients/desktop/');
            expect(clients.web.platforms).toContain('all');
        });

        test('should return mobile client with URL', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.mobile.url).toBe('/clients/mobile/');
            expect(clients.mobile.platforms).toContain('android');
            expect(clients.mobile.platforms).toContain('ios');
        });

        test('should return tablet client with URL', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.tablet.url).toBe('/clients/tablet/');
            expect(clients.tablet.platforms).toContain('android');
            expect(clients.tablet.platforms).toContain('ios');
        });

        test('should have correct download URLs for desktop', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.desktop.downloads.windows.url).toBe('/api/v1/clients/download/unity/windows');
            expect(clients.desktop.downloads.macos.url).toBe('/api/v1/clients/download/unity/macos');
            expect(clients.desktop.downloads.linux.url).toBe('/api/v1/clients/download/unity/linux');
        });

        test('should have correct filenames for desktop', () => {
            const clients = ConfigManager.getDefaultClients();

            expect(clients.desktop.downloads.windows.filename).toBe('DoraMCU-Setup.exe');
            expect(clients.desktop.downloads.macos.filename).toBe('DoraMCU.dmg');
            expect(clients.desktop.downloads.linux.filename).toBe('DoraMCU.AppImage');
        });
    });

    describe('getApiUrl()', () => {

        test('should return default URL when config is not loaded', () => {
            const url = ConfigManager.getApiUrl();
            expect(url).toBe('http://localhost:8080');
        });

        test('should return API URL from loaded config', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({
                    apiUrl: 'http://api.example.com',
                    version: '1.0.0',
                    clients: {}
                })
            });

            await ConfigManager.loadConfig();
            expect(ConfigManager.getApiUrl()).toBe('http://api.example.com');
        });
    });

    describe('getClientsForPlatform()', () => {

        test('should return empty array when config is not loaded', () => {
            const clients = ConfigManager.getClientsForPlatform('windows');
            expect(clients).toEqual([]);
        });

        test('should return clients matching the platform', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: {
                        name: 'Desktop',
                        platforms: ['windows', 'macos', 'linux']
                    },
                    mobile: {
                        name: 'Mobile',
                        platforms: ['android', 'ios']
                    },
                    web: {
                        name: 'Web',
                        platforms: ['all']
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const clients = ConfigManager.getClientsForPlatform('windows');

            expect(clients.length).toBe(2);
            expect(clients[0].id).toBe('desktop');
            expect(clients[1].id).toBe('web');
        });

        test('should include clients with "all" platform', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    web: {
                        name: 'Web',
                        platforms: ['all']
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const clients = ConfigManager.getClientsForPlatform('android');

            expect(clients.length).toBe(1);
            expect(clients[0].id).toBe('web');
        });

        test('should exclude clients not matching the platform', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: {
                        name: 'Desktop',
                        platforms: ['windows', 'macos', 'linux']
                    },
                    mobile: {
                        name: 'Mobile',
                        platforms: ['android', 'ios']
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const clients = ConfigManager.getClientsForPlatform('windows');

            expect(clients.length).toBe(1);
            expect(clients[0].id).toBe('desktop');
        });

        test('should include id in returned clients', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: {
                        name: 'Desktop',
                        platforms: ['windows']
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const clients = ConfigManager.getClientsForPlatform('windows');

            expect(clients[0]).toHaveProperty('id', 'desktop');
            expect(clients[0]).toHaveProperty('name', 'Desktop');
        });
    });

    describe('getDownloadUrl()', () => {

        test('should return null when config is not loaded', () => {
            const url = ConfigManager.getDownloadUrl('desktop', 'windows');
            expect(url).toBeNull();
        });

        test('should return null when client not found', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {}
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const url = ConfigManager.getDownloadUrl('desktop', 'windows');
            expect(url).toBeNull();
        });

        test('should return null when client has no downloads', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    web: {
                        name: 'Web',
                        platforms: ['all'],
                        url: '/clients/desktop/'
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const url = ConfigManager.getDownloadUrl('web', 'windows');
            expect(url).toBeNull();
        });

        test('should return null when OS not found in downloads', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: {
                        downloads: {
                            windows: { url: '/api/v1/clients/download/unity/windows' }
                        }
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const url = ConfigManager.getDownloadUrl('desktop', 'macos');
            expect(url).toBeNull();
        });

        test('should prepend API URL to relative download URL', async () => {
            const config = {
                apiUrl: 'http://api.example.com',
                version: '1.0.0',
                clients: {
                    desktop: {
                        downloads: {
                            windows: { url: '/api/v1/clients/download/unity/windows' }
                        }
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const url = ConfigManager.getDownloadUrl('desktop', 'windows');
            expect(url).toBe('http://api.example.com/api/v1/clients/download/unity/windows');
        });

        test('should return absolute URL as-is', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: {
                        downloads: {
                            windows: { url: 'https://cdn.example.com/DoraMCU-Setup.exe' }
                        }
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            const url = ConfigManager.getDownloadUrl('desktop', 'windows');
            expect(url).toBe('https://cdn.example.com/DoraMCU-Setup.exe');
        });

        test('should return correct URL for each OS', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: {
                        downloads: {
                            windows: { url: '/api/v1/clients/download/unity/windows' },
                            macos: { url: '/api/v1/clients/download/unity/macos' },
                            linux: { url: '/api/v1/clients/download/unity/linux' }
                        }
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();

            expect(ConfigManager.getDownloadUrl('desktop', 'windows'))
                .toBe('http://localhost:8080/api/v1/clients/download/unity/windows');
            expect(ConfigManager.getDownloadUrl('desktop', 'macos'))
                .toBe('http://localhost:8080/api/v1/clients/download/unity/macos');
            expect(ConfigManager.getDownloadUrl('desktop', 'linux'))
                .toBe('http://localhost:8080/api/v1/clients/download/unity/linux');
        });
    });

    describe('getWebClientUrl()', () => {

        test('should return URL from config when available', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: { url: '/custom/desktop/' },
                    mobile: { url: '/custom/mobile/' },
                    tablet: { url: '/custom/tablet/' }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();

            expect(ConfigManager.getWebClientUrl('desktop')).toBe('/custom/desktop/');
            expect(ConfigManager.getWebClientUrl('mobile')).toBe('/custom/mobile/');
            expect(ConfigManager.getWebClientUrl('tablet')).toBe('/custom/tablet/');
        });

        test('should return fallback URL when client not in config', () => {
            expect(ConfigManager.getWebClientUrl('desktop')).toBe('/clients/desktop/');
            expect(ConfigManager.getWebClientUrl('tablet')).toBe('/clients/tablet/');
            expect(ConfigManager.getWebClientUrl('mobile')).toBe('/clients/mobile/');
            expect(ConfigManager.getWebClientUrl('low-end')).toBe('/clients/mobile/');
        });

        test('should return mobile fallback for unknown tier', () => {
            const url = ConfigManager.getWebClientUrl('unknown-tier');
            expect(url).toBe('/clients/mobile/');
        });

        test('should return mobile fallback for low-end tier', () => {
            const url = ConfigManager.getWebClientUrl('low-end');
            expect(url).toBe('/clients/mobile/');
        });

        test('should prefer config URL over fallback', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: { url: '/production/desktop/' }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            expect(ConfigManager.getWebClientUrl('desktop')).toBe('/production/desktop/');
        });

        test('should return fallback when client has no URL property', async () => {
            const config = {
                apiUrl: 'http://localhost:8080',
                version: '1.0.0',
                clients: {
                    desktop: { name: 'Desktop' }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();
            expect(ConfigManager.getWebClientUrl('desktop')).toBe('/clients/desktop/');
        });
    });

    describe('Integration scenarios', () => {

        test('full flow: load config and resolve download URL', async () => {
            const config = {
                apiUrl: 'https://api.doramcu.com',
                version: '3.0.0',
                clients: {
                    desktop: {
                        name: 'DoraMCU Desktop',
                        platforms: ['windows', 'macos', 'linux'],
                        url: '/clients/desktop/',
                        downloads: {
                            windows: { url: '/api/v1/clients/download/unity/windows' },
                            macos: { url: '/api/v1/clients/download/unity/macos' }
                        }
                    },
                    mobile: {
                        name: 'DoraMCU Mobile',
                        platforms: ['android', 'ios'],
                        url: '/clients/mobile/'
                    }
                }
            };
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(config)
            });

            await ConfigManager.loadConfig();

            expect(ConfigManager.getApiUrl()).toBe('https://api.doramcu.com');
            expect(ConfigManager.getDownloadUrl('desktop', 'windows'))
                .toBe('https://api.doramcu.com/api/v1/clients/download/unity/windows');
            expect(ConfigManager.getWebClientUrl('desktop')).toBe('/clients/desktop/');
            expect(ConfigManager.getWebClientUrl('mobile')).toBe('/clients/mobile/');

            const clients = ConfigManager.getClientsForPlatform('windows');
            expect(clients.length).toBe(1);
            expect(clients[0].id).toBe('desktop');
        });

        test('full flow: fallback to defaults on network failure', async () => {
            mockFetch.mockRejectedValue(new Error('DNS lookup failed'));

            const config = await ConfigManager.loadConfig();

            expect(config.apiUrl).toBe('http://localhost:8080');
            expect(ConfigManager.getWebClientUrl('desktop')).toBe('/clients/desktop/');
            expect(ConfigManager.getWebClientUrl('mobile')).toBe('/clients/mobile/');
            expect(ConfigManager.getDownloadUrl('desktop', 'windows'))
                .toBe('http://localhost:8080/api/v1/clients/download/unity/windows');
        });
    });
});

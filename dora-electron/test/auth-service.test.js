/**
 * AuthService Unit Tests
 *
 * Tests for the dora-electron authentication service (auth-service.js).
 * Uses Jest with fetch mocking.
 *
 * Run: npx jest test/auth-service.test.js
 */

// Mock localStorage
const localStorageMock = (function() {
    let store = {};
    return {
        getItem: jest.fn(key => store[key] !== undefined ? store[key] : null),
        setItem: jest.fn((key, value) => { store[key] = String(value); }),
        removeItem: jest.fn(key => { delete store[key]; }),
        clear: jest.fn(() => { store = {}; }),
    };
})();

// Mock fetch
const mockFetch = jest.fn();

// Mock console
const originalConsole = console;

beforeAll(() => {
    console.error = jest.fn();
    console.log = jest.fn();
});

afterAll(() => {
    console.error = originalConsole.error;
    console.log = originalConsole.log;
});

// Import the auth service
beforeEach(() => {
    localStorageMock.clear();
    mockFetch.mockReset();
    jest.resetModules();
    
    // Set up globals before loading the module
    global.localStorage = localStorageMock;
    global.fetch = mockFetch;
    global.window = { 
        dispatchEvent: jest.fn(),
        localStorage: localStorageMock
    };
    
    // Load the module — it attaches authService to window
    require('../renderer/js/auth-service.js');
});

afterEach(() => {
    jest.resetModules();
});

// Helper to get the service from window
function getService() {
    return global.window.authService;
}

describe('AuthService', () => {
    describe('Constructor', () => {
        test('should default serverUrl to localhost:8080', () => {
            expect(getService().getServerUrl()).toBe('http://localhost:8080');
        });

        test('should read serverUrl from localStorage', () => {
            localStorageMock.setItem('serverUrl', 'http://custom:9000');
            jest.resetModules();
            global.localStorage = localStorageMock;
            global.fetch = mockFetch;
            global.window = { dispatchEvent: jest.fn(), localStorage: localStorageMock };
            require('../renderer/js/auth-service.js');
            expect(getService().getServerUrl()).toBe('http://custom:9000');
        });

        test('should default connected to false', () => {
            expect(getService().connected).toBe(false);
        });

        test('should default token to null', () => {
            expect(getService().token).toBeNull();
        });

        test('should read token from localStorage', () => {
            localStorageMock.setItem('authToken', 'stored-token');
            jest.resetModules();
            global.localStorage = localStorageMock;
            global.fetch = mockFetch;
            global.window = { dispatchEvent: jest.fn(), localStorage: localStorageMock };
            require('../renderer/js/auth-service.js');
            expect(getService().token).toBe('stored-token');
        });
    });

    describe('setServerUrl', () => {
        test('should update serverUrl', () => {
            getService().setServerUrl('http://newserver:3000');
            expect(getService().getServerUrl()).toBe('http://newserver:3000');
        });

        test('should persist serverUrl to localStorage', () => {
            getService().setServerUrl('http://newserver:3000');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('serverUrl', 'http://newserver:3000');
        });
    });

    describe('connect', () => {
        test('should set connected to true on successful health check', async () => {
            mockFetch.mockResolvedValue({ ok: true, json: () => Promise.resolve({ status: 'UP' }) });
            await getService().connect();
            expect(getService().connected).toBe(true);
        });

        test('should call /actuator/health endpoint', async () => {
            mockFetch.mockResolvedValue({ ok: true, json: () => Promise.resolve({ status: 'UP' }) });
            await getService().connect();
            expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/actuator/health', {
                method: 'GET',
                mode: 'cors'
            });
        });

        test('should return success object on connect', async () => {
            mockFetch.mockResolvedValue({ ok: true, json: () => Promise.resolve({ status: 'UP' }) });
            const result = await getService().connect();
            expect(result.success).toBe(true);
        });

        test('should set connected to false on failed health check', async () => {
            mockFetch.mockResolvedValue({ ok: false, status: 500 });
            await getService().connect();
            expect(getService().connected).toBe(false);
        });

        test('should set connected to false on network error', async () => {
            mockFetch.mockRejectedValue(new Error('ECONNREFUSED'));
            await getService().connect();
            expect(getService().connected).toBe(false);
        });

        test('should return error object on failure', async () => {
            mockFetch.mockRejectedValue(new Error('ECONNREFUSED'));
            const result = await getService().connect();
            expect(result.success).toBe(false);
            expect(result.error).toBe('Server unreachable');
        });
    });

    describe('login', () => {
        test('should set token on successful login', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({ token: 'jwt-token-123' })
            });
            await getService().login('user', 'pass');
            expect(getService().token).toBe('jwt-token-123');
        });

        test('should persist token to localStorage', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({ token: 'jwt-token-123' })
            });
            await getService().login('user', 'pass');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('authToken', 'jwt-token-123');
        });

        test('should call /api/auth/login endpoint', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({ token: 'jwt-token-123' })
            });
            await getService().login('user', 'pass');
            expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: 'user', password: 'pass' })
            });
        });

        test('should return error object on failed login', async () => {
            mockFetch.mockResolvedValue({ ok: false, status: 401 });
            const result = await getService().login('user', 'wrong');
            expect(result.success).toBe(false);
            expect(result.error).toBe('HTTP 401');
        });

        test('should return error object on network error', async () => {
            mockFetch.mockRejectedValue(new Error('ECONNREFUSED'));
            const result = await getService().login('user', 'pass');
            expect(result.success).toBe(false);
            expect(result.error).toBe('ECONNREFUSED');
        });

        test('should set connected to true on successful login', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({ token: 'jwt-token-123' })
            });
            await getService().login('user', 'pass');
            expect(getService().connected).toBe(true);
        });

        test('should set user on successful login', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({ token: 'jwt-token-123' })
            });
            await getService().login('player', 'pass');
            expect(getService().user.name).toBe('player');
        });
    });

    describe('isAuthenticated', () => {
        test('should return true when token is set', () => {
            getService().token = 'some-token';
            expect(getService().isAuthenticated()).toBe(true);
        });

        test('should return false when token is null', () => {
            getService().token = null;
            expect(getService().isAuthenticated()).toBe(false);
        });
    });

    describe('getStatus', () => {
        test('should return status object', () => {
            const service = getService();
            service.connected = true;
            service.user = { name: 'test' };
            const status = service.getStatus();
            expect(status).toEqual({
                connected: true,
                serverUrl: 'http://localhost:8080',
                user: { name: 'test' }
            });
        });
    });

    describe('fetchJson', () => {
        test('should return JSON response', async () => {
            const response = {
                ok: true,
                json: () => Promise.resolve({ data: 'test' })
            };
            mockFetch.mockResolvedValue(response);
            const result = await getService().fetchJson('/api/test');
            expect(result).toEqual({ data: 'test' });
        });

        test('should include Authorization header when token is set', async () => {
            getService().token = 'my-token';
            const response = {
                ok: true,
                json: () => Promise.resolve({ ok: true })
            };
            mockFetch.mockResolvedValue(response);
            await getService().fetchJson('/api/test');
            const callArgs = mockFetch.mock.calls[0][1];
            expect(callArgs.headers['Authorization']).toBe('Bearer my-token');
        });

        test('should include Content-Type header', async () => {
            const response = {
                ok: true,
                json: () => Promise.resolve({ ok: true })
            };
            mockFetch.mockResolvedValue(response);
            await getService().fetchJson('/api/test');
            const callArgs = mockFetch.mock.calls[0][1];
            expect(callArgs.headers['Content-Type']).toBe('application/json');
        });

        test('should throw on non-ok response', async () => {
            const response = { ok: false, status: 404 };
            mockFetch.mockResolvedValue(response);
            await expect(getService().fetchJson('/api/missing')).rejects.toThrow('HTTP 404');
        });

        test('should throw on network error', async () => {
            mockFetch.mockRejectedValue(new Error('Network error'));
            await expect(getService().fetchJson('/api/test')).rejects.toThrow('Network error');
        });

        test('should merge custom headers', async () => {
            const response = {
                ok: true,
                json: () => Promise.resolve({ ok: true })
            };
            mockFetch.mockResolvedValue(response);
            await getService().fetchJson('/api/test', {
                headers: { 'X-Custom': 'value' }
            });
            const callArgs = mockFetch.mock.calls[0][1];
            expect(callArgs.headers['X-Custom']).toBe('value');
        });

        test('should pass through custom options', async () => {
            const response = {
                ok: true,
                json: () => Promise.resolve({ ok: true })
            };
            mockFetch.mockResolvedValue(response);
            await getService().fetchJson('/api/test', {
                method: 'POST',
                body: JSON.stringify({ key: 'value' })
            });
            const callArgs = mockFetch.mock.calls[0][1];
            expect(callArgs.method).toBe('POST');
            expect(callArgs.body).toBe(JSON.stringify({ key: 'value' }));
        });
    });

    describe('Integration scenarios', () => {
        test('full login flow: connect, login, fetch protected resource', async () => {
            const service = getService();
            // Connect
            mockFetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({ status: 'UP' }) });
            await service.connect();
            expect(service.connected).toBe(true);

            // Login
            mockFetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve({ token: 'session-token' })
            });
            await service.login('player', 'secret');
            expect(service.token).toBe('session-token');

            // Fetch protected resource
            mockFetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve({ username: 'player', isAdmin: false })
            });
            const profile = await service.fetchJson('/api/v1/auth/me');
            expect(profile).toEqual({ username: 'player', isAdmin: false });
        });

        test('server offline: connect fails, login returns error object', async () => {
            const service = getService();
            mockFetch.mockRejectedValue(new Error('ECONNREFUSED'));
            const connectResult = await service.connect();
            expect(service.connected).toBe(false);
            expect(connectResult.success).toBe(false);

            const loginResult = await service.login('user', 'pass');
            expect(loginResult.success).toBe(false);
            expect(service.token).toBeNull();
        });

        test('token refresh after page load', () => {
            localStorageMock.setItem('authToken', 'persisted-token');
            jest.resetModules();
            global.localStorage = localStorageMock;
            global.fetch = mockFetch;
            global.window = { dispatchEvent: jest.fn(), localStorage: localStorageMock };
            require('../renderer/js/auth-service.js');
            expect(getService().token).toBe('persisted-token');
        });

        test('register calls onboarding endpoint', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve({ token: 'reg-token', username: 'newuser' })
            });
            await getService().register({ username: 'newuser', password: 'pass', email: 'test@test.com' });
            expect(mockFetch).toHaveBeenCalledWith('http://localhost:8080/api/v1/onboarding/register', expect.any(Object));
        });
    });
});

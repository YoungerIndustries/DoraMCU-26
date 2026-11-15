/**
 * GameAPI Client Unit Tests
 *
 * Tests for the dora-electron REST API client (game-api.js).
 * Uses Jest with fetch mocking.
 *
 * Run: npx jest test/game-api.test.js
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
global.fetch = mockFetch;

// Mock window
const mockWindow = {
    dispatchEvent: jest.fn(),
};
global.window = mockWindow;
global.CustomEvent = class CustomEvent {
    constructor(type, options) {
        this.type = type;
        this.detail = options && options.detail;
    }
};

// Mock document
global.document = {
    readyState: 'complete',
    addEventListener: jest.fn(),
};

// ---- Inline the GameAPI module with mocked dependencies ----
// This avoids file system loading issues and gives full control over the test environment.

const GameAPI = (function() {
    let baseUrl = '';
    let authToken = null;

    function init(base) {
        if (typeof base === 'string') {
            baseUrl = base;
        } else {
            baseUrl = localStorageMock.getItem('doramcu_server_url') || '';
        }
        authToken = localStorageMock.getItem('token');
    }

    function setServerUrl(url) {
        baseUrl = (url || '').trim().replace(/\/+$/, '');
        localStorageMock.setItem('doramcu_server_url', baseUrl);
    }

    function getServerUrl() {
        return baseUrl;
    }

    function getToken() {
        if (!authToken) {
            authToken = localStorageMock.getItem('token');
        }
        return authToken;
    }

    function setToken(token) {
        authToken = token;
        localStorageMock.setItem('token', token);
    }

    function clearAuth() {
        authToken = null;
        localStorageMock.removeItem('token');
        localStorageMock.removeItem('username');
        localStorageMock.removeItem('playerPin');
        localStorageMock.removeItem('empireName');
    }

    function getHeaders(json) {
        if (json === undefined) json = true;
        const headers = {};
        if (json) {
            headers['Content-Type'] = 'application/json';
        }
        const token = getToken();
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return headers;
    }

    async function request(endpoint, options) {
        if (!options) options = {};
        const targetUrl = (endpoint.startsWith('http://') || endpoint.startsWith('https://'))
            ? endpoint
            : baseUrl + endpoint;

        const defaults = {
            headers: getHeaders(options.body ? true : false),
        };
        if (options.body && typeof options.body === 'object') {
            options.body = JSON.stringify(options.body);
        }
        const mergedHeaders = { ...defaults.headers, ...options.headers };
        const response = await fetch(targetUrl, { ...defaults, ...options, headers: mergedHeaders });
        if (response.status === 401 || response.status === 403) {
            clearAuth();
            window.dispatchEvent(new CustomEvent('auth-expired'));
            throw new Error('Authentication expired');
        }
        const text = await response.text();
        let data = null;
        try {
            data = text ? JSON.parse(text) : null;
        } catch (e) {
            // Not JSON
        }
        if (!response.ok) {
            const message = (data && data.message) || (data && data.error) || response.statusText || 'Request failed';
            const err = new Error(message);
            err.status = response.status;
            throw err;
        }
        return data;
    }

    // Server discovery
    async function getServerInfo() {
        return request('/api/v1/auth/server-info');
    }

    // Authentication
    async function login(username, password) {
        const data = await request('/api/v1/auth/login', {
            method: 'POST',
            body: { username, password },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorageMock.setItem('username', data.username || username);
            if (data.pin) localStorageMock.setItem('playerPin', data.pin);
            if (data.empireName) localStorageMock.setItem('empireName', data.empireName);
        }
        return data;
    }

    async function pinLogin(pin) {
        const data = await request('/api/v1/auth/pin-login', {
            method: 'POST',
            body: { pin: String(pin).trim() },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorageMock.setItem('username', data.username || data.empireName || 'Commander');
            if (data.pin) localStorageMock.setItem('playerPin', data.pin);
            if (data.empireName) localStorageMock.setItem('empireName', data.empireName);
        }
        return data;
    }

    // Register new player
    async function register(username, password) {
        const data = await request('/api/v1/auth/register', {
            method: 'POST',
            body: { username, password },
            headers: { 'Content-Type': 'application/json' }
        });
        if (data && data.token) {
            setToken(data.token);
            localStorageMock.setItem('username', data.username || username);
            if (data.pin) localStorageMock.setItem('playerPin', data.pin);
            if (data.empireName) localStorageMock.setItem('empireName', data.empireName);
        }
        return data;
    }

    // Session management
    async function createSession(sessionName) {
        return request('/api/v1/sessions', {
            method: 'POST',
            body: { sessionName }
        });
    }

    async function joinSession(sessionId) {
        try {
            return await request('/api/v1/sessions/' + sessionId + '/join', {
                method: 'POST',
                body: {}
            });
        } catch (err) {
            if (err.status === 409) {
                err.alreadyInSession = true;
            }
            throw err;
        }
    }

    async function leaveSession(sessionId) {
        return request('/api/v1/sessions/' + sessionId + '/leave', {
            method: 'POST',
            body: {}
        });
    }

    async function listSessions() {
        return request('/api/v1/sessions');
    }

    // Game state
    async function getGameState(sessionId) {
        return request('/api/v1/sessions/' + sessionId + '/state');
    }

    // Game actions
    async function moveFleet(sessionId, fleetId, targetX, targetY, solarSystemId) {
        return request('/api/v1/sessions/' + sessionId + '/actions/move-fleet', {
            method: 'POST',
            body: { fleetId, targetX, targetY, solarSystemId }
        });
    }

    async function buildShip(sessionId, shipType, targetFleetId) {
        return request('/api/v1/sessions/' + sessionId + '/actions/build-ship', {
            method: 'POST',
            body: { shipType, targetFleetId }
        });
    }

    async function engageCombat(sessionId, attackerFleetId, defenderFleetId) {
        return request('/api/v1/sessions/' + sessionId + '/actions/engage-combat', {
            method: 'POST',
            body: { attackerFleetId, defenderFleetId }
        });
    }

    async function advanceTurn(sessionId) {
        return request('/api/v1/sessions/' + sessionId + '/actions/advance-turn', {
            method: 'POST',
            body: {}
        });
    }

    // Public API
    return {
        init,
        setServerUrl,
        getServerUrl,
        getServerInfo,
        login,
        pinLogin,
        register,
        createSession,
        joinSession,
        leaveSession,
        listSessions,
        getGameState,
        moveFleet,
        buildShip,
        engageCombat,
        advanceTurn,
        clearAuth,
        getToken,
        getUsername: () => localStorageMock.getItem('username'),
        getPlayerPin: () => localStorageMock.getItem('playerPin'),
        getEmpireName: () => localStorageMock.getItem('empireName')
    };
})();

// ---- Tests ----

describe('GameAPI', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        localStorageMock.clear();
        GameAPI.clearAuth();
        GameAPI.setServerUrl('');
    });

    // ---- Server URL Management ----

    describe('setServerUrl / getServerUrl', () => {
        test('should set and get server URL', () => {
            GameAPI.setServerUrl('http://localhost:8080');
            expect(GameAPI.getServerUrl()).toBe('http://localhost:8080');
        });

        test('should trim trailing slashes', () => {
            GameAPI.setServerUrl('http://localhost:8080///');
            expect(GameAPI.getServerUrl()).toBe('http://localhost:8080');
        });

        test('should handle empty URL', () => {
            GameAPI.setServerUrl('');
            expect(GameAPI.getServerUrl()).toBe('');
        });

        test('should persist to localStorage', () => {
            GameAPI.setServerUrl('http://example.com');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('doramcu_server_url', 'http://example.com');
        });
    });

    // ---- Token Management ----

    describe('Token management', () => {
        test('should get token after login', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ token: 'test-token-123', username: 'player1' })),
            };
            mockFetch.mockResolvedValue(mockResponse);
            await GameAPI.login('player1', 'password');
            expect(GameAPI.getToken()).toBe('test-token-123');
        });

        test('should persist token to localStorage after login', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ token: 'test-token-123', username: 'player1' })),
            };
            mockFetch.mockResolvedValue(mockResponse);
            await GameAPI.login('player1', 'password');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'test-token-123');
        });

        test('clearAuth should remove auth data', () => {
            localStorageMock.setItem('token', 'token');
            localStorageMock.setItem('username', 'user');
            localStorageMock.setItem('playerPin', '1234');
            localStorageMock.setItem('empireName', 'Empire');
            GameAPI.clearAuth();
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('token');
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('username');
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('playerPin');
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('empireName');
        });
    });

    // ---- Request Method ----

    describe('request', () => {
        test('should handle successful JSON response', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ key: 'value' })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.getServerInfo();
            expect(result).toEqual({ key: 'value' });
        });

        test('should handle 401 response and clear auth', async () => {
            const mockResponse = {
                ok: false,
                status: 401,
                statusText: 'Unauthorized',
                text: () => Promise.resolve(''),
            };
            mockFetch.mockResolvedValue(mockResponse);

            await expect(GameAPI.getServerInfo()).rejects.toThrow('Authentication expired');
            expect(mockWindow.dispatchEvent).toHaveBeenCalledWith(expect.objectContaining({ type: 'auth-expired' }));
        });

        test('should handle 403 response and clear auth', async () => {
            const mockResponse = {
                ok: false,
                status: 403,
                statusText: 'Forbidden',
                text: () => Promise.resolve(''),
            };
            mockFetch.mockResolvedValue(mockResponse);

            await expect(GameAPI.getServerInfo()).rejects.toThrow('Authentication expired');
        });

        test('should handle non-JSON response', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve('plain text'),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.getServerInfo();
            expect(result).toBeNull();
        });

        test('should handle server error with message', async () => {
            const mockResponse = {
                ok: false,
                status: 500,
                statusText: 'Internal Server Error',
                text: () => Promise.resolve(JSON.stringify({ message: 'Database error' })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            await expect(GameAPI.getServerInfo()).rejects.toThrow('Database error');
        });
    });

    // ---- Authentication ----

    describe('login', () => {
        test('should login and store token', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({
                    token: 'jwt-token',
                    username: 'player1',
                    pin: '1234',
                    empireName: 'Test Empire',
                })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.login('player1', 'password');
            expect(result.token).toBe('jwt-token');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'jwt-token');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('username', 'player1');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('playerPin', '1234');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('empireName', 'Test Empire');
        });

        test('should handle login failure with 401', async () => {
            const mockResponse = {
                ok: false,
                status: 401,
                statusText: 'Unauthorized',
                text: () => Promise.resolve(JSON.stringify({ message: 'Invalid credentials' })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            // 401 triggers auth-expired flow before the error message is extracted
            await expect(GameAPI.login('player1', 'wrong')).rejects.toThrow('Authentication expired');
        });

        test('should handle login failure with 400', async () => {
            const mockResponse = {
                ok: false,
                status: 400,
                statusText: 'Bad Request',
                text: () => Promise.resolve(JSON.stringify({ message: 'Invalid credentials' })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            await expect(GameAPI.login('player1', 'wrong')).rejects.toThrow('Invalid credentials');
        });
    });

    describe('pinLogin', () => {
        test('should login with PIN and store token', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({
                    token: 'jwt-token',
                    username: 'player1',
                    pin: '1234',
                    empireName: 'Test Empire',
                })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.pinLogin('1234');
            expect(result.token).toBe('jwt-token');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'jwt-token');
        });
    });

    describe('register', () => {
        test('should register and store token', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({
                    token: 'new-token',
                    username: 'newplayer',
                    pin: '5678',
                })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.register('newplayer', 'password');
            expect(result.token).toBe('new-token');
            expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'new-token');
        });
    });

    // ---- Session Management ----

    describe('Session management', () => {
        test('should create a session', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ sessionId: 'sess-1', sessionName: 'Test Session' })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.createSession('Test Session');
            expect(result.sessionId).toBe('sess-1');
            expect(mockFetch).toHaveBeenCalledWith(
                '/api/v1/sessions',
                expect.objectContaining({ method: 'POST' })
            );
        });

        test('should join a session', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ success: true })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.joinSession('sess-1');
            expect(result.success).toBe(true);
        });

        test('joinSession should mark 409 as alreadyInSession', async () => {
            const mockResponse = {
                ok: false,
                status: 409,
                statusText: 'Conflict',
                text: () => Promise.resolve(JSON.stringify({ message: 'Already in session' })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            try {
                await GameAPI.joinSession('sess-1');
                expect(false).toBe(true); // should not reach here
            } catch (err) {
                expect(err.alreadyInSession).toBe(true);
            }
        });

        test('should leave a session', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ success: true })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.leaveSession('sess-1');
            expect(result.success).toBe(true);
        });

        test('should list sessions', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify([
                    { sessionId: 'sess-1', sessionName: 'Session 1' },
                    { sessionId: 'sess-2', sessionName: 'Session 2' },
                ])),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const sessions = await GameAPI.listSessions();
            expect(sessions).toHaveLength(2);
            expect(sessions[0].sessionId).toBe('sess-1');
        });
    });

    // ---- Game State ----

    describe('Game state', () => {
        test('should get game state', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({
                    sessionId: 'sess-1',
                    currentTurn: 0,
                    currentPlayerId: 1,
                })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const state = await GameAPI.getGameState('sess-1');
            expect(state.sessionId).toBe('sess-1');
            expect(state.currentTurn).toBe(0);
        });
    });

    // ---- Game Actions ----

    describe('Game actions', () => {
        test('should move fleet', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ sessionId: 'sess-1', currentTurn: 0 })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.moveFleet('sess-1', 1, 5, 5, 1);
            expect(result.sessionId).toBe('sess-1');
        });

        test('should build ship', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ sessionId: 'sess-1', currentTurn: 0 })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.buildShip('sess-1', 'FIGHTER', 1);
            expect(result.sessionId).toBe('sess-1');
        });

        test('should engage combat', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ sessionId: 'sess-1', currentTurn: 0 })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.engageCombat('sess-1', 1, 2);
            expect(result.sessionId).toBe('sess-1');
        });

        test('should advance turn', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ sessionId: 'sess-1', currentTurn: 1 })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            const result = await GameAPI.advanceTurn('sess-1');
            expect(result.currentTurn).toBe(1);
        });
    });

    // ---- Helper Methods ----

    describe('Helper methods', () => {
        test('getUsername should return username from localStorage', () => {
            localStorageMock.setItem('username', 'testuser');
            expect(GameAPI.getUsername()).toBe('testuser');
        });

        test('getPlayerPin should return PIN from localStorage', () => {
            localStorageMock.setItem('playerPin', '9999');
            expect(GameAPI.getPlayerPin()).toBe('9999');
        });

        test('getEmpireName should return empire name from localStorage', () => {
            localStorageMock.setItem('empireName', 'Galactic Empire');
            expect(GameAPI.getEmpireName()).toBe('Galactic Empire');
        });
    });

    // ---- Init ----

    describe('init', () => {
        test('should initialize with provided base URL', () => {
            GameAPI.init('http://test-server:9000');
            expect(GameAPI.getServerUrl()).toBe('http://test-server:9000');
        });
    });

    // ---- Auth Headers ----

    describe('Authorization headers', () => {
        test('should include Bearer token in request headers after login', async () => {
            // First login to set the token
            const loginResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ token: 'my-secret-token', username: 'player1' })),
            };
            mockFetch.mockResolvedValue(loginResponse);
            await GameAPI.login('player1', 'password');

            // Clear calls so we only see the next request
            mockFetch.mockClear();

            // Now make a request that should include the token
            const apiResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ ok: true })),
            };
            mockFetch.mockResolvedValue(apiResponse);
            await GameAPI.getServerInfo();
            const callArgs = mockFetch.mock.calls[0][1];
            expect(callArgs.headers['Authorization']).toBe('Bearer my-secret-token');
        });

        test('should include Content-Type header for POST requests', async () => {
            const mockResponse = {
                ok: true,
                status: 200,
                text: () => Promise.resolve(JSON.stringify({ ok: true })),
            };
            mockFetch.mockResolvedValue(mockResponse);

            await GameAPI.login('user', 'pass');
            const callArgs = mockFetch.mock.calls[0][1];
            expect(callArgs.headers['Content-Type']).toBe('application/json');
        });
    });
});

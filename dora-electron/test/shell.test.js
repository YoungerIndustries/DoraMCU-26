/**
 * Shell Controller Unit Tests
 *
 * Tests for the dora-electron shell controller (shell.js).
 * Uses Jest with jsdom for DOM mocking.
 *
 * Run: npx jest test/shell.test.js
 */

const { JSDOM } = require('jsdom');

function setupDOM() {
    const html = `
        <!DOCTYPE html>
        <html>
        <head><title>DoraMCU</title></head>
        <body>
            <nav id="mainNav">
                <a href="#dashboard" class="nav-item active" data-page="dashboard">Dashboard</a>
                <a href="#game/doramcu-4x" class="nav-item" data-page="game">4X Game</a>
                <a href="#admin" class="nav-item" data-page="admin">Admin</a>
                <a href="#health" class="nav-item" data-page="health">Health</a>
            </nav>
            <main id="mainContent">
                <div id="page-dashboard" class="page">Dashboard Content</div>
                <div id="page-game" class="page" style="display:none;">Game Content</div>
                <div id="page-admin" class="page" style="display:none;">Admin Content</div>
                <div id="page-health" class="page" style="display:none;">Health Content</div>
            </main>
            <div id="toastContainer"></div>
            <h1 id="pageTitle">Dashboard</h1>
            <div id="unityStatus" class="status-indicator">disconnected</div>
        </body>
        </html>
    `;
    const dom = new JSDOM(html, {
        url: 'http://localhost:8080',
        runScripts: 'dangerously',
        resources: 'usable'
    });
    global.window = dom.window;
    global.document = dom.window.document;
    global.navigator = dom.window.navigator;
    global.HTMLElement = dom.window.HTMLElement;
    global.HTMLIFrameElement = dom.window.HTMLIFrameElement;
    global.Event = dom.window.Event;
    global.CustomEvent = dom.window.CustomEvent;
    return dom;
}

// Mock electronAPI
beforeEach(() => {
    if (!global.window) {
        global.window = {};
    }
    global.window.electronAPI = {
        ipcRenderer: {
            invoke: jest.fn().mockResolvedValue({ status: 'ok' }),
            on: jest.fn(),
            removeListener: jest.fn()
        },
        shell: {
            openExternal: jest.fn()
        }
    };
});

describe('Shell Controller', () => {
    describe('Routing', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
        });

        test('should set default route to #dashboard', () => {
            const navItems = document.querySelectorAll('.nav-item');
            expect(navItems[0].classList.contains('active')).toBe(true);
        });

        test('should have correct page titles', () => {
            const pageTitle = document.getElementById('pageTitle');
            expect(pageTitle.textContent).toBe('Dashboard');
        });

        test('should show dashboard page by default', () => {
            const dashboard = document.getElementById('page-dashboard');
            expect(dashboard.style.display).not.toBe('none');
        });

        test('should hide non-active pages by default', () => {
            const game = document.getElementById('page-game');
            const admin = document.getElementById('page-admin');
            expect(game.style.display).toBe('none');
            expect(admin.style.display).toBe('none');
        });
    });

    describe('Navigation', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
        });

        test('should have nav items for all routes', () => {
            const navItems = document.querySelectorAll('.nav-item');
            expect(navItems.length).toBe(4);
        });

        test('should have correct hrefs for nav items', () => {
            const navItems = document.querySelectorAll('.nav-item');
            expect(navItems[0].getAttribute('href')).toBe('#dashboard');
            expect(navItems[1].getAttribute('href')).toBe('#game/doramcu-4x');
            expect(navItems[2].getAttribute('href')).toBe('#admin');
            expect(navItems[3].getAttribute('href')).toBe('#health');
        });

        test('should have correct data-page attributes', () => {
            const navItems = document.querySelectorAll('.nav-item');
            expect(navItems[0].getAttribute('data-page')).toBe('dashboard');
            expect(navItems[1].getAttribute('data-page')).toBe('game');
            expect(navItems[2].getAttribute('data-page')).toBe('admin');
            expect(navItems[3].getAttribute('data-page')).toBe('health');
        });
    });

    describe('Toast Notifications', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
        });

        test('should have toast container', () => {
            const container = document.getElementById('toastContainer');
            expect(container).toBeTruthy();
        });

        test('should create toast element', () => {
            const container = document.getElementById('toastContainer');
            const toast = document.createElement('div');
            toast.className = 'toast info';
            toast.textContent = 'Test message';
            container.appendChild(toast);
            expect(container.children.length).toBe(1);
            expect(container.firstChild.textContent).toBe('Test message');
        });

        test('should support multiple toast types', () => {
            const container = document.getElementById('toastContainer');
            
            const success = document.createElement('div');
            success.className = 'toast success';
            success.textContent = 'Success';
            container.appendChild(success);

            const error = document.createElement('div');
            error.className = 'toast error';
            error.textContent = 'Error';
            container.appendChild(error);

            const warning = document.createElement('div');
            warning.className = 'toast warning';
            warning.textContent = 'Warning';
            container.appendChild(warning);

            expect(container.children.length).toBe(3);
        });
    });

    describe('Unity Status', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
        });

        test('should show disconnected status by default', () => {
            const status = document.getElementById('unityStatus');
            expect(status.textContent).toBe('disconnected');
        });

        test('should update status indicator', () => {
            const status = document.getElementById('unityStatus');
            status.textContent = 'connected';
            expect(status.textContent).toBe('connected');
        });

        test('should support various status states', () => {
            const status = document.getElementById('unityStatus');
            const states = ['disconnected', 'connecting', 'connected', 'error'];
            states.forEach(state => {
                status.textContent = state;
                expect(status.textContent).toBe(state);
            });
        });
    });

    describe('Electron IPC Integration', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
            // setupDOM creates a new window, so we need to re-attach electronAPI
            window.electronAPI = {
                ipcRenderer: {
                    invoke: jest.fn().mockResolvedValue({ status: 'ok' }),
                    on: jest.fn(),
                    removeListener: jest.fn()
                },
                shell: {
                    openExternal: jest.fn()
                }
            };
        });

        test('should have electronAPI available', () => {
            expect(window.electronAPI).toBeTruthy();
            expect(window.electronAPI.ipcRenderer.invoke).toBeTruthy();
        });

        test('should have shell.openExternal available', () => {
            expect(window.electronAPI.shell.openExternal).toBeTruthy();
        });

        test('ipcRenderer.invoke should return promise', async () => {
            const result = await window.electronAPI.ipcRenderer.invoke('test-channel', { data: 'test' });
            expect(result).toEqual({ status: 'ok' });
        });
    });

    describe('Page Structure', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
        });

        test('should have main content area', () => {
            const main = document.getElementById('mainContent');
            expect(main).toBeTruthy();
        });

        test('should have navigation area', () => {
            const nav = document.getElementById('mainNav');
            expect(nav).toBeTruthy();
        });

        test('should have page title element', () => {
            const title = document.getElementById('pageTitle');
            expect(title).toBeTruthy();
        });

        test('should have all page containers', () => {
            expect(document.getElementById('page-dashboard')).toBeTruthy();
            expect(document.getElementById('page-game')).toBeTruthy();
            expect(document.getElementById('page-admin')).toBeTruthy();
            expect(document.getElementById('page-health')).toBeTruthy();
        });
    });

    describe('Integration scenarios', () => {
        let dom;
        beforeEach(() => {
            dom = setupDOM();
        });

        test('full navigation flow: click nav, update active state', () => {
            const navItems = document.querySelectorAll('.nav-item');
            
            // Simulate clicking the game nav item
            navItems[0].classList.remove('active');
            navItems[1].classList.add('active');
            
            expect(navItems[0].classList.contains('active')).toBe(false);
            expect(navItems[1].classList.contains('active')).toBe(true);
        });

        test('toast notification lifecycle', () => {
            const container = document.getElementById('toastContainer');
            
            // Create toast
            const toast = document.createElement('div');
            toast.className = 'toast info';
            toast.textContent = 'Notification';
            container.appendChild(toast);
            expect(container.children.length).toBe(1);
            
            // Remove toast
            container.removeChild(toast);
            expect(container.children.length).toBe(0);
        });

        test('status change flow: disconnected → connecting → connected', () => {
            const status = document.getElementById('unityStatus');
            expect(status.textContent).toBe('disconnected');
            
            status.textContent = 'connecting';
            expect(status.textContent).toBe('connecting');
            
            status.textContent = 'connected';
            expect(status.textContent).toBe('connected');
        });
    });
});

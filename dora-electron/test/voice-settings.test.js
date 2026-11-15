/**
 * Voice Settings Panel Unit Tests
 *
 * Tests for the dora-electron voice settings panel (voice-settings.js).
 * Uses Jest with DOM and fetch mocking.
 *
 * Run: npx jest test/voice-settings.test.js
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
    voiceService: null,
    voiceSettingsPanel: null,
};
global.window = mockWindow;

// Mock document
const createdElements = {};
global.document = {
    getElementById: jest.fn(id => createdElements[id] || null),
    querySelectorAll: jest.fn(() => []),
    querySelector: jest.fn(() => null),
    createElement: jest.fn(tag => {
        const el = {
            tagName: tag.toUpperCase(),
            value: '',
            textContent: '',
            innerHTML: '',
            className: '',
            disabled: false,
            checked: false,
            addEventListener: jest.fn(),
            appendChild: jest.fn(),
            setAttribute: jest.fn(),
            removeChild: jest.fn(),
            prepend: jest.fn(),
            style: {},
            options: { length: 0 },
        };
        return el;
    }),
};

// ---- Inline the VoiceSettingsPanel class ----
// Simplified version for testing core logic

const DEFAULT_SETTINGS = {
    micSensitivity: 50,
    micDevice: '',
    ttsVoice: '',
    ttsVolume: 80,
    ttsSpeed: 100,
    voiceProfile: '',
    enableCommands: true,
    enableTts: true,
    enableWebSocket: false,
    enableLocalStt: false
};

class VoiceSettingsPanel {
    constructor() {
        this.serverUrl = localStorageMock.getItem('serverUrl') || 'http://localhost:8080';
        this.settings = this.loadSettings();
        this.elements = {};
        this.isInitialized = false;
    }

    loadSettings() {
        try {
            const saved = localStorageMock.getItem('voiceSettings');
            if (saved) {
                return Object.assign({}, DEFAULT_SETTINGS, JSON.parse(saved));
            }
        } catch (e) {
            // ignore
        }
        return Object.assign({}, DEFAULT_SETTINGS);
    }

    saveSettings() {
        try {
            this.collectSettings();
            localStorageMock.setItem('voiceSettings', JSON.stringify(this.settings));
            this.applyToVoiceService();
            return true;
        } catch (e) {
            return false;
        }
    }

    resetSettings() {
        this.settings = Object.assign({}, DEFAULT_SETTINGS);
        localStorageMock.removeItem('voiceSettings');
        this.applySettingsToUI();
        this.applyToVoiceService();
    }

    collectSettings() {
        if (!this.elements.micSensitivity) return;
        this.settings.micSensitivity = parseInt(this.elements.micSensitivity.value) || 50;
        this.settings.micDevice = this.elements.micDevice ? this.elements.micDevice.value : '';
        this.settings.ttsVoice = this.elements.ttsVoice ? this.elements.ttsVoice.value : '';
        this.settings.ttsVolume = parseInt(this.elements.ttsVolume.value) || 80;
        this.settings.ttsSpeed = parseInt(this.elements.ttsSpeed.value) || 100;
        this.settings.voiceProfile = this.elements.voiceProfileSelect ? this.elements.voiceProfileSelect.value : '';
        this.settings.enableCommands = this.elements.enableCommands ? this.elements.enableCommands.checked : true;
        this.settings.enableTts = this.elements.enableTts ? this.elements.enableTts.checked : true;
        this.settings.enableWebSocket = this.elements.enableWebSocket ? this.elements.enableWebSocket.checked : false;
        this.settings.enableLocalStt = this.elements.enableLocalStt ? this.elements.enableLocalStt.checked : false;
    }

    applySettingsToUI() {
        const s = this.settings;
        if (this.elements.micSensitivity) this.elements.micSensitivity.value = s.micSensitivity;
        if (this.elements.micSensitivityValue) this.elements.micSensitivityValue.textContent = s.micSensitivity + '%';
        if (this.elements.micDevice) this.elements.micDevice.value = s.micDevice;
        if (this.elements.ttsVoice) this.elements.ttsVoice.value = s.ttsVoice;
        if (this.elements.ttsVolume) this.elements.ttsVolume.value = s.ttsVolume;
        if (this.elements.ttsVolumeValue) this.elements.ttsVolumeValue.textContent = s.ttsVolume + '%';
        if (this.elements.ttsSpeed) this.elements.ttsSpeed.value = s.ttsSpeed;
        if (this.elements.ttsSpeedValue) this.elements.ttsSpeedValue.textContent = (s.ttsSpeed / 100).toFixed(1) + 'x';
        if (this.elements.voiceProfileSelect) this.elements.voiceProfileSelect.value = s.voiceProfile;
        if (this.elements.enableCommands) this.elements.enableCommands.checked = s.enableCommands;
        if (this.elements.enableTts) this.elements.enableTts.checked = s.enableTts;
        if (this.elements.enableWebSocket) this.elements.enableWebSocket.checked = s.enableWebSocket;
        if (this.elements.enableLocalStt) this.elements.enableLocalStt.checked = s.enableLocalStt;
    }

    applyToVoiceService() {
        const vs = mockWindow.voiceService;
        if (!vs) return;
        if (vs.setVolume) vs.setVolume(this.settings.ttsVolume / 100);
        if (vs.setEnableCommands) vs.setEnableCommands(this.settings.enableCommands);
        if (vs.setEnableTts) vs.setEnableTts(this.settings.enableTts);
        if (vs.setEnableWebSocket) vs.setEnableWebSocket(this.settings.enableWebSocket);
        if (vs.setEnableLocalStt) vs.setEnableLocalStt(this.settings.enableLocalStt);
        vs.settings = this.settings;
    }

    getSettings() {
        return Object.assign({}, this.settings);
    }

    isCommandsEnabled() {
        return this.settings.enableCommands;
    }

    isTtsEnabled() {
        return this.settings.enableTts;
    }

    isWebSocketEnabled() {
        return this.settings.enableWebSocket;
    }

    isLocalSttEnabled() {
        return this.settings.enableLocalStt;
    }
}

// ---- Tests ----

describe('VoiceSettingsPanel', () => {
    beforeEach(() => {
        localStorageMock.clear();
        mockFetch.mockReset();
        mockWindow.voiceService = {
            setVolume: jest.fn(),
            setEnableCommands: jest.fn(),
            setEnableTts: jest.fn(),
            setEnableWebSocket: jest.fn(),
            setEnableLocalStt: jest.fn(),
            settings: {}
        };
    });

    describe('loadSettings', () => {
        test('returns defaults when no saved settings', () => {
            const panel = new VoiceSettingsPanel();
            const settings = panel.getSettings();
            expect(settings.micSensitivity).toBe(50);
            expect(settings.ttsVolume).toBe(80);
            expect(settings.ttsSpeed).toBe(100);
            expect(settings.enableCommands).toBe(true);
            expect(settings.enableTts).toBe(true);
            expect(settings.enableWebSocket).toBe(false);
            expect(settings.enableLocalStt).toBe(false);
        });

        test('loads saved settings from localStorage', () => {
            const saved = JSON.stringify({ micSensitivity: 75, ttsVolume: 90 });
            localStorageMock.setItem('voiceSettings', saved);
            const panel = new VoiceSettingsPanel();
            const settings = panel.getSettings();
            expect(settings.micSensitivity).toBe(75);
            expect(settings.ttsVolume).toBe(90);
            expect(settings.ttsSpeed).toBe(100); // default
        });

        test('handles corrupted localStorage gracefully', () => {
            localStorageMock.setItem('voiceSettings', 'not json');
            const panel = new VoiceSettingsPanel();
            const settings = panel.getSettings();
            expect(settings.micSensitivity).toBe(50); // default
        });
    });

    describe('saveSettings', () => {
        test('saves settings to localStorage', () => {
            const panel = new VoiceSettingsPanel();
            panel.settings.micSensitivity = 80;
            panel.saveSettings();
            expect(localStorageMock.setItem).toHaveBeenCalledWith(
                'voiceSettings',
                expect.stringContaining('"micSensitivity":80')
            );
        });

        test('applies settings to voiceService', () => {
            const panel = new VoiceSettingsPanel();
            panel.settings.enableCommands = false;
            panel.saveSettings();
            expect(mockWindow.voiceService.setEnableCommands).toHaveBeenCalledWith(false);
        });

        test('returns true on success', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.saveSettings()).toBe(true);
        });
    });

    describe('resetSettings', () => {
        test('resets to defaults', () => {
            const panel = new VoiceSettingsPanel();
            panel.settings.micSensitivity = 99;
            panel.settings.enableCommands = false;
            panel.resetSettings();
            expect(panel.settings.micSensitivity).toBe(50);
            expect(panel.settings.enableCommands).toBe(true);
        });

        test('removes saved settings from localStorage', () => {
            localStorageMock.setItem('voiceSettings', '{}');
            const panel = new VoiceSettingsPanel();
            panel.resetSettings();
            expect(localStorageMock.removeItem).toHaveBeenCalledWith('voiceSettings');
        });
    });

    describe('collectSettings', () => {
        test('collects values from UI elements', () => {
            const panel = new VoiceSettingsPanel();
            panel.elements = {
                micSensitivity: { value: '70' },
                micDevice: { value: 'device-1' },
                ttsVoice: { value: 'alloy' },
                ttsVolume: { value: '90' },
                ttsSpeed: { value: '120' },
                voiceProfileSelect: { value: '5' },
                enableCommands: { checked: true },
                enableTts: { checked: false },
                enableWebSocket: { checked: true },
                enableLocalStt: { checked: false },
            };
            panel.collectSettings();
            expect(panel.settings.micSensitivity).toBe(70);
            expect(panel.settings.micDevice).toBe('device-1');
            expect(panel.settings.ttsVoice).toBe('alloy');
            expect(panel.settings.ttsVolume).toBe(90);
            expect(panel.settings.ttsSpeed).toBe(120);
            expect(panel.settings.voiceProfile).toBe('5');
            expect(panel.settings.enableCommands).toBe(true);
            expect(panel.settings.enableTts).toBe(false);
            expect(panel.settings.enableWebSocket).toBe(true);
            expect(panel.settings.enableLocalStt).toBe(false);
        });

        test('handles missing elements gracefully', () => {
            const panel = new VoiceSettingsPanel();
            panel.elements = {};
            panel.collectSettings(); // should not throw
        });
    });

    describe('applySettingsToUI', () => {
        test('applies settings to UI elements', () => {
            const panel = new VoiceSettingsPanel();
            panel.settings.micSensitivity = 60;
            panel.settings.ttsVolume = 85;
            panel.settings.ttsSpeed = 150;
            panel.elements = {
                micSensitivity: { value: 0 },
                micSensitivityValue: { textContent: '' },
                micDevice: { value: '' },
                ttsVoice: { value: '' },
                ttsVolume: { value: 0 },
                ttsVolumeValue: { textContent: '' },
                ttsSpeed: { value: 0 },
                ttsSpeedValue: { textContent: '' },
                voiceProfileSelect: { value: '' },
                enableCommands: { checked: false },
                enableTts: { checked: false },
                enableWebSocket: { checked: false },
                enableLocalStt: { checked: false },
            };
            panel.applySettingsToUI();
            expect(panel.elements.micSensitivity.value).toBe(60);
            expect(panel.elements.micSensitivityValue.textContent).toBe('60%');
            expect(panel.elements.ttsVolume.value).toBe(85);
            expect(panel.elements.ttsVolumeValue.textContent).toBe('85%');
            expect(panel.elements.ttsSpeed.value).toBe(150);
            expect(panel.elements.ttsSpeedValue.textContent).toBe('1.5x');
        });
    });

    describe('applyToVoiceService', () => {
        test('applies volume setting', () => {
            const panel = new VoiceSettingsPanel();
            panel.settings.ttsVolume = 80;
            panel.applyToVoiceService();
            expect(mockWindow.voiceService.setVolume).toHaveBeenCalledWith(0.8);
        });

        test('applies feature flags', () => {
            const panel = new VoiceSettingsPanel();
            panel.settings.enableCommands = false;
            panel.settings.enableTts = false;
            panel.settings.enableWebSocket = true;
            panel.settings.enableLocalStt = true;
            panel.applyToVoiceService();
            expect(mockWindow.voiceService.setEnableCommands).toHaveBeenCalledWith(false);
            expect(mockWindow.voiceService.setEnableTts).toHaveBeenCalledWith(false);
            expect(mockWindow.voiceService.setEnableWebSocket).toHaveBeenCalledWith(true);
            expect(mockWindow.voiceService.setEnableLocalStt).toHaveBeenCalledWith(true);
        });

        test('handles missing voiceService', () => {
            mockWindow.voiceService = null;
            const panel = new VoiceSettingsPanel();
            panel.applyToVoiceService(); // should not throw
        });
    });

    describe('Feature flag getters', () => {
        test('isCommandsEnabled returns correct value', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.isCommandsEnabled()).toBe(true);
            panel.settings.enableCommands = false;
            expect(panel.isCommandsEnabled()).toBe(false);
        });

        test('isTtsEnabled returns correct value', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.isTtsEnabled()).toBe(true);
            panel.settings.enableTts = false;
            expect(panel.isTtsEnabled()).toBe(false);
        });

        test('isWebSocketEnabled returns correct value', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.isWebSocketEnabled()).toBe(false);
            panel.settings.enableWebSocket = true;
            expect(panel.isWebSocketEnabled()).toBe(true);
        });

        test('isLocalSttEnabled returns correct value', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.isLocalSttEnabled()).toBe(false);
            panel.settings.enableLocalStt = true;
            expect(panel.isLocalSttEnabled()).toBe(true);
        });
    });

    describe('Default settings', () => {
        test('micSensitivity defaults to 50', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.micSensitivity).toBe(50);
        });

        test('ttsVolume defaults to 80', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.ttsVolume).toBe(80);
        });

        test('ttsSpeed defaults to 100', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.ttsSpeed).toBe(100);
        });

        test('enableCommands defaults to true', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.enableCommands).toBe(true);
        });

        test('enableTts defaults to true', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.enableTts).toBe(true);
        });

        test('enableWebSocket defaults to false', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.enableWebSocket).toBe(false);
        });

        test('enableLocalStt defaults to false', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.settings.enableLocalStt).toBe(false);
        });
    });

    describe('Server URL', () => {
        test('reads serverUrl from localStorage', () => {
            localStorageMock.setItem('serverUrl', 'http://myserver:9000');
            const panel = new VoiceSettingsPanel();
            expect(panel.serverUrl).toBe('http://myserver:9000');
        });

        test('defaults to localhost:8080', () => {
            const panel = new VoiceSettingsPanel();
            expect(panel.serverUrl).toBe('http://localhost:8080');
        });
    });
});

// Voice Settings Panel — Manages voice configuration UI in the Settings tab
// Browser-compatible singleton (no CommonJS require/module.exports)
(function() {
    'use strict';

    const STORAGE_KEY = 'voiceSettings';

    // Default settings
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
        enableLocalStt: false,
        enableCharacterTts: true
    };

    class VoiceSettingsPanel {
        constructor() {
            this.serverUrl = localStorage.getItem('serverUrl') || 'http://localhost:8080';
            this.settings = this.loadSettings();
            this.elements = {};
            this.isInitialized = false;
        }

        /**
         * Load settings from localStorage or use defaults.
         */
        loadSettings() {
            try {
                const saved = localStorage.getItem(STORAGE_KEY);
                if (saved) {
                    return Object.assign({}, DEFAULT_SETTINGS, JSON.parse(saved));
                }
            } catch (e) {
                console.warn('[VoiceSettings] Failed to load settings:', e.message);
            }
            return Object.assign({}, DEFAULT_SETTINGS);
        }

        /**
         * Save settings to localStorage and apply to voiceService.
         */
        saveSettings() {
            try {
                this.collectSettings();
                localStorage.setItem(STORAGE_KEY, JSON.stringify(this.settings));
                this.applyToVoiceService();
                console.log('[VoiceSettings] Settings saved:', this.settings);
                return true;
            } catch (e) {
                console.error('[VoiceSettings] Failed to save settings:', e.message);
                return false;
            }
        }

        /**
         * Reset settings to defaults.
         */
        resetSettings() {
            this.settings = Object.assign({}, DEFAULT_SETTINGS);
            localStorage.removeItem(STORAGE_KEY);
            this.applySettingsToUI();
            this.applyToVoiceService();
            console.log('[VoiceSettings] Settings reset to defaults');
        }

        /**
         * Collect current UI values into settings object.
         */
        collectSettings() {
            this.settings.micSensitivity = parseInt(this.elements.micSensitivity.value) || 50;
            this.settings.micDevice = this.elements.micDevice.value;
            this.settings.ttsVoice = this.elements.ttsVoice.value;
            this.settings.ttsVolume = parseInt(this.elements.ttsVolume.value) || 80;
            this.settings.ttsSpeed = parseInt(this.elements.ttsSpeed.value) || 100;
            this.settings.voiceProfile = this.elements.voiceProfileSelect.value;
            this.settings.enableCommands = this.elements.enableCommands.checked;
            this.settings.enableTts = this.elements.enableTts.checked;
            this.settings.enableWebSocket = this.elements.enableWebSocket.checked;
            this.settings.enableLocalStt = this.elements.enableLocalStt.checked;
            this.settings.enableCharacterTts = this.elements.enableCharacterTts.checked;
        }

        /**
         * Apply settings object to UI elements.
         */
        applySettingsToUI() {
            const s = this.settings;
            this.elements.micSensitivity.value = s.micSensitivity;
            this.elements.micSensitivityValue.textContent = s.micSensitivity + '%';
            this.elements.micDevice.value = s.micDevice;
            this.elements.ttsVoice.value = s.ttsVoice;
            this.elements.ttsVolume.value = s.ttsVolume;
            this.elements.ttsVolumeValue.textContent = s.ttsVolume + '%';
            this.elements.ttsSpeed.value = s.ttsSpeed;
            this.elements.ttsSpeedValue.textContent = (s.ttsSpeed / 100).toFixed(1) + 'x';
            this.elements.voiceProfileSelect.value = s.voiceProfile;
            this.elements.enableCommands.checked = s.enableCommands;
            this.elements.enableTts.checked = s.enableTts;
            this.elements.enableWebSocket.checked = s.enableWebSocket;
            this.elements.enableLocalStt.checked = s.enableLocalStt;
            this.elements.enableCharacterTts.checked = s.enableCharacterTts;
        }

        /**
         * Apply settings to the voiceService singleton.
         */
        applyToVoiceService() {
            const vs = window.voiceService;
            if (!vs) return;

            // Apply volume setting
            if (vs.setVolume) {
                vs.setVolume(this.settings.ttsVolume / 100);
            }

            // Apply feature flags
            if (vs.setEnableCommands) {
                vs.setEnableCommands(this.settings.enableCommands);
            }
            if (vs.setEnableTts) {
                vs.setEnableTts(this.settings.enableTts);
            }
            if (vs.setEnableWebSocket) {
                vs.setEnableWebSocket(this.settings.enableWebSocket);
            }
            if (vs.setEnableLocalStt) {
                vs.setEnableLocalStt(this.settings.enableLocalStt);
            }

            // Store settings on voiceService for access by other modules
            vs.settings = this.settings;
        }

        /**
         * Cache DOM elements.
         */
        cacheElements() {
            this.elements = {
                micSensitivity: document.getElementById('voiceMicSensitivity'),
                micSensitivityValue: document.getElementById('voiceMicSensitivityValue'),
                micDevice: document.getElementById('voiceMicDevice'),
                micStatus: document.getElementById('voiceMicStatus'),
                ttsVoice: document.getElementById('voiceTtsVoice'),
                ttsVolume: document.getElementById('voiceTtsVolume'),
                ttsVolumeValue: document.getElementById('voiceTtsVolumeValue'),
                ttsSpeed: document.getElementById('voiceTtsSpeed'),
                ttsSpeedValue: document.getElementById('voiceTtsSpeedValue'),
                ttsStatus: document.getElementById('voiceTtsStatus'),
                voiceProfileSelect: document.getElementById('voiceProfileSelect'),
                voiceProfileStatus: document.getElementById('voiceProfileStatus'),
                enableCommands: document.getElementById('voiceEnableCommands'),
                enableTts: document.getElementById('voiceEnableTts'),
                enableWebSocket: document.getElementById('voiceEnableWebSocket'),
                enableLocalStt: document.getElementById('voiceEnableLocalStt'),
                enableCharacterTts: document.getElementById('voiceEnableCharacterTts'),
                testMicBtn: document.getElementById('voiceTestMicBtn'),
                testTtsBtn: document.getElementById('voiceTestTtsBtn'),
                loadProfilesBtn: document.getElementById('voiceLoadProfilesBtn'),
                saveSettingsBtn: document.getElementById('voiceSaveSettingsBtn'),
                resetSettingsBtn: document.getElementById('voiceResetSettingsBtn')
            };
        }

        /**
         * Initialize the voice settings panel.
         */
        init() {
            if (this.isInitialized) return;
            this.cacheElements();
            this.applySettingsToUI();
            this.bindEvents();
            this.applyToVoiceService();
            this.isInitialized = true;
            console.log('[VoiceSettings] Initialized');
        }

        /**
         * Bind event listeners.
         */
        bindEvents() {
            // Range sliders: update display values
            this.elements.micSensitivity.addEventListener('input', () => {
                this.elements.micSensitivityValue.textContent = this.elements.micSensitivity.value + '%';
            });

            this.elements.ttsVolume.addEventListener('input', () => {
                this.elements.ttsVolumeValue.textContent = this.elements.ttsVolume.value + '%';
            });

            this.elements.ttsSpeed.addEventListener('input', () => {
                const speed = parseInt(this.elements.ttsSpeed.value) / 100;
                this.elements.ttsSpeedValue.textContent = speed.toFixed(1) + 'x';
            });

            // Test Microphone
            this.elements.testMicBtn.addEventListener('click', () => this.testMicrophone());

            // Test TTS
            this.elements.testTtsBtn.addEventListener('click', () => this.testTTS());

            // Load Voice Profiles
            this.elements.loadProfilesBtn.addEventListener('click', () => this.loadVoiceProfiles());

            // Load TTS Voices
            this.elements.ttsVoice.addEventListener('change', () => {
                this.settings.ttsVoice = this.elements.ttsVoice.value;
            });

            // Load microphone devices on init
            this.loadMicrophoneDevices();

            // Load TTS voices on init
            this.loadTTSVoices();

            // Save Settings
            this.elements.saveSettingsBtn.addEventListener('click', () => {
                const success = this.saveSettings();
                const status = this.elements.saveSettingsBtn;
                if (success) {
                    status.textContent = '✓ Saved!';
                    setTimeout(() => {
                        status.textContent = '💾 Save Settings';
                    }, 2000);
                }
            });

            // Reset Settings
            this.elements.resetSettingsBtn.addEventListener('click', () => {
                if (confirm('Reset all voice settings to defaults?')) {
                    this.resetSettings();
                }
            });

            // Feature toggles: auto-apply when changed
            ['enableCommands', 'enableTts', 'enableWebSocket', 'enableLocalStt', 'enableCharacterTts'].forEach(key => {
                this.elements[key].addEventListener('change', () => {
                    this.settings[key] = this.elements[key].checked;
                    this.applyToVoiceService();
                });
            });
        }

        /**
         * Load available microphone devices.
         */
        async loadMicrophoneDevices() {
            try {
                // Request permission first
                const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                stream.getTracks().forEach(track => track.stop());

                const devices = await navigator.mediaDevices.enumerateDevices();
                const audioInputs = devices.filter(d => d.kind === 'audioinput');

                const select = this.elements.micDevice;
                select.innerHTML = '<option value="">Default</option>';
                audioInputs.forEach((device, i) => {
                    const option = document.createElement('option');
                    option.value = device.deviceId;
                    option.textContent = device.label || `Microphone ${i + 1}`;
                    select.appendChild(option);
                });

                this.setMicStatus('Found ' + audioInputs.length + ' microphone(s)', 'success');
            } catch (e) {
                console.warn('[VoiceSettings] Failed to list microphones:', e.message);
                this.setMicStatus('Microphone access denied', 'error');
            }
        }

        /**
         * Load available TTS voices from the server.
         */
        async loadTTSVoices() {
            try {
                const vs = window.voiceService;
                if (!vs) return;

                const result = await vs.getVoices();
                if (result.success && result.voices) {
                    const select = this.elements.ttsVoice;
                    select.innerHTML = '<option value="">Default</option>';
                    result.voices.forEach(voice => {
                        const option = document.createElement('option');
                        option.value = voice;
                        option.textContent = voice;
                        select.appendChild(option);
                    });
                    this.setTtsStatus('Loaded ' + result.voices.length + ' voice(s)', 'success');
                }
            } catch (e) {
                console.warn('[VoiceSettings] Failed to load TTS voices:', e.message);
                this.setTtsStatus('Failed to load voices', 'error');
            }
        }

        /**
         * Load voice profiles from the server.
         */
        async loadVoiceProfiles() {
            this.setProfileStatus('Loading profiles...', 'testing');
            try {
                const response = await fetch(this.serverUrl + '/api/ai/voice/profiles');
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                const data = await response.json();
                const profiles = data.profiles || [];
                const select = this.elements.voiceProfileSelect;
                select.innerHTML = '<option value="">Default Profile</option>';
                profiles.forEach(profile => {
                    const option = document.createElement('option');
                    option.value = profile.id;
                    option.textContent = profile.name + (profile.tone ? ` (${profile.tone})` : '');
                    select.appendChild(option);
                });

                this.setProfileStatus('Loaded ' + profiles.length + ' profile(s)', 'success');
            } catch (e) {
                console.error('[VoiceSettings] Failed to load profiles:', e.message);
                this.setProfileStatus('Failed to load profiles: ' + e.message, 'error');
            }
        }

        /**
         * Test the microphone by recording briefly.
         */
        async testMicrophone() {
            this.setMicStatus('Testing microphone...', 'testing');
            this.elements.testMicBtn.disabled = true;

            try {
                const vs = window.voiceService;
                if (!vs) {
                    this.setMicStatus('Voice service not available', 'error');
                    return;
                }

                const constraints = { audio: true };
                if (this.elements.micDevice.value) {
                    constraints.audio = { deviceId: { exact: this.elements.micDevice.value } };
                }

                const result = await vs.startRecordingWithConstraints(constraints);
                if (!result.success) {
                    this.setMicStatus('Failed: ' + result.error, 'error');
                    return;
                }

                // Record for 2 seconds then stop
                await new Promise(resolve => setTimeout(resolve, 2000));
                const stopResult = await vs.stopRecording();

                if (stopResult.success) {
                    this.setMicStatus('✓ Microphone working! (' + stopResult.audioBase64.length + ' bytes)', 'success');
                } else {
                    this.setMicStatus('Recording failed: ' + stopResult.error, 'error');
                }
            } catch (e) {
                this.setMicStatus('Error: ' + e.message, 'error');
            } finally {
                this.elements.testMicBtn.disabled = false;
            }
        }

        /**
         * Test TTS by speaking a sample phrase.
         */
        async testTTS() {
            this.setTtsStatus('Testing TTS...', 'testing');
            this.elements.testTtsBtn.disabled = true;

            try {
                const vs = window.voiceService;
                if (!vs) {
                    this.setTtsStatus('Voice service not available', 'error');
                    return;
                }

                const text = 'Hello! This is a test of the voice system.';
                const voice = this.elements.ttsVoice.value || null;

                const result = await vs.synthesize(text, voice);
                if (!result.success) {
                    this.setTtsStatus('Synthesis failed: ' + result.error, 'error');
                    return;
                }

                if (result.audioBase64) {
                    const playResult = await vs.playAudio(result.audioBase64);
                    if (playResult.success) {
                        this.setTtsStatus('✓ TTS working!', 'success');
                    } else {
                        this.setTtsStatus('Playback failed: ' + playResult.error, 'error');
                    }
                } else {
                    this.setTtsStatus('No audio returned', 'error');
                }
            } catch (e) {
                this.setTtsStatus('Error: ' + e.message, 'error');
            } finally {
                this.elements.testTtsBtn.disabled = false;
            }
        }

        /**
         * Set microphone status text.
         */
        setMicStatus(text, type) {
            const el = this.elements.micStatus;
            el.textContent = text;
            el.className = 'voice-status-text ' + (type || '');
        }

        /**
         * Set TTS status text.
         */
        setTtsStatus(text, type) {
            const el = this.elements.ttsStatus;
            el.textContent = text;
            el.className = 'voice-status-text ' + (type || '');
        }

        /**
         * Set voice profile status text.
         */
        setProfileStatus(text, type) {
            const el = this.elements.voiceProfileStatus;
            el.textContent = text;
            el.className = 'voice-status-text ' + (type || '');
        }

        /**
         * Get current settings.
         */
        getSettings() {
            return Object.assign({}, this.settings);
        }

        /**
         * Check if voice commands are enabled.
         */
        isCommandsEnabled() {
            return this.settings.enableCommands;
        }

        /**
         * Check if TTS playback is enabled.
         */
        isTtsEnabled() {
            return this.settings.enableTts;
        }

        /**
         * Check if WebSocket mode is enabled.
         */
        isWebSocketEnabled() {
            return this.settings.enableWebSocket;
        }

        /**
         * Check if local STT is enabled.
         */
        isLocalSttEnabled() {
            return this.settings.enableLocalStt;
        }

        /**
         * Check if character TTS auto-speak is enabled.
         */
        isCharacterTtsEnabled() {
            return this.settings.enableCharacterTts !== false;
        }
    }

    // Expose as global singleton
    window.voiceSettingsPanel = new VoiceSettingsPanel();
})();

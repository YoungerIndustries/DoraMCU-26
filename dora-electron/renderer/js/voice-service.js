// Voice Service — Microphone capture, audio playback, and voice API integration
// Browser-compatible singleton (no CommonJS require/module.exports)
(function() {
    'use strict';

    class VoiceService {
        constructor() {
            this.serverUrl = localStorage.getItem('serverUrl') || 'http://localhost:8080';
            this.mediaRecorder = null;
            this.audioChunks = [];
            this.isRecording = false;
            this.stream = null;
            this.audioContext = null;
            this.voices = [];

            // WebSocket connection for voice commands
            this.ws = null;
            this.wsConnected = false;
            this.wsSessionId = null;
            this.wsPlayerId = null;

            // Web Speech API for client-side STT
            this.SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            this.recognition = null;
            this.isSpeechRecognizing = false;

            // Settings (managed by voice-settings.js)
            this.settings = {};
            this.volume = 1.0;
            this.enableCommands = true;
            this.enableTts = true;
            this.enableWebSocket = false;
            this.enableLocalStt = false;
        }

        /**
         * Set the TTS playback volume (0.0 - 1.0).
         */
        setVolume(vol) {
            this.volume = Math.max(0, Math.min(1, vol));
        }

        /**
         * Enable or disable voice commands.
         */
        setEnableCommands(enabled) {
            this.enableCommands = !!enabled;
        }

        /**
         * Enable or disable auto TTS playback.
         */
        setEnableTts(enabled) {
            this.enableTts = !!enabled;
        }

        /**
         * Enable or disable WebSocket mode.
         */
        setEnableWebSocket(enabled) {
            this.enableWebSocket = !!enabled;
        }

        /**
         * Enable or disable local STT.
         */
        setEnableLocalStt(enabled) {
            this.enableLocalStt = !!enabled;
        }

        /**
         * Start recording with custom constraints.
         * @param {object} constraints - MediaStream constraints
         * @returns {Promise<{success: boolean, error?: string}>}
         */
        async startRecordingWithConstraints(constraints) {
            try {
                this.stream = await navigator.mediaDevices.getUserMedia(constraints);
                this.audioChunks = [];

                const options = { mimeType: 'audio/webm' };
                if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                    options.mimeType = 'audio/ogg';
                }

                this.mediaRecorder = new MediaRecorder(this.stream, options);

                this.mediaRecorder.ondataavailable = (event) => {
                    if (event.data.size > 0) {
                        this.audioChunks.push(event.data);
                    }
                };

                this.mediaRecorder.start(100);
                this.isRecording = true;

                console.log('Voice recording started with constraints');
                return { success: true };
            } catch (error) {
                console.error('Failed to start recording with constraints:', error.message);
                return { success: false, error: error.message };
            }
        }

        setServerUrl(url) {
            this.serverUrl = url;
            localStorage.setItem('serverUrl', url);
        }

        /**
         * Initialize the audio context for playback.
         */
        initAudioContext() {
            if (!this.audioContext) {
                this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
            }
            return this.audioContext;
        }

        /**
         * Request microphone access and start recording.
         * @returns {Promise<{success: boolean, error?: string}>}
         */
        async startRecording() {
            try {
                this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                this.audioChunks = [];

                const options = { mimeType: 'audio/webm' };
                if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                    options.mimeType = 'audio/ogg';
                }

                this.mediaRecorder = new MediaRecorder(this.stream, options);

                this.mediaRecorder.ondataavailable = (event) => {
                    if (event.data.size > 0) {
                        this.audioChunks.push(event.data);
                    }
                };

                this.mediaRecorder.start(100); // Collect data every 100ms
                this.isRecording = true;

                console.log('Voice recording started');
                return { success: true };
            } catch (error) {
                console.error('Failed to start recording:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Stop recording and return the audio as base64.
         * @returns {Promise<{success: boolean, audioBase64?: string, error?: string}>}
         */
        async stopRecording() {
            return new Promise((resolve) => {
                if (!this.mediaRecorder || !this.isRecording) {
                    resolve({ success: false, error: 'Not recording' });
                    return;
                }

                this.mediaRecorder.onstop = () => {
                    this.isRecording = false;

                    // Stop the microphone stream
                    if (this.stream) {
                        this.stream.getTracks().forEach(track => track.stop());
                        this.stream = null;
                    }

                    if (this.audioChunks.length === 0) {
                        resolve({ success: false, error: 'No audio data captured' });
                        return;
                    }

                    const blob = new Blob(this.audioChunks, { type: this.mediaRecorder.mimeType });
                    this.audioChunks = [];

                    const reader = new FileReader();
                    reader.onload = () => {
                        // Extract base64 from data URL
                        const base64 = reader.result.split(',')[1];
                        console.log('Voice recording stopped, audio size:', base64.length, 'bytes');
                        resolve({ success: true, audioBase64: base64 });
                    };
                    reader.onerror = () => {
                        resolve({ success: false, error: 'Failed to read audio data' });
                    };
                    reader.readAsDataURL(blob);
                };

                this.mediaRecorder.stop();
            });
        }

        /**
         * Check if currently recording.
         */
        isCurrentlyRecording() {
            return this.isRecording;
        }

        /**
         * Transcribe audio to text using the server's STT endpoint.
         * @param {string} audioBase64 - Base64-encoded audio data
         * @param {string} languageHint - Optional language hint
         * @returns {Promise<{success: boolean, text?: string, language?: string, error?: string}>}
         */
        async transcribe(audioBase64, languageHint) {
            try {
                const body = {
                    audioBase64: audioBase64,
                    languageHint: languageHint || null
                };

                const response = await fetch(this.serverUrl + '/api/ai/voice/transcribe', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.error || `HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('Transcription result:', result.text);
                return { success: true, text: result.text, language: result.language };
            } catch (error) {
                console.error('Transcription failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Synthesize text to speech using the server's TTS endpoint.
         * @param {string} text - Text to speak
         * @param {string} voice - Optional voice name
         * @returns {Promise<{success: boolean, audioBase64?: string, error?: string}>}
         */
        async synthesize(text, voice) {
            try {
                const body = {
                    text: text,
                    voice: voice || null
                };

                const response = await fetch(this.serverUrl + '/api/ai/voice/synthesize', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.error || `HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('TTS synthesis result, audio size:', result.audioBase64 ? result.audioBase64.length : 0);
                return { success: true, audioBase64: result.audioBase64, format: result.format };
            } catch (error) {
                console.error('TTS synthesis failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Play audio from base64-encoded data.
         * @param {string} audioBase64 - Base64-encoded audio data
         * @returns {Promise<{success: boolean, error?: string}>}
         */
        async playAudio(audioBase64) {
            try {
                if (!audioBase64 || audioBase64.length < 10) {
                    return { success: false, error: 'No audio data to play' };
                }

                const context = this.initAudioContext();

                // Convert base64 to ArrayBuffer
                const binaryString = atob(audioBase64);
                const bytes = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }

                // Decode and play
                const audioBuffer = await context.decodeAudioData(bytes.buffer);
                const source = context.createBufferSource();
                source.buffer = audioBuffer;
                source.connect(context.destination);
                source.start(0);

                console.log('Audio playback started');
                return { success: true };
            } catch (error) {
                console.error('Audio playback failed:', error.message);
                // Fallback: try using Audio element for WAV data
                try {
                    const dataUrl = 'data:audio/wav;base64,' + audioBase64;
                    const audio = new Audio(dataUrl);
                    await audio.play();
                    console.log('Audio playback started (fallback)');
                    return { success: true };
                } catch (fallbackError) {
                    console.error('Fallback audio playback also failed:', fallbackError.message);
                    return { success: false, error: error.message };
                }
            }
        }

        /**
         * Send a voice command to the server (STT + interpretation).
         * @param {string} audioBase64 - Base64-encoded audio data
         * @param {string} sessionId - Optional session ID
         * @param {string} playerId - Optional player ID
         * @returns {Promise<{success: boolean, commandType?: string, parameters?: object, error?: string}>}
         */
        async sendVoiceCommand(audioBase64, sessionId, playerId) {
            try {
                const body = {
                    audioBase64: audioBase64,
                    sessionId: sessionId || null,
                    playerId: playerId || null
                };

                const response = await fetch(this.serverUrl + '/api/ai/voice/command', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.error || `HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('Voice command result:', result.commandType, result.parameters);
                return {
                    success: true,
                    commandType: result.commandType,
                    parameters: result.parameters,
                    executed: result.executed,
                    message: result.message,
                    transcribedText: result.transcribedText
                };
            } catch (error) {
                console.error('Voice command failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Voice chat: record, transcribe, get AI response, and play audio.
         * @param {string} sessionId - Optional session ID
         * @returns {Promise<{success: boolean, transcribedText?: string, aiResponseText?: string, error?: string}>}
         */
        async voiceChat(sessionId) {
            try {
                // Step 1: Start recording
                const startResult = await this.startRecording();
                if (!startResult.success) {
                    return startResult;
                }

                // Signal to caller that recording has started
                if (this.onRecordingStart) {
                    this.onRecordingStart();
                }

                // Auto-stop after 5 seconds of silence or max 10 seconds
                const maxDuration = 10000;
                const startTime = Date.now();

                // Wait for manual stop or timeout
                while (this.isRecording && (Date.now() - startTime) < maxDuration) {
                    await new Promise(resolve => setTimeout(resolve, 100));
                }

                // Step 2: Stop recording
                const stopResult = await this.stopRecording();
                if (!stopResult.success) {
                    return stopResult;
                }

                // Step 3: Send to voice chat endpoint
                const body = {
                    audioBase64: stopResult.audioBase64,
                    sessionId: sessionId || null
                };

                const response = await fetch(this.serverUrl + '/api/ai/voice/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.error || `HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('Voice chat result:', result);

                // Step 4: Play AI response audio if available
                if (result.audioBase64) {
                    await this.playAudio(result.audioBase64);
                }

                return {
                    success: true,
                    transcribedText: result.transcribedText,
                    aiResponseText: result.aiResponseText
                };
            } catch (error) {
                console.error('Voice chat failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Get the list of available TTS voices.
         * @returns {Promise<{success: boolean, voices?: string[], error?: string}>}
         */
        async getVoices() {
            try {
                const response = await fetch(this.serverUrl + '/api/ai/voice/voices', {
                    method: 'GET'
                });

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                const result = await response.json();
                this.voices = result.voices || [];
                console.log('Available voices:', this.voices);
                return { success: true, voices: this.voices };
            } catch (error) {
                console.error('Failed to get voices:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Full voice command flow: record → send → get result.
         * @param {string} sessionId - Optional session ID
         * @param {string} playerId - Optional player ID
         * @returns {Promise<{success: boolean, commandType?: string, error?: string}>}
         */
        async recordAndCommand(sessionId, playerId) {
            try {
                // Step 1: Record
                const startResult = await this.startRecording();
                if (!startResult.success) {
                    return startResult;
                }

                if (this.onRecordingStart) {
                    this.onRecordingStart();
                }

                // Wait for manual stop
                while (this.isRecording) {
                    await new Promise(resolve => setTimeout(resolve, 100));
                }

                const stopResult = await this.stopRecording();
                if (!stopResult.success) {
                    return stopResult;
                }

                // Step 2: Send command
                return await this.sendVoiceCommand(stopResult.audioBase64, sessionId, playerId);
            } catch (error) {
                console.error('Record and command failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        // ── Client-side STT (Web Speech API) ──

        /**
         * Check if Web Speech API is available.
         */
        isSpeechRecognitionAvailable() {
            return !!this.SpeechRecognition;
        }

        /**
         * Transcribe audio locally using the Web Speech API.
         * This is faster than server-side STT but requires browser support.
         * @param {string} language - Optional language code (e.g., 'en-US')
         * @returns {Promise<{success: boolean, text?: string, error?: string}>}
         */
        transcribeLocal(language) {
            return new Promise((resolve) => {
                if (!this.SpeechRecognition) {
                    resolve({ success: false, error: 'Web Speech API not available' });
                    return;
                }

                try {
                    const recognition = new this.SpeechRecognition();
                    recognition.continuous = false;
                    recognition.interimResults = false;
                    recognition.lang = language || 'en-US';

                    let finalTranscript = '';

                    recognition.onresult = (event) => {
                        const result = event.results[0];
                        if (result.isFinal) {
                            finalTranscript = result[0].transcript;
                        }
                    };

                    recognition.onend = () => {
                        if (finalTranscript) {
                            console.log('Local transcription:', finalTranscript);
                            resolve({ success: true, text: finalTranscript });
                        } else {
                            resolve({ success: false, error: 'No speech detected' });
                        }
                    };

                    recognition.onerror = (event) => {
                        console.error('Speech recognition error:', event.error);
                        resolve({ success: false, error: event.error });
                    };

                    recognition.start();
                    this.isSpeechRecognizing = true;

                    // Auto-stop after 5 seconds to prevent hanging
                    setTimeout(() => {
                        if (this.isSpeechRecognizing) {
                            recognition.stop();
                            this.isSpeechRecognizing = false;
                        }
                    }, 5000);

                } catch (error) {
                    console.error('Failed to start speech recognition:', error.message);
                    resolve({ success: false, error: error.message });
                }
            });
        }

        // ── WebSocket Voice Commands ──

        /**
         * Connect to the FPS WebSocket for real-time voice commands.
         * @param {string} sessionId - Game session ID
         * @param {string} playerId - Player ID
         * @param {string} playerName - Player name
         * @param {number} team - Team number
         * @returns {Promise<{success: boolean, error?: string}>}
         */
        async connectWebSocket(sessionId, playerId, playerName, team) {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                return { success: true };
            }

            try {
                const wsUrl = this.serverUrl.replace('http://', 'ws://').replace('https://', 'wss://');
                const url = `${wsUrl}/ws/fps?sessionId=${encodeURIComponent(sessionId)}&playerId=${encodeURIComponent(playerId)}&playerName=${encodeURIComponent(playerName)}&team=${team || 0}`;

                return new Promise((resolve) => {
                    this.ws = new WebSocket(url);

                    this.ws.onopen = () => {
                        console.log('[VoiceService] WebSocket connected');
                        this.wsConnected = true;
                        this.wsSessionId = sessionId;
                        this.wsPlayerId = playerId;
                        if (this.onWebSocketConnected) {
                            this.onWebSocketConnected();
                        }
                        resolve({ success: true });
                    };

                    this.ws.onmessage = (event) => {
                        this._handleWebSocketMessage(JSON.parse(event.data));
                    };

                    this.ws.onclose = (event) => {
                        console.log(`[VoiceService] WebSocket disconnected: ${event.reason}`);
                        this.wsConnected = false;
                        if (this.onWebSocketDisconnected) {
                            this.onWebSocketDisconnected();
                        }
                    };

                    this.ws.onerror = (error) => {
                        console.error('[VoiceService] WebSocket error');
                        resolve({ success: false, error: 'WebSocket connection failed' });
                    };

                    // Timeout after 5 seconds
                    setTimeout(() => {
                        if (this.ws && this.ws.readyState !== WebSocket.OPEN) {
                            resolve({ success: false, error: 'WebSocket connection timeout' });
                        }
                    }, 5000);
                });
            } catch (error) {
                console.error('WebSocket connection failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Disconnect from the FPS WebSocket.
         */
        disconnectWebSocket() {
            if (this.ws) {
                this.ws.close();
                this.ws = null;
                this.wsConnected = false;
            }
        }

        /**
         * Send a voice command via WebSocket (uses client-side STT first).
         * Falls back to REST if WebSocket is not connected.
         * @param {string} sessionId - Optional session ID
         * @param {string} playerId - Optional player ID
         * @returns {Promise<{success: boolean, commandType?: string, parameters?: object, error?: string}>}
         */
        async sendVoiceCommandViaWebSocket(sessionId, playerId) {
            // If WebSocket is connected, use client-side STT + WebSocket
            if (this.wsConnected && this.ws && this.ws.readyState === WebSocket.OPEN) {
                return await this._voiceCommandViaWebSocket();
            }

            // Fallback: use REST endpoint with microphone recording
            console.log('[VoiceService] WebSocket not connected, falling back to REST');
            return null; // Signal to use REST fallback
        }

        /**
         * Internal: Send voice command via WebSocket using client-side STT.
         */
        async _voiceCommandViaWebSocket() {
            return new Promise((resolve) => {
                // Use Web Speech API for local transcription
                if (!this.SpeechRecognition) {
                    resolve({ success: false, error: 'Web Speech API not available' });
                    return;
                }

                try {
                    const recognition = new this.SpeechRecognition();
                    recognition.continuous = false;
                    recognition.interimResults = false;
                    recognition.lang = 'en-US';

                    let finalTranscript = '';

                    recognition.onresult = (event) => {
                        const result = event.results[0];
                        if (result.isFinal) {
                            finalTranscript = result[0].transcript;
                        }
                    };

                    recognition.onend = () => {
                        if (!finalTranscript) {
                            resolve({ success: false, error: 'No speech detected' });
                            return;
                        }

                        console.log('[VoiceService] Sending voice command via WebSocket:', finalTranscript);

                        // Send the transcribed text over WebSocket
                        this.ws.send(JSON.stringify({
                            type: 'VOICE_COMMAND',
                            sessionId: this.wsSessionId,
                            playerId: this.wsPlayerId,
                            text: finalTranscript
                        }));

                        // Wait for the response
                        const handler = (event) => {
                            const data = JSON.parse(event.data);
                            if (data.type === 'VOICE_COMMAND_RESULT') {
                                // Remove the listener
                                this.ws.onmessage = (e) => this._handleWebSocketMessage(JSON.parse(e.data));
                                resolve({
                                    success: true,
                                    commandType: data.commandType,
                                    parameters: data.parameters || {},
                                    executed: data.executed,
                                    message: data.message,
                                    transcribedText: data.transcribedText || finalTranscript
                                });
                            }
                        };
                        this.ws.onmessage = handler;

                        // Timeout after 5 seconds
                        setTimeout(() => {
                            this.ws.onmessage = (e) => this._handleWebSocketMessage(JSON.parse(e.data));
                            resolve({ success: false, error: 'Voice command timeout' });
                        }, 5000);
                    };

                    recognition.onerror = (event) => {
                        console.error('[VoiceService] Speech recognition error:', event.error);
                        resolve({ success: false, error: event.error });
                    };

                    recognition.start();

                    // Auto-stop after 5 seconds
                    setTimeout(() => {
                        recognition.stop();
                    }, 5000);

                } catch (error) {
                    console.error('[VoiceService] Failed to start speech recognition:', error.message);
                    resolve({ success: false, error: error.message });
                }
            });
        }

        /**
         * Handle incoming WebSocket messages.
         */
        _handleWebSocketMessage(data) {
            switch (data.type) {
                case 'CONNECT_ACK':
                    this.wsSessionId = data.sessionId;
                    this.wsPlayerId = data.playerId;
                    console.log('[VoiceService] Connected to session:', data.sessionId);
                    break;
                case 'VOICE_COMMAND_RESULT':
                    if (this.onVoiceCommandResult) {
                        this.onVoiceCommandResult(data);
                    }
                    break;
                case 'EVENT':
                    if (this.onGameEvent) {
                        this.onGameEvent(data);
                    }
                    break;
                case 'ERROR':
                    console.error('[VoiceService] Server error:', data.message);
                    if (this.onError) {
                        this.onError(data.message);
                    }
                    break;
            }
        }

        // ---- Character Dialogue Methods ----

        /**
         * Chat with an AI character via SSE. Streams text and plays audio response.
         * @param {number} characterId - The character ID to chat with
         * @param {string} message - The user message
         * @param {string} [sessionId] - Optional session ID
         * @param {Function} [onText] - Callback for the full AI response text
         * @param {Function} [onAudio] - Callback when audio is ready (receives base64 audio)
         * @param {Function} [onError] - Callback for errors
         * @returns {Promise<{success: boolean, text?: string, error?: string}>}
         */
        async chatWithCharacter(characterId, message, sessionId, onText, onAudio, onError) {
            try {
                const body = {
                    characterId: characterId,
                    sessionId: sessionId || null,
                    message: message
                };

                const response = await fetch(this.serverUrl + '/api/ai/voice/character/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                // Handle SSE response
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = '';
                let fullText = '';
                let audioBase64 = null;
                let characterName = null;

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });

                    // Parse SSE events from buffer
                    const lines = buffer.split('\n');
                    buffer = lines.pop() || '';

                    let eventName = 'message';
                    for (const line of lines) {
                        if (line.startsWith('event:')) {
                            eventName = line.substring(6).trim();
                        } else if (line.startsWith('data:')) {
                            const data = line.substring(5).trim();

                            switch (eventName) {
                                case 'character':
                                    characterName = data;
                                    console.log('[VoiceService] Character:', characterName);
                                    break;
                                case 'message':
                                    fullText = data;
                                    if (onText) {
                                        onText(data, characterName);
                                    }
                                    break;
                                case 'audio':
                                    audioBase64 = data;
                                    if (onAudio) {
                                        onAudio(data, characterName);
                                    }
                                    // Auto-play if enabled
                                    const settings = this.settings || {};
                                    if (settings.enableCharacterTts !== false && settings.enableTts !== false) {
                                        this.playCharacterAudio(data, characterName);
                                    }
                                    break;
                                case 'error':
                                    console.error('[VoiceService] Character chat error:', data);
                                    if (onError) {
                                        onError(data);
                                    }
                                    break;
                                case 'audio_error':
                                    console.warn('[VoiceService] Audio synthesis error:', data);
                                    break;
                            }
                        }
                    }
                }

                return {
                    success: true,
                    text: fullText,
                    characterName: characterName
                };
            } catch (error) {
                console.error('[VoiceService] Character chat failed:', error.message);
                if (onError) {
                    onError(error.message);
                }
                return { success: false, error: error.message };
            }
        }

        /**
         * Speak text as a specific character using their voice profile.
         * @param {number} characterId - The character ID
         * @param {string} text - The text to speak
         * @returns {Promise<{success: boolean, audioBase64?: string, error?: string}>}
         */
        async speakAsCharacter(characterId, text) {
            try {
                const response = await fetch(this.serverUrl + '/api/ai/voice/character/speak', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ characterId, text })
                });

                if (!response.ok) {
                    if (response.status === 404) {
                        return { success: false, error: 'Character not found' };
                    }
                    const error = await response.json();
                    throw new Error(error.error || `HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('[VoiceService] Character speak result:', result);

                // Auto-play the audio
                if (result.audioBase64) {
                    await this.playAudio(result.audioBase64);
                }

                return { success: true, audioBase64: result.audioBase64 };
            } catch (error) {
                console.error('[VoiceService] Speak as character failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Speak text using a specific voice profile.
         * @param {number} profileId - The voice profile ID
         * @param {string} text - The text to speak
         * @returns {Promise<{success: boolean, audioBase64?: string, error?: string}>}
         */
        async speakWithProfile(profileId, text) {
            try {
                const response = await fetch(this.serverUrl + '/api/ai/voice/profiles/speak', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ profileId, text })
                });

                if (!response.ok) {
                    if (response.status === 404) {
                        return { success: false, error: 'Profile not found' };
                    }
                    const error = await response.json();
                    throw new Error(error.error || `HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('[VoiceService] Profile speak result:', result);

                // Auto-play the audio
                if (result.audioBase64) {
                    await this.playAudio(result.audioBase64);
                }

                return { success: true, audioBase64: result.audioBase64 };
            } catch (error) {
                console.error('[VoiceService] Speak with profile failed:', error.message);
                return { success: false, error: error.message };
            }
        }

        /**
         * Play audio with character voice indicators.
         * Updates the HUD voice status to show which character is speaking.
         * @param {string} audioBase64 - Base64-encoded audio data
         * @param {string} [characterName] - Name of the character speaking
         */
        async playCharacterAudio(audioBase64, characterName) {
            if (!audioBase64) return;

            // Update voice status if HUD controller is available
            if (window.hudController && window.hudController.updateVoiceStatus) {
                const status = characterName ? `Speaking: ${characterName}` : 'Speaking...';
                window.hudController.updateVoiceStatus(status, false);
            }

            try {
                await this.playAudio(audioBase64);
            } finally {
                // Reset voice status when done
                if (window.hudController && window.hudController.updateVoiceStatus) {
                    window.hudController.updateVoiceStatus('', false);
                }
            }
        }

        /**
         * Get the list of characters with their voice profiles.
         * @returns {Promise<{success: boolean, characters?: Array, error?: string}>}
         */
        async getCharactersWithVoices() {
            try {
                const response = await fetch(this.serverUrl + '/api/ai/voice/characters', {
                    method: 'GET'
                });

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                const result = await response.json();
                console.log('[VoiceService] Characters with voices:', result.characters);
                return { success: true, characters: result.characters || [] };
            } catch (error) {
                console.error('[VoiceService] Failed to get characters:', error.message);
                return { success: false, error: error.message };
            }
        }
    }

    // Expose as global singleton for browser scripts
    window.voiceService = new VoiceService();
})();

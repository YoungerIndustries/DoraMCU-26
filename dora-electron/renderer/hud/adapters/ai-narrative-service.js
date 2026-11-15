/* AI Narrative Service — Shared service for AI chat calls with SSE streaming */

class AiNarrativeService {
    constructor(apiBase) {
        // apiBase is the server root, e.g. "http://localhost:8080"
        this.apiBase = apiBase;
        this.chatEndpoint = apiBase + '/api/ai/chat';
        this.streaming = false;
        this.currentController = null;
    }

    // ── System Prompts per Game Theme ──

    getSystemPrompt(theme) {
        const prompts = {
            'captains-log': `You are the narrator for a Star Trek solo RPG adventure. Describe scenes, consequences, and developments in the style of Star Trek mission reports. Keep responses concise and immersive. Use Star Trek terminology and tone.`,
            'dnd-5e': `You are a Dungeon Master for a D&D 5th Edition campaign. Describe scenes, combat, and consequences in an engaging, dramatic style. Keep responses concise. Use fantasy terminology and evoke vivid imagery.`,
            'traveller': `You are a referee for a Classic Traveller campaign. Describe career events, combat, and adventures in a gritty, realistic style. Keep responses concise. Use Traveller terminology and maintain a hard sci-fi tone.`,
            'mech-warrior': `You are a battle reporter for a BattleTech-style mech combat simulation. Describe combat actions, damage, and battlefield events with tactical detail. Keep responses concise. Use mech warfare terminology.`,
            'earth-2026': `You are a narrator for a modern-day survival RPG. Describe survival situations, encounters, and discoveries with tension and realism. Keep responses concise. Use modern urban survival terminology.`,
            'babylon5': `You are a chronicler of the Babylon5 universe. Describe events, dialogues, and story developments with dramatic flair. Keep responses concise. Use Babylon5 terminology and maintain the show's tone.`,
            'star-trek-4x': `You are a strategic advisor for a Star Trek empire. Describe exploration discoveries, diplomatic events, and battles with strategic insight. Keep responses concise. Use Star Trek terminology and 4X strategy framing.`
        };
        return prompts[theme] || prompts['captains-log'];
    }

    // ── Core Chat Methods ──

    async generateNarrative(sessionId, message, systemPrompt) {
        const payload = {
            sessionId: sessionId,
            message: systemPrompt + '\n\n' + message,
            mode: 'chat'
        };

        const response = await fetch(this.chatEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`AI chat failed: ${response.status} ${response.statusText}`);
        }

        return response;
    }

    // ── SSE Streaming to Panel ──

    async streamToPanel(sessionId, message, systemPrompt, onChunk, onComplete, onError) {
        if (this.streaming) {
            this._abortStreaming();
        }

        this.streaming = true;
        this.currentController = new AbortController();

        try {
            const payload = {
                sessionId: sessionId,
                message: systemPrompt + '\n\n' + message,
                mode: 'chat'
            };

            const response = await fetch(this.chatEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'text/event-stream'
                },
                body: JSON.stringify(payload),
                signal: this.currentController.signal
            });

            if (!response.ok) {
                throw new Error(`AI chat failed: ${response.status} ${response.statusText}`);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            let fullText = '';

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        const data = line.slice(6);
                        try {
                            const parsed = JSON.parse(data);
                            if (parsed.status === 'streaming' && parsed.data) {
                                fullText += parsed.data;
                                if (onChunk) onChunk(parsed.data, fullText);
                            } else if (parsed.status === 'done') {
                                if (parsed.data) {
                                    fullText += parsed.data;
                                    if (onChunk) onChunk(parsed.data, fullText);
                                }
                                if (onComplete) onComplete(fullText);
                                return;
                            }
                        } catch (e) {
                            // Non-JSON data, treat as raw text chunk
                            if (data && data !== '{}') {
                                fullText += data;
                                if (onChunk) onChunk(data, fullText);
                            }
                        }
                    } else if (line.startsWith('event:')) {
                        // Event type marker, skip
                    }
                }
            }

            // If stream ended without 'done' event
            if (onComplete) onComplete(fullText);
        } catch (err) {
            if (err.name === 'AbortError') {
                // User aborted
                if (onComplete) onComplete('');
            } else {
                if (onError) onError(err.message);
            }
        } finally {
            this.streaming = false;
            this.currentController = null;
        }
    }

    // ── Abort Streaming ──

    _abortStreaming() {
        if (this.currentController) {
            this.currentController.abort();
        }
    }

    abortStreaming() {
        this._abortStreaming();
    }

    // ── Conversation Management ──

    async clearConversation(sessionId) {
        try {
            await fetch(this.chatEndpoint + '/' + encodeURIComponent(sessionId), {
                method: 'DELETE'
            });
        } catch (err) {
            console.warn('[AiNarrativeService] Failed to clear conversation:', err.message);
        }
    }

    // ── Simple Non-Streaming Chat (fallback) ──

    async chat(sessionId, message, systemPrompt) {
        const payload = {
            sessionId: sessionId,
            message: systemPrompt + '\n\n' + message,
            mode: 'chat'
        };

        const response = await fetch(this.chatEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`AI chat failed: ${response.status} ${response.statusText}`);
        }

        // Read full SSE response and return complete text
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let fullText = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';

            for (const line of lines) {
                if (line.startsWith('data: ')) {
                    const data = line.slice(6);
                    try {
                        const parsed = JSON.parse(data);
                        if (parsed.status === 'streaming' && parsed.data) {
                            fullText += parsed.data;
                        } else if (parsed.status === 'done' && parsed.data) {
                            fullText += parsed.data;
                        }
                    } catch (e) {
                        if (data && data !== '{}') {
                            fullText += data;
                        }
                    }
                }
            }
        }

        return fullText;
    }
}

// Expose for game-launcher.js initialization
window.AiNarrativeService = AiNarrativeService;

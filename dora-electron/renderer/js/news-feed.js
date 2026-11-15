/**
 * Live news feed module for the DoraMCU News page.
 *
 * Connects to the SSE endpoint at /api/v1/news/live and displays
 * real-time DoraMCU universe events in the news feed section.
 */

(function() {
    'use strict';

    let eventSource = null;
    let reconnectTimeout = null;
    const MAX_EVENTS = 50;

    // API base URL
    function getApiUrl() {
        return (localStorage.getItem('serverUrl') || 'http://localhost:8080');
    }

    // Get event icon based on type
    function getEventIcon(type) {
        const icons = {
            'BUILD_STARTED': '🔨',
            'BUILD_COMPLETED': '✅',
            'BUILD_FAILED': '❌',
            'SECTOR_GENERATED': '🗺️',
            'GALAXY_CREATED': '🌌',
            'HEALTH_THRESHOLD_CROSSED': '⚠️',
            'CHARACTER_TRANSITIONED': '🚀',
            'WORK_ORDER_CREATED': '📋',
            'AI_AGENT_DEPLOYED': '🤖',
            'ASSESSMENT_COMPLETED': '📊',
            'WORMHOLE_CREATED': '🌀',
            'WORMHOLE_TRAVEL': '🛸',
            'WORMHOLE_TOGGLED': '🔀'
        };
        return icons[type] || '📰';
    }

    // Format timestamp
    function formatTime(timestamp) {
        try {
            const date = new Date(timestamp);
            return date.toLocaleTimeString();
        } catch (e) {
            return '';
        }
    }

    // Add event to the feed
    function addEventToFeed(event) {
        const feed = document.getElementById('newsFeed');
        if (!feed) return;

        // Remove "connecting" message
        const emptyState = feed.querySelector('.empty-state');
        if (emptyState) {
            emptyState.remove();
        }

        const item = document.createElement('div');
        item.className = 'news-event';

        const icon = getEventIcon(event.type || event.eventType);
        const title = event.title || event.data?.title || 'Unknown Event';
        const galaxy = event.galaxyName || event.data?.galaxyName || '';
        const time = event.timestamp ? formatTime(event.timestamp) : '';

        item.innerHTML =
            '<span class="event-icon">' + icon + '</span>' +
            '<div class="event-content">' +
                '<div class="event-title">' + title + '</div>' +
                (galaxy ? '<div class="event-galaxy">📍 ' + galaxy + '</div>' : '') +
                (time ? '<div class="event-time">' + time + '</div>' : '') +
            '</div>';

        // Prepend to feed (newest first)
        feed.insertBefore(item, feed.firstChild);

        // Limit feed size
        while (feed.querySelectorAll('.news-event').length > MAX_EVENTS) {
            const last = feed.querySelector('.news-event:last-child');
            if (last) last.remove();
        }
    }

    // Connect to SSE stream
    function connect() {
        // Close existing connection
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }

        const statusEl = document.getElementById('newsStatus');
        if (statusEl) {
            statusEl.textContent = 'Connecting...';
            statusEl.className = 'news-status connecting';
        }

        try {
            eventSource = new EventSource(getApiUrl() + '/api/v1/news/live');

            eventSource.onopen = function() {
                if (statusEl) {
                    statusEl.textContent = '● Live';
                    statusEl.className = 'news-status connected';
                }
            };

            // Handle "connected" event (welcome message)
            eventSource.addEventListener('connected', function(e) {
                const data = JSON.parse(e.data);
                if (statusEl) {
                    statusEl.textContent = '● Live (' + (data.subscriberCount || 0) + ' subscribers)';
                }
            });

            // Handle news events
            eventSource.onmessage = function(e) {
                try {
                    const event = JSON.parse(e.data);
                    addEventToFeed(event);
                } catch (err) {
                    console.warn('Failed to parse news event:', err);
                }
            };

            eventSource.onerror = function() {
                if (statusEl) {
                    statusEl.textContent = 'Disconnected — reconnecting...';
                    statusEl.className = 'news-status disconnected';
                }
                eventSource.close();
                eventSource = null;

                // Reconnect after delay
                if (reconnectTimeout) {
                    clearTimeout(reconnectTimeout);
                }
                reconnectTimeout = setTimeout(connect, 5000);
            };

        } catch (e) {
            console.warn('SSE connection failed:', e);
            const statusEl = document.getElementById('newsStatus');
            if (statusEl) {
                statusEl.textContent = 'Connection failed';
                statusEl.className = 'news-status disconnected';
            }
            // Retry
            reconnectTimeout = setTimeout(connect, 10000);
        }
    }

    // Fetch initial feed history
    async function fetchFeedHistory() {
        const feed = document.getElementById('newsFeed');
        if (!feed) return;

        try {
            const response = await fetch(getApiUrl() + '/api/v1/news/feed?limit=20');
            if (!response.ok) return;

            const events = await response.json();
            if (events && events.length > 0) {
                // Clear loading message
                const emptyState = feed.querySelector('.empty-state');
                if (emptyState) emptyState.remove();

                // Add events in reverse order (newest first)
                for (let i = events.length - 1; i >= 0; i--) {
                    addEventToFeed(events[i]);
                }
            }
        } catch (e) {
            console.warn('Could not fetch news feed history:', e);
        }
    }

    // Update the live clock in the news station header
    function updateNewsClock() {
        const el = document.getElementById('newsTime');
        if (el) {
            const now = new Date();
            el.textContent = now.toLocaleTimeString('en-US', { hour12: false });
        }
    }

    // Initialize
    function init() {
        // Start the live clock
        updateNewsClock();
        setInterval(updateNewsClock, 1000);

        fetchFeedHistory();
        connect();
    }

    // Expose init globally
    window.initNewsFeed = init;

    // Note: Auto-init disabled — news feed is initialized by game-launcher.js
    // when the News tab is first visited (via switchTab).
    // This prevents double initialization and ensures proper tab-based loading.

})();

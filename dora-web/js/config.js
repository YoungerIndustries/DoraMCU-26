// Configuration Manager for DoraMCU
// Fetches and manages server configuration from config.json

const ConfigManager = {
    
    config: null,
    configCache: new Map(),
    
    // Load configuration from config.json
    async loadConfig() {
        // Return cached config if available
        if (this.config) {
            return this.config;
        }
        
        try {
            // Fetch config.json from the same origin
            const response = await fetch('config.json');
            
            if (!response.ok) {
                throw new Error(`Failed to load config.json: ${response.status} ${response.statusText}`);
            }
            
            this.config = await response.json();
            
            // Validate required fields
            if (!this.config.apiUrl) {
                console.warn('Config missing apiUrl, using default');
                this.config.apiUrl = 'http://localhost:8080';
            }
            
            if (!this.config.clients) {
                console.warn('Config missing clients manifest, using defaults');
                this.config.clients = this.getDefaultClients();
            }
            
            if (!this.config.version) {
                this.config.version = '0.0.0';
            }
            
            console.log('DoraMCU Config loaded:', this.config);
            return this.config;
            
        } catch (error) {
            console.error('Error loading config:', error);
            
            // Fallback to default config
            this.config = this.getDefaultConfig();
            console.warn('Using default config:', this.config);
            return this.config;
        }
    },
    
    // Get default configuration
    getDefaultConfig() {
        return {
            apiUrl: 'http://localhost:8080',
            version: '0.0.0',
            clients: this.getDefaultClients()
        };
    },
    
    // Get default client manifest
    getDefaultClients() {
        return {
            desktop: {
                name: 'DoraMCU Desktop',
                description: 'Full-featured desktop game client',
                platforms: ['windows', 'macos', 'linux'],
                downloads: {
                    windows: {
                        url: '/api/v1/clients/download/unity/windows',
                        filename: 'DoraMCU-Setup.exe',
                        size: '150MB',
                        architecture: 'x64'
                    },
                    macos: {
                        url: '/api/v1/clients/download/unity/macos',
                        filename: 'DoraMCU.dmg',
                        size: '140MB',
                        architecture: 'x64'
                    },
                    linux: {
                        url: '/api/v1/clients/download/unity/linux',
                        filename: 'DoraMCU.AppImage',
                        size: '145MB',
                        architecture: 'x64'
                    }
                }
            },
            electron: {
                name: 'DoraMCU Electron',
                description: 'Electron-based desktop launcher',
                platforms: ['windows', 'macos', 'linux'],
                downloads: {
                    windows: {
                        url: '/api/v1/clients/download/electron/windows',
                        filename: 'DoraMCU-Electron-Setup.exe',
                        size: '80MB',
                        architecture: 'x64'
                    }
                }
            },
            web: {
                name: 'DoraMCU Web',
                description: 'Browser-based game client',
                platforms: ['all'],
                url: '/clients/desktop/'
            },
            mobile: {
                name: 'DoraMCU Mobile',
                description: 'Lightweight PWA game client',
                platforms: ['android', 'ios'],
                url: '/clients/mobile/'
            },
            tablet: {
                name: 'DoraMCU Tablet',
                description: 'Tablet-optimized game client',
                platforms: ['android', 'ios'],
                url: '/clients/tablet/'
            }
        };
    },
    
    // Get API URL for backend requests
    getApiUrl() {
        return this.config?.apiUrl || 'http://localhost:8080';
    },
    
    // Get available clients for a specific platform
    getClientsForPlatform(os) {
        if (!this.config?.clients) {
            return [];
        }
        
        const available = [];
        
        for (const [key, client] of Object.entries(this.config.clients)) {
            if (client.platforms && (client.platforms.includes('all') || client.platforms.includes(os))) {
                available.push({ id: key, ...client });
            }
        }
        
        return available;
    },
    
    // Get download URL for a specific client and platform
    getDownloadUrl(clientId, os) {
        const client = this.config?.clients?.[clientId];
        if (!client || !client.downloads) {
            return null;
        }
        
        const download = client.downloads[os];
        if (!download) {
            return null;
        }
        
        // If URL is relative, prepend API URL
        if (download.url.startsWith('/')) {
            return this.getApiUrl() + download.url;
        }
        
        return download.url;
    },
    
    // Get web client URL for a specific tier
    getWebClientUrl(tier) {
        const client = this.config?.clients?.[tier];
        if (client && client.url) {
            return client.url;
        }
        
        // Fallback URLs
        const fallbacks = {
            'desktop': '/clients/desktop/',
            'tablet': '/clients/tablet/',
            'mobile': '/clients/mobile/',
            'low-end': '/clients/mobile/'
        };
        
        return fallbacks[tier] || '/clients/mobile/';
    }
};

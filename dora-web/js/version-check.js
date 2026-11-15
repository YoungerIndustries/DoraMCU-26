// Version Check and Installation Detection for DoraMCU
// Detects installed desktop clients, compares versions, and manages PWA installation

const VersionChecker = {
    
    // Storage keys
    STORAGE_INSTALLED_VERSION: 'doramcu_installed_version',
    STORAGE_INSTALL_TYPE: 'doramcu_install_type',
    STORAGE_LAST_CHECK: 'doramcu_last_version_check',
    
    // Custom protocol for deep linking to installed app
    PROTOCOL_URL: 'doramcu://open',
    
    /**
     * Check if a desktop client is installed and if the version is current.
     * Returns an object with installation status and version info.
     */
    async checkInstallation(serverVersion) {
        const result = {
            installed: false,
            installType: null, // 'pwa', 'electron', 'unity', null
            installedVersion: null,
            serverVersion: serverVersion,
            needsUpdate: false,
            canDeepLink: false,
            canInstallPWA: false
        };
        
        // Check PWA standalone mode (installed via browser)
        if (window.matchMedia('(display-mode: standalone)').matches || 
            window.navigator.standalone === true) {
            result.installed = true;
            result.installType = 'pwa';
            const storedVersion = localStorage.getItem(this.STORAGE_INSTALLED_VERSION);
            result.installedVersion = storedVersion || serverVersion;
            result.needsUpdate = this.compareVersions(result.installedVersion, serverVersion) < 0;
        }
        
        // Check for stored Electron/Unity installation
        if (!result.installed) {
            const installType = localStorage.getItem(this.STORAGE_INSTALL_TYPE);
            const storedVersion = localStorage.getItem(this.STORAGE_INSTALLED_VERSION);
            
            if (installType && storedVersion) {
                result.installed = true;
                result.installType = installType;
                result.installedVersion = storedVersion;
                result.needsUpdate = this.compareVersions(storedVersion, serverVersion) < 0;
            }
        }
        
        // Check if PWA install prompt is available
        if (!result.installed && window['deferredPrompt']) {
            result.canInstallPWA = true;
        }
        
        // Check if deep linking is possible (for Electron/Unity)
        result.canDeepLink = result.installed && (result.installType === 'electron' || result.installType === 'unity');
        
        return result;
    },
    
    /**
     * Attempt to open the installed desktop client via custom protocol.
     * Returns true if the attempt was made (browser may block it).
     */
    tryOpenDesktopClient() {
        // Try custom protocol handler
        const link = document.createElement('a');
        link.href = this.PROTOCOL_URL;
        link.style.display = 'none';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // Set a timeout to detect if the app opened
        return new Promise((resolve) => {
            const startTime = Date.now();
            const checkVisibility = () => {
                if (document.hidden) {
                    resolve(true); // App likely opened
                } else if (Date.now() - startTime > 2000) {
                    resolve(false); // Timeout - app didn't open
                } else {
                    setTimeout(checkVisibility, 100);
                }
            };
            checkVisibility();
        });
    },
    
    /**
     * Register a desktop client installation.
     */
    registerInstallation(type, version) {
        localStorage.setItem(this.STORAGE_INSTALL_TYPE, type);
        localStorage.setItem(this.STORAGE_INSTALLED_VERSION, version);
        localStorage.setItem(this.STORAGE_LAST_CHECK, Date.now().toString());
        console.log(`Registered ${type} client installation: v${version}`);
    },
    
    /**
     * Compare two semantic version strings.
     * Returns: -1 if a < b, 0 if a == b, 1 if a > b
     */
    compareVersions(a, b) {
        if (!a || !b) return 0;
        
        const partsA = a.split('.').map(Number);
        const partsB = b.split('.').map(Number);
        
        const maxLen = Math.max(partsA.length, partsB.length);
        for (let i = 0; i < maxLen; i++) {
            const numA = partsA[i] || 0;
            const numB = partsB[i] || 0;
            if (numA < numB) return -1;
            if (numA > numB) return 1;
        }
        return 0;
    },
    
    /**
     * Fetch the server version and check for updates.
     */
    async fetchServerVersion() {
        try {
            // Try the dedicated version endpoint first
            const response = await fetch('/api/v1/clients/version');
            if (response.ok) {
                const data = await response.json();
                return data.version;
            }
        } catch (error) {
            console.warn('Version endpoint unavailable, trying manifest:', error);
        }
        
        // Fallback to manifest endpoint (always available)
        try {
            const manifestResponse = await fetch('/api/v1/clients/manifest');
            if (manifestResponse.ok) {
                const manifest = await manifestResponse.json();
                return manifest.version;
            }
        } catch (error) {
            console.warn('Manifest endpoint unavailable:', error);
        }
        
        // Final fallback to config
        const config = await ConfigManager.loadConfig();
        return config.version || '0.0.0';
    },
    
    /**
     * Check if PWA install prompt is available and trigger it.
     */
    async installPWA() {
        if (window['deferredPrompt']) {
            try {
                window['deferredPrompt'].prompt();
                const outcome = await window['deferredPrompt'].userChoice;
                if (outcome.outcome === 'accepted') {
                    console.log('PWA installation accepted');
                    window['deferredPrompt'] = null;
                }
                return outcome.outcome === 'accepted';
            } catch (error) {
                console.error('PWA install failed:', error);
                return false;
            }
        }
        return false;
    }
};

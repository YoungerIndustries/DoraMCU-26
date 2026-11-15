// Client Loader Module for DoraMCU
// Dynamically loads the appropriate client module based on platform detection

const ClientLoader = {
    
    // Load the appropriate client based on platform and config
    async loadClient(platform, config) {
        const statusEl = document.getElementById('status');
        const container = document.getElementById('client-container');
        const loadingContainer = document.querySelector('.loading-container');
        
        console.log('Loading client for platform:', platform);
        console.log('Configuration:', config);
        
        try {
            // Determine the client type to load
            const clientType = PlatformDetector.getRecommendedClient(platform.tier, platform.os);
            
            // For desktop, offer native client download option
            if (clientType === 'desktop' && PlatformDetector.hasNativeClient(platform.os)) {
                statusEl.textContent = 'Desktop detected - loading full client...';
                return await this.loadDesktopClient(platform, config);
            }
            
            // For tablet, load tablet-optimized web client
            if (clientType === 'tablet') {
                statusEl.textContent = 'Tablet detected - loading optimized client...';
                return await this.loadWebClient('tablet', config);
            }
            
            // For mobile, load lightweight PWA client
            if (clientType === 'mobile') {
                statusEl.textContent = 'Mobile detected - loading lightweight client...';
                return await this.loadWebClient('mobile', config);
            }
            
            // For low-end devices, load minimal client
            if (clientType === 'low-end') {
                statusEl.textContent = 'Loading minimal client...';
                return await this.loadWebClient('mobile', config);
            }
            
            // Fallback to mobile client
            return await this.loadWebClient('mobile', config);
            
        } catch (error) {
            console.error('Failed to load client:', error);
            statusEl.textContent = 'Error loading client. Showing fallback.';
            statusEl.style.color = '#ff6b6b';
            
            // Fallback to mobile client
            return await this.loadWebClient('mobile', config);
        }
    },
    
    // Load desktop client with native client offer
    async loadDesktopClient(platform, config) {
        const statusEl = document.getElementById('status');
        
        // Check if native client is available
        const downloadUrl = ConfigManager.getDownloadUrl('desktop', platform.os);
        
        if (downloadUrl) {
            statusEl.textContent = 'Full client available. Loading web version...';
            
            // Show download offer after a short delay
            setTimeout(() => {
                this.showNativeClientOffer(downloadUrl, platform.os);
            }, 2000);
        }
        
        // Load the full web client
        return await this.loadWebClient('desktop', config);
    },
    
    // Load web-based client for the specified tier
    async loadWebClient(tier, config) {
        const statusEl = document.getElementById('status');
        const container = document.getElementById('client-container');
        const loadingContainer = document.querySelector('.loading-container');
        
        // Get the client URL from config
        const clientUrl = ConfigManager.getWebClientUrl(tier);
        
        statusEl.textContent = `Loading ${tier} client from ${clientUrl}...`;
        
        // Create iframe to load the client
        const iframe = document.createElement('iframe');
        iframe.src = clientUrl;
        iframe.style.width = '100%';
        iframe.style.height = '100%';
        iframe.style.border = 'none';
        iframe.title = `DoraMCU ${tier} client`;
        
        // Add API URL as a message to the iframe
        iframe.onload = () => {
            try {
                iframe.contentWindow.postMessage({
                    type: 'CONFIG',
                    apiUrl: config.apiUrl,
                    version: config.version
                }, '*');
            } catch (e) {
                console.warn('Could not send config to iframe:', e);
            }
        };
        
        // Hide loading screen and show client
        loadingContainer.style.display = 'none';
        container.style.display = 'block';
        container.appendChild(iframe);
        
        console.log(`Loaded ${tier} client from ${clientUrl}`);
    },
    
    // Show native client download offer
    showNativeClientOffer(downloadUrl, os) {
        const container = document.getElementById('client-container');
        
        // Check if offer already exists
        if (document.getElementById('native-client-offer')) {
            return;
        }
        
        const offer = document.createElement('div');
        offer.id = 'native-client-offer';
        offer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #2a2a4a;
            border: 1px solid #00d4ff;
            border-radius: 8px;
            padding: 15px;
            z-index: 1000;
            max-width: 300px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
        `;
        
        const osName = os.charAt(0).toUpperCase() + os.slice(1);
        offer.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                <strong style="color: #00d4ff;">Native Client Available</strong>
                <button id="close-offer" style="background: none; border: none; color: #fff; cursor: pointer; font-size: 18px;">&times;</button>
            </div>
            <p style="font-size: 12px; color: #a0a0a0; margin-bottom: 10px;">
                Get the full-featured ${osName} desktop client for the best experience.
            </p>
            <a href="${downloadUrl}" id="download-native" style="
                display: block;
                text-align: center;
                background: #00d4ff;
                color: #1a1a2e;
                padding: 8px;
                border-radius: 4px;
                text-decoration: none;
                font-weight: bold;
                font-size: 12px;
            ">Download for ${osName}</a>
        `;
        
        document.body.appendChild(offer);
        
        // Close button handler
        document.getElementById('close-offer').addEventListener('click', () => {
            offer.remove();
        });
        
        // Auto-hide after 10 seconds
        setTimeout(() => {
            if (offer.parentNode) {
                offer.remove();
            }
        }, 10000);
    }
};

// Platform Detection Module for DoraMCU
// Classifies devices into tiers based on hardware capabilities and device type

const PlatformDetector = {
    
    // Device tiers
    TIER_DESKTOP_FULL: 'desktop',
    TIER_TABLET: 'tablet',
    TIER_MOBILE: 'mobile',
    TIER_LOW_END: 'low-end',
    
    // Operating systems
    OS_WINDOWS: 'windows',
    OS_MACOS: 'macos',
    OS_LINUX: 'linux',
    OS_ANDROID: 'android',
    OS_IOS: 'ios',
    OS_UNKNOWN: 'unknown',
    
    // Detect the current platform
    detect() {
        const userAgent = navigator.userAgent;
        const platform = navigator.platform || '';
        
        // Detect operating system
        const os = this.detectOS(userAgent, platform);
        
        // Detect device type
        const isTouchDevice = this.isTouchDevice();
        const screenWidth = window.screen.width;
        const screenHeight = window.screen.height;
        const hasHighDPI = window.devicePixelRatio > 1;
        
        // Detect hardware capabilities
        const cores = navigator.hardwareConcurrency || 2;
        const memory = navigator.deviceMemory || 4;
        
        // Determine device tier
        const tier = this.determineTier(os, isTouchDevice, screenWidth, cores, memory);
        
        // Build platform info object
        return {
            tier: tier,
            os: os,
            isTouchDevice: isTouchDevice,
            screenWidth: screenWidth,
            screenHeight: screenHeight,
            hasHighDPI: hasHighDPI,
            cores: cores,
            memory: memory,
            userAgent: userAgent,
            platform: platform
        };
    },
    
    // Detect operating system from user agent
    detectOS(userAgent, platform) {
        const ua = userAgent.toLowerCase();
        const plat = platform.toLowerCase();
        
        if (ua.includes('iphone') || ua.includes('ipad') || (ua.includes('mac') && this.isTouchDevice())) {
            return this.OS_IOS;
        }
        if (ua.includes('android')) {
            return this.OS_ANDROID;
        }
        if (plat.includes('win') || ua.includes('windows')) {
            return this.OS_WINDOWS;
        }
        if (plat.includes('mac') || ua.includes('mac os')) {
            return this.OS_MACOS;
        }
        if (plat.includes('linux') || ua.includes('linux')) {
            return this.OS_LINUX;
        }
        return this.OS_UNKNOWN;
    },
    
    // Check if device has touch support
    isTouchDevice() {
        return 'ontouchstart' in window || 
               navigator.maxTouchPoints > 0 || 
               navigator.msMaxTouchPoints > 0;
    },
    
    // Determine the device tier based on capabilities
    determineTier(os, isTouchDevice, screenWidth, cores, memory) {
        // Tier 4: Low-end devices
        if (cores < 2 || memory < 2) {
            return this.TIER_LOW_END;
        }
        
        // Tier 3: Mobile devices (touch, small screen)
        if (isTouchDevice && screenWidth < 768) {
            return this.TIER_MOBILE;
        }
        
        // Tier 2: Tablet devices (touch, medium screen)
        if (isTouchDevice && screenWidth >= 768 && screenWidth <= 1024) {
            return this.TIER_TABLET;
        }
        
        // Tier 2: Large tablets (iPad Pro, Surface Pro, etc.)
        if (isTouchDevice && screenWidth > 1024 && cores >= 4) {
            return this.TIER_TABLET;
        }
        
        // Tier 1: Desktop (no touch, full capabilities)
        if (!isTouchDevice && cores >= 4 && memory >= 4) {
            return this.TIER_DESKTOP_FULL;
        }
        
        // Fallback for desktop-like devices with lower specs
        if (!isTouchDevice) {
            return this.TIER_DESKTOP_FULL;
        }
        
        // Default fallback
        return this.TIER_MOBILE;
    },
    
    // Check if native client is available for this platform
    hasNativeClient(os) {
        return os === this.OS_WINDOWS || os === this.OS_MACOS || os === this.OS_LINUX;
    },
    
    // Get recommended client type for this platform
    getRecommendedClient(tier, os) {
        switch (tier) {
            case this.TIER_DESKTOP_FULL:
                return 'desktop';
            case this.TIER_TABLET:
                return 'tablet';
            case this.TIER_MOBILE:
                return 'mobile';
            case this.TIER_LOW_END:
                return 'low-end';
            default:
                return 'mobile';
        }
    }
};

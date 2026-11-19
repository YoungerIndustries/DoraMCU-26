package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.service.voice.VoiceProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes default voice profiles on application startup.
 */
@Component
public class VoiceProfileInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VoiceProfileInitializer.class);

    private final VoiceProfileService voiceProfileService;

    public VoiceProfileInitializer(VoiceProfileService voiceProfileService) {
        this.voiceProfileService = voiceProfileService;
    }

    @Override
    public void run(String... args) {
        try {
            voiceProfileService.initializeDefaultProfiles();
        } catch (Exception e) {
            log.warn("Failed to initialize default voice profiles: {}", e.getMessage());
        }
    }
}

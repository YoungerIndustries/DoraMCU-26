package com.yisy.modules.doramcu.skin.config;

import com.yisy.modules.doramcu.hex.Skin;

import com.yisy.modules.doramcu.skin.DefaultSkinLibrary;
import com.yisy.modules.doramcu.skin.SkinService;
import com.yisy.modules.doramcu.skin.repository.SkinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the default skin library at application startup.
 * <p>
 * Loads 10 pre-built skins (ships, stations, vehicles, buildings) and 3 faction
 * themes into the database. Runs only once when the skins table is empty, so
 * re-starting the application will not duplicate the default data.
 * </p>
 * <p>
 * Enabled by setting {@code app.seed-data=true} in the application properties.
 * </p>
 */
@Configuration
public class SkinDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SkinDataInitializer.class);

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initDefaultSkins(SkinService skinService, SkinRepository skinRepository) {
        return args -> {
            if (skinRepository.count() > 0) {
                log.info("Skins table already contains data — skipping default skin initialization");
                return;
            }
            log.warn("SEED DATA IS ENABLED - Loading default skin library (development only)!");
            log.info("Initializing default skin library with 10 skins and 3 faction themes...");
            DefaultSkinLibrary.initialize(skinService);
            log.info("Default skin library initialization completed: {} skins loaded", skinRepository.count());
        };
    }
}

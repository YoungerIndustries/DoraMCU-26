package com.yisy.modules.doramcu.ai.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${doramcu.web.static-dir:}")
    private String webStaticDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        List<String> locations = new ArrayList<>();
        // Support external static directory (e.g., /opt/doramcu/web/ on Pi)
        // Place it FIRST so it takes precedence over classpath resources
        if (webStaticDir != null && !webStaticDir.isEmpty()) {
            String dir = webStaticDir;
            if (!dir.endsWith("/")) {
                dir += "/";
            }
            locations.add("file:" + dir);
        }
        locations.add("classpath:/static/");
        registry.addResourceHandler("/**")
                .addResourceLocations(locations.toArray(new String[0]))
                .setCachePeriod(0);
    }
}

package com.yisy.modules.doramcu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${doramcu.web-ui.external:false}")
    private boolean webUiExternal;

    @Value("${doramcu.web.static-dir:}")
    private String webStaticDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // When web-ui.external=true, skip static resource handling (API-only mode)
        // The external web server (nginx/CDN) serves the dora-web/ static files
        if (!webUiExternal) {
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
            // Disable caching for static resources in development to ensure updated files are always served
            registry.addResourceHandler("/**")
                    .addResourceLocations(locations.toArray(new String[0]))
                    .setCachePeriod(0);
        }
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // When web-ui.external=true, skip root redirect (API-only mode)
        // The external web server serves the unified index.html
        if (!webUiExternal) {
            // Root path redirects to the D&D dark fantasy login page (index.html), which then redirects to game.html (corporation dashboard) after login
            registry.addRedirectViewController("/", "/index.html");
        }
        // Corporation dashboard is served by the neuron PortalController as well,
        // so no redirect to the old Angular app.
        // Web client redirects — serve index.html for each client tier
        registry.addRedirectViewController("/clients/desktop/", "/clients/desktop/index.html");
        registry.addRedirectViewController("/clients/mobile/", "/clients/mobile/index.html");
        registry.addRedirectViewController("/clients/tablet/", "/clients/tablet/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow SSE and REST API access from Electron app and other origins
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Cache-Control", "X-Accel-Buffering", "Connection", "Content-Type")
                .maxAge(3600);
    }

    /**
     * Filter that forces no-cache headers on all static resource requests.
     * This ensures browsers always fetch fresh content during development.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    @Bean
    public Filter noCacheFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                String path = httpRequest.getRequestURI();

                // SSE endpoints — disable buffering and caching so the stream works through proxies
                if (path.startsWith("/api/v1/news/live") || path.startsWith("/api/v1/events/")) {
                    httpResponse.setHeader("Cache-Control", "no-cache");
                    httpResponse.setHeader("X-Accel-Buffering", "no");
                    httpResponse.setHeader("Connection", "keep-alive");
                }
                // Apply no-cache headers to static resources
                else if (path.startsWith("/static/") || path.endsWith(".html") ||
                    path.endsWith(".css") || path.endsWith(".js") ||
                    path.endsWith(".png") || path.endsWith(".svg") || path.endsWith(".ico")) {
                    httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    httpResponse.setHeader("Pragma", "no-cache");
                    httpResponse.setDateHeader("Expires", 0);
                }

                chain.doFilter(request, response);
            }
        };
    }
}

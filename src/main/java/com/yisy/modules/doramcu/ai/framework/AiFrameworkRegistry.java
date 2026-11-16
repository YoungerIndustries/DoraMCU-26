package com.yisy.modules.doramcu.ai.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central registry for all AI framework services.
 */
@Service
public class AiFrameworkRegistry implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiFrameworkRegistry.class);

    private final List<AiService> services = new ArrayList<>();
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    @Autowired
    public AiFrameworkRegistry(ApplicationContext context) {
        Map<String, AiService> beans = context.getBeansOfType(AiService.class);
        for (AiService service : beans.values()) {
            if (service != this) {
                services.add(service);
            }
        }
        log.info("Registered {} AI services", services.size());
    }

    @Override
    public String getServiceId() { return "ai-framework-registry"; }
    @Override
    public String getServiceName() { return "AI Framework Registry"; }
    @Override
    public AiServiceStatus getStatus() { return status; }

    @Override
    public void initialize() {
        status = AiServiceStatus.STARTING;
        for (AiService service : services) {
            try { service.initialize(); }
            catch (Exception e) { log.error("Failed to initialize: {}", service.getServiceName(), e); }
        }
        status = AiServiceStatus.RUNNING;
    }

    @Override
    public void shutdown() {
        for (AiService service : services) {
            try { service.shutdown(); }
            catch (Exception e) { log.error("Failed to shutdown: {}", service.getServiceName(), e); }
        }
        status = AiServiceStatus.STOPPED;
    }

    @Override
    public boolean isHealthy() {
        for (AiService service : services) {
            if (!service.isHealthy()) return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("serviceCount", services.size());
        metrics.put("status", status.name());
        return metrics;
    }

    public Optional<AiService> getService(String serviceId) {
        return services.stream().filter(s -> s.getServiceId().equals(serviceId)).findFirst();
    }

    public List<AiService> getAllServices() {
        return Collections.unmodifiableList(services);
    }

    public <T extends AiService> List<T> getServicesByType(Class<T> clazz) {
        return services.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }
}

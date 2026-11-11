package com.yisy.modules.doramcu.neuron.service;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA Entity Listener that automatically publishes CDC events when entities
 * are created, updated, or deleted.
 * <p>
 * To enable CDC for an entity, add the annotation:
 * {@code @EntityListeners(CdcEntityListener.class)}
 * </p>
 * <p>
 * Example:
 * <pre>
 * &#64;Entity
 * &#64;EntityListeners(CdcEntityListener.class)
 * public class Player { ... }
 * </pre>
 * </p>
 * <p>
 * This class is both a JPA entity listener (for @PostPersist/@PostUpdate/@PostRemove)
 * and a Spring component (to access the CdcEventPublisher bean via ApplicationContext).
 * </p>
 */
@Component
public class CdcEntityListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        CdcEventPublisher publisher = getPublisher();
        if (publisher != null) {
            publisher.onEntityCreated(entity);
        }
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        CdcEventPublisher publisher = getPublisher();
        if (publisher != null) {
            publisher.onEntityUpdated(entity);
        }
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        CdcEventPublisher publisher = getPublisher();
        if (publisher != null) {
            publisher.onEntityDeleted(entity);
        }
    }

    private static CdcEventPublisher getPublisher() {
        if (applicationContext != null) {
            return applicationContext.getBean(CdcEventPublisher.class);
        }
        return null;
    }
}

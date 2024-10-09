package com.blackduck.integration.alert.api.processor;

/**
 * Marks a class as a cache that should be cleared at the end of the notification processing lifecycle.
 */
public interface NotificationProcessingLifecycleCache {
    void clear();

}

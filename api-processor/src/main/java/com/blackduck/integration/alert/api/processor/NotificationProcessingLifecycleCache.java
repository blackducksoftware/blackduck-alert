/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

/**
 * Marks a class as a cache that should be cleared at the end of the notification processing lifecycle.
 */
public interface NotificationProcessingLifecycleCache {
    void clear();

}

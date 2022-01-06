/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api;

/**
 * Marks a class as a cache that should be cleared at the end of the notification processing lifecycle.
 */
public interface NotificationProcessingLifecycleCache {
    void clear();

}

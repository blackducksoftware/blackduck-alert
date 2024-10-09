/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

// Javadoc of Runnable implies running in a thread and being executed asynchronously.
@FunctionalInterface
public interface Procedure {
    void run();
}

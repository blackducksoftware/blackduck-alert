/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider.lifecycle;

import java.util.List;

import com.blackduck.integration.alert.api.provider.state.ProviderProperties;

public interface ProviderTaskFactory {
    List<ProviderTask> createTasks(ProviderProperties providerProperties);

}

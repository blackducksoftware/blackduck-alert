/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider;

import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;

public abstract class ProviderContent {
    private final Set<ProviderNotificationType> supportedNotificationTypes;
    private final Set<ProcessingType> supportedProcessingTypes;

    public ProviderContent(Set<ProviderNotificationType> supportedNotificationTypes, Set<ProcessingType> supportedProcessingTypes) {
        this.supportedNotificationTypes = supportedNotificationTypes;
        this.supportedProcessingTypes = supportedProcessingTypes;
    }

    public Set<ProviderNotificationType> getContentTypes() {
        return Set.copyOf(supportedNotificationTypes);
    }

    public Set<ProcessingType> getSupportedProcessingTypes() {
        return Set.copyOf(supportedProcessingTypes);
    }

}

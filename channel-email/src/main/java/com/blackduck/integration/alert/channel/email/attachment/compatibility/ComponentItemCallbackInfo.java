/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.attachment.compatibility;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;

public class ComponentItemCallbackInfo extends AlertSerializableModel {
    private final String callbackUrl;
    private final ProviderKey providerKey;
    private final String notificationType;

    public ComponentItemCallbackInfo(String callbackUrl, ProviderKey providerKey, String notificationType) {
        this.callbackUrl = callbackUrl;
        this.providerKey = providerKey;
        this.notificationType = notificationType;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public ProviderKey getProviderKey() {
        return providerKey;
    }

    public String getNotificationType() {
        return notificationType;
    }

}

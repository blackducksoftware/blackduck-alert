/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

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

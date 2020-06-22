package com.synopsys.integration.alert.common.message.model;

import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

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

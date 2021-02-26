/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.processor.notification;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class NotificationFilterModel extends AlertSerializableModel {
    private final String provider;
    private final Long providerConfigId;
    private final NotificationType notificationType;

    public NotificationFilterModel(String provider, Long providerConfigId, NotificationType notificationType) {
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.notificationType = notificationType;
    }

    public String getProvider() {
        return provider;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

}

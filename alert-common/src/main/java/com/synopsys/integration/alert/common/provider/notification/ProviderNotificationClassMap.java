/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider.notification;

import java.util.Map;

public class ProviderNotificationClassMap {
    private Map<String, Class<?>> notificationTypeToClass;

    public ProviderNotificationClassMap(Map<String, Class<?>> notificationTypeToClass) {
        this.notificationTypeToClass = notificationTypeToClass;
    }

    public Class<?> get(String notificationType) {
        return notificationTypeToClass.get(notificationType);
    }

}

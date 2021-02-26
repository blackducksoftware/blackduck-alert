/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.cache;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;

public class NotificationDeserializationCache {
    private Gson gson;
    private ProviderNotificationClassMap providerNotificationClassMap;
    private Map<Long, Object> idToDeserializedContent;

    public NotificationDeserializationCache(Gson gson, ProviderNotificationClassMap providerNotificationClassMap) {
        this.gson = gson;
        this.providerNotificationClassMap = providerNotificationClassMap;
        this.idToDeserializedContent = new HashMap<>();
    }

    public <T> T getTypedContent(AlertNotificationModel notification, Class<T> clazz) {
        return clazz.cast(getTypedContent(notification));
    }

    public Object getTypedContent(AlertNotificationModel notification) {
        Object deserializedNotification = idToDeserializedContent.get(notification.getId());
        if (null == deserializedNotification) {
            deserializedNotification = deserializeNotification(notification);
        }
        return deserializedNotification;
    }

    private Object deserializeNotification(AlertNotificationModel notification) {
        Class<?> notificationTypeClass = providerNotificationClassMap.get(notification.getNotificationType());
        Object deserializedNotification = gson.fromJson(notification.getContent(), notificationTypeClass);
        idToDeserializedContent.put(notification.getId(), deserializedNotification);
        return deserializedNotification;
    }

}

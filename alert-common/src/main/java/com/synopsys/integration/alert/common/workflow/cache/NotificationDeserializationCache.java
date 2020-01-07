/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.workflow.cache;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;

public class NotificationDeserializationCache {
    private Gson gson;
    private ProviderNotificationClassMap providerNotificationClassMap;
    private Map<Long, Object> idToDeserializedContent;

    public NotificationDeserializationCache(Gson gson, ProviderNotificationClassMap providerNotificationClassMap) {
        this.gson = gson;
        this.providerNotificationClassMap = providerNotificationClassMap;
        this.idToDeserializedContent = new HashMap<>();
    }

    public <T> T getTypedContent(AlertNotificationWrapper notification, Class<T> clazz) {
        return clazz.cast(getTypedContent(notification));
    }

    public Object getTypedContent(AlertNotificationWrapper notification) {
        Object deserializedNotification = idToDeserializedContent.get(notification.getId());
        if (null == deserializedNotification) {
            deserializedNotification = deserializeNotification(notification);
        }
        return deserializedNotification;
    }

    private Object deserializeNotification(AlertNotificationWrapper notification) {
        Class<?> notificationTypeClass = providerNotificationClassMap.get(notification.getNotificationType());
        Object deserializedNotification = gson.fromJson(notification.getContent(), notificationTypeClass);
        idToDeserializedContent.put(notification.getId(), deserializedNotification);
        return deserializedNotification;
    }

}

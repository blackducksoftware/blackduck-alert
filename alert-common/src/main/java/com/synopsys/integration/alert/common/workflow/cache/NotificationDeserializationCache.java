/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationContentClassMap;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;

public class NotificationDeserializationCache {
    private Gson gson;
    private ProviderNotificationContentClassMap providerNotificationClassMap;
    private Map<Long, NotificationView> idToDeserializedContent;

    public NotificationDeserializationCache(Gson gson, ProviderNotificationContentClassMap providerNotificationClassMap) {
        this.gson = gson;
        this.providerNotificationClassMap = providerNotificationClassMap;
        this.idToDeserializedContent = new HashMap<>();
    }

    public <T> T getTypedContent(AlertNotificationWrapper notification, Class<T> clazz) {
        return clazz.cast(getTypedContent(notification));
    }

    public NotificationView getTypedContent(AlertNotificationWrapper notification) {
        NotificationView deserializedNotification = idToDeserializedContent.get(notification.getId());
        if (null == deserializedNotification) {
            deserializedNotification = deserializeNotification(notification);
        }
        return deserializedNotification;
    }

    private NotificationView deserializeNotification(AlertNotificationWrapper notification) {
        Class<? extends NotificationView> notificationTypeClass = providerNotificationClassMap.get(notification.getNotificationType());
        NotificationView deserializedNotification = gson.fromJson(notification.getContent(), notificationTypeClass);
        idToDeserializedContent.put(notification.getId(), deserializedNotification);
        return deserializedNotification;
    }

}

/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.processor.api.detail;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public abstract class NotificationDetailExtractor<T extends NotificationContentComponent> {
    private final String JSON_FIELD_NOTIFICATION_CONTENT = "content";

    private final NotificationType notificationType;
    private final Class<T> notificationContentClass;
    private final Gson gson;

    public NotificationDetailExtractor(NotificationType notificationType, Class<T> notificationContentClass, Gson gson) {
        this.notificationType = notificationType;
        this.notificationContentClass = notificationContentClass;
        this.gson = gson;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public final List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel) {
        // The NotificationView object (which we assume getContent() deserializes to) does not have a common "content" field explicitly defined.
        JsonObject notificationViewJsonObject = gson.fromJson(alertNotificationModel.getContent(), JsonObject.class);
        JsonObject notificationContentJsonObject = notificationViewJsonObject.get(JSON_FIELD_NOTIFICATION_CONTENT).getAsJsonObject();
        T notificationContent = gson.fromJson(notificationContentJsonObject, notificationContentClass);
        return extractDetailedContent(alertNotificationModel, notificationContent);
    }

    protected abstract List<DetailedNotificationContent> extractDetailedContent(AlertNotificationModel alertNotificationModel, T notificationContent);

}

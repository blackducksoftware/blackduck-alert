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
package com.synopsys.integration.alert.processor.api.filter.model;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;

public class NotificationContentWrapper extends AlertSerializableModel {
    private final AlertNotificationModel alertNotificationModel;
    private final NotificationContentComponent notificationContent;

    private final Class<? extends NotificationContentComponent> notificationContentClass;

    public NotificationContentWrapper(
        AlertNotificationModel alertNotificationModel,
        NotificationContentComponent notificationContent,
        Class<? extends NotificationContentComponent> notificationContentClass
    ) {
        this.alertNotificationModel = alertNotificationModel;
        this.notificationContent = notificationContent;
        this.notificationContentClass = notificationContentClass;
    }

    public AlertNotificationModel getAlertNotificationModel() {
        return alertNotificationModel;
    }

    public NotificationContentComponent getNotificationContent() {
        return notificationContent;
    }

    public String extractNotificationType() {
        return getAlertNotificationModel().getNotificationType();
    }

    public Long getNotificationId() {
        return getAlertNotificationModel().getId();
    }

    public Class<? extends NotificationContentComponent> getNotificationContentClass() {
        return notificationContentClass;
    }

}

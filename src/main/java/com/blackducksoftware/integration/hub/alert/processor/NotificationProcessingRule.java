/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public abstract class NotificationProcessingRule<M extends NotificationProcessingModel> {
    private final GlobalProperties globalProperties;
    private final NotificationType notificationType;

    public NotificationProcessingRule(final GlobalProperties globalProperties, final NotificationType notificationType) {
        this.globalProperties = globalProperties;
        this.notificationType = notificationType;
    }

    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    public boolean isApplicable(final CommonNotificationState commonNotificationState) {
        return notificationType == commonNotificationState.getType();
    }

    public List<String> getContentDetailKeys(final CommonNotificationState commonNotificationState) {
        final List<NotificationContentDetail> contentDetailList = commonNotificationState.getContent().getNotificationContentDetails();
        final List<String> contentKeyList = contentDetailList.stream().map(NotificationContentDetail::getContentDetailKey).collect(Collectors.toList());
        return contentKeyList;
    }

    public abstract void apply(final Map<String, M> modelMap, final CommonNotificationState commonNotificationState, final HubBucket bucket);
}

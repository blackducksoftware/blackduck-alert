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
package com.synopsys.integration.alert.processor.api.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.model.NotificationContentWrapper;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public abstract class ProviderMessageExtractor<T extends NotificationContentComponent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationType notificationType;
    private final Class<T> notificationContentClass;

    protected ProviderMessageExtractor(NotificationType notificationType, Class<T> notificationContentClass) {
        this.notificationType = notificationType;
        this.notificationContentClass = notificationContentClass;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public final ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper) {
        if (!notificationContentClass.isAssignableFrom(notificationContentWrapper.getNotificationContentClass())) {
            logger.error("The notification type provided is incompatible with this extractor: {}", notificationContentWrapper.extractNotificationType());
            return ProviderMessageHolder.empty();
        }

        T stronglyTypedContent = notificationContentClass.cast(notificationContentWrapper.getNotificationContent());
        return extract(notificationContentWrapper, stronglyTypedContent);
    }

    protected abstract ProviderMessageHolder extract(NotificationContentWrapper notificationContentWrapper, T notificationContent);

}

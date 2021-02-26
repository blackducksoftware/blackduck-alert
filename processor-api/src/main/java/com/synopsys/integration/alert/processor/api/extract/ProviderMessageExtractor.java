/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
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

package com.synopsys.integration.alert.processor.api;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.filter.FilterableNotificationWrapper;

public interface NotificationPreProcessor {
    FilterableNotificationWrapper<?> wrapNotification(AlertNotificationModel notification);

}

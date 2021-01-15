package com.synopsys.integration.alert.processor.api;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;

// TODO update this when we have completed models
public abstract class NotificationProcessor {
    public abstract List<DistributionEvent> processNotifications(List<AlertNotificationModel> notifications);

    public abstract List<DistributionEvent> processNotifications(FrequencyType frequency, List<AlertNotificationModel> notifications);

}

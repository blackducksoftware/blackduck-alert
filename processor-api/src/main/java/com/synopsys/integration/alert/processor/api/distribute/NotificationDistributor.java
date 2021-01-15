package com.synopsys.integration.alert.processor.api.distribute;

import com.synopsys.integration.alert.processor.api.detail.NotificationDetails;

public interface NotificationDistributor {
    void distribute(Object job, NotificationDetails notificationDetails);

}

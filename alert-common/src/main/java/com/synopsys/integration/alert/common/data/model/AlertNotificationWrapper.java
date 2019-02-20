package com.synopsys.integration.alert.common.data.model;

import java.util.Date;

public interface AlertNotificationWrapper {
    Long getId();

    Date getCreatedAt();

    String getProvider();

    String getNotificationType();

    String getContent();

    Date getProviderCreationTime();
}

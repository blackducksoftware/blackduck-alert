package com.synopsys.integration.alert.provider.polaris.model;

public class AlertPolarisNotification {
    private AlertPolarisIssueNotificationContentModel content;

    public AlertPolarisNotification(final AlertPolarisIssueNotificationContentModel content) {
        this.content = content;
    }

    public AlertPolarisIssueNotificationContentModel getContent() {
        return content;
    }

}

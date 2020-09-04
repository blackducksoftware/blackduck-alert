package com.synopsys.integration.alert.provider.blackduck.web;

import java.util.List;

public class NotificationFilterModelOptions {
    private List<NotificationFilterModel> options;

    public NotificationFilterModelOptions(List<NotificationFilterModel> options) {
        this.options = options;
    }

    public List<NotificationFilterModel> getOptions() {
        return options;
    }
}

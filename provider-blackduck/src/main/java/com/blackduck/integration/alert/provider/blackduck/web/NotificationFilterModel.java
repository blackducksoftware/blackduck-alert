package com.blackduck.integration.alert.provider.blackduck.web;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class NotificationFilterModel extends AlertSerializableModel {
    private String name;

    public NotificationFilterModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

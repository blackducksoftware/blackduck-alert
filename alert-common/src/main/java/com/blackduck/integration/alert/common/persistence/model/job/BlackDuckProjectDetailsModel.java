package com.blackduck.integration.alert.common.persistence.model.job;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class BlackDuckProjectDetailsModel extends AlertSerializableModel {
    private final String name;
    private final String href;

    public BlackDuckProjectDetailsModel(String name, String href) {
        this.name = name;
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

}

package com.blackduck.integration.alert.common.rest.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public abstract class Config extends AlertSerializableModel {
    private String id;

    protected Config() {
    }

    protected Config(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

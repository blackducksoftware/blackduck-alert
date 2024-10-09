package com.blackduck.integration.alert.common.rest.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class ExistenceModel extends AlertSerializableModel {
    private Boolean exists;

    public ExistenceModel() {
        // For serialization
    }

    public ExistenceModel(Boolean exists) {
        this.exists = exists;
    }

    public Boolean getExists() {
        return exists;
    }

}

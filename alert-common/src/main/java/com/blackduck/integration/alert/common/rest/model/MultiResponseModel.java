package com.blackduck.integration.alert.common.rest.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MultiResponseModel<T extends AlertSerializableModel> extends AlertSerializableModel {
    private final List<T> models;

    MultiResponseModel() {
        // For serialization
        this(List.of());
    }

    public MultiResponseModel(List<T> models) {
        this.models = models;
    }

    @JsonIgnore
    protected List<T> getModels() {
        return models;
    }

}

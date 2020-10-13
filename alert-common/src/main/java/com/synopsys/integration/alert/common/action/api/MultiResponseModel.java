package com.synopsys.integration.alert.common.action.api;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MultiResponseModel<T extends AlertSerializableModel> extends AlertSerializableModel {
    private final List<T> models;

    MultiResponseModel() {
        // For serialization
        this(List.of());
    }

    public MultiResponseModel(List<T> models) {
        this.models = models;
    }

    public List<T> getModels() {
        return models;
    }

}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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

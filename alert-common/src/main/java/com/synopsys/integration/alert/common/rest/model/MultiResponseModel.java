/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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

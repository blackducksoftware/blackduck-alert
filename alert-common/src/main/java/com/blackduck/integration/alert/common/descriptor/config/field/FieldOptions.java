/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.config.field;

import java.io.Serializable;
import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class FieldOptions<T extends Serializable> extends AlertSerializableModel {
    // Do not rename this the UI looks for a field named options in the JSON object it receives.
    private final List<T> options;

    public FieldOptions(List<T> options) {
        this.options = options;
    }

    public List<T> getOptions() {
        return options;
    }

}

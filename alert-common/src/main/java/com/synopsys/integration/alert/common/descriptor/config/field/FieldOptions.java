/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FieldOptions<T extends Serializable> extends AlertSerializableModel {
    // Do not rename this the UI looks for a field named options in the JSON object it receives.
    private List<T> options;

    public FieldOptions(List<T> options) {
        this.options = options;
    }

    public List<T> getOptions() {
        return options;
    }
}

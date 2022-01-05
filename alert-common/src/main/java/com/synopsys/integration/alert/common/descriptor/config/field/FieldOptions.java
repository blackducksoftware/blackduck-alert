/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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

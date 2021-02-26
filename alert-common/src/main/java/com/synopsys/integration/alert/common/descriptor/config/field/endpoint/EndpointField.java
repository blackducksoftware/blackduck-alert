/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public abstract class EndpointField extends ConfigField {
    private final String buttonLabel;
    private final String url;

    public EndpointField(String key, String label, String description, FieldType type, String buttonLabel, String url) {
        super(key, label, description, type);
        this.buttonLabel = buttonLabel;
        this.url = url;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getUrl() {
        return url;
    }

}

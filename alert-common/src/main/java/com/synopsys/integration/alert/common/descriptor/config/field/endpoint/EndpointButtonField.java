/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

public class EndpointButtonField extends EndpointField {
    private boolean successBox;
    private List<ConfigField> subFields;

    public EndpointButtonField(String key, String label, String description, String buttonLabel) {
        super(key, label, description, FieldType.ENDPOINT_BUTTON, buttonLabel, AbstractFunctionController.API_FUNCTION_URL);
        this.successBox = Boolean.FALSE;
        this.subFields = new LinkedList<>();
    }

    public EndpointButtonField applySuccessBox(boolean successBox) {
        this.successBox = successBox;
        return this;
    }

    public EndpointButtonField applySubFields(List<ConfigField> subFields) {
        if (null != subFields) {
            this.subFields = subFields;
        }
        return this;
    }

    public EndpointButtonField applySubField(ConfigField field) {
        if (null != field) {
            subFields.add(field);
        }
        return this;
    }

    public Boolean getSuccessBox() {
        return successBox;
    }

    public List<ConfigField> getSubFields() {
        return subFields;
    }

}

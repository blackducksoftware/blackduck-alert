/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.LinkedList;

import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

public class EndpointSelectField extends SelectConfigField {
    private String url;

    public EndpointSelectField(String key, String label, String description) {
        super(key, label, description, FieldType.ENDPOINT_SELECT, new LinkedList<>());
        this.url = AbstractFunctionController.API_FUNCTION_URL;
    }

    public EndpointSelectField applyUrl(String url) {
        if (null != url) {
            this.url = url;
        }
        return this;
    }

    public String getUrl() {
        return url;
    }

}

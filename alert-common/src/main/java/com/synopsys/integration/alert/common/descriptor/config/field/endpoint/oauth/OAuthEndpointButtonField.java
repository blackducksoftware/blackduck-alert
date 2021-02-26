/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth;

import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointField;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

public class OAuthEndpointButtonField extends EndpointField {
    public OAuthEndpointButtonField(String key, String label, String description, String buttonLabel) {
        super(key, label, description, FieldType.OAUTH_ENDPOINT_BUTTON, buttonLabel, AbstractFunctionController.API_FUNCTION_URL);
    }
}

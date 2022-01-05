/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class ConfigurationValidationHelper {
    private final AuthorizationManager authorizationManager;
    private final ConfigContextEnum context;
    private final DescriptorKey descriptorKey;

    public ConfigurationValidationHelper(AuthorizationManager authorizationManager, ConfigContextEnum context, DescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.context = context;
        this.descriptorKey = descriptorKey;
    }

    public ValidationActionResponse validate(Supplier<ValidationResponseModel> validator) {
        if (!authorizationManager.hasExecutePermission(context, descriptorKey)) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }

        return new ValidationActionResponse(HttpStatus.OK, validator.get());
    }
}

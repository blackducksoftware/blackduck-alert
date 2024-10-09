/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.api;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.ValidationActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.ConfigurationTestResult;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

public class ConfigurationTestHelper {

    private AuthorizationManager authorizationManager;
    private ConfigContextEnum context;
    private DescriptorKey descriptorKey;

    public ConfigurationTestHelper(AuthorizationManager authorizationManager, ConfigContextEnum context, DescriptorKey descriptorKey) {
        this.authorizationManager = authorizationManager;
        this.context = context;
        this.descriptorKey = descriptorKey;
    }

    public ValidationActionResponse test(Supplier<ValidationActionResponse> validationSupplier, Supplier<ConfigurationTestResult> testResultSupplier) {
        if (!authorizationManager.hasExecutePermission(context, descriptorKey)) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }

        ValidationActionResponse validationResponse = validationSupplier.get();
        // validationResponse.isError() will always be false due to the validationHelper always returning an HttpStatus OK. If unused by future test actions
        //  using different validation schemes, this should be removed.
        if (validationResponse.isError() || validationResponse.hasValidationErrors()) {
            return validationResponse;
        }

        ConfigurationTestResult testResult = testResultSupplier.get();
        if (testResult.isSuccess()) {
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success(testResult.getStatusMessage()));
        } else {
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.generalError(testResult.getStatusMessage()));
        }
    }
}

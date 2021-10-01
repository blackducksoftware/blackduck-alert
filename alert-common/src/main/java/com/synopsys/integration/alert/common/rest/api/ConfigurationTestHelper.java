/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.function.ThrowingSupplier;

@Component
public class ConfigurationTestHelper {

    private AuthorizationManager authorizationManager;

    @Autowired
    public ConfigurationTestHelper(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public ValidationActionResponse test(Supplier<ValidationActionResponse> validationSupplier, ThrowingSupplier<MessageResult, AlertException> testResultSupplier, ConfigContextEnum context, DescriptorKey descriptorKey) {
        if (!authorizationManager.hasExecutePermission(context, descriptorKey)) {
            ValidationResponseModel responseModel = ValidationResponseModel.generalError(ActionResponse.FORBIDDEN_MESSAGE);
            return new ValidationActionResponse(HttpStatus.FORBIDDEN, responseModel);
        }

        ValidationActionResponse validationResponse = validationSupplier.get();
        if (validationResponse.isError()) {
            return validationResponse;
        }

        try {
            MessageResult messageResult = testResultSupplier.get();
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.success(messageResult.getStatusMessage()));
        } catch (AlertException e) {
            return new ValidationActionResponse(HttpStatus.OK, ValidationResponseModel.generalError(e.getMessage()));
        }
    }
}

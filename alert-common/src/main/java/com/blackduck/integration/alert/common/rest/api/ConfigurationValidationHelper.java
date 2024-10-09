package com.blackduck.integration.alert.common.rest.api;

import java.util.function.Supplier;

import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.ValidationActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

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

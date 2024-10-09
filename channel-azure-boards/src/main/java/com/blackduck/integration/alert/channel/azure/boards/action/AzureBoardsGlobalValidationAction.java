package com.blackduck.integration.alert.channel.azure.boards.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class AzureBoardsGlobalValidationAction {
    private final AzureBoardsGlobalConfigurationValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public AzureBoardsGlobalValidationAction(AzureBoardsGlobalConfigurationValidator validator, AuthorizationManager authorizationManager) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS);
    }

    public ActionResponse<ValidationResponseModel> validate(AzureBoardsGlobalConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource, requestResource.getId()));
    }
}

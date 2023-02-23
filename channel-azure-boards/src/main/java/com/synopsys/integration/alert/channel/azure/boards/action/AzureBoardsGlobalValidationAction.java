package com.synopsys.integration.alert.channel.azure.boards.action;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

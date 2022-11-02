package com.synopsys.integration.alert.channel.github.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.channel.github.validator.GitHubGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class GitHubGlobalValidationAction {
    private final GitHubGlobalConfigurationValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public GitHubGlobalValidationAction(GitHubGlobalConfigurationValidator validator, AuthorizationManager authorizationManager) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.GITHUB);
    }

    public ActionResponse<ValidationResponseModel> validate(GitHubGlobalConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource, requestResource.getId()));
    }
}

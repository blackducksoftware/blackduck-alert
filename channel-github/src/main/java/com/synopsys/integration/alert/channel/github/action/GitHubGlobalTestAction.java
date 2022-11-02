package com.synopsys.integration.alert.channel.github.action;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.github.database.accessor.GitHubGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.channel.github.validator.GitHubGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class GitHubGlobalTestAction {
    private static final String TEST_ERROR_MESSAGE = "An error occurred during testing: ";
    private final ConfigurationValidationHelper validationHelper;
    private final ConfigurationTestHelper testHelper;
    private final GitHubGlobalConfigurationValidator validator;
    private final GitHubGlobalConfigAccessor configurationAccessor;

    @Autowired
    public GitHubGlobalTestAction(
        AuthorizationManager authorizationManager,
        GitHubGlobalConfigurationValidator validator,
        GitHubGlobalConfigAccessor configurationAccessor
    ) {
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.validator = validator;
        this.configurationAccessor = configurationAccessor;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(GitHubGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource, requestResource.getId()));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(requestResource));
    }

    public ConfigurationTestResult testConfigModelContent(GitHubGlobalConfigModel gitHubGlobalConfigModel) {
        try {
            if (BooleanUtils.toBoolean(gitHubGlobalConfigModel.getIsApiTokenSet().orElse(Boolean.FALSE)) && gitHubGlobalConfigModel.getIsApiTokenSet().isEmpty()) {
                configurationAccessor.getConfiguration(UUID.fromString(gitHubGlobalConfigModel.getId()))
                    .map(GitHubGlobalConfigModel::getApiToken)
                    .ifPresent(gitHubGlobalConfigModel::setApiToken);
            }

        } catch (Exception ex) {
            return ConfigurationTestResult.failure(TEST_ERROR_MESSAGE + ex.getMessage());
        }
        return ConfigurationTestResult.success("Successfully connected to GitHub instance.");
    }
}

package com.synopsys.integration.alert.channel.github.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.github.database.accessor.GitHubGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.channel.github.validator.GitHubGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class GitHubGlobalCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final GitHubGlobalConfigAccessor configurationAccessor;
    private final GitHubGlobalConfigurationValidator validator;

    @Autowired
    public GitHubGlobalCrudActions(AuthorizationManager authorizationManager, GitHubGlobalConfigAccessor configurationAccessor, GitHubGlobalConfigurationValidator validator) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.GITHUB);
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
    }

    public ActionResponse<GitHubGlobalConfigModel> getOne(UUID id) {
        return configurationHelper.getOne(() -> configurationAccessor.getConfiguration(id));
    }

    public ActionResponse<AlertPagedModel<GitHubGlobalConfigModel>> getPaged(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        return configurationHelper.getPage(() -> configurationAccessor.getConfigurationPage(page, size, searchTerm, sortName, sortOrder));
    }

    public ActionResponse<GitHubGlobalConfigModel> create(GitHubGlobalConfigModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource, null),
            () -> configurationAccessor.existsConfigurationByName(resource.getName()),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<GitHubGlobalConfigModel> update(UUID id, GitHubGlobalConfigModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource, id.toString()),
            () -> configurationAccessor.existsConfigurationById(id),
            () -> configurationAccessor.updateConfiguration(id, requestResource)
        );
    }

    public ActionResponse<GitHubGlobalConfigModel> delete(UUID id) {
        return configurationHelper.delete(
            () -> configurationAccessor.existsConfigurationById(id),
            () -> configurationAccessor.deleteConfiguration(id)
        );
    }

}

package com.synopsys.integration.alert.channel.jira.server.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraServerGlobalCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final JiraServerGlobalConfigAccessor configurationAccessor;
    private final JiraServerGlobalConfigurationValidator validator;

    @Autowired
    public JiraServerGlobalCrudActions(AuthorizationManager authorizationManager, JiraServerGlobalConfigAccessor configurationAccessor, JiraServerGlobalConfigurationValidator validator) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
    }

    public ActionResponse<JiraServerGlobalConfigModel> getOne(UUID id) {
        return configurationHelper.getOne(() -> configurationAccessor.getConfiguration(id));
    }

    public ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> getPaged(int page, int size) {
        return configurationHelper.getPage(() -> configurationAccessor.getConfigurationPage(page, size));
    }

    public ActionResponse<JiraServerGlobalConfigModel> create(JiraServerGlobalConfigModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<JiraServerGlobalConfigModel> update(UUID id, JiraServerGlobalConfigModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource),
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.updateConfiguration(id, requestResource)
        );
    }
    
    public ActionResponse<JiraServerGlobalConfigModel> delete(UUID id) {
        return configurationHelper.delete(
            () -> configurationAccessor.getConfiguration(id).isPresent(),
            () -> configurationAccessor.deleteConfiguration(id)
        );
    }

}

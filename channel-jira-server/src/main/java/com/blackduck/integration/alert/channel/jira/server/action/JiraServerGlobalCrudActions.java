/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

@Component
public class JiraServerGlobalCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final JiraServerGlobalConfigAccessor configurationAccessor;
    private final JiraServerGlobalConfigurationValidator validator;

    @Autowired
    public JiraServerGlobalCrudActions(
        AuthorizationManager authorizationManager,
        JiraServerGlobalConfigAccessor configurationAccessor,
        JiraServerGlobalConfigurationValidator validator
    ) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
        this.configurationAccessor = configurationAccessor;
        this.validator = validator;
    }

    public ActionResponse<JiraServerGlobalConfigModel> getOne(UUID id) {
        return configurationHelper.getOne(() -> configurationAccessor.getConfiguration(id));
    }

    public ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> getPaged(
        int page, int size, String searchTerm, String sortName, String sortOrder
    ) {
        return configurationHelper.getPage(() -> configurationAccessor.getConfigurationPage(page, size, searchTerm, sortName, sortOrder));
    }

    public ActionResponse<JiraServerGlobalConfigModel> create(JiraServerGlobalConfigModel resource) {
        return configurationHelper.create(
            () -> validator.validate(resource, null),
            () -> configurationAccessor.existsConfigurationByName(resource.getName()),
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<JiraServerGlobalConfigModel> update(UUID id, JiraServerGlobalConfigModel requestResource) {
        return configurationHelper.update(
            () -> validator.validate(requestResource, id.toString()),
            () -> configurationAccessor.existsConfigurationById(id),
            () -> configurationAccessor.updateConfiguration(id, requestResource)
        );
    }

    public ActionResponse<JiraServerGlobalConfigModel> delete(UUID id) {
        return configurationHelper.delete(
            () -> configurationAccessor.existsConfigurationById(id),
            () -> configurationAccessor.deleteConfiguration(id)
        );
    }

}
